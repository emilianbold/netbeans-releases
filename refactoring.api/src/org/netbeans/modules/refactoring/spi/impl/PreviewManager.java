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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanel;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanelContainer;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class PreviewManager {
    
    private class Pair {
        DiffController dc;
        NewDiffSource source;
        Pair(DiffController dc, NewDiffSource source) {
            this.dc = dc;
            this.source = source;
        }
    }

    private static PreviewManager manager;
    private WeakHashMap<RefactoringPanel, HashMap<FileObject, Pair>> map = new WeakHashMap();
    private PreviewManager() {
    }
    
    public static PreviewManager getDefault() {
        if (manager==null)
            manager = new PreviewManager();
        return manager;
    }
    
    public void clean(RefactoringPanel panel) {
        map.remove(panel);
    }
    
    private Pair getPair(SimpleRefactoringElementImplementation element) {
        RefactoringPanel current = RefactoringPanelContainer.getRefactoringComponent().getCurrentPanel();
        HashMap<FileObject, Pair> m = map.get(current);
        if (m!=null) {
            Pair pair = m.get(element.getParentFile());
            if (pair!=null)
                return pair;
        }
        NewDiffSource nds;
        try {
            DiffController diffView = DiffController.create(
                    new OldDiffSource(element),
                    nds = new NewDiffSource(element));
            if (m==null) {
                m = new HashMap<FileObject, Pair>();
                map.put(current, m);
            }
            Pair p = new Pair(diffView, nds);
            m.put(element.getParentFile(), p);
            return p;
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
    }
    
    public void refresh(SimpleRefactoringElementImplementation element) {
        try {
            String newText = SPIAccessor.DEFAULT.getNewFileContent(element);
            if (newText==null) {
                UI.setComponentForRefactoringPreview(null);
                return;
            }
            Pair p = getPair(element);
            UI.setComponentForRefactoringPreview(p.dc.getJComponent());
            p.source.setNewText(newText);
            p.dc.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, element.getPosition().getBegin().getLine());
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
    }
    
    private class OldDiffSource extends StreamSource {
        private FileObject file;
        
        OldDiffSource(SimpleRefactoringElementImplementation r) {
            this.file = r.getParentFile();
        }
        public String getName() {
            return file.getName();
        }

        public String getTitle() {
            return file.getNameExt();
        }

        public String getMIMEType() {
            return file.getMIMEType();
        }

        public Reader createReader() throws IOException {
            return null;
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
        
        public Lookup getLookup() {
            return Lookups.singleton(file);
        }
        
    }
    
    private class NewDiffSource extends StreamSource {
        private SimpleRefactoringElementImplementation element;
        
        NewDiffSource(SimpleRefactoringElementImplementation r) {
            this.element = r;
        }

        public String getName() {
            return "Proposed refactoring";
        }
        
        public String getTitle() {
            return  "Refactored " + element.getParentFile().getNameExt();
        }
        
        public String getMIMEType() {
            return element.getParentFile().getMIMEType();
        }
        
        public Reader createReader() throws IOException {
            return null;
        }
        
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
        
        private Document internal;
        private Document getDocument() {
            if (internal==null) {
                internal = CloneableEditorSupport.getEditorKit(getMIMEType()).createDefaultDocument();
            }
            return internal;
        }
        
        public Lookup getLookup() {
            return Lookups.singleton(getDocument());
        }
        
        public void setNewText(String r) {
            try {
                internal.remove(0, internal.getLength());
                internal.insertString(0, r, null);
            } catch (BadLocationException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }        
}
