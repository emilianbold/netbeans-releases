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
public class EjbBindingsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


    public EjbBindingsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public EjbBindingsType(int options) {
        super(comparators,runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(4);
        this.createProperty(ENTERPRISE_BEAN_ID,
                ENTERPRISE_BEAN,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        
        this.createAttribute(ENTERPRISE_BEAN, HREF_ID, ENTERPRISE_BEAN_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(ENTERPRISE_BEAN, XMI_TYPE_ID, ENTERPRISE_BEAN_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createProperty(RES_REF_BINDINGS_ID,
                RES_REF_BINDINGS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                ResRefBindingsType.class);
        this.createAttribute(RES_REF_BINDINGS,XMI_ID_ID  ,RES_REF_BINDINGS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RES_REF_BINDINGS,JNDI_NAME_ID  ,RES_REF_BINDINGS_JNDI_NAME  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(EJB_REF_BINDINGS_ID,
                EJB_REF_BINDINGS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                EjbRefBindingsType.class);
        this.createAttribute(EJB_REF_BINDINGS,XMI_ID_ID  ,EJB_REF_BINDINGS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(EJB_REF_BINDINGS,JNDI_NAME_ID  ,EJB_REF_BINDINGS_JNDI_NAME  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(RES_ENV_REF_BINDINGS_ID, 	// NOI18N
                RES_ENV_REF_BINDINGS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                ResEnvRefBindingsType.class);
        this.createAttribute(RES_ENV_REF_BINDINGS,XMI_ID_ID  ,RES_ENV_REF_BINDINGS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RES_ENV_REF_BINDINGS,JNDI_NAME_ID  ,RES_ENV_REF_BINDINGS_JNDI_NAME  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        
        this.createProperty(CMP_CONNECTION_FACTORY_ID,
                CMP_CONNECTION_FACTORY,
                Common.TYPE_0_1 | Common.TYPE_STRING| Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(CMP_CONNECTION_FACTORY,XMI_ID_ID,CMP_CONNECTION_FACTORY_XMI_ID,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(CMP_CONNECTION_FACTORY,JNDI_NAME_ID,CMP_CONNECTION_FACTORY_JNDI_NAME,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(CMP_CONNECTION_FACTORY,RES_AUTH_ID,CMP_CONNECTION_FACTORY_RES_AUTH,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.initialize(options);
    }
    public void initialize(int options) {
        setHref("");
    }
    
    public void setEnterpriseBean(String value) {
        this.setValue(ENTERPRISE_BEAN, value);
    }
    
    public String getEnterpriseBean() {
        return ( String)this.getValue(ENTERPRISE_BEAN);
    }
    
    public void setXmiId(String value) {
        this.setAttributeValue(EJB_BINDINGS_XMI_ID,value);
    }
    
    public String getXmiId() {
        return this.getAttributeValue(EJB_BINDINGS_XMI_ID);
    }
    
    public void setJndiName(String value) {
        this.setAttributeValue(EJB_BINDINGS_JNDI_NAME,value);
    }
    public String getJndiName() {
        return this.getAttributeValue(EJB_BINDINGS_JNDI_NAME);
    }
    public CommonRef [] getReferences() {
        ResRefBindingsType[] res=getResRefBindings();
        EjbRefBindingsType[] ejb=getEjbRefBindings();
        ResEnvRefBindingsType[] resenv=getResEnvRefBindings();
        CommonRef ref[]=new CommonRef[res.length+ejb.length+resenv.length];
        int index=0;
        for (int i=0;i<res.length;i++) {
            ref[index++]=res[i];
        }
        for (int i=0;i<ejb.length;i++) {
            ref[index++]=ejb[i];
        }
        for (int i=0;i<resenv.length;i++) {
            ref[index++]=resenv[i];
        }
        return ref;
    }
    
    
    
    // functions for manupulation ResRefBindings
    public void setResRefBindings(int index,ResRefBindingsType value) {
        this.setValue(RES_REF_BINDINGS, index,value);
    }
    
    public void setResRefBindings(ResRefBindingsType[]value) {
        this.setValue(RES_REF_BINDINGS, value);
    }
    
    public ResRefBindingsType[] getResRefBindings() {
        return (ResRefBindingsType[]) this.getValues(RES_REF_BINDINGS);
    }
    public ResRefBindingsType getResRefBindings(int index) {
        return (ResRefBindingsType)this.getValue(RES_REF_BINDINGS,index);
    }
    public int sizeResRefBindings() {
        return this.size(RES_REF_BINDINGS);
    }
    public int addResRefBindings(ResRefBindingsType value) {
        int positionOfNewItem = this.addValue(RES_REF_BINDINGS, value);
        return positionOfNewItem;
    }
    
    public int removeResRefBindings(ResRefBindingsType value) {
        return this.removeValue(RES_REF_BINDINGS, value);
    }
    
// functions for manupulation ResEnvRefBindings
    public void setResEnvRefBindings(int index,ResEnvRefBindingsType value) {
        this.setValue(RES_ENV_REF_BINDINGS, index,value);
    }
    
    public void setResEnvRefBindings(ResEnvRefBindingsType[]value) {
        this.setValue(RES_ENV_REF_BINDINGS, value);
    }
    
    public ResEnvRefBindingsType[] getResEnvRefBindings() {
        return (ResEnvRefBindingsType[]) this.getValues(RES_ENV_REF_BINDINGS);
    }
    public ResEnvRefBindingsType getResEnvRefBindings(int index) {
        return (ResEnvRefBindingsType)this.getValue(RES_ENV_REF_BINDINGS,index);
    }
    public int sizeResEnvRefBindings() {
        return this.size(RES_ENV_REF_BINDINGS);
    }
    public int addResEnvRefBindings(ResEnvRefBindingsType value) {
        int positionOfNewItem = this.addValue(RES_ENV_REF_BINDINGS, value);
        return positionOfNewItem;
    }
    
    public int removeResEnvRefBindings(ResEnvRefBindingsType value) {
        return this.removeValue(RES_ENV_REF_BINDINGS, value);
    }
    
    // functions for manupulation EjbRefBindings
    public void setEjbRefBindings(int index,EjbRefBindingsType value) {
        this.setValue(EJB_REF_BINDINGS, index,value);
    }
    
    public void setEjbRefBindings(EjbRefBindingsType[]value) {
        this.setValue(EJB_REF_BINDINGS, value);
    }
    
    public EjbRefBindingsType[] getEjbRefBindings() {
        return (EjbRefBindingsType[]) this.getValues(EJB_REF_BINDINGS);
    }
    public EjbRefBindingsType getEjbRefBindings(int index) {
        return (EjbRefBindingsType)this.getValue(EJB_REF_BINDINGS,index);
    }
    public int sizeEjbRefBindings() {
        return this.size(EJB_REF_BINDINGS);
    }
    public int addEjbRefBindings(EjbRefBindingsType value) {
        int positionOfNewItem = this.addValue(EJB_REF_BINDINGS, value);
        return positionOfNewItem;
    }
    
    public int removeEjbRefBindings(EjbRefBindingsType value) {
        return this.removeValue(EJB_REF_BINDINGS, value);
    }
    
    public int addReferenceBinding(CommonRef reference){
        if(reference instanceof ResRefBindingsType) {
            return addResRefBindings((ResRefBindingsType)reference);
        } else  if(reference instanceof ResEnvRefBindingsType) {
            return addResEnvRefBindings((ResEnvRefBindingsType)reference);
        } else  if(reference instanceof EjbRefBindingsType) {
            return addEjbRefBindings((EjbRefBindingsType)reference);
        }
        else return 0;
    }
    public int removeReferenceBinding(CommonRef reference){
        if(reference instanceof ResRefBindingsType) {
            return removeResRefBindings((ResRefBindingsType)reference);
        } else  if(reference instanceof ResEnvRefBindingsType) {
            return removeResEnvRefBindings((ResEnvRefBindingsType)reference);
        } else  if(reference instanceof EjbRefBindingsType) {
            return removeEjbRefBindings((EjbRefBindingsType)reference);
        }
        else return 0;
    }
    
    public void setCmpConnectionFactory(String value) {
        this.setValue(CMP_CONNECTION_FACTORY,value);
    }
    public String getCmpConnectionFactory() {
        return (String)this.getValue(CMP_CONNECTION_FACTORY);
    }
    public void setCmpConnectionFactoryXmiId(String value) {
        if(size(CMP_CONNECTION_FACTORY)==0) {
            setCmpConnectionFactory("");
        }
        this.setAttributeValue(CMP_CONNECTION_FACTORY,CMP_CONNECTION_FACTORY_XMI_ID,value);
    }
    public String getCmpConnectionFactoryXmiId() {
        if(size(CMP_CONNECTION_FACTORY)==0) {
            return null;
        }
        return (String) this.getAttributeValue(CMP_CONNECTION_FACTORY,CMP_CONNECTION_FACTORY_XMI_ID);
    }
    public void setCmpConnectionFactoryJndiName(String value) {
        if(size(CMP_CONNECTION_FACTORY)==0) {
            setCmpConnectionFactory("");
        }
        this.setAttributeValue(CMP_CONNECTION_FACTORY,CMP_CONNECTION_FACTORY_JNDI_NAME,value);
    }
    public String getCmpConnectionFactoryJndiName() {
        if(size(CMP_CONNECTION_FACTORY)==0) {
            return null;
        }
        return (String) this.getAttributeValue(CMP_CONNECTION_FACTORY,CMP_CONNECTION_FACTORY_JNDI_NAME);
    }
    public void setCmpConnectionFactoryResAuth(String value) {
        if(size(CMP_CONNECTION_FACTORY)==0) {
            setCmpConnectionFactory("");
        }
        this.setAttributeValue(CMP_CONNECTION_FACTORY,CMP_CONNECTION_FACTORY_RES_AUTH,value);
    }
    public String getCmpConnectionFactoryResAuth() {
        if(size(CMP_CONNECTION_FACTORY)==0) {
            return null;
        }
        return (String) this.getAttributeValue(CMP_CONNECTION_FACTORY,CMP_CONNECTION_FACTORY_RES_AUTH);
    }
    
    
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    
    public void setHref(java.lang.String value) {
        setAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_HREF,DDXmi.addEjbJarHrefToId(value));
    }
    
    public String getHref() {
        return DDXmi.getIdFromHref((String)getAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_HREF));
    }
    
    public void setXmiType(java.lang.String value) {
        setAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_TYPE,value);
    }
    
    public String getXmiType() {
        return (String)getAttributeValue(ENTERPRISE_BEAN, ENTERPRISE_BEAN_TYPE);
    }
    
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getEnterpriseBean()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getEnterpriseBean() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "EnterpriseBean", this);	// NOI18N
        }
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "EnterpriseBean", this);	// NOI18N
        }
        if(getXmiType()==null)     {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "EnterpriseBean", this);	// NOI18N
        }
        if(getXmiType()!=EJB_ENTERPRISE_BEAN_TYPE_SESSION) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiType()!= ejb:Session", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "EnterpriseBean", this);	// NOI18N
        }
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("EnterpriseBean");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getEnterpriseBean();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(ENTERPRISE_BEAN, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
