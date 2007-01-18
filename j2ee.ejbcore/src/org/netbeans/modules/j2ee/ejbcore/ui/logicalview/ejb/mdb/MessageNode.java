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


package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.mdb;

import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.actions.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.DeleteEJBDialog;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.openide.cookies.OpenCookie;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Chris Webster
 * @author Ludovic Champenois
 * @author Martin Adamek
 */
public class MessageNode extends AbstractNode implements OpenCookie {
    
    private final PropertyChangeListener nameChangeListener;
    private final EjbViewController controller;
    
    public MessageNode(MessageDriven model, EjbJar module, ClassPath srcPath) {
        this(new InstanceContent(), model, module, srcPath);
    }
    
    private MessageNode(InstanceContent content, MessageDriven model, EjbJar module, ClassPath srcPath) {
        super(Children.LEAF, new AbstractLookup(content));
        setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/ejb/mdb/MessageNodeIcon.gif");
        setName(model.getEjbName()+"");
        controller = new EjbViewController(model, module, srcPath);
        setDisplayName();
        nameChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                setDisplayName();
            }
        };
        model.addPropertyChangeListener(
            WeakListeners.propertyChange(nameChangeListener,model));
        content.add(this);
        content.add(controller.getBeanClass());
        if (controller.getBeanDo() != null) {
            content.add(controller.getBeanDo());
        }
    }
    
    private void setDisplayName() {
        setDisplayName(controller.getDisplayName());
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            null,
            SystemAction.get(DeleteAction.class)
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // TODO
        // return new HelpCtx(SessionNode.class);
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws java.io.IOException {
        String deleteOptions = DeleteEJBDialog.open(controller.getDisplayName());
        if (!deleteOptions.equals(DeleteEJBDialog.DELETE_NOTHING)) {
            if (deleteOptions.equals(DeleteEJBDialog.DELETE_ONLY_DD)) {
                controller.delete(false);
            } else {
                controller.delete(true);
            }
        }
    }
    
    public boolean canCopy() {
        return false;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public void open() {
        DataObject dataObject = controller.getBeanDo();
        if (dataObject != null) {
            OpenCookie cookie = (OpenCookie) dataObject.getCookie(OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
    }

    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }

    /**
     * Adds possibility to display custom delete dialog
     */
    public Object getValue(String attributeName) {
        Object retValue;
        if (attributeName.equals("customDelete")) {
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }

}
