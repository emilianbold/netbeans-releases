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

package org.netbeans.modules.web.jsf.wizards;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.palette.items.JsfForm;
import org.netbeans.modules.web.jsf.palette.items.JsfTable;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 *
 * @author Pavel Buzek
 */
public class JSFClientGenerator {
    
    private static String INDEX_PAGE = "index.jsp";
  //TODO: RETOUCHE  
    public static void generateJSFPages(Project project, String entityClass, String jsfFolder, String controllerClass, FileObject pkg) throws IOException {
//        boolean isInjection = Util.isSupportedJavaEEVersion(project);
        
        String simpleControllerName = simpleClassName(controllerClass);
        String simpleEntityName = simpleClassName(entityClass);
        if (jsfFolder.startsWith("/")) {
            jsfFolder = jsfFolder.substring(1);
        }
        
        Sources srcs = (Sources) project.getLookup().lookup(Sources.class);
        String pkgName = controllerClass.substring(0, controllerClass.lastIndexOf('.'));
        
        String persistenceUnit = null;
//        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project);
//        if (persistenceScopes.length > 0) {
//            FileObject persXml = persistenceScopes[0].getPersistenceXml();
//            if (persXml != null) {
//                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
//                PersistenceUnit units[] = persistence.getPersistenceUnit();
//                if (units.length > 0) {
//                    persistenceUnit = units[0].getName();
//                }
//            }
//        }
//        SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
//        final FileObject jsfRoot = FileUtil.createFolder(sgWeb[0].getRootFolder(), jsfFolder);
//        
//        String simpleConverterName = simpleEntityName + "Converter"; //NOI18N
//        String converterName = pkgName + "." + simpleConverterName;
//        String fieldName = fieldFromClassName(simpleEntityName);
//
//        //detect access type
//        JavaClass jc = JsfForm.resolveJavaClass(pkg, entityClass);
//        boolean fieldAccess = JsfForm.isFieldAccess(jc);
//        
//        Method methods [] = JsfForm.getEntityMethods(jc);
//        String idProperty = "Cannot Detect @Id Property";
//        String idGetter = "Cannot Detect @Id Property";
//        String idPropertyType = "int";
//        List toOneRelMethods = new ArrayList();
//        List toManyRelMethods = new ArrayList();
//        for (int i = 0; i < methods.length; i++) {
//            if (methods[i].getName().startsWith("get")) {
//                Feature f = fieldAccess ? JsfForm.guessField(methods[i]) : methods[i];
//                if (f != null) {
//                    for (Iterator it = f.getAnnotations().iterator(); it.hasNext();) {
//                        Annotation ann = (Annotation) it.next();
//                        if (ann.getType() == null) {
//                            //cannot resolve type, invalid code
//                            continue;
//                        }
//                        String annName = ann.getType().getName();
//                        if ("javax.persistence.Id".equals(annName) ||
//                                "javax.persistence.EmbeddedId".equals(annName)) {
//                            String name = methods[i].getName().substring(3);
//                            idGetter = methods[i].getName();
//                            idProperty = getPropNameFromMethod(methods[i].getName());
//                            idPropertyType = methods[i].getType().getName();
//                        } else if ("javax.persistence.OneToOne".equals(annName) ||
//                                "javax.persistence.ManyToOne".equals(annName)) {
//                            toOneRelMethods.add(methods[i]);
//                        } else if ("javax.persistence.OneToMany".equals(annName) ||
//                                "javax.persistence.ManyToMany".equals(annName)) {
//                            toManyRelMethods.add(methods[i]);
//                        }
//                    }
//                }
//            }
//        }
//        
//        JEditorPane ep = new JEditorPane("text/x-jsp", "");
//        BaseDocument doc = new BaseDocument(ep.getEditorKit().getClass(), false);
//        WebModule wm = WebModule.getWebModule(jsfRoot);
//        boolean addLinksToIndex = false;
//        
//        //automatically add JSF framework if it is not added
//        JSFFrameworkProvider fp = new JSFFrameworkProvider();
//        if (!fp.isInWebModule(wm)) {
//            fp.extend(wm);
//        }
//        
//        boolean rollback = true;
//        
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            JMIGenerationUtil.addInterface(jc, "java.io.Serializable"); //NOI18N
//            rollback = false;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            JavaModel.getJavaRepository().endTrans(rollback);
//        }
//            
//        JavaClass javaClass = generateControllerClass(fieldName, pkg, idProperty, idGetter, persistenceUnit, simpleControllerName, entityClass, idPropertyType, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection, fieldAccess);
//        
//        String managedBean =  getManagedBeanName(simpleEntityName);
//        JavaClass converter = generateConverter(pkg, simpleConverterName, controllerClass, simpleControllerName, entityClass, simpleEntityName, idPropertyType, idGetter, managedBean, isInjection);
//            
//        // <editor-fold desc=" Add link to List.jsp into index.jsp ">
//            
//        FileObject documentBase = wm.getDocumentBase();
//        FileObject indexjsp = documentBase.getFileObject(INDEX_PAGE); //NOI18N
//        if (indexjsp != null){
//            
//            String content = JSFFrameworkProvider.readResource(indexjsp.getInputStream(), "UTF-8"); //NO18N
//            
//            // what find
//            String find = "<h1>JSP Page</h1>"; // NOI18N
//            String endLine = System.getProperty("line.separator"); //NOI18N
//            if ( content.indexOf(find) > 0){
//                addLinksToIndex = true;
//                StringBuffer replace = new StringBuffer();
//                replace.append(find);
//                replace.append(endLine);
//                replace.append("    <br/>");                        //NOI18N
//                replace.append(endLine);
//                replace.append("    <a href=\".");                  //NOI18N
//                replace.append(JSFConfigUtilities.translateURI(JSFConfigUtilities.getActionServletMapping(wm.getDeploymentDescriptor()),"/" + jsfFolder + "/List.jsp")); //NOI18N
//                replace.append("\">");                              //NOI18N
//                replace.append("List of " + simpleEntityName);
//                replace.append("</a>");                             //NOI18N
//                content = content.replaceFirst(find, new String (replace.toString().getBytes("UTF8"), "UTF-8")); //NOI18N
//                JSFFrameworkProvider.createFile(indexjsp, content, "UTF-8"); //NOI18N
//            }
//        }
//
//        // </editor-fold>
//        
//        String linkToIndex = addLinksToIndex ? "<br>\n<a href=\"" + wm.getContextPath() + "/" + INDEX_PAGE + "\">Back to index</a>\n" : "";
//        // <editor-fold desc=" Generate List.jsp ">
//        FileSystem fs = jsfRoot.getFileSystem();
//        StringBuffer listSb = new StringBuffer();
//        listSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
//                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
//                + "<title>List " + simpleEntityName + "</title>\n"
//                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
//        listSb.append("<h1>Listing " + simpleEntityName + "s</h1>\n");
//        listSb.append("<h:form>\n");
//        listSb.append("<h:commandLink action=\"#{" + managedBean + ".createSetup}\" value=\"New " + simpleEntityName + "\"/>\n"
//                + linkToIndex + "<br>\n");
//        listSb.append(MessageFormat.format("<h:outputText value=\"Item #'{'{0}.firstItem + 1'}'..#'{'{0}.lastItem'}' of #'{'{0}.itemCount}\"/>"
//                + "&nbsp;\n"
//                + "<h:commandLink action=\"#'{'{0}.prev'}'\" value=\"Previous #'{'{0}.batchSize'}'\" rendered=\"#'{'{0}.firstItem >= {0}.batchSize'}'\"/>"
//                + "&nbsp;\n"
//                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Next #'{'{0}.batchSize'}'\" rendered=\"#'{'{0}.lastItem + {0}.batchSize <= {0}.itemCount}\"/>"
//                + "&nbsp;\n"
//                + "<h:commandLink action=\"#'{'{0}.next'}'\" value=\"Remaining #'{'{0}.itemCount - {0}.lastItem'}'\"\n"
//                + "rendered=\"#'{'{0}.lastItem < {0}.itemCount && {0}.lastItem + {0}.batchSize > {0}.itemCount'}'\"/>\n", managedBean));
//        listSb.append("<h:dataTable value='#{" + managedBean + "." + fieldName + "s}' var='item' border=\"1\" cellpadding=\"2\" cellspacing=\"0\">\n");
//        String commands = "<h:column>\n <h:commandLink value=\"Destroy\" action=\"#'{'" + managedBean + ".destroy'}'\">\n" 
//                + "<f:param name=\"" + idProperty +"\" value=\"#'{'{0}." + idProperty + "'}'\"/>\n"
//                + "</h:commandLink>\n  <h:outputText value=\" \"/>\n"
//                + " <h:commandLink value=\"Edit\" action=\"#'{'" + managedBean + ".editSetup'}'\">\n"
//                + "<f:param name=\"" + idProperty +"\" value=\"#'{'{0}." + idProperty + "'}'\"/>\n"
//                + "</h:commandLink>\n </h:column>\n";
//        JsfTable.createTable(JMIUtils.findClass(entityClass), managedBean + "." + fieldName, listSb, commands, "detailSetup");
//        listSb.append("</h:dataTable>\n </h:form>\n</f:view>\n</body>\n</html>\n");
//        
//        try {
//            doc.remove(0, doc.getLength());
//            doc.insertString(0, listSb.toString(), null);
//            doc.getFormatter().reformat(doc, 0, doc.getLength());
//            listSb.replace(0, listSb.length(), doc.getText(0, doc.getLength()));
//        } catch (BadLocationException e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//        }
//        
//        final String listText = listSb.toString();
//
//        fs.runAtomicAction(new FileSystem.AtomicAction() {
//            public void run() throws IOException {
//                FileObject list = FileUtil.createData(jsfRoot, "List.jsp");//NOI18N
//                FileLock lock = list.lock();
//                try {
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(list.getOutputStream(lock)));
//                    bw.write(listText);
//                    bw.close();
//                }
//                finally {
//                    lock.releaseLock();
//                }
//            }
//        });
//        
//        // </editor-fold>
//        
//        // <editor-fold desc=" Generate New.jsp ">
//        
//        StringBuffer newSb = new StringBuffer();
//        newSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
//                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
//                + "<title>New " + simpleEntityName + "</title>\n"
//                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
//        newSb.append("<h1>New " + managedBean + "</h1>\n");
//        newSb.append("<h:form>\n  <h:panelGrid columns=\"2\">\n");
//        
//        JsfForm.createForm(JMIUtils.findClass(entityClass), JsfForm.FORM_TYPE_NEW, managedBean + "." + fieldName, newSb, true);
//        newSb.append("</h:panelGrid>\n");
//        
//        List<String> classNames = new ArrayList<String>();
//        List<String> idProperties = new ArrayList<String>();
//        for(Iterator it = toOneRelMethods.iterator(); it.hasNext();) {
//            Method m = (Method) it.next();
//            Method otherSide = JsfForm.getOtherSideOfRelation(m, fieldAccess);
//            if (otherSide != null) {
//                JavaClass relClass = (JavaClass) otherSide.getDeclaringClass();
//                classNames.add(simpleClassName(relClass.getName()));
//                idProperties.add(getPropNameFromMethod(m.getName()));
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
//                    classNames.get(i) + "}\" value=\"Create\" rendered=\"#{" + managedBean + "." + fieldName + "." + idProperties.get(i) + " != null" + negativeCondition.toString() + "}\"/>\n");
//            if (i > 0) {
//                newRenderDefaultOption.append(" and ");
//            }
//            newRenderDefaultOption.append(managedBean + "." + fieldName + "." + idProperties.get(i) + " == null");
//                
//        }
//        
////      <h:commandLink action="#{comment.create}" value="Create" rendered="#{comment.comment.postId == null}"/>
//        if (classNames.size() == 0) {
//            newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\"/>\n<br>\n");
//        } else {
//            newSb.append("<h:commandLink action=\"#{" + managedBean + ".create}\" value=\"Create\" rendered=\"#{" + newRenderDefaultOption.toString() + "}\"/>\n<br>\n");
//        }
//        
//        newSb.append("<h:commandLink action=\"" + fieldName + "_list\" value=\"Show All " + simpleEntityName + "\"/>\n " + linkToIndex
//                + "</h:form>\n </f:view>\n</body>\n</html>\n");
//        
//        try {
//            doc.remove(0, doc.getLength());
//            doc.insertString(0, newSb.toString(), null);
//            doc.getFormatter().reformat(doc, 0, doc.getLength());
//            newSb.replace(0, newSb.length(), doc.getText(0, doc.getLength()));
//        } catch (BadLocationException e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//        }
//        final String newText = newSb.toString();
//
//        fs.runAtomicAction(new FileSystem.AtomicAction() {
//            public void run() throws IOException {
//                FileObject newForm = FileUtil.createData(jsfRoot, "New.jsp");//NOI18N
//                FileLock lock = newForm.lock();
//                try {
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(newForm.getOutputStream(lock)));
//                    bw.write(newText);
//                    bw.close();
//                }
//                finally {
//                    lock.releaseLock();
//                }
//            }
//        });
//        
//        // </editor-fold>
//
//        // <editor-fold desc=" Generate Edit.jsp ">
//        
//        StringBuffer editSb = new StringBuffer();
//        editSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
//                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
//                + "<title>Edit " + simpleEntityName + "</title>\n"
//                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
//        editSb.append("<h1>Edit " + managedBean + "</h1>\n");
//        editSb.append("<h:form>\n  <h:inputHidden value=\"#{" + managedBean + "." + fieldName + "}\" immediate=\"true\"/>\n"
//                + "<h:panelGrid columns=\"2\">\n");
//        
//        JsfForm.createForm(JMIUtils.findClass(entityClass), JsfForm.FORM_TYPE_EDIT, managedBean + "." + fieldName, editSb, true);
//        editSb.append("</h:panelGrid>\n<h:commandLink action=\"#{" + managedBean + ".edit}\" value=\"Save\"/>\n<br>\n"
//                + "<h:commandLink action=\"" + fieldName + "_list\" value=\"Show All " + simpleEntityName + "\"/>\n" + linkToIndex
//                + "</h:form>\n </f:view>\n</body>\n</html>\n");
//
//        try {
//            doc.remove(0, doc.getLength());
//            doc.insertString(0, editSb.toString(), null);
//            doc.getFormatter().reformat(doc, 0, doc.getLength());
//            editSb.replace(0, editSb.length(), doc.getText(0, doc.getLength()));
//        } catch (BadLocationException e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//        }
//
//        final String editText = editSb.toString();
//
//        fs.runAtomicAction(new FileSystem.AtomicAction() {
//            public void run() throws IOException {
//                FileObject editForm = FileUtil.createData(jsfRoot, "Edit.jsp");//NOI18N
//                FileLock lock = editForm.lock();
//                try {
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(editForm.getOutputStream(lock)));
//                    bw.write(editText);
//                    bw.close();
//                }
//                finally {
//                    lock.releaseLock();
//                }
//            }
//        });
//        
//        // </editor-fold>
//        
//        // <editor-fold desc=" Generate Detail.jsp ">
//        
//        StringBuffer detailSb = new StringBuffer();
//        detailSb.append("<%@page contentType=\"text/html\"%>\n<%@page pageEncoding=\"UTF-8\"%>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n"
//                + "<%@taglib uri=\"http://java.sun.com/jsf/html\" prefix=\"h\" %>\n"
//                + "<html>\n<head>\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
//                + "<title>Detail of " + simpleEntityName + "</title>\n"
//                + "</head>\n<body>\n<f:view>\n  <h:messages errorStyle=\"color: red\" infoStyle=\"color: green\" layout=\"table\"/>\n ");
//        detailSb.append("<h1>Detail of " + managedBean + "</h1>\n");
//        detailSb.append("<h:form>\n  <h:panelGrid columns=\"2\">\n");
//        
//        JsfForm.createForm(JMIUtils.findClass(entityClass), JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, detailSb, true);
//        detailSb.append("</h:panelGrid>\n");
//        JsfForm.createTablesForRelated(JMIUtils.findClass(entityClass), JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, idProperty, isInjection, detailSb);
//        detailSb.append("<h:commandLink action=\"" + fieldName + "_edit\" value=\"Edit\" />\n<br>\n"
//                + "<h:commandLink action=\"" + fieldName + "_list\" value=\"Show All " + simpleEntityName + "\"/>\n" + linkToIndex
//                + "</h:form>\n </f:view>\n</body>\n</html>\n");
//
//        try {
//            doc.remove(0, doc.getLength());
//            doc.insertString(0, detailSb.toString(), null);
//            doc.getFormatter().reformat(doc, 0, doc.getLength());
//            detailSb.replace(0, detailSb.length(), doc.getText(0, doc.getLength()));
//        } catch (BadLocationException e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//        }
//
//        final String detailText = detailSb.toString();
//
//        fs.runAtomicAction(new FileSystem.AtomicAction() {
//            public void run() throws IOException {
//                FileObject detailForm = FileUtil.createData(jsfRoot, "Detail.jsp");//NOI18N
//                FileLock lock = detailForm.lock();
//                try {
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(detailForm.getOutputStream(lock)));
//                    bw.write(detailText);
//                    bw.close();
//                }
//                finally {
//                    lock.releaseLock();
//                }
//            }
//        });
//        
//        // </editor-fold>
//        
//        // <editor-fold desc=" Add managed bean, converters and navigation rules into faces-config.xml ">
//        
//        String[] configFiles = JSFConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
//        if (configFiles.length > 0) {
//            FileObject fo = wm.getDocumentBase().getFileObject(configFiles[0]);
//            try {
//                JSFConfigDataObject configDO = (JSFConfigDataObject)DataObject.find(fo);
//                FacesConfig config= configDO.getFacesConfig();
//                ManagedBean mb = config.newManagedBean();
//                mb.setManagedBeanName(managedBean);
//                mb.setManagedBeanClass(controllerClass);
//                mb.setManagedBeanScope("session");
//                config.addManagedBean(mb);
//
//                Converter cv = config.newConverter();
//                cv.setConverterForClass(entityClass);
//                cv.setConverterClass(converterName);
//                config.addConverter(cv);
//                
//                Type idType = JMIUtils.resolveType(idPropertyType);
//                if (idType instanceof JavaClass) {
//                    JavaClass idClass = (JavaClass) idType;
//                    if (JsfForm.isEmbeddableClass(idClass)) {
//                        cv = config.newConverter();
//                        cv.setConverterForClass(idPropertyType);
//                        cv.setConverterClass((pkgName.length() > 0 ? pkgName + "." : "") + simpleClassName(idPropertyType) + "Converter");
//                        config.addConverter(cv);
//                    }
//                }
//                
//                NavigationRule nr = config.newNavigationRule();
//                NavigationCase nc = nr.newNavigationCase();
//                nc.setFromOutcome(fieldName + "_create");
//                nc.setToViewId("/" + jsfFolder + "/New.jsp");
//                nr.addNavigationCase(nc);
//                config.addNavigationRule(nr);
//
//                nr = config.newNavigationRule();
//                nc = nr.newNavigationCase();
//                nc.setFromOutcome(fieldName + "_list");
//                nc.setToViewId("/" + jsfFolder + "/List.jsp");
//                nr.addNavigationCase(nc);
//                config.addNavigationRule(nr);
//
//                nr = config.newNavigationRule();
//                nc = nr.newNavigationCase();
//                nc.setFromOutcome(fieldName + "_edit");
//                nc.setToViewId("/" + jsfFolder + "/Edit.jsp");
//                nr.addNavigationCase(nc);
//                config.addNavigationRule(nr);
//
//                nr = config.newNavigationRule();
//                nc = nr.newNavigationCase();
//                nc.setFromOutcome(fieldName + "_detail");
//                nc.setToViewId("/" + jsfFolder + "/Detail.jsp");
//                nr.addNavigationCase(nc);
//                config.addNavigationRule(nr);
//
//                configDO.write(config);
//            } catch (IOException ioex) {
//                ErrorManager.getDefault().notify(ioex);
//            }
//        }
//        // </editor-fold>
        
    }
    
//    private static JavaClass generateConverter(
//            final FileObject pkg,
//            final String simpleConverterName,
//            final String controllerClass,
//            final String simpleControllerName,
//            final String entityClass,
//            final String simpleEntityName,
//            final String idPropertyType,
//            final String idGetter,
//            final String managedBeanName,
//            final boolean isInjection) {
//        JavaClass javaClass = null;
//        boolean rollback = true;
//        
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            JavaClass idClass = null;
//            Type idType = JMIUtils.resolveType(idPropertyType);
//            if (idType instanceof JavaClass) {
//                idClass = (JavaClass) idType;
//            }
//            boolean embeddable = idClass != null && JsfForm.isEmbeddableClass(idClass);
//            JavaClass idConverter = null;
//            Parameter p;
//            Method getAsObjectE = null;
//            Method getAsStringE = null;
//            
//            if (embeddable) {
//                idConverter = JMIGenerationUtil.createClass(pkg, simpleClassName(idPropertyType) + "Converter"); //NOI18N
//                JMIGenerationUtil.addInterface(idConverter, "javax.faces.convert.Converter");
//                getAsObjectE = JMIGenerationUtil.createMethod(idConverter, "getAsObject", Modifier.PUBLIC, "java.lang.Object"); //NOI18N
//                p = JMIGenerationUtil.createParameter(idConverter, "facesContext", "javax.faces.context.FacesContext"); //NOI18N
//                getAsObjectE.getParameters().add(p);
//                p  = JMIGenerationUtil.createParameter(idConverter, "uIComponent", "javax.faces.component.UIComponent"); //NOI18N
//                getAsObjectE.getParameters().add(p);
//                p  = JMIGenerationUtil.createParameter(idConverter, "string", "java.lang.String"); //NOI18N
//                getAsObjectE.getParameters().add(p);
//                
//                getAsStringE = JMIGenerationUtil.createMethod(idConverter, "getAsString", Modifier.PUBLIC, "java.lang.String"); //NOI18N
//                p = JMIGenerationUtil.createParameter(idConverter, "facesContext", "javax.faces.context.FacesContext"); //NOI18N
//                getAsStringE.getParameters().add(p);
//                p  = JMIGenerationUtil.createParameter(idConverter, "uIComponent", "javax.faces.component.UIComponent"); //NOI18N
//                getAsStringE.getParameters().add(p);
//                p  = JMIGenerationUtil.createParameter(idConverter, "object", "java.lang.Object"); //NOI18N
//                getAsStringE.getParameters().add(p);
//            }
//            
//            javaClass = JMIGenerationUtil.createClass(pkg, simpleConverterName);
//            JMIGenerationUtil.addInterface(javaClass, "javax.faces.convert.Converter");
//            
//            Method getAsObject = JMIGenerationUtil.createMethod(javaClass, "getAsObject", Modifier.PUBLIC, "java.lang.Object"); //NOI18N
//            p = JMIGenerationUtil.createParameter(javaClass, "facesContext", "javax.faces.context.FacesContext"); //NOI18N
//            getAsObject.getParameters().add(p);
//            p  = JMIGenerationUtil.createParameter(javaClass, "uIComponent", "javax.faces.component.UIComponent"); //NOI18N
//            getAsObject.getParameters().add(p);
//            p  = JMIGenerationUtil.createParameter(javaClass, "string", "java.lang.String"); //NOI18N
//            getAsObject.getParameters().add(p);
//
//            String controllerReferenceName = JMIGenerationUtil.createImport(javaClass, controllerClass).getName();
//            StringBuffer getAsObjectBody = new StringBuffer();
//            getAsObjectBody.append("if (string == null) {\n return null;\n }\n");
//            
//            String controllerVariable;
//            if (isInjection) {
//                controllerVariable= controllerReferenceName + " controller = (" 
//                        + controllerReferenceName 
//                        + ") facesContext.getApplication().getELResolver().getValue(\nfacesContext.getELContext(), null, \"" 
//                        + managedBeanName +"\");\n";
//            } else {
//                controllerVariable = controllerReferenceName + " controller = ("
//                        + controllerReferenceName 
//                        + ") facesContext.getApplication().getVariableResolver().resolveVariable(\nfacesContext, \"" 
//                        + managedBeanName +"\");\n";
//            }
//            ArrayList<Method> paramSetters = new ArrayList<Method>();
//            if (embeddable) {
//                getAsObjectBody.append(idPropertyType + " id = new " 
//                        + idPropertyType + "();\n");
//                Method methods [] = JMIUtils.getMethods(idClass);
//                boolean fieldAccess = JsfForm.isFieldAccess(idClass);
//                JMIGenerationUtil.createImport(javaClass, "java.util.StringTokenizer");
//                JMIGenerationUtil.createImport(idConverter, "java.util.StringTokenizer");
//                getAsObjectBody.append("StringTokenizer idTokens = new StringTokenizer(string, \";\");\n");
//                for (int i = 0; i < methods.length; i++) {
//                    if (methods[i].getName().startsWith("get")) {
//                        paramSetters.add(methods[i]);
//                    }
//                }
//                int params = paramSetters.size();
//                getAsObjectBody.append("String params[] = new String[" + params + "];\n"
//                        + "int i = 0;\n while(idTokens.hasMoreTokens()) {\n"
//                        + "params[i++] = idTokens.nextToken();\n }\n"
//                        + "if (i != " + params + ") {\n"
//                        + "throw new IllegalArgumentException(\"Expected format of parameter string is a set of "
//                        + params + " IDs delimited by ;\");\n }\n");
//                for (int i = 0; i < paramSetters.size(); i++) {
//                    getAsObjectBody.append("id.s" + paramSetters.get(i).getName().substring(1) + "(" 
//                            + createIdFieldInitialization(paramSetters.get(i).getType().getName(), "params[" + i + "]") + ");\n");
//                }
//                getAsObjectE.setBodyText(getAsObjectBody.toString() + "return id;\n");
//                idConverter.getFeatures().add(getAsObjectE);
//                getAsObjectBody.append(controllerVariable + "\n return controller.find" + simpleEntityName + "(id);");
//            } else {
//                getAsObjectBody.append(createIdFieldDeclaration(idPropertyType, "string") + "\n"
//                        + controllerVariable
//                        + "\n return controller.find" + simpleEntityName + "(id);");
//            }
//            getAsObject.setBodyText(getAsObjectBody.toString());
//            javaClass.getFeatures().add(getAsObject);
//            
//            Method getAsString = JMIGenerationUtil.createMethod(javaClass, "getAsString", Modifier.PUBLIC, "java.lang.String"); //NOI18N
//            p = JMIGenerationUtil.createParameter(javaClass, "facesContext", "javax.faces.context.FacesContext"); //NOI18N
//            getAsString.getParameters().add(p);
//            p  = JMIGenerationUtil.createParameter(javaClass, "uIComponent", "javax.faces.component.UIComponent"); //NOI18N
//            getAsString.getParameters().add(p);
//            p  = JMIGenerationUtil.createParameter(javaClass, "object", "java.lang.Object"); //NOI18N
//            getAsString.getParameters().add(p);
//            
//            String entityReferenceName = JMIGenerationUtil.createImport(javaClass, entityClass).getName();
//            String idPropertyTypeRefName = null;
//            StringBuffer getAsStringBody = new StringBuffer();
//            StringBuffer getAsStringEBody = new StringBuffer();
//            getAsStringBody.append("if (object == null) {\n return null;\n }\n"
//                    + "if(object instanceof " + entityReferenceName + ") {\n"
//                    + entityReferenceName + " o = (" + entityReferenceName +") object;\n");
//            if (embeddable) {
//                idPropertyTypeRefName = JMIGenerationUtil.createImport(idConverter, idPropertyType).getName();
//                getAsStringEBody.append("if (object == null) {\n return null;\n }\n"
//                        + "if(object instanceof " + idPropertyTypeRefName + ") {\n"
//                        + idPropertyTypeRefName + " o = (" + idPropertyTypeRefName +") object;\n");
//                getAsStringBody.append("return ");
//                getAsStringEBody.append("return ");
//                for(int i = 0; i < paramSetters.size(); i++) {
//                    if (i > 0) {
//                        getAsStringBody.append(" + \";\" + ");
//                        getAsStringEBody.append(" + \";\" + ");
//                    }
//                    getAsStringBody.append("o." + idGetter + "()." + paramSetters.get(i).getName() + "()");
//                    getAsStringEBody.append("o." + paramSetters.get(i).getName() + "()");
//                }
//                getAsStringBody.append(";\n");
//                getAsStringEBody.append(";\n");
//            } else {
//                getAsStringBody.append("return \"\" + o." + idGetter + "();\n");
//            }
//            getAsStringBody.append("} else {\n"
//                    + "throw new IllegalArgumentException(\"object:\" + object + \" of type:\" + object.getClass().getName() + \"; expected type: " + entityClass +"\");\n}");
//            getAsString.setBodyText(getAsStringBody.toString());
//            javaClass.getFeatures().add(getAsString);
//            if (embeddable) {
//                getAsStringEBody.append("} else {\n"
//                        + "throw new IllegalArgumentException(\"object:\" + object + \" of type:\" + object.getClass().getName() + \"; expected type: " + idPropertyTypeRefName +"\");\n}");
//                getAsStringE.setBodyText(getAsStringEBody.toString());
//                idConverter.getFeatures().add(getAsStringE);
//            }
//            
//            rollback = false;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            JavaModel.getJavaRepository().endTrans(rollback);
//        }
//        return javaClass;
//    }
//    
//    private static JavaClass generateControllerClass(
//            final String fieldName, 
//            final FileObject pkg, 
//            final String idProperty, 
//            final String idGetter, 
//            final String persistenceUnit, 
//            final String simpleControllerName, 
//            final String entityClass, 
//            final String idPropertyType, 
//            final String simpleEntityName,
//            final List toOneRelMethods,
//            final List toManyRelMethods,
//            final boolean isInjection,
//            final boolean isFieldAccess) {
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
//    }

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
            idField = "(" + idPropertyType + ") FacesContext.getCurrentInstance().getApplication().\n"
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
}
