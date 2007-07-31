/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
    
    private File importRoot;
    
    private File toDir;
    
    private Thread myThread;
    
    private boolean overWriteFiles = false;
    
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
    public ImportDirectory(File importRoot, File toDir) {
        this(importRoot, toDir, false);
    }
    
    /** Creates a new instance of ImportDirectory */
    public ImportDirectory(File importRoot, File toDir, boolean overWriteFiles) {
        this.importRoot = importRoot;
        this.toDir = toDir;
        this.overWriteFiles = overWriteFiles;
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
            infoCollector = new InfoCollector(importRoot);
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
            
            String impRootStr = this.importRoot.toURI().toString();
            String toDirStr = this.toDir.toURI().toString();
            StringBuffer strBuff = new StringBuffer(srcFile.toURI().toString());
            String destStr = strBuff.replace(0, impRootStr.length()-1, toDirStr).toString();
            
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
