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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
