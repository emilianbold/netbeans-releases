/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.validation;

import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.startup.layers.LayerCacheManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.Log;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/** Checks consistency of System File System contents.
 */
public class ValidateLayerConsistencyTest extends NbTestCase {

    private static final String SFS_LB = "SystemFileSystem.localizingBundle";

    private ClassLoader contextClassLoader;   
    
    public ValidateLayerConsistencyTest(String name) {
        super (name);
    }

    @Override
    protected int timeOut() {
        // sometimes can deadlock and then we need to see the thread dump
        return 1000 * 60 * 10;
    }
    
    public @Override void setUp() throws Exception {
        clearWorkDir();
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                contextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(Lookup.getDefault().lookup(ClassLoader.class));
                return null;
            }
        });
    }
    
    public @Override void tearDown() {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                return null;
            }
        });
    }
    
    protected @Override boolean runInEQ() {
        return true;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ValidateLayerConsistencyTest.class).
                clusters("(?!ergonomics).*").enableClasspathModules(false).enableModules(".*").gui(false)));
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ValidateLayerConsistencyTest.class).
                clusters("(platform|harness|ide|websvccommon|gsf|java|profiler|nb)[0-9.]*").enableClasspathModules(false).enableModules(".*").gui(false)));
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ValidateLayerConsistencyTest.class).
                clusters("(platform|ide)[0-9.]*").enableClasspathModules(false).enableModules(".*").gui(false)));
        return suite;
    }

    private void assertNoErrors(String message, Collection<String> warnings) {
        if (warnings.isEmpty()) {
            return;
        }
        StringBuilder b = new StringBuilder(message);
        for (String warning : new TreeSet<String>(warnings)) {
            b.append('\n').append(warning);
        }
        fail(b.toString());
    }

    /* Causes mysterious failure in otherwise OK-looking UI/Runtime/org-netbeans-modules-db-explorer-nodes-RootNode.instance: 
    @Override
    protected Level logLevel() {
        return Level.FINER;
    }
    */
    
    public void testAreAttributesFine () {
        List<String> errors = new ArrayList<String>();
        
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
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
                "Keymaps/NetBeans55/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/Emacs/D-BACK_QUOTE.shadow".equals(fo.getPath())
            ) {
                // #46753
                continue;
            }
            if (
                "Services/Browsers/FirefoxBrowser.settings".equals(fo.getPath()) ||
                "Services/Browsers/MozillaBrowser.settings".equals(fo.getPath()) ||
                "Services/Browsers/NetscapeBrowser.settings".equals(fo.getPath())
            ) {
                // #161784
                continue;
            }
            
            Enumeration<String> attrs = fo.getAttributes();
            while (attrs.hasMoreElements()) {
                String name = attrs.nextElement();

                if (name.equals("instanceCreate")) {
                    continue; // will be handled in testInstantiateAllInstances anyway
                }
                
                Object attr = fo.getAttribute(name);
                if (attr == null) {
                    CharSequence warning = Log.enable("", Level.WARNING);
                    if (
                        fo.getAttribute("class:" + name) != null &&
                        fo.getAttribute(name) == null &&
                        warning.length() == 0
                    ) {
                        // ok, factory method returned null
                        continue;
                    }

                    errors.add("File: " + fo.getPath() + " attribute name: " + name);
                }

                if (attr instanceof URL) {
                    URL u = (URL) attr;
                    int read = -1;
                    try {
                        read = u.openStream().read(new byte[4096]);
                    } catch (IOException ex) {
                        errors.add(fo.getPath() + ": " + ex.getMessage());
                    }
                    if (read <= 0) {
                        errors.add("URL resource does not exist: " + fo.getPath() + " attr: " + name + " value: " + attr);
                    }
                }

            }
        }
        
        assertNoErrors("Some attributes in files are unreadable", errors);
    }
    
    public void testValidShadows () {
        // might be better to move into editor/options tests as it is valid only if there are options
        List<String> errors = new ArrayList<String>();
        
        FileObject root = FileUtil.getConfigRoot();
        
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
                "Keymaps/NetBeans55/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/Emacs/D-BACK_QUOTE.shadow".equals(fo.getPath())
            ) {
                // #46753
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                DataShadow ds = obj.getLookup().lookup(DataShadow.class);
                if (ds != null) {
                    Object o = ds.getOriginal();
                    if (o == null) {
                        errors.add("File " + fo + " has no original.");
                    }
                }
                else if ("shadow".equals(fo.getExt())) {
                    errors.add("File " + fo + " is not a valid DataShadow.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errors.add ("File " + fo + " threw " + ex);
            }
        }
        
        assertNoErrors("Some shadow files in NetBeans profile are broken", errors);
        
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
        
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
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
                ex.printStackTrace();
                errors.add ("File " + fo + " cannot be read: " + ex);
            }
        }
        
        assertNoErrors("Some files are unreadable", errors);
    }
    
    public void testInstantiateAllInstances () {
        List<String> errors = new ArrayList<String>();
        
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        FILE: while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
            if (skipFile(fo.getPath())) {
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
                if (ic != null) {
                    String iof = (String) fo.getAttribute("instanceOf");
                    if (iof != null) {
                        for (String clz : iof.split("[, ]+")) {
                            try {
                                Class<?> c = Lookup.getDefault().lookup(ClassLoader.class).loadClass(clz);
                            } catch (ClassNotFoundException x) {
                                // E.g. Services/Hidden/org-netbeans-lib-jsch-antlibrary.instance in ide cluster
                                // cannot be loaded (and would just be ignored) if running without java cluster
                                System.err.println("Warning: skipping " + fo.getPath() + " due to inaccessible interface " + clz);
                                continue FILE;
                            }
                        }
                    }
                    Object o = ic.instanceCreate ();
                }
            } catch (Exception ex) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                ex.printStackTrace(ps);
                ps.flush();
                errors.add(
                    "File " + fo.getPath() +
                    "\nRead from: " + Arrays.toString((Object[])fo.getAttribute("layers")) +
                    "\nthrew: " + baos);
            }
        }
        
        assertNoErrors("Some instances cannot be created", errors);
    }

    public void testActionInstancesOnlyInActionsFolder() {
        List<String> errors = new ArrayList<String>();

        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        FILE: while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();

            if (skipFile(fo.getPath())) {
                continue;
            }

            try {
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
                if (ic == null) {
                    continue;
                }
                Object o;
                try {
                    o = ic.instanceCreate();
                } catch (ClassNotFoundException ok) {
                    // wrong instances are catched by another test
                    continue;
                }
                if (!(o instanceof Action)) {
                    continue;
                }
                if (fo.hasExt("xml")) {
                    continue;
                }
                if (fo.getPath().startsWith("Actions/")) {
                    continue;
                }
                if (fo.getPath().startsWith("Editors/")) {
                    // editor is a bit different world
                    continue;
                }
                if (fo.getPath().startsWith("Databases/Explorer/")) {
                    // db explorer actions shall not influence start
                    // => let them be for now.
                    continue;
                }
                if (fo.getPath().startsWith("WelcomePage/")) {
                    // welcome screen actions are not intended for end user
                    continue;
                }
                if (fo.getPath().startsWith("Projects/org-netbeans-modules-mobility-project/Actions/")) {
                    // I am not sure what mobility is doing, but
                    // I guess I do not need to care
                    continue;
                }
                if (fo.getPath().startsWith("NativeProjects/Actions/")) {
                    // I should probably report a bug for NativeProjects
                    continue;
                }
                if (fo.getPath().startsWith("contextmenu/uml/")) {
                    // UML is not the most important thing to fix
                    continue;
                }
                if (fo.getPath().equals("Menu/Help/org-netbeans-modules-j2ee-blueprints-ShowBluePrintsAction.instance")) {
                    // action included in some binary blob
                    continue;
                }
                if (
                    fo.getPath().startsWith("Loaders/text/x-java/Actions/") ||
                    fo.getPath().startsWith("Loaders/text/x-jsp/Actions/")
                ) {
                    // imho both java and jsp are doing something wrong, they
                    // should refer to some standard action, not invent their
                    // own
                    continue;
                }

                if (Boolean.TRUE.equals(fo.getAttribute("misplaced.action.allowed"))) {
                    // it seems necessary some actions to stay outside
                    // of the Actions folder
                    continue;
                }
                if (fo.hasExt("shadow")) {
                    o = fo.getAttribute("originalFile");
                    if (o instanceof String) {
                        String origF = o.toString().replaceFirst("\\/*", "");
                        if (origF.startsWith("Actions/")) {
                            continue;
                        }
                    }
                }
                errors.add("File " + fo.getPath() + " represents an action which is not in Actions/ subfolder. Provided by " + Arrays.toString((Object[])fo.getAttribute("layers")));
            } catch (Exception ex) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                ex.printStackTrace(ps);
                ps.flush();
                errors.add ("File " + fo.getPath() + " threw: " + baos);
            }
        }

        assertNoErrors(errors.size() + " actions is not registered properly", errors);
    }
    
    public void testIfOneFileIsDefinedTwiceByDifferentModulesTheyNeedToHaveMutualDependency() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        // String -> List<Modules>
        Map<String,List<String>> files = new HashMap<String,List<String>>();
        class ContentAndAttrs {
            final byte[] contents;
            final Map<String,Object> attrs;
            private final URL layerURL;
            ContentAndAttrs(byte[] contents, Map<String,Object> attrs, URL layerURL) {
                this.contents = contents;
                this.attrs = attrs;
                this.layerURL = layerURL;
            }
            public @Override String toString() {
                return "ContentAndAttrs[contents=" + Arrays.toString(contents) + ",attrs=" + attrs + ";from=" + layerURL + "]";
            }
            public @Override int hashCode() {
                return Arrays.hashCode(contents) ^ attrs.hashCode();
            }
            public @Override boolean equals(Object o) {
                if (!(o instanceof ContentAndAttrs)) {
                    return false;
                }
                ContentAndAttrs caa = (ContentAndAttrs) o;
                return Arrays.equals(contents, caa.contents) && attrs.equals(caa.attrs);
            }
        }
        /* < FO path , { content, attributes } > */
        Map<String,ContentAndAttrs> contents = new HashMap<String,ContentAndAttrs>();
        /* < FO path , < module name, { content, attributes } > > */
        Map<String,Map<String,ContentAndAttrs>> differentContents = new HashMap<String,Map<String,ContentAndAttrs>>();
        Map</* path */String,Map</* attr name */String,Map</* module name */String,/* attr value */Object>>> folderAttributes =
                new TreeMap<String,Map<String,Map<String,Object>>>();
        StringBuffer sb = new StringBuffer();
        Map<String,URL> hiddenFiles = new HashMap<String, URL>();
        Set<String> allFiles = new HashSet<String>();
        final String suffix = "_hidden";

        
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
            URL base = new URL(u, "../");
            URL layerURL = new URL(base, layer);
            java.net.URLConnection connect = layerURL.openConnection ();
            connect.setDefaultUseCaches (false);
            FileSystem fs = new XMLFileSystem(layerURL);

            Enumeration<? extends FileObject> all = fs.getRoot().getChildren(true);
            while (all.hasMoreElements ()) {
                FileObject fo = all.nextElement ();
                String path = fo.getPath();

                if (path.endsWith(suffix)) {
                    hiddenFiles.put(path, layerURL);
                } else {
                    allFiles.add(path);
                }

                Map<String,Object> attributes = getAttributes(fo, base);

                /* XXX too many failures to enable yet:
                // Check for misplaced localizing bundle attributes.
                // Might be more natural to check in testAreAttributesFine
                // but easier here because of the way we load one layer at a time.
                for (Map.Entry<String,Object> entry : attributes.entrySet()) {
                    if (entry.getKey().equals(SFS_LB) && entry.getValue() instanceof Exception) {
                        sb.append("Module " + module + " defines an incorrect " + SFS_LB + " on " + path +
                                ": " + fo.getAttribute(SFS_LB) + "\n");
                    }
                }
                 */

                if (fo.isFolder()) {
                    for (Map.Entry<String,Object> attr : attributes.entrySet()) {
                        Map<String,Map<String,Object>> m1 = folderAttributes.get(path);
                        if (m1 == null) {
                            m1 = new TreeMap<String,Map<String,Object>>();
                            folderAttributes.put(path, m1);
                        }
                        Map<String,Object> m2 = m1.get(attr.getKey());
                        if (m2 == null) {
                            m2 = new TreeMap<String,Object>();
                            m1.put(attr.getKey(), m2);
                        }
                        m2.put(module, attr.getValue());
                    }
                    continue;
                }
                
                List<String> list = files.get(path);
                if (list == null) {
                    list = new ArrayList<String>();
                    files.put (path, list);
                    list.add (module);
                    contents.put(path, new ContentAndAttrs(getFileContent(fo), attributes, layerURL));
                } else {
                    ContentAndAttrs contentAttrs = contents.get(path);
                    ContentAndAttrs nue = new ContentAndAttrs(getFileContent(fo), attributes, layerURL);
                    if (!nue.equals(contentAttrs)) {
                        //System.err.println("Found differences in " + path + " between " + nue + " and " + contentAttrs);
                        Map<String,ContentAndAttrs> diffs = differentContents.get(path);
                        if (diffs == null) {
                            diffs = new HashMap<String,ContentAndAttrs>();
                            differentContents.put(path, diffs);
                            diffs.put(list.get(0), contentAttrs);
                        }
                        diffs.put(module, nue);
                        list.add (module);
                    }
                }
            }
            // make sure the filesystem closes the stream
            connect.getInputStream ().close ();
        }
        contents = null; // Not needed any more
        
        for (Map.Entry<String,List<String>> e : files.entrySet()) {
            List<String> list = e.getValue();
            if (list.size () == 1) continue;
            
            Collection<? extends ModuleInfo> res = Lookup.getDefault().lookupAll(ModuleInfo.class);
            assertFalse("Some modules found", res.isEmpty());
            
            List<String> list2 = new ArrayList<String>(list);
            for (String name : list) {
                for (ModuleInfo info : res) {
                    if (name.equals (info.getCodeName ())) {
                        // remove dependencies
                        for (Dependency d : info.getDependencies()) {
                            list2.remove(d.getName());
                        }
                    }
                }
            }
            // ok, modules depend on each other
            if (list2.size() <= 1) continue;
            
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
        
        for (Map.Entry<String,Map<String,Map<String,Object>>> entry1 : folderAttributes.entrySet()) {
            for (Map.Entry<String,Map<String,Object>> entry2 : entry1.getValue().entrySet()) {
                if (new HashSet<Object>(entry2.getValue().values()).size() > 1) {
                    // XXX currently do not check if the modules are unrelated by dependency.
                    sb.append("Some modules conflict on the definition of " + entry2.getKey() + " in " + entry1.getKey() + ": " + entry2.getValue() + "\n");
                }
            }
        }

        if (sb.length () > 0) {
            fail ("Some modules override their files and do not depend on each other\n" + sb);
        }


        for (Map.Entry<String, URL> e : hiddenFiles.entrySet()) {
            String p = e.getKey().substring(0, e.getKey().length() - suffix.length());
            if (allFiles.contains(p)) {
                continue;
            }
            sb.append("file " + e.getKey() + " from " + e.getValue() + " does not hide any other file\n");
        }

        if (sb.length () > 0) {
            fail ("There are some useless hidden files\n" + sb);
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
        System.setProperty("netbeans.user", cacheDir.getPath());

        LayerCacheManager bcm = LayerCacheManager.manager(true);
        Logger err = Logger.getLogger("org.netbeans.core.projects.cache");
        LayerParseHandler h = new LayerParseHandler();
        err.addHandler(h);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bcm.store(bcm.createEmptyFileSystem(), urls, os);
        assertNoErrors("No errors or warnings during layer parsing", h.errors);
    }
    
    private static class LayerParseHandler extends Handler {
        List<String> errors = new ArrayList<String>();
        
        LayerParseHandler () {}
        
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

    public void testFolderOrdering() throws Exception {
        LayerParseHandler h = new LayerParseHandler();
        Logger.getLogger("org.openide.filesystems.Ordering").addHandler(h);
        Set<List<String>> editorMultiFolders = new HashSet<List<String>>();
        Pattern editorFolder = Pattern.compile("Editors/(application|text)/([^/]+)(/.+|$)");
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            if (fo.isFolder()) {
                loadChildren(fo);
                assertNull("OpenIDE-Folder-Order attr should not be used on " + fo, fo.getAttribute("OpenIDE-Folder-Order"));
                assertNull("OpenIDE-Folder-SortMode attr should not be used on " + fo, fo.getAttribute("OpenIDE-Folder-SortMode"));
                String path = fo.getPath();
                Matcher m = editorFolder.matcher(path);
                if (m.matches()) {
                    List<String> multiPath = new ArrayList<String>(3);
                    multiPath.add(path);
                    if (m.group(2).endsWith("+xml")) {
                        multiPath.add("Editors/" + m.group(1) + "/xml" + m.group(3));
                    }
                    multiPath.add("Editors" + m.group(3));
                    editorMultiFolders.add(multiPath);
                }
            }
        }
        assertNoErrors("No warnings relating to folder ordering; " +
                "cf: http://deadlock.netbeans.org/hudson/job/nbms-and-javadoc/lastSuccessfulBuild/artifact/nbbuild/build/generated/layers.txt", h.errors());
        for (List<String> multiPath : editorMultiFolders) {
            List<FileSystem> layers = new ArrayList<FileSystem>(3);
            for (final String path : multiPath) {
                FileObject folder = FileUtil.getConfigFile(path);
                if (folder != null) {
                    layers.add(new MultiFileSystem(new FileSystem[] {folder.getFileSystem()}) {
                        protected @Override FileObject findResourceOn(FileSystem fs, String res) {
                            FileObject f = fs.findResource(path + '/' + res);
                            return Boolean.TRUE.equals(f.getAttribute("hidden")) ? null : f;
                        }
                    });
                }
            }
            loadChildren(new MultiFileSystem(layers.toArray(new FileSystem[layers.size()])).getRoot());
            assertNoErrors("No warnings relating to folder ordering in " + multiPath + 
                    "; cf: http://deadlock.netbeans.org/hudson/job/nbms-and-javadoc/lastSuccessfulBuild/artifact/nbbuild/build/generated/layers.txt",
                    h.errors());
        }
    }
    private static void loadChildren(FileObject folder) {
        List<FileObject> kids = new ArrayList<FileObject>();
        for (DataObject kid : DataFolder.findFolder(folder).getChildren()) {
            kids.add(kid.getPrimaryFile());
        }
        FileUtil.getOrder(kids, true);
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
    
    private static Map<String,Object> getAttributes(FileObject fo, URL base) {
        Map<String,Object> attrs = new TreeMap<String,Object>();
        Enumeration<String> en = fo.getAttributes();
        while (en.hasMoreElements()) {
            String attrName = en.nextElement();
            if (attrName.equals("instanceCreate")) {
                continue;
            }
            Object attr = fo.getAttribute(attrName);
            if (attrName.equals(SFS_LB)) {
                try {
                    String bundleName = (String) attr;
                    URL bundle = new URL(base, bundleName.replace('.', '/') + ".properties");
                    Properties p = new Properties();
                    InputStream is = bundle.openStream();
                    try {
                        p.load(is);
                    } finally {
                        is.close();
                    }
                    String path = fo.getPath();
                    attr = p.get(path);
                    if (attr == null) {
                        attr = new MissingResourceException("No such bundle entry " + path + " in " + bundleName, bundleName, path);
                    }
                } catch (Exception x) {
                    attr = x;
                }
            }
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
