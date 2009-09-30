/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.nodes.property;

import java.beans.PropertyEditor;
import java.util.logging.Level;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import org.netbeans.modules.sun.manager.jbi.editors.ApplicationVariablesEditor;
import org.netbeans.modules.sun.manager.jbi.editors.JBILogLevelEditor;
import org.netbeans.modules.sun.manager.jbi.nodes.AppserverJBIMgmtNode;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.netbeans.modules.sun.manager.jbi.nodes.JBINode;
import org.netbeans.modules.sun.manager.jbi.util.StackTraceUtil;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.nodes.PropertySupport;

/**
 * 
 * @author jqian
 */
public class JBIPropertySupportFactory {       
    
    
    /**
     * Returns the appropriate PropertySupport given the MBean Attribute and its
     * MBeanAttributeInfo.
     *
     * @param parent    an instance of AppserverJBIMgmtNode. This is necessary 
     *        for us to create the anonymous PropertySupport class that calls 
     *        the setProperty method implementation of a subclass of an instance
     *        of AppserverJBIMgmtNode. 
     * @param attr  an MBean Attribute object containing the name/value
     * @param info  the MBeanAttributeInfo for this Attribute
     * 
     * @return a PropertySupport for the attribute
     */
    @SuppressWarnings("unchecked")
    public static PropertySupport getPropertySupport(
            final PropertySheetOwner parent, 
            final Attribute attr,
            final MBeanAttributeInfo info) {
        
        PropertySupport support = null;
        
        Object attrValue = attr.getValue();
        if (attrValue instanceof Boolean) {
            support = new MyPropertySupport(parent, Boolean.class, attr, info); 
        } else if (attrValue instanceof Integer) {
            support = new MyPropertySupport(parent, Integer.class, attr, info);
        } else if (attrValue instanceof Level &&
                parent instanceof AppserverJBIMgmtNode) {  
            support = createLogLevelProperty((AppserverJBIMgmtNode)parent, attr, info);
        } else if (attrValue instanceof TabularData) {
            support = createTabularDataProperty((JBIComponentNode)parent, attr, info);  
        } else {  // default           
            support = new MyPropertySupport(parent, String.class, attr, info);
        }
        return support;
    }
    
    /**
     * Creates PorpertySupport for some TabularData. 
     */ 
    private static PropertySupport createTabularDataProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        
        PropertySupport support = null;
        
        String attrName = attr.getName();
        if (attrName.equals(JBIComponentNode.ENVIRONMENT_VARIABLES_NAME)) {
            support = createEnvironmentVariablesProperty(parent, attr, info);
        } else if (attrName.equals(JBIComponentNode.APPLICATION_VARIABLES_NAME)) {
            support = createApplicationVariablesProperty(parent, attr, info); 
        } else if (attrName.equals(JBIComponentNode.APPLICATION_CONFIGURATIONS_NAME)) {
            support = createApplicationConfigurationsProperty(parent, attr, info);
        }
        
        return support;
    }
    
    /**
     * Creates PorpertySupport for Environment Variables
     * (for backward compatibility).
     */
    private static PropertySupport createEnvironmentVariablesProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {

        return new MyPropertySupport<TabularDataSupport>(
                parent, TabularDataSupport.class, attr, info) {

            @Override
            public PropertyEditor getPropertyEditor(){     
                TabularType tabularType = null;
                TabularData tabularData = (TabularData)attr.getValue();
                if (tabularData != null) {
                    tabularType = tabularData.getTabularType();
                }
                return new ApplicationVariablesEditor(false, tabularType, null, // FIXME
                        info.isWritable());  
            }
        };
    }
    
    /**
     * Creates PorpertySupport for Application Variables. 
     */
    private static PropertySupport createApplicationVariablesProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        return new ApplicationVariablesPropertySupport(parent, attr, info);
    }
    
    /**
     * Creates PorpertySupport for Application Configurations. 
     */ 
    private static PropertySupport createApplicationConfigurationsProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        return new ApplicationConfigurationsPropertySupport(
                parent, attr, info, parent.getName());
    }
  
    public static PropertySupport createLogLevelProperty(
            final AppserverJBIMgmtNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        
        assert (parent instanceof JBIComponentNode) || (parent instanceof JBINode);
        
        return new MyPropertySupport<Level>(parent, Level.class, attr, info) {
                      
            @Override
            public void setValue(Level val){
                try {
                    if (parent instanceof JBIComponentNode) {
                        attribute = ((JBIComponentNode)parent).
                                setLoggerSheetProperty(attr.getName(), val);
                    } else {
                        attribute = ((JBINode)parent).
                                setLoggerSheetProperty(attr.getName(), val);                        
                    }
                } catch (RuntimeException rex) {
                    rex.printStackTrace();
                }
            }
            
            @Override
            public PropertyEditor getPropertyEditor(){
                return new JBILogLevelEditor();
            }
        };
    }    
}                
    
class MyPropertySupport<T> extends PropertySupport<T> {

    protected PropertySheetOwner parent;
    protected Attribute attribute;

    MyPropertySupport(PropertySheetOwner parent,                
            Class<T> type, 
            Attribute attr, 
            MBeanAttributeInfo info) {
        super(attr.getName(), type, info.getName(), 
                Utils.getTooltip(info.getDescription()), 
                info.isReadable(), info.isWritable());
        
        // Doesn't work yet. #124256
//        // Use non-HTML version in the property sheet's description area.
//        setValue("nodeDescription", info.getDescription()); // NOI18N 
        
        this.attribute = attr;
        this.parent = parent;
    }

    @SuppressWarnings(value = "unchecked")
    public T getValue() {
        return (T) attribute.getValue();
    }

    public void setValue(T attrValue) {
        // #156551
        if (!canWrite() && StackTraceUtil.isCalledBy(
                "org.openide.explorer.propertysheet.PropertyDialogManager", // NOI18N
                "cancelValue")) { // NOI18N
            return;
        }

        try {
            attribute = parent.setSheetProperty(getName(), attrValue);
        } catch (RuntimeException rex) {
            rex.printStackTrace();
        }
    }
}
