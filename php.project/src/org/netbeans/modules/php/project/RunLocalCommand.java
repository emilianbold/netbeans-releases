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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.options.CommandLinePreferences;
import org.netbeans.modules.php.project.options.ProjectActionsPreferences;
import org.netbeans.modules.php.project.ui.actions.SystemPackageFinder;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author avk
 */
public class RunLocalCommand extends AbstractCommand implements Cancellable{

    // bundle keys
    private static final String LBL_MALFORMED_URL 
            = "LBL_MalformedUrl";                               // NOI18N
    private static final String LBL_SCRIPT_NOT_SAVED_WARN 
            = "LBL_ScriptNotSavedWarn";                         // NOI18N
    private static final String LBL_EXEC_CANCELLED_MSG
            = "LBL_ScriptExecCancelledMsg";                     // NOI18N
    private static final String LBL_EXEC_ERROR_MSG
            = "LBL_ScriptExecErrorMsg";                         // NOI18N
    private static final String LBL_EXEC_EXIT_VALUE_MSG
            = "LBL_ScriptExecExitValueMsg";                     // NOI18N
    private static final String LBL_PHP_INTERPRETER_MSG 
            = "LBL_PhpInterpreterMsg";                          // NOI18N
    private static final String LBL_CAN_NOT_FIND_INTERPRETER 
            = "LBL_CantFindInterpreter";                        // NOI18N
    private static final String LBL_NO_INDEX_FILE 
            = "LBL_NoIndexFile";                                // NOI18N
    private static final String LBL_OUT_TAB_TITLE 
            = "LBL_RunLocalOutputTabTitle";                     // NOI18N

    private static final String LF = "\n";                      // NOI18N
    // 
    static final int RUNTIME_EXIT_NORMAL = 0;
    
    static final String PROPERTY_SYSTEM_OS_NAME = "os.name"; // NOI18N
    
    static final String PROPERTY_SYSTEM_TMP_DIR = "java.io.tmpdir"; // NOI18N
    
    public static final String LBL_INTERPRETED_PHP_EXT = "LBL_InterpretedPhpExt"; // NOI18N
    
    public static final String RUN_LOCAL_ACTION = "run.local"; // NOI18N
    
    private static final String RUN_LOCAL_LABEL = PhpActionProvider.LBL_RUN_LOCAL;
    
    private static final String DEFAULT_INDEX_NAME = "index";   // NOI18N
    
    private static final String DEFAULT_INDEX_EXT = "php";      // NOI18N
    
    private static final String DEFAULT_INDEX 
            = DEFAULT_INDEX_NAME+"."+DEFAULT_INDEX_EXT;         // NOI18N

    private static Logger LOGGER = Logger.getLogger(RunLocalCommand.class.getName());
    
    public RunLocalCommand(Project project) {
        super(project);
    }

    public String getId() {
        return RUN_LOCAL_ACTION;
    }

    public String getLabel() {
        return NbBundle.getMessage(RunLocalCommand.class, RUN_LOCAL_LABEL);
    }

    @Override
    public boolean isEnabled() {
        return myIsEnabled;
    }

    public boolean cancel() {
        isCancelled = true;
        if (myProcess != null){
            myProcess.destroy();
        }
        notifyMsg(LBL_EXEC_CANCELLED_MSG, this.getClass(), getLabel());
        return true;
    }
    
    public void run() {
        refresh();
        ProgressHandle progress = ProgressHandleFactory.createHandle(getLabel(), this);
        progress.start();
        try {
            saveProject();
            String php = getPhpInterpreter();
            //saveScripts();
            String[] scripts = getScriptsToRun();
            if (scripts.length == 0){
                notifyMsg(LBL_NO_INDEX_FILE, DEFAULT_INDEX);
                return;
            }
            
            if (php != null) {
                for (String script : scripts) {
                    if (!isCancelled){
                        
                        String title = NbBundle.getMessage( RunLocalCommand.class, 
                                LBL_OUT_TAB_TITLE, getLabel(), script);
                        setOutputTabTitle(title);
                        
                        File outFile = runScript(php, script);
                        if (outFile != null) {
                            openInOutput(outFile);
                            openFileInBrowser(outFile);
                        }
                    }
                }
            } else {
                notifyMsg(LBL_CAN_NOT_FIND_INTERPRETER, this.getClass());
            }
        } finally {
            progress.finish();
        }
    }

    @Override
    protected void refresh() {
        super.refresh();
        isCancelled = false;
        setOutputTabTitle(super.getOutputTabTitle());
    }
    
    @Override
    protected String getOutputTabTitle() {
        return myOutputTabTitle;
    }

    protected void setOutputTabTitle(String title){
        myOutputTabTitle = title;
    }
    
    protected boolean openFileInBrowser(File file) {
        if (!isOpenInBrowser()){
            return true;
        }
        if (isCancelled){
            return false;
        }
        
        URL url;
        try {
            url = file.toURL();
        } catch (MalformedURLException e) {
            notifyMsg(LBL_MALFORMED_URL, file.getPath());
            return false;
        }
        if (isCancelled){
            return false;
        }
        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        return true;
    }

    protected boolean openInOutput(File file){
        boolean success = true;
        if (isCancelled){
            return false;
        }
        if (!isPrintToOutput()){
            return success;
        }
        
        BufferedReader reader = null;
        OutputWriter outputWriter = null;

        try {
            outputWriter = getOutputWriter();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line;
            while ((line = reader.readLine()) != null) {
                outputWriter.println(line);
                outputWriter.flush();
            }
        } catch (IOException ex) {
            outputWriter.println(ex.getMessage());
            outputWriter.flush();
            success = false;
        } finally {
            if (outputWriter != null) {
                outputWriter.close();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // do nothing
                }
            }
        }

        return success;
    }

    /**
     * returns file with result output.
     * can be null in case of any errors during script execution.
     */
    private File runScript(String php, String script) {
        boolean success = true;

        notifyMsg(LBL_PHP_INTERPRETER_MSG, this.getClass(), php);
        
        File tmpFile = null;
        FileWriter tmpOutput = null;
        BufferedReader reader = null;
        BufferedReader errReader = null;
        if (isCancelled){
            return null;
        }
        try {
            String[] command = new String[]{php, script};
            Runtime runtime = Runtime.getRuntime();
            myProcess = runtime.exec(command); // NOI18N
            
            tmpFile = getTmpFile(script);
            tmpOutput = new FileWriter(tmpFile);

            reader = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));
            errReader = new BufferedReader(new InputStreamReader(myProcess.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                    tmpOutput.write(line + LF);
            }

            while ((line = errReader.readLine()) != null) {
                notifyMsg(LBL_EXEC_ERROR_MSG, line);
            }
            try {
                if (myProcess.waitFor() != 0) {
                    notifyMsg(LBL_EXEC_EXIT_VALUE_MSG, myProcess.exitValue());
                }
            } catch (InterruptedException e) {
                notifyMsg(LBL_EXEC_ERROR_MSG, e.getMessage());
            }
            
            
        } catch (IOException ex) {
            notifyMsg(LBL_EXEC_ERROR_MSG, ex.getMessage());
            success = false;
        } finally {
            if (tmpOutput != null) {
                try {
                    tmpOutput.close();
                } catch (IOException ex) {
                    notifyMsg(LBL_EXEC_ERROR_MSG, ex.getMessage());
                    success = false;
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException ex) {
                    success = false;
                }
            }
            if (errReader != null){
                try {
                    errReader.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }

        if (success && !isCancelled){
            return tmpFile;
        } else {
            return null;
        }
    }

    private OutputWriter getOutputWriter() {
        InputOutput io = IOProvider.getDefault().getIO(getOutputTabTitle(), false);
        io.select();
        OutputWriter writer = io.getOut();
        return writer;
    }

    private String getPhpInterpreter() {
        String path = ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

            public String run() {
                String commandPath = getAntProjectHelper().
                        getStandardPropertyEvaluator().
                        getProperty(PhpProject.COMMAND_PATH);
                return commandPath;
            }
        });
        if (path == null) {
            path = CommandLinePreferences.getInstance().getPhpInterpreter();
        }
        if (path != null){
            path = path.trim();
        }
        if (path != null && path.length() == 0){
            path = null;
        }
        return path;
    }

    private void saveProject(){
        PhpCommandUtils.saveAll();
    }

    private void saveScripts(){
        if (getFileObjects() != null) {
            for (FileObject fileObject : getFileObjects()) {
                saveFile(fileObject);
            }
        }
    }

    private String[] getScriptsToRun() {
        List<String> scripts = new ArrayList<String>();
        if (getFileObjects() != null) {
            for (FileObject fileObject : getFileObjects()) {
                
                String path = getScriptFromFO(fileObject);
                if (path != null) {
                    scripts.add(path);
                }
            }
        }
        return scripts.toArray(new String[]{});
    }

    private String getScriptFromFO(FileObject fileObject) {
        File file = fileObject.isFolder()
                ? getDefaultIndex(fileObject)
                : FileUtil.toFile(fileObject);

        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }
    
    private void saveFile(FileObject fromObject){
        try {
            PhpCommandUtils.saveFile(fromObject);
        } catch (IOException ex) {
            notifyMsg(LBL_SCRIPT_NOT_SAVED_WARN, getRelativeSrcPath(fromObject));
        }
    }

    private File getDefaultIndex(FileObject sourceRoot) {
        FileObject[] children = sourceRoot.getChildren();
        for (FileObject fo : children) {
            if (fo.getName().equalsIgnoreCase(DEFAULT_INDEX_NAME) 
                    && fo.getExt().equalsIgnoreCase(DEFAULT_INDEX_EXT)) 
            {
                return FileUtil.toFile(fo);
            }
        }
        return null;
    }

    private boolean isPrintToOutput(){
        return ProjectActionsPreferences.getInstance()
                .getCommandLineRunPrintToOutput();
    }
    
    private boolean isOpenInBrowser(){
        return ProjectActionsPreferences.getInstance()
                .getCommandLineRunOpenInBrowser();
    }
    
    private File getTmpFile(String script) throws IOException {

        File scriptFile = new File(script);
        String ext = loadFormattedMsg(LBL_INTERPRETED_PHP_EXT, this.getClass());
        File tmpFile = File.createTempFile(scriptFile.getName(), ext);
        tmpFile.deleteOnExit();

        return tmpFile;
    }

    private static boolean myIsEnabled = SystemPackageFinder.isSupportedOs();
    private Process myProcess;
    private boolean isCancelled = false;
    private String myOutputTabTitle;

 
}