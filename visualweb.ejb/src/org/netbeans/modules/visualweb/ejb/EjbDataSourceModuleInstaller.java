/*
 * EjbModuleInstaller.java
 *
 * Created on May 4, 2004, 10:20 AM
 */

package org.netbeans.modules.visualweb.ejb;
import java.io.File;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.visualweb.extension.openide.io.RaveFileCopy;

/**
 * This class is to initialize the necessary information when
 * the Ejb data source module gets first installed or restored.
 *
 * @author  cao
 */
public class EjbDataSourceModuleInstaller extends ModuleInstall 
{
    /*public void installed() 
    {
        restored();
    }*/
    
    public void restored() 
    {
        // First, copy the samples
        copySamples();
        
        // Load the ejbs
        try
        {
            EjbDataSourceManager.getInstance().load();
        }
        catch( Exception e )
        {
            // Problem load the ejb data source. 
            // Log a waring and go on
            ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.ejb.EjbDataSourceModuleInstaller").log( ErrorManager.WARNING, "Failed to load ejbdatasource.xml when restoring ejb module" );
            e.printStackTrace();
        }
    }
    
    public void close() {
        // Save all the information to a xml file
        EjbDataSourceManager.getInstance().save();
    }
    
     public void uninstalled() {
         close();
     }
    
    private void copySamples()
    {
        String resetWm = System.getProperty("resetWindowManager");
        
        // Copy ejbsource.xml to the user dir
        File ejbDir = new File(System.getProperty("netbeans.user"), "ejb-datasource"); // NOI18N
        File sampleEjbDir = InstalledFileLocator.getDefault().locate("samples/ejb/ejb-datasource", null, false ); // NOI18N
        
        if ("true".equals(resetWm))
            deleteDirectory(ejbDir);
        
        if( !ejbDir.exists()) {
            File[] files = sampleEjbDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if( files[i].getName().indexOf( ".xml" ) != -1 )
                    copyFile(files[i],  new File(ejbDir, files[i].getName()));
            }
        }
    }
    
    private void copyFile(File src, File dest) {
        try {
            RaveFileCopy.fileCopy(src, dest);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    
    private void deleteDirectory(File dir){
        if (!dir.delete()) {
            if (dir.isDirectory()) {
                java.io.File list[] = dir.listFiles();
                for (int i=0; i < list.length ; i++) {
                    deleteDirectory(list[i]);
                }
            }
            dir.delete();
        }
    }
    
}
