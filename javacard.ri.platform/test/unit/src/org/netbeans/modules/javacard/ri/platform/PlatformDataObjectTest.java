/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.ri.platform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.javacard.spi.Cards;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.ri.platform.loader.JavacardPlatformDataObject;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatformLocator;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import static org.junit.Assert.*;

public class PlatformDataObjectTest  {

    static File riPlatformDir;
    static File wrapperPlatformDir;
    static FileObject riPlatformFo;
    static FileObject wrapperPlatformFo;
    static DataObject riPlatformDo;
    static DataObject wrapperPlatformDo;
    static File userdir;
    static File tmp;
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.setProperty ("PlatformDataObjectTest", "true");
        tmp = new File (System.getProperty("java.io.tmpdir"));
        userdir = new File (tmp, "ud" + System.currentTimeMillis());
        System.setProperty ("netbeans.user", userdir.getAbsolutePath());
        riPlatformDir = setUpFakePlatform("RIPlatform");
        wrapperPlatformDir = setUpFakePlatform("WrapperPlatform");
        riPlatformFo = install (riPlatformDir, "RIPlatform");
        assertNotNull (riPlatformFo);
        assertEquals (JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION, riPlatformFo.getExt());
        wrapperPlatformFo = install (wrapperPlatformDir, "WrapperPlatform");
        assertNotNull (wrapperPlatformFo);
        assertEquals (JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION, wrapperPlatformFo.getExt());
        FileObject cardsInstance = FileUtil.createData(FileUtil.getConfigRoot(), getFakeCardsInstanceFileName("WrapperTest"));
        riPlatformDo = DataObject.find (riPlatformFo);
        assertNotNull (riPlatformDo);
        wrapperPlatformDo = DataObject.find (wrapperPlatformFo);
        assertNotNull (wrapperPlatformDo);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        delFo(riPlatformDir);
        delFo(wrapperPlatformDir);
//        for (FileObject fo : FileUtil.getConfigRoot().getChildren()) {
//            fo.delete();
//        }
        //Wipe our temporary userdir
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(tmp);
        FileObject fo = lfs.getRoot().getFileObject(userdir.getName());
        fo.delete();
    }

    @Test
    public void testLoaderIsUsed() {
        assertEquals (JavacardPlatformDataObject.class, riPlatformDo.getClass());
        assertEquals (JavacardPlatformDataObject.class, wrapperPlatformDo.getClass());
        JavacardPlatform riPlatform = riPlatformDo.getLookup().lookup(JavacardPlatform.class);
        JavacardPlatform wPlatform = wrapperPlatformDo.getLookup().lookup(JavacardPlatform.class);
        assertEquals (RIPlatform.class, riPlatform.getClass());
        assertEquals (RIPlatform.class, wPlatform.getClass());
    }

    @Test
    public void testWrappingOccurs() {
        JavacardPlatform riPlatform = riPlatformDo.getLookup().lookup(JavacardPlatform.class);
        assertNotNull ("Platform not found in DOB lookup", riPlatform);
        JavacardPlatform wPlatform = wrapperPlatformDo.getLookup().lookup(JavacardPlatform.class);
        assertNotNull ("Platform not found in DOB lookup", wPlatform);
        PropertiesAdapter adap = wrapperPlatformDo.getLookup().lookup(PropertiesAdapter.class);
        assertNotNull ("No PropertiesAdapter in data object's lookup", adap);
        ObservableProperties p = adap.asProperties();
        assertNotNull ("Properties adapter returns null", p);

        FileObject riPlatformDF = FileUtil.toFileObject(FileUtil.normalizeFile(riPlatformDir));
        FileObject wPlatformDF = FileUtil.toFileObject(FileUtil.normalizeFile(wrapperPlatformDir));

        FileObject classicApiJar = riPlatformDF.getFileObject("lib/api_classic.jar");
        FileObject connectedApiJar = riPlatformDF.getFileObject("lib/api_connected.jar");
        FileObject additionalConnectedJar = wPlatformDF.getFileObject("lib/additional.jar");
        FileObject additionalClassicJar = wPlatformDF.getFileObject("lib/additional2.jar");
        assertNotNull (classicApiJar);
        assertNotNull (connectedApiJar);
        assertNotNull (additionalClassicJar);
        assertNotNull (additionalConnectedJar);
        File classicApiJarF = FileUtil.toFile(classicApiJar);
        File connectedApiJarF = FileUtil.toFile(connectedApiJar);
        File additionalConnectedJarF = FileUtil.toFile(additionalConnectedJar);
        File additionalClassicJarF = FileUtil.toFile(additionalClassicJar);

        String expectedClassicBootClasspath = additionalClassicJarF.getAbsolutePath() + File.pathSeparator +
                classicApiJarF.getAbsolutePath();
        String expectedConnectedBootClasspath = connectedApiJarF.getAbsolutePath() + File.pathSeparator + additionalConnectedJarF.getAbsolutePath();
        
        fineAssertEquals ("Class path mismatch - expected:" + expectedClassicBootClasspath + " got:" +
                wPlatform.toProperties().getProperty(JavacardPlatformKeyNames.PLATFORM_CLASSIC_BOOT_CLASSPATH),expectedClassicBootClasspath,
                wPlatform.toProperties().getProperty(JavacardPlatformKeyNames.PLATFORM_CLASSIC_BOOT_CLASSPATH));
        fineAssertEquals ("Class path mismatch - expected:" + expectedConnectedBootClasspath + " got:" +
                wPlatform.toProperties().getProperty(JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH)
                ,expectedConnectedBootClasspath,
                wPlatform.toProperties().getProperty(JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH));

        ClassPath classpath = wPlatform.getBootstrapLibraries(ProjectKind.CLASSIC_APPLET);
        Set<String> allNames = new HashSet<String>(Arrays.asList(new String[] {"api_classic.jar", "additional2.jar"}));
        testClasspathMatches (classpath, allNames);

        allNames = new HashSet<String>(Arrays.asList(new String[] {"api_connected.jar", "additional.jar" }));
        allNames.remove("additional2.jar");
        classpath = wPlatform.getBootstrapLibraries(ProjectKind.WEB);
        testClasspathMatches (classpath, allNames);

        classpath = riPlatform.getBootstrapLibraries(ProjectKind.WEB);
        testClasspathMatches (classpath, new HashSet<String>(Arrays.asList("api_connected.jar")));

        classpath = riPlatform.getBootstrapLibraries(ProjectKind.CLASSIC_APPLET);
        testClasspathMatches (classpath, new HashSet<String>(Arrays.asList("api_classic.jar")));

//        FileObject emulator = riPlatform.findTool(JavacardPlatform.TOOL_EMULATOR);
//        assertNotNull (emulator);
    }

    private void fineAssertEquals (String msg, String a, String b) {
        char[] ac = a.toCharArray();
        char[] bc = b.toCharArray();
        for (int i = 0; i < Math.min(ac.length, bc.length); i++) {
            assertEquals (msg + "\nMismatch at char " + i + ": \n" + msgString(a, i) + "\n" + msgString(b, i), 
                    new String(new char[] { ac[i] }), new String(new char[] { bc[i]}));
        }
        assertEquals("Different lengths: " + msg, ac.length, bc.length);
    }

    private String msgString (String s, int i) {
        char[] c = new char[i];
        Arrays.fill(c, ' ');
        StringBuilder sb = new StringBuilder(s);
        sb.append ("\n");
        sb.append (c);
        sb.append ("^\n");
        return sb.toString();
    }

    @Test
    public void testCardsInstanceIsInjected() {
        JavacardPlatform wPlatform = wrapperPlatformDo.getLookup().lookup(JavacardPlatform.class);
        assertNotNull ("Platform not found in DOB lookup", wPlatform);
        Cards c =wPlatform.getCards();
        assertNotNull (c);
        assertEquals (FakeCards.class, c.getClass());
    }

    private static void testClasspathMatches (ClassPath classpath, Set<String> allNames) {
        String asString = classpath.toString(ClassPath.PathConversionMode.FAIL);
        String[] paths = asString.split (File.pathSeparator);
        File[] files = new File[paths.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(paths[i]);
//            assertTrue (files[i].getAbsolutePath() + " does not exist", files[i].exists());
            allNames.remove(files[i].getName());
        }
        assertTrue ("The following libs are not included in the classpath (" + classpath + "): " + allNames, allNames.isEmpty());
    }

    private static String getFakeCardsInstanceFileName(String kind) {
        StringBuilder sb = new StringBuilder(CommonSystemFilesystemPaths.SFS_ADD_HANDLER_REGISTRATION_ROOT + kind + "/" +
                FakeCardsFactory.class.getName().replace('.', '-'));
        sb.append(".instance");
        return sb.toString();
    }
    
    private static void delFo (File f) throws IOException {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        fo.delete();
        assertFalse (fo.isValid());
    }

    private static FileObject install (File platformDir, String name) throws IOException {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(platformDir));
        for (JavacardPlatformLocator l : Lookup.getDefault().lookupAll(JavacardPlatformLocator.class)) {
            if (l.accept(fo)) {
                return l.install(fo, name);
            }
        }
        throw new AssertionError("No module could set up a platform for " + platformDir);
    }

    private static File setUpFakePlatform(String propsName) throws Exception {
        InputStream in = PlatformDataObjectTest.class.getResourceAsStream(propsName + ".properties");
        Properties p = new Properties();
        try {
            p.load(in);
        } finally {
            in.close();
        }
        p.setProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME, propsName);
        File f = new File (System.getProperty("java.io.tmpdir"));
//        System.setProperty ("netbeans.user", f.getAbsolutePath());
        assertNotNull (f);
        String dirName = System.currentTimeMillis() + propsName;
        File dir = new File (f, dirName);
        assertTrue (dir.mkdir());
        File pprops = new File (dir, "platform.properties");
        assertTrue (pprops.createNewFile());
        OutputStream out = new BufferedOutputStream(new FileOutputStream(pprops));
        try {
            p.store(out, PlatformDataObjectTest.class.getName() + " fake platform " + propsName);
        } finally {
            out.close();
        }
        File libdir = new File (dir, "lib");
        assertTrue (libdir.mkdir());
        String[] names = getLibNames();
        for (String s : names) {
            File lib = new File (libdir, s);
            assertTrue (lib.createNewFile());
        }
        File bindir = new File (dir, "bin");
        assertTrue (bindir.mkdir());
        File fakeEmulator = new File (bindir, "cjcre.exe");
        assertTrue(fakeEmulator.createNewFile());
        return dir;
    }

    private static String[] getLibNames() {
        return new String[] {"api_connected.jar", "api_classic.jar", "additional.jar", "additional2.jar"};
    }

    public static final class FakeCards extends Cards implements Lookup.Provider {
        private final Provider src;
        FakeCards (Lookup.Provider src) {
            this.src = src;
        }
        @Override
        public List<? extends Provider> getCardSources() {
            return Collections.singletonList(this);
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
