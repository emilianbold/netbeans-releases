/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.awt;

import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.*;
import org.openide.loaders.*;

/** Checks the consistence of Menu folder.
 *
 * @author Jaroslav Tulach
 */
public class ValidateLayerMenuTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public ValidateLayerMenuTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ValidateLayerMenuTest.class);
        
        return suite;
    }
    
    //
    // override in subclasses
    //
    
    protected String rootName () {
        return "Menu";
    }
    
    /** Allowes to skip filest that are know to be broken */
    protected boolean skipFile (FileObject fo) {
        // ignore these files, there are helper for implementation of 
        // View/filesystems, View/Runtime, View/Projects, etc.
        return fo.getPath().startsWith ("Menu/Window/oldRoots") && fo.hasExt ("txt");
    }
    
    protected boolean correctInstance (Object obj) {
        if (obj instanceof javax.swing.Action) {
            return true;
        }
        if (obj instanceof org.openide.util.actions.Presenter.Menu) {
            return true;
        }
        if (obj instanceof javax.swing.JSeparator) {
            return true;
        }
        if (obj instanceof javax.swing.JMenuItem) {
            return true;
        }
        
        return false;
    }
    
    
    //
    // the test
    // 
    
    public void testContentCorrect () throws Exception {
        java.util.ArrayList errors = new java.util.ArrayList ();
        
        DataFolder df = DataFolder.findFolder (Repository.getDefault().getDefaultFileSystem().findResource (rootName ()));
        verifyMenu (df, errors);
        
        if (!errors.isEmpty()) {
            fail ("Some files do not provide valid menu elements" + errors);
        }
    }
    
    private void verifyMenu (DataFolder f, java.util.ArrayList errors) throws Exception {
        DataObject[] arr = f.getChildren();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof DataFolder) {
                verifyMenu ((DataFolder)arr[i], errors);
                continue;
            } 
            FileObject file = arr[i].getPrimaryFile ();
            
            if (skipFile (file)) {
                continue;
            }
            
            Object url = file.getURL();
            
            InstanceCookie ic = (InstanceCookie)arr[i].getCookie(InstanceCookie.class);
            if (ic == null) {
                errors.add ("\n    File " + file + " does not have instance cookie, url: " + url);
                continue;
            }
            
            try {
                Object obj = ic.instanceCreate();
                if (correctInstance (obj)) {
                    continue;
                }
                errors.add ("\n    File " + arr[i].getPrimaryFile () + " does not provide correct instance: " + obj + " url: " + url);
            } catch (Exception ex) {
                errors.add ("\n    File " + arr[i].getPrimaryFile () + " cannot be read " + ex + " url: " + url);
            }
        }
    }
    
}

