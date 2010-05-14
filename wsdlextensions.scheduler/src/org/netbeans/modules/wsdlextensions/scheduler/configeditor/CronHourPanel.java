/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeSupport;
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
public class CronHourPanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConstants {
    
    private static final String ERR_INVALID_HOUR_VALUE =
            NbBundle.getMessage(CronHourPanel.class,
                    "ERR_INVALID_HOUR_VALUE");                          //NOI18N

    /** Creates new form CronHourPanel */
    public CronHourPanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.HOUR);
        
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
            spnSingleHour
        });
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleHours,
        });
        addRadioComponents(radInterval, new JComponent[] {
            spnStartingHour,
            spnRepeatInterval,
        });
        addRadioComponents(radRange, new JComponent[] {
            spnFromHour,
            spnToHour,
        });
        
        txfMultipleHours.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleHours.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleHours);
                            fireConditionPropertyChange(txfMultipleHours);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
                
        suppressNotification = false;
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpHour);
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
                spnFromHour.setValue(new Integer(from));
                spnToHour.setValue(new Integer(to));
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval));
                int repeat = parseInt(tok.substring(interval + 1));
                spnStartingHour.setValue(new Integer(starting));
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
                        
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                int hour = parseInt(tok);
                spnSingleHour.setValue(new Integer(hour));
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int hour = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(hour);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleHours.setText((sb.length() > 0) ? sb.toString() : null);
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
        Enumeration<AbstractButton> rads = bgpHour.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(getSpinnerValue(spnSingleHour));
                } else if (radMultiple.equals(btn)) {
                    String hours = txfMultipleHours.getText();
                    if (!Utils.isEmpty(hours)) {
                        StringTokenizer st =
                                new StringTokenizer(hours, LAX_DELIM);
                        List<Integer> hourList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer s = new Integer(parseInt(st.nextToken()));
                            if (hourList.indexOf(s) == -1) {
                                int idx = 0;
                                for (Integer i : hourList) {
                                    if (s.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                hourList.add(idx, s);
                            }
                        }
                        for (Integer i : hourList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    sb.append(getSpinnerValue(spnStartingHour));
                    sb.append(INTERVAL_MODIFIER);
                    sb.append(getSpinnerValue(spnRepeatInterval));
                } else if (radRange.equals(btn)) {
                    sb.append(getSpinnerValue(spnFromHour));
                    sb.append(RANGE_MODIFIER);
                    sb.append(getSpinnerValue(spnToHour));
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
            // fall through
        }
        if (result < 0) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_HOUR_VALUE);
            }
            result = 0;
        } else if (result > 23) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_HOUR_VALUE);
            }
            result = 23;
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

        bgpHour = new javax.swing.ButtonGroup();
        lblChooseHourCondition = new javax.swing.JLabel();
        radSingle = new javax.swing.JRadioButton();
        lblSingleHour = new javax.swing.JLabel();
        spnSingleHour = new javax.swing.JSpinner();
        lblSingleOfDay = new javax.swing.JLabel();
        radEvery = new javax.swing.JRadioButton();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleHours = new javax.swing.JLabel();
        txfMultipleHours = new javax.swing.JTextField();
        lblMultipleHint = new javax.swing.JLabel();
        radInterval = new javax.swing.JRadioButton();
        lblStartingHour = new javax.swing.JLabel();
        spnStartingHour = new javax.swing.JSpinner();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromHour = new javax.swing.JLabel();
        spnFromHour = new javax.swing.JSpinner();
        lblToHour = new javax.swing.JLabel();
        spnToHour = new javax.swing.JSpinner();
        lblRangeOfDay = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lblChooseHourCondition, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblChooseHourCondition.text")); // NOI18N
        lblChooseHourCondition.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblChooseHourCondition.toolTipText")); // NOI18N

        bgpHour.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_HOUR"); // NOI18N
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

        lblSingleHour.setLabelFor(spnSingleHour);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleHour, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.text")); // NOI18N
        lblSingleHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.toolTipText")); // NOI18N

        spnSingleHour.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        spnSingleHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.toolTipText")); // NOI18N
        spnSingleHour.setEditor(new JSpinner.NumberEditor(spnSingleHour, "00"));  //NOI18N
        spnSingleHour.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnSingleHourStateChanged(evt);
            }
        });
        registerFocusGained(spnSingleHour,
            "CronHourPanel.radSingle.title",//NOI18N
            "CronHourPanel.lblSingleHour.toolTipText");//NOI18N

        lblSingleOfDay.setLabelFor(spnSingleHour);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleOfDay, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleOfDay.text")); // NOI18N
        lblSingleOfDay.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.toolTipText")); // NOI18N

        bgpHour.add(radEvery);
        org.openide.awt.Mnemonics.setLocalizedText(radEvery, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radEvery.text")); // NOI18N
        radEvery.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radEvery.toolTipText")); // NOI18N
        radEvery.setActionCommand("RAD_EVERY_HOUR"); // NOI18N
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

        bgpHour.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULT_HOUR"); // NOI18N
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

        lblMultipleHours.setLabelFor(txfMultipleHours);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHours, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.text")); // NOI18N
        lblMultipleHours.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.toolTipText")); // NOI18N

        txfMultipleHours.setText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.txfMultipleHours.text")); // NOI18N
        txfMultipleHours.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.toolTipText")); // NOI18N
        txfMultipleHours.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleHoursFocusGained(evt);
            }
        });

        lblMultipleHint.setLabelFor(txfMultipleHours);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        bgpHour.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_HOUR"); // NOI18N
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

        lblStartingHour.setLabelFor(spnStartingHour);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingHour, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.text")); // NOI18N
        lblStartingHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.toolTipText")); // NOI18N

        spnStartingHour.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        spnStartingHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.toolTipText")); // NOI18N
        spnStartingHour.setEditor(new JSpinner.NumberEditor(spnStartingHour, "00"));  //NOI18N
        spnStartingHour.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnStartingHourStateChanged(evt);
            }
        });
        registerFocusGained(spnStartingHour,
            "CronHourPanel.radInterval.title",//NOI18N
            "CronHourPanel.lblStartingHour.toolTipText");//NOI18N

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 22, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronHourPanel.radInterval.title",//NOI18N
            "CronHourPanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpHour.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_HOUR"); // NOI18N
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

        lblFromHour.setLabelFor(spnFromHour);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromHour, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.text")); // NOI18N
        lblFromHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.toolTipText")); // NOI18N

        spnFromHour.setModel(new javax.swing.SpinnerNumberModel(0, 0, 23, 1));
        spnFromHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.toolTipText")); // NOI18N
        spnFromHour.setEditor(new JSpinner.NumberEditor(spnFromHour, "00"));  //NOI18N
        spnFromHour.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromHourStateChanged(evt);
            }
        });
        registerFocusGained(spnFromHour,
            "CronHourPanel.radRange.title",//NOI18N
            "CronHourPanel.lblFromHour.toolTipText");//NOI18N

        lblToHour.setLabelFor(spnToHour);
        org.openide.awt.Mnemonics.setLocalizedText(lblToHour, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.text")); // NOI18N
        lblToHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.toolTipText")); // NOI18N

        spnToHour.setModel(new javax.swing.SpinnerNumberModel(1, 0, 23, 1));
        spnToHour.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.toolTipText")); // NOI18N
        spnToHour.setEditor(new JSpinner.NumberEditor(spnToHour, "00"));  //NOI18N
        spnToHour.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToHourStateChanged(evt);
            }
        });
        registerFocusGained(spnToHour,
            "CronHourPanel.radRange.title",//NOI18N
            "CronHourPanel.lblToHour.toolTipText");//NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblRangeOfDay, NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRangeOfDay.text")); // NOI18N
        lblRangeOfDay.setToolTipText(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.toolTipText")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblChooseHourCondition)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingHour)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnStartingHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromHour)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnFromHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToHour)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnToHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRangeOfDay))
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleHour)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnSingleHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleOfDay))
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHours)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleHours, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHint))
                            .add(radEvery))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblChooseHourCondition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleHour)
                    .add(spnSingleHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSingleOfDay))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEvery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleHours)
                    .add(txfMultipleHours, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingHour)
                    .add(spnStartingHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromHour)
                    .add(spnFromHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToHour)
                    .add(spnToHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRangeOfDay))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblChooseHourCondition.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblChooseHourCondition.text")); // NOI18N
        lblChooseHourCondition.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblChooseHourCondition.toolTipText")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radSingle.toolTipText")); // NOI18N
        lblSingleHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.text")); // NOI18N
        lblSingleHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.toolTipText")); // NOI18N
        spnSingleHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.text")); // NOI18N
        spnSingleHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleHour.toolTipText")); // NOI18N
        lblSingleOfDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleOfDay.text")); // NOI18N
        lblSingleOfDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblSingleOfDay.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radEvery.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radEvery.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleHours.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.text")); // NOI18N
        lblMultipleHours.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.toolTipText")); // NOI18N
        txfMultipleHours.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.text")); // NOI18N
        txfMultipleHours.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHours.toolTipText")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblMultipleHint.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radInterval.toolTipText")); // NOI18N
        lblStartingHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.text")); // NOI18N
        lblStartingHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.toolTipText")); // NOI18N
        spnStartingHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.text")); // NOI18N
        spnStartingHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblStartingHour.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRepeatInterval.toolTipText")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.radRange.toolTipText")); // NOI18N
        lblFromHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.text")); // NOI18N
        lblFromHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.toolTipText")); // NOI18N
        spnFromHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.text")); // NOI18N
        spnFromHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblFromHour.toolTipText")); // NOI18N
        lblToHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.text")); // NOI18N
        lblToHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.toolTipText")); // NOI18N
        spnToHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.text")); // NOI18N
        spnToHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblToHour.toolTipText")); // NOI18N
        lblRangeOfDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRangeOfDay.text")); // NOI18N
        lblRangeOfDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronHourPanel.class, "CronHourPanel.lblRangeOfDay.text")); // NOI18N
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

private void spnSingleHourStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnSingleHourStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnSingleHourStateChanged

private void spnStartingHourStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnStartingHourStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnStartingHourStateChanged

private void spnRepeatIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatIntervalStateChanged

private void spnFromHourStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromHourStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnFromHourStateChanged

private void spnToHourStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToHourStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnToHourStateChanged

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronHourPanel.radSingle.title",                  //NOI18N
            "CronHourPanel.radSingle.toolTipText");                     //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void radEveryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryFocusGained
    updateDescription("CronHourPanel.radEvery.title",                   //NOI18N
            "CronHourPanel.radEvery.toolTipText");                      //NOI18N
}//GEN-LAST:event_radEveryFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronHourPanel.radMultiple.title",                //NOI18N
            "CronHourPanel.radMultiple.toolTipText");                   //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleHoursFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleHoursFocusGained
    updateDescription("CronHourPanel.radMultiple.title",                //NOI18N
            "CronHourPanel.lblMultipleHours.toolTipText");              //NOI18N
}//GEN-LAST:event_txfMultipleHoursFocusGained

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronHourPanel.radInterval.title",                //NOI18N
            "CronHourPanel.radInterval.toolTipText");                   //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronHourPanel.radRange.title",                   //NOI18N
            "CronHourPanel.radRange.toolTipText");                      //NOI18N
}//GEN-LAST:event_radRangeFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpHour;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblChooseHourCondition;
    private javax.swing.JLabel lblFromHour;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblMultipleHours;
    private javax.swing.JLabel lblRangeOfDay;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleHour;
    private javax.swing.JLabel lblSingleOfDay;
    private javax.swing.JLabel lblStartingHour;
    private javax.swing.JLabel lblToHour;
    private javax.swing.JRadioButton radEvery;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnFromHour;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JSpinner spnSingleHour;
    private javax.swing.JSpinner spnStartingHour;
    private javax.swing.JSpinner spnToHour;
    private javax.swing.JTextField txfMultipleHours;
    // End of variables declaration//GEN-END:variables

}
