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

package org.netbeans.modules.quicksearch;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Quick search toolbar component
 * @author  Jan Becicka
 */
public abstract class AbstractQuickSearchComboBar extends javax.swing.JPanel implements ActionListener {

    private static final String CATEGORY = "cat";

    QuickSearchPopup displayer = new QuickSearchPopup(this);
    WeakReference<TopComponent> caller;

    Color origForeground;
    protected final KeyStroke keyStroke;

    protected JTextComponent command;

    public AbstractQuickSearchComboBar(KeyStroke ks) {
        keyStroke = ks;

        initComponents();

        setShowHint(true);

        command.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                textChanged();
            }

            public void removeUpdate(DocumentEvent arg0) {
                textChanged();
            }

            public void changedUpdate(DocumentEvent arg0) {
                textChanged();
            }

            private void textChanged () {
                if (command.isFocusOwner()) {
                    displayer.maybeEvaluate(command.getText());
                }
            }

        });
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    protected abstract JTextComponent createCommandField();

    protected abstract JComponent getInnerComponent();

    private void initComponents() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setName("Form"); // NOI18N
        setOpaque(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        command = createCommandField();
        command.setToolTipText(org.openide.util.NbBundle.getMessage(AbstractQuickSearchComboBar.class, "AbstractQuickSearchComboBar.command.toolTipText", new Object[] {"(" + SearchResultRender.getKeyStrokeAsText(keyStroke) + ")"})); // NOI18N
        command.setName("command"); // NOI18N
        command.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                commandFocusGained(evt);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                commandFocusLost(evt);
            }
        });
        command.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                commandKeyPressed(evt);
            }
        });
        command.addMouseListener(new MouseAdapter() {
            public @Override void mouseClicked(MouseEvent e) {
                displayer.explicitlyInvoked();
            }
        });
    }

    private void formFocusLost(java.awt.event.FocusEvent evt) {
        displayer.setVisible(false);
    }

    private void commandKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
            displayer.selectNext();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            displayer.selectPrev();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            invokeSelectedItem();
        } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
            returnFocus(true);
            displayer.clearModel();
        } else if (evt.getKeyCode() == KeyEvent.VK_F10 &&
                evt.isShiftDown()) {
            maybeShowPopup(null);
        }
    }

    /** Actually invokes action selected in the results list */
    public void invokeSelectedItem () {
        JList list = displayer.getList();
        ResultsModel.ItemResult ir = (ItemResult) list.getSelectedValue();

        // special handling of invocation of "more results item" (three dots)
        if (ir != null) {
            Runnable action = ir.getAction();
            if (action instanceof CategoryResult) {
                CategoryResult cr = (CategoryResult)action;
                evaluateCategory(cr.getCategory(), true);
                return;
            }
        }

        // #137259: invoke only some results were found
        if (list.getModel().getSize() > 0) {
            returnFocus(false);
            // #137342: run action later to let focus indeed be transferred
            // by previous returnFocus() call
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        displayer.invoke();
                    }
            });
        }
    }

    private void returnFocus (boolean force) {
        displayer.setVisible(false);
        if (caller != null) {
            TopComponent tc = caller.get();
            if (tc != null) {
                tc.requestActive();
                tc.requestFocus();
                return;
            }
        }
        if (force) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        }
    }


    private void commandFocusLost(java.awt.event.FocusEvent evt) {
        displayer.setVisible(false);
        setShowHint(true);
    }

    private void commandFocusGained(java.awt.event.FocusEvent evt) {
        caller = new WeakReference<TopComponent>(TopComponent.getRegistry().getActivated());
        setShowHint(false);
        if (CommandEvaluator.isCatTemporary()) {
            CommandEvaluator.setCatTemporary(false);
            CommandEvaluator.setEvalCat(null);
        }
    }

    protected void maybeShowPopup (MouseEvent evt) {
        if (evt != null && !SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }

        JPopupMenu pm = new JPopupMenu();
        ProviderModel.Category evalCat = null;
        if (!CommandEvaluator.isCatTemporary()) {
            evalCat = CommandEvaluator.getEvalCat();
        }

        JRadioButtonMenuItem allCats = new JRadioButtonMenuItem(
                NbBundle.getMessage(getClass(), "LBL_AllCategories"), evalCat == null);
        allCats.addActionListener(this);
        pm.add(allCats);

        for (ProviderModel.Category cat : ProviderModel.getInstance().getCategories()) {
            if (!CommandEvaluator.RECENT.equals(cat.getName())) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(cat.getDisplayName(), cat == evalCat);
                item.putClientProperty(CATEGORY, cat);
                item.addActionListener(this);
                pm.add(item);
            }
        }

        pm.show(getInnerComponent(), 0, getInnerComponent().getHeight() - 1);
    }

    /** ActionListener implementation, reaction to popup menu item invocation */
    public void actionPerformed(ActionEvent e) {
        JRadioButtonMenuItem item = (JRadioButtonMenuItem)e.getSource();
        CommandEvaluator.setEvalCat((Category) item.getClientProperty(CATEGORY));
        CommandEvaluator.setCatTemporary(false);
        // refresh hint
        setShowHint(!command.isFocusOwner());
    }

    /** Runs evaluation narrowed to specified category
     *
     */
    public void evaluateCategory (Category cat, boolean temporary) {
        CommandEvaluator.setEvalCat(cat);
        CommandEvaluator.setCatTemporary(temporary);
        displayer.maybeEvaluate(command.getText());
    }

    public void setNoResults (boolean areNoResults) {
        // no op when called too soon
        if (command == null || origForeground == null) {
            return;
        }
        // don't alter color if showing hint already
        if (command.getForeground().equals(command.getDisabledTextColor())) {
            return;
        }
        command.setForeground(areNoResults ? Color.RED : origForeground);
    }

    private void setShowHint (boolean showHint) {
        // remember orig color on first invocation
        if (origForeground == null) {
            origForeground = command.getForeground();
        }
        if (showHint) {
            command.setForeground(command.getDisabledTextColor());
            Category evalCat = CommandEvaluator.getEvalCat();
            if (evalCat != null && !CommandEvaluator.isCatTemporary()) {
                command.setText(getHintText(evalCat));
            } else {
                command.setText(getHintText(null));
            }
        } else {
            command.setForeground(origForeground);
            command.setText("");
        }
    }

    private String getHintText (Category cat) {
        StringBuilder sb = new StringBuilder();
        if (cat != null) {
            sb.append(NbBundle.getMessage(AbstractQuickSearchComboBar.class,
                    "MSG_DiscoverabilityHint2", cat.getDisplayName())); //NOI18N
        } else {
            sb.append(NbBundle.getMessage(AbstractQuickSearchComboBar.class, "MSG_DiscoverabilityHint")); //NOI18N
        }
        sb.append(" (");
        sb.append(SearchResultRender.getKeyStrokeAsText(keyStroke));
        sb.append(")");

        return sb.toString();
    }


    @Override
    public void requestFocus() {
        super.requestFocus();
        command.requestFocus();
    }

    public JTextComponent getCommand() {
        return command;
    }

    public int getBottomLineY () {
        return getInnerComponent().getY() + getInnerComponent().getHeight();
    }

    static Color getComboBorderColor () {
        Color shadow = UIManager.getColor(
                Utilities.isWindows() ? "Nb.ScrollPane.Border.color" : "TextField.shadow");
        return shadow != null ? shadow : getPopupBorderColor();
    }

    static Color getPopupBorderColor () {
        Color shadow = UIManager.getColor("controlShadow");
        return shadow != null ? shadow : Color.GRAY;
    }

    static Color getTextBackground () {
        Color textB = UIManager.getColor("TextPane.background");
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            textB = UIManager.getColor("NbExplorerView.background"); //NOI18N
        return textB != null ? textB : Color.WHITE;
    }

    static Color getResultBackground () {
        return getTextBackground();
    }

    static Color getCategoryTextColor () {
        Color shadow = UIManager.getColor("textInactiveText");
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
            shadow = UIManager.getColor("Table.foreground");
        return shadow != null ? shadow : Color.DARK_GRAY;
    }

    protected int computePrefWidth () {
        FontMetrics fm = command.getFontMetrics(command.getFont());
        ProviderModel pModel = ProviderModel.getInstance();
        int maxWidth = 0;
        for (Category cat : pModel.getCategories()) {
            // skip recent category
            if (CommandEvaluator.RECENT.equals(cat.getName())) {
                continue;
            }
            maxWidth = Math.max(maxWidth, fm.stringWidth(getHintText(cat)));
        }
        // don't allow width grow too much
        return Math.min(350, maxWidth);
    }
}
