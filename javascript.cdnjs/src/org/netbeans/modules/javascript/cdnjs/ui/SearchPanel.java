/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.cdnjs.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.text.View;
import org.netbeans.modules.javascript.cdnjs.Library;
import org.netbeans.modules.javascript.cdnjs.LibraryProvider;
import org.openide.util.NbBundle;

/**
 * Panel for searching CDNJS libraries.
 *
 * @author Jan Stola
 */
class SearchPanel extends javax.swing.JPanel {
    /** Data listener for CDNJS provider. */
    private final Listener listener = new Listener();
    /** The last search term. */
    private String lastSearchTerm;

    /**
     * Creates a new {@code SearchPanel}.
     */
    SearchPanel() {
        initComponents();
        librariesList.setCellRenderer(new LibraryRenderer());
        versionComboBox.setRenderer(new LibraryVersionRenderer());
        updateLibraries(new Library[0]);
        librarySelected(null);
    }

    /**
     * Activates this panel (i.e. registers all necessary listeners).
     * This method should be called before the panel is shown to the user.
     */
    final void activate() {
        LibraryProvider.getInstance().addPropertyChangeListener(listener);
    }

    /**
     * Deactivates this panel (i.e. unregisters the listeners). This method
     * should be called when the panel is no longer shown to the user.
     */
    final void deactivate() {
        LibraryProvider.getInstance().removePropertyChangeListener(listener);
    }

    /**
     * Invoked when a library is selected.
     * 
     * @param library selected library (or {@code null} when no library is selected).
     */
    private void librarySelected(Library library) {
        boolean emptySelection = (library == null);
        String description = null;
        if (!emptySelection) {
            description = library.getDescription();
            if (description == null) { // Issue 248134
                description = ""; // NOI18N
            }
            description = "<html>" + description; // NOI18N            
        }
        updateHomePageLink(library, false);
        descriptionTextLabel.setText(description);
        versionComboBox.setModel(versionComboBoxModelFor(library));
        versionComboBox.setEnabled(!emptySelection);
        addButton.setEnabled(!emptySelection);
    }

    /**
     * Returns {@code ComboBoxModel} for the given library.
     * 
     * @param library library for which to return the model.
     * @return {@code ComboBoxModel} for the given library.
     */
    private ComboBoxModel<Library.Version> versionComboBoxModelFor(Library library) {
        return new DefaultComboBoxModel<>(library == null ? new Library.Version[0] : library.getVersions());
    }

    /**
     * Starts the search for the libraries matching the current search term.
     */
    @NbBundle.Messages({
        "# {0} - search term",
        "SearchPanel.message.searching=Looking for \"{0}\" libraries"
    })
    private void startSearch() {
        librarySelected(null);
        lastSearchTerm = searchField.getText().trim();
        Library[] libraries = LibraryProvider.getInstance().findLibraries(lastSearchTerm);
        if (libraries == null) {
            messageLabel.setText(Bundle.SearchPanel_message_searching(lastSearchTerm));
            showComponent(messageLabel);
        } else {
            updateLibraries(libraries);
        }
    }

    /**
     * Updates the list of displayed libraries. This method is invoked when
     * a search is finished.
     * 
     * @param libraries libraries matching the last search term (or {@code null}
     * when the search failed). When there are no matching libraries than
     * an empty array is given.
     */
    @NbBundle.Messages({"SearchPanel.message.searchFailed=Search failed :-("})
    final void updateLibraries(Library[] libraries) {
        if (libraries == null) {
            messageLabel.setText(Bundle.SearchPanel_message_searchFailed());
            showComponent(messageLabel);
        } else if (libraries.length == 0) {
            messageLabel.setText(NbBundle.getMessage(SearchPanel.class, "SearchPanel.messageLabel.text")); // NOI18N
            showComponent(messageLabel);
        } else {
            librariesList.setModel(libraryListModelFor(libraries));
            showComponent(searchPanel);
        }
    }

    /**
     * Shows the given component in the main area of the layout.
     * 
     * @param component component to show.
     */
    private void showComponent(JComponent component) {
        if (component.getParent() == null) {
            JComponent shownComponent = (component == messageLabel) ? searchPanel : messageLabel;
            ((GroupLayout)getLayout()).replace(shownComponent, component);
        }
    }

    /**
     * Returns the {@code ListModel} for the given libraries.
     * 
     * @param libraries libraries for which to return the model.
     * @return {@code ListModel} for the given libraries.
     */
    private ListModel<Library> libraryListModelFor(Library[] libraries) {
        DefaultListModel<Library> listModel = new DefaultListModel<>();
        if (libraries != null) {
            for (Library library : libraries) {
                listModel.addElement(library);
            }
        }
        return listModel;
    }

    /**
     * Returns the selected library version.
     * 
     * @return selected library version.
     */
    Library.Version getSelectedVersion() {
        Object item = versionComboBox.getSelectedItem();
        return (item instanceof Library.Version) ? (Library.Version)item : null;
    }

    /**
     * Returns the Add button (to be used in a dialog showing this panel).
     * 
     * @return Add button.
     */
    JButton getAddButton() {
        return addButton;
    }

    /**
     * Returns the Cancel button (to be used in a dialog showing this panel).
     * 
     * @return Cancel button.
     */
    JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Shows the home-page of the selected library.
     */
    void showHomePage() {
        String homePage = getSelectedVersion().getLibrary().getHomePage();
        try {
            URI uri = new URI(homePage);
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(SearchPanel.class.getName()).log(Level.INFO, null, ex);
        }
    }

    /**
     * Updates the label that shows the home-page of the selected library.
     * 
     * @param library selected library.
     * @param linkVisible if {@code true} then the text of the label
     * is underlined, i.e., the label looks like a link.
     */
    void updateHomePageLink(Library library, boolean linkVisible) {
        String homePage = null;
        if (library != null) {
            homePage = library.getHomePage();
            if (homePage != null) {
                homePage = "<html>" + (linkVisible ? "<u>" : "") + homePage;
            }
        }
        homePageLabel.setVisible(homePage != null);
        homePageLinkLabel.setVisible(homePage != null);
        homePageLinkLabel.setText(homePage);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        searchPanel = new javax.swing.JPanel();
        librariesScrollPane = new javax.swing.JScrollPane();
        librariesList = new javax.swing.JList<Library>();
        librariesLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox<Library.Version>();
        versionLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextLabel = new javax.swing.JLabel() {
            @Override
            public Dimension getPreferredSize() {
                Object view = getClientProperty("html"); // NOI18N
                Container container = getParent();
                if ((view instanceof View) && (container != null)) {
                    Dimension containerDim = container.getSize();
                    ((View)view).setSize(containerDim.width, containerDim.height);
                }
                return super.getPreferredSize();
            }
        };
        homePageLabel = new javax.swing.JLabel();
        homePageLinkLabel = new javax.swing.JLabel() {
            public Dimension getMinimumSize() {
                Dimension dim = super.getMinimumSize();
                return new Dimension(0, dim.height);
            }
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.addButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.cancelButton.text")); // NOI18N

        librariesList.addListSelectionListener(formListener);
        librariesScrollPane.setViewportView(librariesList);

        librariesLabel.setLabelFor(librariesList);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.librariesLabel.text")); // NOI18N

        descriptionLabel.setLabelFor(descriptionTextLabel);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.descriptionLabel.text")); // NOI18N

        versionLabel.setLabelFor(versionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.versionLabel.text")); // NOI18N

        descriptionScrollPane.setBorder(null);
        descriptionScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        descriptionTextLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        descriptionScrollPane.setViewportView(descriptionTextLabel);

        org.openide.awt.Mnemonics.setLocalizedText(homePageLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.homePageLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(homePageLinkLabel, "placeholder"); // NOI18N
        homePageLinkLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homePageLinkLabel.addMouseListener(formListener);

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(librariesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(descriptionScrollPane)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(versionLabel)
                            .addComponent(homePageLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(homePageLinkLabel)
                            .addComponent(versionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(descriptionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(librariesScrollPane)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(descriptionScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(homePageLabel)
                            .addComponent(homePageLinkLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(versionLabel)
                            .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0))
        );

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchLabel.text")); // NOI18N

        searchField.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(formListener);

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(SearchPanel.class, "SearchPanel.messageLabel.text")); // NOI18N
        messageLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener, javax.swing.event.ListSelectionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == searchField) {
                SearchPanel.this.searchFieldActionPerformed(evt);
            }
            else if (evt.getSource() == searchButton) {
                SearchPanel.this.searchButtonActionPerformed(evt);
            }
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == homePageLinkLabel) {
                SearchPanel.this.homePageLinkLabelMouseClicked(evt);
            }
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == homePageLinkLabel) {
                SearchPanel.this.homePageLinkLabelMouseEntered(evt);
            }
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == homePageLinkLabel) {
                SearchPanel.this.homePageLinkLabelMouseExited(evt);
            }
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            if (evt.getSource() == librariesList) {
                SearchPanel.this.librariesListValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void librariesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_librariesListValueChanged
        Library library = librariesList.getSelectedValue();
        librarySelected(library);
    }//GEN-LAST:event_librariesListValueChanged

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        startSearch();
    }//GEN-LAST:event_searchFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        startSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void homePageLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseClicked
        showHomePage();
    }//GEN-LAST:event_homePageLinkLabelMouseClicked

    private void homePageLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseEntered
        Library library = librariesList.getSelectedValue();
        updateHomePageLink(library, true);
    }//GEN-LAST:event_homePageLinkLabelMouseEntered

    private void homePageLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homePageLinkLabelMouseExited
        Library library = librariesList.getSelectedValue();
        updateHomePageLink(library, false);
    }//GEN-LAST:event_homePageLinkLabelMouseExited


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JLabel descriptionTextLabel;
    private javax.swing.JLabel homePageLabel;
    private javax.swing.JLabel homePageLinkLabel;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JList<Library> librariesList;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JComboBox<Library.Version> versionComboBox;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Data listener for CDNJS provider.
     */
    class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String searchTerm = evt.getPropertyName();
                    if (searchTerm.equals(lastSearchTerm)) {
                        Library[] libraries = (Library[])evt.getNewValue();
                        updateLibraries(libraries);
                    }
                }
            });
        }
        
    }

    /**
     * Renderer of {@code Library} objects.
     */
    static class LibraryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Library) {
                Library library = (Library)value;
                value = library.getName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    /**
     * Renderer of {@code Library.Version} objects.
     */
    static class LibraryVersionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Library.Version) {
                Library.Version version = (Library.Version)value;
                value = version.getName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}
