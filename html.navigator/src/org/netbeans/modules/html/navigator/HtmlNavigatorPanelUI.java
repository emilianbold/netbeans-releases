/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.navigator;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
public class HtmlNavigatorPanelUI extends JPanel implements ExplorerManager.Provider {

    private static final String NOT_CONNECTED = "no connection";
    private static final String CONNECTED = "connected";
    
    public static RequestProcessor RP = new RequestProcessor(HtmlNavigatorPanelUI.class);
    private static int MESSAGE_SHOW_TIME = 5000; //5 seconds
    
    private ExplorerManager manager;
    private BeanTreeView view;
    private HtmlParserResult result;
    private Action[] panelActions;
    private WaitNode waitNode = new WaitNode();
    private Lookup lookup;
    private JLabel statusLabel;
    private JLabel stateLabel;
    private JPanel statusPanel;
    private PageModel pageModel;
    
    private final PropertyChangeListener pageInspectorListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageInspectorImpl.PROP_MODEL.equals(propName)) {
                setPageModel(PageInspectorImpl.getDefault().getPage());
            }
        }
    };
    
    private final PropertyChangeListener pageModelListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageModel.PROP_DOCUMENT.equals(propName)) {
                pageModelDocumentChanged();
            }
            
        }
    };

    public HtmlNavigatorPanelUI() {
        manager = new ExplorerManager();
        view = new BeanTreeView();

        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        
        statusLabel = new JLabel();;
        stateLabel = new JLabel(NOT_CONNECTED);
        stateLabel.setEnabled(false);
        statusPanel = new JPanel();
        statusPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(stateLabel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
        
        manager.setRootContext(waitNode);

        panelActions = new Action[]{};

        lookup = ExplorerUtils.createLookup(manager, getActionMap());

        //listen on the page inspector
        PageInspectorImpl.getDefault().addPropertyChangeListener(pageInspectorListener);
    }

    //Set a new PageModel. It will install a new PropertyChangeListener for the PageModel changes
    private void setPageModel(PageModel model) {
        if(this.pageModel != null) {
            this.pageModel.removePropertyChangeListener(pageModelListener);
        }
        this.pageModel = model;

        if(model != null) {
            model.addPropertyChangeListener(pageModelListener);
        }
        
        stateLabel.setEnabled(true);
        stateLabel.setText(CONNECTED);
        
        setStatusText("Inspected file has changed.");
    }
    
    private void pageModelDocumentChanged() {
        setStatusText("Fresh DOM obtained.");
        
        HtmlNode root = getRootNode();
        if(root != null) {
            root.refreshDOMStatus();
        }
    }
    
    public PageModel getPageModel() {
        return pageModel;
    }

    private void setStatusText(String text) {
        System.out.println("HtmlNavigator: " + text);
        
        statusLabel.setText(text);
        
        RP.post(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        statusLabel.setText(null);
                    }
                });
            }
        }, MESSAGE_SHOW_TIME);
    }

    public Action[] getActions() {
        return panelActions;
    }

    void activate() {
        showWaitNode();
    }

    void deactivate() {
    }

    private void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.setRootVisible(true);
                manager.setRootContext(waitNode);
            }
        });
    }

    public HtmlNode getRootNode() {
        Node node = manager.getRootContext();   
        return  node instanceof HtmlNode ? (HtmlNode) node : null;
    }
    
    public void selectElementNode(final int offset) {
        HtmlNode root = getRootNode();
        if(root == null) {
            return ;
        }
        
        final Node match = root.getNodeForOffset(offset);
        if(match == null) {
            return ;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Node[] selectedNodes = manager.getSelectedNodes();
                if (!(selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0] == match)) {
                    try {
                        manager.setSelectedNodes(new Node[]{match});
                    } catch (PropertyVetoException propertyVetoException) {
                        Exceptions.printStackTrace(propertyVetoException);
                    }
                }
            }
        });
    }

    public void refresh(HtmlParserResult result, int offset) {
        FileObject file = result.getSnapshot().getSource().getFileObject();
        refresh(new HtmlElementDescription(result.root(), file), offset);
    }

    private void refresh(final HtmlElementDescription description, final int offset) {
        final FileObject fileObject = description.getFileObject();
        final HtmlNode rootNode = getRootNode();

        if (rootNode != null && rootNode.getFileObject().equals(fileObject)) {
            //same file, just update the content
            final Runnable r = new Runnable() {
                public void run() {
                    long startTime = System.currentTimeMillis();
                    rootNode.updateRecursively(description);
                    long endTime = System.currentTimeMillis();
                    Logger.getLogger("TIMER").log(Level.FINE, "Navigator Merge",
                            new Object[]{fileObject, endTime - startTime});
                }
            };
            RP.post(r);
        } else {
            // new fileobject => refresh completely
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    view.setRootVisible(false);
                    view.setAutoWaitCursor(false);
                    manager.setRootContext(new HtmlNode(description, HtmlNavigatorPanelUI.this, fileObject));

                    int expandDepth = -1;

                    // impl hack: Node expansion is synced by VisualizerNode to the AWT thread, possibly delayed
                    expandNodeByDefaultRecursively(manager.getRootContext(), 0, expandDepth);
                    // set soe back only after all pending expansion events are processed:
//                    Mutex.EVENT.writeAccess(new Runnable() {
//                        @Override
//                        public void run() {
//                            view.setScrollOnExpand( scrollOnExpand );
//                        }
//                    });
                    view.setAutoWaitCursor(true);
                }
            };
            RP.post(r);
        }

        if (offset != -1) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    selectElementNode(offset);
                }
            });
        }
    }

    void performExpansion(final Collection<Node> expand, final Collection<Node> expandRec) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Node n : expand) {
                    expandNode(n);
                }

                for (Node n : expandRec) {
                    expandNodeByDefaultRecursively(n);
                }
            }
        };

        RP.post(r);
    }

    public void expandNode(Node n) {
        view.expandNode(n);
    }

    private void expandNodeByDefaultRecursively(Node node) {
        // using 0, -1 since we cannot quickly resolve currentDepth
        expandNodeByDefaultRecursively(node, 0, -1);
    }

    private void expandNodeByDefaultRecursively(Node node, int currentDepth, int maxDepth) {
        if (maxDepth >= 0 && currentDepth >= maxDepth) {
            return;
        }
        expandNode(node);
        for (Node subNode : node.getChildren().getNodes()) {
            expandNodeByDefaultRecursively(subNode, currentDepth + 1, maxDepth);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public Lookup getLookup() {
        return lookup;
    }

    private static class WaitNode extends AbstractNode {

        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/wait.png"); // NOI18N
        private String displayName;

        @NbBundle.Messages("lbl.wait.node=Please wait...")
        WaitNode() {
            super(Children.LEAF);
            displayName = Bundle.lbl_wait_node();
        }

        @Override
        public Image getIcon(int type) {
            return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return displayName;
        }
    }
}
