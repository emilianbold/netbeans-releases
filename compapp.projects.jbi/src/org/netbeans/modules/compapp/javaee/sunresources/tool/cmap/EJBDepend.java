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

/**
 * @author echou
 *
 */
public class EJBDepend {

    private CMapNode source;
    private String logicalName = ""; // NOI18N
    private String targetIntfName;
    private CMapNode target;
    
    public EJBDepend(CMapNode source) {
        this.source = source;
    }

    public String getTargetIntfName() {
        return targetIntfName;
    }

    public void setTargetIntfName(String targetIntfName) {
        this.targetIntfName = targetIntfName;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("targetIntfName=" + targetIntfName + ", "); // NOI18N
        sb.append("targetNode=@" + ((target == null)?null:target.hashCode())); // NOI18N
        return sb.toString();
    }

    public CMapNode getTarget() {
        return target;
    }

    public void setTarget(CMapNode target) {
        this.target = target;
    }
    
}
