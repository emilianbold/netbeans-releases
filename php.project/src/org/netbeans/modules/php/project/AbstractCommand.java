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

import java.io.File;
import java.text.MessageFormat;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.netbeans.modules.php.rt.utils.PhpProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author avk
 */
public abstract class AbstractCommand implements Command {

    private static final String LBL_OUT_TAB_TITLE = "LBL_OutputTabTitle";

    public AbstractCommand(Project project) {
        myProject = project;
        initActionFiles();
    }

    /**
     * If true, this action should be performed asynchronously in a private thread.
     * If false, it will be performed synchronously as called in the event thread.
     * @return true if this action should automatically be performed asynchronously
     */
    public boolean asynchronous() {
        return PhpCommandUtils.defaultAsynchronous();
    }
    
    public boolean isEnabled() {
        return true;
    }

    public void setActionFiles( FileObject[] files ) {
        myFiles = files;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractCommand)){
            return false;
        }
        AbstractCommand command = (AbstractCommand)obj;
        return command.getId().equals(this.getId())
                && command.getLabel().equals(this.getLabel());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode()*37 
                + this.getLabel().hashCode();
    }
    
    protected FileObject[] getFileObjects() {
        return myFiles;
    }

    protected Project getProject() {
        return myProject;
    }

    /**
     * should be called in the beginning of run() method
     */
    protected void refresh() {
        initOutputTabTitle();
    }
    
    protected String getRelativeSrcPath(FileObject fileObject) {
        return PhpProjectUtils.getRelativeSrcPath(getProject(), fileObject);
    }
     
    protected AntProjectHelper getAntProjectHelper() {
        return getProject().getLookup().lookup(AntProjectHelper.class);
    }

    protected FileObject[] getSourceObjects(Project phpProject) {
        return Utils.getSourceObjects(phpProject);
    }

    protected FileObject getSourceObject() {
        FileObject[] sources = getSourceObjects(getProject());
        if (sources == null || sources.length == 0) {
            return null;
        }
        /*
         * I choose only first source root.
         * TODO: change if we decide to support multiple src roots
         */
        return sources[0];
    }
    
    protected String getOutputTabTitle() {
        return myOutputTabTitle;
    }

    protected void notifyMsg(String bundleKey, Object... args) {
        notifyMsg(bundleKey, getClass(), args);
    }

    protected void notifyMsg(String bundleKey, Class clazz, Object... args) {
        String msg = loadFormattedMsg(bundleKey, clazz, args);
        logToOutput(getOutputTabTitle(), msg);
    }

    protected void statusMsg(String bundleKey, Object... args) {
        statusMsg(bundleKey, getClass(), args);
    }

    protected void statusMsg(String bundleKey, Class clazz, Object... args) {
        String msg = loadFormattedMsg(bundleKey, clazz, args);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    protected static String loadFormattedMsg(String bundleKey, Class clazz, Object... args) {
        String msg = NbBundle.getMessage(clazz, bundleKey);
        if (args.length > 0) {
            msg = MessageFormat.format(msg, args);
        }
        return msg;
    }

    protected boolean isNbProject(File file) {
        // moved away from constructor. to prevent NPE on the first init.
        if (PROJECT_XML == null){
            PROJECT_XML = getProject().getLookup().lookup(AntProjectHelper.class)
                .resolveFile(AntProjectHelper.PROJECT_XML_PATH);
        }
        File nbProjectFile = PROJECT_XML.getParentFile();
        return file.equals(nbProjectFile);
    }

    private void initActionFiles() {
        /*
         * This method should be called in constructor of class because
         * <code>nodes</code> array could be changed while action
         * execution. So one need to initialize fileObjects array once
         * and use it for access to action files.
         */
        myFiles = PhpCommandUtils.getActionFiles();
    }

    private static void logToOutput(String outTabTitle, String msg) {
        InputOutput io = IOProvider.getDefault().getIO(outTabTitle, false);
        io.select();
        OutputWriter writer = io.getOut();
        writer.println(msg); //write tag to output window
        writer.flush();
        writer.close();
    }

    /** sets default output tab title.
     * Can't be added to constructor because it causes problems 
     * with standard project actions like copy etc.
     * <br>
     * is now added into refresh() command
     */
    private void initOutputTabTitle(){
            myOutputTabTitle = NbBundle.getMessage(
                    AbstractCommand.class, LBL_OUT_TAB_TITLE, getLabel());
    }

    private final Project myProject;
    private FileObject[] myFiles;
    private String myOutputTabTitle;
    private File PROJECT_XML;

}
