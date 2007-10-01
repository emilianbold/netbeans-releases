/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.system.cleaner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Dmitry Lipin
 */


public abstract class ProcessOnExitCleanerHandler extends OnExitCleanerHandler {
    protected List <String> runningCommand;
    private String cleanerFileName ;
    
    protected ProcessOnExitCleanerHandler(String cleanerFileName) {
        this.cleanerFileName = cleanerFileName;
    }
    protected File getCleanerFile() throws IOException{
        String name = cleanerFileName;
        int idx = name.lastIndexOf(".");
        String ext = "";
        if(idx > 0) {
            ext = name.substring(idx);
            name = name.substring(0, idx);
        }
        return File.createTempFile(name, ext, SystemUtils.getTempDirectory());        
    }
    
    protected File getListFile() throws IOException{
        return File.createTempFile(DELETING_FILES_LIST,null, SystemUtils.getTempDirectory());        
    }
    
    protected abstract void writeCleaningFileList(File listFile, List <String> files) throws IOException;
    protected abstract void writeCleaner(File cleanerFile) throws IOException;
    
    public void init(){
        if(fileList.size() > 0) {
            try {
            File listFile = getListFile();
            
            List <String> paths = new ArrayList <String> ();
            for(File f : fileList) {
                paths.add(f.getAbsolutePath());
            }
            Collections.sort(paths, Collections.reverseOrder());
            
            
                writeCleaningFileList(listFile, paths);                
                File cleanerFile = getCleanerFile();
                writeCleaner(cleanerFile);
                SystemUtils.correctFilesPermissions(cleanerFile);
                runningCommand = new ArrayList <String> ();
                runningCommand.add(cleanerFile.getCanonicalPath());
                runningCommand.add(listFile.getCanonicalPath());
            } catch  (IOException e) {
                // do nothing then..
            }
        }
    }
    
    public void run() {
        init();
        if(runningCommand!=null ) {
            try {                
                ProcessBuilder builder= new ProcessBuilder(runningCommand);
                builder.directory(SystemUtils.getUserHomeDirectory());
                builder.start();
                LogManager.log(ErrorLevel.DEBUG, "... cleaning process has been started ");
            } catch (IOException ex) {
                LogManager.log(ex);
            }
        }
    }
}
