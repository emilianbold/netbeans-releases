/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.*;

import org.openide.loaders.DataObject;

/** Checks the consistence of System File System content.
 *
 * @author Jaroslav Tulach
 */
public class ValidateLayerConsistencyTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public ValidateLayerConsistencyTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ValidateLayerConsistencyTest.class);
        
        return suite;
    }

    protected boolean runInEQ() {
        return true;
    }
    
    public void testAreAttributesFine () {
        java.util.ArrayList errors = new java.util.ArrayList ();
        
        java.util.Enumeration files = Repository.getDefault().getDefaultFileSystem().getRoot ().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = (FileObject)files.nextElement();
            
            // XXX #16761 Removing attr in MFO causes storing special-null value even in unneeded cases.
            // When the issue is fixed remove this hack.
            if("Windows2/Modes/debugger".equals(fo.getPath()) // NOI18N
            || "Windows2/Modes/explorer".equals(fo.getPath())) { // NOI18N
                continue;
            }
            
            java.util.Enumeration attrs = fo.getAttributes();
            while (attrs.hasMoreElements()) {
                String name = (String)attrs.nextElement();
                
                if (fo.getAttribute(name) == null) {
                    errors.add ("\n    File " + fo + " attribute name " + name);
                }
            }
        }
        
        if (!errors.isEmpty()) {
            fail ("Some attributes in files are unreadable" + errors);
        }
    }
    
    
    public void testContentCanBeRead () {
        java.util.ArrayList errors = new java.util.ArrayList ();
        byte[] buffer = new byte[4096];
        
        java.util.Enumeration files = Repository.getDefault().getDefaultFileSystem().getRoot ().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = (FileObject)files.nextElement();
            
            if (!fo.isData ()) {
                continue;
            }
            long size = fo.getSize();
            
            try {
                InputStream is = fo.getInputStream();
                long read = 0;
                for (;;) {
                    int len = is.read (buffer);
                    if (len == -1) break;
                    read += len;
                }
                is.close ();
                
                if (size != -1) {
                    assertEquals ("The amount of data in stream is the same as the length", size, read);
                }
                
            } catch (java.io.IOException ex) {
                errors.add ("\n    File " + fo + " cannot be read " + ex);
            }
        }
        
        if (!errors.isEmpty()) {
            fail ("Some files are unreadable" + errors);
        }
    }
    
    public void testInstantiateAllInstances () {
        java.util.ArrayList errors = new java.util.ArrayList ();
        
        java.util.Enumeration files = Repository.getDefault().getDefaultFileSystem().getRoot ().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = (FileObject)files.nextElement();
            
            if (skipFile(fo.getPath())) {
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = (InstanceCookie)obj.getCookie (InstanceCookie.class);
                if (ic != null) {
                    Object o = ic.instanceCreate ();
                }
            } catch (ClassNotFoundException ex) {
                errors.add ("\n    File " + fo + " thrown exception " + ex);
            } catch (java.io.IOException ex) {
                errors.add ("\n    File " + fo + " thrown exception " + ex);
            }
        }
        
        if (!errors.isEmpty()) {
            fail ("Some instances cannot be created " + errors);
        }
    }
    
    private boolean skipFile (String s) {
        if (s.startsWith ("Templates/") && !s.startsWith ("Templates/Services")) {
            if (s.endsWith (".shadow") || s.endsWith (".java")) {
                return true;
            }
        }
        
        if (s.startsWith ("Templates/GUIForms")) return true;
        if (s.startsWith ("Palette/Borders/javax-swing-border-")) return true;
        if (s.startsWith ("Palette/Layouts/javax-swing-BoxLayout")) return true;
        if (s.startsWith ("Templates/Beans/")) return true;
        if (s.startsWith ("PaletteUI/org-netbeans-modules-form-palette-CPComponent")) return true;
        if (s.startsWith ("Templates/Ant/CustomTask.java")) return true;
        if (s.startsWith ("Templates/Privileged/Main.shadow")) return true;
        if (s.startsWith ("Templates/Privileged/JFrame.shadow")) return true;
        if (s.startsWith ("Templates/Privileged/Class.shadow")) return true;
        if (s.startsWith ("Templates/Classes")) return true;
        if (s.startsWith ("Templates/JSP_Servlet")) return true;
        if (s.startsWith ("EnvironmentProviders/ProfileTypes/Execution/nb-j2ee-deployment.instance")) return true;
        if (s.startsWith ("Shortcuts/C-BACK_QUOTE.shadow")) return true;
        
        return false;
    }
}
