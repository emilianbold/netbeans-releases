/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.setup;

import java.beans.PropertyVetoException;

import java.io.File;
import java.io.IOException;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jemmy.operators.JMenuBarOperator;


public class IDESetupTest extends org.netbeans.junit.NbTestCase {
    
    public IDESetupTest(java.lang.String testName) {
        super(testName);
    }

    
    public void testOpenWebProject() {
        org.netbeans.junit.ide.ProjectSupport.openProject(System.getProperty("xtest.data")+"/PerformanceTestWebApplication");
    }

    
    /** 
     * Close Welcome. 
     */
    public void testCloseWelcome(){
        new TopComponentOperator("Welcome").close(); //NOI18N 
        
    }
    
    
    /** 
     * Close Memory Toolbar. 
     */
    public void testCloseMemoryToolbar(){
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu("View|Toolbars|Memory","|");
    }
    
    
/*    private FileSystem installVcs(Repository r, File f) {
        final CommandLineVcsFileSystem fs = new CommandLineVcsFileSystem ();
        try {
            // set config file and turn off refresh
            String profile = "cvs.xml"; // NOI18N
            System.out.println("VCS profile = "+profile);
            if (fs.readConfiguration(profile)) {
                fs.setConfigFileName(profile);
            }
            fs.setAutoRefresh(GeneralVcsSettings.AUTO_REFRESH_NO_REFRESH);
            fs.disableRefresh();

            fs.setRootDirectory(f);
            r.addFileSystem(fs);
// promoD            Task t = new RequestProcessor ().post(new Runnable() {
//                public void run() {
//                    JCStorage storage = JCStorage.getStorage();
//                    storage.parseFSOnBackground (fs);
//                }
//            });  
//            t.waitFinished();
            // TODO should wait until code completion is created
        }
        catch (IOException ioe) {
            fail ("Filesystem mounting failed - "+ioe.getLocalizedMessage());
        }
        catch (PropertyVetoException ioe) {
            fail ("Filesystem mounting failed - "+ioe.getLocalizedMessage());
        }
        return fs;
    }
 */
}
