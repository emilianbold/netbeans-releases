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
public class WSEjbExt extends DDXmi{
    private static final String ROOT=TYPE_EJB_EXT_ID;

    private static final String ROOT_NAME="EjbJarExt";

    /** Creates a new instance of WSAppExt */
    public WSEjbExt() {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    public WSEjbExt(org.w3c.dom.Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    public WSEjbExt(File f,boolean validate) throws IOException{
        this(GraphManager.createXmlDocument(new FileInputStream(f), validate), Common.NO_DEFAULT_VALUES);
    }
    
    public WSEjbExt(InputStream in, boolean validate) {
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
    public WSEjbExt(int options) {
        super(options,ROOT);
    }
    public void initialize(int options) {
        
    }
    
    public void setDefaults() {
        setXmiVersion();
        setNsXmi();
        setNsEjbExt();
        setNsEjb();
        setXmiId("EJBJarExtension");
        setNsCommonextLocaltran();
        setEjbJar("");
        setEjbJarHref("ID_ejb_jar");
    }
    
    protected void initOptions(int options) {
        this.graphManager = new GraphManager(this);
        this.createRoot(ROOT, ROOT_NAME,	// NOI18N
                Common.TYPE_1 | Common.TYPE_BEAN, WSEjbExt.class);
        
        initPropertyTables(2);
        this.createAttribute(XMI_ID_ID,        XMI_ID,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_EJB_ID,        NS_EJB,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_EJB_EXT_ID,    NS_EJB_EXT,       AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_XMI_ID,        NS_XMI,           AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(XMI_VERSION_ID,   XMI_VERSION,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(NS_COMMONEXT_LOCALTRAN_ID,NS_COMMONEXT_LOCALTRAN,      AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        
        this.createProperty(EJB_EXTENSIONS_ID, 	
                EJB_EXTENSIONS,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                EjbExtensionsType.class);
        this.createAttribute(EJB_EXTENSIONS,XMI_TYPE_ID , EJB_EXTENSIONS_XMI_TYPE  , AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(EJB_EXTENSIONS,XMI_ID_ID   , EJB_EXTENSIONS_XMI_ID    , AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.createAttribute(EJB_EXTENSIONS,NAME_ID     , EJB_EXTENSIONS_XMI_NAME  , AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        
        this.createProperty(EJB_JAR_ID, 	// NOI18N
                EJB_JAR,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(EJB_JAR,HREF_ID,EJB_JAR_HREF,AttrProp.CDATA | AttrProp.IMPLIED,null, null);
        this.initialize(options);
    }
    
    public void setEjbExtensions(int index,EjbExtensionsType value) {
        this.setValue(EJB_EXTENSIONS, index,value);
    }
    
    public void setEjbExtensions(EjbExtensionsType[]value) {
        this.setValue(EJB_EXTENSIONS, value);
    }
    //
    public EjbExtensionsType[] getEjbExtensions() {
        return (EjbExtensionsType[])this.getValues(EJB_EXTENSIONS);
    }
    public EjbExtensionsType getEjbExtensions(int index) {
        return (EjbExtensionsType)this.getValue(EJB_EXTENSIONS,index);
    }
    public int sizeEjbExtensions() {
        return this.size(EJB_EXTENSIONS);
    }
    public int addEjbExtensions(EjbExtensionsType value) {
        int positionOfNewItem = this.addValue(EJB_EXTENSIONS, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeEjbExtensions(EjbExtensionsType value) {
        return this.removeValue(EJB_EXTENSIONS, value);
    }
    
    
    
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if (getEjbExtensions()!= null) {
            // Validating property jdbcConnectionPool
            for (int _index = 0; _index < sizeEjbExtensions(); ++_index) {
                EjbExtensionsType element = getEjbExtensions(_index);
                if (element != null) {
                    element.validate();
                }
                
            }
        }
        if (getEjbJar()== null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getEjbJar() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EJB_JAR, this);	// NOI18N
        }
        if (getEjbJarHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getEjbJarHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, EJB_JAR, this);	// NOI18N
        }
        if (getNsEjb()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsEjb() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
        }
        if (getNsEjbExt()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getNsEjbExt() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, ROOT, this);	// NOI18N
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
        str.append(EJB_JAR);	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getApplication();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(EJB_JAR, 0, str, indent);
        
        str.append(indent);
        str.append(EJB_EXTENSIONS+"["+this.sizeEjbExtensions()+"]");	// NOI18N
        for(int i=0; i<this.sizeEjbExtensions(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (BaseBean) this.getEjbExtensions(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(EJB_EXTENSIONS, i, str, indent);
        }
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
