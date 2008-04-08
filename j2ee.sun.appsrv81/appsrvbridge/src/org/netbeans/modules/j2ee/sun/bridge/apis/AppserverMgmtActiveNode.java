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
package org.netbeans.modules.j2ee.sun.bridge.apis;

import java.util.Iterator;
import java.util.List;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.netbeans.modules.j2ee.sun.util.PropertySupportFactory;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;



/**
 * The parent class for all appserver plugin management leaf nodes. All leaf 
 * nodes for the NetBeans runtime tab J2EE plugin must extend this class for 
 * effectively communicating via AMX API.
 */
public abstract class AppserverMgmtActiveNode extends AppserverMgmtNode {
  
    private Controller controller;
    private PropertySupportFactory propSupportFactory = 
                PropertySupportFactory.getInstance();
    
    /**
     * Abstract constructor for an AppserverLeafNode called by subclass.
     *
     * @param nodeType The type of leaf node to construct (e.g. JVM, etc.)
     */
    public AppserverMgmtActiveNode(Children children, String nodeType) {
        super(children, nodeType);
    }

    
    /**
     * Abstract constructor for an AppserverLeafNode called by subclass.
     *
     * @param nodeType The type of leaf node to construct (e.g. JVM, etc.)
     */
    public AppserverMgmtActiveNode(Children children, Controller controller, 
            String nodeType) {
        super(children, nodeType);
        this.controller = controller;
    }
    
    
    /**
     * Abstract constructor for an AppserverLeafNode called by subclass.
     *
     * @param nodeType The type of leaf node to construct (e.g. JVM, etc.)
     */
    public AppserverMgmtActiveNode(final Children children, 
            final AppserverMgmtController controller, 
            final String nodeType) {
        super(controller, children, nodeType);
    }
    
    
    /**
     * Returns the controller object for interfacing with AMX apis.
     *
     * @return The controller.
     */
    protected final Controller getController() {
        return controller;
    }
    
    /**
     * Creates a properties Sheet for viewing when a user chooses the option
     * from the right-click menu.
     *
     * @returns the Sheet to display when Properties is chosen by the user.
     */
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
            
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            props.put(createPropertySupportArray(getSheetProperties()));
            return sheet;
        }catch(NullPointerException ex){
            return sheet;
        } catch(RuntimeException rex) {
            return sheet;
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    
    /**
     * Creates a PropertySupport array from a map of component properties.
     *
     * @param properties The properties of the component.
     * @return An array of PropertySupport objects.
     */
    PropertySupport[] createPropertySupportArray(final java.util.Map attrMap) {
        PropertySupport[] supports = new PropertySupport[attrMap.size()];
        int i = 0;
        for(Iterator itr = attrMap.keySet().iterator(); itr.hasNext(); ) {
            Attribute attr = (Attribute) itr.next();
            MBeanAttributeInfo info = (MBeanAttributeInfo) attrMap.get(attr);
            supports[i] = 
                propSupportFactory.getPropertySupport(this, attr, info);
            i++;
        }
        return supports; 
    }

    
    /**
     * Return a list of all those String properties to be ignored for display.
     *
     * @return A java.util.List of all String properties to ignore in the
     *         display (Property editors).
     */
    protected List getPropertiesToIgnore() {
        return NodeTypes.getNodeProperties(getNodeType());
    }
    
    
    /**
     * Returns all the properties of the leaf node to disply in the properties
     * window (or Sheet). This must be overriden in order for the Sheet to be
     * processed.
     *
     * @returns a java.util.Map of all properties to be accessed from the Sheet.
     */
    abstract protected java.util.Map getSheetProperties();
    
    /**
     * Sets the property as an attribute to the underlying AMX mbeans. It 
     * usually will delegate to the controller object which is responsible for
     * finding the correct AMX mbean objectname in order to execute a 
     * JMX setAttribute.
     *
     * @param attrName The name of the property to be set.
     * @param value The value retrieved from the property sheet to be set in the
     *        backend.
     * @returns the updated Attribute accessed from the Sheet.
     */
    abstract public javax.management.Attribute setSheetProperty(String attrName, Object value);
    
    
}
