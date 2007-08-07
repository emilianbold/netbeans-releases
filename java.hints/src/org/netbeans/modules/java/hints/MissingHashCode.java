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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class MissingHashCode extends AbstractHint {
    transient volatile boolean[] stop = { false };
    
    /** Creates a new instance of AddOverrideAnnotation */
    public MissingHashCode() {
        super( true, true, AbstractHint.HintSeverity.WARNING );
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        stop = new boolean [1];
        try {
            Document doc = compilationInfo.getDocument();
            
            if (doc == null) {
                return null;
            }
            
            Element e = compilationInfo.getTrees().getElement(treePath);
            if (e == null) {
                return null;
            }
            
            Element parent = e.getEnclosingElement();
            ExecutableElement[] ret = EqualsHashCodeGenerator.overridesHashCodeAndEquals(compilationInfo, parent, stop);
            if (e != ret[0] && e != ret[1]) {
                return null;
            }

            String addHint = null;
            if (ret[0] == null && ret[1] != null) {
                addHint = "MSG_GenEquals"; // NOI18N
            }
            if (ret[1] == null && ret[0] != null) {
                addHint = "MSG_GenHashCode"; // NOI18N
            }
                
            if (addHint != null) {

                List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                    addHint, 
                    TreePathHandle.create(parent, compilationInfo), 
                    compilationInfo.getFileObject()
                ));

                int[] span = Utilities.findIdentifierSpan(treePath, compilationInfo, doc);

                if (span[0] != (-1) && span[1] != (-1)) {
                    ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(), 
                        NbBundle.getMessage(MissingHashCode.class, addHint), 
                        fixes, 
                        doc, 
                        doc.createPosition((int) span[0]), 
                        doc.createPosition((int) span[1])
                    );

                    return Collections.singletonList(ed);
                }
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return null;
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MissingHashCode.class, "MSG_MissingHashCode");
    }

    public String getDescription() {
        return NbBundle.getMessage(MissingHashCode.class, "HINT_MissingHashCode");
    }

    public void cancel() {
        stop[0] = true;
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    
          
    private static final class FixImpl implements Fix, Runnable {
        private TreePathHandle handle;
        private FileObject file;
        private String msg;
        
        public FixImpl(String type, TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
            this.msg = type;
        }
        
        public String getText() {
            return NbBundle.getMessage(MissingHashCode.class, msg);
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() {
            EventQueue.invokeLater(this);
            return null;
        }
        
        public void run() {
            try {
                EditorCookie cook = DataObject.find(file).getLookup().lookup(EditorCookie.class);
                JEditorPane[] arr = cook.getOpenedPanes();
                if (arr == null) {
                    return;
                }
                EqualsHashCodeGenerator.invokeEqualsHashCode(handle, arr[0]);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
}
