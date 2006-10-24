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
package org.netbeans.modules.java.editor.imports;

import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixAllImports {
    
    private static final JavaFixAllImports INSTANCE = new JavaFixAllImports();
    
    public static JavaFixAllImports getDefault() {
        return INSTANCE;
    }
    
    /** Creates a new instance of JavaFixAllImports */
    private JavaFixAllImports() {
    }
    
    public void fixAllImports(FileObject fo) {
        try {
            JavaSource.forFileObject(fo).runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController info) {
                    try {
                        info.toPhase(Phase.ELEMENTS_RESOLVED);

                        ComputeImports.Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> candidates = new ComputeImports().computeCandidates(info);

                        Map<String, List<TypeElement>> filteredCandidates = candidates.a;
                        Map<String, List<TypeElement>> notFilteredCandidates = candidates.b;

                        int size = notFilteredCandidates.size();
                        String[] names = new String[size];
                        String[][] variants = new String[size][];
                        String[] defaults = new String[size];
                        Map<String, TypeElement> fqn2TE = new HashMap<String, TypeElement>();

                        int index = 0;

                        for (String key : notFilteredCandidates.keySet()) {
                            names[index] = key;

                            List<TypeElement> unfilteredVars = notFilteredCandidates.get(key);
                            List<TypeElement> filteredVars = filteredCandidates.get(key);

                            if (!unfilteredVars.isEmpty()) {
                                variants[index] = new String[unfilteredVars.size()];

                                int i = 0;

                                for (TypeElement e : filteredVars) {
                                    variants[index][i++] = e.getQualifiedName().toString();
                                    fqn2TE.put(e.getQualifiedName().toString(), e);
                                }

                                for (TypeElement e : unfilteredVars) {
                                    if (filteredVars.contains(e))
                                        continue;

                                    variants[index][i++] = "<html><font color='#808080'><s>" + e.getQualifiedName().toString();
                                    fqn2TE.put(e.getQualifiedName().toString(), e);
                                }
                            } else {
                                variants[index] = new String[1];
                                variants[index][0] = "<html><font color='#FF0000'>&lt;cannot be resolved&gt;";
                            }

                            defaults[index] = variants[index][0];

                            index++;
                        }

                        FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

                        panel.initPanel(names, variants, defaults);

                        DialogDescriptor dd = new DialogDescriptor(panel, "Fix All Imports");
                        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

                        d.setVisible(true);

                        d.setVisible(false);
                        d.dispose();

                        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                            //do imports:
                            List<String> toImport = new ArrayList<String>();

                            for (String fqn : panel.getSelections()) {
                                TypeElement el = fqn2TE.get(fqn);

                                if (el != null) {
                                    toImport.add(el.getQualifiedName().toString());
                                }
                            }

                            try {
                                SourceUtils.addImports(info, toImport);
                            } catch (BadLocationException ex) {
                                ErrorManager.getDefault().notify(ex);
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    } catch (IOException ex) {
                        //TODO: ErrorManager
                        ex.printStackTrace();
                    }
                }
            }, true);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
}
