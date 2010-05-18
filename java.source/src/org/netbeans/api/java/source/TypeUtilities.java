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
package org.netbeans.api.java.source;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import java.util.List;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**Various utilities related to the {@link TypeMirror}s.
 * 
 * @see javax.lang.model.util.Types
 *
 * @since 0.6
 * 
 * @author Jan Lahoda
 */
public final class TypeUtilities {

    private final CompilationInfo info;

    /** Creates a new instance of CommentUtilities */
    TypeUtilities(final CompilationInfo info) {
        assert info != null;
        this.info = info;
    }

    /**Check if type t1 can be cast to t2.
     * 
     * @param t1 cast from type
     * @param t2 cast to type
     * @return true if and only if type t1 can be cast to t2 without a compile time error
     * @throws IllegalArgumentException if the 't1' is of {@link TypeKind#EXECUTABLE EXACUTABLE},
     *         {@link TypeKind#PACKAGE PACKAGE}, {@link TypeKind#NONE NONE}, or {@link TypeKind#OTHER OTHER} kind
     * 
     * @since 0.6
     */
    public boolean isCastable(TypeMirror t1, TypeMirror t2) {
        switch(t1.getKind()) {
            case EXECUTABLE:
            case PACKAGE:
            case NONE:
            case OTHER:
                throw new IllegalArgumentException();
            default:
                return Types.instance(info.impl.getJavacTask().getContext()).isCastable((Type) t1, (Type) t2);
        }
    }
    
    /**
     * Substitute all occurrences of a type in 'from' with the corresponding type
     * in 'to' in 'type'. 'from' and 'to' lists have to be of the same length.
     * 
     * @param type in which the types should be substituted
     * @param from types to substitute
     * @param to   substitute to types
     * @return type corresponding to input 'type' with all references to any type from 'from'
     *         replaced with a corresponding type from 'to'
     * @throws IllegalArgumentException if the 'from' and 'to' lists are not of the same length
     * @since 0.36
     */
    public TypeMirror substitute(TypeMirror type, List<? extends TypeMirror> from, List<? extends TypeMirror> to) {
        if (from.size() != to.size()) {
            throw new IllegalArgumentException();
        }
        com.sun.tools.javac.util.List<Type> l1 = com.sun.tools.javac.util.List.nil();
        for (TypeMirror typeMirror : from)
            l1 = l1.prepend((Type)typeMirror);
        com.sun.tools.javac.util.List<Type> l2 = com.sun.tools.javac.util.List.nil();
        for (TypeMirror typeMirror : to)
            l2 = l2.prepend((Type)typeMirror);
        return Types.instance(info.impl.getJavacTask().getContext()).subst((Type)type, l1, l2);
    }
    
}
