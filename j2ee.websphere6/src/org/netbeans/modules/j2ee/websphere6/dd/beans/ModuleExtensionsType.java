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
public class ModuleExtensionsType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);




    /**
     * Creates a new instance of ModuleExtensionsType
     */
    public ModuleExtensionsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public ModuleExtensionsType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.createProperty(MODULE_ID, 	// NOI18N
                MODULE,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                java.lang.String.class);
        this.createAttribute(MODULE, XMI_TYPE_ID, MODULE_XMI_TYPE,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.createAttribute(MODULE, HREF_ID, MODULE_HREF,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    
    public void setModule(String value) {
        this.setValue(MODULE, value);
    }
    
    public String getModule() {
        return ( String)this.getValue(MODULE);
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
        if(getModule()==null) {
            setModule("");
        }
        setAttributeValue(MODULE, MODULE_HREF,DDXmi.addAppHrefToId(value));
    }
    
    public String getHref() {
        return DDXmi.getIdFromHref((String)getAttributeValue(MODULE, MODULE_HREF));
    }
    
    public void setModuleType(String value) {
        if(getModule()==null) {
            setModule("");            
        }
        for(int i=0;i<MODULE_TYPES.length;i++) {
            if(value.equals(MODULE_TYPES[i]))      {
                setAttributeValue(MODULE,MODULE_XMI_TYPE,MODULE_TYPE_STRINGS[i]);
                return;
            }
        }
        setAttributeValue(MODULE, MODULE_XMI_TYPE,value);
    }
    
    public String getModuleType() {
        String getValue=(String)getAttributeValue(MODULE,MODULE_XMI_TYPE);
        if(getValue==null) {
            return null;
        }
        for(int i=0;i<MODULE_TYPE_STRINGS.length;i++) {
            if(getValue.equals(MODULE_TYPE_STRINGS[i])) {
                return MODULE_TYPES[i];
            }
        }
        return getValue;
    }
    public void setType(String value) {
        setModuleExtensionsType(value);
        setModuleType(value);
    }
    public String getType() {
        String moduleType=getModuleType();
        String moduleExtType=getModuleExtensionsType();
        if(moduleType==null || moduleExtType==null) {
            return null;
        }
        if(moduleType.equals(moduleExtType)) {
            return moduleType;
        } else {
            return null;
        }
    }
    
    public void setXmiId(String value)  {
        setAttributeValue(MODULE_EXTENSIONS_XMI_ID,value);
    }
    
    public String getXmiId(){
        return (String)getAttributeValue(MODULE_EXTENSIONS_XMI_ID);
    }
    
    public String getModuleExtensionsType(){
        String getValue=(String)getAttributeValue(MODULE_EXTENSIONS_XMI_TYPE);
        for(int i=0;i<MODULE_EXTENSIONS_TYPE_STRINGS.length;i++) {
            if(getValue.equals(MODULE_EXTENSIONS_TYPE_STRINGS[i])) {
                return MODULE_TYPES[i];
            }
        }
        return getValue;
    }
    public void setModuleExtensionsType(String value)  {
        for(int i=0;i<MODULE_TYPES.length;i++) {
            if(value.equals(MODULE_TYPES[i]))      {
                setAttributeValue(MODULE_EXTENSIONS_XMI_TYPE,MODULE_EXTENSIONS_TYPE_STRINGS[i]);
                return;
            }
        }
        setAttributeValue(MODULE_EXTENSIONS_XMI_TYPE,value);
    }
    
    public void setAltRoot(String value)  {
        setAttributeValue(MODULE_EXTENSIONS_ALT_ROOT,value);
    }
    
    public String getAltRoot(){
        return (String)getAttributeValue(MODULE_EXTENSIONS_ALT_ROOT);
    }
    
    public void setAltBindings(String value)  {
        setAttributeValue(MODULE_EXTENSIONS_ALT_BINDINGS,value);
    }
    
    public String getAltBindings(){
        return (String)getAttributeValue(MODULE_EXTENSIONS_ALT_BINDINGS);
    }
    public void setAltExtensions(String value)  {
        setAttributeValue(MODULE_EXTENSIONS_ALT_EXTENSIONS,value);
    }
    
    public String getAltExtensions(){
        return (String)getAttributeValue(MODULE_EXTENSIONS_ALT_EXTENSIONS);
    }
    
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        
        if(getModule()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getModule() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "ModuleExtensions", this);	// NOI18N
        }
        if(getHref()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getHref() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "Module", this);	// NOI18N
        }
        if(getModuleType()==null) {
            throw new org.netbeans.modules.schema2beans.ValidateException("getModuleType() == null", org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, "Module", this);	// NOI18N
        }
        String type=getModuleType();
        if( (type!= MODULE_TYPE_WEB) &&
                (type!=MODULE_TYPE_EJB) &&
                (type!=MODULE_TYPE_JAVA_CLIENT) &&
                (type!=MODULE_TYPE_CONNECTOR)
                ){
            throw new org.netbeans.modules.schema2beans.ValidateException(
                    "getModuleType() != {" +
                    MODULE_TYPE_WEB+","+
                    MODULE_TYPE_EJB+","+
                    MODULE_TYPE_JAVA_CLIENT+","+
                    MODULE_TYPE_CONNECTOR+"}",
                    org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, MODULE, this);	// NOI18N
        }
        String extensionType=getModuleExtensionsType();
        if( (extensionType!= MODULE_TYPE_WEB) &&
                (extensionType!=MODULE_TYPE_EJB) &&
                (extensionType!=MODULE_TYPE_JAVA_CLIENT) &&
                (extensionType!=MODULE_TYPE_CONNECTOR)
                ){
            throw new org.netbeans.modules.schema2beans.ValidateException(
                    "getModuleExtensionType() != {" +
                    MODULE_TYPE_WEB+","+
                    MODULE_TYPE_EJB+","+
                    MODULE_TYPE_JAVA_CLIENT+","+
                    MODULE_TYPE_CONNECTOR+"}",
                    org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, MODULE_EXTENSIONS, this);	// NOI18N
        }
        if(extensionType!=type) {
            throw new org.netbeans.modules.schema2beans.ValidateException(
                    "getModuleExtensionType() != getModuleType()",
                    org.netbeans.modules.schema2beans.ValidateException.FailureType.NULL_VALUE, MODULE_EXTENSIONS, this);	// NOI18N
        }
    }
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Module");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getModule();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(MODULE, 0, str, indent);
        
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
}
