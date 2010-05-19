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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExTabbedPane;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.EscalationNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class EscalationPanel extends DesignViewPanel implements Widget {
    
    private TEscalation escalation;
    
    private TimeoutPanel timeoutPanel;
    private AssignmentPanel assignmentPanel;
    private LocalNotificationsPanel localNotificationsPanel;
    
    private TitledPanel titledPanel;
    private RemoveEscalationAction removeEscalationAction;
    private Widget widgetParent;
    
    public EscalationPanel(Widget widgetParent, DesignView designView,
            TEscalation escalation)
    {
        super(designView);

        ExUtils.setA11Y(this, "EscalationPanel"); // NOI18N

        this.widgetParent = widgetParent;
        this.escalation = escalation;
        this.removeEscalationAction = new RemoveEscalationAction();
        
        setBackground(TitledPanel.BACKGROUND_COLOR);
        setOpaque(true);
        setBorder(null);
        setLayout(new BorderLayout());
        
        timeoutPanel = new TimeoutPanel(this, designView, escalation);
        timeoutPanel.setBorder(new TimeoutBorder());
        
        assignmentPanel = new AssignmentPanel(this, designView, escalation);
        assignmentPanel.setBorder(new AssignmentBorder());
        
        localNotificationsPanel = new LocalNotificationsPanel(this, designView,
                escalation);
        localNotificationsPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        add(timeoutPanel, BorderLayout.NORTH);
        add(assignmentPanel, BorderLayout.CENTER);
        add(localNotificationsPanel, BorderLayout.SOUTH);
        
        titledPanel = new TitledPanel(getMessage("LBL_ESCALATION"), // NOI18N
                removeEscalationAction, this, 0);
        ExUtils.setA11Y(titledPanel, EscalationPanel.class,
                "EscalationTitlePanel"); // NOI18N
    }
    
    public TEscalation getEscalation() {
        return escalation;
    }
    
    public void processWLMModelChanged() {
        assignmentPanel.processWLMModelChanged();
        timeoutPanel.processWLMModelChanged();
        localNotificationsPanel.processWLMModelChanged();
    }
    
    public JComponent getView() {
        return titledPanel;
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public Widget getWidget(int index) {
        if (index == 0) {
            return timeoutPanel;
        }

        if (index == 1) {
            return assignmentPanel;
        }

        if (index == 2) {
            return localNotificationsPanel;
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        return 3;
    }

    public Node getWidgetNode() {
        return new EscalationNode(escalation, Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showEscalationsTab();
        scrollRectToVisible(new Rectangle(getSize()));
    }

    public WLMComponent getWidgetWLMComponent() {
        return escalation;
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.ESCALATION;
    }
    
    private static class TimeoutBorder implements Border {
        public void paintBorder(Component c, Graphics g, int x, int y, 
                int width, int height) 
        {
            int x2 = x + width - 1;
            int y2 = y + height - 1;
            g.setColor(ExTabbedPane.TAB_BORDER_COLOR);
            g.drawLine(x, y2, x2, y2);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(4, 0, 8, 0);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    }

    private static class AssignmentBorder implements Border {
        public void paintBorder(Component c, Graphics g, int x, int y, 
                int width, int height) 
        {
            
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(8, 0, 0, 0);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    }
    
    private class RemoveEscalationAction extends AbstractAction {
        RemoveEscalationAction() {
            super(getMessage("LBL_REMOVE_ESCALATION")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            WLMModel model = getModel();
            TTask task = getTask();
            
            if (model.startTransaction()) {
                try {
                    task.removeEscalation(escalation);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(EscalationPanel.class, key);
    }            
}
