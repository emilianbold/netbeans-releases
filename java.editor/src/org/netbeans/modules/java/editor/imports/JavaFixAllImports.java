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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.java.editor.semantic.SemanticHighlighter;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixAllImports {
    
    //-J-Dorg.netbeans.modules.java.editor.imports.JavaFixAllImports.invalid_import_html="<html><font color='#808080'>"
    public static final String NOT_VALID_IMPORT_HTML = System.getProperty(JavaFixAllImports.class.getName() + ".invalid_import_html", "");
    
    private static final String PREFS_KEY = JavaFixAllImports.class.getName();
    private static final String KEY_REMOVE_UNUSED_IMPORTS = "removeUnusedImports"; // NOI18N
    private static final JavaFixAllImports INSTANCE = new JavaFixAllImports();
    
    public static JavaFixAllImports getDefault() {
        return INSTANCE;
    }
    
    /** Creates a new instance of JavaFixAllImports */
    private JavaFixAllImports() {
    }
    
    public void fixAllImports(FileObject fo) {
        final AtomicBoolean cancel = new AtomicBoolean();
        final JavaSource javaSource = JavaSource.forFileObject(fo);
        final AtomicReference<ImportData> id = new AtomicReference<ImportData>();
        final Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) {
                boolean removeUnusedImports;
                try {
                    wc.toPhase(Phase.RESOLVED);
                    if (cancel.get()) {
                        return;
                    }

                    final ImportData data = computeImports(wc);

                    if (cancel.get()) {
                        return;
                    }

                    if (data.shouldShowImportsPanel) {
                        if (!cancel.get()) {
                            id.set(data);
                        }
                    } else {
                        Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);
                        
                        removeUnusedImports = prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true);
                        performFixImports(wc, data, data.defaults, removeUnusedImports);
                    }
                } catch (IOException ex) {
                    //TODO: ErrorManager
                    ex.printStackTrace();
                }
            }
        };

        if (javaSource == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaFixAllImports.class, "MSG_CannotFixImports"));
        } else {
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                public void run() {
                    try {
                        javaSource.runModificationTask(task).commit();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, "Fix All Imports", cancel, false);

            if (id.get() != null && !cancel.get()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showFixImportsDialog(javaSource, id.get());
                    }
                });
            }
        }
    }
    
    private static List<TreePathHandle> getImportsFromSamePackage(WorkingCopy wc) {
        ImportVisitor v = new ImportVisitor(wc);
        v.scan(wc.getCompilationUnit(), null);
        return v.getImports();
    }

    private static class ImportVisitor extends TreePathScanner {
        private CompilationInfo info;
        private String currentPackage;
        private List<TreePathHandle> imports;

        private ImportVisitor (CompilationInfo info) {
            this.info = info;
            ExpressionTree pkg = info.getCompilationUnit().getPackageName();
            currentPackage = pkg != null ? pkg.toString() : "";
            imports = new ArrayList<TreePathHandle>();
        }

        @Override
        public Object visitImport(ImportTree node, Object d) {
            if (node.getQualifiedIdentifier().getKind() == Kind.MEMBER_SELECT) {
                ExpressionTree exp = ((MemberSelectTree) node.getQualifiedIdentifier()).getExpression();
                if (exp.toString().equals(currentPackage)) {
                    imports.add(TreePathHandle.create(getCurrentPath(), info));
                }
            }

            super.visitImport(node, null);
            return null;
        }

        List<TreePathHandle> getImports() {
            return imports;
        }
    }

    private static void performFixImports(WorkingCopy wc, ImportData data, String[] selections, boolean removeUnusedImports) throws IOException {
        //do imports:
        Set<Element> toImport = new HashSet<Element>();

        for (String dn : selections) {
            String fqn = data.displayName2FQN.get(dn);
            TypeElement el = data.fqn2TE.get(fqn != null ? fqn : dn);

            if (el != null) {
                toImport.add(el);
            }
        }

        CompilationUnitTree cut = wc.getCompilationUnit();

        boolean someImportsWereRemoved = false;
        
        if (removeUnusedImports) {
            //compute imports to remove:
            List<TreePathHandle> unusedImports = SemanticHighlighter.computeUnusedImports(wc);
            unusedImports.addAll(getImportsFromSamePackage(wc));
            someImportsWereRemoved = !unusedImports.isEmpty();

            // make the changes to the source
            for (TreePathHandle handle : unusedImports) {
                TreePath path = handle.resolve(wc);

                assert path != null;

                cut = wc.getTreeMaker().removeCompUnitImport(cut, (ImportTree) path.getLeaf());
            }
        }

        cut = GeneratorUtilities.get(wc).addImports(cut, toImport);

        wc.rewrite(wc.getCompilationUnit(), cut);

        if( !data.shouldShowImportsPanel ) {
            String statusText;
            if( toImport.isEmpty() && !someImportsWereRemoved ) {
                Toolkit.getDefaultToolkit().beep();
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_NothingToFix" ); //NOI18N
            } else if( toImport.isEmpty() && someImportsWereRemoved ) {
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_UnusedImportsRemoved" ); //NOI18N
            } else {
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_ImportsFixed" ); //NOI18N
            }
            StatusDisplayer.getDefault().setStatusText( statusText );
        }
    }

    private static ImportData computeImports(CompilationInfo info) {
        ComputeImports.Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> candidates = new ComputeImports().computeCandidates(info);

        Map<String, List<TypeElement>> filteredCandidates = candidates.a;
        Map<String, List<TypeElement>> notFilteredCandidates = candidates.b;

        int size = notFilteredCandidates.size();
        ImportData data = new ImportData(size);

        int index = 0;

        boolean shouldShowImportsPanel = false;

        for (String key : notFilteredCandidates.keySet()) {
            data.names[index] = key;

            List<TypeElement> unfilteredVars = notFilteredCandidates.get(key);
            List<TypeElement> filteredVars = filteredCandidates.get(key);

            shouldShowImportsPanel |= unfilteredVars.size() > 1;

            if (!unfilteredVars.isEmpty()) {
                data.variants[index] = new String[unfilteredVars.size()];
                data.icons[index] = new Icon[data.variants[index].length];

                int i = -1;
                int minImportanceLevel = Integer.MAX_VALUE;

                for (TypeElement e : filteredVars) {
                    data.variants[index][++i] = e.getQualifiedName().toString();
                    data.icons[index][i] = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                    int level = Utilities.getImportanceLevel(data.variants[index][i]);
                    if (level < minImportanceLevel) {
                        data.defaults[index] = data.variants[index][i];
                        minImportanceLevel = level;
                    }
                    data.fqn2TE.put(e.getQualifiedName().toString(), e);
                }
                
                if (data.defaults[index] != null)
                    minImportanceLevel = Integer.MIN_VALUE;

                for (TypeElement e : unfilteredVars) {
                    if (filteredVars.contains(e))
                        continue;

                    String fqn = e.getQualifiedName().toString();
                    String dn = NOT_VALID_IMPORT_HTML + fqn;

                    data.variants[index][++i] = dn;
                    data.icons[index][i] = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                    int level = Utilities.getImportanceLevel(fqn);
                    if (level < minImportanceLevel) {
                        data.defaults[index] = data.variants[index][i];
                        minImportanceLevel = level;
                    }
                    data.fqn2TE.put(fqn, e);
                    data.displayName2FQN.put(dn, fqn);
                }
            } else {
                data.variants[index] = new String[1];
                data.variants[index][0] = NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_CannotResolve"); //NOI18N
                data.defaults[index] = data.variants[index][0];
                data.icons[index] = new Icon[1];
                data.icons[index][0] = ImageUtilities.loadImageIcon("org/netbeans/modules/java/editor/resources/error-glyph.gif", false);//NOI18N
            }

            index++;
        }

        data.shouldShowImportsPanel = shouldShowImportsPanel;

        return data;
    }

    static final class ImportData {
        public final String[] names;
        public final String[][] variants;
        public final Icon[][] icons;
        public final String[] defaults;
        public final Map<String, TypeElement> fqn2TE;
        public final Map<String, String> displayName2FQN;
        public       boolean shouldShowImportsPanel;

        public ImportData(int size) {
            names = new String[size];
            variants = new String[size][];
            icons = new Icon[size][];
            defaults = new String[size];
            fqn2TE = new HashMap<String, TypeElement>();
            displayName2FQN = new HashMap<String, String>();
        }
    }

    private static final RequestProcessor WORKER = new RequestProcessor(JavaFixAllImports.class.getName(), 1);
    
    private static void showFixImportsDialog(final JavaSource js, final ImportData data) {
        final Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);
        final FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

        panel.initPanel(data.names, data.variants, data.icons, data.defaults, prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true));

        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");
        final AtomicBoolean stop = new AtomicBoolean();
        DialogDescriptor dd = new DialogDescriptor(panel,
                                                   NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_Title"), //NOI18N
                                                   true,
                                                   new Object[] {ok, cancel},
                                                   ok,
                                                   DialogDescriptor.DEFAULT_ALIGN,
                                                   HelpCtx.DEFAULT_HELP,
                                                   new ActionListener() {
                                                       public void actionPerformed(ActionEvent e) {}
                                                   },
                                                   true
                                                   );

        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                final String[] selections = panel.getSelections();
                final boolean removeUnusedImports = panel.getRemoveUnusedImports();
                WORKER.post(new Runnable() {
                    public void run() {
                        try {
                            js.runModificationTask(new Task<WorkingCopy>() {
                                public void run(WorkingCopy wc) throws Exception {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            cancel.setEnabled(false);
                                            ((JDialog)d).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                        }
                                    });                                    
                                    wc.toPhase(Phase.RESOLVED);
                                    if (stop.get()) return;
                                    performFixImports(wc, data, selections, removeUnusedImports);
                                }
                            }).commit();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        prefs.putBoolean(KEY_REMOVE_UNUSED_IMPORTS, removeUnusedImports);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                d.setVisible(false);
                            }
                        });
                    }
                });
            }
        });

        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stop.set(true);
                d.setVisible(false);
            }
        });

        d.setVisible(true);

        d.dispose();
    }

}
