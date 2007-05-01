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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.netbeans.modules.uml.core.coreapplication.ICodeGenerator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Operation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;

import org.netbeans.modules.uml.codegen.java.merging.Merger;


public class JavaCodegen implements ICodeGenerator {

    public final static String COLON_COLON = "::"; // NOI18N
    public final static String JAVA = "java"; // NOI18N
    public final static String JAVA_EXT = ".java"; // NOI18N
    public final static String DOT = "."; // NOI18N
    

    // templates map TBD of config GUI & API integration 
    public static HashMap<String, TemplateDesc[]> stereotypesToTemplates = new HashMap<String, TemplateDesc[]>();

    static {
	// dummy test config
	stereotypesToTemplates.put("webservice", 
				   new TemplateDesc[] {new TemplateDesc("webservice.ftl", "java"),
						       new TemplateDesc("wsdl.ftl", "xml")});
	stereotypesToTemplates.put("st2", new TemplateDesc[] {new TemplateDesc("st2.ftl", "java")});
    }


    public JavaCodegen(){

    }

    
    public void generate(IClassifier classifier, String targetFolderName, boolean backup) {

	// temporary detour to not mess up with codegen now,
	// will be refactored back soon
	if (System.getProperty("uml.codegen.merge") != null) {
	    generate_merge(classifier, targetFolderName, backup); 
	    return;
	}


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
	    if (sourceFile.exists()) {

		if (backup) {
		    FileObject buFileObj = backupFile(sourceFile);		
		    if (buFileObj != null) {
			FileObject efo = FileUtil.toFileObject(sourceFile);
			if (efo != null) 
			    efo.delete();
		    }
		}		
		try {
		    clinfo.updateFilename(sourceFile.getCanonicalPath());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    } 
                
	    // 2 possible places to get templates from - 
	    // registry and teplates subdir of the project 
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	    FileObject root = fs.getRoot().getFileObject("Templates/UML/CodeGeneration/Java");
	    String projTemplPath = clinfo.getOwningProject().getBaseDirectory()+File.separator+"templates"+File.separator+"java";

	    List<TemplateDesc> templateDescs = templatesToUse(clinfo);
	    Iterator<TemplateDesc> iterDescs = templateDescs.iterator();
	    while(iterDescs.hasNext()) {
		TemplateDesc templDesc = iterDescs.next();	    		
		try {
		    FileObject tfo;
		    File tf = new File(projTemplPath + File.separator + templDesc.templateName);
		    if (tf.exists()) {
			tfo = FileUtil.toFileObject(tf);
		    } else {
			tfo = root.getFileObject(templDesc.templateName);
		    }
		    
		    tfo.setAttribute("javax.script.ScriptEngine", "freemarker");
		    DataObject obj = DataObject.find(tfo);
	    		    
		    FileObject dfo = clinfo.getExportPackageFileObject();
		    if (dfo != null) {
		
			DataFolder folder = DataFolder.findFolder(dfo);
			//Map parameters = Collections.singletonMap("classInfo", clinfo);
			HashMap parameters = new HashMap();
			parameters.put("classInfo", clinfo);
			parameters.put("modelElement", classifier);
			DataObject n = obj.createFromTemplate(folder, clinfo.getName(), parameters);

			try {
			    // TBD codegen inteface returning associative map 
			    // (classifier, generated files) that makes sense in that type 
			    // of codegen; 
			    // the codegen client to decide what type of sources / of what codegen, 
			    // to associate, if any, with the element
			    List<IElement> sourceFiles =  classifier.getSourceFiles();
			    if (sourceFiles != null) {
				for(IElement src : sourceFiles) {
				    if (src instanceof ISourceFileArtifact) {
					classifier.removeSourceFile(((ISourceFileArtifact)src).getSourceFile());
				    }
				}
			    }
			    classifier.addSourceFileNotDuplicate(sourceFile.getCanonicalPath());
			} catch (IOException ex) {
			    ex.printStackTrace();
			}
		    } else {
			// TBD - couldn't create the package directory for some reason
			;			
		    }
		} catch (Exception e) {
		    e.printStackTrace();		
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();		
	}

    }
    
    public void generate_merge(IClassifier classifier, String targetFolderName, boolean backup) {

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
	    File newSourceFile = null;
	    String newTargetFolderName = null;
	    if (sourceFile.exists()) {

		newTargetFolderName = "/tmp/generated_"+((int)(Math.random() * 10000));
		new File(newTargetFolderName).mkdirs();		
		clinfo.setExportSourceFolderName(new File(newTargetFolderName).getAbsolutePath());
		newSourceFile = sourceFile(newTargetFolderName, classifier);

		if (backup) {
		    FileObject buFileObj = backupFile(sourceFile);		
		    if (buFileObj != null) {
			FileObject efo = FileUtil.toFileObject(sourceFile);
			if (efo != null) 
			    ; //efo.delete();
		    }
		}		
		try {
		    //clinfo.updateFilename(sourceFile.getCanonicalPath());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    } 
                
	    // 2 possible places to get templates from - 
	    // registry and teplates subdir of the project 
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	    FileObject root = fs.getRoot().getFileObject("Templates/UML/CodeGeneration/Java");
	    String projTemplPath = clinfo.getOwningProject().getBaseDirectory()+File.separator+"templates"+File.separator+"java";

	    List<TemplateDesc> templateDescs = templatesToUse(clinfo);
	    Iterator<TemplateDesc> iterDescs = templateDescs.iterator();
	    while(iterDescs.hasNext()) {
		TemplateDesc templDesc = iterDescs.next();	    		
		try {
		    FileObject tfo;
		    File tf = new File(projTemplPath + File.separator + templDesc.templateName);
		    if (tf.exists()) {
			tfo = FileUtil.toFileObject(tf);
		    } else {
			tfo = root.getFileObject(templDesc.templateName);
		    }
		    
		    tfo.setAttribute("javax.script.ScriptEngine", "freemarker");
		    DataObject obj = DataObject.find(tfo);
	    		    
		    FileObject dfo = clinfo.getExportPackageFileObject();
		    if (dfo != null) {
		
			DataFolder folder = DataFolder.findFolder(dfo);
			//Map parameters = Collections.singletonMap("classInfo", clinfo);
			HashMap parameters = new HashMap();
			parameters.put("classInfo", clinfo);
			parameters.put("modelElement", classifier);
			DataObject n = obj.createFromTemplate(folder, clinfo.getName(), parameters);

			if (newSourceFile != null) {
			    new Merger(newSourceFile.getAbsolutePath(), sourceFile.getAbsolutePath()).merge();
			}

			try {
			    // TBD codegen inteface returning associative map 
			    // (classifier, generated files) that makes sense in that type 
			    // of codegen; 
			    // the codegen client to decide what type of sources / of what codegen, 
			    // to associate, if any, with the element
			    List<IElement> sourceFiles =  classifier.getSourceFiles();
			    if (sourceFiles != null) {
				for(IElement src : sourceFiles) {
				    if (src instanceof ISourceFileArtifact) {
					classifier.removeSourceFile(((ISourceFileArtifact)src).getSourceFile());
				    }
				}
			    }
			    classifier.addSourceFileNotDuplicate(sourceFile.getCanonicalPath());
			} catch (IOException ex) {
			    ex.printStackTrace();
			}
		    } else {
			// TBD - couldn't create the package directory for some reason
			;			
		    }
		} catch (Exception e) {
		    e.printStackTrace();		
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();		
	}

    }
    


    private File sourceFile(String sourceFolderName, IClassifier classifier)
    {
        File file = null;
        
        String pathName = sourceFolderName + File.separatorChar +
            StringUtilities.replaceAllSubstrings(
                classifier.getFullyQualifiedName(false),
                COLON_COLON, String.valueOf(File.separatorChar)) + JAVA_EXT;
        
        if (pathName != null && pathName.length() > 0)
        {
            file = new File(pathName);            
        }        
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
    

    public List<TemplateDesc> templatesToUse(ClassInfo clinfo) {
	
	List<TemplateDesc> tlist = new ArrayList<TemplateDesc>();

	String stereoName = null;
	IClassifier classElem = clinfo.getClassElement();
	List<Object> stereotypes = classElem.getAppliedStereotypes();
	if ( stereotypes != null && stereotypes.size() > 0 ) {	
	    Iterator iter = stereotypes.iterator();
	    while (iter.hasNext()) {
		IStereotype stereo = (IStereotype)iter.next();
		String name = stereo.getName();	    	    
		TemplateDesc[] templates = stereotypesToTemplates.get(name);
		if (templates != null) {
		    for (int i = 0; i < templates.length; i++) {
			tlist.add(templates[i]);
		    }
		    return tlist;
		}
	    }
	}
	// if nothing found
	TemplateDesc templDef = new TemplateDesc("CompilationUnit.java", "java");
	tlist.add(templDef);
	return tlist;

    }
    

    public static class TemplateDesc {
	public String templateName;
	public String generatedExt;
	
	public TemplateDesc( String name, String extension ) {
	    templateName = name;
	    generatedExt = extension;
	}

    }









}



