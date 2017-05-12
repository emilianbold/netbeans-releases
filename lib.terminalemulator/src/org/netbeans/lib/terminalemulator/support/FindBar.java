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
package org.netbeans.lib.terminalemulator.support;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import static org.netbeans.lib.terminalemulator.support.FindState.FIND_HIGHLIGHT_SEARCH;
import static org.netbeans.lib.terminalemulator.support.FindState.FIND_SEARCH_BACKWARDS;

/**
 * A panel to facilitate text searches with the following elements: <ul <li>A
 * search pattern text entry field.
 * <li>A Prev search button.
 * <li>A Next search button.
 * <li>An error area.
 * <li>A close button.
 * </ul>
 * <p>
 * A FindBar doesn't do any searching by itself but acts as a controller of a
 * {@link FindState} which it can multiplex via {@link FindBar#setState}.
 *
 * @author ivan
 */
public final class FindBar extends JPanel {

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    private final Owner owner;
    private FindState state;

    private boolean updating = false;   // true while view is being updated

    private final JTextField findText;
    private final JLabel errorLabel;
    private final Color originalColor;
    private final JToggleButton highlightButton;
    private final JToggleButton searchBackwardsButton;

    private final Action closeAction = new FindBarAction("CTL_Close", "resources/find_close.png", this::close); //NOI18N
    private final Action nextAction = new FindBarAction("CTL_Next", "resources/find_next.png", this::next); //NOI18N
    private final Action prevAction = new FindBarAction("CTL_Previous", "resources/find_previous.png", this::prev); //NOI18N
    private final Action highlightAction = new FindBarAction("CTL_Highlight", "resources/highlight.png", this::toggleHighlight); //NOI18N
    private final Action searchBackwardsAction = new FindBarAction("CTL_Backwards", "resources/search_backwards.png", this::searchBackwards); //NOI18N

    /**
     * Callback interface used to communicate to the owner of a {@link FindBar}
     * that it's close button was pressed.
     */
    public interface Owner {

        public void close(FindBar who);

        default JButton createCloseButton(Action closeAction) {
            return new JButton(closeAction);
        }
    }

    private class FindBarAction extends AbstractAction {

        private final Runnable action;

        public FindBarAction(String ctlName, String iconPath, Runnable action) {
            super(Catalog.get(ctlName), new ImageIcon(FindBar.class.getResource(iconPath)));
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            action.run();
        }
    }

    /**
     * Construct a FindBar.
     *
     * @param owner Is used to call {@link Owner#close()} when the close button
     * is pressed.
     */
    public FindBar(Owner owner) {
        super();
        this.owner = owner;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        JLabel findLabel = new JLabel();
        findLabel.setText(Catalog.get("LBL_Find") + ":");// NOI18N
        findText = new JTextField() {

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, super.getPreferredSize().height);
            }
        };
        originalColor = findText.getForeground();

        findText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!updating) {
                    state.setPattern(findText.getText());
                    error(state.getStatus(), false);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });

        findLabel.setLabelFor(findText);
        JButton prevButton = new JButton(prevAction);
        adjustButton(prevButton);
        JButton nextButton = new JButton(nextAction);
        adjustButton(nextButton);
        JButton closeButton = owner.createCloseButton(closeAction);
        adjustButtonLook(closeButton);

        JToolBar.Separator leftSeparator = new JToolBar.Separator();
        leftSeparator.setOrientation(SwingConstants.VERTICAL);

        highlightButton = new JToggleButton(highlightAction);
        highlightButton.setText(null);
        highlightButton.setToolTipText(Catalog.get("TOOLTIP_Highlight")); // NOI18N
        adjustButton(highlightButton);

        searchBackwardsButton = new JToggleButton(searchBackwardsAction);
        searchBackwardsButton.setText(null);
        searchBackwardsButton.setToolTipText(Catalog.get("TOOLTIP_Backwards")); // NOI18N
        adjustButton(searchBackwardsButton);

        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK), getName(prevAction));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), getName(nextAction));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), getName(closeAction));

        ActionMap actionMap = getActionMap();
        actionMap.put(prevAction.getValue(Action.NAME), prevAction);
        actionMap.put(nextAction.getValue(Action.NAME), nextAction);
        actionMap.put(closeAction.getValue(Action.NAME), closeAction);

        findText.getActionMap().put(nextAction.getValue(Action.NAME), nextAction);
        findText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), getName(nextAction));

        findText.getActionMap().put(prevAction.getValue(Action.NAME), prevAction);
        findText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), getName(prevAction));

        errorLabel = new JLabel();

        add(Box.createRigidArea(new Dimension(5, 0)));
        add(findLabel);
        add(findText);
        add(prevButton);
        add(nextButton);
        add(leftSeparator);
        add(highlightButton);
        add(searchBackwardsButton);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(errorLabel);
        add(Box.createHorizontalGlue());
        add(closeButton);
    }

    private static String getName(Action a) {
        Object name = a.getValue(Action.NAME);
        if (name instanceof String) {
            return (String) name;
        } else {
            return "null"; // NOI18N
        }
    }

    /**
     * Set the FindState for this panel.
     *
     * @param state the FindState.
     */
    public void setState(FindState state) {
        this.state = state;

        // Adjust the view to reflect the model
        updating = true;
        try {
            if (state != null) {
                findText.setText(state.getPattern());

                Object highlightProperty = state.getProperty(FIND_HIGHLIGHT_SEARCH);
                Boolean highlight = highlightProperty instanceof Boolean && (Boolean) highlightProperty;
                state.putProperty(FIND_HIGHLIGHT_SEARCH, highlight);
                highlightButton.setSelected(highlight);
                
                Object backwardsProperty = state.getProperty(FIND_SEARCH_BACKWARDS);
                Boolean backwards = backwardsProperty instanceof Boolean && (Boolean) backwardsProperty;
                state.putProperty(FIND_SEARCH_BACKWARDS, backwards);
                searchBackwardsButton.setSelected(backwards);

                error(state.getStatus(), false);
            } else {
                findText.setText("");
                error(FindState.Status.OK, false);
            }
        } finally {
            updating = false;
        }
    }

    public void requestTextFocus() {
        findText.requestFocus();
    }

    /**
     * Get the FindState for this panel.
     *
     * @return the FindState.
     */
    public FindState getState() {
        return state;
    }

    private void error(FindState.Status status, boolean prevNext) {
        switch (status) {
            case OK:
                errorLabel.setText("");
                findText.setForeground(originalColor);
                break;
            case NOTFOUND:
                errorLabel.setText(Catalog.get("MSG_NotFound"));// NOI18N
                findText.setForeground(FontPanel.ERROR_COLOR);
                break;
            case WILLWRAP:
                errorLabel.setText(Catalog.get("MSG_OneMore"));	// NOI18N
                findText.setForeground(originalColor);
                break;
            case EMPTYPATTERN:
                if (prevNext) {
                    errorLabel.setText(Catalog.get("MSG_Empty"));// NOI18N
                } else {
                    errorLabel.setText("");
                }
                findText.setForeground(originalColor);
                break;
        }
    }

    private void close() {
        owner.close(this);
    }

    private void next() {
        if (state != null) {
            state.next();
            error(state.getStatus(), true);
        }
    }

    private void prev() {
        if (state != null) {
            state.prev();
            error(state.getStatus(), true);
        }
    }

    private void toggleHighlight() {
        if (state != null) {
            boolean isSelected = highlightButton.isSelected();
            state.putProperty(FIND_HIGHLIGHT_SEARCH, isSelected);
        }
    }

    private void searchBackwards() {
        if (state != null) {
            boolean isSelected = searchBackwardsButton.isSelected();
            state.putProperty(FIND_SEARCH_BACKWARDS, isSelected);
        }
    }

    /*
     * We're a panel so do our own toolbar-style fly-over hiliting of buttons.
     * Why not be a toolbar?
     * Because of it's graded background which we don't want.
     */
    private void adjustButton(final AbstractButton button) {
        adjustButtonLook(button);
        if (button instanceof JToggleButton) {
            button.addChangeListener((ChangeEvent e) -> {
                updateButtonLook(button, button.isSelected());
            });
        }
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    updateButtonLook(button, true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button instanceof JToggleButton && button.isSelected()) {
                    return;
                }
                updateButtonLook(button, false);
            }
        });
    }

    private void adjustButtonLook(final AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        button.setMargin(BUTTON_INSETS);
        button.setFocusable(false);
    }

    private void updateButtonLook(AbstractButton button, boolean selected) {
        button.setContentAreaFilled(selected);
        button.setBorderPainted(selected);
    }
}
