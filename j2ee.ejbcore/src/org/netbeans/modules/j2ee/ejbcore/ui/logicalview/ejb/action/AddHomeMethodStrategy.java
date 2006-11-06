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
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.util.NbBundle;


/**
 * Action that can always be invoked and work procedurally.
 * @author cwebster
 */
public class AddHomeMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddHomeMethodStrategy(String name) {
        super (name);
    }
    public AddHomeMethodStrategy () {
        super(NbBundle.getMessage(AddHomeMethodAction.class, "LBL_AddHomeMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(TypeElement jc) {
        //TODO: RETOUCHE
        return null;
//        Method me = JMIUtils.createMethod(jc);
//        me.setName("homeMethod"); //NOI18N
//        return new MethodType.HomeMethodType(me);
    }

//    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
//        Method[] methodElements = Utils.getMethods(c, true, false);
//	MethodsNode methodsNode = getMethodsNode();
//	boolean local = methodsNode == null ? c.hasLocal() : (methodsNode.isLocal() && c.hasLocal());
//	boolean remote = methodsNode == null ? c.hasRemote() : (!methodsNode.isLocal() && c.hasRemote());
//        return MethodCollectorFactory.homeCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), methodElements, remote, local);
//    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_HOME;
    }
}
