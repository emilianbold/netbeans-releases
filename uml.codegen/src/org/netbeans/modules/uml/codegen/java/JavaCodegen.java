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
import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplate;
import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplatesRetriever;
import org.netbeans.modules.uml.codegen.java.merging.Merger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.netbeans.modules.uml.core.coreapplication.ICodeGenerator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;


public class JavaCodegen implements ICodeGenerator 
{
    public final static String COLON_COLON = "::"; // NOI18N
    public final static String JAVA = "java"; // NOI18N
    public final static String JAVA_EXT = ".java"; // NOI18N
    public final static String DOT = "."; // NOI18N

    public JavaCodegen()
    {
        DomainTemplatesRetriever.clear();
    }

       
    public void generate(
        IClassifier classifier, String targetFolderName, Properties props) 
    {
	boolean backup = Boolean.valueOf(
            props.getProperty("backup", "true")).booleanValue();
        
	boolean genMarkers = Boolean.valueOf(
            props.getProperty("generateMarkers", "true")).booleanValue();
        
	try {	    
	    
	    ClassInfo clinfo = new ClassInfo(classifier);

	    // skip inner class/interface/enumeration elements
	    // as they are taken care of by their outer class code gen
	    if (clinfo.getOuterClass() != null)
		return;
        
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
		//String tmpdir = "/tmp/generated_";
		String tmpdir = System.getProperty("java.io.tmpdir")+"/generated_";
	
                newTargetFolderName = new File(tmpdir).getCanonicalPath()
		    +((int)(Math.random() * 10000));
		
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
                return;
            
	    Iterator<DomainTemplate> iterTemplates = domainTemplates.iterator();

            while (iterTemplates.hasNext()) 
            {
		DomainTemplate domainTemplate = iterTemplates.next();
                
		try 
                {
		    FileObject templteFileObject;
		    File templateFile = new File(projTemplPath + 
                        File.separator + domainTemplate.getTemplateFilename());
                    
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

			if (newSourceFile != null) 
                        {
                            
                            new Merger(newSourceFile.getAbsolutePath(), 
                                sourceFile.getAbsolutePath()).merge();
                            
			    FileObject targetFolderFO = 
                                FileUtil.toFileObject(newTargetFolder);
			    
                            if (targetFolderFO != null) 
				targetFolderFO.delete();
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
			;			
		    }
		}
                
                catch (Exception e) 
                {
		    e.printStackTrace();		
		}
	    }
	} 
        
        catch (Exception e) 
        {
	    e.printStackTrace();		
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

}
