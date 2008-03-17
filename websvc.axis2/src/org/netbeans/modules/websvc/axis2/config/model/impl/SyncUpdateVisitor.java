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
package org.netbeans.modules.websvc.axis2.config.model.impl;

import org.netbeans.modules.websvc.axis2.config.model.Axis2;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Component;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Visitor;
import org.netbeans.modules.websvc.axis2.config.model.GenerateWsdl;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;


public class SyncUpdateVisitor extends Axis2Visitor.Default implements ComponentUpdater<Axis2Component> {
    private Axis2Component target;
    private Operation operation;
    private int index;
    
    public SyncUpdateVisitor() {
    }
    
    public void update(Axis2Component target, Axis2Component child, Operation operation) {
        update(target, child, -1 , operation);
    }
    
    public void update(Axis2Component target, Axis2Component child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }
    
    private void insert(String propertyName, Axis2Component component) {
        ((Axis2ComponentImpl)target).insertAtIndex(propertyName, component, index);
    }
    
    private void remove(String propertyName, Axis2Component component) {
        ((Axis2ComponentImpl)target).removeChild(propertyName, component);
    }
    
    public void visit(GenerateWsdl generateWsdl) {
        if (target instanceof Service) {
            if (operation == Operation.ADD) {
                insert(Service.GENERATE_WSDL_PROP, generateWsdl);
            } else {
                remove(Service.GENERATE_WSDL_PROP, generateWsdl);
            }
        }
    }
    
    public void visit(Service service) {
        if (target instanceof Axis2) {
            if (operation == Operation.ADD) {
                insert(Axis2.SERVICE_PROP, service);
            } else {
                remove(Axis2.SERVICE_PROP, service);
            }
        }
    }
    
}
