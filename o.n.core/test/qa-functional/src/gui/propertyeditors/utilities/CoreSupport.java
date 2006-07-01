/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors.utilities;

import java.io.PrintStream;
import java.io.IOException;
import java.awt.Component;
import javax.swing.JDialog;
import org.netbeans.jellytools.JellyTestCase;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;

import org.netbeans.junit.NbTestCase;

// ide imports
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;

/** Utilities for core tests. From this class extends supports for each testsuite.
 *
 * @author  Marian.Mirilovic@Sun.Com
 * @version
 */

public class CoreSupport {
    
    /** Creates new utilities */
    public CoreSupport() {
    }
    
    /**
     * Find path to the Sample Project.
     * 
     * @return path to the Sample Project 
     * @param testCase 
     */
    public static String getSampleProjectPath(JellyTestCase testCase) {
        return new java.io.File(testCase.getDataDir(),"SampleProject").getPath();
    }
    
    
    /**
     * Find file system name.
     * 
     * @param exc 
     * @param err 
     */
/*    public static String getFS(String _package, String fileName, String fileExtension){
        FileObject f = findFileObject(_package, fileName, fileExtension);
        
        if(f==null)
            throw new JemmyException("Unable find file " + fileName + "." + fileExtension + " in package " + _package);
        
        String fs;
        try{
            fs = f.getFileSystem().getSystemName();
        }catch(FileStateInvalidException exc){
            throw new JemmyException("FileStateInvalidException during attempt get filesystem name for " + fileName + "." + fileExtension + " in package " + _package);
        }
        
        // hack for Win NT/2K , where in FileObject is bad file separator !!!
        char fileSeparator = System.getProperty("file.separator").charAt(0);
        String fsName = fs.replace('/',fileSeparator).replace('\\',fileSeparator);
        //String path = fsName+ ", " + _package + ", " + fileName;
        
        return fsName;
    }
    
    
    public static String getPath(String packageName, String fileName, String fileExtension, String delim){
        String FS_Name = getFS(packageName, fileName, fileExtension);
        return FS_Name + delim + packageName.replace('.',delim.charAt(0)) + delim + fileName;
    }
    
    public static String getSystemPath(String packageName, String fileName, String fileExtension){
        String fileSeparator = System.getProperty("file.separator");
        return getPath(packageName, fileName, fileExtension, fileSeparator)+ "." + fileExtension;
    }
*/    
    public static void writeExc(Exception exc, PrintStream err) {
        err.println("Test ERROR: ");
        exc.printStackTrace(err);
    }
    
    /**
     * 
     * @param testCase 
     */
    public static void makeIDEScreenshot(NbTestCase testCase) {
        try{
            testCase.getWorkDir();
            org.netbeans.jemmy.util.PNGEncoder.captureScreen(testCase.getWorkDirPath()+System.getProperty("file.separator")+"IDEscreenshot.png");
        }catch(Exception ioexc){
            testCase.log("Impossible make IDE screenshot!!! \n" + ioexc.toString());
        }
    }
    
    /**
     * 
     * @param testCase 
     * @param component 
     */
    public static void makeWindowScreenshot(NbTestCase testCase, Component component) {
        try{
            testCase.getWorkDir();
            if(component != null)
                org.netbeans.jemmy.util.PNGEncoder.captureScreen(component,testCase.getWorkDirPath()+System.getProperty("file.separator")+"ComponentScreenshot.png");
            else
                makeIDEScreenshot(testCase);
        }catch(Exception ioexc){
            testCase.log("Impossible make component screenshot!!! \n =========" +component.toString() + "\n ======== \n " + ioexc.toString());
        }
    }
    
    
    public static void closeAllModal() {                                                                                                                  
        JDialogOperator oper = null;                                                                                                               
        // find some JDialog                                                                                                                       
        JDialog jDialog = JDialogOperator.findJDialog(ComponentSearcher.getTrueChooser(""));                                                       
        // number of opened non-modal                                                                                                              
        int nonModal = 0;                                                                                                                          
        // until any modal dialog is opened                                                                                                        
        while(jDialog!=null) {                                                                                                                     
            oper = new JDialogOperator(jDialog);                                                                                                   
            if(oper.isModal()) {                                                                                                                   
                // close if modal                                                                                                                  
                oper.close();                                                                                                                      
            } else {                                                                                                                               
                // increment nonModal                                                                                                              
                nonModal++;                                                                                                                        
            }                                                                                                                                      
            // use nonModal variable as index to skip opened non-modal dialogs                                                                     
            jDialog = JDialogOperator.findJDialog(ComponentSearcher.getTrueChooser(""), nonModal);                                                 
        }                                                                                                                                          
    }                                                                                                                                              

}
