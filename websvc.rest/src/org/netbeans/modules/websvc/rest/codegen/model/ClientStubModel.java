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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.lang.model.element.AnnotationMirror;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 * ClientStubModel
 *
 * @author Ayub Khan
 */
public class ClientStubModel {
    
    public static final int EXPAND_LEVEL_MAX = 2;

    public ClientStubModel() {        
    }
    
    public ResourceModel createModel(Project p) {
        return new SourceModeler(p);
    }
    
    public ResourceModel createModel(InputStream is) {
        return new WadlModeler(is);
    }

    public static String normalizeName(final String name) {
        return toValidJavaName(name);
    }

    private static String toValidJavaName(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        if (Character.isJavaIdentifierStart(name.charAt(0))) {
            sb.append(name.charAt(0));
        } else {
            sb.append("_");
        }
        for (int i=1; i<name.length(); i++) {
            if (Character.isJavaIdentifierPart(name.charAt(i))) {
                sb.append(name.charAt(i));
            } else {
                sb.append("_");
            }
        }
        return sb.toString();
    }
    
    public abstract class ResourceModel {

        private List<Resource> resourceList = new ArrayList<Resource>();
        
        public List<Resource> getResources() {
            return resourceList;
        }

        public void addResource(Resource m) {
            resourceList.add(m);
        }
        
        public abstract void build() throws IOException;
    }

    public class SourceModeler extends ResourceModel {

        private Project p;
        private SortedMap<String, JavaSource> srcMap = new TreeMap<String, JavaSource>();
        private Map<String, RepresentationDocument> documentMap = 
                new HashMap<String, RepresentationDocument>();

        public SourceModeler(Project p) {
            this.p = p;
        }

        private JavaSource getJavaSource(String className) {
            return srcMap.get(className);
        }

        public void build() throws IOException {
            List<JavaSource> sources = JavaSourceHelper.getJavaSources(p);
            List<JavaSource> staticR = new ArrayList<JavaSource>();
            List<JavaSource> dynamicR = new ArrayList<JavaSource>();
            List<JavaSource> converters = new ArrayList<JavaSource>();
            for (JavaSource src : sources) {
                if (JavaSourceHelper.isEntity(src)) {
                    continue;
                }
                String className = JavaSourceHelper.getClassNameQuietly(src);
                if (className == null) {
                    continue;
                }
                srcMap.put(className, src);
                if (RestUtils.isStaticResource(src)) {
                    staticR.add(src);
                } else if (RestUtils.isConverter(src)) {
                    converters.add(src);
                } else if (RestUtils.isDynamicResource(src)) {
                    dynamicR.add(src);
                }
            }
            for (JavaSource rSrc : staticR) {
                Resource r = createResource(rSrc);
                addResource(r);
            }
            for (JavaSource rSrc : dynamicR) {
                Resource r = createResource(rSrc);
                addResource(r);
            }
        }

        private Resource createResource(JavaSource rSrc) throws IOException {
            String name = null;
            String path = null;
            String template = RestUtils.findUri(rSrc);
            if (template != null) {
                path = template;
                name = path;
                if (name.startsWith("/")) {
                    name = name.substring(1);
                }
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            } else {
                String className = JavaSourceHelper.getClassNameQuietly(rSrc);
                name = RestUtils.findStubNameFromClass(className);
                path = name.substring(0, 1).toLowerCase() + name.substring(1);
            }
            Resource r = new Resource(normalizeName(name), path);
            buildResource(r, rSrc);
            return r;
        }

        private void buildResource(Resource r, JavaSource src) throws IOException {
            r.setSource(src);
            List<MethodTree> trees = JavaSourceHelper.getAllMethods(src);
            for (MethodTree tree : trees) {
                String mName = tree.getName().toString();
                Method m = null;
                List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
                if (mAnons != null && mAnons.size() > 0) {
                    for (AnnotationTree mAnon : mAnons) {
                        String mAnonType = mAnon.getAnnotationType().toString();
                        List<? extends ExpressionTree> eAnons = mAnon.getArguments();
                        if (RestConstants.PATH_ANNOTATION.equals(mAnonType) || RestConstants.PATH.equals(mAnonType)) {
                            m = processPathAnnotation(r, m, mName, tree, eAnons);
                        } else if (RestConstants.PRODUCE_MIME_ANNOTATION.equals(mAnonType) || RestConstants.PRODUCE_MIME.equals(mAnonType)) {
                            m = processMimeAnnotation(m, mName, tree, eAnons, MethodType.GET);
                        } else if (RestConstants.CONSUME_MIME_ANNOTATION.equals(mAnonType) || RestConstants.CONSUME_MIME.equals(mAnonType)) {
                            m = processMimeAnnotation(m, mName, tree, eAnons, MethodType.POST);
                        } else if (m == null) {
                            if (RestConstants.GET_ANNOTATION.equals(mAnonType) || RestConstants.GET.equals(mAnonType)) {
                                m = createMethodFromAnnotation(mName, tree, MethodType.GET);
                            } else if (RestConstants.POST_ANNOTATION.equals(mAnonType) || RestConstants.POST.equals(mAnonType)) {
                                m = createMethodFromAnnotation(mName, tree, MethodType.POST);
                            } else if (RestConstants.PUT_ANNOTATION.equals(mAnonType) || RestConstants.PUT.equals(mAnonType)) {
                                m = createMethodFromAnnotation(mName, tree, MethodType.PUT);
                            } else if (RestConstants.DELETE_ANNOTATION.equals(mAnonType) || RestConstants.DELETE.equals(mAnonType)) {
                                m = createMethodFromAnnotation(mName, tree, MethodType.DELETE);
                            }
                        }
                    }
                }
                if (m != null) {
                    r.addMethod(m);
                }
            }
            buildRepresentationDocument(r, src);
        }

        private Method createMethodFromAnnotation(String name, MethodTree tree, MethodType type) {
            Method m = createMethod(name, tree);
            m.setType(type);
            return m;
        }

        private Method processMimeAnnotation(Method m, String name, MethodTree tree,
                List<? extends ExpressionTree> eAnons, MethodType type) {
            if (m == null) {
                m = createMethod(name, tree);
            }
            for (ExpressionTree eAnon : eAnons) {
                String value = RestUtils.getValueFromAnnotation(eAnon);
                String[] mimeTypes = value.split(",");
                for (String mimeType : mimeTypes) {
                    mimeType = mimeType.trim();
                    if (mimeType.startsWith("\"")) {
                        mimeType = mimeType.substring(1);
                    }
                    if (mimeType.endsWith("\"")) {
                        mimeType = mimeType.substring(0, mimeType.length() - 1);
                    }
                    Representation rep = new Representation(mimeType);
                    if (type == MethodType.GET) {
                        if (!m.getResponse().containsRepresentation(rep)) {
                            m.getResponse().addRepresentation(rep);
                        }
                    } else {
                        if (!m.getRequest().containsRepresentation(rep)) {
                            m.getRequest().addRepresentation(rep);
                        }
                    }
                }
            }
            return m;
        }

        private Method processPathAnnotation(Resource r, Method m, String name, MethodTree tree,
                List<? extends ExpressionTree> eAnons) throws IOException {
            m = createNavigationMethod(name);
            m.setTree(tree);
            m.setType(MethodType.GETCHILD);
            for (ExpressionTree eAnon : eAnons) {
                String value = eAnon.toString();
                if (value.contains("{")) {
                    String childRes = tree.getReturnType().toString();
                    if (childRes.indexOf(".") != -1) {
                        childRes = childRes.substring(childRes.lastIndexOf(".") + 1);
                    }
                    if (childRes.indexOf(Constants.RESOURCE_SUFFIX) != -1) {
                        childRes = childRes.substring(0, childRes.indexOf(Constants.RESOURCE_SUFFIX));
                    }
                    String rName = r.getName();
                    if (Util.pluralize(childRes).equals(rName)) {
                        r.setIsContainer(true);
                    }
                    value = value.substring(value.indexOf("{") + 1, value.lastIndexOf("}"));
                } else if (value.contains("\"") && value.contains("/")) {
                    value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("/"));
                } else {
                    throw new IOException("Cannot find method with " +
                            "@Path(\"subresource/\") or @Path(\"{containerId}/\")");
                }
                ((NavigationMethod) m).setNavigationUri(value);
            }
            String returnType = tree.getReturnType().toString();
            if (returnType != null) {
                int ndx = returnType.lastIndexOf(".");
                if (ndx != -1) {
                    returnType = returnType.substring(ndx + 1);
                }
            }
            String linkName = RestUtils.findStubNameFromClass(returnType);
            ((NavigationMethod) m).setLinkName(linkName);
            return m;
        }

        private Method createMethod(String mName, MethodTree tree) {
            Method m = new Method(mName);
            Representation rep1 = new Representation(Constants.MimeType.XML.value());
            Request request = new Request();
            request.addRepresentation(rep1);
            m.setRequest(request);
            Representation rep2 = new Representation(Constants.MimeType.XML.value());
            Response response = new Response();
            response.addRepresentation(rep2);
            m.setResponse(response);
            m.setTree(tree);
            return m;
        }

        private Method createNavigationMethod(String mName) {
            Method m = new NavigationMethod(mName);
            Representation rep1 = new Representation(Constants.MimeType.XML.value());
            Request request = new Request();
            request.addRepresentation(rep1);
            m.setRequest(request);
            Representation rep2 = new Representation(Constants.MimeType.XML.value());
            Response response = new Response();
            response.addRepresentation(rep2);
            m.setResponse(response);
            return m;
        }

        private void buildRepresentationDocument(Resource r, JavaSource rSrc) throws IOException {
            RepresentationDocument rDoc = r.getRepresentation();
            /*
             * Find a getXml method from resource source rSrc. The return type will be its converter
             * Eg: 
             *  @HttpMethod("GET")
             *  @ProduceMime(Constants.MimeType.XML.value())
             *  public PlaylistsConverter getXml() {}
             */
            MethodTree method = RestUtils.findGetAsXmlMethod(rSrc); // getXml() method tree
            if (method != null) {
                String converter = method.getReturnType().toString();

                RepresentationNode rootNode = null;
                JavaSource js = getJavaSource(converter);
                if (js != null) {
                    rootNode = createRootNode(js, rDoc);
                } else { // check classpath
                    Class converterClass = TypeUtil.getClass(converter, rSrc, p);
                    if (converterClass != null) {
                        rootNode = createRootNode(converterClass, rDoc);
                    }
                }
                rDoc.setRoot(rootNode);
                documentMap.put(converter, rDoc);
                if (rootNode != null) {
                    rootNode.setIsContainer(r.isContainer());
                    int expandLevel = 0;
                    processConverter(converter, rDoc, rootNode, r, expandLevel);
                }
            }
        }

        private RepresentationNode createRootNode(JavaSource cSrc, RepresentationDocument rr) {
            List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(cSrc);
            if (annotations == null) {
                return null;
            }
            for (int i = 0; i < annotations.size(); i++) {
                AnnotationMirror annotation = annotations.get(i);
                String cAnonType = annotation.getAnnotationType().toString();
                if (Constants.XML_ROOT_ELEMENT_ANNOTATION.equals(cAnonType) || Constants.XML_ROOT_ELEMENT.equals(cAnonType)) {
                    /*
                     *  @XmlRootElement(name = "playlists")
                     *  public class PlaylistsConverter {}
                     */
                    String rootName = RestUtils.getValueFromAnnotation(annotation); // "playlists" 
                    RepresentationNode rootNode = new RepresentationNode(rootName);
                    rootNode.setIsRoot(true);
                    return rootNode;
                }
            }
            return null;
        }

        private RepresentationNode createRootNode(Class converterClass, RepresentationDocument rr) {
            List<Annotation> annotations = TypeUtil.getAnnotations(converterClass, true);
            for (Annotation annotation : annotations) {
                String cAnonType = annotation.annotationType().getName();
                if (Constants.XML_ROOT_ELEMENT_ANNOTATION.equals(cAnonType) ||
                        Constants.XML_ROOT_ELEMENT.equals(cAnonType)) {
                    String rootName = TypeUtil.getAnnotationValueName(annotation);
                    RepresentationNode rootNode = new RepresentationNode(rootName);
                    rootNode.setIsRoot(true);
                    return rootNode;
                }
            }
            return null;
        }

        private String findBaseClassName(String name) {
            String cName = name;
            if (cName.indexOf("Collection<") > -1 || cName.indexOf("List<") > -1 ||
                    cName.indexOf("Set<") > -1) {
                if (cName.indexOf("<") != -1) {
                    cName = cName.substring(cName.indexOf("<") + 1, cName.indexOf(">"));
                }
            }
            return cName;
        }

        private void processConverter(String converter, RepresentationDocument rr,
                RepresentationNode rElem, Resource r, int expandLevel) throws IOException {
            assert converter != null;
            String cName = findBaseClassName(converter);
            JavaSource cSrc = getJavaSource(cName);
            if (cSrc == null) {
                Class cClass = TypeUtil.getClass(cName, r.getSource(), p);
                if (cClass != null) {
                    processConverter(cClass, rr, rElem, r, expandLevel);
                }
            } else {
                processConverter(cSrc, rr, rElem, r, expandLevel);
            }
        }

        private void processConverter(JavaSource cSrc, RepresentationDocument rr,
                RepresentationNode rElem, Resource r, int expandLevel) throws IOException {
            if (expandLevel++ == EXPAND_LEVEL_MAX) {
                return;
            }
            List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(cSrc);
            if (annotations == null || annotations.size() == 0) {
                return;
            }
            for (int i = 0; i < annotations.size(); i++) {
                AnnotationMirror annotation = annotations.get(i);
                String cAnonType = annotation.getAnnotationType().toString();
                if (Constants.XML_ROOT_ELEMENT_ANNOTATION.equals(cAnonType) || Constants.XML_ROOT_ELEMENT.equals(cAnonType)) {
                    rElem.setSource(cSrc);
                    List<MethodTree> cTrees = JavaSourceHelper.getAllMethods(cSrc);
                    if (cTrees == null) {
                        continue;
                    }
                    List<RepresentationNode> elems = new ArrayList<RepresentationNode>();
                    List<String> rTypes = new ArrayList<String>();
                    for (int j = 0; j < cTrees.size(); j++) {
                        MethodTree tree = cTrees.get(j);
                        List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
                        if (mAnons == null) {
                            continue;
                        }
                        for (int k = 0; k < mAnons.size(); k++) {
                            AnnotationTree mAnon = mAnons.get(k);
                            String mAnonType = mAnon.getAnnotationType().toString();
                            if (Constants.XML_ELEMENT_ANNOTATION.equals(mAnonType) || Constants.XML_ELEMENT.equals(mAnonType)) {
                                /*
                                 *  @XmlElement(name = "playlistRef")
                                 *  public Collection<PlaylistRefConverter> getReferences() {}
                                 */
                                String c = tree.getReturnType().toString();
                                RepresentationDocument cDoc = documentMap.get(c);
                                RepresentationNode cElem = null;
                                String eName = RestUtils.findElementName(tree); // "playlistRef"
                                if (cDoc == null || cDoc.getRoot() == null) {
                                    String rName = findRepresentationNameFromClass(c);
                                    if (rName == null || rName.length() == 0) {
                                        rName = eName;
                                    }
                                    cElem = new RepresentationNode(rName);
                                    cElem.setId(eName);
                                    cElem.setLink(tree); //getReferences() method tree for tracking the link                                
                                    elems.add(cElem);//process later
                                    rTypes.add(c);//process later                                
                                } else {
                                    cElem = cDoc.getRoot().clone();
                                    cElem.setId(eName);
                                }
                                rElem.addChild(cElem);
                            } else if (Constants.XML_ATTRIBUTE_ANNOTATION.equals(mAnonType) || Constants.XML_ATTRIBUTE.equals(mAnonType)) {
                                /*
                                 *  @XmlAttribute
                                 *  public URI getUri() {}
                                 */
                                String attrName = RestUtils.findElementName(tree); // "uri"
                                RepresentationNode attr = new RepresentationNode(attrName);
                                attr.setLink(tree);
                                rElem.addAttribute(attr);
                            }
                        }
                    }

                    //now process element methods
                    for (int j = 0; j < elems.size(); j++) {
                        processConverter(rTypes.get(j), rr, elems.get(j), r, expandLevel);
                    }
                }
            }
        }

        private String findRepresentationNameFromClass(String name) {
            String cName = findBaseClassName(name);
            JavaSource cSrc = getJavaSource(cName);
            if (cSrc != null) {
                cName = RestUtils.findStubNameFromClass(cName);
                return cName.substring(0, 1).toLowerCase() + cName.substring(1);
            }
            return null;
        }

        private void processConverter(Class converterClass, RepresentationDocument rr,
                RepresentationNode rElem, Resource r, int expandLevel) throws IOException {
            if (expandLevel++ == EXPAND_LEVEL_MAX) {
                return;
            }
            rElem.setSource(null);
            if (!TypeUtil.isXmlRoot(converterClass)) {
                return;
            }
            List<RepresentationNode> elems = new ArrayList<RepresentationNode>();
            for (java.lang.reflect.Method method : converterClass.getMethods()) {
                String c = method.getGenericReturnType().toString();
                String defaultName = method.getName();
                if (defaultName.startsWith("get")) { //NOI18N
                    defaultName = defaultName.substring(3);
                }
                RepresentationDocument cDoc = documentMap.get(c);
                Annotation xmlelementAnn = TypeUtil.getXmlElementAnnotation(method);
                if (xmlelementAnn != null) {
                    collectElement(xmlelementAnn, rElem, cDoc, elems, c, defaultName);
                }
            }

            for (Field field : converterClass.getDeclaredFields()) {
                String c = field.getGenericType().toString();
                RepresentationDocument cDoc = documentMap.get(c);
                Annotation xmlelementAnn = TypeUtil.getXmlElementAnnotation(field);
                if (xmlelementAnn != null) {
                    collectElement(xmlelementAnn, rElem, cDoc, elems, c, field.getName());
                }
                Annotation xmlAttributeAnn = TypeUtil.getXmlAttributeAnnotation(field);
                if (xmlAttributeAnn != null) {
                    collectAttribute(xmlAttributeAnn, rElem, field.getName());
                }
            }

            //now process element methods
            for (int j = 0; j < elems.size(); j++) {
                processConverter(elems.get(j).getType(), rr, elems.get(j), r, expandLevel);
            }
        }

        private void collectElement(Annotation xmlAnn, RepresentationNode rElem, RepresentationDocument cDoc,
                List<RepresentationNode> elems, String type, String defaultName) {

            /*
             *  @XmlElement(name = "playlistRef")
             *  public Collection<PlaylistRefConverter> getReferences() {}
             */
            RepresentationNode cElem = null;
            if (cDoc == null || cDoc.getRoot() == null) {
                String eName = TypeUtil.getAnnotationValueName(xmlAnn); // "playlistRef"
                if (eName == null) {
                    eName = defaultName;
                }
                cElem = new RepresentationNode(eName);
                cElem.setType(type);
                cElem.setLink(null); //getReferences() method tree for tracking the link                                
                elems.add(cElem);//process later
            } else {
                cElem = cDoc.getRoot();
            }
            rElem.addChild(cElem);
        }

        private void collectAttribute(Annotation xmlAnn, RepresentationNode rElem, String defaultName) {
            /*
             *  @XmlAttribute
             *  public URI getUri() {}
             */
            String attrName = TypeUtil.getAnnotationValueName(xmlAnn); // "uri"
            if (attrName == null || attrName.equals("##default")) { //NOI18N
                attrName = defaultName;
            }
            RepresentationNode attr = new RepresentationNode(attrName);
            attr.setLink(null);
            rElem.addAttribute(attr);
        }
    }

    public class WadlModeler extends ResourceModel {

        private Map<String, Resource> staticRMap = new HashMap<String, Resource>();
        private InputStream is;
        private String baseUrl;

        public WadlModeler(InputStream is) {
            this.is = is;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }

        public void build() throws IOException {
            List<Node> staticR = new ArrayList<Node>();
            List<Node> dynamicR = new ArrayList<Node>();
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);

                Node app = doc.getDocumentElement();
                Node resources = getChildNodes(app, "resources").get(0);
                
                //App base
                this.baseUrl = getAttributeValue(resources, "base");

                //Resources
                List<Node> resourceNodes = getChildNodes(resources, "resource");
                for (Node s: resourceNodes) {
                    staticR.add(s);
                }
                
                //Sub-Resources
                for (Node sNode : staticR) {
                    List<Node> subRNodes = getChildNodes(sNode, "resource");
                    for (Node d:subRNodes) {
                        dynamicR.add(d);
                        NodeList resNodes = RestUtils.getNodeList(d, "*/resource");
                        if (resNodes != null) {
                            for (int i = 0; i < resNodes.getLength(); i++) {
                                dynamicR.add(resNodes.item(i));
                            }
                        }
                    }
                }
                
                for (Node sNode : staticR) {
                    Resource r = createResource(doc, sNode);
                    staticRMap.put(r.getName(), r);
                    addResource(r);
                }
                for (Node dNode : dynamicR) {
                    Resource r = createResource(doc, dNode);
                    String rName = r.getName();
                    if(rName.endsWith(Constants.COLLECTION))
                        rName = rName.substring(0, rName.indexOf(Constants.COLLECTION))+"s";
                    if(staticRMap.get(rName) == null)
                        addResource(r);
                }
                
                //If any resource is a container, then remove trailing '/' on baseUrl
                for (Resource r : getResources()) {
                    if (r.isContainer() && this.baseUrl.endsWith("/")) {
                        this.baseUrl = this.baseUrl.substring(0, this.baseUrl.length()-1);
                        break;
                    }
                }
            } catch (Exception ex) {
                throw new IOException(ex.getMessage());
            }
        }

        private Resource createResource(Document doc, Node n) throws IOException {
            try {
                String path = getAttributeValue(n, "path");
                String name = findResourceNameFromPath(path);
                if (isContainerItem(n)) {
                    Node p = n.getParentNode();
                    String pName = findResourceNameFromPath(getAttributeValue(p, "path"));
                    pName = pName.substring(0, pName.length()-1);
                    if(name.startsWith(pName)) {
                        name = pName;
                        path = "/"+name+"/";
                    }
                }
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                Resource r = new Resource(name, path);
                buildResource(r, doc, n);
                return r;
            } catch (Exception ex) {
                throw new IOException(ex.getMessage());
            }
        }
        
        private String findResourceNameFromPath(String path) {
            String name = path.replaceAll("/", "");
            name = name.replace("{", "");
            name = name.replace("}", "");
            return normalizeName(name);
        }
            
        private void buildResource(Resource r, Document doc, Node n) throws IOException {
            try {
                //Methods
                NodeList methods = RestUtils.getNodeList(n, "method");
                if (methods != null && methods.getLength() > 0) {
                    for (int j = 0; j < methods.getLength(); j++) {
                        Node method = methods.item(j);
                        String mName = getAttributeValue(method, "name");
                        if (mName == null) {
                            mName = getAttributeValue(method, "href");
                            if (mName == null) {
                                throw new IOException("Method do not have name or " +
                                        "href attribute for resource: " + r.getName());
                            } else {
                                String ref = mName;
                                if (ref.startsWith("#")) {
                                    ref = ref.substring(1);
                                }
                                method = findMethodNodeByRef(doc, ref);
                                mName = getAttributeValue(method, "name");
                                addMethod(r, mName, method);
                            }
                        } else {
                            addMethod(r, mName, method);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new IOException(ex.getMessage());
            }
            buildRepresentationDocument(r, n);
        }
        
        private void buildRepresentationDocument(Resource r, Node n) throws IOException {
            RepresentationDocument rDoc = r.getRepresentation();
            if(isContainer(n)) {
                RepresentationNode rootNode = createRootNode(findResourceNameFromPath(r.getPath()));
                rDoc.setRoot(rootNode);
                rootNode.setIsContainer(r.isContainer());
                int expandLevel = 0;
                processSubResource(n, rDoc, rootNode, r, expandLevel);
            } else if(isContainerItem(n)) {
                RepresentationNode rootNode = createRootNode(findResourceNameFromPath(r.getPath()));
                rDoc.setRoot(rootNode);
                
                //Template params as fields
                List<Node> params = getChildNodes(n, "param");
                for (Node param:params) {
                    String eName = getAttributeValue(param, "name");
                    RepresentationNode cElem = new RepresentationNode(eName);
                    rootNode.addChild(cElem);
                }
            }
        }

        private RepresentationNode createRootNode(String name) { 
            RepresentationNode rootNode = new RepresentationNode(name);
            rootNode.setIsRoot(true);
            return rootNode;
        }

        private void processSubResource(Node n, RepresentationDocument rr,
                RepresentationNode rElem, Resource r, int expandLevel) throws IOException {
            List<Node> childNodes = getChildNodes(n, "resource");
            for(Node child : childNodes) {
                if(isContainerItem(child)) {
                    r.setIsContainer(true);
                    String name = findResourceNameFromPath(getAttributeValue(child, "path"));
                    String pName = findResourceNameFromPath(getAttributeValue(n, "path"));
                    pName = pName.substring(0, pName.length()-1);
                    if(name.startsWith(pName)) {
                        name = pName;
                    }
                    rElem.addChild(createRootNode(name));
                }
            }
        }
        
        private boolean isContainer(Node n) {
            List<Node> childNodes = getChildNodes(n, "resource");
            for(Node child : childNodes) {
                if(isContainerItem(child)) {
                    return true;
                }
            }
            return false;
        }
        
        private boolean isContainerItem(Node n) {
            String path = getAttributeValue(n, "path");
            return path.contains("{") && path.contains("}");
        }
        
        private List<Node> getChildNodes(Node n, String name) {
            List<Node> childNodes = new ArrayList<Node>();
            NodeList childs = n.getChildNodes();
            if(childs != null) {
                for(int i=0;i<childs.getLength();i++) {
                    Node child = childs.item(i);
                    String cName = child.getNodeName();
                    if(cName.indexOf(":")!=-1) {
                        cName = cName.substring(cName.indexOf(":")+1);
                    }
                    if(cName.equals(name) && (child.getNamespaceURI() == null || 
                            child.getNamespaceURI().equals(n.getNamespaceURI())))
                        childNodes.add(child);
                }
            }
            return childNodes;
        }
        
        private String getAttributeValue(Node attr, String name) {
            NamedNodeMap mAttrList = attr.getAttributes();
            Attr refAttr = (Attr) mAttrList.getNamedItem(name);
            if (refAttr != null) {
                return refAttr.getNodeValue();
            }
            return null;
        }

        private Node findMethodNodeByRef(Node doc, String ref) throws XPathExpressionException {
            Node method = null;
            NodeList methods = RestUtils.getNodeList(doc, "//application/method");
            if (methods != null && methods.getLength() > 0) {
                for (int j = 0; j < methods.getLength(); j++) {
                    method = methods.item(j);
                    NamedNodeMap mAttrList = method.getAttributes();
                    Attr idAttr = (Attr) mAttrList.getNamedItem("id");
                    if (idAttr != null) {
                        String mName = idAttr.getNodeValue();
                        if (mName.equals(ref)) {
                            return method;
                        }
                    }
                }
            }
            return method;
        }

        private void addMethod(Resource r, String mName, Node method) throws XPathExpressionException {
            if (mName != null) {
                Method m = new Method(mName.toLowerCase());
                m.setType(MethodType.valueOf(mName));
                NodeList requests = RestUtils.getNodeList(method, "request");
                if (requests != null && requests.getLength() > 0) {
                    Node request = requests.item(0);
                    Request req = new Request();
                    NodeList reps = RestUtils.getNodeList(request, "representation");
                    if (reps != null && reps.getLength() > 0) {
                        for (int l = 0; l < reps.getLength(); l++) {
                            Node rep = reps.item(l);
                            String media = Constants.MimeType.XML.value();
                            Attr mediaAttr = (Attr) rep.getAttributes().getNamedItem("mediaType");
                            if (mediaAttr != null) {
                                media = mediaAttr.getNodeValue();
                            }
                            Representation rep1 = new Representation(media);
                            req.addRepresentation(rep1);
                        }
                    }
                    m.setRequest(req);
                }
                NodeList responses = RestUtils.getNodeList(method, "response");
                if (responses != null && responses.getLength() > 0) {
                    Node request = responses.item(0);
                    Response res = new Response();
                    NodeList ress = RestUtils.getNodeList(request, "representation");
                    if (ress != null && ress.getLength() > 0) {
                        for (int l = 0; l < ress.getLength(); l++) {
                            Node rep = ress.item(l);
                            String media = Constants.MimeType.XML.value();
                            Attr mediaAttr = (Attr) rep.getAttributes().getNamedItem("mediaType");
                            if (mediaAttr != null) {
                                media = mediaAttr.getNodeValue();
                            }
                            Representation rep1 = new Representation(media);
                            res.addRepresentation(rep1);
                        }
                    }
                    m.setResponse(res);
                }
                r.addMethod(m);
            }
        }
    }

    public class Resource {

        private String name;
        private String path;
        private String desc;

        private JavaSource src;
        private RepresentationDocument rep;
        private boolean isContainer;
        private boolean isContainerItem;

        private List<Method> methodList = Collections.emptyList();

        public Resource(String name, String path, String desc) {
            this.name = name;
            this.path = path;
            this.desc = desc;
            this.methodList = new ArrayList<Method>();
            this.rep = new RepresentationDocument();
        } 
        
        public Resource(String name, String path) {
            this(name, path, name);
        }       

        public String getName() {
            return name;
        }
        
        public String getPath() {
            return path;
        }

        public String getDescription() {
            return desc;
        }

        public void setDescription(String desc) {
            this.desc = desc;
        }        

        public List<Method> getMethods() {
            return methodList;
        }

        public void addMethod(Method m) {
            methodList.add(m);
        }

        protected JavaSource getSource() {
            return this.src;
        }

        protected void setSource(JavaSource src) {
            this.src = src;
        }     

        public RepresentationDocument getRepresentation() {
            return rep;
        }
        
        public boolean isContainer() {
            return isContainer;
        } 
        
        public void setIsContainer(boolean isContainer) {
            this.isContainer = isContainer;
        }
        
        public boolean isContainerItem() {
            //TODO
//            return this.isContainerItem;
            return !this.isContainer;
        } 
        
        public void setIsContainerItem(boolean isContainerItem) {
            this.isContainerItem = isContainerItem;
        }    
        
        @Override
        public String toString() {
            return getName()+" : "+getPath();
        }
    }

    public class RepresentationDocument {

        private RepresentationNode root;

        public RepresentationDocument() {
        }

        public RepresentationNode getRoot() {
            return root;
        }
        
        protected void setRoot(RepresentationNode root) {
            this.root = root;
        }        
    }
    
    public class RepresentationNode implements Cloneable {

        private String name;
        private String id;
        private String type;

        private MethodTree link;
        private JavaSource src;

        private List<RepresentationNode> attrList = new ArrayList<RepresentationNode>();
        private List<RepresentationNode> childList = new ArrayList<RepresentationNode>();
        private boolean isRoot;
        private boolean isContainer;

        public RepresentationNode(String name) {
            this.name = name;
            this.id = name;
        }
        
        public String getName() {
            return name;
        }
        
        public String getId() {
            return id;
        }

        private void setId(String id) {
            this.id = id;
        }
        
        public MethodTree getLink() {
            return link;
        }
        
        protected void setLink(MethodTree link) {
            this.link = link;
        }
        
        public List<RepresentationNode> getAttributes() {
            return attrList;
        }

        public void addAttribute(RepresentationNode m) {
            attrList.add(m);
        }
        
        public List<RepresentationNode> getChildren() {
            return childList;
        }

        public void addChild(RepresentationNode m) {
            childList.add(m);
        }

        public boolean isRoot() {
            return isRoot;
        }
        
        protected void setIsRoot(boolean isRoot) {
            this.isRoot = isRoot;
        }
        
        public boolean isReference() {
            return getAttributes().size() > 0 || getChildren().size() > 0;
        }
        
        public boolean isEntity() {
            return this.src != null;
        }
        
        protected JavaSource getSource() {
            return this.src;
        }

        protected void setSource(JavaSource src) {
            this.src = src;
        }  
        
        public boolean isContainer() {
            return isContainer;
        }
        
        protected void setIsContainer(boolean isContainer) {
            this.isContainer = isContainer;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        @Override
        public RepresentationNode clone() {
            try {
                RepresentationNode clone = (RepresentationNode)super.clone();
                return clone;
            } catch (CloneNotSupportedException cne) {
                throw new RuntimeException(cne);
            }
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }    
        
    public enum MethodType {

        GET(Constants.HttpMethodType.GET.value()) //NOI18N
        , PUT(Constants.HttpMethodType.PUT.value()) //NOI18N
        , POST(Constants.HttpMethodType.POST.value()) //NOI18N
        , DELETE(Constants.HttpMethodType.DELETE.value()) //NOI18N
        , GETCHILD("getChild");
        //NOI18N
        private String prefix;

        MethodType(String prefix) {
            this.prefix = prefix;
        }

        public String value() {
            return name();
        }

        public String prefix() {
            return prefix;
        }
    }

    public class Method {

        private String name;
        private MethodType type;
        private Request request;
        private Response response;
        
        private MethodTree tree;

        public Method(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public MethodType getType() {
            return type;
        }

        protected void setType(MethodType type) {
            this.type = type;
        }

        public Request getRequest() {
            return request;
        }

        protected void setRequest(Request r) {
            this.request = r;
        }

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response r) {
            this.response = r;
        }
        
        protected MethodTree getTree() {
            return tree;
        }
        
        protected void setTree(MethodTree tree) {
            this.tree = tree;
        }
    }

    public class NavigationMethod extends Method {

        private String nUri; //uri to get child stub eg: 'playlistId'
        private String linkName; //child stub name eg: 'Playlist'

        public NavigationMethod(String name) {
            super(name);
        }

        public String getNavigationUri() {
            return nUri;
        }

        protected void setNavigationUri(String nUri) {
            this.nUri = nUri;
        }

        public String getLinkName() {
            return linkName;
        }

        protected void setLinkName(String linkName) {
            this.linkName = linkName;
        }
    }

    public class Request {

        private List<Representation> repList = new ArrayList<Representation>();

        public Request() {
        }

        public List<Representation> getRepresentation() {
            return repList;
        }

        protected void addRepresentation(Representation rep) {
            this.repList.add(rep);
        }
        
        protected void setRepresentation(List<Representation> repList) {
            this.repList = repList;
        }
        
        protected boolean containsRepresentation(Representation rep) {
            return this.repList.contains(rep);
        }
    }

    public class Response {

        private List<Representation> repList = new ArrayList<Representation>();

        public Response() {
        }

        public List<Representation> getRepresentation() {
            return repList;
        }

        protected void addRepresentation(Representation rep) {
            this.repList.add(rep);
        }
        
        protected void setRepresentation(List<Representation> repList) {
            this.repList = repList;
        }
        
        protected boolean containsRepresentation(Representation rep) {
            return this.repList.contains(rep);
        }
    }

    public class Representation implements Comparator {

        private String mime;

        public Representation(String mime) {
            this.mime = mime;
        }

        public String getMime() {
            return mime;
        }
        
        @Override
        public boolean equals(Object o1) {
            return this.getMime().equals(((Representation)o1).getMime());
        }

        @Override
        public int hashCode() {
            return getMime().hashCode();
        }

        public int compare(Object o1, Object o2) {
            return ((Representation)o1).getMime().compareTo(((Representation)o2).getMime());
        }
    }
}
