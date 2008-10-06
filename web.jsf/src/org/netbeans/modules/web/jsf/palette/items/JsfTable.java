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
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Buzek
 */
public final class JsfTable implements ActiveEditorDrop {
    
    private static String [] BEGIN = {
        "<h:form>\n <h:dataTable value=\"#'{'{0}'}'\" var=\"{1}\">\n",
        "<h:form>\n <h1><h:outputText value=\"List\"/></h1>\n <h:dataTable value=\"#'{'{0}'}'\" var=\"{1}\">\n",
    };
    private static String [] END = {
        "</h:dataTable>\n </h:form>\n",
        "</h:dataTable>\n </h:form>\n",
    };
    private static String [] ITEM = {
        "",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\" #'{'{3}.{2}'}'\"/>\n</h:column>\n",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'{5}.{2}'}'\">\n <f:convertDateTime pattern=\"{4}\" />\n</h:outputText>\n</h:column>\n"
    };
    
    private String variable = "";
    private String bean = "";
    private int formType = 0;
    private ArrayList<ManagedBean> managedBeans;
    
    public JsfTable() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        JsfTableCustomizer jsfTableCustomizer = new JsfTableCustomizer(this, targetComponent);
        boolean accept = jsfTableCustomizer.showDialog();
        if (accept) {
            try {
                String prefixHtml = JSFPaletteUtilities.findJsfHtmlPrefix(targetComponent);
                String prefixCore = JSFPaletteUtilities.findJsfCorePrefix(targetComponent);
                
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
        stringBuffer.append(MessageFormat.format(BEGIN [formType], new Object [] {variable, "item"})); //NOI18N
        
        FileObject targetJspFO = JsfForm.getFO(target);
        JavaSource javaSource = JavaSource.create(JsfForm.createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(bean);
                createTable(controller, typeElement, variable, stringBuffer, "", null, null);
            }
        }, true);
        stringBuffer.append(END [formType]);
        if (surroundWithFView) {
            stringBuffer.append("</f:view>\n");
        }
        return stringBuffer.toString();
    }
    
    /** @param commands a message that will be added to the end of each line in table,
     *  it will be formated using {0} = iterator variable
     */
    public static void createTable(CompilationController controller, TypeElement bean, String variable, StringBuffer stringBuffer, 
            String commands, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, String tableVarName) {
        if (tableVarName == null) {
            tableVarName = "item"; //NOI18N
        }
        int formType = 1;
        TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType();
        if (bean != null) {
            ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
            boolean fieldAccess = JpaControllerUtil.isFieldAccess(bean);
            for (ExecutableElement method : methods) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    int isRelationship = JpaControllerUtil.isRelationship(controller, method, fieldAccess);
                    String name = methodName.substring(3);
                    String propName = JpaControllerUtil.getPropNameFromMethod(methodName);
                    if (JsfForm.isId(controller, method, fieldAccess)) {
                        TypeMirror rType = method.getReturnType();
                        if (TypeKind.DECLARED == rType.getKind()) {
                            DeclaredType rTypeDeclared = (DeclaredType)rType;
                            TypeElement rTypeElement = (TypeElement) rTypeDeclared.asElement();
                            if (JpaControllerUtil.isEmbeddableClass(rTypeElement)) {
                                if (embeddedPkSupport == null) {
                                    embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
                                }
                                for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(controller, bean)) {
                                    if (!embeddedPkSupport.isRedundantWithRelationshipField(controller, bean, pkMethod)) {
                                        String pkMethodName = pkMethod.getSimpleName().toString();
                                        String pkPropTitle = pkMethodName.substring(3);
                                        String pkPropName = propName + "." + JpaControllerUtil.getPropNameFromMethod(pkMethodName);
                                        stringBuffer.append(MessageFormat.format(ITEM [1], new Object [] {pkPropTitle, null, pkPropName, tableVarName}));
                                    }
                                }
                            }
                            else {
                                stringBuffer.append(MessageFormat.format(ITEM [1], new Object [] {name, variable, propName, tableVarName}));
                            }
                        }
                    } else if (controller.getTypes().isSameType(dateTypeMirror, method.getReturnType())) {
                        //param 3 - temporal, param 4 - date/time format
                        String temporal = JsfForm.getTemporal(controller, method, fieldAccess);
                        if (temporal == null) {
                            stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName, tableVarName}));
                        } else {
                            stringBuffer.append(MessageFormat.format(ITEM [2], new Object [] {name, variable, propName, temporal, JsfForm.getDateTimeFormat(temporal), tableVarName}));
                        }
                    } else if (isRelationship == JpaControllerUtil.REL_NONE || isRelationship == JpaControllerUtil.REL_TO_ONE) {
                        stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName, tableVarName}));
                    }
                }
            }
        }
        stringBuffer.append(MessageFormat.format(commands, new Object [] {tableVarName}));
    }
    
    public ManagedBean[] getManagedBeanNames(JTextComponent target) {
        managedBeans = new ArrayList<ManagedBean>();
        FileObject fileObject = JsfForm.getFO(target);
        if (fileObject != null) {
            WebModule webModule = WebModule.getWebModule(fileObject);
            for (FileObject fo : ConfigurationUtils.getFacesConfigFiles(webModule)) {
                JSFConfigModel model = ConfigurationUtils.getConfigModel(fo, true);
                managedBeans.addAll(model.getRootComponent().getManagedBeans());
            }
        }
        return managedBeans.toArray(new ManagedBean[managedBeans.size()]);
    }
    
    public String[] getMethodsForBean(final ManagedBean bean, JTextComponent target) throws IOException {
        final List<String> result = new ArrayList<String>();
        FileObject fileObject = JsfForm.getFO(target);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(bean.getManagedBeanClass());
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    result.add(method.getSimpleName().toString());
                }
            }
        }, true);
        return result.toArray(new String[result.size()]);
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
    
}
