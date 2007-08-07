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
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class HideFieldByVar extends HideField {
    /** Creates a new instance of AddOverrideAnnotation */
    public HideFieldByVar() {
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_HiddenFieldByVar"); // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_HiddenFieldByVar"); // NOI18N
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo compilationInfo, TreePath treePath, Document doc, int[] bounds) {
        if (treePath.getLeaf().getKind() != Kind.VARIABLE) {
            return null;
        }
        VariableTree vt = (VariableTree)treePath.getLeaf();
        Element el = compilationInfo.getTrees().getElement(treePath);
        if (el == null) {
            return null;
        }
        if (el.getKind() == ElementKind.FIELD) {
            return null;
        }
        boolean isStatic = false;
        while (el != null && !(el instanceof TypeElement)) {
            isStatic = el.getModifiers().contains(Modifier.STATIC);
            el = el.getEnclosingElement();
        }
        if (el == null) {
            return null;
        }
        if (treePath.getParentPath().getLeaf().getKind() == Kind.METHOD) {
            // skip method values
            return null;
        }

        Element hidden = null;
        for (Element e : compilationInfo.getElements().getAllMembers((TypeElement)el)) {
            if (stop) {
                return null;
            }
            
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }
            if (isStatic && !e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (e.getSimpleName() == vt.getName()) {
                hidden = e;
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
    
}
