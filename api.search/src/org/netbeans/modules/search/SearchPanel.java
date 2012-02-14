/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author jhavlin
 */
public class SearchPanel extends JPanel implements FocusListener,
        ActionListener {

    private static SearchPanel currentlyShown = null;
    private boolean replacing;
    private boolean activateWithPreviousValues = false; // TODO
    private boolean projectWide = false; // TODO
    private List<SearchProvider.Presenter> presenters;
    /**
     * OK button.
     */
    private JButton okButton;
    /**
     * Cancel button.
     */
    private JButton cancelButton;
    /**
     * Tabbed pane if there are extra providers.
     */
    JTabbedPane tabbedPane = null;
    /**
     * Dialog in which this search panel is displayed.
     */
    private Dialog dialog;
    /**
     * Selected Search presenter
     */
    private SearchProvider.Presenter selectedPresenter;

    /**
     * Panel that can show form with settings for several search providers.
     */
    public SearchPanel(boolean replacing) {
        this.replacing = replacing;
        init();
    }

    private void init() {

        List<SearchProvider.Presenter> extraPresenters = makeExtraPresenters();
        SearchProvider basicProvider = BasicSearchProvider.getInstance();
        final SearchProvider.Presenter basicPresenter =
                basicProvider.createPresenter(replacing);

        presenters = new LinkedList<SearchProvider.Presenter>();
        presenters.add(basicPresenter);
        setLayout(new GridLayout(1, 1));

        if (extraPresenters.isEmpty()) {
            add(basicPresenter.createForm());
        } else {

            tabbedPane = new JTabbedPane();
            tabbedPane.add(basicPresenter.createForm());

            for (SearchProvider.Presenter presenter : extraPresenters) {
                tabbedPane.add(presenter.createForm());
                presenters.add(presenter);
            }
            tabbedPane.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    tabChanged();
                }
            });
            add(tabbedPane);
            // TODO select last opened tab.
        }

        selectedPresenter = basicPresenter;

        for (final SearchProvider.Presenter p : presenters) {
            p.addUsabilityChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    okButton.setEnabled(p.isUsable());
                }
            });
        }
        initLocalStrings();
        initAccessibility();
    }

    private void initLocalStrings() throws MissingResourceException {
        setName(NbBundle.getMessage(SearchPanel.class,
                "TEXT_TITLE_CUSTOMIZE"));           //NOI18N

        Mnemonics.setLocalizedText(okButton = new JButton(),
                NbBundle.getMessage(
                org.netbeans.modules.search.SearchPanel.class,
                "TEXT_BUTTON_SEARCH"));     //NOI18N

        Mnemonics.setLocalizedText(cancelButton = new JButton(),
                NbBundle.getMessage(
                org.netbeans.modules.search.SearchPanel.class,
                "TEXT_BUTTON_CANCEL"));     //NOI18N
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACS_SearchPanel")); // NOI18N
        if (tabbedPane != null) {
            tabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "ACSN_Tabs")); // NOI18N
            tabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACSD_Tabs")); // NOI18N
        }
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACS_TEXT_BUTTON_SEARCH")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "ACS_TEXT_BUTTON_CANCEL")); // NOI18N
    }

    /**
     * Make list of presenters created for all available search providers.
     */
    private List<SearchProvider.Presenter> makeExtraPresenters() {

        List<SearchProvider.Presenter> extraPresenters =
                new LinkedList<SearchProvider.Presenter>();
        for (SearchProvider p :
                Lookup.getDefault().lookupAll(SearchProvider.class)) {
            if ((!replacing || p.isReplaceSupported()) && p.isEnabled()) {
                extraPresenters.add(p.createPresenter(replacing));
            }
        }
        return extraPresenters;
    }

    public void showDialog() {

        String titleMsgKey = projectWide
                ? (replacing
                ? "LBL_ReplaceInProjects" //NOI18N
                : "LBL_FindInProjects") //NOI18N
                : (replacing
                ? "LBL_ReplaceInFiles" //NOI18N
                : "LBL_FindInFiles");         //NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(getClass(), titleMsgKey),
                false,
                new Object[]{okButton, cancelButton},
                okButton,
                DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx(getClass().getCanonicalName() + "." + replacing),
                this);

        dialogDescriptor.setTitle(NbBundle.getMessage(getClass(), titleMsgKey));

        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.addWindowListener(new DialogCloseListener());

        dialog.pack();
        dialog.setVisible(
                true);
        dialog.requestFocus();
        setCurrentlyShown(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        // Tab changed
        tabChanged();
    }

    @Override
    public void focusLost(FocusEvent e) {
        // Tab changed
        tabChanged();
    }

    /**
     * Called when tab panel was changed.
     */
    private void tabChanged() {
        if (tabbedPane != null) {
            int i = tabbedPane.getSelectedIndex();
            SearchProvider.Presenter p = presenters.get(i);
            selectedPresenter = p;
            okButton.setEnabled(p.isUsable());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            search();
        } else if (e.getSource() == cancelButton) {
            cancel();
        }
    }

    private void search() {

        if (selectedPresenter != null) {
            SearchComposition<?> sc = selectedPresenter.composeSearch();
            if (sc != null) {
                SearchTask st = new SearchTask(sc, replacing);
                Manager.getInstance().scheduleSearchTask(st);
                close();
            }
        }
    }

    private void cancel() {
        close();
    }

    /**
     * Is this panel in search-and-replace mode?
     */
    boolean isSearchAndReplace() {
        return replacing;
    }

    /**
     * Close this search panel - dispose its containig dialog.
     *
     * {@link DialogCloseListener#windowClosed(java.awt.event.WindowEvent)} will
     * be called afterwards.
     */
    public void close() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }

    /**
     * Focus containig dialog.
     */
    void focusDialog() {
        if (dialog != null) {
            dialog.requestFocus();
        }
    }

    /**
     * Get currently displayed search panel, or null if no panel is shown.
     */
    static SearchPanel getCurrentlyShown() {
        synchronized (SearchPanel.class) {
            return currentlyShown;
        }
    }

    /**
     * Set currently shoen panel, can be null (no panel shown currently.)
     */
    static void setCurrentlyShown(SearchPanel searchPanel) {
        synchronized (SearchPanel.class) {
            SearchPanel.currentlyShown = searchPanel;
        }
    }

    /**
     * Dialog-Close listener that clears reference to currently displayed panel
     * when its dialog is closed.
     */
    private class DialogCloseListener extends WindowAdapter {

        @Override
        public void windowClosed(WindowEvent e) {
            for (SearchProvider.Presenter presenter : presenters) {
                presenter.clean();
            }
            if (getCurrentlyShown() == SearchPanel.this) {
                setCurrentlyShown(null);
            }
        }
    }
}
