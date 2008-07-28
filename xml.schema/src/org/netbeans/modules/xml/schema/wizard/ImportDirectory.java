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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.schema.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.xml.retriever.*;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.RetrieverEngine;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author girix
 */
public final class ImportDirectory implements Runnable{
    
    private String importRoot;
    
    private File toDir;
    
    private Thread myThread;
    
    private boolean overWriteFiles = false;
    
    private DocumentTypesEnum docType;
    
    InfoCollector infoCollector = null;
    
    private void initOPTab(){
        InputOutput io = IOProvider.getDefault().getIO(opTabTitle, false);
        OutputWriter optab = io.getOut();
        io.select();
        try{
            optab.reset();
        }catch (Exception e){
            //don't care
        }
    }
    
    /** Creates a new instance of ImportDirectory */
    public ImportDirectory(String importRoot, File toDir) {
        //default to schema
        this(importRoot, toDir, false, DocumentTypesEnum.schema);
    }
    
    /** Creates a new instance of ImportDirectory */
    public ImportDirectory(String importRoot, File toDir, boolean overWriteFiles, DocumentTypesEnum type) {
        this.importRoot = importRoot;
        this.toDir = toDir;
        this.overWriteFiles = overWriteFiles;
        this.docType = type;
        initOPTab();
        start();
    }
    
    
    public void setOverwriteFiles(boolean overWriteFiles){
        this.overWriteFiles = overWriteFiles;
    }
    
    public void start(){
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ImportDirectory.class,
                "LBL_PROGRESSBAR_Retrieve_XML")); //NOI18N
        ph.start();
        ph.switchToIndeterminate();
        try{
            infoCollector = new InfoCollector(importRoot, docType);
            if(infoCollector.hasReports()){
                if(infoCollector.hasErrors()){
                    //show error and exit
                    String errorMess = NbBundle.getMessage(ImportDirectory.class,
                            "MSG_directory_closure_error"); //NOI18N
                    NotifyDescriptor.Message ndm = new NotifyDescriptor.Message(errorMess, NotifyDescriptor.Message.ERROR_MESSAGE);
                    opErrors();
                    DialogDisplayer.getDefault().notify(ndm);
                    //System.out.printf("\n\nErrors(%d): %s\n\n",infoCollector.getErrors().size(), infoCollector.getErrors());
                    return;
                }
                if(infoCollector.hasWarnings()){
                    //show warning message and ask the user to quit or copy
                    //if quit return
                    //else continue copy
                    String warningMess = NbBundle.getMessage(ImportDirectory.class,
                            "MSG_absolute_resource_warning"); //NOI18N
                    String warningTitle = NbBundle.getMessage(ImportDirectory.class,
                            "TITLE_absolute_resource_warning"); //NOI18N
                    NotifyDescriptor.Confirmation ndc =
                            new NotifyDescriptor.Confirmation(warningMess, warningTitle,
                            NotifyDescriptor.Confirmation.YES_NO_OPTION,
                            NotifyDescriptor.Confirmation.WARNING_MESSAGE);
                    opWarnings();
                    DialogDisplayer.getDefault().notify(ndc);
                    if(ndc.getValue() == ndc.NO_OPTION)
                        return;
                    //System.out.printf("\n\nWarnings(%d): %s\n\n",infoCollector.getWarnings().size(),infoCollector.getWarnings());
                    //return;
                }
            }   //start copying files
            copyFiles();
            //optionally show in o/p window
            showCopiedFiles();
        }finally{
            ph.finish();
        }
        invokeRetrieverEngineIfRequired();
    }
    
    Map<File, File> copiedFiles = new HashMap<File, File>();
    Map<File, File> errorsWhileCopyFiles = new HashMap<File, File>();
    
    private void copyFiles() {
        List<File> copyList = this.infoCollector.getCopyableFileList();
        for(File srcFile: copyList){
            //construct a file object
            FileObject source = FileUtil.toFileObject(FileUtil.normalizeFile(srcFile));
            
            String toDirStr = this.toDir.toURI().toString();
            StringBuffer sb = new StringBuffer(toDirStr);
            String destStr = sb.append(srcFile.getName()).toString();
          //  System.out.println("SRC NAME IS = " + srcFile.getName());
        //    System.out.println("toDIR str is " + toDirStr);
         //   System.out.println("DEST NAME IS = " + destStr);
                    
            File destFile = null;
            try {
                destFile = new File(new URI(destStr));
            } catch (URISyntaxException ex) {
                continue;
            }
            
            if(source == null){
                //gracefully continue with other files if the source is null
                errorsWhileCopyFiles.put(srcFile, destFile);
                continue;
            }
            
            if(destFile.isFile()){
                if(this.overWriteFiles){
                    destFile.delete();
                }
            }
            
            String fileName = destFile.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            File destParent = destFile.getParentFile();
            
            destParent.mkdirs();
            
            FileObject destParentFOB = FileUtil.toFileObject(FileUtil.normalizeFile(destParent));
            try {
                FileUtil.copyFile(source, destParentFOB, fileName);
            } catch (IOException ex) {
                errorsWhileCopyFiles.put(srcFile, destFile);
                continue;
            }
            copiedFiles.put(srcFile, destFile);
        }
    }
    private void showCopiedFiles() {
        InputOutput io = IOProvider.getDefault().getIO(opTabTitle, false);
        io.setErrSeparated(true);
        OutputWriter error = io.getErr();
        OutputWriter output = io.getOut();
        
        if(errorsWhileCopyFiles.size() > 0){
            error.println(NbBundle.getMessage(ImportDirectory.class,
                    "MSG_OUTPUT_errors_while_copy")); //NOI18N
            for(File file: errorsWhileCopyFiles.keySet()){
                String[] params = {file.toString(), errorsWhileCopyFiles.get(file).toString()};
                error.println(NbBundle.getMessage(ImportDirectory.class,
                        "MSG_OUTPUT_from_target", params));
                error.println(NbBundle.getMessage(ImportDirectory.class,"MSG_POSSIBLE_CAUSE_FOR_ERROR"));
            }
        }
        error.close();
        if(copiedFiles.size() > 0){
            output.println(NbBundle.getMessage(ImportDirectory.class,
                    "MSG_OUTPUT_list_of_files_ret")); //NOI18N
            for(File file: copiedFiles.keySet()){
                String[] params = {file.toString(), copiedFiles.get(file).toString()};
                output.println(NbBundle.getMessage(ImportDirectory.class,
                        "MSG_OUTPUT_from_copied", params));
            }
        }
        output.close();
    }
    static final String opTabTitle = NbBundle.getMessage(ImportDirectory.class,
            "TITLE_retriever_output_tab_title");//NOI18N
    private void opErrors() {
        InputOutput io = IOProvider.getDefault().getIO(opTabTitle, false);
        io.setErrSeparated(true);
        OutputWriter error = io.getErr();
        String errorMess =
                NbBundle.getMessage(ImportDirectory.class,"MSG_OUTPUT_directory_closure"); //NOI18N
        Map<File, List<InfoCollector.InfoEntry>> errors = this.infoCollector.getErrors();
        error.printf("\n%s (%d):\n", errorMess, errors.size()); //NOI18N
        for(File file : errors.keySet()){
            String msgFileStr = NbBundle.getMessage(ImportDirectory.class,
                    "MSG_OUTPUT_file"); //NOI18N
            error.printf("%s %s\n", msgFileStr, file.toString());
            List<InfoCollector.InfoEntry> entList =errors.get(file);
            String msgOverfloLoc =
                    NbBundle.getMessage(ImportDirectory.class,
                    "MSG_OUTPUT_overflowing_location"); //NOI18N
            for(InfoCollector.InfoEntry ent : entList){
                error.printf("    %s %s\n", msgOverfloLoc, ent.getChildStr()); //NOI18N
            }
        }
        error.close();
    }
    
    private void opWarnings() {
        InputOutput io = IOProvider.getDefault().getIO(opTabTitle, false);
        io.setErrSeparated(true);
        OutputWriter error = io.getErr();
        String errorMess =
                NbBundle.getMessage(ImportDirectory.class,
                "MSG_OUTPUT_absolute_resource"); //NOI18N
        Map<File, List<InfoCollector.InfoEntry>> errors = this.infoCollector.getWarnings();
        error.printf("\n%s (%d):\n", errorMess, errors.size()); //NOI18N
        for(File file : errors.keySet()){
            String msgFileStr =
                    NbBundle.getMessage(ImportDirectory.class, "MSG_OUTPUT_file"); //NOI18N
            error.printf("%s %s\n", msgFileStr, file.toString());
            List<InfoCollector.InfoEntry> entList =errors.get(file);
            String msgAbsLoc = NbBundle.getMessage(ImportDirectory.class,
                    "MSG_OUTPUT_absolute_location"); //NOI18N
            for(InfoCollector.InfoEntry ent : entList){
                error.printf("    %s %s\n", msgAbsLoc, ent.getChildStr());
            }
        }
        error.close();
    }
    
    private void invokeRetrieverEngineIfRequired() {
        Map<File, List<InfoCollector.InfoEntry>> errors = this.infoCollector.getAbsURL2Info();
        for(File file : errors.keySet()){
            List<InfoCollector.InfoEntry> entList =errors.get(file);
            for(InfoCollector.InfoEntry ent : entList){
                if(ent.getInfoType() == ent.getInfoType().url){
                    String urlStr = ent.getChildStr();
                    URL url = null;
                    try {
                        url = new URL(urlStr);
                    } catch (MalformedURLException ex) {
                        continue;
                    }
                    RetrieverEngine instance = RetrieverEngine.getRetrieverEngine(toDir);
                    RetrieveEntry rent = null;
                    rent = new RetrieveEntry(null, url.toString(), file, null, DocumentTypesEnum.schema, true);
                    instance.addResourceToRetrieve(rent);
                    instance.start();
                }
            }
        }
    }
    
    
}
