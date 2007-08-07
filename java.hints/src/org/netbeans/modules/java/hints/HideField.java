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
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.rename.InstantRenamePerformer;
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
public class HideField extends AbstractHint {
    transient volatile boolean stop;
    
    /** Creates a new instance of AddOverrideAnnotation */
    public HideField() {
        super( true, true, AbstractHint.HintSeverity.WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.VARIABLE);
    }

    protected List<Fix> computeFixes(CompilationInfo compilationInfo, TreePath treePath, Document doc, int[] bounds) {
        stop = false;
        
        Element el = compilationInfo.getTrees().getElement(treePath);
        if (el == null || el.getKind() != ElementKind.FIELD) {
            return null;
        }
        if (el.getSimpleName().contentEquals("<error>")) { //NOI18N
            return null;
        }
        
        Element hidden = null;
        TypeElement te = (TypeElement)el.getEnclosingElement();
        for (Element field : compilationInfo.getElements().getAllMembers(te)) {
            if (stop) {
                return null;
            }
            if (compilationInfo.getElements().hides(el, field)) {
                hidden = field;
                break;
            }
        }
        if (hidden == null) {
            return null;
        }
        
        int[] span = Utilities.findIdentifierSpan(
            treePath,
            compilationInfo,
            doc
        );
        if (span[0] == (-1) || span[1] == (-1)) {
            return null;
        }
        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
            (span[1] + span[0]) / 2,
            compilationInfo.getFileObject()
        ));


        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        try {
            Document doc = compilationInfo.getDocument();
            
            if (doc == null) {
                return null;
            }
        
            int[] span = new int[2];
            List<Fix> fixes = computeFixes(compilationInfo, treePath, doc, span);
            if (fixes == null) {
                return null;
            }

            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(),
                getDisplayName(),
                fixes,
                doc,
                doc.createPosition(span[0]),
                doc.createPosition(span[1]) // NOI18N
            );

            return Collections.singletonList(ed);
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
        return NbBundle.getMessage(DoubleCheck.class, "MSG_HiddenField"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_HiddenField"); // NOI18N
    }

    public void cancel() {
        stop = true;
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    

    static final class FixImpl implements Fix, Runnable {
        private final int caret;
        private final FileObject file;
        
        public FixImpl(int caret, FileObject file) {
            this.caret = caret;
            this.file = file;
        }
        
        
        public String getText() {
            return NbBundle.getMessage(DoubleCheck.class, "MSG_FixHiddenFiledText"); // NOI18N
        }
        
        public ChangeInfo implement() throws IOException {
            SwingUtilities.invokeLater(this);
            return null;
        }
        
        public void run() {
            try {
                EditorCookie cook = DataObject.find(file).getLookup().lookup(EditorCookie.class);
                JEditorPane[] arr = cook.getOpenedPanes();
                if (arr == null) {
                    return;
                }
                arr[0].setCaretPosition(caret);
                InstantRenamePerformer.invokeInstantRename(arr[0]);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        
        @Override public String toString() {
            return "FixHideField"; // NOI18N
        }

        public void cancel() {
        }
    }
    
}
