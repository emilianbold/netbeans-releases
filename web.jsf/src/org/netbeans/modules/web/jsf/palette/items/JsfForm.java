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

package org.netbeans.modules.web.jsf.palette.items;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
/*import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.AttributeValue;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.NamedElement;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.ParameterizedType;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.VariableAccess;*/
import org.netbeans.modules.editor.NbEditorUtilities;
//import org.netbeans.modules.j2ee.common.JMIUtils;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.wizards.JSFClinetGenerator;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Pavel Buzek
 */
public class JsfForm implements ActiveEditorDrop {
        
//                    columnClasses="list-column-left, list-column-left,
//                    list-column-right, list-column-center"
//                    rowClasses="list-row-even, list-row-odd"
    
    public static final int FORM_TYPE_EMPTY = 0;
    public static final int FORM_TYPE_DETAIL = 1;
    public static final int FORM_TYPE_NEW = 2;
    public static final int FORM_TYPE_EDIT = 3;
    
    private static String [] BEGIN = {
        "<h:form>\n",
        "<h2>Detail</h2>\n <h:form>\n<h:panelGrid columns=\"2\">\n",
        "<h2>Create</h2>\n <h:form>\n<h:panelGrid columns=\"2\">\n",
        "<h2>Edit</h2>\n <h:form>\n<h:panelGrid columns=\"2\">\n",
    };
    private static String [] END = {
        "</h:form>\n",
        "</h:panelGrid>\n </h:form>\n",
        "</h:panelGrid>\n </h:form>\n",
        "</h:panelGrid>\n </h:form>\n",
    };
    private static String [] ITEM = {
        "",
        "<h:outputText value=\"{0}:\"/>\n <h:outputText value=\"#'{'{1}.{2}}\" title=\"{0}\" />\n",
        "<h:outputText value=\"{0}:\"/>\n <h:inputText id=\"{2}\" value=\"#'{'{1}.{2}}\" title=\"{0}\" />\n",
        "<h:outputText value=\"{0}:\"/>\n <h:inputText id=\"{2}\" value=\"#'{'{1}.{2}}\" title=\"{0}\" />\n",
        //relationship *ToOne - use combo box
        "<h:outputText value=\"{0}:\"/>\n <h:selectOneMenu id=\"{2}\" value=\"#'{'{1}.{2}}\" title=\"{0}\">\n <f:selectItems value=\"#'{'{3}.{2}s'}'\"/>\n </h:selectOneMenu>\n",
        //use date time converter
        "<h:outputText value=\"{0} ({4}):\"/>\n <h:inputText id=\"{2}\" value=\"#'{'{1}.{2}}\" title=\"{0}\" >\n <f:convertDateTime type=\"{3}\" pattern=\"{4}\" />\n</h:inputText>\n",
        //relationship *ToOne - use combo box, in FORM_TYPE_NEW display only if not pre set
        "<h:outputText value=\"{0}:\" rendered=\"#'{'{1}.{2} == null}\"/>\n <h:selectOneMenu id=\"{2}\" value=\"#'{'{1}.{2}}\" title=\"{0}\" rendered=\"#'{'{1}.{2} == null}\">\n <f:selectItems value=\"#'{'{3}.{2}s'}'\"/>\n </h:selectOneMenu>\n",
    };
    
    private String variable = "";
    private String bean = "";
    private int formType = 0;
    
    public JsfForm() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        
        JsfFormCustomizer c = new JsfFormCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            try {
                Caret caret = targetComponent.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                int len = targetComponent.getDocument().getLength() - p1;
                boolean containsFView = targetComponent.getText(0, p0).contains("<f:view>")
                    && targetComponent.getText(p1, len).contains("</f:view>");
                String body = createBody(targetComponent, !containsFView);
                JSFPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }
    
    private String createBody(JTextComponent target, boolean surroundWithFView) {
        StringBuffer sb = new StringBuffer();
        if (surroundWithFView) {
            sb.append("<f:view>\n");
        }
        sb.append(MessageFormat.format(BEGIN [formType], new Object [] {variable}));
        //TODO: RETOUCHE
       /* JavaClass jc = resolveJavaClass(target, bean);
        if (jc != null) {
            createForm(jc, formType, variable, sb, false);
        }
        */
        sb.append(END [formType]);
        if (surroundWithFView) {
            sb.append("</f:view>\n");
        }
        return sb.toString();
    }
    
    public static final int REL_NONE = 0;
    public static final int REL_TO_ONE = 1;
    public static final int REL_TO_MANY = 2;
    
    //TODO: RETOUCHE
/*    public static int isRelationship(Method m, boolean isFieldAccess) {
        Feature f = isFieldAccess ? guessField(m) : m;
        if (f != null) {
            for(Iterator it = f.getAnnotations().iterator(); it.hasNext();) {
                Annotation an = (Annotation) it.next();
                if ("javax.persistence.OneToOne".equals(an.getType().getName()) ||
                        "javax.persistence.ManyToOne".equals(an.getType().getName())) {
                    return REL_TO_ONE;
                }
                if ("javax.persistence.OneToMany".equals(an.getType().getName()) ||
                        "javax.persistence.ManyToMany".equals(an.getType().getName())) {
                    return REL_TO_MANY;
                }
            }
        }
        return REL_NONE;
    }*/
    
//    public static Method getOtherSideOfRelation(Method m, boolean isFieldAccess) {
//        Type type = m.getType();
//        if (type instanceof ParameterizedType) {
//            MultipartId id = (MultipartId) m.getTypeName();
//            for (Iterator it = id.getTypeArguments().iterator(); it.hasNext();) {
//                MultipartId param = (MultipartId) it.next();
//                NamedElement parType = param.getElement();
//                if (param instanceof JavaClass) {
//                    type = (JavaClass) param;
//                }
//            }
//        }
//        
//        if (type instanceof JavaClass) {
//            JavaClass jc = (JavaClass) type;
//            //PENDING detect using mappedBy parameter of the relationship annotation!!
//            for(Method method : getEntityMethods(jc)) {
//                Type t = method.getType();
//                if (t instanceof ParameterizedType) {
//                    MultipartId id = (MultipartId) method.getTypeName();
//                    for (Iterator it = id.getTypeArguments().iterator(); it.hasNext();) {
//                        MultipartId param = (MultipartId) it.next();
//                        NamedElement parType = param.getElement();
//                        if (param instanceof JavaClass) {
//                            t = (JavaClass) param;
//                            break;
//                        }
//                    }
//                }
//                if (t instanceof JavaClass && m.getDeclaringClass().getName().equals(t.getName())) {
//                    return method;
//                }
//            }
//        }
//        return null;
//    }
    
    /** Returns all methods in class and its super classes which are entity
     * classes or mapped superclasses.
     */
//    public static Method[] getEntityMethods(JavaClass clazz) {
//        List<Method> result = new LinkedList();
//        JavaClass jc = clazz;
//        while (jc != null) {
//            boolean isEntityOrMapped = false;
//            for (Iterator it = jc.getAnnotations().iterator(); it.hasNext();) {
//                Annotation ann = (Annotation) it.next();
//                if (ann == null || ann.getType() == null) {
//                    //cannot resolve type, invalid code
//                    continue;
//                }
//                String annName = ann.getType().getName();
//                if ("javax.persistence.Entity".equals(annName) ||
//                        "javax.persistence.MappedSuperclass".equals(annName)) {
//                    isEntityOrMapped = true;
//                    break;
//                }
//            }
//            if (isEntityOrMapped) {
//                List features = jc.getFeatures();
//                for (Iterator it = features.iterator(); it.hasNext();) {
//                    Object o =  it.next();
//                    if (o instanceof Method) {
//                        result.add((Method)o);
//                    }
//                }
//            }
//            jc = jc.getSuperClass();
//        }
//        return (Method[]) result.toArray(new Method[result.size()]);
//    }
    
//    static boolean isId(Method m, boolean isFieldAccess) {
//        Feature f = isFieldAccess ? guessField(m) : m;
//        if (f != null) {
//            for(Iterator it = f.getAnnotations().iterator(); it.hasNext();) {
//                Annotation an = (Annotation) it.next();
//                String annName = an.getType().getName();
//                if ("javax.persistence.Id".equals(annName) ||
//                        "javax.persistence.EmbeddedId".equals(annName)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    
//    static boolean isGenerated(Method m, boolean isFieldAccess) {
//        Feature f = isFieldAccess ? guessField(m) : m;
//        if (f != null) {
//            for(Iterator it = f.getAnnotations().iterator(); it.hasNext();) {
//                Annotation an = (Annotation) it.next();
//                if ("javax.persistence.GeneratedValue".equals(an.getType().getName())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    
//    static String getTemporal(Method m, boolean isFieldAccess) {
//        Feature f = isFieldAccess ? guessField(m) : m;
//        if (f != null) {
//            for(Iterator it = f.getAnnotations().iterator(); it.hasNext();) {
//                Annotation an = (Annotation) it.next();
//                if ("javax.persistence.Temporal".equals(an.getType().getName())) {
//                    AttributeValue temporalValue = (AttributeValue) an.getAttributeValues().get(0);
//                    if (temporalValue != null) {
//                        VariableAccess va = (VariableAccess) temporalValue.getValue();
//                        String temporal = va.getElement().getName();
//                        return temporal;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    static FileObject getFO(JTextComponent target) {
        Document doc = target.getDocument();
        if (doc != null) {
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            if (dobj != null) {
                return dobj.getPrimaryFile();
            }
        }
        return null;
    }
    
    static ClassPath getFullClasspath(FileObject fo) {
        ArrayList entries = new ArrayList();
        ArrayList urls = new ArrayList();
        entries.addAll(ClassPath.getClassPath(fo, ClassPath.SOURCE).entries());
        entries.addAll(ClassPath.getClassPath(fo, ClassPath.BOOT).entries());
        entries.addAll(ClassPath.getClassPath(fo, ClassPath.COMPILE).entries());
        for (Iterator it = entries.iterator(); it.hasNext();) {
            ClassPath.Entry e = (ClassPath.Entry) it.next();
            urls.add(e.getURL());
        }
        return ClassPathSupport.createClassPath((URL[]) urls.toArray(new URL[urls.size()]));
    }
    
//    public static JavaClass resolveJavaClass(FileObject foInClassPath, String bean) {
//        JavaClass jc = JMIUtils.findClass(bean, getFullClasspath(foInClassPath));
//        if (jc == null) { //try to add project to cp
//        	JavaClass clazzAnywhere = JMIUtils.findClass(bean);
//        	if (clazzAnywhere != null) {
//	            Project projectWithEntity = FileOwnerQuery.getOwner(JavaModel.getFileObject(clazzAnywhere.getResource()));
//	            Project project = FileOwnerQuery.getOwner(foInClassPath);
//    	        ProjectClassPathExtender pcpe = (ProjectClassPathExtender) project.getLookup().lookup(ProjectClassPathExtender.class);
//        	    AntArtifact artifact[] = AntArtifactQuery.findArtifactsByType(projectWithEntity, JavaProjectConstants.ARTIFACT_TYPE_JAR);
//	            try {
//    	            if (artifact.length > 0) {
//        	            pcpe.addAntArtifact(artifact[0], artifact[0].getArtifactLocations()[0]);
//            	    }
//                	jc = JMIUtils.findClass(bean, getFullClasspath(foInClassPath));
//	            } catch (IOException ex) {
//    	            ErrorManager.getDefault().notify(ex);
//        	    }
//        	}
//        }
//        return jc;
//    }
//    
//    static JavaClass resolveJavaClass(JTextComponent target, String bean) {
//        Document doc = target.getDocument();
//        if (doc != null) {
//            DataObject dobj = NbEditorUtilities.getDataObject(doc);
//            if (dobj != null) {
//                return resolveJavaClass(dobj.getPrimaryFile(), bean);
//            }
//        }
//        return null;
//    }
            
    static boolean hasModuleJsf(JTextComponent target) {
        FileObject f = getFO(target);
        if (f != null) {
            WebModule wm = WebModule.getWebModule(f);
            String[] configFiles = JSFConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
            return configFiles != null && configFiles.length > 0;
        }
        return false;
    }
    
//    public static boolean isEntityClass(JavaClass jc) {
//        for(Annotation ann : (List <Annotation>) jc.getAnnotations()) {
//            if ("javax.persistence.Entity".equals(ann.getType().getName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    public static boolean isEmbeddableClass(JavaClass jc) {
//        for(Annotation ann : (List <Annotation>) jc.getAnnotations()) {
//            if ("javax.persistence.Embeddable".equals(ann.getType().getName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    public static boolean isFieldAccess(JavaClass clazz) {
//        boolean fieldAccess = false;
//        boolean accessTypeDetected = false;
//        JavaClass jc = clazz;
//        while (jc != null) {
//            List features = jc.getFeatures();
//            for (Iterator featuresIter = features.iterator(); featuresIter.hasNext() && !accessTypeDetected;) {
//                Feature feature = (Feature) featuresIter.next();
//                for (Iterator it = feature.getAnnotations().iterator(); it.hasNext() && !accessTypeDetected;) {
//                    Annotation ann = (Annotation) it.next();
//                    if (ann != null && ann.getType() != null) {
//                        String annName = ann.getType().getName();
//                        if ("javax.persistence.Id".equals(annName) ||
//                                "javax.persistence.EmbeddedId".equals(annName)) {
//                            if (feature instanceof Field) {
//                                fieldAccess = true;
//                            }
//                            accessTypeDetected = true;
//                        }
//                    }
//                }
//            }
//            if (!accessTypeDetected) {
//                ErrorManager.getDefault().log(ErrorManager.WARNING, "Failed to detect correct access type for class:" + jc.getName());
//            }
//            jc = jc.getSuperClass();
//        }
//        
//        return fieldAccess;
//    }
//
//    public static Field guessField(Method getter) {
//        String name = getter.getName().substring(3);
//        String guessFieldName = name.substring(0,1).toLowerCase() + name.substring(1);
//        for (Iterator featuresIter = getter.getDeclaringClass().getFeatures().iterator(); featuresIter.hasNext();) {
//            Feature feature = (Feature) featuresIter.next();
//            if (guessFieldName.equals(feature.getName()) && (feature instanceof Field)) {
//                return (Field) feature;
//            }
//        }
//        ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot detect the field associated with property: " + guessFieldName);
//        return null;
//    }
//
//    /** Check if there is a setter corresponding with the getter */
//    public static boolean isReadOnly(Method getter) {
//        String setterName = "set" + getter.getName().substring(3); //NOI18N
//        Type propertyType = getter.getType();
//        for (Iterator featuresIter = getter.getDeclaringClass().getFeatures().iterator(); featuresIter.hasNext();) {
//            Feature feature = (Feature) featuresIter.next();
//            if (setterName.equals(feature.getName()) && (feature instanceof Method)) {
//                Method setter = (Method) feature;
//                if (setter.getParameters().size() == 1) {
//                    Parameter firstParam = (Parameter) setter.getParameters().get(0);
//                    if (firstParam.getType().equals(propertyType)) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }

    static String getDateTimeFormat(String temporal) {
        if ("DATE".equals(temporal)) {
            return "MM/dd/yyyy";
        } else if ("TIME".equals(temporal)) {
            return "hh:mm:ss";
        } else {
            return "MM/dd/yyyy, hh:mm:ss";
        }
    }
    
//    public static void createForm(JavaClass bean, int formType, String variable, StringBuffer sb, boolean createSelectForRel) {
//        Method methods [] = getEntityMethods(bean);
//        boolean fieldAccess = isFieldAccess(bean);
//        for (int i = 0; i < methods.length; i++) {
//            if (methods[i].getName().startsWith("get")) {
//                int isRelationship = isRelationship(methods[i], fieldAccess);
//                String name = methods[i].getName().substring(3);
//                String propName = JSFClinetGenerator.getPropNameFromMethod(methods[i].getName());
//                if (formType == FORM_TYPE_NEW && 
//                        ((isId(methods[i], fieldAccess) && isGenerated(methods[i], fieldAccess))
//                        || isReadOnly(methods[i]))) {
//                    //skip if in create form if it is generated
//                } else if (formType == FORM_TYPE_EDIT && (isId(methods[i], fieldAccess) || isReadOnly(methods[i]))) {
//                    //make id non editable
//                    sb.append(MessageFormat.format(ITEM [FORM_TYPE_DETAIL], new Object [] {name, variable, propName}));
//                } else if ((formType == FORM_TYPE_NEW || formType == FORM_TYPE_EDIT) && "java.util.Date".equals(methods[i].getType().getName())) {
//                    String temporal = getTemporal(methods[i], fieldAccess);
//                    if (temporal == null) {
//                        sb.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName}));
//                    } else {
//                        //param 3 - temporal, param 4 - date/time format
//                        sb.append(MessageFormat.format(ITEM [5], new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal)}));
//                    }
//                } else if ((formType == FORM_TYPE_DETAIL && isRelationship == REL_TO_ONE) || isRelationship == REL_NONE) {
//                    //normal field (input or output text)
//                    sb.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName}));
//                } else if (isRelationship == REL_TO_ONE) {
//                    //combo box for editing toOne relationships
//                    sb.append(MessageFormat.format(formType == FORM_TYPE_EDIT ? ITEM [4] : ITEM[6]/* FORM_TYPE_NEW */, new Object [] {name, variable, propName, variable.substring(0, variable.lastIndexOf('.'))}));
//                }
//            }
//        }
//    }
//    
//    public static void createTablesForRelated(JavaClass bean, int formType, String variable, String idProperty, boolean isInjection, StringBuffer sb) {
//        Method methods [] = getEntityMethods(bean);
//        String simpleClass = JSFClinetGenerator.simpleClassName(bean.getName());
//        String managedBean = JSFClinetGenerator.getManagedBeanName(simpleClass);
//        boolean fieldAccess = isFieldAccess(bean);
//        //generate tables of objects with ToMany relationships
//        if (formType == FORM_TYPE_DETAIL) {
//            for (int i = 0; i < methods.length; i++) {
//                if (methods[i].getName().startsWith("get")) {
//                    int isRelationship = isRelationship(methods[i], fieldAccess);
//                    String name = methods[i].getName().substring(3);
//                    String methodName = methods[i].getName();
//                    String propName = JSFClinetGenerator.getPropNameFromMethod(methodName);
//                    if (isRelationship == REL_TO_MANY) {
//                        Method otherSide = getOtherSideOfRelation(methods[i], fieldAccess);
//                        int otherSideMultiplicity = REL_TO_ONE;
//                        if (otherSide != null) {
//                            JavaClass relClass = (JavaClass) otherSide.getDeclaringClass();
//                            boolean isRelFieldAccess = isFieldAccess(relClass);
//                            otherSideMultiplicity = isRelationship(otherSide, isRelFieldAccess);
//                        }
//                        MultipartId id = (MultipartId) methods[i].getTypeName();
//                        JavaClass jc = null;
//                        for (Iterator it = id.getTypeArguments().iterator(); it.hasNext();) {
//                            MultipartId param = (MultipartId) it.next();
//                            NamedElement parType = param.getElement();
//                            if (param instanceof JavaClass) {
//                                jc = (JavaClass) param;
//                                break;
//                            }
//                        }
//                        if (jc != null) {
//                            boolean relatedIsFieldAccess = isFieldAccess(jc);
//                            String relatedIdProperty = JSFClinetGenerator.getPropNameFromMethod(getIdGetter(relatedIsFieldAccess, jc).getName());
//                            String relatedClass = JSFClinetGenerator.simpleClassName(jc.getName());
//                            String relatedManagedBean = JSFClinetGenerator.getManagedBeanName(relatedClass);
//                            String detailManagedBean = JSFClinetGenerator.simpleClassName(bean.getName());
//                            sb.append("<h2>List of " + name + "</h2>\n");
//                            sb.append("<h:outputText rendered=\"#{not " + relatedManagedBean + ".detail" + relatedClass + "s.rowAvailable}\" value=\"No " + name + "\"/><br>\n");
//                            sb.append("<h:dataTable value=\"#{" + relatedManagedBean + ".detail" + relatedClass + "s}\" var=\"item\" \n");
//                            sb.append("border=\"1\" cellpadding=\"2\" cellspacing=\"0\" \n rendered=\"#{not empty " + relatedManagedBean + ".detail" + relatedClass + "s}\">\n"); //NOI18N
//                            String removeItems = "remove" + methodName.substring(3);
//                            String commands = " <h:column>\n <h:commandLink value=\"Destroy\" action=\"#'{'" + relatedManagedBean + ".destroyFrom" + detailManagedBean + "'}'\">\n" 
//                                    + "<f:param name=\"" + relatedIdProperty +"\" value=\"#'{'{0}." + relatedIdProperty + "'}'\"/>\n"
//                                    + "<f:param name=\"relatedId\" value=\"#'{'" + variable + "." + idProperty + "'}'\"/>\n"
//                                    + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
//                                    + " <h:commandLink value=\"Edit\" action=\"#'{'" + relatedManagedBean + ".editSetup'}'\">\n"
//                                    + "<f:param name=\"" + relatedIdProperty +"\" value=\"#'{'{0}." + relatedIdProperty + "'}'\"/>\n"
//                                    + "<h:outputText value=\" \"/>\n </h:commandLink>\n"
//                                    + (otherSideMultiplicity == REL_TO_MANY ? "<h:commandLink value=\"Remove\" action=\"#'{'" + managedBean + "." + removeItems + "'}'\"/>" : "")
//                                    + "</h:column>\n";
//                            
//                            JsfTable.createTable(jc, variable + "." + propName, sb, commands, "detailSetup");
//                            sb.append("</h:dataTable>\n");
//                            if (otherSideMultiplicity == REL_TO_MANY) {
//                                sb.append("<br>\n Add " + relatedClass + "s:\n <br>\n");
//                                String itemsToAdd = JSFClinetGenerator.getPropNameFromMethod(methodName + "ToAdd");
//                                sb.append("<h:selectManyListbox id=\"add" + relatedClass + "s\" value=\"#{" 
//                                        + managedBean + "." + itemsToAdd + "}\" title=\"Add " + name + ":\">\n");
//                                String availableItems = JSFClinetGenerator.getPropNameFromMethod(methodName + "Available");
//                                sb.append("<f:selectItems value=\"#{" + managedBean + "." + availableItems + "}\"/>\n");
//                                sb.append("</h:selectManyListbox>\n");
//                                String addItems = "add" + methodName.substring(3);
//                                sb.append("<h:commandButton value=\"Add\" action=\"#{" + managedBean + "." + addItems + "}\"/>\n <br>\n");
//                            }
//                            sb.append("<h:commandLink value=\"New " + name + "\" action=\"#{" + relatedManagedBean + ".createFrom" + detailManagedBean + "Setup}\">\n");
//                            sb.append("<f:param name=\"relatedId\" value=\"#{" + variable + "." + idProperty + "}\"/>\n");
//                            sb.append("</h:commandLink>\n <br>\n <br>\n");
//                        } else {
//                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "cannot find referenced class: " + methods[i].getType().getName());
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public static Method getIdGetter(final boolean isFieldAccess, final JavaClass jc) {
//        Method m[] = getEntityMethods(jc);
//        for (int j = 0; j < m.length; j++) {
//            if (m[j].getName().startsWith("get")) {
//                Feature f = isFieldAccess ? JsfForm.guessField(m[j]) : m[j];
//                if (f != null) {
//                    for (Iterator it = f.getAnnotations().iterator(); it.hasNext();) {
//                        Annotation ann = (Annotation) it.next();
//                        if (ann != null) {
//                            String annName = ann.getType().getName();
//                            if ("javax.persistence.Id".equals(annName) ||
//                                    "javax.persistence.EmbeddedId".equals(annName)) {
//                                return m[j];
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find ID getter in class: " + jc.getName());
//        return null;
//    }
    
    public String getVariable() {
        return variable;
    }
    
    public void setVariable(String variable) {
        this.variable = variable;
    }
    
    public String getBean() {
        return bean;
    }
    
    public void setBean(String collection) {
        this.bean = collection;
    }
    
    public int getFormType() {
        return formType;
    }
    
    public void setFormType(int formType) {
        this.formType = formType;
    }
    
}
