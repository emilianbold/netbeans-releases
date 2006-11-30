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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
//TODO: RETOUCHE
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.modules.editor.NbEditorKit;
//import org.netbeans.modules.editor.NbEditorUtilities;
//import org.netbeans.modules.j2ee.common.JMIUtils;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.config.model.FacesConfig;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.wizards.JSFClinetGenerator;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Pavel Buzek
 */
public class JsfTable implements ActiveEditorDrop {
        
    private static String [] BEGIN = {
        "<h:form>\n <h:dataTable value=\"#'{'{0}'}'\" var=\"item\">\n",
        "<h:form>\n <h1><h:outputText value=\"List\"/></h1>\n <h:dataTable value=\"#'{'{0}'}'\" var=\"item\">\n",
    };
    private static String [] END = {
        "</h:dataTable>\n </h:form>\n",
        "</h:dataTable>\n </h:form>\n",
    };
    private static String [] ITEM = {
        "",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'item.{2}'}'\"/>\n</h:column>\n",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'item.{2}'}'\">\n <f:convertDateTime type=\"{3}\" pattern=\"{4}\" />\n</h:outputText>\n</h:column>\n",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n"
                + "<h:commandLink action=\"#'{'{4}'}'\" value=\"#'{'item.{2}'}'\"/>\n </h:column>\n",
    };
    
    private String variable = "";
    private String bean = "";
    private int formType = 0;
    private ArrayList managedBeans;
    
    public JsfTable() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        
        JsfTableCustomizer c = new JsfTableCustomizer(this, targetComponent);
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
    //TODO: RETOUCHE
    private String createBody(JTextComponent target, boolean surroundWithFView) {
        StringBuffer sb = new StringBuffer();
        if (surroundWithFView) {
            sb.append("<f:view>\n");
        }
        sb.append(MessageFormat.format(BEGIN [formType], new Object [] {variable}));
//        JavaClass jc = JsfForm.resolveJavaClass(target, bean);
//        if (jc != null) {
//            createTable(jc, variable, sb, "", null);
//        }
        sb.append(END [formType]);
        if (surroundWithFView) {
            sb.append("</f:view>\n");
        }
        return sb.toString();
    }
    
    /** @param commands a message that will be added to the end of each line in table, 
     *  it will be formated using {0} = iterator variable
     */
//    public static void createTable(JavaClass bean, String variable, StringBuffer sb, String commands, String setupDetail) {
//        int formType = 1;
//        Method methods [] = JsfForm.getEntityMethods(bean);
//        boolean fieldAccess = JsfForm.isFieldAccess(bean);
//        for (int i = 0; i < methods.length; i++) {
//            if (methods[i].getName().startsWith("get")) {
//                int isRelationship = JsfForm.isRelationship(methods[i], fieldAccess);
//                String name = methods[i].getName().substring(3);
//                String propName = name.substring(0,1).toLowerCase() + name.substring(1);
//                if (setupDetail != null && JsfForm.isId(methods[i], fieldAccess)) {
//                    String managedBeanName = JSFClinetGenerator.getManagedBeanName(JSFClinetGenerator.simpleClassName(bean.getName()));
//                    sb.append(MessageFormat.format(ITEM [3], new Object [] {name, variable, propName, propName, managedBeanName + "." + setupDetail}));
//                } else if ("java.util.Date".equals(methods[i].getType().getName())) {
//                    //param 3 - temporal, param 4 - date/time format
//                    String temporal = JsfForm.getTemporal(methods[i], fieldAccess);
//                    if (temporal == null) {
//                        sb.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName}));
//                    } else {
//                        sb.append(MessageFormat.format(ITEM [2], new Object [] {name, variable, propName, temporal, JsfForm.getDateTimeFormat(temporal)}));
//                    }
//                } else if (isRelationship == JsfForm.REL_NONE || isRelationship == JsfForm.REL_TO_ONE) {
//                    sb.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName}));
////links to related objects -- does not work correctly for composite IDs                    
////                } else if (isRelationship == JsfForm.REL_TO_ONE) {
////                    String managedBeanName = JSFClinetGenerator.getManagedBeanName(JSFClinetGenerator.simpleClassName(methods[i].getType().getName()));
////                    String relatedIdProp = JSFClinetGenerator.getPropNameFromMethod(JsfForm.getIdGetter(fieldAccess, (JavaClass) methods[i].getType()).getName());
////                    sb.append(MessageFormat.format(ITEM [3], new Object [] {name, variable, propName + "." + relatedIdProp , relatedIdProp, managedBeanName + "." + setupDetail}));
//                }
//            }
//        }
//        sb.append(MessageFormat.format(commands, new Object [] {"item"}));
//    }
//    
//    public ManagedBean[] getManagedBeanNames(JTextComponent target) {
//        managedBeans = new ArrayList();
//        FileObject f = JsfForm.getFO(target);
//        if (f != null) {
//            WebModule wm = WebModule.getWebModule(f);
//            String[] configFiles = JSFConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
//            for (int i = 0; i < configFiles.length; i++) {
//                FileObject fo = wm.getDocumentBase().getFileObject(configFiles[i]);
//                try {
//                    JSFConfigDataObject configDO = (JSFConfigDataObject)DataObject.find(fo);
//                    FacesConfig config= configDO.getFacesConfig();
//                    managedBeans.addAll(Arrays.asList(config.getManagedBean()));
//                } catch (IOException ioex) {
//                    ErrorManager.getDefault().notify(ioex);
//                }
//            }
//        }
//        return (ManagedBean[]) managedBeans.toArray(new ManagedBean[managedBeans.size()]);
//    }
//        
//    
//    public String[] getMethodsForBean(ManagedBean bean, JTextComponent target) {
//        JavaClass cls = bean == null ? null : JMIUtils.findClass(bean.getManagedBeanClass(), JsfForm.getFO(target));
//        if (cls == null) {
//            return new String[0];
//        }
//        Method[] m = JMIUtils.getMethods(cls);
//        String[] names = new String[m.length];
//        for (int i = 0; i < m.length; i++) {
//            names[i] = m[i].getName();
//        }
//        return names;
//    }
//    
//    public String getVariable() {
//        return variable;
//    }
    
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
