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
package org.netbeans.dlight.visualizers;

import org.netbeans.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.support.TreeTableNode;
import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.dlight.visualizers.api.impl.TreeTableVisualizerConfigurationAccessor;
import org.netbeans.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.netbeans.modules.dlight.spi.dataprovider.support.TreeTableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.dlight.visualizers.ImageLoader;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author mt154047
 */
public class TreeTableVisualizer<T extends TreeTableNode> extends JTable implements Visualizer<TreeTableVisualizerConfiguration>, OnTimerTask {

  //public static final String IS_CALLS = "TopTenFunctionsIsCalls"; // NOI18N
  private JToolBar buttonsToolbar;
  private JButton refresh;
  private TreeTableVisualizerConfiguration configuration;
  private DefaultTreeModel treeModel = null;
  protected final DefaultMutableTreeNode TREE_ROOT = new DefaultMutableTreeNode("ROOT");
  private JPanel mainPanel = null;
  private TreeModelImpl treeModelImpl;
  private TableModelImpl tableModelImpl;
  private Models.CompoundModel compoundModel;
  protected JComponent treeTableView;
  private TreeTableDataProvider<T> dataProvider;
  private OnTimerRefreshVisualizerHandler timerHandler;


  public TreeTableVisualizer() {
  }
  

  protected TreeTableVisualizer( TreeTableVisualizerConfiguration configuration, DataProvider dataProvider) {
    timerHandler = new OnTimerRefreshVisualizerHandler(this, 5);
    this.configuration = configuration;
    this.dataProvider = (TreeTableDataProvider<T>)dataProvider;
//    init(c);
//    linkWith(dataView);
//    isCalls = NbPreferences.forModule(CallersCalleesVisualizer.class).getBoolean(IS_CALLS, true);
    treeModel = new DefaultTreeModel(TREE_ROOT);
//    metricsList = dataProvider.getMetricsList();
    initComponents();
    updateButtons();
  }

  @Override
  public void addNotify() {
    super.addNotify();

    if (timerHandler.isSessionRunning()) {
      timerHandler.startTimer();
      return;
    }

    if (timerHandler.isSessionAnalyzed() ||
            timerHandler.isSessionPaused()) {
      onTimer();
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    timerHandler.stopTimer();
  }

  protected DefaultMutableTreeNode getTreeRoot() {
    return TREE_ROOT;
  }

  /**
   * Fire treeModelChanged event in AWT Thread
   */
  protected void fireTreeModelChanged() {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {

        treeModelImpl.fireTreeModelChanged();
      }
    });


  }

  /**
   * Fire treeModelChanged event in AWT Thread
   */
  protected void fireTreeModelChanged(final DefaultMutableTreeNode node) {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        treeModelImpl.fireTreeModelChanged(node);
      }
    });
  }

  protected void updateButtons() {
  }

  protected JToolBar getButtonsTolbar() {
    return buttonsToolbar;
  }

  protected void initComponents() {
    setLayout(new BorderLayout());
    buttonsToolbar = new JToolBar();
    refresh = new JButton();

    buttonsToolbar.setFloatable(false);
    buttonsToolbar.setOrientation(1);
    buttonsToolbar.setRollover(true);

    // Refresh button...
    refresh.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
//    refresh.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "RefreshActionTooltip")); // NOI18N
    refresh.setFocusable(false);
    refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    refresh.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fillModel(configuration.getMetadata().getColumns());
      }
    });

    buttonsToolbar.add(refresh);

    add(buttonsToolbar, BorderLayout.LINE_START);

    mainPanel = new JPanel();
    mainPanel.removeAll();
    add(mainPanel, BorderLayout.CENTER);

    List<ColumnModel> columns = new ArrayList<ColumnModel>();
    Column[] tableColumns = TreeTableVisualizerConfigurationAccessor.getDefault().getTableColumns(configuration);
    for (final Column f : tableColumns) {
      //      final int col = i;
      ColumnModel column = new ColumnModel() {

        boolean isVisible = true;
        boolean isSorted = false;
        boolean isSortedDescending = false;
        int currentOrderNumber = -1;

        public String getID() {
          return f.getColumnName();
        }

        public String getDisplayName() {
          return f.getColumnUName();
        }

        public Class getType() {
          return f.getColumnClass();
        }

        @Override
        public void setCurrentOrderNumber(int newOrderNumber) {
          this.currentOrderNumber = newOrderNumber;
        }

        @Override
        public int getCurrentOrderNumber() {
          return currentOrderNumber;
        }

        @Override
        public void setVisible(boolean arg0) {
          this.isVisible = arg0;
        }

        @Override
        public boolean isVisible() {
          return isVisible;
        }

        @Override
        public boolean isSortable() {
          return true;
        }

        @Override
        public boolean isSorted() {
          return isSorted;
        }

        @Override
        public void setSorted(boolean sorted) {
          this.isSorted = sorted;
        }

        @Override
        public void setSortedDescending(boolean sortedDescending) {
          this.isSortedDescending = sortedDescending;
        }

        @Override
        public boolean isSortedDescending() {
          return isSortedDescending;
        }
      };

      columns.add(column);
    }

    List<Model> models = new ArrayList<Model>();
    treeModelImpl = new TreeModelImpl();

    models.add(treeModelImpl);//tree model
    tableModelImpl = new TableModelImpl();
    models.add(tableModelImpl);
    models.addAll(columns);
    models.add(new NodeModelImpl());
//    models.add(new NodeActionsProviderImpl());
    compoundModel = Models.createCompoundModel(models);
    treeTableView = Models.createView(compoundModel);
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(treeTableView, BorderLayout.CENTER);

    treeModelImpl.fireTreeModelChanged();
    //we should find JTable and set new Renderer
    //tableModelImpl.fireTableValueChanged();
    mainPanel.repaint();
    repaint();
    revalidate();
  }

  protected void fillModel(final List<Column> columns) {

    RequestProcessor.getDefault().post(new Runnable() {

      public void run() {
        List<T> list = dataProvider.getTableView(columns, null, Integer.MAX_VALUE);
        updateList(list);
      }
    });

  }

  protected void updateList(List<T> list) {
    TREE_ROOT.removeAllChildren();

    UIThread.invoke(new Runnable() {

      public void run() {
        treeModelImpl.fireTreeModelChanged();
      }
    });

    if (list != null) {
      for (T value : list) {
        TREE_ROOT.add(new DefaultMutableTreeNode(value));
      }
    }

    UIThread.invoke(new Runnable() {

      public void run() {
        treeModelImpl.fireTreeModelChanged();
      }
    });

  }

  protected void loadTree(final DefaultMutableTreeNode rootNode, final List<T> path) {
    //we should show Loading Node
    //this.functionsCallTreeModel.get
    //go away from AWT Thread
    RequestProcessor.getDefault().post(new Runnable() {

      public void run() {

        List<T> result = null;
//        if (CallersCalleesVisualizer.this.isCalls) {
//          result = dataProvider.getChildren(path, isCalls);
//        } else {
//          result = dataProvider.getCallers(path, false);
//        }
        result = dataProvider.getChildren(path);
        updateTree(rootNode, result);
      }
    });
  }

  protected void updateTree(final DefaultMutableTreeNode rootNode, List<T> result) {
    //add them all as a children to rootNode
    rootNode.removeAllChildren();
    if (result != null) {
      for (T value : result) {
        rootNode.add(new DefaultMutableTreeNode(value));
      }
    }

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        treeModelImpl.fireTreeModelChanged(rootNode);
      }
    });
  }

  public int onTimer() {
    //we have sync call here, want async
    updateList(dataProvider.getTableView(null, null, Integer.MAX_VALUE));
    return 0;
  }

  public VisualizerContainer getDefaultContainer() {
    return VisualizerTopComponentTopComponent.findInstance();
  }

  public TreeTableVisualizerConfiguration getVisualizerConfiguration() {
    return configuration;
  }

  public Visualizer create(TreeTableVisualizerConfiguration configuration, DataProvider provider) {
    return new TreeTableVisualizer(configuration, dataProvider);
  }

  public String getID() {
    return VisualizerConfigurationIDsProvider.TREE_TABLE_VISUALIZER;
  }

  public JComponent getComponent() {
    return this;
  }

  protected class TreeModelImpl implements TreeModel, TreeExpansionModel {

    private final Object listenersLock = new Object();
    private Vector<ModelListener> listeners = new Vector<ModelListener>();

    public Object getRoot() {
      return ROOT;
    }

    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
      //throw new UnsupportedOperationException("Not supported yet.");
//      if (parent == ROOT) {
      Object real_parent = parent;
      if (parent == ROOT) {
        real_parent = TREE_ROOT;
      }
      //return functionsList.getChildren(null).toArray();
//        return functionsCallTreeModel.get`
      if (real_parent instanceof DefaultMutableTreeNode) {
        List<Object> result = new ArrayList<Object>();
        for (int i = from; i <= to; i++) {
          if (i >= 0 && i < treeModel.getChildCount(real_parent)) {
            result.add(treeModel.getChild(real_parent, i));
          }
        }
        return result.toArray();
      }

      throw new UnknownTypeException(parent);
    }

    void fireTreeModelChanged(DefaultMutableTreeNode node) {
      synchronized (listenersLock) {
        for (ModelListener l : listeners) {
          l.modelChanged(new ModelEvent.NodeChanged(TreeTableVisualizer.this, node));
        }
      }
    }

    void fireTreeModelChanged() {
      synchronized (listenersLock) {
        for (ModelListener l : listeners) {
          l.modelChanged(new ModelEvent.TreeChanged(TreeTableVisualizer.this));
          l.modelChanged(new ModelEvent.NodeChanged(TreeTableVisualizer.this, ROOT));
        }
      }


    }

    public boolean isLeaf(Object node) {
      if (node == ROOT) {
        return false;
      }
      if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(configuration)) {
        return true;
      }
      return timerHandler.isSessionRunning();
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
      Object real_node = node;
      if (node == ROOT) {
        real_node = TREE_ROOT;
        return treeModel.getChildCount(real_node);
      }

      if (real_node instanceof DefaultMutableTreeNode) {
        if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(configuration)) {
          return 0;
        }
        return 1;
      }
      return 0;
    }

    public void addModelListener(ModelListener l) {
      synchronized (listenersLock) {
        listeners.add(l);
      }
    //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeModelListener(ModelListener l) {
      synchronized (listenersLock) {
        listeners.remove(l);
      }
    }

    public boolean isExpanded(Object node) throws UnknownTypeException {
      //throw new UnsupportedOperationException("Not supported yet.");
      return false;
    }

    public void nodeExpanded(Object node) {
      if (node == ROOT) {
        return;
      }

      if (!(node instanceof DefaultMutableTreeNode)) {
        return;
      }

      DefaultMutableTreeNode tNode = (DefaultMutableTreeNode) node;
      List<T> result = Arrays.asList((T) tNode.getUserObject());
      loadTree(tNode, result);
    }

    public void nodeCollapsed(Object node) {
      //System.out.println("nodeCollapsed invoked " + node);
    }
  }

  class TableModelImpl implements TableModel {

    private Vector<ModelListener> listeners = new Vector<ModelListener>();

    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
      if (!(node instanceof DefaultMutableTreeNode)) {
        throw new UnknownTypeException(node);
      }
//      if ("iconID".equals(columnID)) {
//        return new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/dlight/resources/who_calls.png"));
//      }

      DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;
      Object nodeObject = theNode.getUserObject();

      if (nodeObject instanceof TreeTableNode) {
        //return ((T)nodeObject).getMetricValue(getMetricByID(columnID));
        return ((TreeTableNode) nodeObject).getValue(columnID);
      }

      return "";
    }

    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
      //throw new UnsupportedOperationException("Not supported yet.");
      return true;
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
      //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addModelListener(ModelListener l) {
      //throw new UnsupportedOperationException("Not supported yet.");
      listeners.add(l);
    }

    void fireTableValueChanged() {
    }

    public void removeModelListener(ModelListener l) {
      listeners.remove(l);
    //throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  class NodeModelImpl implements ExtendedNodeModel {

    public String getDisplayName(Object node) {
      if (node == TreeModel.ROOT) {
        return TreeTableVisualizerConfigurationAccessor.getDefault().getTreeColumn(configuration).getColumnUName();
      }
      if (node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
        Object nodeObject = treeNode.getUserObject();
        return (nodeObject instanceof TreeTableNode) ? ((TreeTableNode) nodeObject).getValue() + " " : nodeObject.toString();
      }
      return "Unknown";
    }

    public String getIconBase(Object node) {
      return null;
    }

    public String getShortDescription(Object node) {
      if (node == TreeModel.ROOT) {
        return TreeTableVisualizerConfigurationAccessor.getDefault().getTreeColumn(configuration).getColumnUName();
      }
      if (node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
        return ((TreeTableNode) treeNode.getUserObject()).getValue() + "";
      }
      return "Unknown";
    }

    public void addModelListener(ModelListener arg0) {
      //    throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeModelListener(ModelListener arg0) {
      //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean canRename(Object arg0) throws UnknownTypeException {
      return false;
    }

    public boolean canCopy(Object arg0) throws UnknownTypeException {
      return false;
    }

    public boolean canCut(Object arg0) throws UnknownTypeException {
      return false;
    }

    public Transferable clipboardCopy(Object arg0) throws IOException, UnknownTypeException {
      return null;
    }

    public Transferable clipboardCut(Object arg0) throws IOException, UnknownTypeException {
      return null;
    }

    public PasteType[] getPasteTypes(Object arg0, Transferable arg1) throws UnknownTypeException {
      return null;
    }

    public void setName(Object arg0, String arg1) throws UnknownTypeException {
      //throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getIconBaseWithExtension(Object arg0) throws UnknownTypeException {
//      return CsmImageName.FUNCTION_GLOBAL;
      return null;
    }
  }
}

