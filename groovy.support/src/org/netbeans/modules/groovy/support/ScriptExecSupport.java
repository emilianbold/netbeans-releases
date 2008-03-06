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

package org.netbeans.modules.groovy.support;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.InstanceCookie;
import org.openide.execution.ExecutionEngine;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/** 
 * Support class for generic script execution.
 *
 * @author Petr Hamernik
 */
class ScriptExecSupport {

    /** This is a prefix of environment properties which has to be removed
     * and the rest is passed down to Runnable.exec
     */
    private static final String ENV_PREFIX_TO_REMOVE = "ENV-";

    /** The object to be executed
     */
    private FileObject fileObject;
    
    /** Creates the execution support for the dataObject.
     * @param dataObject The object to be executed
     */
    public ScriptExecSupport(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    /** Starts the script runnable in ExecutionEngine
     */
    public void runScript() {
        try {
            // try the project system first - only if the action is disabled
            // or something fails resort to the execution engine
            FileObject f = Repository.getDefault().getDefaultFileSystem().findResource(
                    "Actions/Project/org-netbeans-modules-project-ui-RunSingle.instance"); // NOI18N
            if (f != null) {
                DataObject dobj = DataObject.find(f);
                InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                Object obj = ic.instanceCreate();
                if (obj instanceof Action) {
                    Action a = (Action)obj;
                    if (a.isEnabled()) {
                        a.actionPerformed(new ActionEvent(this, 0, null));
                        return;
                    }
                }
            }
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
        }
        // 1. Save all modified files
        LifecycleManager.getDefault().saveAll();

        // 2. Prepare runnable
        ProcessRunnable toRun = null;
        try {
            toRun = new ProcessRunnable();
        }
        catch ( Exception exc ) {
            String msg = exc.getMessage();
            if ( ( msg == null ) || ( msg.length() == 0 ) ) {
                msg = exc.getClass().toString();
                if ( exc.getStackTrace().length > 0 ) {
                    msg = msg + " " + exc.getStackTrace()[0].toString();
                }
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message( msg, NotifyDescriptor.Message.ERROR_MESSAGE );
            DialogDisplayer.getDefault().notify( nd );
            return;
        }
        
        // 2. Creates tab in output window with the name of the dataobject
        InputOutput io = IOProvider.getDefault().getIO( fileObject.getNameExt(), false );
        toRun.setIO( io );
        try {
            io.getOut().reset();
        }
        catch (IOException exc ) {
            // doesn't matter if output window can't be cleared before script starts.
        }
        
        // 4. Run it
        ExecutionEngine.getDefault().execute( fileObject.getNameExt(), toRun, io );
    }

    /** The runnable which launch new process out of this VM
     */
    private class ProcessRunnable implements Runnable {
        
        /** The InputOutput where this process should print to.
         */
        InputOutput io;
        
        /** Command to be run
         */
        String[] command;
        
        /** Environment for the external process.
         */
        String[] env;
        
        /** Create new ProcessRunnable
         * @param io Where to print the out/err from the external process
         */
        ProcessRunnable() {
            command = createCommand();
            env = createEnv();
        }
        
        void setIO( InputOutput io ) {
            this.io = io;
        }

        /** Collects all elements of command line
         */
        private String[] createCommand() {
            String []res = new String[2];
            res[0] = getScriptExec();

            File f = FileUtil.toFile( fileObject );
            res[1] = f.getAbsolutePath();

            return res;
        }

        /** Prepare the environment for external process
         */
        private String[] createEnv() {
            ArrayList<String> newEnv = new ArrayList<String>();
            Iterator iter = System.getProperties().entrySet().iterator();
            while ( iter.hasNext() ) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = entry.getKey().toString();
                if ( key.toUpperCase().startsWith( ENV_PREFIX_TO_REMOVE ) ) {
                    String item = key.substring( ENV_PREFIX_TO_REMOVE.length() ) + "=" + entry.getValue().toString();  //NOI18N
                    newEnv.add( item );
                }
            }
            newEnv.add( "CLASSPATH=" + getClassPath() ); // NOI18N
            return newEnv.toArray( new String[newEnv.size()] ); 
        }

        /** Display a message in status line.
         * @param key The key of message (in resource bundle) which should be printed.
         */
        private void displayMsg( String key ) {
            MessageFormat fmt = new MessageFormat( NbBundle.getMessage( ScriptExecSupport.class, key ) );
            String msg = fmt.format( new Object[] { fileObject.getNameExt() } );
            StatusDisplayer.getDefault().setStatusText( msg );
        }

        /** Run script as external process
         */
        public void run() {
            try {
                displayMsg( "MSG_ScriptStarted" ); //NOI18N
                if ( io != null ) {
                    io.getOut().println( command[0] + " " + command[1] );
                }
                
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec( command, null );
                
                if ( io != null ) {
                    StreamRedirect errorGobbler = new StreamRedirect( fileObject, proc.getErrorStream(), io.getErr() );
                    StreamRedirect outputGobbler = new StreamRedirect( fileObject, proc.getInputStream(), io.getOut() );
                    errorGobbler.start();
                    outputGobbler.start();
                }

                int exitVal = proc.waitFor();
                displayMsg( ( exitVal == 0 ) ? "MSG_ScriptFinished" : "MSG_ScriptFailed" ); //NOI18N
                
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /** Only access method 
     */
//    GroovyDataObject getDataObject() {
//        return dataObject;
//    }

    /** Creates the text form (platform dependent) of the classpath.
     * The classpath is obtained from current project.
     *
     * @return The classpath for external script execution
     */
    String getClassPath() {
        Project project = FileOwnerQuery.getOwner( fileObject);

        ClassPathProvider cpp = project.getLookup().lookup( ClassPathProvider.class );
        if ( cpp == null )
            return ""; //NOI18N
        
        ClassPath cp = cpp.findClassPath( fileObject, ClassPath.EXECUTE );
        if ( cp == null )
            return ""; //NOI18N
        
        FileObject[] roots = cp.getRoots();
        if (roots.length == 0)
            return ""; //NOI18N
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < roots.length; i++) {
            if ( i > 0 ) {
                buf.append( File.pathSeparatorChar );
            }
            
            try {
                URL url=roots[i].getURL();
                if (url.getProtocol().equals("jar")) { // NOI18N
                    url=FileUtil.getArchiveFile(url);
                }
                URI uri = new URI(url.toExternalForm());
                File f =  new File(uri);
                
                buf.append(f.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
	return buf.toString();  
    }

    public String getScriptExec() {
        GroovySettings groovyOption = new GroovySettings();
        File home = new File(groovyOption.getGroovyHome());
        if ( ( home == null ) || (!home.exists() ) ) {
            String msg = NbBundle.getMessage( ScriptExecSupport.class, "MSG_HomeNotSet" );
            throw new IllegalStateException( msg );
        }
        
        StringBuffer buf = new StringBuffer();
        
        buf.append( home.getAbsolutePath() );
        buf.append( Utilities.isWindows() ? "\\bin\\groovy.bat" : "/bin/groovy" ); //NOI18N
        
        return buf.toString();
    }

}
