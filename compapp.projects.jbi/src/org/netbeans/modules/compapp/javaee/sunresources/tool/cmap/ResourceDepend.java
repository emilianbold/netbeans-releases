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

import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode.ResourceType;

/**
 * @author echou
 *
 */
public class ResourceDepend {

    private CMapNode source;
    private String mappedName = ""; // NOI18N
    private String targetResType;
    private String targetResJndiName;
    private ResourceType type = ResourceType.OTHER;
    private ResourceNode target;
    private Properties props = new Properties();
    
    public ResourceDepend(CMapNode source) {
        this.source = source;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public String getTargetResType() {
        return targetResType;
    }

    public void setTargetResType(String targetResType) {
        this.targetResType = targetResType;
    }

    public String getTargetResJndiName() {
        return targetResJndiName;
    }

    public void setTargetResJndiName(String targetResJndiName) {
        this.targetResJndiName = targetResJndiName;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mappedName=" + mappedName + ", "); // NOI18N
        sb.append("targetResType=" + targetResType + ", "); // NOI18N
        sb.append("targetResJndiName=" + targetResJndiName + ", "); // NOI18N
        sb.append("type=" + type + ", "); // NOI18N
        sb.append("props=" + props); // NOI18N
        sb.append("targetNode=@" + ((target == null)?null:target.hashCode())); // NOI18N
        return sb.toString();
    }

    public CMapNode getTarget() {
        return target;
    }

    public void setTarget(ResourceNode target) {
        this.target = target;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

}