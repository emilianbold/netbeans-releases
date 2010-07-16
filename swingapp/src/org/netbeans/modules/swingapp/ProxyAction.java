/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.swingapp;

import org.jdesktop.application.ResourceMap;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private String id; //this is the name of the action if the name attribute
    //is specified, otherwise it is the method name
    private String methodName; // this is always the method name
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
    public ProxyAction(String className, String id, String methodName) {
        this.classname = className;
        this.id = id;
        this.blockingType = BlockingType.NONE;
        this.methodName = methodName;
    }
    
    ProxyAction() {
        this("","","");
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
        methodName = copy.methodName;
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
        Object iconString = getResource("icon",String.class); // NOI18N
        Object smallIcon = getResource("smallIcon", Icon.class); // NOI18N
        Object smallIconString = getResource("smallIcon",String.class); // NOI18N
        
        putValue(Action.SMALL_ICON, smallIcon != null ? smallIcon : icon);
        putValue(Action.SMALL_ICON+".IconName",smallIcon != null ? smallIconString : iconString); // NOI18N
        
        putValue(ActionPropertyEditorPanel.LARGE_ICON_KEY, getResource("largeIcon", Icon.class)); // NOI18N
        putValue(ActionPropertyEditorPanel.LARGE_ICON_KEY+".IconName", getResource("largeIcon",String.class)); // NOI18N

        String blockPrefix = getId() + ".BlockingDialog."; // NOI18N
        putValue("BlockingDialog.message",getResource(blockPrefix, "optionPane.message", String.class)); // NOI18N
        putValue("BlockingDialog.title",getResource(blockPrefix, "title", String.class)); // NOI18N
        
        // also keep icon names
        putValue("IconName", getIconName("icon")); // NOI18N
        putValue("SmallIconName", getIconName("smallIcon")); // NOI18N
        putValue("LargeIconName", getIconName("largeIcon")); // NOI18N
    }
    
    private Object getResource(String name, Class valueType) {
        return getResource(getId() + ".Action.", name, valueType); // NOI18N
    }

    private Object getResource(String prefix, String name, Class valueType) {
        try {
            return resourceMap.getObject(prefix + name, valueType);
        } catch (ResourceMap.LookupException ex) {
            Logger.getLogger(ProxyAction.class.getName()).log(
                    Level.INFO, "Error loading action resource: "+prefix+name, ex); // NOI18N
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
            Logger.getLogger(ProxyAction.class.getName()).log(
                    Level.INFO, "there was an error loading the icon name for: " + name, ex); // NOI18N
            return null;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
    }
    
    public String getId() {
        return id;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getClassname() {
        return classname;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }
    
    @Override
    public String toString() {
        return (id!=null) ? id : "no action set";
    }

    @Override
    public Object getValue(String key) {
        return values.get(key);
    }
    
    @Override
    public void putValue(String key, Object value) {
        values.put(key,value);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //pcs.addPropertyChangeListener(listener);
    }
    
    @Override
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
            //only if id != methodname has the name attribute been used
            if(!(id.equals(methodName))) {
                return true;
            }
            return false;
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
            return id;
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
    
    @Override
    public String getClassPathResourceName() {
        return null;
    }
    
    @Override
    public String getJavaInitializationCode() {
        return null; // the code should be generated by property editor based
                     // on where the action is set
    }

    @Override
    public String getKey() {
        return null;
    }
    
    @Override
    public Object getValue() {
        return this;
    }
    
    @Override
    public Object getDesignValue() {
        return this;
    }
    
    @Override
    public Object getDesignValue(Object target) {
        return null;
    }
    
    @Override
    public String getDescription() {
        return id;
    }
    
    @Override
    public Object copy(FormProperty targetFormProperty) {
        return new ProxyAction(this);
        // [TODO: perhaps we should get another resource map if copied to different form]
    }
    
}
