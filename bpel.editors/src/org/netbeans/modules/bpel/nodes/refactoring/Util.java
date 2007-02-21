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
package org.netbeans.modules.bpel.nodes.refactoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.bpel.model.api.Process;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class Util {
    
    public static final int MAX_SIMPLE_NAME_LENGTH = 50;
    public static final String ENTITY_SEPARATOR = "."; // NOI18N
    private Util() {
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
        StringBuffer path = new StringBuffer(getEntityName(entity));
        BpelEntity tmpEntity = entity;
        while((tmpEntity = tmpEntity.getParent()) != null) {
            if (tmpEntity.getElementType() == filter) {
                continue;
            }
            
            String tmpEntityName = getEntityName(tmpEntity);
            if (tmpEntityName != null && tmpEntityName.length() > 0) {
                path.insert(0,ENTITY_SEPARATOR).insert(0,tmpEntityName);
            }
        }
        
        return path.toString();
    }
    
    public static String getEntityName(BpelEntity entity) {
        assert entity != null;
        String name = null;
        if (entity instanceof Named) {
            name = ((Named)entity).getName();
        } else if (entity instanceof BooleanExpr) {
            name = ((BooleanExpr)entity).getContent();
            name = name == null ? null : name.trim();
            if (name != null && name.length() > MAX_SIMPLE_NAME_LENGTH) {
                name = name.substring(0, MAX_SIMPLE_NAME_LENGTH);
            }
        } else {
            org.netbeans.modules.bpel.editors.api.nodes.NodeType
                    bpelNodeType = org.netbeans.modules.bpel.editors.api.utils.
                                        Util.getBasicNodeType(entity);
            
            if (bpelNodeType != null) {
                switch (bpelNodeType) {
                    case UNKNOWN_TYPE:
                        name = getTagName(entity);
                        break;
                    case DEFAULT_BPEL_ENTITY_NODE:
                        name = getTagName(entity);
                        break;
                    default:
                        name = bpelNodeType.getDisplayName();
                }
            }
//            
//            name = bpelNodeType == null ? name : bpelNodeType.getDisplayName();
        }
        
        return name == null ? "" : name;
    }
    
    public static String getTagName(BpelEntity entity ) {
        if (entity == null) {
            return null;
        }
        
        Element enEl = entity.getPeer();
        return enEl == null ? null : enEl.getTagName();
    }
    
    public static String getNodeName(BpelNode node) {
        assert node != null;
            
        Object ref = node.getReference();
        if (ref == null || !(ref instanceof BpelEntity)) {
            return node.getHtmlDisplayName();
        }
        String nodeName = null;
        NodeType nodeType = node.getNodeType();
        switch (nodeType) {
            case BOOLEAN_EXPR :
                nodeName = Util.getEntityName((BpelEntity)ref);
                break;
            case INVOKE :
            case RECEIVE :
            case REPLY :
            case ON_EVENT :
            case MESSAGE_HANDLER :
            case PARTNER_LINK :
                nodeName = node.getShortDescription();
                if (nodeName != null 
                        && nodeName.startsWith(nodeType.getDisplayName())) 
                {
                    String tmpNodeName = nodeName
                                    .substring(nodeType.getDisplayName().length());
                    if (tmpNodeName.trim().length() > 0) {
                        nodeName = tmpNodeName;
                    }
                }
                break;
            default:
                nodeName = node.getHtmlDisplayName();
        }
        return nodeName;
    }
}
