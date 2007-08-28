/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.openide.filesystems.FileObject;
import javax.xml.xpath.*;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * REST support utilitities across all project types.
 *
 * @author Nam Nguyen
 */
public class RestUtils {
   
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
        restSupport.ensureRestDevelopmentReady();
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
    
    public static MetadataModel<RestServicesMetadata> getRestServicesMetadataModel(Project project) {
        return getRestSupport(project).getRestServicesMetadataModel();
    }
    
    public static void disableRestServicesChangeListner(Project project) {
        final MetadataModel<RestServicesMetadata> wsModel = RestUtils.getRestServicesMetadataModel(project);
        try {
            wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(final RestServicesMetadata metadata) {
                    metadata.getRoot().disablePropertyChangeListener();
                    return null;
                }
            });
        } catch (java.io.IOException ex) {
            
        }
    }
    
    public static void enableRestServicesChangeListner(Project project) {
     final MetadataModel<RestServicesMetadata> wsModel = RestUtils.getRestServicesMetadataModel(project);
        try {
            wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(final RestServicesMetadata metadata) {
                    metadata.getRoot().enablePropertyChangeListener();
                    return null;
                }
            });
        } catch (java.io.IOException ex) {
            
        }
    }

    public static void addRestApiJar(Project project) throws IOException {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        restSupport.addJSR311apiJar();
    }
            
    public static String getAttributeValue(Document doc, String nodePath, String attrName) throws XPathExpressionException {
        String attrValue = null;
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr3 = xpath.compile(nodePath+"/@"+attrName);
        Object result3 = expr3.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes3 = (NodeList) result3;
        for (int i = 0; i < nodes3.getLength(); i++) {
            attrValue = nodes3.item(i).getNodeValue();
            break;
        }
        return attrValue;
    }
    
    public static NodeList getNodeList(Document doc, String nodePath) throws XPathExpressionException {
        String attrValue = null;
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr3 = xpath.compile(nodePath);
        Object result3 = expr3.evaluate(doc, XPathConstants.NODESET);
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
        int index = name.lastIndexOf("Resource");
        if (index != -1) {
            name = name.substring(0, index);
        } else {
            index = name.lastIndexOf("Converter");
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
            if (Constants.URI_TEMPLATE.equals(cAnonType) || Constants.URI_TEMPLATE_ANNOTATION.equals(cAnonType)) {
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
                if (Constants.URI_TEMPLATE.equals(classAnonType)) {
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
                } else {
                    return false;
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
                    if (Constants.URI_TEMPLATE_ANNOTATION.equals(mAnonType) || Constants.URI_TEMPLATE.equals(mAnonType)) {
                        return true;
                    } else if (Constants.HTTP_METHOD_ANNOTATION.equals(mAnonType) || Constants.HTTP_METHOD.equals(mAnonType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static String findElementName(MethodTree tree, ClientStubModel.Resource r) {
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
            String mName = tree.getName().toString();
            ClientStubModel.Method m = null;
            boolean isHttpGetMethod = false;
            boolean isXmlMime = false;
            List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
            if (mAnons != null && mAnons.size() > 0) {
                for (AnnotationTree mAnon : mAnons) {
                    String mAnonType = mAnon.getAnnotationType().toString();
                    if (Constants.HTTP_METHOD_ANNOTATION.equals(mAnonType) || Constants.HTTP_METHOD.equals(mAnonType)) {
                        String value = getValueFromAnnotation(mAnon);
                        if (value.equals("GET")) {
                            isHttpGetMethod = true;
                        }
                    } else if (Constants.PRODUCE_MIME_ANNOTATION.equals(mAnonType) || Constants.PRODUCE_MIME.equals(mAnonType)) {
                        isXmlMime = true;
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
