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
/*
 * BaseResourceNode.java
 *
 * Created on August 18, 2005, 9:30 AM
 *
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import javax.swing.Action;

import org.openide.nodes.Children;
import org.openide.loaders.DataNode;

import org.openide.actions.DeleteAction;
import org.openide.actions.RenameAction;
import org.openide.actions.PropertiesAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.j2ee.sun.ide.sunresources.resourceactions.RegisterAction;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;

import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;

/**
 *
 * @author Nitya Doraisamy
 */
public abstract class BaseResourceNode extends DataNode implements java.beans.PropertyChangeListener {
    
    protected SunResourceDataObject sunResourceObj = null;
                
    /** Creates a new instance of BaseResourceNode */
    public BaseResourceNode(SunResourceDataObject obj) {
        super(obj, Children.LEAF);
        this.sunResourceObj = obj;
    }
    
    public javax.swing.Action getPreferredAction(){
        return SystemAction.get(PropertiesAction.class);
    }
    
    public Action[] getActions(boolean flag) {
        return new SystemAction[]{
            SystemAction.get(RegisterAction.class),
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }
    
    protected SunResourceDataObject getSunResourceDataObject() {
        return (SunResourceDataObject)getDataObject();
    }
    
    abstract public Resources getBeanGraph();
    
    abstract public void propertyChange(java.beans.PropertyChangeEvent evt);
    
}
