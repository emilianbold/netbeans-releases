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
package org.netbeans.api.java.source;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
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

    private CompilationInfo info;

    /** Creates a new instance of CommentUtilities */
    TypeUtilities(CompilationInfo info) {
        this.info = info;
    }

    /**Check if type t1 can be cast to t2.
     * 
     * @param t1 cast from type
     * @param t2 cast to type
     * @return true if and only if type t1 can be cast to t2 without a compile time error
     * 
     * @since 0.6
     */
    public boolean isCastable(TypeMirror t1, TypeMirror t2) {
        return Types.instance(info.getJavacTask().getContext()).isCastable((Type) t1, (Type) t2);
    }
    
}
