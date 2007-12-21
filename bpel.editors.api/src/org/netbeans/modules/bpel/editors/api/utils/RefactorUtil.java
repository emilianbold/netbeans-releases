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
package org.netbeans.modules.bpel.editors.api.utils;

import javax.swing.Icon;
import java.beans.BeanInfo;
import javax.swing.ImageIcon;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class RefactorUtil {
    public static final int MAX_SIMPLE_NAME_LENGTH = 50;
    public static final String ENTITY_SEPARATOR = "."; // NOI18N
    
    private RefactorUtil() {
    }
    
    public static String getName(Component component) {
        String name = null;

        if (component instanceof Named) {
            name = ((Named)component).getName();
        } else if (component instanceof BooleanExpr) {
            name = ((BooleanExpr)component).getContent();
            name = name == null ? null : name.trim();
            if (name != null && name.length() > MAX_SIMPLE_NAME_LENGTH) {
                name = name.substring(0, MAX_SIMPLE_NAME_LENGTH);
            }
        } else if (component instanceof BpelEntity) {
            org.netbeans.modules.bpel.editors.api.nodes.NodeType
                    bpelNodeType = org.netbeans.modules.bpel.editors.api.utils.
                                        Util.getBasicNodeType((BpelEntity)component);
            
            if (bpelNodeType != null 
                    && ! NodeType.UNKNOWN_TYPE.equals(bpelNodeType))
            {
                name = bpelNodeType.getDisplayName();
            }
        }
        
        if (name == null && component instanceof DocumentComponent) {
            name = Util.getTagName((DocumentComponent)component);
        }
        
        return name == null ? "" : name;
    }
    
    public static String getHtmlName(Component component) {
        String htmlName = null;
        NodeType nodeType = getBpelNodeType(component);
        
        HtmlNameManager[] nameManagers =  HtmlNameManager.HTML_NAME_MANAGERS;
        for (HtmlNameManager htmlNameManager : nameManagers) {
            if (htmlNameManager.accept(nodeType, component)) {
                htmlName = htmlNameManager.getHtmlName(nodeType, component);
            }
        }

        htmlName = htmlName == null ? "" : htmlName;
        
        return removeHtmlHeader(htmlName);
    }
    
    public static Icon getIcon(Component component) {
        // vlv
        Node node = getNode(component);

        if (node  != null) {
          return new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        Icon icon = null;

        if (component instanceof BpelEntity) {
            org.netbeans.modules.bpel.editors.api.nodes.NodeType
                bpelNodeType = org.netbeans.modules.bpel.editors.api.utils.
                                        Util.getBasicNodeType((BpelEntity)component);
            if (bpelNodeType != null 
                    && ! org.netbeans.modules.bpel.editors.api.nodes.NodeType.
                                UNKNOWN_TYPE.equals(bpelNodeType)) 
            {
                icon = bpelNodeType.getIcon();
            }
        }
        
        icon = icon != null 
                ? icon 
                : org.netbeans.modules.bpel.editors.api.nodes.NodeType.
                                            DEFAULT_BPEL_ENTITY_NODE.getIcon();
        
        return icon;
    }
                        
    // vlv
    public static String getToolTip(Component component) {
      String type = getType(component);

      if (type != null) {
        return "<html>" + type + " <b>" + getName(component) + "</b></html>";
      }
      return getName(component);
    }

    // vlv
    public static String getType(Component component) {
      String type = null;
      
      if (component instanceof BpelEntity) {
        type = ((BpelEntity) component).getElementType().getName();
      }
      else if (component instanceof SchemaComponent) {
        type = ((SchemaComponent) component).getComponentType().getName();
      }
      else {
        type = component.getClass().getName();
      }
      int k = type.lastIndexOf("."); // NOI18N

      if (k == -1) {
        return type;
      }
      return type.substring(k + 1);
    }

    // vlv
    private static Node getNode(Component component) {
      if (component instanceof SchemaComponent) {
        SchemaComponent schemaComponent = (SchemaComponent) component;
        CategorizedSchemaNodeFactory factory = new CategorizedSchemaNodeFactory(schemaComponent.getModel(), Lookups.singleton(schemaComponent));
        return factory.createNode(schemaComponent);
      }
      if (component instanceof WSDLComponent) {
        return NodesFactory.getInstance().create(component);
      }
      return null;
    }
    
    private static String removeHtmlHeader(String htmlString) {
        if (htmlString == null) {
            return htmlString;
        }
        
        String htmlStart = "<html>"; // NOI18N
        String htmlEnd = "</html>"; // NOI18N
        
        if (htmlString.matches(htmlStart+".*"+htmlEnd)) {
            htmlString = htmlString.substring(htmlStart.length() -1,
                    htmlString.length() - htmlEnd.length() + 1);
        }
        
        return htmlString;
    }
    
    private static NodeType getBpelNodeType(Component component) {
        if (!(component instanceof BpelEntity)) {
            return null;
        }
        
        return org.netbeans.modules.bpel.editors.api.utils.
                Util.getBasicNodeType((BpelEntity)component);
    }


    public static String getUsageContextPath(String suffix, BpelEntity entity, Class<? extends BpelEntity> filter) {
        String resultStr = getUsageContextPath(entity, filter);
        if (resultStr != null) {
            suffix = suffix == null ? "" : ENTITY_SEPARATOR+suffix; // NOI18N
            resultStr += suffix;
        } else {
            resultStr = suffix;
        }
        return resultStr;
    }
    
    public static String getUsageContextPath(BpelEntity entity, Class<? extends BpelEntity> filter) {
        assert entity != null;
        StringBuffer path = new StringBuffer(getName(entity));
        BpelEntity tmpEntity = entity;
        while((tmpEntity = tmpEntity.getParent()) != null) {
            if (tmpEntity.getElementType() == filter) {
                continue;
            }
            
            String tmpEntityName = getName(tmpEntity);
            if (tmpEntityName != null && tmpEntityName.length() > 0) {
                path.insert(0,ENTITY_SEPARATOR).insert(0,tmpEntityName);
            }
        }
        
        return path.toString();
    }
}
