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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.LogicalViews;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 * One freeform project.
 * @author Jesse Glick
 */
final class FreeformProject implements Project {
    
    public static final String NS = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    
    public FreeformProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = initEval();
        lookup = initLookup();
    }
    
    private PropertyEvaluator initEval() throws IOException {
        PropertyProvider preprovider = helper.getStockPropertyPreprovider();
        List/*<PropertyProvider>*/ defs = new ArrayList();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element properties = Util.findElement(genldata, "properties", NS); // NOI18N
        if (properties != null) {
            List/*<Element>*/ props = Util.findSubElements(properties);
            Iterator it = props.iterator();
            while (it.hasNext()) {
                Element e = (Element)it.next();
                if (e.getLocalName().equals("property")) { // NOI18N
                    defs.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap(e.getAttribute("name"), Util.findText(e))));
                } else {
                    assert e.getLocalName().equals("property-file") : e;
                    String fname = Util.findText(e);
                    if (fname.indexOf("${") != -1) {
                        throw new IOException("XXX not yet implemented");
                    }
                    FileObject propfile = helper.resolveFileObject(fname);
                    if (propfile != null) {
                        // XXX need to listen to changes in this file too
                        Properties p = new Properties();
                        InputStream is = propfile.getInputStream();
                        try {
                            p.load(is);
                        } finally {
                            is.close();
                        }
                        defs.add(PropertyUtils.fixedPropertyProvider(p));
                    }
                }
            }
        }
        return PropertyUtils.sequentialPropertyEvaluator(preprovider, (PropertyProvider[]) defs.toArray(new PropertyProvider[defs.size()]));
    }
    
    private Lookup initLookup() throws IOException {
        return Lookups.fixed(new Object[] {
            new Info(),
            initSources(),
            new Actions(),
            new View(),
        });
    }
    
    private Sources initSources() {
        final SourcesHelper h = new SourcesHelper(helper, evaluator());
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element foldersE = Util.findElement(genldata, "folders", NS); // NOI18N
        if (foldersE != null) {
            List/*<Element>*/ folders = Util.findSubElements(foldersE);
            Iterator it = folders.iterator();
            while (it.hasNext()) {
                Element folderE = (Element)it.next();
                Element locationE = Util.findElement(folderE, "location", NS); // NOI18N
                String location = Util.findText(locationE);
                if (folderE.getLocalName().equals("build-folder")) { // NOI18N
                    h.addNonSourceRoot(location);
                } else {
                    assert folderE.getLocalName().equals("source-folder") : folderE;
                    Element nameE = Util.findElement(folderE, "label", NS); // NOI18N
                    String name = Util.findText(nameE);
                    Element typeE = Util.findElement(folderE, "type", NS); // NOI18N
                    if (typeE != null) {
                        String type = Util.findText(typeE);
                        h.addTypedSourceRoot(location, type, name, null, null);
                    } else {
                        h.addPrincipalSourceRoot(location, name, null, null);
                    }
                }
            }
        }
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return h.createSources();
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private final class Info implements ProjectInformation {
        
        public Info() {}
        
        public String getName() {
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element nameEl = Util.findElement(genldata, "name", NS); // NOI18N
            return Util.findText(nameEl);
        }
        
        public String getDisplayName() {
            return getName();
        }
        
        public Icon getIcon() {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/AntIcon.gif", true)); // NOI18N
        }
        
        public Project getProject() {
            return FreeformProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
    }
    
    private final class Actions implements ActionProvider {
        
        public Actions() {}
        
        public String[] getSupportedActions() {
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element actionsEl = Util.findElement(genldata, "ide-actions", NS); // NOI18N
            List/*<Element>*/ actions = Util.findSubElements(actionsEl);
            List/*<String>*/ names = new ArrayList(actions.size());
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                Element actionEl = (Element)it.next();
                if (Util.findElement(actionEl, "context", NS) != null) { // NOI18N
                    throw new UnsupportedOperationException("XXX No support for <context> yet"); // NOI18N
                }
                names.add(actionEl.getAttribute("name")); // NOI18N
            }
            return (String[])names.toArray(new String[names.size()]);
        }
        
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            // XXX check context, and also perhaps existence of script
            return true;
        }
        
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element actionsEl = Util.findElement(genldata, "ide-actions", NS); // NOI18N
            List/*<Element>*/ actions = Util.findSubElements(actionsEl);
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                Element actionEl = (Element)it.next();
                if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                    runConfiguredAction(actionEl);
                    return;
                }
            }
            throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
        }
        
    }
    
    /**
     * Run a project action as described by subelements <script> and <target>.
     */
    private void runConfiguredAction(Element actionEl) {
        String script;
        Element scriptEl = Util.findElement(actionEl, "script", NS); // NOI18N
        if (scriptEl != null) {
            script = Util.findText(scriptEl);
        } else {
            script = "build.xml"; // NOI18N
        }
        String scriptLocation = evaluator().evaluate(script);
        FileObject scriptFile = helper.resolveFileObject(scriptLocation);
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
    
    private final class View implements LogicalViewProvider {
        
        public View() {}
        
        public Node createLogicalView() {
            return new RootNode(FreeformProject.this);
        }
        
        public Node findPath(Node root, Object target) {
            // XXX
            return null;
        }
        
    }
    
    private static final class RootNode extends AbstractNode {
        
        private final FreeformProject p;
        
        public RootNode(FreeformProject p) {
            super(new RootChildren(p), Lookups.singleton(p));
            this.p = p;
        }
        
        public String getName() {
            return ProjectUtils.getInformation(p).getName();
        }
        
        public String getDisplayName() {
            return ProjectUtils.getInformation(p).getDisplayName();
        }
        
        public String getShortDescription() {
            // XXX I18N
            return "Freeform project in " + FileUtil.toFile(p.getProjectDirectory()).getAbsolutePath();
        }
        
        public Image getIcon(int type) {
            return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/AntIcon.gif", true); // NOI18N
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public Action[] getActions(boolean context) {
            List/*<Action>*/ actions = new ArrayList();
            actions.add(LogicalViews.newFileAction());
            // Requested actions.
            Element genldata = p.helper.getPrimaryConfigurationData(true);
            Element viewEl = Util.findElement(genldata, "view", NS); // NOI18N
            if (viewEl != null) {
                Element contextMenuEl = Util.findElement(viewEl, "context-menu", NS); // NOI18N
                if (contextMenuEl != null) {
                    actions.add(null);
                    List/*<Element>*/ actionEls = Util.findSubElements(contextMenuEl);
                    Iterator it = actionEls.iterator();
                    while (it.hasNext()) {
                        Element actionEl = (Element)it.next();
                        if (actionEl.getLocalName().equals("ide-action")) { // NOI18N
                            String cmd = actionEl.getAttribute("name");
                            String displayName = cmd; // XXX I18N
                            actions.add(ProjectSensitiveActions.projectCommandAction(cmd, displayName, null));
                        } else {
                            assert actionEl.getLocalName().equals("action") : actionEl;
                            actions.add(new CustomAction(actionEl));
                        }
                    }
                }
            }
            // Back to generic actions.
            actions.add(null);
            actions.add(LogicalViews.setAsMainProjectAction());
            actions.add(LogicalViews.openSubprojectsAction());
            actions.add(LogicalViews.closeProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            actions.add(null);
            actions.add(SystemAction.get(ToolsAction.class));
            actions.add(null);
            actions.add(LogicalViews.customizeProjectAction());
            return (Action[])actions.toArray(new Action[actions.size()]);
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
        private final class CustomAction extends AbstractAction {
            
            private final Element actionEl;
            
            public CustomAction(Element actionEl) {
                this.actionEl = actionEl;
            }
            
            public void actionPerformed(ActionEvent e) {
                p.runConfiguredAction(actionEl);
            }
            
            public boolean isEnabled() {
                // XXX check for existence of script, perhaps
                return true;
            }
            
            public Object getValue(String key) {
                if (key.equals(Action.NAME)) {
                    Element labelEl = Util.findElement(actionEl, "label", NS); // NOI18N
                    return Util.findText(labelEl);
                } else {
                    return super.getValue(key);
                }
            }
            
        }
        
    }
    
    private static final class RootChildren extends Children.Keys/*<Element>*/ {
        
        private final FreeformProject p;
        
        public RootChildren(FreeformProject p) {
            this.p = p;
        }
        
        protected void addNotify() {
            super.addNotify();
            Element genldata = p.helper.getPrimaryConfigurationData(true);
            Element viewEl = Util.findElement(genldata, "view", NS); // NOI18N
            if (viewEl != null) {
                Element itemsEl = Util.findElement(viewEl, "items", NS); // NOI18N
                setKeys(Util.findSubElements(itemsEl));
            } else {
                setKeys(Collections.EMPTY_SET);
            }
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Element itemEl = (Element)key;
            Element locationEl = Util.findElement(itemEl, "location", NS); // NOI18N
            String location = Util.findText(locationEl);
            String locationEval = p.evaluator().evaluate(location);
            FileObject file = p.helper.resolveFileObject(locationEval);
            if (file == null) {
                // Not there... skip this node.
                return null;
            }
            String label;
            Element labelEl = Util.findElement(itemEl, "label", NS); // NOI18N
            if (labelEl != null) {
                label = Util.findText(labelEl);
            } else {
                label = file.getNameExt();
            }
            Node base;
            boolean pkgUi;
            DataObject fileDO;
            try {
                fileDO = DataObject.find(file);
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
            if (itemEl.getLocalName().equals("source-folder")) { // NOI18N
                if (!file.isFolder()) {
                    // Just a file. Skip it.
                    return null;
                }
                String style = itemEl.getAttribute("style"); // NOI18N
                if (style.equals("tree")) { // NOI18N
                    // For now use FolderNode. XXX want to use VisibilityQuery etc.
                    // so may need to use some projects-specific call in the future.
                    pkgUi = false;
                    base = fileDO.getNodeDelegate();
                } else {
                    assert style.equals("packages") : style;
                    pkgUi = true;
                    base = new AbstractNode(PackageView.createPackageView(file), Lookups.singleton(fileDO));
                }
            } else {
                assert itemEl.getLocalName().equals("source-file") : itemEl;
                pkgUi = false;
                base = fileDO.getNodeDelegate();
            }
            return new Node[] {new ViewItemNode(base, location, label, pkgUi)};
        }
        
    }
    
    private static final class ViewItemNode extends FilterNode {
        
        private final String name;
        private final String displayName;
        private final boolean pkgUi;
        
        public ViewItemNode(Node orig, String name, String displayName, boolean pkgUi) {
            super(orig);
            this.name = name;
            this.displayName = displayName;
            this.pkgUi = pkgUi;
        }
        
        public String getName() {
            return name;
        }        
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean canRename() {
            return false;
        }        
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
        public Action[] getActions(boolean context) {
            if (pkgUi) {
                return new Action[] {
                    SystemAction.get(OpenLocalExplorerAction.class),
                    SystemAction.get(FindAction.class),
                    null,
                    LogicalViews.newFileAction(),
                };
            } else {
                return super.getActions(context);
            }
        }
        
        public Image getIcon(int type) {
            if (pkgUi) {
                return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/packageRoot.gif", true); // NOI18N
            } else {
                return super.getIcon(type);
            }
        }
        
        public Image getOpenedIcon(int type) {
            if (pkgUi) {
                return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/packageRootOpen.gif", true); // NOI18N
            } else {
                return super.getOpenedIcon(type);
            }
        }
        
    }
    
}
