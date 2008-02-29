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

package org.netbeans.modules.web.jsf.wizards;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.palette.items.JsfForm;
import org.netbeans.modules.web.jsf.palette.items.JsfTable;
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator.AnnotationInfo;
import org.netbeans.modules.web.jsf.wizards.JSFClientGenerator.TypeInfo;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Pavel Buzek
 */
public class JSFClientGenerator {
    
    private static String INDEX_PAGE = "index.jsp"; //NOI18N
    private static String WELCOME_JSF_PAGE = "welcomeJSF.jsp";  //NOI18N
    
    public static void generateJSFPages(Project project, final String entityClass, String jsfFolder, String controllerClass, FileObject pkg, FileObject controllerFileObject) throws IOException {
        final boolean isInjection = true;//Util.isSupportedJavaEEVersion(project);
        
        String simpleControllerName = simpleClassName(controllerClass);
        final String simpleEntityName = simpleClassName(entityClass);
        if (jsfFolder.startsWith("/")) {
            jsfFolder = jsfFolder.substring(1);
        }
        
        Sources srcs = (Sources) project.getLookup().lookup(Sources.class);
        String pkgName = controllerClass.substring(0, controllerClass.lastIndexOf('.'));
        
        String persistenceUnit = null;
        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project);
        if (persistenceScopes.length > 0) {
            FileObject persXml = persistenceScopes[0].getPersistenceXml();
            if (persXml != null) {
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
                PersistenceUnit units[] = persistence.getPersistenceUnit();
                if (units.length > 0) {
                    persistenceUnit = units[0].getName();
                }
            }
        }
        SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        final FileObject jsfRoot = FileUtil.createFolder(sgWeb[0].getRootFolder(), jsfFolder);
        
        String simpleConverterName = simpleEntityName + "Converter"; //NOI18N
        String converterName = pkgName + "." + simpleConverterName;
        final String fieldName = fieldFromClassName(simpleEntityName);

        final List<ElementHandle<ExecutableElement>> idGetter = new ArrayList<ElementHandle<ExecutableElement>>();
        final FileObject[] arrEntityClassFO = new FileObject[1];
        final List<ElementHandle<ExecutableElement>> toOneRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final List<ElementHandle<ExecutableElement>> toManyRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final boolean[] fieldAccess = new boolean[] { false };
        final String[] idProperty = new String[1];

        //detect access type
        final ClasspathInfo classpathInfo = ClasspathInfo.create(pkg);
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement jc = controller.getElements().getTypeElement(entityClass);
                arrEntityClassFO[0] = org.netbeans.api.java.source.SourceUtils.getFile(jc, controller.getClasspathInfo());
                fieldAccess[0] = JsfForm.isFieldAccess(jc);
                for (ExecutableElement method : JsfForm.getEntityMethods(jc)) {
                    String methodName = method.getSimpleName().toString();
                    if (methodName.startsWith("get")) {
                        Element f = fieldAccess[0] ? JsfForm.guessField(controller, method) : method;
                        if (f != null) {
                            if (JsfForm.isAnnotatedWith(f, "javax.persistence.Id") ||
                                    JsfForm.isAnnotatedWith(f, "javax.persistence.EmbeddedId")) {
                                idGetter.add(ElementHandle.create(method));
                                idProperty[0] = getPropNameFromMethod(methodName);
                            } else if (JsfForm.isAnnotatedWith(f, "javax.persistence.OneToOne") ||
                                    JsfForm.isAnnotatedWith(f, "javax.persistence.ManyToOne")) {
                                toOneRelMethods.add(ElementHandle.create(method));
                            } else if (JsfForm.isAnnotatedWith(f, "javax.persistence.OneToMany") ||
                                    JsfForm.isAnnotatedWith(f, "javax.persistence.ManyToMany")) {
                                toManyRelMethods.add(ElementHandle.create(method));
                            }
                        }
                    }
                }
            }
        }, true);
        
        if (arrEntityClassFO[0] != null) {
            addImplementsClause(arrEntityClassFO[0], entityClass, "java.io.Serializable"); //NOI18N
        }
            
        JEditorPane ep = new JEditorPane("text/x-jsp", "");
        final BaseDocument doc = new BaseDocument(ep.getEditorKit().getClass(), false);
        WebModule wm = WebModule.getWebModule(jsfRoot);
        
        //automatically add JSF framework if it is not added
        JSFFrameworkProvider fp = new JSFFrameworkProvider();
        if (!fp.isInWebModule(wm)) {
            WebModuleExtender wme = fp.createWebModuleExtender(wm, ExtenderController.create());
            wme.extend(wm);
        }
        
        controllerFileObject = generateControllerClass(fieldName, pkg, idGetter.get(0), persistenceUnit, simpleControllerName, 
                entityClass, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection, fieldAccess[0], controllerFileObject);
        
        final String managedBean =  getManagedBeanName(simpleEntityName);
        FileObject converter = generateConverter(controllerFileObject, pkg, simpleConverterName, controllerClass, simpleControllerName, entityClass, 
                simpleEntityName, idGetter.get(0), managedBean, isInjection);
            
        final String indexJspToUse = addLinkToListJspIntoIndexJsp(wm, jsfFolder, simpleEntityName);
        final String linkToIndex = indexJspToUse != null ? "<br />\n<a href=\"" + wm.getContextPath() + "/" + indexJspToUse + "\">Index</a>\n" : "";  //NOI18N

        generateListJsp(jsfRoot, classpathInfo, entityClass, simpleEntityName, managedBean, linkToIndex, fieldName, idProperty[0], doc);
        
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateNewJsp(controller, entityClass, simpleEntityName, managedBean, fieldName, toOneRelMethods, fieldAccess[0], linkToIndex, doc, jsfRoot);
            }
        }, true);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateEditJsp(controller, entityClass, simpleEntityName, managedBean, fieldName, linkToIndex, doc, jsfRoot);
            }
        }, true);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateDetailJsp(controller, entityClass, simpleEntityName, managedBean, fieldName, idProperty[0], isInjection, linkToIndex, doc, jsfRoot);
            }
        }, true);
        
        addStuffToFacesConfigXml(classpathInfo, wm, managedBean, controllerClass, entityClass, converterName, fieldName, jsfFolder, idGetter.get(0), pkgName, controllerFileObject);
    }

    private static String addLinkToListJspIntoIndexJsp(WebModule wm, String jsfFolder, String simpleEntityName) throws FileNotFoundException, IOException {
        FileObject documentBase = wm.getDocumentBase();
        FileObject indexjsp = documentBase.getFileObject(INDEX_PAGE); //NOI18N
        String indexjspString = INDEX_PAGE;
        String find = "<title>JSP Page</title>"; // NOI18N
        if (indexjsp == null) {
            indexjsp = documentBase.getFileObject(WELCOME_JSF_PAGE); //NOI18N
            indexjspString = "faces/" + WELCOME_JSF_PAGE;
            find = "<h1><h:outputText value=\"JavaServer Faces\" /></h1>"; //NOI18N
        }
        if (indexjsp != null){
            String content = JSFFrameworkProvider.readResource(indexjsp.getInputStream(), "UTF-8"); //NO18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            if ( content.indexOf(find) > 0){
                StringBuffer replace = new StringBuffer();
                String findForm = "<h:form>";
                boolean needsForm = content.indexOf(findForm) == -1;
                if (needsForm) {
                    replace.append(findForm);
                    replace.append(endLine);
                }
                replace.append(find);
                replace.append(endLine);
                replace.append("    <br/>");                        //NOI18N
                replace.append(endLine);
//                replace.append("    <a href=\"./");                  //NOI18N
//                replace.append(ConfigurationUtils.translateURI(ConfigurationUtils.getFacesServletMapping(wm),jsfFolder + "/List.jsp")); //NOI18N
//                replace.append("\">");                              //NOI18N
                String managedBeanName = getManagedBeanName(simpleEntityName);
                replace.append("<h:commandLink action=\"#{" + managedBeanName + ".listSetup}\" value=\"");
                replace.append("Show All " + simpleEntityName + "s");
                replace.append("\"/>");
                replace.append(endLine);
//                replace.append("</a>");  
                if (needsForm) {
                    replace.append("</h:form>");
                    replace.append(endLine);
                }
                content = content.replaceFirst(find, new String (replace.toString().getBytes("UTF8"), "UTF-8")); //NOI18N
                JSFFrameworkProvider.createFile(indexjsp, content, "UTF-8"); //NOI18N
                return indexjspString;
            }
        }
        return null;
    }

    private static void generateListJsp(final FileObject jsfRoot, ClasspathInfo classpathInfo, final String entityClass, String simpleEntityName, 
            final String managedBean, String linkToIndex, final String fieldName, String idProperty, BaseDocument doc) throws FileStateInvalidException, IOException {
        FileSystem fs = jsfRoot.getFileSystem();
        final StringBuffer listSb = new StringBuffer();
        listSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "<title>Listing " + simpleEntityName + "s</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        listSb.append("<h1>Listing " + simpleEntityName + "s</h1>\n");
        listSb.append("<h:form>\n");
        listSb.append("<h:outputText escape=\"false\" value=\"(No " + simpleEntityName + "s Found)<br />\" rendered=\"#{" + managedBean + ".itemCount == 0}\" />\n");
        listSb.append("<h:panelGroup rendered=\"#{" + managedBean + ".itemCount > 0}\">\n");
        listSb.append(MessageFormat.format("<h:outputText value=\"Item #'{'{0}.firstItem + 1'}'..#'{'{0}.lastItem'}' of #'{'{0}.itemCount}\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.prev'}'\" value=\"Previous #'{'{0}.batchSize'}'\" rendered=\"#'{'{0}.firstItem >= {0}.batchSize'}'\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Next #'{'{0}.batchSize'}'\" rendered=\"#'{'{0}.lastItem + {0}.batchSize <= {0}.itemCount}\"/>"
                + "&nbsp;\n"
                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Remaining #'{'{0}.itemCount - {0}.lastItem'}'\"\n"
                + "rendered=\"#'{'{0}.lastItem < {0}.itemCount && {0}.lastItem + {0}.batchSize > {0}.itemCount'}'\"/>\n", managedBean));
        listSb.append("<h:dataTable value='#{" + managedBean + "." + fieldName + "s}' var='item' border=\"1\" cellpadding=\"2\" cellspacing=\"0\">\n");
        final  String commands = "<h:column>\n <f:facet name=\"header\">\n <h:outputText escape=\"false\" value=\"&nbsp;\"/>\n </f:facet>\n"
                + "<h:commandLink value=\"Show\" action=\"#'{'" + managedBean + ".detailSetup'}'\">\n" 
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'" + managedBean + ".asString[{0}]'}'\"/>\n"               
                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                + "<h:commandLink value=\"Edit\" action=\"#'{'" + managedBean + ".editSetup'}'\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'" + managedBean + ".asString[{0}]'}'\"/>\n"
                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
                + "<h:commandLink value=\"Destroy\" action=\"#'{'" + managedBean + ".destroy'}'\">\n" 
                + "<f:param name=\"jsfcrud.current" + simpleEntityName +"\" value=\"#'{'" + managedBean + ".asString[{0}]'}'\"/>\n"
                + "</h:commandLink>\n </h:column>\n";
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                JsfTable.createTable(controller, typeElement, managedBean + "." + fieldName, listSb, commands, null);
            }
        }, true);
        listSb.append("</h:dataTable>\n</h:panelGroup>\n");
        listSb.append("<br />\n<h:commandLink action=\"#{" + managedBean + ".createSetup}\" value=\"New " + simpleEntityName + "\"/>\n"
                + linkToIndex + "\n");
        listSb.append("</h:form>\n</f:view>\n</body>\n</html>\n");
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, listSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            listSb.replace(0, listSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
        final String listText = listSb.toString();

        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject list = FileUtil.createData(jsfRoot, "List.jsp");//NOI18N
                FileLock lock = list.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(list.getOutputStream(lock)));
                    bw.write(listText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }
    
    private static void generateNewJsp(CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            List<ElementHandle<ExecutableElement>> toOneRelMethods, boolean fieldAccess, String linkToIndex, BaseDocument doc, final FileObject jsfRoot) throws FileStateInvalidException, IOException {
        StringBuffer newSb = new StringBuffer();
        newSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "<title>New " + simpleEntityName + "</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        newSb.append("<h1>New " + simpleEntityName + "</h1>\n");
        newSb.append("<h:form>\n  <h:panelGrid columns=\"2\">\n");
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_NEW, managedBean + "." + fieldName, newSb, true, entityClass);
        newSb.append("</h:panelGrid>\n<br />\n");
        
//        List<String> classNames = new ArrayList<String>();
//        List<String> idProperties = new ArrayList<String>();
//        for (ElementHandle<ExecutableElement> handle : toOneRelMethods) {
//            ExecutableElement method = handle.resolve(controller);
//            ExecutableElement otherSide = JsfForm.getOtherSideOfRelation(controller, method, fieldAccess);
//            if (otherSide != null) {
//                TypeElement relClass = (TypeElement) otherSide.getEnclosingElement();
//                classNames.add(relClass.getQualifiedName().toString());
//                idProperties.add(getPropNameFromMethod(method.getSimpleName().toString()));
//            }
//        }
//        
////      <h:commandLink action="#{comment.createFromPost}" value="Create" rendered="#{comment.comment.postId != null}"/>
//        StringBuffer newRenderDefaultOption = new StringBuffer();
//        for(int i = 0; i < classNames.size(); i++) {
//            StringBuffer negativeCondition = new StringBuffer();
//            if (classNames.size() > 0) {
//                for(int j = 0; j < classNames.size(); j++) {
//                    if (i != j) {
//                        negativeCondition.append(" and " + managedBean + "." + fieldName + "." + idProperties.get(j) + " == null");
//                    }
//                }
//            }
//            newSb.append("<h:commandLink action=\"#{" + managedBean + ".createFrom" + 
//                    simpleClassName(classNames.get(i)) + "}\" value=\"Create\" rendered=\"#{" + managedBean + "." + fieldName + "." + idProperties.get(i) + " != null" + negativeCondition.toString() + "}\"/>\n");
//            if (i > 0) {
//                newRenderDefaultOption.append(" and ");
//            }
//            newRenderDefaultOption.append(managedBean + "." + fieldName + "." + idProperties.get(i) + " == null");
//                
//        }
//        
////      <h:commandLink action="#{comment.create}" value="Create" rendered="#{comment.comment.postId == null}"/>
//        if (classNames.size() == 0) {
//            newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\"/>\n<br />\n");
//        } else {
//            newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\" rendered=\"#{" + newRenderDefaultOption.toString() + "}\"/>\n<br />\n");
//        }
        newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\"/>\n<br />\n");
        
        newSb.append("<br />\n<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + "s\" immediate=\"true\"/>\n " + linkToIndex
                + "</h:form>\n </f:view>\n</body>\n</html>\n");
        
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, newSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            newSb.replace(0, newSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        final String newText = newSb.toString();

        FileSystem fs = jsfRoot.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject newForm = FileUtil.createData(jsfRoot, "New.jsp");//NOI18N
                FileLock lock = newForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(newForm.getOutputStream(lock)));
                    bw.write(newText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }
    
    private static void generateEditJsp(CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            String linkToIndex, BaseDocument doc, final FileObject jsfRoot) throws FileStateInvalidException, IOException {
        StringBuffer editSb = new StringBuffer();
        editSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "<title>Editing " + simpleEntityName + "</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        editSb.append("<h1>Editing " + simpleEntityName + "</h1>\n");
        editSb.append("<h:form>\n"
                + "<h:panelGrid columns=\"2\">\n");
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_EDIT, managedBean + "." + fieldName, editSb, true, entityClass);
        editSb.append("</h:panelGrid>\n<br />\n<h:commandLink action=\"#{" + managedBean + ".edit}\" value=\"Save\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\"/>\n"
                + "</h:commandLink>\n"
                + "<br />\n<br />\n"
                + "<h:commandLink action=\"#{" + managedBean + ".detailSetup}\" value=\"Show\" immediate=\"true\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\"/>\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + "s\" immediate=\"true\"/>\n" + linkToIndex
                + "</h:form>\n </f:view>\n</body>\n</html>\n");

        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, editSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            editSb.replace(0, editSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

        final String editText = editSb.toString();

        FileSystem fs = jsfRoot.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject editForm = FileUtil.createData(jsfRoot, "Edit.jsp");//NOI18N
                FileLock lock = editForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(editForm.getOutputStream(lock)));
                    bw.write(editText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }

    private static void generateDetailJsp(CompilationController controller, String entityClass, String simpleEntityName, String managedBean, 
            String fieldName, String idProperty, boolean isInjection, String linkToIndex, BaseDocument doc, final FileObject jsfRoot) throws FileStateInvalidException, IOException {
        StringBuffer detailSb = new StringBuffer();
        detailSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "<title>" + simpleEntityName + " Detail</title>\n"
                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
        detailSb.append("<h1>" + simpleEntityName + " Detail</h1>\n");
        detailSb.append("<h:form>\n  <h:panelGrid columns=\"2\">\n");
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, detailSb, true, entityClass);
        JsfForm.createTablesForRelated(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, idProperty, isInjection, detailSb);
        detailSb.append("</h:panelGrid>\n");
        detailSb.append("<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".destroy}\" value=\"Destroy\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\" />\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".editSetup}\" value=\"Edit\">\n"
                + "<f:param name=\"jsfcrud.current" + simpleEntityName + "\" value=\"#{" + managedBean + ".asString[" + managedBean + "." + fieldName + "]}\" />\n"
                + "</h:commandLink>\n"
                + "<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".createSetup}\" value=\"New " + simpleEntityName + "\" />\n<br />\n"
                + "<h:commandLink action=\"#{" + fieldName + ".listSetup}\" value=\"Show All " + simpleEntityName + "s\"/>\n" + linkToIndex
                + "</h:form>\n </f:view>\n</body>\n</html>\n");

        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, detailSb.toString(), null);
            Formatter formatter = doc.getFormatter();
            formatter.reformatLock();
            formatter.reformat(doc, 0, doc.getLength());
            formatter.reformatUnlock();
            detailSb.replace(0, detailSb.length(), doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

        final String detailText = detailSb.toString();

        FileSystem fs = jsfRoot.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject detailForm = FileUtil.createData(jsfRoot, "Detail.jsp");//NOI18N
                FileLock lock = detailForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(detailForm.getOutputStream(lock)));
                    bw.write(detailText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }

    private static void addStuffToFacesConfigXml(ClasspathInfo classpathInfo, WebModule wm, String managedBean, String controllerClass, String entityClass, 
            String converterName, String fieldName, String jsfFolder, final ElementHandle<ExecutableElement> idGetterHandle, String pkgName, FileObject controllerFileObject) {
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(wm);
        if (configFiles.length > 0) {
            // using first found faces-config.xml, is it OK?
            FileObject fo = configFiles[0];
            JSFConfigModel model = null;
            try {
                model = ConfigurationUtils.getConfigModel(fo, true);
                model.startTransaction();
                FacesConfig config = model.getRootComponent();
                ManagedBean mb = model.getFactory().createManagedBean();
                mb.setManagedBeanName(managedBean);
                mb.setManagedBeanClass(controllerClass);
                mb.setManagedBeanScope(ManagedBean.Scope.SESSION);
                config.addManagedBean(mb);

                Converter cv = model.getFactory().createConverter();
                cv.setConverterForClass(entityClass);
                cv.setConverterClass(converterName);
                config.addConverter(cv);
                
                final String[] idPropertyType = new String[1];
                //JavaSource javaSource = JavaSource.create(classpathInfo);
                JavaSource javaSource = JavaSource.forFileObject(controllerFileObject);
                javaSource.runModificationTask(new Task<WorkingCopy>() {
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        ExecutableElement idGetter = idGetterHandle.resolve(workingCopy);
                        if (TypeKind.DECLARED == idGetter.getReturnType().getKind()) {
                            DeclaredType declaredType = (DeclaredType) idGetter.getReturnType();
                            TypeElement typeElement = (TypeElement) declaredType.asElement();
                            if (JsfForm.isEmbeddableClass(typeElement)) {
                                idPropertyType[0] = typeElement.getQualifiedName().toString();
                            }
                        }
                    }
                });
                if (idPropertyType[0] != null) {
                    cv = model.getFactory().createConverter();
                    cv.setConverterForClass(idPropertyType[0]);
                    cv.setConverterClass((pkgName.length() > 0 ? pkgName + "." : "") + simpleClassName(idPropertyType[0]) + "Converter");
                    config.addConverter(cv);
                }
                
                NavigationRule nr = model.getFactory().createNavigationRule();
                NavigationCase nc = model.getFactory().createNavigationCase();
                nc.setFromOutcome(fieldName + "_create");
                nc.setToViewId("/" + jsfFolder + "/New.jsp");
                nr.addNavigationCase(nc);
                config.addNavigationRule(nr);

                nr = model.getFactory().createNavigationRule();
                nc = model.getFactory().createNavigationCase();
                nc.setFromOutcome(fieldName + "_list");
                nc.setToViewId("/" + jsfFolder + "/List.jsp");
                nr.addNavigationCase(nc);
                config.addNavigationRule(nr);

                nr = model.getFactory().createNavigationRule();
                nc = model.getFactory().createNavigationCase();
                nc.setFromOutcome(fieldName + "_edit");
                nc.setToViewId("/" + jsfFolder + "/Edit.jsp");
                nr.addNavigationCase(nc);
                config.addNavigationRule(nr);

                nr = model.getFactory().createNavigationRule();
                nc = model.getFactory().createNavigationCase();
                nc.setFromOutcome(fieldName + "_detail");
                nc.setToViewId("/" + jsfFolder + "/Detail.jsp");
                nr.addNavigationCase(nc);
                config.addNavigationRule(nr);

            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
            finally {
                //TODO: RETOUCHE correct write to JSF model?
                model.endTransaction();
            }
        }
    }
    
    private static FileObject generateConverter(
            final FileObject controllerFileObject,
            final FileObject pkg,
            final String simpleConverterName,
            final String controllerClass,
            final String simpleControllerName,
            final String entityClass,
            final String simpleEntityName,
            final ElementHandle<ExecutableElement> idGetter,
            final String managedBeanName,
            final boolean isInjection) throws IOException {

        final boolean[] embeddable = new boolean[] { false };
        final String[] idClassSimpleName = new String[1];
        final String[] idPropertyType = new String[1];
        final ArrayList<MethodModel> paramSetters = new ArrayList<MethodModel>();
        final boolean[] fieldAccess = new boolean[] { false };
        final String[] idGetterName = new String[1];
        JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
        controllerJavaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController compilationController) throws IOException {
                compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement idGetterElement = idGetter.resolve(compilationController);
                idGetterName[0] = idGetterElement.getSimpleName().toString();
                TypeMirror idType = idGetterElement.getReturnType();
                if (TypeKind.DECLARED == idType.getKind()) {
                    DeclaredType declaredType = (DeclaredType) idType;
                    TypeElement idClass = (TypeElement) declaredType.asElement();
                    embeddable[0] = idClass != null && JsfForm.isEmbeddableClass(idClass);
                    idClassSimpleName[0] = idClass.getSimpleName().toString();
                    idPropertyType[0] = idClass.getQualifiedName().toString();
                    fieldAccess[0] = JsfForm.isFieldAccess(idClass);
                    for (ExecutableElement method : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                        if (method.getSimpleName().toString().startsWith("set")) {
                            paramSetters.add(MethodModelSupport.createMethodModel(compilationController, method));
                        }
                    }
                }
            }
        }, true);
        
        String controllerReferenceName = controllerClass;
        StringBuffer getAsObjectBody = new StringBuffer();
        getAsObjectBody.append("if (string == null || string.length() == 0) {\n return null;\n }\n");

        String controllerVariable;
        if (isInjection) {
            controllerVariable= controllerReferenceName + " controller = (" 
                    + controllerReferenceName 
                    + ") facesContext.getApplication().getELResolver().getValue(\nfacesContext.getELContext(), null, \"" 
                    + managedBeanName +"\");\n";
        } else {
            controllerVariable = controllerReferenceName + " controller = ("
                    + controllerReferenceName 
                    + ") facesContext.getApplication().getVariableResolver().resolveVariable(\nfacesContext, \"" 
                    + managedBeanName +"\");\n";
        }
        if (embeddable[0]) {
            getAsObjectBody.append(idPropertyType[0] + " id = new " + idPropertyType[0] + "();\n");
            getAsObjectBody.append("StringTokenizer idTokens = new StringTokenizer(string, \";\");\n");
            int params = paramSetters.size();
            getAsObjectBody.append("String params[] = new String[" + params + "];\n"
                    + "int i = 0;\n while(idTokens.hasMoreTokens()) {\n"
                    + "params[i++] = idTokens.nextToken();\n }\n"
                    + "if (i != " + params + ") {\n"
                    + "throw new IllegalArgumentException(\"Expected format of parameter string is a set of "
                    + params + " IDs delimited by ;\");\n }\n");
            for (int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                getAsObjectBody.append("id.s" + setter.getName().substring(1) + "(" 
                        + createIdFieldInitialization(setter.getReturnType(), "params[" + i + "]") + ");\n");
            }

            getAsObjectBody.append(controllerVariable + "\n return controller.find" + simpleEntityName + "(id);");
        } else {
            getAsObjectBody.append(createIdFieldDeclaration(idPropertyType[0], "string") + "\n"
                    + controllerVariable
                    + "\n return controller.find" + simpleEntityName + "(id);");
        }
        
        final MethodModel getAsObject = MethodModel.create(
                "getAsObject",
                "java.lang.Object",
                getAsObjectBody.toString(),
                Arrays.asList(
                    MethodModel.Variable.create("javax.faces.context.FacesContext", "facesContext"),
                    MethodModel.Variable.create("javax.faces.component.UIComponent", "component"),
                    MethodModel.Variable.create("java.lang.String", "string")
                ),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );

        String entityReferenceName = entityClass;
        String idPropertyTypeRefName = null;
        StringBuffer getAsStringBody = new StringBuffer();
        StringBuffer getAsStringEBody = new StringBuffer();
        getAsStringBody.append("if (object == null) {\n return null;\n }\n"
                + "if(object instanceof " + entityReferenceName + ") {\n"
                + entityReferenceName + " o = (" + entityReferenceName +") object;\n");
        if (embeddable[0]) {
            idPropertyTypeRefName = idPropertyType[0];
            getAsStringEBody.append("if (object == null) {\n return null;\n }\n"
                    + "if(object instanceof " + idPropertyTypeRefName + ") {\n"
                    + idPropertyTypeRefName + " o = (" + idPropertyTypeRefName +") object;\n");
            getAsStringBody.append("return ");
            getAsStringEBody.append("return ");
            for(int i = 0; i < paramSetters.size(); i++) {
                if (i > 0) {
                    getAsStringBody.append(" + \";\" + ");
                    getAsStringEBody.append(" + \";\" + ");
                }
                getAsStringBody.append("o." + idGetter + "()." + paramSetters.get(i).getName() + "()");
                getAsStringEBody.append("o." + paramSetters.get(i).getName() + "()");
            }
            getAsStringBody.append(";\n");
            getAsStringEBody.append(";\n");
        } else {
            String oDotGetId = "o." + idGetterName[0] + "()";
            getAsStringBody.append("return " + oDotGetId + " == null ? \"\" : " + oDotGetId + ".toString();\n");
        }
        getAsStringBody.append("} else {\n"
                + "throw new IllegalArgumentException(\"object:\" + object + \" of type:\" + object.getClass().getName() + \"; expected type: " + entityClass +"\");\n}");
        
        final MethodModel getAsString = MethodModel.create(
                "getAsString",
                "java.lang.String",
                getAsStringBody.toString(),
                Arrays.asList(
                    MethodModel.Variable.create("javax.faces.context.FacesContext", "facesContext"),
                    MethodModel.Variable.create("javax.faces.component.UIComponent", "component"),
                    MethodModel.Variable.create("java.lang.Object", "object")
                ),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );

        FileObject converterFileObject = GenerationUtils.createClass(pkg, simpleConverterName, null);
        JavaSource converterJavaSource = JavaSource.forFileObject(converterFileObject);
        converterJavaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                TypeElement converterTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree classTree = workingCopy.getTrees().getTree(converterTypeElement);
                ClassTree modifiedClassTree = generationUtils.addImplementsClause(classTree, "javax.faces.convert.Converter");
                MethodTree getAsObjectTree = MethodModelSupport.createMethodTree(workingCopy, getAsObject);
                MethodTree getAsStringTree = MethodModelSupport.createMethodTree(workingCopy, getAsString);
                modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsObjectTree);
                modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsStringTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();

        if (embeddable[0]) {
            getAsStringEBody.append("} else {\n"
                    + "throw new IllegalArgumentException(\"object:\" + object + \" of type:\" + object.getClass().getName() + \"; expected type: " + idPropertyTypeRefName +"\");\n}");
            
            final MethodModel getAsStringE = MethodModel.create(
                    "getAsString",
                    "java.lang.String",
                    getAsStringEBody.toString(),
                    Arrays.asList(
                        MethodModel.Variable.create("javax.faces.context.FacesContext", "facesContext"),
                        MethodModel.Variable.create("javax.faces.component.UIComponent", "facesContext"),
                        MethodModel.Variable.create("java.lang.Object", "object")
                    ),
                    Collections.<String>emptyList(),
                    Collections.singleton(Modifier.PUBLIC)
                    );
            
            final MethodModel getAsObjectE = MethodModel.create(
                    "getAsObject",
                    "java.lang.Object",
                    getAsObjectBody.toString() + "return id;\n",
                    Arrays.asList(
                        MethodModel.Variable.create("javax.faces.context.FacesContext", "facesContext"),
                        MethodModel.Variable.create("javax.faces.component.UIComponent", "facesContext"),
                        MethodModel.Variable.create("java.lang.String", "string")
                    ),
                    Collections.<String>emptyList(),
                    Collections.singleton(Modifier.PUBLIC)
                    );
            
            FileObject idConverter = GenerationUtils.createClass(pkg, idClassSimpleName[0] + "Converter", null); //NOI18N
            JavaSource idConverterJavaSource = JavaSource.forFileObject(idConverter);
            idConverterJavaSource.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                    TypeElement idConverterTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    ClassTree classTree = workingCopy.getTrees().getTree(idConverterTypeElement);
                    ClassTree modifiedClassTree = generationUtils.addImplementsClause(classTree, "javax.faces.convert.Converter");
                    MethodTree getAsObjectETree = MethodModelSupport.createMethodTree(workingCopy, getAsObjectE);
                    MethodTree getAsStringETree = MethodModelSupport.createMethodTree(workingCopy, getAsStringE);
                    modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsObjectETree);
                    modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsStringETree);
                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }).commit();

        }

        return converterFileObject;
    }
    
    private static FileObject generateControllerClass(
            final String fieldName, 
            final FileObject pkg, 
            final ElementHandle<ExecutableElement> idGetter, 
            final String persistenceUnit, 
            final String simpleControllerName, 
            final String entityClass, 
            final String simpleEntityName,
            final List<ElementHandle<ExecutableElement>> toOneRelMethods,
            final List<ElementHandle<ExecutableElement>> toManyRelMethods,
            final boolean isInjection,
            final boolean isFieldAccess,
            final FileObject controllerFileObject) throws IOException {
        
            //FileObject controllerFileObject = GenerationUtils.createClass(pkg, simpleControllerName, null);
    
            final String[] idPropertyType = new String[1];
            final String[] idGetterName = new String[1];
            
            JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
            controllerJavaSource.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    ExecutableElement idGetterElement = idGetter.resolve(workingCopy);
                    idGetterName[0] = idGetterElement.getSimpleName().toString();
                    TypeMirror idType = idGetterElement.getReturnType();
                    if (TypeKind.DECLARED == idType.getKind()) {
                        DeclaredType declaredType = (DeclaredType) idType;
                        TypeElement idClass = (TypeElement) declaredType.asElement();
                        idPropertyType[0] = idClass.getQualifiedName().toString();
                    }
                    
                    
                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    TypeElement controllerTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    ClassTree classTree = workingCopy.getTrees().getTree(controllerTypeElement);
                    ClassTree modifiedClassTree = classTree;
                    
                    int privateModifier = java.lang.reflect.Modifier.PRIVATE;
                    int publicModifier = java.lang.reflect.Modifier.PUBLIC;
                    int publicStaticModifier = publicModifier + java.lang.reflect.Modifier.STATIC;
                    
//        JavaClass javaClass = null;
//        boolean rollback = true;
//        
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            javaClass = JMIGenerationUtil.createClass(pkg, simpleControllerName);
//            
//            String entityReferenceName = JMIGenerationUtil.createImport(javaClass, entityClass).getName();
//            Field entityField = JMIGenerationUtil.createField(javaClass, fieldName, Modifier.PRIVATE, entityClass);
//            javaClass.getFeatures().add(entityField);
//            
//            TypeReference dmReference = JMIGenerationUtil.createImport(javaClass, "javax.faces.model.DataModel"); //NOI18N
//            Field dmField = JMIGenerationUtil.createField(javaClass, "model", Modifier.PRIVATE, dmReference.getName()); //NOI18N
//            javaClass.getFeatures().add(dmField);
//            
//            TypeReference ldmReference = JMIGenerationUtil.createImport(javaClass, "javax.faces.model.ListDataModel"); //NOI18N
                    
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName, entityClass, privateModifier, null, null);
                   
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName + "s", new TypeInfo("java.util.List", new String[]{entityClass}), privateModifier, null, null);
                    
//            Field emfField = JMIGenerationUtil.createField(javaClass, "emf", Modifier.PRIVATE, "javax.persistence.EntityManagerFactory");
//            if (isInjection) {
//                Field utxField = JMIGenerationUtil.createField(javaClass, "utx", Modifier.PRIVATE, "javax.transaction.UserTransaction");
//                Annotation resourceAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.annotation.Resource", Collections.EMPTY_LIST); //NOI18N
//                utxField.getAnnotations().add(resourceAnnotation);
//                javaClass.getFeatures().add(utxField);
//                
//                Annotation persistenceContextAnnotation;
//                if (persistenceUnit == null) {
//                    persistenceContextAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.PersistenceUnit", Collections.EMPTY_LIST); //NOI18N
//                } else {
//                    AttributeValue persistenceContextAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "unitName", persistenceUnit); //NOI18N
//                    persistenceContextAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.PersistenceUnit", Collections.singletonList(persistenceContextAttrValue)); //NOI18N
//                }
//                emfField.getAnnotations().add(persistenceContextAnnotation);
//            } else {
//                
//                Constructor constr = javaClass.getConstructor(Collections.EMPTY_LIST, false);
//                boolean noDefaultConstructor = constr == null;
//                if (noDefaultConstructor) {
//                    constr = JMIGenerationUtil.createConstructor(javaClass, Modifier.PUBLIC);
//                }
//                JMIGenerationUtil.createImport(javaClass, "javax.persistence.Persistence");
//                constr.setBodyText("emf = Persistence.createEntityManagerFactory(\"" + persistenceUnit + "\");"); //NOI18N
//                if (noDefaultConstructor) {
//                    javaClass.getFeatures().add(constr);
//                }
//            }
//            javaClass.getFeatures().add(emfField);
                    
                    AnnotationInfo[] annotations = null;
                    if (isInjection) {
                        annotations = new AnnotationInfo[1];
                        annotations[0] = new AnnotationInfo("javax.annotation.Resource");
                        modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "utx", "javax.transaction.UserTransaction", privateModifier, null, annotations);
                        
                        if (persistenceUnit == null) {
                            annotations[0] = new AnnotationInfo("javax.persistence.PersistenceUnit");
                        } else {
                            annotations[0] = new AnnotationInfo("javax.persistence.PersistenceUnit", new String[]{"unitName"}, new Object[]{persistenceUnit});
                        }
                    } else {
                        Set<Modifier> publicModifierSet = new HashSet<Modifier>();
                        publicModifierSet.add(Modifier.PUBLIC);
                        MethodTree modifiedConstructor = make.Method(
                                make.Modifiers(publicModifierSet), // public
                                "<init>",
                                null, // return type
                                Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                Collections.<VariableTree>emptyList(), // arguments - none
                                Collections.<ExpressionTree>emptyList(), // throws 
                                "{ emf = Persistence.createEntityManagerFactory(\"" + persistenceUnit + "\"); }", // body text
                                null // default value - not applicable here, used by annotations
                            );
                        MethodTree constructor = null;
                        for(Tree tree : modifiedClassTree.getMembers()) {
                            if(Tree.Kind.METHOD == tree.getKind()) {
                                MethodTree mtree = (MethodTree)tree;
                                List<? extends VariableTree> mTreeParameters = mtree.getParameters();
                                if(mtree.getName().toString().equals("<init>") &&
                                        (mTreeParameters == null || mTreeParameters.size() == 0) &&
                                        !workingCopy.getTreeUtilities().isSynthetic(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), classTree))) {
                                        constructor = mtree;
                                        break;
                                }
                            }
                        }
                        if (constructor == null) {
                            modifiedClassTree = make.addClassMember(modifiedClassTree, modifiedConstructor);
                        }
                        else {
                            workingCopy.rewrite(constructor, modifiedConstructor);
                        }
                    }
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "emf", "javax.persistence.EntityManagerFactory", privateModifier, null, annotations);
                    
//            Method getEntityManager = JMIGenerationUtil.createMethod(javaClass, "getEntityManager", Modifier.PRIVATE, "javax.persistence.EntityManager");
//            getEntityManager.setBodyText("return emf.createEntityManager();");
//            javaClass.getFeatures().add(getEntityManager);
//            
//            Field batchSizeField = JMIGenerationUtil.createField(javaClass, "batchSize", Modifier.PRIVATE, "int");
//            batchSizeField.setInitialValueText("20");
//            javaClass.getFeatures().add(batchSizeField);
//            
//            Field firstItemField = JMIGenerationUtil.createField(javaClass, "firstItem", Modifier.PRIVATE, "int");
//            firstItemField.setInitialValueText("0");
//            javaClass.getFeatures().add(firstItemField);
                    
                    MethodInfo methodInfo = new MethodInfo("getEntityManager", publicModifier, "javax.persistence.EntityManager", null, null, null, "return emf.createEntityManager();", null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "batchSize", "int", publicModifier, new Integer(5), null);
                    
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "firstItem", "int", privateModifier, new Integer(0), null);
                    
                    modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "itemCount", "int", privateModifier, new Integer(-1), null);
                    
                    String bodyText = "return get" + simpleEntityName + "sAvailable(false);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "sAvailableSelectMany", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return get" + simpleEntityName + "sAvailable(true);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "sAvailableSelectOne", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "List<" + simpleEntityName + "> all" + simpleEntityName + "s = get" + simpleEntityName + "s(true);\n" + 
                        "int size = one ? all" + simpleEntityName + "s.size() + 1 : all" + simpleEntityName + "s.size();\n" + 
                        "SelectItem[] items = new SelectItem[size];\n" + 
                        "int i = 0;\n" + 
                        "if (one) {\n" + 
                        "items[0] = new SelectItem(\"\", \"---\");\n" + 
                        "i++;\n" + 
                        "}\n" + 
                        "for (" + simpleEntityName + " x : all" + simpleEntityName + "s) {\n" + 
                        "items[i++] = new SelectItem(x, x.toString());\n" + 
                        "}\n" + 
                        "return items;";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "sAvailable", privateModifier, "javax.faces.model.SelectItem[]", null, new String[]{"boolean"}, new String[]{"one"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    
//            StringBuffer updateRelatedInCreate = new StringBuffer();
//            StringBuffer updateRelatedInEditPre = new StringBuffer();
//            StringBuffer updateRelatedInEditPost = new StringBuffer();
//            StringBuffer updateRelatedInDestroy = new StringBuffer();
//            
//            List allRelMethods = new ArrayList(toOneRelMethods);
//            allRelMethods.addAll(toManyRelMethods);
//            
//            Method getEntity = JMIGenerationUtil.createMethod(javaClass, "get" + simpleEntityName, Modifier.PUBLIC, entityClass);  //NOI18N
//            getEntity.setBodyText("return " + fieldName + ";"); //NOI18N
//            javaClass.getFeatures().add(getEntity);
//            
//            Method setEntity = JMIGenerationUtil.createMethod(javaClass, "set" + simpleEntityName, Modifier.PUBLIC, "void");  //NOI18N
//            setEntity.setBodyText("this." + fieldName + " = " + fieldName + ";"); //NOI18N
//            Parameter entityParameter = JMIGenerationUtil.createParameter(javaClass, fieldName, entityClass);
//            setEntity.getParameters().add(entityParameter);
//            javaClass.getFeatures().add(setEntity);
//            
//            Method getEntityForDetail = JMIGenerationUtil.createMethod(javaClass, "getDetail" + simpleEntityName + "s", Modifier.PUBLIC, "DataModel");  //NOI18N
//            getEntityForDetail.setBodyText("return model;"); //NOI18N
//            javaClass.getFeatures().add(getEntityForDetail);
//            
//            JMIGenerationUtil.createImport(javaClass, "java.util.Collection");
//            JMIGenerationUtil.createImport(javaClass, "java.util.ArrayList"); //NOI18N
//            Method setEntityForDetail = JMIGenerationUtil.createMethod(javaClass, "setDetail" + simpleEntityName + "s", Modifier.PUBLIC, "void");  //NOI18N
//            setEntityForDetail.setBodyText("model = new ListDataModel(new ArrayList(m));"); //NOI18N
//            Parameter entityForDetailParameter = JMIGenerationUtil.createParameter(javaClass, "m", "Collection<" + entityReferenceName + ">"); //NOI18N
//            setEntityForDetail.getParameters().add(entityForDetailParameter);
//            javaClass.getFeatures().add(setEntityForDetail);
                    
                    StringBuffer updateRelatedInCreate = new StringBuffer();
                    StringBuffer updateRelatedInEditPre = new StringBuffer();
                    StringBuffer updateRelatedInEditPost = new StringBuffer();
                    StringBuffer updateRelatedInDestroy = new StringBuffer();
                    StringBuffer initRelatedInCreateSetup = new StringBuffer();

                    List<ElementHandle<ExecutableElement>> allRelMethods = new ArrayList<ElementHandle<ExecutableElement>>(toOneRelMethods);
                    allRelMethods.addAll(toManyRelMethods);
                    
//                    String setEntityName = "set" + simpleEntityName;
//                    String setEntityBodyText = "this." + fieldName + " = " + fieldName + ";";
                    
                    //generate "get" + simpleEntityName and setEntityName methods after the upcoming for loop because setEntityBodyText can be modified within the loop
                    
//                    methodInfo = new MethodInfo("getDetail" + simpleEntityName + "s", publicModifier, "javax.faces.model.DataModel", null, null, null, "return model;", null, null);
//                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    String[] importFqs = {"javax.persistence.Query",
                                "javax.faces.application.FacesMessage",
                                "javax.faces.context.FacesContext",
                                "java.lang.reflect.InvocationTargetException",
                                "java.lang.reflect.Method",
                                "javax.faces.FacesException",
                                "java.util.HashMap"
                    };
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }
                    
//                    TypeInfo[] typeInfos = {new TypeInfo("java.util.Collection", new String[]{entityClass})};
//                    methodInfo = new MethodInfo("setDetail" + simpleEntityName + "s", publicModifier, new TypeInfo("void"), null, typeInfos, new String[]{"m"}, "model = new ListDataModel(new ArrayList(m));", null, null);
//                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    

                    String entityReferenceName = entityClass;
                    
//            // <editor-fold desc=" all relations ">
//            for(Iterator it = allRelMethods.iterator(); it.hasNext();) {
//                Method m = (Method) it.next();
//                int multiplicity = JsfForm.isRelationship(m, isFieldAccess);
//                Method otherSide = JsfForm.getOtherSideOfRelation(m, isFieldAccess);
//                if (otherSide != null) {
//                    JavaClass relClass = (JavaClass) otherSide.getDeclaringClass();
//                    boolean isRelFieldAccess = JsfForm.isFieldAccess(relClass);
//                    int otherSideMultiplicity = JsfForm.isRelationship(otherSide, isRelFieldAccess);
//                    Type t = m.getType();
//                    boolean isCollection = false;
//                    if (t instanceof ParameterizedType) {
//                        MultipartId id = (MultipartId) m.getTypeName();
//                        for (Iterator iter = id.getTypeArguments().iterator(); iter.hasNext();) {
//                            MultipartId param = (MultipartId) iter.next();
//                            NamedElement parType = param.getElement();
//                            if (param instanceof JavaClass) {
//                                t = (JavaClass) param;
//                                isCollection = true;
//                            }
//                        }
//                    }
//                    String relTypeReference = JMIGenerationUtil.createImport(javaClass, t.getName()).getName();
//                    String relType = t.getName();
//                    String simpleRelType = simpleClassName(relType);
//                    String relFieldName = getPropNameFromMethod(m.getName());
                    
                    for(Iterator<ElementHandle<ExecutableElement>> it = allRelMethods.iterator(); it.hasNext();) {
                        ElementHandle<ExecutableElement> handle = it.next();
                        ExecutableElement m = handle.resolve(workingCopy);
                        int multiplicity = JsfForm.isRelationship(workingCopy, m, isFieldAccess);
                        ExecutableElement otherSide = JsfForm.getOtherSideOfRelation(workingCopy, m, isFieldAccess);

                        if (otherSide != null) {
                            TypeElement relClass = (TypeElement)otherSide.getEnclosingElement();
                            boolean isRelFieldAccess = JsfForm.isFieldAccess(relClass);
                            int otherSideMultiplicity = JsfForm.isRelationship(workingCopy, otherSide, isRelFieldAccess);
                            TypeMirror t = m.getReturnType();
                            TypeMirror tstripped = JsfForm.stripCollection(t, workingCopy.getTypes());
                            boolean isCollection = t != tstripped;
                            String relType = tstripped.toString();
                            String simpleRelType = simpleClassName(relType); //just "Pavilion"
                            String relTypeReference = simpleRelType;
                            String mName = m.getSimpleName().toString();
                            String otherName = otherSide.getSimpleName().toString();
                            String relFieldName = getPropNameFromMethod(mName);
                            
//                    updateRelatedInCreate.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            (isCollection ? "for(" + relTypeReference + " " + relFieldName + " : " + fieldName + "." + m.getName() + "()){\n" :
//                                relTypeReference + " " + relFieldName + "=" + fieldName + "." + m.getName() +"();\n" +
//                                "if (" + relFieldName + " != null) {\n") +
//                            relFieldName + " = em.merge(" + relFieldName +");\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + ".s" + otherSide.getName().substring(1) + "(" + fieldName+ ");\n" :
//                                relFieldName + "." + otherSide.getName() + "().add(" + fieldName +");\n") +
//                            relFieldName + "=em.merge(" + relFieldName +");\n}\n\n");
                            
                            if (isCollection) {
                                initRelatedInCreateSetup.append("if " + fieldName + "." + mName + "() == null) {\n" + 
                                        fieldName + ".s" + mName.substring(1) + "(new ArrayList<" + relTypeReference + ">());\n}\n");
                            }
                            
                            String relTypeInstanceName = relTypeReference.substring(0,1).toLowerCase() + relTypeReference.substring(1);
                            if (multiplicity == JsfForm.REL_TO_MANY && otherSideMultiplicity == JsfForm.REL_TO_ONE){
                                modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.ArrayList");
                                updateRelatedInCreate.append("Map<" + simpleEntityName + ",List<" + relTypeReference + ">> " + relTypeInstanceName + "sToRemove = new HashMap<" + simpleEntityName + ",List<" + relTypeReference + ">>();\n");
                            }
                            updateRelatedInCreate.append("\n//update property " + relFieldName + " of entity " + fieldName + "\n" + //mbohm: why doesn't this show
                                                        (isCollection ? "for(" + relTypeReference + " " + relTypeInstanceName + " : " + fieldName + "." + mName + "()){\n" :
                                                            relTypeReference + " " + relTypeInstanceName + "=" + fieldName + "." + mName +"();\n" +
                                                            "if (" + relTypeInstanceName + " != null) {\n"));
                                                            //if 1:1, be sure to orphan the related entity's current related entity
                            String relrelInstanceName = "old" + simpleEntityName;
                            String relrelGetterName = otherName;
                            if (otherSideMultiplicity == JsfForm.REL_TO_ONE){
                                updateRelatedInCreate.append(simpleEntityName + " " + relrelInstanceName + " = " + relTypeInstanceName + "." + relrelGetterName + "();\n");
                                if (multiplicity == JsfForm.REL_TO_ONE) {
                                        updateRelatedInCreate.append("if (" + relrelInstanceName + " != null) {\n" + 
                                        relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
                                        "}\n");    
                                }
                            }
                            updateRelatedInCreate.append( ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relTypeInstanceName + ".s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                                            relTypeInstanceName + "." + otherName + "().add(" + fieldName +");\n") +
                                                        relTypeInstanceName + " = em.merge(" + relTypeInstanceName +");\n");
                            if (multiplicity == JsfForm.REL_TO_MANY && otherSideMultiplicity == JsfForm.REL_TO_ONE){
                                String relTypeListName = relTypeInstanceName + "List";
                                updateRelatedInCreate.append("if " + relrelInstanceName + " != null) {\n" +
                                        "List<" + relTypeReference + "> " + relTypeListName + " = " + relTypeInstanceName + "sToRemove.get(" + relrelInstanceName + ");\n" +
                                        "if " + relTypeListName + " == null) {\n" +
                                        relTypeListName + " = new ArrayList<" + relTypeReference + ">();\n" +
                                        relTypeInstanceName + "sToRemove.put(" + relrelInstanceName + ", " + relTypeListName + ");\n" +
                                        "}\n" +
                                        relTypeListName + ".add(" + relTypeInstanceName + ");\n" +
                                        "}\n}\n" +
                                        "for (" + simpleEntityName + " " + relrelInstanceName + " : " + relTypeInstanceName + "sToRemove.keySet()) {\n" +
                                        "List<" + relTypeReference + "> " + relTypeListName + " = " + relTypeInstanceName + "sToRemove.get(" + relrelInstanceName + ");\n" +
                                        "for (" + relTypeReference + " " + relTypeInstanceName + " : " + relTypeListName + ") {\n" +
                                        relrelInstanceName + "." + mName + "().remove(" + relTypeInstanceName + ");\n" +
                                        "}\n" +
                                        relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n");
                                        
                            }
                            updateRelatedInCreate.append("}\n");
                            
//                    if (isCollection) {
//                        updateRelatedInEditPre.append("\n Collection<" + relTypeReference + "> " + relFieldName + "sOld = em.find("
//                            + entityReferenceName +".class, " + fieldName + "." + idGetter + "())." + m.getName() + "();\n");
//                        updateRelatedInEditPost.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            "Collection <" + relTypeReference + "> " + relFieldName + "sNew = " + fieldName + "." + m.getName() + "();\n" +
//                            "for(" + relTypeReference + " " + relFieldName + "New : " + relFieldName + "sNew) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "New.s" + otherSide.getName().substring(1) + "(" + fieldName+ ");\n" :
//                                relFieldName + "New." + otherSide.getName() + "().add(" + fieldName +");\n") +
//                            relFieldName + "New=em.merge(" + relFieldName +"New);\n}\n" +
//                            "for(" + relTypeReference + " " + relFieldName + "Old : " + relFieldName + "sOld) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "Old.s" + otherSide.getName().substring(1) + "(null);\n" :
//                                relFieldName + "Old." + otherSide.getName() + "().remove(" + fieldName +");\n") +
//                            relFieldName + "Old=em.merge(" + relFieldName +"Old);\n}\n");
//                    } else {
//                        updateRelatedInEditPre.append("\n" + relTypeReference + " " + relFieldName + "Old = em.find("
//                            + entityReferenceName +".class, " + fieldName + "." + idGetter + "())." + m.getName() + "();\n");
//                        updateRelatedInEditPost.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            relTypeReference + " " + relFieldName + "New = " + fieldName + "." + m.getName() +"();\n" +
//                            "if(" + relFieldName + "New != null) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "New.s" + otherSide.getName().substring(1) + "(" + fieldName+ ");\n" :
//                                relFieldName + "New." + otherSide.getName() + "().add(" + fieldName +");\n") +
//                            relFieldName + "New=em.merge(" + relFieldName +"New);\n}\n" +
//                            "if(" + relFieldName + "Old != null) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "Old.s" + otherSide.getName().substring(1) + "(null);\n" :
//                                relFieldName + "Old." + otherSide.getName() + "().remove(" + fieldName +");\n") +
//                            relFieldName + "Old=em.merge(" + relFieldName +"Old);\n}\n");
//                    } 
                            
                            if (isCollection) {
                                updateRelatedInEditPre.append("\n Collection<" + relTypeReference + "> " + relTypeInstanceName + "sOld = em.find(" +
                                    simpleEntityName + ".class, " + fieldName + "." + idGetterName[0] + "())." + mName + "();\n");
                                updateRelatedInEditPost.append("\n//update property " + relTypeInstanceName + " of entity " + fieldName + "\n" + //mbohm: why doesn't this show up
                                    "Collection <" + relTypeReference + "> " + relTypeInstanceName + "sNew = " + fieldName + "." + mName + "();\n" +
                                    "for(" + relTypeReference + " " + relTypeInstanceName + "Old : " + relTypeInstanceName + "sOld) {\n" +
                                    ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relTypeInstanceName + "Old.s" + otherName.substring(1) + "(null);\n" :
                                        relTypeInstanceName + "Old." + otherName + "().remove(" + fieldName +");\n") +
                                    relTypeInstanceName + "Old = em.merge(" + relTypeInstanceName +"Old);\n}\n");
                                    if (otherSideMultiplicity == JsfForm.REL_TO_ONE) {
                                       updateRelatedInEditPost.append("Map<" + simpleEntityName + ",List<" + relTypeReference + ">> " + relTypeInstanceName + "sToRemove = new HashMap<" + simpleEntityName + ",List<" + relTypeReference + ">>();\n"); 
                                    }
                                    updateRelatedInEditPost.append("for(" + relTypeReference + " " + relTypeInstanceName + "New : " + relTypeInstanceName + "sNew) {\n" +
                                    ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? simpleEntityName + " " + relrelInstanceName + " = " + relTypeInstanceName + "New." + relrelGetterName + "();\n" +
                                        relTypeInstanceName + "New.s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                        relTypeInstanceName + "New." + otherName + "().add(" + fieldName +");\n") +
                                    relTypeInstanceName + "New=em.merge(" + relTypeInstanceName +"New);\n");
                                    String relTypeListName = relTypeInstanceName + "List";
                                    if (otherSideMultiplicity == JsfForm.REL_TO_ONE) {
                                        updateRelatedInEditPost.append("if " + relrelInstanceName + " != null) {\n" +
                                            "List<" + relTypeReference + "> " + relTypeListName + " = " + relTypeInstanceName + "sToRemove.get(" + relrelInstanceName + ");\n" +
                                            "if " + relTypeListName + " == null) {\n" +
                                            relTypeListName + " = new ArrayList<" + relTypeReference + ">();\n" +
                                            relTypeInstanceName + "sToRemove.put(" + relrelInstanceName + ", " + relTypeListName + ");\n" +
                                            "}\n" +
                                            relTypeListName + ".add(" + relTypeInstanceName + "New);\n" +
                                            "}\n");
                                    }
                                    updateRelatedInEditPost.append("}\n");
                                    if (otherSideMultiplicity == JsfForm.REL_TO_ONE) {
                                            updateRelatedInEditPost.append("for (" + simpleEntityName + " " + relrelInstanceName + " : " + relTypeInstanceName + "sToRemove.keySet()) {\n" +
                                            "List<" + relTypeReference + "> " + relTypeListName + " = " + relTypeInstanceName + "sToRemove.get(" + relrelInstanceName + ");\n" +
                                            "for (" + relTypeReference + " " + relTypeInstanceName + " : " + relTypeListName + ") {\n" +
                                            relrelInstanceName + "." + mName + "().remove(" + relTypeInstanceName + ");\n" +
                                            "}\n" +
                                            relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n}\n");
                                    }
                            } else {
                                updateRelatedInEditPre.append("\n" + relTypeReference + " " + relFieldName + "Old = em.find("
                                    + entityReferenceName +".class, " + fieldName + "." + idGetterName[0] + "())." + mName + "();\n");
                                updateRelatedInEditPost.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
                                    relTypeReference + " " + relFieldName + "New = " + fieldName + "." + mName +"();\n" +
                                    
                                    "if(" + relFieldName + "Old != null && !" + relFieldName + "Old.equals(" + relFieldName + "New)) {\n" +
                                    ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "Old.s" + otherName.substring(1) + "(null);\n" :
                                        relFieldName + "Old." + otherName + "().remove(" + fieldName +");\n") +
                                    relFieldName + "Old = em.merge(" + relFieldName +"Old);\n}\n" +
                                    "if(" + relFieldName + "New != null && !" + relFieldName + "New.equals(" + relFieldName + "Old)) {\n");
                                if (multiplicity == JsfForm.REL_TO_ONE && otherSideMultiplicity == JsfForm.REL_TO_ONE){
                                    updateRelatedInEditPost.append(simpleEntityName + " " + relrelInstanceName + " = " + relFieldName + "New." + relrelGetterName + "();\n" + 
                                            "if (" + relrelInstanceName + " != null) {\n" + 
                                            relrelInstanceName + ".s" + mName.substring(1) + "(null);\n" + 
                                            relrelInstanceName + " = em.merge(" + relrelInstanceName + ");\n" + 
                                            "}\n");
                                }
                                updateRelatedInEditPost.append(
                                    ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "New.s" + otherName.substring(1) + "(" + fieldName+ ");\n" :
                                        relFieldName + "New." + otherName + "().add(" + fieldName +");\n") +
                                    relFieldName + "New=em.merge(" + relFieldName +"New);\n}\n"
                                    );
                            } 
                            
//                    updateRelatedInDestroy.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            (isCollection ? "Collection<" + relTypeReference + "> " + relFieldName + "s" : relTypeReference + " " + relFieldName) + " = " + fieldName + "." + m.getName() +"();\n" +
//                            (isCollection ? "for(" + relTypeReference + " " + relFieldName + " : " + relFieldName + "s" : "if (" + relFieldName + " != null") + ") {\n" +
//                            relFieldName + " = em.merge(" + relFieldName +");\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + ".s" + otherSide.getName().substring(1) + "(null);\n" :
//                                relFieldName + "." + otherSide.getName() + "().remove(" + fieldName +");\n") +
//                            relFieldName + "=em.merge(" + relFieldName +");\n}\n\n");

                            updateRelatedInDestroy.append("\n//update property " + relFieldName + " of entity " + fieldName + "\n" +    //mbohm: why doesn't this show up
                                    (isCollection ? "Collection<" + relTypeReference + "> " + relTypeInstanceName + "s" : relTypeReference + " " + relTypeInstanceName) + " = " + fieldName + "." + mName +"();\n" +
                                    (isCollection ? "for(" + relTypeReference + " " + relTypeInstanceName + " : " + relTypeInstanceName + "s" : "if (" + relTypeInstanceName + " != null") + ") {\n" +
                                    ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relTypeInstanceName + ".s" + otherName.substring(1) + "(null);\n" :
                                        relTypeInstanceName + "." + otherName + "().remove(" + fieldName +");\n") +
                                    relTypeInstanceName + " = em.merge(" + relTypeInstanceName +");\n}\n\n");
                            
//                    Method destroyFromDetail = JMIGenerationUtil.createMethod(javaClass, "destroyFrom" + simpleRelType, Modifier.PUBLIC, "String"); //NOI18N
//                    String relIdGetter = JsfForm.getIdGetter(isRelFieldAccess, relClass).getName();
//                    destroyFromDetail.setBodyText(simpleRelType + " param = get" + simpleRelType + "Controller().get" + simpleRelType + "();\n"
//                            + "destroy();\n"
//                            + "EntityManager em = getEntityManager();\n try {\n"
//                            + "get" + simpleRelType + "Controller().set" + simpleRelType 
//                            + "(em.find(" + relTypeReference + ".class, param." + relIdGetter + "()));\n"
//                            + "} finally {\n em.close();\n}\n"
//                            + "return \"" + getManagedBeanName(simpleRelType) + "_detail\";\n");
//                    javaClass.getFeatures().add(destroyFromDetail);
                            
//                            String relIdGetter = JsfForm.getIdGetter(workingCopy, isRelFieldAccess, relClass).getSimpleName().toString();
//                            bodyText = simpleRelType + " param = get" + simpleRelType + "Controller().get" + simpleRelType + "();\n" + 
//                                    "destroy();\n" + 
//                                    "EntityManager em = getEntityManager();\n try {\n" + 
//                                    "get" + simpleRelType + "Controller().set" + simpleRelType + 
//                                    "(em.find(" + relTypeReference + ".class, param." + relIdGetter + "()));\n" + 
//                                    "} finally {\n em.close();\n}\n" + 
//                                    "return \"" + getManagedBeanName(simpleRelType) + "_detail\";\n";
//                            
//                            methodInfo = new MethodInfo("destroyFrom" + simpleRelType, publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
//                            modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                            
//                    Method controllerAccess = JMIGenerationUtil.createMethod(javaClass, "get" + simpleRelType + "Controller", Modifier.PRIVATE, simpleRelType + "Controller"); //NOI18N
//                    if (isInjection) {
//                        controllerAccess.setBodyText("FacesContext context = FacesContext.getCurrentInstance();\n"
//                                + "return (" + simpleRelType +"Controller) context.getApplication().getELResolver().getValue(\n context.getELContext(), null, \"" 
//                                + getManagedBeanName(simpleRelType) +"\");\n");
//                    } else {
//                        controllerAccess.setBodyText("FacesContext context = FacesContext.getCurrentInstance();\n"
//                                + "return (" + simpleRelType +"Controller) context.getApplication().getVariableResolver().resolveVariable(\n context, \"" 
//                                + getManagedBeanName(simpleRelType) +"\");\n");
//                    }
//                    javaClass.getFeatures().add(controllerAccess);
                            
//                            if (isInjection) {
//                                bodyText = "FacesContext context = FacesContext.getCurrentInstance();\n" + 
//                                        "return (" + simpleRelType +"Controller) context.getApplication().getELResolver().getValue(\n context.getELContext(), null, \"" + 
//                                        getManagedBeanName(simpleRelType) +"\");\n";
//                            } else {
//                                bodyText = "FacesContext context = FacesContext.getCurrentInstance();\n" + 
//                                        "return (" + simpleRelType +"Controller) context.getApplication().getVariableResolver().resolveVariable(\n context, \""  +
//                                        getManagedBeanName(simpleRelType) +"\");\n";
//                            }
//                            
//                            String controllerAccessName = "get" + simpleRelType + "Controller";
//                            methodInfo = new MethodInfo(controllerAccessName, privateModifier, relType + "Controller", null, null, null, bodyText, null, null);
//                            modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                            
//                    if (multiplicity == JsfForm.REL_TO_MANY) {
//                        setEntity.setBodyText(setEntity.getBodyText() + "\n"
//                                + controllerAccess.getName() + "().setDetail" + simpleRelType 
//                                + "s(" + fieldName + "." + m.getName() + "());");
//                    }
                            
//                            if (otherSideMultiplicity == JsfForm.REL_TO_ONE) {
//                                bodyText = setEntityName + "(" + controllerAccessName + "().get" + simpleRelType + "()." + otherName + "();\nreturn \"" + fieldName + "_detail\";\n";
//                                methodInfo = new MethodInfo("detailSetupFrom" + simpleRelType + "Detail", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
//                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
//                            }
                            
//                            if (multiplicity == JsfForm.REL_TO_MANY) {
//                                setEntityBodyText += "\n" +
//                                controllerAccessName + "().setDetail" + simpleRelType +
//                                "s(" + fieldName + "." + mName + "());";
//                            }
                            
//                    if (multiplicity == JsfForm.REL_TO_MANY && otherSideMultiplicity == JsfForm.REL_TO_MANY) {
//                        //methods needed to add items into N:M relationship
//                        JMIGenerationUtil.createImport(javaClass, "javax.faces.model.SelectItem");
//                        JMIGenerationUtil.createImport(javaClass, "java.util.List");
//                        Method getRelatedAvailable = JMIGenerationUtil.createMethodArray(javaClass, m.getName() + "Available", Modifier.PUBLIC, "javax.faces.model.SelectItem");
//                        getRelatedAvailable.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                                + "Query q = em.createQuery(\"select o from " + simpleRelType + " as o where "
//                                + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? ":param not member of o." + getPropNameFromMethod(otherSide.getName()) + "\");\n" : 
//                                    "o." + getPropNameFromMethod(otherSide.getName()) + " <> :param or o." + getPropNameFromMethod(otherSide.getName()) + " IS NULL\");\n")
//                                + "q.setParameter(\"param\", " + fieldName + ");\n"
//                                + "List <" + simpleRelType + "> l = (List <" + simpleRelType + ">) q.getResultList();\n"
//                                + "SelectItem select[] = new SelectItem[l.size()];\n"
//                                + "int i = 0;\n"
//                                + "for(" + simpleRelType + " x : l) {\n"
//                                + "select[i++] = new SelectItem(x);\n"
//                                + "}\n return select;\n"
//                                + "} finally {\n em.close();\n}\n");
//                        javaClass.getFeatures().add(getRelatedAvailable);
                            
                            if (multiplicity == JsfForm.REL_TO_MANY) {
//                                //methods needed to add items into N:M relationship
//                                modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.List");
//                                bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
//                                        "Query q = em.createQuery(\"select o from " + simpleRelType + " as o where " + 
//                                        (otherSideMultiplicity == JsfForm.REL_TO_MANY ? ":param not member of o." + getPropNameFromMethod(otherName) + "\");\n" : 
//                                            "o." + getPropNameFromMethod(otherName) + " <> :param or o." + getPropNameFromMethod(otherName) + " IS NULL\");\n") + 
//                                        "q.setParameter(\"param\", " + fieldName + ");\n" + 
//                                        "List <" + simpleRelType + "> l = (List <" + simpleRelType + ">) q.getResultList();\n" + 
//                                        "SelectItem select[] = new SelectItem[l.size()];\n" + 
//                                        "int i = 0;\n" + 
//                                        "for(" + simpleRelType + " x : l) {\n" + 
//                                        "select[i++] = new SelectItem(x);\n" + 
//                                        "}\n return select;\n" + 
//                                        "} finally {\n em.close();\n}\n";
//                                methodInfo = new MethodInfo(mName + "Available", publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
//                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                
//                        Field relatedToAdd = JMIGenerationUtil.createFieldArray(javaClass, getPropNameFromMethod(m.getName()), Modifier.PUBLIC, relTypeReference);
//                        javaClass.getFeatures().add(relatedToAdd);
//
//                        Method getRelatedToAdd = JMIGenerationUtil.createMethodArray(javaClass, m.getName() + "ToAdd", Modifier.PUBLIC, relTypeReference);
//                        getRelatedToAdd.setBodyText("return " + relatedToAdd.getName() + ";\n");
//                        javaClass.getFeatures().add(getRelatedToAdd);
//
//                        Method setRelatedToAdd = JMIGenerationUtil.createMethod(javaClass, "s" + m.getName().substring(1) + "ToAdd", Modifier.PUBLIC, "void");
//                        Parameter setRelatedToAddParam = JMIGenerationUtil.createParameterArray(javaClass, relatedToAdd.getName(), relTypeReference);
//                        setRelatedToAdd.getParameters().add(setRelatedToAddParam);
//                        setRelatedToAdd.setBodyText("this." + relatedToAdd.getName() + " = " + relatedToAdd.getName() + ";\n");
//                        javaClass.getFeatures().add(setRelatedToAdd);
                                
                                importFqs = new String[]{"java.util.Arrays",
                                            "java.util.Collection"
                                  };
                                for (String importFq : importFqs) {
                                    modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                                }
                                
                                String relatedToAddName = getPropNameFromMethod(mName);
//                                String relatedCollectionType = relType + "[]";
//                                modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, relatedToAddName, relatedCollectionType, publicModifier, null, null);
//
//                                methodInfo = new MethodInfo(mName + "ToAdd", publicModifier, relatedCollectionType, null, null, null, "return " + relatedToAddName + ";\n", null, null);
//                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
//                                
//                                methodInfo = new MethodInfo("s" + mName.substring(1) + "ToAdd", publicModifier, "void", null, new String[]{relatedCollectionType}, new String[]{relatedToAddName}, "this." + relatedToAddName + " = " + relatedToAddName + ";\n", null, null);
//                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                
                                bodyText = "List<" + simpleRelType + "> " + relatedToAddName + "List = Arrays.asList(" + relatedToAddName + ");\n" +
                                        fieldName + ".s" + mName.substring(1) + "(" + relatedToAddName + "List);";
                                methodInfo = new MethodInfo("s" + mName.substring(1) + "Of" + simpleEntityName, publicModifier, "void", null, new String[]{relType + "[]"}, new String[]{relatedToAddName}, bodyText, null, null);
                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                
                                bodyText = "Collection<" + simpleRelType + "> " + relatedToAddName + " = " + fieldName + "." + mName + "();\n" +
                                        "if (" + relatedToAddName + " == null) {\n" + 
                                        "return new " + simpleRelType + "[0];\n" + 
                                        "}\n" + 
                                        "return " + relatedToAddName + ".toArray(new " + simpleRelType + "[0]);";
                                methodInfo = new MethodInfo(mName + "Of" + simpleEntityName, publicModifier, relType + "[]", null, null, null, bodyText, null, null);
                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                
//                        Method addRelated = JMIGenerationUtil.createMethod(javaClass, "add" + m.getName().substring(3), Modifier.PUBLIC, "String");
//                        addRelated.setBodyText("EntityManager em = getEntityManager();\n"
//                                + "try {\n em.getTransaction().begin();\n"
//                                + "for(" + simpleRelType + " entity : " + relatedToAdd.getName() + ") {\n"
//                                + "entity." + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? otherSide.getName() + "().add(" + fieldName + ");\n" : "s" + otherSide.getName().substring(1) + "(" + fieldName + ");\n")
//                                + "entity = em.merge(entity);\n"
//                                + fieldName + "." + m.getName() + "().add(entity);\n"
//                                + "}\n"
//                                + fieldName + " = em.merge(" + fieldName + ");\n"
//                                + "em.getTransaction().commit();\n"
//                                + setEntity.getName() + "(" + fieldName + ");\n"
//                                + "addSuccessMessage(\"" + simpleRelType + " successfully added.\");\n"
//                                + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n"
//                                + "em.getTransaction().rollback();\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n"
//                                + "}\n } finally {\n em.close();\n }\n"
//                                + "return \"" + fieldName + "_detail\";\n");
//                        javaClass.getFeatures().add(addRelated);
//                                bodyText = "";
//                                bodyText = "if (" + relatedToAddName + " == null || " + relatedToAddName + ".length == 0) {\n" + 
//                                        "addErrorMessage(\"You must select one or more " + simpleRelType.toLowerCase() + "s to add.\");\n" + 
//                                        "return null;\n" + 
//                                        "}\n";
//                                bodyText += "EntityManager em = getEntityManager();\n" +
//                                    "try {\n utx.begin();\n" + 
//                                    "for(" + simpleRelType + " entity : " + relatedToAddName + ") {\n" + 
//                                    "entity." + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? otherName + "().add(" + fieldName + ");\n" : "s" + otherName.substring(1) + "(" + fieldName + ");\n") + 
//                                    "entity = em.merge(entity);\n" + 
//                                    fieldName + "." + mName + "().add(entity);\n" + 
//                                    "}\n" + 
//                                    fieldName + " = em.merge(" + fieldName + ");\n" + 
//                                    "utx.commit();\n" + 
//                                    setEntityName + "(" + fieldName + ");\n" + 
//                                    "addSuccessMessage(\"" + simpleRelType + " successfully added.\");\n" +
//                                    "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + 
//                                    "utx.rollback();\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n" + 
//                                    "}\nreturn null;\n} finally {\n em.close();\n }\n" + 
//                                    "return \"" + fieldName + "_detail\";\n";
//                                methodInfo = new MethodInfo("add" + mName.substring(3), publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
//                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                
//                        Method removeRelated = JMIGenerationUtil.createMethod(javaClass, "remove" + m.getName().substring(3), Modifier.PUBLIC, "String");
//                        removeRelated.setBodyText("EntityManager em = getEntityManager();\n"
//                                + "try {\n"
//                                + "em.getTransaction().begin();\n"
//                                + simpleRelType + " entity = (" + simpleRelType +") " + controllerAccess.getName() + "().getDetail" + simpleRelType + "s().getRowData();\n"
//                                + "entity." + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? otherSide.getName() + "().remove(" + fieldName + ");\n" : "s" + otherSide.getName().substring(1) + "(null);\n")
//                                + "entity = em.merge(entity);\n"
//                                + fieldName + "." + m.getName() + "().remove(entity);\n"
//                                + fieldName + " = em.merge(" + fieldName + ");\n"
//                                + "em.getTransaction().commit();\n"
//                                + setEntity.getName() + "(" + fieldName + ");\n"
//                                + "addSuccessMessage(\"" + simpleEntityName + " successfully removed.\");\n"
//                                + "} catch (Exception ex) {\n"
//                                + "try {\n"
//                                + "addErrorMessage(ex.getLocalizedMessage());\n"
//                                + "em.getTransaction().rollback();\n"
//                                + "} catch (Exception e) {\n"
//                                + "addErrorMessage(e.getLocalizedMessage());\n"
//                                + "}\n } finally {\n em.close();\n }\n"
//                                + "return \"" + fieldName + "_detail\";\n");
//                        javaClass.getFeatures().add(removeRelated);
//                    }
                                
//                                bodyText = "EntityManager em = getEntityManager();\n" + 
//                                    "try {\n" + 
//                                    "utx.begin();\n" + 
//                                    simpleRelType + " entity = (" + simpleRelType +") " + controllerAccessName + "().getDetail" + simpleRelType + "s().getRowData();\n" + 
//                                    "entity." + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? otherName + "().remove(" + fieldName + ");\n" : "s" + otherName.substring(1) + "(null);\n") + 
//                                    "entity = em.merge(entity);\n" + 
//                                    fieldName + "." + mName + "().remove(entity);\n" + 
//                                    fieldName + " = em.merge(" + fieldName + ");\n" + 
//                                    "utx.commit();\n" + 
//                                    setEntityName + "(" + fieldName + ");\n" + 
//                                    "addSuccessMessage(\"" + simpleRelType + " successfully removed.\");\n" +
//                                    "} catch (Exception ex) {\n" + 
//                                    "try {\n" + 
//                                    "addErrorMessage(ex.getLocalizedMessage());\n" + 
//                                    "utx.rollback();\n" + 
//                                    "} catch (Exception e) {\n" + 
//                                    "addErrorMessage(e.getLocalizedMessage());\n" + 
//                                    "}\nreturn null;\n} finally {\n em.close();\n }\n" + 
//                                    "return \"" + fieldName + "_detail\";\n";
//                                methodInfo = new MethodInfo("remove" + mName.substring(3), publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
//                                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                            }
                            
//                    Method createFromDetailSetup = JMIGenerationUtil.createMethod(javaClass, "createFrom" + simpleRelType +"Setup", Modifier.PUBLIC, "String"); //NOI18N
//                    createFromDetailSetup.setBodyText("this." + fieldName + " = new " + entityReferenceName + "();\n"
//                            + "EntityManager em = getEntityManager();\n try{\n"
//                            + (isCollection ? "if (" + fieldName + "." + m.getName() + "() == null) {\n" + fieldName + ".s" + m.getName().substring(1) + "(new ArrayList());\n}\n" : "")
//                            + fieldName + (isCollection ? "." + m.getName() + "().add" : ".s" + m.getName().substring(1)) + "(em.find(" + relTypeReference + ".class, get" + simpleRelType + "Controller().get" + simpleRelType + "()." + relIdGetter + "()));\n"
//                            + "} finally {\n em.close();\n}\n"
//                            + "return \"" + getManagedBeanName(simpleEntityName) + "_create\";\n");
//                    javaClass.getFeatures().add(createFromDetailSetup);
//                    
//                    Method createFromDetail = JMIGenerationUtil.createMethod(javaClass, "createFrom" + simpleRelType, Modifier.PUBLIC, "String"); //NOI18N
//                    createFromDetail.setBodyText("create();\n" +
//                            "get" + simpleRelType + "Controller().set" + simpleRelType + "(" + fieldName + "." + m.getName() + "()"
//                            + (isCollection ? ".iterator().next()" : "") + ");\n" +
//                            "return \"" + getManagedBeanName(simpleRelType) + "_detail\";\n");
//                    javaClass.getFeatures().add(createFromDetail);
//                } else {
//                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
//                }
//            }
//            // </editor-fold>
                            
//                            bodyText = "this." + fieldName + " = new " + entityReferenceName + "();\n" + 
//                                "EntityManager em = getEntityManager();\n try{\n" + 
//                                (isCollection ? "if (" + fieldName + "." + mName + "() == null) {\n" + fieldName + ".s" + mName.substring(1) + "(new ArrayList());\n}\n" : "") + 
//                                fieldName + (isCollection ? "." + mName + "().add" : ".s" + mName.substring(1)) + "(em.find(" + relTypeReference + ".class, get" + simpleRelType + "Controller().get" + simpleRelType + "()." + relIdGetter + "()));\n" + 
//                                "} finally {\n em.close();\n}\n" + 
//                                "return \"" + getManagedBeanName(simpleEntityName) + "_create\";\n";
//                            methodInfo = new MethodInfo("createFrom" + simpleRelType +"Setup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
//                            modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                            
//                            bodyText = "create();\n" +
//                                "get" + simpleRelType + "Controller().set" + simpleRelType + "(" + fieldName + "." + mName + "()" + 
//                                (isCollection ? ".iterator().next()" : "") + ");\n" +
//                                "return \"" + getManagedBeanName(simpleRelType) + "_detail\";\n";
//                            methodInfo = new MethodInfo("createFrom" + simpleRelType, publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
//                            modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                            
                        } else {
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
                        }

                    }
                    
                    String getFromReqParamMethod = "get" + simpleEntityName + "FromRequest";
                    
                    bodyText = "if (" + fieldName + " == null) {\n" +
                            fieldName + " = " + getFromReqParamMethod + "();\n" +
                            "}\n" + 
                            "if (" + fieldName + " == null) {\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" +
                            "}\n" + 
                            "return " + fieldName + ";";
                    methodInfo = new MethodInfo("get" + simpleEntityName, publicModifier, entityClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

//                    methodInfo = new MethodInfo(setEntityName, publicModifier, "void", null, new String[]{entityClass}, new String[]{fieldName}, setEntityBodyText, null, null);
//                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//            Method createSetup = JMIGenerationUtil.createMethod(javaClass, "createSetup", Modifier.PUBLIC, "String");  //NOI18N
//            createSetup.setBodyText("this." + fieldName + " = new " + entityReferenceName + "();\n return \"" + fieldName + "_create\";"); //NOI18N
//            javaClass.getFeatures().add(createSetup);
                    
                    bodyText = "reset(true);\n" + 
                            "return \"" + fieldName + "_list\";";
                    methodInfo = new MethodInfo("listSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//                    bodyText = "this." + fieldName + " = new " + entityReferenceName + "();\n"; 
//                    bodyText += initRelatedInCreateSetup.toString();
                    bodyText = "reset(false);\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" + 
                            "return \"" + fieldName + "_create\";";
                    methodInfo = new MethodInfo("createSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//            String BEGIN = isInjection ? "utx.begin();" : "em.getTransaction().begin();";
//            String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
//            String ROLLBACK = isInjection ? "utx.rollback();" : "em.getTransaction().rollback();";
//            
//            Method create = JMIGenerationUtil.createMethod(javaClass, "create", Modifier.PUBLIC, "String");  //NOI18N
//            create.setBodyText("EntityManager em = getEntityManager();\n"
//                    + "try {\n " + BEGIN + "\n em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n"  //NOI18N
//                    + "addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n" //NOI18N
//                    + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + ROLLBACK + "\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n}\n } "  //NOI18N
//                    + "finally {\n em.close();\n }\n"
//                    + "return \"" + fieldName + "_list\";");
//            javaClass.getFeatures().add(create);
            
                    //mbohm: have calls to em.joinTransaction() after BEGIN been removed on purpose?
                    String BEGIN = isInjection ? "utx.begin();" : "em.getTransaction().begin();";
                    String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
                    String ROLLBACK = isInjection ? "utx.rollback();" : "em.getTransaction().rollback();";
                    
                    String newEntityStringVar = "new" + simpleEntityName + "String";
                    String entityStringVar = fieldName + "String";
                    bodyText = simpleEntityName + "Converter converter = new " + simpleEntityName + "Converter();\n" +
                            "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new " + simpleEntityName + "());\n" +
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "if (!" + newEntityStringVar + ".equals(" + entityStringVar + ")) {\n" +
                            "addErrorMessage(\"Could not create " + fieldName + ". Try again.\");\n" +
                            "return createSetup();\n" +
                            "}\n";
                    bodyText += "EntityManager em = getEntityManager();\n" + 
                            "try {\n " + BEGIN + "\n em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n" +   //NOI18N
                            "addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n"  + //NOI18N
                            "} catch (Exception ex) {\n try {\n ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" + ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
                            "}\nreturn null;\n} " +   //NOI18N
                            "finally {\n em.close();\n }\n" + 
                            "return listSetup();";
                    methodInfo = new MethodInfo("create", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
//            String setFromReqParamMethod = "set" + simpleEntityName + "FromRequestParam";
//            String getFromReqParamMethod = "get" + simpleEntityName + "FromRequestParam";
//            
//            Method showSetup = JMIGenerationUtil.createMethod(javaClass, "detailSetup", Modifier.PUBLIC, "String");  //NOI18N
//            showSetup.setBodyText(setFromReqParamMethod + "();\n return \"" + fieldName + "_detail\";"); //NOI18N
//            javaClass.getFeatures().add(showSetup);
//            
//            Method editSetup = JMIGenerationUtil.createMethod(javaClass, "editSetup", Modifier.PUBLIC, "String");  //NOI18N
//            editSetup.setBodyText(setFromReqParamMethod + "();\n return \"" + fieldName + "_edit\";"); //NOI18N
//            javaClass.getFeatures().add(editSetup);
                    
//                    String setFromReqParamMethod = "set" + simpleEntityName + "FromRequestParam";
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_detail\");";
                    methodInfo = new MethodInfo("detailSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_edit\");";
                    methodInfo = new MethodInfo("editSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = "reset(false);\n" + 
                            fieldName + " = " + getFromReqParamMethod + "();\n" +
                            "if (" + fieldName + " == null) {\n" +
                            "String request" + simpleEntityName + "String = getRequestParameter(\"jsfcrud.current" +  simpleEntityName + "\");\n" +
                            "addErrorMessage(\"The " + fieldName + " with id \" + request" + simpleEntityName + "String + \" no longer exists.\");\n";
                    String relatedControllerOutcomeSwath = "String relatedControllerOutcome = relatedControllerOutcome();\n" +
                            "if (relatedControllerOutcome != null {\n" +
                            "return relatedControllerOutcome;\n" +
                            "}\n";
                    bodyText += relatedControllerOutcomeSwath + 
                            "return listSetup();\n" +
                            "}\n" +
                            "return destination;";
                    methodInfo = new MethodInfo("scalarSetup", privateModifier, "java.lang.String", null, new String[]{"java.lang.String"}, new String[]{"destination"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

//            
//            Method edit = JMIGenerationUtil.createMethod(javaClass, "edit", Modifier.PUBLIC, "String");  //NOI18N
//            edit.setBodyText("EntityManager em = getEntityManager();\n"
//                    + "try {\n " + BEGIN + "\n" + updateRelatedInEditPre.toString() 
//                    + fieldName + " = em.merge(" + fieldName + ");\n "
//                    + updateRelatedInEditPost.toString() + COMMIT + "\n"  //NOI18N
//                    + "addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n"  //NOI18N
//                    + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + ROLLBACK + "\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n}\n} "  //NOI18N
//                    + "finally {\n em.close();\n }\n" //NOI18N
//                    + "return \"" + fieldName + "_list\";"); //NOI18N
//            javaClass.getFeatures().add(edit);
//
//            
//            Method destroy = JMIGenerationUtil.createMethod(javaClass, "destroy", Modifier.PUBLIC, "String");  //NOI18N
//            destroy.setBodyText("EntityManager em = getEntityManager();\n"
//                    + "try {\n " + BEGIN + "\n" + entityReferenceName + " " + fieldName + " = " + getFromReqParamMethod + "();\n"
//                    + fieldName + " = em.merge(" + fieldName + ");\n" + updateRelatedInDestroy.toString() 
//                    + "em.remove(" + fieldName + ");\n " + COMMIT + "\n"  //NOI18N
//                    + "addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n"  //NOI18N
//                    + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + ROLLBACK + "\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n}\n} "  //NOI18N
//                    + "finally {\n em.close();\n }\n" //NOI18N
//                    + "return \"" + fieldName + "_list\";"); //NOI18N
//            javaClass.getFeatures().add(destroy);
//            
//            String idField = createIdFieldDeclaration(idPropertyType, "param");
          
                    entityStringVar = fieldName + "String";
                    String currentEntityStringVar = "current" + simpleEntityName + "String";
                    bodyText = simpleEntityName + "Converter converter = new " + simpleEntityName + "Converter();\n" +
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "String " + currentEntityStringVar + " = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "if " + entityStringVar + " == null || " + entityStringVar + ".length() == 0 || !" + entityStringVar + ".equals(" + currentEntityStringVar + ")) {\n" +
                            "String outcome = editSetup();\n" +
                            "if (\"" + fieldName + "_edit\".equals(outcome)) {\n" +
                            "addErrorMessage(\"Could not edit " + fieldName + ". Try again.\");\n" +
                            "}\n" +
                            "return outcome;\n" +
                            "}\n";
                    bodyText += "EntityManager em = getEntityManager();\n" + 
                        "try {\n " + BEGIN + "\n" + updateRelatedInEditPre.toString() + 
                        fieldName + " = em.merge(" + fieldName + ");\n " + 
                        updateRelatedInEditPost.toString() + COMMIT + "\n" +   //NOI18N
                        "addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n" +   //NOI18N
                        "} catch (Exception ex) {\n try {\n String msg = ex.getLocalizedMessage();\n" + 
                        "if (msg != null && msg.length() > 0) {\n" +
                        "addErrorMessage(msg);\n" +
                        "}\n" +
                        "else if (" + getFromReqParamMethod + "() == null) {\n" +
                        "addErrorMessage(\"The " + fieldName + " with id \" + current" + simpleEntityName + "String + \" no longer exists.\");\n" +
                        ROLLBACK +
                        "\nreturn listSetup();\n" +
                        "}\n" +
                        "else {\n" +
                        "addErrorMessage(\"A persistence error occurred.\");\n" +
                        "}\n" +
                        ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
                        "}\nreturn null;\n} " +   //NOI18N
                        "finally {\n em.close();\n }\n" +  //NOI18N
                        "return detailSetup();";
                    methodInfo = new MethodInfo("edit", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = fieldName + " = " + getFromReqParamMethod + "();\n" +
                            "if (" + fieldName + " == null) {\n" +
                            "String current" + simpleEntityName + "String = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "addErrorMessage(\"The " + fieldName + " with id \" + current" + simpleEntityName + "String + \" no longer exists.\");\n" +
                            relatedControllerOutcomeSwath + 
                            "return listSetup();\n" +
                            "}\n";                    
                    bodyText += "EntityManager em = getEntityManager();\n" + 
                        "try {\n " + BEGIN + "\n" + 
                        fieldName + " = em.merge(" + fieldName + ");\n" + updateRelatedInDestroy.toString() + 
                        "em.remove(" + fieldName + ");\n " + COMMIT + "\n" +   //NOI18N
                        "addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n" +   //NOI18N
                        "} catch (Exception ex) {\n try {\n ensureAddErrorMessage(ex, \"A persistence error occurred.\");\n" + ROLLBACK + "\n } catch (Exception e) {\n ensureAddErrorMessage(e, \"An error occurred attempting to roll back the transaction.\");\n" + 
                        "}\nreturn null;\n} " +   //NOI18N
                        "finally {\n em.close();\n }\n" +  //NOI18N
                        relatedControllerOutcomeSwath + 
                            "return listSetup();";
                    methodInfo = new MethodInfo("destroy", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    String idField = createIdFieldDeclaration(idPropertyType[0], "param");

//            JMIGenerationUtil.createImport(javaClass, "javax.faces.context.FacesContext");
//            JMIGenerationUtil.createImport(javaClass, "javax.faces.application.FacesMessage");
//            Method getFromReq = JMIGenerationUtil.createMethod(javaClass, getFromReqParamMethod, Modifier.PUBLIC, entityClass);  //NOI18N
//            JavaClass idClass = null;
//            Type idType = JMIUtils.resolveType(idPropertyType);
//            if (idType instanceof JavaClass) {
//                idClass = (JavaClass) idType;
//            }
//            boolean embeddable = idClass != null && JsfForm.isEmbeddableClass(idClass);
//            getFromReq.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + entityReferenceName + " o = (" + entityReferenceName +") model.getRowData();\n"
//                    + "o = em.merge(o);\n"
//                    + "return o;\n"
//                    + "} finally {\n em.close();\n}\n");
//            javaClass.getFeatures().add(getFromReq);
                    
                    bodyText = "String theId = getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "return (" + simpleEntityName + ")new " + simpleEntityName + "Converter().getAsObject(FacesContext.getCurrentInstance(), null, theId);";
                    methodInfo = new MethodInfo(getFromReqParamMethod, privateModifier, entityClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
                    bodyText = "return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);";
                    methodInfo = new MethodInfo("getRequestParameter", privateModifier, "java.lang.String", null, new String[]{"java.lang.String"}, new String[]{"key"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);        
                    
//            
//            Method setFromReq = JMIGenerationUtil.createMethod(javaClass, setFromReqParamMethod, Modifier.PUBLIC, "void");  //NOI18N
//            setFromReq.setBodyText(entityReferenceName + " " + fieldName + " = " + getFromReqParamMethod + "();\n" //NOI18N
//                + "set" + simpleEntityName + "(" + fieldName + ");"); //NOI18N
//            javaClass.getFeatures().add(setFromReq);
                    
//                    bodyText = entityReferenceName + " " + fieldName + " = " + getFromReqParamMethod + "();\n" + //NOI18N
//                        "set" + simpleEntityName + "(" + fieldName + ");";
//                    methodInfo = new MethodInfo(setFromReqParamMethod, publicModifier, "void", null, null, null, bodyText, null, null);
//                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
//            Method getEntities = JMIGenerationUtil.createMethod(javaClass, "get" + simpleEntityName + "s", Modifier.PUBLIC, dmReference.getName());  //NOI18N
//            JMIGenerationUtil.createImport(javaClass, "javax.persistence.Query");
//            getEntities.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + "Query q = em.createQuery(\"select object(o) from " + simpleEntityName +" as o\");\n"
//                    + "q.setMaxResults(batchSize);\n"
//                    + "q.setFirstResult(firstItem);\n"
//                    + "model = new " + ldmReference.getName() + "(q.getResultList());\n"
//                    + "return model;\n" //NOI18N
//                    + "} finally {\n em.close();\n}\n");
//            javaClass.getFeatures().add(getEntities);
                    
                    TypeInfo listOfEntityType = new TypeInfo("java.util.List", new String[]{entityClass});
                    
                    bodyText = "if (" + fieldName + "s == null) {\n" +
                            fieldName + "s = get" + simpleEntityName + "s(false);\n" +
                            "}\n" +
                            "return " + fieldName + "s;";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "s", publicModifier, listOfEntityType, null, null, null, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        "Query q = em.createQuery(\"select object(o) from " + simpleEntityName +" as o\");\n" + 
                        "if (!all) {\n" +
                        "q.setMaxResults(batchSize);\n" + 
                        "q.setFirstResult(getFirstItem());\n" + 
                        "}\n" +
                        "return q.getResultList();\n" + 
                        "} finally {\n em.close();\n}\n";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "s", publicModifier, listOfEntityType, null, TypeInfo.fromStrings(new String[]{"boolean"}), new String[]{"all"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
//            Method addErrorMessage = JMIGenerationUtil.createMethod(javaClass, "addErrorMessage", Modifier.PUBLIC + Modifier.STATIC, "void");  //NOI18N
//            addErrorMessage.setBodyText("FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);\n" //NOI18N
//                + "FacesContext fc = FacesContext.getCurrentInstance();\n" //NOI18N
//                + "fc.addMessage(null, facesMsg);"); //NOI18N
//            Parameter msgParameter = JMIGenerationUtil.createParameter(javaClass, "msg", "String");
//            addErrorMessage.getParameters().add(msgParameter);
//            javaClass.getFeatures().add(addErrorMessage);
//            
//            Method addSuccessMessage = JMIGenerationUtil.createMethod(javaClass, "addSuccessMessage", Modifier.PUBLIC + Modifier.STATIC, "void");  //NOI18N
//            addSuccessMessage.setBodyText("FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);\n" //NOI18N
//                + "FacesContext fc = FacesContext.getCurrentInstance();\n" //NOI18N
//                + "fc.addMessage(\"successInfo\", facesMsg);"); //NOI18N
//            Parameter successMsgParameter = JMIGenerationUtil.createParameter(javaClass, "msg", "String");
//            addSuccessMessage.getParameters().add(successMsgParameter);
//            javaClass.getFeatures().add(addSuccessMessage);
//            
//            //getter for converter
//            Method findById = JMIGenerationUtil.createMethod(javaClass, "find" + simpleEntityName, Modifier.PUBLIC, entityClass);  //NOI18N
//            findById.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + entityReferenceName + " o = (" + entityReferenceName + ") em.find(" + entityReferenceName + ".class, id);\n"
//                    + "return o;\n"
//                    + "} finally {\n em.close();\n}\n");
//            Parameter idParameter = JMIGenerationUtil.createParameter(javaClass, "id", idPropertyType);
//            findById.getParameters().add(idParameter);
//            javaClass.getFeatures().add(findById);
                    
                    bodyText = "String msg = ex.getLocalizedMessage();\n" +
                            "if (msg != null && msg.length() > 0) {\n" +
                            "addErrorMessage(msg);\n" +
                            "}\n" +
                            "else {\n" +
                            "addErrorMessage(defaultMsg);\n" +
                            "}\n";
                    methodInfo = new MethodInfo("ensureAddErrorMessage", privateModifier, "void", null, new String[]{"java.lang.Exception", "java.lang.String"}, new String[]{"ex", "defaultMsg"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);\n" + //NOI18N
                        "FacesContext.getCurrentInstance().addMessage(null, facesMsg);"; //NOI18N
                    methodInfo = new MethodInfo("addErrorMessage", publicStaticModifier, "void", null, new String[]{"java.lang.String"}, new String[]{"msg"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);\n" + //NOI18N
                        "FacesContext.getCurrentInstance().addMessage(\"successInfo\", facesMsg);"; //NOI18N
                    methodInfo = new MethodInfo("addSuccessMessage", publicStaticModifier, "void", null, new String[]{"java.lang.String"}, new String[]{"msg"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    //getter for converter
                    bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
                        simpleEntityName + " o = (" + simpleEntityName + ") em.find(" + simpleEntityName + ".class, id);\n" + 
                        "return o;\n" + 
                        "} finally {\n em.close();\n}\n";
                    methodInfo = new MethodInfo("find" + simpleEntityName, publicModifier, entityClass, null, new String[]{idPropertyType[0]}, new String[]{"id"}, bodyText, null, null);
                    modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                    
              // <editor-fold desc=" toOne relations ">
//            for(Iterator it = toOneRelMethods.iterator(); it.hasNext();) {
//                Method m = (Method) it.next();
//                String relType = m.getType().getName();
//                String simpleRelType = simpleClassName(relType);
//              for(Iterator<ElementHandle<ExecutableElement>> it = toOneRelMethods.iterator(); it.hasNext();) {
//                    ElementHandle<ExecutableElement> handle = it.next();
//                    ExecutableElement m = handle.resolve(workingCopy);
//                    String mName = m.getSimpleName().toString();
//                    TypeMirror t = m.getReturnType();
//                    String relType = JsfForm.stripCollection(t, workingCopy.getTypes()).toString();
//                    String simpleRelType = simpleClassName(relType);
//                    String relTypeReference = simpleRelType;
//                
//                String methodName = m.getName() + "s";
//                    String methodName = mName + "s";
                    
//                //make sure we do not generate >1 getter for each type
//                boolean alredyGenerated = false;
//                for (Iterator it2 = javaClass.getFeatures().iterator(); it2.hasNext();) {
//                    Feature f = (Feature) it2.next();
//                    if (methodName.equals(f.getName())) {
//                        alredyGenerated = true;
//                        break;
//                    }
//                }
                    
                    //make sure we do not generate >1 getter for each type
//                    boolean alreadyGenerated = false;
//                    for(Tree tree : modifiedClassTree.getMembers()) {
//                        if(Tree.Kind.METHOD == tree.getKind()) {
//                            MethodTree mtree = (MethodTree)tree;
//                            if(mtree.getName().toString().equals(methodName)) {
//                                alreadyGenerated = true;
//                                break;
//                            }
//                        }
//                    }
                    
//                if (!alredyGenerated) {
//                    Method selectItems = JMIGenerationUtil.createMethodArray(javaClass, methodName, Modifier.PUBLIC, "javax.faces.model.SelectItem"); //NOI18N
//                    JMIGenerationUtil.createImport(javaClass, "javax.faces.model.SelectItem"); //NOI18N
//                    JMIGenerationUtil.createImport(javaClass, "java.util.List"); //NOI18N
//                    String relTypeReference = JMIGenerationUtil.createImport(javaClass, relType).getName();
//                    selectItems.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                            + "List <" + relTypeReference + "> l = (List <" + relTypeReference +">) em.createQuery(\"select o from " + simpleRelType + " as o\").getResultList();\n"
//                            + "SelectItem select[] = new SelectItem[l.size()];\n"
//                            + "int i = 0;\n for(" + relTypeReference + " x : l) {\n"
//                            + "select[i++] = new SelectItem(x);\n}\nreturn select;\n"
//                            + "} finally {\n em.close();\n}\n");
//                    javaClass.getFeatures().add(selectItems);
//                }
//            }
//            // </editor-fold>

//                    if (!alreadyGenerated) {
//                        modifiedImportCut = TreeMakerUtils.createImport(workingCopy, modifiedImportCut, "java.util.List");
//                        bodyText = "EntityManager em = getEntityManager();\n try{\n" + 
//                                "List <" + relTypeReference + "> l = (List <" + relTypeReference +">) em.createQuery(\"select o from " + simpleRelType + " as o\").getResultList();\n" + 
//                                "SelectItem select[] = new SelectItem[l.size()];\n" + 
//                                "int i = 0;\n for(" + relTypeReference + " x : l) {\n" + 
//                                "select[i++] = new SelectItem(x);\n}\nreturn select;\n" + 
//                                "} finally {\n em.close();\n}\n";
//                        methodInfo = new MethodInfo(methodName, publicModifier, "javax.faces.model.SelectItem[]", null, null, null, bodyText, null, null);
//                        modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
//                    }
//               }
               // </editor-fold>
                    
//            Method getItemCount = JMIGenerationUtil.createMethod(javaClass, "getItemCount", Modifier.PUBLIC, "int");
//            getItemCount.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + "int count = ((Long) em.createQuery(\"select count(o) from " + simpleEntityName + " as o\").getSingleResult()).intValue();\n"
//                    + "return count;\n"
//                    + "} finally {\n em.close();\n}\n");
//            javaClass.getFeatures().add(getItemCount);
//            
//            Method getFirstItem = JMIGenerationUtil.createMethod(javaClass, "getFirstItem", Modifier.PUBLIC, "int");
//            getFirstItem.setBodyText("return firstItem;");
//            javaClass.getFeatures().add(getFirstItem);
//            
//            Method getLastItem = JMIGenerationUtil.createMethod(javaClass, "getLastItem", Modifier.PUBLIC, "int");
//            getLastItem.setBodyText("int size = getItemCount();\n return firstItem + batchSize > size ? size : firstItem + batchSize;\n");
//            javaClass.getFeatures().add(getLastItem);
//
//            Method getBatchSize = JMIGenerationUtil.createMethod(javaClass, "getBatchSize", Modifier.PUBLIC, "int");
//            getBatchSize.setBodyText("return batchSize;");
//            javaClass.getFeatures().add(getBatchSize);

                bodyText = "if (itemCount == -1) {\n" +
                        "EntityManager em = getEntityManager();\n try{\n" + 
                    "itemCount = ((Long) em.createQuery(\"select count(o) from " + simpleEntityName + " as o\").getSingleResult()).intValue();\n" + 
                    "} finally {\n em.close();\n}\n" +
                    "}\n" +
                    "return itemCount;";
                methodInfo = new MethodInfo("getItemCount", publicModifier, "int", null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                bodyText = "getItemCount();\n" +
                        "if (firstItem >= itemCount) {\n" +
                        "if (itemCount == 0) {\n" +
                        "firstItem = 0;\n" +
                        "}\n" +
                        "else {\n" +
                        "int zeroBasedItemCount = itemCount - 1;\n" +
                        "double pageDouble = zeroBasedItemCount / batchSize;\n" +
                        "int page = (int)Math.floor(pageDouble);\n" +
                        "firstItem = page * batchSize;\n" +
                        "}\n" +
                        "}\n" +
                        "return firstItem;";
                methodInfo = new MethodInfo("getFirstItem", publicModifier, "int", null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                bodyText = "getFirstItem();\n" +
                        "return firstItem + batchSize > itemCount ? itemCount : firstItem + batchSize;";
                methodInfo = new MethodInfo("getLastItem", publicModifier, "int", null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                methodInfo = new MethodInfo("getBatchSize", publicModifier, "int", null, null, null, "return batchSize;", null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 
                
//            Method next = JMIGenerationUtil.createMethod(javaClass, "next", Modifier.PUBLIC, "String");
//            next.setBodyText("if (firstItem + batchSize < getItemCount()) {\n"
//                    + "firstItem += batchSize;\n}\n"
//                    + "return \"" + fieldName + "_list\";\n");
//            javaClass.getFeatures().add(next);
//            
//            Method prev = JMIGenerationUtil.createMethod(javaClass, "prev", Modifier.PUBLIC, "String");
//            prev.setBodyText("firstItem -= batchSize;\n if (firstItem < 0) {\nfirstItem = 0;\n}\n"
//                    + "return \"" + fieldName + "_list\";\n");
//            javaClass.getFeatures().add(prev);
//            
//            rollback = false;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            JavaModel.getJavaRepository().endTrans(rollback);
//        }
//        return javaClass;

                bodyText = "reset(false);\n" +
                        "getFirstItem();\n" +
                        "if firstItem + batchSize < itemCount) {\n" +
                        "firstItem += batchSize;\n" +
                        "}\n" +
                        "return \"" + fieldName + "_list\"";
                methodInfo = new MethodInfo("next", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                
                bodyText = "reset(false);\n" +
                        "getFirstItem();\n" +
                        "firstItem -= batchSize;\n if (firstItem < 0) {\nfirstItem = 0;\n}\n" + 
                    "return \"" + fieldName + "_list\";\n";
                methodInfo = new MethodInfo("prev", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                
                bodyText = "String relatedControllerString = getRequestParameter(\"jsfcrud.relatedController\");\n" +
                    "String relatedControllerTypeString = getRequestParameter(\"jsfcrud.relatedControllerType\");\n" +
                    "if (relatedControllerString != null && relatedControllerTypeString != null) {\n" +
                    "FacesContext context = FacesContext.getCurrentInstance();\n" +
                    "Object relatedController = context.getApplication().getELResolver().getValue(context.getELContext(), null, relatedControllerString);\n" +
                    "try {\n" +
                    "Class<?> relatedControllerType = Class.forName(relatedControllerTypeString);\n" +
                    "Method detailSetupMethod = relatedControllerType.getMethod(\"detailSetup\");\n" +
                    "return (String)detailSetupMethod.invoke(relatedController);\n" +
                    "} catch (ClassNotFoundException e) {\n" +
                    "throw new FacesException(e);\n" +
                    "} catch (NoSuchMethodException e) {\n" +
                    "throw new FacesException(e);\n" +
                    "} catch (IllegalAccessException e) {\n" +
                    "throw new FacesException(e);\n" +
                    "} catch (InvocationTargetException e) {\n" +
                    "throw new FacesException(e);\n" +
                    "}\n" +
                    "}\n" +
                    "return null;";
                methodInfo = new MethodInfo("relatedControllerOutcome", privateModifier, "java.lang.String", null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                bodyText = fieldName + " = null;\n" +
                        fieldName + "s = null;\n" +
                        "itemCount = -1;\n" +
                        "if (resetFirstItem) {\n" +
                        "firstItem = 0;\n" +
                        "}\n";
                methodInfo = new MethodInfo("reset", privateModifier, "void", null, new String[]{"boolean"}, new String[]{"resetFirstItem"}, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    
                
                TypeInfo asStringType = new TypeInfo("java.util.Map", new String[]{entityClass, "java.lang.String"});
                modifiedClassTree = TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "asString", asStringType, privateModifier, null, null);
                
                bodyText = "if (asString == null) {\n" +
                        "asString = new HashMap<" + simpleEntityName + ",String>() {" +
                        "@Override\n" +
                        "public String get(Object key) {\n" +
                        "return new " + simpleEntityName + "Converter().getAsString(FacesContext.getCurrentInstance(), null, (" + simpleEntityName + ")key);\n" +
                        "}\n" +
                        "};\n" +
                        "}\n" +
                        "return asString;";
                methodInfo = new MethodInfo("getAsString", publicModifier, asStringType, null, null, null, bodyText, null, null);
                modifiedClassTree = TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    
                
                workingCopy.rewrite(classTree, modifiedClassTree);
              }
            }).commit();
    
    
        return controllerFileObject;
        
//        JavaClass javaClass = null;
//        boolean rollback = true;
//        
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            javaClass = JMIGenerationUtil.createClass(pkg, simpleControllerName);
//            
//            String entityReferenceName = JMIGenerationUtil.createImport(javaClass, entityClass).getName();
//            Field entityField = JMIGenerationUtil.createField(javaClass, fieldName, Modifier.PRIVATE, entityClass);
//            javaClass.getFeatures().add(entityField);
//            
//            TypeReference dmReference = JMIGenerationUtil.createImport(javaClass, "javax.faces.model.DataModel"); //NOI18N
//            Field dmField = JMIGenerationUtil.createField(javaClass, "model", Modifier.PRIVATE, dmReference.getName()); //NOI18N
//            javaClass.getFeatures().add(dmField);
//            
//            TypeReference ldmReference = JMIGenerationUtil.createImport(javaClass, "javax.faces.model.ListDataModel"); //NOI18N
//
//            Field emfField = JMIGenerationUtil.createField(javaClass, "emf", Modifier.PRIVATE, "javax.persistence.EntityManagerFactory");
//            if (isInjection) {
//                Field utxField = JMIGenerationUtil.createField(javaClass, "utx", Modifier.PRIVATE, "javax.transaction.UserTransaction");
//                Annotation resourceAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.annotation.Resource", Collections.EMPTY_LIST); //NOI18N
//                utxField.getAnnotations().add(resourceAnnotation);
//                javaClass.getFeatures().add(utxField);
//                
//                Annotation persistenceContextAnnotation;
//                if (persistenceUnit == null) {
//                    persistenceContextAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.PersistenceUnit", Collections.EMPTY_LIST); //NOI18N
//                } else {
//                    AttributeValue persistenceContextAttrValue = JMIGenerationUtil.createAttributeValue(javaClass, "unitName", persistenceUnit); //NOI18N
//                    persistenceContextAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.PersistenceUnit", Collections.singletonList(persistenceContextAttrValue)); //NOI18N
//                }
//                emfField.getAnnotations().add(persistenceContextAnnotation);
//            } else {
//                
//                Constructor constr = javaClass.getConstructor(Collections.EMPTY_LIST, false);
//                boolean noDefaultConstructor = constr == null;
//                if (noDefaultConstructor) {
//                    constr = JMIGenerationUtil.createConstructor(javaClass, Modifier.PUBLIC);
//                }
//                JMIGenerationUtil.createImport(javaClass, "javax.persistence.Persistence");
//                constr.setBodyText("emf = Persistence.createEntityManagerFactory(\"" + persistenceUnit + "\");"); //NOI18N
//                if (noDefaultConstructor) {
//                    javaClass.getFeatures().add(constr);
//                }
//            }
//            javaClass.getFeatures().add(emfField);
//            
//            Method getEntityManager = JMIGenerationUtil.createMethod(javaClass, "getEntityManager", Modifier.PRIVATE, "javax.persistence.EntityManager");
//            getEntityManager.setBodyText("return emf.createEntityManager();");
//            javaClass.getFeatures().add(getEntityManager);
//            
//            Field batchSizeField = JMIGenerationUtil.createField(javaClass, "batchSize", Modifier.PRIVATE, "int");
//            batchSizeField.setInitialValueText("20");
//            javaClass.getFeatures().add(batchSizeField);
//            
//            Field firstItemField = JMIGenerationUtil.createField(javaClass, "firstItem", Modifier.PRIVATE, "int");
//            firstItemField.setInitialValueText("0");
//            javaClass.getFeatures().add(firstItemField);
//            
//            StringBuffer updateRelatedInCreate = new StringBuffer();
//            StringBuffer updateRelatedInEditPre = new StringBuffer();
//            StringBuffer updateRelatedInEditPost = new StringBuffer();
//            StringBuffer updateRelatedInDestroy = new StringBuffer();
//            
//            List allRelMethods = new ArrayList(toOneRelMethods);
//            allRelMethods.addAll(toManyRelMethods);
//            
//            Method getEntity = JMIGenerationUtil.createMethod(javaClass, "get" + simpleEntityName, Modifier.PUBLIC, entityClass);  //NOI18N
//            getEntity.setBodyText("return " + fieldName + ";"); //NOI18N
//            javaClass.getFeatures().add(getEntity);
//            
//            Method setEntity = JMIGenerationUtil.createMethod(javaClass, "set" + simpleEntityName, Modifier.PUBLIC, "void");  //NOI18N
//            setEntity.setBodyText("this." + fieldName + " = " + fieldName + ";"); //NOI18N
//            Parameter entityParameter = JMIGenerationUtil.createParameter(javaClass, fieldName, entityClass);
//            setEntity.getParameters().add(entityParameter);
//            javaClass.getFeatures().add(setEntity);
//            
//            Method getEntityForDetail = JMIGenerationUtil.createMethod(javaClass, "getDetail" + simpleEntityName + "s", Modifier.PUBLIC, "DataModel");  //NOI18N
//            getEntityForDetail.setBodyText("return model;"); //NOI18N
//            javaClass.getFeatures().add(getEntityForDetail);
//            
//            JMIGenerationUtil.createImport(javaClass, "java.util.Collection");
//            JMIGenerationUtil.createImport(javaClass, "java.util.ArrayList"); //NOI18N
//            Method setEntityForDetail = JMIGenerationUtil.createMethod(javaClass, "setDetail" + simpleEntityName + "s", Modifier.PUBLIC, "void");  //NOI18N
//            setEntityForDetail.setBodyText("model = new ListDataModel(new ArrayList(m));"); //NOI18N
//            Parameter entityForDetailParameter = JMIGenerationUtil.createParameter(javaClass, "m", "Collection<" + entityReferenceName + ">"); //NOI18N
//            setEntityForDetail.getParameters().add(entityForDetailParameter);
//            javaClass.getFeatures().add(setEntityForDetail);
//            
//            // <editor-fold desc=" all relations ">
//            for(Iterator it = allRelMethods.iterator(); it.hasNext();) {
//                Method m = (Method) it.next();
//                int multiplicity = JsfForm.isRelationship(m, isFieldAccess);
//                Method otherSide = JsfForm.getOtherSideOfRelation(m, isFieldAccess);
//                if (otherSide != null) {
//                    JavaClass relClass = (JavaClass) otherSide.getDeclaringClass();
//                    boolean isRelFieldAccess = JsfForm.isFieldAccess(relClass);
//                    int otherSideMultiplicity = JsfForm.isRelationship(otherSide, isRelFieldAccess);
//                    Type t = m.getType();
//                    boolean isCollection = false;
//                    if (t instanceof ParameterizedType) {
//                        MultipartId id = (MultipartId) m.getTypeName();
//                        for (Iterator iter = id.getTypeArguments().iterator(); iter.hasNext();) {
//                            MultipartId param = (MultipartId) iter.next();
//                            NamedElement parType = param.getElement();
//                            if (param instanceof JavaClass) {
//                                t = (JavaClass) param;
//                                isCollection = true;
//                            }
//                        }
//                    }
//                    String relTypeReference = JMIGenerationUtil.createImport(javaClass, t.getName()).getName();
//                    String relType = t.getName();
//                    String simpleRelType = simpleClassName(relType);
//                    String relFieldName = getPropNameFromMethod(m.getName());
//                    
//                    updateRelatedInCreate.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            (isCollection ? "for(" + relTypeReference + " " + relFieldName + " : " + fieldName + "." + m.getName() + "()){\n" :
//                                relTypeReference + " " + relFieldName + "=" + fieldName + "." + m.getName() +"();\n" +
//                                "if (" + relFieldName + " != null) {\n") +
//                            relFieldName + " = em.merge(" + relFieldName +");\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + ".s" + otherSide.getName().substring(1) + "(" + fieldName+ ");\n" :
//                                relFieldName + "." + otherSide.getName() + "().add(" + fieldName +");\n") +
//                            relFieldName + "=em.merge(" + relFieldName +");\n}\n\n");
//                    
//                    if (isCollection) {
//                        updateRelatedInEditPre.append("\n Collection<" + relTypeReference + "> " + relFieldName + "sOld = em.find("
//                            + entityReferenceName +".class, " + fieldName + "." + idGetter + "())." + m.getName() + "();\n");
//                        updateRelatedInEditPost.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            "Collection <" + relTypeReference + "> " + relFieldName + "sNew = " + fieldName + "." + m.getName() + "();\n" +
//                            "for(" + relTypeReference + " " + relFieldName + "New : " + relFieldName + "sNew) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "New.s" + otherSide.getName().substring(1) + "(" + fieldName+ ");\n" :
//                                relFieldName + "New." + otherSide.getName() + "().add(" + fieldName +");\n") +
//                            relFieldName + "New=em.merge(" + relFieldName +"New);\n}\n" +
//                            "for(" + relTypeReference + " " + relFieldName + "Old : " + relFieldName + "sOld) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "Old.s" + otherSide.getName().substring(1) + "(null);\n" :
//                                relFieldName + "Old." + otherSide.getName() + "().remove(" + fieldName +");\n") +
//                            relFieldName + "Old=em.merge(" + relFieldName +"Old);\n}\n");
//                    } else {
//                        updateRelatedInEditPre.append("\n" + relTypeReference + " " + relFieldName + "Old = em.find("
//                            + entityReferenceName +".class, " + fieldName + "." + idGetter + "())." + m.getName() + "();\n");
//                        updateRelatedInEditPost.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            relTypeReference + " " + relFieldName + "New = " + fieldName + "." + m.getName() +"();\n" +
//                            "if(" + relFieldName + "New != null) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "New.s" + otherSide.getName().substring(1) + "(" + fieldName+ ");\n" :
//                                relFieldName + "New." + otherSide.getName() + "().add(" + fieldName +");\n") +
//                            relFieldName + "New=em.merge(" + relFieldName +"New);\n}\n" +
//                            "if(" + relFieldName + "Old != null) {\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + "Old.s" + otherSide.getName().substring(1) + "(null);\n" :
//                                relFieldName + "Old." + otherSide.getName() + "().remove(" + fieldName +");\n") +
//                            relFieldName + "Old=em.merge(" + relFieldName +"Old);\n}\n");
//                    } 
//                    
//                    updateRelatedInDestroy.append("\n//update property " + relFieldName + " of entity " + simpleRelType + "\n" +
//                            (isCollection ? "Collection<" + relTypeReference + "> " + relFieldName + "s" : relTypeReference + " " + relFieldName) + " = " + fieldName + "." + m.getName() +"();\n" +
//                            (isCollection ? "for(" + relTypeReference + " " + relFieldName + " : " + relFieldName + "s" : "if (" + relFieldName + " != null") + ") {\n" +
//                            relFieldName + " = em.merge(" + relFieldName +");\n" +
//                            ((otherSideMultiplicity == JsfForm.REL_TO_ONE) ? relFieldName + ".s" + otherSide.getName().substring(1) + "(null);\n" :
//                                relFieldName + "." + otherSide.getName() + "().remove(" + fieldName +");\n") +
//                            relFieldName + "=em.merge(" + relFieldName +");\n}\n\n");
//                    
//                    Method destroyFromDetail = JMIGenerationUtil.createMethod(javaClass, "destroyFrom" + simpleRelType, Modifier.PUBLIC, "String"); //NOI18N
//                    String relIdGetter = JsfForm.getIdGetter(isRelFieldAccess, relClass).getName();
//                    destroyFromDetail.setBodyText(simpleRelType + " param = get" + simpleRelType + "Controller().get" + simpleRelType + "();\n"
//                            + "destroy();\n"
//                            + "EntityManager em = getEntityManager();\n try {\n"
//                            + "get" + simpleRelType + "Controller().set" + simpleRelType 
//                            + "(em.find(" + relTypeReference + ".class, param." + relIdGetter + "()));\n"
//                            + "} finally {\n em.close();\n}\n"
//                            + "return \"" + getManagedBeanName(simpleRelType) + "_detail\";\n");
//                    javaClass.getFeatures().add(destroyFromDetail);
//
//                    Method controllerAccess = JMIGenerationUtil.createMethod(javaClass, "get" + simpleRelType + "Controller", Modifier.PRIVATE, simpleRelType + "Controller"); //NOI18N
//                    if (isInjection) {
//                        controllerAccess.setBodyText("FacesContext context = FacesContext.getCurrentInstance();\n"
//                                + "return (" + simpleRelType +"Controller) context.getApplication().getELResolver().getValue(\n context.getELContext(), null, \"" 
//                                + getManagedBeanName(simpleRelType) +"\");\n");
//                    } else {
//                        controllerAccess.setBodyText("FacesContext context = FacesContext.getCurrentInstance();\n"
//                                + "return (" + simpleRelType +"Controller) context.getApplication().getVariableResolver().resolveVariable(\n context, \"" 
//                                + getManagedBeanName(simpleRelType) +"\");\n");
//                    }
//                    javaClass.getFeatures().add(controllerAccess);
//                    
//                    if (multiplicity == JsfForm.REL_TO_MANY) {
//                        setEntity.setBodyText(setEntity.getBodyText() + "\n"
//                                + controllerAccess.getName() + "().setDetail" + simpleRelType 
//                                + "s(" + fieldName + "." + m.getName() + "());");
//                    }
//                    
//                    if (multiplicity == JsfForm.REL_TO_MANY && otherSideMultiplicity == JsfForm.REL_TO_MANY) {
//                        //methods needed to add items into N:M relationship
//                        JMIGenerationUtil.createImport(javaClass, "javax.faces.model.SelectItem");
//                        JMIGenerationUtil.createImport(javaClass, "java.util.List");
//                        Method getRelatedAvailable = JMIGenerationUtil.createMethodArray(javaClass, m.getName() + "Available", Modifier.PUBLIC, "javax.faces.model.SelectItem");
//                        getRelatedAvailable.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                                + "Query q = em.createQuery(\"select o from " + simpleRelType + " as o where "
//                                + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? ":param not member of o." + getPropNameFromMethod(otherSide.getName()) + "\");\n" : 
//                                    "o." + getPropNameFromMethod(otherSide.getName()) + " <> :param or o." + getPropNameFromMethod(otherSide.getName()) + " IS NULL\");\n")
//                                + "q.setParameter(\"param\", " + fieldName + ");\n"
//                                + "List <" + simpleRelType + "> l = (List <" + simpleRelType + ">) q.getResultList();\n"
//                                + "SelectItem select[] = new SelectItem[l.size()];\n"
//                                + "int i = 0;\n"
//                                + "for(" + simpleRelType + " x : l) {\n"
//                                + "select[i++] = new SelectItem(x);\n"
//                                + "}\n return select;\n"
//                                + "} finally {\n em.close();\n}\n");
//                        javaClass.getFeatures().add(getRelatedAvailable);
//
//                        Field relatedToAdd = JMIGenerationUtil.createFieldArray(javaClass, getPropNameFromMethod(m.getName()), Modifier.PUBLIC, relTypeReference);
//                        javaClass.getFeatures().add(relatedToAdd);
//
//                        Method getRelatedToAdd = JMIGenerationUtil.createMethodArray(javaClass, m.getName() + "ToAdd", Modifier.PUBLIC, relTypeReference);
//                        getRelatedToAdd.setBodyText("return " + relatedToAdd.getName() + ";\n");
//                        javaClass.getFeatures().add(getRelatedToAdd);
//
//                        Method setRelatedToAdd = JMIGenerationUtil.createMethod(javaClass, "s" + m.getName().substring(1) + "ToAdd", Modifier.PUBLIC, "void");
//                        Parameter setRelatedToAddParam = JMIGenerationUtil.createParameterArray(javaClass, relatedToAdd.getName(), relTypeReference);
//                        setRelatedToAdd.getParameters().add(setRelatedToAddParam);
//                        setRelatedToAdd.setBodyText("this." + relatedToAdd.getName() + " = " + relatedToAdd.getName() + ";\n");
//                        javaClass.getFeatures().add(setRelatedToAdd);
//
//                        Method addRelated = JMIGenerationUtil.createMethod(javaClass, "add" + m.getName().substring(3), Modifier.PUBLIC, "String");
//                        addRelated.setBodyText("EntityManager em = getEntityManager();\n"
//                                + "try {\n em.getTransaction().begin();\n"
//                                + "for(" + simpleRelType + " entity : " + relatedToAdd.getName() + ") {\n"
//                                + "entity." + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? otherSide.getName() + "().add(" + fieldName + ");\n" : "s" + otherSide.getName().substring(1) + "(" + fieldName + ");\n")
//                                + "entity = em.merge(entity);\n"
//                                + fieldName + "." + m.getName() + "().add(entity);\n"
//                                + "}\n"
//                                + fieldName + " = em.merge(" + fieldName + ");\n"
//                                + "em.getTransaction().commit();\n"
//                                + setEntity.getName() + "(" + fieldName + ");\n"
//                                + "addSuccessMessage(\"" + simpleRelType + " successfully added.\");\n"
//                                + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n"
//                                + "em.getTransaction().rollback();\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n"
//                                + "}\n } finally {\n em.close();\n }\n"
//                                + "return \"" + fieldName + "_detail\";\n");
//                        javaClass.getFeatures().add(addRelated);
//                        
//                        Method removeRelated = JMIGenerationUtil.createMethod(javaClass, "remove" + m.getName().substring(3), Modifier.PUBLIC, "String");
//                        removeRelated.setBodyText("EntityManager em = getEntityManager();\n"
//                                + "try {\n"
//                                + "em.getTransaction().begin();\n"
//                                + simpleRelType + " entity = (" + simpleRelType +") " + controllerAccess.getName() + "().getDetail" + simpleRelType + "s().getRowData();\n"
//                                + "entity." + (otherSideMultiplicity == JsfForm.REL_TO_MANY ? otherSide.getName() + "().remove(" + fieldName + ");\n" : "s" + otherSide.getName().substring(1) + "(null);\n")
//                                + "entity = em.merge(entity);\n"
//                                + fieldName + "." + m.getName() + "().remove(entity);\n"
//                                + fieldName + " = em.merge(" + fieldName + ");\n"
//                                + "em.getTransaction().commit();\n"
//                                + setEntity.getName() + "(" + fieldName + ");\n"
//                                + "addSuccessMessage(\"" + simpleEntityName + " successfully removed.\");\n"
//                                + "} catch (Exception ex) {\n"
//                                + "try {\n"
//                                + "addErrorMessage(ex.getLocalizedMessage());\n"
//                                + "em.getTransaction().rollback();\n"
//                                + "} catch (Exception e) {\n"
//                                + "addErrorMessage(e.getLocalizedMessage());\n"
//                                + "}\n } finally {\n em.close();\n }\n"
//                                + "return \"" + fieldName + "_detail\";\n");
//                        javaClass.getFeatures().add(removeRelated);
//                    }
//                    
//                    Method createFromDetailSetup = JMIGenerationUtil.createMethod(javaClass, "createFrom" + simpleRelType +"Setup", Modifier.PUBLIC, "String"); //NOI18N
//                    createFromDetailSetup.setBodyText("this." + fieldName + " = new " + entityReferenceName + "();\n"
//                            + "EntityManager em = getEntityManager();\n try{\n"
//                            + (isCollection ? "if (" + fieldName + "." + m.getName() + "() == null) {\n" + fieldName + ".s" + m.getName().substring(1) + "(new ArrayList());\n}\n" : "")
//                            + fieldName + (isCollection ? "." + m.getName() + "().add" : ".s" + m.getName().substring(1)) + "(em.find(" + relTypeReference + ".class, get" + simpleRelType + "Controller().get" + simpleRelType + "()." + relIdGetter + "()));\n"
//                            + "} finally {\n em.close();\n}\n"
//                            + "return \"" + getManagedBeanName(simpleEntityName) + "_create\";\n");
//                    javaClass.getFeatures().add(createFromDetailSetup);
//                    
//                    Method createFromDetail = JMIGenerationUtil.createMethod(javaClass, "createFrom" + simpleRelType, Modifier.PUBLIC, "String"); //NOI18N
//                    createFromDetail.setBodyText("create();\n" +
//                            "get" + simpleRelType + "Controller().set" + simpleRelType + "(" + fieldName + "." + m.getName() + "()"
//                            + (isCollection ? ".iterator().next()" : "") + ");\n" +
//                            "return \"" + getManagedBeanName(simpleRelType) + "_detail\";\n");
//                    javaClass.getFeatures().add(createFromDetail);
//                } else {
//                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot detect other side of a relationship.");
//                }
//            }
//            // </editor-fold>
//            
//            Method createSetup = JMIGenerationUtil.createMethod(javaClass, "createSetup", Modifier.PUBLIC, "String");  //NOI18N
//            createSetup.setBodyText("this." + fieldName + " = new " + entityReferenceName + "();\n return \"" + fieldName + "_create\";"); //NOI18N
//            javaClass.getFeatures().add(createSetup);
//            
//            String BEGIN = isInjection ? "utx.begin();" : "em.getTransaction().begin();";
//            String COMMIT = isInjection ? "utx.commit();" : "em.getTransaction().commit();";
//            String ROLLBACK = isInjection ? "utx.rollback();" : "em.getTransaction().rollback();";
//            
//            Method create = JMIGenerationUtil.createMethod(javaClass, "create", Modifier.PUBLIC, "String");  //NOI18N
//            create.setBodyText("EntityManager em = getEntityManager();\n"
//                    + "try {\n " + BEGIN + "\n em.persist(" + fieldName + ");\n" + updateRelatedInCreate.toString() + COMMIT + "\n"  //NOI18N
//                    + "addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n" //NOI18N
//                    + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + ROLLBACK + "\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n}\n } "  //NOI18N
//                    + "finally {\n em.close();\n }\n"
//                    + "return \"" + fieldName + "_list\";");
//            javaClass.getFeatures().add(create);
//          
//            String setFromReqParamMethod = "set" + simpleEntityName + "FromRequestParam";
//            String getFromReqParamMethod = "get" + simpleEntityName + "FromRequestParam";
//            
//            Method showSetup = JMIGenerationUtil.createMethod(javaClass, "detailSetup", Modifier.PUBLIC, "String");  //NOI18N
//            showSetup.setBodyText(setFromReqParamMethod + "();\n return \"" + fieldName + "_detail\";"); //NOI18N
//            javaClass.getFeatures().add(showSetup);
//            
//            Method editSetup = JMIGenerationUtil.createMethod(javaClass, "editSetup", Modifier.PUBLIC, "String");  //NOI18N
//            editSetup.setBodyText(setFromReqParamMethod + "();\n return \"" + fieldName + "_edit\";"); //NOI18N
//            javaClass.getFeatures().add(editSetup);
//            
//            Method edit = JMIGenerationUtil.createMethod(javaClass, "edit", Modifier.PUBLIC, "String");  //NOI18N
//            edit.setBodyText("EntityManager em = getEntityManager();\n"
//                    + "try {\n " + BEGIN + "\n" + updateRelatedInEditPre.toString() 
//                    + fieldName + " = em.merge(" + fieldName + ");\n "
//                    + updateRelatedInEditPost.toString() + COMMIT + "\n"  //NOI18N
//                    + "addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n"  //NOI18N
//                    + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + ROLLBACK + "\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n}\n} "  //NOI18N
//                    + "finally {\n em.close();\n }\n" //NOI18N
//                    + "return \"" + fieldName + "_list\";"); //NOI18N
//            javaClass.getFeatures().add(edit);
//
//            
//            Method destroy = JMIGenerationUtil.createMethod(javaClass, "destroy", Modifier.PUBLIC, "String");  //NOI18N
//            destroy.setBodyText("EntityManager em = getEntityManager();\n"
//                    + "try {\n " + BEGIN + "\n" + entityReferenceName + " " + fieldName + " = " + getFromReqParamMethod + "();\n"
//                    + fieldName + " = em.merge(" + fieldName + ");\n" + updateRelatedInDestroy.toString() 
//                    + "em.remove(" + fieldName + ");\n " + COMMIT + "\n"  //NOI18N
//                    + "addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n"  //NOI18N
//                    + "} catch (Exception ex) {\n try {\n addErrorMessage(ex.getLocalizedMessage());\n" + ROLLBACK + "\n } catch (Exception e) {\n addErrorMessage(e.getLocalizedMessage());\n}\n} "  //NOI18N
//                    + "finally {\n em.close();\n }\n" //NOI18N
//                    + "return \"" + fieldName + "_list\";"); //NOI18N
//            javaClass.getFeatures().add(destroy);
//            
//            String idField = createIdFieldDeclaration(idPropertyType, "param");
//            
//            JMIGenerationUtil.createImport(javaClass, "javax.faces.context.FacesContext");
//            JMIGenerationUtil.createImport(javaClass, "javax.faces.application.FacesMessage");
//            Method getFromReq = JMIGenerationUtil.createMethod(javaClass, getFromReqParamMethod, Modifier.PUBLIC, entityClass);  //NOI18N
//            JavaClass idClass = null;
//            Type idType = JMIUtils.resolveType(idPropertyType);
//            if (idType instanceof JavaClass) {
//                idClass = (JavaClass) idType;
//            }
//            boolean embeddable = idClass != null && JsfForm.isEmbeddableClass(idClass);
//            getFromReq.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + entityReferenceName + " o = (" + entityReferenceName +") model.getRowData();\n"
//                    + "o = em.merge(o);\n"
//                    + "return o;\n"
//                    + "} finally {\n em.close();\n}\n");
//            javaClass.getFeatures().add(getFromReq);
//            
//            Method setFromReq = JMIGenerationUtil.createMethod(javaClass, setFromReqParamMethod, Modifier.PUBLIC, "void");  //NOI18N
//            setFromReq.setBodyText(entityReferenceName + " " + fieldName + " = " + getFromReqParamMethod + "();\n" //NOI18N
//                + "set" + simpleEntityName + "(" + fieldName + ");"); //NOI18N
//            javaClass.getFeatures().add(setFromReq);
//
//            Method getEntities = JMIGenerationUtil.createMethod(javaClass, "get" + simpleEntityName + "s", Modifier.PUBLIC, dmReference.getName());  //NOI18N
//            JMIGenerationUtil.createImport(javaClass, "javax.persistence.Query");
//            getEntities.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + "Query q = em.createQuery(\"select object(o) from " + simpleEntityName +" as o\");\n"
//                    + "q.setMaxResults(batchSize);\n"
//                    + "q.setFirstResult(firstItem);\n"
//                    + "model = new " + ldmReference.getName() + "(q.getResultList());\n"
//                    + "return model;\n" //NOI18N
//                    + "} finally {\n em.close();\n}\n");
//            javaClass.getFeatures().add(getEntities);
//            
//            Method addErrorMessage = JMIGenerationUtil.createMethod(javaClass, "addErrorMessage", Modifier.PUBLIC + Modifier.STATIC, "void");  //NOI18N
//            addErrorMessage.setBodyText("FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);\n" //NOI18N
//                + "FacesContext fc = FacesContext.getCurrentInstance();\n" //NOI18N
//                + "fc.addMessage(null, facesMsg);"); //NOI18N
//            Parameter msgParameter = JMIGenerationUtil.createParameter(javaClass, "msg", "String");
//            addErrorMessage.getParameters().add(msgParameter);
//            javaClass.getFeatures().add(addErrorMessage);
//            
//            Method addSuccessMessage = JMIGenerationUtil.createMethod(javaClass, "addSuccessMessage", Modifier.PUBLIC + Modifier.STATIC, "void");  //NOI18N
//            addSuccessMessage.setBodyText("FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);\n" //NOI18N
//                + "FacesContext fc = FacesContext.getCurrentInstance();\n" //NOI18N
//                + "fc.addMessage(\"successInfo\", facesMsg);"); //NOI18N
//            Parameter successMsgParameter = JMIGenerationUtil.createParameter(javaClass, "msg", "String");
//            addSuccessMessage.getParameters().add(successMsgParameter);
//            javaClass.getFeatures().add(addSuccessMessage);
//            
//            //getter for converter
//            Method findById = JMIGenerationUtil.createMethod(javaClass, "find" + simpleEntityName, Modifier.PUBLIC, entityClass);  //NOI18N
//            findById.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + entityReferenceName + " o = (" + entityReferenceName + ") em.find(" + entityReferenceName + ".class, id);\n"
//                    + "return o;\n"
//                    + "} finally {\n em.close();\n}\n");
//            Parameter idParameter = JMIGenerationUtil.createParameter(javaClass, "id", idPropertyType);
//            findById.getParameters().add(idParameter);
//            javaClass.getFeatures().add(findById);
//            
//            // <editor-fold desc=" toOne relations ">
//            for(Iterator it = toOneRelMethods.iterator(); it.hasNext();) {
//                Method m = (Method) it.next();
//                String relType = m.getType().getName();
//                String simpleRelType = simpleClassName(relType);
//                
//                String methodName = m.getName() + "s";
//                //make sure we do not generate >1 getter for each type
//                boolean alredyGenerated = false;
//                for (Iterator it2 = javaClass.getFeatures().iterator(); it2.hasNext();) {
//                    Feature f = (Feature) it2.next();
//                    if (methodName.equals(f.getName())) {
//                        alredyGenerated = true;
//                        break;
//                    }
//                }
//                
//                if (!alredyGenerated) {
//                    Method selectItems = JMIGenerationUtil.createMethodArray(javaClass, methodName, Modifier.PUBLIC, "javax.faces.model.SelectItem"); //NOI18N
//                    JMIGenerationUtil.createImport(javaClass, "javax.faces.model.SelectItem"); //NOI18N
//                    JMIGenerationUtil.createImport(javaClass, "java.util.List"); //NOI18N
//                    String relTypeReference = JMIGenerationUtil.createImport(javaClass, relType).getName();
//                    selectItems.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                            + "List <" + relTypeReference + "> l = (List <" + relTypeReference +">) em.createQuery(\"select o from " + simpleRelType + " as o\").getResultList();\n"
//                            + "SelectItem select[] = new SelectItem[l.size()];\n"
//                            + "int i = 0;\n for(" + relTypeReference + " x : l) {\n"
//                            + "select[i++] = new SelectItem(x);\n}\nreturn select;\n"
//                            + "} finally {\n em.close();\n}\n");
//                    javaClass.getFeatures().add(selectItems);
//                }
//            }
//            // </editor-fold>
//
//            Method getItemCount = JMIGenerationUtil.createMethod(javaClass, "getItemCount", Modifier.PUBLIC, "int");
//            getItemCount.setBodyText("EntityManager em = getEntityManager();\n try{\n"
//                    + "int count = ((Long) em.createQuery(\"select count(o) from " + simpleEntityName + " as o\").getSingleResult()).intValue();\n"
//                    + "return count;\n"
//                    + "} finally {\n em.close();\n}\n");
//            javaClass.getFeatures().add(getItemCount);
//            
//            Method getFirstItem = JMIGenerationUtil.createMethod(javaClass, "getFirstItem", Modifier.PUBLIC, "int");
//            getFirstItem.setBodyText("return firstItem;");
//            javaClass.getFeatures().add(getFirstItem);
//            
//            Method getLastItem = JMIGenerationUtil.createMethod(javaClass, "getLastItem", Modifier.PUBLIC, "int");
//            getLastItem.setBodyText("int size = getItemCount();\n return firstItem + batchSize > size ? size : firstItem + batchSize;\n");
//            javaClass.getFeatures().add(getLastItem);
//
//            Method getBatchSize = JMIGenerationUtil.createMethod(javaClass, "getBatchSize", Modifier.PUBLIC, "int");
//            getBatchSize.setBodyText("return batchSize;");
//            javaClass.getFeatures().add(getBatchSize);
//            
//            Method next = JMIGenerationUtil.createMethod(javaClass, "next", Modifier.PUBLIC, "String");
//            next.setBodyText("if (firstItem + batchSize < getItemCount()) {\n"
//                    + "firstItem += batchSize;\n}\n"
//                    + "return \"" + fieldName + "_list\";\n");
//            javaClass.getFeatures().add(next);
//            
//            Method prev = JMIGenerationUtil.createMethod(javaClass, "prev", Modifier.PUBLIC, "String");
//            prev.setBodyText("firstItem -= batchSize;\n if (firstItem < 0) {\nfirstItem = 0;\n}\n"
//                    + "return \"" + fieldName + "_list\";\n");
//            javaClass.getFeatures().add(prev);
//            
//            rollback = false;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            JavaModel.getJavaRepository().endTrans(rollback);
//        }
//        return javaClass;
    }

    private static HashSet<String> CONVERTED_TYPES = new HashSet<String>();
    static {
        CONVERTED_TYPES.add("Boolean");
        CONVERTED_TYPES.add("Byte");
        CONVERTED_TYPES.add("Double");
        CONVERTED_TYPES.add("Float");
        CONVERTED_TYPES.add("Integer");
        CONVERTED_TYPES.add("Long");
        CONVERTED_TYPES.add("Short");
        CONVERTED_TYPES.add("StringBuffer");
    }
    private static HashMap<String,String> PRIMITIVE_TYPES = new HashMap<String, String>();
    static {
        PRIMITIVE_TYPES.put("boolean", "Boolean");
        PRIMITIVE_TYPES.put("byte", "Byte");
        PRIMITIVE_TYPES.put("double", "Double");
        PRIMITIVE_TYPES.put("float", "Float");
        PRIMITIVE_TYPES.put("int", "Integer");
        PRIMITIVE_TYPES.put("long", "Long");
        PRIMITIVE_TYPES.put("short", "Short");
    }
    
    /** @param valueVar is name of a String variable */
    private static String createIdFieldDeclaration(String idPropertyType, String valueVar) {
    	String idField;
        if (idPropertyType.startsWith("java.lang.")) {
            String shortName = idPropertyType.substring(10);
            idField = shortName + " id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        } else if (idPropertyType.equals("java.math.BigInteger") || "BigInteger".equals(idPropertyType)) {
            idField = "java.math.BigInteger id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        } else if (idPropertyType.equals("java.math.BigDecimal") || "BigDecimal".equals(idPropertyType)) {
            idField = "java.math.BigDecimal id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        } else {
            idField = idPropertyType + " id = " + createIdFieldInitialization(idPropertyType, valueVar) + ";";
        }
        return idField;
    }
    
    /** @param valueVar is name of a String variable */
    private static String createIdFieldInitialization(String idPropertyType, String valueVar) {
    	String idField;
        //PENDING cannot assume that key type is Integer, Long, String, int or long
    	if ("char".equals(idPropertyType)) {
            idField = valueVar + ".charAt(0);";
        } else if (PRIMITIVE_TYPES.containsKey(idPropertyType)) {
            String objectType = PRIMITIVE_TYPES.get(idPropertyType);
            String methodName = "parse" + idPropertyType.substring(0,1).toUpperCase() + idPropertyType.substring(1);
            idField = objectType + "." + methodName + "(" + valueVar + ")";
        } else if (idPropertyType.equals("java.math.BigInteger") || "BigInteger".equals(idPropertyType)) {
            idField = "new java.math.BigInteger(" + valueVar + ")";
        } else if (idPropertyType.equals("java.math.BigDecimal") || "BigDecimal".equals(idPropertyType)) {
            idField = "new java.math.BigDecimal(" + valueVar + ")";
        } else if (idPropertyType.equals("java.lang.String") || "String".equals(idPropertyType)) {
            idField = valueVar;
        } else if (idPropertyType.startsWith("java.lang.")) {
            String shortName = idPropertyType.substring(10);
            idField = "new " + shortName + "(" + valueVar + ")";
        } else if (CONVERTED_TYPES.contains(idPropertyType)) {
            idField = "new " + idPropertyType + "(" + valueVar + ")";
        } else {
            idField = "(" + idPropertyType + ") javax.faces.context.FacesContext.getCurrentInstance().getApplication().\n"
                    + "createConverter(" + idPropertyType + ".class).getAsObject(FacesContext.\n"
                    + "getCurrentInstance(), null, " + valueVar + ")";
        }
        return idField;
    }
    
    public static String simpleClassName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot > 0 ? fqn.substring(lastDot + 1) : fqn;
    }

    public static String fieldFromClassName(String className) {
        boolean makeFirstLower = className.length() == 1 || (!Character.isUpperCase(className.charAt(1)));
        String candidate = makeFirstLower ? className.substring(0,1).toLowerCase() + className.substring(1) : className;
        if (!Utilities.isJavaIdentifier(candidate)) {
            candidate += "1"; //NOI18N
        }
        return candidate;
    }
    
    public static String getManagedBeanName(String simpleEntityName) {
        int len = simpleEntityName.length();
        return len > 1 ? simpleEntityName.substring(0,1).toLowerCase() + simpleEntityName.substring(1) : simpleEntityName.toLowerCase();
    }
    
    public static String getPropNameFromMethod(String name) {
        //getABcd should be converted to ABcd, getFooBar should become fooBar
        //getA1 is "a1", getA_ is a_, getAB is AB
        boolean makeFirstLower = name.length() < 5 || (!Character.isUpperCase(name.charAt(4)));
        return makeFirstLower ? name.substring(3,4).toLowerCase() + name.substring(4) : name.substring(3);
    }

    private static void addImplementsClause(FileObject fileObject, final String className, final String interfaceName) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] modified = new boolean[] { false };
        ModificationResult modificationResult = javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                TypeMirror interfaceType = workingCopy.getElements().getTypeElement(interfaceName).asType();
                if (!workingCopy.getTypes().isSubtype(typeElement.asType(), interfaceType)) {
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    GenerationUtils.newInstance(workingCopy).addImplementsClause(classTree, interfaceName);
                    modified[0] = true;
                }
            }
        });
        if (modified[0]) {
            modificationResult.commit();
        }
    }

    private static MethodTree createMethod(WorkingCopy workingCopy, Modifier[] modifiers, String returnType, String name, 
            String[] params, String[] exceptions, String body) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Number of params can't be odd");
        }
        List<MethodModel.Variable> paramsList = new ArrayList<MethodModel.Variable>();
        for (int i = 0; i < params.length; i++) {
            paramsList.add(MethodModel.Variable.create(params[i], params[i + 1]));
            i++;
        }

        MethodModel methodModel = MethodModel.create(
                name,
                returnType,
                body,
                paramsList,
                Arrays.asList(exceptions),
                new HashSet<Modifier>(Arrays.asList(modifiers))
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
    
    static class TypeInfo {
        
        private String rawType;
        private TypeInfo[] declaredTypeParameters;
        
        public String getRawType() {
            return rawType;
        }

        public TypeInfo[] getDeclaredTypeParameters() {
            return declaredTypeParameters;
        }
       
        public TypeInfo(String rawType) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
        }
        
        public TypeInfo(String rawType, TypeInfo[] declaredTypeParameters) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                return;
            }
            this.declaredTypeParameters = declaredTypeParameters;
        }
        
        public TypeInfo(String rawType, String[] declaredTypeParamStrings) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
            if (declaredTypeParamStrings == null || declaredTypeParamStrings.length == 0) {
                return;
            }
            this.declaredTypeParameters = TypeInfo.fromStrings(declaredTypeParamStrings);
        }
        
        public static TypeInfo[] fromStrings(String[] strings) {
            if (strings == null || strings.length == 0) {
                return null;
            }
            TypeInfo[] typeInfos = new TypeInfo[strings.length];
            for (int i = 0; i < strings.length; i++) {
                typeInfos[i] = new TypeInfo(strings[i]);
            }
            return typeInfos;
        }
    }
    
    static class MethodInfo {
        
        private String         name;
        private int            modifiers;
        private TypeInfo          returnType;
        private TypeInfo[]        exceptionTypes;
        private TypeInfo[]        parameterTypes;
        private String[]       parameterNames;
        private String         methodBodyText;
        private AnnotationInfo[] annotations;
        private String         commentText;
        
        /**
         * Constructs a MethodInfo with the specified name, modifiers,
         * returnType, parameterTypes, parameterNames, methodBody, and commentText.
         *
         * @param name The method name for this MethodInfo
         * @param modifiers The method {@link Modifier} bits
         * @param returnType The return type for this MethodInfo
         * @param exceptionsThrown The exceptions the method throws
         * @param parameterTypes The parameter types for this MethodInfo
         * @param parameterNames The parameter names for this MethodInfo
         * @param methodBodyText The Java source code for the body of this MethodInfo
         * @param annotations The annotation for this MethodInfo
         * @param commentText The comment text for this MethodInfo
         */
        public MethodInfo(String name, int modifiers, TypeInfo returnType, TypeInfo[] exceptionTypes,
                TypeInfo[] parameterTypes, String[] parameterNames, String methodBodyText,  AnnotationInfo[] annotations,
                String commentText) {
            
            this.name = name;
            this.modifiers = modifiers;
            this.returnType = returnType;
            this.exceptionTypes = exceptionTypes;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.methodBodyText = methodBodyText;
            this.annotations = annotations;
            this.commentText = commentText;
        }
        
        public MethodInfo(String name, int modifiers, String returnType, String[] exceptionTypes,
                String[] parameterTypes, String[] parameterNames, String methodBodyText,  AnnotationInfo[] annotations,
                String commentText) {
            
            this.name = name;
            this.modifiers = modifiers;
            this.returnType = new TypeInfo(returnType);
            this.exceptionTypes = TypeInfo.fromStrings(exceptionTypes);
            this.parameterTypes = TypeInfo.fromStrings(parameterTypes);
            this.parameterNames = parameterNames;
            this.methodBodyText = methodBodyText;
            this.annotations = annotations;
            this.commentText = commentText;
        }
        
        public String getName() {
            return name;
        }
        
        public int getModifiers() {
            return modifiers;
        }
        
        public TypeInfo getReturnType() {
            return returnType;
        }
        
        public TypeInfo[] getExceptionTypes() {
            return exceptionTypes;
        }
        
        public String getMethodBodyText() {
            return methodBodyText;
        }
        
        public TypeInfo[] getParameterTypes() {
            return parameterTypes;
        }
        
        public String[] getParameterNames() {
            return parameterNames;
        }
        
        public  AnnotationInfo[] getAnnotations() {
            return annotations;
        }
        
        public String getCommentText() {
            return commentText;
        }
    }
    
    static class AnnotationInfo {
        private String type;
        private String[] argNames;
        private Object[] argValues;
        
        public AnnotationInfo(String type) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
        }
        
        public AnnotationInfo(String type, String[] argNames, Object[] argValues) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
            if (argNames == null) {
                if (argValues != null) {
                    throw new IllegalArgumentException();
                }
            } else if (argValues == null || argValues.length != argNames.length) {
                throw new IllegalArgumentException();
            }
            this.argNames = argNames;
            this.argValues = argValues;
        }
        
        public String getType() {
            return type;
        }
        
        public String[] getArgNames() {
            return argNames;
        }
        
        public Object[] getArgValues() {
            return argValues;
        }
    }
    
    static class TreeMakerUtils {
        
        public static ClassTree addVariable(ClassTree classTree, WorkingCopy wc, String name, TypeInfo type, int modifiers, Object initializer, AnnotationInfo[] annotations) {
            Tree typeTree = createType(wc, type);
            ModifiersTree modTree = createModifiers(wc, modifiers, annotations);
            TreeMaker make = wc.getTreeMaker();
            VariableTree tree = make.Variable(modTree, name, typeTree, make.Literal(initializer));
            return make.addClassMember(classTree, tree);
        }
        
        public static ClassTree addVariable(ClassTree classTree, WorkingCopy wc, String name, String type, int modifiers, Object initializer, AnnotationInfo[] annotations) {
            return addVariable(classTree, wc, name, new TypeInfo(type), modifiers, initializer, annotations);
        }
        
    /*
     * Creates a new variable tree for a given name and type
     */
        private static VariableTree createVariable(WorkingCopy wc, String name, TypeInfo type) {
            return createVariable(wc, name, createType(wc, type));
        }
        
    /*
     * Creates a new variable tree for a given name and type
     */
        private static VariableTree createVariable(WorkingCopy wc, String name, Tree type) {
            TreeMaker make = wc.getTreeMaker();
            return make.Variable(createModifiers(wc), name, type, null);
        }
        
        public static ClassTree addMethod(ClassTree classTree, WorkingCopy wc, MethodInfo mInfo) {
            MethodTree tree = createMethod(wc, mInfo);
            return wc.getTreeMaker().addClassMember(classTree, tree);
        }
        
    /*
     * Creates a method given context method and return type name
     */
        private static MethodTree createMethod(WorkingCopy wc, MethodInfo mInfo) {
            TreeMaker make = wc.getTreeMaker();
            TypeInfo[] pTypes = mInfo.getParameterTypes();
            String[] pNames = mInfo.getParameterNames();
            List<VariableTree> params = new ArrayList<VariableTree>();
            for (int i = 0 ; pTypes != null && i < pTypes.length; i++) {
                VariableTree vtree = createVariable(wc, pNames[i], pTypes[i]);
                params.add(vtree);
            }
            
            TypeInfo[] excepTypes = mInfo.getExceptionTypes();
            List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
            for (int i = 0 ; excepTypes != null && i < excepTypes.length; i++) {
                throwsList.add((ExpressionTree)createType(wc, excepTypes[i]));
            }
            
            String body = mInfo.getMethodBodyText();
            if(body == null) {
                body = "";
            }
            
            MethodTree mtree = make.Method(createModifiers(wc, mInfo.getModifiers(), mInfo.getAnnotations()),
                    mInfo.getName(),
                    createType(wc, mInfo.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    params,
                    throwsList,
                    "{" + body + "}",
                    null
                    );
            
            //         if(mInfo.getCommentText() != null) {
            //             Comment comment = Comment.create(Comment.Style.JAVADOC, -2,
            //                     -2, -2, mInfo.getCommentText());
            //             make.addComment(mtree, comment, true);
            //         }
            
            return mtree;
        }
        
    /*
     * Returns a tree for a given type in string format
     * Note that import for type is handled by make.QualIdent()
     */
        private static Tree createType(WorkingCopy wc, TypeInfo type) {
            if(type == null) {
                return null;
            }
            String rawType = type.getRawType();
            
            TreeMaker make = wc.getTreeMaker();
            if (rawType.endsWith("[]")) { // NOI18N
                String rawTypeName = rawType.substring(0, rawType.length()-2);
                TypeInfo scalarTypeInfo = new TypeInfo(rawTypeName, type.getDeclaredTypeParameters());
                return make.ArrayType(createType(wc, scalarTypeInfo));
            }
            
            TypeKind primitiveTypeKind = null;
            if ("boolean".equals(rawType)) {           // NOI18N
                primitiveTypeKind = TypeKind.BOOLEAN;
            } else if ("byte".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.BYTE;
            } else if ("short".equals(rawType)) {      // NOI18N
                primitiveTypeKind = TypeKind.SHORT;
            } else if ("int".equals(rawType)) {        // NOI18N
                primitiveTypeKind = TypeKind.INT;
            } else if ("long".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.LONG;
            } else if ("char".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.CHAR;
            } else if ("float".equals(rawType)) {      // NOI18N
                primitiveTypeKind = TypeKind.FLOAT;
            } else if ("double".equals(rawType)) {     // NOI18N
                primitiveTypeKind = TypeKind.DOUBLE;
            } else if ("void".equals(rawType)) {
                primitiveTypeKind = TypeKind.VOID;
            }
            if (primitiveTypeKind != null) {
                return make.PrimitiveType(primitiveTypeKind);
            }
            
            TypeInfo[] declaredTypeParameters = type.getDeclaredTypeParameters();
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                TypeElement typeElement = wc.getElements().getTypeElement(rawType);
                if (typeElement == null) {
                    throw new IllegalArgumentException("Type " + rawType + " cannot be found"); // NOI18N
                }
                return make.QualIdent(typeElement);
            }
            else {
                TypeMirror typeMirror = getTypeMirror(wc, type);
                return make.Type(typeMirror);
            }
        }
        
        private static TypeMirror getTypeMirror(WorkingCopy wc, TypeInfo type) {
            TreeMaker make = wc.getTreeMaker();
            String rawType = type.getRawType();
            TypeElement rawTypeElement = wc.getElements().getTypeElement(rawType);
            if (rawTypeElement == null) {
                throw new IllegalArgumentException("Type " + rawType + " cannot be found"); // NOI18N
            }
            TypeInfo[] declaredTypeParameters = type.getDeclaredTypeParameters();
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                make.QualIdent(rawTypeElement);
                return rawTypeElement.asType();
            }
            else {
                TypeMirror[] declaredTypeMirrors = new TypeMirror[declaredTypeParameters.length];
                for (int i = 0; i < declaredTypeParameters.length; i++) {
                    declaredTypeMirrors[i] = getTypeMirror(wc, declaredTypeParameters[i]);
                }
                DeclaredType declaredType = wc.getTypes().getDeclaredType(rawTypeElement, declaredTypeMirrors);
                return declaredType;
            }
        }
        
        private static ModifiersTree createModifiers(WorkingCopy wc) {
            return wc.getTreeMaker().Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
        }
        
        private static ModifiersTree createModifiers(WorkingCopy wc, long flags, AnnotationInfo[] annotations) {
            if (annotations == null || annotations.length == 0) {
                return wc.getTreeMaker().Modifiers(flags, Collections.<AnnotationTree>emptyList());
            }
            GenerationUtils generationUtils = GenerationUtils.newInstance(wc);
            List<AnnotationTree> annotationTrees = new ArrayList<AnnotationTree>();
            for (AnnotationInfo annotation : annotations) {
                //append an AnnotationTree
                String[] argNames = annotation.getArgNames();
                if (argNames != null && argNames.length > 0) {
                    //one or more args in this annotation
                    Object[] argValues = annotation.getArgValues();
                    List<ExpressionTree> argTrees = new ArrayList<ExpressionTree>();
                    for (int i = 0; i < argNames.length; i++) {
                        ExpressionTree argTree = generationUtils.createAnnotationArgument(argNames[i], argValues[i]);
                        argTrees.add(argTree);
                    }
                    AnnotationTree annotationTree = generationUtils.createAnnotation(annotation.getType(), argTrees);
                    annotationTrees.add(annotationTree);
                } else {
                    //no args in this annotation
                    AnnotationTree annotationTree = generationUtils.createAnnotation(annotation.getType());
                    annotationTrees.add(annotationTree);
                }
            }
            return wc.getTreeMaker().Modifiers(flags, annotationTrees);
        }
        
        public static CompilationUnitTree createImport(WorkingCopy wc, CompilationUnitTree modifiedCut, String fq) {
            if (modifiedCut == null) {
                modifiedCut = wc.getCompilationUnit();  //use committed cut as modifiedCut
            }
            List<? extends ImportTree> imports = modifiedCut.getImports();
            boolean found = false;
            for (ImportTree imp : imports) {
               if (fq.equals(imp.getQualifiedIdentifier().toString())) {
                   found = true; 
                   break;
               }
            }
            if (!found) {
                TreeMaker make = wc.getTreeMaker();
                CompilationUnitTree newCut = make.addCompUnitImport(
                    modifiedCut, 
                    make.Import(make.Identifier(fq), false)
                );                                              //create a newCut from modifiedCut
                wc.rewrite(wc.getCompilationUnit(), newCut);    //replace committed cut with newCut in change map
                return newCut;                                  //return the newCut we just created
            }
            return modifiedCut; //no newCut created from modifiedCut, so just return modifiedCut
        }
        
    }

}
