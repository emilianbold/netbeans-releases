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
public class WSAppBnd extends DDXmi{

    private static final String ROOT=TYPE_APP_BND_ID;
    private static final String ROOT_NAME="ApplicationBnd";


    /** Creates a new instance of AppBndXmi */
    public WSAppBnd() {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    public WSAppBnd(org.w3c.dom.Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    
    public WSAppBnd(int options) {
        super(options,ROOT);
        //initOptions(options);
    }
    
    
    public WSAppBnd(File f,boolean validate) throws IOException{
        this(GraphManager.createXmlDocument(new FileInputStream(f), validate), Common.NO_DEFAULT_VALUES);
    }
    
    public WSAppBnd(InputStream in, boolean validate) {
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
        setNsApp();
        setNsAppBnd();
        setNsCommon();
        setNsXmi();
        setNsXsi();
        setXmiId("Application_ID_Bnd");
        setAuthTable(new AuthorizationTableType());
        setAuthTableId(AUTH_TABLE+"_1");
        setApplication("");
        setApplicationHref("Application_ID");
    }
    protected void initOptions(int options) {
        this.graphManager = new GraphManager(this);
        this.createRoot(ROOT, ROOT_NAME,
                Common.TYPE_1 | Common.TYPE_BEAN, WSAppBnd.class);
        
        this.createAttribute(XMI_ID_ID,    XMI_ID,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_APP_ID,    NS_APP,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_COMMON_ID, NS_COMMON,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_APP_BND_ID,NS_APP_BND,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XMI_ID,    NS_XMI,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XSI_ID,    NS_XSI,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_VERSION_ID,XMI_VERSION,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(3);
        
        this.createProperty(AUTH_TABLE_ID,
                AUTH_TABLE,
                Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY,
                AuthorizationTableType.class);
        this.createAttribute(AUTH_TABLE,XMI_ID_ID,AUTH_TABLE_XMI_ID,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(APPLICATION_ID,
                APPLICATION,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(APPLICATION,HREF_ID,APPLICATION_HREF,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        
        this.createProperty(RUN_AS_MAP_ID,
                RUN_AS_MAP,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(RUN_AS_MAP,XMI_ID_ID,RUN_AS_MAP_XMI_ID,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.initialize(options);
    }
    
    public void setAuthTable(AuthorizationTableType value) {
        this.setValue(AUTH_TABLE, value);
    }
    
    //
    public AuthorizationTableType getAuthorizationTable() {
        return (AuthorizationTableType)this.getValue(AUTH_TABLE);
    }
    public int sizeAuthorizationTable() {
        return this.size(AUTH_TABLE);
    }
    
    public void setAuthTableId(String value)  {
        setAttributeValue(AUTH_TABLE,AUTH_TABLE_XMI_ID,value);
    }
    public String getAuthTableId(){
        return (String)getAttributeValue(AUTH_TABLE,AUTH_TABLE_XMI_ID);
    }
    
    public void setRunAsMapId(String value)  {
        if(getRunAsMap()==null) {
            setRunAsMap("");
        }
        setAttributeValue(RUN_AS_MAP,RUN_AS_MAP_XMI_ID,value);
    }
    public String getRunAsMapId(){
        if(getRunAsMap()==null) {
            return null;
        }
        return (String)getAttributeValue(RUN_AS_MAP,RUN_AS_MAP_XMI_ID);
    }
    public void setRunAsMap(String value)  {
        setValue(RUN_AS_MAP,value);
    }
    public String getRunAsMap(){
        return (String)getValue(RUN_AS_MAP);
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if (getAuthorizationTable()!= null) {
            getAuthorizationTable().validate();
        } else {
            throw new org.netbeans.modules.schema2beans.ValidateException("getAuthorizationTable() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, AUTH_TABLE, this);	// NOI18N
        }
        
        if (getApplication()== null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getApplication() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, APPLICATION, this);	// NOI18N
        }
        if(getApplicationHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getApplicationHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, APPLICATION, this);	// NOI18N
        }
        if(getNsApp()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsApp() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsAppBnd()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsAppBnd() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsCommon()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsCommon() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsXmi()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsXmi() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getAuthorizationTable()!=null)
            if(getAuthTableId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getAuthTableId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, AUTH_TABLE, this);	// NOI18N
            }
        if(getXmiId()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiId() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getXmiVersion()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getXmiVersion() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
    }
    
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        
        str.append(indent);
        str.append(AUTH_TABLE);	// NOI18N
        n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuthorizationTable();
        if (n != null)
            n.dump(str, indent + "\t");	// NOI18N
        else
            str.append(indent+"\tnull");	// NOI18N
        this.dumpAttributes(AUTH_TABLE, 0, str, indent);
        
        str.append(indent);
        str.append(APPLICATION);	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getApplication();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(APPLICATION, 0, str, indent);
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
