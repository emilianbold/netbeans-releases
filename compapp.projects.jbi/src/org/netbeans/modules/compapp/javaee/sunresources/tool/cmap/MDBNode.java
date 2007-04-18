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
import org.netbeans.modules.classfile.ClassFile;

/**
 * @author echou
 *
 */
public class MDBNode extends CMapNode {

    private Properties activationConfig = new Properties();
    private String msgListenerIntClassName;
    private String mappedName;
    private ResourceNode targetListenNode;
    
    /**
     * This constructor is invoked from EJB 2.1 style DD processing
     * 
     */
    public MDBNode() {
        super();
    }
    
    /** 
     * This constructor is invoked from EJB 3.0 style annotation processing
     *
     * @param cls
     * @param type
     */
    public MDBNode(Class cls, CMapNodeType type) {
        super(cls.getSimpleName(), cls.getName(), type);
    }
    
    /*
     * invoked from NetBeans ClassFile API
     */
    public MDBNode(ClassFile cls, CMapNodeType type) {
        super(cls.getName().getSimpleName(), cls.getName().getExternalName(), type);
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }
    
    public ResourceNode getTargetListenNode() {
        return targetListenNode;
    }

    public void setTargetListenNode(ResourceNode targetListenNode) {
        this.targetListenNode = targetListenNode;
    }

    public String getMsgListenerIntClassName() {
        return msgListenerIntClassName;
    }

    public void setMsgListenerIntClassName(String msgListenerIntClassName) {
        this.msgListenerIntClassName = msgListenerIntClassName;
    }
    
    public Properties getActivationConfig() {
        return activationConfig;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("\tmappedName=" + mappedName + ", "); // NOI18N
        sb.append("targetListenNode=@" +  // NOI18N
                ((targetListenNode == null)?null:targetListenNode.hashCode()) + ", "); // NOI18N
        sb.append("msgListenerIntClass=" + msgListenerIntClassName + ", "); // NOI18N
        sb.append("activationConfig=" + activationConfig); // NOI18N
        sb.append("\n"); // NOI18N
        return sb.toString();
    }
    
}
