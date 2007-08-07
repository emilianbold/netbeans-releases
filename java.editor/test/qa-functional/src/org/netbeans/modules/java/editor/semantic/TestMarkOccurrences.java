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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.semantic;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.ext.ExtCaret;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;


/**
 *
 * @author Jiri Prox
 */
public class TestMarkOccurrences extends NbTestCase {
    
    private FileObject fileObject;
    
    private MarkOccurrencesHighlighterFactory factory;
    
    private JEditorPane editorPane;
    
    private DataObject dataObject;
    
    private JavaSource js;
    
    private static final SimpleMark[] EMPTY = new TestMarkOccurrences.SimpleMark[]{};
    
    private final SimpleMark[] TEST_TYPE = new SimpleMark[] {
        new SimpleMark(270,274,null),
        new SimpleMark(195,199,null),
        new SimpleMark(160,164,null),
        new SimpleMark(64,68,null)
    };
    
    private final SimpleMark[] TEST_METHOD = new SimpleMark[] {
        new SimpleMark(259,265,null),
        new SimpleMark(336,342,null),
        new SimpleMark(381,387,null),
        new SimpleMark(183,189,null),
        new SimpleMark(150,156,null)
    };
    
    private final SimpleMark[] TEST_FIELD = new SimpleMark[] {
        new SimpleMark(141,144,null),
        new SimpleMark(102,105,null),
        new SimpleMark(186,189,null),
        new SimpleMark(59,62,null),
        new SimpleMark(224,227,null)
    };
    
    private final SimpleMark[] TEST_CONST = new SimpleMark[] {
        new SimpleMark(141,146,null),
        new SimpleMark(243,248,null),
        new SimpleMark(182,187,null),
        new SimpleMark(77,82,null),
        new SimpleMark(273,278,null)
    };
    
    
    private final SimpleMark[] TEST_LOCAL1 = new SimpleMark[] {
        new SimpleMark(180,186,null),
        new SimpleMark(105,111,null),
        new SimpleMark(211,217,null)
    };
    
    private final SimpleMark[] TEST_LOCAL2 = new SimpleMark[] {
        new SimpleMark(140,147,null),
        new SimpleMark(243,250,null),
        new SimpleMark(287,294,null),
        new SimpleMark(233,240,null),
        new SimpleMark(203,210,null)
    };
    
    private final SimpleMark[] TEST_THROWING1 = new SimpleMark[] {
        new SimpleMark(352,373,null)
    };
    
    private final SimpleMark[] TEST_THROWING2 = new SimpleMark[] {
        new SimpleMark(615,625,null)
    };
    
    private final SimpleMark[] TEST_EXIT = new SimpleMark[] {
        new SimpleMark(635,644,null),
        new SimpleMark(352,373,null),
        new SimpleMark(615,625,null),
        new SimpleMark(460,472,null)
    };
    
    private final SimpleMark[] TEST_IMPLEMENT1 = new SimpleMark[] {
        new SimpleMark(123,126,null)
    };
    
    private final SimpleMark[] TEST_IMPLEMENT2 = new SimpleMark[] {
        new SimpleMark(224,233,null)
    };
    
    private final SimpleMark[] TEST_KEEP = new SimpleMark[] {
        new SimpleMark(40,45,null)
                
    };
    
    private final SimpleMark[] TEST_LABELS1 = new SimpleMark[] {
        new SimpleMark(414,415,null),
        new SimpleMark(93,98,null)
    };
    
    private final SimpleMark[] TEST_LABELS2 = new SimpleMark[] {
        new SimpleMark(184,189,null),
        new SimpleMark(383,384,null)
    };
    
    private final SimpleMark[] TEST_OVERRIDE = new SimpleMark[] {
        new SimpleMark(271,284,null),
        new SimpleMark(178,191,null)
    };
    
    public Document getDocument() {
        try {
            DataObject d = DataObject.find(fileObject);
            EditorCookie ec = d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (DataObjectNotFoundException donfe) {
            fail();
        }
        return null;
    }
    
    public TestMarkOccurrences(String name) {
        super(name);
    }
    
    private class SimpleMark implements Comparable {
        int start;
        int end;
        Color color;
        
        public SimpleMark(int start, int end, Color color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }
        
        public int compareTo(Object o) {
            return new Integer(start).compareTo(((SimpleMark)o).start);
        }
        
    }
    
    private void closeFile() {
        EditorCookie ec = null;
        if(dataObject!=null) ec = dataObject.getCookie(EditorCookie.class);
        if(ec != null) ec.close();
    }
    
    static boolean firstStart = true;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(firstStart) {
            js = openFile("Test.java");
            sleep(2000);
            try {
                ExtCaret c = (ExtCaret) editorPane.getCaret();
                c.setDot(66);
                sleep(2000);
                c.setDot(272);
                sleep(2000);                
            } catch(Exception e) {
                //ignoring
            } finally {
                closeFile();
            }            
            firstStart = false;
        }
    }
    
    
    
    @Override
    protected void tearDown() throws Exception {
        closeFile();
        super.tearDown();
    }
    
    private OffsetsBag foundMarks;
    
    class MyTask implements Task<CompilationController> {
        
        
        public void run(CompilationController parameter) throws Exception {
            foundMarks = null;
            Document doc = getDocument();
            CancellableTask<CompilationInfo> task  = factory.createTask(fileObject);//new MarkOccurrencesHighlighter(fileObject);
            MarkOccurrencesHighlighter moh;
            if(task instanceof MarkOccurrencesHighlighter) moh = (MarkOccurrencesHighlighter) factory.createTask(fileObject);//new MarkOccurrencesHighlighter(fileObject);
            else return;
            int caretPosition = MarkOccurrencesHighlighterFactory.getLastPosition(fileObject);
            Preferences node = MarkOccurencesSettings.getCurrentNode();
            OffsetsBag highlights = moh.processImpl(parameter, node, doc, caretPosition);
            foundMarks  = highlights;
        }
    }
    
    private void browse(String s) {
        int c = 0;
        for(int i=0;i<s.length();i++) {
            char r = s.charAt(i);
            if(r=='\n') r='&';
            if(c==0) System.out.print(i+" "+r);
            else System.out.print(r);
            if(c==9) System.out.println();
            c = (c+1) % 10;
        }
    }
    
    private JavaSource openFile(String name) throws DataObjectNotFoundException, IOException, InterruptedException, InvocationTargetException {
        String dataDir = System.getProperty("xtest.data");
        File sample = new File(dataDir+"/projects/java_editor_test/src/markOccurrences",name);
        assertTrue("file "+sample.getAbsolutePath()+" does not exist",sample.exists());
        
        fileObject = FileUtil.toFileObject(sample);
        dataObject = DataObject.find(fileObject);
        JavaSource js = JavaSource.forFileObject(fileObject);
        factory = new MarkOccurrencesHighlighterFactory();
        sleep(500);
        final EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        ec.openDocument();
        ec.open();
        
        sleep(500);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JEditorPane[] panes = ec.getOpenedPanes();
                editorPane = panes[0];
                
            }
        });
        return js;
        
    }
    
    private void setAndCheck(int pos,SimpleMark[] marks) throws IOException {
        ExtCaret c = (ExtCaret) editorPane.getCaret();
        c.setDot(pos);
        sleep(2500);
        js.runUserActionTask(new MyTask() ,false);
        sleep(500);
        Arrays.sort(marks);
        //assertEquals("Wrong number of highlight marks",marks.length,highlights.length);
        String etalon = "";
        for (int i = 0; i < marks.length; i++) {
            SimpleMark m = marks[i];
            etalon = etalon + "["+m.start+","+m.end+"] ";
        }
        String ref = "";
        //not locking, should be fine in tests:
        HighlightsSequence hs = foundMarks.getHighlights(0, editorPane.getDocument().getLength());
        while (hs.moveNext()) {
            ref = ref + "["+hs.getStartOffset()+","+hs.getEndOffset()+"] ";
            
        }
        assertEquals(etalon, ref);
        
        
        
    }
    
    public void testType() throws Exception {
        SimpleMark[] marks = TEST_TYPE;
        js = openFile("Test.java");       
        setAndCheck(66, marks);       
        setAndCheck(272, marks);
        
    }
    
    public void testMethod() throws Exception {
        SimpleMark[] marks = TEST_METHOD;
        js = openFile("Test2.java");        
        setAndCheck(153, marks);
        setAndCheck(185, marks);
        setAndCheck(260, marks);
        setAndCheck(340, marks);
        setAndCheck(385, marks);
    }
    
    public void testField() throws Exception {
        SimpleMark[] marks = TEST_FIELD;
        js = openFile("Test3.java");
        setAndCheck(61, marks);
        setAndCheck(104, marks);
        setAndCheck(142, marks);
        setAndCheck(188, marks);
        setAndCheck(225, marks);
    }
    
    public void testConstant() throws Exception {
        SimpleMark[] marks = TEST_CONST;
        js = openFile("Test4.java");
        setAndCheck(78, marks);
        setAndCheck(143, marks);
        setAndCheck(184, marks);
        setAndCheck(246, marks);
        setAndCheck(276, marks);
    }
    
    public void testLocal() throws Exception {
        SimpleMark[] marks = TEST_LOCAL1;
        js = openFile("Test5.java");
        
        setAndCheck(109, marks);
        setAndCheck(182, marks);
        setAndCheck(217, marks);
        marks = TEST_LOCAL2;
        setAndCheck(141, marks);
        setAndCheck(208, marks);
        setAndCheck(238, marks);
        setAndCheck(248, marks);
        setAndCheck(290, marks);
    }
    
    public void testThrowingPoints() throws Exception {
        SimpleMark[] marks = TEST_THROWING1;
        js = openFile("Test6.java");
        setAndCheck(295, marks);
        
        marks = TEST_THROWING2;
        setAndCheck(315, marks);
    }
    
    public void testExitPoints() throws Exception {
        SimpleMark[] marks = TEST_EXIT;
        js = openFile("Test6.java");
        setAndCheck(250, marks);
    }
    
    public void testImplementing() throws Exception {
        SimpleMark[] marks = TEST_IMPLEMENT1;
        js = openFile("Test8.java");
        setAndCheck(60, marks);
        
        marks = TEST_IMPLEMENT2;
        setAndCheck(70, marks);
    }
    
    public void testOverriding() throws Exception {
        SimpleMark[] marks = TEST_OVERRIDE;
        js = openFile("Test9.java");
        setAndCheck(130, marks);
    }
    
    public void testLabels() throws Exception {
        SimpleMark[] marks = TEST_LABELS1;
        js = openFile("Testa.java");
        setAndCheck(162, marks);
        setAndCheck(284, marks);
        
        marks = TEST_LABELS2;
        setAndCheck(333, marks);
        
    }
    
    public void testOptions() throws Exception {
        Preferences setting = MarkOccurencesSettings.getCurrentNode();
        setting.putBoolean(MarkOccurencesSettings.ON_OFF, false);
        js = openFile("Test.java");
        setAndCheck(80, EMPTY);
        setAndCheck(205, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.ON_OFF, true);
        setAndCheck(168, new SimpleMark[]{new SimpleMark(166,170,null)});
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.BREAK_CONTINUE, false);
        js = openFile("Testa.java");
        setAndCheck(162, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.BREAK_CONTINUE, true);
        setAndCheck(162, TEST_LABELS1);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.CONSTANTS, false);
        js = openFile("Test4.java");
        setAndCheck(78, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.CONSTANTS, true);
        setAndCheck(78, TEST_CONST);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.EXCEPTIONS, false);
        js = openFile("Test6.java");
        setAndCheck(295, new SimpleMark[]{new SimpleMark(73,94,null),new SimpleMark(281,302,null)});
        setting.putBoolean(MarkOccurencesSettings.EXCEPTIONS, true);
        setAndCheck(295, TEST_THROWING1);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.EXIT, false);
        js = openFile("Test6.java");
        setAndCheck(250,  new SimpleMark[]{new SimpleMark(246,252,null),
                                           new SimpleMark(261,267,null),
                                           new SimpleMark(481,487,null)});                        
        setting.putBoolean(MarkOccurencesSettings.EXIT, true);
        setAndCheck(250, TEST_EXIT);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.FIELDS, false);
        js = openFile("Test3.java");
        setAndCheck(61, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.FIELDS, true);
        setAndCheck(61, TEST_FIELD);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.IMPLEMENTS, false);
        js = openFile("Test8.java");
        setAndCheck(60, new SimpleMark[]{new SimpleMark(57,65,null)});
        
        setting.putBoolean(MarkOccurencesSettings.IMPLEMENTS, true);
        setAndCheck(60, TEST_IMPLEMENT1);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.LOCAL_VARIABLES, false);
        js = openFile("Test5.java");
        setAndCheck(109, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.LOCAL_VARIABLES, true);
        setAndCheck(109, TEST_LOCAL1);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.METHODS, false);
        js = openFile("Test2.java");
        setAndCheck(153, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.METHODS, true);
        setAndCheck(153, TEST_METHOD);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.OVERRIDES, false);
        js = openFile("Test9.java");
        setAndCheck(130,new SimpleMark[]{new SimpleMark(77,94,null),
                                           new SimpleMark(124,141,null)
                                        });                 
        setting.putBoolean(MarkOccurencesSettings.OVERRIDES, true);
        setAndCheck(130, TEST_OVERRIDE);
        closeFile();
        
        setting.putBoolean(MarkOccurencesSettings.TYPES, false);
        js = openFile("Test.java");
        setAndCheck(66, EMPTY);
        setting.putBoolean(MarkOccurencesSettings.TYPES, true);
        setAndCheck(66, TEST_TYPE);
        
    }
    
    
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch(InterruptedException ie) {
            // ignored
        }
    }
    
    public static void main(String[] args) {
        new TestRunner().run(TestMarkOccurrences.class);
    }
    
}
