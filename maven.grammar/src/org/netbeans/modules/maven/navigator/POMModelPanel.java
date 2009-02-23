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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import javax.swing.text.JTextComponent;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.Project;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  mkleint
 */
public class POMModelPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {

    private static final String NAVIGATOR_SHOW_UNDEFINED = "navigator.showUndefined"; //NOi18N
    private transient ExplorerManager explorerManager = new ExplorerManager();
    
    private BeanTreeView treeView;
    private DataObject current;
    private FileChangeAdapter adapter = new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                showWaitNode();
                RequestProcessor.getDefault().post(POMModelPanel.this);
            }
        };
    private TapPanel filtersPanel;

    private boolean filterIncludeUndefined;

    /** Creates new form POMInheritancePanel */
    public POMModelPanel() {
        initComponents();
        filterIncludeUndefined = NbPreferences.forModule(POMModelPanel.class).getBoolean(NAVIGATOR_SHOW_UNDEFINED, false);

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
        getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node[] nds = getExplorerManager().getSelectedNodes();
                    if (nds.length == 1) {
                        selectByNode(nds[0], null);
                    }
                }
            }

            private void selectByNode(Node nd, POMQName name) {
                if (nd == null) {
                    return;
                }
                POMModelVisitor.POMCutHolder holder = nd.getLookup().lookup(POMModelVisitor.POMCutHolder.class);
                if (holder != null) {
                    Object[] objs = holder.getCutValues();
                    if (objs[0] != null && objs[0] instanceof POMComponent) {
                        POMComponent pc = (POMComponent) objs[0];
                        int pos;
                        if (name != null) {
                            //TODO if not a simple child, then this fails.
                            pos = pc.findChildElementPosition(name.getQName());
                        } else {
                            pos = pc.getModel().getAccess().findPosition(pc.getPeer());
                        }
                        if (pos != -1) {
                            select(pos);
                        }
                    } else if (objs[0] != null && name == null) {
                        selectByNode(nd.getParentNode(), nd.getLookup().lookup(POMQName.class));
                    }
                }
            }
        });
    }

    private void select(final int pos) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditorCookie.Observable ec = current.getLookup().lookup(EditorCookie.Observable.class);
                if (ec == null) {
                    System.out.println("ec is null");
                    return;
                }
                JEditorPane[] panes = ec.getOpenedPanes();
                if (panes != null && panes.length > 0) {
                    // editor already opened, so just select
                    JTextComponent component = panes[0];
                    component.setCaretPosition(pos);
                } else  {
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
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = d;
        current.getPrimaryFile().addFileChangeListener(adapter);
        showWaitNode();
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        if (current != null) {
            File file = FileUtil.toFile(current.getPrimaryFile());
            // can be null for stuff in jars?
            if (file != null) {
                try {
                    ModelLineage lin = EmbedderFactory.createModelLineage(file, EmbedderFactory.createOnlineEmbedder(), false);
                    @SuppressWarnings("unchecked")
                    Iterator<File> it = lin.fileIterator();
                    List<Project> prjs = new ArrayList<Project>();
                    POMQNames names = null;
                    while (it.hasNext()) {
                        File pom = it.next();
                        FileUtil.refreshFor(pom);
                        FileObject fo = FileUtil.toFileObject(pom);
                        if (fo != null) {
                            ModelSource ms = org.netbeans.modules.maven.model.Utilities.createModelSource(fo);
                            POMModel mdl = POMModelFactory.getDefault().getModel(ms);
                            if (mdl != null) {
                                prjs.add(mdl.getProject());
                                names = mdl.getPOMQNames();
                            } else {
                                System.out.println("no model for " + pom);
                            }
                        } else {
                            System.out.println("no fileobject for " + pom);
                        }
                    }
                    final POMModelVisitor.POMCutHolder hold = new POMModelVisitor.SingleObjectCH(names, names.PROJECT, "root", Project.class,  filterIncludeUndefined);
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
            tg1.setSelected(filterIncludeUndefined);
            toolbar.add(tg1);
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);

            box.add(toolbar);
            return box;

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
        an.setIconBaseWithExtension("org/netbeans/modules/maven/navigator/wait.gif");
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
    

    private class ShowUndefinedAction extends AbstractAction {

        public ShowUndefinedAction() {
            putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/filterHideFields.gif")));
            putValue(SHORT_DESCRIPTION, "Show only POM elements defined in at least one place.");
        }


        public void actionPerformed(ActionEvent e) {
            filterIncludeUndefined = !filterIncludeUndefined;
            NbPreferences.forModule(POMModelPanel.class).putBoolean( NAVIGATOR_SHOW_UNDEFINED, filterIncludeUndefined);

            POMModelVisitor.PomChildren keys = (POMModelVisitor.PomChildren) explorerManager.getRootContext().getChildren();
            keys.reshow(filterIncludeUndefined);
        }
        
    }
}

