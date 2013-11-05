package org.molgenis.genomebrowser.controller;

import static org.molgenis.genomebrowser.controller.GenomebrowserController.URI;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.genomebrowser.services.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class GenomebrowserController extends MolgenisPluginController
{
	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM   = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS   = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
	
	public static final String URI = "/plugin/genomebrowser";
	private final MolgenisSettings molgenisSettings;
	public MutationService mutationService;
	
	@Autowired
	public GenomebrowserController(MolgenisSettings molgenisSettings, MutationService service)
	{
		super(URI);
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		this.molgenisSettings = molgenisSettings;
		this.mutationService = service;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{	
		model.addAttribute(INITLOCATION, molgenisSettings.getProperty(INITLOCATION));
		model.addAttribute(COORDSYSTEM, molgenisSettings.getProperty(COORDSYSTEM));  
		model.addAttribute(CHAINS, molgenisSettings.getProperty(CHAINS));
		model.addAttribute(SOURCES, molgenisSettings.getProperty(SOURCES));
		model.addAttribute(BROWSERLINKS, molgenisSettings.getProperty(BROWSERLINKS));  
		model.addAttribute(SEARCHENDPOINT, molgenisSettings.getProperty(SEARCHENDPOINT));
		model.addAttribute(KARYOTYPEENDPOINT, molgenisSettings.getProperty(KARYOTYPEENDPOINT));

		return "view-genomebrowser";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/data", produces = {MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody
	JsonArray getAll(HttpServletResponse response, @RequestParam(value = "mutation", required = false) String mutationId,
			@RequestParam(value = "segment", required = true) String segmentId) throws ParseException, DatabaseException, IOException
	{
		if(mutationId==null){
			mutationId="";
		}
		return mutationService.getPatientMutationData(segmentId,mutationId);
	}
}