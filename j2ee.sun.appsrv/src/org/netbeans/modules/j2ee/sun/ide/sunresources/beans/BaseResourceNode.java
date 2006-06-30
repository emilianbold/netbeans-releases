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
/*
 * BaseResourceNode.java
 *
 * Created on August 18, 2005, 9:30 AM
 *
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import org.openide.nodes.Children;
import org.openide.loaders.DataNode;
import org.openide.actions.PropertiesAction;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;

import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;

/**
 *
 * @author Nitya Doraisamy
 */
public abstract class BaseResourceNode extends DataNode implements java.beans.PropertyChangeListener {
    
                
    /** Creates a new instance of BaseResourceNode */
    public BaseResourceNode(SunResourceDataObject obj) {
        super(obj, Children.LEAF);
    }
    
    public javax.swing.Action getPreferredAction(){
        return SystemAction.get(PropertiesAction.class);
    }
    
    protected SunResourceDataObject getSunResourceDataObject() {
        return (SunResourceDataObject)getDataObject();
    }
    
    abstract public Resources getBeanGraph();
    
    abstract public void propertyChange(java.beans.PropertyChangeEvent evt);
    
}
