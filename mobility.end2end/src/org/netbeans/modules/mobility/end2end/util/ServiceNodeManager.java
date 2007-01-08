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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.end2end.util;
import java.awt.Image;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
//import org.netbeans.jmi.javamodel.Array;
//import org.netbeans.jmi.javamodel.CallableFeature;
//import org.netbeans.jmi.javamodel.Constructor;
//import org.netbeans.jmi.javamodel.Element;
//import org.netbeans.jmi.javamodel.Feature;
//import org.netbeans.jmi.javamodel.Field;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.Parameter;
//import org.netbeans.jmi.javamodel.PrimitiveType;
//import org.netbeans.jmi.javamodel.Resource;
//import org.netbeans.jmi.javamodel.Type;
//import org.netbeans.modules.java.JavaDataObject;
//import org.netbeans.modules.java.ui.nodes.SourceNodes;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

public class ServiceNodeManager {
    
    final static String DEFAULT_PACKAGE = "<default package>"; // NOI18N
    
    public static Node getRootNode(final Project project) {
//        JavaModel.getJavaRepository().beginTrans(false);
//        try {
//            final FileObject rootFile = project.getProjectDirectory();
//            final DataObject dataObject = DataObject.find(rootFile);
//            final Node rootNode = new DataNode(dataObject, createNodesForProject(project)) {
//                public boolean canRename() {
//                    return false;
//                }
//            };
//            rootNode.setDisplayName(ProjectUtils.getInformation(project).getDisplayName());
//            rootNode.setName(ProjectUtils.getInformation(project).getName());
//            return rootNode;
//        } catch (Exception e) {
//            //System.out.println("INNNNNN ERRRRRRRRRRRRR");
//            ErrorManager.getDefault().notify(e);
//            return new AbstractNode(Children.LEAF);
//        } finally {
//            JavaModel.getJavaRepository().endTrans();
//        }
        return new AbstractNode( Children.LEAF );
    }
    
    /**
     * Creates node subtrees for the given data objects
     *
     * @param dataObjs DataObjects representing either classes or packages
     *
     * @return the child nodes
     */
    private static Children createChildren(final DataObject[] dataObjs) {
        final List<Node> nodeList = new ArrayList<Node>();
        for ( DataObject dob : dataObjs ) {
            final Node[] nodes = createNodes(dob);
            if (nodes != null) {
                nodeList.addAll(Arrays.asList(nodes));
            }
        }
        final Node[] nodes = nodeList.toArray(new Node[nodeList.size()]);
        final Children children = new Children.Array();
        children.add(nodes);
        return children;
    }
    
    /**
     * Creates child nodes for a class. Each child node represents either a
     * method or a nested class.
     *
     * @param source the class
     * @param includeMethods true if the methods of this class are to be
     *        included. This will only be false if the class itself cannot be
     *        exported, but one of its nested classes can be.
     *
     * @return the child nodes
     */
//    private static Children createChildrenFromClass(final JavaClass source) {
//        final List<Node> nodeList = new ArrayList<Node>();
//        List<Feature> features = source.getFeatures();
//        for ( final Feature feature : features ) {
//            if (feature instanceof JavaClass){ //create nodes for inner classes
//                final Node[] nodes = createNodesFromClass((JavaClass)feature);
//                if (nodes != null) {
//                    nodeList.addAll(Arrays.asList(nodes));
//                }
//            }
//            
//            if (feature instanceof Method){
//                if (isMethodValid((Method)feature)) {
//                    final Node n = SourceNodes.getExplorerFactory().createMethodNode((Method)feature);
//                    nodeList.add(n);
//                }
//            }
//        }        
//        
//        final Node[] nodes = nodeList.toArray(new Node[nodeList.size()]);
//        final Children children = new Children.Array();
//        children.add(nodes);
//        return children;
//    }
    
    /**
     * Creates the subnodes, both nested classes and methods, for a given class
     *
     * @param clazz the class element
     *
     * @return subnodes
     */
//    private static Node[] createNodesFromClass(final JavaClass clazz) {
//        if ( Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())){
//            return null;
//        }
//        
//        Node node;
//        final Children children = createChildrenFromClass(clazz);
//        if (children.getNodesCount() > 0) {
//            node = SourceNodes.getExplorerFactory().createClassNode(clazz);
//            final FilterNode fn = new FilterNode(node, children);
//            return new Node[]{fn};
//        } 
//        return null;
//    }
    
    private static Children createNodesForProject(final Project p) {
        //  System.out.println("in createNodesForProject");
        final Sources s = ProjectUtils.getSources(p);
        final SourceGroup[] groups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        // Create the nodes
        final ArrayList<Node> nodesList = new ArrayList<Node>(groups.length);
        
        for (int i = 0; i < groups.length; i++) {
            //  System.out.println("in loop for " + groups.length);
            final DataFolder srcFolder = DataFolder.findFolder(groups[i].getRootFolder());
            final Map<String,ArrayList<Node>> mys = getAllPackages(srcFolder);
            final Set<String> packages = mys.keySet();
            for (final Iterator<String> it = packages.iterator(); it.hasNext();) {
                final String thisPackage = it.next();
                final List<Node> classesNodes = mys.get(thisPackage);
                
                final Node nodes[] = new Node[classesNodes.size()];
                if (nodes.length == 0){
                    it.remove();
                    continue;
                }
                //  System.out.println("size =  " + nodesList.size());
                classesNodes.toArray(nodes);
                final Children children = new Children.Array();
                children.add(nodes);
                
                //  System.out.println("srcfolder = "+srcFolder.toString());
                final Node srcNode = new PackageNode(thisPackage, children);
                nodesList.add(srcNode);
            }
            
        }
        
        final Node nodes[] = new Node[nodesList.size()];
        //  System.out.println("size =  " + nodesList.size());
        nodesList.toArray(nodes);
        final Children children = new Children.Array();
        children.add(nodes);
        return children;
        
        //  return nodes;
    }
    
    
    private static void findAllClasses(final DataObject data, List<DataObject> list) {
        if (list == null) {
            list = new ArrayList<DataObject>();
        }
        if (data == null)
            return;
//        if (data instanceof JavaDataObject) {
//            list.add(data);
//            return;
//        }
        if (data instanceof DataFolder) {
            final DataObject[] dataObjs = ((DataFolder) data).getChildren();
            for (int i = 0; i < dataObjs.length; i++) {
                findAllClasses(dataObjs[i], list);
            }
        }
    }
    
    private static Map<String,ArrayList<Node>> getAllPackages(final DataObject data) {
        final List<DataObject> classesList = new ArrayList<DataObject>();
        findAllClasses(data, classesList);
        final Map<String,ArrayList<Node>> packageMap = new TreeMap<String,ArrayList<Node>>();
//        for ( final DataObject j : classesList ) {
//            final Resource res = JavaModel.getResource(j.getPrimaryFile());
//            if (( res.getStatus() & 0x80000000) == 0 ) {
//                String packageName = res.getPackageName();
//                //filter out javon default packages
//                if (checkIfWcwService(j)) {
//                    continue;
//                }
//                if (packageName.trim().equals(""))
//                    packageName = DEFAULT_PACKAGE;
//                if (!packageMap.containsKey(packageName)) {
//                    packageMap.put(packageName, new ArrayList<Node>());
//                }
//                final List<JavaClass> classes = res.getClassifiers();
//                final List<Node> nodeList = packageMap.get(packageName);
//                for ( final JavaClass jc : classes ) {
//                    if( jc.isInterface()) continue;
//                    final Node[] nodes = createNodesFromClass(jc);
//                    if (nodes != null) {
//                    	nodeList.addAll(Arrays.asList(nodes));                        
//                    }
//                }
//            }
//        }
        return packageMap;
        
        
    }
    
    private static boolean checkIfWcwService(final DataObject doj) {
        if (doj.getPrimaryFile().isData()){
            final FileObject parent = doj.getPrimaryFile().getParent();
            if (parent != null && parent.getName().indexOf("support") != -1){ //NOI18N
                final FileObject[] children = parent.getChildren();
                for (int i = 0; i < children.length; i++){
                    if (children[i].getName().startsWith("InvocationAbstraction")){ //NOI18N
                        return true;
                    } else if (children[i].getName().startsWith("Utility")){ //NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Creates the subnodes for a data object. Returns only those nodes that
     * are to be displayed in the explorer view
     *
     * @param data the base object
     *
     * @return the node subtrees
     */
    private static Node[] createNodes(final DataObject data) {
        if (data instanceof DataFolder) {
            final DataObject[] dataObjs = ((DataFolder) data).getChildren();
            final Children children = createChildren(dataObjs);
            final DataNode node = new DataNode(data, children) {
                public boolean canRename() {
                    return false;
                }
            };
            node.setIconBase("org/openide/resources/defaultFolder"); //NOI18N
            return new Node[]{node};
//        } else if (data instanceof JavaDataObject) {
//            if ( (JavaModel.getResource(data.getPrimaryFile()).getStatus() & 0x80000000) != 0 ) {
//                return null;
//            }
//            final List<JavaClass> classes = JavaModel.getResource(data.getPrimaryFile()).getClassifiers();
//            final List<Node> nodeList = new ArrayList<Node>();
//            for ( JavaClass jc : classes ) {
//                final Node[] nodes = createNodesFromClass(jc);
//                if (nodes != null) {
//                    nodeList.addAll(Arrays.asList(nodes));
//                }
//            }
//            return nodeList.toArray(new Node[nodeList.size()]);
        } else {
            return null;
        }
    }
    
    
    
    /**
     * Checks to see whether or not a given type is supported in exported
     * methods
     *
     * @param type the Netbeans type to be checked
     *
     * @return "true" iff the type is supported
     */
//    private static boolean isTypeSupported(final Type type) {
//        // System.err.println(" - isTypeSupported( " + type.getName() + " )");
//        if( type instanceof Array ) {
//            //System.err.println(" - field is array = " + type.getName());
//            final Array a = (Array)type;
//            return isTypeSupported( a.getType());
//        } else if (type instanceof PrimitiveType){
//            return true;
//        } else if( type instanceof JavaClass ) {
//            final JavaClass jc = (JavaClass)type;
//            final String className = jc.getName();
//            // Check for wrapper types
//            if( "java.lang.Boolean".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Byte".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Short".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Character".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Integer".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Long".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Float".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Double".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.util.Vector".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.util.Hashtable".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.util.Date".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.String".equals( className )) { // NOI18N
//                return true;
//            } else if( "java.lang.Object".equals( className )) { // NOI18N
//                return true;
//            } else {
//                // check if package is not prohibited
//                // java.util or java.lang
//                if( className.startsWith( "java.util" ) || className.startsWith( "java.lang" )) {
//                    return false;
//                }
//                // check if it is Bean
//                
//                // Check predecessor
//                if( !isTypeSupported( jc.getSuperClass())) {
//                    return false;
//                }
//                //System.err.println(" - bean checking - " + type.getName());
//                final Map<String,Field> fields = new HashMap<String,Field>();
//                final Map<String,CallableFeature> methods = new HashMap<String,CallableFeature>();
//                
//                final List<Feature> features = jc.getFeatures();
//                
//                for ( final Feature feature : features ) {
//                    if( feature instanceof Field ) {
//                        final Field field = (Field)feature;
//                        final int modifier = field.getModifiers();
//                        // We are not interested in static or final fields
//                        if( Modifier.isFinal( modifier ) || Modifier.isStatic( modifier )) {
//                            continue;
//                        }
//                        fields.put( field.getName(), field );
//                    }
//                    if( feature instanceof CallableFeature ) {
//                        final CallableFeature callable = (CallableFeature)feature;
//                        // Check for default constuctor
//                        final int modifier = callable.getModifiers();
//                        // We don't want any static, abstract or private methods
//                        if( !Modifier.isPublic( modifier ) || Modifier.isStatic( modifier ) ||
//                                Modifier.isAbstract( modifier )) {
//                            continue;
//                        }
//                        // Check for default constructor
//                        if( callable.getName() == null && callable.getParameters().size() == 0 ) {
//                            continue;
//                        }
//                        // Other than default constructors and methods with no or more than 1 parameter
//                        // are not welcome
//                        if( callable.getName() == null || !(
//                                ( callable.getParameters().size() == 1 && "void".equals( callable.getType().getName())) || // NOI18N
//                                ( callable.getParameters().size() == 0 && !"void".equals( callable.getType().getName()))))   // NOI18N
//                        {
//                            continue;
//                        }
//                        methods.put( callable.getName(), callable );
//                    }
//                }
//                //System.err.println(" - fields = " + fields.size());
//                //System.err.println(" - methods = " + methods.size());
//                // Now we traverse each field and methods
//                for ( final String name : fields.keySet() ) {
//                    final Field field = fields.get( name );
//                    
//                    // Check if type is supported
//                    if( !field.getType().equals( type ) && !isTypeSupported( field.getType())) {
//                        return false;
//                    }
//                    // If field is public we don't need setters and getters
//                    if( Modifier.isPublic( field.getModifiers())) {
//                        continue;
//                    }
//                    // Check for setter + getter;
//                    final String fieldName = name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
//                    // field must have setter and getter or ignore it
//                    final boolean containsSetter = methods.containsKey( "set" + fieldName );
//                    final boolean containsGetter = methods.containsKey( "get" + fieldName ) || methods.containsKey( "is" + fieldName );
//                    
//                    // has setter and getter
//                    if( containsSetter && containsGetter ) {
//                        continue;
//                    }
//                    
//                    // is not public and has setter nor getter
//                    if( !containsSetter && !containsGetter ) {
//                        continue;
//                    }
//                    // has only setter or getter - it's not JavaBean
//                    return false;
//                }
//                return true;
//            }
//        }
//        return false;
//    }
    
    /** Checks to see if a method can be exported */
//    private static boolean isMethodValid(final Method method) {
//        // check that method is public
//        if (!Modifier.isPublic(method.getModifiers())) {
//            return false;
//        }
//        
//        // check that either method is static, or the class
//        // has a public nullary constructor
//        if (!Modifier.isStatic(method.getModifiers())) {
//            final JavaClass clazz = (JavaClass)method.getDeclaringClass();
//            final List<Feature> cons = clazz.getFeatures();
//            for ( final Feature feature : cons ) {
//                if (feature instanceof Constructor ){
//                    final List<Parameter> params = ((Constructor)feature).getParameters();
//                    if (params.size() == 0) {
//                        final int modifiers = ((Constructor)feature).getModifiers();
//                        if (Modifier.isPublic(modifiers)) {
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        
//        // check if return type is supported
//        
//        if (!isTypeSupported(method.getType())) {
//            return false;
//        }
//        
//        
//        // check parameter types
//        final List<Parameter> params = method.getParameters();
//        for (final Parameter par : params) {
//            if (!isTypeSupported(par.getType())) {
//                return false;
//            }
//        }
//        
//        
//        return true;
//    }
//    
//    /** Checks to see if a node describes a method that can be exported */
//    public static boolean isMethodNodeValid(final Node node) {
//        final Method me = (Method) node.getLookup().lookup(Element.class);
//        if (me == null) {
//            return false;
//        }
//        return isMethodValid(me);
//    }
//    
//    /** Checks to see if a class contains valid methods */
//    public static boolean containsValidMethods(final Node node) {
//        final Node[] nodes = node.getChildren().getNodes();
//        for (int i = 0; i < nodes.length; i++) {
//            if (isMethodNodeValid(nodes[i])) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    
    static class PackageNode extends AbstractNode {
        
        final Image PACKAGE_BADGE = Utilities.loadImage("org/netbeans/spi/java/project/support/ui/packageBadge.gif"); // NOI18N
        
        protected PackageNode(String packageName, Children children) {
            super(children);
            
            
            //this.group = group;
            setName(packageName);
            setDisplayName(packageName);
            // setIconBase("org/netbeans/modules/java/j2seproject/ui/resources/packageRoot");
        }
        
        public Image getIcon(@SuppressWarnings("unused")
		final int type) {
            return PACKAGE_BADGE;
        }
        
        public Image getOpenedIcon(@SuppressWarnings("unused")
		final int type) {
            return PACKAGE_BADGE;
        }
        
        
    }
}
