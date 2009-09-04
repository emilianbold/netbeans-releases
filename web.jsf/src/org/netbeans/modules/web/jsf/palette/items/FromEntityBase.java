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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
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
            mbc.setDialog(dlg, dd);
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
                String managedBean = mbc.getManagedBeanProperty();
                if (managedBean != null && managedBean.lastIndexOf(".") != -1) {
                    managedBean = managedBean.substring(0, managedBean.lastIndexOf("."));
                }
                String body = expandTemplate(targetComponent, !containsFView, FileEncodingQuery.getEncoding(fo),
                        mbc.getBeanClass(), managedBean, mbc.getManagedBeanProperty());
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
            Charset encoding, final String entityClass, final String managedBean,
            final String managedBeanProperty) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append("<f:view>\n"); // NOI18N
        }
        FileObject targetJspFO = EntityClass.getFO(target);
        final Map<String, Object> params = createFieldParameters(targetJspFO, entityClass, 
                managedBean, managedBeanProperty, isCollectionComponent(), false);

        FileObject tableTemplate = FileUtil.getConfigRoot().getFileObject(getTemplate());
        StringWriter w = new StringWriter();
        JSFPaletteUtilities.expandJSFTemplate(tableTemplate, params, encoding, w);
        stringBuffer.append(w.toString());

        if (surroundWithFView) {
            stringBuffer.append("</f:view>\n"); // NOI18N
        }
        return stringBuffer.toString();
    }

    public static Map<String, Object> createFieldParameters(FileObject targetJspFO, final String entityClass,
            final String managedBean, final String managedBeanProperty, final boolean collectionComponent,
            final boolean initValueGetters) throws IOException {
        final Map<String, Object> params = new HashMap<String, Object>();
        JavaSource javaSource = JavaSource.create(EntityClass.createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                enumerateEntityFields(params, controller, typeElement, managedBeanProperty, collectionComponent, initValueGetters);
            }
        }, true);
        params.put("managedBean", managedBean); // NOI18N
        params.put("managedBeanProperty", managedBeanProperty); // NOI18N
        String entityName = entityClass;
        if (entityName.lastIndexOf(".") != -1) {
            entityName = entityName.substring(entityClass.lastIndexOf(".")+1);
        }
        params.put("entityName", entityName); // NOI18N
        return params;
    }

    private static void enumerateEntityFields(Map<String, Object> params, CompilationController controller, 
            TypeElement bean, String managedBeanProperty, boolean collectionComponent, boolean initValueGetters) {
        List<TemplateData> templateData = new ArrayList<TemplateData>();
        List<FieldDesc> fields = new ArrayList<FieldDesc>();
        if (bean != null) {
            ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
            JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport = null;
            for (ExecutableElement method : methods) {
                FieldDesc fd = new FieldDesc(controller, method, bean, initValueGetters);
                if (fd.isValid()) {
                    int relationship = fd.getRelationship();
                    if (EntityClass.isId(controller, method, fd.isFieldAccess())) {
                        fd.setPrimaryKey();
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

        processFields(params, templateData, controller, bean, fields, managedBeanProperty, collectionComponent);

        params.put("entityDescriptors", templateData); // NOI18N
        params.put("item", ITEM_VAR); // NOI18N
        params.put("comment", Boolean.FALSE); // NOI18N
    }

    private static ExecutableElement findPrimaryKeyGetter(CompilationController controller, TypeElement bean) {
        ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
        for (ExecutableElement method : methods) {
            FieldDesc fd = new FieldDesc(controller, method, bean, false);
            if (fd.isValid()) {
                if (EntityClass.isId(controller, method, fd.isFieldAccess())) {
                    return method;
                }
            }
        }
        return null;
    }

    private static void processFields(Map<String, Object> params, List<TemplateData> templateData,
            CompilationController controller, TypeElement bean, List<FieldDesc> fields, String managedBeanProperty,
            boolean collectionComponent) {
        for (FieldDesc fd : fields) {
            templateData.add(new TemplateData(fd, (collectionComponent ? ITEM_VAR : managedBeanProperty)+"."));
        }
    }

    public static void createParamsForConverterTemplate(final Map<String, Object> params, final FileObject targetJspFO,
            final String entityClass) throws IOException {
        JavaSource javaSource = JavaSource.create(EntityClass.createClasspathInfo(targetJspFO));
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                createParamsForConverterTemplate(params, controller, typeElement);
            }
        }, true);
    }

    private static final String INDENT = "            "; // TODO: jsut reformat generated code
    
    private static void createParamsForConverterTemplate(Map<String, Object> params, CompilationController controller, 
            TypeElement bean) throws IOException {
        // primary key type:
        ExecutableElement primaryGetter = findPrimaryKeyGetter(controller, bean);
        TypeMirror idType = primaryGetter.getReturnType();
        StringBuffer key = new StringBuffer();
        StringBuffer stringKey = new StringBuffer();
        String keyType;
        String keyTypeFQN;
        if (TypeKind.DECLARED == idType.getKind()) {
            DeclaredType declaredType = (DeclaredType) idType;
            TypeElement idClass = (TypeElement) declaredType.asElement();
            boolean embeddable = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
            keyType = idClass.getSimpleName().toString();
            keyTypeFQN = idClass.getQualifiedName().toString();
            if (embeddable) {
                params.put("keyEmbedded", Boolean.TRUE);
                int index = 0;
                for (ExecutableElement method : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                    if (method.getSimpleName().toString().startsWith("set")) {
                        addParam(key, stringKey, method.getSimpleName().toString(), index, 
                                keyType, keyTypeFQN, method.getParameters().get(0).asType());
                        index++;
                    }
                }
            } else {
                params.put("keyEmbedded", Boolean.FALSE);
                addParam(key, stringKey, null, -1, keyType, keyTypeFQN, idType);
            }
        } else {
            params.put("keyEmbedded", Boolean.FALSE);
            //keyType = getCorrespondingType(idType);
            keyTypeFQN = keyType = idType.toString();
            addParam(key, stringKey, null, -1, keyType, keyTypeFQN, idType);
        }
        params.put("keyType", keyTypeFQN);
        if (key.toString().endsWith("\n")) {
            key.setLength(key.length()-1);
        }
        params.put("keyBody", key.toString());
        if (stringKey.toString().endsWith("\n")) {
            stringKey.setLength(stringKey.length()-1);
        }
        params.put("keyStringBody", stringKey.toString());
        params.put("keyGetter", primaryGetter.getSimpleName().toString());
    }

    private static void addParam(StringBuffer key, StringBuffer stringKey, String setter, 
            int index, String keyType, String keyTypeFQN, TypeMirror idType) {
        if (index == 0) {
            key.append(INDENT+"String values[] = value.split(SEPARATOR_ESCAPED);\n");
            key.append(INDENT+"key = new "+keyTypeFQN+"();\n");
        }
        if (index > 0) {
            stringKey.append(INDENT+"sb.append(SEPARATOR);\n");
        }

        // do conversion
        String conversion = getConversionFromString(idType, index, keyType);

        if (setter != null) {
            key.append(INDENT+"key."+setter+"("+conversion+");\n");
            stringKey.append(INDENT+"sb.append(value.g"+setter.substring(1)+"());\n");
        } else {
            key.append(INDENT+"key = "+conversion+";\n");
            stringKey.append(INDENT+"sb.append(value);\n");
        }
    }

    private static String getConversionFromString(TypeMirror idType, int index, String keyType) {
        String param = index == -1 ? "value" : "values["+index+"]";
        if (TypeKind.BOOLEAN == idType.getKind()) {
            return "Boolean.parseBoolean("+param+")";
        } else if (TypeKind.BYTE == idType.getKind()) {
            return "Byte.parseByte("+param+")";
        } else if (TypeKind.CHAR == idType.getKind()) {
            return param+".charAt(0)";
        } else if (TypeKind.DOUBLE == idType.getKind()) {
            return "Double.parseDouble("+param+")";
        } else if (TypeKind.FLOAT == idType.getKind()) {
            return "Float.parseFloat("+param+")";
        } else if (TypeKind.INT == idType.getKind()) {
            return "Integer.parseInteger("+param+")";
        } else if (TypeKind.LONG == idType.getKind()) {
            return "Long.parseLong("+param+")";
        } else if (TypeKind.SHORT == idType.getKind()) {
            return "Short.parseShort("+param+")";
        } else if (TypeKind.DECLARED == idType.getKind()) {
            if ("Boolean".equals(idType.toString()) || "java.lang.Boolean".equals(idType.toString())) {
                return "Boolean.valueOf("+param+")";
            } else if ("Byte".equals(idType.toString()) || "java.lang.Byte".equals(idType.toString())) {
                return "Byte.valueOf("+param+")";
            } else if ("Character".equals(idType.toString()) || "java.lang.Character".equals(idType.toString())) {
                return "new Character("+param+".charAt(0))";
            } else if ("Double".equals(idType.toString()) || "java.lang.Double".equals(idType.toString())) {
                return "Double.valueOf("+param+")";
            } else if ("Float".equals(idType.toString()) || "java.lang.Float".equals(idType.toString())) {
                return "Float.valueOf("+param+")";
            } else if ("Integer".equals(idType.toString()) || "java.lang.Integer".equals(idType.toString())) {
                return "Integer.valueOf("+param+")";
            } else if ("Long".equals(idType.toString()) || "java.lang.Long".equals(idType.toString())) {
                return "Long.valueOf("+param+")";
            } else if ("Short".equals(idType.toString()) || "java.lang.Short".equals(idType.toString())) {
                return "Short.valueOf("+param+")";
            }
        }
        return param;
    }

//    private static String getCorrespondingType(TypeMirror idType) {
//        if (TypeKind.BOOLEAN == idType.getKind()) {
//            return "boolean";
//        } else if (TypeKind.BYTE == idType.getKind()) {
//            return "byte";
//        } else if (TypeKind.CHAR == idType.getKind()) {
//            return "char";
//        } else if (TypeKind.DOUBLE == idType.getKind()) {
//            return "double";
//        } else if (TypeKind.FLOAT == idType.getKind()) {
//            return "float";
//        } else if (TypeKind.INT == idType.getKind()) {
//            return "int";
//        } else if (TypeKind.LONG == idType.getKind()) {
//            return "long";
//        } else if (TypeKind.SHORT == idType.getKind()) {
//            return "short";
//        } else {
//            return "UnknownType";
//        }
//    }

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
        private String valuesGetter = "fixme";
        private boolean primaryKey;

        private FieldDesc(CompilationController controller, ExecutableElement method, TypeElement bean, boolean enableValueGetters) {
            this(controller, method, bean);
            if (enableValueGetters) {
                valuesGetter = null;
            }
        }

        private FieldDesc(CompilationController controller, ExecutableElement method, TypeElement bean) {
            this.controller = controller;
            this.method = method;
            this.bean = bean;
            this.methodName = method.getSimpleName().toString();
            this.label = this.methodName.substring(3);
            this.propertyName = JpaControllerUtil.getPropNameFromMethod(getMethodName());
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey() {
            this.primaryKey = true;
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
                    ",valuesGetter="+getValuesGetter()+ // NOI18N
                    "]"; // NOI18N
        }

        private String getRelationClassName(CompilationController controller, ExecutableElement executableElement, boolean isFieldAccess) {
            TypeMirror passedReturnType = executableElement.getReturnType();
            if (TypeKind.DECLARED != passedReturnType.getKind() || !(passedReturnType instanceof DeclaredType)) {
                return null;
            }
            Types types = controller.getTypes();
            TypeMirror passedReturnTypeStripped = JpaControllerUtil.stripCollection((DeclaredType)passedReturnType, types);
            if (passedReturnTypeStripped == null) {
                return null;
            }
            TypeElement passedReturnTypeStrippedElement = (TypeElement) types.asElement(passedReturnTypeStripped);
            return passedReturnTypeStrippedElement.getSimpleName().toString();
        }

        public String getValuesGetter() {
            if (getRelationship() == JpaControllerUtil.REL_NONE) {
                return null;
            }
            if (valuesGetter == null) {
                String name = getRelationClassName(controller, method, isFieldAccess());
                if (name == null) {
                    valuesGetter = "";
                } else {
                    name = name.substring(0, 1).toLowerCase() + name.substring(1);
                    valuesGetter = name + "Controller." +
                        (getRelationship() == JpaControllerUtil.REL_TO_ONE ? "itemsAvailableSelectOne" : "itemsAvailableSelectMany");
                }
            }
            return valuesGetter;
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
            return fd.isRequired();
        }

        public String getValuesGetter() {
            return fd.getValuesGetter();
        }

        @Override
        public String toString() {
            return "TemplateData[fd="+fd+",prefix="+prefix+"]"; // NOI18N
        }

    }

}
