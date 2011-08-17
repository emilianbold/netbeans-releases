/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openidex.search.SearchInfo;

/** 
 * @author jhavlin
 */
public class MatchingObjectTest extends NbTestCase {

    public MatchingObjectTest() {
        super("MatchingObjectTest");
    }

    /**
     * Adapt string to case of a pattern.
     * 
     * @param found Found string - case pattern.
     * @param replacement Replacement string - adapted value.
     * @return 
     */
    public String adapt(String found, String replacement) {
        return MatchingObject.adaptCase(replacement, found);
    }

    /** Test adaption of case of replacement strings - simple.
     * 
     * @throws Exception 
     */
    public void testAdaptCase() throws Exception {

        assertEquals("next1", adapt("test1", "next1"));
        assertEquals("Next1", adapt("Test1", "next1"));
        assertEquals("NEXT1", adapt("TEST1", "next1"));
        assertEquals("nEXT1", adapt("tEST1", "next1"));
    }

    /** Test adaption of case of replacement strings - camel case.
     * 
     * @throws Exception 
     */
    public void testAdaptCaseCamelCase() throws Exception {

        assertEquals("somethingElse", adapt("fooBar", "SomethingElse"));
        assertEquals("somethingelse", adapt("foobar", "SomethingElse"));

        assertEquals("MBox", adapt("JPanel", "MBox"));
        assertEquals("mBox", adapt("jPanel", "MBox"));
        assertEquals("mbox", adapt("jpanel", "MBox"));

        assertEquals("DaRealJunk", adapt("MyClass", "DaRealJunk"));
        assertEquals("daRealJunk", adapt("myClass", "DaRealJunk"));
        assertEquals("darealjunk", adapt("myclass", "DaRealJunk"));

        assertEquals("MyClass", adapt("DaRealJunk", "MyClass"));
        assertEquals("myClass", adapt("daRealJunk", "MyClass"));
        assertEquals("myclass", adapt("darealjunk", "MyClass"));

        assertEquals("FooBar", adapt("Foo", "fooBar"));
    }

    /** Test replacing in filesystem files.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws InvocationTargetException 
     */
    public void testReplaceInFilePreserveCase() throws IOException,
            InterruptedException,
            InvocationTargetException {

        assertEquals("writing data",
                replaceInFilePreserveCase("writing dta", "dta", "data"));

        assertEquals("writing DATA",
                replaceInFilePreserveCase("writing DTA", "dta", "data"));

        assertEquals("writing Data",
                replaceInFilePreserveCase("writing Dta", "dta", "data"));

        assertEquals("writing dATA",
                replaceInFilePreserveCase("writing dTA", "dta", "data"));
    }

    /** Helper method - create file, write its content, find it, replace in it
     * and return its new content.
     * 
     * @param fileContent
     * @param find
     * @param replace
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws InvocationTargetException 
     */
    public String replaceInFilePreserveCase(String fileContent, String find,
            String replace) throws IOException, InterruptedException,
            InvocationTargetException {

        FileObject fo = null;
        try {
            fo = writeTempFile(fileContent);

            BasicSearchCriteria bsc = new BasicSearchCriteria();
            bsc.setTextPattern(find);
            bsc.setRegexp(false);
            bsc.setReplaceExpr(replace);
            bsc.setPreserveCase(true);
            bsc.onOk();

            SearchScope ss = new TempFileSearchScope(fo);
            final SearchTask st = new SearchTask(ss, bsc,
                    Collections.EMPTY_LIST);
            final ResultModel rm = st.getResultModel();
            rm.setObserver(new ResultTreeModel(rm));

            Runnable setPanel = new Runnable() {

                @Override
                public void run() {
                    rm.setObserver(new ResultViewPanel(st));
                }
            };
            SwingUtilities.invokeAndWait(setPanel);            

            Thread stt = new Thread(st);
            stt.start();
            stt.join();           

            ReplaceTask rt = new ReplaceTask(
                    st.getResultModel().getMatchingObjects());
            Thread rtt = new Thread(rt);
            rtt.start();
            rtt.join();

            String result = readFile(fo);
            return result;
        } finally {
            fo.delete();
        }
    }

    /** Search scope containing one temporary file only.
     * 
     */
    public static class TempFileSearchScope extends SearchScope {

        private FileObject fo;

        public TempFileSearchScope(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public String getTypeId() {
            return "test";
        }

        @Override
        protected String getDisplayName() {
            return "test search scope";
        }

        @Override
        protected boolean isApplicable() {
            return true;
        }

        @Override
        protected void addChangeListener(ChangeListener l) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void removeChangeListener(ChangeListener l) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected SearchInfo getSearchInfo() {
            return new SearchInfo() {

                @Override
                public boolean canSearch() {
                    return true;
                }

                @Override
                public Iterator<DataObject> objectsToSearch() {
                    DataObject dataObject = null;
                    try {
                        dataObject = DataObject.find(fo);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    List<DataObject> l =
                            Collections.singletonList(dataObject);
                    return l.iterator();
                }
            };
        }
    }

    /** Write temporary file with simple string content.
     * 
     * @param content Content of the file.
     * @return
     * @throws IOException 
     */
    public FileObject writeTempFile(String content) throws IOException {

        FileObject fo = null;
        OutputStream os = null;
        OutputStreamWriter osw = null;

        try {
            File f = File.createTempFile("matchingObjectTest", ".tst");
            f.deleteOnExit();
            fo = FileUtil.createData(f);
            os = fo.getOutputStream();
            osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(content);
        } finally {
            try {
                osw.close();
            } catch (Throwable t) {
            }
            try {
                os.close();
            } catch (Throwable t) {
            }
        }

        return fo;
    }

    /** Read content of a simple text file.
     * 
     * @param fo
     * @return
     * @throws IOException 
     */
    public String readFile(FileObject fo) throws IOException {

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            is = fo.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);

            String line = null;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                } else {
                    sb.append('\n');
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            try {
                br.close();
            } catch (Throwable t) {
            }
            try {
                isr.close();
            } catch (Throwable t) {
            }
            try {
                is.close();
            } catch (Throwable t) {
            }
        }
    }
}
