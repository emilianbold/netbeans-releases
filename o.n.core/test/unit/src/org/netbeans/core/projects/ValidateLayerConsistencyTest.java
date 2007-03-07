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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.core.startup.layers.BinaryCacheManager;
import org.netbeans.core.startup.layers.ParsingLayerCacheManager;
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

    public void setUp() throws Exception {
        clearWorkDir();
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                contextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class));
                return null;
            }
        });
    }
    
    public void tearDown() {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                return null;
            }
        });
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testAreAttributesFine () {
        List<String> errors = new ArrayList<String>();
        
        Enumeration<? extends FileObject> files = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
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
            
            Enumeration<String> attrs = fo.getAttributes();
            while (attrs.hasMoreElements()) {
                String name = attrs.nextElement();
                
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
        List<String> errors = new ArrayList<String>();
        
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        Enumeration<? extends FileObject> en = root.getChildren(true);
        int cnt = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
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
                DataShadow ds = obj.getCookie(DataShadow.class);
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
        
        if (ValidateLayerConsistencyTest.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
            // do not check the count as this probably means we are running
            // plain Unit test and not inside the IDE mode
            return;
        }
        
        
        if (cnt == 0) {
            fail("No file objects on system file system!");
        }
    }
    
    
    public void testContentCanBeRead () {
        List<String> errors = new ArrayList<String>();
        byte[] buffer = new byte[4096];
        
        Enumeration<? extends FileObject> files = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
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
        List<String> errors = new ArrayList<String>();
        
        Enumeration<? extends FileObject> files = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
            if (skipFile(fo.getPath())) {
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = obj.getCookie(InstanceCookie.class);
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
        Map<String,List<String>> files = new HashMap<String,List<String>>();
        class ContentAndAttrs {
            final byte[] contents;
            final Map<String,Object> attrs;
            ContentAndAttrs(byte[] contents, Map<String,Object> attrs) {
                this.contents = contents;
                this.attrs = attrs;
            }
        }
        /* < FO path , { content, attributes } > */
        Map<String,ContentAndAttrs> contents = new HashMap<String,ContentAndAttrs>();
        /* < FO path , < module name, { content, attributes } > > */
        Map<String,Map<String,ContentAndAttrs>> differentContents = new HashMap<String,Map<String,ContentAndAttrs>>();
        
        boolean atLeastOne = false;
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = en.nextElement();
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
            
            Enumeration<? extends FileObject> all = fs.getRoot().getChildren(true);
            while (all.hasMoreElements ()) {
                FileObject fo = all.nextElement ();
                if (!fo.isData ()) continue;
                
                String path = fo.getPath();
                List<String> list = files.get(path);
                if (list == null) {
                    list = new ArrayList<String>();
                    files.put (path, list);
                    list.add (module);
                    contents.put(path, new ContentAndAttrs(getFileContent(fo), getAttributes(fo)));
                } else {
                    ContentAndAttrs contentAttrs = contents.get(path);
                    byte[] foc = getFileContent(fo);
                    Map<String,Object> foa = getAttributes(fo);
                    if (!Arrays.equals(foc, contentAttrs.contents) || !foa.equals(contentAttrs.attrs)) {
                        Map<String,ContentAndAttrs> diffs = differentContents.get(path);
                        if (diffs == null) {
                            diffs = new HashMap<String,ContentAndAttrs>();
                            differentContents.put(path, diffs);
                            diffs.put(list.get(0), contentAttrs);
                        }
                        diffs.put(module, new ContentAndAttrs(foc, foa));
                        list.add (module);
                    }
                }
            }
            // make sure the filesystem closes the stream
            connect.getInputStream ().close ();
        }
        contents = null; // Not needed any more
        
        StringBuffer sb = new StringBuffer ();
        for (Map.Entry<String,List<String>> e : files.entrySet()) {
            List<String> list = e.getValue();
            if (list.size () == 1) continue;
            
            Collection<? extends ModuleInfo> res = Lookup.getDefault().lookupAll(ModuleInfo.class);
            assertFalse("Some modules found", res.isEmpty());
            
            for (String name : new ArrayList<String>(list)) {
                for (ModuleInfo info : res) {
                    if (name.equals (info.getCodeName ())) {
                        // remove dependencies
                        for (Dependency d : info.getDependencies()) {
                            list.remove (d.getName ());
                        }
                    }
                }
            }
            // ok, modules depend on each other
            if (list.size () <= 1) continue;
            
            sb.append (e.getKey ()).append( " is provided by: " ).append(list).append('\n');
            Map<String,ContentAndAttrs> diffList = differentContents.get(e.getKey());
            if (diffList != null) {
                if (list.size() == 2) {
                    String module1 = list.get(0);
                    String module2 = list.get(1);
                    ContentAndAttrs contentAttrs1 = diffList.get(module1);
                    ContentAndAttrs contentAttrs2 = diffList.get(module2);
                    if (!Arrays.equals(contentAttrs1.contents, contentAttrs2.contents)) {
                        sb.append(' ').append(module1).append(": content = '").append(new String(contentAttrs1.contents)).append('\n');
                        sb.append(' ').append(module2).append(": content = '").append(new String(contentAttrs2.contents)).append('\n');
                    }
                    if (!contentAttrs1.attrs.equals(contentAttrs2.attrs)) {
                        Map<String,Object> attr1 = contentAttrs1.attrs;
                        Map<String,Object> attr2 = contentAttrs2.attrs;
                        Set<String> keys = new HashSet<String>(attr1.keySet());
                        keys.retainAll(attr2.keySet());
                        for (String attribute : keys) {
                            Object value1 = attr1.get(attribute);
                            Object value2 = attr2.get(attribute);
                            if (value1 == value2 || (value1 != null && value1.equals(value2))) {
                                // Remove the common attributes so that just the differences show up
                                attr1.remove(attribute);
                                attr2.remove(attribute);
                            }
                        }
                        sb.append(' ').append(module1).append(": different attributes = '").append(contentAttrs1.attrs).append('\n');
                        sb.append(' ').append(module2).append(": different attributes = '").append(contentAttrs2.attrs).append('\n');
                    }
                } else {
                    for (String module : list) {
                        ContentAndAttrs contentAttrs = diffList.get(module);
                        sb.append(" " + module + ": content = '" + new String(contentAttrs.contents) + "', attributes = " + contentAttrs.attrs + "\n");
                    }
                }
            }
        }        
        
        assertTrue ("At least one layer file is usually used", atLeastOne);
        
        if (sb.length () > 0) {
            fail ("Some modules override their files and do not depend on each other\n" + sb);
        }
    }
    
    public void testNoWarningsFromLayerParsing() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        List<URL> urls = new ArrayList<URL>();
        boolean atLeastOne = false;
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = en.nextElement();
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
            urls.add(layerURL);
        }
        
        File cacheDir;
        File workDir = getWorkDir();
        int i = 0;
        do {
            cacheDir = new File(workDir, "layercache"+i);
            i++;
        } while (!cacheDir.mkdir());

        BinaryCacheManager bcm = new BinaryCacheManager(cacheDir);
        Logger err = Logger.getLogger("org.netbeans.core.projects.cache");
        LayerParsehandler h = new LayerParsehandler();
        err.addHandler(h);
        bcm.store(urls);
        assertEquals("No errors or warnings during layer parsing: "+h.errors().toString(), 0, h.errors().size());
    }
    
    private static class LayerParsehandler extends Handler {
        List<String> errors = new ArrayList<String>();
        
        LayerParsehandler () {}
        
        public void publish(LogRecord rec) {
            if (Level.WARNING.equals(rec.getLevel()) || Level.SEVERE.equals(rec.getLevel())) {
                errors.add(MessageFormat.format(rec.getMessage(), rec.getParameters()));
            }
        }
        
        List<String> errors() {
            return errors;
        }

        public void flush() {
        }

        public void close() throws SecurityException {
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
    
    private static Map<String,Object> getAttributes(FileObject fo) {
        Map<String,Object> attrs = new HashMap<String,Object>();
        Enumeration<String> en = fo.getAttributes();
        while (en.hasMoreElements()) {
            String attrName = en.nextElement();
            Object attr = fo.getAttribute(attrName);
            attrs.put(attrName, attr);
        }
        return attrs;
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
