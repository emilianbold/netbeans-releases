/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.projects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.netbeans.api.debugger.jpda.ObjectVariable;

import org.openide.util.ImageUtilities;


// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class CallStackView extends TopComponent {
// ====
public class ToolTipView extends JComponent implements org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    public static final String TOOLTIP_VIEW_NAME = "ToolTipView";

    private static volatile String expression;
    private static volatile ObjectVariable variable;

    private transient JComponent contentComponent;
    private transient ViewModelListener viewModelListener;
    private String name; // Store just the name persistently, we'll create the component from that
    
    private ToolTipView(String expression, ObjectVariable v, String icon) {
        ToolTipView.expression = expression;
        variable = v;
        this.name = TOOLTIP_VIEW_NAME;
        componentShowing(icon);
    }

    static String getExpression() {
        return expression;
    }

    static ObjectVariable getVariable() {
        return variable;
    }

    private void componentShowing (String icon) {
        if (viewModelListener != null) {
            viewModelListener.setUp();
            return ;
        }
        JComponent buttonsPane;
        if (contentComponent == null) {
            setLayout (new BorderLayout ());
            contentComponent = new javax.swing.JPanel(new BorderLayout ());
            
            //tree = Models.createView (Models.EMPTY_MODEL);
            add (contentComponent, BorderLayout.CENTER);  //NOI18N
            JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.setBorderPainted(true);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
            toolBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                    javax.swing.UIManager.getDefaults().getColor("Separator.background")),
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                    javax.swing.UIManager.getDefaults().getColor("Separator.foreground"))));
            toolBar.setPreferredSize(new Dimension(26, 10));
            add(toolBar, BorderLayout.WEST);
            buttonsPane = toolBar;
        } else {
            buttonsPane = (JComponent) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.WEST);
        }
        // <RAVE> CR 6207738 - fix debugger help IDs
        // Use the modified constructor that stores the propertiesHelpID
        // for nodes in this view
        // viewModelListener = new ViewModelListener (
        //     "ThreadsView",
        //     tree
        // );
        // ====
        viewModelListener = new ViewModelListener (
            name,
            contentComponent,
            buttonsPane,
            null,
            ImageUtilities.loadImage (icon),
            variable
        );
        // </RAVE>
    }
    
    //protected void componentHidden () {
    @Override
    public void removeNotify() {
        super.removeNotify();//componentHidden ();
        variable = null;
        if (viewModelListener != null) {
            viewModelListener.destroy ();
        }
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct help ID
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerJPDAToolTipNode");
    }
    // </RAVE>
    
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (contentComponent == null) return false;
        return contentComponent.requestFocusInWindow ();
    }

    /*
    public void requestActive() {
        super.requestActive();
        if (contentComponent != null) {
            contentComponent.requestFocusInWindow ();
        }
    }
     */

    /*
    public String getName () {
        return NbBundle.getMessage (ToolTipView.class, displayNameResource);
    }
    
    public String getToolTipText () {
        return NbBundle.getMessage (ToolTipView.class, toolTipResource);// NOI18N
    }
     */
    
    
    /** Creates the view. */
    public static synchronized JComponent getToolTipView(String expression, ObjectVariable v) {
        return new ToolTipView(
                expression,
                v,
                "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"
        );
    }
    
    
    static ExpandableTooltip createExpandableTooltip(String toolTipText) {
        return new ExpandableTooltip(toolTipText);
    }

    static class ExpandableTooltip extends JPanel {

        private static final String UI_PREFIX = "ToolTip"; // NOI18N
        
        private JButton expButton;
        private boolean widthCheck = true;

        public ExpandableTooltip(String toolTipText) {
            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
            Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
            Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

            if (font != null) {
                setFont(font);
            }
            if (foreColor != null) {
                setForeground(foreColor);
            }
            if (backColor != null) {
                setBackground(backColor);
            }
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getForeground()),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)
            ));

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            Icon expIcon = UIManager.getIcon ("Tree.collapsedIcon");    // NOI18N
            expButton = new JButton(expIcon);
            expButton.setBorder(null);
            expButton.setBorderPainted(false);
            expButton.setContentAreaFilled(false);
            add(expButton);
            JLabel l = new JLabel(toolTipText);
            add(l);
        }

        void addExpansionListener(ActionListener treeExpansionListener) {
            expButton.addActionListener(treeExpansionListener);
        }

        void setWidthCheck(boolean widthCheck) {
            this.widthCheck = widthCheck;
        }

        @Override
        public void setSize(int width, int height) {
            Dimension prefSize = getPreferredSize();
            if (widthCheck) {
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    super.setSize(width, 10000); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
            }
            if (height >= prefSize.height) { // enough height
                height = prefSize.height;
            }
            super.setSize(width, height);
        }

    }

}
