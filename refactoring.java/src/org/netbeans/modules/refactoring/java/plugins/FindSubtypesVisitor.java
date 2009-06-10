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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class FindSubtypesVisitor extends FindVisitor {

    private boolean recursive;
    public FindSubtypesVisitor(boolean recursive, WorkingCopy workingCopy) {
        super(workingCopy);
        this.recursive = recursive;
    }

    @Override
    public Tree visitClass(ClassTree node, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return super.visitClass(node, elementToFind);
        }
        if (recursive) {
            if (isSubtype(getCurrentPath(), elementToFind)) {
                addUsage(getCurrentPath());
            }
        } else {
            TypeElement el = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            Types types = workingCopy.getTypes();
            if (el.getSuperclass()!=null && types.isSameType(types.erasure(el.getSuperclass()), types.erasure(elementToFind.asType())) || containsType(el.getInterfaces(), elementToFind.asType())) {
                addUsage(getCurrentPath());
            } 
        }
        return super.visitClass(node, elementToFind);
    }
    
    private boolean containsType(List<? extends TypeMirror> list, TypeMirror t) {
        Types types = workingCopy.getTypes();
        t = types.erasure(t);
        for (TypeMirror m:list) {
            if (types.isSameType(t, types.erasure(m))) {
                return true;
            };
        }
        return false;
    }
    
    protected boolean isSubtype(TreePath t1, Element t2) {
        Types types = workingCopy.getTypes();
        Trees trees = workingCopy.getTrees();
        TypeMirror tm1 = trees.getTypeMirror(t1);
        if (tm1 == null) {
            return false;
        }
        tm1 = types.erasure(tm1);
        TypeMirror tm2 = types.erasure(t2.asType());

        return types.isSubtype(tm1, tm2) && !types.isSameType(tm1, tm2);
    }

}
