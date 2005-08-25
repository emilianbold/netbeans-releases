/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.struts.editor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.config.model.Action;
import org.netbeans.modules.web.struts.config.model.ActionMappings;
import org.netbeans.modules.web.struts.config.model.FormBean;
import org.netbeans.modules.web.struts.config.model.FormBeans;
import org.netbeans.modules.web.struts.config.model.FormProperty;
import org.netbeans.modules.web.struts.config.model.Forward;
import org.netbeans.modules.web.struts.config.model.GlobalExceptions;
import org.netbeans.modules.web.struts.config.model.GlobalForwards;
import org.netbeans.modules.web.struts.config.model.StrutsConfig;
import org.netbeans.modules.web.struts.config.model.StrutsException;
import org.netbeans.modules.web.struts.dialogs.AddDialog;
import org.netbeans.modules.web.struts.dialogs.AddExceptionDialogPanel;
import org.netbeans.modules.web.struts.dialogs.AddFIActionPanel;
import org.netbeans.modules.web.struts.dialogs.AddActionPanel;
import org.netbeans.modules.web.struts.dialogs.AddFormBeanPanel;
import org.netbeans.modules.web.struts.dialogs.AddFormPropertyPanel;
import org.netbeans.modules.web.struts.dialogs.AddForwardDialogPanel;
import org.netbeans.modules.web.struts.dialogs.ValidatingPanel;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Petr Pisl
 */
public final class StrutsPopupAction extends SystemAction implements Presenter.Popup {
    
    private ArrayList actions = null;
    
    public String getName() {
        return NbBundle.getMessage(StrutsPopupAction.class, "org-netbeans-modules-web-struts-editor-StrutsPopupAction.instance"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing - should never be called
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public javax.swing.JMenuItem getPopupPresenter() {
        return new SubMenu(getName());
    }
    
    public class SubMenu extends JMenu {
        
        public SubMenu(String s){
            super(s);
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            pm.add(new AddFormBeanAction());
            pm.add(new AddActionAction());
            pm.add(new AddForwardInlcudeAction());
            pm.add(new AddForwardAction());
            pm.add(new AddExceptionAction());
            pm.add(new AddFormPropertyAction());
            pm.pack();
            return pm;
        }
    }
    
    public static class AddFormBeanAction extends BaseAction{
        public AddFormBeanAction(){
            super(NbBundle.getBundle(StrutsPopupAction.class).getString("add-form-bean-action")); //NOI18N
            //putValue(SHORT_DESCRIPTION, );
            //putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(StrutsPopupAction.class).getString(addFormBean));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            DataObject data = NbEditorUtilities.getDataObject(doc);
            AddFormBeanPanel dialogPanel = new AddFormBeanPanel();
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(StrutsPopupAction.class,"TTL_AddFormBean"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                //TODO:implement action
                try {
                    StrutsConfig config = ((StrutsConfigDataObject)data).getStrutsConfig();
                    if (config==null) return; //TODO:inform that XML file is corrupted
                    FormBeans beans = config.getFormBeans();
                    if (beans==null) {
                        beans = new FormBeans();
                        config.setFormBeans(beans);
                    }
                    FormBean bean = new FormBean();
                    bean.setAttributeValue("type",dialogPanel.getFormBeanClass()); //NOI18N
                    bean.setAttributeValue("name",dialogPanel.getFormName()); //NOI18N
                    beans.addFormBean(bean);
                    ((StrutsConfigDataObject)data).write(config);
                } catch (java.io.IOException ex) {}
            }
            
        }
    }
    
    public static class AddActionAction extends BaseAction {
        public AddActionAction(){
            super(NbBundle.getBundle(StrutsPopupAction.class).getString("add-action-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            DataObject data = NbEditorUtilities.getDataObject(doc);
            AddActionPanel dialogPanel = new AddActionPanel((StrutsConfigDataObject)data);
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(StrutsPopupAction.class,"TTL_AddAction"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                //TODO:implement action
                try {
                    StrutsConfig config = ((StrutsConfigDataObject)data).getStrutsConfig();
                    if (config==null) return; //TODO:inform that XML file is corrupted
                    ActionMappings mappings = config.getActionMappings();
                    if (mappings==null) {
                        mappings = new ActionMappings();
                        config.setActionMappings(mappings);
                    }
                    Action action = new Action();
                    action.setAttributeValue("type",dialogPanel.getActionClass()); //NOI18N
                    action.setAttributeValue("path",dialogPanel.getActionPath()); //NOI18N
                    action.setAttributeValue("name",dialogPanel.getFormName()); //NOI18N
                    action.setAttributeValue("input",dialogPanel.getInput()); //NOI18N
                    action.setAttributeValue("validate",dialogPanel.getValidate()); //NOI18N
                    action.setAttributeValue("scope",dialogPanel.getScope()); //NOI18N
                    action.setAttributeValue("attribute",dialogPanel.getAttribute()); //NOI18N
                    action.setAttributeValue("parameter",dialogPanel.getParameter()); //NOI18N
                    mappings.addAction(action);
                    ((StrutsConfigDataObject)data).write(config);
                } catch (java.io.IOException ex) {}
            }
        }
    }
    
    public static class AddForwardInlcudeAction extends BaseAction {
        public AddForwardInlcudeAction(){
            super(NbBundle.getBundle(StrutsPopupAction.class).getString("add-forward-include-action-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            DataObject data = NbEditorUtilities.getDataObject(doc);
            
            AddFIActionPanel dialogPanel = new AddFIActionPanel((StrutsConfigDataObject)data);
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(StrutsPopupAction.class,"TTL_Forward-Include"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    StrutsConfig config = ((StrutsConfigDataObject)data).getStrutsConfig();
                    if (config==null) return; //TODO:inform that XML file is corrupted
                    ActionMappings mappings = config.getActionMappings();
                    if (mappings==null) {
                        mappings = new ActionMappings();
                        config.setActionMappings(mappings);
                    }
                    Action action = new Action();
                    action.setAttributeValue("path",dialogPanel.getActionPath()); //NOI18N
                    if (dialogPanel.isForward()) {
                        action.setAttributeValue("forward",dialogPanel.getResource()); //NOI18N
                    } else {
                        action.setAttributeValue("include",dialogPanel.getResource()); //NOI18N
                    }
                    mappings.addAction(action);
                    ((StrutsConfigDataObject)data).write(config);
                } catch (java.io.IOException ex) {}    
            }
        }
    }
    
    public static class AddFormPropertyAction extends BaseAction {
        public AddFormPropertyAction(){
            super(NbBundle.getBundle(StrutsPopupAction.class).getString("add-form-property-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            DataObject data = NbEditorUtilities.getDataObject(doc);
            
            AddFormPropertyPanel dialogPanel = new AddFormPropertyPanel((StrutsConfigDataObject)data);
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(StrutsPopupAction.class,"TTL_AddFormProperty"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                //TODO:implement action
                try {
                    StrutsConfig config = ((StrutsConfigDataObject)data).getStrutsConfig();
                    if (config==null) return; //TODO:inform that XML file is corrupted
                    FormProperty prop = new FormProperty();
                    prop.setAttributeValue("name",dialogPanel.getPropertyName()); //NOI18N
                    prop.setAttributeValue("type",dialogPanel.getPropertyType());
                    if (dialogPanel.isArray()) {
                        prop.setAttributeValue("size",dialogPanel.getArraySize()); //NOI18N
                    } else {
                        prop.setAttributeValue("initial",dialogPanel.getInitValue()); //NOI18N
                    }
                    FormBean[] beans = config.getFormBeans().getFormBean();
                    for (int i=0;i<beans.length;i++) {
                        if (dialogPanel.getFormName().equals(beans[i].getAttributeValue("name"))) { //NOI18N
                            beans[i].addFormProperty(prop);
                            beans[i].setAttributeValue("dynamic","true");
                            break;
                        }
                    }
                    ((StrutsConfigDataObject)data).write(config);
                 
                } catch (java.io.IOException ex) {}
            }
        }
    }
    
    public static class AddForwardAction extends BaseAction{
        public AddForwardAction(){
            super(NbBundle.getBundle(StrutsPopupAction.class).getString("add-forward-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            DataObject data = NbEditorUtilities.getDataObject(doc);
            try {
                StrutsConfig config = ((StrutsConfigDataObject)data).getStrutsConfig();
                if (config==null) return; //TODO:inform that XML file is corrupted
                String actionPath = StrutsEditorUtilities.getActionPath((BaseDocument)doc, target.getCaretPosition());
                String targetActionPath=null;
                
                if (actionPath != null && data instanceof StrutsConfigDataObject){
                    Action[] actions = config.getActionMappings().getAction();
                    String path;
                    for (int i = 0; i < actions.length; i++){
                        path = actions[i].getAttributeValue("path"); //NOI18N
                        if (path != null && path.equals(actionPath)
                                && actions[i].getAttributeValue("type") != null){ //NOI18N
                            targetActionPath=path;
                            break;
                        }
                    }
                }
                
                AddForwardDialogPanel dialogPanel=null;
                if (targetActionPath==null) 
                    dialogPanel = new AddForwardDialogPanel((StrutsConfigDataObject)data);
                else 
                    dialogPanel = new AddForwardDialogPanel((StrutsConfigDataObject)data, targetActionPath);
                AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(StrutsPopupAction.class,"TTL_AddForward"));
                dialog.disableAdd(); // disable Add button
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                    Forward forward = new Forward();
                    forward.setAttributeValue("name",dialogPanel.getForwardName()); //NOI18N
                    forward.setAttributeValue("path",dialogPanel.getForwardTo()); //NOI18N
                    forward.setAttributeValue("redirect",dialogPanel.getRedirect()); //NOI18N
                    if (dialogPanel.isGlobal()) {
                        GlobalForwards forwards = config.getGlobalForwards();
                        if (forwards==null) {
                            forwards = new GlobalForwards();
                            config.setGlobalForwards(forwards);
                        }
                        forwards.addForward(forward);
                    } else {
                        Action[] actions = config.getActionMappings().getAction();
                        for (int i=0;i<actions.length;i++) {
                            if (dialogPanel.getLocationAction().equals(actions[i].getAttributeValue("path"))) { //NOI18N
                                actions[i].addForward(forward);
                                break;
                            }
                        }
                    }
                    ((StrutsConfigDataObject)data).write(config);
                }
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    public static class AddExceptionAction extends BaseAction{
        public AddExceptionAction(){
            super(NbBundle.getBundle(StrutsPopupAction.class).getString("add-exception-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            DataObject data = NbEditorUtilities.getDataObject(doc);
            try {
                StrutsConfig config = ((StrutsConfigDataObject)data).getStrutsConfig();
                if (config==null) return; //TODO:inform that XML file is corrupted
                String actionPath = StrutsEditorUtilities.getActionPath((BaseDocument)doc, target.getCaretPosition());
                String targetActionPath=null;
                
                if (actionPath != null && data instanceof StrutsConfigDataObject){
                    Action[] actions = config.getActionMappings().getAction();
                    String path;
                    for (int i = 0; i < actions.length; i++){
                        path = actions[i].getAttributeValue("path"); //NOI18N
                        if (path != null && path.equals(actionPath)
                                && actions[i].getAttributeValue("type") != null){ //NOI18N
                            targetActionPath=path;
                            break;
                        }
                    }
                }
                
                AddExceptionDialogPanel dialogPanel=null;
                if (targetActionPath==null) 
                    dialogPanel = new AddExceptionDialogPanel((StrutsConfigDataObject)data);
                else 
                    dialogPanel = new AddExceptionDialogPanel((StrutsConfigDataObject)data, targetActionPath);
                
                AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(StrutsPopupAction.class,"TTL_AddException"));
                dialog.disableAdd(); // disable Add button
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                    //TODO:implement action
                    StrutsException exception = new StrutsException();
                    exception.setAttributeValue("bundle",dialogPanel.getResourceBundle()); //NOI18N
                    exception.setAttributeValue("type",dialogPanel.getExceptionType()); //NOI18N
                    exception.setAttributeValue("key",dialogPanel.getExceptionKey()); //NOI18N
                    exception.setAttributeValue("path",dialogPanel.getForwardTo()); //NOI18N
                    exception.setAttributeValue("scope",dialogPanel.getScope()); //NOI18N
                    if (dialogPanel.isGlobal()) {
                        GlobalExceptions exceptions = config.getGlobalExceptions();
                        if (exceptions==null) {
                            exceptions = new GlobalExceptions();
                            config.setGlobalExceptions(exceptions);
                        }
                        exceptions.addStrutsException(exception);
                    } else {
                        Action[] actions = config.getActionMappings().getAction();
                        for (int i=0;i<actions.length;i++) {
                            if (dialogPanel.getLocationAction().equals(actions[i].getAttributeValue("path"))) { //NOI18N
                                actions[i].addStrutsException(exception);
                                break;
                            }
                        }
                    }
                    ((StrutsConfigDataObject)data).write(config);
                }
            } catch (java.io.IOException ex) {}
        }
    }
    
}
