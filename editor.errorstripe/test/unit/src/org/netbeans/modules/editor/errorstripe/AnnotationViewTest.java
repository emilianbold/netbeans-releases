/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.errorstripe.spi.Mark;
import org.netbeans.modules.editor.errorstripe.spi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.spi.MarkProviderCreator;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.editor.errorstripe.spi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationViewTest extends TestCase {
    
    public AnnotationViewTest(String testName) {
        super(testName);
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp() throws Exception {
        UnitUtilities.prepareTest(new String[] {"/org/netbeans/modules/editor/plain/resources/layer.xml"}, new Object[0]);
        BaseKit.getKit(PlainKit.class);
//        AnnotationTestUtilities.register();
        BaseOptions.findObject(BaseOptions.class, true);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AnnotationViewTest.class);
        
//        suite.addTest(TestSuite.createTest(AnnotationViewTest.class, "testGetMainMarkForBlock"));
        
        return suite;
    }
    
    public void testModelToView() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                double         pos   = aView.modelToView(2);
                
                assertEquals(aView.viewToModel(pos)[0], aView.viewToModel(aView.modelToView(aView.viewToModel(pos)[0]))[0]);
                
                assertEquals(aView.modelToView(Utilities.getLineOffset(document, document.getLength()) + 1), (-1.0), 0.0001d);
            }
        });
    }
    
    public void testViewToModelIsContinuous() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                int[] last = new int[] {-1, -1};
                
                for (double pos = AnnotationView.HEIGHT_OFFSET; pos < (aView.getHeight() - AnnotationView.HEIGHT_LOWER_OFFSET); pos = pos + 1) {
                    int[] current = aView.viewToModel(pos);
                    
                    if (current == null)
                        continue;
                    assertTrue(last[0] <= current[0]);
                    assertTrue(last[1] <= current[1]);
                    
                    last = current;
                }
            }
        });
    }
    
    public void testGetAnnotationIsContinuous() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                Mark mark = null;
                boolean wasMark = false;
                
                for (double pos = AnnotationView.HEIGHT_OFFSET; pos < (aView.getHeight() - AnnotationView.HEIGHT_LOWER_OFFSET); pos = pos + 1) {
                    Mark newMark = aView.getMarkForPoint(pos);
                    
                    if (newMark != null && mark!= null) {
                        assertTrue(newMark == mark);
                    }
                    
                    if (wasMark) {
                        assertNull("pos=" + pos + ", mark=" + mark + ", newMark=" + newMark, newMark);
                    }
                    
                    if (mark != null && newMark == null) {
                        wasMark = true;
                    }
                    
                    mark = newMark;
                }
            }
        });
    }
    
    public void testGetLinesSpanIsContinuous() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                int startLine = 1;
                int linesCount = Utilities.getRowCount(document);
                
                while (startLine < linesCount) {
                    int[] span = aView.getLinesSpan(startLine);
                    
                    assertTrue(startLine >= span[0]);
                    assertTrue(startLine <= span[1]);
                    
                    if (span[1] < linesCount) {
                        int[] newSpan = aView.getLinesSpan(span[1] + 1);
                        
                        assertEquals(newSpan[0], span[1] + 1);
                    }
                    
                    startLine = span[1] + 1;
                }
            }
        });
    }
    
    public void testMarkSensitiveStripe1() throws Exception {
        performTest(new Action() {
            public void test(AnnotationView aView, BaseDocument document) throws Exception {
                double position = aView.modelToView(6);
                double start    = position - AnnotationView.UPPER_HANDLE;
                double end      = position + AnnotationView.PIXELS_FOR_LINE + AnnotationView.LOWER_HANDLE - 1;
                
                for (double pos = start; pos <= end; pos++) {
                    Mark m = aView.getMarkForPoint(pos);
                    
                    assertNotNull("pos=" + pos + ", start=" + start + ", end=" + end + ", position=" + position, m);
                }
                
                Mark m1 = aView.getMarkForPoint(start - 1);
                
                assertNull("There is a mark at position: " + (start - 1), m1);
                
                Mark m2 = aView.getMarkForPoint(end   + 1);
                
                assertNull("There is a mark at position: " + (end + 1), m2);
            }
        });
    }
    
    private static String[] getContents() {
        StringBuffer largeBuffer = new StringBuffer(16384);
        
        for (int cntr = 0; cntr < 16300; cntr++) {
            largeBuffer.append('\n');
        }
        
        String large = largeBuffer.toString();
        
        String small = "\n\n\n\n\n\n\n\n";
        String medium = large.substring(0, 300);
        
        return new String[] {small, medium, large};
    }
    
    private static void performTest(final Action action) throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        performTest(action);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return ;
        }
        
        JFrame f = new JFrame();
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        TestMark mark1 = new TestMark(new Status(Status.STATUS_ERROR), null, null, new int[] {6, 6});
        TestMark mark2 = new TestMark(new Status(Status.STATUS_OK), null, null, new int[] {6, 6});
        
        List marks = Arrays.asList(new Mark[]{mark1, mark2});
        
        TestMarkProvider provider = new TestMarkProvider(Collections.EMPTY_LIST, MarkProvider.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = new TestMarkProviderCreator(provider);
        
        AnnotationView aView = new AnnotationView(editor, Arrays.asList(new MarkProviderCreator[] {creator}));
        
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);
        f.getContentPane().add(aView, BorderLayout.EAST);
        
        f.setSize(500, 500);
        
        f.setVisible(true);

        String[] contents = getContents();
        
        for (int index = 0; index < contents.length; index++) {
            BaseDocument bd = (BaseDocument) editor.getDocument();
            
            bd.insertString(0, contents[index], null);
            
            provider.setMarks(marks);
            
            action.test(aView, bd);
            
            provider.setMarks(Collections.EMPTY_LIST);
            
            bd.remove(0, bd.getLength());
        }
        
        f.setVisible(false);
    }
    
    private static abstract class Action {
        public abstract void test(AnnotationView aView, BaseDocument document) throws Exception;
    }

    public void testGetMainMarkForBlock() throws /*BadLocation*/Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        testGetMainMarkForBlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return ;
        }
        
        JEditorPane editor = new JEditorPane();
        BaseDocument bd = new BaseDocument(PlainKit.class, false);
        
        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
        
        TestMark mark1 = new TestMark(new Status(Status.STATUS_ERROR), null, null, new int[] {2, 2});
        TestMark mark2 = new TestMark(new Status(Status.STATUS_OK), null, null, new int[] {2, 2});
        TestMark mark3 = new TestMark(new Status(Status.STATUS_WARNING), null, null, new int[] {2, 4});
        
        List marks1 = Arrays.asList(new Mark[]{mark1, mark2, mark3});
        List marks2 = Arrays.asList(new Mark[]{mark1, mark3});
        List marks3 = Arrays.asList(new Mark[]{mark2, mark3});
        List marks4 = Arrays.asList(new Mark[]{mark1, mark2});
        List marks5 = Arrays.asList(new Mark[]{mark3});
        
        TestMarkProvider provider = new TestMarkProvider(marks1, MarkProvider.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = new TestMarkProviderCreator(provider);
        
        AnnotationView aView = new AnnotationView(editor, Arrays.asList(new MarkProviderCreator[] {creator}));
        
        assertEquals(mark1, aView.getMainMarkForBlock(2, 2));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 3));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 4));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 3));
        assertEquals(null, aView.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks2);
        
        assertEquals(mark1, aView.getMainMarkForBlock(2, 2));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 3));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 4));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 3));
        assertEquals(null, aView.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks3);
        
        assertEquals(mark3, aView.getMainMarkForBlock(2, 2));
        assertEquals(mark3, aView.getMainMarkForBlock(2, 3));
        assertEquals(mark3, aView.getMainMarkForBlock(2, 4));
        assertEquals(mark3, aView.getMainMarkForBlock(2, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 3));
        assertEquals(null, aView.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks4);
        
        assertEquals(mark1, aView.getMainMarkForBlock(2, 2));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 3));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 4));
        assertEquals(mark1, aView.getMainMarkForBlock(2, 6));
        assertEquals(null, aView.getMainMarkForBlock(3, 6));
        assertEquals(null, aView.getMainMarkForBlock(3, 3));
        assertEquals(null, aView.getMainMarkForBlock(6, 6));
        
        provider.setMarks(marks5);
        
        assertEquals(mark3, aView.getMainMarkForBlock(2, 2));
        assertEquals(mark3, aView.getMainMarkForBlock(2, 3));
        assertEquals(mark3, aView.getMainMarkForBlock(2, 4));
        assertEquals(mark3, aView.getMainMarkForBlock(2, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 6));
        assertEquals(mark3, aView.getMainMarkForBlock(3, 3));
        assertEquals(null, aView.getMainMarkForBlock(6, 6));
    }
//    
//    public void testComputeTotalStatus() throws Exception {
//        if (!SwingUtilities.isEventDispatchThread()) {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                public void run() {
//                    try {
//                        testComputeTotalStatus();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            return ;
//        }
//        
//        JFrame f = new JFrame();
//        JEditorPane editor = new JEditorPane();
//        
//        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
//        
//        AnnotationView aView = new AnnotationView(editor);
//        
//        f.getContentPane().setLayout(new BorderLayout());
//        f.getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);
//        f.getContentPane().add(aView, BorderLayout.EAST);
//        
//        f.setSize(500, 500);
//        
//        f.setVisible(true);
//
//        BaseDocument bd = (BaseDocument) editor.getDocument();
//        
//        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
//        
//        Position start = bd.createPosition(Utilities.getRowStartFromLineOffset(bd, 2));
//        
//        AnnotationDesc a1 = new AnnotationTestUtilities.TestAnnotationDesc1(bd, start);
//        AnnotationDesc a2 = new AnnotationTestUtilities.TestAnnotationDesc2(bd, start);
//        
//        bd.getAnnotations().addAnnotation(a1);
//        bd.getAnnotations().addAnnotation(a2);
//        
//        assertEquals(Status.STATUS_ERROR, aView.computeTotalStatus().getStatus());
//        
//        bd.getAnnotations().activateNextAnnotation(2);
//        
//        assertEquals(Status.STATUS_ERROR, aView.computeTotalStatus().getStatus());
//        
//        f.setVisible(false);
//    }
    
    public void testAnnotationViewFactory() {
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        assertNotNull(new AnnotationViewFactory().createSideBar(editor));
    }
    
    public void testMarkUpdates() {
        JEditorPane editor = new JEditorPane();
        
        TestMark mark1 = new TestMark(new Status(Status.STATUS_ERROR), null, null, new int[] {2, 2});
        TestMark mark2 = new TestMark(new Status(Status.STATUS_OK), null, null, new int[] {2, 2});
        TestMark mark3 = new TestMark(new Status(Status.STATUS_OK), null, null, new int[] {4, 6});
        
        List marks = Arrays.asList(new Mark[]{mark1, mark2});
        List marksOnlyFirst = Arrays.asList(new Mark[]{mark1});
        List marksOnlySecond = Arrays.asList(new Mark[]{mark2});
        List marksFirstAndThird = Arrays.asList(new Mark[]{mark1, mark3});
        
        TestMarkProvider provider = new TestMarkProvider(marks, MarkProvider.UP_TO_DATE_OK);
        TestMarkProviderCreator creator = new TestMarkProviderCreator(provider);
        
        AnnotationView aView = new AnnotationView(editor, Arrays.asList(new MarkProviderCreator[] {creator}));
        
        List mergedMarks;
        SortedMap map;
        
        mergedMarks = aView.getMergedMarks();
        
        assertEquals(marks, mergedMarks);
        
        map = aView.getMarkMap();
        
        assertEquals(1, map.size());
        assertEquals(marks, map.get(map.firstKey()));
        
        provider.setMarks(marksOnlyFirst);
        
        mergedMarks = aView.getMergedMarks();
        
        assertEquals(marksOnlyFirst, mergedMarks);
        
        map = aView.getMarkMap();
        
        assertEquals(1, map.size());
        assertEquals(marksOnlyFirst, map.get(map.firstKey()));
        
        provider.setMarks(marksFirstAndThird);
        
        mergedMarks = aView.getMergedMarks();
        
        assertEquals(marksFirstAndThird, mergedMarks);
        
        map = aView.getMarkMap();
        
        assertEquals(4, map.size());
        assertEquals(new HashSet(Arrays.asList(new Integer[] {new Integer(2), new Integer(4), new Integer(5), new Integer(6)})), map.keySet());
        assertEquals(Arrays.asList(new Mark[] {mark1}), map.get(new Integer(2)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(4)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(5)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(6)));
        
        provider.setMarks(Collections.EMPTY_LIST);
        
        mergedMarks = aView.getMergedMarks();
        
        assertEquals(Collections.EMPTY_LIST, mergedMarks);
        
        map = aView.getMarkMap();
        
        assertEquals(0, map.size());
        
        provider.setMarks(marksFirstAndThird);
        
        mergedMarks = aView.getMergedMarks();
        
        assertEquals(marksFirstAndThird, mergedMarks);
        
        map = aView.getMarkMap();
        
        assertEquals(4, map.size());
        assertEquals(new HashSet(Arrays.asList(new Integer[] {new Integer(2), new Integer(4), new Integer(5), new Integer(6)})), map.keySet());
        assertEquals(Arrays.asList(new Mark[] {mark1}), map.get(new Integer(2)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(4)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(5)));
        assertEquals(Arrays.asList(new Mark[] {mark3}), map.get(new Integer(6)));
    }
}
