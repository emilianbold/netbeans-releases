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
 * to extend the choice of license to i ts licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.profiler.stp.ui;

import org.netbeans.lib.profiler.common.filters.FilterUtils;
import org.netbeans.lib.profiler.common.filters.SimpleFilter;
import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 *
 * @author Tomas Hurka
 * @author  Jiri Sedlacek
 */
@NbBundle.Messages({
    "QuickFilterPanel_FilterTypeLabelText=Filter Type:",
    "QuickFilterPanel_FilterTypeExclusiveRadioText=&Exclusive",
    "QuickFilterPanel_FilterTypeInclusiveRadioText=&Inclusive",
    "QuickFilterPanel_FilterValueLabelText=&Filter Value:",
    "QuickFilterPanel_OkButtonText=OK",
    "QuickFilterPanel_CancelButtonText=Cancel",
    "QuickFilterPanel_EmptyFilterMsg=Empty instrumentation filter",
    "QuickFilterPanel_InvalidFilterMsg=Not a valid profiling instrumentation filter",
//# HTML-formatted
    "QuickFilterPanel_HintMsg=<strong>Example:</strong> <code>java.*</code> or <code>javax.swing.</code> or <code>javax.xml.parsers.SAXParser</code><br><br><strong>Exclusive</strong> filter means listed methods are not instrumented. <strong>Inclusive</strong> filter means only listed methods are instrumented. Wildcard '<strong>*</strong>' symbol can only be used at end of filter value.",
    "QuickFilterPanel_FilterTypeExclusiveRadioAccessDescr=Selected classes will not be instrumented.",
    "QuickFilterPanel_FilterTypeInclusiveRadioAccessDescr=Only selected classes will be instrumented.",
    "QuickFilterPanel_FilterValueTextFieldAccessName=Enter quick filter value here."
})
public final class QuickFilterPanel extends JPanel implements HelpCtx.Provider {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private final class FilterValueTextFieldDocumentListener implements DocumentListener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void changedUpdate(final DocumentEvent e) {
            checkFilterValue();
        }

        public void insertUpdate(final DocumentEvent e) {
            checkFilterValue();
        }

        public void removeUpdate(final DocumentEvent e) {
            checkFilterValue();
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "QuickFilterPanel.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static QuickFilterPanel defaultInstance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ButtonGroup filterTypeButtonGroup;
    private HTMLTextArea hintArea;
    private JButton CancelButton;
    private JButton OKButton;
    private JLabel filterTypeLabel;
    private JLabel filterValueHintLabel;
    private JLabel filterValueLabel;
    private JRadioButton filterTypeExclusiveRadio;
    private JRadioButton filterTypeInclusiveRadio;
    private JTextArea filterValueTextArea;
    private SimpleFilter quickFilter;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of QuickFilterPanel */
    private QuickFilterPanel() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static QuickFilterPanel getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new QuickFilterPanel();
        }

        return defaultInstance;
    }

    public JButton getCancelButton() {
        return CancelButton;
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public JButton getOKButton() {
        return OKButton;
    }

    public void applyChanges() {
        if (filterTypeExclusiveRadio.isSelected()) {
            quickFilter.setFilterType(SimpleFilter.SIMPLE_FILTER_EXCLUSIVE);
        } else {
            quickFilter.setFilterType(SimpleFilter.SIMPLE_FILTER_INCLUSIVE);
        }

        quickFilter.setFilterValue(getFilterValueInternal());
    }

    // TODO: just to keep backward compatibility, should be removed after code cleanup!!!
    public void init() {
        init(FilterUtils.QUICK_FILTER);
    }

    public void init(SimpleFilter quickFilter) {
        this.quickFilter = quickFilter;

        if ((filterTypeExclusiveRadio != null) && (filterTypeInclusiveRadio != null)) {
            switch (quickFilter.getFilterType()) {
                case SimpleFilter.SIMPLE_FILTER_EXCLUSIVE:
                    filterTypeExclusiveRadio.setSelected(true);
                    break;
                case SimpleFilter.SIMPLE_FILTER_INCLUSIVE:
                    filterTypeInclusiveRadio.setSelected(true);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal Quick filter type:"+quickFilter.getFilterType());
            }
        }

        if (filterTypeLabel != null) {
            String[] filterParts = FilterUtils.getSeparateFilters(quickFilter.getFilterValue());
            java.util.List<String> filterPartsList = new ArrayList(filterParts.length);

            for (String filterPart : filterParts) {
                filterPartsList.add(filterPart);
            }

            Collections.sort(filterPartsList);

            StringBuilder val = new StringBuilder(filterParts.length);
            Iterator<String> it = filterPartsList.iterator();

            while (it.hasNext()) {
                val.append(it.next());

                if (it.hasNext()) {
                    val.append("\n"); // NOI18N
                }
            }

            filterValueTextArea.setText(val.toString());
            filterValueTextArea.setCaretPosition(0);
            filterValueTextArea.requestFocus();
        }

        checkFilterValue();
    }

    private boolean isFilterValid() {
        String[] filterParts = getFilterValues();

        for (int i = 0; i < filterParts.length; i++) {
            if (!FilterUtils.isValidProfilerFilter(filterParts[i])) {
                return false;
            }
        }

        return true;
    }

    // Converts JTextArea text delimited by \n to FilterUtils text delimited by ,
    private String getFilterValueInternal() {
        StringBuilder convertedValue = new StringBuilder();

        String[] filterValues = getFilterValues();

        for (int i = 0; i < filterValues.length; i++) {
            String filterValue = filterValues[i].trim();

            if ((i != (filterValues.length - 1)) && !filterValue.endsWith(",")) {
                filterValue = filterValue + ", "; // NOI18N
            }

            convertedValue.append(filterValue);
        }

        return convertedValue.toString();
    }

    private String[] getFilterValues() {
        return filterValueTextArea.getText().split("\\n"); // NOI18N
    }

    private void checkFilterValue() {
        if (filterValueTextArea == null) {
            return;
        }

        getFilterValues();

        String filterValue = filterValueTextArea.getText().trim();

        if (filterValue.length() == 0) {
            filterValueHintLabel.setText(Bundle.QuickFilterPanel_EmptyFilterMsg());
            OKButton.setEnabled(false);
            filterValueTextArea.setForeground(Color.red);
            filterValueTextArea.setSelectedTextColor(Color.red);
        } else if (!isFilterValid()) {
            filterValueHintLabel.setText(Bundle.QuickFilterPanel_InvalidFilterMsg());
            OKButton.setEnabled(false);
            filterValueTextArea.setForeground(Color.red);
            filterValueTextArea.setSelectedTextColor(Color.red);
        } else {
            filterValueHintLabel.setText(" "); // NOI18N
            OKButton.setEnabled(true);
            filterValueTextArea.setForeground(UIManager.getColor("Label.foreground")); // NOI18N
            filterValueTextArea.setSelectedTextColor(UIManager.getColor("Label.foreground")); // NOI18N
        }
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        // buttons to export
        OKButton = new JButton(Bundle.QuickFilterPanel_OkButtonText());
        CancelButton = new JButton(Bundle.QuickFilterPanel_CancelButtonText());

        filterTypeLabel = new JLabel();
        filterValueLabel = new JLabel();
        filterValueHintLabel = new JLabel();
        filterTypeButtonGroup = new ButtonGroup();
        filterTypeExclusiveRadio = new JRadioButton();
        filterTypeInclusiveRadio = new JRadioButton();
        filterValueTextArea = new JTextArea();
        hintArea = new HTMLTextArea() {
                public Dimension getPreferredSize() { // Workaround to force the text area not to consume horizontal space to fit the contents to just one line

                    return new Dimension(1, super.getPreferredSize().height);
                }
            };

        setLayout(new GridBagLayout());

        // filterTypeLabel
        filterTypeLabel.setText(Bundle.QuickFilterPanel_FilterTypeLabelText());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 15);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(filterTypeLabel, gridBagConstraints);

        // filterTypeExclusiveRadio
        org.openide.awt.Mnemonics.setLocalizedText(filterTypeExclusiveRadio, Bundle.QuickFilterPanel_FilterTypeExclusiveRadioText());
        filterTypeExclusiveRadio.getAccessibleContext().setAccessibleDescription(Bundle.QuickFilterPanel_FilterTypeExclusiveRadioAccessDescr());
        filterTypeButtonGroup.add(filterTypeExclusiveRadio);

        // filterTypeInclusiveRadio
        org.openide.awt.Mnemonics.setLocalizedText(filterTypeInclusiveRadio, Bundle.QuickFilterPanel_FilterTypeInclusiveRadioText());
        filterTypeInclusiveRadio.getAccessibleContext().setAccessibleDescription(Bundle.QuickFilterPanel_FilterTypeInclusiveRadioAccessDescr());
        filterTypeInclusiveRadio.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        filterTypeButtonGroup.add(filterTypeInclusiveRadio);

        // filterRadiosPanel
        final JPanel filterRadiosPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        filterRadiosPanel.add(filterTypeExclusiveRadio);
        filterRadiosPanel.add(filterTypeInclusiveRadio);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 5, 15);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(filterRadiosPanel, gridBagConstraints);

        // filterValueLabel
        org.openide.awt.Mnemonics.setLocalizedText(filterValueLabel, Bundle.QuickFilterPanel_FilterValueLabelText());
        filterValueLabel.setLabelFor(filterValueTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(filterValueLabel, gridBagConstraints);

        // filterValueTextArea
        filterValueTextArea.getAccessibleContext().setAccessibleName(Bundle.QuickFilterPanel_FilterValueTextFieldAccessName());
        filterValueTextArea.setSelectionColor(UIConstants.TABLE_SELECTION_BACKGROUND_COLOR);
        filterValueTextArea.setSelectedTextColor(UIConstants.TABLE_SELECTION_FOREGROUND_COLOR);
        filterValueTextArea.getDocument().addDocumentListener(new FilterValueTextFieldDocumentListener());

        JTextArea temp = new JTextArea();
        temp.setColumns(45);
        temp.setRows(6);

        JScrollPane filterValueScrollPane = new JScrollPane(filterValueTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filterValueScrollPane.setPreferredSize(new Dimension(temp.getPreferredSize().width, temp.getPreferredSize().height));
        temp = null;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(filterValueScrollPane, gridBagConstraints);

        // filterValueHintLabel
        filterValueHintLabel.setText(" "); // NOI18N
        filterValueHintLabel.setForeground(new Color(89, 79, 191)); // the same as nb wizard error message
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 10);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(filterValueHintLabel, gridBagConstraints);

        // panel filling bottom space
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(new JPanel(), gridBagConstraints);

        Color panelBackground = UIManager.getColor("Panel.background"); //NOI18N
        Color hintBackground = UIUtils.getSafeColor(panelBackground.getRed() - 10, panelBackground.getGreen() - 10,
                                                    panelBackground.getBlue() - 10);

        // hintArea
        hintArea.setText(Bundle.QuickFilterPanel_HintMsg()); // NOI18N
        hintArea.setEnabled(false);
        hintArea.setDisabledTextColor(Color.darkGray);
        hintArea.setBackground(hintBackground);
        hintArea.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, hintBackground));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 7);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(hintArea, gridBagConstraints);

        checkFilterValue();
    }

    /**
     * @param args the command line arguments
     */

    /*  public static void main (String[] args) {
       try {
         UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
         //UIManager.setLookAndFeel("plaf.metal.MetalLookAndFeel"); //NOI18N
         //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //NOI18N
         //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N
       } catch (Exception e){};
       JFrame frame = new JFrame("FilterSetsPanel Viewer"); //NOI18N
       frame.getContentPane().add(new QuickFilterPanel());
       frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
       frame.pack();
       frame.show();
       }
     */
}
