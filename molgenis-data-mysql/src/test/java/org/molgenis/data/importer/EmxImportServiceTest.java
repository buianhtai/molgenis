package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.AppConfig;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.semantic.UntypedTagService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration tests for the entire EMX importer.
 */
@Test
@ContextConfiguration(classes = AppConfig.class)
public class EmxImportServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection store;

	@Autowired
	DataServiceImpl dataService;

	@Autowired
	PermissionSystemService permissionSystemService;

	@Autowired
	MetaDataServiceImpl metaDataService;

	@Autowired
	UntypedTagService tagService;

	@BeforeMethod
	public void beforeMethod()
	{
		if (dataService.hasRepository("import_person"))
		{
			dataService.deleteAll("import_person");
		}
		if (dataService.hasRepository("import_city"))
		{
			dataService.deleteAll("import_city");
		}
	}

	@Test
	public void testValidationReport() throws IOException, InvalidFormatException, URISyntaxException
	{
		// open test source
		File f = ResourceUtils.getFile(getClass(), "/example_invalid.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		// create importer
		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService), dataService);

		// generate report
		EntitiesValidationReport report = importer.validateImport(f, source);

		// SheetsImportable
		AssertJUnit.assertEquals(report.getSheetsImportable().size(), 4);
		Assert.assertTrue(report.getSheetsImportable().get("import_person"));
		Assert.assertTrue(report.getSheetsImportable().get("import_city"));
		Assert.assertFalse(report.getSheetsImportable().get("unknown_entity"));
		Assert.assertFalse(report.getSheetsImportable().get("unknown_fields"));

		// FieldsAvailable
		AssertJUnit.assertEquals(report.getFieldsAvailable().size(), 2);
		AssertJUnit.assertEquals(report.getFieldsAvailable().get("import_person").size(), 1);
		AssertJUnit.assertEquals(report.getFieldsAvailable().get("import_city").size(), 0);
		AssertJUnit.assertTrue(report.getFieldsAvailable().get("import_person").contains("otherAttribute"));

		// FieldsImportable
		AssertJUnit.assertEquals(report.getFieldsImportable().size(), 2);
		AssertJUnit.assertEquals(report.getFieldsImportable().get("import_person").size(), 6);
		AssertJUnit.assertTrue(report.getFieldsImportable().get("import_person").contains("firstName"));
		AssertJUnit.assertFalse(report.getFieldsImportable().get("import_person").contains("unknownField"));

		// FieldsUnknown
		AssertJUnit.assertEquals(report.getFieldsUnknown().size(), 2);
		AssertJUnit.assertEquals(report.getFieldsUnknown().get("import_person").size(), 1);
		AssertJUnit.assertTrue(report.getFieldsUnknown().get("import_person").contains("unknownField"));
		AssertJUnit.assertEquals(report.getFieldsUnknown().get("import_city").size(), 0);

		// FieldsRequired missing
		AssertJUnit.assertEquals(report.getFieldsRequired().size(), 2);
		AssertJUnit.assertEquals(report.getFieldsRequired().get("import_person").size(), 1);
		AssertJUnit.assertTrue(report.getFieldsRequired().get("import_person").contains("birthday"));
		AssertJUnit.assertEquals(report.getFieldsRequired().get("import_city").size(), 0);
	}

	@Test
	public void testImportReport() throws IOException, InvalidFormatException, InterruptedException
	{
		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		Assert.assertEquals(source.getNumberOfSheets(), 4);
		Assert.assertNotNull(source.getRepository("attributes"));

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService), dataService);

		// test import
		EntityImportReport report = importer.doImport(source, DatabaseAction.ADD);
		
		List<Entity> entities = (List<Entity>) StreamSupport
				.stream(dataService.getRepository("import_person").spliterator(), false)
				.filter(e -> e.getEntities("children").iterator().hasNext())
				.collect(Collectors.toCollection(ArrayList::new));
		Assert.assertEquals(new Integer(entities.size()), new Integer(1));
		Iterator<Entity> children = entities.get(0).getEntities("children").iterator();
		Assert.assertEquals(children.next().getIdValue(), "john");
		Assert.assertEquals(children.next().getIdValue(), "jane");

		// test report
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(2));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(3));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(3));

	}

	@Test
	public void testImportReportNoMeta() throws IOException, InvalidFormatException, InterruptedException
	{

		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService), dataService);

		// test import
		importer.doImport(source, DatabaseAction.ADD);

		// create test excel
		File file_no_meta = ResourceUtils.getFile(getClass(), "/example_no_meta.xlsx");
		ExcelRepositoryCollection source_no_meta = new ExcelRepositoryCollection(file_no_meta);

		// test import
		EntityImportReport report = importer.doImport(source_no_meta, DatabaseAction.ADD);

		AssertJUnit.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(4));
		AssertJUnit.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(4));

	}
}
