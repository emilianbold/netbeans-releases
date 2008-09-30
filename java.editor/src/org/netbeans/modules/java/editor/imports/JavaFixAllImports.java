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
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.java.editor.semantic.SemanticHighlighter;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixAllImports {
    
    private static final String PREFS_KEY = JavaFixAllImports.class.getName();
    private static final String KEY_REMOVE_UNUSED_IMPORTS = "removeUnusedImports";
    private static final JavaFixAllImports INSTANCE = new JavaFixAllImports();
    
    public static JavaFixAllImports getDefault() {
        return INSTANCE;
    }
    
    /** Creates a new instance of JavaFixAllImports */
    private JavaFixAllImports() {
    }
    
    public void fixAllImports(FileObject fo) {
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy wc) {
                try {
                    wc.toPhase(Phase.RESOLVED);

                    ComputeImports.Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> candidates = new ComputeImports().computeCandidates(wc);

                    Map<String, List<TypeElement>> filteredCandidates = candidates.a;
                    Map<String, List<TypeElement>> notFilteredCandidates = candidates.b;

                    int size = notFilteredCandidates.size();
                    String[] names = new String[size];
                    String[][] variants = new String[size][];
                    Icon[][] icons = new Icon[size][];
                    String[] defaults = new String[size];
                    Map<String, TypeElement> fqn2TE = new HashMap<String, TypeElement>();
                    Map<String, String> displayName2FQN = new HashMap<String, String>();
                    Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);

                    int index = 0;
                    
                    boolean shouldShowImportsPanel = false;

                    for (String key : notFilteredCandidates.keySet()) {
                        names[index] = key;

                        List<TypeElement> unfilteredVars = notFilteredCandidates.get(key);
                        List<TypeElement> filteredVars = filteredCandidates.get(key);

                        shouldShowImportsPanel |= unfilteredVars.size() > 1;
                        
                        if (!unfilteredVars.isEmpty()) {
                            variants[index] = new String[unfilteredVars.size()];
                            icons[index] = new Icon[variants[index].length];

                            int i = -1;
                            int minImportanceLevel = Integer.MAX_VALUE;

                            for (TypeElement e : filteredVars) {
                                variants[index][++i] = e.getQualifiedName().toString();
                                icons[index][i] = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                                int level = Utilities.getImportanceLevel(variants[index][i]);
                                if (level < minImportanceLevel) {
                                    defaults[index] = variants[index][i];
                                    minImportanceLevel = level;
                                }
                                fqn2TE.put(e.getQualifiedName().toString(), e);
                            }

                            for (TypeElement e : unfilteredVars) {
                                if (filteredVars.contains(e))
                                    continue;

                                String fqn = e.getQualifiedName().toString();
                                String dn = "<html><font color='#808080'><s>" + fqn;
                                
                                variants[index][++i] = dn;
                                int level = Utilities.getImportanceLevel(fqn);
                                if (level < minImportanceLevel) {
                                    defaults[index] = variants[index][i];
                                    minImportanceLevel = level;
                                }
                                fqn2TE.put(fqn, e);
                                displayName2FQN.put(dn, fqn);
                            }
                        } else {
                            variants[index] = new String[1];
                            variants[index][0] = NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_CannotResolve"); //NOI18N
                            defaults[index] = variants[index][0];
                            icons[index] = new Icon[1];
                            icons[index][0] = new ImageIcon( ImageUtilities.loadImage("org/netbeans/modules/java/editor/resources/error-glyph.gif") );//NOI18N
                        }

                        index++;
                    }

                    boolean fixImports = false;
                    String[] selections = null;
                    boolean removeUnusedImports;
                    
                    if( shouldShowImportsPanel ) {
                        FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

                        panel.initPanel(names, variants, icons, defaults, prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true));

                        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_Title")); //NOI18N
                        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

                        d.setVisible(true);

                        d.setVisible(false);
                        d.dispose();
                        fixImports = dd.getValue() == DialogDescriptor.OK_OPTION;
                        selections = panel.getSelections();
                        removeUnusedImports = panel.getRemoveUnusedImports();
                    } else {
                        fixImports = true;
                        selections = defaults;
                        removeUnusedImports = prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true);
                    }

                    if ( fixImports ) {
                        
                        if( shouldShowImportsPanel )
                            prefs.putBoolean(KEY_REMOVE_UNUSED_IMPORTS, removeUnusedImports);
                        
                        //do imports:
                        List<String> toImport = new ArrayList<String>();

                        for (String dn : selections) {
                            String fqn = displayName2FQN.get(dn);
                            TypeElement el = fqn2TE.get(fqn != null ? fqn : dn);

                            if (el != null) {
                                toImport.add(el.getQualifiedName().toString());
                            }
                        }
                        
                        CompilationUnitTree cut = wc.getCompilationUnit();
                        
                        boolean someImportsWereRemoved = false;
                        if (removeUnusedImports) {
                            //compute imports to remove:
                            List<TreePathHandle> unusedImports = SemanticHighlighter.computeUnusedImports(wc);
                            someImportsWereRemoved = !unusedImports.isEmpty();
                            
                            // make the changes to the source
                            for (TreePathHandle handle : unusedImports) {
                                TreePath path = handle.resolve(wc);
                                
                                assert path != null;
                                
                                cut = wc.getTreeMaker().removeCompUnitImport(cut, (ImportTree) path.getLeaf());
                            }
                        }
                        
                        cut = addImports(cut, toImport, wc.getTreeMaker());
                        
                        wc.rewrite(wc.getCompilationUnit(), cut);
                        
                        if( !shouldShowImportsPanel ) {
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
                } catch (IOException ex) {
                    //TODO: ErrorManager
                    ex.printStackTrace();
                }
            }
        };
        try {
            JavaSource javaSource = JavaSource.forFileObject(fo);
            if (javaSource==null) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaFixAllImports.class, "MSG_CannotFixImports" ));
            } else {
                javaSource.runModificationTask(task).commit();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    //XXX: copied from SourceUtils.addImports. Ideally, should be on one place only:
    public static CompilationUnitTree addImports(CompilationUnitTree cut, List<String> toImport, TreeMaker make)
        throws IOException {
        // do not modify the list given by the caller (may be reused or immutable).
        toImport = new ArrayList<String>(toImport); 
        Collections.sort(toImport);

        List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
        int currentToImport = toImport.size() - 1;
        int currentExisting = imports.size() - 1;
        
        while (currentToImport >= 0 && currentExisting >= 0) {
            String currentToImportText = toImport.get(currentToImport);
            
            while (currentExisting >= 0 && (imports.get(currentExisting).isStatic() || imports.get(currentExisting).getQualifiedIdentifier().toString().compareTo(currentToImportText) > 0))
                currentExisting--;
            
            if (currentExisting >= 0) {
                imports.add(currentExisting+1, make.Import(make.Identifier(currentToImportText), false));
                currentToImport--;
            }
        }
        // we are at the head of import section and we still have some imports
        // to add, put them to the very beginning
        while (currentToImport >= 0) {
            String importText = toImport.get(currentToImport);
            imports.add(0, make.Import(make.Identifier(importText), false));
            currentToImport--;
        }
        // return a copy of the unit with changed imports section
        return make.CompilationUnit(cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
    }
    
}
