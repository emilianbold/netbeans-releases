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
package org.netbeans.modules.java.guards;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.CloneableOpenSupport;

/**
 * minimal impl of an editor support
 */
final class Editor implements GuardedEditorSupport {
    
    CloneableEditorSupport support = new EditorSupport();
    InputStream is = null;
    StyledDocument doc = null;
    
    /**
     * here you can pass document content
     */
    public void setStringContent(String txt) {
        is = new ByteArrayInputStream(txt.getBytes());
    }
    
    public StyledDocument getDocument() {
        return doc;
    }
    
    class EditorSupport extends CloneableEditorSupport {
        
        EditorSupport() {
            super(new CESEnv());
        }
        
        protected String messageSave() {
            throw new UnsupportedOperationException();
        }
        
        protected String messageName() {
            return "";
        }
        
        protected String messageToolTip() {
            throw new UnsupportedOperationException();
        }
        
        protected String messageOpening() {
            throw new UnsupportedOperationException();
        }
        
        protected String messageOpened() {
            throw new UnsupportedOperationException();
        }
        
        protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
        throws IOException, BadLocationException {
            Editor.this.doc = doc;
            kit.read(stream, doc, 0);
        }
        
    }
    
    class CESEnv implements CloneableEditorSupport.Env {
        public InputStream inputStream() throws IOException {
            return Editor.this.is;
        }
        
        public OutputStream outputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
        
        public Date getTime() {
            throw new UnsupportedOperationException();
        }
        
        public String getMimeType() {
            return "text/x-java";
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public boolean isValid() {
            return true;
        }
        
        public boolean isModified() {
            throw new UnsupportedOperationException();
        }
        
        public void markModified() throws IOException {
            throw new UnsupportedOperationException();
        }
        
        public void unmarkModified() {
            throw new UnsupportedOperationException();
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return Editor.this.support;
        }
        
    }
    
}