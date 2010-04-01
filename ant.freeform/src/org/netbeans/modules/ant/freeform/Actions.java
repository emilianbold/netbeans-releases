/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ant.freeform;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.ui.TargetMappingPanel;
import org.netbeans.modules.ant.freeform.ui.UnboundTargetAlert;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.Element;

/**
 * Action bindings for a freeform project.
 * @author Jesse Glick
 */
public final class Actions implements ActionProvider {

    private static final Logger LOG = Logger.getLogger(Actions.class.getName());

    /**
     * Some routine global actions for which we can supply a display name.
     * These are IDE-specific.
     */
    private static final Set<String> COMMON_IDE_GLOBAL_ACTIONS = new HashSet<String>(Arrays.asList(
        ActionProvider.COMMAND_DEBUG,
        ActionProvider.COMMAND_DELETE,
        ActionProvider.COMMAND_COPY,
        ActionProvider.COMMAND_MOVE,
        ActionProvider.COMMAND_RENAME));
    /**
     * Similar to {@link #COMMON_IDE_GLOBAL_ACTIONS}, but these are not IDE-specific.
     * We also mark all of these as bound in the project; if the user
     * does not really have a binding, they are prompted for one when
     * the action is "run".
     */
    private static final Set<String> COMMON_NON_IDE_GLOBAL_ACTIONS = new HashSet<String>(Arrays.asList(
        ActionProvider.COMMAND_BUILD,
        ActionProvider.COMMAND_CLEAN,
        ActionProvider.COMMAND_REBUILD,
        ActionProvider.COMMAND_RUN,
        ActionProvider.COMMAND_TEST,
        // XXX JavaProjectConstants.COMMAND_JAVADOC
        "javadoc", // NOI18N
        // XXX WebProjectConstants.COMMAND_REDEPLOY
        // XXX should this really be here? perhaps not, once web part of #46886 is implemented...
        "redeploy",
        // XXX deploy action of EJB freeform project
        "deploy")); // NOI18N
    
    private final FreeformProject project;
    
    /**
     * Create a new action provider.
     * @param project the associated project
     */
    public Actions(FreeformProject project) {
        this.project = project;
    }
    
    public String[] getSupportedActions() {
        Element genldata = project.getPrimaryConfigurationData();
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            return new String[0];
        }
        // Use a set, not a list, since when using context you can define one action several times:
        Set<String> names = new LinkedHashSet<String>();
        for (Element actionEl : Util.findSubElements(actionsEl)) {
            names.add(actionEl.getAttribute("name")); // NOI18N
        }
        // #46886: also always enable all common global actions, in case they should be selected:
        names.addAll(COMMON_NON_IDE_GLOBAL_ACTIONS);
        names.add(COMMAND_RENAME);
        names.add(COMMAND_MOVE);
        names.add(COMMAND_COPY);
        names.add(COMMAND_DELETE);
        return names.toArray(new String[names.size()]);
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            return true;
        }
        if (COMMAND_COPY.equals(command)) {
            return true;
        }
        if (COMMAND_RENAME.equals(command)) {
            return true;
        }
        if (COMMAND_MOVE.equals(command)) {
            return true;
        }
        
        Element genldata = project.getPrimaryConfigurationData();
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        boolean foundAction = false;
        for (Element actionEl : Util.findSubElements(actionsEl)) {
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                foundAction = true;
                // XXX perhaps check also existence of script
                Element contextEl = Util.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL); // NOI18N
                if (contextEl != null) {
                    // Check whether the context contains files all in this folder,
                    // matching the pattern if any, and matching the arity (single/multiple).
                    Map<String,FileObject> selection = findSelection(contextEl, context, project);
                    LOG.log(Level.FINE, "detected selection {0} for command {1} in {2}", new Object[] {selection, command, project});
                    if (selection.size() == 1) {
                        // Definitely enabled.
                        return true;
                    } else if (!selection.isEmpty()) {
                        // Multiple selection; check arity.
                        Element arityEl = Util.findElement(contextEl, "arity", FreeformProjectType.NS_GENERAL); // NOI18N
                        assert arityEl != null : "No <arity> in <context> for " + command;
                        if (Util.findElement(arityEl, "separated-files", FreeformProjectType.NS_GENERAL) != null) { // NOI18N
                            // Supports multiple selection, take it.
                            return true;
                        }
                    }
                } else {
                    // Not context-sensitive.
                    return true;
                }
            }
        }
        if (COMMON_NON_IDE_GLOBAL_ACTIONS.contains(command)) {
            // #46886: these are always enabled if they are not specifically bound.
            return true;
        }
        if (foundAction) {
            // Was at least one context-aware variant but did not match.
            return false;
        } else {
            throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
        }
    }
    
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        Element genldata = project.getPrimaryConfigurationData();
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        boolean foundAction = false;
        for (Element actionEl : Util.findSubElements(actionsEl)) {
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                foundAction = true;
                runConfiguredAction(project, actionEl, context);
            }
        }
        if (!foundAction) {
            if (COMMON_NON_IDE_GLOBAL_ACTIONS.contains(command)) {
                // #46886: try to bind it.
                if (addGlobalBinding(command)) {
                    // If bound, run it immediately.
                    invokeAction(command, context);
                }
            } else {
                throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
            }
        }
    }
    
    /**
     * Find a file selection in a lookup context based on a project.xml <context> declaration.
     * If all DataObject's (or FileObject's) in the lookup match the folder named in the declaration,
     * and match any optional pattern declaration, then they are returned as a map from relative
     * path to actual file object. Otherwise an empty map is returned.
     */
    private static Map<String,FileObject> findSelection(Element contextEl, Lookup context, FreeformProject project) {
        Collection<? extends DataObject> filesDO = context.lookupAll(DataObject.class);
        if (filesDO.isEmpty()) {
             return Collections.emptyMap();
        }
        Collection<FileObject> _files = new ArrayList<FileObject>(filesDO.size());
        for (DataObject d : filesDO) {
            _files.add(d.getPrimaryFile());
        }
        Collection<? extends FileObject> files = _files;
        Element folderEl = Util.findElement(contextEl, "folder", FreeformProjectType.NS_GENERAL); // NOI18N
        assert folderEl != null : "Must have <folder> in <context>";
        String rawtext = Util.findText(folderEl);
        assert rawtext != null : "Must have text contents in <folder>";
        String evaltext = project.evaluator().evaluate(rawtext);
        if (evaltext == null) {
            return Collections.emptyMap();
        }
        FileObject folder = project.helper().resolveFileObject(evaltext);
        if (folder == null) {
            return Collections.emptyMap();
        }
        Pattern pattern = null;
        Element patternEl = Util.findElement(contextEl, "pattern", FreeformProjectType.NS_GENERAL); // NOI18N
        if (patternEl != null) {
            String text = Util.findText(patternEl);
            assert text != null : "Must have text contents in <pattern>";
            try {
                pattern = Pattern.compile(text);
            } catch (PatternSyntaxException e) {
                org.netbeans.modules.ant.freeform.Util.err.annotate(e, ErrorManager.UNKNOWN, "From <pattern> in " + FileUtil.getFileDisplayName(project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH)), null, null, null); // NOI18N
                org.netbeans.modules.ant.freeform.Util.err.notify(e);
                return Collections.emptyMap();
            }
        }
        Map<String,FileObject> result = new HashMap<String,FileObject>();
        for (FileObject file : files) {
            String path = FileUtil.getRelativePath(folder, file);
            if (path == null) {
                return Collections.emptyMap();
            }
            if (pattern != null && !pattern.matcher(path).find()) {
                return Collections.emptyMap();
            }
            result.put(path, file);
        }
        return result;
    }
    
    /**
     * Run a project action as described by subelements <script> and <target>.
     */
    private static void runConfiguredAction(FreeformProject project, Element actionEl, Lookup context) {
        String script;
        Element scriptEl = Util.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
        if (scriptEl != null) {
            script = Util.findText(scriptEl);
        } else {
            script = "build.xml"; // NOI18N
        }
        String scriptLocation = project.evaluator().evaluate(script);
        FileObject scriptFile = null;
        if (scriptLocation != null)  {
            scriptFile = project.helper().resolveFileObject(scriptLocation);
        }
        if (scriptFile == null) {
            //#57011: if the script does not exist, show a warning:
            NotifyDescriptor nd = new NotifyDescriptor.Message(MessageFormat.format(NbBundle.getMessage(Actions.class, "LBL_ScriptFileNotFoundError"), new Object[] {scriptLocation}), NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        List<Element> targets = Util.findSubElements(actionEl);
        List<String> targetNames = new ArrayList<String>(targets.size());
        for (Element targetEl : targets) {
            if (!targetEl.getLocalName().equals("target")) { // NOI18N
                continue;
            }
            targetNames.add(Util.findText(targetEl));
        }
        String[] targetNameArray;
        if (!targetNames.isEmpty()) {
            targetNameArray = targetNames.toArray(new String[targetNames.size()]);
        } else {
            // Run default target.
            targetNameArray = null;
        }
        Properties props = new Properties();
        Element contextEl = Util.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextEl != null) {
            Map<String,FileObject> selection = findSelection(contextEl, context, project);
            if (selection.isEmpty()) {
                return;
            }
            String separator = null;
            if (selection.size() > 1) {
                // Find the right separator.
                Element arityEl = Util.findElement(contextEl, "arity", FreeformProjectType.NS_GENERAL); // NOI18N
                assert arityEl != null : "No <arity> in <context> for " + actionEl.getAttribute("name");
                Element sepFilesEl = Util.findElement(arityEl, "separated-files", FreeformProjectType.NS_GENERAL); // NOI18N
                if (sepFilesEl == null) {
                    // Only handles single files -> skip it.
                    return;
                }
                separator = Util.findText(sepFilesEl);
            }
            Element formatEl = Util.findElement(contextEl, "format", FreeformProjectType.NS_GENERAL); // NOI18N
            assert formatEl != null : "No <format> in <context> for " + actionEl.getAttribute("name");
            String format = Util.findText(formatEl);
            StringBuffer buf = new StringBuffer();
            Iterator<Map.Entry<String,FileObject>> it = selection.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,FileObject> entry = it.next();
                if (format.equals("absolute-path")) { // NOI18N
                    File f = FileUtil.toFile(entry.getValue());
                    if (f == null) {
                        // Not a disk file??
                        return;
                    }
                    buf.append(f.getAbsolutePath());
                } else if (format.equals("relative-path")) { // NOI18N
                    buf.append(entry.getKey());
                } else if (format.equals("absolute-path-noext")) { // NOI18N
                    File f = FileUtil.toFile(entry.getValue());
                    if (f == null) {
                        // Not a disk file??
                        return;
                    }
                    String path = f.getAbsolutePath();
                    int dot = path.lastIndexOf('.');
                    if (dot > path.lastIndexOf('/')) {
                        path = path.substring(0, dot);
                    }
                    buf.append(path);
                } else if (format.equals("relative-path-noext")) { // NOI18N
                    String path = entry.getKey();
                    int dot = path.lastIndexOf('.');
                    if (dot > path.lastIndexOf('/')) {
                        path = path.substring(0, dot);
                    }
                    buf.append(path);
                } else {
                    assert format.equals("java-name") : format;
                    String path = entry.getKey();
                    int dot = path.lastIndexOf('.');
                    String dotless;
                    if (dot == -1 || dot < path.lastIndexOf('/')) {
                        dotless = path;
                    } else {
                        dotless = path.substring(0, dot);
                    }
                    String javaname = dotless.replace('/', '.');
                    buf.append(javaname);
                }
                if (it.hasNext()) {
                    assert separator != null;
                    buf.append(separator);
                }
            }
            Element propEl = Util.findElement(contextEl, "property", FreeformProjectType.NS_GENERAL); // NOI18N
            assert propEl != null : "No <property> in <context> for " + actionEl.getAttribute("name");
            String prop = Util.findText(propEl);
            assert prop != null : "Must have text contents in <property>";
            props.setProperty(prop, buf.toString());
        }
        for (Element propEl : targets) {
            if (!propEl.getLocalName().equals("property")) { // NOI18N
                continue;
            }
            String rawtext = Util.findText(propEl);
            if (rawtext == null) {
                // Legal to have e.g. <property name="intentionally-left-blank"/>
                rawtext = ""; // NOI18N
            }
            String evaltext = project.evaluator().evaluate(rawtext); // might be null
            if (evaltext != null) {
                props.setProperty(propEl.getAttribute("name"), evaltext); // NOI18N
            }
        }
        TARGET_RUNNER.runTarget(scriptFile, targetNameArray, props);
    }

    public static final class Custom extends AbstractAction implements ContextAwareAction {
        public Custom() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }
        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }
        public @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends FreeformProject> projects = actionContext.lookupAll(FreeformProject.class);
            if (projects.size() != 1) {
                return this;
            }
            final FreeformProject p = projects.iterator().next();
            class A extends AbstractAction implements Presenter.Popup {
                public @Override void actionPerformed(ActionEvent e) {
                    assert false;
                }
                public @Override JMenuItem getPopupPresenter() {
                    class M extends JMenuItem implements DynamicMenuContent {
                        public @Override JComponent[] getMenuPresenters() {
                            Action[] actions = contextMenuCustomActions(p);
                            JComponent[] comps = new JComponent[actions.length];
                            for (int i = 0; i < actions.length; i++) {
                                if (actions[i] != null) {
                                    JMenuItem item = new JMenuItem();
                                    org.openide.awt.Actions.connect(item, actions[i], true);
                                    comps[i] = item;
                                } else {
                                    comps[i] = new JSeparator();
                                }
                            }
                            return comps;
                        }
                        public @Override JComponent[] synchMenuPresenters(JComponent[] items) {
                            return getMenuPresenters();
                        }
                    }
                    return new M();
                }
            }
            return new A();
        }
    }
    
    /**
     * Build the context menu for a project.
     * @param p a freeform project
     * @return a list of actions (or null for separators)
     */
    private static Action[] contextMenuCustomActions(FreeformProject p) {
        List<Action> actions = new ArrayList<Action>();
        Element genldata = p.getPrimaryConfigurationData();
        Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl != null) {
            Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
            if (contextMenuEl != null) {
                actions.add(null);
                for (Element actionEl : Util.findSubElements(contextMenuEl)) {
                    if (actionEl.getLocalName().equals("ide-action")) { // NOI18N
                        String cmd = actionEl.getAttribute("name");
                        String displayName;
                        if (COMMON_IDE_GLOBAL_ACTIONS.contains(cmd) || COMMON_NON_IDE_GLOBAL_ACTIONS.contains(cmd)) {
                            displayName = NbBundle.getMessage(Actions.class, "CMD_" + cmd);
                        } else {
                            // OK, fall back to raw name.
                            displayName = cmd;
                        }
                        actions.add(ProjectSensitiveActions.projectCommandAction(cmd, displayName, null));
                    } else if (actionEl.getLocalName().equals("separator")) { // NOI18N
                        actions.add(null);
                    } else {
                        assert actionEl.getLocalName().equals("action") : actionEl;
                        actions.add(new CustomAction(p, actionEl));
                    }
                }
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    private static final class CustomAction extends AbstractAction {

        private final FreeformProject p;
        private final Element actionEl;
        
        public CustomAction(FreeformProject p, Element actionEl) {
            this.p = p;
            this.actionEl = actionEl;
        }
        
        public void actionPerformed(ActionEvent e) {
            runConfiguredAction(p, actionEl, Lookup.EMPTY);
        }
        
        public boolean isEnabled() {
            String script;
            Element scriptEl = Util.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
            if (scriptEl != null) {
                script = Util.findText(scriptEl);
            } else {
                script = "build.xml"; // NOI18N
            }
            String scriptLocation = p.evaluator().evaluate(script);
            return p.helper().resolveFileObject(scriptLocation) != null;
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
    
    // Overridable for unit tests only:
    static TargetRunner TARGET_RUNNER = new TargetRunner();
    
    static class TargetRunner {
        public TargetRunner() {}
        public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props) {
            try {
                ActionUtils.runTarget(scriptFile, targetNameArray, props);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    /**
     * Prompt the user to make a binding for a common global command.
     * Available targets are shown. If one is selected, it is bound
     * (and also added to the context menu of the project), as if the user
     * had picked it in {@link TargetMappingPanel}.
     * @param command the command name as in {@link ActionProvider}
     * @return true if a binding was successfully created, false if it was cancelled
     * @see "#46886"
     */
    private boolean addGlobalBinding(String command) {
        try {
            return new UnboundTargetAlert(project, command).accepted();
        } catch (IOException e) {
            // Problem generating bindings - so skip it.
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
}
