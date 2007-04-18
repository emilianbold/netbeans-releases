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

package org.netbeans.modules.compapp.javaee.sunresources.tool.cmap;

import java.util.Properties;


/**
 * @author echou
 *
 */
public class ResourceNode extends CMapNode {

    public enum ResourceType { JMS, WEBSERVICE, OTHER }
    
    private String logicalName;
    private String resJndiName;
    private String resType;
    private ResourceType nodeType;
    
    public ResourceNode(String logicalName, String resType, 
            String resJndiName, ResourceType nodeType, Properties props) {
        super(resType, CMapNodeType.RESOURCE);
        this.logicalName = logicalName;
        this.resType = resType;
        this.resJndiName = resJndiName;
        this.nodeType = nodeType;
        if (props != null) {
            mergeProperties(props, false);
        }
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getResJndiName() {
        return resJndiName;
    }

    public void setResJndiName(String resJndiName) {
        this.resJndiName = resJndiName;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("@" + this.hashCode() + ", "); // NOI18N
        sb.append("type=" + type + ", "); // NOI18N
        sb.append("logicalName=" + logicalName + ", "); // NOI18N
        sb.append("resType=" + resType + ", "); // NOI18N
        sb.append("resJndiName=" + resJndiName + ", "); // NOI18N
        sb.append("nodeType=" + nodeType); // NOI18N
        sb.append("\n"); // NOI18N
        return sb.toString();
    }

    public ResourceType getNodeType() {
        return nodeType;
    }

    public void setNodeType(ResourceType nodeType) {
        this.nodeType = nodeType;
    }

    
}
