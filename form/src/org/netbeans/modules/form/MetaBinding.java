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

import java.util.*;

public class MetaBinding {
    public static final int UPDATE_STRATEGY_READ_WRITE = 0;
    public static final int UPDATE_STRATEGY_READ = 1;
    public static final int UPDATE_STRATEGY_READ_ONCE = 2;
    public static final String TABLE_COLUMN_CLASS_PARAMETER = "javax.swing.binding.ParameterKeys.COLUMN_CLASS"; // NOI18N
    public static final String EDITABLE_PARAMETER = "javax.swing.binding.ParameterKeys.EDITABLE"; // NOI18N
    public static final String TEXT_CHANGE_STRATEGY = "javax.swing.binding.ParameterKeys.TEXT_CHANGE_STRATEGY"; // NOI18N
    public static final String TEXT_CHANGE_ON_TYPE = "javax.swing.binding.TextChangeStrategy.ON_TYPE"; // NOI18N
    public static final String TEXT_CHANGE_ON_ACTION_OR_FOCUS_LOST = "javax.swing.binding.TextChangeStrategy.ON_ACTION_OR_FOCUS_LOST"; // NOI18N;
    public static final String TEXT_CHANGE_ON_FOCUS_LOST = "javax.swing.binding.TextChangeStrategy.ON_FOCUS_LOST"; // NOI18N;
    public static final String DISPLAY_PARAMETER = "DISPLAY"; // NOI18N
    public static final String NAME_PARAMETER = "NAME"; // NOI18N
    public static final String IGNORE_ADJUSTING_PARAMETER = "IGNORE_ADJUSTING"; // NOI18N
    private RADComponent source;
    private RADComponent target;
    private String sourcePath;
    private String targetPath;
    private int updateStrategy = UPDATE_STRATEGY_READ_WRITE;
    private boolean nullValueSpecified;
    private boolean incompleteValueSpecified;
    private Map<String,String> parameters = new TreeMap<String, String>();
    private boolean bindImmediately;

    private List<MetaBinding> subBindings;

    public MetaBinding(RADComponent source, String sourcePath, RADComponent target, String targetPath) {
        this.source = source;
        this.sourcePath = sourcePath;
        this.target = target;
        this.targetPath = targetPath;
    }

    public RADComponent getSource() {
        return source;
    }

    void setSource(RADComponent source) {
        this.source = source;
    }

    public RADComponent getTarget() {
        return target;
    }

    void setTarget(RADComponent target) {
        this.target = target;
        // backward compatibility hack
        Class clazz = target.getBeanClass();
        if (hasSubBindings() && 
                (javax.swing.JComboBox.class.isAssignableFrom(clazz)
                || javax.swing.JList.class.isAssignableFrom(clazz))) {
            assert (subBindings.size() == 1);
            MetaBinding display = getSubBindings().iterator().next();
            setParameter(DISPLAY_PARAMETER, display.getSourcePath());
            clearSubBindings();
        }
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public int getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(int updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public boolean isNullValueSpecified() {
        return nullValueSpecified;
    }

    public void setNullValueSpecified(boolean nullValueSpecified) {
        this.nullValueSpecified = nullValueSpecified;
    }

    public boolean isIncompletePathValueSpecified() {
        return incompleteValueSpecified;
    }

    public void setIncompletePathValueSpecified(boolean incompleteValueSpecified) {
        this.incompleteValueSpecified = incompleteValueSpecified;
    }

    public boolean isConverterSpecified() {
        BindingProperty prop = getTarget().getBindingProperty(getTargetPath());
        FormProperty converterProp = prop.getConverterProperty();
        return !converterProp.isDefaultValue();
    }

    public boolean isValidatorSpecified() {
        BindingProperty prop = getTarget().getBindingProperty(getTargetPath());
        FormProperty validatorProp = prop.getValidatorProperty();
        return !validatorProp.isDefaultValue();
    }

    public boolean isNameSpecified() {
        BindingProperty prop = getTarget().getBindingProperty(getTargetPath());
        FormProperty nameProp = prop.getNameProperty();
        return !nameProp.isDefaultValue();
    }

    public boolean isBindImmediately() {
        return bindImmediately;
    }

    public void setBindImmediately(boolean bindImmediately) {
        this.bindImmediately = bindImmediately;
    }

    public void setParameter(String name, String value) {
        if (value == null) {
            parameters.remove(name);
        } else {
            name = changeObsoleteName(name);
            if (name.equals(MetaBinding.TEXT_CHANGE_STRATEGY)) {
                value = changeObsoleteValue(value);
            }
            parameters.put(name, value);
        }
    }

    private static String changeObsoleteName(String name) {
        if (name.startsWith("javax.swing.binding.SwingBindingSupport")) { // NOI18N
            if (name.startsWith("javax.swing.binding.SwingBindingSupport.TableColumnClassParameter")) { // NOI18N
                name = MetaBinding.TABLE_COLUMN_CLASS_PARAMETER;
            } else if (name.startsWith("javax.swing.binding.SwingBindingSupport.EditableParameter")) { // NOI18N
                name = MetaBinding.EDITABLE_PARAMETER;
            } else if (name.startsWith("javax.swing.binding.SwingBindingSupport.TextChangeStrategyParameter")) { // NOI18N
                name = MetaBinding.TEXT_CHANGE_STRATEGY;
            }
        }
        return name;
    }

    private static String changeObsoleteValue(String value) {
        if (value.equals("javax.swing.binding.SwingBindingSupport.TextChangeStrategy.CHANGE_ON_TYPE")) { // NOI18N
            value = MetaBinding.TEXT_CHANGE_ON_TYPE;
        } else if (value.equals("javax.swing.binding.SwingBindingSupport.TextChangeStrategy.CHANGE_ON_ACTION_OR_FOCUS_LOST")) { // NOI18N
            value = MetaBinding.TEXT_CHANGE_ON_ACTION_OR_FOCUS_LOST;
        } else if (value.equals("javax.swing.binding.SwingBindingSupport.TextChangeStrategy.CHANGE_ON_FOCUS_LOST")) { // NOI18N
            value = MetaBinding.TEXT_CHANGE_ON_FOCUS_LOST;
        }
        return value;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }
    
    Map<String,String> getParameters() {
        return parameters;
    }

    public boolean hasSubBindings() {
        return subBindings != null && subBindings.size() > 0;
    }

    public Collection<MetaBinding> getSubBindings() {
        return subBindings != null && subBindings.size() > 0 ?
               Collections.unmodifiableCollection(subBindings) : null;
    }

    public MetaBinding addSubBinding(String sourcePath, String targetPath) {
        if (subBindings == null) {
            subBindings = new ArrayList<MetaBinding>();
        }
        MetaBinding binding = new MetaBinding(null, sourcePath, null, targetPath);
        subBindings.add(binding);
        return binding;
    }

    public void removeSubBinding(MetaBinding binding) {
        if (subBindings != null)
            subBindings.remove(binding);
    }

    public void clearSubBindings() {
        subBindings.clear();
    }
}
