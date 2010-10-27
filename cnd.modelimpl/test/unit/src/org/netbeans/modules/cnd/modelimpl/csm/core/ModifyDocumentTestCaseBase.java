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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Test for reaction on editor modifications
 * @author Vladimir Voskresensky
 */
public class ModifyDocumentTestCaseBase extends ProjectBasedTestCase {
    private final ObjectsChangeListener doListener = new ObjectsChangeListener();
    public ModifyDocumentTestCaseBase(String testName) {
        super(testName, true);
    }

    @Override
    protected void setUp() throws Exception {
        System.err.printf("setUp %s %d\n", getName(), System.currentTimeMillis());
        super.setUp();
        doListener.clear();
        DataObject.getRegistry().addChangeListener(doListener);
        ModelSupport.instance().startup();
        System.err.printf("setUp end %s %d\n", getName(), System.currentTimeMillis());
    }

    @Override
    protected void tearDown() throws Exception {
        System.err.printf("tearDown %s %d\n", getName(), System.currentTimeMillis());
        super.tearDown();
        ModelSupport.instance().shutdown();
        DataObject.getRegistry().removeChangeListener(doListener);
        doListener.clear();
        System.err.printf("tearDown end %s %d\n", getName(), System.currentTimeMillis());
    }

    protected final void deleteTextThenUndo(final File sourceFile, final int startPos, final int endPos, int numDecls, int numDeclsAfterRemove) throws Exception {
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST UNDO OF DELETE BLOCK\n");
        }
        final AtomicReference<Exception> exRef = new AtomicReference<Exception>();
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<CountDownLatch>();
        final CsmProject project = super.getProject();
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        assertNotNull(doc);
        final int length = doc.getLength();
        assertTrue(length > 0);
        final int delLen;
        if (endPos < 0) {
            delLen = length;
        } else {
            delLen = Math.min(endPos, length)-startPos;
        }
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener(fileImpl, condRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            int curNumDecls = fileImpl.getDeclarationsSize();
            assertEquals("different number of declarations", numDecls, curNumDecls);
            // insert dead code block
            // create barier
            CountDownLatch parse1 = new CountDownLatch(1);
            condRef.set(parse1);
            // modify document
            final UndoManager urm = getUndoRedoManager(sourceFile);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        doc.remove(startPos, delLen);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            try {
                assertTrue("must have undo", urm.canUndo());
                assertEquals("must have only one modified object", 1, this.doListener.size());
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    if (true || TraceFlags.TRACE_191307_BUG) {
                        exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    }
                } else {
                    curNumDecls = fileImpl.getDeclarationsSize();
                    assertEquals("different number of declarations", numDeclsAfterRemove, curNumDecls);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                if (exRef.get() == null) {
                    // let's undo changes
                    closeDocument(sourceFile, urm, doc, project, null);
                    curNumDecls = fileImpl.getDeclarationsSize();
                    assertEquals("different number of declarations after undo", numDecls, curNumDecls);
                }
            }
        } finally {
            System.err.flush();
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }
    
    protected final void insertDeadBlockText(final File sourceFile, final String ifdefTxt, final int pos,
                                    int deadBlocksBeforeModifcation, int deadBlocksAfterModifications) throws Exception {
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("TEST INSERT DEAD BLOCK\n");
        }
        final AtomicReference<Exception> exRef = new AtomicReference<Exception>();
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<CountDownLatch>();
        final CsmProject project = super.getProject();
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
            checkDeadBlocks(project, fileImpl, "1. text before inserting dead block:", doc, "File must have " + deadBlocksBeforeModifcation + " dead code blocks ", deadBlocksBeforeModifcation);

            // insert dead code block
            // create barier
            CountDownLatch parse1 = new CountDownLatch(1);
            condRef.set(parse1);
            // modify document
            final UndoManager urm = getUndoRedoManager(sourceFile);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (TraceFlags.TRACE_182342_BUG) {
                            System.err.printf("Inserting dead block in position %d: %s\n", pos, ifdefTxt);
                        }
                        doc.insertString(pos,
                                        ifdefTxt,
                                        null);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });

            try {
                assertTrue("must have undo", urm.canUndo());
                assertEquals("must have only one modified object", 1, this.doListener.size());
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    if (true || TraceFlags.TRACE_182342_BUG) {
                        exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    }
                } else {
                    checkDeadBlocks(project, fileImpl, "2. text after inserting dead block:", doc, "File must have " + deadBlocksAfterModifications + " dead code block ", deadBlocksAfterModifications);
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

    protected final void removeDeadBlock(final File sourceFile, int deadBlocksBeforeRemove, int deadBlocksAfterRemove) throws Exception {
        final AtomicReference<Exception> exRef = new AtomicReference<Exception>();
        final AtomicReference<CountDownLatch> condRef = new AtomicReference<CountDownLatch>();
        final CsmProject project = super.getProject();
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

            final List<CsmOffsetable> unusedCodeBlocks = checkDeadBlocks(project, fileImpl, "1. text before deleting dead block:", doc, "File must have " + deadBlocksBeforeRemove + " dead code block ", deadBlocksBeforeRemove);
            // insert dead code block
            // create barier
            CountDownLatch parse1 = new CountDownLatch(1);
            condRef.set(parse1);
            // modify document
            final UndoManager urm = getUndoRedoManager(sourceFile);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        for (CsmOffsetable block : unusedCodeBlocks) {
                            if (TraceFlags.TRACE_182342_BUG) {
                                System.err.printf("Removing dead block [%d-%d]\n", block.getStartOffset(), block.getEndOffset());
                            }
                            doc.remove(block.getStartOffset(), block.getEndOffset() - block.getStartOffset());
                        }
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            try {
                assertTrue("must have undo", urm.canUndo());
                assertEquals("must have only one modified object", 1, this.doListener.size());
                if (!parse1.await(20, TimeUnit.SECONDS)) {
                    if (true || TraceFlags.TRACE_182342_BUG) {
                        exRef.compareAndSet(null, new TimeoutException("not finished await"));
                    }
                } else {
                    checkDeadBlocks(project, fileImpl, "2. text after deleting dead block:", doc, "File must have " + deadBlocksAfterRemove + " dead code blocks ", deadBlocksAfterRemove);
                    assertEquals("must be exactly one parse event", 1, parseCounter.get());
                }
            } catch (InterruptedException ex) {
                exRef.compareAndSet(null, ex);
            } finally {
                closeDocument(sourceFile, urm, doc, project, listener);
            }
        } finally {
            System.err.flush();
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    private void saveDocument(final File sourceFile, final BaseDocument doc, final CsmProject project) throws DataObjectNotFoundException, BadLocationException, IOException {
        DataObject testDataObject = DataObject.find(CndFileUtils.toFileObject(sourceFile));
        SaveCookie save = testDataObject.getLookup().lookup(SaveCookie.class);
        assertNotNull(save);
        save.save();
        if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
            System.err.printf("document text after save\n==============\n%s\n===============\n", doc.getText(0, doc.getLength()));
        }
        project.waitParse();
    }
        
    private void closeDocument(final File sourceFile, final UndoManager urm, final BaseDocument doc, final CsmProject project, final CsmProgressListener listener) throws DataObjectNotFoundException, BadLocationException {
        if (listener != null) {
            CsmListeners.getDefault().removeProgressListener(listener);
        }
        urm.undo();
        DataObject testDataObject = DataObject.find(CndFileUtils.toFileObject(sourceFile));
        CloseCookie close = testDataObject.getLookup().lookup(CloseCookie.class);
        if (close != null) {
            close.close();
        }
        if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
            System.err.printf("document text after close\n==============\n%s\n===============\n", doc.getText(0, doc.getLength()));
        }
        project.waitParse();
    }
    
    private CsmProgressListener createFileParseListener(final FileImpl fileImpl, final AtomicReference<CountDownLatch> condRef, final AtomicInteger parseCounter) {
        final CsmProgressListener listener = new CsmProgressAdapter() {

            @Override
            public void fileParsingFinished(CsmFile file) {
                if (TraceFlags.TRACE_182342_BUG) {
                    new Exception(getName() + " fileParsingFinished " + file).printStackTrace(System.err); // NOI18N
                }
                parseCounter.incrementAndGet();
                if (file.equals(fileImpl)) {
                    CountDownLatch cond = condRef.get();
                    cond.countDown();
                }
            }
        };
        return listener;
    }

    private CsmProgressListener createFileParseListener2(final FileImpl fileImpl, final AtomicReference<Semaphore> condRef, final AtomicInteger parseCounter) {
        final CsmProgressListener listener = new CsmProgressAdapter() {

            @Override
            public void fileParsingFinished(CsmFile file) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    new Exception(getName() + " fileParsingFinished parsingState = " + ((FileImpl)file).getParsingStateFromTest() + " state = " + ((FileImpl)file).getState() + " " + file).printStackTrace(System.err); // NOI18N
                }
                parseCounter.incrementAndGet();
                if (file.equals(fileImpl)) {
                    Semaphore cond = condRef.get();
                    cond.release();
                }
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

    protected void insertTextThenSave(File sourceFile, final int insertLine, final String insertString, int numDecls, int numDeclsAfterModification, boolean doUndoRedo) throws Exception {
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST INSERT/SAVE then UNDO/REDO\n");
        }
        final AtomicReference<Exception> exRef = new AtomicReference<Exception>();
        final AtomicReference<Semaphore> condRef = new AtomicReference<Semaphore>();
        final CsmProject project = super.getProject();
        final FileImpl fileImpl = (FileImpl) getCsmFile(sourceFile);
        assertNotNull(fileImpl);
        final BaseDocument doc = getBaseDocument(sourceFile);
        final UndoManager urm = getUndoRedoManager(sourceFile);
        final int insertOffset = CndCoreTestUtils.getDocumentOffset(doc, insertLine, 0);
        assertNotNull(doc);
        project.waitParse();
        final AtomicInteger parseCounter = new AtomicInteger(0);
        CsmProgressListener listener = createFileParseListener2(fileImpl, condRef, parseCounter);
        CsmListeners.getDefault().addProgressListener(listener);
        try {
            int curNumDecls = fileImpl.getDeclarationsSize();
            assertEquals("different number of declarations", numDecls, curNumDecls);
            // insert dead code block
            // create barier
            Semaphore waitParseSemaphore = new Semaphore(0);
            condRef.set(waitParseSemaphore);
            // modify document
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        doc.insertString(insertOffset, insertString, null);
                    } catch (BadLocationException ex) {
                        exRef.compareAndSet(null, ex);
                    }
                }
            });
            assertTrue("must have undo", urm.canUndo());
            assertEquals("must have only one modified object", 1, this.doListener.size());
            waitParseSemaphore.acquire();
            assertTrue(fileImpl.isParsed());
            curNumDecls = fileImpl.getDeclarationsSize();
            assertEquals("different number of declarations", numDeclsAfterModification, curNumDecls);
            assertEquals("must be exactly one parse event", 1, parseCounter.get());
            // let's save changes
            saveDocument(sourceFile, doc, project);
            waitParseSemaphore.acquire();
            assertTrue(fileImpl.isParsed());
            curNumDecls = fileImpl.getDeclarationsSize();
            assertEquals("different number of declarations after save", numDeclsAfterModification, curNumDecls);
//            assertEquals("must be exactly two parse events", 2, parseCounter.get());
            assertEquals("must have zero modified object", 0, this.doListener.size());
            
            assertTrue("must have undoable modification", urm.canUndo());
            if (doUndoRedo) {
                urm.undo();            
                waitParseSemaphore.acquire();
                assertTrue(fileImpl.isParsed());
                curNumDecls = fileImpl.getDeclarationsSize();
                assertEquals("different number of declarations after save and undo", numDecls, curNumDecls);
    //            assertEquals("must be exactly three parse events", 3, parseCounter.get());
                assertEquals("must have only one modified object", 1, this.doListener.size());

                assertTrue("must have redoable modification", urm.canRedo());
                urm.redo();
                waitParseSemaphore.acquire();
                assertTrue(fileImpl.isParsed());
                curNumDecls = fileImpl.getDeclarationsSize();
                assertEquals("different number of declarations after save and undo", numDeclsAfterModification, curNumDecls);
    //            assertEquals("must be exactly four parse events", 4, parseCounter.get());
                assertEquals("must have zero modified object", 0, this.doListener.size());
            }
        } finally {
            System.err.flush();
            CsmListeners.getDefault().removeProgressListener(listener);
            Exception ex = exRef.get();
            if (ex != null) {
                throw ex;
            }
        }
    }

    private static final class ObjectsChangeListener implements ChangeListener {
        private final Set<DataObject> modifiedDOs = new HashSet<DataObject>();
        @Override
        public void stateChanged(ChangeEvent e) {
            DataObject[] objs = DataObject.getRegistry().getModified();
            modifiedDOs.clear();
            modifiedDOs.addAll(Arrays.asList(objs));
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                System.err.println("ObjectsChangeListener: stateChanged " + e);
                ModelSupport.traceDataObjectRegistryStateChanged(e);
            }
        }

        public void clear() {
            modifiedDOs.clear();
        }

        public int size() {
            return modifiedDOs.size();
        }
    }
}
