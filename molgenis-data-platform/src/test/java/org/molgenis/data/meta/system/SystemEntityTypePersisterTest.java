package org.molgenis.data.meta.system;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.test.AbstractMockitoTest;

class SystemEntityTypePersisterTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private SystemEntityTypeRegistry systemEntityTypeRegistry;
  @Mock private EntityTypeDependencyResolver entityTypeDependencyResolver;
  @Mock private SystemPackageRegistry systemPackageRegistry;
  @Mock private MutableAclClassService mutableAclClassService;

  private SystemEntityTypePersister systemEntityTypePersister;

  @Mock private MetaDataService metaDataService;

  @BeforeEach
  void setUpBeforeMethod() {
    systemEntityTypePersister =
        new SystemEntityTypePersister(
            dataService,
            systemEntityTypeRegistry,
            entityTypeDependencyResolver,
            systemPackageRegistry,
            mutableAclClassService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void removeNonExistingSystemEntities() {
    when(dataService.getMeta()).thenReturn(metaDataService);
    Package systemPackage = mock(Package.class);
    when(systemPackage.getId()).thenReturn(PACKAGE_SYSTEM);

    EntityType refRemovedMeta =
        when(mock(EntityType.class).getId()).thenReturn("refRemoved").getMock();
    when(refRemovedMeta.getPackage()).thenReturn(systemPackage);

    EntityType removedMeta = when(mock(EntityType.class).getId()).thenReturn("removed").getMock();
    when(removedMeta.getPackage()).thenReturn(systemPackage);

    EntityType refEntityType =
        when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();
    when(refEntityType.getPackage()).thenReturn(systemPackage);

    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
    when(entityType.getPackage()).thenReturn(systemPackage);

    doReturn(true).when(systemEntityTypeRegistry).hasSystemEntityType("entity");
    doReturn(true).when(systemEntityTypeRegistry).hasSystemEntityType("refEntity");

    Repository<EntityType> repository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1))
                  .accept(asList(refEntityType, entityType, refRemovedMeta, removedMeta));
              return null;
            })
        .when(repository)
        .forEachBatched(any(Fetch.class), any(), eq(1000));

    systemEntityTypePersister.removeNonExistingSystemEntityTypes();
    verify(metaDataService).deleteEntityTypes(asList("refRemoved", "removed"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void persistSystemPackageChange() {
    RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
    when(metaDataService.getDefaultBackend()).thenReturn(defaultRepoCollection);
    when(dataService.getMeta()).thenReturn(metaDataService);
    Repository<EntityType> repository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1)).accept(emptyList());
              return null;
            })
        .when(repository)
        .forEachBatched(any(Fetch.class), any(), eq(1000));

    when(systemEntityTypeRegistry.getSystemEntityTypes()).thenAnswer(invocation -> Stream.empty());

    String packageId0 = "packageId0";
    String packageName0 = "packageName0";
    SystemPackage package0 =
        when(mock(SystemPackage.class).getId()).thenReturn(packageName0).getMock();
    when(package0.getId()).thenReturn(packageId0);
    String packageId1 = "packageId1";
    String packageName1 = "packageName1";
    SystemPackage package1 =
        when(mock(SystemPackage.class).getId()).thenReturn(packageName1).getMock();
    when(package1.getId()).thenReturn(packageId1);
    when(systemPackageRegistry.getSystemPackages()).thenReturn(Stream.of(package0, package1));
    doAnswer(invocation -> Stream.of(package0)).when(dataService).findAll(PACKAGE, Package.class);
    systemEntityTypePersister.persist();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(metaDataService).upsertPackages(captor.capture());
    assertEquals(newArrayList(package0, package1), captor.getValue().collect(toList()));
  }

  // regression test for https://github.com/molgenis/molgenis/issues/5168
  @SuppressWarnings("unchecked")
  @Test
  void persistSystemPackageNoChange() {
    RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
    when(metaDataService.getDefaultBackend()).thenReturn(defaultRepoCollection);
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(systemEntityTypeRegistry.getSystemEntityTypes()).thenAnswer(invocation -> Stream.empty());
    Repository<EntityType> repository = mock(Repository.class);
    when(dataService.getRepository(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(repository);
    doAnswer(
            invocation -> {
              ((Consumer<List<Entity>>) invocation.getArgument(1)).accept(emptyList());
              return null;
            })
        .when(repository)
        .forEachBatched(any(Fetch.class), any(), eq(1000));

    String packageId0 = "packageId0";
    String packageName0 = "packageName0";
    SystemPackage package0 =
        when(mock(SystemPackage.class).getId()).thenReturn(packageName0).getMock();
    when(package0.getId()).thenReturn(packageId0);
    String packageId1 = "packageId1";
    String packageName1 = "packageName1";
    SystemPackage package1 =
        when(mock(SystemPackage.class).getId()).thenReturn(packageName1).getMock();
    when(package1.getId()).thenReturn(packageId1);
    when(systemPackageRegistry.getSystemPackages()).thenReturn(Stream.of(package0, package1));
    doAnswer(invocation -> Stream.of(package0, package1))
        .when(dataService)
        .findAll(PACKAGE, Package.class);
    systemEntityTypePersister.persist();
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Package>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(metaDataService).upsertPackages(captor.capture());
    assertEquals(newArrayList(package0, package1), captor.getValue().collect(toList()));
  }
}
