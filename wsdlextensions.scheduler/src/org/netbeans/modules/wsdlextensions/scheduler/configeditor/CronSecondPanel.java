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
public class CronSecondPanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConstants {
    
    private static final String ERR_INVALID_SECOND_VALUE =
            NbBundle.getMessage(CronSecondPanel.class,
                    "ERR_INVALID_SECOND_VALUE");                        //NOI18N
    
    /** Creates new form CronSecondPanel */
    public CronSecondPanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.SECOND);
        
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
            spnSingleSecond
        });
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleSeconds,
        });
        addRadioComponents(radInterval, new JComponent[] {
            spnStartingSecond,
            spnRepeatInterval,
        });
        addRadioComponents(radRange, new JComponent[] {
            spnFromSecond,
            spnToSecond,
        });
        
        txfMultipleSeconds.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleSeconds.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleSeconds);
                            fireConditionPropertyChange(txfMultipleSeconds);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
                
        suppressNotification = false;
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpSecond);
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
                spnFromSecond.setValue(new Integer(from));
                spnToSecond.setValue(new Integer(to));
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval));
                int repeat = parseInt(tok.substring(interval + 1));
                spnStartingSecond.setValue(new Integer(starting));
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
                        
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                int second = parseInt(tok);
                spnSingleSecond.setValue(new Integer(second));
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int second = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(second);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleSeconds.setText((sb.length() > 0) ? sb.toString()
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
        Enumeration<AbstractButton> rads = bgpSecond.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(getSpinnerValue(spnSingleSecond));
                } else if (radMultiple.equals(btn)) {
                    String seconds = txfMultipleSeconds.getText();
                    if (!Utils.isEmpty(seconds)) {
                        StringTokenizer st =
                                new StringTokenizer(seconds, LAX_DELIM);
                        List<Integer> secondList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer s = new Integer(parseInt(st.nextToken()));
                            if (secondList.indexOf(s) == -1) {
                                int idx = 0;
                                for (Integer i : secondList) {
                                    if (s.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                secondList.add(idx, s);
                            }
                        }
                        for (Integer i : secondList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    sb.append(getSpinnerValue(spnStartingSecond));
                    sb.append(INTERVAL_MODIFIER);
                    sb.append(getSpinnerValue(spnRepeatInterval));
                } else if (radRange.equals(btn)) {
                    sb.append(getSpinnerValue(spnFromSecond));
                    sb.append(RANGE_MODIFIER);
                    sb.append(getSpinnerValue(spnToSecond));
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
                throw new SchedulerArgumentException(ERR_INVALID_SECOND_VALUE);
            }
            result = 0;
        } else if (result > 59) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_SECOND_VALUE);
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

        bgpSecond = new javax.swing.ButtonGroup();
        lblChooseSecondCondition = new javax.swing.JLabel();
        radSingle = new javax.swing.JRadioButton();
        lblSingleSecond = new javax.swing.JLabel();
        spnSingleSecond = new javax.swing.JSpinner();
        lblSingleOfMinute = new javax.swing.JLabel();
        radEvery = new javax.swing.JRadioButton();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleSeconds = new javax.swing.JLabel();
        txfMultipleSeconds = new javax.swing.JTextField();
        lblMultipleHint = new javax.swing.JLabel();
        radInterval = new javax.swing.JRadioButton();
        lblStartingSecond = new javax.swing.JLabel();
        spnStartingSecond = new javax.swing.JSpinner();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromSecond = new javax.swing.JLabel();
        spnFromSecond = new javax.swing.JSpinner();
        lblToSecond = new javax.swing.JLabel();
        spnToSecond = new javax.swing.JSpinner();
        lblRangeOfMinute = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lblChooseSecondCondition, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblChooseSecondCondition.text")); // NOI18N
        lblChooseSecondCondition.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblChooseSecondCondition.toolTipText")); // NOI18N

        bgpSecond.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_SECOND"); // NOI18N
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

        lblSingleSecond.setLabelFor(spnSingleSecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleSecond, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.text")); // NOI18N
        lblSingleSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.toolTipText")); // NOI18N

        spnSingleSecond.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        spnSingleSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.toolTipText")); // NOI18N
        spnSingleSecond.setEditor(new JSpinner.NumberEditor(spnSingleSecond, "00"));  //NOI18N
        spnSingleSecond.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnSingleSecondStateChanged(evt);
            }
        });
        registerFocusGained(spnSingleSecond,
            "CronSecondPanel.radSingle.title",//NOI18N
            "CronSecondPanel.lblSingleSecond.toolTipText");//NOI18N

        lblSingleOfMinute.setLabelFor(spnSingleSecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleOfMinute, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleOfMinute.text")); // NOI18N
        lblSingleOfMinute.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.toolTipText")); // NOI18N

        bgpSecond.add(radEvery);
        org.openide.awt.Mnemonics.setLocalizedText(radEvery, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radEvery.text")); // NOI18N
        radEvery.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radEvery.toolTipText")); // NOI18N
        radEvery.setActionCommand("RAD_EVERY_SECOND"); // NOI18N
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

        bgpSecond.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULT_SECOND"); // NOI18N
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

        lblMultipleSeconds.setLabelFor(txfMultipleSeconds);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleSeconds, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.text")); // NOI18N
        lblMultipleSeconds.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.toolTipText")); // NOI18N

        txfMultipleSeconds.setText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.txfMultipleSeconds.text")); // NOI18N
        txfMultipleSeconds.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.toolTipText")); // NOI18N
        txfMultipleSeconds.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleSecondsFocusGained(evt);
            }
        });

        lblMultipleHint.setLabelFor(txfMultipleSeconds);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        bgpSecond.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_SECOND"); // NOI18N
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

        lblStartingSecond.setLabelFor(spnStartingSecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingSecond, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.text")); // NOI18N
        lblStartingSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.toolTipText")); // NOI18N

        spnStartingSecond.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        spnStartingSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.toolTipText")); // NOI18N
        spnStartingSecond.setEditor(new JSpinner.NumberEditor(spnStartingSecond, "00"));  //NOI18N
        spnStartingSecond.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnStartingSecondStateChanged(evt);
            }
        });
        registerFocusGained(spnStartingSecond,
            "CronSecondPanel.radInterval.title",//NOI18N
            "CronSecondPanel.lblStartingSecond.toolTipText");//NOI18N

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 58, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronSecondPanel.radInterval.title",//NOI18N
            "CronSecondPanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpSecond.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_SECOND"); // NOI18N
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

        lblFromSecond.setLabelFor(spnFromSecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromSecond, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.text")); // NOI18N
        lblFromSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.toolTipText")); // NOI18N

        spnFromSecond.setModel(new javax.swing.SpinnerNumberModel(0, 0, 59, 1));
        spnFromSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.toolTipText")); // NOI18N
        spnFromSecond.setEditor(new JSpinner.NumberEditor(spnFromSecond, "00"));  //NOI18N
        spnFromSecond.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromSecondStateChanged(evt);
            }
        });
        registerFocusGained(spnFromSecond,
            "CronSecondPanel.radRange.title",//NOI18N
            "CronSecondPanel.lblFromSecond.toolTipText");//NOI18N

        lblToSecond.setLabelFor(spnToSecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblToSecond, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.text")); // NOI18N
        lblToSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.toolTipText")); // NOI18N

        spnToSecond.setModel(new javax.swing.SpinnerNumberModel(1, 0, 59, 1));
        spnToSecond.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.toolTipText")); // NOI18N
        spnToSecond.setEditor(new JSpinner.NumberEditor(spnToSecond, "00"));  //NOI18N
        spnToSecond.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToSecondStateChanged(evt);
            }
        });
        registerFocusGained(spnToSecond,
            "CronSecondPanel.radRange.title",//NOI18N
            "CronSecondPanel.lblToSecond.toolTipText");//NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblRangeOfMinute, NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRangeOfMinute.text")); // NOI18N
        lblRangeOfMinute.setToolTipText(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.toolTipText")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblChooseSecondCondition)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingSecond)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnStartingSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromSecond)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnFromSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToSecond)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnToSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRangeOfMinute))
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleSecond)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnSingleSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleOfMinute))
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleSeconds)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleSeconds, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHint))
                            .add(radEvery))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblChooseSecondCondition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleSecond)
                    .add(spnSingleSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSingleOfMinute))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEvery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleSeconds)
                    .add(txfMultipleSeconds, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingSecond)
                    .add(spnStartingSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromSecond)
                    .add(spnFromSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToSecond)
                    .add(spnToSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRangeOfMinute))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblChooseSecondCondition.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblChooseSecondCondition.text")); // NOI18N
        lblChooseSecondCondition.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblChooseSecondCondition.toolTipText")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radSingle.toolTipText")); // NOI18N
        lblSingleSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.text")); // NOI18N
        lblSingleSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.toolTipText")); // NOI18N
        spnSingleSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.text")); // NOI18N
        spnSingleSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleSecond.toolTipText")); // NOI18N
        lblSingleOfMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleOfMinute.text")); // NOI18N
        lblSingleOfMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblSingleOfMinute.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radEvery.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radEvery.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleSeconds.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.text")); // NOI18N
        lblMultipleSeconds.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.toolTipText")); // NOI18N
        txfMultipleSeconds.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.text")); // NOI18N
        txfMultipleSeconds.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleSeconds.toolTipText")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblMultipleHint.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radInterval.toolTipText")); // NOI18N
        lblStartingSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.text")); // NOI18N
        lblStartingSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.toolTipText")); // NOI18N
        spnStartingSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.text")); // NOI18N
        spnStartingSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblStartingSecond.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblAfterwards.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.radRange.toolTipText")); // NOI18N
        lblFromSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.text")); // NOI18N
        lblFromSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.toolTipText")); // NOI18N
        spnFromSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.text")); // NOI18N
        spnFromSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblFromSecond.toolTipText")); // NOI18N
        lblToSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.text")); // NOI18N
        lblToSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.toolTipText")); // NOI18N
        spnToSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.text")); // NOI18N
        spnToSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblToSecond.toolTipText")); // NOI18N
        lblRangeOfMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRangeOfMinute.text")); // NOI18N
        lblRangeOfMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronSecondPanel.class, "CronSecondPanel.lblRangeOfMinute.text")); // NOI18N
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

private void spnSingleSecondStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnSingleSecondStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnSingleSecondStateChanged

private void spnStartingSecondStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnStartingSecondStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnStartingSecondStateChanged

private void spnRepeatIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatIntervalStateChanged

private void spnFromSecondStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromSecondStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnFromSecondStateChanged

private void spnToSecondStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToSecondStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnToSecondStateChanged

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronSecondPanel.radSingle.title",                //NOI18N
            "CronSecondPanel.radSingle.toolTipText");                   //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void radEveryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryFocusGained
    updateDescription("CronSecondPanel.radEvery.title",                 //NOI18N
            "CronSecondPanel.radEvery.toolTipText");                    //NOI18N
}//GEN-LAST:event_radEveryFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronSecondPanel.radMultiple.title",              //NOI18N
            "CronSecondPanel.radMultiple.toolTipText");                 //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleSecondsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleSecondsFocusGained
    updateDescription("CronSecondPanel.radMultiple.title",              //NOI18N
            "CronSecondPanel.lblMultipleSeconds.toolTipText");          //NOI18N
}//GEN-LAST:event_txfMultipleSecondsFocusGained

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronSecondPanel.radInterval.title",              //NOI18N
            "CronSecondPanel.radInterval.toolTipText");                 //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronSecondPanel.radRange.title",                 //NOI18N
            "CronSecondPanel.radRange.toolTipText");                    //NOI18N
}//GEN-LAST:event_radRangeFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpSecond;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblChooseSecondCondition;
    private javax.swing.JLabel lblFromSecond;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblMultipleSeconds;
    private javax.swing.JLabel lblRangeOfMinute;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleOfMinute;
    private javax.swing.JLabel lblSingleSecond;
    private javax.swing.JLabel lblStartingSecond;
    private javax.swing.JLabel lblToSecond;
    private javax.swing.JRadioButton radEvery;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnFromSecond;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JSpinner spnSingleSecond;
    private javax.swing.JSpinner spnStartingSecond;
    private javax.swing.JSpinner spnToSecond;
    private javax.swing.JTextField txfMultipleSeconds;
    // End of variables declaration//GEN-END:variables

}
