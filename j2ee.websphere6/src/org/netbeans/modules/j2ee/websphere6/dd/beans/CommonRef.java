/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
/**
 *
 * @author dlm198383
 */
public abstract class CommonRef extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants{
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    private String BINDING_REFERENCE;
    private String BINDING_REFERENCE_XMI_ID;
    private String BINDING_REFERENCE_XMI_TYPE;
    private String BINDING_REFERENCE_JNDI_NAME;
    private String BINDING_REFERENCE_HREF;
    static public final String COMMON_REFERENCE="CommonReference"; //NOI18N
    protected String hrefType=WEB_APPLICATION; //default value
    
    public CommonRef(String dtdName, String propName, String xmiIdName, String jndiNamePropName, String hrefPropName, String typePropName) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        
        BINDING_REFERENCE=propName;
        BINDING_REFERENCE_XMI_ID=xmiIdName;
        BINDING_REFERENCE_JNDI_NAME=jndiNamePropName;
        BINDING_REFERENCE_HREF=hrefPropName;
        BINDING_REFERENCE_XMI_TYPE=typePropName;
        initPropertyTables(2);
        this.createProperty(dtdName, 	// NOI18N
                BINDING_REFERENCE,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(BINDING_REFERENCE, HREF_ID, BINDING_REFERENCE_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(BINDING_REFERENCE, XMI_TYPE_ID, BINDING_REFERENCE_XMI_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
    }
    
    
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getBindingReference()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getBindingReference() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, COMMON_REFERENCE, this);	// NOI18N
        }
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, COMMON_REFERENCE, this);	// NOI18N
        }
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, COMMON_REFERENCE, this);	// NOI18N
        }
        if(getJndiName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getJndiName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, COMMON_REFERENCE, this);	// NOI18N
        }
    }
    public String getBindingReference() {
        return (String) this.getValue(BINDING_REFERENCE);
    }
    public String getHrefType() {
        return hrefType;
    }
    public void setHrefType(String value) {
        hrefType=value;
    }
    
    public String getHref() {
        return DDXmi.getIdFromHref((String)this.getAttributeValue(BINDING_REFERENCE, BINDING_REFERENCE_HREF));
    }
    public String getXmiId() {
        return (String) getAttributeValue(BINDING_REFERENCE_XMI_ID);
    }
     public String getXmiType() {
        return (String) getAttributeValue(BINDING_REFERENCE,BINDING_REFERENCE_XMI_TYPE);
    }
    public String getJndiName() {
        return (String) this.getAttributeValue(BINDING_REFERENCE_JNDI_NAME);
    }
    
    public void setBindingReference(String value) {
        this.setValue(BINDING_REFERENCE, value);
    }
    
    public void setHref(String value) {
        if(getBindingReference()==null) {
            setBindingReference("");
        }
        if(hrefType.equals(WEB_APPLICATION)) {
            this.setAttributeValue(BINDING_REFERENCE, BINDING_REFERENCE_HREF,DDXmi.addWebHrefToId(value));
        } else  if(hrefType.equals(EJB_JAR)) {
            this.setAttributeValue(BINDING_REFERENCE, BINDING_REFERENCE_HREF,DDXmi.addEjbJarHrefToId(value));
        } else  if(hrefType.equals(APPLICATION)) {
            this.setAttributeValue(BINDING_REFERENCE, BINDING_REFERENCE_HREF,DDXmi.addAppHrefToId(value));
        } else {
            this.setAttributeValue(BINDING_REFERENCE, BINDING_REFERENCE_HREF,value);
        }
    }
    public void setXmiId(String value) {
        this.setAttributeValue(BINDING_REFERENCE_XMI_ID,value);
    }
    public void setXmiType(String value) {
        this.setAttributeValue(BINDING_REFERENCE, BINDING_REFERENCE_XMI_TYPE,value);
    }
    public void setJndiName(String value) {
        this.setAttributeValue(BINDING_REFERENCE_JNDI_NAME,value);
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append(BINDING_REFERENCE);	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getBindingReference();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(BINDING_REFERENCE, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
    public abstract String getType();
}
