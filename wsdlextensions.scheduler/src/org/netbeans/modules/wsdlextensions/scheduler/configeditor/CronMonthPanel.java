/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeSupport;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.scheduler.model.CronConstants;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author  sunsoabi_edwong
 */
public class CronMonthPanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConstants {
    
    private static final String[] SHORT_MONTHS = CronField.MONTH.getKeys();
    private static final String ERR_INVALID_MONTH_VALUE =
            NbBundle.getMessage(CronMonthPanel.class,
                    "ERR_INVALID_MONTH_VALUE",                          //NOI18N
                    Utils.firstSecondLastOfList(SHORT_MONTHS));

    /** Creates new form CronMonthPanel */
    public CronMonthPanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.MONTH);
        
        preInitComponents();
        initComponents();
        postInitComponents();
    }
    
    private void preInitComponents() {
        pcs = new PropertyChangeSupport(this);
        suppressNotification = false;
    }
    
    private void postInitComponents() {
        suppressNotification = true;
        
        addRadioComponents(radSingle, new JComponent[] {
            spnSingleMonth
        });
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleMonths,
        });
        addRadioComponents(radInterval, new JComponent[] {
            spnStartingMonth,
            spnRepeatInterval,
        });
        addRadioComponents(radRange, new JComponent[] {
            spnFromMonth,
            spnToMonth,
        });
        
        txfMultipleMonths.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleMonths.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleMonths);
                            fireConditionPropertyChange(txfMultipleMonths);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
        
        suppressNotification = false;
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpMonth);
    }
    
    public void setCondition(String cond) {
        
        StringTokenizer st = new StringTokenizer(cond, LAX_DELIM);
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int range = tok.indexOf(RANGE_MODIFIER);
            int interval = tok.indexOf(INTERVAL_MODIFIER);
            
            if (tok.indexOf(EVERY_MODIFIER) != -1) {
                radEvery.setSelected(true);
                return;
            } else if (range != -1) {
                suppressNotification = true;
                int from = parseInt(tok.substring(0, range));
                int to = parseInt(tok.substring(range + 1));
                spnFromMonth.setValue(new Integer(from));
                spnToMonth.setValue(new Integer(to));
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval));
                int repeat = parseInt(tok.substring(interval + 1));
                spnStartingMonth.setValue(new Integer(starting));
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
                        
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                int month = parseInt(tok);
                spnSingleMonth.setValue(new Integer(month));
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int month = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(month);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleMonths.setText((sb.length() > 0) ? sb.toString() : null);
            suppressNotification = false;
            
            radMultiple.setSelected(true);
        }
    }
    
    private int getSpinnerValue(JSpinner spn) {
        try {
            spn.commitEdit();
            return ((Integer) spn.getValue()).intValue();
        } catch (ParseException pe) {
            return ((Integer) ((SpinnerNumberModel) spn.getModel())
                    .getMinimum()).intValue();
        }
    }
    
    public String getCondition() {
        StringBuilder sb = new StringBuilder();
        Enumeration<AbstractButton> rads = bgpMonth.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(getSpinnerValue(spnSingleMonth));
                } else if (radMultiple.equals(btn)) {
                    String months = txfMultipleMonths.getText();
                    if (!Utils.isEmpty(months)) {
                        StringTokenizer st =
                                new StringTokenizer(months, LAX_DELIM);
                        List<Integer> monthList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer s = new Integer(parseInt(st.nextToken()));
                            if (monthList.indexOf(s) == -1) {
                                int idx = 0;
                                for (Integer i : monthList) {
                                    if (s.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                monthList.add(idx, s);
                            }
                        }
                        for (Integer i : monthList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    sb.append(getSpinnerValue(spnStartingMonth));
                    sb.append(INTERVAL_MODIFIER);
                    sb.append(getSpinnerValue(spnRepeatInterval));
                } else if (radRange.equals(btn)) {
                    sb.append(getSpinnerValue(spnFromMonth));
                    sb.append(RANGE_MODIFIER);
                    sb.append(getSpinnerValue(spnToMonth));
                } else if (radEvery.equals(btn)) {
                    sb.append(EVERY_MODIFIER);
                }
                break;
            }
        }
        String cond = sb.toString();
        return (!Utils.isEmpty(cond) ? cond : EVERY_MODIFIER);
    }
    
    private int parseInt(String s) {
        return parseInt(false, s);
    }
    
    int parseInt(boolean user, String s) {
        int result = -1;
        try {
            result = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            result = -1;
            int i = 1;
            for (String m : SHORT_MONTHS) {
                if (m.equalsIgnoreCase(s)) {
                    result = i;
                    break;
                }
                i++;
            }
        }
        if (result < 1) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_MONTH_VALUE);
            }
            result = 1;
        } else if (result > 12) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_MONTH_VALUE);
            }
            result = 12;
        }
        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgpMonth = new javax.swing.ButtonGroup();
        lblChooseMonthCondition = new javax.swing.JLabel();
        radSingle = new javax.swing.JRadioButton();
        lblSingleMonth = new javax.swing.JLabel();
        spnSingleMonth = new javax.swing.JSpinner();
        lblSingleOfYear = new javax.swing.JLabel();
        radEvery = new javax.swing.JRadioButton();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleMonths = new javax.swing.JLabel();
        txfMultipleMonths = new javax.swing.JTextField();
        lblMultipleHint = new javax.swing.JLabel();
        radInterval = new javax.swing.JRadioButton();
        lblStartingMonth = new javax.swing.JLabel();
        spnStartingMonth = new javax.swing.JSpinner();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromMonth = new javax.swing.JLabel();
        spnFromMonth = new javax.swing.JSpinner();
        lblToMonth = new javax.swing.JLabel();
        spnToMonth = new javax.swing.JSpinner();
        lblRangeOfYear = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lblChooseMonthCondition, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblChooseMonthCondition.text")); // NOI18N
        lblChooseMonthCondition.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblChooseMonthCondition.toolTipText")); // NOI18N

        bgpMonth.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_MONTH"); // NOI18N
        radSingle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radSingleItemStateChanged(evt);
            }
        });
        radSingle.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radSingleFocusGained(evt);
            }
        });

        lblSingleMonth.setLabelFor(spnSingleMonth);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleMonth, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.text")); // NOI18N
        lblSingleMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.toolTipText")); // NOI18N

        spnSingleMonth.setModel(new javax.swing.SpinnerNumberModel(1, 1, 12, 1));
        spnSingleMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.toolTipText")); // NOI18N
        spnSingleMonth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnSingleMonthStateChanged(evt);
            }
        });
        registerFocusGained(spnSingleMonth,
            "CronMonthPanel.radSingle.title",//NOI18N
            "CronMonthPanel.lblSingleMonth.toolTipText");//NOI18N

        lblSingleOfYear.setLabelFor(spnSingleMonth);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleOfYear, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleOfYear.text")); // NOI18N
        lblSingleOfYear.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.toolTipText")); // NOI18N

        bgpMonth.add(radEvery);
        org.openide.awt.Mnemonics.setLocalizedText(radEvery, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radEvery.text")); // NOI18N
        radEvery.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radEvery.toolTipText")); // NOI18N
        radEvery.setActionCommand("RAD_EVERY_MONTH"); // NOI18N
        radEvery.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radEveryItemStateChanged(evt);
            }
        });
        radEvery.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radEveryFocusGained(evt);
            }
        });

        bgpMonth.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULT_MONTH"); // NOI18N
        radMultiple.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radMultipleItemStateChanged(evt);
            }
        });
        radMultiple.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radMultipleFocusGained(evt);
            }
        });

        lblMultipleMonths.setLabelFor(txfMultipleMonths);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleMonths, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.text")); // NOI18N
        lblMultipleMonths.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.toolTipText")); // NOI18N

        txfMultipleMonths.setText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.txfMultipleMonths.text")); // NOI18N
        txfMultipleMonths.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.toolTipText")); // NOI18N
        txfMultipleMonths.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleMonthsFocusGained(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        bgpMonth.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_MONTH"); // NOI18N
        radInterval.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radIntervalItemStateChanged(evt);
            }
        });
        radInterval.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radIntervalFocusGained(evt);
            }
        });

        lblStartingMonth.setLabelFor(spnStartingMonth);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingMonth, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.text")); // NOI18N
        lblStartingMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.toolTipText")); // NOI18N

        spnStartingMonth.setModel(new javax.swing.SpinnerNumberModel(1, 1, 12, 1));
        spnStartingMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.toolTipText")); // NOI18N
        spnStartingMonth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnStartingMonthStateChanged(evt);
            }
        });
        registerFocusGained(spnStartingMonth,
            "CronMonthPanel.radInterval.title",//NOI18N
            "CronMonthPanel.lblStartingMonth.toolTipText");//NOI18N

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 11, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronMonthPanel.radInterval.title",//NOI18N
            "CronMonthPanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpMonth.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_MONTH"); // NOI18N
        radRange.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radRangeItemStateChanged(evt);
            }
        });
        radRange.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radRangeFocusGained(evt);
            }
        });

        lblFromMonth.setLabelFor(spnFromMonth);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromMonth, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.text")); // NOI18N
        lblFromMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.toolTipText")); // NOI18N

        spnFromMonth.setModel(new javax.swing.SpinnerNumberModel(1, 1, 12, 1));
        spnFromMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.toolTipText")); // NOI18N
        spnFromMonth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromMonthStateChanged(evt);
            }
        });
        registerFocusGained(spnFromMonth,
            "CronMonthPanel.radRange.title",//NOI18N
            "CronMonthPanel.lblFromMonth.toolTipText");//NOI18N

        lblToMonth.setLabelFor(spnToMonth);
        org.openide.awt.Mnemonics.setLocalizedText(lblToMonth, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.text")); // NOI18N
        lblToMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.toolTipText")); // NOI18N

        spnToMonth.setModel(new javax.swing.SpinnerNumberModel(2, 1, 12, 1));
        spnToMonth.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.toolTipText")); // NOI18N
        spnToMonth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToMonthStateChanged(evt);
            }
        });
        registerFocusGained(spnToMonth,
            "CronMonthPanel.radRange.title",//NOI18N
            "CronMonthPanel.lblToMonth.toolTipText");//NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblRangeOfYear, NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRangeOfYear.text")); // NOI18N
        lblRangeOfYear.setToolTipText(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.toolTipText")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblChooseMonthCondition)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingMonth)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnStartingMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromMonth)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnFromMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToMonth)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnToMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRangeOfYear))
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleMonth)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnSingleMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleOfYear))
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleMonths)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleMonths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHint))
                            .add(radEvery))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblChooseMonthCondition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleMonth)
                    .add(spnSingleMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSingleOfYear))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEvery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleMonths)
                    .add(txfMultipleMonths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingMonth)
                    .add(spnStartingMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromMonth)
                    .add(spnFromMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToMonth)
                    .add(spnToMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRangeOfYear))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblChooseMonthCondition.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblChooseMonthCondition.text")); // NOI18N
        lblChooseMonthCondition.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblChooseMonthCondition.toolTipText")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radSingle.toolTipText")); // NOI18N
        lblSingleMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.text")); // NOI18N
        lblSingleMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.toolTipText")); // NOI18N
        spnSingleMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.text")); // NOI18N
        spnSingleMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleMonth.toolTipText")); // NOI18N
        lblSingleOfYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleOfYear.text")); // NOI18N
        lblSingleOfYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblSingleOfYear.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radEvery.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radEvery.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleMonths.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.text")); // NOI18N
        lblMultipleMonths.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.toolTipText")); // NOI18N
        txfMultipleMonths.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.text")); // NOI18N
        txfMultipleMonths.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleMonths.toolTipText")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblMultipleHint.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radInterval.toolTipText")); // NOI18N
        lblStartingMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.text")); // NOI18N
        lblStartingMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.toolTipText")); // NOI18N
        spnStartingMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.text")); // NOI18N
        spnStartingMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblStartingMonth.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronDayPanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblAfterwards.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.radRange.toolTipText")); // NOI18N
        lblFromMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.text")); // NOI18N
        lblFromMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.toolTipText")); // NOI18N
        spnFromMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.text")); // NOI18N
        spnFromMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblFromMonth.toolTipText")); // NOI18N
        lblToMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.text")); // NOI18N
        lblToMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.toolTipText")); // NOI18N
        spnToMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.text")); // NOI18N
        spnToMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblToMonth.toolTipText")); // NOI18N
        lblRangeOfYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRangeOfYear.text")); // NOI18N
        lblRangeOfYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMonthPanel.class, "CronMonthPanel.lblRangeOfYear.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void radSingleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radSingleItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radSingleItemStateChanged

private void radMultipleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radMultipleItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radMultipleItemStateChanged

private void radIntervalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radIntervalItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radIntervalItemStateChanged

private void radRangeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radRangeItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radRangeItemStateChanged

private void radEveryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radEveryItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radEveryItemStateChanged

private void spnSingleMonthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnSingleMonthStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnSingleMonthStateChanged

private void spnStartingMonthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnStartingMonthStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnStartingMonthStateChanged

private void spnRepeatIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatIntervalStateChanged

private void spnFromMonthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromMonthStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnFromMonthStateChanged

private void spnToMonthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToMonthStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnToMonthStateChanged

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronMonthPanel.radSingle.title",                 //NOI18N
            "CronMonthPanel.radSingle.toolTipText");                    //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void radEveryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryFocusGained
    updateDescription("CronMonthPanel.radEvery.title",                  //NOI18N
            "CronMonthPanel.radEvery.toolTipText");                     //NOI18N
}//GEN-LAST:event_radEveryFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronMonthPanel.radMultiple.title",               //NOI18N
            "CronMonthPanel.radMultiple.toolTipText");                  //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleMonthsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleMonthsFocusGained
    updateDescription("CronMonthPanel.radMultiple.title",               //NOI18N
            "CronMonthPanel.lblMultipleMonths.toolTipText");            //NOI18N
}//GEN-LAST:event_txfMultipleMonthsFocusGained

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronMonthPanel.radInterval.title",               //NOI18N
            "CronMonthPanel.radInterval.toolTipText");                  //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronMonthPanel.radRange.title",                  //NOI18N
            "CronMonthPanel.radRange.toolTipText");                     //NOI18N
}//GEN-LAST:event_radRangeFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpMonth;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblChooseMonthCondition;
    private javax.swing.JLabel lblFromMonth;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblMultipleMonths;
    private javax.swing.JLabel lblRangeOfYear;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleMonth;
    private javax.swing.JLabel lblSingleOfYear;
    private javax.swing.JLabel lblStartingMonth;
    private javax.swing.JLabel lblToMonth;
    private javax.swing.JRadioButton radEvery;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnFromMonth;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JSpinner spnSingleMonth;
    private javax.swing.JSpinner spnStartingMonth;
    private javax.swing.JSpinner spnToMonth;
    private javax.swing.JTextField txfMultipleMonths;
    // End of variables declaration//GEN-END:variables

}
