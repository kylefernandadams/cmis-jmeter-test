package org.alfresco.jmeter.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.jmeter.util.CmisHelper;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class GetDocumentContentStream implements JavaSamplerClient{
    private static final Logger logger = LoggingManager.getLoggerForClass();
    
    private CmisHelper cmis;
    private Folder parentFolder = null;


	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("cmis.username", "${cmis.username}");
        defaultParameters.addArgument("cmis.password", "${cmis.password}");
        defaultParameters.addArgument("cmis.url", "${cmis.protocol}://${cmis.host}:${cmis.port}/${cmis.url.context}");	
        defaultParameters.addArgument("cmis.folder.path", "${cmis.folder.path}");

        
		return defaultParameters;
	}

	public SampleResult runTest(JavaSamplerContext runTestContext) {
		String cmisFolderPath = runTestContext.getParameter("cmis.folder.path");
		
		SampleResult result = new SampleResult();
		result.sampleStart();
		try{
			String rootFolder = cmis.getRootFolder();
	        logger.info("Successfully retrieved root folder.");
	        
	        boolean exists = cmis.folderExistsAtPath(rootFolder + cmisFolderPath);
	        logger.info("Folder exists at path? " + exists);

	        
	        if(!exists){
	        	String folderPath = cmis.createFolder(rootFolder, cmisFolderPath);
	            logger.info("Folder created with path: " + folderPath);
	            
	            logger.info("Expecting: " + rootFolder + cmisFolderPath);
	        }
	        else{
	        	parentFolder = (Folder) cmis.getObjectByPath(rootFolder + cmisFolderPath);
	        	logger.info("Successfully retrieved folder: " + parentFolder.getPath());
	        }

	        Document document = (Document) cmis.getFirstChild(parentFolder);
	        logger.info("Successfully retrieved document: " + document.getName());
	        
	        ContentStream contentStream = document.getContentStream();
	        String contentAsString = cmis.getContentAsString(contentStream);
	        logger.info("Successfully retrieved content stream with length: " + contentAsString.length() + " and mimetype: " + contentStream.getMimeType());
	        
	        result.sampleEnd();
	        result.setSuccessful(true);
	        result.setResponseMessage("Successfully retrieved cmis document content stream.");
	        result.setResponseCodeOK();
	        
		}
		catch(Exception e){
			result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage("Failed to retrieved cmis document content stream: " + e);
            
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace( new PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString().getBytes());
            result.setDataType(SampleResult.TEXT);
            result.setResponseCode("500");
		}
			
        return result;
	}

	public void setupTest(JavaSamplerContext setupContext) {
		String username = setupContext.getParameter("cmis.username");
		String password = setupContext.getParameter("cmis.password");
		String cmisUrl = setupContext.getParameter("cmis.url");

        cmis = new CmisHelper(username, password, cmisUrl);
	}

	public void teardownTest(JavaSamplerContext teardownContext) {
		
		
	}
}
