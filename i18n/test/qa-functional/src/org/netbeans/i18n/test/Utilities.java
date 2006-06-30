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

package org.netbeans.i18n.test;

import java.io.File;
import java.io.PrintWriter;

// ide imports
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

//import org.netbeans.test.oo.gui.jelly.JellyException;
//import org.netbeans.test.oo.gui.jello.JelloBundle;

/**
 *
 * @author  Marian.Mirilovic@czech.sun.com
 * @version
 */

public class Utilities extends Object {
    /** form bundles */
    public static String formBundle = "org.netbeans.modules.form.Bundle";
    public static String formPaletteBundle = "org.netbeans.modules.form.palette.Bundle";
    public static String formActionsBundle = "org.netbeans.modules.form.actions.Bundle";
    
    /** core bundles */
    public static String windowsCoreBundle = "org.netbeans.core.windows.Bundle";
    public static String statusCompile = "org.netbeans.core.compiler.Bundle";
    
    /** openide bundles */
    public static String openideActions = "org.openide.actions.Bundle";
    public static String openideText = "org.openide.text.Bundle";
    
    /** jdbc bundles */
    public static String jdbcPaletteBundle = "org.netbeans.modules.jdbc.resources.Bundle";
    
    //public static String OPEN = JelloBundle.getString (openideActions, "Open"); // Open
    //public static String EDIT = JelloBundle.getString (openideActions, "Edit"); // Edit
    
    /** test resource package */
    public static String Resources = "Resources";
    public static String passCaption = " PASS - \n\t";
    public static String failCaption = " FAIL - \n\t";
    public static String errorCaption = "Test ERROR : ";
    
    /* names of Top Components class */
    public static String TP_ComponentPalette = "org.netbeans.modules.form.palette.PaletteTopComponent";
    public static String TP_ComponentInspector = "org.netbeans.modules.form.ComponentInspector";
    public static String TP_FormDesigner = "org.netbeans.modules.form.FormDesigner";
    
    //public static String separ=(File.separatorChar == '/')?File.separator:"\\\\";
    public static String separ="/";
    
    /** Creates new utilities */
    public Utilities() {
    }
    
    /** Find file object by name and extension.
     * @param _pack package name
     * @param _name file name
     * @param _extension file extension
     * @return finded file object or null
     */
    public static FileObject findFileObject(String _package, String _name, String _extension) {
        //FileObject f = Repository.getDefault().find(_package, _name, _extension);
        String name="";
        //System.out.println("package "+_package+" name="+_name+" ext="+_extension);
        if (_package != null && _package.length() > 0) {
            name=_package.replaceAll("\\.",separ)+separ;
        }
        if (_name != null && _name.length() > 0) {
            name+=_name;
        }
        if (_extension != null && _extension.length() > 0) {
            name+="."+_extension;
        }
        //System.out.println("name="+name);
        FileObject f=Repository.getDefault().findResource(name);
        return f;
    }
    
    /** Find file system name.
     * @param _package package name
     * @param fileName file name
     * @param fileExtension file extension
     * @throws FileStateInvalidException
     * @return  full FileSystem name  */
    public static String getFS(String _package, String fileName, String fileExtension) throws Exception {
        FileObject f = findFileObject(_package, fileName, fileExtension);
        
        if(f == null)
            throw new Exception("Unable find file " + fileName + "." + fileExtension + " in package " + _package);
        
        String fs;
        try {
            //fs = f.getFileSystem().getSystemName();
            fs = f.getFileSystem().getDisplayName();
        } catch(FileStateInvalidException exc){
            throw new Exception("FileStateInvalidException during attempt get filesystem name for " + fileName + "." + fileExtension + " in package " + _package);
        }
        
        // hack for Win NT/2K , where in FileObject is bad file separator !!!
        //char fileSeparator = System.getProperty("file.separator").charAt(0);
        String fsName = fs.replaceAll("\\\\",separ);
        //String path = fsName+ ", " + _package + ", " + fileName;
        
        return fsName;
    }
    
    public static String getPath(String packageName, String fileName, String fileExtension, String delim) throws Exception {
        String FS_Name = getFS(packageName, fileName, fileExtension);
        return FS_Name + delim + packageName.replace('.',delim.charAt(0)) + delim + fileName;
    }
    
    public static String getPath(String packageName, String delim) throws Exception {
        String FS_Name = getFS(packageName, null, null);
        return FS_Name + delim + packageName.replace('.',delim.charAt(0));
    }
    
    public static String getSystemPath(String packageName, String fileName, String fileExtension) throws Exception {
        return getPath(packageName, fileName, fileExtension, separ)+ "." + fileExtension;
    }
    
    public static void writeExc(Exception exc, PrintWriter err) {
        err.println(errorCaption);
        exc.printStackTrace(err);
    }
    
    public static void main(java.lang.String[] args) {
        try {
            //String s=getFS("org.netbeans.test.gui.projects","Utillities","java");
            String s;//=getFS("aaa.bbb","aaa","txt");
            s=getPath("org.netbeans.test.gui.projects", "data", null, "|");
            System.out.println(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
