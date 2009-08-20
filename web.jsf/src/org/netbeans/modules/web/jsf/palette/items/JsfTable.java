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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.jsf.api.palette.PaletteItem;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Buzek
 * @author Po-Ting Wu
 */
public final class JsfTable extends EntityClass implements ActiveEditorDrop, PaletteItem {
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
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'{3}.{2}'}'\"/>\n</h:column>\n",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'{5}.{2}'}'\">\n <f:convertDateTime pattern=\"{4}\" />\n</h:outputText>\n</h:column>\n"
    };
    
    public JsfTable() {
    }
    
    protected String getName() {
        return "Table"; // NOI18N
    }

    public void insert(JTextComponent component) {
        handleTransfer(component);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(JsfForm.class, "NAME_jsp-JsfTable");
    }
    
    protected String createBody(JTextComponent target, boolean surroundWithFView) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append("<f:view>\n");
        }
        stringBuffer.append(MessageFormat.format(BEGIN [formType], new Object [] {variable, "item"})); //NOI18N
        
        FileObject targetJspFO = getFO(target);
        JavaSource javaSource = JavaSource.create(createClasspathInfo(targetJspFO));
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
                    if (isId(controller, method, fieldAccess)) {
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
                        String temporal = getTemporal(controller, method, fieldAccess);
                        if (temporal == null) {
                            stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName, tableVarName}));
                        } else {
                            stringBuffer.append(MessageFormat.format(ITEM [2], new Object [] {name, variable, propName, temporal, getDateTimeFormat(temporal), tableVarName}));
                        }
                    } else if (isRelationship == JpaControllerUtil.REL_NONE || isRelationship == JpaControllerUtil.REL_TO_ONE) {
                        stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName, tableVarName}));
                    }
                }
            }
        }
        stringBuffer.append(MessageFormat.format(commands, new Object [] {tableVarName}));
    }
}
