/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExTabbedPane;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.StyledLabel;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.EscalationsNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class EscalationsPanel extends DesignViewPanel implements Widget {
    
    private AddEscalation addEscalation;
    private AddEscalationAction addEscalationAction;
    
    public EscalationsPanel(DesignView designView) {
        super(designView);

        ExUtils.setA11Y(this, "EscalationsPanel"); // NOI18N

        addEscalationAction = new AddEscalationAction();
        addEscalation = new AddEscalation();
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        add(addEscalation);
        
        processWLMModelChanged(false);
    }
    
    public void processWLMModelChanged(boolean processChildren) {
        TTask task = getTask();
        List<TEscalation> escalationsList = (task == null) ? null 
                : task.getEscalations();
        
        if (escalationsList == null || escalationsList.size() == 0) {
            for (int i = getComponentCount() - 2; i >= 0; i--) {
                remove(i);
            }
        } else {
            Map<TEscalation, EscalationPanel> modelToViewMap 
                    = new HashMap<TEscalation, EscalationPanel>();
            
            for (int i = getComponentCount() - 3; i >= 0; i -= 2) {
                Component component = getComponent(i);
                EscalationPanel escalationPanel 
                        = ((EscalationPanel) ((TitledPanel) component)
                        .getContent());
                
                TEscalation escalation = escalationPanel.getEscalation();
                if (escalationsList.contains(escalation)) {
                    modelToViewMap.put(escalationPanel.getEscalation(), 
                            escalationPanel);
                } else {
                    remove(i + 1);
                    remove(i);
                }
            }
            
            for (int i = 0; i < escalationsList.size(); i++) {
                TEscalation escalation = escalationsList.get(i);

                EscalationPanel escalationPanel = modelToViewMap
                        .get(escalation);
                
                if (escalationPanel == null) {
                    escalationPanel = new EscalationPanel(this, getDesignView(),
                            escalation);
                    add(escalationPanel.getView(), i * 2);
                    add(Box.createVerticalStrut(12), i * 2 + 1)
                            .setFocusable(false);
                } 
           }

            if (processChildren) {
                for (EscalationPanel escalationPanel : modelToViewMap.values()) 
                {
                    escalationPanel.processWLMModelChanged();
                }
            }
        }
        
        revalidate();
        repaint();
    }
 
    void addToTabbedPane(ExTabbedPane tabbedPane) {
        ExTabbedPane.Tab tab = tabbedPane.addTab(NAME, this, true,
                getDesignView().getLDAPCompactBrowser(), null);
        
        tab.addHeaderRow(getMessage("LBL_ESCALATIONS"), // NOI18N
                null, // NOI18N
                StyledLabel.PLAIN_STYLE);
    }

    public Widget getWidgetParent() {
        return getDesignView();
    }

    public Widget getWidget(int index) {
        int componentCount = getComponentCount();
        
        for (int i = 0; i <= componentCount; i++) {
            Component component = getComponent(i);
            if (component instanceof TitledPanel) {
                JComponent content = ((TitledPanel) component).getContent();
                if (content instanceof EscalationPanel) {
                    if (index == 0) {
                        return (EscalationPanel) content;
                    } else {
                        index--;
                    }
                }
            }
        }
    
        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        int count = 0;
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            Component component = getComponent(i);
            if ((component instanceof TitledPanel) && (((TitledPanel)
                    component).getContent() instanceof EscalationPanel))
            {
                count++;
            }
        }
        return count;
    }

    public Node getWidgetNode() {
        return new EscalationsNode(getTask(), Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showEscalationsTab();
    }

    public WLMComponent getWidgetWLMComponent() {
        return getTask();
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.ESCALATIONS;
    }
    
    private static class InsertEscalation extends JPanel {
        private LinkButton insertEscalationButton;

        InsertEscalation() {
            setOpaque(false);
            insertEscalationButton = new LinkButton(getMessage(
                    "LBL_ADD_ESCALATION")); // NOI18N
            add(insertEscalationButton);
        }
        
        @Override
        public Insets getInsets() {
            return new Insets(0, 8, 0, 8);
        }

        @Override
        public void doLayout() {
            Insets insets = getInsets();
            
            int w = getWidth() - insets.left - insets.right;
            int h = getHeight() - insets.top - insets.bottom;
            
            Dimension size = insertEscalationButton.getPreferredSize();
            
            insertEscalationButton.setBounds(
                    insets.left + (w - size.width) / 2,
                    insets.top + (h - size.height) / 2, 
                    size.width, 
                    size.height);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldStoke = g2.getStroke();
            
            int x1 = 1;
            int x4 = getWidth() - 2;
            
            int x2 = insertEscalationButton.getX() - 1;
            int x3 = insertEscalationButton.getX() 
                    + insertEscalationButton.getWidth();

            int y = insertEscalationButton.getY() 
                    + insertEscalationButton.getHeight() / 2;
            
            g2.setColor(ExTabbedPane.TAB_BORDER_COLOR);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 1, new float[] { 4, 4 } , 0));
            
            if (x1 <= x2) {
                g2.drawLine(x2, y, x1, y);
            }
            
            if (x3 <= x4) {
                g2.drawLine(x3, y, x4, y);
            }
            
            g2.setStroke(oldStoke);
        }
        
        @Override
        public Dimension getPreferredSize() {
            Insets insets = getInsets();
            Dimension size = insertEscalationButton.getPreferredSize();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }
        
        @Override
        public Dimension getMaximumSize() {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }
    }
    
    private class AddEscalation extends JPanel implements FocusListener {
        private LinkButton addEscalationButton;

        AddEscalation() {
            setOpaque(false);
            addEscalationButton = new LinkButton(getMessage(
                    "LBL_ADD_ESCALATION")); // NOI18N
            addEscalationButton.addActionListener(addEscalationAction);
            addEscalationButton.addFocusListener(this);

            ExUtils.setA11Y(this, EscalationPanel.class, 
                    "AddEscalation"); // NOI18N
            ExUtils.setA11Y(addEscalationButton, EscalationPanel.class, 
                    "AddEscalationButton"); // NOI18N

            add(addEscalationButton);
        }
        
        @Override
        public Insets getInsets() {
            return new Insets(8, 8, 8, 8);
        }

        @Override
        public void doLayout() {
            Insets insets = getInsets();
            
            int w = getWidth() - insets.left - insets.right;
            int h = getHeight() - insets.top - insets.bottom;
            
            Dimension size = addEscalationButton.getPreferredSize();
            
            addEscalationButton.setBounds(
                    insets.left + (w - size.width) / 2,
                    insets.top + (h - size.height) / 2, 
                    size.width, 
                    size.height);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldStoke = g2.getStroke();
            
            g2.setColor(ExTabbedPane.TAB_BORDER_COLOR);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 1, new float[] { 4, 4 } , 0));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            
            g2.setStroke(oldStoke);
        }
        
        @Override
        public Dimension getPreferredSize() {
            Insets insets = getInsets();
            Dimension size = addEscalationButton.getPreferredSize();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }
        
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        public void focusGained(FocusEvent e) {
            selectWidget(EscalationsPanel.this);
        }

        public void focusLost(FocusEvent e) {
            // do nothing
        }
    }
    
    private class AddEscalationAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            WLMModel model = getModel();
            TTask task = model.getTask();

            if (model.startTransaction()) {
                TEscalation escalation = null;
                try {
                    escalation = model.getFactory().createEscalation(model);
                    task.addEscalation(escalation);
                } finally {
                    model.endTransaction();
                }
            } 
        }
    }
    
    public static final String NAME = "ESCALATIONS"; // NOI18N
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(EscalationsPanel.class, key);
    }       
}
