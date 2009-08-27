/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.palette.items;

import java.awt.Dialog;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public abstract class FromEntityBase {

    private static final String ITEM_VAR = "item";

    private boolean readOnly = false;

    protected abstract boolean isCollectionComponent();

    protected abstract boolean showReadOnlyFormFlag();

    protected abstract String getDialogTitle();

    protected abstract String getTemplate();

    protected final boolean isReadOnlyForm() {
        return readOnly;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        Project p = null;
        FileObject fo = JSFPaletteUtilities.getFileObject(targetComponent);
        if (fo != null) {
            p = FileOwnerQuery.getOwner(fo);
        }
        if (p == null) {
            return false;
        }

        ManagedBeanCustomizer mbc = new ManagedBeanCustomizer(p, isCollectionComponent(), showReadOnlyFormFlag());
        DialogDescriptor dd = new DialogDescriptor(mbc,
            getDialogTitle(),
            true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(dd);
            mbc.setDialog(dlg);
            dlg.setVisible(true);
        } finally {
            if (dlg != null)
                dlg.dispose();
        }

        boolean accept = (dd.getValue() == DialogDescriptor.OK_OPTION && !mbc.isCancelled());
        readOnly = mbc.isReadOnly();
        if (accept) {
            try {
                boolean containsFView = isInViewTag(targetComponent);
                String body = expandTemplate(targetComponent, !containsFView, FileEncodingQuery.getEncoding(fo),
                        mbc.getBeanClass(), mbc.getManagedBeanProperty());
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

    public static boolean isInViewTag(JTextComponent targetComponent) {
        try {
            Caret caret = targetComponent.getCaret();
            int position0 = Math.min(caret.getDot(), caret.getMark());
            int position1 = Math.max(caret.getDot(), caret.getMark());
            int len = targetComponent.getDocument().getLength() - position1;
            return targetComponent.getText(0, position0).contains("<f:view>") // NOI18N
                    && targetComponent.getText(position1, len).contains("</f:view>"); // NOI18N
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            // we don't know; let's assume we are:
            return true;
        }
    }

    public void insert(JTextComponent component) {
        handleTransfer(component);
    }

    private String expandTemplate(JTextComponent target, boolean surroundWithFView,
            Charset encoding, final String entityClass, final String managedBeanProperty) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append("<f:view>\n"); // NOI18N
        }
        final Map<String, Object> params = new HashMap<String, Object>();
        FileObject targetJspFO = EntityClass.getFO(target);
        JavaSource javaSource = JavaSource.create(EntityClass.createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                enumerateEntityFields(params, controller, typeElement, managedBeanProperty);
            }
        }, true);
        params.put("managedBeanProperty", managedBeanProperty); // NOI18N

        FileObject tableTemplate = FileUtil.getConfigRoot().getFileObject(getTemplate());
        StringWriter w = new StringWriter();
        JSFPaletteUtilities.expandJSFTemplate(tableTemplate, params, encoding, w);
        stringBuffer.append(w.toString());

        if (surroundWithFView) {
            stringBuffer.append("</f:view>\n"); // NOI18N
        }
        return stringBuffer.toString();
    }

    private void enumerateEntityFields(Map<String, Object> params, CompilationController controller, TypeElement bean, String managedBeanProperty) {
        List<TemplateData> templateData = new ArrayList<TemplateData>();
        List<FieldDesc> fields = new ArrayList<FieldDesc>();
        if (bean != null) {
            ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
            JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport = null;
            for (ExecutableElement method : methods) {
                FieldDesc fd = new FieldDesc(controller, method, bean);
                if (fd.isValid()) {
                    int relationship = fd.getRelationship();
                    if (EntityClass.isId(controller, method, fd.isFieldAccess())) {
                        TypeMirror rType = method.getReturnType();
                        if (TypeKind.DECLARED == rType.getKind()) {
                            DeclaredType rTypeDeclared = (DeclaredType)rType;
                            TypeElement rTypeElement = (TypeElement) rTypeDeclared.asElement();
                            if (JpaControllerUtil.isEmbeddableClass(rTypeElement)) {
                                if (embeddedPkSupport == null) {
                                    embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
                                }
                                String propName = fd.getPropertyName();
                                for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(controller, bean)) {
                                    if (!embeddedPkSupport.isRedundantWithRelationshipField(controller, bean, pkMethod)) {
                                        String pkMethodName = pkMethod.getSimpleName().toString();
                                        fd = new FieldDesc(controller, pkMethod, bean);
                                        fd.setLabel(pkMethodName.substring(3));
                                        fd.setPropertyName(propName + "." + JpaControllerUtil.getPropNameFromMethod(pkMethodName));
                                        fields.add(fd);
                                    }
                                }
                            } else {
                                fields.add(fd);
                            }
                            continue;
                        }
                    } else if (fd.getDateTimeFormat().length() > 0) {
                        fields.add(fd);
                    } else if (relationship == JpaControllerUtil.REL_NONE || relationship == JpaControllerUtil.REL_TO_ONE) {
                        fields.add(fd);
                    }
                }
            }
        }

        processFields(params, templateData, controller, bean, fields, managedBeanProperty);

        params.put("entityDescriptors", templateData); // NOI18N
        params.put("item", ITEM_VAR); // NOI18N
        params.put("comment", Boolean.FALSE); // NOI18N
    }

    private void processFields(Map<String, Object> params, List<TemplateData> templateData,
            CompilationController controller, TypeElement bean, List<FieldDesc> fields, String managedBeanProperty) {
        for (FieldDesc fd : fields) {
            templateData.add(new TemplateData(fd, (isCollectionComponent() ? ITEM_VAR : managedBeanProperty)+"."));
        }
    }

    public final static class FieldDesc {

        private ExecutableElement method;
        private String methodName;
        private String propertyName;
        private String label;
        private Boolean fieldAccess = null;
        private Integer relationship = null;
        private TypeElement bean;
        private CompilationController controller;
        private String dateTimeFormat = null;

        private FieldDesc(CompilationController controller, ExecutableElement method, TypeElement bean) {
            this.controller = controller;
            this.method = method;
            this.bean = bean;
            this.methodName = method.getSimpleName().toString();
            this.label = this.methodName.substring(3);
            this.propertyName = JpaControllerUtil.getPropNameFromMethod(getMethodName());
        }

        public String getMethodName() {
            return methodName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        private boolean isFieldAccess() {
            if (fieldAccess == null) {
                fieldAccess = Boolean.valueOf(JpaControllerUtil.isFieldAccess(bean));
            }
            return fieldAccess.booleanValue();
        }

        public boolean isValid() {
            return getMethodName().startsWith("get"); // NOI18N
        }

        public int getRelationship() {
            if (relationship == null) {
                relationship = Integer.valueOf(JpaControllerUtil.isRelationship(controller, method, isFieldAccess()));
            }
            return relationship.intValue();
        }

        public String getDateTimeFormat() {
            if (dateTimeFormat == null) {
                dateTimeFormat = "";
                TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType(); // NOI18N
                if (controller.getTypes().isSameType(dateTypeMirror, method.getReturnType())) {
                    String temporal = EntityClass.getTemporal(controller, method, isFieldAccess());
                    if (temporal != null) {
                        dateTimeFormat = EntityClass.getDateTimeFormat(temporal);
                    }
                }
            }
            return dateTimeFormat;
        }

        private boolean isBlob() {
            Element fieldElement = isFieldAccess() ? JpaControllerUtil.guessField(controller, method) : method;
            return JpaControllerUtil.isAnnotatedWith(fieldElement, "javax.persistence.Lob"); // NOI18N
        }

        @Override
        public String toString() {
            return "Field[" + // NOI18N
                    "methodName="+getMethodName()+ // NOI18N
                    ",propertyName="+getPropertyName()+ // NOI18N
                    ",label="+label+ // NOI18N
                    ",valid="+isValid()+ // NOI18N
                    ",field="+isFieldAccess()+ // NOI18N
                    ",relationship="+getRelationship()+ // NOI18N
                    ",datetime="+getDateTimeFormat()+ // NOI18N
                    "]"; // NOI18N
        }

        private boolean isRequired() {
            return !JpaControllerUtil.isFieldOptionalAndNullable(controller, method, isFieldAccess());
        }

    }

    public static final class TemplateData {
        private FieldDesc fd;
        private String prefix;

        private TemplateData(FieldDesc fd, String prefix) {
            this.fd = fd;
            this.prefix = prefix;
        }

        public String getLabel() {
            return fd.getLabel();
        }

        public String getName() {
            return prefix+fd.getPropertyName();
        }

        public String getDateTimeFormat() {
            return fd.getDateTimeFormat();
        }

        public boolean isBlob() {
            return fd.isBlob();
        }

        public boolean isRelationshipOne() {
            return fd.getRelationship() == JpaControllerUtil.REL_TO_ONE;
        }

        public boolean isRelationshipMany() {
            return fd.getRelationship() == JpaControllerUtil.REL_TO_MANY;
        }

        public String getId() {
            return fd.getPropertyName();
        }

        public boolean isRequired() {
            System.err.println("-"+fd);
            return fd.isRequired();
        }

        @Override
        public String toString() {
            return "TemplateData[fd="+fd+",prefix="+prefix+"]"; // NOI18N
        }

    }

}
