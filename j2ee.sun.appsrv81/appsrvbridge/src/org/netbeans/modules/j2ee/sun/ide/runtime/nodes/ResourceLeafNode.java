/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.sun.ide.runtime.nodes;

import javax.swing.Action;

import java.util.List;
import java.util.Arrays;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtLeafNode;
import org.netbeans.modules.j2ee.sun.bridge.apis.Removable;

import org.openide.actions.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.j2ee.sun.ide.runtime.actions.DeleteResourceAction;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.netbeans.modules.j2ee.sun.util.NodeTypes;        


/**
 * The parent class for all appserver plugin management leaf nodes. All leaf 
 * nodes for the NetBeans runtime tab J2EE plugin must extend this class for 
 * effectively communicating via AMX API.
 */
public abstract class ResourceLeafNode extends AppserverMgmtLeafNode 
        implements Removable {
  
    private String resourceName;
    
    
    /**
     * Abstract constructor for an AppserverLeafNode called by subclass.
     *
     * @param nodeType The type of leaf node to construct (e.g. JVM, etc.)
     * @param resName The name of the resource.
     */
    public ResourceLeafNode(final AppserverMgmtController controller, 
            final String nodeType, final String resName) {
        super(controller, nodeType);
        this.resourceName = resName;
        setDisplayName(resName);
    }
    
    
    
  
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on a node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(DeleteResourceAction.class),
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    
    /**
     *
     */
    protected String getResourceName() {
        return this.resourceName;
    }
    
    
    /**
     *
     *
     */
    abstract public void remove();
    
    protected List getPropertiesToIgnore() {
        return NodeTypes.getNodeProperties(NodeTypes.SERVER_RESOURCE);
    }
    
    /**
     * Sets the property as an attribute to the underlying AMX mbeans. It 
     * usually will delegate to the controller object which is responsible for
     * finding the correct AMX mbean objectname in order to execute a 
     * JMX setAttribute.
     *
     * @param props Object array containing updated properties as Attributes
     * @param oldProps set of old properties
     */
    public void updateExtraProperty(Object[] props, java.util.Map oldProps) {
        getAppserverMgmtController().
            updateResourceExtraProperty(getResourceName(), getNodeType(), props, oldProps);
    }
    
}
