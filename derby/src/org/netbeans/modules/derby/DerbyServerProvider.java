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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.spi.db.explorer.ServerProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 *
 * @author  Ludo, Petr Jiricka, David Van Couvering
 */
public class DerbyServerProvider implements ServerProvider {
    
    // XXX this class does too much. Should maybe be split into 
    // DatabaseRuntimeImpl, DerbyStartStop and the rest.
    
    // XXX refactor this soon, it is full of race conditions!
    
    private static final Logger LOGGER = Logger.getLogger(DerbyServerProvider.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private static final int START_TIMEOUT = 5; // seconds
    
    private static DerbyServerProvider reg=null;
    ArrayList actions = new ArrayList();
    
    /** Derby server process */
    static Process process = null;
    
    /** Creates a new instance of DerbyServerProvider */
    private DerbyServerProvider() {
        actions.add(new StartAction());
        actions.add(new StopAction());
        actions.add(new CreateDatabaseAction());
        actions.add(new DerbySettingsAction());        
    }
    
    public static synchronized DerbyServerProvider getDefault(){
        if (reg==null)
            reg= new DerbyServerProvider();
        return reg;
    }
    
    /**
     * Whether this runtime accepts this connection string.
     */
    public boolean acceptsDatabaseURL(String url){
        return url.trim().startsWith("jdbc:derby://localhost"); // NOI18N
    }
    
    /**
     * Is database server up and running.
     */
    public boolean isRunning(){
        if (process!=null){
            try{
                int e = process.exitValue();
                process=null;
            } catch (IllegalThreadStateException e){
                //not exited yet...it's ok
                
            }
        }
        return (process!=null);
        
    }
    
    public String getJDBCDriverClass() {
        return DerbyOptions.DRIVER_CLASS_NET;
    }
    
    /**
     * Can the database be started from inside the IDE?
     */
    public boolean canStart(){
        // issue 81619: should only try to start if the location is set
        return DerbyOptions.getDefault().getLocation().length() > 0;
    }
    
    /**
     * Start the database server.
     */
    public void start(){
        start(START_TIMEOUT);
    }
    
    public boolean canRegister() {
        return true;
    }

    public List getActions() {
        return actions;
    }

    public String getDisplayName() {
        return NbBundle.getBundle(DerbyServerProvider.class).getString("LBL_JavaDBServer");
    }

    public String getShortDescription() {
        return NbBundle.getBundle(DerbyServerProvider.class).getString("DSC_JavaDBServer");
    }
    
    private String getNetworkServerClasspath() {
        return 
            Util.getDerbyFile("lib/derby.jar").getAbsolutePath() + File.pathSeparator +
            Util.getDerbyFile("lib/derbytools.jar").getAbsolutePath() + File.pathSeparator +
            Util.getDerbyFile("lib/derbynet.jar").getAbsolutePath(); // NOI18N
    }
        
    public int getPort() {
        return 1527;
    }
    
    /** Posts the creation of the new database to request processor.
     */
    void postCreateNewDatabase(final String databaseName, final String user, final String password) throws Exception {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    // DerbyDatabases.createDatabase would start the database too, but
                    // doing it beforehand to avoid having two progress bars running
                    if (!ensureStarted(true)) {
                        return;
                    }
                    
                    ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                        DerbyServerProvider.class, "MSG_CreatingDBProgressLabel", databaseName));
                    ph.start();
                    try {
                        DerbyDatabases.createDatabase(databaseName, user, password);
                    } finally {
                        ph.finish();
                    }
               } catch (Exception e) {
                    LOGGER.log(Level.WARNING, null, e);
                    String message = NbBundle.getMessage(DerbyServerProvider.class, "ERR_CreateDatabase", e.getMessage());
                    Util.showInformation(message);
               }
           }
        });
    }
    
    private String getDerbySystemHome() {
        // return System.getProperty("netbeans.user") + File.separator + "derby";
        return DerbyOptions.getDefault().getSystemHome();
    }
    
    private void createDerbyPropertiesFile() {
        File derbyProperties = new File(getDerbySystemHome(), "derby.properties");
        if (derbyProperties.exists())
            return;
        Properties derbyProps = new Properties();
        // fill it
        if (Utilities.isMac()) {
            derbyProps.setProperty("derby.storage.fileSyncTransactionLog", "true");
        }

        // write it out
        OutputStream fileos = null; 
        try {
            File derbyPropertiesParent = derbyProperties.getParentFile();
            derbyPropertiesParent.mkdirs();
            fileos = new FileOutputStream(derbyProperties);
            derbyProps.store(fileos, NbBundle.getMessage(DerbyServerProvider.class, "MSG_DerbyPropsFile"));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (fileos != null) {
                try {
                    fileos.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        
    }
    
    private File getInstallLocation() {
        String location = DerbyOptions.getDefault().getLocation();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new File(location);
    }
            
    private String[] getEnvironment() {
        String location = DerbyOptions.getDefault().getLocation();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new String[] { "DERBY_INSTALL=" + location }; // NOI18N
    }
    
    private JavaPlatform getJavaPlatform() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        return jpm.getDefaultPlatform(); 
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     *
     * @param  waitTime the time to wait. If less than or equal to zero, do not
     *         wait at all.
     *
     * @return true if the server is definitely started, false otherwise (the server is
     *         not started or the status is unknown). If <code>waitTime</code> was
     *         less than or equal to zero, then always false.
     */
    private boolean start(int waitTime){
        if (process!=null){// seems to be already running?
            stop();
        }
        if (!Util.checkInstallLocation()) {
            return false;
        }
        try {
            ExecSupport ee= new ExecSupport();
            ee.setStringToLookFor("" + getPort());
            FileObject javaFO = getJavaPlatform().findTool("java");
            if (javaFO == null)
                throw new Exception (NbBundle.getMessage(DerbyServerProvider.class, "EXC_JavaExecutableNotFound"));
            String java = FileUtil.toFile(javaFO).getAbsolutePath();
            
            // create the derby.properties file
            createDerbyPropertiesFile();
            
            // java -Dderby.system.home="<userdir/derby>" -classpath  
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl start
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" + 
              " org.apache.derby.drda.NetworkServerControl start"
            );
            if (LOG) {
                LOGGER.log(Level.FINE, "Running " + desc.getProcessName() + " " + desc.getArguments());
            }
            process = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );

            ee.displayProcessOutputs(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"));
            if (waitTime > 0) {
                // to make sure the server is up and running
                return waitStart(ee, waitTime);
            } else {
                return false;
            }
        } catch (Exception e) {
            Util.showInformation(e.getLocalizedMessage());
            return false;
        }
    }
    
    private boolean waitStart(ExecSupport execSupport, int waitTime) {
        boolean started = false;
        String waitMessage = NbBundle.getMessage(DerbyServerProvider.class, "MSG_StartingDerby");
        ProgressHandle progress = ProgressHandleFactory.createHandle(waitMessage);
        progress.start();
        try {
            while (!started) {
                started = execSupport.waitForMessage(waitTime * 1000);
                if (!started) {
                    String title = NbBundle.getMessage(DerbyServerProvider.class, "LBL_DerbyDatabase");
                    String message = NbBundle.getMessage(DerbyServerProvider.class, "MSG_WaitStart", waitTime);
                    NotifyDescriptor waitConfirmation = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(waitConfirmation) != NotifyDescriptor.YES_OPTION) {
                        break;
                    }
                }
            }
            if (!started) {
                LOGGER.log(Level.WARNING, "Derby server failed to start"); // NOI18N
            }
        } finally {
            progress.finish();
        }
        return started;
    }
    
    /**
     * Stop the database server.
     */
    public void stop(){
        try {
            if (process==null){//nothing to stop...
                return;
            }
            //BufferedWriter processIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            //processIn.write("q\ny\n");
            //processIn.flush();
            String java = FileUtil.toFile(getJavaPlatform().findTool("java")).getAbsolutePath();
            if (java == null)
                throw new Exception (NbBundle.getMessage(DerbyServerProvider.class, "EXC_JavaExecutableNotFound"));
            // java -Dderby.system.home="<userdir/derby>" -classpath  
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl shutdown
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" + 
              " org.apache.derby.drda.NetworkServerControl shutdown"
            );
            if (LOG) {
                LOGGER.log(Level.FINE, "Running " + desc.getProcessName() + " " + desc.getArguments());
            }
            Process shutwownProcess = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );
            shutwownProcess.waitFor();

            process.destroy();
        } 
        catch (Exception e) {
            Util.showInformation(e.getMessage());
        }
        finally {
            process=null;
        }
    }
    
    /**
     * Starts the server if necessary, and can wait for it to start if it was
     * not already started.
     *
     * @param  waitIfNotStarted true if to wait for a certain period of time for the server to start 
     *         if it is not already started; false otherwise.
     *
     * @return true if the server is definitely known to be started, false otherwise.
     */
    public boolean ensureStarted(boolean waitIfNotStarted) {
        if (isRunning()) {
            return true;
        }
        if (!canStart()) {
            return false;
        }
        if (waitIfNotStarted) {
            return start(START_TIMEOUT);
        } else {
            start(0);
            return false;
        }
    }
}
