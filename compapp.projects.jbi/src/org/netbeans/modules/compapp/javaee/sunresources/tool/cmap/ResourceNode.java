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
