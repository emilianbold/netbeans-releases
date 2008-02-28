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

package org.netbeans.modules.web.jsf.palette.items;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Buzek
 * @author Po-Ting Wu
 */
public final class JsfForm implements ActiveEditorDrop {
        
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
        "<h:outputText value=\"{0}:\"/>\n <h:commandLink action=\"#'{'{2}.detailSetupFrom{3}Detail}\" value=\"#'{'{1}.{2}}\" />\n"
    };
    
    private String variable = "";
    private String bean = "";
    private int formType = 0;
    
    public JsfForm() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        
        JsfFormCustomizer jsfFormCustomizer = new JsfFormCustomizer(this, targetComponent);
        boolean accept = jsfFormCustomizer.showDialog();
        if (accept) {
            try {
                Caret caret = targetComponent.getCaret();
                int position0 = Math.min(caret.getDot(), caret.getMark());
                int position1 = Math.max(caret.getDot(), caret.getMark());
                int len = targetComponent.getDocument().getLength() - position1;
                boolean containsFView = targetComponent.getText(0, position0).contains("<f:view>")
                    && targetComponent.getText(position1, len).contains("</f:view>");
                String body = createBody(targetComponent, !containsFView);
                JSFPaletteUtilities.insert(body, targetComponent);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                accept = false;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                accept = false;
            }
        }
        
        return accept;
    }
    
    private String createBody(JTextComponent target, boolean surroundWithFView) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append("<f:view>\n");
        }
        stringBuffer.append(MessageFormat.format(BEGIN [formType], new Object [] {variable}));

        FileObject targetJspFO = getFO(target);
        JavaSource javaSource = JavaSource.create(createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(bean);
                createForm(controller, typeElement, formType, variable, stringBuffer, false);
            }
        }, true);

        stringBuffer.append(END [formType]);
        if (surroundWithFView) {
            stringBuffer.append("</f:view>\n");
        }
        return stringBuffer.toString();
    }
    
    public static final int REL_NONE = 0;
    public static final int REL_TO_ONE = 1;
    public static final int REL_TO_MANY = 2;
    
    public static int isRelationship(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            if (isAnnotatedWith(element, "javax.persistence.OneToOne") || isAnnotatedWith(element, "javax.persistence.ManyToOne")) {
                return REL_TO_ONE;
            }
            if (isAnnotatedWith(element, "javax.persistence.OneToMany") || isAnnotatedWith(element, "javax.persistence.ManyToMany")) {
                return REL_TO_MANY;
            }
        }
        return REL_NONE;
    }
    
    public static ExecutableElement getOtherSideOfRelation(CompilationController controller, ExecutableElement executableElement, boolean isFieldAccess) {
        TypeMirror passedReturnType = executableElement.getReturnType();
        if (TypeKind.DECLARED != passedReturnType.getKind() || !(passedReturnType instanceof DeclaredType)) {
            return null;
        }
        Types types = controller.getTypes();
        TypeMirror passedReturnTypeStripped = stripCollection((DeclaredType)passedReturnType, types);
        if (passedReturnTypeStripped == null) {
            return null;
        }
        TypeElement passedReturnTypeStrippedElement = (TypeElement) types.asElement(passedReturnTypeStripped);
        
        //try to find a mappedBy annotation element on the possiblyAnnotatedElement
        Element possiblyAnnotatedElement = isFieldAccess ? guessField(controller, executableElement) : executableElement;
        String mappedBy = null;
        AnnotationMirror persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.OneToOne");  //NOI18N"
        if (persistenceAnnotation == null) {
            persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.OneToMany");  //NOI18N"
        }
        if (persistenceAnnotation == null) {
            persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.ManyToOne");  //NOI18N"
        }
        if (persistenceAnnotation == null) {
            persistenceAnnotation = findAnnotation(possiblyAnnotatedElement, "javax.persistence.ManyToMany");  //NOI18N"
        }
        if (persistenceAnnotation != null) {
            Map<? extends ExecutableElement,? extends AnnotationValue> persistenceAnnotationMap = persistenceAnnotation.getElementValues();
            for (ExecutableElement key : persistenceAnnotationMap.keySet()) {
                if ("mappedBy".equals(key.getSimpleName().toString())) {
                    AnnotationValue mappedByValue = persistenceAnnotationMap.get(key);
                    mappedBy = mappedByValue.toString();
                    if (mappedBy.startsWith("\"") && mappedBy.endsWith("\"")) {
                        mappedBy = mappedBy.substring(1, mappedBy.length() - 1);
                    }
                    break;
                }
            }
        }
        for (ExecutableElement method : getEntityMethods(passedReturnTypeStrippedElement)) {
            if (mappedBy != null && mappedBy.length() > 0) {
                String tail = mappedBy.length() > 1 ? mappedBy.substring(1) : "";
                String getterName = "get" + mappedBy.substring(0,1).toUpperCase() + tail;
                if (getterName.equals(method.getSimpleName().toString())) {
                    return method;
                }
            }
            else {
                TypeMirror iteratedReturnType = method.getReturnType();
                iteratedReturnType = stripCollection(iteratedReturnType, types);
                TypeMirror executableElementEnclosingType = executableElement.getEnclosingElement().asType();
                if (types.isSameType(executableElementEnclosingType, iteratedReturnType)) {
                    return method;
                }
            }
        }
        return null;
    }
    
    public static TypeMirror stripCollection(TypeMirror passedType, Types types) {
        if (TypeKind.DECLARED != passedType.getKind() || !(passedType instanceof DeclaredType)) {
            return passedType;
        }
        TypeElement passedTypeElement = (TypeElement) types.asElement(passedType);
        String passedTypeQualifiedName = passedTypeElement.getQualifiedName().toString();   //does not include type parameter info
        Class passedTypeClass = null;
        try {
            passedTypeClass = Class.forName(passedTypeQualifiedName);
        } catch (ClassNotFoundException e) {
            //just let passedTypeClass be null
        }
        if (passedTypeClass != null && Collection.class.isAssignableFrom(passedTypeClass)) {
            List<? extends TypeMirror> passedTypeArgs = ((DeclaredType)passedType).getTypeArguments();
            if (passedTypeArgs.size() == 0) {
                return null;
            }
            return passedTypeArgs.get(0);
        }
        return passedType;
    }
    
    /** Returns all methods in class and its super classes which are entity
     * classes or mapped superclasses.
     */
    public static ExecutableElement[] getEntityMethods(TypeElement entityTypeElement) {
        List<ExecutableElement> result = new LinkedList<ExecutableElement>();
        TypeElement typeElement = entityTypeElement;
        while (typeElement != null) {
            if (isAnnotatedWith(typeElement, "javax.persistence.Entity") || isAnnotatedWith(typeElement, "javax.persistence.MappedSuperclass")) { // NOI18N
                result.addAll(ElementFilter.methodsIn(typeElement.getEnclosedElements()));
            }
            Element enclosingElement = typeElement.getEnclosingElement();
            if (ElementKind.CLASS == enclosingElement.getKind()) {
                typeElement = (TypeElement) enclosingElement;
            } else {
                typeElement = null;
            }
        }
        return result.toArray(new ExecutableElement[result.size()]);
    }
    
    static boolean isId(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    static boolean isGenerated(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            if (isAnnotatedWith(element, "javax.persistence.GeneratedValue")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    static String getTemporal(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? guessField(controller, method) : method;
        if (element != null) {
            AnnotationMirror annotationMirror = findAnnotation(element, "javax.persistence.Temporal"); // NOI18N
            if (annotationMirror != null) {
                Collection<? extends AnnotationValue> attributes = annotationMirror.getElementValues().values();
                if (attributes.iterator().hasNext()) {
                    AnnotationValue annotationValue = attributes.iterator().next();
                    if (annotationValue != null) {
                        //TODO: RETOUCHE annotation attribute value
                        return null;//annotationValue.getValue();
                    }
                }
            }
        }
        return null;
    }

    static FileObject getFO(JTextComponent target) {
        Document doc = target.getDocument();
        if (doc != null) {
            return NbEditorUtilities.getFileObject(doc);
        }
        return null;
    }
    
    static ClasspathInfo createClasspathInfo(FileObject fileObject) {
        return ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT),
                ClassPath.getClassPath(fileObject, ClassPath.COMPILE),
                ClassPath.getClassPath(fileObject, ClassPath.SOURCE)
                );
    }
    
    static boolean hasModuleJsf(JTextComponent target) {
        FileObject fileObject = getFO(target);
        if (fileObject != null) {
            WebModule webModule = WebModule.getWebModule(fileObject);
            String[] configFiles = JSFConfigUtilities.getConfigFiles(webModule);
            return configFiles != null && configFiles.length > 0;
        }
        return false;
    }
    
    public static boolean isEntityClass(TypeElement typeElement) {
        if (isAnnotatedWith(typeElement, "javax.persistence.Entity")) {
            return true;
        }
        return false;
    }
    
    public static boolean isEmbeddableClass(TypeElement typeElement) {
        if (isAnnotatedWith(typeElement, "javax.persistence.Embeddable")) {
            return true;
        }
        return false;
    }
    
    public static boolean isFieldAccess(TypeElement clazz) {
        boolean fieldAccess = false;
        boolean accessTypeDetected = false;
        TypeElement typeElement = clazz;
//        while (typeElement != null) {
        if (typeElement != null) {
            for (Element element : typeElement.getEnclosedElements()) {
                if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) {
                    if (ElementKind.FIELD == element.getKind()) {
                        fieldAccess = true;
                    }
                    accessTypeDetected = true;
                }
            }
            if (!accessTypeDetected) {
                Logger.getLogger("global").log(Level.WARNING, "Failed to detect correct access type for class:" + typeElement.getQualifiedName()); // NOI18N
            }
        }
//            typeElement = (TypeElement) typeElement.getEnclosingElement();
//        }
        return fieldAccess;
    }

    public static VariableElement guessField(CompilationController controller, ExecutableElement getter) {
        String name = getter.getSimpleName().toString().substring(3);
        String guessFieldName = name.substring(0,1).toLowerCase() + name.substring(1);
        TypeElement typeElement = (TypeElement) getter.getEnclosingElement();
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (variableElement.getSimpleName().contentEquals(guessFieldName)) {
                return variableElement;
            }
        }
        Logger.getLogger("global").log(Level.WARNING, "Cannot detect the field associated with property: " + guessFieldName);
        return null;
    }

    /** Check if there is a setter corresponding with the getter */
    public static boolean isReadOnly(Types types, ExecutableElement getter) {
        String setterName = "set" + getter.getSimpleName().toString().substring(3); //NOI18N
        TypeMirror propertyType = getter.getReturnType();
        TypeElement enclosingClass = (TypeElement) getter.getEnclosingElement();
        for (ExecutableElement executableElement : ElementFilter.methodsIn(enclosingClass.getEnclosedElements())) {
            if (executableElement.getSimpleName().contentEquals(setterName)) {
                if (executableElement.getParameters().size() == 1) {
                    VariableElement firstParam = executableElement.getParameters().get(0);
                    if (types.isSameType(firstParam.asType(), propertyType)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static String getDateTimeFormat(String temporal) {
        if ("DATE".equals(temporal)) {
            return "MM/dd/yyyy";
        } else if ("TIME".equals(temporal)) {
            return "hh:mm:ss";
        } else {
            return "MM/dd/yyyy, hh:mm:ss";
        }
    }
    
    public static void createForm(CompilationController controller, TypeElement bean, int formType, String variable, StringBuffer stringBuffer, boolean createSelectForRel) {
        ExecutableElement methods [] = getEntityMethods(bean);
        boolean fieldAccess = isFieldAccess(bean);
        TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType();
        for (ExecutableElement method : methods) {
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith("get")) {
                int isRelationship = isRelationship(controller, method, fieldAccess);
                String name = methodName.substring(3);
                String propName = JSFClientGenerator.getPropNameFromMethod(methodName);
                if (formType == FORM_TYPE_NEW && 
                        ((isId(controller, method, fieldAccess) && isGenerated(controller, method, fieldAccess)) || 
                        isReadOnly(controller.getTypes(), method))) {
                    //skip if in create form if it is generated
                } else if (formType == FORM_TYPE_EDIT && (isId(controller, method, fieldAccess) || isReadOnly(controller.getTypes(), method))) {
                    //make id non editable
                    stringBuffer.append(MessageFormat.format(ITEM [FORM_TYPE_DETAIL], new Object [] {name, variable, propName}));
                } else if ((formType == FORM_TYPE_NEW || formType == FORM_TYPE_EDIT) && controller.getTypes().isSameType(dateTypeMirror, method.getReturnType())) {
                    String temporal = getTemporal(controller, method, fieldAccess);
                    if (temporal == null) {
                        stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName}));
                    } else {
                        //param 3 - temporal, param 4 - date/time format
                        stringBuffer.append(MessageFormat.format(ITEM [5], new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal)}));
                    }
                } else if (formType == FORM_TYPE_DETAIL && isRelationship == REL_TO_ONE) {
                    stringBuffer.append(MessageFormat.format(ITEM [7], new Object [] {name, variable, propName, bean.getSimpleName()}));
                } else if (isRelationship == REL_NONE) {
                    //normal field (input or output text)
                    stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName}));
                } else if (isRelationship == REL_TO_ONE) {
                    //combo box for editing toOne relationships
                    stringBuffer.append(MessageFormat.format(formType == FORM_TYPE_EDIT ? ITEM [4] : ITEM[6]/* FORM_TYPE_NEW */, new Object [] {name, variable, propName, variable.substring(0, variable.lastIndexOf('.'))}));
                }
            }
        }
    }
    
    public static void createTablesForRelated(CompilationController controller, TypeElement bean, int formType, String variable, 
            String idProperty, boolean isInjection, StringBuffer stringBuffer) {
        ExecutableElement methods [] = getEntityMethods(bean);
        String simpleClass = bean.getSimpleName().toString();
        String managedBean = JSFClientGenerator.getManagedBeanName(simpleClass);
        boolean fieldAccess = isFieldAccess(bean);
        //generate tables of objects with ToMany relationships
        if (formType == FORM_TYPE_DETAIL) {
            for (ExecutableElement method : methods) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    int isRelationship = isRelationship(controller, method, fieldAccess);
                    String name = methodName.substring(3);
                    String propName = JSFClientGenerator.getPropNameFromMethod(methodName);
                    if (isRelationship == REL_TO_MANY) {
                        ExecutableElement otherSide = getOtherSideOfRelation(controller, method, fieldAccess);
                        int otherSideMultiplicity = REL_TO_ONE;
                        if (otherSide != null) {
                            TypeElement relClass = (TypeElement) otherSide.getEnclosingElement();
                            boolean isRelFieldAccess = isFieldAccess(relClass);
                            otherSideMultiplicity = isRelationship(controller, otherSide, isRelFieldAccess);
                        }

                        Types types = controller.getTypes();                        
                        TypeMirror typeArgMirror = stripCollection(method.getReturnType(), types);
                        TypeElement typeElement = (TypeElement)types.asElement(typeArgMirror);
                        
                        if (typeElement != null) {
                            boolean relatedIsFieldAccess = isFieldAccess(typeElement);
                            String getterName = getIdGetter(controller, relatedIsFieldAccess, typeElement).getSimpleName().toString();
                            String relatedIdProperty = JSFClientGenerator.getPropNameFromMethod(getterName);
                            String relatedClass = typeElement.getSimpleName().toString();
                            String relatedManagedBean = JSFClientGenerator.getManagedBeanName(relatedClass);
                            String detailManagedBean = bean.getSimpleName().toString();
                            stringBuffer.append("<br />\n");
                            stringBuffer.append("<b>List of " + name + ":</b>\n");
                            stringBuffer.append("<h:outputText rendered=\"#{" + relatedManagedBean + ".detail" + relatedClass + "s.rowCount == 0}\" escape=\"false\" value=\"<br />(No " + name + " Found)\"/>\n<br />\n");
                            stringBuffer.append("<h:dataTable value=\"#{" + relatedManagedBean + ".detail" + relatedClass + "s}\" var=\"item\" \n");
                            stringBuffer.append("border=\"1\" cellpadding=\"2\" cellspacing=\"0\" \n rendered=\"#{" + relatedManagedBean + ".detail" + relatedClass + "s.rowCount > 0}\">\n"); //NOI18N
                            String removeItems = "remove" + methodName.substring(3);
                            String commands = " <h:column>\n <h:commandLink value=\"Destroy\" action=\"#'{'" + relatedManagedBean + ".destroyFrom" + detailManagedBean + "'}'\">\n" 
                                    + "<f:param name=\"" + relatedIdProperty +"\" value=\"#'{'{0}." + relatedIdProperty + "'}'\"/>\n"
                                    + "<f:param name=\"relatedId\" value=\"#'{'" + variable + "." + idProperty + "'}'\"/>\n"
                                    + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                                    + " <h:commandLink value=\"Edit\" action=\"#'{'" + relatedManagedBean + ".editSetup'}'\">\n"
                                    + "<f:param name=\"" + relatedIdProperty +"\" value=\"#'{'{0}." + relatedIdProperty + "'}'\"/>\n"
                                    + "<h:outputText value=\" \"/>\n </h:commandLink>\n"
                                    + "<h:commandLink value=\"Remove\" action=\"#'{'" + managedBean + "." + removeItems + "'}'\"/>"
                                    + "</h:column>\n";
                            
                            JsfTable.createTable(controller, typeElement, variable + "." + propName, stringBuffer, commands, "detailSetup");
                            stringBuffer.append("</h:dataTable>\n");

                            String availableItems = JSFClientGenerator.getPropNameFromMethod(methodName + "Available");
                            stringBuffer.append("<h:panelGroup rendered=\"#{not empty " + managedBean + "." + availableItems + "}\">");
                            stringBuffer.append("<br />\n<b>Add " + relatedClass + "s:</b>\n<br />\n");
                            String itemsToAdd = JSFClientGenerator.getPropNameFromMethod(methodName + "ToAdd");
                            stringBuffer.append("<h:selectManyListbox id=\"add" + relatedClass + "s\" value=\"#{" 
                                    + managedBean + "." + itemsToAdd + "}\" title=\"Add " + name + ":\">\n");
                            stringBuffer.append("<f:selectItems value=\"#{" + managedBean + "." + availableItems + "}\"/>\n");
                            stringBuffer.append("</h:selectManyListbox>\n");
                            String addItems = "add" + methodName.substring(3);
                            stringBuffer.append("<h:commandButton value=\"Add\" action=\"#{" + managedBean + "." + addItems + "}\"/>\n <br>\n");
                            stringBuffer.append("</h:panelGroup>\n");

                            stringBuffer.append("<br />\n<h:commandLink value=\"Add New " + typeElement.getSimpleName() + "\" action=\"#{" + relatedManagedBean + ".createFrom" + detailManagedBean + "Setup}\">\n");
                            stringBuffer.append("<f:param name=\"relatedId\" value=\"#{" + variable + "." + idProperty + "}\"/>\n");
                            stringBuffer.append("</h:commandLink>\n<br />\n");
                        } else {
                            Logger.getLogger("global").log(Level.INFO, "cannot find referenced class: " + method.getReturnType()); // NOI18N
                        }
                    }
                }
            }
        }
    }

    public static ExecutableElement getIdGetter(CompilationController controller, final boolean isFieldAccess, final TypeElement typeElement) {
        ExecutableElement[] methods = getEntityMethods(typeElement);
        for (ExecutableElement method : methods) {
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith("get")) {
                Element element = isFieldAccess ? JsfForm.guessField(controller, method) : method;
                if (element != null) {
                    if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) {
                        return method;
                    }
                }
            }
        }
        Logger.getLogger("global").log(Level.WARNING, "Cannot find ID getter in class: " + typeElement.getQualifiedName());
        return null;
    }
    
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
    
    public static boolean isAnnotatedWith(Element element, String annotationFqn) {
        return findAnnotation(element, annotationFqn) != null;
    }
    
    private static AnnotationMirror findAnnotation(Element element, String annotationFqn) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            DeclaredType annotationDeclaredType = annotationMirror.getAnnotationType();
            TypeElement annotationTypeElement = (TypeElement) annotationDeclaredType.asElement();
            Name name = annotationTypeElement.getQualifiedName();
            if (name.contentEquals(annotationFqn)) {
                return annotationMirror;
            }
        }
        return null;
    }    
}
