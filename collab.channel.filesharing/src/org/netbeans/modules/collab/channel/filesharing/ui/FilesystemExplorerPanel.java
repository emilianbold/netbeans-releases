/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.collab.channel.filesharing.ui;

import org.openide.explorer.*;
import org.openide.explorer.view.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import java.awt.*;
import java.awt.dnd.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.TreeCellRenderer;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.eventhandler.SyncWaitPanel;
import org.netbeans.modules.collab.core.Debug;


/**
 * Panel for FilesharingCollablet Filesystem Explorer
 *
 * @author  Todd Fast <todd.fast@sun.com>
 * @version 1.0
 */
public class FilesystemExplorerPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider,
    PropertyChangeListener, FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private BeanTreeView treeView;
    private JLabel statusLine = new JLabel(" ");
    private FilesharingContext context;
    private ExplorerManager explorerManager;
    private Lookup lookup;
    private DataObject dataObject = null;
    private FileObject file = null;
    private HashMap someMap = new HashMap();
    private int number = 0;
    private ProjectsRootNode rootNode = null;
    private Node expandTreeNode;
    private int fileCount = 0;
    private String statusMessage = "";

    /**
     *
     * @param channel
     */
    public FilesystemExplorerPanel(FilesharingContext context) {
        super();
        this.context = context;
        initialize();
        enablePanel();
        context.setFilesystemExplorer(this);
    }

    /**
     *
     *
     */
    protected void initialize() {
        setLayout(new BorderLayout());

        // Reduce the preferred size to a reasonable one
        setPreferredSize(new Dimension(300, 200));

        // Create explorer tree
        treeView = new BeanTreeView();
        treeView.setBorder(UIManager.getBorder("ScrollPane.border"));
        treeView.setRootVisible(true);
        treeView.setAllowedDropActions(DnDConstants.ACTION_COPY);

        //treeView.setAllowedDragActions(DnDConstants.ACTION_COPY_OR_MOVE);
        if (getContext().isReadOnlyConversation()) {
            treeView.setDropTarget(true);
        }

        add(treeView, BorderLayout.CENTER);

        // Add the status line
        JPanel statusLinePanel = new JPanel();
        statusLinePanel.setLayout(new BorderLayout());
        statusLine.setBorder(new ThinBevelBorder(ThinBevelBorder.LOWERED));
        statusLinePanel.add(statusLine, BorderLayout.CENTER);
        add(statusLinePanel, BorderLayout.SOUTH);
        setStatusLineText(
            NbBundle.getMessage(FilesystemExplorerPanel.class, "LBL_FilesystemExplorerPanel_NumFiles", new Integer(0))
        );

        // Populate the action map
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(getExplorerManager()));

        //        map.put(DefaultEditorKit.cutAction,
        //			ExplorerUtils.actionCut(getExplorerManager()));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(getExplorerManager()));
        map.put("delete", ExplorerUtils.actionDelete(getExplorerManager(), true)); // or false

        // Create the lookup
        lookup = ExplorerUtils.createLookup(getExplorerManager(), map);

        try {
            FileSystem filesystem = getContext().getCollabFilesystem();

            if (filesystem == null) {
                throw new IllegalArgumentException("Filesystem was null");
            }

            Debug.log("CollabFileHandlerSupport", "FilesystemExplorerPanel, user: " + context.getLoginUser());
            rootNode = new ProjectsRootNode(filesystem);
            getExplorerManager().setRootContext(rootNode);
            rootNode.init();
        } catch (Exception e) {
            // TODO: Proper error handling
            Debug.errorManager.notify(e);
        }

        // Accessibility
        getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(FilesystemExplorerPanel.class, "ACSD_FilesystemExplorerPanel_Name")
        ); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(FilesystemExplorerPanel.class, "ACSD_FilesystemExplorerPanel_Description")
        ); // NOI18N

        setHelpCtx();
    }

    /**
     *set help ctx map id for context sensitive help
     *
     */
    private void setHelpCtx() {
        HelpCtx.setHelpIDString(this, "collab_sharing_files"); //NOI18n
    }

    /**
     * updateStatusLineText
     *
     * @param fileEvent
     * @param fileCreated
     */
    public void updateStatusLineText(int fileCount, String statusMessage) {
        this.fileCount = fileCount;
        this.statusMessage = statusMessage;

        String value = NbBundle.getMessage(
                FilesystemExplorerPanel.class, "LBL_FilesystemExplorerPanel_NumFiles", new Integer(fileCount)
            );
        setStatusLineText(value + "          " + statusMessage);
    }

    /**
     *
     * @return filesharing context
     */
    public FilesharingContext getContext() {
        return this.context;
    }

    /**
     *
     * @return explorer manager
     */
    public synchronized ExplorerManager getExplorerManager() {
        if (explorerManager == null) {
            explorerManager = new ExplorerManager();
            explorerManager.addPropertyChangeListener(this);
        }

        return explorerManager;
    }

    /**
     *
     * @return ide lookup
     */
    public Lookup getLookup() {
        return lookup;
    }

    /**
     *
     *
     */
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(getExplorerManager(), true);
    }

    /**
     *
     *
     */
    public void removeNotify() {
        ExplorerUtils.activateActions(getExplorerManager(), false);
        super.removeNotify();
    }

    /**
     *
     * @param event
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (
            event.getSource() instanceof ExplorerManager &&
                ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())
        ) {
            ((FilesharingCollablet) getContext().getChannel()).fireNodeSelectionChange((Node[]) event.getNewValue());
        } else if (event.getPropertyName().equals(FILE_COUNT_CHANGED)) {
            final int fileCount = ((Integer) event.getNewValue()).intValue();
            Debug.out.println("FEP::fileCount:" + fileCount);
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        updateStatusLineText(fileCount, statusMessage);
                    }
                }
            );
        } else if (event.getPropertyName().equals(FS_STATUS_CHANGE)) {
            final String statusMessage = ((String) event.getNewValue());
            Debug.out.println("FEP::message:" + statusMessage);
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        updateStatusLineText(fileCount, statusMessage);
                    }
                }
            );
        }
    }

    /**
     *
     * @return status line text
     */
    public String getStatusLineText() {
        return statusLine.getText();
    }

    /**
     *
     * @param value
     */
    public void setStatusLineText(String value) {
        Debug.out.println("FEP:setStatusLineText" + value);
        statusLine.setText(" " + value);
        statusLine.setToolTipText(value);
    }

    /*
     * enable DnD, copy/paste, New
     *
     */
    public void setDropFile(boolean enable) {
        try {
            treeView.setDropTarget(enable);
        } catch (Throwable th) {
            //ignore, may be the file drop support is not available
            Debug.log("CollabFileHandlerSupport", "FilesystemExplorerPanel, " + //NoI18n
                "setDropFile failed"
            ); //NoI18n
            Debug.logDebugException("FilesystemExplorerPanel, " + //NoI18n
                "setDropFile failed", //NoI18n	
                th, true
            );
        }
    }

    /*
     * is DnD, copy/paste, New
     *
     */
    public boolean isDropFile() {
        return treeView.isDropTarget();
    }

    /*
     * enable
     *
     */
    public void enablePanel() {
        Debug.out.println("FEP: enablePanel");

        String message = "";
        updateStatusLineText(this.fileCount, message);
        treeView.setEnabled(true);

        //hack, to hide wait dialog
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    SyncWaitPanel.hideDialog();
                }
            }
        );
    }

    /*
     * disable
     *
     */
    public void disablePanel() {
        Debug.out.println("FEP: disablePanel");

        String message = NbBundle.getMessage(FilesystemExplorerPanel.class, "LBL_FilesystemExplorerPanel_PauseMessage");
        updateStatusLineText(this.fileCount, message);
        treeView.setEnabled(false);
    }

    /*
     * createProjectNode
     *
     * @return projectNode
     */
    public Node createProjectNode(String name, String projectName)
    throws IOException {
        return getRootNode().createProjectNode(name, projectName);
    }

    public BeanTreeView getBeanTree() {
        return treeView;
    }

    /*
     * getRootNode
     *
     */
    public ProjectsRootNode getRootNode() {
        return rootNode;
    }

    public void expandAllTree() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        getBeanTree().expandAll();
                    } catch (Throwable th) {
                        //ignore
                    }
                }
            }
        );
    }

    public void expandTreeNode(String path) {
        expandTreeNode(null, path);
    }

    public void expandTreeNode(Node destNode, final String path) {
        expandTreeNode = destNode;
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        if (expandTreeNode == null) {
                            expandTreeNode = getRootNode();
                        }

                        if ((path == null) || (path.length() == 0)) {
                            getBeanTree().expandNode(expandTreeNode);

                            return;
                        }

                        String[] paths = path.split(FILE_SEPERATOR);

                        for (int j = 0; j < paths.length; j++) {
                            Debug.out.println("paths: " + paths[j]);

                            if ((expandTreeNode != null) && (expandTreeNode.getChildren() != null)) {
                                expandTreeNode = expandTreeNode.getChildren().findChild(paths[j]);
                            } else {
                                Debug.out.println("expandTreeNode children null: " + paths[j]);
                            }
                        }

                        if (expandTreeNode != null) {
                            getBeanTree().expandNode(expandTreeNode);
                        }
                    } catch (Throwable th) {
                        //ignore
                    }
                }
            }
        );
    }

}
