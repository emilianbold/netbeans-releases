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
package org.netbeans.modules.dlight.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.DLightSessionListener;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

public class DLightSessionsViewPanel extends JPanel
        implements DLightSessionListener, ValidationListener {

    private static final String stateColumnID = "State"; // NOI18N
    private DLightSession currentSession = null;
    private Models.CompoundModel model;
    private Vector<ModelListener> listeners = new Vector<ModelListener>();
    private final static Comparator<DLightTool> toolsComparator;
    private boolean isEmpty = true;
    private JComponent treeTableView = null;

    static {
        toolsComparator = new Comparator<DLightTool>() {

            public int compare(DLightTool o1, DLightTool o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
    }

    public DLightSessionsViewPanel() {
    }

    public void activeSessionChanged(DLightSession oldSession, DLightSession newSession) {
        if (newSession == null) {
            setEmptyContent();
        } else if (newSession == currentSession) {
            return;
        } else {
            updateContent(newSession);
        }
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        updateContent(session);
    }

    public void sessionAdded(DLightSession newSession) {
//        updateContent();
    }

    public void sessionRemoved(DLightSession removedSession) {
//        updateContent();
    }

    void startup() {
        DLightManager.getDefault().addDLightSessionListener(this);

        model = initModel();
        treeTableView = Models.createView(model);
        setTreeViewContent();
    }

    private Models.CompoundModel initModel() {
        List<ColumnModelImpl> columns = Arrays.asList(
                new ColumnModelImpl(stateColumnID, loc("DLightSessionsViewPanel.stateColumn.name"), String.class)); // NOI18N

        List<Model> models = new ArrayList<Model>();
        models.add(new TreeModelImpl());
        models.add(new TableModelImpl());
        models.add(new NodeModelImpl());
        models.add(new NodeActionsProviderImpl());
        models.addAll(columns);

        return Models.createCompoundModel(models);
    }

    private void setEmptyContent() {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(loc("DLightSessionsViewPanel.emptyContent.text")); // NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        add(label);
        revalidate();
        repaint();
        isEmpty = true;
    }

    private void setTreeViewContent() {
        UIThread.invoke(new Runnable() {

            public void run() {
                removeAll();
                setLayout(new BorderLayout());

                if (treeTableView != null) {
                    add(treeTableView, BorderLayout.CENTER);
                    isEmpty = false;
                }

                revalidate();
                repaint();
            }
        });
    }

    private static String loc(String key) {
        return NbBundle.getMessage(DLightSessionsViewPanel.class, key);
    }

    protected void updateContent(Object node) {
        for (ModelListener l : listeners) {
            l.modelChanged(new ModelEvent.TreeChanged(node == null ? model.getRoot() : node));
        }

        if (isEmpty) {
            setTreeViewContent();
        }
    }

    public void validationStateChanged(Validateable source, ValidationStatus oldStatus, ValidationStatus newStatus) {
        updateContent(source);
    }

    class TreeModelImpl implements TreeModel {

        public Object getRoot() {
            return ROOT;
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (parent == ROOT) {
                List<DLightSession> result = new ArrayList<DLightSession>();
                result.addAll(DLightManager.getDefault().getSessionsList());
                return result.toArray();
            }

            if (parent instanceof DLightSession) {
                DLightSession session = (DLightSession) parent;
                SortedSet<DLightTool> tools = new TreeSet<DLightTool>(toolsComparator);
                tools.addAll(session.getTools());
                return tools.toArray();
            }

            throw new UnknownTypeException(parent);
        }

        public boolean isLeaf(Object node) {
            if (node == ROOT) {
                return false;
            }

            if (node instanceof DLightSession) {
                return false;
            }

            return true;
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            if (node == ROOT) {
                return DLightManager.getDefault().getSessionsList().size();
            }

            if (node instanceof DLightSession) {
                DLightSession session = (DLightSession) node;
                return session.getTools().size();
            }

            return 0;
        }

        public void addModelListener(ModelListener l) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }

        public void removeModelListener(ModelListener l) {
            listeners.remove(l);
        }
    }

    class TableModelImpl implements TableModel {

        public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
            if (columnID == null) {
                return null;
            }

            if (node instanceof DLightSession) {
                DLightSession session = (DLightSession) node;

                if (columnID.equals(stateColumnID)) {
                    return session.getState().toString();
                }

                return null;
            }

            if (node instanceof DLightTool) {
                DLightTool tool = (DLightTool) node;
                ValidationStatus status = tool.getValidationStatus();
                if (columnID.equals(stateColumnID)) {
                    if (status.isValid()) {
                        return "OK"; // NOI18N
                    }

                    if (status.isInvalid()) {
                        return "Tool cannot be used. " + status.getReason(); // NOI18N
                    }

                    return status.getReason();
                }

                return null;
            }

            if (node instanceof JToolTip) {
                if (columnID.equals(stateColumnID)) {
                    // TODO:
                    return "ToolState"; // NOI18N
                }
            }

            throw new UnknownTypeException(node);
        }

        public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
            return true;
        }

        public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        }

        public void addModelListener(ModelListener l) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }

        public void removeModelListener(ModelListener l) {
            listeners.remove(l);
        }
    }

    class NodeModelImpl implements ExtendedNodeModel {

        public boolean canRename(Object node) throws UnknownTypeException {
            return false;
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            return false;
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            return false;
        }

        public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
            return null;
        }

        public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
            return null;
        }

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            return null;
        }

        public void setName(Object node, String name) throws UnknownTypeException {
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            return null;
        }

        public String getDisplayName(Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return loc("DLightSessionsViewPanel.NodeModelImpl.rootNode.name"); // NOI18N
            }

            if (node instanceof DLightSession) {
                DLightSession session = (DLightSession) node;
                // TODO:
                if (DLightManager.getDefault().getActiveSession() == session) {
                    return "<html><b>" + session.getDescription() + "</b></html>"; // NOI18N
                } else {
                    return session.getDescription();
                }
            }

            if (node instanceof DLightTool) {
                DLightTool tool = (DLightTool) node;
                ValidationStatus status = tool.getValidationStatus();

                if (status.isValid()) {
                    return tool.getName();
                }

                if (status.isInvalid()) {
                    return "<html><font color=\"#FF0000\">" + tool.getName() + "</font></html>"; // NOI18N
                }

                return "<html><b>?</b> " + tool.getName() + "</html>"; // NOI18N
            }

            throw new UnknownTypeException(node);
        }

        public String getIconBase(Object node) throws UnknownTypeException {
            return null;
        }

        public String getShortDescription(Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return loc("DLightSessionsViewPanel.NodeModelImpl.rootNode.tooltip"); // NOI18N
            }

            if (node instanceof DLightSession) {
                DLightSession session = (DLightSession) node;
                // TODO:
                return session.getDescription();
            }

            if (node instanceof DLightTool) {
                DLightTool tool = (DLightTool) node;
                return tool.toString();
            }

            throw new UnknownTypeException(node);
        }

        public void addModelListener(ModelListener l) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }

        public void removeModelListener(ModelListener l) {
            listeners.remove(l);
        }
    }

    class NodeActionsProviderImpl implements NodeActionsProvider {

        public void performDefaultAction(Object node) throws UnknownTypeException {
            if (!(node instanceof Event)) {
                throw new UnknownTypeException(node);
            }
        }

        public Action[] getActions(Object node) throws UnknownTypeException {
            if (node instanceof DLightSession) {
                final DLightSession session = (DLightSession) node;
                AbstractAction makeCurrentAction = new AbstractAction(loc("DLightSessionsViewPanel.actions.MakeCurrent.name")) { // NOI18N

                    public void actionPerformed(ActionEvent e) {
                        DLightManager.getDefault().setActiveSession(session);
                    }
                };

                makeCurrentAction.setEnabled(!session.isActive());

                AbstractAction closeSessionAction = new AbstractAction(loc("DLightSessionsViewPanel.actions.CloseSession.name")) { // NOI18N

                    public void actionPerformed(ActionEvent e) {
                        DLightManager.getDefault().closeSession(session);
                    }
                };

                return new Action[]{makeCurrentAction, closeSessionAction};
            }

            ValidationStatus status = null;

            if (node instanceof Validateable) {
                status = ((Validateable) node).getValidationStatus();
            }

            if (node instanceof DLightTool) {
                return status == null ? new Action[0] : status.getRequiredActions().toArray(new Action[0]);
            }

            return new Action[]{};
        }
    }

    static class ColumnModelImpl extends ColumnModel {

        private String id;
        private String displayName;
        private Class type;
        private boolean isVisible = true;

        public ColumnModelImpl(String id, String displayName, Class type) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Class getType() {
            return type;
        }

        @Override
        public void setVisible(boolean visible) {
            isVisible = visible;
        }

        @Override
        public boolean isVisible() {
            return isVisible;
        }
    }
}
