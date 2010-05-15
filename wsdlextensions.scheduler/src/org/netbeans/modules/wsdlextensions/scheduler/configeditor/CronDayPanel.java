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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.scheduler.model.CronConstants;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author  sunsoabi_edwong
 */
public class CronDayPanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConditionEditor.MutexNonSpecific,
                CronConstants {
    
    private static final String ERR_INVALID_DAY_VALUE =
            NbBundle.getMessage(CronDayPanel.class,
                    "ERR_INVALID_DAY_VALUE");                           //NOI18N
    
    /** Creates new form CronDayPanel */
    public CronDayPanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.DAY);
        
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
            comSingleDay,
            ckbNearestWeekday,
        });
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleDays,
        });
        addRadioComponents(radInterval, new JComponent[] {
            comStartingDay,
            spnRepeatInterval,
        });
        addRadioComponents(radRange, new JComponent[] {
            comFromDay,
            comToDay,
        });
        
        txfMultipleDays.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleDays.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleDays);
                            fireConditionPropertyChange(txfMultipleDays);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
                
        suppressNotification = false;
    }
    
    private void smartSetSelected(JCheckBox ckb, boolean selected) {
        if (ckb.isSelected() != selected) {
            ckb.setSelected(selected);
        }
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpDay);
    }
    
    public void setCondition(String cond) {
        
        StringTokenizer st = new StringTokenizer(cond, LAX_DELIM);
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int range = tok.indexOf(RANGE_MODIFIER);
            int interval = tok.indexOf(INTERVAL_MODIFIER);
            
            if (tok.indexOf(EVERY_MODIFIER) != -1) {
                radEveryDay.setSelected(true);
                return;
            } else if (tok.indexOf(NONSPECIFIC_MODIFIER) != -1) {
                radNoSpecificDay.setSelected(true);
                return;
            } else if (range != -1) {
                suppressNotification = true;
                int from = parseInt(tok.substring(0, range)) - 1;
                int to = parseInt(tok.substring(range + 1)) - 1;
                comFromDay.setSelectedIndex(from);
                comToDay.setSelectedIndex(to);
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval)) - 1;
                int repeat = parseInt(tok.substring(interval + 1));
                comStartingDay.setSelectedIndex(starting);
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
            
            int weekday = tok.indexOf(WEEKDAY_MODIFIER);
            if (tok.indexOf(LAST_MODIFIER) != -1) {
                if (weekday != -1) {
                    radLastWeekday.setSelected(true);
                } else {
                    radLastDay.setSelected(true);
                }
                return;
            }
            
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                String nstr = (weekday != -1) ? tok.substring(0, weekday) : tok;
                int day = parseInt(nstr) - 1;
                comSingleDay.setSelectedIndex(day);
                smartSetSelected(ckbNearestWeekday, (weekday != -1));
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int day = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(day);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleDays.setText((sb.length() > 0) ? sb.toString() : null);
            suppressNotification = false;
            
            radMultiple.setSelected(true);
        }
    }
    
    public String getCondition() {
        StringBuilder sb = new StringBuilder();
        Enumeration<AbstractButton> rads = bgpDay.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(comSingleDay.getSelectedItem());
                    if (ckbNearestWeekday.isSelected()) {
                        sb.append(WEEKDAY_MODIFIER);
                    }
                } else if (radLastDay.equals(btn)) {
                    sb.append(LAST_MODIFIER);
                } else if (radLastWeekday.equals(btn)) {
                    sb.append(LAST_MODIFIER).append(WEEKDAY_MODIFIER);
                } else if (radMultiple.equals(btn)) {
                    String days = txfMultipleDays.getText();
                    if (!Utils.isEmpty(days)) {
                        StringTokenizer st =
                                new StringTokenizer(days, LAX_DELIM);
                        List<Integer> dayList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer d = new Integer(parseInt(st.nextToken()));
                            if (dayList.indexOf(d) == -1) {
                                int idx = 0;
                                for (Integer i : dayList) {
                                    if (d.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                dayList.add(idx, d);
                            }
                        }
                        for (Integer i : dayList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    try {
                        spnRepeatInterval.commitEdit();
                        sb.append(comStartingDay.getSelectedItem());
                        sb.append(INTERVAL_MODIFIER);
                        sb.append(spnRepeatInterval.getValue());
                    } catch (ParseException pe) {
                        // ignore
                    }
                } else if (radRange.equals(btn)) {
                    sb.append(comFromDay.getSelectedItem());
                    sb.append(RANGE_MODIFIER);
                    sb.append(comToDay.getSelectedItem());
                } else if (radEveryDay.equals(btn)) {
                    sb.append(EVERY_MODIFIER);
                } else if (radNoSpecificDay.equals(btn)) {
                    sb.append(NONSPECIFIC_MODIFIER);
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
        if (result < 1) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_DAY_VALUE);
            }
            result = 1;
        } else if (result > 31) {
            if (user) {
                throw new SchedulerArgumentException(ERR_INVALID_DAY_VALUE);
            }
            result = 31;
        }
        return result;
    }
    
    public boolean isNoSpecificValue() {
        return radNoSpecificDay.isSelected();
    }
    
    public void selectNoSpecificValue() {
        radNoSpecificDay.setSelected(true);
    }
    
    public void revertNoSpecificValue() {
        if (isNoSpecificValue()) {
            AbstractButton previous = getPreviousSelectedCondition();
            if (previous != null) {
                previous.setSelected(true);
            } else {
                radEveryDay.setSelected(true);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgpDay = new javax.swing.ButtonGroup();
        lblChooseDayCondition = new javax.swing.JLabel();
        radSingle = new javax.swing.JRadioButton();
        lblSingleDay = new javax.swing.JLabel();
        comSingleDay = new javax.swing.JComboBox();
        lblSingleOfMonth = new javax.swing.JLabel();
        ckbNearestWeekday = new javax.swing.JCheckBox();
        radEveryDay = new javax.swing.JRadioButton();
        radLastDay = new javax.swing.JRadioButton();
        radLastWeekday = new javax.swing.JRadioButton();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleDays = new javax.swing.JLabel();
        txfMultipleDays = new javax.swing.JTextField();
        radInterval = new javax.swing.JRadioButton();
        lblStartingDay = new javax.swing.JLabel();
        comStartingDay = new javax.swing.JComboBox();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromDay = new javax.swing.JLabel();
        comFromDay = new javax.swing.JComboBox();
        lblToDay = new javax.swing.JLabel();
        comToDay = new javax.swing.JComboBox();
        lblOfMonth = new javax.swing.JLabel();
        radNoSpecificDay = new javax.swing.JRadioButton();
        lblMultipleHint = new javax.swing.JLabel();

        lblChooseDayCondition.setLabelFor(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(lblChooseDayCondition, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblChooseDayCondition.text")); // NOI18N
        lblChooseDayCondition.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblChooseDayCondition.toolTipText")); // NOI18N

        bgpDay.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_DAY"); // NOI18N
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

        lblSingleDay.setLabelFor(comSingleDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.text")); // NOI18N
        lblSingleDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.toolTipText")); // NOI18N

        comSingleDay.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        comSingleDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.toolTipText")); // NOI18N
        comSingleDay.setActionCommand("COM_SINGLE_DAY"); // NOI18N
        comSingleDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comSingleDayItemStateChanged(evt);
            }
        });
        comSingleDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comSingleDayFocusGained(evt);
            }
        });

        lblSingleOfMonth.setLabelFor(comSingleDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleOfMonth, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleOfMonth.text")); // NOI18N
        lblSingleOfMonth.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ckbNearestWeekday, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.ckbNearestWeekday.text")); // NOI18N
        ckbNearestWeekday.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.ckbNearestWeekday.toolTipText")); // NOI18N
        ckbNearestWeekday.setActionCommand("CKB_NEAREST_WEEKDAY"); // NOI18N
        ckbNearestWeekday.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ckbNearestWeekdayItemStateChanged(evt);
            }
        });
        ckbNearestWeekday.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ckbNearestWeekdayFocusGained(evt);
            }
        });

        bgpDay.add(radEveryDay);
        org.openide.awt.Mnemonics.setLocalizedText(radEveryDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radEveryDay.text")); // NOI18N
        radEveryDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radEveryDay.toolTipText")); // NOI18N
        radEveryDay.setActionCommand("RAD_EVERY_DAY"); // NOI18N
        radEveryDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radEveryDayItemStateChanged(evt);
            }
        });
        radEveryDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radEveryDayFocusGained(evt);
            }
        });

        bgpDay.add(radLastDay);
        org.openide.awt.Mnemonics.setLocalizedText(radLastDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastDay.text")); // NOI18N
        radLastDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastDay.toolTipText")); // NOI18N
        radLastDay.setActionCommand("RAD_LAST_DAY"); // NOI18N
        radLastDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radLastDayItemStateChanged(evt);
            }
        });
        radLastDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radLastDayFocusGained(evt);
            }
        });

        bgpDay.add(radLastWeekday);
        org.openide.awt.Mnemonics.setLocalizedText(radLastWeekday, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastWeekday.text")); // NOI18N
        radLastWeekday.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastWeekday.toolTipText")); // NOI18N
        radLastWeekday.setActionCommand("RAD_LAST_WEEKDAY"); // NOI18N
        radLastWeekday.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radLastWeekdayItemStateChanged(evt);
            }
        });
        radLastWeekday.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radLastWeekdayFocusGained(evt);
            }
        });

        bgpDay.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULTIPLE_DAY"); // NOI18N
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

        lblMultipleDays.setLabelFor(txfMultipleDays);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleDays, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.text")); // NOI18N
        lblMultipleDays.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.toolTipText")); // NOI18N

        txfMultipleDays.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.toolTipText")); // NOI18N
        txfMultipleDays.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleDaysFocusGained(evt);
            }
        });

        bgpDay.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_DAY"); // NOI18N
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

        lblStartingDay.setLabelFor(comStartingDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.text")); // NOI18N
        lblStartingDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.toolTipText")); // NOI18N

        comStartingDay.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        comStartingDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.toolTipText")); // NOI18N
        comStartingDay.setActionCommand("COM_STARTING_DAY"); // NOI18N
        comStartingDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comStartingDayItemStateChanged(evt);
            }
        });
        comStartingDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comStartingDayFocusGained(evt);
            }
        });

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 30, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatingIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronDayPanel.radInterval.title",//NOI18N
            "CronDayPanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpDay.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_DAY"); // NOI18N
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

        lblFromDay.setLabelFor(comFromDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.text")); // NOI18N
        lblFromDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.toolTipText")); // NOI18N

        comFromDay.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        comFromDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.toolTipText")); // NOI18N
        comFromDay.setActionCommand("COM_FROM_DAY"); // NOI18N
        comFromDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comFromDayItemStateChanged(evt);
            }
        });
        comFromDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comFromDayFocusGained(evt);
            }
        });

        lblToDay.setLabelFor(comToDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblToDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.text")); // NOI18N
        lblToDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.toolTipText")); // NOI18N

        comToDay.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        comToDay.setSelectedIndex(1);
        comToDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.toolTipText")); // NOI18N
        comToDay.setActionCommand("COM_TO_DAY"); // NOI18N
        comToDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comToDayItemStateChanged(evt);
            }
        });
        comToDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comToDayFocusGained(evt);
            }
        });

        lblOfMonth.setLabelFor(comToDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblOfMonth, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblOfMonth.text")); // NOI18N
        lblOfMonth.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.toolTipText")); // NOI18N

        bgpDay.add(radNoSpecificDay);
        org.openide.awt.Mnemonics.setLocalizedText(radNoSpecificDay, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radNoSpecificDay.text")); // NOI18N
        radNoSpecificDay.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radNoSpecificDay.toolTipText")); // NOI18N
        radNoSpecificDay.setActionCommand("RAD_NO_SPECIFIC_DAY"); // NOI18N
        radNoSpecificDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radNoSpecificDayItemStateChanged(evt);
            }
        });
        radNoSpecificDay.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radNoSpecificDayFocusGained(evt);
            }
        });

        lblMultipleHint.setLabelFor(txfMultipleDays);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblChooseDayCondition)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleDay)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comSingleDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleOfMonth)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(ckbNearestWeekday))
                            .add(radLastWeekday)
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleDays)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleDays, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleHint))
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingDay)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comStartingDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromDay)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comFromDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToDay)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comToDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblOfMonth))
                            .add(radEveryDay)
                            .add(radNoSpecificDay)
                            .add(radLastDay))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblChooseDayCondition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleDay)
                    .add(comSingleDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSingleOfMonth)
                    .add(ckbNearestWeekday))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEveryDay)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radLastDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radLastWeekday)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleDays)
                    .add(txfMultipleDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingDay)
                    .add(comStartingDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromDay)
                    .add(comFromDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToDay)
                    .add(comToDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblOfMonth))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radNoSpecificDay)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblChooseDayCondition.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblChooseDayCondition.text")); // NOI18N
        lblChooseDayCondition.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblChooseDayCondition.toolTipText")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radSingle.toolTipText")); // NOI18N
        lblSingleDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.text")); // NOI18N
        lblSingleDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.toolTipText")); // NOI18N
        comSingleDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.text")); // NOI18N
        comSingleDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.toolTipText")); // NOI18N
        lblSingleOfMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleOfMonth.text")); // NOI18N
        lblSingleOfMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblSingleDay.toolTipText")); // NOI18N
        ckbNearestWeekday.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.ckbNearestWeekday.text")); // NOI18N
        ckbNearestWeekday.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.ckbNearestWeekday.toolTipText")); // NOI18N
        radEveryDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radEveryDay.text")); // NOI18N
        radEveryDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radEveryDay.toolTipText")); // NOI18N
        radLastDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastDay.text")); // NOI18N
        radLastDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastDay.toolTipText")); // NOI18N
        radLastWeekday.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastWeekday.text")); // NOI18N
        radLastWeekday.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radLastWeekday.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleDays.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.text")); // NOI18N
        lblMultipleDays.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.toolTipText")); // NOI18N
        txfMultipleDays.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.text")); // NOI18N
        txfMultipleDays.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleDays.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radInterval.toolTipText")); // NOI18N
        lblStartingDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.text")); // NOI18N
        lblStartingDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.toolTipText")); // NOI18N
        comStartingDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.text")); // NOI18N
        comStartingDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblStartingDay.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblAfterwards.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radRange.toolTipText")); // NOI18N
        lblFromDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.text")); // NOI18N
        lblFromDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.toolTipText")); // NOI18N
        comFromDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.text")); // NOI18N
        comFromDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblFromDay.toolTipText")); // NOI18N
        lblToDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.text")); // NOI18N
        lblToDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.toolTipText")); // NOI18N
        comToDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.text")); // NOI18N
        comToDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblToDay.toolTipText")); // NOI18N
        lblOfMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblOfMonth.text")); // NOI18N
        lblOfMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblOfMonth.text")); // NOI18N
        radNoSpecificDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radNoSpecificDay.text")); // NOI18N
        radNoSpecificDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.radNoSpecificDay.toolTipText")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayPanel.class, "CronDayPanel.lblMultipleHint.toolTipText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void radSingleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radSingleItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radSingleItemStateChanged

private void radLastDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radLastDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radLastDayItemStateChanged

private void radLastWeekdayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radLastWeekdayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radLastWeekdayItemStateChanged

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

private void radEveryDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radEveryDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radEveryDayItemStateChanged

private void radNoSpecificDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radNoSpecificDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radNoSpecificDayItemStateChanged

private void spnRepeatingIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatingIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatingIntervalStateChanged

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronDayPanel.radSingle.title",                   //NOI18N
            "CronDayPanel.radSingle.toolTipText");                      //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void comSingleDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comSingleDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comSingleDayItemStateChanged

private void ckbNearestWeekdayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ckbNearestWeekdayItemStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_ckbNearestWeekdayItemStateChanged

private void ckbNearestWeekdayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ckbNearestWeekdayFocusGained
    updateDescription("CronDayPanel.ckbNearestWeekday.title",           //NOI18N
            "CronDayPanel.ckbNearestWeekday.toolTipText");              //NOI18N
}//GEN-LAST:event_ckbNearestWeekdayFocusGained

private void radEveryDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryDayFocusGained
    updateDescription("CronDayPanel.radEveryDay.title",                 //NOI18N
            "CronDayPanel.radEveryDay.toolTipText");                    //NOI18N
}//GEN-LAST:event_radEveryDayFocusGained

private void radLastDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radLastDayFocusGained
    updateDescription("CronDayPanel.radLastDay.title",                  //NOI18N
            "CronDayPanel.radLastDay.toolTipText");                     //NOI18N
}//GEN-LAST:event_radLastDayFocusGained

private void radLastWeekdayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radLastWeekdayFocusGained
    updateDescription("CronDayPanel.radLastWeekday.title",              //NOI18N
            "CronDayPanel.radLastWeekday.toolTipText");                 //NOI18N
}//GEN-LAST:event_radLastWeekdayFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronDayPanel.radMultiple.title",                 //NOI18N
            "CronDayPanel.radMultiple.toolTipText");                    //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleDaysFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleDaysFocusGained
    updateDescription("CronDayPanel.radMultiple.title",                 //NOI18N
            "CronDayPanel.lblMultipleDays.toolTipText");                //NOI18N
}//GEN-LAST:event_txfMultipleDaysFocusGained

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronDayPanel.radInterval.title",                 //NOI18N
            "CronDayPanel.radInterval.toolTipText");                    //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void comStartingDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comStartingDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comStartingDayItemStateChanged

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronDayPanel.radRange.title",                    //NOI18N
            "CronDayPanel.radRange.toolTipText");                       //NOI18N
}//GEN-LAST:event_radRangeFocusGained

private void comFromDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comFromDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comFromDayItemStateChanged

private void comToDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comToDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comToDayItemStateChanged

private void radNoSpecificDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radNoSpecificDayFocusGained
    updateDescription("CronDayPanel.radNoSpecificDay.title",            //NOI18N
            "CronDayPanel.radNoSpecificDay.toolTipText");               //NOI18N
}//GEN-LAST:event_radNoSpecificDayFocusGained

private void comSingleDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comSingleDayFocusGained
    updateDescription("CronDayPanel.radSingle.title",                   //NOI18N
            "CronDayPanel.lblSingleDay.toolTipText");                   //NOI18N
}//GEN-LAST:event_comSingleDayFocusGained

private void comStartingDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comStartingDayFocusGained
    updateDescription("CronDayPanel.radInterval.title",                 //NOI18N
            "CronDayPanel.lblStartingDay.toolTipText");                 //NOI18N
}//GEN-LAST:event_comStartingDayFocusGained

private void comFromDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comFromDayFocusGained
    updateDescription("CronDayPanel.radRange.title",                    //NOI18N
            "CronDayPanel.lblFromDay.toolTipText");                     //NOI18N
}//GEN-LAST:event_comFromDayFocusGained

private void comToDayFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comToDayFocusGained
    updateDescription("CronDayPanel.radRange.title",                    //NOI18N
            "CronDayPanel.lblToDay.toolTipText");                       //NOI18N
}//GEN-LAST:event_comToDayFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpDay;
    private javax.swing.JCheckBox ckbNearestWeekday;
    private javax.swing.JComboBox comFromDay;
    private javax.swing.JComboBox comSingleDay;
    private javax.swing.JComboBox comStartingDay;
    private javax.swing.JComboBox comToDay;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblChooseDayCondition;
    private javax.swing.JLabel lblFromDay;
    private javax.swing.JLabel lblMultipleDays;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblOfMonth;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleDay;
    private javax.swing.JLabel lblSingleOfMonth;
    private javax.swing.JLabel lblStartingDay;
    private javax.swing.JLabel lblToDay;
    private javax.swing.JRadioButton radEveryDay;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radLastDay;
    private javax.swing.JRadioButton radLastWeekday;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radNoSpecificDay;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JTextField txfMultipleDays;
    // End of variables declaration//GEN-END:variables

}
