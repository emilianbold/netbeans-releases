/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.codegen.java;


import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

import org.netbeans.modules.uml.core.coreapplication.ICodeGenerator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.REIntegrationUtil;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.util.ITaskSupervisor;
import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplate;
import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplatesRetriever;
import org.netbeans.modules.uml.codegen.java.merging.FileBuilder;
import org.netbeans.modules.uml.codegen.java.merging.Merger;
import org.netbeans.modules.uml.codegen.java.merging.Merger.ParsedInfo;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.util.StringTokenizer2;
import org.openide.util.Exceptions;

public class JavaCodegen implements ICodeGenerator 
{
    public final static String COLON_COLON = "::"; // NOI18N
    public final static String JAVA = "java"; // NOI18N
    public final static String JAVA_EXT = ".java"; // NOI18N
    public final static String DOT = "."; // NOI18N
    public final static String LOG_INDENT = "  "; // NOI18N
    public final static String SEP = "/"; // NOI18N
    public final static String TILDE = "~"; // NOI18N

    public JavaCodegen()
    {
        DomainTemplatesRetriever.clear();
    }
       
    public void generate(ITaskSupervisor task,
        List<IElement> elements, String targetFolderName, Properties props)
    {
        boolean backup = Boolean.valueOf(
            props.getProperty("backup", "true")).booleanValue(); // NOI18N

        boolean genMarkers = Boolean.valueOf(
            props.getProperty("generateMarkers", "true")).booleanValue(); // NOI18N

        boolean showGCDialog = Boolean.valueOf(
            props.getProperty("showGCDialog", "true")).booleanValue(); // NOI18N

        int errorsCount = 0;
        int total = elements.size();

        task.start(total);
        task.log(task.SUMMARY, getBundleMessage("MSG_CodeGenSelectedOptions")); // NOI18N

        task.log(task.SUMMARY, LOG_INDENT +
            getBundleMessage("MSG_SourceFolderLocation") + " - " + targetFolderName); // NOI18N

        task.log(task.SUMMARY, LOG_INDENT +
            getBundleMessage("MSG_BackupSources") + " - " + backup); // NOI18N

        task.log(task.SUMMARY, LOG_INDENT +
            getBundleMessage("MSG_GenerateMarkers") + " - " + genMarkers); // NOI18N

        task.log(task.SUMMARY, LOG_INDENT +
            getBundleMessage("MSG_ShowGCDialog") + " - " + showGCDialog); // NOI18N

        task.log(task.SUMMARY, ""); // NOI18N

        if (targetFolderName == null || targetFolderName.length() == 0)
        {
            task.log(task.TERSE, getBundleMessage("MSG_EmptyTargetSourcePath")); // NOI18N
            task.fail();
        }
        
        else
        {
            FileObject targetSrcFolderFO = 
                FileUtil.toFileObject(new File(targetFolderName));

            if (targetSrcFolderFO == null || !targetSrcFolderFO.isValid())
            {
                task.log(task.TERSE, getBundleMessage(
                    "MSG_TargetSourcePathNotValid", targetFolderName));

                task.fail();
            }
        }
        
        String tempGenerationTargetDir = null;
        int counter = 0;
        ScriptEngineManager mgr = new ScriptEngineManager();
        HashMap<FileObject, ScriptEngine> engines = new  HashMap<FileObject, ScriptEngine>();

        ClassInfo.eraseRefClasses();

        for (IElement pElement : elements)
        {
            // has the task been canceled by the user?
            if (!task.proceed(1))
                return;

            counter++;

            task.log(task.TERSE, NbBundle.getMessage(JavaCodegen.class,
                "MSG_ProcessingElementCounterTotal", // NOI18N
                counter, total) + ": ", false); // NOI18N

            if (pElement == null)
            {
                task.log(task.TERSE, getBundleMessage("MSG_SkipNullElement")); // NOI18N
                continue;
            }

            if (!(pElement instanceof IClassifier))
            {
                task.log(task.TERSE, getBundleMessage("MSG_SkipNotClassifierElement",
                    ((INamedElement) pElement).getName())); // NOI18N
                continue;
            }

            IClassifier classifier = (IClassifier) pElement;
            if (classifier.getName().equalsIgnoreCase(getUnnamedElementPreference()))
            {
                task.log(task.TERSE, getBundleMessage("MSG_SkipUnnamedElement") +
                    getUnnamedElementPreference());
                continue;
            }

            if (classifier.getName().length() == 0)
            {
                task.log(task.TERSE, getBundleMessage("MSG_SkipUnnamedElement") + 
                    getUnnamedElementPreference());
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

                FileObject root = fs.getRoot().getFileObject(
                    DomainTemplatesRetriever.TEMPLATES_BASE_FOLDER); // NOI18N

                String projTemplPath = clinfo.getOwningProject().getBaseDirectory() 
                    + File.separator + "templates" + File.separator + "java"; // NOI18N

                List<DomainTemplate> domainTemplates = DomainTemplatesRetriever
                    .retrieveTemplates(clinfo.getClassElement());

                if (domainTemplates == null || domainTemplates.size() == 0)
                {
                    task.log(task.TERSE, getBundleMessage(
                        "MSG_ErrorNoTemplatesDefinedForElement")); // NOI18N
                    
                    errorsCount++;
                    continue;
                }
                
                else
                    task.log(task.TERSE, ""); // NOI18N

                Iterator<DomainTemplate> iterTemplates = domainTemplates.iterator();

                while (iterTemplates.hasNext())
                {
                    try
                    {
                        DomainTemplate domainTemplate = iterTemplates.next();
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
                        
                        FileMapping fmap = new FileMapping();
                        fmap.templateFileObject = templteFileObject;
                        fmap.domainTemplate = domainTemplate;

                        FileObject exportPkgFileObject =
                            clinfo.getExportPackageFileObject(
                                domainTemplate.getFolderPath());

                        if (exportPkgFileObject != null)
                        {
                            String ext = domainTemplate.getExtension();
                            if (ext == null)
                                ext = templteFileObject.getExt();

                            if (ext != null)
                            {
                                if (ext.startsWith("."))
                                    ext = ext.substring(1);

                                fmap.ext = ext;
                            }

                            // lets check for existing source 
                            String extAdd = (fmap.ext != null ? "." + fmap.ext : "");
                            
                            String targetPackageFolderPath = FileUtil.toFile(
                                exportPkgFileObject).getCanonicalPath();
                            
                            String targName = getOutputName(
                                clinfo.getName(), domainTemplate) + extAdd;
                            
                            String targetFilePath = 
                                targetPackageFolderPath + SEP + targName;
                            
                            fmap.targetFilePath = targetFilePath;
                            File targetFile = new File(targetFilePath);
                            targetFiles.add(targetFile);

                            String charset = REIntegrationUtil.getEncoding(targetFile.getCanonicalPath());
                            if (charset == null) {
                                charset = REIntegrationUtil.getEncoding(targetPackageFolderPath);
                            }
                            fmap.charset = charset;

                            if (targetFile.exists())
                            {
                                fmap.existingSourcePath = targetFile.getCanonicalPath();
                                FileObject buFileObj = backupFile(targetFile);
                                if (buFileObj != null)
                                {
                                    fmap.existingSourceBackupPath = FileUtil.
                                        toFile(buFileObj).getCanonicalPath();
                                }
                                
                                ParsedInfo existingFileInfo = 
                                    merger.parse(targetFilePath, charset);
                                
                                if (existingFileInfo != null)
                                {
                                    fmap.existingFileInfo = existingFileInfo;
                                    fmap.merge = true;
                                    genToTmp = true;
                                }
                                
                                String exName = new File(
                                    targetFile.getCanonicalPath()).getName();
                                
                                if (!exName.equals(targName))
                                {
                                    FileObject trgFO = FileUtil.toFileObject(targetFile);

                                    if (trgFO != null)
                                        trgFO.delete();
                                }
                            }
                        
                            else
                                checkAsc = true;
                        }
                        
                        else
                        {
                            // TBD - couldn't create the package directory for some reason
                            task.log(task.TERSE, 
                                getBundleMessage("MSG_ErrorCreatingPackageDir")); // NOI18N	
                            
                            errorsCount++;
                            continue;
                        }
                        
                        fmappings.add(fmap);
                    }
                    
                    catch (IOException ex)
                    {
                        String excMsg = ex.getMessage();
                        
                        if (excMsg == null)
                            excMsg = ex.getClass().getName();
                        
                        task.log(task.TERSE, getBundleMessage(
                            "MSG_ErrorWhileProcessingElement") + " " + excMsg); // NOI18N
                        
                        errorsCount++;
                        ex.printStackTrace();
                    }
                }

                Hashtable<String, ParsedInfo> ascfiles = 
                    new Hashtable<String, ParsedInfo>();
                
                if (checkAsc)
                {
                    List<IElement> sourceFiles = classifier.getSourceFiles();
                    if (sourceFiles != null)
                    {
                        for (IElement src : sourceFiles)
                        {
                            if (!(src instanceof ISourceFileArtifact))
                                continue;
                            
                            ParsedInfo ascInfo = null;
                            
                            String fileName = ((ISourceFileArtifact)src).getSourceFile();
                            File ascFile = new File(fileName);
                            
                            if (targetFiles.contains(ascFile))
                                continue;
                            
                            try {
                                if (!inSubdir(new File(targetFolderName), ascFile))
                                    continue;

                                ascInfo = merger.parse(ascFile.getCanonicalPath(),
                                                       REIntegrationUtil.getEncoding(ascFile.getCanonicalPath()));
                            } 
                            catch (IOException iox)   
                            {          
                                task.log(task.SUMMARY, LOG_INDENT 
                                         + getBundleMessage("MSG_ErrorAccessingExistingSource", 
                                                            fileName)); // NOI18N
                            }

                            if (ascInfo == null)
                                continue;
                            
                            if (!inCorrectPackageSubdir(
                                new File(targetFolderName), ascFile, ascInfo))
                            {
                                continue;
                            }
                            
                            List<String> ids = ascInfo.getDefinitiveClassIds();
                            if (ids == null)
                                continue;
                            
                            for (String id : ids)
                            {
                                if (id != null)
                                    ascfiles.put(id, ascInfo);
                            }
                        }
                    }
                }

                if (genToTmp) 
                {
                    if (tempGenerationTargetDir == null) 
                    {
                        tempGenerationTargetDir = getTempGenerationTargetDir();
                    }
                    clinfo.setExportSourceFolderName(tempGenerationTargetDir);
                }

                for (FileMapping fmap : fmappings)
                {
                    DomainTemplate domainTemplate = fmap.domainTemplate;

                    task.log(task.SUMMARY, LOG_INDENT + NbBundle.getMessage(
                        JavaCodegen.class, "MSG_SourceCodeGenerating", // NOI18N
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
                                FileUtil.toFileObject(new File(
                                    fmap.existingSourcePath)).delete();
                            }

                            HashMap<String, Object> parameters = 
                                new HashMap<String, Object>();

                            parameters.put("classInfo", clinfo); // NOI18N
                            parameters.put("modelElement", classifier); // NOI18N

                            Hashtable<String, Object> codegenOptions = 
                                new Hashtable<String, Object>();

                            codegenOptions.put("GENERATE_MARKER_ID", genMarkers); // NOI18N
                            parameters.put("codegenOptions", codegenOptions); // NOI18N

                            FileObject templFO = fmap.templateFileObject;
                            
                            ScriptEngine engine = engines.get(templFO);
                            if (engine == null) 
                            {
                                String engineID = null;
                                Object value = templFO.getAttribute("javax.script.ScriptEngine");
                        
                                if (value instanceof String) 
                                {
                                    engineID = (String) value;
                                }
                                if (engineID == null || engineID.length() == 0) 
                                {
                                    engineID = "freemarker";
                                }
                                engine = mgr.getEngineByName(engineID);
                                if (engine == null) 
                                {
                                    task.log(task.TERSE, getBundleMessage(
                                        "MSG_ErrorNonExistingScriptingEngine", engineID)); // NOI18N
                                    errorsCount++;
                                    continue;
                                }
                                engines.put(templFO, engine);
                            }

                            engine.getContext().getBindings(
                                ScriptContext.ENGINE_SCOPE).clear();

                            engine.getContext().setAttribute(
                                FileObject.class.getName(), 
                                templFO, ScriptContext.ENGINE_SCOPE);

                            engine.getContext().getBindings(
                                ScriptContext.ENGINE_SCOPE).putAll(parameters);

                            String pathto = FileUtil.toFile(exportPkgFileObject)
                                .getCanonicalPath() + SEP + getOutputName(
                                clinfo.getName(), domainTemplate) + (
                                fmap.ext != null ? "." + fmap.ext : "");

                            File fileto = new File(pathto);
                            Writer os = null;
                            if (fmap.charset != null && java.nio.charset.Charset.isSupported(fmap.charset) ) 
                            {
                                os = new OutputStreamWriter(new FileOutputStream(fileto), fmap.charset);
                            } 
                            else
                            {
                                os = new OutputStreamWriter(new FileOutputStream(fileto));
                            }
                            Writer w = new BufferedWriter(os);
                            engine.getContext().setWriter(w);
                            
                            InputStreamReader is = new InputStreamReader(
                                templFO.getInputStream());

                            engine.eval(is);
                            is.close();
                            w.close();

                            if (fileto.exists())
                            {
                                fmap.generatedFilePath = 
                                    fileto.getCanonicalPath();

                                task.log(task.TERSE, " " + 
                                    getBundleMessage("MSG_OK")); // NOI18N	
                            }

                            else
                            {
                                task.log(task.TERSE, getBundleMessage(
                                    "MSG_ErrorWhileSourceCodeGenerating"));

                                errorsCount++;
                            }
                        }

                        else
                        {
                            // TBD - couldn't create the package directory for some reason
                            task.log(task.TERSE, getBundleMessage(
                                "MSG_ErrorCreatingPackageDir")); // NOI18N	

                            errorsCount++;
                            continue;
                        }
                    }
                    
                    catch (Exception e)
                    {
                        task.log(task.TERSE, getBundleMessage(
                            "MSG_ErrorWhileSourceCodeGenerating") // NOI18N
                            + " " + e.getMessage());
                        errorsCount++;
                        e.printStackTrace();
                        continue;
                    }


                    try
                    {
                        if (!(fmap.existingSourcePath == null && ascfiles.size() == 0))
                        {
                            fmap.generatedFileInfo = merger.parse(fmap.generatedFilePath, fmap.charset);
                            
                            if (fmap.existingSourcePath != null)
                            {
                                task.log(task.SUMMARY, LOG_INDENT + 
                                    getBundleMessage("MSG_ExistingSource") // NOI18N
                                    + " -  " + fmap.existingSourcePath);

                                if (fmap.existingFileInfo != null)
                                {
                                    if (fmap.generatedFileInfo != null)
                                    {
                                        task.log(task.SUMMARY, LOG_INDENT + 
                                            getBundleMessage("MSG_SourceCodeMerging"), false); // NOI18N
                                        
                                        merger.merge(fmap.generatedFileInfo,
                                            fmap.generatedFilePath,
                                            fmap.existingFileInfo,
                                            fmap.existingSourceBackupPath,
                                            fmap.targetFilePath);
                                        
                                        task.log(task.TERSE, " " + 
                                            getBundleMessage("MSG_OK")); // NOI18N
                                    }
                                    else
                                    {
                                        fmap.merge = false;
                                        
                                        task.log(task.SUMMARY, LOG_INDENT + 
                                            getBundleMessage("MSG_GeneratedSourceNotParseableOverwritten")); // NOI18N
                                        
                                        continue;
                                    }
                                }
                                
                                else
                                {
                                    task.log(task.SUMMARY, LOG_INDENT + 
                                        getBundleMessage("MSG_ExistingSourceNotParseableOverwritten")); // NOI18N
                                    
                                    continue;
                                }
                            }
                            else
                            {
                                ParsedInfo ascInfo = extractById(ascfiles, fmap.generatedFileInfo);
                                if (ascInfo != null)
                                {
                                    fmap.existingSourcePath = ascInfo.getFilePath();
                                    if (backup)
                                    {
                                        FileObject buFileObj = backupFile(
                                            new File(ascInfo.getFilePath()));
                                        
                                        fmap.existingSourceBackupPath = 
                                            FileUtil.toFile(buFileObj).getCanonicalPath();
                                    }
                                    
                                    fmap.merge = true;
                                    task.log(task.SUMMARY, LOG_INDENT + 
                                        getBundleMessage("MSG_ExistingSource") // NOI18N
                                        + " -  " + ascInfo.getFilePath());
                                    
                                    task.log(task.SUMMARY, LOG_INDENT + 
                                        getBundleMessage("MSG_SourceCodeMerging"), false); // NOI18N
                                    
                                    merger.merge(fmap.generatedFileInfo,
                                        fmap.generatedFilePath,
                                        ascInfo,
                                        ascInfo.getFilePath(),
                                        fmap.targetFilePath);
                                    
                                    task.log(task.TERSE, " " + 
                                        getBundleMessage("MSG_OK")); // NOI18N
                                }
                            }
                        }
                    }

                    catch (IOException ex)
                    {
                        task.log(task.TERSE, getBundleMessage(
                            "MSG_ErrorWhileSourceCodeMerging") + " " + // NOI18N
                            ex.getMessage());
                        
                        errorsCount++;
                        ex.printStackTrace();
                    }
                }

                boolean theSameSet = true;
                int i = 0;
                List<IElement> sourceFiles = classifier.getSourceFiles();

                if (sourceFiles == null || fmappings.size() != sourceFiles.size()) 
                {
                    theSameSet = false;
                }
                for (FileMapping fmap : fmappings)                    
                {
                    if (genToTmp)
                    {
                        if (!fmap.merge)
                        {
                            FileBuilder.copyFile(new File(fmap.generatedFilePath),
                                new File(fmap.targetFilePath));
                        }
                    }
                    
                    if (fmap.existingSourcePath != null)
                    {
                        File exf = new File(fmap.existingSourcePath);
                        FileObject fo = null;
                        if (!exf.equals(new File(fmap.targetFilePath)))
                        {
                            fo = FileUtil.toFileObject(exf);
                            if (fo != null)
                            {
                                fo.delete();
                            }
                        }
                        else
                        {

                            if ((!backup) && (fmap.merge) && 
                                (fmap.existingSourceBackupPath != null))
                            {
                                fo = FileUtil.toFileObject(new File(fmap.existingSourceBackupPath));
                                if (fo != null)
                                {
                                    fo.delete();
                                }
                            }
                        }
                    }
                    
                    if (theSameSet) 
                    {                        
                        IElement src = sourceFiles.get(i);
                        if (src instanceof ISourceFileArtifact)
                        {
                            String srcPath = ((ISourceFileArtifact) src).getFileName();
                            String canonicalSrcPath = null;
                            try 
                            {
                                canonicalSrcPath = new File(srcPath).getCanonicalPath();
                            }
                            catch (IOException iox)   
                            {                                
                                task.log(task.SUMMARY, LOG_INDENT 
                                         + getBundleMessage("MSG_ErrorAccessingExistingSource", 
                                                            srcPath)); // NOI18N
                            }                            
                            if (! (new File(fmap.targetFilePath).getCanonicalPath()
                                   .equals(canonicalSrcPath))) 
                            {
                                theSameSet = false;
                            }
                        }
                        else 
                        {
                            theSameSet = false;
                        }
                    }
                    i++;
                }
                    
                if (! theSameSet) 
                {
                    sourceFiles = classifier.getSourceFiles();
                
                    if (sourceFiles != null)
                    {
                        for (IElement src : sourceFiles)
                        {
                            if (src instanceof ISourceFileArtifact)
                            {
                                classifier.removeSourceFile((
                                    (ISourceFileArtifact) src).getSourceFile());
                            }
                        }
                    }

                    for (FileMapping fmap : fmappings)
                    {                                     
                        classifier.addSourceFileNotDuplicate(fmap.targetFilePath);
                    }
                }

                if (genToTmp) 
                {
                    for (FileMapping fmap : fmappings)
                    {                                     
                        deleteDirs(tempGenerationTargetDir, fmap.generatedFilePath); 
                    }                    
                }
            }
            
            catch (Exception e)
            {
                String excMsg = e.getMessage();
                
                if (excMsg == null)
                    excMsg = e.getClass().getName();
                
                task.log(task.TERSE, getBundleMessage(
                    "MSG_ErrorWhileProcessingElement") + " " + excMsg); // NOI18N
                    
                errorsCount++;
                e.printStackTrace();
            }

            ClassInfo.eraseRefClass(classifier);
        }
        
        if (errorsCount > 0)
            task.fail();
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
	FileUtil.createFolder(newTargetFolder);
	return newTargetFolder.getCanonicalPath();
    }

    private void deleteDirs(String topParent, String path) 
    {               
        File topDir = new File(topParent);
        if (path == null) 
        {
            return;
        }
        File cur = new File(path);
        if (! inSubdir(topDir, cur)) 
        {
            return;
        }
        File pp = topDir.getParentFile();
        if (pp == null || pp.equals(topDir)) 
        {
            return;
        }
        boolean last = false;
        FileObject fo = null;
        while(true) 
        {
            if (cur.exists()) 
            {
                fo = FileUtil.toFileObject(cur);
                FileObject[] children = fo.getChildren();
                if (children == null || children.length == 0) 
                {
                    try {
                        fo.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } 
                else 
                {
                    return;
                }
            }
            cur = cur.getParentFile();
            if (last || cur == null) 
            {
                return;
            }
            if (topDir.equals(cur)) 
            {
                last = true;
            }
        }
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

    private static String getBundleMessage(String key, String eleName)
    {
        return NbBundle.getMessage(JavaCodegen.class, key, eleName);
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
	public FileObject templateFileObject = null;
	public DomainTemplate domainTemplate = null;
        public String charset = null;
    }

}
