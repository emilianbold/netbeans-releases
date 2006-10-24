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
package org.netbeans.api.java.source.gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JEditorPane;
import junit.framework.Assert;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Pavel Flaska
 */
public abstract class GeneratorTest extends NbTestCase {

    private FileObject dataDir;
    
    File testFile = null;
    
    public GeneratorTest(String aName) {
        super(aName);
    }
    
    private void deepCopy(FileObject source, FileObject targetDirectory) throws IOException {
        for (FileObject child : source.getChildren()) {
            if (child.isFolder()) {
                FileObject target = targetDirectory.createFolder(child.getNameExt());
                
                deepCopy(child, target);
            } else {
                FileUtil.copyFile(child, targetDirectory, child.getName());
            }
        }
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        dataDir = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject dataTargetPackage = FileUtil.createFolder(dataDir, getSourcePckg());
        assertNotNull(dataTargetPackage);
        FileObject dataSourceFolder = FileUtil.toFileObject(getDataDir()).getFileObject(getSourcePckg());
        assertNotNull(dataSourceFolder);
        deepCopy(dataSourceFolder, dataTargetPackage);
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return ClassPathSupport.createClassPath(new FileObject[] {dataDir});
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type == ClassPath.BOOT)
                        return createClassPath(System.getProperty("sun.boot.class.path"));
                    return null;
            }
        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {loader, cpp});
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    public <R, P> void process(final Transformer<R, P> transformer) throws IOException {
        assertNotNull(testFile);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        JavaSource js = JavaSource.forFileObject(testSourceFO);
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {
            }
            public void run(WorkingCopy wc) throws IOException {
                wc.toPhase(Phase.RESOLVED);
                if (transformer instanceof MutableTransformer) {
                    MutableTransformer mutable = (MutableTransformer) transformer;
                    mutable.setWorkingCopy(wc);
                }
                SourceUtilsTestUtil2.run(wc, transformer);
            }
        }).commit();
        printFile();
    }
    
    private static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List/*<PathResourceImplementation>*/ list = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL(f);
            if (url!=null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }
    
    // XXX this method could probably be removed... use standard FileUtil stuff
    private static URL getRootURL  (File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
            } else {
                url = f.toURI().toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL(surl+"/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }
    
    private static boolean isArchiveFile(File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }
    
    String getGoldenDir() {
        return getDataDir() + "/goldenfiles";
    }
    
    String getSourceDir() {
        return FileUtil.toFile(dataDir).getAbsolutePath();
    }
    
    public static File getFile(String aDataDir, String aFileName) throws FileStateInvalidException {
        String result = new File(aDataDir).getAbsolutePath() + '/' + aFileName;
        return new File(result);
    }
    
    static JavaSource getJavaSource(File aFile) throws IOException {
        FileObject testSourceFO = FileUtil.toFileObject(aFile);
        assertNotNull(testSourceFO);
        return JavaSource.forFileObject(testSourceFO);
    }

    File getTestFile() {
        return testFile;
    }
    
    void assertFiles(final String aGoldenFile) throws IOException, FileStateInvalidException {
        assertFile("File is not correctly generated.",
            getTestFile(),
            getFile(getGoldenDir(), getGoldenPckg() + aGoldenFile),
            getWorkDir()
        );
    }
    
    void printFile() throws FileNotFoundException, IOException {
        PrintStream log = getLog();
        BufferedReader in = new BufferedReader(new FileReader(getTestFile()));
        String str;
        while ((str = in.readLine()) != null) {
            log.println(str);
        }
        in.close();
    }

    abstract String getGoldenPckg();

    abstract String getSourcePckg();

}
