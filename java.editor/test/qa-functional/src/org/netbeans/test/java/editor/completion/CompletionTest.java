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
package org.netbeans.test.java.editor.completion;

import java.io.*;
import javax.swing.text.*;
import javax.swing.*;
import java.util.List;
import java.util.Arrays;
import org.openide.filesystems.FileObject;
//import java.lang.reflect.UndeclaredThrowableException;
//import org.netbeans.api.project.Project;
//import org.netbeans.api.project.ProjectManager;

//import org.netbeans.editor.*;
//import org.netbeans.editor.ext.*;
//import org.openide.nodes.*;
//import org.openide.*;
//import org.openide.cookies.*;
//import org.openide.filesystems.*;
//import org.openide.loaders.*;
//import org.openide.util.Task;   //Some compilation error occurs when this class is imported directly (class Task not found).
//import org.openide.util.*;
//import org.openide.cookies.EditorCookie;
//import org.netbeans.editor.Utilities;
//import org.netbeans.modules.project.ui.OpenProjectList;
//import org.openide.cookies.SourceCookie;
//import org.openide.src.SourceElement;
//import org.openide.text.CloneableEditorSupport;
//import org.netbeans.junit.ide.ProjectSupport;

/**<FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * Editor module API test: completion/CompletionTest
 * </B>
 * </FONT>
 *
 * <P>
 * <B>What it tests:</B><BR>
 * The purpose of this test is to test Java (and HTML) code completion. This test
 * is done on some layer between user and API. It uses file and completion
 * is called on the top of the file, but it is never shown.
 * </P>
 *
 * <P>
 * <B>How it works:</B><BR>
 * TestFile is opened, given text is written to it, and code completion is
 * asked to return response. The type of completion is defined by the type of
 * the file. Unfortunately, it is not possible to ask completion for response
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
 * @author  Jan Lahoda
 * @version 1.0
 */
public class CompletionTest extends java.lang.Object {
    
    private static final long OPENING_TIMEOUT = 60 * 1000;
    private static final long SLEEP_TIME = 1000;
    
    /** Creates new CompletionTest */
    public CompletionTest() {
    }
    
    private void completionQuery(PrintWriter  out,
                                 PrintWriter  log,
                                 JEditorPane  editor,
                                 boolean      sort
                                ) {
        throw new UnsupportedOperationException("Commented out after retouche merge");
//        BaseDocument doc = Utilities.getDocument(editor);
//        SyntaxSupport support = doc.getSyntaxSupport();
//        
//        Completion completion = ExtUtilities.getCompletion(editor);
//        
//        if (completion != null) {
//            CompletionQuery completionQuery = completion.getQuery();
//            
//            if (completionQuery != null) {
//                CompletionQuery.Result query = completionQuery.query(editor, editor.getCaret().getDot(), support);
//                
//                if (query != null) {
//                    List list = query.getData();
//                    
//                    if (list != null) {
//                        
//                        String[] texts = new String[list.size()];
//                        for (int cntr = 0; cntr < list.size(); cntr++) {
//                            texts[cntr] = list.get(cntr).toString();
//                        };
//                        if (sort)
//                            Arrays.sort(texts);
//                        
//                        for (int cntr = 0; cntr < texts.length; cntr++) {
//                            out.println(texts[cntr].toString());
//                        };
//                    } else {
//                        log.println("CompletionTest: query.getData() == null");
//                        throw new IllegalStateException("CompletionTest: query.getData() == null");
//                    }
//                } else {
//                    log.println("CompletionTest: completionQuery.query(pane, end, support) == null");
//                    throw new IllegalStateException("CompletionTest: completionQuery.query(pane, end, support) == null");
//                }
//            } else {
//                log.println("CompletionTest: completion.getQuery() == null");
//                throw new IllegalStateException("CompletionTest: completion.getQuery() == null");
//            }
//        } else {
//            log.println("CompletionTest: ExtUtilities.getCompletion(pane) == null");
//            throw new IllegalStateException("CompletionTest: ExtUtilities.getCompletion(pane) == null");
//        }
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
    private void testPerform(PrintWriter out, PrintWriter log,
    JEditorPane editor,
    boolean sort,
    String assign,
    int lineIndex) throws BadLocationException, IOException {
        throw new UnsupportedOperationException("Commented out after retouche merge");
//        if (!SwingUtilities.isEventDispatchThread())
//            throw new IllegalStateException("The testPerform method may be called only inside AWT event dispatch thread.");
//        
//        BaseDocument doc        = Utilities.getDocument(editor);
//        int          lineOffset = Utilities.getRowStartFromLineOffset(doc, lineIndex -1);
//        
//        editor.grabFocus();
//        editor.getCaret().setDot(lineOffset);
//        doc.insertString(lineOffset, assign, null);
//        reparseDocument((DataObject) doc.getProperty(doc.StreamDescriptionProperty));
//        completionQuery(out, log, editor, sort);
    }
    
    public void test(final PrintWriter out, final PrintWriter log,
                     final String assign, final boolean sort,
                     final File dataDir, final String projectName,
                     final String testFileName, final int line) throws Exception {
                throw new UnsupportedOperationException("Commented out after retouche merge");
//        try {
//            log.println("Completion test start.");
//            log.flush();
//            
//            FileObject testFileObject = getTestFile(dataDir, projectName, testFileName, log);
//            final DataObject testFile       = DataObject.find(testFileObject);
//            
//            try {
//                Runnable test = new Runnable() {
//                    public void run() {
//                        try {
//                            JEditorPane editor  = getAnEditorPane(testFile, log);              
//                            testPerform(out, log, editor, sort, assign, line);
//                        } catch (Exception e) {
//                            e.printStackTrace(log);
//                        };
//                    }
//                };
//                if (SwingUtilities.isEventDispatchThread()) {
//                    test.run();
//                } else {
//                    SwingUtilities.invokeAndWait(test);
//                }
//            } finally {
//                testFile.setModified(false);
//                ((CloseCookie) testFile.getCookie(CloseCookie.class)).close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace(log);
//            throw e;
//        }
    }
    
    private FileObject getTestFile(File dataDir, String projectName, String testFile, PrintWriter log) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Commented out after retouche merge");
//        File projectFile = new File(dataDir, projectName);
//        FileObject project = FileUtil.toFileObject(projectFile);
//        Object prj= ProjectSupport.openProject(projectFile);
//        
//        if (prj == null)
//            throw new IllegalStateException("Given directory \"" + project + "\" does not contain a project.");
//        
//        log.println("Project found: " + prj);
//
//        FileObject test = project.getFileObject("src/" + testFile);
//        
//        if (test == null)
//            throw new IllegalStateException("Given test file does not exist.");
//        
//        return test;
    }
    
//    public static void main(String[] args) throws Exception {
//        PrintWriter out = new PrintWriter(System.out);
//        PrintWriter log = new PrintWriter(System.err);
//        new CompletionTest().test(out, log, "int[] a; a.", false, "org/netbeans/test/editor/completion/data/testfiles/CompletionTestPerformer/TestFile.java", 20);
//        out.flush();
//        log.flush();
//    }
    
    //Utility methods:
//    private static void checkEditorCookie(DataObject od) {
//        EditorCookie ec =(EditorCookie) od.getCookie(EditorCookie.class);
//        
//        if (ec == null)
//            throw new IllegalStateException("Given file (\"" + od.getName() + "\") does not have EditorCookie.");
//    }
    
//    private static JEditorPane getAnEditorPane(DataObject file, PrintWriter log) throws Exception {
//        EditorCookie  cookie = (EditorCookie)file.getCookie(EditorCookie.class);
//        
//        if (cookie == null)
//            throw new IllegalStateException("Given file (\"" + file.getName() + "\") does not have EditorCookie.");
//        
//        JEditorPane[] panes = cookie.getOpenedPanes();
//        long          start = System.currentTimeMillis();
//        
//        if (panes == null) {
//            //Prepare by opening a document. The actual opening into the editor
//            //should be faster (hopefully...).
//            cookie.openDocument();
//            cookie.open();
//            while ((panes = cookie.getOpenedPanes()) == null && (System.currentTimeMillis() - start) < OPENING_TIMEOUT) {
//                try {
//                    Thread.sleep(SLEEP_TIME);
//                } catch (InterruptedException e) {
//                    e.printStackTrace(log);
//                }
//            };
//            
//            log.println("Waiting spent: " + (System.currentTimeMillis() - start) + "ms.");
//        };
//        
//        if (panes == null)
//            throw new IllegalStateException("The editor was not opened. The timeout was: " + OPENING_TIMEOUT + "ms.");
//        
//        return panes[0];
//    }
    
//    private static boolean isSomePaneOpened(DataObject file) {
//        EditorCookie cookie = (EditorCookie) file.getCookie(EditorCookie.class);
//        
//        if (cookie == null) {
//            return false;
//        }
//        
//        return cookie.getOpenedPanes() != null;
//    }
    
//    private static void reparseDocument(DataObject file) throws IOException {
//        saveDocument(file);
//        SourceCookie sc = (SourceCookie) file.getCookie(SourceCookie.class);
//        
//        if (sc != null) {
//            SourceElement se = sc.getSource();
//            se.prepare().waitFinished();
//        } else {
//            System.err.println("reparseDocument: SourceCookie cookie not found in testFile!");
//        }
//    }
    
//    private static void saveDocument(DataObject file) throws IOException { //!!!WARNING: if this exception is thrown, the test may be locked (the file in editor may be modified, but not saved. problems with IDE finishing are supposed in this case).
//        SaveCookie sc = (SaveCookie) file.getCookie(SaveCookie.class);
//        
//        if (sc != null) {
//            sc.save();
//        }
//    }
  
}
