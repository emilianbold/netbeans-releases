/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.explorer.propertysheet.editors.ModifierEditor;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.IndentEngine;
import org.openide.util.Utilities;
import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.form.editors.CustomCodeEditor;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.forminfo.MenuBarContainer;
import org.netbeans.modules.form.forminfo.JMenuBarContainer;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Iterator;

/* TODO
   - Exception handling in guarded blocks - from FormSettings???, or as a property of formManager

   - BeanContext support
   - External Event Handlers
*/

/**
 * JavaCodeGenerator is the default code generator which produces a Java source
 * for the form.
 *
 * @author Ian Formanek
 */

class JavaCodeGenerator extends CodeGenerator {
    private static Object GEN_LOCK = new Object();

    protected static final String AUX_VARIABLE_MODIFIER =
        "JavaCodeGenerator_VariableModifier"; // NOI18N
    protected static final String AUX_SERIALIZE_TO =
        "JavaCodeGenerator_SerializeTo"; // NOI18N
    protected static final String AUX_CODE_GENERATION =
        "JavaCodeGenerator_CodeGeneration"; // NOI18N
    protected static final String AUX_CREATE_CODE_PRE =
        "JavaCodeGenerator_CreateCodePre"; // NOI18N
    protected static final String AUX_CREATE_CODE_POST =
        "JavaCodeGenerator_CreateCodePost"; // NOI18N
    protected static final String AUX_CREATE_CODE_CUSTOM =
        "JavaCodeGenerator_CreateCodeCustom"; // NOI18N
    protected static final String AUX_INIT_CODE_PRE =
        "JavaCodeGenerator_InitCodePre"; // NOI18N
    protected static final String AUX_INIT_CODE_POST =
        "JavaCodeGenerator_InitCodePost"; // NOI18N

    protected static final String SECTION_INIT_COMPONENTS =
        "initComponents"; // NOI18N
    protected static final String SECTION_VARIABLES =
        "variables"; // NOI18N
    protected static final String SECTION_EVENT_PREFIX =
        "event_"; // NOI18N

    public static final Integer VALUE_GENERATE_CODE = new Integer(0);
    public static final Integer VALUE_SERIALIZE = new Integer(1);

    private static final String INIT_COMPONENTS_HEADER =
        "private void initComponents() {\n"; // NOI18N
    private static final String INIT_COMPONENTS_FOOTER = "}\n"; // NOI18N
    private static final String VARIABLES_HEADER =
        FormEditor.getFormBundle().getString("MSG_VariablesBegin");
    private static final String VARIABLES_FOOTER =
        FormEditor.getFormBundle().getString("MSG_VariablesEnd");

    /** The prefix for event handler sections */
    private static final String EVT_SECTION_PREFIX = "event_"; // NOI18N

    private static final String oneIndent = "  "; // [PENDING - indentation engine] // NOI18N

    private FormManager2 formManager;
    private boolean initialized = false;
    private boolean errorInitializing = false;

    private JavaEditor.SimpleSection initComponentsSection;
    private JavaEditor.SimpleSection variablesSection;

    /** Creates new JavaCodeGenerator */

    public JavaCodeGenerator() {
    }

    public void initialize(FormManager2 formManager) {
        if (!initialized) {
            this.formManager = formManager;
            formManager.addFormListener(new JCGFormListener());
        }
        initialized = true;
        FormEditorSupport s = formManager.getFormEditorSupport();
        initComponentsSection = s.findSimpleSection(SECTION_INIT_COMPONENTS);
        variablesSection = s.findSimpleSection(SECTION_VARIABLES);

        if ((initComponentsSection == null) ||(variablesSection == null)) {
            System.out.println("ERROR: Cannot initialize guarded sections... code generation is disabled."); // NOI18N
            errorInitializing = true;
        }
    }

    /**
     * Alows the code generator to provide synthetic properties for specified
     * component which are specific to the code generation method.  E.g. a
     * JavaCodeGenerator will return variableName property, as it generates
     * global Java variable for every component
     * @param component The RADComponent for which the properties are to be obtained
     */

    public Node.Property[] getSyntheticProperties(final RADComponent component) {
        Node.Property variableProperty = new PropertySupport.ReadWrite(
            "variableName",
            String.class,
            FormEditor.getFormBundle().getString("MSG_JC_VariableName"),
            FormEditor.getFormBundle().getString("MSG_JC_VariableDesc"))
        {
            public void setValue(Object value) {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException();
                }
                String oldValue = component.getName();
                component.setName((String)value);
                component.getNodeReference().firePropertyChangeHelper(
                    "variableName", oldValue, value); // NOI18N
            }

            public Object getValue() {
                return component.getName();
            }
        };

        if (!component.getFormManager().getFormEditorSupport().supportsAdvancedFeatures()) {
            return new Node.Property[] { variableProperty };
        }
        else {
            Node.Property[] props = new Node.Property[] {
                variableProperty,
                new PropertySupport.ReadWrite(
                    "useDefaultModifiers",
                    Boolean.TYPE,
                    FormEditor.getFormBundle().getString("MSG_JC_UseDefaultMod"),
                    FormEditor.getFormBundle().getString("MSG_JC_UseDefaultModDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof Boolean)) {
                            throw new IllegalArgumentException();
                        }
                        boolean useDefaultModifiers =((Boolean)value).booleanValue();
                        if (useDefaultModifiers) {
                            component.setAuxValue(AUX_VARIABLE_MODIFIER, null);
                        } else {
                            component.setAuxValue(AUX_VARIABLE_MODIFIER, new Integer(FormEditor.getFormSettings().getVariablesModifier()));
                        }
                        regenerateVariables();
                        component.getNodeReference().notifyPropertiesChange();
                        component.getNodeReference().notifyPropertySetsChange();
                    }

                    public Object getValue() {
                        return new Boolean(component.getAuxValue(AUX_VARIABLE_MODIFIER) == null);
                    }

                },
                new PropertySupport.ReadWrite(
                    "modifiers",
                    Integer.class,
                    FormEditor.getFormBundle().getString("MSG_JC_VariableModifiers"),
                    FormEditor.getFormBundle().getString("MSG_JC_VariableModifiersDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof Integer)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_VARIABLE_MODIFIER, value);
                        regenerateVariables();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        return component.getAuxValue(AUX_VARIABLE_MODIFIER);
                    }

                    public boolean canWrite() {
                        return(component.getAuxValue(AUX_VARIABLE_MODIFIER) != null);
                    }

                    public PropertyEditor getPropertyEditor() {
                        return new ModifierEditor(Modifier.PUBLIC
                                                  | Modifier.PROTECTED
                                                  | Modifier.PRIVATE
                                                  | Modifier.STATIC
                                                  | Modifier.FINAL
                                                  | Modifier.TRANSIENT
                                                  | Modifier.VOLATILE);
                    }
                },
                new PropertySupport.ReadWrite(
                    "codeGeneration",
                    Integer.TYPE,
                    FormEditor.getFormBundle().getString("MSG_JC_CodeGeneration"),
                    FormEditor.getFormBundle().getString("MSG_JC_CodeGenerationDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof Integer)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_CODE_GENERATION, value);
                        if (value.equals(VALUE_SERIALIZE)) {
                            if (component.getAuxValue(AUX_SERIALIZE_TO) == null) {
                                component.setAuxValue(AUX_SERIALIZE_TO,
                                                      getDefaultSerializedName(component));
                            }
                        }
                        regenerateInitializer();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_CODE_GENERATION);
                        if (value == null) {
                            if (component.hasHiddenState()) {
                                value = VALUE_SERIALIZE;
                            } else {
                                value = VALUE_GENERATE_CODE;
                            }
                        }
                        return value;
                    }

                    public PropertyEditor getPropertyEditor() {
                        return new CodeGenerateEditor(component);
                    }
                },
                new CodePropertySupportRW(
                    "creationCodePre",
                    String.class,
                    FormEditor.getFormBundle().getString("MSG_JC_PreCreationCode"),
                    FormEditor.getFormBundle().getString("MSG_JC_PreCreationCodeDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_CREATE_CODE_PRE, value);
                        regenerateInitializer();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_CREATE_CODE_PRE);
                        if (value == null) {
                            value = ""; // NOI18N
                        }
                        return value;
                    }
                },
                new CodePropertySupportRW(
                    "creationCodePost",
                    String.class,
                    FormEditor.getFormBundle().getString("MSG_JC_PostCreationCode"),
                    FormEditor.getFormBundle().getString("MSG_JC_PostCreationCodeDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_CREATE_CODE_POST, value);
                        regenerateInitializer();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_CREATE_CODE_POST);
                        if (value == null) {
                            value = ""; // NOI18N
                        }
                        return value;
                    }
                },
                new CodePropertySupportRW(
                    "initCodePre",
                    String.class,
                    FormEditor.getFormBundle().getString("MSG_JC_PreInitCode"),
                    FormEditor.getFormBundle().getString("MSG_JC_PreInitCodeDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_INIT_CODE_PRE, value);
                        regenerateInitializer();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_INIT_CODE_PRE);
                        if (value == null) {
                            value = ""; // NOI18N
                        }
                        return value;
                    }
                },
                new CodePropertySupportRW(
                    "initCodePost",
                    String.class,
                    FormEditor.getFormBundle().getString("MSG_JC_PostInitCode"),
                    FormEditor.getFormBundle().getString("MSG_JC_PostInitCodeDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_INIT_CODE_POST, value);
                        regenerateInitializer();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_INIT_CODE_POST);
                        if (value == null) {
                            value = ""; // NOI18N
                        }
                        return value;
                    }
                },
                new PropertySupport.ReadWrite(
                    "serializeTo",
                    String.class,
                    FormEditor.getFormBundle().getString("MSG_JC_SerializeTo"),
                    FormEditor.getFormBundle().getString("MSG_JC_SerializeToDesc"))
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_SERIALIZE_TO, value);
                        regenerateInitializer();
                        component.getNodeReference().notifyPropertiesChange();
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_SERIALIZE_TO);
                        if (value == null) {
                            value = getDefaultSerializedName(component);
                        }
                        return value;
                    }
                }
            };

            Integer generationType =(Integer) component.getAuxValue(AUX_CODE_GENERATION);
            if ((generationType == null) ||(generationType.equals(VALUE_GENERATE_CODE))) {
                Node.Property[] moreProps = new Node.Property[props.length + 1];
                for (int i=0, n=props.length; i<n; i++) {
                    moreProps [i] = props [i];
                }
                moreProps [moreProps.length -1] =
                    new CodePropertySupportRW(
                        "creationCodeCustom",
                        String.class,
                        FormEditor.getFormBundle().getString("MSG_JC_CustomCreationCode"),
                        FormEditor.getFormBundle().getString("MSG_JC_CustomCreationCodeDesc"))
                    {
                        public void setValue(Object value) {
                            if (!(value instanceof String)) {
                                throw new IllegalArgumentException();
                            }
                            component.setAuxValue(AUX_CREATE_CODE_CUSTOM, value);
                            regenerateInitializer();
                            component.getNodeReference().notifyPropertiesChange();
                        }

                        public Object getValue() {
                            Object value = component.getAuxValue(AUX_CREATE_CODE_CUSTOM);
                            if (value == null) {
                                value = ""; // NOI18N
                            }
                            return value;
                        }
                        public boolean canWrite() {
                            Integer genType =(Integer)component.getAuxValue(AUX_CODE_GENERATION);
                            return((genType == null) ||(genType.equals(VALUE_GENERATE_CODE)));
                        }
                    };
                return moreProps;
            } else {
                return props;
            }
        }
    }

    //
    // Private Methods
    //

    private String getDefaultSerializedName(RADComponent component) {
        return component.getFormManager().getFormObject().getName()
            + "_" + component.getName(); // NOI18N
    }

    private void regenerateInitializer() {
        if (errorInitializing)
            return;

        try {
            IndentEngine engine = IndentEngine.find("text/x-java"); // NOI18N
            AWTIndentStringWriter initCodeBuffer = new AWTIndentStringWriter();
            Writer initCodeWriter = engine.createWriter(
                formManager.getFormEditorSupport().getDocument(),
                initComponentsSection.getBegin().getOffset(), initCodeBuffer);

            initCodeWriter.write(INIT_COMPONENTS_HEADER);
            RADForm form = formManager.getRADForm();
            RADComponent top = form.getTopLevelComponent();
            RADComponent[] nonVisualComponents = formManager.getNonVisualComponents();
            for (int i = 0; i < nonVisualComponents.length; i++) {
                addCreateCode(nonVisualComponents[i], initCodeWriter);
            }
            addCreateCode(top, initCodeWriter);

            for (int i = 0; i < nonVisualComponents.length; i++) {
                addInitCode(nonVisualComponents[i], initCodeWriter, initCodeBuffer, 0);
            }
            addInitCode(top, initCodeWriter, initCodeBuffer, 0);

            // for visual forms append sizing text
            if (form.getTopLevelComponent() instanceof RADVisualFormContainer) {
                RADVisualFormContainer visualForm =
                    (RADVisualFormContainer)form.getTopLevelComponent();

                // 1. generate code for menu, if the form is menu bar container and has
                // a menu associated

                String menuComp = visualForm.getFormMenu();
                if (menuComp != null) {
                    String menuText = null;
                    if (visualForm.getFormInfo() instanceof JMenuBarContainer) {
                        menuText = "setJMenuBar("; // NOI18N
                    } else if (visualForm.getFormInfo() instanceof MenuBarContainer) {
                        menuText = "setMenuBar("; // NOI18N
                    }
                    if (menuText != null) {
                        menuText = menuText + menuComp + ");\n\n"; // NOI18N
                        initCodeWriter.write(menuText);
                    }
                }

                // 2. generate size code according to form size policy

                int formPolicy = visualForm.getFormSizePolicy();
                boolean genSize = visualForm.getGenerateSize();
                boolean genPosition = visualForm.getGeneratePosition();
                boolean genCenter = visualForm.getGenerateCenter();
                Dimension formSize = visualForm.getFormSize();
                Point formPosition = visualForm.getFormPosition();

                String sizeText = ""; // NOI18N

                switch (formPolicy) {
                    case RADVisualFormContainer.GEN_PACK:
                        sizeText = "pack();\n"; // NOI18N
                        break;
                    case RADVisualFormContainer.GEN_BOUNDS:
                        if (genCenter) {
                            StringBuffer sizeBuffer = new StringBuffer();
                            if (genSize) {
                                sizeBuffer.append("pack();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension dialogSize = getSize();\n"); // NOI18N
                                sizeBuffer.append("setSize(new java.awt.Dimension("+formSize.width + ", " + formSize.height + "));\n"); // NOI18N
                                sizeBuffer.append("setLocation((screenSize.width-"+formSize.width+")/2,(screenSize.height-"+formSize.height+")/2);\n"); // NOI18N
                            } else {
                                sizeBuffer.append("pack();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension dialogSize = getSize();\n"); // NOI18N
                                sizeBuffer.append("setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);\n"); // NOI18N
                            }

                            sizeText = sizeBuffer.toString();

                        } else if (genPosition && genSize) { // both size and position
                            sizeText = "setBounds("+formPosition.x + ", " + formPosition.y +", " + formSize.width + ", " + formSize.height + ");\n"; // NOI18N
                        } else if (genPosition) { // position only
                            sizeText = "setLocation(new java.awt.Point("+formPosition.x + ", " + formPosition.y + "));\n"; // NOI18N
                        } else if (genSize) { // size only
                            sizeText = "setSize(new java.awt.Dimension("+formSize.width + ", " + formSize.height + "));\n"; // NOI18N
                        }
                        break;
                }

                initCodeWriter.write(sizeText);
            }

            initCodeWriter.write(INIT_COMPONENTS_FOOTER);
            initCodeWriter.close();
            // set the text into the guarded block
            synchronized(GEN_LOCK) {
                String originalText = initComponentsSection.getText();
                String newText = initCodeBuffer.toString();
                if (!newText.equals(originalText)) {
                    initComponentsSection.setText(newText);
                    clearUndo();
                }
            }
        } catch (IOException e) {
            throw new InternalError(); // cannot happen
        }
    }

    private void regenerateVariables() {
        if (errorInitializing)
            return;

        IndentEngine engine = IndentEngine.find("text/x-java"); // NOI18N
        StringWriter variablesBuffer = new StringWriter();
        Writer variablesWriter = engine.createWriter(
            formManager.getFormEditorSupport().getDocument(),
            variablesSection.getBegin().getOffset(),
            variablesBuffer);

        try {
            variablesWriter.write(VARIABLES_HEADER);
            variablesWriter.write("\n"); // NOI18N
            RADForm form = formManager.getRADForm();

            addVariables(formManager.getNonVisualsContainer(), variablesWriter);
            addVariables(form.getFormContainer(), variablesWriter);

            variablesWriter.write(VARIABLES_FOOTER);
            variablesWriter.write("\n"); // NOI18N
            variablesWriter.close();
            synchronized(GEN_LOCK) {
                String originalText = variablesSection.getText();
                String newText = variablesBuffer.toString();
                if (!newText.equals(originalText)) {
                    variablesSection.setText(newText);
                    clearUndo();
                }
            }
        } catch (IOException e) {
            throw new InternalError(); // cannot happen
        }
    }

    private void addCreateCode(RADComponent comp, Writer initCodeWriter)
        throws IOException
    {
        if (!(comp instanceof FormContainer)) {
            generateComponentCreate(comp, initCodeWriter);
        }
        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                addCreateCode(children[i], initCodeWriter);
            }
        }
    }

    private void addInitCode(RADComponent comp,
                             Writer initCodeWriter,
                             AWTIndentStringWriter initCodeBuffer,
                             int level) throws IOException {
        generateComponentInit(comp, initCodeWriter);
        generateComponentEvents(comp, initCodeWriter);

        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                if ((comp instanceof FormContainer)
                    ||(!FormEditor.getFormSettings().getIndentAWTHierarchy())) {
                    initCodeWriter.write("\n"); // NOI18N
                    // do not indent for top-level children
                    addInitCode(children[i], initCodeWriter, initCodeBuffer, level);

                    if (comp instanceof RADVisualContainer) {
                        if (comp instanceof RADVisualFormContainer) {
                            // no indent for top-level container
                            initCodeWriter.write("\n"); // NOI18N
                            generateComponentAddCode(children[i],(RADVisualContainer)comp, initCodeWriter);
                        } else {
                            generateComponentAddCode(children[i],(RADVisualContainer)comp, initCodeWriter);
                        }
                    } // [PENDING - adding to non-visual containers]

                } else {
                    initCodeWriter.flush();
                    initCodeBuffer.setIndentLevel(level + 1, oneIndent);
                    initCodeWriter.write("\n"); // NOI18N
                    addInitCode(children[i], initCodeWriter, initCodeBuffer, level + 1);

                    if (comp instanceof RADVisualContainer) {
                        if (comp instanceof RADVisualFormContainer) {
                            // no indent for top-level container
                            initCodeWriter.write("\n"); // NOI18N
                            generateComponentAddCode(children[i],(RADVisualContainer)comp, initCodeWriter);
                        } else {
                            generateComponentAddCode(children[i],(RADVisualContainer)comp, initCodeWriter);
                        }
                    } else if (comp instanceof RADMenuComponent) {
                        generateMenuAddCode(children[i],(RADMenuComponent)comp, initCodeWriter);
                    } // [PENDING - adding to non-visual containers]

                    initCodeWriter.flush();
                    initCodeBuffer.setIndentLevel(level, oneIndent);
                }
            }
        }
        initCodeWriter.write("\n"); // NOI18N
    }

    private void generateComponentCreate(RADComponent comp,
                                         Writer initCodeWriter) throws IOException {
        if ((comp instanceof RADMenuItemComponent) &&(((RADMenuItemComponent)comp).getMenuItemType() == RADMenuItemComponent.T_SEPARATOR)) {
            // do noty generate init for AWT separator as it is not a real component
            return;
        }

        String preCode =(String) comp.getAuxValue(AUX_CREATE_CODE_PRE);
        String postCode =(String) comp.getAuxValue(AUX_CREATE_CODE_POST);
        String customCreateCode =(String) comp.getAuxValue(AUX_CREATE_CODE_CUSTOM);
        if ((preCode != null) &&(!preCode.equals(""))) { // NOI18N
            initCodeWriter.write(preCode);
            initCodeWriter.write("\n"); // NOI18N
        }
        Integer generationType =(Integer)comp.getAuxValue(AUX_CODE_GENERATION);
        if (comp.hasHiddenState() ||((generationType != null) &&(generationType.equals(VALUE_SERIALIZE)))) {
            String serializeTo =(String)comp.getAuxValue(AUX_SERIALIZE_TO);
            if (serializeTo == null) {
                serializeTo = getDefaultSerializedName(comp);
                comp.setAuxValue(AUX_SERIALIZE_TO, serializeTo);
            }
            initCodeWriter.write("try {\n"); // NOI18N
            initCodeWriter.write(comp.getName());
            initCodeWriter.write(" =("); // NOI18N
            initCodeWriter.write(comp.getBeanClass().getName());
            initCodeWriter.write(")java.beans.Beans.instantiate(getClass().getClassLoader(), \""); // NOI18N
            // write package name
            String packageName = formManager.getFormObject().getPrimaryFile().getParent().getPackageName('.');
            if (!"".equals(packageName)) { // NOI18N
                initCodeWriter.write(packageName + "."); // NOI18N
            }
            initCodeWriter.write(serializeTo);
            initCodeWriter.write("\");\n"); // NOI18N
            initCodeWriter.write("} catch (ClassNotFoundException e) {\n"); // NOI18N
            initCodeWriter.write("e.printStackTrace();\n"); // NOI18N
            initCodeWriter.write("} catch (java.io.IOException e) {\n"); // NOI18N
            initCodeWriter.write("e.printStackTrace();\n"); // NOI18N
            initCodeWriter.write("}\n"); // NOI18N
        } else {
            Class[] exceptions=null;
            try {
                exceptions = comp.getBeanClass().getConstructor(new Class [0]).getExceptionTypes();
            } catch (NoSuchMethodException e) {
                //PENDING  -announce this !!
                e.printStackTrace();
            }
            if (exceptions.length > 0) {
                initCodeWriter.write("try {\n"); // NOI18N
            }

            if ((customCreateCode != null) &&(!customCreateCode.equals(""))) { // NOI18N
                initCodeWriter.write(comp.getName() + " = "); // NOI18N
                initCodeWriter.write(customCreateCode);
            } else {
                initCodeWriter.write(comp.getName() + " = "); // NOI18N
                initCodeWriter.write("new "); // NOI18N
                initCodeWriter.write(comp.getBeanClass().getName() + "();"); // NOI18N
            }
            initCodeWriter.write("\n"); // NOI18N

            int varCount = 1;
            // add the catch for all checked exceptions
            for (int j = 0; j < exceptions.length; j++) {
                initCodeWriter.write("} catch ("); // NOI18N
                initCodeWriter.write(exceptions[j].getName());
                initCodeWriter.write(" "); // NOI18N
                String excName = "e"+varCount; // NOI18N
                varCount++;
                while (formManager.getVariablesPool().isReserved(excName)) {
                    excName = "e"+varCount; // NOI18N
                    varCount++;
                }
                initCodeWriter.write(excName);
                initCodeWriter.write(") {\n"); // NOI18N
                initCodeWriter.write(excName);
                initCodeWriter.write(".printStackTrace();\n"); // NOI18N
                if (j == exceptions.length - 1) {
                    initCodeWriter.write("}\n"); // NOI18N
                }
            }
        }
        if ((postCode != null) &&(!postCode.equals(""))) { // NOI18N
            initCodeWriter.write(postCode);
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generateComponentInit(RADComponent comp, Writer initCodeWriter) throws IOException {
        if (comp instanceof RADVisualContainer) {
            DesignLayout dl =((RADVisualContainer)comp).getDesignLayout();
            String layoutInitCode = dl.generateInitCode((RADVisualContainer)comp);
            if (layoutInitCode != null) {
                // generate layout init code
                initCodeWriter.write(layoutInitCode);
            }
        }

        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        String preCode =(String) comp.getAuxValue(AUX_INIT_CODE_PRE);
        String postCode =(String) comp.getAuxValue(AUX_INIT_CODE_POST);
        if ((preCode != null) &&(!preCode.equals(""))) { // NOI18N
            initCodeWriter.write(preCode);
            initCodeWriter.write("\n"); // NOI18N
        }
        if ((genType == null) || VALUE_GENERATE_CODE.equals(genType)) {
            // not serialized ==>> save
            RADComponent.RADProperty[] props = comp.getAllProperties();
            for (int i = 0; i < props.length; i++) {
                if (props[i].isChanged() ||(props[i].getPreCode() != null) ||(props[i].getPostCode() != null)) {
                    /*      if (desc instanceof IndexedPropertyDescriptor) { // [PENDING]
                            generateIndexedPropertySetter(comp, rprop, initCodeWriter);
                            } else { */
                    generatePropertySetter(comp, props[i], initCodeWriter);
                    //      }
                }
            }
        }
        if ((postCode != null) &&(!postCode.equals(""))) { // NOI18N
            initCodeWriter.write(postCode);
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generateComponentAddCode(RADComponent comp, RADVisualContainer container, Writer initCodeWriter) throws IOException {
        DesignLayout dl = container.getDesignLayout();
        initCodeWriter.write(dl.generateComponentCode(container,(RADVisualComponent)comp));
    }

    private void generateMenuAddCode(RADComponent comp, RADMenuComponent container, Writer initCodeWriter) throws IOException {
        if ((comp instanceof RADMenuItemComponent) &&(((RADMenuItemComponent)comp).getMenuItemType() == RADMenuItemComponent.T_SEPARATOR)) {
            // treat AWT Separator specially - it is not a component
            initCodeWriter.write(container.getName());
            initCodeWriter.write(".addSeparator();"); // NOI18N
        } else {
            initCodeWriter.write(container.getName());
            initCodeWriter.write(".add("); // NOI18N
            initCodeWriter.write(comp.getName());
            initCodeWriter.write(");"); // NOI18N
        }
    }

    /*  private void generateIndexedPropertySetter(RADComponent comp, PropertyDescriptor desc, StringBuffer text, String indent) {
        System.out.println("generateIndexedPropertySetter: NotImplemented...(Property: "+desc.getName()+", Value: "+value+")"); // [PENDING]
        }
    */

    private synchronized void generatePropertySetter(RADComponent comp, RADComponent.RADProperty prop, Writer initCodeWriter) throws IOException {
        String javaInitializationString = null;
        PropertyDescriptor desc = prop.getPropertyDescriptor();
        Method writeMethod = desc.getWriteMethod();
        if (prop.isChanged()) {
            PropertyEditor ed = null;
            try {
                if (prop.getCurrentEditor() instanceof RADConnectionPropertyEditor) {
                    ed = new RADConnectionPropertyEditor(prop.getPropertyDescriptor().getPropertyType());
                } else {
                    ed =(PropertyEditor)prop.getCurrentEditor().getClass().newInstance();
                }

                if (ed != null) { // cannot generate without property editor

                    Object value = prop.getValue();

                    // process FormAwareEditors
                    if (ed instanceof FormAwareEditor) {
                        ((FormAwareEditor)ed).setRADComponent(comp, prop);
                    }
                    if (ed instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
                        ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)ed).attach(new org.openide.nodes.Node[] { comp.getNodeReference() });
                    }

                    // null values are generated separately, as most property editors cannot cope with nulls
                    if (value != null) {
                        ed.setValue(value);

                        javaInitializationString = ed.getJavaInitializationString();
                        if ("???".equals(javaInitializationString)) { // NOI18N
                            javaInitializationString = null; // cannot generate code for this property
                        }
                    } else {
                        // null values are generated separately, as most property editors cannot cope with nulls
                        javaInitializationString = "null"; // NOI18N
                    }
                }
            } catch (Exception e) {
                if (System.getProperty("netbeans.debug.exceptions") != null) e.printStackTrace();
                // cannot generate code for this property without the property editor
            }
        }

        String preCode = prop.getPreCode();
        String postCode = prop.getPostCode();

        // 1. pre initialization code
        if (preCode != null) {
            initCodeWriter.write(preCode);
            if (!preCode.endsWith("\n")) initCodeWriter.write("\n"); // NOI18N
        }

        // 2. property setter code
        if (javaInitializationString != null) {
            // if the setter throws checked exceptions, we must generate try/catch block around it.
            Class[] exceptions = writeMethod.getExceptionTypes();
            if (exceptions.length > 0) {
                initCodeWriter.write("try {\n"); // NOI18N
            }

            initCodeWriter.write(getVariableGenString(comp, false));
            initCodeWriter.write(writeMethod.getName());
            initCodeWriter.write("("); // NOI18N

            initCodeWriter.write(javaInitializationString);

            initCodeWriter.write(");\n"); // NOI18N

            int varCount = 1;
            // add the catch for all checked exceptions
            for (int j = 0; j < exceptions.length; j++) {
                initCodeWriter.write("} catch ("); // NOI18N
                initCodeWriter.write(exceptions[j].getName());
                initCodeWriter.write(" "); // NOI18N
                String excName = "e"+varCount; // NOI18N
                varCount++;
                while (formManager.getVariablesPool().isReserved(excName)) {
                    excName = "e"+varCount; // NOI18N
                    varCount++;
                }
                initCodeWriter.write(excName);
                initCodeWriter.write(") {\n"); // NOI18N
                initCodeWriter.write(excName);
                initCodeWriter.write(".printStackTrace();\n"); // NOI18N
                if (j == exceptions.length - 1) {
                    initCodeWriter.write("}\n"); // NOI18N
                }
            }
        }

        // 3. post initialization code
        if (postCode != null) {
            initCodeWriter.write(postCode);
            if (!postCode.endsWith("\n")) initCodeWriter.write("\n"); // NOI18N
        }
    }


    private void generateComponentEvents(RADComponent comp, Writer initCodeWriter) throws IOException {
        String variablePrefix = getVariableGenString(comp, false);

        EventsList.EventSet[] eventSets = comp.getEventsList().getEventSets();

        // go through the event sets - we generate the innerclass for whole
        // EventSet at once
        for (int i = 0; i < eventSets.length; i++) {
            EventsList.Event events[] = eventSets[i].getEvents();
            EventSetDescriptor eventSetDesc = eventSets[i].getEventSetDescriptor();

            // try to find adpater to use instead of the listener
            Class classToGenerate = BeanSupport.getAdapterForListener(
                eventSetDesc.getListenerType());
            boolean adapterUsed = true;
            if (classToGenerate == null) { // if not found, we must use the listener
                classToGenerate = eventSetDesc.getListenerType();
                adapterUsed = false;
            }

            // test if we should generate the addListener for this eventSet
            boolean shouldGenerate = false;
            boolean[] shouldGenerateEvent = new boolean[events.length];
            for (int j = 0; j < events.length; j++) {
                if (events[j].getHandlers().size() > 0) {
                    shouldGenerate = true;
                    shouldGenerateEvent[j] = true;
                    continue;
                }
                else shouldGenerateEvent[j] = false;
            }
            // if we should generate inner class for this listener and we do not
            // use adapter, we must generate all methods!!!
            if (shouldGenerate && !adapterUsed)
                for (int j = 0; j < events.length; j++)
                    shouldGenerateEvent[j] = true;

            if (shouldGenerate) {
                Method eventAddMethod = eventSetDesc.getAddListenerMethod();

                boolean unicastEvent = false;
                if ((eventAddMethod.getExceptionTypes().length == 1) &&
                    (java.util.TooManyListenersException.class.equals(eventAddMethod.getExceptionTypes()[0])))
                    unicastEvent = true;

                if (unicastEvent) {
                    initCodeWriter.write("try {\n"); // NOI18N
                }

                // beginning of the addXXXListener
                initCodeWriter.write(variablePrefix);
                initCodeWriter.write(eventSetDesc.getAddListenerMethod().getName());
                initCodeWriter.write("(new "); // NOI18N
                initCodeWriter.write(classToGenerate.getName() + "() {\n"); // NOI18N

                // listener innerclass' methods - indented one more indent to the right
                for (int j = 0; j < events.length; j++) {
                    if (!shouldGenerateEvent[j])
                        continue;

                    Method evtMethod = events[j].getListenerMethod();
                    Class[] evtParams = evtMethod.getParameterTypes();
                    String[] varNames;

                    if ((evtParams.length == 1) &&
                        (java.util.EventObject.class.isAssignableFrom(evtParams[0])))
                        varNames = new String[] {
                            FormEditor.getFormSettings().getEventVariableName()
                        };
                    else {
                        varNames = new String[evtParams.length];
                        for (int k = 0; k < evtParams.length; k ++)
                            varNames[k] = "param" + k; // NOI18N
                    }

                    // generate the listener's method
                    initCodeWriter.write(getMethodHeaderText(evtMethod, varNames));
                    initCodeWriter.write(" {\n"); // NOI18N

                    if (events[j].getHandlers().size() > 0) {
                        // generate the call to the handlers
                        for (Iterator it = events[j].getHandlers().iterator(); it.hasNext();) {
                            EventsManager.EventHandler handler =(EventsManager.EventHandler) it.next();
                            initCodeWriter.write(handler.getName());
                            initCodeWriter.write("("); // NOI18N
                            for (int k = 0; k < varNames.length; k++) {
                                initCodeWriter.write(varNames[k]);
                                if (k != varNames.length - 1)
                                    initCodeWriter.write(", "); // NOI18N
                            }
                            initCodeWriter.write(");"); // NOI18N
                            if (it.hasNext())
                                initCodeWriter.write("\n"); // NOI18N
                        }
                    }
                    initCodeWriter.write("\n"); // NOI18N
                    initCodeWriter.write("}\n"); // NOI18N
                }

                // end of the innerclass
                initCodeWriter.write("}\n"); // NOI18N
                initCodeWriter.write(");\n"); // NOI18N


                // if the event is unicast, generate the catch for TooManyListenersException
                if (unicastEvent) {
                    initCodeWriter.write("} catch (java.util.TooManyListenersException "); // NOI18N
                    String varName = "e"; // NOI18N
                    if (formManager.getVariablesPool().isReserved(varName)) {
                        int varCount = 1;
                        varName = "e1"; // NOI18N
                        while (true) {
                            if (!(formManager.getVariablesPool().isReserved(varName)))
                                break;
                            varName = "e"+varCount; // NOI18N
                        }
                    }

                    initCodeWriter.write(varName);
                    initCodeWriter.write(") {\n"); // NOI18N
                    initCodeWriter.write(varName);
                    initCodeWriter.write(".printStackTrace();\n"); // NOI18N
                    initCodeWriter.write("}\n"); // NOI18N
                }
            }
        }
    }

    private void addVariables(ComponentContainer cont, Writer variablesWriter) throws IOException {
        RADComponent[] children = cont.getSubBeans();

        for (int i = 0; i < children.length; i++) {
            if ((children[i] instanceof RADMenuItemComponent) &&(((RADMenuItemComponent)children[i]).getMenuItemType() == RADMenuItemComponent.T_SEPARATOR)) {
                // treat AWT Separator specially - it is not a component
                continue;
            }
            Integer m =(Integer) children[i].getAuxValue(AUX_VARIABLE_MODIFIER);
            int modifiers =(m != null) ? m.intValue() : FormEditor.getFormSettings().getVariablesModifier();
            variablesWriter.write(java.lang.reflect.Modifier.toString(modifiers));
            variablesWriter.write(" "); // NOI18N
            variablesWriter.write(children[i].getBeanClass().getName());
            variablesWriter.write(" "); // NOI18N
            variablesWriter.write(children[i].getName());
            variablesWriter.write(";\n"); // NOI18N
            if (children[i] instanceof ComponentContainer) {
                addVariables((ComponentContainer)children[i], variablesWriter);
            }
        }
    }

    private static String getVariableGenString(RADComponent comp, boolean containerCode) {
        if (comp instanceof FormContainer) {
            if (containerCode) {
                return(((FormContainer)comp).getFormInfo().getContainerGenName());
            } else {
                return ""; // NOI18N
            }
        } else {
            return comp.getName() + "."; // NOI18N
        }
    }


    // -----------------------------------------------------------------------------
    // Event handlers

    /** Generates the specified event handler, if it does not exist yet.
     * @param handlerName The name of the event handler
     * @param paramList the list of event handler parameter types
     * @param bodyText the body text of the event handler or null for default(empty) one
     * @return true if the event handler have not existed yet and was creaated, false otherwise
     */
    public boolean generateEventHandler(String handlerName, String[] paramTypes, String bodyText) {
        if (errorInitializing) return false;
        if (getEventHandlerSection(handlerName) != null)
            return false;

        synchronized(GEN_LOCK) {
            FormEditorSupport s = formManager.getFormEditorSupport();

            try {
                JavaEditor.InteriorSection sec = s.createInteriorSectionAfter(initComponentsSection, getEventSectionName(handlerName));
                sec.setHeader(getEventHandlerHeader(handlerName, paramTypes));
                sec.setBody(getEventHandlerBody(handlerName, paramTypes, bodyText));
                sec.setBottom(getEventHandlerFooter(handlerName, paramTypes));
            } catch (javax.swing.text.BadLocationException e) {
            }
            clearUndo();
        }

        return true;
    }

    /** Changes the text of the specified event handler, if it already exists.
     * @param handlerName The name of the event handler
     * @param paramList the list of event handler parameter types
     * @param bodyText the new body text of the event handler or null for default(empty) one
     * @return true if the event handler existed and was modified, false otherwise
     */
    public boolean changeEventHandler(final String handlerName, final String[] paramTypes, final String bodyText) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec == null)
            return false;

        synchronized(GEN_LOCK) {
            sec.setHeader(getEventHandlerHeader(handlerName, paramTypes));
            sec.setBody(getEventHandlerBody(handlerName, paramTypes, bodyText));
            sec.setBottom(getEventHandlerFooter(handlerName, paramTypes));
            clearUndo();
        }
        return true;
    }

    /** Removes the specified event handler - removes the whole method together with the user code!
     * @param handlerName The name of the event handler
     */
    public boolean deleteEventHandler(String handlerName) {
        synchronized(GEN_LOCK) {
            JavaEditor.InteriorSection section = getEventHandlerSection(handlerName);
            if (section == null)
                return false;
            section.deleteSection();
            clearUndo();
        }

        return true;
    }

    private String getEventHandlerHeader(String handlerName, String[] paramTypes) {
        StringBuffer buf = new StringBuffer();

        // [IAN] following line contains a hack, where the first two spaces in the event handler header
        // is a quick workaround for the bug, where sections in JavaEditor do not use Indentation Engine
        // and thus the first line of event handlers was not indented correctly
        buf.append("  private void "); // NOI18N
        buf.append(handlerName);
        buf.append("("); // NOI18N

        // create variable names
        String[] varNames = new String [paramTypes.length];

        if (paramTypes.length == 1)
            varNames [0] = paramTypes [0] + " " + new FormLoaderSettings().getEventVariableName(); // NOI18N
        else
            for (int i = 0; i < paramTypes.length; i ++)
                varNames [i] = paramTypes [0] + " param" + i; // NOI18N

        for (int i = 0; i < paramTypes.length; i++) {
            buf.append(varNames[i]);
            if (i != paramTypes.length - 1)
                buf.append(", "); // NOI18N
            else
                buf.append(") {\n"); // NOI18N
        }
        return buf.toString();
    }

    private String getEventHandlerBody(String handlerName, String[] paramTypes, String bodyText) {
        if (bodyText == null) {
            bodyText = getDefaultEventBody();
        } else {
            /*      bodyText = Utilities.replaceString(bodyText, "\n", "\n"+oneIndent);
                    bodyText = Utilities.replaceString(bodyText, "\t", oneIndent);
                    bodyText = oneIndent + oneIndent + bodyText; */ // [PENDING]
        }
        return bodyText;
    }

    private String getEventHandlerFooter(String handlerName, String[] paramTypes) {
        return "  }\n"; // NOI18N
    }

    private String getDefaultEventBody() {
        return FormEditor.getFormBundle().getString("MSG_EventHandlerBody");
    }

    /** Renames the specified event handler to the given new name.
     * @param oldHandlerName The old name of the event handler
     * @param newHandlerName The new name of the event handler
     */
    public boolean renameEventHandler(String oldHandlerName, String newHandlerName, String[] paramTypes) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(oldHandlerName);
        if (sec == null) {
            return false;
        }

        synchronized(GEN_LOCK) {
            sec.setHeader(getEventHandlerHeader(newHandlerName, paramTypes));
            sec.setBottom(getEventHandlerFooter(newHandlerName, paramTypes));
            try {
                sec.setName(getEventSectionName(newHandlerName));
                clearUndo();
            } catch (java.beans.PropertyVetoException e) {
                return false;
            }
        }

        return true;
    }

    /** Focuses the specified event handler in the editor. */
    public void gotoEventHandler(String handlerName) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null) {
            sec.openAt();
            formManager.getFormEditorSupport().gotoEditor();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Private methods

    /** Clears undo buffer after code generation */
    private void clearUndo() {
        formManager.getFormEditorSupport().getUndoManager().discardAllEdits();
    }

    // sections acquirement

    private JavaEditor.InteriorSection getEventHandlerSection(String eventName) {
        FormEditorSupport s = formManager.getFormEditorSupport();
        return s.findInteriorSection(getEventSectionName(eventName));
    }

    // other

    private String getEventSectionName(String handlerName) {
        return EVT_SECTION_PREFIX + handlerName;
    }

    /** A utility method for formatting method header text for specified
     * method, its name and parameter names.
     * @param m The method - its modifiers, return type, parameter types and
     *                       exceptions are used
     * @param paramNames An array of names of parameters - the length of this
     *            array MUST be the same as the actual number of method's parameters
     */
    private String getMethodHeaderText(Method m, String[] paramNames) {
        StringBuffer buf = new StringBuffer() ;
        buf.append("public "); // NOI18N
        buf.append(m.getReturnType().getName());
        buf.append(" "); // NOI18N
        buf.append(m.getName());
        buf.append("("); // NOI18N

        Class[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            buf.append(params[i].getName());
            buf.append(" "); // NOI18N
            buf.append(paramNames[i]);
            if (i != params.length - 1)
                buf.append(", "); // NOI18N
        }
        buf.append(")"); // NOI18N

        Class[] exceptions = m.getExceptionTypes();
        if (exceptions.length != 0) {
            buf.append("\n"); // NOI18N
            buf.append("throws "); // NOI18N
        }
        for (int i = 0; i < exceptions.length; i++) {
            buf.append(exceptions[i].getName());
            if (i != exceptions.length - 1)
                buf.append(", "); // NOI18N
        }

        return buf.toString();
    }

    //
    // {{{ JCGFormListener
    //

    private class JCGFormListener implements FormListener
    {
        /** Called when the form is succesfully loaded and fully initialized */

        public void formLoaded() {
            regenerateVariables();
            regenerateInitializer();
        }

        public void codeChanged() {
            regenerateInitializer();
        }

        public void formChanged() {
            regenerateVariables();
            regenerateInitializer();
        }

        /** Called when the form is about to be saved */

        public void formToBeSaved() {
            serializeComponentsRecursively(formManager.getRADForm().getTopLevelComponent());

            RADComponent[] nonVisuals = formManager.getNonVisualComponents();
            for (int i = 0; i < nonVisuals.length; i++) {
                serializeComponentsRecursively(nonVisuals[i]);
            }
        }

        private void serializeComponentsRecursively(RADComponent comp) {
            Object value = comp.getAuxValue(AUX_CODE_GENERATION);
            if ((value != null) && VALUE_SERIALIZE.equals(value)) {
                String serializeTo =(String)comp.getAuxValue(AUX_SERIALIZE_TO);
                if (serializeTo != null) {
                    try {
                        FileObject fo = formManager.getFormObject().getPrimaryFile();
                        FileObject serFile = fo.getParent().getFileObject(serializeTo, "ser"); // NOI18N
                        if (serFile == null) {
                            serFile = fo.getParent().createData(serializeTo, "ser"); // NOI18N
                        }
                        if (serFile != null) {
                            FileLock lock = null;
                            java.io.ObjectOutputStream oos = null;
                            try {
                                lock = serFile.lock();
                                oos = new java.io.ObjectOutputStream(serFile.getOutputStream(lock));
                                if (comp instanceof RADVisualContainer) {
                                    // [PENDING - remove temporarily the subcomponents]
                                }
                                oos.writeObject(comp.getBeanInstance());
                            } finally {
                                if (oos != null) oos.close();
                                if (lock != null) lock.releaseLock();
                            }
                        } else {
                            // [PENDING - handle problem]
                        }
                    } catch (java.io.NotSerializableException e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    } catch (Exception e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    }
                } else {
                    // [PENDING - notify error]
                }
            }
            if (comp instanceof ComponentContainer) {
                RADComponent[] children =((ComponentContainer)comp).getSubBeans();
                for (int i = 0; i < children.length; i++) {
                    serializeComponentsRecursively(children[i]);
                }
            }
        }

        /**
         * Called when the order of components within their parent changes
         * @param cont the container on which the components were reordered
         */

        public void componentsReordered(ComponentContainer cont) {
            regenerateVariables();
            regenerateInitializer();
        }

        /**
         * Called when a new component is added to the form
         * @param evt the event object describing the event
         */

        public void componentsAdded(RADComponent[] comps) {
            regenerateVariables();
            regenerateInitializer();
        }

        /**
         * Called when any component is removed from the form
         * @param evt the event object describing the event
         */
        public void componentsRemoved(RADComponent[] comps) {
            regenerateVariables();
            regenerateInitializer();
        }

        /**
         * Called when any synthetic property of a component on the form is changed
         * The synthetic properties include: variableName, serialize,
         * serializeName, generateGlobalVariable
         * @param evt the event object describing the event
         */

        public void componentChanged(FormPropertyEvent evt) {
            regenerateVariables();
            regenerateInitializer();
        }

        /**
         * Called when any bean property of a component on the form is changed
         * @param evt the event object describing the event
         */

        public void propertyChanged(FormPropertyEvent evt) {
            regenerateInitializer();
        }

        /**
         * Called when any layout property of specified component on given
         * container changes
         * @param container the visual container on which layout the change happened
         * @param component the component which layout property changed or null if
         * layout's own property changed
         * @param propertyName name of changed property
         * @param oldValue old value of changed property
         * @param newValue new value of changed property
         */

        public void layoutChanged(RADVisualContainer container,
                                  RADVisualComponent component,
                                  String propertyName, Object oldValue, Object newValue) {
            regenerateInitializer();
        }

        /**
         * Called when an event handler is added to a component on the form
         * @param evt the event object describing the event
         */
        public void eventAdded(FormEventEvent evt) {
            regenerateInitializer();
        }

        /**
         * Called when an event handler is added to a component on the form
         * @param evt the event object describing the event
         */
        public void eventRemoved(FormEventEvent evt) {
            regenerateInitializer();
        }

        /**
         * Called when an event handler is renamed on a component on the form
         * @param evt the event object describing the event
         */
        public void eventRenamed(FormEventEvent evt) {
            regenerateInitializer();
        }
    }
    // }}}

    //
    // {{{ AWTIndentStringWriter
    //

    static class AWTIndentStringWriter extends StringWriter {
        String currentIndent = null;

        void setIndentLevel(int level, String indentString) {
            if (level == 0) {
                currentIndent = null;
            } else {
                currentIndent = ""; // NOI18N
                for (int i = 0; i < level; i++) {
                    currentIndent = currentIndent + indentString;
                }
            }
        }

        /**
         * Write a single character.
         */
        public void write(int c) {
            if ((currentIndent != null) &&(c ==(int)'\n')) {
                super.write("\n" + currentIndent); // NOI18N
            } else {
                super.write(c);
            }
        }

        /**
         * Write a portion of an array of characters.
         *
         * @param  cbuf  Array of characters
         * @param  off   Offset from which to start writing characters
         * @param  len   Number of characters to write
         */

        public void write(char cbuf[], int off, int len) {
            if (currentIndent != null) {
                String str = new String(cbuf, off, len);
                str = Utilities.replaceString(str, "\n", "\n"+currentIndent); // NOI18N
                char[] newBuf = str.toCharArray();
                super.write(newBuf, 0, newBuf.length);
            } else {
                super.write(cbuf, off, len);
            }
        }

        /**
         * Write a string.
         */
        public void write(String str) {
            if (currentIndent != null) str = Utilities.replaceString(str, "\n", "\n"+currentIndent); // NOI18N
            super.write(str);
        }

        /**
         * Write a portion of a string.
         *
         * @param  str  String to be written
         * @param  off  Offset from which to start writing characters
         * @param  len  Number of characters to write
         */
        public void write(String str, int off, int len)  {
            if (currentIndent != null) str = Utilities.replaceString(
                str, "\n", "\n"+currentIndent); // NOI18N
            super.write(str, off, len);
        }
    }

    // }}}


    //
    // {{{ CodeGenerateEditor
    //

    final public static class CodeGenerateEditor extends PropertyEditorSupport
    {
        private RADComponent component;

        /** Display Names for alignment. */
        private static final String generateName = FormEditor.getFormBundle().getString("VALUE_codeGen_generate");
        private static final String serializeName = FormEditor.getFormBundle().getString("VALUE_codeGen_serialize");

        public CodeGenerateEditor(RADComponent component) {
            this.component = component;
        }

        /** @return names of the possible directions */
        public String[] getTags() {
            if (component.hasHiddenState()) {
                return new String[] { serializeName } ;
            } else {
                return new String[] { generateName, serializeName } ;
            }
        }

        /** @return text for the current value */
        public String getAsText() {
            Integer value =(Integer)getValue();
            if (value.equals(VALUE_SERIALIZE)) return serializeName;
            else return generateName;
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            if (component.hasHiddenState()) {
                setValue(VALUE_SERIALIZE);
            } else {
                if (serializeName.equals(str)) {
                    setValue(VALUE_SERIALIZE);
                } else if (generateName.equals(str)) {
                    setValue(VALUE_GENERATE_CODE);
                }
            }
        }
    }

    // }}}

    //
    // {{{ CodePropertySupportRW
    //

    abstract class CodePropertySupportRW extends PropertySupport.ReadWrite
    {
        CodePropertySupportRW(String name, Class type,
                              String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                public java.awt.Component getCustomEditor() {
                    return new CustomCodeEditor(CodePropertySupportRW.this);
                }

                public boolean supportsCustomEditor() {
                    return true;
                }
            };
        }
    }

    // }}}
}
