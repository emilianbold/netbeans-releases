/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom.visitor;

import org.netbeans.modules.maven.model.pom.*;

/**
 *
 * @author mkleint
 */
public class FindReferencedVisitor<T extends ReferenceablePOMComponent> 
        extends DefaultVisitor {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private POMComponent root;
    
    public FindReferencedVisitor(POMComponent root) {
        this.root = root;
    }

    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }
    
    public T find(String[] localNames, Class<T>[] types) {
        assert localNames.length == types.length;
        
        for (int index = 0; index < localNames.length; index++) {
            this.type = types[index];
            this.localName = localNames[index];
            visitChildren(root);
            
            root = referenced;
            if (index < localNames.length - 1) {
                referenced = null;
            }
        }
        return referenced;
    }

    private void checkReference(ReferenceablePOMComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName() != null &&
                 c.getName().equals(localName) || "*".equals(localName)) { // NOI18N
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(POMComponent c) {
        for (POMComponent child : c.getChildren()) {
            if (referenced != null) { // before start each visit
                return;
            }
            child.accept(this);
        }
    }
   
}
