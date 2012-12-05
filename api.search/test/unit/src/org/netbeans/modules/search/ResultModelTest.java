/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchFilter;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.matcher.TrivialFileMatcher;
import org.netbeans.modules.search.ui.ResultsOutlineSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class ResultModelTest extends NbTestCase {

    public ResultModelTest(String name) {
        super(name);
    }
    ResultTreeModel rtm;

    /**
     * Bug 203883 - [71cat] AssertionError at
     * org.netbeans.modules.search.ResultTreeModel.updateRootNodeSelection.
     *
     * If one file object is found twice (it can happen on some filesystems) and
     * then deleted, it should throw no error.
     */
    public void testObjectFoundTwice() throws Exception {

        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        final FileObject fo = root.createData("test.txt");

        final ResultModel rm = new ResultModel(
                new BasicSearchCriteria(), "test");

        rm.objectFound(fo, Charset.defaultCharset(), null);
        rm.objectFound(fo, Charset.defaultCharset(), null);

        assertEquals(1, rm.getMatchingObjects().size());

        final AtomicBoolean errorFound = new AtomicBoolean(false);
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                for (MatchingObject mo : rm.getMatchingObjects()) {
                    try {
                        mo.getFileObject().delete();
                    } catch (IOException ex) {
                        errorFound.set(true);
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        assertFalse(errorFound.get());
    }

    /**
     * The ConcurrentModificationException in bug 223221 is not thrown always.
     * It's better to run the test several times.
     */
    public void testBug223221ManyTimes() throws InterruptedException,
            InvocationTargetException {
        for (int i = 0; i < 5; i++) {
            testBug223221();
        }
    }

    public void testBug223221() throws InterruptedException,
            InvocationTargetException {
        final BasicSearchCriteria bsc = new BasicSearchCriteria();
        final FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        final ResultModel rm = new ResultModel(bsc, "test");
        final BasicComposition bc = new BasicComposition(
                new TestSearchInfo(root),
                new TrivialFileMatcher(), bsc, "test");
        final ResultsOutlineSupport[] rosHolder = new ResultsOutlineSupport[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                rosHolder[0] = new ResultsOutlineSupport(
                        false, true, rm, bc, Node.EMPTY);
            }
        });
        final Semaphore semaphore = new Semaphore(0);
        final Exception[] thrownException = new Exception[1];

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 500; i++) {
                    if (i == 100) {
                        semaphore.release();
                    }
                    try {
                        FileObject fo = root.createData(i + ".txt");
                        rm.objectFound(fo, Charset.defaultCharset(), null);
                        try {
                            EventQueue.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    rosHolder[0].setFlatMode();
                                    rosHolder[0].update();
                                    rosHolder[0].getResultsNode()
                                            .getChildren().getNodes(true);
                                }
                            });
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.tryAcquire(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    rosHolder[0].clean();
                } catch (ConcurrentModificationException e) {
                    thrownException[0] = e;
                }
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNull("No ConcurrentModificationException show be thrown.",
                thrownException[0]);
    }

    private static class TestSearchInfo extends SearchInfo {

        private FileObject searchRoot;

        public TestSearchInfo(FileObject searchRoot) {
            this.searchRoot = searchRoot;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            SearchRoot sr = new SearchRoot(searchRoot, (List<SearchFilter>) null);
            return Collections.singletonList(sr);
        }

        @Override
        protected Iterator<FileObject> createFilesToSearchIterator(
                SearchScopeOptions options, SearchListener listener, AtomicBoolean terminated) {
            return Collections.<FileObject>emptyList().iterator();
        }

        @Override
        protected Iterator<URI> createUrisToSearchIterator(
                SearchScopeOptions options, SearchListener listener,
                AtomicBoolean terminated) {
            return null;
        }
    };
}
