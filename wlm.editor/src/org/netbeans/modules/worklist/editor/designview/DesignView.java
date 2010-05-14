/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.modules.soa.ldap.LDAPUtils;
import org.netbeans.modules.soa.ldap.browser.compact.LDAPCompactBrowser;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.dataloader.WorklistDataObject;
import org.netbeans.modules.worklist.editor.designview.components.ExScrollPane;
import org.netbeans.modules.worklist.editor.designview.components.ExTabbedPane;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.nodes.TaskNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author anjeleevich
 */
public class DesignView extends JPanel implements Widget {
    private TopComponent topComponent;

    private WorklistDataObject dataObject;
    
    private ExScrollPane errorMessageScrollPane;
    private JEditorPane errorMessagePane;
    
    private ExTabbedPane tabbedPane;
    
    private BasicPropertiesPanel basicPropertiesPanel;
    private EscalationsPanel escalationsPanel;
    private ActionsPanel actionsPanel;
    private NotificationsPanel notificationsPanel;
    
    private ModelChangeHandler modelChangeHandler;

    private Widget selectedWidget;

    private SelectWidgetRunnable selectWidgetRunnable;
    private UpdateWidgetRunnable updateWidgetRunnable;

    private final Object sync = new Object();

    private LDAPCompactBrowser ldapCompactBrowser;
    private JToggleButton expandLDAPCompactBrowserButton;

    private ChangeListener tabbedPaneChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            ExTabbedPane.Tab tab = tabbedPane.getActiveTab();
            
            JComponent centerContent = (tab == null) ? null
                    : tab.getCenterContent();

            Widget widget = (Widget) centerContent;

            selectWidget(widget);

            expandLDAPCompactBrowserButton.setEnabled(
                    (centerContent == basicPropertiesPanel)
                    || (centerContent == escalationsPanel));
        }
    };

    public DesignView(TopComponent topComponent, 
            WorklistDataObject dataObject)
    {
        ExUtils.setA11Y(this, "DesignView"); // NOI18N

        this.topComponent = topComponent;
        this.dataObject = dataObject;

        selectedWidget = this;
        tabbedPane = new ExTabbedPane();
        tabbedPane.setVisible(true);
        ExUtils.setA11Y(tabbedPane, DesignView.class,
                "DesignViewTabbedPane"); // NOI18N

        ldapCompactBrowser = new LDAPCompactBrowser();
        ExUtils.setA11Y(ldapCompactBrowser, DesignView.class,
                "LDAPCompactBrowser"); // NOI18N

        basicPropertiesPanel = new BasicPropertiesPanel(this);
        basicPropertiesPanel.addToTabbedPane(tabbedPane);
        
        escalationsPanel = new EscalationsPanel(this);
        escalationsPanel.addToTabbedPane(tabbedPane);
        
        notificationsPanel = new NotificationsPanel(this);
        notificationsPanel.addToTabbedPane(tabbedPane);

        actionsPanel = new ActionsPanel(this);
        actionsPanel.addToTabbedPane(tabbedPane);

        errorMessagePane = new JEditorPane();
        errorMessagePane.setEditorKitForContentType("text/html", // NOI18N
                new HTMLEditorKit()); 
        errorMessagePane.setEditable(false);
        errorMessagePane.setPreferredSize(new Dimension(200, 200));
        errorMessagePane.setBorder(new EmptyBorder(20, 20, 20, 20));
        errorMessagePane.setContentType("text/html"); // NOI18N
        errorMessagePane.setBackground(this.getBackground());
        errorMessagePane.setText(NbBundle.getMessage(DesignView.class, 
                "BROKEN_SOURCES_ERROR_MESSAGE")); // NOI18N

        errorMessageScrollPane = new ExScrollPane(errorMessagePane);
        errorMessageScrollPane.setVisible(false);

        ExUtils.setA11Y(errorMessagePane, DesignView.class, 
                "ErrorMessagePane"); // NOI18N
        ExUtils.setA11Y(errorMessageScrollPane, DesignView.class,
                "ErrorMessageScrollPane"); // NOI18N
        
        add(errorMessageScrollPane);
        add(tabbedPane);

        tabbedPane.addChangeListener(tabbedPaneChangeListener);
        
        modelChangeHandler = new ModelChangeHandler(this);

        expandLDAPCompactBrowserButton = new JToggleButton(LDAPUtils
                .getLDAPIcon());
        expandLDAPCompactBrowserButton.setToolTipText(NbBundle
                .getMessage(DesignView.class,
                        "TTT_EXPAND_COLLAPSE_LDAP_BROWSER")); // NOI18
        expandLDAPCompactBrowserButton.setSelected(ldapCompactBrowser
                .isExpanded());
        expandLDAPCompactBrowserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ldapCompactBrowser.setExpanded(expandLDAPCompactBrowserButton
                        .isSelected());
            }
        });

        ExUtils.setA11Y(expandLDAPCompactBrowserButton, DesignView.class,
                "ExpandLDAPCompactBrowserButton"); // NOI18N

        ldapCompactBrowser.addPropertyChangeListener(LDAPCompactBrowser
                .EXPANDED_RPOPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                expandLDAPCompactBrowserButton.setSelected(ldapCompactBrowser
                        .isExpanded());
            }
        });
    }

    public JToggleButton getExpandLDAPCompactBrowserButton() {
        return expandLDAPCompactBrowserButton;
    }

    public LDAPCompactBrowser getLDAPCompactBrowser() {
        return ldapCompactBrowser;
    }

    public void showBasicPropertiesTab() {
        tabbedPane.activateTab(basicPropertiesPanel);
    }

    public void showEscalationsTab() {
        tabbedPane.activateTab(escalationsPanel);
    }

    public void showActionsTab() {
        tabbedPane.activateTab(actionsPanel);
    }

    public void showNotificationsTab() {
        tabbedPane.activateTab(notificationsPanel);
    }

    public void selectNode(WLMNode node) {
        // set temporal activate node (1)
        // to avoid blinking of navigator selection and property sheet
        setActivatedNode(node);

        Widget widget = findWidgetForNode(this, node);
        if (widget != null) {
            selectWidget(widget);
            widget.requestFocusToWidget();
        } else {
            // restore old activated node. See (1).
            setActivatedNode(selectedWidget.getWidgetNode());
        }
    }

    public void selectComponent(WLMComponent component) {
        Widget widget = findWidgetForComponent(this, component);

        while (widget == null) {
            component = component.getParent();
            widget = findWidgetForComponent(this, component);
        }

        selectWidget(widget);
        widget.requestFocusToWidget();
    }

    public Widget findWidgetForNode(Widget widget, WLMNode node) {
        Widget result = null;

        int widgetCount = widget.getWidgetCount();
        for (int i = 0; result == null && i < widgetCount; i++) {
            result = findWidgetForNode(widget.getWidget(i), node);
        }

        if (result == null
                && node.getType() == widget.getWidgetNodeType()
                && widget.getWidgetWLMComponent() == node.getWLMComponent())
        {
            result = widget;
        }
        
        return result;
    }

    public Widget findWidgetForComponent(Widget widget,
            WLMComponent component)
    {
        if (component == null) {
            return basicPropertiesPanel;
        }

        if (component instanceof TTask) {
            return basicPropertiesPanel;
        }

        Widget result = null;

        int widgetCount = widget.getWidgetCount();

        for (int i = 0; result == null && i < widgetCount; i++) {
            result = findWidgetForComponent(widget.getWidget(i), component);
        }

        if (result == null && (component == widget.getWidgetWLMComponent())) {
            result = widget;
        }

        return result;
    }

    Lookup getNodeLookup() {
        return dataObject.getLookup();
    }

    void setActivatedNode(Node node) {
        topComponent.setActivatedNodes(new Node[] { node });
    }

    void selectWidget(Widget widget) {
        synchronized (sync) {
            if (selectWidgetRunnable == null) {
                selectWidgetRunnable = new SelectWidgetRunnable(widget);
                SwingUtilities.invokeLater(selectWidgetRunnable);
            } else {
                selectWidgetRunnable.setWidget(widget);
            }
        }
    }

    void selectWidgetImpl(Widget widget) {
        if (selectedWidget != widget) {
            this.selectedWidget = widget;
            setActivatedNode(widget.getWidgetNode());
        }
    }

    void updateWidget() {
        synchronized (sync) {
            if (updateWidgetRunnable == null) {
                updateWidgetRunnable = new UpdateWidgetRunnable();
                SwingUtilities.invokeLater(updateWidgetRunnable);
            }
        }
    }

    void updateWidgetImpl() {
        if (selectedWidget != null) {
            // at first will check does selected widget exists
            // if true, let't reload corresponding node
            // else - select another widget
            if (isWidgetInHierarhy(selectedWidget)) {
                Node[] nodes = topComponent.getActivatedNodes();
                if (nodes != null) {
                    for (Node node : nodes) {
                        if (node instanceof WLMNode) {
                            ((WLMNode) node).reload();
                        }
                    }
                }
                return;
            }
            
            Widget widget = selectedWidget;
            Widget parent = selectedWidget.getWidgetParent();
            
            Widget selectedWidgetCandidate = null;

            while (parent != null) {
                int widgetCount = parent.getWidgetCount();

                boolean inParent = false;
                for (int i = 0; i < widgetCount; i++) {
                    if (parent.getWidget(i) == widget) {
                        inParent = true;
                        break;
                    }
                }

                if (!inParent) {
                    selectedWidgetCandidate = parent;
                }

                widget = parent;
                parent = widget.getWidgetParent();
            }

            if (selectedWidgetCandidate != null) {
                selectWidget(selectedWidgetCandidate);
                return;
            }
        }

        selectWidget((Widget) tabbedPane.getActiveTab().getCenterContent());
    }

    private boolean isWidgetInHierarhy(Widget widget) {
        if (widget == null) {
            return false;
        }

        Widget parent = widget.getWidgetParent();
        while (parent != null) {
            int widgetCount = parent.getWidgetCount();

            boolean inParent = false;
            for (int i = 0; i < widgetCount; i++) {
                if (parent.getWidget(i) == widget) {
                    inParent = true;
                    break;
                }
            }

            if (!inParent) {
                return false;
            }

            widget = parent;
            parent = widget.getWidgetParent();
        }

        return true;
    }

    EscalationsPanel getEscalationsPanel() {
        return escalationsPanel;
    }
    
    BasicPropertiesPanel getBasicPropertiesPanel() {
        return basicPropertiesPanel;
    }
    
    ActionsPanel getActionsPanel() {
        return actionsPanel;
    }
    
    NotificationsPanel getNotificationsPanel() {
        return notificationsPanel;
    }
    
    public WorklistDataObject getDataObject() {
        return dataObject;
    }
    
    public WLMModel getModel() {
        return dataObject.getModel();
    }
    
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        
        tabbedPane.setBounds(0, 0, w, h);
        errorMessageScrollPane.setBounds(0, 0, w, h);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    void setModelIsBroken(boolean broken) {
        if (broken != errorMessageScrollPane.isVisible()) {
            errorMessageScrollPane.setVisible(broken);
            tabbedPane.setVisible(!broken);

            if (broken) {
                topComponent.setActivatedNodes(new Node[] {
                    dataObject.getNodeDelegate() } );
            } else {
                topComponent.setActivatedNodes(new Node[] {
                    selectedWidget.getWidgetNode()
                });
                updateWidget();
            }

            revalidate();
            repaint();
        }
    }

    public Widget getWidget(int index) {
        if (index == 0) {
            return basicPropertiesPanel;
        }

        if (index == 1) {
            return escalationsPanel;
        }

        if (index == 2) {
            return actionsPanel;
        }

        if (index == 3) {
            return notificationsPanel;
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        return 4;
    }

    public Node getWidgetNode() {
        return new TaskNode(getModel().getTask(), Children.LEAF,
                getNodeLookup());
    }

    public void requestFocusToWidget() {
        
    }

    public Widget getWidgetParent() {
        return null;
    }

    public WLMComponent getWidgetWLMComponent() {
        return getModel().getTask();
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.TASK;
    }

    private class SelectWidgetRunnable implements Runnable {
        private Widget toSelect;

        SelectWidgetRunnable(Widget toSelect) {
            this.toSelect = toSelect;
        }

        void setWidget(Widget toSelect) {
            this.toSelect = toSelect;
        }

        public void run() {
            synchronized (sync) {
                selectWidgetRunnable = null;
            }
            selectWidgetImpl(toSelect);
        }
    }

    private class UpdateWidgetRunnable implements Runnable {
        public void run() {
            synchronized (sync) {
                updateWidgetRunnable = null;
            }
            updateWidgetImpl();
        }
    }
}
