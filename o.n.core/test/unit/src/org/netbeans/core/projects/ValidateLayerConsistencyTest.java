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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/** Checks consistency of System File System contents.
 *
 * @author Jaroslav Tulach
 */
public class ValidateLayerConsistencyTest extends NbTestCase {
    
    private ClassLoader contextClassLoader;   
    
    public ValidateLayerConsistencyTest(String name) {
        super (name);
    }
    
    public static Test suite() {
        return new NbTestSuite(ValidateLayerConsistencyTest.class);
    }

    public void setUp() {
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                contextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class));
                return null;
            }
        });
    }
    
    public void tearDown() {
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                return null;
            }
        });
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testAreAttributesFine () {
        List/*<String>*/ errors = new ArrayList();
        
        Enumeration/*<FileObject>*/ files = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = (FileObject)files.nextElement();
            
            // XXX #16761 Removing attr in MFO causes storing special-null value even in unneeded cases.
            // When the issue is fixed remove this hack.
            if("Windows2/Modes/debugger".equals(fo.getPath()) // NOI18N
            || "Windows2/Modes/explorer".equals(fo.getPath())) { // NOI18N
                continue;
            }
            
            if (
                "Keymaps/NetBeans/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/Emacs/D-BACK_QUOTE.shadow".equals(fo.getPath())
            ) {
                // #46753
                continue;
            }
            
            Enumeration/*<String>*/ attrs = fo.getAttributes();
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
    
    public void testValidShadows () {
        // might be better to move into editor/options tests as it is valid only if there are options
        List/*<String>*/ errors = new ArrayList();
        
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        Enumeration en = root.getChildren(true);
        int cnt = 0;
        while (en.hasMoreElements()) {
            FileObject fo = (FileObject)en.nextElement();
            cnt++;
            
            // XXX #16761 Removing attr in MFO causes storing special-null value even in unneeded cases.
            // When the issue is fixed remove this hack.
            if("Windows2/Modes/debugger".equals(fo.getPath()) // NOI18N
            || "Windows2/Modes/explorer".equals(fo.getPath())) { // NOI18N
                continue;
            }
            
            if (
                "Keymaps/NetBeans/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/Emacs/D-BACK_QUOTE.shadow".equals(fo.getPath())
            ) {
                // #46753
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                DataShadow ds = (DataShadow)obj.getCookie (DataShadow.class);
                if (ds != null) {
                    Object o = ds.getOriginal();
                    if (o == null) {
                        errors.add("\nFile " + fo + " has no original.");
                    }
                }
                else if ("shadow".equals(fo.getExt())) {
                    errors.add("\nFile " + fo + " is not a valid DataShadow.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errors.add ("\n    File " + fo + " thrown exception " + ex);
            }
        }
        
        if (!errors.isEmpty()) {
            fail ("Some shadow files in NetBeans profile are broken:" + errors);
        }
        
        if (cnt == 0) {
            fail("No file objects on system file system!");
        }
    }
    
    
    public void testContentCanBeRead () {
        List/*<String>*/ errors = new ArrayList();
        byte[] buffer = new byte[4096];
        
        Enumeration/*<FileObject>*/ files = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = (FileObject)files.nextElement();
            
            if (!fo.isData ()) {
                continue;
            }
            long size = fo.getSize();
            
            try {
                long read = 0;
                InputStream is = fo.getInputStream();
                try {
                    for (;;) {
                        int len = is.read (buffer);
                        if (len == -1) break;
                        read += len;
                    }
                } finally {
                    is.close ();
                }
                
                if (size != -1) {
                    assertEquals ("The amount of data in stream is the same as the length", size, read);
                }
                
            } catch (IOException ex) {
                errors.add ("\n    File " + fo + " cannot be read " + ex);
            }
        }
        
        if (!errors.isEmpty()) {
            fail ("Some files are unreadable" + errors);
        }
    }
    
    public void testInstantiateAllInstances () {
        List/*<String>*/ errors = new ArrayList();
        
        Enumeration/*<FileOject>*/ files = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren(true);
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
    
    public void testIfOneFileIsDefinedTwiceByDifferentModulesTheyNeedToHaveMutualDependency() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        // String -> List<Modules>
        Map/*<String,List<String>>*/ files = new HashMap();
        /* < FO path , { content, attributes } > */
        Map<String, Object[]> contents = new HashMap<String, Object[]>();
        /* < FO path , < module name, { content, attributes } > > */
        Map<String, Map<String, Object[]>> differentContents = new HashMap<String, Map<String, Object[]>>();
        
        boolean atLeastOne = false;
        Enumeration/*<URL>*/ en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = (URL) en.nextElement();
            InputStream is = u.openStream();
            Manifest mf;
            try {
                mf = new Manifest(is);
            } finally {
                is.close();
            }
            String module = mf.getMainAttributes ().getValue ("OpenIDE-Module");
            if (module == null) continue;
            String layer = mf.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
            if (layer == null) continue;
            
            atLeastOne = true;
            URL layerURL = new URL(u, "../" + layer);
            java.net.URLConnection connect = layerURL.openConnection ();
            connect.setDefaultUseCaches (false);
            FileSystem fs = new XMLFileSystem(layerURL);
            
            Enumeration/*<FileObject>*/ all = fs.getRoot().getChildren(true);
            while (all.hasMoreElements ()) {
                FileObject fo = (FileObject)all.nextElement ();
                if (!fo.isData ()) continue;
                
                String path = fo.getPath();
                List<String> list = (List) files.get(path);
                if (list == null) {
                    list = new ArrayList();
                    files.put (path, list);
                    list.add (module);
                    byte[] foc = getFileContent(fo);
                    Map foa = getAttributes(fo);
                    contents.put(path, new Object[] { foc, foa });
                } else {
                    Object[] contentAttrs = contents.get(path);
                    byte[] foc = getFileContent(fo);
                    Map foa = getAttributes(fo);
                    if (!equals(foc, (byte[]) contentAttrs[0]) || !foa.equals(contentAttrs[1])) {
                        Map<String, Object[]> diffs = differentContents.get(path);
                        if (diffs == null) {
                            diffs = new HashMap<String, Object[]>();
                            differentContents.put(path, diffs);
                            diffs.put(list.get(0), contentAttrs);
                        }
                        diffs.put(module, new Object[] { foc, foa });
                        list.add (module);
                    }
                }
            }
            // make sure the filesystem closes the stream
            connect.getInputStream ().close ();
        }
        contents = null; // Not needed any more
        
        Iterator<Map.Entry<String,List<String>>> it = files.entrySet().iterator();
        StringBuffer sb = new StringBuffer ();
        while (it.hasNext ()) {
            Map.Entry<String,List<String>> e = (Map.Entry) it.next();
            List<String> list = (List) e.getValue();
            if (list.size () == 1) continue;
            
            Lookup.Result/*<ModuleInfo>*/ res = Lookup.getDefault().lookupResult(ModuleInfo.class);
            assertFalse ("Some modules found", res.allInstances ().isEmpty ());
            
            Iterator/*<String>*/ names = new ArrayList(list).iterator();
            while (names.hasNext ()) {
                String name = (String)names.next ();
                Iterator/*<ModuleInfo>*/ modules = res.allInstances().iterator();
                while (modules.hasNext ()) {
                    ModuleInfo info = (ModuleInfo)modules.next ();
                    if (name.equals (info.getCodeName ())) {
                        // remove dependencies
                        Iterator/*<Dependency>*/ deps = info.getDependencies().iterator();
                        while (deps.hasNext ()) {
                            Dependency d = (Dependency)deps.next ();
                            list.remove (d.getName ());
                        }
                    }
                }
            }
            // ok, modules depend on each other
            if (list.size () <= 1) continue;
            
            sb.append (e.getKey () + " is provided by: " + list + "\n");
            Map<String, Object[]> diffList = differentContents.get(e.getKey());
            if (diffList != null) {
                if (list.size() == 2) {
                    String module1 = list.get(0);
                    String module2 = list.get(1);
                    Object[] contentAttrs1 = diffList.get(module1);
                    Object[] contentAttrs2 = diffList.get(module2);
                    if (!equals((byte[]) contentAttrs1[0], (byte[]) contentAttrs2[0])) {
                        sb.append(" "+module1+": content = '"+new String((byte[]) contentAttrs1[0])+"\n");
                        sb.append(" "+module2+": content = '"+new String((byte[]) contentAttrs2[0])+"\n");
                    }
                    if (!contentAttrs1[1].equals(contentAttrs2[1])) {
                        Map attr1 = (Map) contentAttrs1[1];
                        Map attr2 = (Map) contentAttrs2[1];
                        Set keys = new HashSet(attr1.keySet());
                        keys.retainAll(attr2.keySet());
                        for (Iterator keysIt = keys.iterator(); keysIt.hasNext(); ) {
                            Object attribute = keysIt.next();
                            Object value1 = attr1.get(attribute);
                            Object value2 = attr2.get(attribute);
                            if (value1 == value2 || (value1 != null && value1.equals(value2))) {
                                // Remove the common attributes so that just the differences show up
                                attr1.remove(attribute);
                                attr2.remove(attribute);
                            }
                        }
                        sb.append(" "+module1+": different attributes = '"+contentAttrs1[1]+"\n");
                        sb.append(" "+module2+": different attributes = '"+contentAttrs2[1]+"\n");
                    }
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        String module = list.get(i);
                        Object[] contentAttrs = diffList.get(module);
                        sb.append(" "+module+": content = '"+new String((byte[]) contentAttrs[0])+"', attributes = "+contentAttrs[1]+"\n");
                    }
                }
            }
        }        
        
        assertTrue ("At least one layer file is usually used", atLeastOne);
        
        if (sb.length () > 0) {
            fail ("Some modules override their files and do not depend on each other\n" + sb);
        }
    }
    
    private static byte[] getFileContent(FileObject fo) throws IOException {
        BufferedInputStream in = new BufferedInputStream(fo.getInputStream());
        int size = (int) fo.getSize();
        byte[] content = new byte[size];
        int length = 0;
        while(length < size) {
            int readLength = in.read(content, length, size - length);
            if (readLength <= 0) {
                throw new IOException("Bad size for "+fo+", size = "+size+", but actual length is "+length);
            }
            length +=readLength;
        }
        return content;
    }
    
    private static Map getAttributes(FileObject fo) {
        Map attrs = new HashMap();
        Enumeration<String> en = fo.getAttributes();
        while (en.hasMoreElements()) {
            String attrName = en.nextElement();
            Object attr = fo.getAttribute(attrName);
            attrs.put(attrName, attr);
        }
        return attrs;
    }
    
    private static boolean equals(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) return false;
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) return false;
        }
        return true;
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
