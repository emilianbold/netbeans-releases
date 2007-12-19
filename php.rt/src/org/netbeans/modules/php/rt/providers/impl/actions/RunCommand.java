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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.ActionsDialogs;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.netbeans.modules.php.rt.utils.ServerActionsPreferences;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class RunCommand extends AbstractCommand implements Command, Cloneable {
    private static final String LBL_RUN_WITHOUT_UPLOAD_TITLE 
            = "LBL_RunWithoutUpload_Title"; // NOI18N

    private static final String LBL_RUN_WITHOUT_UPLOAD_MESSAGE 
            = "LBL_RunWithoutUpload_Message"; // NOI18N
    
    private static final String LBL_UPLOAD_IS_OFF 
            = "LBL_FilesUploadIsOff_Message"; // NOI18N
    
    private static final String LBL_UPLOAD_IS_ON 
            = "LBL_FilesUploadIsOn_Message"; // NOI18N
    
    private static final String LBL_UPLOAD_WAS_INTERRUPTED 
            = "LBL_FilesUploadWasInterrupted_Message"; // NOI18N

    private static final String AMP                 = "&";                  // NOI18N

    private static final String EQUAL               = "=";                  // NOI18N

    private static final String QUEST               = "?";                  // NOI18N

    private static final String HTTP                = "http://";            // NOI18N
    
    private static final String LBL_MALFORMED_URL   = "LBL_MalformedUrl";   // NOI18N
    
    private static final String LBL_RUN             = "LBL_RunProject";     // NOI18N
    
    protected static final String RUN               = ActionProvider.COMMAND_RUN;

    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    /**
     * @param project
     */
    public RunCommand( Project project , WebServerProvider provider) 
    {
        super( project , provider );
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        refresh();
        
        saveProject();
        Host host = getHost();
        if (!checkHost(host)) {
            return;
        }
        
        String serverBaseUrl = getServerBaseUrl(host);
        
        boolean proceed = uploadFiles();
        if (!proceed){
            return;
        }
        if (getFileObjects() == null) {
            openPathInBrowser(serverBaseUrl, ""  );
        }
        else {
            for ( FileObject fileObject : getFileObjects() ) {
                String path = getRelativeSrcPath(fileObject);
                if ( path != null ) {
                    path = '/'+path.replace( File.separatorChar, '/');
                    if ( !openPathInBrowser(serverBaseUrl, path) ) {
                        break;
                    }
                }
                else {
                    // TODO 

                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getId()
     */
    public String getId() {
        return RUN;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getLabel()
     */
    public String getLabel() {
        return NbBundle.getMessage( RunCommand.class , LBL_RUN );
    }

    @Override
    protected boolean checkHost(Host host) {
        if (!super.checkHost(host)){
            return false;
        }
        // todo check file part if will use it.
        return checkHostHttpPart(host);
    }


    
    /**
     * <p>
     * creates path to open in browser based on serverName and path
     * to file. Takes into account contect path specified in project.
     * </p>
     * <p>
     * Should be overriden to use another specific details in path
     * </p>
     * @param serverName - server name
     * @param path to file starting from project src root.
     */
    protected String getUrlTextToOpen(String serverBaseUrl, String path){
            String urlText = "";

        try {
            urlText = HostImpl.Helper.addSubdirectoryToUrl(serverBaseUrl, getContext());
            urlText = HostImpl.Helper.addSubdirectoryToUrl(urlText, path);
            return urlText;
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex.getMessage());
        }
        return serverBaseUrl + getContext() + path;
    }
    
    /** 
     * opens path in browser.
     * uses @see getUrlText to create full path to open.
     */
    protected boolean openPathInBrowser( String serverBaseUrl, String path ) {
        String urlText = appendParams( getUrlTextToOpen(serverBaseUrl, path) );
        URL url;
        try {
            url = new URL( urlText );
        }
        catch (MalformedURLException e) {
            notifyMsg(LBL_MALFORMED_URL, urlText);
            LOGGER.log(Level.INFO, null, e);
            return false;
        }
        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        return true;
    }
    
    protected String appendParams( String url ) {
        if ( myParams == null ) {
            return url;
        }
        StringBuilder builder = null;
        if ( url.contains( QUEST ) ){
            int indx = url.lastIndexOf( QUEST );
            if ( indx == url.length() -1 ) {
                builder = new StringBuilder( url );
            }
            else {
                builder = new StringBuilder( url );
                builder.append( AMP );
            }
        }
        else {
            builder = new StringBuilder( url );
            builder.append( QUEST );
        }
        appandParams( builder );
        return builder.toString();
    }
    
    protected void addParameter( String paramName , String paramValue ) {
        if ( myParams == null ) {
            myParams = new HashMap<String, String>();
        }
        myParams.put( paramName , paramValue );
    }
    
    private void saveProject(){
        PhpCommandUtils.saveAll();
    }
    
    /**
     * performs files upload.
     * @return boolean true if we should proceed with run, false if should stop.
     */
    private boolean uploadFiles(){
        if (!isUploadBeforeRun()) {
            return true;
        }

        try {
            UploadFilesCommand command = getUploadCommand();
            if (command == null) {
                return confirmProceedNoUpload();
            }

            UploadFilesCommand clonedCommand = (UploadFilesCommand) command.clone();
            clonedCommand.setOutputTabTitleData(getLabel());
            clonedCommand.run();
            // test success
            if(clonedCommand.wasInterrupted()){
                notifyInterrupted(clonedCommand);
                return false;
            }
            if (!clonedCommand.wasSuccessfull()){
                return confirmProceedNoUpload();
            }

        } catch (CloneNotSupportedException ex) {
            assert false;
        }
        return true;
    }
    
    private boolean confirmProceedNoUpload(){
        String title = NbBundle.getMessage(RunCommand.class, 
                LBL_RUN_WITHOUT_UPLOAD_TITLE);
        String msg = NbBundle.getMessage(RunCommand.class, 
                LBL_RUN_WITHOUT_UPLOAD_MESSAGE);
        return ActionsDialogs.userConfirmYesNo(title, msg);
    }
    
    private boolean isUploadBeforeRun(){
        boolean upload = ServerActionsPreferences.getInstance()
                .getUploadBeforeRun();
        if (upload){
            notifyMsg(LBL_UPLOAD_IS_ON);
        } else {
            notifyMsg(LBL_UPLOAD_IS_OFF);
        }
        return upload;
    }
    
    private void notifyInterrupted(UploadFilesCommand uploadCommand){
        notifyMsg(LBL_UPLOAD_WAS_INTERRUPTED, uploadCommand.getLabel(), getLabel());
    }
    
    private UploadFilesCommand getUploadCommand() {
        Command[] commands = getProvider().getCommandProvider()
                .getCommands(getProject());
        Command uploadCommand = null;
        for (Command command : commands) {
            String id = command.getId();
            if (UploadFilesCommand.UPLOAD.equals(id)) {
                uploadCommand = command;
            }
        }
        if (uploadCommand == null) {
            return null;
        }
        assert uploadCommand instanceof UploadFilesCommand;
        return (UploadFilesCommand) uploadCommand;
    }

    private String getServerBaseUrl(Host host){
        String url = "";
        if (host instanceof HostImpl){
            url = HostImpl.Helper.getHttpUrl((HostImpl)host);
        } else {
            url = HTTP + host.getServerName();
        }
        return url;
    }
    
    private void appandParams( StringBuilder builder ) {
        for( Entry<String, String> entry : myParams.entrySet()) {
            builder.append( entry.getKey() );
            builder.append( EQUAL );
            builder.append( entry.getValue() );
        }
    }
    
    private Map<String, String> myParams; 
}
