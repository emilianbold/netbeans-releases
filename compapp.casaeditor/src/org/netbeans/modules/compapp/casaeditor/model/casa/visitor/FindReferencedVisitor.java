/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.model.casa.visitor;

import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.ReferenceableCasaComponent;


/**
 *
 * @author Nam Nguyen
 */
public class FindReferencedVisitor<T extends ReferenceableCasaComponent> 
        extends CasaComponentVisitor.Default {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private Casa root;
    
    /** Creates a new instance of FindReferencedVisitor */
    public FindReferencedVisitor(Casa root) {
        this.root = root;
    }
    
    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }
    
    private void checkReference(ReferenceableCasaComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName() != null &&
                 c.getName().equals(localName)) {
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(CasaComponent c) {
        for (CasaComponent child : c.getChildren()) {
           if (referenced != null) { // before start each visit
                return;
           }
            child.accept(this);
        }
    }
    
    @Override
    public void visit(CasaEndpoints endpoints) { // TMP FIXME
        visitChildren(endpoints);
    }

    @Override
    public void visit(CasaEndpoint target) {
        checkReference(target, true);        
    }    
}
