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
package org.netbeans.lib.terminalemulator.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.Term;

public final class TermOptionsPanel extends javax.swing.JPanel {

    private TermOptions termOptions;
    private final Term term;
    private Action act;

    private class ColorAction extends AbstractAction {
        private final String name;
        private final JButton button;

        public ColorAction(JButton button, String name) {
            this.button = button;
            this.name = name;
        }

        public void actionPerformed(ActionEvent a) {
            Color newColor = JColorChooser.showDialog(
                SwingUtilities.getAncestorOfClass(Dialog.class, button),
                name,
                null);

            if (newColor != null) {
                button.setBackground(newColor);
                if (button == foregroundButton)
                    termOptions.setForeground(newColor);
                else if (button == backgroundButton)
                    termOptions.setBackground(newColor);
                else if (button == selectionButton)
                    termOptions.setSelectionBackground(newColor);
            }
        }
    }


    /** Creates new form TermOptionsPanel */
    public TermOptionsPanel() {
        initComponents();

        term = new Term();
        final String line1String = "Hello from term\r\n";
        final char line1[] = line1String.toCharArray();
        term.putChars(line1, 0, line1.length);

        Border termBorder = BorderFactory.createLoweredBevelBorder();
        term.setBorder(termBorder);
        term.pushStream(new LineDiscipline());
        term.setRowsColumns(7, 60);
        term.setClickToType(true);

        previewPanel.add(term, BorderLayout.CENTER);

    }

    private final PropertyChangeListener propertyListener =
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                previewTermOptions();
            }
        };

    /**
     * Set the model for this view.
     * Changes in the panel are directly reflected in this model which may be
     * {@link TermOptions#assign}ed later.
     * @param termOptions
     */
    public void setTermOptions(TermOptions termOptions) {
        if (this.termOptions != null)
            this.termOptions.removePropertyChangeListener(propertyListener);
        this.termOptions = termOptions;

        applyTermOptions();

        if (this.termOptions != null)
            this.termOptions.addPropertyChangeListener(propertyListener);

        previewTermOptions();

    }

    /**
     * Transfer model values to view widgets.
     */
    private void applyTermOptions() {
        // TMP fontSizeSpinner.setValue(termOptions.getFontSize());
        fontText.setText(termOptions.getFont().getFamily() + " " + termOptions.getFont().getSize());
        foregroundButton.setBackground(termOptions.getForeground());
        backgroundButton.setBackground(termOptions.getBackground());
        selectionButton.setBackground(termOptions.getSelectionBackground());
        historySizeSpinner.setValue(termOptions.getHistorySize());
        tabSizeSpinner.setValue(termOptions.getTabSize());
        clickToTypeCheckBox.setSelected(termOptions.getClickToType());
        scrollOnInputCheckBox.setSelected(termOptions.getScrollOnInput());
        scrollOnOutputCheckBox.setSelected(termOptions.getScrollOnOutput());
        lineWrapCheckBox.setSelected(termOptions.getLineWrap());

    }

    /**
     * Adjust dialog size and layout.
     * *
     * If the chosen font size is >= 14 the term preview area grows too
     * large for the dialog (I think). The result is that the term preview
     * area, button sizes and the font name textarea all shrink to a point.
     *
     * This is an attemt to force the dialog to resize itself but it doesn't work.
     */
    private void patchSizes() {
        term.invalidate();
        previewPanel.validate();

        previewPanel.invalidate();
        this.validate();

        this.invalidate();

        Component p = getParent();
        while (p != null) {
            if (p instanceof JFrame) {
                ((JFrame) p).pack();
                break;
            }
            p = p.getParent();
        }
        /* TMP
        invalidate();
        Component p = getParent();
        if (p != null) {
            p.validate();
        }

        Component p = getParent();
        while (p != null) {
            if (p instanceof JFrame) {
                ((JFrame) p).pack();
                break;
            }
            p = p.getParent();
        }
         */
    }

    /**
     * Apply current models values to the preview area Term.
     */
    private void previewTermOptions() {
        if (term == null)
            return;

        /* OLD
        Font font = new Font("monospaced",
                             Font.PLAIN,
                             termOptions.getFontSize());
        term.setFont(font);
         */
        term.setFixedFont(true);
        term.setFont(termOptions.getFont());

        term.setBackground(termOptions.getBackground());
        term.setForeground(termOptions.getForeground());
        term.setHighlightColor(termOptions.getSelectionBackground());
        term.setHistorySize(termOptions.getHistorySize());
        term.setTabSize(termOptions.getTabSize());

        term.setClickToType(termOptions.getClickToType());
        term.setScrollOnInput(termOptions.getScrollOnInput());
        term.setScrollOnOutput(termOptions.getScrollOnOutput());
        term.setHorizontallyScrollable(!termOptions.getLineWrap());

        term.setRowsColumns(7, 60);

        patchSizes();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {
                java.awt.GridBagConstraints gridBagConstraints;

                descriptionLabel = new javax.swing.JLabel();
                restoreButton = new javax.swing.JButton();
                fontLabel = new javax.swing.JLabel();
                fontText = new javax.swing.JTextField();
                fontButton = new javax.swing.JButton();
                fontSizeLabel = new javax.swing.JLabel();
                fontSizeSpinner = new javax.swing.JSpinner();
                foregroundLabel = new javax.swing.JLabel();
                foregroundButton = new javax.swing.JButton();
                act = new ColorAction(foregroundButton, "Choose Foreground Color");
                foregroundButton.setAction(act);
                backgroundLabel = new javax.swing.JLabel();
                backgroundButton = new javax.swing.JButton();
                act = new ColorAction(backgroundButton, "Choose Background Color");
                backgroundButton.setAction(act);
                selectionLabel = new javax.swing.JLabel();
                selectionButton = new javax.swing.JButton();
                act = new ColorAction(selectionButton, "Choose Selection Background Color");
                selectionButton.setAction(act);
                historySizeLabel = new javax.swing.JLabel();
                historySizeSpinner = new javax.swing.JSpinner();
                tabSizeLabel = new javax.swing.JLabel();
                tabSizeSpinner = new javax.swing.JSpinner();
                clickToTypeCheckBox = new javax.swing.JCheckBox();
                scrollOnInputCheckBox = new javax.swing.JCheckBox();
                scrollOnOutputCheckBox = new javax.swing.JCheckBox();
                lineWrapCheckBox = new javax.swing.JCheckBox();
                previewLabel = new javax.swing.JLabel();
                previewPanel = new javax.swing.JPanel();
                filler = new javax.swing.JPanel();

                setLayout(new java.awt.GridBagLayout());

                descriptionLabel.setText("Options governing Terminal Windows");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
                add(descriptionLabel, gridBagConstraints);

                restoreButton.setText("Restore");
                restoreButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                restoreActionPerformed(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(restoreButton, gridBagConstraints);

                fontLabel.setText("Font:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(fontLabel, gridBagConstraints);

                fontText.setColumns(20);
                fontText.setEditable(false);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(fontText, gridBagConstraints);

                fontButton.setText("...");
                fontButton.setMaximumSize(new java.awt.Dimension(20, 20));
                fontButton.setMinimumSize(new java.awt.Dimension(20, 20));
                fontButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                chooseFont(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(fontButton, gridBagConstraints);

                fontSizeLabel.setLabelFor(fontSizeSpinner);
                fontSizeLabel.setText("Font Size:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(fontSizeLabel, gridBagConstraints);

                fontSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(12, 8, 48, 2));
                fontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                fontSizeSpinnerStateChanged(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(fontSizeSpinner, gridBagConstraints);

                foregroundLabel.setLabelFor(foregroundButton);
                foregroundLabel.setText("Foreground Color:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(foregroundLabel, gridBagConstraints);

                foregroundButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                foregroundButton.setPreferredSize(new java.awt.Dimension(20, 20));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(foregroundButton, gridBagConstraints);

                backgroundLabel.setLabelFor(backgroundButton);
                backgroundLabel.setText("Background Color:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(backgroundLabel, gridBagConstraints);

                backgroundButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                backgroundButton.setPreferredSize(new java.awt.Dimension(20, 20));
                backgroundButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                backgroundButtonActionPerformed(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(backgroundButton, gridBagConstraints);

                selectionLabel.setLabelFor(selectionButton);
                selectionLabel.setText("Selection Background Color:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(selectionLabel, gridBagConstraints);

                selectionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                selectionButton.setPreferredSize(new java.awt.Dimension(20, 20));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(selectionButton, gridBagConstraints);

                historySizeLabel.setLabelFor(historySizeSpinner);
                historySizeLabel.setText("History Size:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(historySizeLabel, gridBagConstraints);

                historySizeSpinner.setModel(new javax.swing.SpinnerNumberModel(4000, 0, 50000, 10));
                historySizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                historySizeSpinnerStateChanged(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(historySizeSpinner, gridBagConstraints);

                tabSizeLabel.setLabelFor(tabSizeSpinner);
                tabSizeLabel.setText("Tab Size:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(tabSizeLabel, gridBagConstraints);

                tabSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 1, 16, 1));
                tabSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                tabSizeSpinnerStateChanged(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(tabSizeSpinner, gridBagConstraints);

                clickToTypeCheckBox.setText("Click To Type");
                clickToTypeCheckBox.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                clickToTypeActionPerformed(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(clickToTypeCheckBox, gridBagConstraints);

                scrollOnInputCheckBox.setText("Scroll On Input");
                scrollOnInputCheckBox.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                scrollOnInputActionPerformed(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(scrollOnInputCheckBox, gridBagConstraints);

                scrollOnOutputCheckBox.setText("Scroll On Output");
                scrollOnOutputCheckBox.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                scrollOnOutputActionPerformed(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(scrollOnOutputCheckBox, gridBagConstraints);

                lineWrapCheckBox.setText("Wrap Lines");
                lineWrapCheckBox.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                lineWrapActionPerformed(evt);
                        }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(lineWrapCheckBox, gridBagConstraints);

                previewLabel.setLabelFor(previewPanel);
                previewLabel.setText("Preview:");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 11;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(previewLabel, gridBagConstraints);

                previewPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                previewPanel.setLayout(new java.awt.BorderLayout());
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
                add(previewPanel, gridBagConstraints);
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
                add(filler, gridBagConstraints);
        }// </editor-fold>//GEN-END:initComponents

    private void restoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreActionPerformed
        termOptions.resetToDefault();
        applyTermOptions();
        previewTermOptions();
    }//GEN-LAST:event_restoreActionPerformed

    private void fontSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fontSizeSpinnerStateChanged
        /* OLD
        int fontSize = termOptions.getFontSize();
        Object fontSizeObj = fontSizeSpinner.getValue();
        if (fontSizeObj instanceof Integer) {
            fontSize = ((Integer) fontSizeObj).intValue();
            termOptions.setFontSize(fontSize);
        }
         */
    }//GEN-LAST:event_fontSizeSpinnerStateChanged

    private void historySizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_historySizeSpinnerStateChanged
        int historySize = termOptions.getHistorySize();
        Object historySizeObj = historySizeSpinner.getValue();
        if (historySizeObj instanceof Integer) {
            historySize = ((Integer) historySizeObj).intValue();
            termOptions.setHistorySize(historySize);
        }
}//GEN-LAST:event_historySizeSpinnerStateChanged

    private void tabSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabSizeSpinnerStateChanged
        int tabSize = termOptions.getTabSize();
        Object tabSizeObj = tabSizeSpinner.getValue();
        if (tabSizeObj instanceof Integer) {
            tabSize = ((Integer) tabSizeObj).intValue();
            termOptions.setTabSize(tabSize);
        }
}//GEN-LAST:event_tabSizeSpinnerStateChanged

    private void clickToTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clickToTypeActionPerformed
        termOptions.setClickToType(clickToTypeCheckBox.isSelected());
    }//GEN-LAST:event_clickToTypeActionPerformed

    private void scrollOnInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollOnInputActionPerformed
        termOptions.setScrollOnInput(scrollOnInputCheckBox.isSelected());
}//GEN-LAST:event_scrollOnInputActionPerformed

    private void scrollOnOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollOnOutputActionPerformed
        termOptions.setScrollOnOutput(scrollOnOutputCheckBox.isSelected());
}//GEN-LAST:event_scrollOnOutputActionPerformed

    private void lineWrapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineWrapActionPerformed
        termOptions.setLineWrap(lineWrapCheckBox.isSelected());
}//GEN-LAST:event_lineWrapActionPerformed

    String getStyleName (int i) {
        if ((i & Font.BOLD) > 0)
            if ((i & Font.ITALIC) > 0) return "CTL_BoldItalic";
            else return "CTL_Bold";
        else
            if ((i & Font.ITALIC) > 0) return "CTL_Italic";
            else return "CTL_Plain";
    }


    private void chooseFont(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFont

        /*
        PropertyEditor pe = PropertyEditorManager.findEditor(Font.class);
        JOptionPane.showOptionDialog(previewPanel,
                                     pe.getCustomEditor(),
                                     "TITLE",
                                     JOptionPane.OK_CANCEL_OPTION,
                                     JOptionPane.QUESTION_MESSAGE, null, null, null);
         */
        FontPanel panel = new FontPanel(termOptions.getFont(),this);
        int choice = JOptionPane.showOptionDialog(previewPanel,
                                                  panel,
                                                  "TITLE",
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (choice == JOptionPane.OK_OPTION) {
            termOptions.setFont(panel.font());
            applyTermOptions();
        }
    }//GEN-LAST:event_chooseFont

    private void backgroundButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_backgroundButtonActionPerformed


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton backgroundButton;
        private javax.swing.JLabel backgroundLabel;
        private javax.swing.JCheckBox clickToTypeCheckBox;
        private javax.swing.JLabel descriptionLabel;
        private javax.swing.JPanel filler;
        private javax.swing.JButton fontButton;
        private javax.swing.JLabel fontLabel;
        private javax.swing.JLabel fontSizeLabel;
        private javax.swing.JSpinner fontSizeSpinner;
        private javax.swing.JTextField fontText;
        private javax.swing.JButton foregroundButton;
        private javax.swing.JLabel foregroundLabel;
        private javax.swing.JLabel historySizeLabel;
        private javax.swing.JSpinner historySizeSpinner;
        private javax.swing.JCheckBox lineWrapCheckBox;
        private javax.swing.JLabel previewLabel;
        private javax.swing.JPanel previewPanel;
        private javax.swing.JButton restoreButton;
        private javax.swing.JCheckBox scrollOnInputCheckBox;
        private javax.swing.JCheckBox scrollOnOutputCheckBox;
        private javax.swing.JButton selectionButton;
        private javax.swing.JLabel selectionLabel;
        private javax.swing.JLabel tabSizeLabel;
        private javax.swing.JSpinner tabSizeSpinner;
        // End of variables declaration//GEN-END:variables

}
