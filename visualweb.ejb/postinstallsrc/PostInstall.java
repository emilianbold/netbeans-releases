/*
 * Copyright ? 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
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
