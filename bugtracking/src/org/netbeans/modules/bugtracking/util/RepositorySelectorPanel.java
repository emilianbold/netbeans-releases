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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static java.awt.Component.LEFT_ALIGNMENT;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.NORTH;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.SwingConstants.WEST;
import static org.jdesktop.layout.LayoutStyle.RELATED;
import static org.jdesktop.layout.LayoutStyle.UNRELATED;

/**
 * Allows the user to select an existing connection to a bug-tracking repository
 * or to create a new connection.
 *
 * @author Marian Petras
 */
final class RepositorySelectorPanel extends JPanel implements ItemListener {

    private static final String EMPTY_PANEL = "empty panel";            //NOI18N

    private final Repository[] existingRepositories;
    private final BugtrackingConnector[] connectors;

    private final JComboBox combo;
    private final JLabel lblSelect;

    private JComponent cardsPanel;
    private JComponent emptyConnectorPanel;
    private JComponent message;
    private JComponent[] connectorPanels;
    private Repository[] newRepositories;

    private Repository currentRepository;

    private Dimension requestedSize;

    /**
     * 
     * @param existingRepositories
     * @param connectors
     * @param repoToPreselect  repository to be pre-selected, or {@code null}
     */
    RepositorySelectorPanel(Repository[] existingRepositories,
                            BugtrackingConnector[] connectors,
                            Repository repoToPreselect) {

        this.existingRepositories = existingRepositories;
        this.connectors = connectors;

        lblSelect = new JLabel();
        combo = new JComboBox(getComboPopupDisplayNames());

        if (repoToPreselect != null) {
            int indexToPreselect = getIndexOf(repoToPreselect);
            if (indexToPreselect >= 0) {
                combo.setSelectedIndex(indexToPreselect);
            }
        }
        initComponents();
        itemSelected(combo.getSelectedIndex());
    }

    private int getIndexOf(Repository repoToPreselect) {
        if (repoToPreselect == null) {
            return -1;
        }
        for (int i = 0; i < existingRepositories.length; i++) {
            if (existingRepositories[i] == repoToPreselect) {
                return i;
            }
        }
        return -1;
    }

    Repository getSelectedRepository() {
        return currentRepository;
    }

    boolean validateValues() {
        boolean valid = isNewConnectorSelected()
                                ? currentRepository.getController().isValid()
                                : true;
        if (!valid) {
            displayInvalidValuesMsg();
        }
        return valid;
    }

    private void displayInvalidValuesMsg() {
        setMessageVisible(true);

        class Fader implements Runnable {
            public void run() {
                if (EventQueue.isDispatchThread()) {
                    setMessageVisible(false);
                } else {
                    EventQueue.invokeLater(this);
                }
            }
        }

        RequestProcessor.getDefault().post(new Fader(), 4000);
    }

    private void initComponents() {
        Mnemonics.setLocalizedText(
                lblSelect, getText("LBL_SelectBugtrackingRepository")); //NOI18N

        combo.setEditable(false);
        combo.addItemListener(this);

        setLayout(new BoxLayout(this, Y_AXIS));

        add(lblSelect);
        add(createVerticalStrut(lblSelect, combo, RELATED));
        add(combo);

        lblSelect.setLabelFor(combo);

        lblSelect.setAlignmentX(LEFT_ALIGNMENT);
        combo.setAlignmentX(LEFT_ALIGNMENT);

        addInsets();
    }

    private void addInsets() {
        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(this, NORTH, null),
                layoutStyle.getContainerGap(this, WEST,  null),
                layoutStyle.getContainerGap(this, SOUTH, null),
                layoutStyle.getContainerGap(this, EAST,  null)));
    }

    public void itemStateChanged(ItemEvent e) {
        itemSelected(combo.getSelectedIndex());
    }

    private void itemSelected(int index) {
        assert (index != -1);

        boolean connectorUsedFirstTime;

        if (isNewConnectorSelected(index)) {
            int connectorIndex = getNewConnectorIndex(index);
            connectorUsedFirstTime = displayConnectorForm(connectorIndex);
            currentRepository = newRepositories[connectorIndex];
        } else {
            if (cardsPanel != null) {
                displayEmptyPanel();
            }
            connectorUsedFirstTime = false;
            currentRepository = existingRepositories[index];
        }
        setMessageVisible(false);

        if (connectorUsedFirstTime) {
            expandDialogToFitNewConnectorForm();
        }
    }

    private void expandDialogToFitNewConnectorForm() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window == null) {
            return;
        }

        Dimension currSize = getSize();
        Dimension prefSize = getPreferredSize();
        if ((currSize.width >= prefSize.width) && (currSize.height >= prefSize.height)) {
            /* the dialog is large enough to fit the form */
            return;
        }

        try {
            requestedSize = new Dimension(
                                    Math.max(currSize.width, prefSize.width),
                                    Math.max(currSize.height, prefSize.height));
            window.pack();
        } finally {
            requestedSize = null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return (requestedSize != null) ? requestedSize
                                       : super.getPreferredSize();
    }

    private boolean isNewConnectorSelected() {
        return isNewConnectorSelected(combo.getSelectedIndex());
    }

    private boolean isNewConnectorSelected(int selectedItemIndex) {
        return (selectedItemIndex >= existingRepositories.length);
    }

    private int getNewConnectorIndex(int selectedItemIndex) {
        return selectedItemIndex - existingRepositories.length;
    }

    private boolean displayConnectorForm(int connectorIndex) {
        prepareCardsPanel();
        return displayConnectorPanel(connectorIndex);
    }

    private void displayEmptyPanel() {
        if (emptyConnectorPanel == null) {
            emptyConnectorPanel = new JPanel();
            cardsPanel.add(emptyConnectorPanel, EMPTY_PANEL);
        }
        ((CardLayout) cardsPanel.getLayout()).show(cardsPanel, EMPTY_PANEL);
    }

    private boolean displayConnectorPanel(int connectorIndex) {
        boolean justCreated = prepareConnectorPanel(connectorIndex);
        ((CardLayout) cardsPanel.getLayout()).show(cardsPanel, String.valueOf(connectorIndex));

        return justCreated;
    }

    /**
     * Creates a connector panel for the given connector, if it does not exist
     * yet. If the panel already exists, this method does nothing.
     * 
     * @param connectorIndex  index of the connector for which a panel is needed
     * @return  {@code true} if the panel was just created,
     *          {@code false} if the panel had already existed
     */
    private boolean prepareConnectorPanel(int connectorIndex) {
        assert (connectorPanels == null) == (newRepositories == null);

        if (connectorPanels == null) {
            connectorPanels = new JComponent[connectors.length];
            newRepositories = new Repository[connectors.length];
        }

        assert (connectorPanels[connectorIndex] == null) == (newRepositories[connectorIndex] == null);

        if (connectorPanels[connectorIndex] == null) {
            BugtrackingConnector connector = connectors[connectorIndex];

            Repository repository = connector.createRepository();
            newRepositories[connectorIndex] = repository;

            JComponent panel = repository.getController().getComponent();
            connectorPanels[connectorIndex] = panel;

            cardsPanel.add(panel, String.valueOf(connectorIndex));
            return true;
        } else {
            return false;
        }
    }

    private JComponent prepareCardsPanel() {
        if (cardsPanel == null) {
            cardsPanel = new JPanel(new CardLayout());

            add(createVerticalStrut(combo, cardsPanel, UNRELATED));
            add(cardsPanel);

            JComponent messagePanel = createMessagePanel();
            add(createVerticalStrut(cardsPanel, messagePanel, UNRELATED));
            add(messagePanel);

            cardsPanel.setAlignmentX(LEFT_ALIGNMENT);
            messagePanel.setAlignmentX(LEFT_ALIGNMENT);
        }
        return cardsPanel;
    }

    private JComponent createMessagePanel() {
        message = new JLabel(NbBundle.getMessage(getClass(),
                                                 "MSG_InvalidConnectionValues")); //NOI18N
        message.setForeground(Color.RED);

        JPanel messagePanel = new JPanel();
        messagePanel.add(message);
        messagePanel.setPreferredSize(messagePanel.getPreferredSize());
        setMessageVisible(false);

        return messagePanel;
    }

    private void setMessageVisible(boolean visible) {
        if (message != null) {
            message.setVisible(visible);
        }
    }

    private String getText(String msgKey) {
        return NbBundle.getMessage(getClass(), msgKey);
    }

    private Component createVerticalStrut(JComponent compA, JComponent compB, int related) {
        return Box.createVerticalStrut(getSpace(compA, compB, related, VERTICAL));
    }

    private int getSpace(JComponent compA, JComponent compB, int related, int horizontal) {
        return LayoutStyle.getSharedInstance()
               .getPreferredGap(compA,
                                compB,
                                related,
                                (horizontal == HORIZONTAL) ? EAST : SOUTH,
                                this);
    }

    private String[] getComboPopupDisplayNames() {
        String[] result = new String[existingRepositories.length + connectors.length];

        for (int i = 0; i < existingRepositories.length; i++) {
            result[i] = existingRepositories[i].getDisplayName();
        }

        String newConnectionTemplate = NbBundle.getMessage(
                                            getClass(),
                                            "NewBugtrackingRepositoryConnection"); //NOI18N
        MessageFormat newConnectionFormat = new MessageFormat(newConnectionTemplate);
        for (int i = 0; i < connectors.length; i++) {
            result[existingRepositories.length + i]
                    = newConnectionFormat.format(new Object[] {connectors[i].getDisplayName()});
        }

        return result;
    }

}
