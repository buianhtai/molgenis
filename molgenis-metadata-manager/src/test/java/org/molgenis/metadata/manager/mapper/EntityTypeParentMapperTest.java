package org.molgenis.metadata.manager.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.metadata.manager.model.EditorAttributeIdentifier;
import org.molgenis.metadata.manager.model.EditorEntityTypeParent;

class EntityTypeParentMapperTest {
  @Mock private AttributeReferenceMapper attributeReferenceMapper;

  @Mock private EntityTypeMetadata entityTypeMetadata;

  @Mock private DataService dataService;

  private EntityTypeParentMapper entityTypeParentMapper;

  @BeforeEach
  void setUpBeforeMethod() {
    MockitoAnnotations.initMocks(this);
    entityTypeParentMapper =
        new EntityTypeParentMapper(attributeReferenceMapper, entityTypeMetadata, dataService);
  }

  @Test
  void testEntityTypeParentMapper() {
    assertThrows(NullPointerException.class, () -> new EntityTypeParentMapper(null, null, null));
  }

  @Test
  void testToEntityTypeReference() {
    String id = "id";
    String label = "label";
    @SuppressWarnings("unchecked")
    List<EditorAttributeIdentifier> attributes = mock(List.class);
    EditorEntityTypeParent parent = mock(EditorEntityTypeParent.class);
    EditorEntityTypeParent editorEntityTypeParent =
        EditorEntityTypeParent.create(id, label, attributes, parent);
    EntityType entityType = entityTypeParentMapper.toEntityTypeReference(editorEntityTypeParent);
    assertEquals(id, entityType.getIdValue());
  }

  @Test
  void testToEntityTypeReferenceNull() {
    assertNull(entityTypeParentMapper.toEntityTypeReference(null));
  }

  @Test
  void testToEditorEntityTypeParent() {
    String parentId = "parentId";
    String parentLabel = "parentLabel";
    EntityType parentEntityType = mock(EntityType.class);
    when(parentEntityType.getId()).thenReturn(parentId);
    when(parentEntityType.getLabel()).thenReturn(parentLabel);
    @SuppressWarnings("unchecked")
    Iterable<Attribute> parentAttributes = mock(Iterable.class);
    when(parentEntityType.getOwnAllAttributes()).thenReturn(parentAttributes);

    String id = "id";
    String label = "label";
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(id);
    when(entityType.getLabel()).thenReturn(label);
    @SuppressWarnings("unchecked")
    Iterable<Attribute> attributes = mock(Iterable.class);
    when(entityType.getOwnAllAttributes()).thenReturn(attributes);
    when(entityType.getExtends()).thenReturn(parentEntityType);

    @SuppressWarnings("unchecked")
    ImmutableList<EditorAttributeIdentifier> editorAttributes = mock(ImmutableList.class);
    when(attributeReferenceMapper.toEditorAttributeIdentifiers(attributes))
        .thenReturn(editorAttributes);
    @SuppressWarnings("unchecked")
    ImmutableList<EditorAttributeIdentifier> parentEditorAttributes = mock(ImmutableList.class);
    when(attributeReferenceMapper.toEditorAttributeIdentifiers(parentAttributes))
        .thenReturn(parentEditorAttributes);

    EditorEntityTypeParent editorEntityTypeParent =
        entityTypeParentMapper.toEditorEntityTypeParent(entityType);
    EditorEntityTypeParent expectedEditorEntityTypeParent =
        EditorEntityTypeParent.create(
            id,
            label,
            editorAttributes,
            EditorEntityTypeParent.create(parentId, parentLabel, parentEditorAttributes, null));
    assertEquals(expectedEditorEntityTypeParent, editorEntityTypeParent);
  }

  @Test
  void testToEditorEntityTypeParentNull() {
    assertNull(entityTypeParentMapper.toEditorEntityTypeParent(null));
  }
}
