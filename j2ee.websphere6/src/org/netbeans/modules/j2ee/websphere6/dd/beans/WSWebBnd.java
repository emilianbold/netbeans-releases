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
import java.io.*;

/**
 *
 * @author dlm198383
 */
public class WSWebBnd extends DDXmi {
    private static final String ROOT=TYPE_WEB_APP_BND_ID;

    private static final String ROOT_NAME="WebApplicationBnd";



    /** Creates a new instance of WSAppBnd */
    public WSWebBnd() {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    public WSWebBnd(org.w3c.dom.Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    public WSWebBnd(int options) {
        super(options,ROOT);
    }
    
    
    public WSWebBnd(File f,boolean validate) throws IOException{
        this(GraphManager.createXmlDocument(new FileInputStream(f), validate), Common.NO_DEFAULT_VALUES);
    }
    
    public WSWebBnd(InputStream in, boolean validate) {
        this(GraphManager.createXmlDocument(in, validate), Common.NO_DEFAULT_VALUES);
    }
    
    protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException {
        if (doc == null) {
            doc = GraphManager.createRootElementNode(ROOT);	// NOI18N
            if (doc == null)
                throw new Schema2BeansException(Common.getMessage(
                        "CantCreateDOMRoot_msg", ROOT));
        }
        Node n = GraphManager.getElementNode(ROOT, doc);	// NOI18N
        if (n == null) {
            throw new Schema2BeansException(Common.getMessage("DocRootNotInDOMGraph_msg", ROOT, doc.getFirstChild().getNodeName()));
        }
        this.graphManager.setXmlDocument(doc);
        this.createBean(n, this.graphManager());
        this.initialize(options);
    };
    public void initialize(int options) {
        
    }
    
    public void setDefaults() {
        setNsWebAppBnd();
        setNsWebApp();
        setXmiVersion();
        setNsXmi();
        setNsCommon();
        setNsCommonBnd();
        setXmiId("WebAppBinding");
        setVirtualHostName("default_host");
        setWebApplication("");
        setWebApplicationHref("WebApp");
    }
    
    protected void initOptions(int options) {
        this.graphManager = new GraphManager(this);
        this.createRoot(ROOT, ROOT_NAME,	// NOI18N
                Common.TYPE_1 | Common.TYPE_BEAN, WSWebBnd.class);
        initPropertyTables(3);
        //this.createAttribute(ROOT_NAME,XMI_ID_ID,            XMI_ID,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_ID_ID,            XMI_ID,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_COMMON_ID,         NS_COMMON,        AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_COMMON_BND_ID,     NS_COMMON_BND,    AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XMI_ID,            NS_XMI,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_VERSION_ID,       XMI_VERSION,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createAttribute(NS_WEB_APP_ID,        NS_WEB_APP,       AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_WEB_APP_BND_ID,    NS_WEB_APP_BND,   AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(VIRTUAL_HOST_NAME_ID, VIRTUAL_HOST_NAME,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
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
        
        this.createProperty(RES_ENV_REF_BINDINGS_ID,
                RES_ENV_REF_BINDINGS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                ResEnvRefBindingsType.class);
        this.createAttribute(RES_ENV_REF_BINDINGS,XMI_ID_ID  ,RES_ENV_REF_BINDINGS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RES_ENV_REF_BINDINGS,JNDI_NAME_ID  ,RES_ENV_REF_BINDINGS_JNDI_NAME  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(WEB_aPPLICATION_ID, 	// NOI18N
                WEB_APPLICATION,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(WEB_APPLICATION,HREF_ID,WEB_APPLICATION_HREF,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.initialize(options);
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
    
    
    public void setVirtualHostName(String value)  {
        setAttributeValue(VIRTUAL_HOST_NAME,value);
    }
    
    public String getVirtualHostName(){
        return (String)getAttributeValue(VIRTUAL_HOST_NAME);
    }
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        if (getResRefBindings()!= null) {
            for (int _index = 0; _index < sizeResRefBindings(); ++_index) {
                ResRefBindingsType element = getResRefBindings(_index);
                if (element != null) {
                    element.validate();
                }
            }
        }
        
        if (getEjbRefBindings()!= null) {
            for (int _index = 0; _index < sizeEjbRefBindings(); ++_index) {
                EjbRefBindingsType element = getEjbRefBindings(_index);
                if (element != null) {
                    element.validate();
                }
            }
        }
        if (getResEnvRefBindings()!= null) {
            for (int _index = 0; _index < sizeResEnvRefBindings(); ++_index) {
                ResEnvRefBindingsType element = getResEnvRefBindings(_index);
                if (element != null) {
                    element.validate();
                }
            }
        }
        if (getNsCommon()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsCommon() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if (getNsCommonBnd()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsCommonBnd() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if (getNsXmi()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsXmi() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getXmiVersion()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiVersion() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        
        if (getWebApplication()== null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getWebApplication() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, WEB_APPLICATION, this);	// NOI18N
        }
        if (getWebApplicationHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getWebApplicationHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, WEB_APPLICATION, this);	// NOI18N
        }
        if (getNsWebApp()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsWebApp() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if (getNsWebAppBnd()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsWebAppBnd() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getVirtualHostName()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getVirtualHostName() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
    }
    
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        BaseBean n;
        
        str.append(indent);
        str.append(WEB_APPLICATION);	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getApplication();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(WEB_APPLICATION, 0, str, indent);
        
        str.append(indent);
        str.append(RES_REF_BINDINGS+"["+this.sizeResRefBindings()+"]");	// NOI18N
        for(int i=0; i<this.sizeResRefBindings(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getResRefBindings(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(RES_REF_BINDINGS, i, str, indent);
        }
        
        
        str.append(indent);
        str.append(EJB_REF_BINDINGS+"["+this.sizeEjbRefBindings()+"]");	// NOI18N
        for(int i=0; i<this.sizeEjbRefBindings(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getEjbRefBindings(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(EJB_REF_BINDINGS, i, str, indent);
        }
        
        
        str.append(RES_ENV_REF_BINDINGS+"["+this.sizeResEnvRefBindings()+"]");	// NOI18N
        for(int i=0; i<this.sizeResEnvRefBindings(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getResEnvRefBindings(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(RES_ENV_REF_BINDINGS, i, str, indent);
        }
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
