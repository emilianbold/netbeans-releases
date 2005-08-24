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
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.common.DDEditorNavigator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


public class CMPFieldNode extends AbstractNode implements PropertyChangeListener, OpenCookie {
    private CmpField field;
    private EntityMethodController controller;
    private static final String CMP_FIELD_ICON = "org/netbeans/modules/j2ee/ejbcore/resources/CMPFieldIcon.gif"; //NOI18N
    private FileObject ddFile;
    
    public CMPFieldNode(CmpField field, EntityMethodController controller, FileObject ddFile) {
        super(Children.LEAF);
        this.field = field;
        this.ddFile = ddFile;
        this.controller = controller;
        field.addPropertyChangeListener(WeakListeners.propertyChange(this, field));
    }
    
    public String getDisplayName(){
        return field.getFieldName();
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage(CMP_FIELD_ICON);
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
        try {
            DataObject ddFileDO = DataObject.find(ddFile);
            Object c = ddFileDO.getCookie(DDEditorNavigator.class);
            if (c != null) {
                ((DDEditorNavigator) c).showElement(field);
            }
        } catch (DataObjectNotFoundException donf) {
            // do nothing
        }
    }
}