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
public class WSAppExt extends DDXmi{
    private static final String ROOT=TYPE_APP_EXT_APP_ID;

    private static final String ROOT_NAME="ApplicationExt";

    /** Creates a new instance of WSAppExt */
    public WSAppExt() {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    public WSAppExt(org.w3c.dom.Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    public WSAppExt(int options) {
        super(options,ROOT);
        //initOptions(options);
    }
    
    public WSAppExt(File f,boolean validate) throws IOException{
        this(GraphManager.createXmlDocument(new FileInputStream(f), validate), Common.NO_DEFAULT_VALUES);
    }
    
    public WSAppExt(InputStream in, boolean validate) {
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
        setXmiVersion();
        setNsXmi();
        setNsXsi();
        setNsAppExt();
        setNsApp();
        setXmiId("Application_ID_Ext");                
        setApplicationHref("Application_ID");
    }
    
    protected void initOptions(int options) {
        this.graphManager = new GraphManager(this);
        this.createRoot(ROOT, ROOT_NAME,	// NOI18N
                Common.TYPE_1 | Common.TYPE_BEAN, WSAppExt.class);
        
        initPropertyTables(2);
        this.createAttribute(XMI_ID_ID,     XMI_ID,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_APP_ID,     NS_APP,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_APP_EXT_ID, NS_APP_EXT,  AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XMI_ID,     NS_XMI,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XSI_ID,     NS_XSI,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_VERSION_ID,XMI_VERSION, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RELOAD_ENABLED_ID,RELOAD_ENABLED, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(RELOAD_INTERVAL_ID,RELOAD_INTERVAL, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(SHARED_SESSION_CONTEXT_ID,SHARED_SESSION_CONTEXT, AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(MODULE_EXTENSIONS_ID, 	
                MODULE_EXTENSIONS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                ModuleExtensionsType.class);
        this.createAttribute(MODULE_EXTENSIONS,XMI_TYPE_ID,        MODULE_EXTENSIONS_XMI_TYPE,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(MODULE_EXTENSIONS,ALT_ROOT_ID ,       MODULE_EXTENSIONS_ALT_ROOT ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(MODULE_EXTENSIONS,ALT_BINDINGS_ID,    MODULE_EXTENSIONS_ALT_BINDINGS ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(MODULE_EXTENSIONS,ALT_EXTENSIONS_ID , MODULE_EXTENSIONS_ALT_EXTENSIONS ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(MODULE_EXTENSIONS,XMI_ID_ID  ,        MODULE_EXTENSIONS_XMI_ID  ,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(APPLICATION_ID, 	
                APPLICATION,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(APPLICATION,HREF_ID,APPLICATION_HREF,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.initialize(options);
    }
    
    public void setModuleExtensions(int index,ModuleExtensionsType value) {
        this.setValue(MODULE_EXTENSIONS, index,value);
    }
    
    public void setModuleExtensions(ModuleExtensionsType[]value) {
        this.setValue(MODULE_EXTENSIONS, value);
    }
    //
    public ModuleExtensionsType[] getModuleExtensions() {
        return (ModuleExtensionsType[])this.getValues(MODULE_EXTENSIONS);
    }
    public ModuleExtensionsType getModuleExtensions(int index) {
        return (ModuleExtensionsType)this.getValue(MODULE_EXTENSIONS,index);
    }
    public int sizeModuleExtensions() {
        return this.size(MODULE_EXTENSIONS);
    }
    public int addModuleExtensions(ModuleExtensionsType value) {
        int positionOfNewItem = this.addValue(MODULE_EXTENSIONS, value);
        return positionOfNewItem;
    }
    
    
    public int removeModuleExtensions(ModuleExtensionsType value) {
        return this.removeValue(MODULE_EXTENSIONS, value);
    }
    
    public void setSharedSession(boolean value) {
        this.setAttributeValue(SHARED_SESSION_CONTEXT, (value==true)?"true":"false");
    }
    //
    public boolean getSharedSession() {
        String getSharedSting=this.getAttributeValue(SHARED_SESSION_CONTEXT);
        if(getSharedSting==null) return false;
        else return (getSharedSting.equals("true")?true:false);        
    }
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {        
        if (getModuleExtensions()!= null) {
            // Validating property jdbcConnectionPool
            for (int _index = 0; _index < sizeModuleExtensions(); ++_index) {
                ModuleExtensionsType element = getModuleExtensions(_index);
                if (element != null) {
                    element.validate();
                }
            }
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
        if(getNsAppExt()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsAppExt() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsCommon()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsCommon() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if(getNsXmi()==null) {
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
        str.append(MODULE_EXTENSIONS+"["+this.sizeModuleExtensions()+"]");	// NOI18N
        for(int i=0; i<this.sizeModuleExtensions(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getModuleExtensions(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(MODULE_EXTENSIONS, i, str, indent);
        }
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
