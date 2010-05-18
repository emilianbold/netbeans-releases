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

// BEGIN_NOI18N

public abstract class DDXmi extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants{

    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    private String ROOT;
    public DDXmi(String root) {
        this(null, Common.USE_DEFAULT_VALUES,root);
    }
    
    public DDXmi(org.w3c.dom.Node doc, int options,String root) {
        this(Common.NO_DEFAULT_VALUES,root);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    /*
    public static DDXmi createGraph(File f,boolean validate) throws java.io.IOException {return null;}
    public static DDXmi createGraph(InputStream in, boolean validate) {return null;}
    public static DDXmi createGraph() {return null;}
     */
    protected abstract void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException;
    /*
    {
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
    };*/
    public void setDefaults() {
    }
    
    public DDXmi(int options,String root){
        super(comparators, runtimeVersion);
        ROOT=root;
        initOptions(options);
    }
    
    
    protected abstract void initOptions(int options); //{     this.graphManager = new GraphManager(this);  }
    
    public static String getIdFromHref(String value) {
        if(value==null) return null;
        String index_strings[]=new String [] {APP_HREF_PREFIX, WEB_HREF_PREFIX, EJBJAR_HREF_PREFIX};
        for(int i=0;i<index_strings.length;i++) {
            int ind=value.indexOf(index_strings[i]);
            if(ind!=-1) {
                return  value.substring(ind+index_strings[i].length());
            }
        }
        return value;
    }
    
    public static String addAppHrefToId(String value){
        String newS=(value==null)?
            APP_HREF_PREFIX:
            (value.indexOf(APP_HREF_PREFIX)==-1?
                (APP_HREF_PREFIX+value):
                value);
        return newS;
    }
    
    public static String addWebHrefToId(String value){
        String newS=(value==null)?
            WEB_HREF_PREFIX:
            (value.indexOf(WEB_HREF_PREFIX)==-1?
                (WEB_HREF_PREFIX+value):
                value);
        return newS;
    }
    public static String addEjbJarHrefToId(String value){
        String newS=(value==null)?
            EJBJAR_HREF_PREFIX:
            (value.indexOf(EJBJAR_HREF_PREFIX)==-1?
                (EJBJAR_HREF_PREFIX+value):
                value);
        return newS;
    }
    public static String addHrefToId(String value,String hrefType) {
         if(hrefType.equals(WEB_APPLICATION)) {
            return addWebHrefToId(value);
        } else  if(hrefType.equals(EJB_JAR)) {
            return addEjbJarHrefToId(value);
        } else  if(hrefType.equals(APPLICATION)) {
            return addAppHrefToId(value);
        } else {
            return value;
        }
    }
    
    public void setApplication(String value) {
        setValue(APPLICATION,value);
    }
    public String getApplication() {
        return (String)this.getValue(APPLICATION);
    }
    
    public void setApplicationHref(String value) {
        if(getApplication()==null) {
            setApplication("");
        }
        setAttributeValue(APPLICATION,APPLICATION_HREF,addAppHrefToId(value));
    }
    
    public String getApplicationHref() {
        return  getIdFromHref((String)this.getAttributeValue(APPLICATION,APPLICATION_HREF));
    }
    
    
    public void setWebApplication(String value) {
        setValue(WEB_APPLICATION,value);
    }
    public String getWebApplication() {
        return (String)this.getValue(WEB_APPLICATION);
    }
    
    public void setWebApplicationHref(String value) {
        
        setAttributeValue(WEB_APPLICATION,WEB_APPLICATION_HREF,addWebHrefToId(value));
    }
    
    public String getWebApplicationHref() {
        return getIdFromHref((String)this.getAttributeValue(WEB_APPLICATION,WEB_APPLICATION_HREF));
        
    }
    
    
    
    
    public void setEjbJar(String value) {
        setValue(EJB_JAR,value);
    }
    
    public String getEjbJar() {
        return (String)this.getValue(EJB_JAR);
    }
    public void setEjbJarHref(String value) {
        
        setAttributeValue(EJB_JAR,EJB_JAR_HREF,addEjbJarHrefToId(value));
    }
    public String getEjbJarHref() {
        return getIdFromHref((String)this.getAttributeValue(EJB_JAR,EJB_JAR_HREF));
        
    }
    
    public void setXmiVersion() {
        setXmiVersion(XMI_VERSION_STRING);
    }
    public void setXmiVersion(String value) {
        setAttributeValue(XMI_VERSION,value);
    }
    public String getXmiVersion() {
        return (String)this.getAttributeValue(XMI_VERSION);
    }
    
    public void setNsXmi() {
        setNsXmi(NS_XMI_STRING);
    }
    public void setNsXmi(String value) {
        setAttributeValue(NS_XMI,value);
    }
    public String getNsXmi() {
        return (String)this.getAttributeValue(NS_XMI);
    }
    
    
    public void setNsXsi() {
        setNsXsi(NS_XSI_STRING);
    }
    public void setNsXsi(String value) {
        setAttributeValue(NS_XSI,value);
    }
    public String getNsXsi() {
        return (String)this.getAttributeValue(NS_XSI);
    }
    
    
    public void setReloadInterval() {
        setReloadInterval("3");
    }
    public void setReloadInterval(String value) {
        setAttributeValue(RELOAD_INTERVAL,value);
    }
    public String getReloadInterval() {
        String getString=(String)this.getAttributeValue(RELOAD_INTERVAL);
        return  (getString==null)?"0":getString;        
    }
    
    public void setReload() {
        setReload(true);
    }
    public void setReload(boolean value) {
        setAttributeValue(RELOAD_ENABLED,(value==true)?"true":"false");
    }
    public boolean getReload() {
        String getReloadString=this.getAttributeValue(RELOAD_ENABLED);
        if(getReloadString==null) return false;
        else return (getReloadString.equals("true")?true:false);
    }
    
    public void setAdditionalClassPath() {
        setAdditionalClassPath("");
    }
    public void setAdditionalClassPath(String value) {
        setAttributeValue(ADDITIONAL_CLASSPATH,value);
    }
    public String getAdditionalClassPath() {
        return (String)this.getAttributeValue(ADDITIONAL_CLASSPATH);
    }
    
    public void setFileServingEnabled() {
        setFileServingEnabled(true);
    }
    public void setFileServingEnabled(boolean value) {
        setAttributeValue(FILE_SERVING_ENABLED,(value==true)?"true":"false");
    }
    public boolean getFileServingEnabled() {
        return (this.getAttributeValue(FILE_SERVING_ENABLED).equals("true")?true:false);
    }
    
    
    public void setDirectoryBrowsing() {
        setDirectoryBrowsing(false);
    }
    public void setDirectoryBrowsing(boolean value) {
        setAttributeValue(DIRECTORY_BROWSING_ENABLED,(value==true)?"true":"false");
    }
    public boolean getDirectoryBrowsing() {
        return (this.getAttributeValue(DIRECTORY_BROWSING_ENABLED).equals("true")?true:false);
    }
    
    public void setServeServletsByClassname() {
        setServeServletsByClassname(true);
    }
    public void setServeServletsByClassname(boolean value) {
        setAttributeValue(SERVE_SERVLETS,(value==true)?"true":"false");
    }
    public boolean getServeServletsByClassname() {
        return this.getAttributeValue(SERVE_SERVLETS).equals("true")?true:false;
    }
    
    public boolean getPrecompileJSPs(){
        return (this.getAttributeValue(PRECOMPILE_JSPS).equals("true")?true:false);
    }
    public void setPrecompileJSPs(){
        setPrecompileJSPs(false);
    }
    public void setPrecompileJSPs(boolean value){
        this.setAttributeValue(PRECOMPILE_JSPS,(value==true)?"true":"false");
    }
    public boolean getAutoRequestEncoding(){
        return this.getAttributeValue(AUTO_REQUEST_ENCODING).equals("true")?true:false;
    }
    public void setAutoRequestEncoding(){
        setAutoRequestEncoding(true);
    }
    public void setAutoRequestEncoding(boolean value){
        this.setAttributeValue(AUTO_REQUEST_ENCODING,(value==true)?"true":"false");
    }
    public boolean getAutoResponseEncoding(){
        return this.getAttributeValue(AUTO_RESPONSE_ENCODING).equals("true")?true:false;
    }
    public void setAutoResponseEncoding(){
        setAutoResponseEncoding(true);
    }
    public void setAutoResponseEncoding(boolean value){
        this.setAttributeValue(AUTO_RESPONSE_ENCODING,value==true?"true":"false");
    }
    
    public boolean getAutoLoadFilters(){
        return this.getAttributeValue(AUTO_LOAD_FILTERS).equals("true")?true:false;
    }
    public void setAutoLoadFilters(){
        setAutoLoadFilters(true);
    }
    public void setAutoLoadFilters(boolean value){
        this.setAttributeValue(AUTO_LOAD_FILTERS,value==true?"true":"false");
    }
    
    
    
    public void setNsCommonextLocaltran() {
        setNsCommonextLocaltran(NS_COMMONEXT_LOCALTRAN_STRING);
    }
    public void setNsCommonextLocaltran(String value) {
        setAttributeValue(NS_COMMONEXT_LOCALTRAN,value);
    }
    public String getNsCommonextLocaltran() {
        return (String)this.getAttributeValue(NS_COMMONEXT_LOCALTRAN);
    }
    
    
    
    
    public void setNsAppBnd() {
        setNsAppBnd(NS_APP_BND_STRING);
    }
    public void setNsAppBnd(String value) {
        setAttributeValue(NS_APP_BND,value);
    }
    public String getNsAppBnd() {
        return (String)this.getAttributeValue(NS_APP_BND);
    }
    
    
    
    public void setNsAppExt() {
        setNsAppExt(NS_APP_EXT_STRING);
    }
    public void setNsAppExt(String value) {
        setAttributeValue(NS_APP_EXT,value);
    }
    public String getNsAppExt() {
        return (String)this.getAttributeValue(NS_APP_EXT);
    }
    
    
    public void setNsEjb() {
        setNsEjb(NS_EJB_STRING);
    }
    public void setNsEjb(String value) {
        setAttributeValue(NS_EJB,value);
    }
    public String getNsEjb() {
        return (String)this.getAttributeValue(NS_EJB);
    }
    
    public void setNsEjbBnd() {
        setNsEjbBnd(NS_EJB_BND_STRING);
    }
    public void setNsEjbBnd(String value) {
        setAttributeValue(NS_EJB_BND,value);
    }
    public String getNsEjbBnd() {
        return (String)this.getAttributeValue(NS_EJB_BND);
    }
    
    
    
    public void setNsEjbExt() {
        setNsEjbExt(NS_EJB_EXT_STRING);
    }
    public void setNsEjbExt(String value) {
        setAttributeValue(NS_EJB_EXT,value);
    }
    public String getNsEjbExt() {
        return (String)this.getAttributeValue(NS_EJB_EXT);
    }
    
    public void setNsWebApp() {
        setNsWebApp(NS_WEB_APP_STRING);
    }
    public void setNsWebApp(String value) {
        setAttributeValue(NS_WEB_APP,value);
    }
    public String getNsWebApp() {
        return (String)this.getAttributeValue(NS_WEB_APP);
    }
    
    
    
    public void setNsWebAppExt() {
        setNsWebAppExt(NS_WEB_APP_EXT_STRING);
    }
    public void setNsWebAppExt(String value) {
        setAttributeValue(NS_WEB_APP_EXT,value);
    }
    public String getNsWebAppExt() {
        return (String)this.getAttributeValue(NS_WEB_APP_EXT);
    }
    
    
    public void setNsWebAppBnd() {
        setNsWebAppBnd(NS_WEB_APP_BND_STRING);
    }
    public void setNsWebAppBnd(String value) {
        setAttributeValue(NS_WEB_APP_BND,value);
    }
    public String getNsWebAppBnd() {
        return (String)this.getAttributeValue(NS_WEB_APP_BND);
    }
    
    public void setNsCommon() {
        setNsCommon(NS_COMMON_STRING);
    }
    public void setNsCommon(String value) {
        setAttributeValue(NS_COMMON,value);
    }
    public String getNsCommon() {
        return (String)this.getAttributeValue(NS_COMMON);
    }
    
    public void setNsCommonBnd() {
        setNsCommonBnd(NS_COMMON_BND_STRING);
    }
    public void setNsCommonBnd(String value) {
        setAttributeValue(NS_COMMON_BND,value);
    }
    public String getNsCommonBnd() {
        return (String)this.getAttributeValue(NS_COMMON_BND);
    }
    
    public void setNsApp(){
        setNsApp(NS_APP_STRING);
    }
    public void setNsApp(String value) {
        setAttributeValue(NS_APP,value);
    }
    public String getNsApp() {
        return (String)this.getAttributeValue(NS_APP);
    }
    
    
    public void setXmiId(String value) {
        setAttributeValue(XMI_ID,value);
    }
    public String getXmiId() {
        return (String)this.getAttributeValue(XMI_ID);
    }
    
    
    // Setting the default values of the properties
    void initialize(int options) {};
    
    // Special serializer: output XML as serialization
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos);
        String str = baos.toString();;
        // System.out.println("str='"+str+"'");
        out.writeUTF(str);
    }
    // Special deserializer: read XML as deserialization
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
        try{
            init(comparators, runtimeVersion);
            String strDocument = in.readUTF();
            // System.out.println("strDocument='"+strDocument+"'");
            ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
            Document doc = GraphManager.createXmlDocument(bais, false);
            initOptions(Common.NO_DEFAULT_VALUES);
            initFromNode(doc, Common.NO_DEFAULT_VALUES);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void _setSchemaLocation(String location) {
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
        }
        setAttributeValue("xsi:schemaLocation", location);
    }
    
    public String _getSchemaLocation() {
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
        }
        return getAttributeValue("xsi:schemaLocation");
    }
    
    
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
        
    }
    public String dumpBeanNode(){
        return null;
    }
    
// END_NOI18N
}
