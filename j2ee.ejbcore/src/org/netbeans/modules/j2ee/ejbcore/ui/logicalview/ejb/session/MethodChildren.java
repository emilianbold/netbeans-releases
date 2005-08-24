/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.awt.Image;
import java.util.Collection;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.ComponentMethodModel;
import org.netbeans.modules.j2ee.common.ui.nodes.ComponentMethodViewStrategy;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.openide.cookies.OpenCookie;
import org.openide.util.Utilities;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class MethodChildren extends ComponentMethodModel {
    private ComponentMethodViewStrategy mvs;
    private SessionMethodController controller;
    private boolean local;
    
    public MethodChildren(SessionMethodController smc, Collection interfaces, boolean local) {
        super(smc.getBeanClass(), interfaces);
        controller = smc;
        this.local = local;
        mvs = new SessionStrategy();
    }

    protected Collection getInterfaces() {
        if (local) {
            return controller.getLocalInterfaces();
        } else {
            return controller.getRemoteInterfaces();
        }
    }
    
    public ComponentMethodViewStrategy createViewStrategy() {
        return mvs;
    }

    public boolean isLocal() {
        return local;
    }
    
    private class SessionStrategy implements ComponentMethodViewStrategy {
        
        public void deleteImplMethod(Method me, JavaClass implClass, Collection interfaces) {
            controller.delete(me, local);
        }

        public Image getBadge(Method me, Collection interfaces) {
            return null;
        }

        public Image getIcon(Method me, java.util.Collection interfaces) {
            IconVisitor iv = new IconVisitor();
            return Utilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
        }

        public OpenCookie getOpenCookie(Method me, JavaClass implClass, Collection interfaces) {
            Method impl = controller.getPrimaryImplementation(me);
            return (OpenCookie) JMIUtils.getCookie(impl, OpenCookie.class);
        }
        
    }
}
