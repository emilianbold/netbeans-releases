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
package org.netbeans.modules.versioning.ui.history;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.versioning.ui.history.LocalHistoryRootNode.WaitNode;
import org.netbeans.modules.versioning.ui.history.RevisionNode.Filter;
import org.netbeans.modules.versioning.ui.history.RevisionNode.MessageProperty;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEvent;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryFileView implements PreferenceChangeListener, VCSHistoryProvider.HistoryChangeListener {
           
    private FileTablePanel tablePanel;             
    private File[] files;

    private RequestProcessor rp = new RequestProcessor("LocalHistoryView", 1, true);    
    private final HistoryTopComponent tc; 
    private Filter filter;
    private Task refreshTask;
    private Task vcsTask;
    private final VersioningSystem versioningSystem;
    private final VersioningSystem lh;
    
    private Date currentDateFrom; 
    private LoadNextAction loadNextAction;
    
    public LocalHistoryFileView(File[] files, VersioningSystem versioningSystem, HistoryTopComponent tc) {                       
        this.tc = tc;
        this.files = files;
        this.versioningSystem = versioningSystem;
        this.lh = History.getInstance().getLocalHistory(files);
        
        registerHistoryListener(versioningSystem, this);
        registerHistoryListener(lh, this);
         
        tablePanel = new FileTablePanel();

        HistorySettings.getInstance().addPreferenceListener(this);

        loadNextAction = new LoadNextAction();
    }
    
    public void refresh() {
        refreshTablePanel(null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if(HistorySettings.PROP_INCREMENTS.equals(evt.getKey()) ||
           HistorySettings.PROP_LOAD_ALL.equals(evt.getKey())) 
        {
            LocalHistoryRootNode rootNode = getRootNode();
            if(rootNode != null) {
                loadNextAction.refreshName();
            }    
            // XXX invoke either for all or increment value
        }
    }
    
    private LocalHistoryRootNode getRootNode() {
        Node rootContext = tablePanel.getExplorerManager().getRootContext();
        if(rootContext == null || !(rootContext instanceof LocalHistoryRootNode)) {
            return null;
        }  
        return (LocalHistoryRootNode) rootContext;
    }
    
    public ExplorerManager getExplorerManager() {
        return tablePanel.getExplorerManager();
    }
    
    public JPanel getPanel() {
        return tablePanel;
    }
    
    public void close() {
        unregisterHistoryListener(versioningSystem, this);
        unregisterHistoryListener(lh, this);
    }    
    
    private synchronized void refreshTablePanel(VCSHistoryProvider providerToRefresh) {                  
        if(refreshTask != null) {
            refreshTask.cancel();
            if(vcsTask != null) {
                vcsTask.cancel();
                vcsTask = null;
            }
        }
        refreshTask = rp.create(new RefreshTable(providerToRefresh));                    
        refreshTask.schedule(100);
    }

    void requestActive() {
        if(getRootNode() == null) {
            // invoked for the first time -> refresh
            History.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    refresh();
                    tablePanel.requestActivate();
                }
            });
        } else {
            tablePanel.requestActivate();
        }
    }

    File[] getFiles() {
        return files;
    }
                     
    void setFilter(RevisionNode.Filter filter) {
        this.filter = filter;
        tablePanel.treeView.getOutline().setQuickFilter(0, filter);
    }
    
    void fireFilterChanged() {
        tablePanel.treeView.getOutline().setQuickFilter(0, filter);
    }
    
    // XXX serialize vcs calls
    // XXX on deserialization do not invoke for every opened TC, but only the activated one
    private void loadVCSEntries(final File[] files, final boolean forceLoadAll) {
        if(versioningSystem == null) {
            return;
        }
        vcsTask = rp.post(new Runnable() {
            @Override
            public void run() {                        
                VCSHistoryProvider hp = versioningSystem.getVCSHistoryProvider();
                if(hp == null) {
                    return;
                }
                LocalHistoryRootNode rootNode = getRootNode();
                if(rootNode == null) {
                    return;
                }    
                try {
                    rootNode.loadingStarted();

                    VCSHistoryProvider.HistoryEntry[] vcsHistory;
                    if(forceLoadAll || HistorySettings.getInstance().getLoadAll()) {
                        vcsHistory = hp.getHistory(files, (Date) null); // get all
                        // XXX need different text for "Showing Subversion revisions..."
                    } else {
                        int increment = HistorySettings.getInstance().getIncrements();
                        if(currentDateFrom == null) {
                            currentDateFrom = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * (long) increment); // last X days
                        } else {
                            currentDateFrom = new Date(currentDateFrom.getTime() - 1000 * 60 * 60 * 24 * (long) increment); // last X days
                        }                
                        vcsHistory = hp.getHistory(files, currentDateFrom); // get all
                    }

                    if(vcsHistory == null || vcsHistory.length == 0) {
                        return;
                    }                
                    List<HistoryEntry> entries = new ArrayList<HistoryEntry>(vcsHistory.length);
                    for (VCSHistoryProvider.HistoryEntry he : vcsHistory) {
                        entries.add(new HistoryEntry(he, false));
                    }
                    rootNode.addVCSEntries(entries.toArray(new HistoryEntry[entries.size()]));
                } finally {
                    rootNode.loadingFinished(currentDateFrom);
                    // XXX yet select the first node on
                }
            }
        });
    }

    private void restoreSelection(final LocalHistoryRootNode root, Node[] oldSelection) {
        tablePanel.getExplorerManager().setRootContext(root);
        if(root.getChildren().getNodesCount() > 0) {                
                if (oldSelection != null && oldSelection.length > 0) {                        
                    Node[] newSelection = getEqualNodes(root, oldSelection);                        
                    if(newSelection.length > 0) {
                    setSelection(newSelection);
                    } else {
                        Node[] newExploredContext = getEqualNodes(root, new Node[] { oldSelection[0].getParentNode() });
                        if(newExploredContext.length > 0) {
                        selectFirstNeighborNode(newExploredContext[0], oldSelection[0]);
                        }
                    }
                } else {
                    selectFirstNode(root);
                }
            } else {
            setSelection(new Node[]{});
            }   
        }
        
        private Node[] getEqualNodes(Node root, Node[] oldNodes) {    
            List<Node> ret = new ArrayList<Node>();
            for(Node on : oldNodes) {
                Node node = findEqualInChildren(root, on);
                if(node != null) {
                    ret.add(node);                                
                }
                if(root.getName().equals(on.getName())) {
                    ret.add(root);
                }
            }            
            return ret.toArray(new Node[ret.size()]);                            
        }                   
        
        private Node findEqualInChildren(Node node, Node toFind) {
            Node[] children = node.getChildren().getNodes();
            for(Node child : children) {
                if(toFind.getName().equals(child.getName())) {
                    return child;                
                }
                Node n = findEqualInChildren(child, toFind);
                if(n != null) {
                    return n;
                }                 
            }
            return null;
        } 

        private void selectFirstNode(final Node root) {        
            Node[] dateFolders = root.getChildren().getNodes();
            if (dateFolders != null && dateFolders.length > 0) {
                final Node[] nodes = dateFolders[0].getChildren().getNodes();
                if (nodes != null && nodes.length > 0) {                
                setSelection(new Node[]{ nodes[0] });
                }
            }        
        }

    private void selectFirstNeighborNode(Node context, Node oldSelection) {            
            Node[] children = context.getChildren().getNodes();
            if(children.length > 0 && children[0] instanceof Comparable) {
                Node[] newSelection = new Node[] { children[0] } ;
                for(int i = 1; i < children.length; i++) {
                    Comparable c = (Comparable) children[i];
                    if( c.compareTo(oldSelection) < 0 ) {
                       newSelection[0] = children[i]; 
                    }                                            
                }
            setSelection(newSelection);
                tablePanel.getExplorerManager().setExploredContext(context);
            }        
        }   
        
    private void setSelection(final Node[] nodes) {
            SwingUtilities.invokeLater(new Runnable() {
            @Override
                public void run() {
                    try {
                        tablePanel.getExplorerManager().setSelectedNodes(nodes);
                    } catch (PropertyVetoException ex) {
                        // ignore
                    }
                }
            });                                             
        }

    @Override
    public void fireHistoryChanged(HistoryEvent evt) {
        Set<File> fileSet = new HashSet<File>();
        for (File f : evt.getFiles()) {
            if(f != null) {
                fileSet.add(f);
            }
        }
        if(fileSet.isEmpty()) {
            return;
        }
        for (File file : files) {
            if(fileSet.contains(file)) {
                refreshTablePanel(evt.getSource());
            }
        }
    }

    private static void registerHistoryListener(VersioningSystem versioningSystem, VCSHistoryProvider.HistoryChangeListener l) {
        VCSHistoryProvider hp = getHistoryProvider(versioningSystem);
        if(hp != null) {
            hp.addHistoryChangeListener(l);
        }
    }

    private static void unregisterHistoryListener(VersioningSystem versioningSystem, VCSHistoryProvider.HistoryChangeListener l) {
        VCSHistoryProvider hp = getHistoryProvider(versioningSystem);
        if(hp != null) {
            hp.addHistoryChangeListener(l);
        }
    }
    
    private static VCSHistoryProvider getHistoryProvider(VersioningSystem versioningSystem) {
        if(versioningSystem == null) {
            return null;
        }
        return  versioningSystem.getVCSHistoryProvider();
    }
    
    /**
     * Selects a node with the timestamp = toSelect, otherwise the selection stays.
     * If there wasn't a selection set yet then the first node will be selected.
     */ 
    private class RefreshTable implements Runnable {
        private final VCSHistoryProvider providerToRefresh;

        RefreshTable(VCSHistoryProvider providerToRefresh) {
            this.providerToRefresh = providerToRefresh;
        }
        
        @Override
        public void run() {  
            LocalHistoryRootNode root = getRootNode();
            if(root == null) {
                final String vcsName = (String) (versioningSystem != null ? 
                                                    versioningSystem.getProperty(VersioningSystem.PROP_DISPLAY_NAME) :
                                                    null);
                root = new LocalHistoryRootNode(files, vcsName, loadNextAction, createActions()); 
                tablePanel.getExplorerManager().setRootContext(root);
            }
            
            // refresh local history
            VCSHistoryProvider lhProvider = getHistoryProvider(History.getInstance().getLocalHistory(files));
            if(lhProvider != null && (providerToRefresh == null || lhProvider == providerToRefresh)) {
                root.addLHEntries(loadLHEntries(files));
            }
            // refresh vcs
            VCSHistoryProvider vcsProvider = getHistoryProvider(versioningSystem);
            if(tc != null && vcsProvider != null && (providerToRefresh == null || providerToRefresh == vcsProvider)) {
                loadVCSEntries(files, false);
            }
            tablePanel.revalidate();
            tablePanel.repaint();
        }
    } 
    
    private HistoryEntry[] loadLHEntries(File[] files) {
        VersioningSystem lh = History.getInstance().getLocalHistory(files);
        if(lh == null) {
            return new HistoryEntry[0];
        }
        VCSHistoryProvider hp = lh.getVCSHistoryProvider();
        if(hp == null) {
            return new HistoryEntry[0];
        }
        VCSHistoryProvider.HistoryEntry[] vcsHistory = hp.getHistory(files, null);
        HistoryEntry[] history = new HistoryEntry[vcsHistory.length];
        for (int i = 0; i < vcsHistory.length; i++) {
            history[i] = new HistoryEntry(vcsHistory[i], true);
        }
        return history;
    }

    private Action[] createActions() {
        List<Action> actions = new LinkedList<Action>();
        actions.add(loadNextAction); 
        actions.add(new AbstractAction("Load All") { // XXX name
            @Override
            public void actionPerformed(ActionEvent e) {
                loadVCSEntries(files, true);
            }
        });
        actions.add(null); 
        actions.add(new AbstractAction("Always Load All") { // XXX name
            @Override
            public void actionPerformed(ActionEvent e) {
                HistorySettings.getInstance().setLoadAll(true);
                loadVCSEntries(files, true);
            }
        });
        return actions.toArray(new Action[actions.size()]);
    }
    private class FileTablePanel extends JPanel implements ExplorerManager.Provider, TreeExpansionListener {
        private final BrowserTreeTableView treeView;    
        private final ExplorerManager manager;

        public FileTablePanel() { 
            manager = new ExplorerManager();

            setLayout(new GridBagLayout());

            treeView = new BrowserTreeTableView();             
            treeView.addTreeExpansionListener(this);
            setLayout(new BorderLayout());
            add(treeView, BorderLayout.CENTER);
        }   

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        void requestActivate() {
            treeView.requestFocusInWindow();
        }

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            Object obj = event.getPath().getLastPathComponent();
            if(obj == null) return;
            Node n = Visualizer.findNode(obj);
            if(LocalHistoryRootNode.isLoadNext(n)) { // XXX move to lhrootnode
                loadNextAction.actionPerformed(null);
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            // do nothing
        }
        
        private class BrowserTreeTableView extends OutlineView {    
            BrowserTreeTableView() {
                super( NbBundle.getMessage(LocalHistoryFileView.class, "LBL_LocalHistory_Column_Date")); //NOI18N
                setupColumns();

                getOutline().setShowHorizontalLines(true);
                getOutline().setShowVerticalLines(false);
                getOutline().setRootVisible(false);                    
//                getOutline().setGridColor(Color.white);
                
                setBorder(BorderFactory.createEtchedBorder());
                
        //        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        //        treeView.getAccessibleContext().setAccessibleName(browserAcsn);           
                getOutline().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);            
                setPopupAllowed(true);    
                setDragSource(false);
                setDropTarget(false);
                getOutline().setColumnHidingAllowed(false);
                getOutline().setRenderDataProvider( new NoLeafIconRenderDataProvider( getOutline().getRenderDataProvider() ) );
                getOutline().setDefaultRenderer(Node.Property.class, new PropertyRenderer(getOutline()));
                
                // XXX do we need this
                final ETableColumn column = (ETableColumn) getOutline().getColumnModel().getColumn(0);
                column.setNestedComparator(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        if(LocalHistoryRootNode.isLoadNext(o1)) {
                            return column.isAscending() ? 1 : -1;
                        } else if(LocalHistoryRootNode.isLoadNext(o2)) {
                            return column.isAscending() ? -1 : 1;
            }

                        if(o1 instanceof RevisionNode && o2 instanceof RevisionNode) {
                            return ((RevisionNode)o1).compareTo(o2);
                        }
                        return 0; // XXX
                    }
                });
                
                TableMouseListener l = new TableMouseListener(getOutline());
                getOutline().addMouseMotionListener(l);
                getOutline().addMouseListener(l);
                
            }
            
            @Override
            public void addNotify() {
                super.addNotify();
                setDefaultColumnSizes();
            }

            private void setupColumns() {
                ResourceBundle loc = NbBundle.getBundle(FileTablePanel.class);            

                Node.Property [] columns; 
                int idx = 0;
                if(versioningSystem != null) {
                    columns = new Node.Property[3];
                    columns[idx++] = new ColumnDescriptor<String>(
                                    RevisionNode.PROPERTY_NAME_VERSION, 
                                    String.class, 
                                    loc.getString("LBL_LocalHistory_Column_Version"),          // NOI18N            
                                    loc.getString("LBL_LocalHistory_Column_Version_Desc"));    // NOI18N            
                    columns[idx++] = new ColumnDescriptor<String>(
                                    RevisionNode.PROPERTY_NAME_USER, 
                                    String.class, 
                                    loc.getString("LBL_LocalHistory_Column_User"),             // NOI18N            
                                    loc.getString("LBL_LocalHistory_Column_User_Desc"));       // NOI18N            
                } else {
                columns = new Node.Property[1];
                }
                columns[idx] = new ColumnDescriptor<String>(
                                RevisionNode.PROPERTY_NAME_LABEL, 
                                String.class, 
                                loc.getString("LBL_LocalHistory_Column_Label"),            // NOI18N            
                                loc.getString("LBL_LocalHistory_Column_Label_Desc"));      // NOI18N            
                setProperties(columns);
            }    

            private void setDefaultColumnSizes() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int width = getWidth();                    
                            getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 20 / 100);
                            if(versioningSystem != null) {
                                getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 10 / 100);                        
                                getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);                        
                                getOutline().getColumnModel().getColumn(3).setPreferredWidth(width * 60 / 100);                        
                            } else {
                                getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 80 / 100);                        
                    }
                    }
                });
            }            
    
            private class NoLeafIconRenderDataProvider implements RenderDataProvider {
                private RenderDataProvider delegate;
                public NoLeafIconRenderDataProvider( RenderDataProvider delegate ) {
                    this.delegate = delegate;
                }

                @Override
                public String getDisplayName(Object o) {
                    Node n = Visualizer.findNode(o);
                    if(LocalHistoryRootNode.isLoadNext(n)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("<html><font color=#0000FF>");
                        sb.append(delegate.getDisplayName(o));
                        sb.append("</font></html>");
                        return sb.toString();
                    }
                    return delegate.getDisplayName(o);
                }

                @Override
                public boolean isHtmlDisplayName(Object o) {
                    Node n = Visualizer.findNode(o);
                    if(LocalHistoryRootNode.isLoadNext(n)) {
                        return true;
                    }
                    return delegate.isHtmlDisplayName(o);
                }

                @Override
                public Color getBackground(Object o) {
                    Color b = delegate.getBackground(o);
                    return b;
                }

                @Override
                public Color getForeground(Object o) {
                    return delegate.getForeground(o);
                }

                @Override
                public String getTooltipText(Object o) {
                    return delegate.getTooltipText(o);
                }

                @Override
                public Icon getIcon(Object o) {
                    final Node n = Visualizer.findNode(o);
                    if(n instanceof WaitNode) {
                        return delegate.getIcon(o);
                    }
                    if(getOutline().getOutlineModel().isLeaf(o) || LocalHistoryRootNode.isLoadNext(n))
                        return NO_ICON;
                    return null;
                }

            }
        }     
    }    
    
    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {        
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }      
    
    private static final Icon NO_ICON = new NoIcon();
    private static class NoIcon implements Icon {
        public void paintIcon(Component c, Graphics g, int x, int y) { }
        public int getIconWidth() { return 0; }
        public int getIconHeight() { return 0; }
    }

    private boolean containsHyperlink(String message) throws IllegalAccessException {
        int[] spans = getHyperlinkSpans(message);
        return spans != null && spans.length >= 2;
    }
    private int[] getHyperlinkSpans(String message) throws IllegalAccessException {
        List<VCSHyperlinkProvider> providers = History.getInstance().getHyperlinkProviders();
        for (VCSHyperlinkProvider hp : providers) {
            int[] spans = hp.getSpans(message);
            if (spans != null && spans.length >= 2) {
                return spans;
            }
        }
        return null;
    }
            
    private class PropertyRenderer extends DefaultOutlineCellRenderer {
        private final Outline outline;
        private static final int VISIBLE_START_CHARS = 0;
        public PropertyRenderer(Outline outline) {
            this.outline = outline;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            if((value instanceof Node.Property)) {
                try {
                    String valueString = getDisplayValue((Node.Property) value);
                    valueString = computeFitText(table, row, column, valueString);
                    valueString = escapeForHTMLLabel(valueString); // XXX might clash with rendered filter matches
                    
                    Filter f = tc != null ? tc.getSelectedFilter() : null;
                    if(f != null) {
                        valueString = f.getRendererValue(valueString);
                    }
                    if(value instanceof RevisionNode.MessageProperty) {
                        String[] lines = valueString.split("\n");
                        if(lines.length > 0) {
                            int[] spans = getHyperlinkSpans(lines[0]);
                            if (spans != null && spans.length >= 2) {
                                StringBuilder sb = new StringBuilder();
                                String line = addHyperlink(escapeForHTMLLabel(lines[0]), spans);
                                sb.append(line);
                                for (int i = 1; i < lines.length; i++) {
                                    sb.append("\n");
                                    sb.append(escapeForHTMLLabel(lines[i]));
                                }
                                valueString = sb.toString();
                            }
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append(valueString);
                    sb.append("</html>");
                    valueString = sb.toString();
                    
                    renderer = super.getTableCellRendererComponent(table, valueString, isSelected, hasFocus, row, column);
                    if(renderer instanceof JComponent) {
                        JComponent comp = (JComponent)renderer;
                        comp.setToolTipText(getTooltip((Node.Property) value));
                    }
                } catch (Exception ex) {
                    History.LOG.log(Level.WARNING, null, ex);
                    renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            renderer.setBackground (isSelected ? outline.getSelectionBackground() : outline.getBackground());
            return renderer;
        }
        
        String getDisplayValue(Node.Property p) throws IllegalAccessException, InvocationTargetException {
            String value = (String) p.getValue();
            return value != null ? value : "";
        }
        
        String getTooltip(Node.Property p) throws IllegalAccessException, InvocationTargetException {
            String tooltip = p.toString();
            if(tooltip.contains("\n")) {
                tooltip = escapeForHTMLLabel(tooltip);
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                StringTokenizer st = new StringTokenizer(tooltip, "\n");
                while(true) {
                    sb.append(st.nextToken());
                    if(st.hasMoreTokens()) {
                        sb.append("<br>");
                    } else {
                        break;
                    }
                }
                sb.append("</html>");
                tooltip = sb.toString();
            } 
            return tooltip;
            
        }

        private String addHyperlink(String s, int[] spans) {
            assert spans.length % 2 == 0;
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < spans.length) {
                int start = spans[i++];
                if(i == 1) {
                    sb.append(s.substring(0, start));
                    sb.append("<font color=#0000FF><u>");
                }
                int end = spans[i++];
                sb.append(s.substring(start, end));
                if(i == spans.length) {
                    sb.append("</u></font>");
                    sb.append(s.substring(end, s.length()));
                }
            }
            return sb.toString();
        }
        
        private String computeFitText(JTable table, int rowIdx, int columnIdx, String text) {
            if(text == null) text = "";
            if (text.length() <= VISIBLE_START_CHARS + 3) return text;

//            Icon icon = label.getIcon();
//            int iconWidth = icon != null ? icon.getIconWidth() : 0;

            FontMetrics fm = table.getFontMetrics(table.getFont());
            TableColumn column = table.getColumnModel().getColumn(columnIdx);
            int width = table.getCellRect(rowIdx, columnIdx, false).width;
            //            int width = label.getSize().width; // - iconWidth;

            String sufix = "...";                                                   // NOI18N
            int sufixLength = fm.stringWidth(sufix + " ");
            int desired = width - sufixLength;
            if (desired <= 0) return text;

            for (int i = 0; i <= text.length() - 1; i++) {
                String prefix = text.substring(0, i);
                int swidth = fm.stringWidth(prefix);
                if (swidth >= desired) {
                    return prefix.length() > 0 ? prefix + sufix: text;
                }
            }
            return text;
        }    
        
        public String escapeForHTMLLabel(String text) {
            if(text == null) {
                return "";                              // NOI18N
            }
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<text.length(); i++) {
                char c = text.charAt(i);
                switch (c) {
                    case '<': sb.append("&lt;"); break; // NOI18N
                    case '>': sb.append("&gt;"); break; // NOI18N
                    default: sb.append(c);
                }
            }
            return sb.toString();
        }
    }
    
    private class TableMouseListener implements MouseListener, MouseMotionListener {
        private final Outline outline;
        private boolean pressedPopup = false;
        public TableMouseListener(Outline outline) {
            this.outline = outline;
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {}

        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                Object value = getValue(e);
                if(value instanceof MessageProperty) {
                    MessageProperty msg = (MessageProperty) value;
                    if(msg == null || !containsHyperlink(msg.getValue())) {
                        outline.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    } else {
                        outline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            } catch (Exception ex) {
                History.LOG.log(Level.WARNING, null, ex);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath path = outline.getLayoutCache().getPathForRow(outline.rowAtPoint(e.getPoint()));
            if(path == null) {
                return;
            }
            Node n = Visualizer.findNode(path.getPathComponent(1)); // XXX idx 1
            if(n == null) {
                return;
            }
            if(!pressedPopup && LocalHistoryRootNode.isLoadNext(Visualizer.findNode(n))) {
                loadNextAction.actionPerformed(null);
            } else {
                Object value = getValue(e);
                if(!(value instanceof MessageProperty)) {
                    return;
                }
                MessageProperty messageProperty = (MessageProperty) value;
                if(messageProperty == null) {
                    return;
                }
                try {
                    outline.getSelectedRow();

                    HistoryEntry entry = n.getLookup().lookup(HistoryEntry.class);
                    if(entry == null) {
                        return;
                    }
                    String message = entry.getMessage();
                    String author = entry.getUsername();
                    String revision = entry.getRevision();
                    Date date = entry.getDateTime();
                    MsgTooltipWindow ttw = new MsgTooltipWindow(outline, files[0], message, revision, author, date);
                    Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, outline);
                    ttw.show(new Point(p.x, p.y));
                } catch (Exception ex) {
                    History.LOG.log(Level.WARNING, null, ex);
                } 
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            pressedPopup = e.isPopupTrigger();
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
                    
        private Object getValue(MouseEvent e) {
            int r = outline.rowAtPoint(e.getPoint());
            int c = outline.columnAtPoint(e.getPoint());
            if(r == -1 || c == -1) {
                return null;
            }
            return outline.getValueAt(r, c);
        }
    }
    
    private class LoadNextAction extends AbstractAction {
        public LoadNextAction() {
            refreshName();
        }
        private void refreshName() {
            String name;
            if(HistorySettings.getInstance().getLoadAll()) {
                name = NbBundle.getMessage(LocalHistoryRootNode.class,  "LBL_LoadAll"); // NOI18N
            } else {
                name = NbBundle.getMessage(LocalHistoryRootNode.class,  "LBL_LoadNext", HistorySettings.getInstance().getIncrements()); // NOI18N
            }
            putValue(Action.NAME, name);
            LocalHistoryRootNode rootNode = getRootNode();
            if(rootNode != null) {
                rootNode.refreshLoadNextName();
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            loadVCSEntries(files, false); 
        }
    }
}
