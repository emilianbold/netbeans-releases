/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.index;

import java.util.Collection;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class PHPIndexTest extends NbTestCase {
    
    public PHPIndexTest(String testName) {
        super(testName);
    }            

    private static final String FOLDER = "GsfPlugins";
    
    @Override
    public void setUp() throws Exception {
        FileObject f = FileUtil.getConfigFile(FOLDER + "/text/html");
        if (f != null) {
            f.delete();
        }
        
        FileUtil.setMIMEType("php", FileUtils.PHP_MIME_TYPE);
        Logger.global.setFilter(new Filter() {
            public boolean isLoggable(LogRecord record) {
                Throwable t = record.getThrown();
                
                if (t == null) {
                    return true;
                }
                
                for (StackTraceElement e : t.getStackTrace()) {
                    if (   "org.netbeans.modules.php.editor.index.GsfUtilities".equals(e.getClassName())
                        && "getBaseDocument".equals(e.getMethodName())
                        && t instanceof ClassNotFoundException) {
                        return false;
                    }
                }
                return false;
            }
        });
    }
    
    public void testLookup1() throws Exception {
        // #138463
        if (true) {
            return;
        }
        performTest(new UserTask() {
            public void cancel() {}

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parameter = (PHPParseResult)resultIterator.getParserResult();
                PHPIndex phpIndex = PHPIndex.get(parameter);

                Collection<IndexedClass> classes = phpIndex.getClasses(parameter, "te", QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);

                assertEquals(1, classes.size());
            }
        },
        "<?php\n" +
        "class test {}\n" +
        "?>");
    }
    
    public void testLookup2() throws Exception {
        // #138463
        if (true) {
            return;
        }
        performTest(new UserTask() {
            public void cancel() {}
            
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parameter = (PHPParseResult)resultIterator.getParserResult();
                PHPIndex phpIndex = PHPIndex.get(parameter);
                Collection<IndexedClass> classes = phpIndex.getClasses(parameter,
                        "TE", QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);

                assertEquals(1, classes.size());
            }
        },
        "<?php\n" +
        "class TEST {}\n" +
        "?>");
    }
    
    public void DISABLEDtestLookupCaseInsensitive() throws Exception {
        performTest(new UserTask() {
            public void cancel() {}
            
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parameter = (PHPParseResult)resultIterator.getParserResult();
                PHPIndex phpIndex = PHPIndex.get(parameter);

                Collection<IndexedClass> classes = phpIndex.getClasses(parameter, "te", QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);

                assertEquals(1, classes.size());
            }
        },
        "<?php\n" +
        "class TEST {}\n" +
        "?>");
    }
    
    protected static String computeFileName(int index) {
        return "test" + (index == (-1) ? "" : (char) ('a' + index)) + ".php";
    }
    
    protected void performTest(final UserTask task, String... code) throws Exception {
        assert false;
//        clearWorkDir();
//        FileUtil.refreshAll();
//
//        FileObject workDir = FileUtil.toFileObject(getWorkDir());
//        FileObject cache = workDir.createFolder("cache");
//        FileObject folder = workDir.createFolder("src");
//        FileObject cluster = workDir.createFolder("cluster");
//        int index = -1;
//
//        for (String c : code) {
//            FileObject f = FileUtil.createData(folder, computeFileName(index));
//            TestUtilities.copyStringToFile(f, c);
//            index++;
//        }
//
//        System.setProperty("netbeans.user", FileUtil.toFile(cache).getAbsolutePath());
//        PHPIndex.setClusterUrl(cluster.getURL().toExternalForm());
//        CountDownLatch l = RepositoryUpdater.getDefault().scheduleCompilationAndWait(folder, folder);
//
//        l.await();
//
//        final FileObject test = folder.getFileObject("test.php");
//
//        Document doc = openDocument(test);
//
//        ClassPath empty = ClassPathSupport.createClassPath(new FileObject[0]);
//        ClassPath source = ClassPathSupport.createClassPath(folder);
//        ClasspathInfo info = ClasspathInfo.create(empty, empty, source);
//        Source s = Source.create(info, test);
//
//        s.runUserActionTask(new CancellableTask<CompilationController>() {
//            public void cancel() {}
//            public void run(CompilationController parameter) throws Exception {
//                parameter.toPhase(Phase.UP_TO_DATE);
//
//                task.run(parameter);
//            }
//        }, true);
    }

    private static Document openDocument(FileObject fileObject) throws Exception {
        DataObject dobj = DataObject.find(fileObject);

        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        return ec.openDocument();
    }

}
