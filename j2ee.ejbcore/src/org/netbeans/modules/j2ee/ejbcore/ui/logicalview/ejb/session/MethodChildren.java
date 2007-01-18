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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.util.Collection;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */

//TODO: RETOUCHE

public class MethodChildren /*extends ComponentMethodModel */{
//    private ComponentMethodViewStrategy mvs;
    private final SessionMethodController controller;
    private final boolean local;
    
    public MethodChildren(SessionMethodController smc, Collection interfaces, boolean local) {
//        super(smc.getBeanClass(), interfaces);
        controller = smc;
        this.local = local;
//        mvs = new SessionStrategy();
    }

    protected Collection getInterfaces() {
        if (local) {
            return controller.getLocalInterfaces();
        } else {
            return controller.getRemoteInterfaces();
        }
    }
    
//    public ComponentMethodViewStrategy createViewStrategy() {
//        return mvs;
//    }
//
//    private class SessionStrategy implements ComponentMethodViewStrategy {
//        
//        public void deleteImplMethod(Method me, JavaClass implClass, Collection interfaces) {
//            controller.delete(me, local);
//        }
//
//        public Image getBadge(Method me, Collection interfaces) {
//            return null;
//        }
//
//        public Image getIcon(Method me, java.util.Collection interfaces) {
//            IconVisitor iv = new IconVisitor();
//            return Utilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
//        }
//
//        public OpenCookie getOpenCookie(Method me, JavaClass implClass, Collection interfaces) {
//            Method impl = controller.getPrimaryImplementation(me);
//            return (OpenCookie) JMIUtils.getCookie(impl, OpenCookie.class);
//        }
//        
//    }
}
