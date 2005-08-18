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
package org.netbeans.modules.web.jsf.editor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.config.model.*;
import org.netbeans.modules.web.jsf.dialogs.AddDialog;
import org.netbeans.modules.web.jsf.dialogs.AddManagedBeanDialog;
import org.netbeans.modules.web.jsf.dialogs.AddNavigationCaseDialog;
import org.netbeans.modules.web.jsf.dialogs.AddNavigationRuleDialog;
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
public final class JSFPopupAction extends SystemAction implements Presenter.Popup {
    
    private ArrayList actions = null;
    
    public String getName() {
        return NbBundle.getMessage(JSFPopupAction.class, "org-netbeans-modules-web-jsf-editor-JSFPopupAction.instance"); // NOI18N
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
            pm.add(new AddNavigationRuleAction());
            pm.add(new AddNavigationCaseAction());
            pm.add(new JSeparator());
            pm.add(new AddManagedBeanAction());
            pm.pack();
            return pm;
        }
    }
    
    public static class AddManagedBeanAction extends BaseAction{
        public AddManagedBeanAction(){
            super(NbBundle.getBundle(JSFPopupAction.class).getString("add-managed-bean-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddManagedBeanDialog dialogPanel = new AddManagedBeanDialog(data);
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(JSFPopupAction.class,"TTL_AddManagedBean"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    FacesConfig config = data.getFacesConfig();
                    if (config == null){
                        return;
                    }
                    ManagedBean bean = new ManagedBean();
                    bean.setManagedBeanName(dialogPanel.getName());
                    bean.setManagedBeanClass(dialogPanel.getBeanClass());
                    bean.setManagedBeanScope(dialogPanel.getScope());
                    if(dialogPanel.getDescription() != null && !dialogPanel.getDescription().equals(""))
                        bean.setDescription(new String[]{"\n" + dialogPanel.getDescription() + "\n"});
                    config.addManagedBean(bean);    
                    data.write(config);
                } 
                catch (java.io.IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
    
    public static class AddNavigationRuleAction extends BaseAction{
        public AddNavigationRuleAction(){
            super(NbBundle.getBundle(JSFPopupAction.class).getString("add-navigation-rule-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddNavigationRuleDialog dialogPanel = new AddNavigationRuleDialog(data);
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(JSFPopupAction.class,"TTL_AddNavigationRule"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    FacesConfig config = data.getFacesConfig();
                    if (config == null){
                        return;
                    }
                    NavigationRule rule = new NavigationRule();
                    rule.setDescription(new String[]{dialogPanel.getDescription()});
                    rule.setFromViewId(dialogPanel.getFromView());
                    config.addNavigationRule(rule);
                    data.write(config);
                } 
                catch (java.io.IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
    
    public static class AddNavigationCaseAction extends BaseAction{
        public AddNavigationCaseAction(){
            super(NbBundle.getBundle(JSFPopupAction.class).getString("add-navigation-case-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddNavigationCaseDialog dialogPanel = new AddNavigationCaseDialog(data, 
                    JSFEditorUtilities.getNavigationRule((BaseDocument)doc, target.getCaretPosition()));
            AddDialog dialog = new AddDialog(dialogPanel,NbBundle.getMessage(JSFPopupAction.class,"TTL_AddNavigationCase"));
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    FacesConfig config = data.getFacesConfig();
                    if (config == null){
                        return;
                    }
                    NavigationRule rule = JSFConfigUtilities.findNavigationRule(config, dialogPanel.getRule());
                    if (rule == null){
                        rule = new NavigationRule();
                        rule.setFromViewId(dialogPanel.getRule());
                        config.addNavigationRule(rule);
                    }
                    NavigationCase nCase = new NavigationCase();
                    if(dialogPanel.getFromAction() != null && !dialogPanel.getFromAction().equals(""))
                        nCase.setFromAction(dialogPanel.getFromAction());
                    if(dialogPanel.getFromOutcome() != null && !dialogPanel.getFromOutcome().equals(""))
                        nCase.setFromOutcome(dialogPanel.getFromOutcome());
                    nCase.setRedirect(dialogPanel.isRedirect());
                    nCase.setToViewId(dialogPanel.getToView());
                    if(dialogPanel.getDescription() != null && !dialogPanel.getDescription().equals(""))
                        nCase.setDescription(new String[]{"\n" + dialogPanel.getDescription() + "\n"});
                    rule.addNavigationCase(nCase);
                    data.write(config);
                } 
                catch (java.io.IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
}
