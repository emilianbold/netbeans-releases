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
package org.netbeans.modules.swingapp;

import org.jdesktop.application.ResourceMap;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.ResourceValue;
import org.openide.awt.Mnemonics;

/**
 * A class which represents an action but contains extra information needed for JSR 296 support.
 * @author joshua.marinacci@sun.com
 */
public class ProxyAction implements Action, ResourceValue, Serializable {
    private String id; // TODO bring back the method name and distinguish it from
                       // the action name (start using the 'name' annotation attr)
    private String classname;
    private Map<String,Object> values = new HashMap<String,Object>();
    private boolean enabled = true;
    private boolean appWide;
    private boolean taskEnabled;
    private String enabledName;
    private String selectedName;
    
    public enum BlockingType { NONE, ACTION, COMPONENT, WINDOW, APPLICATION }
    public enum Scope {Application, Form }
    private BlockingType blockingType;
    
    private transient DesignResourceMap resourceMap;
    
    private static final String[] ANNOTATION_ATTR_NAMES
            = { "block", "enabledProperty", "name", "selectedProperty" }; // NOI18N

    /** Creates a new instance of ProxyAction */
    public ProxyAction(String className, String id) {
        this.classname = className;
        this.id = id;
        this.blockingType = BlockingType.NONE;
    }
    
    ProxyAction() {
        this("","");
    }

    ProxyAction(ProxyAction copy) {
        id = copy.id;
        classname = copy.classname;
        values = copy.values;
        enabled = copy.enabled;
        appWide = copy.appWide;
        taskEnabled = copy.taskEnabled;
        enabledName = copy.enabledName;
        selectedName = copy.selectedName;
        resourceMap = copy.resourceMap;
    }
    
    public void loadFromResourceMap() {
        String text = (String) getResource("text", String.class); // NOI18N
        javax.swing.JLabel label = new javax.swing.JLabel();
        Mnemonics.setLocalizedText(label, text);
        int mnem = label.getDisplayedMnemonic();
        int mnemIndex = label.getDisplayedMnemonicIndex();
        putValue(Action.NAME, label.getText());
        if (mnem != 0) {
            putValue(Action.MNEMONIC_KEY, mnem);
        }
        if (mnemIndex >= 0) {
            putValue("SwingDisplayedMnemonicIndexKey", mnemIndex); // NOI18N
        }
        
        putValue(Action.SHORT_DESCRIPTION, getResource("shortDescription", String.class)); // NOI18N
        putValue(Action.ACCELERATOR_KEY, getResource("accelerator", KeyStroke.class)); // NOI18N
        Object icon = getResource("icon", Icon.class); // NOI18N
        Object iconString = getResource("icon",String.class);
        Object smallIcon = getResource("smallIcon", Icon.class); // NOI18N
        Object smallIconString = getResource("smallIcon",String.class);
        
        putValue(Action.SMALL_ICON, smallIcon != null ? smallIcon : icon);
        putValue(Action.SMALL_ICON+".IconName",smallIcon != null ? smallIconString : iconString);
        
        putValue(ActionPropertyEditorPanel.LARGE_ICON_KEY, getResource("largeIcon", Icon.class)); // NOI18N
        putValue(ActionPropertyEditorPanel.LARGE_ICON_KEY+".IconName", getResource("largeIcon",String.class));
        
        putValue("BlockingDialog.message",getResource("BlockingDialog.message", String.class)); //NOI18N
        putValue("BlockingDialog.title",getResource("BlockingDialog.title", String.class)); //NOI18N
        
        // also keep icon names
        putValue("IconName", getIconName("icon")); // NOI18N
        putValue("SmallIconName", getIconName("smallIcon")); // NOI18N
        putValue("LargeIconName", getIconName("largeIcon")); // NOI18N
    }
    
    private Object getResource(String name, Class valueType) {
        try {
            Object value = null;
//            if (actionType != null) {
//                String key = id + actionType + ".Action." + name; // NOI18N
//                value = resourceMap.getObject(key, valueType);
//            }
            if (value == null) {
                String key = id + ".Action." +name; // NOI18N
                value = resourceMap.getObject(key, valueType);
            }
            return value;
        } catch (ResourceMap.LookupException ex) {
            System.out.println("there was an error loading the resource: " + name); //log
            ex.printStackTrace();
            return null;
        }
    }
    
    private String getIconName(String name) {
        try {
            ResourceValueImpl resValue = null;
//            if (actionType != null) {
//                String key = id + actionType + ".Action." + name; // NOI18N
//                resValue = resourceMap.getResourceValue(key, Icon.class);
//            }
            if (resValue == null) {
                String key = id + ".Action." +name; // NOI18N
                resValue = resourceMap.getResourceValue(key, Icon.class);
            }
            if (resValue != null) {
                String cpName = resValue.getClassPathResourceName();
                return cpName != null ? cpName : resValue.getStringValue();
            }
            return null;
        } catch (ResourceMap.LookupException ex) {
            System.out.println("there was an error loading the icon name for: " + name);//log
            ex.printStackTrace();
            return null;
        }
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getClassname() {
        return classname;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }
    
    public String toString() {
        return (id!=null) ? id : "no action set";
    }

    public Object getValue(String key) {
        return values.get(key);
    }
    
    public void putValue(String key, Object value) {
        values.put(key,value);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //pcs.removePropertyChangeListener(listener);
    }
    
    
    public boolean isAppWide() {
        return appWide;
    }
    
    public void setAppWide(boolean appWide) {
        this.appWide = appWide;
    }
    
    public Scope getScope() {
        if(isAppWide()) {
            return Scope.Application;
        } else {
            return Scope.Form;
        }
    }
    
    public boolean isTaskEnabled() {
        return taskEnabled;
    }
    
    public void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }
    
    public BlockingType getBlockingType() {
        return blockingType;
    }
    
    public void setBlockingType(BlockingType blockingType) {
        this.blockingType = blockingType;
    }
    
    public String getEnabledName() {
        return enabledName;
    }
    
    public void setEnabledName(String enabledName) {
        this.enabledName = enabledName;
    }
    
    public String getSelectedName() {
        return selectedName;
    }
    
    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }
    
    DesignResourceMap getResourceMap() {
        return resourceMap;
    }
    
    void setResourceMap(DesignResourceMap resMap) {
        this.resourceMap = resMap;
    }

    boolean isAnnotationAttributeSet(String attrName) {
        if ("name".equals(attrName)) { // NOI18N
            return false;
            // TBD we don't distinguish id (action name) from method name yet
        }
        Object value = getAnnotationAttributeValue(attrName);
        if (value instanceof BlockingType) {
            return !BlockingType.NONE.equals(value);
        }
        return value != null && !value.equals("") ; // NOI18N
    }

    boolean isAnnotationAttributeUnset(String attrName) {
        if ("name".equals(attrName)) { // NOI18N
            return false;
            // TBD we don't distinguish id (action name) from method name yet
        }
        Object value = getAnnotationAttributeValue(attrName);
        if (value instanceof BlockingType) {
            return BlockingType.NONE.equals(value);
        }
        return value == null || value.equals(""); // NOI18N
    }

    Object getAnnotationAttributeValue(String attrName) {
        if ("block".equals(attrName)) { // NOI18N
            return getBlockingType();
        } else if ("enabledProperty".equals(attrName)) { // NOI18N
            return getEnabledName();
        } else if ("name".equals(attrName)) { // NOI18N
            return null;
            // TBD we don't distinguish id (action name) from method name yet
        } else if ("selectedProperty".equals(attrName)) { // NOI18N
            return getSelectedName();
        }
        return null;
    }

    static String[] getAnnotationAttributeNames() {
        return ANNOTATION_ATTR_NAMES;
    }

    // -----
    // ResourceValue implementation - needed to reflect changes in design locale.
    // The action itself is not separated - to keep the same value
    // if the form is switched to "plain values".
    
    public String getClassPathResourceName() {
        return null;
    }
    
    public String getJavaInitializationCode() {
        return null; // the code should be generated by property editor based
                     // on where the action is set
    }

    public String getKey() {
        return null;
    }
    
    public Object getValue() {
        return this;
    }
    
    public Object getDesignValue() {
        return this;
    }
    
    public Object getDesignValue(Object target) {
        return null;
    }
    
    public String getDescription() {
        return id;
    }
    
    public Object copy(FormProperty targetFormProperty) {
        return new ProxyAction(this);
        // [TODO: perhaps we should get another resource map if copied to different form]
    }
    
}
