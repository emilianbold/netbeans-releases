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

package org.netbeans.modules.form;

import javax.swing.JEditorPane;
import org.openide.*;
import org.openide.explorer.propertysheet.editors.ModifierEditor;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.text.IndentEngine;
import org.openide.util.SharedClassObject;

import org.netbeans.api.editor.fold.*;

import org.netbeans.modules.java.JavaEditor;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.form.editors.CustomCodeEditor;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutCodeGenerator;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*; 
import java.util.*;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;

/**
 * JavaCodeGenerator is the default code generator which produces a Java source
 * for the form.
 *
 * @author Ian Formanek, Jan Stola
 */

class JavaCodeGenerator extends CodeGenerator {

    static final String PROP_VARIABLE_MODIFIER = "modifiers"; // NOI18N
    static final String PROP_VARIABLE_LOCAL = "useLocalVariable"; // NOI18N
    static final String PROP_SERIALIZE_TO = "serializeTo"; // NOI18N
    static final String PROP_CODE_GENERATION = "codeGeneration"; // NOI18N
    static final String PROP_CREATE_CODE_PRE = "creationCodePre"; // NOI18N
    static final String PROP_CREATE_CODE_POST = "creationCodePost"; // NOI18N
    static final String PROP_CREATE_CODE_CUSTOM = "creationCodeCustom"; // NOI18N
    static final String PROP_INIT_CODE_PRE = "initCodePre"; // NOI18N
    static final String PROP_INIT_CODE_POST = "initCodePost"; // NOI18N
    static final String PROP_DECLARATION_PRE = "declarationPre"; // NOI18N
    static final String PROP_DECLARATION_POST = "declarationPost"; // NOI18N
    static final String PROP_GENERATE_MNEMONICS = "generateMnemonicsCode"; // Mnemonics support NOI18N
    static final String PROP_LISTENER_GENERATION_STYLE = "listenerGenerationStyle"; // NOI18N

    static final String AUX_VARIABLE_MODIFIER =
        "JavaCodeGenerator_VariableModifier"; // NOI18N
    static final String AUX_VARIABLE_LOCAL =
        "JavaCodeGenerator_VariableLocal"; // NOI18N
    static final String AUX_SERIALIZE_TO =
        "JavaCodeGenerator_SerializeTo"; // NOI18N
    static final String AUX_CODE_GENERATION =
        "JavaCodeGenerator_CodeGeneration"; // NOI18N
    static final String AUX_CREATE_CODE_PRE =
        "JavaCodeGenerator_CreateCodePre"; // NOI18N
    static final String AUX_CREATE_CODE_POST =
        "JavaCodeGenerator_CreateCodePost"; // NOI18N
    static final String AUX_CREATE_CODE_CUSTOM =
        "JavaCodeGenerator_CreateCodeCustom"; // NOI18N
    static final String AUX_INIT_CODE_PRE =
        "JavaCodeGenerator_InitCodePre"; // NOI18N
    static final String AUX_INIT_CODE_POST =
        "JavaCodeGenerator_InitCodePost"; // NOI18N
    static final String AUX_DECLARATION_PRE =
        "JavaCodeGenerator_DeclarationPre"; // NOI18N
    static final String AUX_DECLARATION_POST =
        "JavaCodeGenerator_DeclarationPost"; // NOI18N

    static final Integer VALUE_GENERATE_CODE = new Integer(0);
    static final Integer VALUE_SERIALIZE = new Integer(1);

    // types of code generation of event listeners
    static final int ANONYMOUS_INNERCLASSES = 0;
    static final int CEDL_INNERCLASS = 1;
    static final int CEDL_MAINCLASS = 2;

    // types of code generation of layout code
    static final int LAYOUT_CODE_AUTO = 0;
    static final int LAYOUT_CODE_JDK6 = 1;
    static final int LAYOUT_CODE_LIBRARY = 2;

    private static final String EVT_SECTION_PREFIX = "event_"; // NOI18N

    private static final String DEFAULT_LISTENER_CLASS_NAME = "FormListener"; // NOI18N

    private static final String CODE_MARK = "*/\n\\"; // NOI18N
    private static final String MARKED_PROPERTY_CODE = "*/\n\\0"; // NOI18N
    private static final String PROPERTY_LINE_COMMENT = "*/\n\\1"; // NOI18N

    private static final String RESOURCE_BUNDLE_OPENING_CODE = "java.util.ResourceBundle.getBundle("; // NOI18N
    private static final String RESOURCE_BUNDLE_CLOSING_CODE = ")."; // NOI18N
    private Map<String,String> bundleVariables;

    private static String variablesHeader;
    private static String variablesFooter;
    private static String eventDispatchCodeComment;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);

    private FormModel formModel;
    private FormEditorSupport formEditorSupport;

    private boolean initialized = false;
    private boolean canGenerate = true;
    private boolean codeUpToDate = true;

    private String listenerClassName;
    private String listenerVariableName;

    // data needed when listener generation style is CEDL_MAINCLASS
    private ClassElement mainClassElement;
    private Class[] listenersInMainClass;
    private Class[] listenersInMainClass_lastSet;

    private JavaEditor.SimpleSection initComponentsSection;
    private JavaEditor.SimpleSection variablesSection;
       
    private Map constructorProperties;
    private Map containerDependentProperties;
    
    private SwingLayoutCodeGenerator swingGenerator;

    private static class PropertiesFilter implements FormProperty.Filter {
		
	private final java.util.List properties;
	
	public PropertiesFilter(java.util.List properties) {
	    this.properties = properties;
	}
	
        public boolean accept(FormProperty property) {	    		     
	    return (property.isChanged()
                       && (properties == null
                           || !properties.contains(property)))
                    || property.getPreCode() != null
                    || property.getPostCode() != null;
        }
    };

    /** Creates new JavaCodeGenerator */

//    public JavaCodeGenerator() {
//    }

    public void initialize(FormModel formModel) {
        if (!initialized) {
            this.formModel = formModel;
            FormDataObject formDO = FormEditor.getFormDataObject(formModel);
            formEditorSupport = formDO.getFormEditorSupport();

            if (formDO.getPrimaryFile().canWrite()) {
                canGenerate = true;
                formModel.addFormModelListener(new FormListener());
            }
            else canGenerate = false;

            initComponentsSection = formEditorSupport.getInitComponentSection();
            variablesSection = formEditorSupport.getVariablesSection();

            if (initComponentsSection == null || variablesSection == null) {
                System.err.println("ERROR: Cannot initialize guarded sections... code generation is disabled."); // NOI18N
                canGenerate = false;
            }

            initialized = true;
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
        ResourceBundle bundle = FormUtils.getBundle();
        java.util.List propList = new ArrayList();
        if (component == null) {
            propList.add(new VariablesModifierProperty());
            propList.add(new LocalVariablesProperty());
            propList.add(new GenerateMnemonicsCodeProperty());
            propList.add(new ListenerGenerationStyleProperty());
            propList.add(new LayoutCodeTargetProperty());
        } else if (component != formModel.getTopRADComponent()) {
            
            propList.add(createBeanClassNameProperty(component));
            
            propList.add(new PropertySupport.ReadWrite(
                RADComponent.PROP_NAME,
                String.class,
                bundle.getString("MSG_JC_VariableName"), // NOI18N
                bundle.getString("MSG_JC_VariableDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    component.setName((String)value);
                    component.getNodeReference().firePropertyChangeHelper(
                        RADComponent.PROP_NAME, null, null); // NOI18N
                }

                public Object getValue() {
                    return component.getName();
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });

            propList.add(new PropertySupport.ReadWrite(
                PROP_VARIABLE_MODIFIER,
                Integer.class,
                bundle.getString("MSG_JC_VariableModifiers"), // NOI18N
                bundle.getString("MSG_JC_VariableModifiersDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= ~CodeVariable.ALL_MODIF_MASK;
                    varType |= ((Integer)value).intValue() & CodeVariable.ALL_MODIF_MASK;
                    if ((varType & (CodeVariable.LOCAL | CodeVariable.FINAL))
                            == CodeVariable.LOCAL)
                        varType |= CodeVariable.EXPLICIT_DECLARATION;

                    if ((varType & CodeVariable.ALL_MODIF_MASK)
                            != (formModel.getSettings().getVariablesModifier()
                                & CodeVariable.ALL_MODIF_MASK))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_MODIFIER, value);
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        if (component.getAuxValue(AUX_VARIABLE_MODIFIER) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_MODIFIER);
                        }
                    }

                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, varName);

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_VARIABLE_MODIFIER, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_VARIABLE_MODIFIER, null, null);
                    }
                }

                public Object getValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_MODIFIER);
                    if (val != null)
                        return val;

                    return new Integer(formModel.getSettings().getVariablesModifier());
                }

                public boolean supportsDefaultValue() {
                    return component.getAuxValue(AUX_VARIABLE_LOCAL) == null;
                }
                
                public void restoreDefaultValue() {
                    if (component.getAuxValue(AUX_VARIABLE_LOCAL) == null)
                        setValue(new Integer(formModel.getSettings().getVariablesModifier()));
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                public PropertyEditor getPropertyEditor() {
                    Boolean local = (Boolean) component.getAuxValue(AUX_VARIABLE_LOCAL);
                    local = (local == null) ? Boolean.valueOf(formModel.getSettings().getVariablesLocal()) : local;
                    return Boolean.TRUE.equals(local) ?
                        new ModifierEditor(Modifier.FINAL)
                        :
                        new ModifierEditor(Modifier.PUBLIC
                                           | Modifier.PROTECTED
                                           | Modifier.PRIVATE
                                           | Modifier.STATIC
                                           | Modifier.FINAL
                                           | Modifier.TRANSIENT
                                           | Modifier.VOLATILE);
                }
            });

            propList.add(new PropertySupport.ReadWrite(
                PROP_VARIABLE_LOCAL,
                Boolean.TYPE,
                bundle.getString("MSG_JC_UseLocalVar"), // NOI18N
                bundle.getString("MSG_JC_UseLocalVarDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Boolean))
                        throw new IllegalArgumentException();

                    Boolean oldValue = (Boolean)getValue();
                    if (value.equals(oldValue)) return;

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= CodeVariable.FINAL
                               | ~(CodeVariable.ALL_MODIF_MASK
                                     | CodeVariable.SCOPE_MASK);
                    if (Boolean.TRUE.equals(value))
                        varType |= CodeVariable.LOCAL
                                   | CodeVariable.EXPLICIT_DECLARATION;
                    else
                        varType |= CodeVariable.FIELD
                                   | formModel.getSettings().getVariablesModifier();

                    if (((varType & CodeVariable.LOCAL) != 0)
                            != (formModel.getSettings().getVariablesLocal()))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_LOCAL, value);
                        component.setAuxValue(
                            AUX_VARIABLE_MODIFIER,
                            new Integer(varType & CodeVariable.ALL_MODIF_MASK));
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        if (component.getAuxValue(AUX_VARIABLE_LOCAL) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_LOCAL);
                        }
                        if (component.getAuxValue(AUX_VARIABLE_MODIFIER) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_MODIFIER);
                        }
                    }

                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, varName);

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_VARIABLE_LOCAL, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().fireComponentPropertySetsChange();
                    }
                }

                public Object getValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_LOCAL);
                    if (val != null)
                        return val;

                    return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
                }

                public boolean supportsDefaultValue() {
                    return true;
                }

                public void restoreDefaultValue() {
                    setValue(Boolean.valueOf(formModel.getSettings().getVariablesLocal()));
                }
                    
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });

            // Mnemonics support - start -
            if (javax.swing.JLabel.class.isAssignableFrom(component.getBeanClass())
                    || javax.swing.AbstractButton.class.isAssignableFrom(component.getBeanClass()))
                propList.add(new PropertySupport.ReadWrite(
                    PROP_GENERATE_MNEMONICS, 
                    Boolean.TYPE,
                    bundle.getString("PROP_GENERATE_MNEMONICS"), // NOI18N
                    bundle.getString("HINT_GENERATE_MNEMONICS2")) // NOI18N
                {
                    public void setValue(Object value) {
                        Object oldValue = getValue();
                        component.setAuxValue(PROP_GENERATE_MNEMONICS, value);
                        formModel.fireSyntheticPropertyChanged(
                            component, PROP_GENERATE_MNEMONICS, oldValue, value);
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_GENERATE_MNEMONICS, null, null); // NOI18N
                    }

                    public Object getValue() {
                        return isUsingMnemonics(component) ?
                               Boolean.TRUE : Boolean.FALSE;
                    }

                    public boolean canWrite() {
                        return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                    }

                    public boolean supportsDefaultValue() {
                        return true;
                    }

                    public void restoreDefaultValue() {
                        setValue(null);
                    }
                });
            // Mnemonics support - end -

            propList.add(new PropertySupport.ReadWrite(
                PROP_CODE_GENERATION,
                Integer.TYPE,
                bundle.getString("MSG_JC_CodeGeneration"), // NOI18N
                bundle.getString("MSG_JC_CodeGenerationDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!getDefaultValue().equals(value))
                        component.setAuxValue(AUX_CODE_GENERATION, value);
                    else if (component.getAuxValue(AUX_CODE_GENERATION) != null) {
                        component.getAuxValues().remove(AUX_CODE_GENERATION);
                    }

                    if (value.equals(VALUE_SERIALIZE)
                            && component.getAuxValue(AUX_SERIALIZE_TO) == null)
                        component.setAuxValue(AUX_SERIALIZE_TO,
                                              getDefaultSerializedName(component));

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_CODE_GENERATION, oldValue, value);
                    component.getNodeReference().firePropertyChangeHelper(
                        PROP_CODE_GENERATION, null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CODE_GENERATION);
                    if (value == null)
                        value = getDefaultValue();
                    return value;
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                public PropertyEditor getPropertyEditor() {
                    return new CodeGenerateEditor(component);
                }

                private Object getDefaultValue() {
                    return component.hasHiddenState() ?
                                VALUE_SERIALIZE : VALUE_GENERATE_CODE;
                }
            });

            propList.add(new CodePropertySupportRW(
                PROP_CREATE_CODE_PRE,
                String.class,
                bundle.getString("MSG_JC_PreCreationCode"), // NOI18N
                bundle.getString("MSG_JC_PreCreationCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_CREATE_CODE_PRE, value);
                    else if (component.getAuxValue(AUX_CREATE_CODE_PRE) != null) {
                        component.getAuxValues().remove(AUX_CREATE_CODE_PRE);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_CREATE_CODE_PRE, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_CREATE_CODE_PRE, null, null);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CREATE_CODE_PRE);
                    if (value == null)
                        value = ""; // NOI18N
                    return value;
                }
            });

            propList.add(new CodePropertySupportRW(
                PROP_CREATE_CODE_POST,
                String.class,
                bundle.getString("MSG_JC_PostCreationCode"), // NOI18N
                bundle.getString("MSG_JC_PostCreationCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_CREATE_CODE_POST, value);
                    else if (component.getAuxValue(AUX_CREATE_CODE_POST) != null) {
                        component.getAuxValues().remove(AUX_CREATE_CODE_POST);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_CREATE_CODE_POST, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_CREATE_CODE_POST, null, null);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CREATE_CODE_POST);
                    if (value == null)
                        value = ""; // NOI18N
                    return value;
                }
            });

            propList.add(new CodePropertySupportRW(
                PROP_INIT_CODE_PRE,
                String.class,
                bundle.getString("MSG_JC_PreInitCode"), // NOI18N
                bundle.getString("MSG_JC_PreInitCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_INIT_CODE_PRE, value);
                    else if (component.getAuxValue(AUX_INIT_CODE_PRE) != null) {
                        component.getAuxValues().remove(AUX_INIT_CODE_PRE);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_INIT_CODE_PRE, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_INIT_CODE_PRE, null, null);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_INIT_CODE_PRE);
                    if (value == null)
                        value = ""; // NOI18N
                    return value;
                }
            });

            propList.add(new CodePropertySupportRW(
                PROP_INIT_CODE_POST,
                String.class,
                bundle.getString("MSG_JC_PostInitCode"), // NOI18N
                bundle.getString("MSG_JC_PostInitCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_INIT_CODE_POST, value);
                    else if (component.getAuxValue(AUX_INIT_CODE_POST) != null) {
                        component.getAuxValues().remove(AUX_INIT_CODE_POST);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_INIT_CODE_POST, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_INIT_CODE_POST, null, null);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_INIT_CODE_POST);
                    if (value == null)
                        value = ""; // NOI18N
                    return value;
                }
            });

            propList.add(new CodePropertySupportRW(
                PROP_DECLARATION_PRE,
                String.class,
                bundle.getString("MSG_JC_PreDeclaration"), // NOI18N
                bundle.getString("MSG_JC_PreDeclarationDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_DECLARATION_PRE, value);
                    else if (component.getAuxValue(AUX_DECLARATION_PRE) != null) {
                        component.getAuxValues().remove(AUX_DECLARATION_PRE);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_DECLARATION_PRE, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_DECLARATION_PRE, null, null);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_DECLARATION_PRE);
                    if (value == null)
                        value = ""; // NOI18N
                    return value;
                }
            });

            propList.add(new CodePropertySupportRW(
                PROP_DECLARATION_POST,
                String.class,
                bundle.getString("MSG_JC_PostDeclaration"), // NOI18N
                bundle.getString("MSG_JC_PostDeclarationDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_DECLARATION_POST, value);
                    else if (component.getAuxValue(AUX_DECLARATION_POST) != null) {
                        component.getAuxValues().remove(AUX_DECLARATION_POST);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_DECLARATION_POST, oldValue, value);
                    if (component.getNodeReference() != null) {
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_DECLARATION_POST, null, null);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_DECLARATION_POST);
                    if (value == null)
                        value = ""; // NOI18N
                    return value;
                }
            });

            propList.add(new PropertySupport.ReadWrite(
                PROP_SERIALIZE_TO,
                String.class,
                bundle.getString("MSG_JC_SerializeTo"), // NOI18N
                bundle.getString("MSG_JC_SerializeToDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value))
                        component.setAuxValue(AUX_SERIALIZE_TO, value);
                    else if (component.getAuxValue(AUX_SERIALIZE_TO) != null) {
                        component.getAuxValues().remove(AUX_SERIALIZE_TO);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_SERIALIZE_TO, oldValue, value);
                    component.getNodeReference().firePropertyChangeHelper(
                        PROP_SERIALIZE_TO, null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_SERIALIZE_TO);
                    if (value == null)
                        value = getDefaultSerializedName(component);
                    return value;
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });

            Integer generationType = (Integer)
                component.getAuxValue(AUX_CODE_GENERATION);
            if (generationType == null
                || generationType.equals(VALUE_GENERATE_CODE))
            {
                propList.add(new CodePropertySupportRW(
                    PROP_CREATE_CODE_CUSTOM,
                    String.class,
                    bundle.getString("MSG_JC_CustomCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_CustomCreationCodeDesc")) // NOI18N
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String))
                            throw new IllegalArgumentException();

                        Object oldValue = getValue();

                        if (!"".equals(value))
                            component.setAuxValue(AUX_CREATE_CODE_CUSTOM, value);
                        else if (component.getAuxValue(AUX_CREATE_CODE_CUSTOM) != null) {
                            component.getAuxValues().remove(AUX_CREATE_CODE_CUSTOM);
                        }

                        formModel.fireSyntheticPropertyChanged(
                            component, PROP_CREATE_CODE_CUSTOM, oldValue, value);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                PROP_CREATE_CODE_CUSTOM, null, null);
                        }
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_CREATE_CODE_CUSTOM);
                        if (value == null)
                            value = ""; // NOI18N
                        return value;
                    }

                    public boolean canWrite() {
                        if (!JavaCodeGenerator.this.canGenerate && !component.isReadOnly())
                            return false;
                        Integer genType =(Integer)component.getAuxValue(AUX_CODE_GENERATION);
                        return((genType == null) ||(genType.equals(VALUE_GENERATE_CODE)));
                    }
                });
            }
        }
        else if (Component.class.isAssignableFrom(component.getBeanClass()))
        {
            propList.add(new PropertySupport.ReadOnly(
                FormDesigner.PROP_DESIGNER_SIZE,
                Dimension.class,
                bundle.getString("MSG_DesignerSize"), // NOI18N
                bundle.getString("HINT_DesignerSize")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Dimension))
                        throw new IllegalArgumentException();
                    if (!getDefaultValue().equals(value))
                        component.setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, value);
                    else if (component.getAuxValue(FormDesigner.PROP_DESIGNER_SIZE) != null) {
                        component.getAuxValues().remove(FormDesigner.PROP_DESIGNER_SIZE);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(FormDesigner.PROP_DESIGNER_SIZE);
                    if (value == null)
                        value = getDefaultValue();
                    return value;
                }

                private Object getDefaultValue() {
                    return new Dimension(400, 300);
                }
            });
        }

        Node.Property[] props = new Node.Property[propList.size()];
        propList.toArray(props);
        return props;
    }

    public static PropertySupport createBeanClassNameProperty(final RADComponent component) {
        final ResourceBundle bundle = FormUtils.getBundle();
        
        return new PropertySupport.ReadOnly(
                "beanClass", // NOI18N
                String.class,
                bundle.getString("MSG_JC_BeanClass"), // NOI18N
                bundle.getString("MSG_JC_BeanClassDesc")) // NOI18N
            {
		String invalid = null;
                public Object getValue() {
                    if(!component.isValid()) {
			if(invalid==null) {
			    invalid = bundle.getString("CTL_LB_InvalidComponent");  // NOI18N
			}
                        return component.getMissingClassName() + ": [" + invalid + "]"; // NOI18N
                    }
                    Class beanClass = component.getBeanClass();
                    if(beanClass!=null) {
                        return beanClass.toString();
                    }
                    return "";              
                }

                public boolean canWrite() {
                    return false;                    
                }     
                
                public PropertyEditor getPropertyEditor() {
                    return new PropertyEditorSupport(){};
                }
            };
    }    
    //
    // Private Methods
    //

    private String getDefaultSerializedName(RADComponent component) {
        return component.getFormModel().getName()
            + "_" + component.getName(); // NOI18N
    }

    void regenerateInitComponents() {
        if (!initialized || !canGenerate)
            return;

        // find indent engine to use or imitate
        IndentEngine indentEngine = IndentEngine.find(
                                        formEditorSupport.getDocument());

        int initComponentsOffset = initComponentsSection.getBegin().getOffset();

        // create Writer for writing the generated code in
        StringWriter initCodeBuffer = new StringWriter(1024);
        CodeWriter initCodeWriter;
        if (formSettings.getUseIndentEngine()) { // use original indent engine
            initCodeWriter = new CodeWriter(
                    indentEngine.createWriter(formEditorSupport.getDocument(),
                                              initComponentsOffset,
                                              initCodeBuffer),
                    true);
        }
        else {
            initCodeWriter = new CodeWriter(initCodeBuffer, true);
        }
        
        if (constructorProperties != null)
            constructorProperties.clear();
        if (containerDependentProperties != null)
            containerDependentProperties.clear();
	formModel.getCodeStructure().clearExternalVariableNames();
	
        try {
            boolean expandInitComponents = false;
            boolean foldGeneratedCode = formSettings.getFoldGeneratedCode();
            if (foldGeneratedCode) {
                String foldDescription = " " + FormUtils.getBundleString("MSG_GeneratedCode"); // NOI18N
                javax.swing.JEditorPane editorPane = formEditorSupport.getEditorPane();
                if (editorPane != null) {
                    FoldHierarchy foldHierarchy = FoldHierarchy.get(editorPane);
                    Fold fold = FoldUtilities.findNearestFold(foldHierarchy, initComponentsOffset);
                    expandInitComponents = (fold != null) && foldDescription.equals(fold.getDescription()) && !fold.isCollapsed();
                }
                initCodeWriter.write("// <editor-fold defaultstate=\"collapsed\" desc=\""); // NOI18N
                initCodeWriter.write(foldDescription);
                initCodeWriter.write("\">\n"); // NOI18N
            }

            initCodeWriter.write("private void initComponents() {\n"); // NOI18N

            if (addLocalVariables(initCodeWriter))
                initCodeWriter.write("\n"); // NOI18N

            Collection<RADComponent> otherComps = formModel.getOtherComponents();
            for (RADComponent metacomp : otherComps) {
                addCreateCode(metacomp, initCodeWriter);
            }
            RADComponent top = formModel.getTopRADComponent();
            addCreateCode(top, initCodeWriter);
            initCodeWriter.write("\n"); // NOI18N

            if (formModel.getSettings().getListenerGenerationStyle() == CEDL_INNERCLASS
                && anyEvents())
            {
                addDispatchListenerDeclaration(initCodeWriter.getWriter());
                initCodeWriter.write("\n"); // NOI18N
            }

            for (RADComponent metacomp : otherComps) {
                addInitCode(metacomp, initCodeWriter, 0);
            }
            if (otherComps.size() > 0)
                initCodeWriter.write("\n"); // NOI18N
            addInitCode(top, initCodeWriter, 0);

            // for visual forms append sizing text
            if (formModel.getTopRADComponent() instanceof RADVisualFormContainer) {
                RADVisualFormContainer visualForm =
                    (RADVisualFormContainer) formModel.getTopRADComponent();

                // generate size code according to form size policy
                int formPolicy = visualForm.getFormSizePolicy();
                boolean genSize = visualForm.getGenerateSize();
                boolean genPosition = visualForm.getGeneratePosition();
                boolean genCenter = visualForm.getGenerateCenter();
                Dimension formSize = visualForm.getFormSize();
                Point formPosition = visualForm.getFormPosition();

                String sizeText = ""; // NOI18N

                if (formPolicy == RADVisualFormContainer.GEN_PACK)
                    sizeText = "pack();\n"; // NOI18N
                else if (formPolicy == RADVisualFormContainer.GEN_BOUNDS) {
                    if (genCenter) {
                        StringBuffer sizeBuffer = new StringBuffer();
                        if (genSize) {
//                                sizeBuffer.append("pack();\n"); // NOI18N
                            sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                            sizeBuffer.append("setBounds((screenSize.width-"+formSize.width+")/2, (screenSize.height-"+formSize.height+")/2, "+formSize.width + ", " + formSize.height + ");\n"); // NOI18N
//                                sizeBuffer.append("setSize(new java.awt.Dimension("+formSize.width + ", " + formSize.height + "));\n"); // NOI18N
//                                sizeBuffer.append("setLocation((screenSize.width-"+formSize.width+")/2,(screenSize.height-"+formSize.height+")/2);\n"); // NOI18N
                        }
                        else {
                            sizeBuffer.append("pack();\n"); // NOI18N
                            sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                            sizeBuffer.append("java.awt.Dimension dialogSize = getSize();\n"); // NOI18N
                            sizeBuffer.append("setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);\n"); // NOI18N
                        }
                        sizeText = sizeBuffer.toString();
                    }
                    else if (genPosition && genSize) // both size and position
                        sizeText = "setBounds("+formPosition.x + ", " // NOI18N
                                   + formPosition.y +", " // NOI18N
                                   + formSize.width + ", " // NOI18N
                                   + formSize.height + ");\n"; // NOI18N
                    else if (genPosition) // position only
                        sizeText = "setLocation(new java.awt.Point(" // NOI18N
                                   + formPosition.x + ", " // NOI18N
                                   + formPosition.y + "));\n"; // NOI18N
                    else if (genSize) // size only
                        sizeText = "setSize(new java.awt.Dimension(" // NOI18N
                                   + formSize.width + ", " // NOI18N
                                   + formSize.height + "));\n"; // NOI18N
                }

                initCodeWriter.write(sizeText);
            }

            // cleanup
            if (constructorProperties != null)
                constructorProperties.clear();
            if (containerDependentProperties != null)
                containerDependentProperties.clear();
	    formModel.getCodeStructure().clearExternalVariableNames();
            bundleVariables = null;

            initCodeWriter.write("}"); // no new line because of fold footer // NOI18N

            int listenerCodeStyle = formModel.getSettings().getListenerGenerationStyle();
            if ((listenerCodeStyle == CEDL_INNERCLASS
                  || listenerCodeStyle == CEDL_MAINCLASS)
                && anyEvents())
            {
                initCodeWriter.write("\n\n"); // NOI18N
                initCodeWriter.write(getEventDispatchCodeComment());
                initCodeWriter.write("\n"); // NOI18N

                generateDispatchListenerCode(initCodeWriter.getWriter());
            }
            else listenersInMainClass = null;

            if (foldGeneratedCode) {
                initCodeWriter.write("// </editor-fold>\n"); // NOI18N
            }
            else {
                initCodeWriter.write("\n"); // NOI18N
            }
            initCodeWriter.getWriter().close();

             // set the text into the guarded block
            String newText = initCodeBuffer.toString();
            if (!formSettings.getUseIndentEngine()) {
                newText = indentCode(newText, indentEngine);
            }
            initComponentsSection.setText(newText);
            
            if (expandInitComponents) {
                FoldHierarchy foldHierarchy = FoldHierarchy.get(formEditorSupport.getEditorPane());
                Fold fold = FoldUtilities.findNearestFold(foldHierarchy, initComponentsOffset);
                if (fold != null) {
                    foldHierarchy.expand(fold);
                }
            }
            clearUndo();
        }
        catch (IOException e) { // should not happen
            e.printStackTrace();
        }
    }

    private void regenerateVariables() {
        if (!initialized || !canGenerate)
            return;
        
        IndentEngine indentEngine = IndentEngine.find(
                                        formEditorSupport.getDocument());

        StringWriter variablesBuffer = new StringWriter(1024);
        CodeWriter variablesWriter;
        
        if (formSettings.getUseIndentEngine()) {
            variablesWriter = new CodeWriter(
                    indentEngine.createWriter(formEditorSupport.getDocument(),
                                              variablesSection.getBegin().getOffset(),
                                              variablesBuffer),
                    false);
        }
        else {
            variablesWriter = new CodeWriter(variablesBuffer, false);
        }
	    
        try {
	    
	    variablesWriter.write(getVariablesHeaderComment());
            variablesWriter.write("\n"); // NOI18N

            addVariables(variablesWriter);
            
            variablesWriter.write(getVariablesFooterComment());
            variablesWriter.write("\n"); // NOI18N
            variablesWriter.getWriter().close();

            String newText = variablesBuffer.toString();
            if (!formSettings.getUseIndentEngine())
                newText = indentCode(newText, indentEngine);

            variablesSection.setText(newText);        
            clearUndo();
        }
        catch (IOException e) { // should not happen
            e.printStackTrace();
        }
    }   
    
    private void addCreateCode(RADComponent comp, CodeWriter initCodeWriter)
        throws IOException
    {
        if (comp == null)
            return;

        if (comp != formModel.getTopRADComponent()) {
            generateComponentCreate(comp, initCodeWriter, true);
        }
        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                addCreateCode(children[i], initCodeWriter);
            }
        }
    }

    private void addInitCode(RADComponent comp,
                             CodeWriter initCodeWriter,
                             int level)
        throws IOException
    {
        if (comp == null)
            return;

        generateComponentInit(comp, initCodeWriter);
        generateComponentEvents(comp, initCodeWriter.getWriter());
        if (comp.getParentComponent() == null)
            generateAccessibilityCode(comp, initCodeWriter);

        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                RADComponent subcomp = children[i];
                addInitCode(subcomp, initCodeWriter, level);

                if (comp instanceof RADVisualContainer) {
                    // visual container
                    generateComponentAddCode(subcomp,
                                             (RADVisualContainer)comp,
                                             initCodeWriter);
                }
                else if (comp instanceof RADMenuComponent) {
                    // menu
                    generateMenuAddCode(subcomp,
                                        (RADMenuComponent) comp,
                                        initCodeWriter);
                } // [PENDING - adding to non-visual containers]

                generateAccessibilityCode(subcomp, initCodeWriter);

                initCodeWriter.write("\n"); // NOI18N
            }

            // hack for properties that can't be set until all children 
            // are added to the container
            java.util.List postProps;
            if (containerDependentProperties != null
                && (postProps = (java.util.List)containerDependentProperties.get(comp))
                    != null)
            {
                for (Iterator it = postProps.iterator(); it.hasNext(); ) {
                    RADProperty prop = (RADProperty) it.next();
                    generatePropertySetter(prop, comp, initCodeWriter);
                }
                initCodeWriter.write("\n"); // NOI18N
            }
            if (comp instanceof RADVisualContainer) {
                RADVisualContainer cont = (RADVisualContainer)comp;
                if (cont.getLayoutSupport() == null) {
                    LayoutComponent layoutCont = formModel.getLayoutModel().getLayoutComponent(cont.getId());
                    if (layoutCont != null) {
                        RADVisualComponent[] comps = cont.getSubComponents();
                        SwingLayoutCodeGenerator.ComponentInfo[] infos = new SwingLayoutCodeGenerator.ComponentInfo[comps.length];
                        for (int i=0; i<comps.length; i++) {
                            RADVisualComponent subComp = comps[i];
                            SwingLayoutCodeGenerator.ComponentInfo info = new SwingLayoutCodeGenerator.ComponentInfo();
                            info.id = subComp.getId();
                            info.variableName = getExpressionJavaString(subComp.getCodeExpression(), ""); // NOI18N
                            info.clazz = subComp.getBeanClass();
                            Node.Property minProp = subComp.getPropertyByName("minimumSize"); // NOI18N
                            Node.Property prefProp = subComp.getPropertyByName("preferredSize"); // NOI18N
                            Node.Property maxProp = subComp.getPropertyByName("maximumSize"); // NOI18N
                            info.sizingChanged = !(((minProp == null) || minProp.isDefaultValue())
                                && ((prefProp == null) || prefProp.isDefaultValue())
                                && ((maxProp == null) || maxProp.isDefaultValue()));
                            info.minSize = ((Component)subComp.getBeanInstance()).getMinimumSize();
                            infos[i] = info;
                        }
                        CodeExpression contExpr = LayoutSupportManager.containerDelegateCodeExpression(
                                                    cont, formModel.getCodeStructure());
                        String contExprStr = getExpressionJavaString(contExpr, ""); // NOI18N
                        CodeVariable contVar = cont.getCodeExpression().getVariable();
                        String contVarName = (contVar == null) ? null : contVar.getName();
                        SwingLayoutCodeGenerator swingGenerator = getSwingGenerator();
                        swingGenerator.generateContainerLayout(
                            initCodeWriter.getWriter(),
                            layoutCont,
                            contExprStr,
                            contVarName,
                            infos,
                            formModel.getSettings().getLayoutCodeTarget() == LAYOUT_CODE_LIBRARY);
                    }
                }
//                generateVisualCode((RADVisualContainer)comp, initCodeWriter);
            }

        }
    }
    
    private SwingLayoutCodeGenerator getSwingGenerator() {
        if (swingGenerator == null) {
            swingGenerator = new SwingLayoutCodeGenerator(formModel.getLayoutModel());
        }
        return swingGenerator;
    }

    private void generateComponentCreate(RADComponent comp,
                                         CodeWriter initCodeWriter,
                                         boolean insideMethod)
        throws IOException
    {
        if (comp instanceof RADMenuItemComponent
            && ((RADMenuItemComponent)comp).getMenuItemType()
                   == RADMenuItemComponent.T_SEPARATOR)
        { // do not generate anything for AWT separator as it is not a real component
            return;
        }

        CodeVariable var = comp.getCodeExpression().getVariable();
        int varType = var.getType();

        if (insideMethod) {
            int finalField = CodeVariable.FIELD | CodeVariable.FINAL;
            if ((varType & finalField) == finalField)
                return;

            String preCode = (String) comp.getAuxValue(AUX_CREATE_CODE_PRE);
            if (preCode != null && !preCode.equals("")) { // NOI18N
                initCodeWriter.write(preCode);
                initCodeWriter.write("\n"); // NOI18N
            }
        }

        Integer generationType = (Integer)comp.getAuxValue(AUX_CODE_GENERATION);
        if (comp.hasHiddenState()
            || (generationType != null
                && generationType.equals(VALUE_SERIALIZE)))
        {   // generate code for restoring serialized component
            if (!insideMethod)
                return;

            String serializeTo = (String)comp.getAuxValue(AUX_SERIALIZE_TO);
            if (serializeTo == null) {
                serializeTo = getDefaultSerializedName(comp);
                comp.setAuxValue(AUX_SERIALIZE_TO, serializeTo);
            }
            initCodeWriter.write("try {\n"); // NOI18N
            initCodeWriter.write(comp.getName());
            initCodeWriter.write(" =("); // NOI18N
            initCodeWriter.write(getSourceClassName(comp.getBeanClass()));
            initCodeWriter.write(")java.beans.Beans.instantiate(getClass().getClassLoader(), \""); // NOI18N

            // write package name
            FileObject fo = formEditorSupport.getFormDataObject().getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            String packageName = cp.getResourceName(fo.getParent());
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
        }
        else { // generate standard component creation code
            StringBuffer varBuf = new StringBuffer();

            int declareMask = CodeVariable.SCOPE_MASK
                              | CodeVariable.DECLARATION_MASK;

            if ((varType & CodeVariable.FINAL) == CodeVariable.FINAL
                || (varType & declareMask) == CodeVariable.LOCAL)
            {   // generate a variable declaration together with the assignment
                varBuf.append(Modifier.toString(
                                varType & CodeVariable.ALL_MODIF_MASK));
                varBuf.append(" "); // NOI18N
                varBuf.append(getSourceClassName(comp.getBeanClass()));
                varBuf.append(" "); // NOI18N
            }

            varBuf.append(var.getName());

            String customCreateCode = (String) comp.getAuxValue(AUX_CREATE_CODE_CUSTOM);
            if (customCreateCode != null && !"".equals(customCreateCode)) { // NOI18N
                initCodeWriter.write(varBuf.toString());
                initCodeWriter.write(" = "); // NOI18N
                initCodeWriter.write(customCreateCode);
                initCodeWriter.write("\n"); // NOI18N
            }
            else {
                CreationDescriptor desc = CreationFactory.getDescriptor(
                                                              comp.getBeanClass());
                if (desc == null)
                    desc = new CreationDescriptor(comp.getBeanClass());

                CreationDescriptor.Creator creator =
                    desc.findBestCreator(comp.getKnownBeanProperties(),
                                         CreationDescriptor.CHANGED_ONLY);
                if (creator == null) // known properties are not enough...
                    creator = desc.findBestCreator(comp.getAllBeanProperties(),
                                               CreationDescriptor.CHANGED_ONLY);

                Class[] exceptions = creator.getExceptionTypes();
                if (insideMethod && needTryCode(exceptions)) {
                    if ((varType & declareMask) == CodeVariable.LOCAL) {
                        initCodeWriter.write(varBuf.toString());
                        initCodeWriter.write(";\n"); // NOI18N
                    }
                    initCodeWriter.write("try {\n"); // NOI18N
                    initCodeWriter.write(var.getName());
                }
                else {
                    initCodeWriter.write(varBuf.toString());
                    exceptions = null;
                }

                initCodeWriter.write(" = "); // NOI18N
		
                String[] propNames = creator.getPropertyNames();		
                FormProperty[] props;
                if (propNames.length > 0) {
                    if (constructorProperties == null)
                        constructorProperties = new HashMap();

		    java.util.List usedProperties = new ArrayList(propNames.length);
                    props = new FormProperty[propNames.length];

                    for (int i=0; i < propNames.length; i++) {
                        FormProperty prop = comp.getBeanProperty(propNames[i]);
                        props[i] = prop;
			usedProperties.add(prop);                        
                    }
		    constructorProperties.put(comp, usedProperties);
                }
                else props = RADComponent.NO_PROPERTIES;

                initCodeWriter.write(creator.getJavaCreationCode(props, null) + ";\n"); // NOI18N

                if (exceptions != null)
                    generateCatchCode(exceptions, initCodeWriter.getWriter());
            }
        }

        if (insideMethod) {
            String postCode = (String) comp.getAuxValue(AUX_CREATE_CODE_POST);
            if (postCode != null && !postCode.equals("")) { // NOI18N
                initCodeWriter.write(postCode);
                initCodeWriter.write("\n"); // NOI18N
            }
        }
    }

    private void generateComponentInit(RADComponent comp,
                                       CodeWriter initCodeWriter)
        throws IOException
    {             
        if (comp instanceof RADVisualContainer) {
            LayoutSupportManager layoutSupport =
                ((RADVisualContainer)comp).getLayoutSupport();

            if (layoutSupport != null && layoutSupport.isLayoutChanged()) {
                Iterator it = layoutSupport.getLayoutCode().getStatementsIterator();
                while (it.hasNext()) {
                    CodeStatement statement = (CodeStatement) it.next();
                    initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
                }
                initCodeWriter.write("\n"); // NOI18N
            }
        }

        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        String preCode = (String) comp.getAuxValue(AUX_INIT_CODE_PRE);
        String postCode = (String) comp.getAuxValue(AUX_INIT_CODE_POST);
        if (preCode != null && !preCode.equals("")) { // NOI18N
            initCodeWriter.write(preCode);
            initCodeWriter.write("\n"); // NOI18N
        }

        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized
	    java.util.List usedProperties = constructorProperties != null ? (java.util.List)constructorProperties.get(comp) : null;
            Iterator it = comp.getBeanPropertiesIterator(new PropertiesFilter(usedProperties), false);
            while (it.hasNext()) {
                FormProperty prop = (FormProperty) it.next();

                if (!FormUtils.isContainerContentDependentProperty(
                                comp.getBeanClass(), prop.getName()))
                {
                    generatePropertySetter(prop, comp, initCodeWriter);
                }
                else {
                    // hack for properties that can't be set until all
                    // children are added to the container
                    java.util.List propList;
                    if (containerDependentProperties != null)
                        propList = (java.util.List)
                                   containerDependentProperties.get(comp);
                    else {
                        containerDependentProperties = new HashMap();
                        propList = null;
                    }
                    if (propList == null) {
                        propList = new LinkedList();
                        containerDependentProperties.put(comp, propList);
                    }

                    propList.add(prop);
                }
            }
        }

        if ((postCode != null) &&(!postCode.equals(""))) { // NOI18N
            initCodeWriter.write(postCode);
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generateAccessibilityCode(RADComponent comp,
                                           CodeWriter initCodeWriter)
        throws IOException
    {
        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized
            FormProperty[] props;
            if (comp instanceof RADVisualComponent)
                props = ((RADVisualComponent)comp).getAccessibilityProperties();
            else if (comp instanceof RADMenuItemComponent)
                props = ((RADMenuItemComponent)comp).getAccessibilityProperties();
            else return;

            for (int i=0; i < props.length; i++) {
                FormProperty prop = props[i];
                if (prop.isChanged() || prop.getPreCode() != null
                                     || prop.getPostCode() != null)
                    generatePropertySetter(prop, comp, initCodeWriter);
            }
        }
    }

    private void generateComponentAddCode(RADComponent comp,
                                          RADVisualContainer container,
                                          CodeWriter initCodeWriter)
        throws IOException
    {
        if (comp instanceof RADVisualComponent) {
            LayoutSupportManager laysup = container.getLayoutSupport();
            CodeGroup componentCode = laysup != null ?
                laysup.getComponentCode((RADVisualComponent)comp) : null;
            if (componentCode != null) {
                Iterator it = componentCode.getStatementsIterator();
                while (it.hasNext()) {
                    CodeStatement statement = (CodeStatement) it.next();
                    initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
                }
            }
        }
        else if (comp instanceof RADMenuComponent) {
            String menuCode;
            RADMenuComponent menuComp = (RADMenuComponent) comp;
            Class contClass = container.getBeanClass();

            if (menuComp.getMenuItemType() == RADMenuItemComponent.T_JMENUBAR
                    && javax.swing.RootPaneContainer.class.isAssignableFrom(contClass))
                menuCode = "setJMenuBar"; // NOI18N
            else if (menuComp.getMenuItemType() == RADMenuItemComponent.T_MENUBAR
                     && java.awt.Frame.class.isAssignableFrom(contClass))
                menuCode = "setMenuBar"; // NOI18N
            else
                menuCode = null;

            if (menuCode != null) {
                initCodeWriter.write(getComponentInvokeString(container, true));
                initCodeWriter.write(menuCode);
                initCodeWriter.write("("); // NOI18N
                initCodeWriter.write(getComponentParameterString(menuComp, true));
                initCodeWriter.write(");\n"); // NOI18N
            }
        }
    }

    private void generateMenuAddCode(RADComponent comp,
                                     RADMenuComponent container,
                                     CodeWriter initCodeWriter)
        throws IOException
    {
        if (comp instanceof RADMenuItemComponent
            && ((RADMenuItemComponent)comp).getMenuItemType()
                             == RADMenuItemComponent.T_SEPARATOR)
        {   // treat AWT Separator specially - it is not a regular component
            initCodeWriter.write(getComponentInvokeString(container, true));
            initCodeWriter.write("addSeparator();"); // NOI18N
        }
        else {
            initCodeWriter.write(getComponentInvokeString(container, true));
            initCodeWriter.write("add("); // NOI18N
            initCodeWriter.write(getComponentParameterString(comp, true));
            initCodeWriter.write(");\n"); // NOI18N
        }
    }

    private void generatePropertySetter(FormProperty prop,
                                        RADComponent comp,
                                        CodeWriter initCodeWriter)
	throws IOException
    {
	generatePropertySetter(prop, comp, null, initCodeWriter);
    }		    
    
    private void generatePropertySetter(FormProperty prop,
                                        RADComponent comp,
					String setterVariable,
                                        CodeWriter initCodeWriter)
        throws IOException
    {
        // 1. pre-initialization code
        String preCode = prop.getPreCode();
        if (preCode != null) {
            initCodeWriter.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                initCodeWriter.write("\n"); // NOI18N
        }        
        
        // 2. property setter code
        if (prop.isChanged()) {
            String javaStr = null;
	    //Object value = null;	    
	    
	    if(setterVariable == null) {
		setterVariable = getComponentInvokeString(comp, true);
	    }
	
	    Object value = null;
	    try {
		value = prop.getValue();    		    
	    } catch (IllegalAccessException ex) {
		ErrorManager.getDefault().notify(ex); // should not happen
		return;	    
	    } catch (InvocationTargetException ex) {    	    
		ErrorManager.getDefault().notify(ex); // should not happen
		return;                                      	
	    }
	    
	    if( prop.getCurrentEditor() instanceof BeanPropertyEditor && value != null) {		    		    		   		    						
		generatePropertyBeanSetterCode(prop, value, setterVariable, initCodeWriter);		
	    } else if ((javaStr = prop.getWholeSetterCode()) != null) {
                initCodeWriter.write(javaStr);
                if (!javaStr.endsWith("\n")) // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
            }
            // Mnemonics support - start -
            else if (comp != null 
		     && "text".equals(prop.getName()) // NOI18N
                     && canUseMnemonics(comp) && isUsingMnemonics(comp))
            {
                if ((javaStr = prop.getJavaInitializationString()) != null) {
                    initCodeWriter.write("org.openide.awt.Mnemonics.setLocalizedText(" // NOI18N
                        + comp.getName() + ", " + javaStr + ");\n"); // NOI18N
                }
            }
            // Mnemonics support - end -	     
	    else if ((javaStr = prop.getPartialSetterCode()) != null) {	    	    			    			    						
		generateSimpleSetterCode(prop, 
					  javaStr, 
					  setterVariable, 
					  initCodeWriter);

	    } 
        }

        // 3. post-initialization code
        String postCode = prop.getPostCode();
        if (postCode != null) {
            initCodeWriter.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generatePropertyBeanSetterCode(FormProperty prop, 	
						Object value,
						String setterVariable, 						
						CodeWriter initCodeWriter)
	throws IOException
    {
	
	FormProperty[] properties = null;
	Class propertyType = null;
	Object realValue = prop.getRealValue(value);
	propertyType = realValue.getClass();	   
			    
	prop.getCurrentEditor().setValue(value);
	BeanPropertyEditor beanPropertyEditor = (BeanPropertyEditor) prop.getCurrentEditor();	    		    
	properties = (FormProperty[]) beanPropertyEditor.getProperties();	    
	if ((properties == null) || (properties.length == 0)) return;
        
	CreationDescriptor.Creator creator = getPropertyCreator(propertyType, properties);
	java.util.List creatorProperties = getCreatorProperties(creator, properties);
															
	java.util.List remainingProperties = new ArrayList();		
	if(properties !=null) {
	    for (int i = 0; i < properties.length; i++) {
		if( properties[i].isChanged() && 
	            !creatorProperties.contains(properties[i]) ) 
		{		    
		    remainingProperties.add(properties[i]);			
		}
	    }					    						
	}

	String propertyInitializationString = 
		creator.getJavaCreationCode(
		    (FormProperty[])creatorProperties.toArray(new FormProperty[creatorProperties.size()]), prop.getValueType());
	
	String javaStr = "";  // NOI18N
	if(remainingProperties.size() == 0) {		    		    
	    generateSimpleSetterCode(prop, 
				      prop.getWriteMethod().getName() + "(" + propertyInitializationString + ")", // NOI18N 
				      setterVariable, 
				      initCodeWriter);
	} else if(remainingProperties.size() > 0) {
	    generateWholePropertyInitialization(prop, propertyType, setterVariable, 
				 propertyInitializationString, remainingProperties, initCodeWriter);	    	    
	}
    }
            
    private java.util.List getCreatorProperties(CreationDescriptor.Creator creator, FormProperty[] properties) {
	String[] propNames = creator.getPropertyNames();	
	java.util.List creatorProperties; 
	if (propNames.length > 0) {
	    creatorProperties = new ArrayList(propNames.length);		    
	    for (int i=0; i < propNames.length; i++) {
		for (int j = 0; j < properties.length; j++) {
		    if(properties[j].getName().equals(propNames[i])) {
			creatorProperties.add(properties[j]);							
			break;
		    }			    			    
		}                        
	    }
	} else {
	    creatorProperties = new ArrayList(0);
	}
	return creatorProperties;
    }
    
    private CreationDescriptor.Creator getPropertyCreator(Class clazz, FormProperty[] properties) {	
	CreationDescriptor creationDesc = CreationFactory.getDescriptor(clazz);
	return creationDesc.findBestCreator(properties,
					    // XXX CHANGED_ONLY ???
					    CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);	
    }
    
    private void generateWholePropertyInitialization(FormProperty prop,
					      Class propertyType, 
					      String setterVariable,
					      String propertyInitializationString, 
					      java.util.List remainingProperties,
					      CodeWriter initCodeWriter)
	throws IOException					    
    {
	String variableName = formModel.getCodeStructure().getExternalVariableName(propertyType, null, true);

	String javaStr = propertyType.getName() + " " + variableName + " = " + propertyInitializationString; // NOI18N		
	initCodeWriter.write(javaStr);
	initCodeWriter.write(";\n"); // NOI18N		    		

	for (Iterator it = remainingProperties.iterator(); it.hasNext();) {
	    generatePropertySetter((FormProperty) it.next(), null, variableName + ".", initCodeWriter); // NOI18N 	
	}

	generateSimpleSetterCode(prop, 
				  prop.getWriteMethod().getName() + "(" + variableName + ")", // NOI18N 
				  setterVariable, 
				  initCodeWriter);	    
    }
    
    private void generateSimpleSetterCode(FormProperty prop, 
					   String partialSetterCode, 
	                                   String setterVariable, 
					   CodeWriter initCodeWriter)
	throws IOException
    {

	// if the setter throws checked exceptions,
	// we must generate try/catch block around it.
	Class[] exceptions = null;
	Method writeMethod = prop.getWriteMethod(); 
	if (writeMethod != null) {
	    exceptions = writeMethod.getExceptionTypes();
	    if (needTryCode(exceptions))
		initCodeWriter.write("try {\n"); // NOI18N
	    else
		exceptions = null;
	}

	initCodeWriter.write(setterVariable + partialSetterCode + ";\n"); // NOI18N

	// add the catch code if needed
	if (exceptions != null)
	    generateCatchCode(exceptions, initCodeWriter.getWriter());
    }    
    
    // generates code for handling events of one component
    // (all component.addXXXListener() calls)
    private void generateComponentEvents(RADComponent component,
                                         Writer initCodeWriter)
        throws IOException
    {
        EventSetDescriptor lastEventSetDesc = null;
        java.util.List listenerEvents = null;

        // we must deal somehow with the fact that for some (pathological)
        // events only anonymous innerclass listener can be generated
        // (CEDL cannot be used)
        int defaultMode = formModel.getSettings().getListenerGenerationStyle();
        int mode = defaultMode;
        boolean mixedMode = false;

        Event[] events = component.getKnownEvents();
        for (int i=0; i < events.length; i++) {
            Event event = events[i];
            if (!event.hasEventHandlers())
                continue;

            EventSetDescriptor eventSetDesc = event.getEventSetDescriptor();
            if (eventSetDesc != lastEventSetDesc) {
                if (lastEventSetDesc != null) {
                    // new listener encountered, generate the previous one
                    generateListenerAddCode(component, lastEventSetDesc, listenerEvents, mode, initCodeWriter);
                    if (mixedMode)
                        generateListenerAddCode(component, lastEventSetDesc, listenerEvents, defaultMode, initCodeWriter);
                    if (listenerEvents != null)
                        listenerEvents.clear();
                }

                lastEventSetDesc = eventSetDesc;
            }

            if (defaultMode != ANONYMOUS_INNERCLASSES)
                if (mode == defaultMode) {
                    if (!event.isInCEDL())
                        mode = ANONYMOUS_INNERCLASSES;
                }
                else if (event.isInCEDL())
                    mixedMode = true;

            if (defaultMode == ANONYMOUS_INNERCLASSES || !event.isInCEDL()) {
                if (listenerEvents == null)
                    listenerEvents = new ArrayList();
                listenerEvents.add(event);
            }
        }

        if (lastEventSetDesc != null) {
            // generate the last listener
            generateListenerAddCode(component, lastEventSetDesc, listenerEvents, mode, initCodeWriter);
            if (mixedMode)
                generateListenerAddCode(component, lastEventSetDesc, listenerEvents, defaultMode, initCodeWriter);
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    // generates complete code for handling one listener
    // (one component.addXXXListener() call)
    private void generateListenerAddCode(RADComponent comp,
                                         EventSetDescriptor eventSetDesc,
                                         java.util.List eventList,
                                         int mode,
                                         Writer codeWriter)
        throws IOException
    {
        Method addListenerMethod = eventSetDesc.getAddListenerMethod();
        Class[] exceptions = addListenerMethod.getExceptionTypes();
        if (needTryCode(exceptions))
            codeWriter.write("try {\n"); // NOI18N
        else
            exceptions = null;

        codeWriter.write(getComponentInvokeString(comp, true));
        codeWriter.write(addListenerMethod.getName());
        codeWriter.write("("); // NOI18N

        switch (mode) {
            case ANONYMOUS_INNERCLASSES:
                codeWriter.write("new "); // NOI18N

                // try to find adpater to use instead of full listener impl
                Class listenerType = eventSetDesc.getListenerType();
                Class adapterClass = BeanSupport.getAdapterForListener(
                                                           listenerType);
                if (adapterClass != null) { // use listener adapter class
                    codeWriter.write(getSourceClassName(adapterClass) + "() {\n"); // NOI18N

                    for (int i=0; i < eventList.size(); i++) {
                        Event event = (Event) eventList.get(i);
                        String[] paramNames = generateListenerMethodHeader(
                                   null, event.getListenerMethod(), codeWriter);
                        generateEventHandlerCalls(event, paramNames, codeWriter, true);
                        codeWriter.write("}\n"); // NOI18N
                    }
                }
                else { // generate full listener implementation (all methods)
                    codeWriter.write(getSourceClassName(listenerType) + "() {\n"); // NOI18N

                    Method[] methods = eventSetDesc.getListenerMethods();
                    for (int i=0; i < methods.length; i++) {
                        Method m = methods[i];
                        Event event = null;
                        for (int j=0; j < eventList.size(); j++) {
                            Event e = (Event) eventList.get(j);
                            if (m.equals(e.getListenerMethod())) {
                                event = e;
                                break;
                            }
                        }
                        String[] paramNames =
                            generateListenerMethodHeader(null, m, codeWriter);
                        if (event != null)
                            generateEventHandlerCalls(event, paramNames, codeWriter, true);
                        codeWriter.write("}\n"); // NOI18N
                    }
                }

                codeWriter.write("}"); // NOI18N
                break;

            case CEDL_INNERCLASS:
                codeWriter.write(getListenerVariableName());
                break;

            case CEDL_MAINCLASS:
                codeWriter.write("this"); // NOI18N
                break;
        }

        codeWriter.write(");\n"); // NOI18N

        if (exceptions != null)
            generateCatchCode(exceptions, codeWriter);
    }

    private RADComponent codeVariableToRADComponent(CodeVariable var) {
        RADComponent metacomp = null;
        Iterator iter = var.getAttachedExpressions().iterator();
        if (iter.hasNext()) {
            Object metaobject = ((CodeExpression)iter.next()).getOrigin().getMetaObject();
            if (metaobject instanceof RADComponent) {
                metacomp = (RADComponent)metaobject;
            }
        }
        return metacomp;
    }

    private void addVariables(CodeWriter variablesWriter)
        throws IOException
    {
        Iterator it = getSortedVariables(CodeVariable.FIELD,
                                         CodeVariable.SCOPE_MASK);

        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            RADComponent metacomp = codeVariableToRADComponent(var);
            if (metacomp == null) continue;

            generateDeclarationPre(variablesWriter, metacomp);

            if ((var.getType() & CodeVariable.FINAL) == CodeVariable.FINAL) {
                // final field variable - add also creation assignment
                generateComponentCreate(metacomp, variablesWriter, false);
            }
            else { // simple field variable declaration
                variablesWriter.write(
                    var.getDeclaration().getJavaCodeString(null, null));
                variablesWriter.write("\n"); // NOI18N
            }

            generateDeclarationPost(variablesWriter, metacomp);
        }
    }

    private boolean addLocalVariables(CodeWriter initCodeWriter)
        throws IOException
    {
        Iterator it = getSortedVariables(
            CodeVariable.LOCAL | CodeVariable.EXPLICIT_DECLARATION,
            CodeVariable.SCOPE_MASK | CodeVariable.DECLARATION_MASK);

        boolean anyVariable = false;
        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            RADComponent metacomp = codeVariableToRADComponent(var);
            if (metacomp != null) {
                generateDeclarationPre(initCodeWriter, metacomp);
            }
            initCodeWriter.write(
                var.getDeclaration().getJavaCodeString(null, null));
            initCodeWriter.write("\n"); // NOI18N
            if (metacomp != null) {
                generateDeclarationPost(initCodeWriter, metacomp);
            }
            anyVariable = true;
        }

        return anyVariable;
    }

    private void generateDeclarationPre(CodeWriter writer, RADComponent metacomp) throws IOException {
        String preCode = (String)metacomp.getAuxValue(AUX_DECLARATION_PRE);
        if (preCode != null && !preCode.equals("")) { // NOI18N
            writer.write(preCode);
            writer.write("\n"); // NOI18N
        }
    }

    private void generateDeclarationPost(CodeWriter writer, RADComponent metacomp) throws IOException {
        String postCode = (String)metacomp.getAuxValue(AUX_DECLARATION_POST);
        if (postCode != null && !postCode.equals("")) { // NOI18N
            writer.write(postCode);
            writer.write("\n"); // NOI18N
        }
    }

    private Iterator getSortedVariables(int type, int typeMask) {
        Collection allVariables = formModel.getCodeStructure().getAllVariables();
        java.util.List variables = new ArrayList(allVariables.size());
        Iterator it = allVariables.iterator();
        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            if (var.getDeclaredType() == org.netbeans.modules.form.Separator.class)
                continue; // treat AWT Separator specially - it is not a component
            if ((var.getType() &  typeMask) == (type & typeMask))
                variables.add(var);
        }
        Collections.sort(variables, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((CodeVariable)o1).getName().compareTo(
                        ((CodeVariable)o2).getName());
            }
        });
        return variables.iterator();
    }

    // Mnemonics support - start -
    static boolean canUseMnemonics(RADComponent comp) {
        return javax.swing.JLabel.class.isAssignableFrom(comp.getBeanClass())
               || javax.swing.AbstractButton.class.isAssignableFrom(comp.getBeanClass());
    }

    static boolean isUsingMnemonics(RADComponent comp) {
        Object mnem = comp.getAuxValue(PROP_GENERATE_MNEMONICS);
        if (mnem != null)
            return Boolean.TRUE.equals(mnem);

        return comp.getFormModel().getSettings().getGenerateMnemonicsCode();
    }
    // Mnemonics support - end -

    private String getComponentParameterString(RADComponent component,
                                               boolean inMainClass)
    {
        if (component == formModel.getTopRADComponent())
            return inMainClass ?
                     "this" : // NOI18N
                     formEditorSupport.getFormDataObject().getName() + ".this"; // NOI18N
        else
            return component.getName();
    }

    private String getComponentInvokeString(RADComponent component,
                                            boolean inMainClass)
    {
        if (component == formModel.getTopRADComponent())
            return inMainClass ?
                     "" : // NOI18N
                     formEditorSupport.getFormDataObject().getName() + ".this."; // NOI18N
        else
            return component.getName() + "."; // NOI18N
    }

    static String getSourceClassName(Class cls) {
        return cls.getName().replace('$', '.').replace('+', '.').replace('/', '.'); // NOI18N
    }

    private static String getVariablesHeaderComment() {
        if (variablesHeader == null)
            variablesHeader = FormUtils.getBundleString("MSG_VariablesBegin"); // NOI18N
        return variablesHeader;
    }

    private static String getVariablesFooterComment() {
        if (variablesFooter == null)
            variablesFooter = FormUtils.getBundleString("MSG_VariablesEnd"); // NOI18N
        return variablesFooter;
    }

    private static String getEventDispatchCodeComment() {
        if (eventDispatchCodeComment == null)
            eventDispatchCodeComment = FormUtils.getBundleString("MSG_EventDispatchCodeComment"); // NOI18N
        return eventDispatchCodeComment;
    }

    private boolean needTryCode(Class[] exceptions) {
        if (exceptions != null)
            for (int i=0; i < exceptions.length; i++)
                if (Exception.class.isAssignableFrom(exceptions[i])
                    && !RuntimeException.class.isAssignableFrom(exceptions[i]))
                {
                    return true;
                }

        return false;
    }

    private void generateCatchCode(Class[] exceptions, Writer initCodeWriter)
        throws IOException
    {
        initCodeWriter.write("}"); // NOI18N
        for (int i=0, exCount=0; i < exceptions.length; i++) {
            Class exception = exceptions[i];
            if (!Exception.class.isAssignableFrom(exception)
                    || RuntimeException.class.isAssignableFrom(exception))
                continue; // need not be caught

            if (i > 0) {
                int j;
                for (j=0; j < i; j++)
                    if (exceptions[j].isAssignableFrom(exception))
                        break;
                if (j < i)
                    continue; // a subclass of this exception already caught
            }

            initCodeWriter.write(" catch ("); // NOI18N
            initCodeWriter.write(getSourceClassName(exception));
            initCodeWriter.write(" "); // NOI18N

            String varName = "e" + ++exCount; // NOI18N

            initCodeWriter.write(varName);
            initCodeWriter.write(") {\n"); // NOI18N
            initCodeWriter.write(varName);
            initCodeWriter.write(".printStackTrace();\n"); // NOI18N
            initCodeWriter.write("}"); // NOI18N
                        
        }
        initCodeWriter.write("\n"); // NOI18N
    }

    private void addDispatchListenerDeclaration(Writer codeWriter)
        throws IOException
    {
        listenerVariableName = null;
        codeWriter.write(getListenerClassName());
        codeWriter.write(" "); // NOI18N
        codeWriter.write(getListenerVariableName());
        codeWriter.write(" = new "); // NOI18N
        codeWriter.write(getListenerClassName());
        codeWriter.write("();\n"); // NOI18N
    }

    private void generateDispatchListenerCode(Writer codeWriter)
        throws IOException
    {   // always ends up with } as last character (no new line - because of fold footer)
        FormEvents formEvents = formModel.getFormEvents();
        boolean innerclass = formModel.getSettings().getListenerGenerationStyle() == CEDL_INNERCLASS;
        boolean mainclass = formModel.getSettings().getListenerGenerationStyle() == CEDL_MAINCLASS;

        Class[] listenersToImplement = formEvents.getCEDLTypes();
        Arrays.sort(listenersToImplement, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Class)o1).getName().compareTo(((Class)o2).getName());
            }
        });

        listenersInMainClass = mainclass ? listenersToImplement : null;

        if (innerclass) {
            String listenerClassName = getListenerClassName();
            codeWriter.write("private class "); // NOI18N
            codeWriter.write(listenerClassName);
            codeWriter.write(" implements "); // NOI18N
            for (int i=0; i < listenersToImplement.length; i++) {
                codeWriter.write(getSourceClassName(listenersToImplement[i]));
                if (i + 1 < listenersToImplement.length)
                    codeWriter.write(", "); // NOI18N
            }
            codeWriter.write(" {\n"); // NOI18N
            codeWriter.write(listenerClassName + "() {}\n"); // NOI18N Issue 72346 resp. 15242
        }

        for (int i=0; i < listenersToImplement.length; i++) {
            boolean implementedInSuperclass =
                mainclass && listenersToImplement[i].isAssignableFrom(
                                          formModel.getFormBaseClass());

            Method[] methods = listenersToImplement[i].getMethods();
            Arrays.sort(methods, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((Method)o1).getName().compareTo(((Method)o2).getName());
                }
            });

            for (int j=0; j < methods.length; j++) {
                Method method = methods[j];
                Event[] events = formEvents.getEventsForCEDLMethod(method);
                if (implementedInSuperclass && events.length == 0)
                    continue;

                String[] paramNames =
                    generateListenerMethodHeader(null, method, codeWriter);

                for (int k=0; k < events.length; k++) {
                    Event event = events[k];
                    if (k + 1 < events.length
                        || method.getReturnType() == Void.TYPE)
                    {                                               
			String componentParameterString = getComponentParameterString(
							    event.getComponent(), false);
			
			CodeVariable variable = event.getComponent().getCodeExpression().getVariable();                                                
			if( variable!= null && ( (variable.getType() & CodeVariable.LOCAL) == CodeVariable.LOCAL) ) {
			    codeWriter.write(
				FormUtils.getFormattedBundleString(
				    "MSG_WrongLocalVariableSettingComment", // NOI18N
				    new Object[] { componentParameterString }));
			}
			
                        codeWriter.write(k == 0 ? "if (" : "else if ("); // NOI18N
                        codeWriter.write(paramNames[0]);
                        codeWriter.write(".getSource() == "); // NOI18N
                        codeWriter.write(componentParameterString);
                        codeWriter.write(") {\n"); // NOI18N						

                        generateEventHandlerCalls(event, paramNames, codeWriter, false);
                        codeWriter.write("}\n"); // NOI18N
                        
                    }
                    else { // the listener method returns something
                        if (k > 0)
                            codeWriter.write("else {\n"); // NOI18N
                        generateEventHandlerCalls(event, paramNames, codeWriter, false);
                        if (k > 0)
                            codeWriter.write("}\n"); // NOI18N
                    }
                }
                if (implementedInSuperclass)
                    generateSuperListenerCall(method, paramNames, codeWriter);

                if (j+1 < methods.length || i+1 < listenersToImplement.length)
                    codeWriter.write("}\n\n"); // NOI18N
                else if (innerclass)
                    codeWriter.write("}\n"); // NOI18N
                else
                    codeWriter.write("}"); // last char // NOI18N
            }
        }

        if (innerclass)
            codeWriter.write("}"); // last char NOI18N
    }

    // modifies the form class declaration to implement required listeners
    // (when event dispatching code is generated as CEDL_MAINCLASS)
    private void ensureMainClassImplementsListeners() {
        if (listenersInMainClass == listenersInMainClass_lastSet)
            return; // no change from last time

        if (listenersInMainClass != null
            && listenersInMainClass_lastSet != null
            && listenersInMainClass.length == listenersInMainClass_lastSet.length)
        {
            boolean different = false;
            for (int i=0; i < listenersInMainClass.length; i++)
                if (listenersInMainClass[i] != listenersInMainClass_lastSet[i]) {
                    different = true;
                    break;
                }
            if (!different)
                return; // no change from last time
        }

        if (mainClassElement == null) {
            String name = formEditorSupport.getFormDataObject().getName();
            ClassElement[] topClasses =
                formEditorSupport.getFormDataObject().getSource().getClasses();
            for (int i=0; i < topClasses.length; i++)
                if (topClasses[i].getName().getName().equals(name)) {
                    mainClassElement = topClasses[i];
                    break;
                }
            if (mainClassElement == null) // still null? hmm...
                return;
        }

        java.util.List toRemove = null;
        if (listenersInMainClass_lastSet != null)
            for (int i=0; i < listenersInMainClass_lastSet.length; i++) {
                Class cls = listenersInMainClass_lastSet[i];
                boolean remains = false;
                if (listenersInMainClass != null)
                    for (int j=0; j < listenersInMainClass.length; j++)
                        if (cls == listenersInMainClass[j]) {
                            remains = true;
                            break;
                        }
                if (!remains) {
                    if (toRemove == null)
                        toRemove = new ArrayList();
                    toRemove.add(cls.getName());
                }
            }

        Identifier[] actualInterfaces = mainClassElement.getInterfaces();
        java.util.List interfacesToBeSet = new ArrayList();

        // first take the current interfaces and exclude the removed ones
        for (int i=0; i < actualInterfaces.length; i++)
            if (toRemove == null
                    || !toRemove.contains(actualInterfaces[i].getFullName()))
                interfacesToBeSet.add(actualInterfaces[i]);

        // then ensure all required interfaces are present
        if (listenersInMainClass != null)
            for (int i=0; i < listenersInMainClass.length; i++) {
                String name = listenersInMainClass[i].getName();
                boolean alreadyIn = false;
                for (int j=0; j < actualInterfaces.length; j++)
                    if (name.equals(actualInterfaces[j].getFullName())) {
                        alreadyIn = true;
                        break;
                    }
                if (!alreadyIn)
                    interfacesToBeSet.add(Identifier.create(name));
            }

        Identifier[] newInterfaces = new Identifier[interfacesToBeSet.size()];
        interfacesToBeSet.toArray(newInterfaces);
        try {
            mainClassElement.setInterfaces(newInterfaces);
        }
        catch (SourceException ex) {}

        listenersInMainClass_lastSet = listenersInMainClass;
    }

    // ---------
    // generating general code structure (metadata from codestructure package)

    // java code for a statement
    private static String getStatementJavaString(CodeStatement statement,
                                                 String thisStr)
    {
        CodeExpression parent = statement.getParentExpression();
        String parentStr;
        if (parent != null) {
            parentStr = getExpressionJavaString(parent, thisStr);
            if ("this".equals(parentStr)) // NOI18N
                parentStr = thisStr;
        }
        else parentStr = null;

        CodeExpression[] params = statement.getStatementParameters();
        String[] paramsStr = new String[params.length];
        for (int i=0; i < params.length; i++)
            paramsStr[i] = getExpressionJavaString(params[i], thisStr);

        return statement.getJavaCodeString(parentStr, paramsStr);
    }

    // java code for an expression
    private static String getExpressionJavaString(CodeExpression exp,
                                                  String thisStr)
    {
        CodeVariable var = exp.getVariable();
        if (var != null)
            return var.getName();

        CodeExpressionOrigin origin = exp.getOrigin();
        if (origin == null)
            return null;

        CodeExpression parent = origin.getParentExpression();
        String parentStr;
        if (parent != null) {
            parentStr = getExpressionJavaString(parent, thisStr);
            if ("this".equals(parentStr)) // NOI18N
                parentStr = thisStr;
        }
        else parentStr = null;

        CodeExpression[] params = origin.getCreationParameters();
        String[] paramsStr = new String[params.length];
        for (int i=0; i < params.length; i++)
            paramsStr[i] = getExpressionJavaString(params[i], thisStr);

        return origin.getJavaCodeString(parentStr, paramsStr);
    }

    // ---------
    // Events

    private boolean anyEvents() {
        return formModel.getFormEvents().hasEventsInCEDL();
    }

    private String getListenerClassName() {
        if (listenerClassName == null) {
            String initText = initComponentsSection.getText();
            int index = initText.lastIndexOf("private class "); // NOI18N
            if (index >= 0) {
                StringBuffer nameBuffer = new StringBuffer(16);
                index += "private class ".length(); // NOI18N

                int length = initText.length();
                while (index < length && initText.charAt(index) == ' ')
                    index++;

                int i = index;
                while (i < length && initText.charAt(i) != ' ')
                    nameBuffer.append(initText.charAt(i++));

                if (i < length)
                    listenerClassName = nameBuffer.toString();
            }

            if (listenerClassName == null) {
                javax.swing.text.Document document = formEditorSupport.getDocument();
                try {
                    String wholeText = document.getText(0, document.getLength());
                    listenerClassName = DEFAULT_LISTENER_CLASS_NAME;
                    while (wholeText.indexOf(listenerClassName) >= 0)
                        listenerClassName = "_" + listenerClassName; // NOI18N
                }
                catch (javax.swing.text.BadLocationException ex) {} // ignore
            }

            if (listenerClassName == null)
                listenerClassName = DEFAULT_LISTENER_CLASS_NAME;
        }

        return listenerClassName;
    }

    private String getListenerVariableName() {
        if (listenerVariableName == null) {
            listenerVariableName = "formListener"; // NOI18N
            CodeStructure codeStructure = formModel.getCodeStructure();
            for (int i=1; codeStructure.isVariableNameReserved(listenerVariableName); i++)
                listenerVariableName = "formListener" + i; // NOI18N
        }
        return listenerVariableName;
    }

    // -----------------------------------------------------------------------------
    // Event handlers

    /** Generates the specified event handler.
     */
    private void generateEventHandler(String handlerName,
                                      Method originalMethod,
                                      String bodyText)
    {
        if (!initialized || !canGenerate)
            return;

        JavaEditor.InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null && bodyText == null)
            return; // already exists, no need to generate

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        Writer codeWriter = engine.createWriter(
                        formEditorSupport.getDocument(),
                        initComponentsSection.getPositionAfter().getOffset(),
                        buffer);

        try {
            if (sec == null)
                sec = formEditorSupport.createInteriorSectionAfter(
                          initComponentsSection,
                          getEventSectionName(handlerName));
            int i1, i2;

            generateListenerMethodHeader(handlerName, originalMethod, codeWriter);
            codeWriter.flush();
            i1 = buffer.getBuffer().length();
            if (bodyText == null)
                bodyText = getDefaultEventBody();
            codeWriter.write(bodyText);
            codeWriter.flush();
            i2 = buffer.getBuffer().length();
            codeWriter.write("}\n"); //NOI18N // footer with new line
            codeWriter.flush();

            sec.setHeader(buffer.getBuffer().substring(0,i1));
            sec.setBody(buffer.getBuffer().substring(i1,i2));
            sec.setBottom(buffer.getBuffer().substring(i2));

            codeWriter.close();
        } 
        catch (javax.swing.text.BadLocationException e) {
            return;
        }
        catch (java.io.IOException ioe) {
            return;
        }

        clearUndo();
    }

    /** Removes the specified event handler - removes the whole method together with the user code!
     * @param handlerName The name of the event handler
     */
    private boolean deleteEventHandler(String handlerName) {
        JavaEditor.InteriorSection section = getEventHandlerSection(handlerName);
        if (section == null || !initialized || !canGenerate)
            return false;

        section.deleteSection();
        clearUndo();

        return true;
    }

    private String getDefaultEventBody() {
        return FormUtils.getBundleString("MSG_EventHandlerBody"); // NOI18N
    }

    /** Renames the specified event handler to the given new name.
     * @param oldHandlerName The old name of the event handler
     * @param newHandlerName The new name of the event handler
     */
    private void renameEventHandler(String oldHandlerName,
                                    String newHandlerName)
    {
        JavaEditor.InteriorSection sec = getEventHandlerSection(oldHandlerName);
        if (sec == null || !initialized || !canGenerate)
            return;

        String header = sec.getHeader();

        // find the old handler name in the handler method header
        int index = header.indexOf('(');
        if (index < 0)
            return; // should not happen unless the handler code is corrupted
        index = header.substring(0, index).lastIndexOf(oldHandlerName);
        if (index < 0)
            return; // old name not found; should not happen

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        Writer codeWriter = engine.createWriter(formEditorSupport.getDocument(),
                                                sec.getPositionBefore().getOffset(),
                                                buffer);
        try {
            codeWriter.write(header.substring(0, index));
            codeWriter.write(newHandlerName);
            codeWriter.write(header.substring(index + oldHandlerName.length()));
            codeWriter.flush();
            int i1 = buffer.getBuffer().length();
            codeWriter.write("}\n"); // NOI18N // footer with new line
            codeWriter.flush();

            sec.setHeader(buffer.getBuffer().substring(0, i1));
            sec.setBottom(buffer.getBuffer().substring(i1));
            sec.setName(getEventSectionName(newHandlerName));

            codeWriter.close();
        } 
        catch (java.beans.PropertyVetoException e) {
            return;
        }
        catch (IOException e) {
            return;
        }

        clearUndo();
    }

    /** Focuses the specified event handler in the editor. */
    private void gotoEventHandler(String handlerName) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null && initialized)
            sec.openAt();
    }

    /** Gets the body (text) of event handler of given name. */
    String getEventHandlerText(String handlerName) {
        JavaEditor.InteriorSection section = getEventHandlerSection(handlerName);
        if (section != null) {
            String tx = section.getText();
            tx = tx.substring(tx.indexOf("{")+1, tx.lastIndexOf("}")).trim() + "\n"; // NOI18N
            return tx;
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------
    // Private methods

    /** Clears undo buffer after code generation */
    private void clearUndo() {
        formEditorSupport.discardEditorUndoableEdits();
    }

    // sections acquirement

    private JavaEditor.InteriorSection getEventHandlerSection(String eventName) {
        return formEditorSupport.findInteriorSection(getEventSectionName(eventName));
    }

    // other

    private String getEventSectionName(String handlerName) {
        return EVT_SECTION_PREFIX + handlerName;
    }

    private String[] generateListenerMethodHeader(String methodName,
                                                  Method originalMethod,
                                                  Writer writer)
        throws IOException
    {
        Class[] paramTypes = originalMethod.getParameterTypes();
        String[] paramNames;

        if (paramTypes.length == 1
            && EventObject.class.isAssignableFrom(paramTypes[0]))
        {
            paramNames = new String[] { formSettings.getEventVariableName() };
        }
        else {
            paramNames = new String[paramTypes.length];
            for (int i=0; i < paramTypes.length; i++)
                paramNames[i] = "param" + i; // NOI18N
        }

        // generate the method
        writer.write(methodName != null ? "private " : "public "); // NOI18N
        writer.write(getSourceClassName(originalMethod.getReturnType()));
        writer.write(" "); // NOI18N
        writer.write(methodName != null ? methodName : originalMethod.getName());
        writer.write("("); // NOI18N

        for (int i=0; i < paramTypes.length; i++) {
            writer.write(getSourceClassName(paramTypes[i]));
            writer.write(" "); // NOI18N
            writer.write(paramNames[i]);
            if (i + 1 < paramTypes.length)
                writer.write(", "); // NOI18N
        }
        writer.write(")"); // NOI18N

        Class[] exceptions = originalMethod.getExceptionTypes();
        if (exceptions.length != 0) {
            writer.write("throws "); // NOI18N
            for (int i=0; i < exceptions.length; i++) {
                writer.write(getSourceClassName(exceptions[i]));
                if (i + 1 < exceptions.length)
                    writer.write(", "); // NOI18N
            }
        }

        writer.write(" {\n"); // NOI18N

        return paramNames;
    }

    private void generateSuperListenerCall(Method method,
                                           String[] paramNames,
                                           Writer codeWriter)
        throws IOException
    {
        if (method.getReturnType() != Void.TYPE)
            codeWriter.write("return "); // NOI18N

        codeWriter.write("super."); // NOI18N
        codeWriter.write(method.getName());
        codeWriter.write("("); // NOI18N

        for (int i=0; i < paramNames.length; i++) {
            codeWriter.write(paramNames[i]);
            if (i + 1 < paramNames.length)
                codeWriter.write(", "); // NOI18N
        }

        codeWriter.write(");\n"); // NOI18N
    }

    private void generateEventHandlerCalls(Event event,
                                           String[] paramNames,
                                           Writer codeWriter,
                                           boolean useShortNameIfPossible)
        throws IOException
    {
        String mainClassRef = null;

        String[] handlers = event.getEventHandlers();
        for (int i=0; i < handlers.length; i++) {
            if (i + 1 == handlers.length
                    && event.getListenerMethod().getReturnType() != Void.TYPE)
                codeWriter.write("return "); // NOI18N

            // with anonymous innerclasses, try to avoid generating full names
            // (for the reason some old forms might be used as innerclasses)
            if (!useShortNameIfPossible
                || event.getListenerMethod().getName().equals(handlers[i]))
            {
                if (mainClassRef == null)
                    mainClassRef = formEditorSupport.getFormDataObject().getName()
                                   + ".this."; // NOI18N
                codeWriter.write(mainClassRef);
            }
            codeWriter.write(handlers[i]);
            codeWriter.write("("); // NOI18N

            for (int j=0; j < paramNames.length; j++) {
                codeWriter.write(paramNames[j]);
                if (j + 1 < paramNames.length)
                    codeWriter.write(", "); // NOI18N
            }

            codeWriter.write(");\n"); // NOI18N
        }
    }
    
    void regenerateCode() {
        if (!codeUpToDate) {	    
            codeUpToDate = true;
            regenerateVariables();
            regenerateInitComponents();
            ensureMainClassImplementsListeners();            
            FormModel.t("code regenerated"); //NOI18N	    
        }
    }
    
    private String indentCode(String code, IndentEngine refEngine) {
        int spacesPerTab = 4;
        boolean braceOnNewLine = false;
        
        if (refEngine != null) {
            Class engineClass = refEngine.getClass();
            try {
                Method m = engineClass.getMethod("getSpacesPerTab", // NOI18N
                        new Class[0]);
                spacesPerTab = ((Integer)m.invoke(refEngine, new Object[0]))
                .intValue();
            } catch (Exception ex) {} // ignore
            
            try {
                Method m = engineClass.getMethod("getJavaFormatNewlineBeforeBrace", // NOI18N
                        new Class[0]);
                braceOnNewLine = ((Boolean)m.invoke(refEngine, new Object[0]))
                .booleanValue();
            } catch (Exception ex) {} // ignore
        }
        
        StringBuffer tab = new StringBuffer(spacesPerTab);
        for (int i=0; i < spacesPerTab; i++)
            tab.append(" "); // NOI18N
        
        return doIndentation(code, 1, tab.toString(), braceOnNewLine);
    }
    
    // simple indentation method
    private static String doIndentation(String code,
            int minIndentLevel,
            String tab,
            boolean braceOnNewLine) {

        int indentLevel = minIndentLevel;
        boolean lastLineEmpty = false;
        int codeLength = code.length();
        StringBuffer buffer = new StringBuffer(codeLength);
        
        int i = 0;
        while (i < codeLength) {
            int lineStart = i;
            int lineEnd;
            boolean startingSpace = true;
            boolean firstClosingBr = false;
            boolean closingBr = false;
            int lastOpeningBr = -1;
            int endingSpace = -1;
            boolean insideString = false;
            int brackets = 0;
            char c;
            
            do { // go through one line
                c = code.charAt(i);
                if (!insideString) {
                    if (c == '}') {
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        } else if (!closingBr)
                            brackets--;
                    } else if (c == ')') {
                        
                        int bracketCount = 0;
                        int begin = i;
                        while (begin < code.length() && (code.charAt(begin) == ')')) {
                            bracketCount += 1;
                            begin += 1;
                        }
                        
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        } else if (!closingBr)
                            brackets -= bracketCount;
                        if (bracketCount > 1) {
                            i += bracketCount - 1;
                        }
                    } else if (c == '{' || c == '(') {
                        closingBr = false;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        } else if (c == '{') // possible last brace on the line
                            lastOpeningBr = i;
                        brackets++;
                    } else if (c == '\"') { // start of String, its content is ignored
                        insideString = true;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        
                    } else if (c == ' ' || c == '\t') {
                        if (endingSpace < 0)
                            endingSpace = i;
                    } else {
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        if (c != '\n') { // this char is not a whitespace
                            endingSpace = -1;
                            if (lastOpeningBr > -1)
                                lastOpeningBr = -1;
                        }
                    }
                } else if (c == '\"' && code.charAt(i-1) != '\\') // end of String
                    insideString = false;
                
                i++;
            }
            while (c != '\n' && i < codeLength);
            
            if ((i-1 == lineStart && code.charAt(lineStart) == '\n')
            || (i-2 == lineStart && code.charAt(lineStart) == '\r')) {
                // the line is empty
                if (!lastLineEmpty) {
                    buffer.append("\n"); // NOI18N
                    lastLineEmpty = true;
                }
                continue; // skip second and more empty lines
            } else lastLineEmpty = false;
            
            // adjust indentation level for the line
            if (firstClosingBr) { // the line starts with } or )
                if (indentLevel > minIndentLevel)
                    indentLevel--;
                if (brackets < 0)
                    brackets = 0; // don't change indentation for the next line
            }
            
            // write indentation space
            for (int j=0; j < indentLevel; j++)
                buffer.append(tab);
            
            if (lastOpeningBr > -1 && braceOnNewLine) {
                // write the line without last opening brace
                // (indentation option "Add New Line Before Brace")
                endingSpace = lastOpeningBr;
                c = code.charAt(endingSpace-1);
                while (c == ' ' || c == '\t') {
                    endingSpace--;
                    c = code.charAt(endingSpace-1);
                }
                i = lastOpeningBr;
                brackets = 0;
            }
            
            // calculate line end
            if (endingSpace < 0) {
                if (c == '\n')
                    if (code.charAt(i-2) == '\r')
                        lineEnd = i-2; // \r\n at the end of the line
                    else
                        lineEnd = i-1; // \n at the end of the line
                else
                    lineEnd = i; // end of whole code string
            } else // skip spaces at the end of the line
                lineEnd = endingSpace;
            
            // write the line
            buffer.append(code.substring(lineStart, lineEnd));
            buffer.append("\n"); // NOI18N
            
            // calculate indentation level for the next line
            if (brackets < 0) {
                if (indentLevel > minIndentLevel)
                    indentLevel += brackets;
                
            } else if (brackets > 0)
                indentLevel++;
        }
        return buffer.toString();
    }

    private String replaceCode(String code, Writer initWriter) throws IOException {
        int idx = code.indexOf(RESOURCE_BUNDLE_OPENING_CODE);
        if (idx >= 0) {
            int endIdx = code.indexOf(RESOURCE_BUNDLE_CLOSING_CODE,
                                      idx + RESOURCE_BUNDLE_OPENING_CODE.length());
            if (endIdx >= 0) {
                String bundleLocation = code.substring(idx + RESOURCE_BUNDLE_OPENING_CODE.length(), endIdx);
                if (bundleVariables == null) {
                    bundleVariables = new HashMap();
                }
                String varName = bundleVariables.get(bundleLocation);
                if (varName == null) {
                    varName = formModel.getCodeStructure().getExternalVariableName(ResourceBundle.class, "bundle", true); // NOI18N
                    bundleVariables.put(bundleLocation, varName);
                    initWriter.write("java.util.ResourceBundle " + varName + " = " // NOI18N
                            + code.substring(idx, endIdx + 1) + "; // NOI18N\n"); // NOI18N
                }
                code = code.substring(0, idx) + varName + code.substring(endIdx + 1);
            }
        }
        return code;
    }

    /**
     * Class for filtering generated code - processing special marks in the code
     * (provided by properties/property editors). This way e.g. code for
     * ResourceBundle.getBundle is optimized (caching the bundle in a variable)
     * or line comments for property setters are placed correctly.
     * [In future pre-init and post-init code could be done this way as well
     *  (and it would work also for nested properties or layout constraints).]
     * To work correctly, this class requires to be given complete statements
     * (so it can add a preceding or following statement).
     */
    private class CodeWriter {
        private Writer writer;
        private boolean inMethod;

        CodeWriter(Writer writer, boolean method) {
            this.writer = writer;
            this.inMethod = method;
        }

        void write(String str) throws IOException {
            int idx = str.indexOf(CODE_MARK);
            if (idx >= 0) {
                StringBuilder buf = new StringBuilder(str.length());
                if (idx > 0) {
                    buf.append(str.substring(0, idx));
                }
                String lineComment = null;

                do {
                    String part;
                    if (str.startsWith(MARKED_PROPERTY_CODE, idx)) {
                        int sub = idx + MARKED_PROPERTY_CODE.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        part = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                        if (inMethod) {
                            part = replaceCode(part, writer);
                        } // can't replace in field variable init
                    }
                    else if (str.startsWith(PROPERTY_LINE_COMMENT, idx)) {
                        int sub = idx + PROPERTY_LINE_COMMENT.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        String lc = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                        if (lineComment == null)
                            lineComment = lc;
                        else if (!lineComment.equals(lc))
                            lineComment = lineComment + " " + lc; // NOI18N
                        continue;
                    }
                    else {
                        int sub = idx;
                        idx = str.indexOf(CODE_MARK, sub);
                        part = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                    }
                    if (lineComment != null) {
                        int eol = part.indexOf('\n');
                        if (eol >= 0) {
                            buf.append(part.substring(0, eol));
                            buf.append(" // "); // NOI18N
                            buf.append(lineComment);
                            buf.append("\n"); // NOI18N
                            part = part.substring(eol+1);
                            lineComment = null;
                        }
                    }
                    buf.append(part);
                }
                while (idx >= 0);

                if (lineComment != null) {
                    buf.append(" // "); // NOI18N
                    buf.append(lineComment);
                }

                str = buf.toString();
            }
            writer.write(str);
        }

        Writer getWriter() {
            return writer;
        }
    }

    //
    // {{{ FormListener
    //

    private class FormListener implements FormModelListener {

        public void formChanged(FormModelEvent[] events) {
            if (events == null)
                return;

            boolean modifying = false;
            boolean toBeSaved = false;
            boolean toBeClosed = false;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];

                // form loaded
                if (ev.getChangeType() == FormModelEvent.FORM_LOADED) {
                    if (formModel.getSettings().getListenerGenerationStyle() == CEDL_MAINCLASS)
                        listenersInMainClass_lastSet =
                            formModel.getFormEvents().getCEDLTypes();
                    continue;
                }

                if (ev.isModifying())
                    modifying = true;

                if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_ADDED) {
                    String handlerName = ev.getEventHandler();
                    String bodyText = ev.getNewEventHandlerContent();
                    if ((ev.getCreatedDeleted() || bodyText != null) && ev.getComponent().isInModel()) {
                        if (!ev.getCreatedDeleted())
                            ev.setOldEventHandlerContent(
                                getEventHandlerText(handlerName));

                        generateEventHandler(handlerName,
                                            (ev.getComponentEvent() == null) ?
                                                formModel.getFormEvents().getOriginalListenerMethod(handlerName) :
                                                ev.getComponentEvent().getListenerMethod(),
                                             bodyText);
                    }
                    if (events.length == 1 && bodyText == null)
                        gotoEventHandler(handlerName);
                }
                else if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_REMOVED) {
                    if (ev.getCreatedDeleted()) {
                        String handlerName = ev.getEventHandler();
                        ev.setOldEventHandlerContent(
                            getEventHandlerText(handlerName));
                        deleteEventHandler(handlerName);
                    }
                }
                else if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_RENAMED) {
                    renameEventHandler(ev.getOldEventHandler(),
                                       ev.getNewEventHandler());
                }
                else if (ev.getChangeType() == FormModelEvent.FORM_TO_BE_SAVED)
                    toBeSaved = true;
                else if (ev.getChangeType() == FormModelEvent.FORM_TO_BE_CLOSED)
                    toBeClosed = true;
            }

            if (modifying)
                codeUpToDate = false;

            if ((!codeUpToDate && toBeSaved) || (isJavaEditorDisplayed())) {
		regenerateCode();
            }

            if (toBeSaved) {
                RADComponent[] components =
                    formModel.getModelContainer().getSubBeans();
                for (int i=0; i < components.length; i++)
                    serializeComponentsRecursively(components[i]);
            }
        }
        
        private boolean isJavaEditorDisplayed() {
            boolean showing = false;
            JEditorPane[] jeditPane = FormEditor.getFormDataObject(formModel).getJavaEditor().getOpenedPanes();
            if (jeditPane != null) {
                for (int i=0; i<jeditPane.length; i++) {
                    if (showing = jeditPane[i].isShowing()) {
                        break;
                    }
                }
            }
            return showing;
        }
        
        private void serializeComponentsRecursively(RADComponent comp) {
            Object value = comp.getAuxValue(AUX_CODE_GENERATION);
            if (comp.hasHiddenState()
                    || (value != null && VALUE_SERIALIZE.equals(value))) {
                String serializeTo =(String)comp.getAuxValue(AUX_SERIALIZE_TO);
                if (serializeTo != null) {
                    try {
                        FileObject fo = formEditorSupport.getFormDataObject().getPrimaryFile();
                        FileObject serFile = fo.getParent().getFileObject(serializeTo, "ser"); // NOI18N
                        if (serFile == null) {
                            serFile = fo.getParent().createData(serializeTo, "ser"); // NOI18N
                        }
                        if (serFile != null) {
                            FileLock lock = null;
                            ObjectOutputStream oos = null;
                            try {
                                lock = serFile.lock();
                                oos = new OOS(serFile.getOutputStream(lock));
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
    }

    // hacked ObjectOutputStream - to replace special values used by property
    // editors (like SuperColor from ColorEditor or NbImageIcon from IconEditor)
    private static class OOS extends ObjectOutputStream {
        OOS(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException {
            if (obj.getClass().getName().startsWith("org.netbeans.") // NOI18N
                || obj.getClass().getName().startsWith("org.openide.")) // NOI18N
            {
                if (obj instanceof java.awt.Color)
                    return new java.awt.Color(((java.awt.Color)obj).getRGB());
                if (obj instanceof javax.swing.ImageIcon)
                    return new javax.swing.ImageIcon(
                        ((javax.swing.ImageIcon)obj).getImage());
            }
            return obj;
        }
    }

    //
    // {{{ CodeGenerateEditor
    //

    final public static class CodeGenerateEditor extends PropertyEditorSupport
    {
        private RADComponent component;

        /** Display Names for alignment. */
        private static final String generateName =
            FormUtils.getBundleString("VALUE_codeGen_generate"); // NOI18N
        private static final String serializeName =
            FormUtils.getBundleString("VALUE_codeGen_serialize"); // NOI18N

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
                public Component getCustomEditor() {
                    return new CustomCodeEditor(CodePropertySupportRW.this,
                                                FormEditor.createCodeEditorPane(formModel));
                }

                public boolean supportsCustomEditor() {
                    return true;
                }
            };
        }

        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !formModel.isReadOnly();
        }
    }

    // }}}
    
    // Properties
    
    private class VariablesModifierProperty extends PropertySupport.ReadWrite {
        
        private VariablesModifierProperty() {
            super(PROP_VARIABLE_MODIFIER,
                Integer.class,
                FormUtils.getBundleString("PROP_VARIABLES_MODIFIER"), // NOI18N
                FormUtils.getBundleString("HINT_VARIABLES_MODIFIER")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            int varType;
            int variablesModifier = newValue.intValue();
            if (formModel.getSettings().getVariablesLocal()) {
                varType = CodeVariable.LOCAL | (variablesModifier & CodeVariable.FINAL) | CodeVariable.EXPLICIT_DECLARATION;
            } else varType = CodeVariable.FIELD | variablesModifier;

            formModel.getCodeStructure().setDefaultVariableType(varType);
            formModel.getSettings().setVariablesModifier(variablesModifier);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_MODIFIER, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            FormRootNode formRootNode = (FormRootNode)formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_MODIFIER, oldValue, newValue);
        }
        
        public Object getValue() {
            return new Integer(formModel.getSettings().getVariablesModifier());
        }

        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(new Integer(FormLoaderSettings.getInstance().getVariablesModifier()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getVariablesModifier() ==
                FormLoaderSettings.getInstance().getVariablesModifier());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        public PropertyEditor getPropertyEditor() {
            boolean local = formModel.getSettings().getVariablesLocal();
            return local ? new ModifierEditor(Modifier.FINAL) :
                new ModifierEditor(Modifier.PUBLIC
                    | Modifier.PROTECTED
                    | Modifier.PRIVATE
                    | Modifier.STATIC
                    | Modifier.FINAL
                    | Modifier.TRANSIENT
                    | Modifier.VOLATILE);
        }
        
    }
    
    private class LocalVariablesProperty extends PropertySupport.ReadWrite {

        private LocalVariablesProperty() {
            super(PROP_VARIABLE_LOCAL,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_VARIABLES_LOCAL"), // NOI18N
                FormUtils.getBundleString("HINT_VARIABLES_LOCAL")); // NOI18N
        }
        
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();            
            if (value.equals(getValue())) return;
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            FormSettings formSettings = formModel.getSettings();
            boolean variablesLocal = newValue.booleanValue();
            int variablesModifier = variablesLocal ? (formSettings.getVariablesModifier() & CodeVariable.FINAL)
                : formSettings.getVariablesModifier();
            Integer oldModif = new Integer(formModel.getSettings().getVariablesModifier());
            Integer newModif = new Integer(variablesModifier);
            int varType = variablesLocal ?
                CodeVariable.LOCAL | variablesModifier | CodeVariable.EXPLICIT_DECLARATION
                : CodeVariable.FIELD | variablesModifier;

            formModel.getCodeStructure().setDefaultVariableType(varType);
            formSettings.setVariablesLocal(variablesLocal);
            formSettings.setVariablesModifier(variablesModifier);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_LOCAL, oldValue, newValue);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_MODIFIER, oldModif, newModif);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            FormRootNode formRootNode = (FormRootNode)formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_LOCAL, oldValue, newValue);
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_MODIFIER, oldModif, newModif);
        }
        
        public Object getValue() {
            return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(Boolean.valueOf(FormLoaderSettings.getInstance().getVariablesLocal()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getVariablesLocal() == 
                FormLoaderSettings.getInstance().getVariablesLocal());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
    }
    
    private class GenerateMnemonicsCodeProperty extends PropertySupport.ReadWrite {
        
        private GenerateMnemonicsCodeProperty() {
            super(PROP_GENERATE_MNEMONICS,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_GENERATE_MNEMONICS"), // NOI18N
                FormUtils.getBundleString("HINT_GENERATE_MNEMONICS2")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            formModel.getSettings().setGenerateMnemonicsCode(newValue.booleanValue());
            formModel.fireSyntheticPropertyChanged(null, PROP_GENERATE_MNEMONICS, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            FormRootNode formRootNode = (FormRootNode)formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                PROP_GENERATE_MNEMONICS, oldValue, newValue);
        }
        
        public Object getValue() {
            return Boolean.valueOf(formModel.getSettings().getGenerateMnemonicsCode());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(Boolean.valueOf(FormLoaderSettings.getInstance().getGenerateMnemonicsCode()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getGenerateMnemonicsCode() == 
                FormLoaderSettings.getInstance().getGenerateMnemonicsCode());
        }
        
    }

    private class ListenerGenerationStyleProperty extends PropertySupport.ReadWrite {
        
        private ListenerGenerationStyleProperty() {
            super(PROP_LISTENER_GENERATION_STYLE,
                Integer.class,
                FormUtils.getBundleString("PROP_LISTENER_GENERATION_STYLE"), // NOI18N
                FormUtils.getBundleString("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            formModel.getSettings().setListenerGenerationStyle(newValue.intValue());
            formModel.fireSyntheticPropertyChanged(null, PROP_LISTENER_GENERATION_STYLE, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            FormRootNode formRootNode = (FormRootNode)formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                PROP_LISTENER_GENERATION_STYLE, oldValue, newValue);
        }
        
        public Object getValue() {
            return new Integer(formModel.getSettings().getListenerGenerationStyle());
        }

        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(new Integer(FormLoaderSettings.getInstance().getListenerGenerationStyle()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getListenerGenerationStyle() ==
                FormLoaderSettings.getInstance().getListenerGenerationStyle());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        public PropertyEditor getPropertyEditor() {
            return new FormLoaderSettingsBeanInfo.ListenerGenerationStyleEditor();
        }
        
    }

    // analogical to ListenerGenerationStyleProperty ...
    private class LayoutCodeTargetProperty extends PropertySupport.ReadWrite {
        
        private LayoutCodeTargetProperty() {
            super(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET,
                Integer.class,
                FormUtils.getBundleString("PROP_LAYOUT_CODE_TARGET"), // NOI18N
                FormUtils.getBundleString("HINT_LAYOUT_CODE_TARGET")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            formModel.getSettings().setLayoutCodeTarget(newValue.intValue());
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.updateProjectForNaturalLayout();
            formModel.fireSyntheticPropertyChanged(null, FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, oldValue, newValue);
            FormRootNode formRootNode = (FormRootNode) formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, oldValue, newValue);
        }

        public Object getValue() {
            return new Integer(formModel.getSettings().getLayoutCodeTarget());
        }

        public boolean supportsDefaultValue() {
            return true;
        }

        public void restoreDefaultValue() {
            setValue(new Integer(FormLoaderSettings.getInstance().getLayoutCodeTarget()));
        }

        public boolean isDefaultValue() {
            return (formModel.getSettings().getLayoutCodeTarget() ==
                    FormLoaderSettings.getInstance().getLayoutCodeTarget());
        }

        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }

        public PropertyEditor getPropertyEditor() {
            return new FormLoaderSettingsBeanInfo.LayoutCodeTargetEditor(true);
        }

    }
}
