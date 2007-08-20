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
package org.netbeans.modules.websvc.rest.codegen.model;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.lang.model.element.AnnotationMirror;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;

/**
 * ClientStubModel
 *
 * @author Ayub Khan
 */
public class ClientStubModel {

    private Project p;

    private SortedMap<String, JavaSource> srcMap = new TreeMap<String, JavaSource>();

    private List<Resource> resourceList = Collections.emptyList();
    
    private Map<String, RepresentationDocument> documentMap = 
            new HashMap<String, RepresentationDocument>();    

    public ClientStubModel() {        
    }

    public List<Resource> getResources() {
        return resourceList;
    }

    public void addResource(Resource m) {
        resourceList.add(m);
    }

    public JavaSource getJavaSource(String className) {
        return srcMap.get(className);
    }

    public void buildModel(Project p) throws IOException {
        this.p = p;
        this.resourceList = new ArrayList<Resource>();
        List<JavaSource> sources = JavaSourceHelper.getJavaSources(p);
        List<JavaSource> staticR = new ArrayList<JavaSource>();
        List<JavaSource> dynamicR = new ArrayList<JavaSource>();
        List<JavaSource> converters = new ArrayList<JavaSource>();
        for (JavaSource src : sources) {
            if (JavaSourceHelper.isEntity(src)) {
                continue;
            }
            String className = JavaSourceHelper.getClassNameQuietly(src);
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
        String pkgName = JavaSourceHelper.getPackageName(rSrc);
        String className = JavaSourceHelper.getClassNameQuietly(rSrc);
        String name = RestUtils.findStubNameFromClass(className);
        String path = RestUtils.findUri(rSrc);
        if (path == null)
            path = name.substring(0, 1).toLowerCase()+name.substring(1);
        Resource r = new Resource(name, path);
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
                    if (Constants.URI_TEMPLATE_ANNOTATION.equals(mAnonType) || Constants.URI_TEMPLATE.equals(mAnonType)) {
                        if (m == null) {
                            m = createNavigationMethod(mName);
                            m.setTree(tree);
                        }
                        m.setType(MethodType.GETCHILD);
                        for (ExpressionTree eAnon : eAnons) {
                            String value = eAnon.toString();
                            if(value.contains("{")) {
                                r.setIsContainer(true);
                                value = value.substring(value.indexOf("{") + 1, value.lastIndexOf("}"));
                            } else if(value.contains("\"") && value.contains("/")) {
                                value = value.substring(value.indexOf("\"") + 1, value.lastIndexOf("/"));
                            } else {
                                throw new IOException("Cannot find method with " +
                                    "@UriTemplate(\"subresource/\") or @UriTemplate(\"{containerId}/\")");
                            }
                            ((NavigationMethod) m).setNavigationUri(value);
                        }
                        String returnType = tree.getReturnType().toString();
                        String linkName = RestUtils.findStubNameFromClass(returnType);
                        ((NavigationMethod) m).setLinkName(linkName);
                    } else if (Constants.HTTP_METHOD_ANNOTATION.equals(mAnonType) || Constants.HTTP_METHOD.equals(mAnonType)) {
                        if (m == null) {
                            m = createMethod(mName);
                            m.setTree(tree);
                        }
                        for (ExpressionTree eAnon : eAnons) {
                            String value = RestUtils.getValueFromAnnotation(eAnon);
                            if (value.equals(MethodType.GET.value())) {
                                m.setType(MethodType.GET);
                            } else if (value.equals(MethodType.PUT.value())) {
                                m.setType(MethodType.PUT);
                            } else if (value.equals(MethodType.POST.value())) {
                                m.setType(MethodType.POST);
                            } else if (value.equals(MethodType.DELETE.value())) {
                                m.setType(MethodType.DELETE);
                            }
                        }
                    } else if (Constants.PRODUCE_MIME_ANNOTATION.equals(mAnonType) || Constants.PRODUCE_MIME.equals(mAnonType)) {
                        if (m == null) {
                            m = createMethod(mName);
                            m.setTree(tree);
                        }
                        for (ExpressionTree eAnon : eAnons) {
                            String value = RestUtils.getValueFromAnnotation(eAnon);
                            Representation rep = new Representation(value);
                            m.getResponse().setRepresentation(rep);
                        }
                    } else if (Constants.CONSUME_MIME_ANNOTATION.equals(mAnonType) || Constants.CONSUME_MIME.equals(mAnonType)) {
                        if (m == null) {
                            m = createMethod(mName);
                            m.setTree(tree);
                        }
                        for (ExpressionTree eAnon : eAnons) {
                            String value = RestUtils.getValueFromAnnotation(eAnon);
                            Representation rep = new Representation(value);
                            m.getRequest().setRepresentation(rep);
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
        
    private Method createMethod(String mName) {
        Method m = new Method(mName);
        Representation rep1 = new Representation("application/xml");
        Request request = new Request();
        request.setRepresentation(rep1);
        m.setRequest(request);
        Representation rep2 = new Representation("application/xml");
        Response response = new Response();
        response.setRepresentation(rep2);
        m.setResponse(response);
        return m;
    }
    
    private Method createNavigationMethod(String mName) {
        Method m = new NavigationMethod(mName);
        Representation rep1 = new Representation("application/xml");
        Request request = new Request();
        request.setRepresentation(rep1);
        m.setRequest(request);
        Representation rep2 = new Representation("application/xml");
        Response response = new Response();
        response.setRepresentation(rep2);
        m.setResponse(response);
        return m;
    }
    
    private void buildRepresentationDocument(Resource r, JavaSource rSrc) throws IOException {
        RepresentationDocument rDoc = r.getRepresentation();
        /*
         * Find a getXml method from resource source rSrc. The return type will be its converter
         * Eg: 
         *  @HttpMethod("GET")
         *  @ProduceMime("application/xml")
         *  public PlaylistsConverter getXml() {}
        */
        MethodTree method = RestUtils.findGetAsXmlMethod(rSrc); // getXml() method tree
        if(method != null) {
            String converter = method.getReturnType().toString();
            RepresentationNode rootNode = createRootNode(converter, rDoc);
            rDoc.setRoot(rootNode);
            documentMap.put(converter, rDoc);
            processConverter(converter, rDoc, rootNode, r);
        }
    }    

    private RepresentationNode createRootNode(String cName, RepresentationDocument rr) {
        JavaSource cSrc = getJavaSource(cName);
        if(cSrc == null)
            return null;
        List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(cSrc);
        if(annotations == null)
            return null;
        for (int i=0;i<annotations.size();i++) {
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
    
    private void processConverter(String converter, RepresentationDocument rr, 
            RepresentationNode rElem, Resource r) {
        assert converter != null;
        String cName = converter;
        if(cName.startsWith("Collection") || cName.startsWith("List") || 
                cName.startsWith("Set")) {
            if(cName.indexOf("<") != -1)
                cName = cName.substring(cName.indexOf("<")+1, cName.indexOf(">"));
        }
        JavaSource cSrc = getJavaSource(cName);
        if(cSrc == null)
            return;
        List<? extends AnnotationMirror> annotations = JavaSourceHelper.getClassAnnotations(cSrc);
        if(annotations == null)
            return;
        for (int i=0;i<annotations.size();i++) {
            AnnotationMirror annotation = annotations.get(i);
            String cAnonType = annotation.getAnnotationType().toString();
            if (Constants.XML_ROOT_ELEMENT_ANNOTATION.equals(cAnonType) || Constants.XML_ROOT_ELEMENT.equals(cAnonType)) {
                rElem.setSource(cSrc);
                List<MethodTree> cTrees = JavaSourceHelper.getAllMethods(cSrc);
                if(cTrees == null)
                    continue;
                List<RepresentationNode> elems = new ArrayList<RepresentationNode>();
                List<String> rTypes = new ArrayList<String>();
                for (int j=0;j<cTrees.size();j++) {
                    MethodTree tree = cTrees.get(j);
                    String mName = tree.getName().toString();
                    List<? extends AnnotationTree> mAnons = tree.getModifiers().getAnnotations();
                    if (mAnons == null)
                        continue;
                    
                    for (int k=0;k<mAnons.size();k++) {
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
                            if(cDoc == null || cDoc.getRoot() == null) {
                                String eName = RestUtils.findElementName(tree, r); // "playlistRef"
                                cElem = new RepresentationNode(eName);
                                cElem.setLink(tree); //getReferences() method tree for tracking the link                                
                                elems.add(cElem);//process later
                                rTypes.add(c);//process later                                
                            } else {
                                cElem = cDoc.getRoot();
                            }
                            rElem.addChild(cElem);
                        } else if (Constants.XML_ATTRIBUTE_ANNOTATION.equals(mAnonType) || Constants.XML_ATTRIBUTE.equals(mAnonType)) {
                            /*
                             *  @XmlAttribute
                             *  public URI getUri() {}
                             */
                            String attrName = RestUtils.getNameFromMethod(tree); // "uri"
                            RepresentationNode attr = new RepresentationNode(attrName);
                            attr.setLink(tree);
                            rElem.addAttribute(attr);
                        }
                    }
                }
                
                //now process element methods
                for (int j=0;j<elems.size();j++) {                    
                    processConverter(rTypes.get(j), rr, elems.get(j), r);
                }                
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

        public String getFileName() {
            return name.toLowerCase();
        }
        
        public String getFileNameExt() {
            return name.toLowerCase() + ".js";
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

    /*
     * Represents JAXB document
     */
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
    
    /*
     * Represents JAXB node
     */
    public class RepresentationNode {

        private String name;

        private MethodTree link;
        private JavaSource src;

        private List<RepresentationNode> attrList = new ArrayList<RepresentationNode>();
        private List<RepresentationNode> childList = new ArrayList<RepresentationNode>();
        private boolean isRoot;

        public RepresentationNode(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
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
        
        protected JavaSource getSource() {
            return this.src;
        }

        protected void setSource(JavaSource src) {
            this.src = src;
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
        private static MethodType NAVIGATE;
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

        private Representation rep;

        public Request() {
        }

        public Representation getRepresentation() {
            return rep;
        }

        protected void setRepresentation(Representation rep) {
            this.rep = rep;
        }
    }

    public class Response {

        private Representation rep;

        public Response() {
        }

        public Representation getRepresentation() {
            return rep;
        }

        protected void setRepresentation(Representation rep) {
            this.rep = rep;
        }
    }

    public class Representation {

        private String mime;

        public Representation(String mime) {
            this.mime = mime;
        }

        public String getMime() {
            return mime;
        }
    }
}
