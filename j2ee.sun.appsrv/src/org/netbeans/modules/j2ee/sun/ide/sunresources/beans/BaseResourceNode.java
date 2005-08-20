/*
 * BaseResourceNode.java
 *
 * Created on August 18, 2005, 9:30 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
