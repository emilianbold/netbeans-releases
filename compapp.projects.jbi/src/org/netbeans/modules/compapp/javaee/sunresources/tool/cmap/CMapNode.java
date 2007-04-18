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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author echou
 *
 */
public class CMapNode {

    public enum CMapNodeType { STATELESS, STATEFUL, MDB, SERVLET, RESOURCE }
    
    protected String logicalName;
    protected String cls;  // class of this node
    protected CMapNodeType type;
    private Properties props = new Properties();
    private ArrayList<EJBDepend> ejbDepends = new ArrayList<EJBDepend> ();
    private ArrayList<ResourceDepend> resDepends = new ArrayList<ResourceDepend> ();
    
    public CMapNode() {
        // default constructor
    }
    
    public CMapNode(String logicalName, String cls, CMapNodeType type) {
        this.logicalName = logicalName;
        this.cls = cls;
        this.type = type;
    }
    
    public CMapNode(String cls, CMapNodeType type) {
        this("", cls, type); // NOI18N
    }
    
    public CMapNodeType getType() {
        return type;
    }

    public void setType(CMapNodeType type) {
        this.type = type;
    }

    public Properties getProps() {
        return this.props;
    }
    
    public ArrayList<EJBDepend> getEjbDepends() {
        return this.ejbDepends;
    }
    
    public ArrayList<ResourceDepend> getResDepends() {
        return this.resDepends;
    }
    
    public String getNodeClass() {
        return this.cls;
    }
    
    public void setNodeClass(String cls) {
        this.cls = cls;
    }
    
    public String getLogicalName() {
        return this.logicalName;
    }
    
    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("@" + this.hashCode() + ", "); // NOI18N
        sb.append("logicalName=" + logicalName + ", "); // NOI18N
        sb.append("type=" + type + ", "); // NOI18N
        sb.append("class=" + cls + "\n"); // NOI18N
        sb.append("\tprops=" + props + "\n"); // NOI18N
        sb.append("\tejbDepends=" + ejbDepends + "\n"); // NOI18N
        sb.append("\tresDepends=" + resDepends + "\n"); // NOI18N
        return sb.toString();
    }
    
    protected void mergeProperties(Properties input, boolean overwrite) {
        for (Enumeration<?> e = input.propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            if (overwrite || this.props.getProperty(key) == null) {
                this.props.setProperty(key, input.getProperty(key));
            }
        }
    }
}
