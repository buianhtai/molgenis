package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.TabixReader;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.MolgenisSimpleSettings;
import org.molgenis.vcf.VcfReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class performs a system call to cross reference a chromosome and genomic location with a tabix indexed file. A
 * match can result in 1, 2 or 3 hits. These matches are reduced to one based on a reference and alternative nucleotide
 * base. The remaining hit will be used to parse two CADD scores.
 * </p>
 * 
 * <p>
 * <b>CADD returns:</b> CADD score Absolute, CADD score Scaled
 * </p>
 * 
 * @author mdehaan
 * 
 * */
@Component("caddService")
public class CaddServiceAnnotator extends VariantAnnotator
{
	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	static final String CADD_SCALED = "CADDSCALED";
	static final String CADD_ABS = "CADDABS";

	private static final String NAME = "CADD";
	
	final List<String> infoFields = Arrays.asList(new String[]{
			"##INFO=<ID="+CADD_SCALED+",Number=1,Type=Float,Description=\"CADD scaled C score, ie. phred-like. See Kircher et al. 2014 (http://www.ncbi.nlm.nih.gov/pubmed/24487276) or CADD website (http://cadd.gs.washington.edu/) for more information.\">",
			"##INFO=<ID="+CADD_ABS+",Number=1,Type=Float,Description=\"CADD absolute C score, ie. unscaled SVM output. Useful as  reference when the scaled score may be unexpected.\">"
	});

	public static final String CADD_FILE_LOCATION_PROPERTY = "cadd_location";
	TabixReader tr;

	@Autowired
	public CaddServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;

		tr = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
	}

	public CaddServiceAnnotator(File caddTsvGzFile, File inputVcfFile, File outputVCFFile)
			throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(CADD_FILE_LOCATION_PROPERTY, caddTsvGzFile.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		tr = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();
		
		//VcfReader vcfReader= new VcfReader();
		
		
		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, CADD_SCALED);
		
        System.out.println("Now starting to process the data.");
		
		while (vcfIter.hasNext())
		{
			Entity record = vcfIter.next();
			
		//	System.out.println("record, before:");
		//	System.out.println(record.toString());

			List<Entity> annotatedRecord = annotateEntity(record);
			
		//	System.out.println("record, annotated:");
		//	System.out.println(annotatedRecord.toString());

			if (annotatedRecord.size() > 1)
			{
				outputVCFWriter.close();
				vcfRepo.close();
				throw new Exception("Multiple outputs for " + record.toString());
			}
			else if (annotatedRecord.size() == 0)
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(record));
			}
			else
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord.get(0)));
			}
		}
		outputVCFWriter.close();
		vcfRepo.close();
		System.out.println("All done!");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public boolean annotationDataExists()
	{
		return new File(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		List<Entity> results = new ArrayList<Entity>();

		//FIXME need to solve this! duplicate notation for CHROM in VcfRepository.CHROM and LocusAnnotator.CHROMOSOME
		String chromosome = entity.getString(VcfRepository.CHROM) != null ? entity.getString(VcfRepository.CHROM) : entity.getString(CHROMOSOME);
		
		Long position = entity.getLong(POSITION); //FIXME use VcfRepository.POS ?
		String reference = entity.getString(REFERENCE); //FIXME use VcfRepository.REF ?
		String alternative = entity.getString(ALTERNATIVE); //FIXME use VcfRepository.ALT ?

		String caddAbs = null;
		String caddScaled = null;

		String next;
		TabixReader.Iterator caddIterator = tr.query(chromosome + ":" + position + "-" + position);
		while (caddIterator != null && (next = caddIterator.next()) != null)
		{
			String[] split = next.split("\t", -1);

			if (split[2].equals(reference) && split[3].equals(alternative))
			{
				caddAbs = split[4];
				caddScaled = split[5];
			}
			// In some cases, the ref and alt are swapped. If this is the case,
			// the initial if statement above will
			// fail, we can just check whether such a swapping has occured
			else if (split[3].equals(reference) && split[2].equals(alternative))
			{
				caddAbs = split[4];
				caddScaled = split[5];
			}
			// If both matchings are incorrect, there is something really wrong
			// with the source files,
			// which we cant do anything about.
			else
			{
				//
			}
		}

		HashMap<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put(CADD_ABS, caddAbs);
		resultMap.put(CADD_SCALED, caddScaled);
		resultMap.put(CHROMOSOME, chromosome);
		resultMap.put(POSITION, position);
		resultMap.put(ALTERNATIVE, alternative);
		resultMap.put(REFERENCE, reference);

		results.add(new MapEntity(resultMap));

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL));

		return metadata;
	}

}
