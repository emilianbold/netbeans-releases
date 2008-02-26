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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.performance.scalability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Pavel Flaška
 */
public class TabSwitchSpeedTest extends NbTestCase {

    private static final long SWITCH_LIMIT = 100; // ms
    private static TopComponent[] openTC;
    
    
    public TabSwitchSpeedTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(TabSwitchSpeedTest.class, ".*");
    }

    @Override
    public void setUp() throws Exception {
        if (openTC == null) {
            FileObject root = FileUtil.toFileObject(getWorkDir());
            assertNotNull("Cannot find dir for " + getWorkDir() + " exists: " + getWorkDir().exists(), root);

            FileObject[] openFiles = new FileObject[30];
            for (int i = 0; i < openFiles.length; i++) {
                openFiles[i] = FileUtil.createData(root, "empty" + i + ".txt");
            }

            openTC = new TopComponent[openFiles.length];
            for (int i = 0; i < openFiles.length; i++) {
                DataObject dobj = DataObject.find(openFiles[i]);
                EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
                cookie.open();
            }
        }
    }

    public void testSimpleSwitch() throws Exception {
        doSwitchTest();
    }
    
    public void testAllPlatform() throws Exception {
        enableModulesFromCluster(".*");
        doSwitchTest();
    }
    
    private void doSwitchTest() throws Exception {
        Thread.sleep(5000);
        
        final TopComponent[][] ref = new TopComponent[1][0];
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                TopComponent tc = TopComponent.getRegistry().getActivated();
                Mode mode = WindowManager.getDefault().findMode(tc);
                ref[0] = mode.getTopComponents();
            }
            
        });
        final TopComponent[] openedComponents = ref[0];
        
        for (int i = openedComponents.length - 1; i > 0; i--) {
            final int index = i;
            final long[] time = new long[2];
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    time[0] = System.currentTimeMillis();
                    openedComponents[index].requestActive();
                }
            });
            Thread.sleep(SWITCH_LIMIT);
        }
        long a = System.currentTimeMillis();
        
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
        long time = System.currentTimeMillis() - a;
        System.err.println("Result time: " + time);
        if (time > 300) {
            fail("Failed, too long: " + time);
        }
    }
    
   private static void enableModulesFromCluster(String cluster) throws Exception {
        Pattern p = Pattern.compile(cluster);
        String dirs = System.getProperty("netbeans.dirs");
        int cnt = 0;
        for (String c : dirs.split(File.pathSeparator)) {
            if (!p.matcher(c).find()) {
                continue;
            }
            
            File cf = new File(c);
            File ud = new File(System.getProperty("netbeans.user"));
            turnModules(ud, cf);
            cnt++;
        }
        if (cnt == 0) {
            fail("Cannot find cluster " + cluster + " in " + dirs);
        }
        
        Repository.getDefault().getDefaultFileSystem().refresh(false);
        LOOP: for (int i = 0; i < 20; i++) {
            Thread.sleep(1000);
            for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                if (!info.isEnabled()) {
                    System.err.println("not enabled yet " + info);
                    continue LOOP;
                }
            }
        }
    }
    private static void turnModules(File ud, File... clusterDirs) throws IOException {
        File config = new File(new File(ud, "config"), "Modules");
        config.mkdirs();

        for (File c : clusterDirs) {
            File modulesDir = new File(new File(c, "config"), "Modules");
            for (File m : modulesDir.listFiles()) {
                String n = m.getName();
                if (n.endsWith(".xml")) {
                    n = n.substring(0, n.length() - 4);
                }
                n = n.replace('-', '.');

                String xml = asString(new FileInputStream(m), true);
                Matcher matcherEnabled = ENABLED.matcher(xml);
             //   Matcher matcherEager = EAGER.matcher(xml);

                boolean found = matcherEnabled.find();

                if (found) {
                    assert matcherEnabled.groupCount() == 1 : "Groups: " + matcherEnabled.groupCount() + " for:\n" + xml;

                    try {
                        String out = 
                            xml.substring(0, matcherEnabled.start(1)) +
                            "true" +
                            xml.substring(matcherEnabled.end(1));
                        writeModule(new File(config, m.getName()), out);
                    } catch (IllegalStateException ex) {
                        throw (IOException)new IOException("Unparsable:\n" + xml).initCause(ex);
                    }
                }
            }
        }
    }
    private static Pattern ENABLED = Pattern.compile("<param name=[\"']enabled[\"']>([^<]*)</param>", Pattern.MULTILINE);

    private static void writeModule(File file, String xml) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        os.write(xml.getBytes("UTF-8"));
        os.close();
    }
    private static String asString(InputStream is, boolean close) throws IOException {
        byte[] arr = new byte[is.available()];
        int len = is.read(arr);
        if (len != arr.length) {
            throw new IOException("Not fully read: " + arr.length + " was " + len);
        }
        if (close) {
            is.close();
        }
        return new String(arr, "UTF-8"); // NOI18N
    }
}


