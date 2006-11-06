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
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.util.NbBundle;


/**
 * @author Pavel Buzek
 */
public class AddFinderMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddFinderMethodStrategy (String name) {
        super(name);
    }
    public AddFinderMethodStrategy () {
        super (NbBundle.getMessage(AddFinderMethodAction.class, "LBL_AddFinderMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(TypeElement jc) {
        return getFinderPrototypeMethod(jc);
    }

    public static MethodType getFinderPrototypeMethod(TypeElement jc) {
        //TODO: RETOUCHE
        return null;
//        Method me = JMIUtils.createMethod(jc);
//        me.setName("findBy"); //NOI18N
//        JMIUtils.addException(me, "javax.ejb.FinderException"); //NOI18N
//        return new MethodType.FinderMethodType(me);
    }

//    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
//        return createFinderDialog(c, pType);
//    }
//
//    public static MethodCustomizer createFinderDialog(EjbMethodController c, MethodType pType) {
//        boolean javaImpl = c.hasJavaImplementation(pType);
//        Method[] methodElements = Utils.getMethods(c, true, false);
//	MethodsNode methodsNode = getMethodsNode();
//	boolean local = methodsNode == null ? c.hasLocal() : (methodsNode.isLocal() && c.hasLocal());
//	boolean remote = methodsNode == null ? c.hasRemote() : (!methodsNode.isLocal() && c.hasRemote());
//        return MethodCollectorFactory.finderCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), !javaImpl, methodElements, remote, local);
//    }
//
//    protected Type remoteReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
//        String fullName = isOneReturn?c.getRemote():Collection.class.getName();
//        return JMIUtils.resolveType(fullName);
//    }
//
//    protected Type localReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
//        String fullName = isOneReturn?c.getLocal():Collection.class.getName();
//        return JMIUtils.resolveType(fullName);
//    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_FINDER;
    }
    
}
