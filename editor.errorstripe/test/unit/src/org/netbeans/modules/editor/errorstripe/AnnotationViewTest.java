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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import junit.framework.*;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.AnnotationTypes.Loader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.plain.PlainKit;

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
        AnnotationTestUtilities.register();
        BaseOptions.findObject(BaseOptions.class, true);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AnnotationViewTest.class);
        
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
                AnnotationDesc annotation = null;
                boolean wasAnnotation = false;
                
                for (double pos = AnnotationView.HEIGHT_OFFSET; pos < (aView.getHeight() - AnnotationView.HEIGHT_LOWER_OFFSET); pos = pos + 1) {
                    AnnotationDesc newAnnotation = aView.getAnnotationForPoint(pos);
                    
                    if (newAnnotation != null && annotation != null) {
                        assertTrue(newAnnotation == annotation);
                    }
                    
                    if (wasAnnotation) {
                        assertNull("pos=" + pos + ", annotation=" + annotation + ", newAnnotation=" + newAnnotation, newAnnotation);
                    }
                    
                    if (annotation != null && newAnnotation == null) {
                        wasAnnotation = true;
                    }
                    
                    annotation = newAnnotation;
                }
            }
        });
    }
    
    private static String[] getContents() {
        StringBuffer largeBuffer = new StringBuffer(16384);
        
        for (int cntr = 0; cntr < 16300; cntr++) {
            largeBuffer.append('\n');
        }
        
        String large = largeBuffer.toString();
        
        String small = "\n\n\n\n";
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
        
        AnnotationView aView = new AnnotationView(editor);
        
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);
        f.getContentPane().add(aView, BorderLayout.EAST);
        
        f.setSize(500, 500);
        
        f.setVisible(true);

        String[] contents = getContents();
        
        for (int index = 0; index < contents.length; index++) {
            BaseDocument bd = (BaseDocument) editor.getDocument();
            
            bd.insertString(0, contents[index], null);
            
            Position start = bd.createPosition(Utilities.getRowStartFromLineOffset(bd, 2));
            
            AnnotationDesc a1 = new AnnotationTestUtilities.TestAnnotationDesc1(bd, start);
            AnnotationDesc a2 = new AnnotationTestUtilities.TestAnnotationDesc2(bd, start);
            
            bd.getAnnotations().addAnnotation(a1);
            bd.getAnnotations().addAnnotation(a2);
            
            action.test(aView, bd);
            
            bd.getAnnotations().removeAnnotation(a1);
            bd.getAnnotations().removeAnnotation(a2);
            bd.remove(0, bd.getLength());
        }
        
        f.setVisible(false);
    }
    
    private static abstract class Action {
        public abstract void test(AnnotationView aView, BaseDocument document) throws Exception;
    }

    public void testGetStatusesForBlock() throws /*BadLocation*/Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        testGetStatusesForBlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return ;
        }
        
        BaseDocument bd = new BaseDocument(PlainKit.class, false);
        
        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
        
        Position start = bd.createPosition(Utilities.getRowStartFromLineOffset(bd, 2));
        
        AnnotationDesc a1 = new AnnotationTestUtilities.TestAnnotationDesc1(bd, start);
        AnnotationDesc a2 = new AnnotationTestUtilities.TestAnnotationDesc2(bd, start);
        
        bd.getAnnotations().addAnnotation(a1);
        bd.getAnnotations().addAnnotation(a2);
        
        List expected = new ArrayList();
        
        expected.add(new AnnotationView.AnnotationStatusPair(new Status(Status.STATUS_ERROR, null, true), a1));
        expected.add(new AnnotationView.AnnotationStatusPair(new Status(Status.STATUS_WARNING, null, true), a2));
        
        assertEquals(expected, AnnotationView.getStatusesForBlockImpl(bd, 2, 2));
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(expected, AnnotationView.getStatusesForBlockImpl(bd, 2, 2));
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(expected, AnnotationView.getStatusesForBlockImpl(bd, 2, 2));
    }
    
    public void testComputeTotalStatus() throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        testComputeTotalStatus();
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
        
        AnnotationView aView = new AnnotationView(editor);
        
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);
        f.getContentPane().add(aView, BorderLayout.EAST);
        
        f.setSize(500, 500);
        
        f.setVisible(true);

        BaseDocument bd = (BaseDocument) editor.getDocument();
        
        bd.insertString(0, "\n\n\n\n\n\n\n\n\n\n", null);
        
        Position start = bd.createPosition(Utilities.getRowStartFromLineOffset(bd, 2));
        
        AnnotationDesc a1 = new AnnotationTestUtilities.TestAnnotationDesc1(bd, start);
        AnnotationDesc a2 = new AnnotationTestUtilities.TestAnnotationDesc2(bd, start);
        
        bd.getAnnotations().addAnnotation(a1);
        bd.getAnnotations().addAnnotation(a2);
        
        assertEquals(Status.STATUS_ERROR, aView.computeTotalStatus().getStatus());
        
        bd.getAnnotations().activateNextAnnotation(2);
        
        assertEquals(Status.STATUS_ERROR, aView.computeTotalStatus().getStatus());
        
        f.setVisible(false);
    }
    
    public void testAnnotationViewFactory() {
        JEditorPane editor = new JEditorPane();
        
        editor.setEditorKit(BaseKit.getKit(PlainKit.class));
        
        assertNotNull(new AnnotationViewFactory().createSideBar(editor));
    }
    
}
