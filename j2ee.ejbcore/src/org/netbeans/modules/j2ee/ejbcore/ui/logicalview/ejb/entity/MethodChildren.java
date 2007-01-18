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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.util.Collection;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */

//TODO: RETOUCHE

public class MethodChildren /*extends ComponentMethodModel*/ {
//    private ComponentMethodViewStrategy mvs;
    private final EntityMethodController controller;
    private final boolean local;
    private final FileObject ddFile;
    private final Entity entity;
    
    public MethodChildren(EntityMethodController smc, Entity model, Collection interfaces, boolean local, FileObject ddFile) {
//        super(smc.getBeanClass(), interfaces);
        controller = smc;
        this.local = local;
        this.ddFile = ddFile;
        this.entity = model;
//        mvs = new EntityStrategy();
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
//    private class EntityStrategy implements ComponentMethodViewStrategy {
//        
//        public void deleteImplMethod(Method me, JavaClass implClass, Collection interfaces) throws java.io.IOException {
//            String methodName = me.getName();
//            if (methodName.startsWith("find") ||     //NOI18N
//                methodName.startsWith("ejbSelect")) {   //NOI18N
//                controller.deleteQueryMapping(me, ddFile);
//            }
//            controller.delete(me,local);
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
//            if (controller.getMethodTypeFromInterface(me).getKind() == MethodType.Kind.FINDER) {
//                return new FinderOpenCookie(me);
//            }
//            Method impl = controller.getPrimaryImplementation(me);
//            return (OpenCookie) JMIUtils.getCookie(impl, OpenCookie.class);
//        }
//    }
//
//    private class FinderOpenCookie implements OpenCookie {
//        
//        private Method me;
//        
//        public FinderOpenCookie(Method me) {
//            this.me = me;
//        }
//        
//        public void open() {
//            try {
//                DataObject ddFileDO = DataObject.find(ddFile);
//                Object c = ddFileDO.getCookie(DDEditorNavigator.class);
//                if (c != null) {
//                    Query[] queries = entity.getQuery();
//                    for (int i = 0; i < queries.length; i++) {
//                        String methodName = queries[i].getQueryMethod().getMethodName();
//                        if (methodName.equals(me.getName())) {
//                            ((DDEditorNavigator) c).showElement(queries[i]);
//                        }
//                    }
//                }
//            } catch (DataObjectNotFoundException donf) {
//                // do nothing
//            }
//        }
//    }
    
}
