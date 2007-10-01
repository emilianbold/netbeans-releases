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
package org.netbeans.modules.java.hints.spi.support;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.hints.errors.SuppressWarningsFixer;
import org.netbeans.spi.editor.hints.Fix;

/** Factory for creating fixes, which add @SuppresWarning to given Element
 *
 * @author Petr Hrebejk
 */
public final class FixFactory {

    private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
    
    private FixFactory() {}
     
    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        decaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g. 
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element 
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */ 
    public static Fix createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {   
        
        if ( keys == null || keys.length == 0) {
            throw new IllegalArgumentException("key must not be null or empty"); // NOI18N
        }
        	
	while (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(treePath.getLeaf().getKind())) {
	    treePath = treePath.getParentPath();
	}
                
        if (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            return new SuppressWarningsFixer.FixImpl(TreePathHandle.create(treePath, compilationInfo), compilationInfo.getFileObject(), keys);
        }
        else {
            throw new IllegalArgumentException("Can't find Declaration for treePath " + treePath);
        }        
    }
    
}
