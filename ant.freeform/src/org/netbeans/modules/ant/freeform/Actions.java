/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.Element;

/**
 * Action bindings for a freeform project.
 * @author Jesse Glick
 */
public final class Actions implements ActionProvider {
    
    private final FreeformProject project;
    
    public Actions(FreeformProject project) {
        this.project = project;
    }
    
    public String[] getSupportedActions() {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            return new String[0];
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        List/*<String>*/ names = new ArrayList(actions.size());
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (Util.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL) != null) { // NOI18N
                throw new UnsupportedOperationException("XXX No support for <context> yet"); // NOI18N
            }
            names.add(actionEl.getAttribute("name")); // NOI18N
        }
        return (String[])names.toArray(new String[names.size()]);
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                // XXX check context, and also perhaps existence of script
                return true;
            }
        }
        throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
    }
    
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                runConfiguredAction(project, actionEl);
                return;
            }
        }
        throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
    }
    
    /**
     * Run a project action as described by subelements <script> and <target>.
     */
    private static void runConfiguredAction(FreeformProject project, Element actionEl) {
        String script;
        Element scriptEl = Util.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
        if (scriptEl != null) {
            script = Util.findText(scriptEl);
        } else {
            script = "build.xml"; // NOI18N
        }
        String scriptLocation = project.evaluator().evaluate(script);
        FileObject scriptFile = project.helper().resolveFileObject(scriptLocation);
        if (scriptFile == null) {
            return;
        }
        List/*<Element>*/ targets = Util.findSubElements(actionEl);
        List/*<String>*/ targetNames = new ArrayList(targets.size());
        Iterator it2 = targets.iterator();
        while (it2.hasNext()) {
            Element targetEl = (Element)it2.next();
            if (!targetEl.getLocalName().equals("target")) { // NOI18N
                continue;
            }
            targetNames.add(Util.findText(targetEl));
        }
        String[] targetNameArray;
        if (!targetNames.isEmpty()) {
            targetNameArray = (String[])targetNames.toArray(new String[targetNames.size()]);
        } else {
            // Run default target.
            targetNameArray = null;
        }
        try {
            ActionUtils.runTarget(scriptFile, targetNameArray, null);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static Action[] createContextMenu(FreeformProject p) {
        List/*<Action>*/ actions = new ArrayList();
        actions.add(CommonProjectActions.newFileAction());
        // Requested actions.
        Element genldata = p.helper().getPrimaryConfigurationData(true);
        Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl != null) {
            Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
            if (contextMenuEl != null) {
                actions.add(null);
                List/*<Element>*/ actionEls = Util.findSubElements(contextMenuEl);
                Iterator it = actionEls.iterator();
                while (it.hasNext()) {
                    Element actionEl = (Element)it.next();
                    if (actionEl.getLocalName().equals("ide-action")) { // NOI18N
                        String cmd = actionEl.getAttribute("name");
                        String displayName;
                        try {
                            displayName = NbBundle.getMessage(Actions.class, "CMD_" + cmd);
                        } catch (MissingResourceException e) {
                            // OK, fall back to raw name.
                            displayName = cmd;
                        }
                        actions.add(ProjectSensitiveActions.projectCommandAction(cmd, displayName, null));
                    } else {
                        assert actionEl.getLocalName().equals("action") : actionEl;
                        actions.add(new CustomAction(p, actionEl));
                    }
                }
            }
        }
        // Back to generic actions.
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        actions.add(null);
        actions.add(SystemAction.get(ToolsAction.class));
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    private static final class CustomAction extends AbstractAction {

        private final FreeformProject p;
        private final Element actionEl;
        
        public CustomAction(FreeformProject p, Element actionEl) {
            this.p = p;
            this.actionEl = actionEl;
        }
        
        public void actionPerformed(ActionEvent e) {
            runConfiguredAction(p, actionEl);
        }
        
        public boolean isEnabled() {
            // XXX check for existence of script, perhaps
            return true;
        }
        
        public Object getValue(String key) {
            if (key.equals(Action.NAME)) {
                Element labelEl = Util.findElement(actionEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
                return Util.findText(labelEl);
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
}
