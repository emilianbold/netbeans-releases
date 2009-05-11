/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.navigator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.navigator.POMModelVisitor.POMCutHolder;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.w3c.dom.NodeList;

/**
 *
 * @author  mkleint
 */
public class POMModelPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable, CaretListener {

    private static final String NAVIGATOR_SHOW_UNDEFINED = "navigator.showUndefined"; //NOI18N
    private transient ExplorerManager explorerManager = new ExplorerManager();
    
    private BeanTreeView treeView;
    private DataObject current;
    private JTextComponent currentComponent;
    private int currentDot = -1;
    private RequestProcessor.Task caretTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            if (currentDot != -1) {
                updateCaret(currentDot);
            }
        }
    });


    private FileChangeAdapter adapter = new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                showWaitNode();
                RequestProcessor.getDefault().post(POMModelPanel.this);
            }
        };
    private TapPanel filtersPanel;

    private Configuration configuration;

    /** Creates new form POMInheritancePanel */
    public POMModelPanel() {
        initComponents();
        configuration = new Configuration();
        boolean filterIncludeUndefined = NbPreferences.forModule(POMModelPanel.class).getBoolean(NAVIGATOR_SHOW_UNDEFINED, true);
        configuration.setFilterUndefined(filterIncludeUndefined);

        treeView = (BeanTreeView)jScrollPane1;
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(POMModelPanel.class, "TIP_TapPanel", keyText)); //NOI18N

        JComponent buttons = createFilterButtons();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N

        add(filtersPanel, BorderLayout.SOUTH);
    }

    static void selectByNode(Node nd, String elementName, int layer) {
        if (nd == null) {
            return;
        }
        POMModelVisitor.POMCutHolder holder = nd.getLookup().lookup(POMModelVisitor.POMCutHolder.class);
        if (holder != null) {
            Object[] objs = holder.getCutValues();
            if (layer >= objs.length) {
                return;
            }
            if (objs[layer] != null && objs[layer] instanceof POMComponent) {
                POMComponent pc = (POMComponent) objs[layer];
                int pos;
                if (elementName != null) {
                    QName qn = POMQName.createQName(elementName, pc.getModel().getPOMQNames().isNSAware());
                    NodeList nl = pc.getPeer().getElementsByTagName(qn.getLocalPart());
                    if (nl != null && nl.getLength() > 0) {
                        pos = pc.getModel().getAccess().findPosition(nl.item(0));
                    } else {
                        pos = -1;
                    }
                } else {
                    pos = pc.getModel().getAccess().findPosition(pc.getPeer());
                }
                if (pos != -1) {
                    select(nd, pos, layer);
                }
            } else if (objs[layer] != null && elementName == null) {
                String name = getElementNameFromNode(nd);
                selectByNode(nd.getParentNode(), name, layer);
            }
        }
    }


    private static void select(final Node node, final int pos, final int layer) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                POMCutHolder hold = node.getLookup().lookup(POMCutHolder.class);
                POMModel[] models = hold.getSource();
                if (models.length <= layer) {
                    return;
                }
                POMModel mdl = models[layer];
                DataObject dobj = mdl.getModelSource().getLookup().lookup(DataObject.class);
                if (dobj == null) {
                    return;
                }
                dobj = ROUtil.checkPOMFileObjectReadOnly(dobj);
                EditorCookie.Observable ec = dobj.getLookup().lookup(EditorCookie.Observable.class);
                if (ec == null) {
                    return;
                }
                JEditorPane[] panes = ec.getOpenedPanes();
                if (panes != null && panes.length > 0) {
                    // editor already opened, so just select
                    JTextComponent component = panes[0];
                    component.setCaretPosition(pos);
                    TopComponent tc = NbEditorUtilities.getOuterTopComponent(component);
                    if (!tc.isVisible()) {
                        tc.requestVisible();
                    }
                } else {
                    // editor not opened yet
                    ec.open();
                    try {
                        ec.openDocument(); //wait to editor to open
                        panes = ec.getOpenedPanes();
                        if (panes != null && panes.length > 0) {
                            JTextComponent component = panes[0];
                            component.setCaretPosition(pos);
                        }
                    } catch (IOException ioe) {
                    }
                }

            }
        });
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    void navigate(DataObject d) {
        cleanup();
        current = d;
        current.getPrimaryFile().addFileChangeListener(adapter);
        showWaitNode();
        RequestProcessor.getDefault().post(this);
    }

    void cleanup() {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        if (currentComponent != null) {
            currentComponent.removeCaretListener(this);
        }
    }
    
    public void run() {
        DataObject currentFile = current;
        //#164852 somehow a folder dataobject slipped in, test mimetype to avoid that.
        // the root cause of the problem is unknown though
        if (currentFile != null && "text/x-maven-pom+xml".equals(currentFile.getPrimaryFile().getMIMEType())) { //NOI18N
            File file = FileUtil.toFile(currentFile.getPrimaryFile());
            // can be null for stuff in jars?
            if (file != null) {
                try {
                    ModelLineage lin = EmbedderFactory.createModelLineage(file, EmbedderFactory.getOnlineEmbedder(), false);
                    @SuppressWarnings("unchecked")
                    Iterator<File> it = lin.fileIterator();
                    List<Project> prjs = new ArrayList<Project>();
                    List<POMModel> mdls = new ArrayList<POMModel>();
                    POMQNames names = null;
                    while (it.hasNext()) {
                        File pom = FileUtil.normalizeFile(it.next());
                        FileUtil.refreshFor(pom);
                        FileObject fo = FileUtil.toFileObject(pom);
                        if (fo != null) {
                            ModelSource ms = org.netbeans.modules.maven.model.Utilities.createModelSource(fo);
                            POMModel mdl = POMModelFactory.getDefault().getModel(ms);
                            if (mdl != null) {
                                prjs.add(mdl.getProject());
                                mdls.add(mdl);
                                names = mdl.getPOMQNames();
                            } else {
                                System.out.println("no model for " + pom);
                            }
                        } else {
                            System.out.println("no fileobject for " + pom);
                        }
                    }
                    final POMModelVisitor.POMCutHolder hold = new POMModelVisitor.SingleObjectCH(mdls.toArray(new POMModel[0]), names, names.PROJECT, Project.class,  configuration); //NOI18N
                    for (Project p : prjs) {
                        hold.addCut(p);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           treeView.setRootVisible(false);
                           explorerManager.setRootContext(hold.createNode());
                        } 
                    });
                } catch (ProjectBuildingException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Error reading model lineage", ex);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           treeView.setRootVisible(true);
                           explorerManager.setRootContext(createErrorNode());
                        }
                    });
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                       treeView.setRootVisible(false);
                       explorerManager.setRootContext(createEmptyNode());
                    } 
                });
            }
            
            //now attach the listener to the textcomponent
            final EditorCookie.Observable ec = currentFile.getLookup().lookup(EditorCookie.Observable.class);
            if (ec == null) {
                //how come?
                return;
            }
            try {
                ec.openDocument(); //wait to editor to open
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        // editor already opened, so just select
                        JTextComponent component = panes[0];
                        component.removeCaretListener(POMModelPanel.this);
                        component.addCaretListener(POMModelPanel.this);
                        currentComponent = component;
                    }
                }
            } );

        }
    }

    /**
     * 
     */
    void release() {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(false);
               explorerManager.setRootContext(createEmptyNode());
            } 
        });
    }

    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(createWaitNode());
            } 
        });
    }

    private JComponent createFilterButtons() {
        Box box = new Box(BoxLayout.X_AXIS);
        box.setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL) {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            JToggleButton tg1 = new JToggleButton(new ShowUndefinedAction());
            tg1.setSelected(configuration.isFilterUndefined());
            toolbar.add(tg1);
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);

            box.add(toolbar);
            return box;

    }

    private static String getElementNameFromNode(Node childNode) {
        String qnName;
        QName qn = childNode.getLookup().lookup(QName.class);
        if (qn == null) {
            POMQName pqn = childNode.getLookup().lookup(POMQName.class);
            if (pqn != null) {
                qn = pqn.getQName();
            }
        }
        if (qn != null) {
            qnName = qn.getLocalPart();
        } else {
            //properties
            qnName = childNode.getLookup().lookup(String.class);
        }
        return qnName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    private static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension("org/netbeans/modules/maven/navigator/wait.gif"); //NOI18N
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Wait"));
        return an;
    }

    private static Node createEmptyNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        return an;
    }

    private static Node createErrorNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Error"));
        return an;
    }


    /**
     * returns true if the value is defined in current pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean isValueDefinedInCurrent(Object[] values) {
        return values[0] != null;
    }

    /**
     * returns true if the value is defined in current pom
     * and one of the parent poms as well.
     */
    static boolean overridesParentValue(Object[] values) {
        if (values.length <= 1) {
            return false;
        }
        boolean curr = values[0] != null;
        boolean par = false;
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null) {
                par = true;
                break;
            }
        }
        return curr && par;

    }

    /**
     * returns true if the value is defined in in any pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean definesValue(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return level where the last value is defined, 0 - current file, 1 - it's parent,... -1 not present..
     *
     */
    static int currentValueDepth(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return i;
            }
        }
        return -1;
    }



    /**
     * gets the first defined value from the list. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static String getValidValue(String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return values[i];
            }
        }
        return null;
    }

    public void caretUpdate(CaretEvent e) {
        if (e.getSource() != currentComponent) {
            ((JTextComponent)e.getSource()).removeCaretListener(this);
            //just a double check we do't get a persistent leak here..
            return;
        }
        currentDot = e.getDot();
        caretTask.schedule(1000);
    }

    private void updateCaret(int caret) {
        POMCutHolder pch = getExplorerManager().getRootContext().getLookup().lookup(POMCutHolder.class);
        if (pch != null) {
            POMComponent pc = (POMComponent) pch.getSource()[0].findComponent(caret);
            Stack<POMComponent> stack = new Stack<POMComponent>();
            while (pc != null) {
                stack.push(pc);
                pc = pc.getParent();
            }
            Node currentNode = getExplorerManager().getRootContext();
            if (stack.empty()) {
                return;
            }
            //pop the project root.
            POMComponent currentpc = stack.pop();
            boolean found = false;
            while (!stack.empty()) {
                currentpc = stack.pop();
                found = false;
                Node[] childs = currentNode.getChildren().getNodes(true);
                Class listClass = null;
                if (currentpc instanceof ModelList) {
                    ModelList lst = (ModelList)currentpc;
                    listClass = lst.getListClass();
                }
                for (Node childNode : childs) {
                    POMCutHolder holder = childNode.getLookup().lookup(POMCutHolder.class);
                    Object currentObj = holder.getCutValues()[0];
                    if (currentObj != null && currentObj instanceof POMComponent) {
                        if (currentObj == currentpc) {
                            treeView.expandNode(currentNode);
                            currentNode = childNode;
                            found = true;
                            break;
                        }
                    }
                    if (currentObj != null && currentObj instanceof String) {
                        String qnName = getElementNameFromNode(childNode);

                        if (qnName == null || (!(currentpc instanceof POMExtensibilityElement))) {
                            //TODO can be also string in lookup;
                            continue;
                        }
                        POMExtensibilityElement exEl = (POMExtensibilityElement) currentpc;
                        if (exEl.getQName().getLocalPart().equals(qnName)) {
                            treeView.expandNode(currentNode);
                            currentNode = childNode;
                            found = true;
                            break;
                        }
                    }
                    if (currentObj != null && holder instanceof POMModelVisitor.ListObjectCH
                            && listClass != null) {
                        POMModelVisitor.ListObjectCH loh = (POMModelVisitor.ListObjectCH)holder;
                        if (loh.getListClass().equals(listClass)) {
                            treeView.expandNode(currentNode);
                            currentNode = childNode;
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    break;
                }
            }
            if (found) {
                try {
                    getExplorerManager().setSelectedNodes(new Node[]{currentNode});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    

    private class ShowUndefinedAction extends AbstractAction {

        public ShowUndefinedAction() {
            putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/filterHideFields.gif"))); //NOI18N
            putValue(SHORT_DESCRIPTION, org.openide.util.NbBundle.getMessage(POMModelPanel.class, "DESC_FilterUndefined"));
        }


        public void actionPerformed(ActionEvent e) {
            boolean current = configuration.isFilterUndefined();
            configuration.setFilterUndefined(!current);
            NbPreferences.forModule(POMModelPanel.class).putBoolean( NAVIGATOR_SHOW_UNDEFINED, !current);
        }
        
    }

    static class Configuration {

        private boolean filterUndefined;
        public static final String PROP_FILTERUNDEFINED = "filterUndefined"; //NOI18N

        /**
         * Get the value of filterUndefined
         *
         * @return the value of filterUndefined
         */
        public boolean isFilterUndefined() {
            return filterUndefined;
        }

        /**
         * Set the value of filterUndefined
         *
         * @param filterUndefined new value of filterUndefined
         */
        public void setFilterUndefined(boolean filterUndefined) {
            boolean oldFilterUndefined = this.filterUndefined;
            this.filterUndefined = filterUndefined;
            propertyChangeSupport.firePropertyChange(PROP_FILTERUNDEFINED, oldFilterUndefined, filterUndefined);
        }
        private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

        /**
         * Add PropertyChangeListener.
         *
         * @param listener
         */
        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        /**
         * Remove PropertyChangeListener.
         *
         * @param listener
         */
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

    }
}

