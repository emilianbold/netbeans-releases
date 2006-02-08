/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JEditorPaneOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JEditorPaneOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the editor pane we use for testing.
     */
    private JEditorPane editorPane;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JEditorPaneOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame("JFrameOperatorTest");
        editorPane = new JEditorPane();
        editorPane.setText("JEditorPaneOperatorTest");
        editorPane.setName("JEditorPaneOperatorTest");
        frame.getContentPane().add(editorPane);
        frame.setName("JFrameOperatorTest");
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }
    
    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JEditorPaneOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator3 = new JEditorPaneOperator(operator1, new NameComponentChooser("JEditorPaneOperatorTest"));
        assertNotNull(operator3);
        
        JEditorPaneOperator operator4 = new JEditorPaneOperator(operator1);
        assertNotNull(operator4);
        
        JEditorPaneOperator operator5 = new JEditorPaneOperator(operator1, "JEditorPaneOperatorTest");
        assertNotNull(operator5);
    }
    
    /**
     * Test findJEditorPane method.
     */
    public void testFindJEditorPane() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPane editorPane1 = JEditorPaneOperator.findJEditorPane(frame, new NameComponentChooser("JEditorPaneOperatorTest"));
        assertNotNull(editorPane1);
        
        JEditorPane editorPane2 = JEditorPaneOperator.findJEditorPane(frame, "JEditorPaneOperatorTest", false, false);
        assertNotNull(editorPane2);
    }
    
    /**
     * Test waitJEditorPane method.
     */
    public void testWaitJEditorPane() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPane editorPane1 = JEditorPaneOperator.waitJEditorPane(frame, new NameComponentChooser("JEditorPaneOperatorTest"));
        assertNotNull(editorPane1);
        
        JEditorPane editorPane2 = JEditorPaneOperator.waitJEditorPane(frame, "JEditorPaneOperatorTest", false, false);
        assertNotNull(editorPane2);
    }
    
    /**
     * Test usePageNavigationKeys method.
     *
     * @todo since this methood is deprecated should we still try to test it,
     *       or will it disappear in a future version of the API?
     */
    public void testUsePageNavigationKeys() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator2 = new JEditorPaneOperator(operator1);
        assertNotNull(operator2);
        
        operator2.usePageNavigationKeys(true);
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator2 = new JEditorPaneOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setContentType("text/plain");
        Hashtable hashtable = operator2.getDump();
        assertEquals("text/plain", (String) hashtable.get(JEditorPaneOperator.CONTENT_TYPE_DPROP));
    }
    
    /**
     * Test addHyperlinkListener method.
     */
    public void testAddHyperlinkListener() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator2 = new JEditorPaneOperator(operator1);
        assertNotNull(operator2);
        
        HyperlinkListenerTest listener = new HyperlinkListenerTest();
        operator2.addHyperlinkListener(listener);
        assertEquals(1, editorPane.getHyperlinkListeners().length);
        
        operator2.fireHyperlinkUpdate(new HyperlinkEvent("", null, null));
        assertNotNull(listener.event);
        
        operator2.removeHyperlinkListener(listener);
        assertEquals(0, editorPane.getHyperlinkListeners().length);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class HyperlinkListenerTest implements HyperlinkListener {
        /**
         * Stores the string for testing.
         */
        public HyperlinkEvent event;
        
        /**
         * Handle hyperlink event.
         */
        public void hyperlinkUpdate(HyperlinkEvent e) {
            event = e;
        }
    }
    
    /**
     * Test getContentType method.
     */
    public void testGetContentType() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator4 = new JEditorPaneOperator(operator1);
        assertNotNull(operator4);
        
        assertEquals("text/plain", operator4.getContentType());
        
        operator4.setContentType("text/html");
        assertEquals("text/html", operator4.getContentType());
        assertEquals(operator4.getContentType(), editorPane.getContentType());
    }
    
    /**
     * Test getEditorKit method.
     */
    public void testGetEditorKit() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator2 = new JEditorPaneOperator(operator1);
        assertNotNull(operator2);
        
        EditorKitTest editorKit = new EditorKitTest();
        operator2.setEditorKit(editorKit);
        assertEquals(editorKit, operator2.getEditorKit());
        assertEquals(editorPane.getEditorKit(), operator2.getEditorKit());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class EditorKitTest extends EditorKit {    
        public String getContentType() {
            return null;
        }

        public ViewFactory getViewFactory() {
            return null;
        }

        public Action[] getActions() {
            return null;
        }

        public Caret createCaret() {
            return null;
        }

        public Document createDefaultDocument() {
            return new DefaultStyledDocument();
        }

        public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        }

        public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
        }

        public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        }

        public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        }
    }
    
    /**
     * Test getEditorKitForContentType method.
     */
    public void testGetEditorKitForContentType() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator2 = new JEditorPaneOperator(operator1);
        assertNotNull(operator2);
        
        EditorKitTest editorKit = new EditorKitTest();
        operator2.setEditorKitForContentType("text/plain", editorKit);
        assertEquals(editorKit, operator2.getEditorKitForContentType("text/plain"));
        assertEquals(editorPane.getEditorKitForContentType("text/plain"), 
                     operator2.getEditorKitForContentType("text/plain"));
    }
    
    /**
     * Test getPage method.
     */
    public void testGetPage() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator4 = new JEditorPaneOperator(operator1);
        assertNotNull(operator4);
        
        try {
            operator4.setPage(new URL("http://jemmy.netbeans.org"));
            assertEquals("http://jemmy.netbeans.org", operator4.getPage().toString());
            assertEquals("http://jemmy.netbeans.org", editorPane.getPage().toString());

            operator4.setPage("http://jemmy.netbeans.org/plan.html");
            
            JEditorPaneOperator operator5 = new JEditorPaneOperator(operator1);
            assertEquals("http://jemmy.netbeans.org/plan.html", operator5.getPage().toString());
            assertEquals("http://jemmy.netbeans.org/plan.html", editorPane.getPage().toString());
        } 
        catch(MalformedURLException mex) {
        } 
        catch(JemmyException jex) {
        }
        
    }
    
    /**
     * Test read method.
     */
    public void testRead() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JEditorPaneOperator operator2 = new JEditorPaneOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setContentType("text/html");
        operator2.read(new ByteArrayInputStream("<html></html>".getBytes()), HTMLDocument.class);
        assertTrue(editorPane.getText().startsWith("<html>"));;
    }
}
