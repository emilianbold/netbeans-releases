/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Antonin Nebuzelsky
 */
public class ResourceBundleBrandingPanel extends AbstractBrandingPanel
        implements ExplorerManager.Provider {

    private final ExplorerManager manager;
    private final AbstractNode waitRoot;
    private static final String WAIT_ICON_PATH =
            "org/netbeans/modules/apisupport/project/suite/resources/wait.png"; // NOI18N
    private RequestProcessor.Task refreshTask = null;
    private RequestProcessor RP = new RequestProcessor(ResourceBundleBrandingPanel.class.getName(), 1);
    private EditRBAction editRBAction = SystemAction.get (EditRBAction.class);
    private OpenRBAction openRBAction = SystemAction.get (OpenRBAction.class);
    private ExpandAllAction expandAllAction = SystemAction.get (ExpandAllAction.class);

    private BasicBrandingModel branding;
    private Project prj;

    public ResourceBundleBrandingPanel(BasicBrandingModel model) {
        super(getMessage("LBL_ResourceBundleTab"), model); //NOI18N
        
        initComponents();

        manager = new ExplorerManager();
        waitRoot = getWaitRoot();
        waitRoot.setName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        waitRoot.setDisplayName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        manager.setRootContext(waitRoot);

        branding = getBranding();
        prj = branding.getProject();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        view = new org.openide.explorer.view.BeanTreeView();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        view.setUseSubstringInQuickSearch(true);
        add(view, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private boolean initialized = false;

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        if (!initialized) {
            refresh();
            initialized = true;
        }
        view.requestFocusInWindow();
    }

    private void refresh() {
        if (refreshTask == null) {
            refreshTask = RP.create(new Runnable() {
                @Override
                public void run() {
                    prepareTree();
                }
            });
        }
        refreshTask.schedule(0);
    }

    private void prepareTree() {
        final List<Node> resourcebundlenodes = new LinkedList<Node>();
        RootNode rootNode = new RootNode(new Children.SortedArray() {
            @Override
            protected Collection<Node> initCollection() {
                return resourcebundlenodes;
            }
        });
        rootNode.setName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        rootNode.setDisplayName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        rootNode.setShortDescription(getMessage("LBL_ResourceBundlesDesc")); // NOI18N

        Set<File> jars;
        if (prj instanceof SuiteProject) {
            SuiteProject suite = (SuiteProject) prj;
            jars = LayerUtils.getPlatformJarsForSuiteComponentProject(suite);
        } else {
            jars = LayerUtils.getPlatformJarsForStandaloneProject(prj);
        }
        for (File file : jars) {
            try {
                URI juri = file.toURI();
                JarFile jf = new JarFile(file);
                String codeNameBase = ManifestManager.getInstance(jf.getManifest(), false).getCodeNameBase();
                Enumeration<JarEntry> entries = jf.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith("Bundle.properties")) { // NOI18N
                        try {
                            URL url = new URL("jar:" + juri + "!/" + entry.getName()); // NOI18N
                            FileObject fo = URLMapper.findFileObject(url);
                            DataObject dobj = DataObject.find(fo);
                            Node dobjnode = dobj.getNodeDelegate();
                            Node filternode = new BundleNode(dobjnode, fo.getPath(), codeNameBase);
                            resourcebundlenodes.add(filternode);
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        manager.setRootContext(rootNode);
    }//GEN-LAST:event_formComponentShown

    private class RootNode extends AbstractNode implements OpenCookie {

        public RootNode(Children children) {
            this(children, new InstanceContent());
        }

        private RootNode(Children children, InstanceContent content) {
            super (children, new AbstractLookup(content));
            content.add(this);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { expandAllAction.createContextAwareInstance(getLookup()) };
        }

        @Override
        public void open() {
            view.expandAll();
            view.requestFocusInWindow();
        }
    }

    static final class ExpandAllAction extends OpenAction {

        @Override
        public String getName() {
            return getMessage("LBL_ResourceBundlesExpand"); // NOI18N
        }
    }

    private class BundleNode extends FilterNode implements OpenCookie, Comparable<BundleNode> {

        public BundleNode(Node orig, String bundlepath, String codenamebase) {
            this (orig, bundlepath, codenamebase, new InstanceContent());
        }

        public BundleNode(Node orig, String bundlepath, String codenamebase, InstanceContent content) {
            super(orig, new BundleChildren (orig, bundlepath, codenamebase), new AbstractLookup(content));
            content.add(this);
            
            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                    | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION
                    | DELEGATE_GET_ACTIONS);

            setDisplayName(bundlepath);
            setShortDescription(codenamebase);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { openRBAction.createContextAwareInstance(getLookup()) };
        }

        @Override
        public Action getPreferredAction() {
            return null;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public void open() {
            EditCookie originalEC = getOriginal().getCookie(EditCookie.class);
            if (null != originalEC)
                originalEC.edit();
        }

        @Override
        public int compareTo(BundleNode o) {
            return getDisplayName().compareTo(o.getDisplayName());
        }
    }

    private class BundleChildren extends Children.Keys<Node> {

        Node original;
        private String bundlepath;
        private String codenamebase;

        public BundleChildren(Node orig, String bundlepath, String codenamebase) {
            super();
            original = orig;
            this.bundlepath = bundlepath;
            this.codenamebase = codenamebase;
        }

        @Override
        protected Node[] createNodes(Node key) {
            // filter out all keys related to module metadata
            if (!key.getDisplayName().toUpperCase().startsWith("OPENIDE-MODULE")) // NOI18N
                return new Node[] { new KeyNode(key, bundlepath, codenamebase) };
            return null;
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        private void refreshList() {
            List keys = new ArrayList();
            Node[] origChildren = original.getChildren().getNodes();
            for (Node node : origChildren) {
                keys.add(node);
            }
            setKeys(keys);
        }
    }

    private class KeyNode extends FilterNode implements EditCookie, OpenCookie {

        private String bundlepath;
        private String codenamebase;

        public KeyNode(Node orig, String bundlepath, String codenamebase) {
            this (orig, bundlepath, codenamebase, new InstanceContent());
        }

        public KeyNode(Node orig, String bundlepath, String codenamebase, InstanceContent content) {
            super(orig, null, new AbstractLookup(content));
            content.add(this);

            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                    | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION
                    | DELEGATE_GET_ACTIONS);

            this.bundlepath = bundlepath;
            this.codenamebase = codenamebase;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { editRBAction.createContextAwareInstance(getLookup()),
                openRBAction.createContextAwareInstance(getLookup()) };
        }

        @Override
        public Action getPreferredAction() {
            return editRBAction.createContextAwareInstance(getLookup());
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public void edit() {
            addKeyToBranding (bundlepath, codenamebase, getOriginal().getDisplayName());
        }

        @Override
        public void open() {
            EditCookie originalEC = getOriginal().getCookie(EditCookie.class);
            if (null != originalEC)
                originalEC.edit();
        }
    }

    static final class EditRBAction extends EditAction {

        @Override
        public String getName() {
            return getMessage ("LBL_ResourceBundlesAddToBranding"); // NOI18N
        }
    }

    static final class OpenRBAction extends OpenAction {

        @Override
        public String getName() {
            return getMessage ("LBL_ResourceBundlesViewOriginal"); // NOI18N
        }
    }

    private void addKeyToBranding (String bundlepath, String codenamebase, String key) {
        BrandingSupport.BundleKey bundleKey = getBranding().getGeneralBundleKeyForModification(codenamebase, bundlepath, key);
        NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(key + " = ", bundlepath, // NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
        String oldValue = bundleKey.getValue();
        inputLine.setInputText(oldValue);
        if (DialogDisplayer.getDefault().notify(inputLine)==NotifyDescriptor.OK_OPTION) {
            String newValue = inputLine.getInputText();
            if (newValue.compareTo(oldValue)!=0) {
                bundleKey.setValue(newValue);
                getBranding().addModifiedGeneralBundleKey(bundleKey);
                setModified();
            }
        }
    }

    @Override
    public void store() {
        // no-op, all modified bundle keys are stored through the model
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(ResourceBundleBrandingPanel.class, key);
    }

    private AbstractNode getWaitRoot() {
        return new AbstractNode(new Children.Array() {
            @Override
            protected Collection<Node> initCollection() {
                return Collections.singleton((Node) new WaitNode());
            }
        });
    }

    private final class WaitNode extends AbstractNode {

        public WaitNode() {
            super(Children.LEAF);
            setDisplayName(CustomizerComponentFactory.WAIT_VALUE);
            setIconBaseWithExtension(WAIT_ICON_PATH);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.BeanTreeView view;
    // End of variables declaration//GEN-END:variables

}
