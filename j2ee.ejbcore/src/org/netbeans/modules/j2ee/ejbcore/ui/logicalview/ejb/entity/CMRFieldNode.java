/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import javax.swing.Action;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.WeakListeners;
import java.io.IOException;
import java.util.List;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;


public class CMRFieldNode extends AbstractNode implements PropertyChangeListener, OpenCookie {
    private CmrField field;
    private EntityMethodController controller;
    private static final String CMR_FIELD_ICON = "org/netbeans/modules/j2ee/ejbcore/resources/CMRFieldIcon.gif"; //NOI18N
    private FileObject ddFile;
    
    public CMRFieldNode(CmrField field, EntityMethodController controller, FileObject ddFile) {
        super(Children.LEAF);
        this.field = field;
        this.ddFile = ddFile;
        this.controller = controller;
        field.addPropertyChangeListener(WeakListeners.propertyChange(this, field));
    }
    
    public String getDisplayName(){
        return field.getCmrFieldName();
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage(CMR_FIELD_ICON);
    }
    
    public boolean canDestroy(){
        return true;
    }
    
    public void destroy() throws IOException{
        controller.deleteField(field, ddFile);
        super.destroy();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireDisplayNameChange(null,null);
    }
    
    public Node.Cookie getCookie(Class type) {
        if(type == OpenCookie.class) {
            return this;
        }
        return super.getCookie(type);
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
                    null,
                    SystemAction.get(DeleteAction.class),
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    //implementation of OpenCookie
    public void open() {
        List methods = controller.getMethods(field);
        if (!methods.isEmpty()) {
            Method getMethod = (Method) methods.get(0);
            OpenCookie cookie = (OpenCookie) JMIUtils.getCookie(getMethod, OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
    }
}