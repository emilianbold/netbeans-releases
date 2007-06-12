/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.codegen.java;


import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.netbeans.modules.uml.core.coreapplication.ICodeGenerator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.util.ITaskSupervisor;

import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplate;
import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplatesRetriever;
import org.netbeans.modules.uml.codegen.java.merging.Merger;

public class JavaCodegen implements ICodeGenerator 
{
    public final static String COLON_COLON = "::"; // NOI18N
    public final static String JAVA = "java"; // NOI18N
    public final static String JAVA_EXT = ".java"; // NOI18N
    public final static String DOT = "."; // NOI18N
    public final static String LOG_INDENT = "  ";

    public JavaCodegen()
    {
        DomainTemplatesRetriever.clear();
    }
       
    public void generate(ITaskSupervisor task,
			 List<IElement> elements, 
			 String targetFolderName, 
			 Properties props) 
    {
	boolean backup = Boolean.valueOf(
            props.getProperty("backup", "true")).booleanValue();
        
	boolean genMarkers = Boolean.valueOf(
            props.getProperty("generateMarkers", "true")).booleanValue();

        int errorsCount = 0;

        int total = elements.size();
        
        task.start(total);

        task.log(task.SUMMARY, getBundleMessage("MSG_CodeGenSelectedOptions")); // NOI18N
        task.log(task.SUMMARY, LOG_INDENT + getBundleMessage
		 ("MSG_SourceFolderLocation") + " -  " + targetFolderName); // NOI18N
        task.log(task.SUMMARY, LOG_INDENT + getBundleMessage
		 ("MSG_BackupSources") + " - " + backup); // NOI18N
        task.log(task.SUMMARY, LOG_INDENT + getBundleMessage
		 ("MSG_GenerateMarkers") + " - " + genMarkers); // NOI18N

	task.log(task.SUMMARY, ""); // NOI18N

        int counter = 0;

        for (IElement pElement: elements)
        {
            // has the task been canceled by the user?
            if (!task.proceed(1))
                return;

            counter++;

            task.log(task.TERSE, NbBundle.getMessage(JavaCodegen.class, 
		"MSG_ProcessingElementCounterTotal", // NOI18N
                counter, total) + ": ", false); // NOI18N

            if (pElement == null) {
                task.log(task.TERSE, getBundleMessage("MSG_SkipNullElement")); // NOI18N
		continue;
	    }
	    if ( ! (pElement instanceof IClassifier) ) 
	    {
		task.log(task.TERSE, getBundleMessage("MSG_SkipNotClassifierElement")); // NOI18N
		continue;
	    }

	    IClassifier classifier = (IClassifier)pElement;
	    if (classifier.getName()
                .equalsIgnoreCase(getUnnamedElementPreference()))
            {
                task.log(task.TERSE, getBundleMessage("MSG_SkipUnnamedElement") // NOI18N
                    + getUnnamedElementPreference());
		continue;		
            }
            if (classifier.getName().length() == 0)
            {
                task.log(task.TERSE, getBundleMessage("MSG_SkipUnnamedElement") // NOI18N
                    + getUnnamedElementPreference());
		continue;		
            }

	    task.log(task.TERSE, classifier.getElementType() + " " // NOI18N
		     + classifier.getFullyQualifiedName(false) + " ... ", false);

	    try 
	    {	    	    
		ClassInfo clinfo = new ClassInfo(classifier);
		
		// skip inner class/interface/enumeration elements
		// as they are taken care of by their outer class code gen
		if (clinfo.getOuterClass() != null)
		    continue;
		            
		clinfo.setMethodsAndMembers(classifier);
		clinfo.setExportSourceFolderName(targetFolderName);
		clinfo.setComment(classifier.getDocumentation());
		
		// TODO sourceFile name determination and thus existence 
		//should be moved below to be done based on the templates descs
		File sourceFile = sourceFile(targetFolderName, classifier);
		
		// existing associated source file
		boolean oldAssociated = false;
		String associatedSource = ClassInfo.getSymbolFilename(classifier);
		File ascFile = null;
		
		if (associatedSource != null) 
		{
		    ascFile = new File(associatedSource);
		    
		    if (ascFile.exists() && !ascFile.equals(sourceFile)) 
		    {
			// check that it is under the same targetFolder
			File targetFolderFile = new File(targetFolderName);
			File parent = ascFile.getParentFile();
			
			while(parent != null) 
			{
			    if (parent.equals(targetFolderFile)) 
			    {
				// TBD the file should be parsed and the path to  
				// parent should be compared with package value
				oldAssociated = true;
				break;
			    }	
			    
			    parent = parent.getParentFile();
			} 
		    }
		}
		
		File newSourceFile = null;
		String newTargetFolderName = null;
		File newTargetFolder = null;
		
		if (! sourceFile.exists() && oldAssociated ) 
		{
		    FileUtil.copyFile(FileUtil.toFileObject(ascFile),
				      FileUtil.toFileObject(sourceFile.getParentFile()),
				      sourceFile.getName(), "");
		}
		
		if (sourceFile.exists()) 
		{
		    String tmpdir = System.getProperty("java.io.tmpdir")+"/generated_";
		    
		    newTargetFolderName = new File(tmpdir).getCanonicalPath()
			+((int)(Math.random() * 100000));
		    
		    newTargetFolder = new File(newTargetFolderName);
		    newTargetFolder.mkdirs();		
		    
		    clinfo.setExportSourceFolderName(
		        new File(newTargetFolderName).getAbsolutePath());
                
		    newSourceFile = sourceFile(newTargetFolderName, classifier);
		    
		    if (backup) 
		    {
			if (!oldAssociated) 
			{
			    FileObject buFileObj = backupFile(sourceFile);		
			    
			    if (buFileObj != null) 
			    {
				FileObject efo = FileUtil.toFileObject(sourceFile);
				if (efo != null) 
				    ; //efo.delete();
			    }
			} 
			
			else 
			{
			    FileObject buFileObj = backupFile(ascFile);		
			    
			    if (buFileObj != null) 
			    {
				FileObject efo = FileUtil.toFileObject(ascFile);
				if (efo != null) 
				    efo.delete();
			    }
			}
		    }
		    
		    try 
		    {
			//clinfo.updateFilename(sourceFile.getCanonicalPath());
		    }
		    
		    catch (Exception ex) 
		    {
			ex.printStackTrace();
		    }
		}
                
		// 2 possible places to get templates from - 
		// registry and teplates subdir of the project 
		FileSystem fs = Repository.getDefault().getDefaultFileSystem();
	    
		FileObject root = fs.getRoot()
		    .getFileObject("Templates/UML/CodeGeneration/Java");
		
		String projTemplPath = clinfo.getOwningProject().getBaseDirectory()
		    + File.separator + "templates" + File.separator + "java";
		
		List<DomainTemplate> domainTemplates = DomainTemplatesRetriever
		    .retrieveTemplates(clinfo.getClassElement());
		
		if (domainTemplates == null || domainTemplates.size() == 0)
		{
		    task.log(task.TERSE, getBundleMessage("MSG_ErrorNoTemplatesDefinedForElement")); // NOI18N
		    errorsCount++;
		    continue;
		} else {
		    task.log(task.TERSE, ""); // NOI18N
		}
		
		Iterator<DomainTemplate> iterTemplates = domainTemplates.iterator();
		
		while (iterTemplates.hasNext()) 
		{
		    DomainTemplate domainTemplate = iterTemplates.next();
				    
		    task.log(task.SUMMARY, LOG_INDENT 
			+ NbBundle.getMessage(JavaCodegen.class, "MSG_SourceCodeGenerating", // NOI18N
					      domainTemplate.getTemplateFilename()), false); // NOI18N
		    
		    try 
		    {
			FileObject templteFileObject;
			File templateFile = new File(projTemplPath 
			    + File.separator + domainTemplate.getTemplateFilename());
			
			if (templateFile.exists())
			    templteFileObject = FileUtil.toFileObject(templateFile);
			
			else 
			{
			    templteFileObject = root.getFileObject(
				domainTemplate.getTemplateFilename());
			}
			
			templteFileObject.setAttribute(
                            "javax.script.ScriptEngine", "freemarker");
                    
			DataObject templateDataObject = 
                            DataObject.find(templteFileObject);
                    
			FileObject exportPkgFileObject = 
			    clinfo.getExportPackageFileObject(
			    domainTemplate.getFolderPath());
			
			if (exportPkgFileObject != null) 
			{
			    DataFolder folder = 
				DataFolder.findFolder(exportPkgFileObject);
			    
			    HashMap parameters = new HashMap();
			    parameters.put("classInfo", clinfo);
			    parameters.put("modelElement", classifier);
			    Hashtable codegenOptions = new Hashtable();
			    codegenOptions.put("GENERATE_MARKER_ID", genMarkers);
			    parameters.put("codegenOptions", codegenOptions);
			    
			    DataObject n = templateDataObject.createFromTemplate(
				folder, 
				getOutputName(clinfo.getName(), domainTemplate), 
				parameters);

			    task.log(task.TERSE, " " + getBundleMessage("MSG_OK")); // NOI18N	
		    
			    try 
			    {
				if (newSourceFile != null) 
				{
				    task.log(task.SUMMARY, LOG_INDENT 
					     + getBundleMessage("MSG_SourceCodeMerging"), false); // NOI18N
				    
				    new Merger(newSourceFile.getAbsolutePath(), 
					sourceFile.getAbsolutePath()).merge();
				
				    FileObject targetFolderFO = 
					FileUtil.toFileObject(newTargetFolder);
				    
				    if (targetFolderFO != null) 
					targetFolderFO.delete();
				    
				    task.log(task.TERSE, " " + getBundleMessage("MSG_OK")); // NOI18N
				}
			    } catch (IOException ex) {
				task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileSourceCodeMerging") // NOI18N
				    + ex.getMessage());
				errorsCount++;
				ex.printStackTrace();
			    }

			    try 
			    {
				List<IElement> sourceFiles = 
				    classifier.getSourceFiles();
				
				if (sourceFiles != null) 
				{
				    for (IElement src : sourceFiles) 
				    {
					if (src instanceof ISourceFileArtifact) 
					{
					    classifier.removeSourceFile(
						((ISourceFileArtifact)src)
						.getSourceFile());
					}
				    }
				}
				
				classifier.addSourceFileNotDuplicate(
				    sourceFile.getCanonicalPath());
			    }
			    
			    catch (IOException ex) 
			    {
				ex.printStackTrace();
			    }
			} 
                    
			else 
			{
			    // TBD - couldn't create the package directory for some reason
			    task.log(task.TERSE, getBundleMessage("MSG_ErrorCreatingPackageDir")); // NOI18N				errorsCount++;

			    ;			
			}
		    }
		    
		    catch (Exception e) 
		    {
			task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileSourceCodeGenerating") // NOI18N
				 + e.getMessage());
			errorsCount++;
			e.printStackTrace();		
		    }
		}
	    } 
	    
	    catch (Exception e) 
	    {
		task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileProcessingElement") // NOI18N
                    + e.getMessage());
		errorsCount++;
		e.printStackTrace();		
	    }
	}
	System.out.println("errorsCount="+errorsCount);
	if (errorsCount > 0) {	    
	    task.fail();
	}
    }
    
    
    private String getOutputName(
        String elementName, DomainTemplate domainTemplate)
    {
        String filenameFormat = domainTemplate.getFilenameFormat();
        
        if (filenameFormat != null && filenameFormat.length() > 0)
        {
            elementName = StringUtilities.replaceAllSubstrings(
                filenameFormat, DomainTemplate.ELEMENT_NAME_TOKEN, elementName);
        }
        
        return elementName;
    }


    private File sourceFile(String sourceFolderName, IClassifier classifier)
    {
        File file = null;
        
        String pathName = sourceFolderName + File.separatorChar +
            StringUtilities.replaceAllSubstrings(
                classifier.getFullyQualifiedName(false),
                COLON_COLON, String.valueOf(File.separatorChar)) + JAVA_EXT;
        
        if (pathName != null && pathName.length() > 0)
            file = new File(pathName);            

        return file;
    }
    

    
    private FileObject backupFile(File file)
    {
        String fileName = file.getName();
        String className = fileName.substring(0, fileName.indexOf('.'));
        
        String[] files = file.getParentFile().list(
            new BackupJavaFilesFilter(fileName));
        
        int nextSeqNum = 0;
        
        for (String curName: files)
        {
            String numStr = curName.substring(
                curName.indexOf(JAVA_EXT)+5); // NOI18N
            
            try
            {
                int seqNum = Integer.parseInt(numStr);

                if (seqNum > nextSeqNum)
                    nextSeqNum = seqNum;
            }

            catch (NumberFormatException ex)
            {
                // silently suppress
            }
        }
        
        nextSeqNum++;
        
        try
        {
            FileObject buFileObj = FileUtil.copyFile(
                FileUtil.toFileObject(file),
                FileUtil.toFileObject(file.getParentFile()),
                className,
                JAVA + nextSeqNum);
            
            return buFileObj;
        }
        
        catch (IOException ex)
        {
            // TODO: conover - provide proper handling
            ex.printStackTrace();
            return null;
        }
    }
    
    
    private static class BackupJavaFilesFilter implements java.io.FilenameFilter
    {
        String searchName;
        
        public BackupJavaFilesFilter(String fileName)
        {
            searchName = fileName;
        }
        
        public boolean accept(File dir, String name)
        {
            return name.contains(searchName);
        }
    }

    private static String getBundleMessage(String key)
    {
        return NbBundle.getMessage(JavaCodegen.class, key);
    }

    private String getUnnamedElementPreference()
    {
        return "Unnamed"; // NOI18N
    }

}
