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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.beans.addproperty;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class AddPropertyCodeGenerator implements CodeGenerator {

    private JTextComponent component;
    private String className;
    private List<String> existingFields;
    private String[] pcsName;
    private String[] vcsName;

    public AddPropertyCodeGenerator(JTextComponent component, String className, List<String> existingFields, String[] pcsName, String[] vcsName) {
        this.component = component;
        this.className = className;
        this.existingFields = existingFields;
        this.pcsName = pcsName;
        this.vcsName = vcsName;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AddPropertyCodeGenerator.class, "DN_AddProperty");
    }

    public void invoke() {
        Object o = component.getDocument().getProperty(Document.StreamDescriptionProperty);

        if (o instanceof DataObject) {
            DataObject d = (DataObject) o;

            perform(d.getPrimaryFile(), component);
        }
    }

    public void perform(FileObject file, JTextComponent pane) {
        JButton ok = new JButton(NbBundle.getMessage(AddPropertyCodeGenerator.class, "LBL_ButtonOK"));
        final AddPropertyPanel addPropertyPanel = new AddPropertyPanel(file, className, existingFields, pcsName, vcsName, ok);
        String caption = NbBundle.getMessage(AddPropertyCodeGenerator.class, "CAP_AddProperty");
        String cancel = NbBundle.getMessage(AddPropertyCodeGenerator.class, "LBL_ButtonCancel");
        DialogDescriptor dd = new DialogDescriptor(addPropertyPanel,caption, true, new Object[] {ok,cancel}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) == ok) {
            insertCode2(file, pane, addPropertyPanel.getAddPropertyConfig());
        }
    }

    /**
     * work around for {@link #insertCode}.
     * @see <a href=http://www.netbeans.org/issues/show_bug.cgi?id=162853>162853</a>
     * @see <a href=http://www.netbeans.org/issues/show_bug.cgi?id=162630>162630</a>
     */
    static void insertCode2(final FileObject file, final JTextComponent pane, final AddPropertyConfig config) {
            final Document doc = pane.getDocument();
            final Reformat r = Reformat.get(pane.getDocument());
            final String code = new AddPropertyGenerator().generate(config);
            final Position[] bounds = new Position[2];

            r.lock();
            try {
                NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
                    public void run() {
                        try {
                            int startOffset = pane.getCaretPosition();
                            doc.insertString(startOffset, code, null);
                            Position start = doc.createPosition(startOffset);
                            Position end = doc.createPosition(startOffset + code.length());
                            r.reformat(Utilities.getRowStart(pane, start.getOffset()), Utilities.getRowEnd(pane, end.getOffset()));
                            bounds[0] = start;
                            bounds[1] = end;
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                r.unlock();
            }

            if (bounds[0] != null) {
                // code insertion to document passed
                try {
                    JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {
                        public void run(WorkingCopy parameter) throws Exception {
                            parameter.toPhase(Phase.RESOLVED);

                            Position start = bounds[0];
                            Position end = bounds[1];
                            
                            new ImportFQNsHack(parameter, start.getOffset(), end.getOffset()).scan(parameter.getCompilationUnit(), null);

                            CompilationUnitTree cut = parameter.getCompilationUnit();

                            parameter.rewrite(cut, parameter.getTreeMaker().CompilationUnit(cut.getPackageName(), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                        }
                    }).commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
    }

    static void insertCode(final FileObject file, final JTextComponent pane, final AddPropertyConfig config) {
        try {
            JavaSource.create(ClasspathInfo.create(file)).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    insertCodeImpl(file, pane, config);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    static void insertCodeImpl(final FileObject file, final JTextComponent pane, final AddPropertyConfig config) {
        try {
            final Document doc = pane.getDocument();
            final Reformat r = Reformat.get(pane.getDocument());

            r.lock();

            try {
                NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
                    public void run() {
                        try {
                            String code = new AddPropertyGenerator().generate(config);
                            int startOffset = pane.getCaretPosition();

                            doc.insertString(startOffset, code, null);

                            final Position start = doc.createPosition(startOffset);
                            final Position end = doc.createPosition(startOffset + code.length());

                            JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {
                                public void run(WorkingCopy parameter) throws Exception {
                                    parameter.toPhase(Phase.RESOLVED);

                                    new ImportFQNsHack(parameter, start.getOffset(), end.getOffset()).scan(parameter.getCompilationUnit(), null);

                                    CompilationUnitTree cut = parameter.getCompilationUnit();

                                    parameter.rewrite(cut, parameter.getTreeMaker().CompilationUnit(cut.getPackageName(), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                                }
                            }).commit();

                            r.reformat(Utilities.getRowStart(pane, start.getOffset()), Utilities.getRowEnd(pane, end.getOffset()));

                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    });
            } finally {
                r.unlock();
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        }

    private static final class ImportFQNsHack extends TreePathScanner<Void, Void> {

        private WorkingCopy wc;
        private int start;
        private int end;

        public ImportFQNsHack(WorkingCopy wc, int start, int end) {
            this.wc = wc;
            this.start = start;
            this.end = end;
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void p) {
            int s = (int) wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), node);
            int e = (int) wc.getTrees().getSourcePositions().getEndPosition(wc.getCompilationUnit(), node);

            if (s >= start && e <= end) {
                Element el = wc.getTrees().getElement(getCurrentPath());

                if (el != null && (el.getKind().isClass() || el.getKind().isInterface()) && ((TypeElement) el).asType().getKind() != TypeKind.ERROR) {
                    wc.rewrite(node, wc.getTreeMaker().QualIdent(el));
                    return null;
                }
            }

            return super.visitMemberSelect(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            return super.visitMethod(node, p);
        }

    }

    public static final class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController cc = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            while (path != null && path.getLeaf().getKind() != Kind.CLASS) {
                path = path.getParentPath();
            }

            if (component == null || cc == null || path == null) {
                return Collections.emptyList();
            }
            
            //find PropertyChangeSupport, VetoableChangeSupport
            //list all fields to detect collisions
            Element e = cc.getTrees().getElement(path);
            
            if (e == null || !e.getKind().isClass()) {
                return Collections.emptyList();
            }

            TypeMirror pcs = resolve(cc, "java.beans.PropertyChangeSupport"); //NOI18N
            TypeMirror vcs = resolve(cc, "java.beans.VetoableChangeSupport"); //NOI18N
            
            if (pcs == null || vcs == null) {
                return Collections.emptyList();
            }
            
            List<String> existingFields = new LinkedList<String>();
            String[] pcsName = new String[2];
            String[] vcsName = new String[2];
            
            for (VariableElement field : ElementFilter.fieldsIn(e.getEnclosedElements())) {
                existingFields.add(field.getSimpleName().toString());
                
                if (field.asType().equals(pcs)) {
                    int i = field.getModifiers().contains(Modifier.STATIC) ? 1 : 0;
                    
                    pcsName[i] = field.getSimpleName().toString();
                }
                
                if (field.asType().equals(vcs)) {
                    int i = field.getModifiers().contains(Modifier.STATIC) ? 1 : 0;

                    vcsName[i] = field.getSimpleName().toString();
                }
            }
            
            String className = ((TypeElement) e).getQualifiedName().toString();
            
            return Collections.singletonList(new AddPropertyCodeGenerator(component, className, existingFields, pcsName,vcsName));
        }
        
        private static TypeMirror resolve(CompilationInfo info, String s) {
            TypeElement te = info.getElements().getTypeElement(s);
            
            if (te == null) return null;
            
            return te.asType();
        }
    }
}
