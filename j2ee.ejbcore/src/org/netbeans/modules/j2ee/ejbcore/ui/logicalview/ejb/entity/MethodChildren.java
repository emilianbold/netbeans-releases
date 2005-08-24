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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.awt.Image;
import java.util.Collection;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.ComponentMethodModel;
import org.netbeans.modules.j2ee.common.ui.nodes.ComponentMethodViewStrategy;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.common.DDEditorNavigator;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.IconVisitor;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class MethodChildren extends ComponentMethodModel {
    private ComponentMethodViewStrategy mvs;
    private EntityMethodController controller;
    private boolean local;
    private FileObject ddFile;
    private Entity entity;
    
    public MethodChildren(EntityMethodController smc, Entity model, Collection interfaces, boolean local, FileObject ddFile) {
        super(smc.getBeanClass(), interfaces);
        controller = smc;
        this.local = local;
        this.ddFile = ddFile;
        this.entity = model;
        mvs = new EntityStrategy();
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
    
    private class EntityStrategy implements ComponentMethodViewStrategy {
        
        public void deleteImplMethod(Method me, JavaClass implClass, Collection interfaces) throws java.io.IOException {
            String methodName = me.getName();
            if (methodName.startsWith("find") ||     //NOI18N
                methodName.startsWith("ejbSelect")) {   //NOI18N
                controller.deleteQueryMapping(me, ddFile);
            }
            controller.delete(me,local);
        }

        public Image getBadge(Method me, Collection interfaces) {
            return null;
        }

        public Image getIcon(Method me, java.util.Collection interfaces) {
            IconVisitor iv = new IconVisitor();
            return Utilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
        }

        public OpenCookie getOpenCookie(Method me, JavaClass implClass, Collection interfaces) {
            if (controller.getMethodTypeFromInterface(me) instanceof MethodType.FinderMethodType) {
                return new FinderOpenCookie(me);
            }
            Method impl = controller.getPrimaryImplementation(me);
            return (OpenCookie) JMIUtils.getCookie(impl, OpenCookie.class);
        }
    }

    private class FinderOpenCookie implements OpenCookie {
        
        private Method me;
        
        public FinderOpenCookie(Method me) {
            this.me = me;
        }
        
        public void open() {
            try {
                DataObject ddFileDO = DataObject.find(ddFile);
                Object c = ddFileDO.getCookie(DDEditorNavigator.class);
                if (c != null) {
                    Query[] queries = entity.getQuery();
                    for (int i = 0; i < queries.length; i++) {
                        String methodName = queries[i].getQueryMethod().getMethodName();
                        if (methodName.equals(me.getName())) {
                            ((DDEditorNavigator) c).showElement(queries[i]);
                        }
                    }
                }
            } catch (DataObjectNotFoundException donf) {
                // do nothing
            }
        }
    }
    
}
