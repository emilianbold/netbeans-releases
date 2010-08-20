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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.versioning.history;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.lang.Object;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.Hyperlink;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * @author Maros Sandor
 */
/**
 * Shows Search History results in a JList.
 *
 * @author Maros Sandor
 */
public abstract class AbstractSummaryView implements MouseListener, ComponentListener, MouseMotionListener {

    static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.util.AbstractSummaryView");
    
    private RootNode rootNode;
    private RequestProcessor rp = new RequestProcessor("SummaryView", 10);
    private boolean populated = false;

    private JPanel panel = new JPanel();
    private final JPanel linesPanel;
    private final Map<String, KenaiUser> kenaiUsersMap;

    String getMessage() {
        return master.getMessage();
    }

    File getRoot() {
        return master.getRoots()[0];
    }

    Map<String, String> getActionColors() {
        return master.getActionColors();
    }

    void fireNodeChanged(String revision) {
        ResultModel tm = ((ResultModel) resultsTree.getModel());
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            TreeNode n = rootNode.getChildAt(i);
            if(n instanceof LogEntryNode) {
                LogEntryNode len = (LogEntryNode)n;
                LogEntry le = (LogEntry) len.getUserObject();
                if(revision.equals(le.getRevision())) {
                    tm.fireTreeNodesChanged(len, len.getPath(), new int[] {}, new TreeNode[] {});
                    return;
                }
            }
        }
    }

    public interface SummaryViewMaster {
        public JComponent getComponent();
        public File[] getRoots();
        public String getMessage();
        public Map<String, String> getActionColors();
        public List<LogEntry> getMoreResults(List<LogEntry> results, int count);
    }

    private final SummaryViewMaster master;

    public static abstract class LogEntry {
        boolean messageExpanded = false;
        private boolean eventsExpanded = false;

        public abstract Collection<Event> getEvents();
        public abstract Collection<Event> getContextEvents();

        public abstract String getAuthor();
        public abstract String getDate();
        public abstract String getRevision();
        public abstract String getRevision2();
        public abstract String getMessage();
        public abstract Action[] getActions();

        public static abstract class Event {
            public abstract String getPath();
            public abstract String getAction();
        }
    }

    private JTree resultsTree;
    private JScrollPane scrollPane;

    private List<LogEntry> dispResults;

    private VCSHyperlinkSupport linkerSupport = new VCSHyperlinkSupport();

    public AbstractSummaryView(SummaryViewMaster master, List<LogEntry> results, Map<String, KenaiUser> kenaiUsersMap) {
        this.master = master;
        this.kenaiUsersMap = kenaiUsersMap;

        dispResults = results;
        rootNode = new RootNode(dispResults);
        resultsTree = new JTree(new DefaultTreeModel(new WaitNode(rootNode)));

        resultsTree.setLargeModel(true);
        resultsTree.setRowHeight(0); // stands for different row heights
        resultsTree.setShowsRootHandles(true);
        resultsTree.setRootVisible(false);

        resultsTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(final TreeExpansionEvent event) {
                TreePath path = event.getPath();
                Object o = path.getLastPathComponent();
                if(o instanceof LogEntryNode) {
                    LogEntryNode n = (LogEntryNode) o;
                    n.populateEvents(path);
                }
            }
            @Override
            public void treeCollapsed(TreeExpansionEvent event) { }
        });

        resultsTree.addMouseListener(this);
        resultsTree.addMouseMotionListener(this);
        resultsTree.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AbstractSummaryView.class, "ACSN_SummaryView_List")); // NOI18N
        resultsTree.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AbstractSummaryView.class, "ACSD_SummaryView_List")); // NOI18N
        scrollPane = new JScrollPane(resultsTree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsTree.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //
                populateTree();
            }
        });

        master.getComponent().addComponentListener(this);

        resultsTree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
        resultsTree.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(resultsTree));
            }
        });

        resultsTree.setSelectionModel(new SelectionModel());

        linesPanel = new JPanel();
        HyperlinkLabel label10 = new HyperlinkLabel();
        HyperlinkLabel label50 = new HyperlinkLabel();
        HyperlinkLabel label100 = new HyperlinkLabel();
        HyperlinkLabel labelAll = new HyperlinkLabel();

        label10.set("10", Color.BLUE, label10.getBackground());
        label50.set("50", Color.BLUE, label10.getBackground());
        label100.set("100", Color.BLUE, label10.getBackground());
        labelAll.set("All Revisions", Color.BLUE, label10.getBackground()); // XXX

        linesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        linesPanel.add(new JLabel("Show more..."));
        linesPanel.add(label10);
        linesPanel.add(new JLabel(","));
        linesPanel.add(label50);
        linesPanel.add(new JLabel(","));
        linesPanel.add(label100);
        linesPanel.add(new JLabel(","));
        linesPanel.add(labelAll);        

        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.PAGE_START);
        panel.add(linesPanel, BorderLayout.PAGE_END);
        panel.validate();
    }

    void populateTree() {
        if(populated) {
            return;
        }
        populated = true;
        resultsTree.setModel(new ResultModel(rootNode));
        resultsTree.setCellRenderer(new SummaryCellRenderer(this, linkerSupport, dispResults, kenaiUsersMap));
    }

    public JTree getList() {       
        return resultsTree;
    }

    public JComponent getTreeComponent() {
        return panel;
    }

    public List getResults() {
        return dispResults;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        // XXX hack -> force cell width, needed only for visible rows!!!
        // XXX

//        System.out.println(" resized !!! ");
//        panel.validate();

        ResultModel tm = ((ResultModel) resultsTree.getModel());
        int c = rootNode.getChildCount();
        TreeNode[] nodes = new TreeNode[c];
        int[] indices = new int[c];
        for (int i = 0; i < c; i++) {
            nodes[i] = rootNode.getChildAt(i);
            indices[i] = i;
        }
        tm.fireTreeNodesChanged(rootNode, rootNode.getPath(), indices, nodes);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // not interested
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // not interested
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // not interested
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath path = resultsTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if (path == null) return;
        Rectangle rect = resultsTree.getPathBounds(path);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        
        String revision = getRevision(path);
        if(revision != null) {
            linkerSupport.mouseClicked(p, revision);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        resultsTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resultsTree.setToolTipText("");
        TreePath path = resultsTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if (path == null) return;
        Rectangle rect = resultsTree.getPathBounds(path);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);

        String revision = getRevision(path);
        if(revision != null) {
            linkerSupport.mouseMoved(p, resultsTree, revision);
        }
    }

    private String getRevision(TreePath path) {
        String revision = null;
        Object o = path.getLastPathComponent();
        if(o instanceof LogEntryNode) {
            LogEntryNode len = (LogEntryNode) o;
            o = len.getUserObject();
            LogEntry le = (LogEntry) len.getUserObject();
            revision = le.getRevision();
        } else if (o instanceof ActionNode) {
            ActionNode an = (ActionNode) o;
            LogEntry le = (LogEntry) ((LogEntryNode) an.getParent()).getUserObject();;
            revision = le.getRevision();
        }
        return revision;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // not interested
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    private void onPopup(MouseEvent e) {
        onPopup(e.getPoint());
    }

    protected abstract void onPopup(Point p);

    public JComponent getComponent() {
        return scrollPane;
    }


    class RootNode extends DefaultMutableTreeNode {
        public RootNode(List<LogEntry> children) {
            int i = 0;
            for (LogEntry logEntry : children) {
                insert(new LogEntryNode(logEntry, this), i++);
            }
        }
    }

    class LogEntryNode extends DefaultMutableTreeNode {
        public WaitNode waitNode;
        private boolean wait = true;

        public LogEntryNode(LogEntry entry, MutableTreeNode parent) {
            super(entry, true);
            waitNode = new WaitNode(this);
            add(waitNode);
        }

        private void populateEvents(final TreePath path) {
            if(!wait) return;
            wait = false;
            rp.create(new Runnable() {
                @Override
                public void run() {
                    final List<LogEntry.Event> events = new LinkedList<LogEntry.Event>(((LogEntry) getUserObject()).getEvents());
                    if(events == null) {
                        return;
                    }
                    Collections.sort(events, new EventComparator());
                    final DefaultTreeModel model = (DefaultTreeModel) resultsTree.getModel();
                    final boolean expanded = resultsTree.isExpanded(path) ;
                    if(waitNode != null) {
                        model.removeNodeFromParent(waitNode);
                    }
                    waitNode = null;
                    EventQueue.invokeLater(new Runnable() {
                       @Override
                        public void run() {
                            model.insertNodeInto(new ActionNode(LogEntryNode.this), LogEntryNode.this, 0);
                            int i = 1;
                            for (LogEntry.Event event : events) {
                                model.insertNodeInto(new EventNode(event, LogEntryNode.this), LogEntryNode.this, i++);
                            }
                            if(expanded) {
                                resultsTree.expandPath(path);
                            }
                        }
                    });
                }
            }).schedule(0);
        }
    }

    class WaitNode extends DefaultMutableTreeNode {
        public WaitNode(MutableTreeNode parent) {
            super("Please wait...", false);
        }
    }

    class EventNode extends DefaultMutableTreeNode {
        public EventNode(LogEntry.Event event, LogEntryNode parent) {
            super(event, false);
        }
    }

    class ActionNode extends DefaultMutableTreeNode {
        public ActionNode(LogEntryNode parent) {
            super(parent, false);
        }
    }
    
 
    private static class HyperlinkLabel extends JLabel {

        public HyperlinkLabel() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void set(String text, Color foreground, Color background) {
            StringBuilder sb = new StringBuilder(100);
            if (foreground.equals(UIManager.getColor("List.foreground"))) { // NOI18N
                sb.append("<html><a href=\"\">"); // NOI18N
                sb.append(text);
                sb.append("</a>"); // NOI18N
            } else {
                sb.append("<html><a href=\"\" style=\"color:"); // NOI18N
                sb.append("rgb("); // NOI18N
                sb.append(foreground.getRed());
                sb.append(","); // NOI18N
                sb.append(foreground.getGreen());
                sb.append(","); // NOI18N
                sb.append(foreground.getBlue());
                sb.append(")"); // NOI18N
                sb.append("\">"); // NOI18N
                sb.append(text);
                sb.append("</a>"); // NOI18N
            }
            setText(sb.toString());
            setBackground(background);
        }
    }

    private class ResultModel extends DefaultTreeModel {
        public ResultModel(TreeNode root) {
            super(root, true);
        }
        @Override
        protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
            super.fireTreeNodesChanged(source, path, childIndices, children);
        }
    }

    private class SelectionModel extends DefaultTreeSelectionModel {

        public SelectionModel() {
        }

        @Override
        public void addSelectionPath(TreePath path) {
            if(!checkPath(path)) return;
            super.addSelectionPath(path);
        }

        @Override
        protected boolean canPathsBeAdded(TreePath[] paths) {
            return super.canPathsBeAdded(paths);
        }

        @Override
        public void addSelectionPaths(TreePath[] paths) {
            super.addSelectionPaths(paths);
        }

        @Override
        public void setSelectionPaths(TreePath[] paths) {
            if(paths.length == 0) {
                return;
            } else if(paths.length > 1) {
                for (TreePath treePath : paths) {
                    if(!checkPath(treePath)) return;
                }
            } else {
                if(!checkPath(paths[0])) {
                    if(!isSelectionEmpty()) {
                        TreePath[] lastPath = getSelectionPaths();
                        if(lastPath.length == 1) {
                            int rPrev = resultsTree.getRowForPath(lastPath[0]);
                            int rNext = resultsTree.getRowForPath(paths[0]);
                            if(rPrev > rNext && rNext - 1 > -1) {
                                rNext--;
                            } else if (rPrev < rNext && ((rNext + 1) < resultsTree.getRowCount())) {
                                rNext++;
                            } else {
                                return;
                            }
                            paths[0] = resultsTree.getPathForRow(rNext);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            super.setSelectionPaths(paths);
        }

        @Override
        public void setSelectionPath(TreePath path) {
            if(!checkPath(path)) return;
            super.setSelectionPath(path);
        }

        private boolean checkPath(TreePath path) {
            return !(path.getLastPathComponent() instanceof ActionNode);
        }

    }

    private static class EventComparator implements Comparator<LogEntry.Event> {
        @Override
        public int compare(LogEntry.Event o1, LogEntry.Event o2) {
            if(o1 == null && o2 == null) {
                return 0;
            } else if(o1 == null) {
                return -1;
            } else if(o2 == null) {
                return 1;
            }
            int c = o1.getAction().compareTo(o2.getAction());
            if(c != 0) {
                return c;
            }
            return o1.getPath().compareTo(o2.getPath());
        }
    }

}
