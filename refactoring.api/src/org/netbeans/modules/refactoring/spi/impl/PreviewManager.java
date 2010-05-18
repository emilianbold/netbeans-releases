/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
            if(element.getPosition() != null) 
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
            if (file.isFolder()) {
                return NbBundle.getMessage(PreviewManager.class,"LBL_FileDoesNotExist");
            }
            return file.getName();
        }

        public String getTitle() {
            if (file.isFolder()) {
                return NbBundle.getMessage(PreviewManager.class,"LBL_FileDoesNotExist");
            }
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
        
        NewDiffSource(SimpleRefactoringElementImplementation refactElemImpl) {
            this.element = refactElemImpl;
        }

        public String getName() {
            return NbBundle.getMessage(PreviewManager.class,"LBL_ProposedRefactoring");
        }
        
        public String getTitle() {
            if (element.getParentFile().isFolder()) {
                return NbBundle.getMessage(PreviewManager.class,"LBL_NewFile");
            }
            return  NbBundle.getMessage(PreviewManager.class,"LBL_Refactored",element.getParentFile().getNameExt());
        }
        
        public String getMIMEType() {
            if (element.getParentFile().isFolder()) {
                //this is hack, all folders are text/x-java
                return "text/x-java"; //NOI18N
            }
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
        
        public void setNewText(String newText) {
            try {
                internal.remove(0, internal.getLength());
                internal.insertString(0, newText, null);
            } catch (BadLocationException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }        
}
