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

package org.netbeans.modules.kenai.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.server.ui.common.AddInstanceAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author  Jan Becicka
 */
public class SearchField extends JPanel implements ActionListener {

    private Kenai selected;
    private static final String KENAI = "kenai"; //NOI18N
    static final String SEARCH = "search"; //NOI18N
    private JTextComponent command = createCommandField();
    private javax.swing.JLabel leftIcon;
    private javax.swing.JPanel panel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JSeparator separator;


    public SearchField() {
        super();
        initComponents();
    }

    public SearchField(Kenai kenai) {
        super();
        selected=kenai;
        initComponents();
        setTooltip();
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panel = new javax.swing.JPanel();
        leftIcon = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        separator = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        panel.setBackground(getTextBackground());
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(getComboBorderColor()));
        panel.setLayout(new java.awt.GridBagLayout());

        leftIcon.setIcon(selected!=null?selected.getIcon():ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/kenai-small.png", true));//NOI18N
        leftIcon.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        leftIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                leftIconMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 1);
        panel.add(leftIcon, gridBagConstraints);

        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setViewportBorder(null);
        scrollPane.setMinimumSize(new java.awt.Dimension(2, 18));

        scrollPane.setViewportView(command);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        panel.add(scrollPane, gridBagConstraints);

        separator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        panel.add(separator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panel, gridBagConstraints);
    }

    public String getText() {
        return command.getText();
    }

    public void setText(String text) {
        command.setText(text);
    }

    private void leftIconMousePressed(java.awt.event.MouseEvent evt) {
        maybeShowPopup(evt);
    }


    protected JTextComponent createCommandField() {
        JTextArea res = new JTextArea();
        res.getAccessibleContext().setAccessibleName(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchLabel.text"));
        res.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchLabel.AccessibleContext.accessibleDescription"));
        res.setRows(1);
        res.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        // disable default Swing's Ctrl+Shift+O binding to enable our global action
        InputMap curIm = res.getInputMap(JComponent.WHEN_FOCUSED);
        while (curIm != null) {
            curIm.remove(KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            curIm = curIm.getParent();
        }
        res.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    SearchField.this.actionPerformed(new ActionEvent(e.getSource(), e.getID(), SEARCH, e.getWhen(), e.getModifiers()));
                    e.consume();
                }
            }

        });
        return res;
    }

    private static Color getTextBackground () {
        Color textB = UIManager.getColor("TextPane.background"); //NOI18N
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            textB = UIManager.getColor("NbExplorerView.background"); //NOI18N
        return textB != null ? textB : Color.WHITE;
    }

    private static Color getComboBorderColor () {
        Color shadow = UIManager.getColor(
                Utilities.isWindows() ? "Nb.ScrollPane.Border.color" : "TextField.shadow"); //NOI18N
        return shadow != null ? shadow : getPopupBorderColor();
    }

    private static Color getPopupBorderColor () {
        Color shadow = UIManager.getColor("controlShadow"); //NOI18N
        return shadow != null ? shadow : Color.GRAY;
    }


    protected void maybeShowPopup (MouseEvent evt) {
        if (evt != null && !SwingUtilities.isLeftMouseButton(evt)) {
            return;
        }

        JPopupMenu pm = new JPopupMenu();

        for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(getKenaiDisplayName(kenai));
            item.setIcon(kenai.getIcon());
            item.setSelected(kenai==getSelectedKenai());
            item.putClientProperty(KENAI, kenai);
            item.addActionListener(this);
            pm.add(item);
        }

        JMenuItem item = new JMenuItem(NbBundle.getMessage(SearchField.class, "CTL_AddNew"));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddInstanceAction addInstanceAction = new AddInstanceAction();
                addInstanceAction.actionPerformed(e);
                Kenai last = ((KenaiServer) addInstanceAction.getTeamServer()).getKenai();
                if (last!=null) {
                    selected = last;
                    setTooltip();
                }
                SearchField.this.actionPerformed(e);
            };
        });
        pm.add(item);


        pm.show(panel, 0, panel.getHeight() - 1);
    }

    private void setTooltip() {
        leftIcon.setToolTipText(getKenaiDisplayName(selected)); // NOI18N
    }

    private static String getKenaiDisplayName(Kenai kenai) {
        if (kenai==null) {
            return ""; //NOI18N
        }
        return "<html><b>" + kenai.getName() + "</b> (" //NOI18N
                + kenai.getUrl().getProtocol() + "://" //NOI18N
                + kenai.getUrl().getHost() + ")</html>"; //NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        JComponent source = (JComponent) e.getSource();
        Object k = source.getClientProperty(KENAI);
        if (k!=null) {
            selected = (Kenai) k;
            setTooltip();
            leftIcon.setIcon(selected.getIcon());
        }
        for (ActionListener l:listenerList.getListeners(ActionListener.class)) {
            l.actionPerformed(e);
        }
    }

    /**
     * Adds the specified action listener to receive
     * action events from this textfield.
     *
     * @param l the action listener to be added
     */
    public synchronized void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes the specified action listener so that it no longer
     * receives action events from this textfield.
     *
     * @param l the action listener to be removed
     */
    public synchronized void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    public Kenai getSelectedKenai() {
        return selected;
    }

}
