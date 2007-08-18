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
package org.netbeans.modules.cnd.completion.cplusplus.ext;
import java.beans.PropertyVetoException;
import java.io.*;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * Completion module API test: completion/CompletionTestPerformer
 * </B>
 * </FONT>
 * 
 * <P>
 * <B>What it tests:</B><BR>
 * The purpose of this test is to test C/C++ code completion. This test
 * is done on some layer between user and API. It uses file and completion
 * is called on the top of the file, but it is never shown.
 * </P>
 * 
 * <P>
 * <B>How it works:</B><BR>
 * TestFile is opened, given text is written to it, and code completion is
 * asked to return response for (row, col) position of document.
 * The type of completion is defined by the type of the file. 
 * Unfortunately, it is not possible to ask completion for response
 * without opening the file.
 * </P>
 * 
 * <P>
 * <B>Settings:</B><BR>
 * This test is not complete test, it's only stub, so for concrete test instance
 * it's necessary to provide text to add and whether the response should be
 * sorted. No more settings needed, when runned on clean build.
 * </P>
 * 
 * <P>
 * <B>Output:</B><BR>
 * The output should be completion reponse in human readable form.
 * </P>
 * 
 * <P>
 * <B>Possible reasons of failure:</B><BR>
 * <UL>
 * <LI>An exception when obtaining indent engine (for example if it doesn't exist).</LI>
 * <LI>An exception when writting to indent engine.</LI>
 * <LI>Possibly unrecognized MIME type.</LI>
 * <LI>Indent engine error.</LI>
 * <LI>The file can not be opened. This test must be able to open the file.
 * The test will fail if it is not able to open the file. In case it starts
 * opening sequence, but the editor is not opened, it may lock.</LI>
 * </UL>
 * </P>
 * 
 * 
 * (copy of Jan Lahoda CompletionTest.java)
 * 
 * @author Vladimir Voskresensky
 * @version 1.0
 */
public class CompletionTestPerformer {
    
    private static final long OPENING_TIMEOUT = 60 * 1000;
    private static final long SLEEP_TIME = 1000;
    
    /**
     * Creates new CompletionTestPerformer
     */
    public CompletionTestPerformer() {
    }
    
    private CompletionItem[] completionQuery(
            PrintWriter  log,
            JEditorPane  editor,
            BaseDocument doc,
            int caretOffset,
            boolean      unsorted,
            int queryType
            ) {
        doc = doc == null ? Utilities.getDocument(editor) : doc;
        SyntaxSupport support = doc.getSyntaxSupport();
        CsmCompletionQuery query = CsmCompletionProvider.getCompletionQuery();
        CsmCompletionQuery.CsmCompletionResult res = (CsmCompletionQuery.CsmCompletionResult)query.query(editor, doc, caretOffset, support, false, !unsorted);
        
        CompletionItem[] array =  res == null ? new CompletionItem[0] : (CompletionItem[])res.getData().toArray(new CompletionItem[res.getData().size()]);
        assert array != null;
        return array;
    }
    
    private String getStringFromCharSequence(CharSequence chs) {
        int length = chs.length();
        String text = chs.subSequence(0, length).toString();
        return text;
    }
    
    /**Currently, this method is supposed to be runned inside the AWT thread.
     * If this condition is not fullfilled, an IllegalStateException is
     * thrown. Do NOT modify this behaviour, or deadlock (or even Swing
     * or NetBeans winsys data corruption) may occur.
     *
     * Currently threading model of this method is compatible with
     * editor code completion threading model. Revise if this changes
     * in future.
     */
    private CompletionItem[] testPerform(PrintWriter log,
            JEditorPane editor,
            BaseDocument doc,
            boolean unsorted,
            String textToInsert, int offsetAfterInsertion, 
            int lineIndex,
            int colIndex,
            int queryType) throws BadLocationException, IOException {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("The testPerform method may be called only inside AWT event dispatch thread.");
        }
        
        doc = doc == null ? Utilities.getDocument(editor) : doc;
        assert doc != null;
        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);
        
        if (textToInsert.length() > 0) {
            doc.atomicLock();
            try {
                doc.insertString(offset, textToInsert, null);
            } finally {
                doc.atomicUnlock();
            }
            saveDocument((DataObject) doc.getProperty(BaseDocument.StreamDescriptionProperty));
            offset += textToInsert.length() + offsetAfterInsertion;
        }
        if (editor != null) {
            editor.grabFocus();
            editor.getCaret().setDot(offset);
        }        
        return completionQuery(log, editor, doc, offset, unsorted, queryType);
    }
    
    public CompletionItem[] test(final PrintWriter log,
            final String textToInsert, int offsetAfterInsertion, final boolean unsorted,
            final File testSourceFile, final int line, final int col) throws Exception {
        return test(log, textToInsert, offsetAfterInsertion, unsorted, testSourceFile, line, col, CompletionProvider.COMPLETION_QUERY_TYPE);
    }
    
    private CompletionItem[] test(final PrintWriter log,
            final String textToInsert, final int offsetAfterInsertion, final boolean unsorted,
            final File testSourceFile, final int line, final int col, final int queryType) throws Exception {
        try {
            final CompletionItem[][] array = new CompletionItem[][] {null};
            log.println("Completion test start.");
            log.flush();
            
            FileObject testFileObject = getTestFile(testSourceFile, log);
            final DataObject testFile = DataObject.find(testFileObject);
            if (testFile == null) {
                throw new DataObjectNotFoundException(testFileObject);
            }
            try {
                final BaseDocument doc = CndCoreTestUtils.getBaseDocument(testFile);
                Runnable run = new Runnable() {
                    public void run() {
                        try {
                            array[0] = testPerform(log, null, doc, unsorted, textToInsert, offsetAfterInsertion, line, col, queryType);
                        } catch (IOException ex) {
                            ex.printStackTrace(log);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace(log);
                        }
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    run.run();
                } else {
                    SwingUtilities.invokeAndWait(run);
                }
            } finally {
                testFile.setModified(false);
                log.flush();
            }
            //((CloseCookie) testFile.getCookie(CloseCookie.class)).close();
            return array[0] == null ? new CompletionItem[0] : array[0];
        } catch (Exception e) {
            e.printStackTrace(log);
            throw e;
        }
    }
    
    private FileObject getTestFile(File testFile, PrintWriter log) throws IOException, InterruptedException, PropertyVetoException {
        FileObject test = FileUtil.toFileObject(testFile);
        CsmFile csmFile = CsmModelAccessor.getModel().findFile(testFile.getAbsolutePath());
        if (test == null || csmFile == null) {
            throw new IllegalStateException("Given test file does not exist.");
        }
        log.println("File found: " + csmFile);
        return test;
    }
    
    private static void saveDocument(DataObject file) throws IOException { //!!!WARNING: if this exception is thrown, the test may be locked (the file in editor may be modified, but not saved. problems with IDE finishing are supposed in this case).
        SaveCookie sc = file.getCookie(SaveCookie.class);
        
        if (sc != null) {
            sc.save();
        }
    }
}
