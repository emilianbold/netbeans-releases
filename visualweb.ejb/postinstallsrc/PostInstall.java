/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Runtime;
import java.net.URL;
import java.util.Properties;
import javax.swing.SwingUtilities;

import java.util.ResourceBundle;

/**
 *
 * @author  Winston Prakash
 * @author  Dongmei Cao
 */

public class PostInstall {
    private String ideHome = System.getProperty("ideHome"); //NOI18N
    
    public static void main(String[] args) {
        PostInstall postInstall = new PostInstall();
        postInstall.deploySampleEjbs();
        Thread.currentThread().yield() ; // allow everything else to clean up.
        System.exit(0) ;  // this is a separate java process, so just die!
    }
    
    private static String deployMsg ;
    static {
            deployMsg = ResourceBundle.getBundle("Bundle").getString("Deploy_MSG") ; // NOI18N
    }
    private void deploySampleEjbs()
    {
        try
        {
            // If the app server is not running, then start it now

            boolean serverRunning = isServerRunning();

            if( !serverRunning ) {
                ProgressWindow.showProgress() ;
                startServer();
            }
            else ProgressWindow.showProgress(deployMsg) ;

            // Invoke script deploy-ejbs.
            // On windows, invoke deploy-ejbs.bat
            // On Solaris, linuz, invoke deploy-ejbs.sh
            
            String startScriptPath = ideHome + File.separator + // NOI18N
                                     "startup" + File.separator + "bin" + // NOI18N
                                     File.separator + "deploy-ejbs";  // NOI18N

            String suffix = null;

            if (System.getProperty("os.name").startsWith("Windows")) { // NOI18N
                suffix = ".bat"; // NOI18N
            } else {
                suffix = ".sh"; // NOI18N
                
                // Add executable permission on the script
                addExecutablePermission( startScriptPath + suffix );
            }

            ProgressWindow.setMessage( deployMsg ) ;
            
            RunCommand runCommand = new RunCommand();
            runCommand.execute( startScriptPath + suffix );

            int retStat = runCommand.getReturnStatus() ;
            if(retStat< 0){
                String errmsg = "Error occured while executing - " + startScriptPath + suffix ;
                System.err.println(errmsg);
            }
            
        }
        catch( java.io.IOException ex )
        {
            ex.printStackTrace();
        }
        finally {
            ProgressWindow.hideProgress() ;
        }
    }
    
    /* chmod on the deploy-ejbs.sh to have executable permission */
    private void addExecutablePermission( String file )
    {
        try {
            RunCommand runCommand = new RunCommand();
            runCommand.execute( "chmod +x " + file );
            if(runCommand.getReturnStatus() < 0){
                System.err.println("Error occured while executing - chmod +x " + file );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
    
    /* Start the Application Server using <IDE_HOME>/startup/bin/start-pe.bat|sh */
    private void startServer(){
        String suffix = null;
        
        if (System.getProperty("os.name").startsWith("Windows")) { // NOI18N
            suffix = ".bat"; // NOI18N
        } else {
            suffix = ".sh"; // NOI18N
        }
        
        String startScriptPath = ideHome + File.separator + // NOI18N
        "startup" + File.separator + "bin" + // NOI18N
        File.separator + "pe-start";  // NOI18N
        
        try {
            RunCommand runCommand = new RunCommand();
            runCommand.execute(startScriptPath + suffix);
            if(runCommand.getReturnStatus() < 0){
                System.err.println("Error occured while executing - " + startScriptPath + suffix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
    
    
    /* Check if the Server is running. The checking is done by testing
       the http connection to the admin port */
    private boolean isServerRunning() throws IOException{
        URL testURL = null;
        try {
            testURL = new URL("http://localhost:" + getServerAdminPort());
            return testConnection(testURL, 10);
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }
    
    /* Check if the HTTP port is accespting connections based on the URL */
    private boolean testConnection(URL testURL, int timeout) {
        
        int i;
        for (i = 0; i < timeout; i++) {
            try {
                testURL.openConnection();
                testURL.openStream();
                return true;
            } catch (Exception e) {
                if (i < (timeout -1)) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception threadException) {
                    }
                }
            }
        }
        return false;
    }
    
    /* Get the Server Admin Port from the install.properties as set by installer*/
    private String getServerAdminPort(){
        String installPropsPath = ideHome + "/system/install.properties"; //NOI18N
        File installPropsFile = new File(installPropsPath);
        if(installPropsFile.exists()){
            try{
                Properties installProps = new Properties();
                installProps.load(new FileInputStream(installPropsFile));
                String asAdminPort = installProps.getProperty("adminPort"); //NOI18N
                if(asAdminPort != null){
                    return asAdminPort;
                }
            }catch(IOException exc){
                exc.printStackTrace();
            }
        }
        return "14848";
    }
}
