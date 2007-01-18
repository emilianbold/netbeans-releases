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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.WeakListeners;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.common.DDEditorNavigator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


public class CMPFieldNode extends AbstractNode implements PropertyChangeListener, OpenCookie {
    private CmpField field;
    private EntityMethodController controller;
    private static final String CMP_FIELD_ICON = "org/netbeans/modules/j2ee/ejbcore/resources/CMPFieldIcon.gif"; //NOI18N
    private FileObject ddFile;
    
    public CMPFieldNode(CmpField field, EntityMethodController controller, FileObject ddFile) {
        this(field, controller, ddFile, new InstanceContent());
    }

    private CMPFieldNode(CmpField field, EntityMethodController controller, FileObject ddFile, InstanceContent instanceContent) {
        super(Children.LEAF, new AbstractLookup(instanceContent));
        instanceContent.add(this); // for enabling Open action
        
        //TODO: RETOUCHE
//        try {
//            ic.add(DataObject.find(JavaModel.getFileObject(controller.getBeanClass().getResource()))); // for enabling SafeDelete action
//        } catch (DataObjectNotFoundException ex) {
//            // ignore
//        }
//        Method getterMethod = controller.getGetterMethod(controller.getBeanClass(), field.getFieldName());
//        if (getterMethod != null) {
//            ic.add(getterMethod); // for SafeDelete refactoring to find Method element
//        }
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
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            null,
        //TODO: RETOUCHE
//            RefactoringActionsFactory.safeDeleteAction().createContextAwareInstance(Utilities.actionsGlobalContext())
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    //implementation of OpenCookie
    public void open() {
        try {
            DataObject ddFileDO = DataObject.find(ddFile);
            Object cookie = ddFileDO.getCookie(DDEditorNavigator.class);
            if (cookie != null) {
                ((DDEditorNavigator) cookie).showElement(field);
            }
        } catch (DataObjectNotFoundException donf) {
            ErrorManager.getDefault().notify(donf);
        }
    }
    
}
