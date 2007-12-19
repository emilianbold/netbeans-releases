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
package org.netbeans.modules.php.rt.providers.impl.actions;

import java.text.MessageFormat;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.rt.providers.impl.AbstractProjectConfigProvider;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.absent.actions.AbsentActionsUtils;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.netbeans.modules.php.rt.utils.PhpProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;


/**
 * @author ads
 *
 */
public abstract class AbstractCommand implements Runnable, 
        Command { // command is implemented to have label

    private static final String LBL_OUT_TAB_TITLE = "LBL_OutputTabTitle";

    //private static final String LBL_ABSENT_HOST = "LBL_AbsentHost"; // NOI18N
    
    private static final String NO_HTTP_TITLE = "LBL_NotConfiguredHttp_Title"; // NOI18N
    
    private static final String NO_HTTP_MESSAGE = "LBL_NotConfiguredHttp_Message"; // NOI18N

    public static final String TMP_FILE_POSTFIX = "~";

    public AbstractCommand(Project project, WebServerProvider provider) {
        myProvider = provider;
        myProject = project;
        initActionFiles();
        initOutputTabTitle();
    }

    /**
     * If true, this action should be performed asynchronously in a private thread.
     * If false, it will be performed synchronously as called in the event thread.
     * @return true if this action should automatically be performed asynchronously
     */
    public boolean asynchronous() {
        return PhpCommandUtils.defaultAsynchronous();
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
    
    

    protected Host getHost() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Host>() {

            public Host run() {
                String hostId = getAntProjectHelper().getStandardPropertyEvaluator().getProperty(WebServerProvider.HOST_ID);
                myHostId = hostId;
                Host host = getProvider().findHost(hostId);
                loadConfig(host);
                return host;
            }
        });
    }

    protected boolean isTempFile(FileObject file) {
        String name = file.getNameExt();
        return name.endsWith(TMP_FILE_POSTFIX);
    }
    
    /**
     * This method should be overriden by subclasses for getting other
     * properties from project.
     * Here I retrieve only cpntect property.
     * @param host
     */
    protected void loadConfig(Host host) {
        myContext = getAntProjectHelper().getStandardPropertyEvaluator().
                getProperty(AbstractProjectConfigProvider.CONTEXT);
    }

    protected boolean checkHost(Host host) {
        if (host == null) {
            AbsentActionsUtils.alertNotConfiguredHost(getProject(), getLabel());
            //notifyMsg( LBL_ABSENT_HOST , AbstractCommand.class, getHostId() );
            return false;
        }
        return true;
    }

    protected boolean checkHostHttpPart(Host host) {
        boolean res = false;
        if (host instanceof HostImpl){
            HostImpl impl = (HostImpl)host;
            if (HostImpl.Helper.isHttpReady(impl)) {
                res = true;
            } else {
                alertHttpNotConfigured(impl, getProject(), getLabel());
            }
        }
        return res;
    }

    private void alertHttpNotConfigured(HostImpl host, Project project, String command){
                String projectName = ProjectUtils.getInformation(project).
                        getDisplayName();

                String title = loadFormattedMsg(NO_HTTP_TITLE, AbstractCommand.class);
                String msg = loadFormattedMsg(NO_HTTP_MESSAGE, AbstractCommand.class,
                        host.getDisplayName(), command, projectName);

                AbsentActionsUtils.alertNotConfiguredHost(project, title, msg);
    }
    
    protected String getContext() {
        return myContext;
    }

    protected FileObject[] getSourceObjects(Project phpProject) {
        return PhpProjectUtils.getSourceObjects(phpProject);
    }

    protected Project getProject() {
        return myProject;
    }

    protected WebServerProvider getProvider() {
        return myProvider;
    }

    protected AntProjectHelper getAntProjectHelper() {
        return getProject().getLookup().lookup(AntProjectHelper.class);
    }

    protected String getOutputTabTitle() {
        return myOutputTabTitle;
    }

    private void initOutputTabTitle(){
        setOutputTabTitleData(getLabel());
    }
    
    /**
     * sets command name to be displayed in output tab title. 
     * Can't change title format. Can be used to display output for several 
     * commands which invoke each other in one tab.
     * @param String command label to be displayed
     */
    protected void setOutputTabTitleData(String commandLabel){
            myOutputTabTitle = NbBundle.getMessage(
                    AbstractCommand.class, LBL_OUT_TAB_TITLE, commandLabel, getHost());
    }
    
    protected void notifyMsg(String bundleKey, Object... args) {
        notifyMsg(bundleKey, getClass(), args);
    }

    protected void notifyMsg(String bundleKey, Class clazz, Object... args) {
        String msg = loadFormattedMsg(bundleKey, clazz, args);
        logToOutput(getOutputTabTitle(), msg);
        //NotifyDescriptor descr = new NotifyDescriptor.Message(msg);
        //DialogDisplayer.getDefault().notify(descr);
    }

    protected void statusMsg(String bundleKey, Object... args) {
        statusMsg(bundleKey, getClass(), args);
    }

    protected void statusMsg(String bundleKey, Class clazz, Object... args) {
        String msg = loadFormattedMsg(bundleKey, clazz, args);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    protected String loadFormattedMsg(String bundleKey, Class clazz, Object... args) {
        String msg = NbBundle.getMessage(clazz, bundleKey);
        if (args.length > 0) {
            msg = MessageFormat.format(msg, args);
        }
        return msg;
    }

    protected FileObject[] getFileObjects() {
        return myFiles;
    }

    protected String getRelativeSrcPath(FileObject fileObject) {
        return PhpProjectUtils.getRelativeSrcPath(getProject(), fileObject);
    }

    protected void refresh() {
        myContext = null;
        myHostId = null;
    }

    protected String getHostId() {
        return myHostId;
    }
     
    private void initActionFiles(){
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

    private final Project myProject;
    private final WebServerProvider myProvider;
    private String myContext;
    private String myHostId;
    private FileObject[] myFiles;
    private String myOutputTabTitle;
}
