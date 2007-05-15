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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.semantic.SemanticHighlighter;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
            }
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

                    for (String key : notFilteredCandidates.keySet()) {
                        names[index] = key;

                        List<TypeElement> unfilteredVars = notFilteredCandidates.get(key);
                        List<TypeElement> filteredVars = filteredCandidates.get(key);

                        if (!unfilteredVars.isEmpty()) {
                            variants[index] = new String[unfilteredVars.size()];
                            icons[index] = new Icon[variants[index].length];

                            int i = -1;
                            int minImportanceLevel = Integer.MAX_VALUE;

                            for (TypeElement e : filteredVars) {
                                variants[index][++i] = e.getQualifiedName().toString();
                                icons[index][i] = UiUtils.getElementIcon(e.getKind(), e.getModifiers());
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
                            icons[index][0] = new ImageIcon( org.openide.util.Utilities.loadImage("org/netbeans/modules/java/editor/resources/error-glyph.gif") );//NOI18N
                        }

                        index++;
                    }

                    FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

                    panel.initPanel(names, variants, icons, defaults, prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true));

                    DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_Title")); //NOI18N
                    Dialog d = DialogDisplayer.getDefault().createDialog(dd);

                    d.setVisible(true);

                    d.setVisible(false);
                    d.dispose();

                    if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                        boolean removeUnusedImports = panel.getRemoveUnusedImports();
                        
                        prefs.putBoolean(KEY_REMOVE_UNUSED_IMPORTS, removeUnusedImports);
                        
                        //do imports:
                        List<String> toImport = new ArrayList<String>();

                        for (String dn : panel.getSelections()) {
                            String fqn = displayName2FQN.get(dn);
                            TypeElement el = fqn2TE.get(fqn != null ? fqn : dn);

                            if (el != null) {
                                toImport.add(el.getQualifiedName().toString());
                            }
                        }
                        
                        CompilationUnitTree cut = wc.getCompilationUnit();
                        
                        if (removeUnusedImports) {
                            //compute imports to remove:
                            List<TreePathHandle> unusedImports = SemanticHighlighter.computeUnusedImports(wc);
                            
                            // make the changes to the source
                            for (TreePathHandle handle : unusedImports) {
                                TreePath path = handle.resolve(wc);
                                
                                assert path != null;
                                
                                cut = wc.getTreeMaker().removeCompUnitImport(cut, (ImportTree) path.getLeaf());
                            }
                        }
                        
                        cut = SourceUtils.addImports(cut, toImport, wc.getTreeMaker());
                        
                        wc.rewrite(wc.getCompilationUnit(), cut);
                    }
                } catch (IOException ex) {
                    //TODO: ErrorManager
                    ex.printStackTrace();
                }
            }
        };
        try {
            JavaSource javaSource = JavaSource.forFileObject(fo);
            javaSource.runModificationTask(task).commit();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
}
