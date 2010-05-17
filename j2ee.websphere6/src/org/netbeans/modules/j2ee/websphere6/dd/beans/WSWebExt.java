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
public class WSWebExt extends DDXmi{
    private static final String ROOT=TYPE_WEB_APP_EXT_ID;

    private static final String ROOT_NAME="WebApplicationExt";

    /** Creates a new instance of WSAWebExt */
    public WSWebExt() {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    public WSWebExt(org.w3c.dom.Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    
     public WSWebExt(File f,boolean validate) throws IOException{
        this(GraphManager.createXmlDocument(new FileInputStream(f), validate), Common.NO_DEFAULT_VALUES);
    }
    
    public WSWebExt(InputStream in, boolean validate) {
        this(GraphManager.createXmlDocument(in, validate), Common.NO_DEFAULT_VALUES);
    }
    
    protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException {
        if (doc == null) {
            doc = GraphManager.createRootElementNode(ROOT);	// NOI18N
            if (doc == null)
                throw new Schema2BeansException(Common.getMessage(
                        "CantCreateDOMRoot_msg", ROOT)); 	// NOI18N
        }
        Node n = GraphManager.getElementNode(ROOT, doc);
        if (n == null) {
            throw new Schema2BeansException(Common.getMessage("DocRootNotInDOMGraph_msg", ROOT, doc.getFirstChild().getNodeName())); 	// NOI18N
        }
        this.graphManager.setXmlDocument(doc);
        this.createBean(n, this.graphManager());
        this.initialize(options);
    };
    
    public WSWebExt(int options) {
        super(options,ROOT);
       // initOptions(options);
    }
    
    
    
    public void initialize(int options) {
        
    }
    
    public void setDefaults() {
        setXmiVersion();
        setNsXmi();
        setNsWebAppExt();
        setNsWebApp();
        setNsXsi();
        setXmiId("WebAppExtension");
        setReload(true);
        setReloadInterval("3");
        setAdditionalClassPath("");
        setFileServingEnabled(true);
        setDirectoryBrowsing();
        setServeServletsByClassname(true);
        setPrecompileJSPs(false);
        setWebApplication("");
        setWebApplicationHref("WebApp");
        setDefaultErrorPage("");
        setAutoLoadFilters(false);
        setAutoRequestEncoding(true);
        setAutoResponseEncoding(true);
    }
    
    
    protected void initOptions(int options) {
        this.graphManager = new GraphManager(this);
        this.createRoot(ROOT, ROOT_NAME,	// NOI18N
                Common.TYPE_1 | Common.TYPE_BEAN, WSWebExt.class);
        
        initPropertyTables(2);
        this.createAttribute(XMI_ID_ID,        XMI_ID,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_WEB_APP_ID,    NS_WEB_APP,       AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_WEB_APP_EXT_ID,NS_WEB_APP_EXT,   AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XMI_ID,        NS_XMI,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XSI_ID,        NS_XSI,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_VERSION_ID,   XMI_VERSION,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RELOAD_INTERVAL_ID,RELOAD_INTERVAL, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RELOAD_ENABLED_ID,RELOAD_ENABLED, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(DEFAULT_ERROR_PAGE_ID,DEFAULT_ERROR_PAGE, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(ADDITIONAL_CLASSPATH_ID,ADDITIONAL_CLASSPATH, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(FILE_SERVING_ENABLED_ID,FILE_SERVING_ENABLED, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(DIRECTORY_BROWSING_ENABLED_ID,DIRECTORY_BROWSING_ENABLED, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(SERVE_SERVLETS_ID,SERVE_SERVLETS, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(PRECOMPILE_JSPS_ID,PRECOMPILE_JSPS,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(AUTO_REQUEST_ENCODING_ID,AUTO_REQUEST_ENCODING,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(AUTO_RESPONSE_ENCODING_ID,AUTO_RESPONSE_ENCODING,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(AUTO_LOAD_FILTERS_ID,AUTO_LOAD_FILTERS,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(EXTENDED_SERVLETS_ID,
                EXTENDED_SERVLETS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                ExtendedServletsType.class);
        this.createAttribute(EXTENDED_SERVLETS,XMI_ID_ID  ,EXTENDED_SERVLETS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(WEB_APPLICATION_ID, 	// NOI18N
                WEB_APPLICATION,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(WEB_APPLICATION,HREF_ID,WEB_APPLICATION_HREF,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.initialize(options);
    }
    
    public void setExtendedServlets(int index,ExtendedServletsType value) {
        this.setValue(EXTENDED_SERVLETS, index,value);
    }
    
    public void setExtendedServlets(ExtendedServletsType[]value) {
        this.setValue(EXTENDED_SERVLETS, value);
    }
//
    public ExtendedServletsType[] getExtendedServlets() {
        return (ExtendedServletsType[])this.getValues(EXTENDED_SERVLETS);
    }
    public ExtendedServletsType getExtendedServlets(int index) {
        return (ExtendedServletsType)this.getValue(EXTENDED_SERVLETS,index);
    }
    public int sizeExtendedServlets() {
        return this.size(EXTENDED_SERVLETS);
    }
    public int addExtendedServlets(ExtendedServletsType value) {
        int positionOfNewItem = this.addValue(EXTENDED_SERVLETS, value);
        return positionOfNewItem;
    }
    
//
// Remove an element using its reference
// Returns the index the element had in the list
//
    public int removeExtendedServlets(ExtendedServletsType value) {
        return this.removeValue(EXTENDED_SERVLETS, value);
    }

    
    public void setDefaultErrorPage(String value)  {
        this.setAttributeValue(DEFAULT_ERROR_PAGE,value);        
    }
    public String getDefaultErrorPage()  {
        return (String) this.getAttributeValue(DEFAULT_ERROR_PAGE);        
    }
    
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if (getExtendedServlets()!= null) {
            // Validating property jdbcConnectionPool
            for (int _index = 0; _index < sizeExtendedServlets(); ++_index) {
                ExtendedServletsType element = getExtendedServlets(_index);
                if (element != null) {
                    element.validate();
                }
                
            }
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
        if (getNsWebAppExt()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsWebAppExt() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if (getNsXmi()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsXmi() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
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
        str.append(EXTENDED_SERVLETS+"["+this.sizeExtendedServlets()+"]");	// NOI18N
        for(int i=0; i<this.sizeExtendedServlets(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getExtendedServlets(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(EXTENDED_SERVLETS, i, str, indent);
        }
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
