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

package org.netbeans.test.web.core.syntax;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import junit.framework.Test;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery.ResultItem;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.netbeans.test.web.TextGraphics2D;
import org.netbeans.test.web.Waiter;
import org.openide.actions.UndoAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.actions.SystemAction;



/**
 * Test goes throught files and looking for CC Test Steps.
 * The CC Test Steps are writen in document body as three lines:
 * JSP comments which start with '<%--CC' prefix, where:
 *<ul>
 *<li> first line contains ccPrefix with optional '|' character which represents
 * cursor position
 *<li> second line contains ccChoice item which will be used for CC substitution
 *<li> third line contains ccResult
 *</ul>
 *
 * For example:<p>
 * <pre><%--CC
 * <%@ taglib |
 * uri
 * <%@ taglib uri=""
 * --%>
 * </pre><p>
 * does:
 * <ul>
 * <li> inserts '<%@ taglib ' string into new line
 * <li> invokes CC
 * <li> dumps Completion Query Result
 * <li> choses "uri" item from the query result and substitute it
 * <li> checks if subtituted line is: '<%@ taglib uri=""'
 * <li> undoes all changes
 * </ul>
 * @author ms113234
 *
 */
public class CompletionTest extends JellyTestCase {
    private static boolean GenerateGoldenFiles = false;//generate golden files, or test
    
    protected FileObject testFileObj;
    protected boolean debug = false;
    protected final static List xmlExts = Arrays.asList(new String[]
    {"xml","xsd","html","tld","jspx","tagx"});
    protected final static List jspExts = Arrays.asList(new String[] {"jsp","tag"});
    protected final static List jsExts = Arrays.asList(new String[] {"js"});
    /** Need to be defined because of JUnit */
    public CompletionTest(String name, FileObject testFileObj) {
        super(name);
        this.testFileObj = testFileObj;
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    public static Test suite() {
        // find folder with test projects and define file objects filter
        File datadir = new CompletionTest(null, null).getDataDir();
        File projectsDir = new File(datadir, "CompletionTestProjects");
        FileObjectFilter filter = new FileObjectFilter() {
            public boolean accept(FileObject fo) {
                String ext = fo.getExt();
                String name = fo.getName();
                return (name.startsWith("test") || name.startsWith("Test"))
                        && (xmlExts.contains(ext) || jspExts.contains(ext) || jsExts.contains(ext)/* || ext.equals("java")*/);
            }
        };
        return RecurrentSuiteFactory.createSuite(CompletionTest.class,
                projectsDir, filter);
    }
    
    public void runTest() throws Exception {
        String ext = testFileObj.getExt();
        if (jspExts.contains(ext)) {
            test(testFileObj, "<%--CC", "--%>");
        } else if (xmlExts.contains(ext)) {
            test(testFileObj, "<!--CC", "-->");
        } else if (ext.equals("java")) {
            test(testFileObj, "/**CC", "*/");
        }else if (ext.equals("js")) {
            test(testFileObj, "/**CC", "*/",true);
        } 
        else {
            throw new JemmyException("File extension of: "+testFileObj.getNameExt()
                    +" is unsupported.");
        }
    }
    private void test(FileObject fileObj, String stepStart, String stepEnd) throws Exception {
         test(fileObj,stepStart,stepEnd,false);     
    }
    private void test(FileObject fileObj, String stepStart, String stepEnd,boolean isJS) throws Exception {
        boolean inStepData = false;
        String[] stepData = new String[3];
        int dataLineIdx = 0;
        
        try {
            // get token chain
            DataObject dataObj = DataObject.find(fileObj);
            final EditorCookie.Observable ed = (Observable) dataObj.getCookie(Observable.class);
            
            // prepare synchronization and register listener
            final Waiter waiter = new Waiter();
            final PropertyChangeListener pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(Observable.PROP_OPENED_PANES)) {
                        waiter.notifyFinished();
                    }
                }
            };
            ed.addPropertyChangeListener(pcl);
            // open document
            BaseDocument doc = (BaseDocument) ed.openDocument();
            ed.open();
            // wait for PROP_OPENED_PANES and remove listener
            assertTrue("The editor pane was not opened in 10 secs.", waiter.waitFinished(10000));
            ed.removePropertyChangeListener(pcl);
            // wait 2s for editor initialization
            Thread.sleep(3000);
            final List<JEditorPane> editorPane = new LinkedList<JEditorPane>();
            Runnable runnable = new Runnable(){
                public void run(){
                    editorPane.add(ed.getOpenedPanes()[0]);
                }
            };
            EventQueue.invokeAndWait(runnable);
            JEditorPane editor = editorPane.get(0);
            ExtSyntaxSupport ess = (ExtSyntaxSupport) doc.getSyntaxSupport();
            TokenItem token = ess.getTokenChain(0, doc.getLength());
            List<TestStep> steps = new java.util.ArrayList<TestStep>();
            // go through token chain an look for CC test steps
            while (token != null) {
                TokenID tokenID = token.getTokenID();
                if (debug) {
                    String tImage = token.getImage();
                    int tEnd = token.getOffset() + tImage.length();
                    System.err.println("# [" + token.getOffset() + "," + tEnd + "] "
                            + tokenID.getName() + " :: " + token.getImage());
                }
                String tag="comment";
                if(isJS)
                   tag="text"; 
                if (tokenID.getName().indexOf(tag) == -1) {
                    token = token.getNext();
                    continue;
                }
                if (inStepData){
                    // probably end of step data
                    if (token.getImage().indexOf(stepEnd) > -1) {
                        inStepData = false;
                        // check obtained CC data and create test step CCsecs
                        if (dataLineIdx == 3) {
                            int offset = token.getOffset() + token.getImage()
                                    .length();
                            TestStep step = new TestStep(stepData, offset);
                            steps.add(step);
                        } else {
                            ref("EE: expected data lines number: 3  but was: "
                                    + dataLineIdx);
                        }
                    } else {
                        // assert CC TEst Data lenght
                        if (dataLineIdx > 2) {
                            String msg = "EE: to much lines in CC Test Data";
                            ref(msg);
                            ref(dumpToken(token));
                            fail(msg);
                        }
                        String str = token.getImage();
                        // suppress new lines
                        if (str.endsWith("\n")) {
                            str = str.substring(0, str.length()-1);
                        }
                        stepData[dataLineIdx++] = str;
                    }
                } else {
                    String text = token.getImage();
                    if(text.startsWith(stepStart)) {
                        if (text.endsWith(stepEnd)) {
                            // all steps line in one toke as .java does
                            String[] lines = text.split("\n\r?|\r\n?");
                            if (lines.length == 5) {
                                int offset = token.getOffset() + token.getImage().length();
                                for (int i = 0; i < 3; i++) {
                                    stepData[i] = lines[i+1];
                                }
                                TestStep step = new TestStep(stepData, offset);
                                steps.add(step);
                            } else {
                                String msg = "EE: expected 5 lines lenght token but got: "
                                        + lines.length;
                                ref(msg);
                                ref(text);
                                for (int i = 0; i < lines.length; i++) {
                                    ref(i+"::"+lines[i]);
                                }
                            }
                        } else {
                            // each step line in separate line as .jsp does
                            inStepData = true;
                            dataLineIdx = 0;
                        }
                    }
                }
                token = token.getNext();
            } // while (token != null)
            run(editor, steps,isJS);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException(ex);
        }
        ending();
    }
    
    protected void run(JEditorPane editor, List steps, boolean isJS) throws Exception{
        Iterator it = steps.iterator();
        while (it.hasNext()) {
            exec(editor, (TestStep) it.next(),isJS);
        }
    }
    
    protected void exec(JEditorPane editor, TestStep step,boolean isJS) throws Exception {
        boolean ccError = false;
        try {
            ref(step.toString());
            BaseDocument doc = (BaseDocument) editor.getDocument();
            // insert prefix and set cursor
            doc.insertString(step.getOffset(), "\n" + step.getPrefix(), null);
            Caret caret = editor.getCaret();
            caret.setDot(step.getCursorPos());
            // run the test at a reasonable speed
            Thread.sleep(500);
            // call CC, the CC window should not appear in case of instant
            // substitution
            CompletionJListOperator comp = null;
            try {
                comp = CompletionJListOperator.showCompletion();
            } catch (JemmyException e) {
                ccError = true;
                log("EE: The CC window did not appear");
                e.printStackTrace(getLog());
            }
            try{
                if (comp!= null){
                    Object o = comp.getCompletionItems().get(0);
                    if (o.toString().contains("No suggestions")){ //try to catch the window
                        Thread.sleep(1000);
                        log(step.toString() + " " + o.toString() + "  trying again\n");
                        comp = new CompletionJListOperator();
                    }
                }else{//try to catch the window
                    Thread.sleep(1000);
                    log(step.toString() + "  trying again\n");
                    comp = new CompletionJListOperator();
                }
            } catch (JemmyException e) {
                ccError = true;
                log("EE: The CC window did not appear for the second time");
                e.printStackTrace(getLog());
            }
            // run the test at a reasonable speed
            Thread.sleep(1500);
            if (comp != null) {
                // dump CC result to golden file
                Iterator items = comp.getCompletionItems().iterator();
                CompletionItem selectedItem = null;
                while (items.hasNext()) {
                    TextGraphics2D g = new TextGraphics2D(comp.getSource());
                    Object next = items.next();
                    String dispText = null;
                    if (next instanceof ResultItem) {
                        ResultItem resItem = (ResultItem) next;
                        Component component = resItem.getPaintComponent((JList)comp.getSource(),
                                false, true);
                        // get display version of the component
                        Method drawM = findMethod(component.getClass(), "draw", new Class[] {Graphics.class});
                        if(drawM != null) {
                            drawM.setAccessible(true);
                            drawM.invoke(component, new Object[] {g});
                        } else if (component instanceof JLabel) {
                            // ??? use java.awt.Component.paint(Grraphics g) method instead?
                            g.drawString(((JLabel) component).getText().trim(), 0, 0);
                        } else {
                            g.drawString(component.toString(), 0, 0);
                        }
                    } else if (next instanceof CompletionItem) {
                        CompletionItem cItem = (CompletionItem) next;
                        if(isJS)
                          g.setText(cItem.getSortText());
                        else
                        
                          cItem.render(g, Font.decode("Dialog-Plain-12"), Color.BLACK, Color.WHITE, 400, 30, false);
                        
                        
                    } else {
                        g.drawString(next.toString(),0 ,0);
                    }
                    dispText = g.getTextUni();
                    // find choice item
                    if (dispText.equals(step.getChoice())) {
                        assertInstanceOf(CompletionItem.class, next);
                        selectedItem = (CompletionItem) next;
                    }
                    ref(g.getTextUni());
                }
                class DefaultActionRunner implements Runnable {
                    CompletionItem item;
                    JEditorPane editor;
                    public DefaultActionRunner(CompletionItem item,
                            JEditorPane editor) {
                        this.item=item;
                        this.editor=editor;
                    }
                    
                    public void run() {
                        item.defaultAction(editor);
                    }
                    
                }
                // substitute completion  and check result
                if (selectedItem != null) {
                    // move to separate class
                    Runnable run = new DefaultActionRunner(selectedItem, editor);
                    // XXX wait before substitution
                    Thread.currentThread().sleep(1000);
                    runInAWT(run);
                } else {
                    ref("EE: cannot find completion item: " + step.getChoice());
                }
            } else if (!ccError) {
                // comp == null && ccError == false => instant substitution
                ref("Instant substitution performed");
            }
            // wait till CompletionJList is hidden
            if (comp != null) {
                int i = 0;
                while (comp.isShowing()) {
                    Thread.currentThread().sleep(500);
                    if (i++ >= 12) {
                        // log status
                        long time = System.currentTimeMillis();
                        String screenFile = time + "-screen.png";
                        log("["+time+"]The CompletionJList was not hidden in 5 secs");
                        log("step: "+step);
                        log("captureScreen:" + screenFile);
                        try {
                            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()
                                    +File.separator+screenFile);
                        } catch (Exception e1) {}
                        break;
                    }
                }
            }
            Thread.currentThread().sleep(500);  //XXX
            int rowStart = Utilities.getRowStart(doc, step.getOffset() + 1);
            int rowEnd = Utilities.getRowEnd(doc, step.getOffset() + 1);
            String result = doc.getText(new int[] {rowStart, rowEnd});
            if  (!result.equals(step.getResult())) {
                ref("EE: unexpected CC result:\n< " + result + "\n> "
                        + step.getResult());
            }
            ref("End cursor position = " + caret.getDot());
        } finally {
            Thread.currentThread().sleep(500);  //XXX
            // undo all changes
            final UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
            assertNotNull("Cannot obtain UndoAction", ua);
            while (ua.isEnabled()) {
                runInAWT(new Runnable() {
                    public void run() {
                        ua.performAction();
                    }
                });
                Thread.currentThread().sleep(50);  //XXX
            }
            Thread.currentThread().sleep(500);
        }
        
    }
    
    protected static void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    protected Method findMethod(Class clazz, String name, Class[] paramTypes) {
        Method method = null;
        for (Class cls=clazz; cls.getSuperclass() != null; cls=cls.getSuperclass()) {
            try {
                method = cls.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                // ignore
            }
            if (method != null) {
                return method;
            }
        }
        return null;
    }
    
    protected void assertInstanceOf(Class expectedType, Object actual) {
        if (!expectedType.isAssignableFrom(actual.getClass())) {
            fail("Expected type: "+expectedType.getName()+"\nbut was: "+
                    actual.getClass().getName());
        }
    }
    
    protected static class TestStep {
        private String prefix;
        private String choice;
        private String result;
        private int offset;
        private int cursorPos;
        
        public TestStep(String data[], int offset) {
            this.prefix = data[0];
            this.choice = data[1];
            this.result = data[2];
            this.offset = offset;
            
            cursorPos = prefix.indexOf('|');
            if (cursorPos != -1) {
                prefix = prefix.replaceFirst("\\|", "");
            } else {
                cursorPos = prefix.length();
            }
            cursorPos += offset + 1;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer(prefix);
            sb.insert(cursorPos - offset - 1, '|');
            return "[" + sb + ", " + choice + ", "+ result + ", " + offset + "]";
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public String getChoice() {
            return choice;
        }
        
        public String getResult() {
            return result;
        }
        
        public int getOffset() {
            return offset;
        }
        
        public int getCursorPos() {
            return cursorPos;
        }
        
    };
    
    private String dumpToken(TokenItem tokenItem) {
        StringBuffer sb = new StringBuffer();
        sb.append("<token \name='");
        sb.append(tokenItem.getTokenID().getName());
        sb.append("'>\n");
        sb.append(tokenItem.getTokenContextPath());
        sb.append("</token>");
        return sb.toString();
    }
    
    protected void ending() throws Exception{
        if (!GenerateGoldenFiles) compareReferenceFiles();
        else {
            getRef().flush();
            File ref = new File(getWorkDir(),this.getName()+".ref");
            File f = getDataDir();
            ArrayList<String> names = new ArrayList<String>();
            names.add("goldenfiles");
            names.add("data");
            names.add("qa-functional");
            while (!f.getName().equals("test")) f = f.getParentFile();
            //            f= new File("/home/jindra/TRUNK/web/jspsyntax/test/"); //internal execution
            for (int i=names.size()-1;i > -1;i--) {
                f=new File(f,names.get(i));
            }
            f=new File(f, getClass().getName().replace('.', File.separatorChar));
            f=new File(f, this.getName()+".pass");
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            ref.renameTo(f);
            assertTrue("Generating golden files to " + f.getAbsolutePath(), false);
        }
        
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
/*        File datadir = new CompletionTest(null, null).getDataDir();
        File projectsDir = new File(datadir, "CompletionTestProjects");
 
        suite.addTest(new CompletionTest("testXMLWellFormed", ));
        suite.addTest(new CompletionTest("testXMLDTDFormed"));
        suite.addTest(new CompletionTest("testXMLXSDFormed"));
        suite.addTest(new CompletionTest("testGenerateDTD"));
        suite.addTest(new CompletionTest("testXSLT"));
 */        junit.textui.TestRunner.run(suite());
    }
    
}
