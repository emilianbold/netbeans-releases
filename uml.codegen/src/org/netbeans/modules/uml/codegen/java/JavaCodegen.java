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
import org.netbeans.modules.uml.codegen.java.merging.FileBuilder;
import org.netbeans.modules.uml.codegen.java.merging.Merger;
import org.netbeans.modules.uml.codegen.java.merging.Merger.ParsedInfo;
import org.netbeans.modules.uml.util.StringTokenizer2;

public class JavaCodegen implements ICodeGenerator 
{
    public final static String COLON_COLON = "::"; // NOI18N
    public final static String JAVA = "java"; // NOI18N
    public final static String JAVA_EXT = ".java"; // NOI18N
    public final static String DOT = "."; // NOI18N
    public final static String LOG_INDENT = "  ";
    public final static String SEP = "/"; //System.getProperty("file.separator"); // NOI18N
    public final static String TILDE = "~";

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
            props.getProperty("backup", "true")).booleanValue(); // NOI18N
        
	boolean genMarkers = Boolean.valueOf(
            props.getProperty("generateMarkers", "true")).booleanValue(); // NOI18N

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

	String tempTemplatesDirName = null;
	FileObject tempTemplatesDirFO = null;
	
	ClassInfo.eraseRefClasses();
	
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
                + classifier.getFullyQualifiedName(false) + " ... ", false); // NOI18N

	    try 
	    {	    	    
		ClassInfo clinfo = new ClassInfo(classifier);
		
		// skip inner class/interface/enumeration elements
		// as they are taken care of by their outer class code gen
		if (clinfo.getOuterClass() != null)
		{
		    task.log(task.TERSE, ""); // NOI18N
		    continue;
		}  
		clinfo.setMethodsAndMembers(classifier);
		clinfo.setExportSourceFolderName(targetFolderName);
		clinfo.setComment(classifier.getDocumentation());
		
		boolean checkAsc = false;
		boolean genToTmp = false;
		ArrayList<FileMapping> fmappings = new ArrayList<FileMapping>();
		HashSet<File> targetFiles = new HashSet<File>();
		Merger merger = new Merger(props);

		// 2 possible places to get templates from - 
		// registry and teplates subdir of the project 
		FileSystem fs = Repository.getDefault().getDefaultFileSystem();
	    
		FileObject root = fs.getRoot()
		    .getFileObject(DomainTemplatesRetriever.TEMPLATES_BASE_FOLDER); // NOI18N
		
		String projTemplPath = clinfo.getOwningProject().getBaseDirectory()
		    + File.separator + "templates" + File.separator + "java"; // NOI18N
		
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
		    try {
		        DomainTemplate domainTemplate = iterTemplates.next();
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
                            "javax.script.ScriptEngine", "freemarker"); // NOI18N
                    
			DataObject templateDataObject = 
                            DataObject.find(templteFileObject);
                    
			FileMapping fmap = new FileMapping();
			fmap.templateDataObject = templateDataObject;
			fmap.domainTemplate = domainTemplate;
			    
			FileObject exportPkgFileObject = 
			    clinfo.getExportPackageFileObject(
			    domainTemplate.getFolderPath());
			
			if (exportPkgFileObject != null) {

			    // lets check for existing source 
			    String templExt = templateDataObject.getPrimaryFile().getExt();
			    String extAdd = domainTemplate.getExtension();
			    if (extAdd != null && extAdd.length() > 0) 
			    {
				String ext = extAdd;
				if (ext.startsWith(".")) 
				{
				    ext = ext.substring(1);
				}				
				if (! ext.equals(templExt)) 
				{
				    fmap.ext = ext;
				}
			    } 
			    else
			    {
				extAdd = "." + templExt; // NOI18N
			    }
			    String targetPackageFolderPath = FileUtil.toFile(exportPkgFileObject).getCanonicalPath();
			    String targName = getOutputName(clinfo.getName(), domainTemplate) + extAdd;
			    String targetFilePath = targetPackageFolderPath + SEP + targName;
			    fmap.targetFilePath = targetFilePath;
			    File targetFile = new File(targetFilePath);
			    targetFiles.add(targetFile);
			    if (targetFile.exists()) 
			    {
				fmap.existingSourcePath = targetFile.getCanonicalPath();
				FileObject buFileObj = backupFile(targetFile);
				if (buFileObj != null) 
				{
				    fmap.existingSourceBackupPath = FileUtil.toFile(buFileObj).getCanonicalPath();
				}
				ParsedInfo existingFileInfo = merger.parse(targetFilePath);
				if (existingFileInfo != null)
				{
				    fmap.existingFileInfo = existingFileInfo;
				    fmap.merge = true;
				    genToTmp = true;
				} 		
				String exName = new File(targetFile.getCanonicalPath()).getName();
				if (!exName.equals(targName)) {
				    FileObject trgFO = FileUtil.toFileObject(targetFile);
				    if (trgFO != null) 
				    {
					trgFO.delete();
				    }
				}
			    } 
			    else 
			    {
				checkAsc = true;
			    }
			} 
			else 
			{
			    // TBD - couldn't create the package directory for some reason
			    task.log(task.TERSE, getBundleMessage("MSG_ErrorCreatingPackageDir")); // NOI18N	
			    errorsCount++;
			    continue;
			}
			fmappings.add(fmap);
		    }
		    catch (IOException ex) 
		    {
			String excMsg = ex.getMessage();
			if (excMsg == null) 
			{
			    excMsg = ex.getClass().getName();
			}
			task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileProcessingElement") // NOI18N
				 + " " + excMsg); // NOI18N
			errorsCount++;
			ex.printStackTrace();
		    }					    
		} 
		
		Hashtable<String, ParsedInfo> ascfiles = new Hashtable<String, ParsedInfo>();
		if (checkAsc)
		{
		    List<IElement> sourceFiles = classifier.getSourceFiles();		    
		    if (sourceFiles != null) 
		    {
			for (IElement src : sourceFiles) 
			{
			    if (! (src instanceof ISourceFileArtifact) )
			    {
				continue;
			    }
			    File ascFile = new File(((ISourceFileArtifact)src).getSourceFile());
			    if (targetFiles.contains(ascFile)) 
			    {
				continue;
			    }
			    if (! inSubdir(new File(targetFolderName), ascFile))
			    {
				continue;
			    }
			    
			    ParsedInfo ascInfo = merger.parse(ascFile.getCanonicalPath());
			    if (ascInfo == null) 
			    {
				continue;
			    }
			    if (! inCorrectPackageSubdir(new File(targetFolderName), ascFile, ascInfo)) 
			    {
				continue;
			    }
			    List<String> ids = ascInfo.getDefinitiveClassIds();
			    if (ids == null) 
			    {
				continue;
			    }
			    for(String id : ids) 
			    {
				if (id != null) 
				{ 
				    ascfiles.put(id, ascInfo);
				}				
			    }
			}
		    }	    
		}

		if (genToTmp) 
		{
		    clinfo.setExportSourceFolderName(getTempGenerationTargetDir());
		} 

		for(FileMapping fmap : fmappings)
		{ 
		    DomainTemplate domainTemplate = fmap.domainTemplate;	    
		    DataObject templateDataObject = fmap.templateDataObject;
			
		    task.log(task.SUMMARY, LOG_INDENT 
			+ NbBundle.getMessage(JavaCodegen.class, "MSG_SourceCodeGenerating", // NOI18N
					      domainTemplate.getTemplateFilename()), false); // NOI18N
		    try
		    {
			FileObject exportPkgFileObject = 
			    clinfo.getExportPackageFileObject(
		            domainTemplate.getFolderPath());
			
			if (exportPkgFileObject != null) 
			{
			    if (!genToTmp && fmap.existingSourcePath != null) 
			    { 
				FileUtil.toFileObject(new File(fmap.existingSourcePath)).delete(); 
			    }

			    DataFolder folder = 
				DataFolder.findFolder(exportPkgFileObject);
			    
			    HashMap parameters = new HashMap();
			    parameters.put("classInfo", clinfo); // NOI18N
			    parameters.put("modelElement", classifier); // NOI18N
			    Hashtable codegenOptions = new Hashtable();
			    codegenOptions.put("GENERATE_MARKER_ID", genMarkers); // NOI18N
			    parameters.put("codegenOptions", codegenOptions); // NOI18N
			    
			    DataObject n = templateDataObject.createFromTemplate(
				folder, 
				getOutputName(clinfo.getName(), domainTemplate), 
				parameters);

			    FileObject genedFO = n.getPrimaryFile();
			    if (genedFO != null) 
			    {
				String genedPath = FileUtil.toFile(genedFO).getCanonicalPath();
				String genedExt = genedFO.getExt();
				if (fmap.ext != null && ! fmap.ext.equals(genedExt)) 
				{ 				    
				    if (genedPath.endsWith("."+genedExt)) // really expected to 
				    {
					int l = ("."+genedExt).length();
					genedPath = genedPath.substring(0, genedPath.length() - l) 
					    + "." + fmap.ext;
					File genedFile = FileUtil.toFile(genedFO);
					if (genedFile != null) 
					{
					    genedFile.renameTo(new File(genedPath));
					}
				    }
				}
				fmap.generatedFilePath = genedPath;
				task.log(task.TERSE, " " + getBundleMessage("MSG_OK")); // NOI18N	
			    }	
			    else 
			    {
				task.log(task.TERSE, 
					 getBundleMessage("MSG_ErrorWhileSourceCodeGenerating"));
				errorsCount++;
			    }
			}
			else 
			{
			    // TBD - couldn't create the package directory for some reason
			    task.log(task.TERSE, getBundleMessage("MSG_ErrorCreatingPackageDir")); // NOI18N	
			    errorsCount++;
			    continue;
			}
		    }
		    catch (Exception e) 
		    {
			task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileSourceCodeGenerating") // NOI18N
				 + " " + e.getMessage());
			errorsCount++;
			e.printStackTrace();
			continue;
		    }

		    try 
		    {
			if ( ! (fmap.existingSourcePath == null && ascfiles.size() == 0) )
			{
			    fmap.generatedFileInfo = merger.parse(fmap.generatedFilePath);
			    if (fmap.existingSourcePath != null)  
			    {
				task.log(task.SUMMARY, LOG_INDENT 
					 + getBundleMessage("MSG_ExistingSource") // NOI18N
					 + " -  " + fmap.existingSourcePath); 
				    
				if (fmap.existingFileInfo != null) 
				{
				    if (fmap.generatedFileInfo != null) 
				    {
					task.log(task.SUMMARY, LOG_INDENT 
						 + getBundleMessage("MSG_SourceCodeMerging"), false); // NOI18N
					merger.merge(fmap.generatedFileInfo, 
						     fmap.generatedFilePath,
						     fmap.existingFileInfo, 
						     fmap.existingSourceBackupPath, 
						     fmap.targetFilePath);    
					task.log(task.TERSE, " " + getBundleMessage("MSG_OK")); // NOI18N
				    } 
				    else 
				    {
					fmap.merge = false;
					task.log(task.SUMMARY, LOG_INDENT 
						 + getBundleMessage("MSG_GeneratedSourceNotParseableOverwritten")); // NOI18N
					continue;
				    }
				} 
				else 
				{
				    task.log(task.SUMMARY, LOG_INDENT 
					+ getBundleMessage("MSG_ExistingSourceNotParseableOverwritten")); // NOI18N
				    continue;
				}
			    } 
			    else 
			    {
				ParsedInfo ascInfo = extractById(ascfiles, fmap.generatedFileInfo);
				if (ascInfo != null) {
				    fmap.existingSourcePath = ascInfo.getFilePath();
				    if (backup) 
				    {
					FileObject buFileObj 
					    = backupFile(new File(ascInfo.getFilePath()));		
					fmap.existingSourceBackupPath = FileUtil.toFile(buFileObj).getCanonicalPath();
				    }
				    fmap.merge = true;
				    task.log(task.SUMMARY, LOG_INDENT 
					     + getBundleMessage("MSG_ExistingSource") // NOI18N
					     + " -  " + ascInfo.getFilePath()); 
				    task.log(task.SUMMARY, LOG_INDENT 
					     + getBundleMessage("MSG_SourceCodeMerging"), false); // NOI18N
				    merger.merge(fmap.generatedFileInfo, 
						 fmap.generatedFilePath,
						 ascInfo, 
						 ascInfo.getFilePath(), 
						 fmap.targetFilePath);     				    
				    task.log(task.TERSE, " " + getBundleMessage("MSG_OK")); // NOI18N
				}
			    } 

			}
		    
		    } 
		    catch (IOException ex) 
		    {
			task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileSourceCodeMerging") // NOI18N
				 + " " + ex.getMessage());
			errorsCount++;
			ex.printStackTrace();
		    }			
			
		}

		List<IElement> sourceFiles = 
		    classifier.getSourceFiles();		
		if (sourceFiles != null) 
		{
		    for (IElement src : sourceFiles) 
		    {
			if (src instanceof ISourceFileArtifact) 
			{
			    classifier.removeSourceFile(((ISourceFileArtifact)src)
							.getSourceFile());
			}
		    }
		}
	     
	    		    
		for(FileMapping fmap : fmappings)
		{ 
		    if (genToTmp) 
		    {
			if (! fmap.merge) 
			{
			    FileBuilder.copyFile(new File(fmap.generatedFilePath),
						 new File(fmap.targetFilePath));
			}		    
		    }
		    if (fmap.existingSourcePath != null) 
		    {
			File exf = new File(fmap.existingSourcePath);
			if (! exf.equals(new File(fmap.targetFilePath))) 
			{
			    exf.delete();
			}
			else 
			{
			    
			    if ( (! backup) && (fmap.merge) 
				 && (fmap.existingSourceBackupPath != null)) 
			    {
				new File(fmap.existingSourceBackupPath).delete();
			    }
			}
		    }
		    classifier.addSourceFileNotDuplicate(fmap.targetFilePath);
		}			    
	    } 	    
	    catch (Exception e) 
	    {
		String excMsg = e.getMessage();
		if (excMsg == null) 
		{
		    excMsg = e.getClass().getName();
		}
		task.log(task.TERSE, getBundleMessage("MSG_ErrorWhileProcessingElement") // NOI18N
                    + " " + excMsg);
		errorsCount++;
		e.printStackTrace();		
	    }	    
	    ClassInfo.eraseRefClass(classifier);
	}
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
    

    private ParsedInfo extractById(Hashtable<String, ParsedInfo> ascfiles, ParsedInfo pinfo) {
	List<String> ids = pinfo.getDefinitiveClassIds();
	if (ids == null) 
	{
	    return null;
	}
	for(String id : ids) 
	{
	    ParsedInfo res = ascfiles.get(id);
	    if (res != null) 
	    {
		ascfiles.remove(id);
		Set<String> keys = ascfiles.keySet();
		if (keys != null) 
		{
		    for(String k : keys) 
		    {
			ParsedInfo pi = ascfiles.get(k);
			if (res.equals(pi)) 
			{
			   ascfiles.remove(k);
			}
		    }
		}
		return res;
	    }
	}
	return null;
    }

    private String getTempGenerationTargetDir() 
	throws IOException
    {		    
	String tmpdir = System.getProperty("java.io.tmpdir")+"/generated_"; // NOI18N
	String trg = new File(tmpdir).getCanonicalPath()
	    +((int)(Math.random() * 100000));
	
	File newTargetFolder = new File(trg);
	newTargetFolder.mkdirs();
	return newTargetFolder.getCanonicalPath();
    }

    
    private boolean inCorrectPackageSubdir(File dir, File guessedChild, Merger.ParsedInfo info)
	throws IOException
    {
	String pack = info.getPackageName();
	String packPath = ""; // NOI18N
	if (pack != null) {
	    packPath = pack.replaceAll("\\.", SEP); // NOI18N
	}
	File packDir = new File(dir.getCanonicalPath() + SEP + packPath);
	File parent = guessedChild.getParentFile();
	return packDir.equals(parent);			
    }

   
    private boolean inSubdir(File dir, File guessedChild) {	
	File parent = guessedChild;
	while(parent != null) 
	{
	    if (parent.equals(dir)) 
	    {
		return true;
	    }	
	    parent = parent.getParentFile();
	}
	return false;
    } 
    
    
    private FileObject backupFile(File file)
    {
        try
        {
	    String fileName = new File(file.getCanonicalPath()).getName();
	    String className;
	    String ext = ""; 
	    int ind = fileName.indexOf('.');
	    if (ind > -1) {
		className = fileName.substring(0, ind);
		if (ind < fileName.length()) 
		{
		    ext = fileName.substring(ind + 1);
		}
	    } 
	    else 
	    {
		className = fileName;
	    }	    
	    String[] files = file.getParentFile()
		.list(new BackupJavaFilesFilter(fileName));
        
	    int nextSeqNum = 0;
        
	    for (String curName: files)
	    {
		String numStr = StringTokenizer2.replace(
		    curName.substring(fileName.length()),
		    TILDE, ""); // NOI18N
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
        
            FileObject buFileObj = FileUtil.copyFile(
                FileUtil.toFileObject(file),
                FileUtil.toFileObject(file.getParentFile()),
                className,
                ext + nextSeqNum + TILDE);
            
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
            return name.toLowerCase().contains(searchName.toLowerCase());
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


    public static class FileMapping {

	public String targetFilePath = null;
	public String ext = null;
	public String generatedFilePath = null;
	public ParsedInfo generatedFileInfo = null;
	public String existingSourcePath = null;
	public String existingSourceBackupPath = null;
	public ParsedInfo existingFileInfo = null;
	public boolean merge = false;
	public DataObject templateDataObject = null;
	public DomainTemplate domainTemplate = null;
    }

}
