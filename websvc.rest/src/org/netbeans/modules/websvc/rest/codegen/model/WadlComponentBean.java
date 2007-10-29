/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Model bean for code generation of JAXWS operation wrapper resource class.
 * 
 * @author nam
 */
public class WadlComponentBean extends RestComponentBean {
    private String url;
    private RestComponentData data;
    private List<ParameterInfo> inputParams;
    
    public WadlComponentBean(RestComponentData data) throws IOException {
        super(deriveResourceName(data.getService().getName()), null, 
                deriveUriTemplate(data.getService().getName()), new MimeType[]{MimeType.XML}, 
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.data = data;
       
        init();
    }

    private void init() throws IOException {
        inputParams = new ArrayList<ParameterInfo>();
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        
        try {
            RestComponentData.Method m = data.getService().getMethods().get(0);
            String mName = m.getName();
            String resourceName = mName.substring(0, mName.lastIndexOf("/"));       //NOI18N
            URL mUrl = new URL(m.getUrl());
            String resource = mUrl.getPath();
            
            if (resource.length() > 0 && resource.charAt(0) == '/') { 
                resource = resource.substring(1); 
            }
            
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            URL t1 = loader.getResource(resource);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(URLMapper.findFileObject(t1).getInputStream());
            
            this.url = RestUtils.getAttributeValue(doc, "//resources", "base") +
                    RestUtils.getAttributeValue(doc, resourceName, "path");  //NOI18N
            
            NodeList resourceNodes = RestUtils.getNodeList(doc, resourceName);
            
            if (resourceNodes != null && resourceNodes.getLength() > 0) {
                Node resourceNode = resourceNodes.item(0);
                findParams(inputParams, resourceNode);
            }
            
            NodeList httpMethodNodes = RestUtils.getNodeList(doc, mName);
            
            if (httpMethodNodes != null && httpMethodNodes.getLength() > 0) {
                Node httpMethodNode = httpMethodNodes.item(0);                
                Attr href = (Attr) httpMethodNode.getAttributes().getNamedItem("href"); //NOI18N
            
                if(href != null) {
                    String id = href.getValue();
                    
                    if(id.startsWith("#")) {   //NOI18N
                        id = id.substring(1);
                    }
                    
                    NodeList gMethodNodes = RestUtils.getNodeList(doc,
                            "//"+httpMethodNode.getNodeName()+"[@id='"+id+"']"); //NOI18N
                    
                    if (gMethodNodes != null && gMethodNodes.getLength() > 0) {
                        httpMethodNode = gMethodNodes.item(0);
                    }
                }
                
                if(httpMethodNode != null) {
                    NodeList childs = httpMethodNode.getChildNodes();
                    if(childs != null) {
                        for(int i=0;i<childs.getLength();i++) {
                            Node child = childs.item(i);
                            String childName = child.getNodeName();
                            
                            if(childName.equals("request")) {    //NOI18N
                                findParams(inputParams, child);
                            } else if(childName.equals("response")) {   //NOI18M
                                findMediaType(child, mimeTypes);
                            }
                        }
                    }
                }
            }
            if(mimeTypes.size() > 0)
                this.setMimeTypes(mimeTypes.toArray(new MimeType[mimeTypes.size()]));
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }   
    }

    protected List<ParameterInfo> initInputParameters() {
        return inputParams;
    }
    
    public String getUrl() {
        return this.url;
    }

    private void findMediaType(Node child, List<MimeType> mimeTypes) throws DOMException {
        NodeList respChilds = child.getChildNodes();
        if (respChilds != null) {
            for (int j = 0; j < respChilds.getLength(); j++) {
                Node respChild = respChilds.item(j);
                if (respChild.getNodeName().equals("representation") && respChild.getAttributes() != null) {
                    Attr mediaAttr = (Attr) respChild.getAttributes().getNamedItem("mediaType");
                    if (mediaAttr != null && mediaAttr.getNodeValue() != null) {
                        String[] mTypes = mediaAttr.getNodeValue().split(",");
                        for(String m:mTypes) {
                            MimeType mType = MimeType.find(m);
                            if (mType != null) {
                                mimeTypes.add(mType);
                            }
                        }
                    }
                }
            }
        }
    }

    private void findParams(List<ParameterInfo> paramInfos, Node child) throws DOMException {
        NodeList params = child.getChildNodes();
        
        if (params != null) {
            for (int j = 0; j < params.getLength(); j++) {
                Node param = params.item(j);
                if (param.getAttributes() != null) {
                    String paramName = null;
                    Class paramType = null;
                    Object defaultValue = null;
                    Node attr = null;
                    
                    if ((attr = param.getAttributes().getNamedItem("name")) != null) {       //NOI18N
                        paramName = attr.getNodeValue();  //NOI18
                    } else {
                        continue;
                    }
                    
                    if ((attr = param.getAttributes().getNamedItem("type")) != null) {   //NOI18N
                        paramType = findJavaType(attr.getNodeValue());  //NOI18N
                        
                        if ((attr = param.getAttributes().getNamedItem("default")) != null) {      //NOI18N
                            defaultValue = getDefaultValue(attr.getNodeValue(), paramType);
                        }
                    }
                    
                    ParameterInfo paramInfo = new ParameterInfo(paramName, paramType);
                    paramInfo.setDefaultValue(defaultValue);
                    paramInfos.add(paramInfo);
                }
            }
        }
    }
       
    private Class findJavaType(String schemaType) {       
        if(schemaType != null) {
            int index = schemaType.indexOf(":");        //NOI18N
            
            if(index != -1) {
                schemaType = schemaType.substring(index+1);
            }
            
            if(schemaType.equals("string")) {     //NOI18N
                return String.class;
            } else if(schemaType.equals("int")) {       //NOI18N
                return Integer.class;
            }
        }
        
        return String.class;
    }
    
    private Object getDefaultValue(String value, Class type) {
        if (type == String.class) {
            return value;
        } else if (type == Integer.class) {
            return new Integer(Integer.parseInt(value));
        }
        
        return null;
    }
}
