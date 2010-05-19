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
public class CronMinutePanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConstants {
    
    private static final String ERR_INVALID_MINUTE_VALUE =
            NbBundle.getMessage(CronMinutePanel.class,
                    "ERR_INVALID_MINUTE_VALUE");                        //NOI18N
    
    /** Creates new form CronMinutePanel */
    public CronMinutePanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.MINUTE);
        
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
            spnSingleMinute
        });
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleMinutes,
        });
        addRadioComponents(radInterval, new JComponent[] {
            spnStartingMinute,
            spnRepeatInterval,
        });
        addRadioComponents(radRange, new JComponent[] {
            spnFromMinute,
            spnToMinute,
        });
        
        txfMultipleMinutes.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireConditionPropertyChange(txfMultipleMinutes);
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireConditionPropertyChange(txfMultipleMinutes);
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleMinutes.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleMinutes);
                            fireConditionPropertyChange(txfMultipleMinutes);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
                
        suppressNotification = false;
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpMinute);
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
                spnFromMinute.setValue(new Integer(from));
                spnToMinute.setValue(new Integer(to));
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval));
                int repeat = parseInt(tok.substring(interval + 1));
                spnStartingMinute.setValue(new Integer(starting));
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
                        
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                int minute = parseInt(tok);
                spnSingleMinute.setValue(new Integer(minute));
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int minute = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(minute);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleMinutes.setText((sb.length() > 0) ? sb.toString()
                    : null);
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
        Enumeration<AbstractButton> rads = bgpMinute.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(getSpinnerValue(spnSingleMinute));
                } else if (radMultiple.equals(btn)) {
                    String minutes = txfMultipleMinutes.getText();
                    if (!Utils.isEmpty(minutes)) {
                        StringTokenizer st =
                                new StringTokenizer(minutes, LAX_DELIM);
                        List<Integer> minuteList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer s = new Integer(parseInt(st.nextToken()));
                            if (minuteList.indexOf(s) == -1) {
                                int idx = 0;
                                for (Integer i : minuteList) {
                                    if (s.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                minuteList.add(idx, s);
                            }
                        }
                        for (Integer i : minuteList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    sb.append(getSpinnerValue(spnStartingMinute));
                    sb.append(INTERVAL_MODIFIER);
                    sb.append(getSpinnerValue(spnRepeatInterval));
                } else if (radRange.equals(btn)) {
                    sb.append(getSpinnerValue(spnFromMinute));
                    sb.append(RANGE_MODIFIER);
                    sb.append(getSpinnerValue(spnToMinute));
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
            // ignore
        }
        if (result < 0) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_MINUTE_VALUE);
            }
            result = 0;
        } else if (result > 59) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_MINUTE_VALUE);
            }
            result = 59;
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

        bgpMinute = new javax.swing.ButtonGroup();
        lblChooseMinuteCondition = new javax.swing.JLabel();
        radSingle = new javax.swing.JRadioButton();
        lblSingleMinute = new javax.swing.JLabel();
        spnSingleMinute = new javax.swing.JSpinner();
        lblSingleOfHour = new javax.swing.JLabel();
        radEvery = new javax.swing.JRadioButton();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleMinutes = new javax.swing.JLabel();
        txfMultipleMinutes = new javax.swing.JTextField();
        radInterval = new javax.swing.JRadioButton();
        lblStartingMinute = new javax.swing.JLabel();
        spnStartingMinute = new javax.swing.JSpinner();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromMinute = new javax.swing.JLabel();
        spnFromMinute = new javax.swing.JSpinner();
        lblToMinute = new javax.swing.JLabel();
        spnToMinute = new javax.swing.JSpinner();
        lblRangeOfHour = new javax.swing.JLabel();
        lblMultipleHint = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lblChooseMinuteCondition, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblChooseMinuteCondition.text")); // NOI18N
        lblChooseMinuteCondition.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblChooseMinuteCondition.toolTipText")); // NOI18N

        bgpMinute.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_MINUTE"); // NOI18N
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

        lblSingleMinute.setLabelFor(spnSingleMinute);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleMinute, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.text")); // NOI18N
        lblSingleMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.toolTipText")); // NOI18N

        spnSingleMinute.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        spnSingleMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.toolTipText")); // NOI18N
        spnSingleMinute.setEditor(new JSpinner.NumberEditor(spnSingleMinute, "00"));  //NOI18N
        spnSingleMinute.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnSingleMinuteStateChanged(evt);
            }
        });
        registerFocusGained(spnSingleMinute,
            "CronMinutePanel.radSingle.title",//NOI18N
            "CronMinutePanel.lblSingleMinute.toolTipText");//NOI18N

        lblSingleOfHour.setLabelFor(spnSingleMinute);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleOfHour, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleOfHour.text")); // NOI18N
        lblSingleOfHour.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.toolTipText")); // NOI18N

        bgpMinute.add(radEvery);
        org.openide.awt.Mnemonics.setLocalizedText(radEvery, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radEvery.text")); // NOI18N
        radEvery.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radEvery.toolTipText")); // NOI18N
        radEvery.setActionCommand("RAD_EVERY_MINUTE"); // NOI18N
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

        bgpMinute.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULT_MINUTE"); // NOI18N
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

        lblMultipleMinutes.setLabelFor(txfMultipleMinutes);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleMinutes, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.text")); // NOI18N
        lblMultipleMinutes.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.toolTipText")); // NOI18N

        txfMultipleMinutes.setText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.txfMultipleMinutes.text")); // NOI18N
        txfMultipleMinutes.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.toolTipText")); // NOI18N
        txfMultipleMinutes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleMinutesFocusGained(evt);
            }
        });

        bgpMinute.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_MINUTE"); // NOI18N
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

        lblStartingMinute.setLabelFor(spnStartingMinute);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingMinute, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.text")); // NOI18N
        lblStartingMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.toolTipText")); // NOI18N

        spnStartingMinute.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        spnStartingMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.toolTipText")); // NOI18N
        spnStartingMinute.setEditor(new JSpinner.NumberEditor(spnStartingMinute, "00"));  //NOI18N
        spnStartingMinute.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnStartingMinuteStateChanged(evt);
            }
        });
        registerFocusGained(spnStartingMinute,
            "CronMinutePanel.radInterval.title",//NOI18N
            "CronMinutePanel.lblStartingMinute.toolTipText");//NOI18N

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 58, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronMinutePanel.radInterval.title",//NOI18N
            "CronMinutePanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpMinute.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_MINUTE"); // NOI18N
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

        lblFromMinute.setLabelFor(spnFromMinute);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromMinute, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.text")); // NOI18N
        lblFromMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.toolTipText")); // NOI18N

        spnFromMinute.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        spnFromMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.toolTipText")); // NOI18N
        spnFromMinute.setEditor(new JSpinner.NumberEditor(spnFromMinute, "00"));  //NOI18N
        spnFromMinute.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromMinuteStateChanged(evt);
            }
        });
        registerFocusGained(spnFromMinute,
            "CronMinutePanel.radRange.title",//NOI18N
            "CronMinutePanel.lblFromMinute.toolTipText");//NOI18N

        lblToMinute.setLabelFor(spnToMinute);
        org.openide.awt.Mnemonics.setLocalizedText(lblToMinute, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.text")); // NOI18N
        lblToMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.toolTipText")); // NOI18N

        spnToMinute.setModel(new javax.swing.SpinnerNumberModel(1, 0, 59, 1));
        spnToMinute.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.toolTipText")); // NOI18N
        spnToMinute.setEditor(new JSpinner.NumberEditor(spnToMinute, "00"));  //NOI18N
        spnToMinute.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToMinuteStateChanged(evt);
            }
        });
        registerFocusGained(spnToMinute,
            "CronMinutePanel.radRange.title",//NOI18N
            "CronMinutePanel.lblToMinute.toolTipText");//NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblRangeOfHour, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRangeOfHour.text")); // NOI18N
        lblRangeOfHour.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.toolTipText")); // NOI18N

        lblMultipleHint.setLabelFor(txfMultipleMinutes);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblChooseMinuteCondition)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingMinute)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnStartingMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromMinute)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnFromMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToMinute)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnToMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRangeOfHour))
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleMinute)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnSingleMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleOfHour))
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleMinutes)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleMinutes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHint))
                            .add(radEvery))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblChooseMinuteCondition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleMinute)
                    .add(spnSingleMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSingleOfHour))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEvery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleMinutes)
                    .add(txfMultipleMinutes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingMinute)
                    .add(spnStartingMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromMinute)
                    .add(spnFromMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToMinute)
                    .add(spnToMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRangeOfHour))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblChooseMinuteCondition.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblChooseMinuteCondition.text")); // NOI18N
        lblChooseMinuteCondition.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblChooseMinuteCondition.toolTipText")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radSingle.toolTipText")); // NOI18N
        lblSingleMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.text")); // NOI18N
        lblSingleMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.toolTipText")); // NOI18N
        spnSingleMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.text")); // NOI18N
        spnSingleMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleMinute.toolTipText")); // NOI18N
        lblSingleOfHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleOfHour.text")); // NOI18N
        lblSingleOfHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblSingleOfHour.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radEvery.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radEvery.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleMinutes.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.text")); // NOI18N
        lblMultipleMinutes.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.toolTipText")); // NOI18N
        txfMultipleMinutes.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.text")); // NOI18N
        txfMultipleMinutes.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleMinutes.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radInterval.toolTipText")); // NOI18N
        lblStartingMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.text")); // NOI18N
        lblStartingMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.toolTipText")); // NOI18N
        spnStartingMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.text")); // NOI18N
        spnStartingMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblStartingMinute.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblAfterwards.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.radRange.toolTipText")); // NOI18N
        lblFromMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.text")); // NOI18N
        lblFromMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.toolTipText")); // NOI18N
        spnFromMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.text")); // NOI18N
        spnFromMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblFromMinute.toolTipText")); // NOI18N
        lblToMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.text")); // NOI18N
        lblToMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.toolTipText")); // NOI18N
        spnToMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.text")); // NOI18N
        spnToMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblToMinute.toolTipText")); // NOI18N
        lblRangeOfHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRangeOfHour.text")); // NOI18N
        lblRangeOfHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblRangeOfHour.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronMinutePanel.class, "CronDayOfWeekPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronMinutePanel.class, "CronMinutePanel.lblMultipleHint.toolTipText")); // NOI18N
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

private void spnSingleMinuteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnSingleMinuteStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnSingleMinuteStateChanged

private void spnStartingMinuteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnStartingMinuteStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnStartingMinuteStateChanged

private void spnRepeatIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatIntervalStateChanged

private void spnFromMinuteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromMinuteStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnFromMinuteStateChanged

private void spnToMinuteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToMinuteStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnToMinuteStateChanged

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronMinutePanel.radSingle.title",                //NOI18N
            "CronMinutePanel.radSingle.toolTipText");                   //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void radEveryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryFocusGained
    updateDescription("CronMinutePanel.radEvery.title",                 //NOI18N
            "CronMinutePanel.radEvery.toolTipText");                    //NOI18N
}//GEN-LAST:event_radEveryFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronMinutePanel.radMultiple.title",              //NOI18N
            "CronMinutePanel.radMultiple.toolTipText");                 //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleMinutesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleMinutesFocusGained
    updateDescription("CronMinutePanel.radMultiple.title",              //NOI18N
            "CronMinutePanel.lblMultipleMinutes.toolTipText");          //NOI18N
}//GEN-LAST:event_txfMultipleMinutesFocusGained

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronMinutePanel.radInterval.title",              //NOI18N
            "CronMinutePanel.radInterval.toolTipText");                 //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronMinutePanel.radRange.title",                 //NOI18N
            "CronMinutePanel.radRange.toolTipText");                    //NOI18N
}//GEN-LAST:event_radRangeFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpMinute;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblChooseMinuteCondition;
    private javax.swing.JLabel lblFromMinute;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblMultipleMinutes;
    private javax.swing.JLabel lblRangeOfHour;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleMinute;
    private javax.swing.JLabel lblSingleOfHour;
    private javax.swing.JLabel lblStartingMinute;
    private javax.swing.JLabel lblToMinute;
    private javax.swing.JRadioButton radEvery;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnFromMinute;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JSpinner spnSingleMinute;
    private javax.swing.JSpinner spnStartingMinute;
    private javax.swing.JSpinner spnToMinute;
    private javax.swing.JTextField txfMultipleMinutes;
    // End of variables declaration//GEN-END:variables

}
