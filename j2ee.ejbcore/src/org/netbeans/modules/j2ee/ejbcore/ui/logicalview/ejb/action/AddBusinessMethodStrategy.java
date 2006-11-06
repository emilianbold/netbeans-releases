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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;
//import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
//import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 * @author Martin Adamek
 */
public class AddBusinessMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddBusinessMethodStrategy(String name) {
        super (name);
    }
    public AddBusinessMethodStrategy () {
        super (NbBundle.getMessage(AddBusinessMethodAction.class, "LBL_AddBusinessMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(TypeElement jc) {
        //TODO: RETOUCHE
        return null;
//        Method method = JMIUtils.createMethod(jc);
//        method.setName("businessMethod");
//        return new MethodType.BusinessMethodType(method);
    }

    //TODO: RETOUCHE
//    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
//	MethodsNode methodsNode = getMethodsNode();
//	boolean local = methodsNode == null ? c.hasLocal() : (methodsNode.isLocal() && c.hasLocal());
//	boolean remote = methodsNode == null ? c.hasRemote() : (!methodsNode.isLocal() && c.hasRemote());
//        return MethodCollectorFactory.businessCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), JMIUtils.getMethods(c.getBeanClass()), remote, local);
//    }

    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_BUSINESS;
    }

}
