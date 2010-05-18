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
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.JRadioButton;
import org.netbeans.modules.wlm.model.api.DeadlineOrDuration;
import org.netbeans.modules.wlm.model.api.TDeadlineExpr;
import org.netbeans.modules.wlm.model.api.TDurationExpr;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.TimeoutNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class TimeoutPanel extends DesignViewPanel implements 
        Widget, FocusListener
{
    private DeadlineOrDuration deadlineOrDuration;
    
    private SetDeadlineAction setDeadlineAction;
    private SetDurationAction setDurationAction;
    
    private JRadioButton deadlineButton;
    private JRadioButton durationButton;
    private TimeoutEditor expression;

    private Widget widgetParent;
    
    public TimeoutPanel(Widget widgetParent, DesignView designView,
            DeadlineOrDuration deadlineOrDuration) 
    {
        super(designView);

        ExUtils.setA11Y(this, "TimeoutPanel"); // NOI18N

        this.widgetParent = widgetParent;
        this.deadlineOrDuration = deadlineOrDuration;
        
        setBorder(null);
        setBackground(TitledPanel.BACKGROUND_COLOR);
        setOpaque(true);
        
        setDeadlineAction = new SetDeadlineAction();
        setDurationAction = new SetDurationAction();
                
        deadlineButton = new JRadioButton(getMessage("RDB_DEADLINE")); // NOI18N
        deadlineButton.setOpaque(false);
        deadlineButton.addActionListener(setDeadlineAction);
        deadlineButton.addFocusListener(this);
        ExUtils.setA11Y(deadlineButton, TimeoutPanel.class, 
                "DeadlineRadioButton"); // NOI18N
        
        durationButton = new JRadioButton(getMessage("RDB_DURATION")); // NOI18N
        durationButton.setOpaque(false);
        durationButton.addActionListener(setDurationAction);
        durationButton.addFocusListener(this);
        ExUtils.setA11Y(durationButton, TimeoutPanel.class,
                "DurationRadioButton"); // NOI18N
        
        expression = new TimeoutEditor(designView);
        expression.addFocusListener(this);
        ExUtils.setA11Y(expression, TimeoutPanel.class,
                "TimeoutExpression"); // NOI18N
        
        add(deadlineButton);
        add(durationButton);
        add(expression);
        
        updateContent();
    }

    public void processWLMModelChanged() {
        updateContent();
        expression.updateContent();
    }
    
    private void updateContent() {
        TDurationExpr duration = deadlineOrDuration.getDuration();
        TDeadlineExpr deadline = deadlineOrDuration.getDeadline();
        
        if (duration != null) {
            durationButton.setSelected(true);
            deadlineButton.setSelected(false);
        } else if (deadline != null) {
            deadlineButton.setSelected(true);
            durationButton.setSelected(false);
        } else {
            durationButton.setSelected(true);
            deadlineButton.setSelected(false);
        }
    }
    
    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            Insets insets = getInsets();
            int x = insets.left;
            int y = insets.top;
            int w = getWidth() - x - insets.right;
            int h = getHeight() - y - insets.bottom;
            
            Dimension deadlineSize = deadlineButton.getPreferredSize();
            Dimension durationSize = durationButton.getPreferredSize();
            Dimension expressionSize = expression.getPreferredSize();

            durationButton.setBounds(x, y + (h - durationSize.height) / 2, 
                    durationSize.width, durationSize.height);
            x += durationSize.width + HGAP;
            w -= durationSize.width + HGAP;
            
            deadlineButton.setBounds(x, y + (h - deadlineSize.height) / 2, 
                    deadlineSize.width, deadlineSize.height);
            x += deadlineSize.width + HGAP;
            w -= deadlineSize.width + HGAP;

            expression.setBounds(x, y + (h - expressionSize.height) / 2, w, 
                    expressionSize.height);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            Dimension deadlineSize = deadlineButton.getPreferredSize();
            Dimension durationSize = durationButton.getPreferredSize();
            Dimension expressionSize = expression.getPreferredSize();
            
            Insets insets = getInsets();
            
            int w = insets.left + deadlineSize.width + HGAP 
                    + durationSize.width + HGAP + expressionSize.width 
                    + insets.right;
            
            int h = insets.top + ExUtils.maxHeight(deadlineSize, durationSize,
                    expressionSize) + insets.bottom;
            
            return new Dimension(w, h);
        }
    }

    @Override
    public Dimension getMaximumSize() {
        synchronized (getTreeLock()) {
            Dimension size = getPreferredSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }
    }

    private void updateTimeoutType() {
        String expr = expression.getXPath();
        
        WLMModel model = getModel();
        if (model.startTransaction()) {
            try {
                TDurationExpr duration = deadlineOrDuration.getDuration();
                TDeadlineExpr deadline = deadlineOrDuration.getDeadline();
                
                if (durationButton.isSelected()) {
                    if (deadline != null) {
                        deadlineOrDuration.removeDeadline(deadline);
                        deadline = null;
                    }
                    
                    if (duration == null) {
                        duration = model.getFactory().createDuration(model);
                        deadlineOrDuration.setDuration(duration);
                    }
                    
                    duration.setContent(expr);
                } else {
                    if (duration != null) {
                        deadlineOrDuration.removeDuration(duration);
                        duration = null;
                    } 
                    
                    if (deadline == null) {
                        deadline = model.getFactory().createDeadline(model);
                        deadlineOrDuration.setDeadline(deadline);
                    }
                    
                    deadline.setContent(expr);
                }
            } finally {
                model.endTransaction();
            }
        }
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public Widget getWidget(int index) {
        throw new IndexOutOfBoundsException("Leaf widget has no children");
    }

    public int getWidgetCount() {
        return 0;
    }

    public Node getWidgetNode() {
        return new TimeoutNode(deadlineOrDuration, Children.LEAF,
                getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showEscalationsTab();
        scrollRectToVisible(new Rectangle(getSize()));
        if (!deadlineButton.hasFocus() && !durationButton.hasFocus()) {
            expression.requestFocusInWindow();
        }
    }

    public WLMComponent getWidgetWLMComponent() {
        return deadlineOrDuration;
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.TIMEOUT;
    }

    public void focusGained(FocusEvent e) {
        selectWidget(this);
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }
    
    private class SetDeadlineAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            deadlineButton.setSelected(true);
            durationButton.setSelected(false);
            updateTimeoutType();
        }
    }

    private class SetDurationAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            deadlineButton.setSelected(false);
            durationButton.setSelected(true);
            updateTimeoutType();
        }
    }
    
    private class TimeoutEditor extends TextFieldEditor {
        TimeoutEditor(DesignView designView) {
            super(designView);
        }

        @Override
        public String getModelValue() {
            TDurationExpr duration = deadlineOrDuration.getDuration();
            TDeadlineExpr deadline = deadlineOrDuration.getDeadline();
            
            String expr = null;
            
            if (duration != null) {
                expr = duration.getContent();
            } else if (deadline != null) {
                expr = deadline.getContent();
            } 
            
            return expr;
        }

        @Override
        public void setModelValue(String value) {
            updateTimeoutType();
        }
    }
    
    private static final int HGAP = 4;
    private static final int VGAP = 2;
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(TimeoutPanel.class, key);
    }           
}
