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
import java.util.Iterator;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.*;

import org.openide.loaders.DataObject;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

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
            
            if ("Shortcuts/D-BACK_QUOTE.shadow".equals(fo.getPath())) {
                // #46753
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
            } catch (Exception ex) {
                ex.printStackTrace();
                errors.add ("\n    File " + fo + " thrown exception " + ex);
            }
        }
        
        if (!errors.isEmpty()) {
            fail ("Some instances cannot be created " + errors);
        }
    }
    
    public void testIfOneFileIsDefinedTwiceByDifferentModulesTheyNeedToHaveMutualDependencyBetweenEachOther () throws Exception {
        ClassLoader l = (ClassLoader)org.openide.util.Lookup.getDefault ().lookup (ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        // String -> List<Modules>
        java.util.HashMap files = new java.util.HashMap ();
        
        boolean atLeastOne = false;
        java.util.Enumeration en = l.getResources ("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            java.net.URL u = (java.net.URL)en.nextElement ();
            java.util.jar.Manifest mf = new java.util.jar.Manifest (u.openStream ());
            String module = mf.getMainAttributes ().getValue ("OpenIDE-Module");
            if (module == null) continue;
            String layer = mf.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
            if (layer == null) continue;
            
            atLeastOne = true;
            java.net.URL layerURL = new java.net.URL (u, "../" + layer);
            org.openide.filesystems.XMLFileSystem fs = new org.openide.filesystems.XMLFileSystem (layerURL);
            
            java.util.Enumeration all = fs.getRoot ().getChildren (true);
            while (all.hasMoreElements ()) {
                FileObject fo = (FileObject)all.nextElement ();
                if (!fo.isData ()) continue;
                
                java.util.List list = (java.util.List)files.get (fo.getPath ());
                if (list == null) {
                    list = new java.util.ArrayList ();
                    files.put (fo.getPath (), list);
                }
                list.add (module);
            }
        }
        
        java.util.Iterator it = files.entrySet ().iterator ();
        StringBuffer sb = new StringBuffer ();
        while (it.hasNext ()) {
            java.util.Map.Entry e = (java.util.Map.Entry)it.next ();
            java.util.List list = (java.util.List)e.getValue ();
            if (list.size () == 1) continue;
            
            Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (ModuleInfo.class));
            assertFalse ("Some modules found", res.allInstances ().isEmpty ());
            
            java.util.Iterator names = new java.util.ArrayList (list).iterator ();
            while (names.hasNext ()) {
                String name = (String)names.next ();
                java.util.Iterator modules = res.allInstances ().iterator ();
                while (modules.hasNext ()) {
                    ModuleInfo info = (ModuleInfo)modules.next ();
                    if (name.equals (info.getCodeName ())) {
                        Iterator deps = info.getDependencies ().iterator ();
                        while (deps.hasNext ()) {
                            Dependency d = (Dependency)deps.next ();
                            list.remove (d.getName ());
                        }
                        // remove dependencies
                        list.removeAll (info.getDependencies ());
                    }
                }
            }
            // ok, modules depend on each other
            if (list.size () <= 1) continue;
            
            sb.append (e.getKey () + " is provided by: " + list + "\n");
        }        
        
        assertTrue ("At least one layer file is usually used", atLeastOne);
        
        if (sb.length () > 0) {
            fail ("Some modules override their files and do not depend on each other\n" + sb);
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
        if (s.startsWith ("Shortcuts/D-BACK_QUOTE.shadow")) return true;
        
        return false;
    }
}
