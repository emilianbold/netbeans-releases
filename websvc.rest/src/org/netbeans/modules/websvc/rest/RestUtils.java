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
package org.netbeans.modules.websvc.rest;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.openide.filesystems.FileObject;
import javax.xml.xpath.*;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.projects.WebProjectRestSupport;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * REST support utilitities across all project types.
 *
 * @author Nam Nguyen
 */
public class RestUtils {
   
    public static void upgrade(Project project) {
         RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            restSupport.upgrade();
        }
    }
    
    /**
     *  Makes sure project is ready for REST development.
     *  @param source source file or directory as part of REST application project.
     */
    public static void ensureRestDevelopmentReady(FileObject source) throws IOException {
        Project p = FileOwnerQuery.getOwner(source);
        if (p != null) {
            ensureRestDevelopmentReady(p);
        }
    }
    
    /**
     *  Makes sure project is ready for REST development.
     *  @param project project to make REST development ready
     */
    public static void ensureRestDevelopmentReady(Project project) throws IOException {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            restSupport.ensureRestDevelopmentReady();
        }
    }
    
    /**
     *  Makes sure project is ready for REST development.
     *  @param project project to make REST development ready
     */
    public static void removeRestDevelopmentReadiness(Project project) throws IOException {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null && restSupport.isRestSupportOn()) {
            restSupport.removeRestDevelopmentReadiness();
        }
    }
    
    /**
     *  Returns true if the project supports REST framework.
     *  @param project project to make REST development ready
     */
    public static boolean supportsRestDevelopment(Project project) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        return restSupport != null;
    }
    
    public static boolean isRestEnabled(Project project) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        return restSupport != null && restSupport.isRestSupportOn();
    }
    
    public static void setRestEnabled(Project project, Boolean v) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            restSupport.setRestSupport(v);
        }
    }
    
    public static RestSupport getRestSupport(Project project) {
        return project.getLookup().lookup(RestSupport.class);
    }
    
    public static RestServicesModel getRestServicesMetadataModel(Project project) {
        RestSupport support = getRestSupport(project);
        if (support != null) {
            return support.getRestServicesModel();
        }
        return null;
    }
    
    public static void disableRestServicesChangeListner(Project project) {
        final RestServicesModel wsModel = RestUtils.getRestServicesMetadataModel(project);
        if (wsModel == null) {
            return;
        }
        wsModel.disablePropertyChangeListener();
    }
    
    public static void enableRestServicesChangeListner(Project project) {
         final RestServicesModel wsModel = RestUtils.getRestServicesMetadataModel(project);
        if (wsModel == null) {
            return;
        }
         wsModel.enablePropertyChangeListener();
    }

    public static boolean hasJTASupport(Project project) {
        RestSupport support = getRestSupport(project);
        
        if (support != null) {
            return support.hasJTASupport();
        }
        
        return false;
    }
    
    public static boolean hasSpringSupport(Project project) {
        RestSupport support = getRestSupport(project);
        
        if (support != null) {
            return support.hasSpringSupport();
        }
        
        return false;
    }

    public static boolean isServerTomcat(Project project) {
        RestSupport support = getRestSupport(project);

        if (support != null) {
            return support.isServerTomcat();
        }

        return false;
    }

    public static boolean isServerGFV3(Project project) {
        RestSupport support = getRestSupport(project);

        if (support != null) {
            return support.isServerGFV3();
        }

        return false;
    }

    public static boolean isServerGFV2(Project project) {
        RestSupport support = getRestSupport(project);

        if (support != null) {
            return support.isServerGFV2();
        }

        return false;
    }

    public static Datasource getDatasource(Project project, String jndiName) {
        RestSupport support = getRestSupport(project);
        
        if (support != null) {
            return ((WebProjectRestSupport) support).getDatasource(jndiName);
        }
        
        return null;
    }
    
    //
    // TODO: The following methods don't belong here. Some of them should go into
    // JavaSourceHelper and the XML/DOM related methods should go into
    // their own utility class.
    //        
    public static String getAttributeValue(Node n, String nodePath, String attrName) throws XPathExpressionException {
        String attrValue = null;
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr3 = xpath.compile(nodePath+"/@"+attrName);
        Object result3 = expr3.evaluate(n, XPathConstants.NODESET);
        NodeList nodes3 = (NodeList) result3;
        for (int i = 0; i < nodes3.getLength(); i++) {
            attrValue = nodes3.item(i).getNodeValue();
            break;
        }
        return attrValue;
    }
    
    public static NodeList getNodeList(Node n, String nodePath) throws XPathExpressionException {
        String attrValue = null;
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr3 = xpath.compile(nodePath);
        Object result3 = expr3.evaluate(n, XPathConstants.NODESET);
        NodeList nodes3 = (NodeList) result3;
        return nodes3;
    }      
    
    public static FileObject findWadlFile(Project p) {
        FileObject pf = p.getProjectDirectory();
        FileObject f = pf.getFileObject("/build/web/WEB-INF/classes/com/sun/ws/rest/wadl/resource/application.wadl");
        return f;
    }

    public static DataObject createDataObjectFromTemplate(String template, 
            FileObject targetFolder, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() > 0;

        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);

        return templateDO.createFromTemplate(dataFolder, targetName);
    }

    public static String findStubNameFromClass(String className) {
        String name = className;
        int index = name.lastIndexOf(Constants.RESOURCE_SUFFIX);
        if (index != -1) {
            name = name.substring(0, index);
        } else {
            index = name.lastIndexOf(Constants.CONVERTER_SUFFIX);
            if (index != -1)
                name = name.substring(0, index);
        }
        return name;
    }

    public static String findUri(JavaSource rSrc) {
        String path = null;
        List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(rSrc);
        for (AnnotationMirror annotation : annotations) {
            String cAnonType = annotation.getAnnotationType().toString();
            if (RestConstants.PATH.equals(cAnonType) || RestConstants.PATH_ANNOTATION.equals(cAnonType)) {
                path = getValueFromAnnotation(annotation);
            }
        }
        return path;
    }

    public static boolean isStaticResource(JavaSource src) {
        List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(src);
        if (annotations != null && annotations.size() > 0) {
            for (AnnotationMirror annotation : annotations) {
                String classAnonType = annotation.getAnnotationType().toString();
                if (RestConstants.PATH.equals(classAnonType)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isConverter(JavaSource src) {
        List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(src);
        if (annotations != null && annotations.size() > 0) {
            for (AnnotationMirror annotation : annotations) {
                String classAnonType = annotation.getAnnotationType().toString();
                if (Constants.XML_ROOT_ELEMENT.equals(classAnonType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDynamicResource(JavaSource src) {
        List<MethodTree> trees = JavaSourceHelper.getAllMethods(src);
        for (MethodTree tree : trees) {
            List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
            if (mAnons != null && mAnons.size() > 0) {
                for (AnnotationTree mAnon : mAnons) {
                    String mAnonType = mAnon.getAnnotationType().toString();
                    if (RestConstants.PATH_ANNOTATION.equals(mAnonType) || RestConstants.PATH.equals(mAnonType)) {
                        return true;
                    } else if (RestConstants.GET_ANNOTATION.equals(mAnonType) || RestConstants.GET.equals(mAnonType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static String findElementName(MethodTree tree) {
        String eName = "";
        List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
        if (mAnons != null && mAnons.size() > 0) {
            for (AnnotationTree mAnon : mAnons) {
                eName = mAnon.toString();
                if (eName.indexOf("\"") != -1) {
                    eName = getValueFromAnnotation(mAnon);
                } else {
                    eName = getNameFromMethod(tree);
                }
            }
        }
        return eName.substring(0, 1).toLowerCase() + eName.substring(1);
    }

    public static MethodTree findGetAsXmlMethod(JavaSource rSrc) {
        MethodTree method = null;
        List<MethodTree> rTrees = JavaSourceHelper.getAllMethods(rSrc);
        for (MethodTree tree : rTrees) {
            boolean isHttpGetMethod = false;
            boolean isXmlMime = false;
            List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
            if (mAnons != null && mAnons.size() > 0) {
                for (AnnotationTree mAnon : mAnons) {
                    String mAnonType = mAnon.getAnnotationType().toString();
                    if (RestConstants.GET_ANNOTATION.equals(mAnonType) || RestConstants.GET.equals(mAnonType)) {
                        isHttpGetMethod = true;
                    } else if (RestConstants.PRODUCE_MIME_ANNOTATION.equals(mAnonType) || 
                            RestConstants.PRODUCE_MIME.equals(mAnonType)) {
                        List<String> mimes = getMimeAnnotationValue(mAnon);
                         if (mimes.contains(Constants.MimeType.JSON.value()) ||
                            mimes.contains(Constants.MimeType.XML.value())) {
                            isXmlMime = true;
                        }
                    }
                }
                if (isHttpGetMethod && isXmlMime) {
                    method = tree;
                    break;
                }
            }
        }
        return method;
    }

    public static String getNameFromMethod(MethodTree tree) {
        String attrName = tree.getName().toString();
        attrName = attrName.substring(attrName.indexOf("get") + 3);
        attrName = attrName.substring(0, 1).toLowerCase() + attrName.substring(1);
        return attrName;
    }
  
    public static String getValueFromAnnotation(AnnotationMirror annotation) {
        return getValueFromAnnotation(annotation.getElementValues().values().toString());
    }
     
    public static String getValueFromAnnotation(AnnotationTree mAnon) {
        return getValueFromAnnotation(mAnon.toString());
    }
    
    public static String getValueFromAnnotation(ExpressionTree eAnon) {
        return getValueFromAnnotation(eAnon.toString());
    }
    
    public static String getValueFromAnnotation(String value) {
        if (value.indexOf("\"") != -1)
            value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
        return value;
    }
    
    public static List<String> getMimeAnnotationValue(AnnotationTree ant) {
        List<? extends ExpressionTree> ets = ant.getArguments();
        if (ets.size() > 0) {
            String value = getValueFromAnnotation(ets.get(0));
            value = value.replace("\"", "");
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }     

    public static String createGetterMethodName(ClientStubModel.RepresentationNode n) {
        String mName = "get";
        if(n.getLink() != null)
            mName = escapeJSReserved(n.getLink().getName().toString());
        else {
            mName = n.getName();
            mName = "get"+mName.substring(0, 1).toUpperCase()+mName.substring(1);
        }
        return mName;
    }  
            
    public static String escapeJSReserved(String key) {
        if(key.equals("delete"))
            return key + "_";
        else
            return key;
    }    
}
