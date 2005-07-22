/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.text;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;

/**
 * Tests that the CloneableEditorSupport.wrapEditorComponent() method
 * is called by CloneableEditor and its result value used.
 *
 * @author Andrei Badea
 */
public class WrapEditorComponentTest extends NbTestCase
implements CloneableEditorSupport.Env {
    
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    
    private WrapEditorComponentCES support;
    
    public WrapEditorComponentTest(String s) {
        super(s);
    }
    
    protected void setUp() {
        support = new WrapEditorComponentCES(this, Lookup.EMPTY);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    /**
     * Tests the wrapEditorComponent() method is called for a default editor.
     */
    public void testWrapEditorComponentInDefaultEditor() {
        searchForWrapperComponent();
    }
    
    /**
     * Tests the wrapEditorComponent() method is called for a custom editor 
     */
    public void testWrapEditorComponentInCustomEditor() {
        // first make the support return a document which has a custom editor
        support.setEditorKit(new NbLikeEditorKitWithCustomEditor());
        
        searchForWrapperComponent();
    }
    
    /**
     * Helper method which opens the support and searches for the wrapper 
     * component.
     */
    private void searchForWrapperComponent() {
        support.open();
        
        CloneableEditor ed = (CloneableEditor)support.getRef ().getAnyComponent();
        Component component = ed.getEditorPane();
        
        boolean found = false;
        while (component != ed) {
            if (WrapEditorComponentCES.WRAPPER_NAME.equals(component.getName())) {
                found = true;
                break;
            }
            component = component.getParent();
        }
        
        assertTrue("The panel containing the editor was not found in the TopComponent.", found);
        
        support.close();
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }    
    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }
    
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
    }
    
    public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public java.util.Date getTime() {
        return date;
    }
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            public void close () throws java.io.IOException {
                super.close ();
                content = new String (toByteArray ());
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isModified() {
        return modified;
    }

    public void markModified() throws java.io.IOException {
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /**
     * Implementation of the CES which overrides the wrapEditorComponent()
     * method, wrapping the editor in a component named WRAPPER_NAME.
     */
    private static final class WrapEditorComponentCES extends CloneableEditorSupport {
        
        public static final String WRAPPER_NAME = "panelWrappingTheEditor";
        
        private EditorKit kit;
        
        public WrapEditorComponentCES(Env env, Lookup l) {
            super(env, l);
        }
        
        protected Component wrapEditorComponent(Component editorComponent) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setName(WRAPPER_NAME);
            panel.add(editorComponent, BorderLayout.CENTER);
            return panel;
        }
        
        protected EditorKit createEditorKit() {
            if (kit != null) {
                return kit;
            } else {
                return super.createEditorKit();
            }
        }
        
        public void setEditorKit(EditorKit kit) {
            this.kit = kit;
        }
        
        public CloneableTopComponent.Ref getRef () {
            return allEditors;
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }
    }
    
    private final static class NbLikeEditorKitWithCustomEditor extends NbLikeEditorKit {
        
        public Document createDefaultDocument() {
            return new CustomDoc();
        }
        
        private final class CustomDoc extends Doc implements NbDocument.CustomEditor {
            
            public Component createEditor(JEditorPane j) {
                JScrollPane result = new JScrollPane();
                result.add(j);
                return result;
            }
        }
    }
}
