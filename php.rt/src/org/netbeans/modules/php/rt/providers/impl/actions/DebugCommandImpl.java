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
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
//import org.netbeans.modules.php.dbgp.api.DebuggerFactory;
//import org.netbeans.modules.php.dbgp.api.SessionId;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
public class DebugCommandImpl extends AbstractCommand implements Command {
    
    private static final String MIME_TYPE = "text/x-php5";
    
    private static final String ERR_DEBUG_SESSION 
                                            = "MSG_ErrDebugSession"; // NOI18N
    
    private static final String ERR_DEBUG_UNCONFIGURED 
                                            = "MSG_ErrDebuggerIsNotConfigured"; // NOI18N
    
    private static final String ERR_DEBUG_FOLDER 
                                            = "MSG_ErrDebuggerRunOnFolder";// NOI18N

    private static final int WAIT_INIT_SESSION 
                                            = 20000;

    private static final String XDEBUG_COOKIE 
                                            = "XDEBUG_SESSION_START";// NOI18N

    private static final String DEBUG       = "debug";               // NOI18N 
    
    private static final String LBL_DEBUG   = "LBL_Debug";           // NOI18N
    
    private static final String LBL_DEBUG_FILE   
                                            = "LBL_DebugFile";       // NOI18N
    
    private static final String REASON_PREFIX
                                            = "    - ";              // NOI18N   

    
    public DebugCommandImpl( Project project, WebServerProvider provider) 
    {
        super(project, provider );
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
//        final SessionId sessionId = getSessionId();
//        if ( sessionId != null ) {
//            runFilesInExistedSession(sessionId);
//        }
//        else {
//            runFilesInFreshSession( null );
//        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getId()
     */
    public String getId() {
        return DEBUG;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getLabel()
     */
    public String getLabel() {
        FileObject[] fileObjects = getFileObjects();
        if ( fileObjects == null || fileObjects.length == 0 ) {
            return null;
        }
        FileObject fileObject = fileObjects[0];
        if ( fileObject.isFolder() ){
            return NbBundle.getMessage( DebugCommandImpl.class, LBL_DEBUG);
        }
        else {
            return NbBundle.getMessage( DebugCommandImpl.class, LBL_DEBUG_FILE );
        }
    }

    public boolean isEnabled() {
        return true;
    }
    
    private RunCommand getRunCommand() {
        Command[] commands = getProvider().getCommandProvider()
                .getCommands(getProject());
        Command runCommand = null;
        for (Command command : commands) {
            String id = command.getId();
            System.out.println("command id :" +id );
            if (RunCommand.RUN.equals(id)) {
                runCommand = command;
            }
        }
        if (runCommand == null) {
            return null;
        }
        assert runCommand instanceof RunCommand;
        return (RunCommand) runCommand;
    }

    private FileObject getFirstFile() {
        FileObject project = getProject().getProjectDirectory();
        FileObject[] files = getFileObjects();
        FileObject startFO  = null;
        int pathLength = 0;
        for (FileObject file : files) {
            if ( file.equals( project )) {
                startFO = project;
                break;
            }
            if ( !MIME_TYPE.equals(file.getMIMEType()) )
            {
                continue;
            }
            String path = file.getPath();
            if ( path.length() >= pathLength ) {
                startFO = file;
                pathLength = path.length();
            }
        }
        return startFO;
    }
    
    private FileObject[] getOtherFiles( FileObject fileObject ) {
        FileObject[] files = getFileObjects();
        if ( files.length == 1) {
            return new FileObject[0];
        }
        List<FileObject> list = new ArrayList<FileObject>( files.length -1 );
        for (FileObject file : files) {
            if ( file != fileObject ) {
                list.add(file);
            }
        }     
        return list.toArray( new FileObject[ list.size() ] );
    }
    
    private void notifyError( FileObject file , int seconds ) {
        String message = NbBundle.getMessage( DebugCommandImpl.class , 
                ERR_DEBUG_SESSION);
        String msg = MessageFormat.format( message, seconds );
        StringBuilder builder = new StringBuilder( msg );
        builder.append( '\n' );
        String badConfiguration = NbBundle.getMessage( DebugCommandImpl.class , 
                ERR_DEBUG_UNCONFIGURED);
        builder.append( REASON_PREFIX );
        builder.append( badConfiguration );
        if ( file.isFolder() ){
            builder.append( '\n' );
            builder.append( REASON_PREFIX );
            String runOnFolder = NbBundle.getMessage( DebugCommandImpl.class , 
                    ERR_DEBUG_FOLDER);
            builder.append( runOnFolder );
        }
        NotifyDescriptor descr = new NotifyDescriptor.Message( builder.toString());
        DialogDisplayer.getDefault().notify(descr);
    }
    

    private void notifyWarning() {
        // TODO : notify about found session but not started debugging,
        // proceed with found session and current set of action files 
    }
    
//    private SessionId getSessionId() {
//        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
//        for (Session session : sessions) {
//            SessionId sessionId = 
//                (SessionId)session.lookupFirst( null , SessionId.class );
//            if ( sessionId == null ) {
//                continue;
//            }
//            Project project = sessionId.getProject();
//            if ( getProject().equals(project) ) {
//                return sessionId;
//            }
//        }
//        return null;
//    }
    

//    private void runFilesInExistedSession( final SessionId sessionId ) {
//        Runnable runnable = new Runnable() {
//
//            public void run() {
//                if (sessionId.waitServerFile(WAIT_INIT_SESSION) == null) {
//                    /*
//                     * This could happen as result of previous error:
//                     * no php files was called as result of starting debugging
//                     * session ( f.e. action was called on project and 
//                     * index.html was opened ). 
//                     * 
//                     * Notify user about existing session.
//                     */
//                    notifyWarning();
//                    /*
//                     *  Run the same process as before but using existed session,
//                     *  without starting debugger.
//                     */ 
//                    runFilesInFreshSession( sessionId );
//                    return;
//                }
//                try {
//                    RunCommand command = getRunCommand();
//                    if ( command == null ){
//                        return;
//                    }
//                    RunCommand clonedCommand = (RunCommand) command.clone();
//                    clonedCommand
//                            .addParameter(XDEBUG_COOKIE, sessionId.getId());
//                    clonedCommand.run();
//                }
//                catch (CloneNotSupportedException e) {
//                    assert false;
//                }
//            }
//        };
//        RequestProcessor.getDefault().post(runnable);
//    }
    
//    private void runFilesInFreshSession( SessionId id ) {
//        final FileObject startFO = getFirstFile();
//        if ( startFO == null ) {
//            return;
//        }
//        
//        SessionId sessionId = id;
//        if ( sessionId == null ) {
//            sessionId = new SessionId( startFO );
//            DebuggerFactory.getDebugger().debug(  sessionId );
//        }
//        
//        final RunCommand command = getRunCommand();
//        if ( command == null ){
//            return ;
//        }
//        if ( id != null) {
//            runFiles( command , startFO, sessionId);
//        }
//        else {
//            final SessionId sessId = sessionId;
//            Runnable runnable = new Runnable() {
//                public void run() {
//                    runFiles( command , startFO, sessId );                    
//                }
//            };
//            RequestProcessor.getDefault().post(runnable);    
//        }
//        
//    }

//    private void runFiles( RunCommand command, FileObject startFO, 
//            SessionId sessionId ) 
//    {
//        try {
//            RunCommand clonedCommand = (RunCommand) command.clone();
//            clonedCommand.addParameter(XDEBUG_COOKIE, sessionId.getId());
//            clonedCommand.setActionFiles(new FileObject[] { startFO });
//            clonedCommand.run();
//
//            String serverFileUri = sessionId.waitServerFile(WAIT_INIT_SESSION);
//            if (serverFileUri == null) {
//                notifyError( startFO , WAIT_INIT_SESSION/1000 );
//                return;
//            }
//
//            FileObject[] others = getOtherFiles(startFO);
//            if (others.length > 0) {
//                clonedCommand = (RunCommand) command.clone();
//                clonedCommand.addParameter(XDEBUG_COOKIE, sessionId.getId());
//                clonedCommand.setActionFiles(getOtherFiles(startFO));
//                clonedCommand.run();
//            }
//        }
//        catch (CloneNotSupportedException e) {
//            assert false;
//        }
//    }
}
