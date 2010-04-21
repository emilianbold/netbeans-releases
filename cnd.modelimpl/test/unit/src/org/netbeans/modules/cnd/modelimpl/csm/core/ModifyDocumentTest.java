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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Test for reaction on editor modifications
 * @author Vladimir Voskresensky
 */
public class ModifyDocumentTest extends ProjectBasedTestCase {
    public ModifyDocumentTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ModelSupport.instance().startup();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ModelSupport.instance().shutdown();
        TraceFlags.TRACE_182342_BUG = false;
    }

    public void testInsertDeadBlock() throws Exception {
        final AtomicReference<Exception> exRef = new AtomicReference<Exception>();
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<CountDownLatch>();
        final CsmProject project = super.getProject();
        final File sourceFile = getDataFile("fileWithoutDeadCode.cc");
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        assertNotNull(doc);
        assertTrue(doc.getLength() > 0);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            checkDeadBlocks(project, fileImpl, "1. text before inserting dead block:", doc, "File must have no dead code blocks ", 0);

            // insert dead code block
            // create barier
            CountDownLatch parse1 = new CountDownLatch(1);
            condRef.set(parse1);
            // modify document
            UndoManager urm = new UndoManager();
            doc.addUndoableEditListener(urm);
            final String ifdefTxt = "#ifdef AAA\n"
                                  + "    dead code text\n"
                                  + "#endif\n";
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        doc.insertString(0,
                                        ifdefTxt,
                                        null);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });

            try {
                if (!parse1.await(10, TimeUnit.SECONDS)) {
                    exRef.compareAndSet(null, new TimeoutException("not finished await"));
                } else {
                    checkDeadBlocks(project, fileImpl, "2. text after inserting dead block:", doc, "File must have one dead code block ", 1);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                closeDocument(sourceFile, urm, doc, project, listener);
            }
        } finally {
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    private void closeDocument(final File sourceFile, final UndoManager urm, final BaseDocument doc, final CsmProject project, final CsmProgressListener listener) throws DataObjectNotFoundException {
        CsmListeners.getDefault().removeProgressListener(listener);
        urm.undo();
        DataObject testDataObject = DataObject.find(FileUtil.toFileObject(sourceFile));
        CloseCookie close = testDataObject.getLookup().lookup(CloseCookie.class);
        if (close != null) {
            close.close();
        }
        project.waitParse();
    }

    public void testRemoveDeadBlock() throws Exception {
        TraceFlags.TRACE_182342_BUG = true;
        final AtomicReference<Exception> exRef = new AtomicReference<Exception>();
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<CountDownLatch>();
        final CsmProject project = super.getProject();
        final File sourceFile = getDataFile("fileWithDeadCode.cc");
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        assertNotNull(doc);
        assertTrue(doc.getLength() > 0);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {

            List<CsmOffsetable> unusedCodeBlocks = checkDeadBlocks(project, fileImpl, "1. text before deleting dead block:", doc, "File must have one dead code block ", 1);
            final CsmOffsetable block = unusedCodeBlocks.iterator().next();
            // insert dead code block
            // create barier
            CountDownLatch parse1 = new CountDownLatch(1);
            condRef.set(parse1);
            // modify document
            UndoManager urm = new UndoManager();
            doc.addUndoableEditListener(urm);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        doc.remove(block.getStartOffset(), block.getEndOffset() - block.getStartOffset());
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            try {
                if (!parse1.await(10, TimeUnit.SECONDS)) {
                    exRef.compareAndSet(null, new TimeoutException("not finished await"));
                } else {
                    checkDeadBlocks(project, fileImpl, "2. text after deleting dead block:", doc, "File must have no dead code blocks ", 0);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                closeDocument(sourceFile, urm, doc, project, listener);
            }
        } finally {
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    private CsmProgressListener createFileParseListener(final FileImpl fileImpl, final AtomicReference<CountDownLatch> condRef, final AtomicInteger parseCounter) {
        final CsmProgressListener listener = new CsmProgressAdapter() {

            @Override
            public void fileParsingFinished(CsmFile file) {
                if (TraceFlags.TRACE_182342_BUG) {
                    new Exception("fileParsingFinished " + file).printStackTrace(System.err); // NOI18N
                }
                if (file.equals(fileImpl)) {
                    CountDownLatch cond = condRef.get();
                    cond.countDown();
                }
                parseCounter.incrementAndGet();
            }
        };
        return listener;
    }

    private List<CsmOffsetable> checkDeadBlocks(final CsmProject project, final FileImpl fileImpl, String docMsg, final BaseDocument doc, String msg, int expectedDeadBlocks) throws BadLocationException {
        project.waitParse();
        List<CsmOffsetable> unusedCodeBlocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(fileImpl);
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("%s\n==============\n%s\n===============\n", docMsg, doc.getText(0, doc.getLength()));
            if (unusedCodeBlocks.isEmpty()) {
                System.err.println("NO DEAD BLOCKS");
            } else {
                int i = 0;
                for (CsmOffsetable csmOffsetable : unusedCodeBlocks) {
                    System.err.printf("DEAD BLOCK %d: [%d-%d]\n", i++, csmOffsetable.getStartOffset(), csmOffsetable.getEndOffset());
                }
            }
        }
        assertEquals(msg + fileImpl.getAbsolutePath(), expectedDeadBlocks, unusedCodeBlocks.size());
        return unusedCodeBlocks;
    }
}
