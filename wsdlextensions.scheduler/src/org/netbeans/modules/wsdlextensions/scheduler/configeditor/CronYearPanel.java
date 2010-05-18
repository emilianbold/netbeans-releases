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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
public class CronYearPanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConstants {
    
    private Map<AbstractButton, JLabel[]> buttonLabels =
            new HashMap<AbstractButton, JLabel[]>();
    
    private static final String ERR_INVALID_YEAR_VALUE =
            NbBundle.getMessage(CronYearPanel.class,
                    "ERR_INVALID_YEAR_VALUE");                          //NOI18N
    
    /** Creates new form CronYearPanel */
    public CronYearPanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.YEAR);
        
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
            spnSingleYear
        });
        buttonLabels.put(radSingle, new JLabel[] {lblSingleYear});
        
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleYears,
        });
        buttonLabels.put(radMultiple, new JLabel[] {lblMultipleYears});
        
        addRadioComponents(radInterval, new JComponent[] {
            spnStartingYear,
            spnRepeatInterval,
        });
        buttonLabels.put(radInterval, new JLabel[] {lblStartingYear,
                lblRepeatInterval, lblAfterwards});
        
        addRadioComponents(radRange, new JComponent[] {
            spnFromYear,
            spnToYear,
        });
        buttonLabels.put(radRange, new JLabel[] {lblFromYear, lblToYear});
        
        txfMultipleYears.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleYears.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleYears);
                            fireConditionPropertyChange(txfMultipleYears);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
         
        Calendar cal = Calendar.getInstance();
        Integer currYear = new Integer(cal.get(Calendar.YEAR));
        cal.add(Calendar.YEAR, 1);
        Integer nextYear = new Integer(cal.get(Calendar.YEAR));
        spnSingleYear.setValue(currYear);
        spnStartingYear.setValue(currYear);
        spnFromYear.setValue(currYear);
        spnToYear.setValue(nextYear);
        
        radEvery.setSelected(true);
        
        suppressNotification = false;
    }
    
    private void enableChoices(boolean enabled) {
        suppressNotification = true;
        Enumeration<AbstractButton> btns = bgpYear.getElements();
        while (btns.hasMoreElements()) {
            AbstractButton ab = btns.nextElement();
            if (enabled) {
                ab.setEnabled(true);
            } else {
                setRadioButtonEnabled(ab, false);
                ab.setEnabled(false);
            }
            JLabel[] labels = buttonLabels.get(ab);
            if (labels != null) {
                for (JLabel l : labels) {
                    l.setEnabled(enabled);
                }
            }
        }
        suppressNotification = false;
        
        if (enabled) {
            updateRadioButtonsEnabling();
        } else {
            fireConditionPropertyChange(bgpYear);
        }
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpYear);
    }
    
    public void setCondition(String cond) {
        boolean specifyYearCond = !Utils.isEmpty(cond);
        ckbChooseYearCondition.setSelected(specifyYearCond);
        if (!specifyYearCond) {
            return;
        }
        
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
                spnFromYear.setValue(new Integer(from));
                spnToYear.setValue(new Integer(to));
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval));
                int repeat = parseInt(tok.substring(interval + 1));
                spnStartingYear.setValue(new Integer(starting));
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
                        
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                int year = parseInt(tok);
                spnSingleYear.setValue(new Integer(year));
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int year = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(year);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleYears.setText((sb.length() > 0) ? sb.toString() : null);
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
        if (!ckbChooseYearCondition.isSelected()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        Enumeration<AbstractButton> rads = bgpYear.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(getSpinnerValue(spnSingleYear));
                } else if (radMultiple.equals(btn)) {
                    String years = txfMultipleYears.getText();
                    if (!Utils.isEmpty(years)) {
                        StringTokenizer st =
                                new StringTokenizer(years, LAX_DELIM);
                        List<Integer> yearList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer s = new Integer(parseInt(st.nextToken()));
                            if (yearList.indexOf(s) == -1) {
                                int idx = 0;
                                for (Integer i : yearList) {
                                    if (s.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                yearList.add(idx, s);
                            }
                        }
                        for (Integer i : yearList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    sb.append(getSpinnerValue(spnStartingYear));
                    sb.append(INTERVAL_MODIFIER);
                    sb.append(getSpinnerValue(spnRepeatInterval));
                } else if (radRange.equals(btn)) {
                    sb.append(getSpinnerValue(spnFromYear));
                    sb.append(RANGE_MODIFIER);
                    sb.append(getSpinnerValue(spnToYear));
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
        if (result < 1970) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_YEAR_VALUE);
            }
            result = 1970;
        } else if (result > 2099) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_YEAR_VALUE);
            }
            result = 2099;
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

        bgpYear = new javax.swing.ButtonGroup();
        ckbChooseYearCondition = new javax.swing.JCheckBox();
        radSingle = new javax.swing.JRadioButton();
        lblSingleYear = new javax.swing.JLabel();
        spnSingleYear = new javax.swing.JSpinner();
        radEvery = new javax.swing.JRadioButton();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleYears = new javax.swing.JLabel();
        txfMultipleYears = new javax.swing.JTextField();
        lblMultipleHint = new javax.swing.JLabel();
        radInterval = new javax.swing.JRadioButton();
        lblStartingYear = new javax.swing.JLabel();
        spnStartingYear = new javax.swing.JSpinner();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromYear = new javax.swing.JLabel();
        spnFromYear = new javax.swing.JSpinner();
        lblToYear = new javax.swing.JLabel();
        spnToYear = new javax.swing.JSpinner();

        ckbChooseYearCondition.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ckbChooseYearCondition, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.ckbChooseYearCondition.text")); // NOI18N
        ckbChooseYearCondition.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.ckbChooseYearCondition.toolTipText")); // NOI18N
        ckbChooseYearCondition.setActionCommand("CKB_CHOOSE_YEAR_COND"); // NOI18N
        ckbChooseYearCondition.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ckbChooseYearConditionItemStateChanged(evt);
            }
        });
        ckbChooseYearCondition.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ckbChooseYearConditionFocusGained(evt);
            }
        });

        bgpYear.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_YEAR"); // NOI18N
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

        lblSingleYear.setLabelFor(spnSingleYear);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleYear, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.text")); // NOI18N
        lblSingleYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.toolTipText")); // NOI18N

        spnSingleYear.setModel(new javax.swing.SpinnerNumberModel(1970, 1970, 2099, 1));
        spnSingleYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.toolTipText")); // NOI18N
        spnSingleYear.setEditor(new JSpinner.NumberEditor(spnSingleYear, "#"));
        spnSingleYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnSingleYearStateChanged(evt);
            }
        });
        registerFocusGained(spnSingleYear,
            "CronYearPanel.radSingle.title",//NOI18N
            "CronYearPanel.lblSingleYear.toolTipText");//NOI18N

        bgpYear.add(radEvery);
        org.openide.awt.Mnemonics.setLocalizedText(radEvery, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radEvery.text")); // NOI18N
        radEvery.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radEvery.toolTipText")); // NOI18N
        radEvery.setActionCommand("RAD_EVERY_YEAR"); // NOI18N
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

        bgpYear.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULT_YEAR"); // NOI18N
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

        lblMultipleYears.setLabelFor(txfMultipleYears);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleYears, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.text")); // NOI18N
        lblMultipleYears.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.toolTipText")); // NOI18N

        txfMultipleYears.setText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.txfMultipleYears.text")); // NOI18N
        txfMultipleYears.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.toolTipText")); // NOI18N
        txfMultipleYears.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleYearsFocusGained(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        bgpYear.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_YEAR"); // NOI18N
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

        lblStartingYear.setLabelFor(spnStartingYear);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingYear, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.text")); // NOI18N
        lblStartingYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.toolTipText")); // NOI18N

        spnStartingYear.setModel(new javax.swing.SpinnerNumberModel(1970, 1970, 2099, 1));
        spnStartingYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.toolTipText")); // NOI18N
        spnStartingYear.setEditor(new JSpinner.NumberEditor(spnStartingYear, "#"));
        spnStartingYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnStartingYearStateChanged(evt);
            }
        });
        registerFocusGained(spnStartingYear,
            "CronYearPanel.radInterval.title",//NOI18N
            "CronYearPanel.lblStartingYear.toolTipText");//NOI18N

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 99, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronYearPanel.radInterval.title",//NOI18N
            "CronYearPanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpYear.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_YEAR"); // NOI18N
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

        lblFromYear.setLabelFor(spnFromYear);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromYear, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.text")); // NOI18N
        lblFromYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.toolTipText")); // NOI18N

        spnFromYear.setModel(new javax.swing.SpinnerNumberModel(1970, 1970, 2099, 1));
        spnFromYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.toolTipText")); // NOI18N
        spnFromYear.setEditor(new JSpinner.NumberEditor(spnFromYear, "#"));
        spnFromYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromYearStateChanged(evt);
            }
        });
        registerFocusGained(spnFromYear,
            "CronYearPanel.radRange.title",//NOI18N
            "CronYearPanel.lblFromYear.toolTipText");//NOI18N

        lblToYear.setLabelFor(spnToYear);
        org.openide.awt.Mnemonics.setLocalizedText(lblToYear, NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.text")); // NOI18N
        lblToYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.toolTipText")); // NOI18N

        spnToYear.setModel(new javax.swing.SpinnerNumberModel(1971, 1970, 2099, 1));
        spnToYear.setToolTipText(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.toolTipText")); // NOI18N
        spnToYear.setEditor(new JSpinner.NumberEditor(spnToYear, "#"));
        spnToYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToYearStateChanged(evt);
            }
        });
        registerFocusGained(spnToYear,
            "CronYearPanel.radRange.title",//NOI18N
            "CronYearPanel.lblToYear.toolTipText");//NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(ckbChooseYearCondition))
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleYear)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnSingleYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(radEvery)
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingYear)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnStartingYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromYear)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnFromYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToYear)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnToYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleYears)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleYears, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHint)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(ckbChooseYearCondition)
                .add(1, 1, 1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleYear)
                    .add(spnSingleYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEvery)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleYears)
                    .add(txfMultipleYears, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingYear)
                    .add(spnStartingYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromYear)
                    .add(spnFromYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToYear)
                    .add(spnToYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radSingle.toolTipText")); // NOI18N
        lblSingleYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.text")); // NOI18N
        lblSingleYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.toolTipText")); // NOI18N
        spnSingleYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.text")); // NOI18N
        spnSingleYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblSingleYear.toolTipText")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radEvery.text")); // NOI18N
        radEvery.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radEvery.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleYears.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.text")); // NOI18N
        lblMultipleYears.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.toolTipText")); // NOI18N
        txfMultipleYears.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.text")); // NOI18N
        txfMultipleYears.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleYears.toolTipText")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblMultipleHint.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radInterval.toolTipText")); // NOI18N
        lblStartingYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.text")); // NOI18N
        lblStartingYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.toolTipText")); // NOI18N
        spnStartingYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.text")); // NOI18N
        spnStartingYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblStartingYear.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblAfterwards.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.radRange.toolTipText")); // NOI18N
        lblFromYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.text")); // NOI18N
        lblFromYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.toolTipText")); // NOI18N
        spnFromYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.text")); // NOI18N
        spnFromYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblFromYear.toolTipText")); // NOI18N
        lblToYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.text")); // NOI18N
        lblToYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.toolTipText")); // NOI18N
        spnToYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.text")); // NOI18N
        spnToYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronYearPanel.class, "CronYearPanel.lblToYear.toolTipText")); // NOI18N
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

private void spnSingleYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnSingleYearStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnSingleYearStateChanged

private void spnStartingYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnStartingYearStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnStartingYearStateChanged

private void spnRepeatIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatIntervalStateChanged

private void spnFromYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromYearStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnFromYearStateChanged

private void spnToYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToYearStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnToYearStateChanged

private void ckbChooseYearConditionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ckbChooseYearConditionItemStateChanged
    enableChoices(ckbChooseYearCondition.isSelected());
}//GEN-LAST:event_ckbChooseYearConditionItemStateChanged

private void ckbChooseYearConditionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ckbChooseYearConditionFocusGained
    updateDescription("CronYearPanel.ckbChooseYearCondition.title",     //NOI18N
            "CronYearPanel.ckbChooseYearCondition.toolTipText");        //NOI18N
}//GEN-LAST:event_ckbChooseYearConditionFocusGained

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronYearPanel.radSingle.title",                  //NOI18N
            "CronYearPanel.radSingle.toolTipText");                     //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void radEveryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryFocusGained
    updateDescription("CronYearPanel.radEvery.title",                   //NOI18N
            "CronYearPanel.radEvery.toolTipText");                      //NOI18N
}//GEN-LAST:event_radEveryFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronYearPanel.radMultiple.title",                //NOI18N
            "CronYearPanel.radMultiple.toolTipText");                   //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleYearsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleYearsFocusGained
    updateDescription("CronYearPanel.radMultiple.title",                //NOI18N
            "CronYearPanel.lblMultipleYears.toolTipText");              //NOI18N
}//GEN-LAST:event_txfMultipleYearsFocusGained

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronYearPanel.radInterval.title",                //NOI18N
            "CronYearPanel.radInterval.toolTipText");                   //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronYearPanel.radRange.title",                   //NOI18N
            "CronYearPanel.radRange.toolTipText");                      //NOI18N
}//GEN-LAST:event_radRangeFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpYear;
    private javax.swing.JCheckBox ckbChooseYearCondition;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblFromYear;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblMultipleYears;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleYear;
    private javax.swing.JLabel lblStartingYear;
    private javax.swing.JLabel lblToYear;
    private javax.swing.JRadioButton radEvery;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnFromYear;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JSpinner spnSingleYear;
    private javax.swing.JSpinner spnStartingYear;
    private javax.swing.JSpinner spnToYear;
    private javax.swing.JTextField txfMultipleYears;
    // End of variables declaration//GEN-END:variables

}
