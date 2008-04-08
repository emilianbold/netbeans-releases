/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.EqualsHashCodeGenerator;
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

            int[] span = compilationInfo.getTreeUtilities().findNameSpan((MethodTree) treePath.getLeaf());

            if (span != null) {
                ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(), 
                    NbBundle.getMessage(MissingHashCode.class, addHint), 
                    fixes, 
                    compilationInfo.getFileObject(),
                    span[0], 
                    span[1]
                );

                return Collections.singletonList(ed);
            }
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
          
    private static final class FixImpl implements Fix, Runnable, Task<WorkingCopy> {
        private TreePathHandle handle;
        private FileObject file;
        private String msg;
        private boolean fieldFound;
        
        public FixImpl(String type, TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
            this.msg = type;
        }
        
        public String getText() {
            return NbBundle.getMessage(MissingHashCode.class, msg);
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(this);
            if (fieldFound) {
                EventQueue.invokeLater(this);
            } else {
                result.commit();
            }
            
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
        
        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            for (Element elem : handle.resolveElement(wc).getEnclosedElements()) {
                if (elem.getKind() == ElementKind.FIELD) {
                    fieldFound = true;
                    return;
                }
            }
            EqualsHashCodeGenerator.generateEqualsAndHashCode(wc, handle.resolve(wc));
        }
        
        @Override
        public String toString() {
            return "Fix";
        }
    }
    
}
