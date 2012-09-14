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

package org.netbeans.modules.web.javascript.debugger.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Keymap;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel.ScopedRemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ui.ViewFactory;

// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class CallStackView extends TopComponent {
// ====
public class ToolTipView extends JComponent implements org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    public static final String TOOLTIP_VIEW_NAME = "ToolTipView";

    private static volatile String expression;
    private static volatile ScopedRemoteObject variable;

    private transient JComponent contentComponent;
    private Debugger debugger;
    private Debugger.Listener debuggerStateChangeListener;
    private ToolTipSupport toolTipSupport;
    private String name; // Store just the name persistently, we'll create the component from that
    
    private ToolTipView(Debugger debugger, String expression, ScopedRemoteObject v, String icon) {
        this.debugger = debugger;
        ToolTipView.expression = expression;
        variable = v;
        this.name = TOOLTIP_VIEW_NAME;
        JComponent c = ViewFactory.getDefault().createViewComponent(
                icon,
                ToolTipView.TOOLTIP_VIEW_NAME,
                "NetbeansDebuggerJSToolTipNode",
                null);
        setLayout (new BorderLayout ());
        add (c, BorderLayout.CENTER);  //NOI18N
        debuggerStateChangeListener = new DebuggerStateChangeListener();
        debugger.addListener(debuggerStateChangeListener);
    }

    static String getExpression() {
        return expression;
    }

    static ScopedRemoteObject getVariable() {
        return variable;
    }

    void setToolTipSupport(ToolTipSupport toolTipSupport) {
        this.toolTipSupport = toolTipSupport;
    }
    
    private void closeToolTip() {
        toolTipSupport.setToolTipVisible(false);
    }
    
    //protected void componentHidden () {
    @Override
    public void removeNotify() {
        super.removeNotify();//componentHidden ();
        variable = null;
        debugger.removeListener(debuggerStateChangeListener);
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct help ID
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerJSToolTipNode");
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
    public static synchronized ToolTipView getToolTipView(Debugger d, String expression, ScopedRemoteObject v) {
        return new ToolTipView(
                d,
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
        private JComponent textToolTip;
        private boolean widthCheck = true;
        private boolean sizeSet = false;

        public ExpandableTooltip(String toolTipText) {
            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
            Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
            Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

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
            expButton.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 5));
            expButton.setBorderPainted(false);
            expButton.setContentAreaFilled(false);
            add(expButton);
            //JLabel l = new JLabel(toolTipText);
            // Multi-line tooltip:
            JTextArea l = createMultiLineToolTip(toolTipText, true);
            if (font != null) {
                l.setFont(font);
            }
            if (foreColor != null) {
                l.setForeground(foreColor);
            }
            if (backColor != null) {
                l.setBackground(backColor);
            }
            textToolTip = l;
            add(l);
        }

        void addExpansionListener(ActionListener treeExpansionListener) {
            expButton.addActionListener(treeExpansionListener);
        }

        void setWidthCheck(boolean widthCheck) {
            this.widthCheck = widthCheck;
        }

        @Override
        public Dimension getPreferredSize() {
            if (!sizeSet) {
                // Be big enough initially.
                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
            return super.getPreferredSize();
        }
        
        @Override
        public void setSize(int width, int height) {
            Dimension prefSize = getPreferredSize();
            Dimension buttonSize = expButton.getPreferredSize();
            if (widthCheck) {
                Insets insets = getInsets();
                int textWidth = width - insets.left - buttonSize.width - insets.right;
                height = Math.max(height, buttonSize.height);
                textToolTip.setSize(textWidth, height);
                Dimension textPreferredSize = textToolTip.getPreferredSize();
                super.setSize(
                        insets.left + buttonSize.width + textPreferredSize.width + insets.right,
                        insets.top + Math.max(buttonSize.height, textPreferredSize.height) + insets.bottom);
            } else {
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                }
                super.setSize(width, height);
            }
            sizeSet = true;
        }
        
        private static JTextArea createMultiLineToolTip(String toolTipText, boolean wrapLines) {
            JTextArea ta = new TextToolTip(wrapLines);
            ta.setText(toolTipText);
            return ta;
        }

        private static class TextToolTip extends JTextArea {
            
            private static final String ELIPSIS = "..."; //NOI18N
            
            private final boolean wrapLines;
            
            public TextToolTip(boolean wrapLines) {
                this.wrapLines = wrapLines;
                setLineWrap(false); // It's necessary to have a big width of preferred size first.
            }
            
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    // Set line wrapping and do super.setSize() to determine
                    // the real height (it will change due to line wrapping)
                    if (wrapLines) {
                        setLineWrap(true);
                        setWrapStyleWord(true);
                    }
                    
                    super.setSize(width, Integer.MAX_VALUE); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                } else { // smaller available height
                    // Check how much can be displayed - cannot rely on line count
                    // because line wrapping may display single physical line
                    // into several visual lines
                    // Before using viewToModel() a setSize() must be called
                    // because otherwise the viewToModel() would return -1.
                    super.setSize(width, Integer.MAX_VALUE);
                    int offset = viewToModel(new Point(0, height));
                    Document doc = getDocument();
                    try {
                        if (offset > ELIPSIS.length()) {
                            offset -= ELIPSIS.length();
                            doc.remove(offset, doc.getLength() - offset);
                            doc.insertString(offset, ELIPSIS, null);
                        }
                    } catch (BadLocationException ble) {
                        // "..." will likely not be displayed but otherwise should be ok
                    }
                    // Recalculate the prefSize as it may be smaller
                    // than the present preferred height
                    height = Math.min(height, getPreferredSize().height);
                }
                super.setSize(width, height);
            }
            
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other JTextAreas
                super.setKeymap(addKeymap(null, map));
            }
        }
    }
    
    private class DebuggerStateChangeListener implements Debugger.Listener {

        @Override
        public void paused(List<CallFrame> callStack, String reason) {}

        @Override
        public void resumed() {
            closeToolTip();
        }

        @Override
        public void reset() {
            closeToolTip();
        }
        
    }

}
