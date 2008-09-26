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
package org.netbeans.modules.swingapp;

import org.jdesktop.application.ResourceConverter;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ActionEditor is a property editor for actions. It only works with JSR 296 support
 * and will show an empty panel with a message in it if used in non-JSR 296 projects.
 * (there is an issue filed to allow property editors to be conditionally removed completely)
 * @author joshua.marinacci@sun.com
 */
public class ActionEditor extends PropertyEditorSupport implements FormAwareEditor,
        XMLPropertyEditor, ExPropertyEditor, VetoableChangeListener {

    private List<String> actionNames;
    private FormModel formModel;
    private RADProperty formProperty;
    private RADComponent radComponent;
    private Map<ProxyAction.Scope,List<ProxyAction>> actionMap;
    private ActionPropertyEditorPanel panel;
    private ProxyAction action;
    private Class componentClass;
    private boolean scannedOnce = false;
    private boolean globalMode = false;
    private boolean globalCreateMode = false;
    private FileObject sourceFile;
    // disabled for 6.0 release: private String NEW_ACTION = "Create new action...";
    private String GLOBAL_SUFFIX = "(global)";
    private static Action jumpToSourceAction;
    
    /** Creates a new instance of ActionEditor */
    public ActionEditor(FileObject sourceFile) {
        this();
        this.sourceFile = sourceFile;
    }
    
    public ActionEditor(FileObject sourceDir, boolean globalCreate) {
        this();
        this.sourceFile = sourceDir;
        globalCreateMode = globalCreate;
    }
    
    public ActionEditor() {
        actionNames = new ArrayList<String>();
        actionMap = new HashMap<ProxyAction.Scope,List<ProxyAction>>();
        actionMap.put(ProxyAction.Scope.Application,new ArrayList<ProxyAction>());
        actionMap.put(ProxyAction.Scope.Form,new ArrayList<ProxyAction>());
        componentClass = JComponent.class;
    }
    
    // property editor impl
    @Override
    public String getJavaInitializationString() {
        if(!isAppFramework()) {
            return super.getJavaInitializationString();
        }
        // The code for getting the action map is quite long - worth "caching"
        // in a variable. Using special code mark to encode 3 data elements:
        // - the code to replace
        // - the type of variable to declare for the code
        // - suggested variable name
        return  CODE_MARK_VARIABLE_SUBST + AppFrameworkSupport.getActionMapCode(getSourceFile())
                + CODE_MARK_VARIABLE_SUBST + javax.swing.ActionMap.class.getName()
                + CODE_MARK_VARIABLE_SUBST + "actionMap" // NOI18N
                + CODE_MARK_LINE_COMMENT + "NOI18N" // NOI18N
                + CODE_MARK_END
                + ".get(\"" + action.getId()+ "\")"; // NOI18N
    }

    // special code marks recognized by form editor:
    private static final String CODE_MARK_END = "*/\n\\0"; // NOI18N
    private static final String CODE_MARK_LINE_COMMENT = "*/\n\\1"; // NOI18N
    private static final String CODE_MARK_VARIABLE_SUBST = "*/\n\\2"; // NOI18N

    // FormAwareEditor impl
    public void setContext(FormModel formModel, FormProperty property) {
        this.formModel = formModel;
        ActionManager.registerFormModel(formModel,getSourceFile());
        this.formProperty = (RADProperty)property;
        this.radComponent = formProperty.getRADComponent();
        this.componentClass = formProperty.getRADComponent().getBeanInstance().getClass();
        FormEditor formEditor = FormEditor.getFormEditor(formModel);
        if (formEditor != null) {
            if (jumpToSourceAction == null) {
                jumpToSourceAction = new JumpToOrCreateNewAction();
            }
            formEditor.registerDefaultComponentAction(jumpToSourceAction);
        }
    }

    // FormAwareEditor impl
    public void updateFormVersionLevel() {
        formModel.raiseVersionLevel(FormModel.FormVersion.NB60, FormModel.FormVersion.NB60);
    }

    private String getComponentName() {
        if(radComponent == null) {
            return null;
        }
        return radComponent.getName();
    }
    
    // determines if this Action property editor is being launched from the GlobalACtionPanel
    // or directly from a form
    public void setGlobalMode(boolean globalMode) {
        this.globalMode = true;
    }
    
    // property editor impl. returns the ActionPropertyEditorPanel to go in a dialog
    @Override
    public Component getCustomEditor() {
        if(!isAppFramework()) {
            return new DisabledEditorPanel();
        }
        if (panel == null) {
            panel = new ActionPropertyEditorPanel(formProperty,getSourceFile());
            panel.addPropertyChangeListener("action", new PropertyChangeListener() { // NOI18N
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    setValue(panel.getSelectedAction());
                }
            });
        }
        FileObject srcFile = getSourceFile();
        Map<ProxyAction.Scope, String> scopeMap = new HashMap<ProxyAction.Scope, String>();
        if(!globalCreateMode) {
            scanForActions();
            scopeMap.put(ProxyAction.Scope.Application, AppFrameworkSupport.getApplicationClassName(srcFile));
            scopeMap.put(ProxyAction.Scope.Form, AppFrameworkSupport.getClassNameForFile(srcFile));
        }
        panel.resetFields();
        if (action != null) {
            ActionManager.initActionFromSource(action, sourceFile);
        }
        panel.updatePanel(actionMap, action, scopeMap, getComponentName(), srcFile);
        panel.setMode(ActionPropertyEditorPanel.Mode.Form);
        panel.attachEnv(env);
        return panel;
    }
    
    // property editor impl
    @Override
    public boolean supportsCustomEditor() {
        if(!isAppFramework()) {
            return super.supportsCustomEditor();
        }
        return true;
    }
    
    // property editor impl
    @Override
    public String getAsText() {
        if(!isAppFramework()) {
            return null;
        }
        
        if(action == null) {
            return "null"; //NOI18N
        } else {
            return action.getId();
        }
    }
    
    // property editor impl
    @Override
    public void setAsText(String string) throws IllegalArgumentException {
        if(!isAppFramework()) {
            return;
        }
        if("null".equals(string)) { //NOI18N
            setValue(null);
            return;
        }
        
        /*disabled for 6.0 release
        if(NEW_ACTION.equals(string)) {
            openNewActionDialog();
        }*/
        
        for(List<ProxyAction> acts : actionMap.values()) {
            for(ProxyAction act : acts) {
                if(act != null) {
                    // check for form scope
                    if(act.getId().equals(string)) {
                        setValue(act);
                        return;
                    }
                    // check for global scope
                    if(string != null && string.endsWith(GLOBAL_SUFFIX) && act.isAppWide()) {
                        String shortname = string.substring(0, string.length() - GLOBAL_SUFFIX.length());
                        if(act.getId().equals(shortname)) {
                            setValue(act);
                            return;
                        }
                    }
                }
            }
        }
        // if it doesn't match an action, set to null
        setValue(null);
    }
    
    // property editor impl
    @Override
    public Object getValue() {
        if(!isAppFramework()) {
            return super.getValue();
        }
        return action;
    }
    
    // property editor impl
    @Override
    public void setValue(Object object) {
        if(!isAppFramework()) {
            super.setValue(object);
            return;
        }
        
        /*disabled for 6.0 release
        if(NEW_ACTION.equals(object)) {
            openNewActionDialog();
        }*/
        
        if(object instanceof ProxyAction) {
            ProxyAction oldAction = action;
            action = (ProxyAction)object;
            ActionManager am = ActionManager.getActionManager(getSourceFile());
            action.setResourceMap(ResourceUtils.getDesignResourceMap(getSourceFile(), true));
            if(!am.actionsMatch(oldAction,action)){
                if (oldAction != null) {
                    am.removeRADComponent(oldAction, radComponent);
                }
                am.addRADComponent(action, radComponent);
            }
        } else {
            ProxyAction oldAction = action;
            action = null;
            ActionManager.getActionManager(getSourceFile()).removeRADComponent(oldAction, radComponent);
        }
    }

    
    // property editor impl
    @Override
    public String[] getTags() {
        if(!isAppFramework()) {
            return null;
        }
        //if(!scannedOnce) {
            scanForActions();
        //}
        return actionNames.toArray(new String[0]);
    }
    
    // property editor impl
    private void scanForActions() {
        actionMap.clear();
        List<ProxyAction> appActions = new ArrayList<ProxyAction>();
        List<ProxyAction> formActions = new ArrayList<ProxyAction>();
        actionMap.put(ProxyAction.Scope.Application, appActions);
        actionMap.put(ProxyAction.Scope.Form, formActions);
        appActions.add(null);
        actionNames.clear();
        actionNames.add("null"); // NOI18N
        
        // grab all of the form scope actions
        List<ProxyAction> actions = getClassActions();
        for(ProxyAction act : actions) {
            act.setAppWide(false);
            formActions.add(act);
            actionNames.add(act.getId());
        }
        // grab all actions from the global scope
        actions = getApplicationActions();
        for(ProxyAction act : actions) {
            act.setAppWide(true);
            appActions.add(act);
            actionNames.add(act.getId() + GLOBAL_SUFFIX);
        }
        //josh: disabling for now. actionNames.add(NEW_ACTION);
        scannedOnce = true;
    }

    /**
     * @return list of actions defined in the source file on which this property
     *         editor is invoked 
     */
    private List<ProxyAction> getClassActions() {
        return ActionManager.getActions(getSourceFile(), !scannedOnce);
    }

    private List<ProxyAction> getApplicationActions() {
        String appClassName = AppFrameworkSupport.getApplicationClassName(getSourceFile());
        assert(appClassName != null);
        return ActionManager.getActionManager(getSourceFile()).getActions(appClassName, !scannedOnce);
    }

    public void setSourceFile(FileObject sourceFile) {
        this.sourceFile = sourceFile;
    }
    private FileObject getSourceFile() {
        if (sourceFile == null && formModel != null) {
            sourceFile = FormEditor.getFormDataObject(formModel).getPrimaryFile();
        }
        return sourceFile;
    }

    public void readFromXML(Node element) throws IOException {
        if(element != null) {
            Element elem = (Element)element;
            if (elem.hasAttribute("id")) { // NOI18N
                FileObject srcFile = getSourceFile();
                String className = elem.getAttribute("class"); // NOI18N
                String id = elem.getAttribute("id"); // NOI18N
                String methodName = elem.getAttribute("methodName"); // NOI18N
                if(methodName == null) methodName = id;
                ProxyAction action = new ProxyAction(className, id, methodName);
                boolean appWide = !className.endsWith(srcFile.getName())
                        || (className.length() != srcFile.getName().length()
                        && className.charAt(className.length() - srcFile.getName().length() - 1) != '.');
                action.setAppWide(appWide);
                action.setResourceMap(ResourceUtils.getDesignResourceMap(srcFile, true));
                action.loadFromResourceMap();
                setValue(action);
            } else {
                setValue(null);
            }
        }
    }

    public Node storeToXML(Document doc) {
        Element elem = doc.createElement("action"); // NOI18N
        if (action != null) {
            elem.setAttribute("id",action.getId()); // NOI18N
            elem.setAttribute("class",action.getClassname()); // NOI18N
            elem.setAttribute("methodName",action.getMethodName()); // NOI18N
        }
        return elem;
    }

    private PropertyEnv env;
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        env.removeVetoableChangeListener(this);
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener(this);
    }
    
    
    // called after the action property editor panel dialog has been closed with the 'okay' button
    // this will *not* be called if the user pressed 'cancel'
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if(!isAppFramework()) {
            return;
        }
        
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())) {
            try {
                confirmChanges(evt);
            } catch (IllegalArgumentException ex) {
                throw new PropertyVetoException(ex.getMessage(), evt);
            }
        }
    }

    void confirmChanges(PropertyChangeEvent evt) throws PropertyVetoException {

        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                && !PropertyEnv.STATE_VALID.equals(evt.getNewValue())) {
            return;
        }

        // if the user created a new action and assigned it to this component
        if(panel.isNewActionCreated()) {
            if(!panel.canCreateNewAction()) {
                if(!panel.isMethodNonEmpty()) {
                    throw new PropertyVetoException(NbBundle.getMessage(ActionEditor.class,"ActionEditor.createMethodError.emptyMethod"),evt);
                }
                if(panel.doesMethodContainBadChars()) {
                    throw new PropertyVetoException(NbBundle.getMessage(ActionEditor.class,"ActionEditor.createMethodError.invalidName",panel.getNewMethodName()),evt);
                }
                if(!panel.isValidClassname()) {
                    throw new PropertyVetoException(NbBundle.getMessage(ActionEditor.class,"ActionEditor.createMethodError.invalidClassname"),evt);
                }
                if(panel.isDuplicateMethod()) {
                    throw new PropertyVetoException(NbBundle.getMessage(ActionEditor.class,"ActionEditor.createMethodError.duplicateMethod",panel.getNewMethodName()),evt);
                }
            }
            ProxyAction act = createNewAction();
            panel.setMode(ActionPropertyEditorPanel.Mode.Form);
            setValue(act);
            scanForActions();
            ActionManager.getActionManager(getSourceFile()).jumpToActionSource(act);
            return;
        }

        if(panel.getSelectedAction() == null) {
            setValue(null);
            return;
        }
        
        // if the user pressed the view source button
        if(panel.isViewSource()) {
            ProxyAction act = panel.getSelectedAction();
            ActionManager.getActionManager(getSourceFile()).jumpToActionSource(act);
            panel.resetFields();
        }

        // if the user updated propertes on the existing action
        if(panel.isActionPropertiesUpdated()) {
            ProxyAction act = panel.getUpdatedAction();
            saveChangedActionProperties(act);
            panel.resetFields();
            ActionManager.getActionManager(getSourceFile()).updateAction(act);
            setValue(act);
        }
    }
    
    private void saveChangedActionProperties(ProxyAction action) {
        FileObject fileInProject = getSourceFile();
        DesignResourceMap map = ResourceUtils.getDesignResourceMap(fileInProject, true);
        
        String actionKey = action.getId() + ".Action"; // NOI18N
        //save properties
        updateOrDeleteResource(action, actionKey+".text", Action.NAME,fileInProject,map, String.class); // NOI18N
        updateOrDeleteResource(action, actionKey+".shortDescription", Action.SHORT_DESCRIPTION,fileInProject,map, String.class); // NOI18N
        
        if(action.getValue(Action.SMALL_ICON) != null) {
            updateOrDeleteResource(action, actionKey + ".icon", Action.SMALL_ICON+".IconName", fileInProject, map, Icon.class); // NOI18N
        } else {
            updateOrDeleteResource(action, actionKey + ".icon", ActionPropertyEditorPanel.LARGE_ICON_KEY+".IconName", fileInProject, map, Icon.class); // NOI18N
        }
        updateOrDeleteResource(action, actionKey + ".smallIcon", Action.SMALL_ICON+".IconName", fileInProject, map, Icon.class); // NOI18N
        updateOrDeleteResource(action, actionKey + ".largeIcon", ActionPropertyEditorPanel.LARGE_ICON_KEY+".IconName", fileInProject, map, Icon.class); // NOI18N
        updateOrDeleteResource(action, actionKey+".accelerator", Action.ACCELERATOR_KEY, fileInProject,map, KeyStroke.class); // NOI18N

        String blockPrefix = action.getId() + ".BlockingDialog."; // NOI18N
        updateOrDeleteResource(action, blockPrefix + "optionPane.message", "BlockingDialog.message", fileInProject,map,String.class); // NOI18N
        updateOrDeleteResource(action, blockPrefix + "title", "BlockingDialog.title", fileInProject,map,String.class); // NOI18N
        // $action_name$.BlockingDialog.title
        // $action_name$.BlockingDialog.optionPane.message
        // $action_name$.BlockingDialog.optionPane.icon
        // $action_name$.BlockingDialog.cancelButton.text
        // $action_name$.BlockingDialog.cancelButton.icon

        map.save();
    }

    private void updateOrDeleteResource(ProxyAction act, String key, String actionKey,
            FileObject fileInProject, DesignResourceMap map, Class type) {
        
        // get the design resource map level
        int level = DesignResourceMap.CLASS_LEVEL;
        if(act.isAppWide()) {
            level = DesignResourceMap.APP_LEVEL;
        }
        
        // get the value from the action
        Object newVal = act.getValue(actionKey);
        
        // if the value is null
        if(act.getValue(actionKey) == null) {
            // if there is a resource already but the action value is null then delete it.
            ResourceValueImpl val = map.getResourceValue(key,type);
            if(val != null) {
                map.removeResourceValue(val);
            }
            return;
        }
        String stringVal = null;
        if(newVal instanceof String) {
            if (Icon.class.equals(type)) { // icons treated specially
                // don't save icon, but its name (assuming we have a classpath resource name)
                String iconCPName = (String) newVal;
                String pkgResName = map.getResourcesDir();
                stringVal = iconCPName.startsWith(pkgResName) ?
                iconCPName.substring(pkgResName.length()) : iconCPName;
                newVal = null;
            } else {
                stringVal = (String)newVal;
            }
        } else if(String.class.equals(type)) {
            stringVal = ""+newVal;
        } else {
            stringVal = ResourceConverter.forType(type).toString(newVal);
        }
        ResourceValueImpl val = new ResourceValueImpl(key, type,
                newVal, null, stringVal, true,
                level, fileInProject);
        map.addResourceValue(val);
    }
    
    public ProxyAction createNewAction() {
        //start a transaction
        ProxyAction newAction = panel.getNewAction();
        boolean appWide = newAction.getClassname().equals(
                AppFrameworkSupport.getApplicationClassName(getSourceFile()));
        newAction.setAppWide(appWide);
        if (ActionManager.getActionManager(getSourceFile()).createActionMethod(newAction)) {
            ActionManager.getActionManager(getSourceFile()).addNewAction(newAction);
            saveChangedActionProperties(newAction);
        }
        return newAction;
    }
    

    private boolean isAppFramework() {
        if(globalCreateMode) { return true; }
        return AppFrameworkSupport.isFrameworkEnabledProject(getSourceFile());
    }

}
