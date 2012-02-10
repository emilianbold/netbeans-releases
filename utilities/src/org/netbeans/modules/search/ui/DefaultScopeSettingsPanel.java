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
package org.netbeans.modules.search.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.search.ui.FileNameComboBox;
import org.netbeans.api.search.ui.ScopeSettingsPanel;
import org.netbeans.modules.search.IgnoreListPanel;
import org.netbeans.modules.search.PatternSandbox;


/**
 *
 * @author jhavlin
 */
public class DefaultScopeSettingsPanel extends ScopeSettingsPanel {

    private FileNameComboBox fileNameComboBox;
    private boolean replacing;
    protected JPanel ignoreListOptionPanel;
    private JButton btnEditIgnoreList;
    protected JCheckBox chkUseIgnoreList;
    private JCheckBox chkFileNameRegex;
    private JButton btnTestFileNamePattern;
    private JCheckBox chkArchives;
    private JCheckBox chkGenerated;
    private List<ChangeListener> settingsChangeListeners =
            new LinkedList<ChangeListener>();
    private ItemListener checkBoxListener;

    /**
     * Create settings panel that can be used in search dialog.
     *
     * @param fileNameComboBox File name combo box that will be bound to the
     * regular-expression check box in the panel.
     * @param replacing Replace mode flag.
     */
    public DefaultScopeSettingsPanel(FileNameComboBox fileNameComboBox,
            boolean replacing) {
        this.fileNameComboBox = fileNameComboBox;
        this.replacing = replacing;
        init();
    }

    private void init() {
        btnTestFileNamePattern = new JButton();
        chkFileNameRegex = new JCheckBox();

        if (!replacing) {
            chkArchives = new JCheckBox();
            chkGenerated = new JCheckBox();
        }
        chkUseIgnoreList = new JCheckBox();
        btnEditIgnoreList = new JButton();
        checkBoxListener = new CheckBoxListener();

        setMnemonics();
        initIgnoreListControlComponents();
        initScopeOptionsRow(replacing);
        initInteraction();
    }

    /**
     * Initialize ignoreListOptionPanel and related control components.
     */
    private void initIgnoreListControlComponents() {
        ignoreListOptionPanel = new CheckBoxWithButtonPanel(chkUseIgnoreList,
                btnEditIgnoreList);
    }

    /**
     * Initialize panel for controls for scope options and add it to the form
     * panel.
     */
    private void initScopeOptionsRow(boolean searchAndReplace) {

        JPanel jp = new JPanel();
        if (searchAndReplace) {
            jp.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            jp.add(ignoreListOptionPanel);
            jp.add(chkFileNameRegex);
            jp.setMaximumSize(jp.getMinimumSize());
        } else {
            FormLayoutHelper flh = new FormLayoutHelper(jp,
                    FormLayoutHelper.DEFAULT_COLUMN,
                    FormLayoutHelper.DEFAULT_COLUMN);
            flh.addRow(chkArchives, chkGenerated);
            flh.addRow(ignoreListOptionPanel,
                    new CheckBoxWithButtonPanel(
                    chkFileNameRegex, btnTestFileNamePattern));
            jp.setMaximumSize(jp.getMinimumSize());
        }
        this.add(jp);
    }

    private void initInteraction() {
        btnTestFileNamePattern.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openPathPatternSandbox();
            }
        });
        btnEditIgnoreList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IgnoreListPanel.openDialog(btnEditIgnoreList);
            }
        });
        chkArchives.addItemListener(checkBoxListener);
        chkGenerated.addItemListener(checkBoxListener);
        chkUseIgnoreList.addItemListener(checkBoxListener);
        if (fileNameComboBox != null) {
            chkFileNameRegex.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    fileNameComboBox.setRegularExpression(
                            chkFileNameRegex.isSelected());
                }
            });
        } else {
            chkFileNameRegex.addItemListener(checkBoxListener);
        }
    }

    private void openPathPatternSandbox() {

        PatternSandbox.openDialog(new PatternSandbox.PathPatternSandbox(
                fileNameComboBox.getSelectedItem() == null
                ? "" : fileNameComboBox.getFileNamePattern()) { //NOI18N

            @Override
            protected void onApply(String pattern) {
                if (pattern.isEmpty()) {
                    if (!fileNameComboBox.isAllFilesInfoDisplayed()) {
                        fileNameComboBox.setSelectedItem(pattern);
                        fileNameComboBox.displayAllFilesInfo();
                    }
                } else {
                    if (fileNameComboBox.isAllFilesInfoDisplayed()) {
                        fileNameComboBox.hideAllFilesInfo();
                    }
                    fileNameComboBox.setSelectedItem(pattern);
                }
            }
        }, btnTestFileNamePattern);
    }

    private void setMnemonics() {

        UiUtils.lclz(chkFileNameRegex,
                "BasicSearchForm.chkFileNameRegex.text");               //NOI18N
        btnTestFileNamePattern.setText(UiUtils.getHtmlLink(
                "BasicSearchForm.btnTestFileNamePattern.text"));        //NOI18N
        btnEditIgnoreList.setText(UiUtils.getHtmlLink(
                "BasicSearchForm.btnEditIgnoreList.text"));             //NOI18N
        UiUtils.lclz(chkUseIgnoreList,
                "BasicSearchForm.chkUseIgnoreList.text");               //NOI18N
        if (!replacing) {
            UiUtils.lclz(chkArchives,
                    "BasicSearchForm.chkArchives.text");                //NOI18N
            UiUtils.lclz(chkGenerated,
                    "BasicSearchForm.chkGenerated.text");               //NOI18N
        }
    }

    @Override
    public boolean isSearchInArchives() {
        return chkArchives.isSelected() && chkArchives.isEnabled();
    }

    @Override
    public boolean isSearchInGenerated() {
        return chkGenerated.isSelected() && chkGenerated.isEnabled();
    }

    @Override
    public boolean isUseIgnoreList() {
        return chkUseIgnoreList.isSelected() && chkUseIgnoreList.isEnabled();
    }

    @Override
    public boolean isFileNameRegExp() {
        return chkFileNameRegex.isSelected() && chkFileNameRegex.isEnabled();
    }

    @Override
    public void setSearchInArchives(boolean searchInArchives) {
        chkArchives.setSelected(searchInArchives);
    }

    @Override
    public void setSearchInGenerated(boolean searchInGenerated) {
        chkGenerated.setSelected(searchInGenerated);
    }

    @Override
    public void setUseIgnoreList(boolean useIgnoreList) {
        chkUseIgnoreList.setSelected(useIgnoreList);
    }

    @Override
    public void setFileNameRegexp(boolean fileNameRegexp) {
        chkFileNameRegex.setSelected(fileNameRegexp);
    }

    @Override
    public void addSettingsChangeListener(ChangeListener cl) {
        settingsChangeListeners.add(cl);
    }

    @Override
    public void removeSettingsChangeListener(ChangeListener cl) {
        settingsChangeListeners.remove(cl);
    }

    private final class CheckBoxListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            ChangeEvent ce = new ChangeEvent(DefaultScopeSettingsPanel.this);
            for (ChangeListener l : settingsChangeListeners) {
                l.stateChanged(ce);
            }
        }
    }
}
