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
import org.netbeans.modules.sun.manager.jbi.editors.EnvironmentVariablesEditor;
import org.netbeans.modules.sun.manager.jbi.editors.JBILogLevelEditor;
import org.netbeans.modules.sun.manager.jbi.editors.SimpleTabularDataEditor;
import org.netbeans.modules.sun.manager.jbi.nodes.AppserverJBIMgmtNode;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
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
            final AppserverJBIMgmtNode parent, 
            final Attribute attr,
            final MBeanAttributeInfo info) {
        
        PropertySupport support = null;
        
        Object attrValue = attr.getValue();
        if (attrValue instanceof Boolean) {
            support = new MyPropertySupport(parent, Boolean.class, attr, info); 
        } else if (attrValue instanceof Integer) {
            support = new MyPropertySupport(parent, Integer.class, attr, info);
        } else if (attrValue instanceof Level) {  
            support = createLogLevelProperty((JBIComponentNode)parent, attr, info);
        } else if (attrValue instanceof TabularData) { 
            TabularDataSupport tabularData = (TabularDataSupport) attrValue;
            TabularType tabularType = tabularData.getTabularType();
            int columnCount = tabularType.getRowType().keySet().size();
            if (columnCount == 3) { // new typed environment variables
                support = createTabularDataProperty(
                        (JBIComponentNode)parent, attr, info, 
                        EnvironmentVariablesEditor.class);
            } else {  // untyped environment variables (for backward compatibility)
                support = createTabularDataProperty(
                        (JBIComponentNode)parent, attr, info, 
                        SimpleTabularDataEditor.class);
            }
        } else {  // default           
            support = new MyPropertySupport(parent, String.class, attr, info);
        }
        return support;
    }
    
    private static PropertySupport createTabularDataProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info, 
            final Class editorClass) {

        return new MyPropertySupport<TabularDataSupport>(
                parent, TabularDataSupport.class, attr, info) {

            public PropertyEditor getPropertyEditor(){
                try {
                    return (PropertyEditor) editorClass.newInstance(); 
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
       
    public static PropertySupport createLogLevelProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        
        return new MyPropertySupport<Level>(parent, Level.class, attr, info) {
                      
            public void setValue(Level val){
                try {
                    attribute = parent.setLoggerSheetProperty(attr.getName(), val);
                } catch (RuntimeException rex) {
                    rex.printStackTrace();
                }
            }
            
            public PropertyEditor getPropertyEditor(){
                return new JBILogLevelEditor();
            }
        };
    }
}                
    
class MyPropertySupport<T> extends PropertySupport<T> {

    private AppserverJBIMgmtNode parent;
    protected Attribute attribute;

    MyPropertySupport(AppserverJBIMgmtNode parent,                
            Class<T> type, 
            Attribute attr, 
            MBeanAttributeInfo info) {
        super(attr.getName(), type, info.getName(), info.getDescription(), 
                info.isReadable(), info.isWritable());
        this.attribute = attr;
        this.parent = parent;
    }


    @SuppressWarnings(value = "unchecked")
    public T getValue() {
        return (T) attribute.getValue();
    }

    public void setValue(T attrValue) {
        try {
            attribute = parent.setSheetProperty(getName(), attrValue);
        } catch (RuntimeException rex) {
            rex.printStackTrace();
        }
    }
}