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

import java.lang.reflect.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 */
public class AddSelectMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddSelectMethodStrategy() {
        super(NbBundle.getMessage(AddSelectMethodAction.class, "LBL_AddSelectMethodAction"));
    }
    
    public AddSelectMethodStrategy(String name) {
        super(name);
    }
    
    protected MethodType getPrototypeMethod(TypeElement jc) {
        //TODO: RETOUCHE
        return null;
//        Method method = JMIUtils.createMethod(jc);
//        method.setName("ejbSelectBy"); //NOI18N
//        method.setType(JMIUtils.resolveType("int"));
//        method.setModifiers(Modifier.PUBLIC|Modifier.ABSTRACT);
//        JMIUtils.addException(method, "javax.ejb.FinderException");
//        return new MethodType.SelectMethodType(method);
    }
    
//    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
//        return MethodCollectorFactory.selectCollector(pType.getMethodElement(), JMIUtils.getMethods(c.getBeanClass()));
//    }
//
//    protected void okButtonPressed(MethodType pType, MethodCustomizer mc, Method prototypeMethod, EjbMethodController c, JavaClass jc) throws java.io.IOException {
//	ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
//	try {
//	    handle.start(100);
//	    EntityMethodController emc = (EntityMethodController) c;
//	    emc.addSelectMethod(prototypeMethod,mc.getEjbQL(), getDDFile(jc));
//	    handle.progress(99);
//	} finally {
//	    handle.finish();
//	}
//    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_SELECT;
    }
}
