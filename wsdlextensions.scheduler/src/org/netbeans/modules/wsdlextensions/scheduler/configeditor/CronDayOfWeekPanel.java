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
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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
public class CronDayOfWeekPanel extends AbstractCronConditionEditor
        implements CronConditionEditor, CronConditionEditor.MutexNonSpecific,
                CronConstants {
    
    private ComboBoxModel singleDOWModel;
    private ComboBoxModel ordinalModel;
    private ComboBoxModel ordinalDOWModel;
    private ComboBoxModel intervalDOWModel;
    private ComboBoxModel rangeFromDOWModel;
    private ComboBoxModel rangeToDOWModel;
    private String[] dowsSunSat;
    
    private static String ERR_INVALID_DAYOFWEEK_VALUE;
    
    /** Creates new form CronDayOfWeekPanel */
    public CronDayOfWeekPanel(AbstractTriggerPanel mainPanel) {
        super(mainPanel, CronField.DAY_OF_WEEK);
        
        preInitComponents();
        initComponents();
        postInitComponents();
    }
    
    private void preInitComponents() {
        pcs = new PropertyChangeSupport(this);
        suppressNotification = false;
        dowsSunSat = CronField.DAY_OF_WEEK.getKeys();
        ERR_INVALID_DAYOFWEEK_VALUE = NbBundle.getMessage(
                CronDayOfWeekPanel.class,
                "ERR_INVALID_DAYOFWEEK_VALUE",                          //NOI18N
                Utils.firstSecondLastOfList(dowsSunSat));
        singleDOWModel = new DefaultComboBoxModel(dowsSunSat);
        ordinalDOWModel = new DefaultComboBoxModel(dowsSunSat);
        intervalDOWModel = new DefaultComboBoxModel(dowsSunSat);
        rangeFromDOWModel = new DefaultComboBoxModel(dowsSunSat);
        rangeToDOWModel = new DefaultComboBoxModel(dowsSunSat);
        ordinalModel = new DefaultComboBoxModel(new String[] {
            NbBundle.getMessage(CronDayOfWeekPanel.class,
                    "CronDayOfWeekPanel.first.text"),                   //NOI18N
            NbBundle.getMessage(CronDayOfWeekPanel.class,
                    "CronDayOfWeekPanel.second.text"),                  //NOI18N
            NbBundle.getMessage(CronDayOfWeekPanel.class,
                    "CronDayOfWeekPanel.third.text"),                   //NOI18N
            NbBundle.getMessage(CronDayOfWeekPanel.class,
                    "CronDayOfWeekPanel.fourth.text"),                  //NOI18N
            NbBundle.getMessage(CronDayOfWeekPanel.class,
                    "CronDayOfWeekPanel.fifth.text"),                   //NOI18N
            NbBundle.getMessage(CronDayOfWeekPanel.class,
                    "CronDayOfWeekPanel.last.text")                     //NOI18N
        });
    }
    
    private void postInitComponents() {
        suppressNotification = true;
        
        addRadioComponents(radSingle, new JComponent[] {
            comSingleDayOfWeek,
        });
        addRadioComponents(radOrdinalDayOfWeek, new JComponent[] {
            comOrdinal,
            comOrdinalDayOfWeek,
        });
        addRadioComponents(radMultiple, new JComponent[] {
            txfMultipleDayOfWeeks,
        });
        addRadioComponents(radInterval, new JComponent[] {
            comStartingDayOfWeek,
            spnRepeatInterval,
        });
        addRadioComponents(radRange, new JComponent[] {
            comFromDayOfWeek,
            comToDayOfWeek,
        });
        
        txfMultipleDayOfWeeks.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        fireIfValid();
                    }

                    public void changedUpdate(DocumentEvent e) {}
                    
                    private void fireIfValid() {
                        String text = txfMultipleDayOfWeeks.getText();
                        if (Utils.isEmpty(text) ||
                                (MSG_ASK_ENTER_MULTIPLE_ENTRIES.indexOf(text)
                                        != -1)) {
                            return;     // ignore
                        }
                        
                        try {
                            validateMultipleEntries(text,
                                    lblMultipleDayOfWeeks);
                            fireConditionPropertyChange(txfMultipleDayOfWeeks);
                        } catch (SchedulerArgumentException sae) {
                            mainPanel.showError(sae);
                        }
                    }
                });
                
        suppressNotification = false;
    }
    
    private void updateRadioButtonsEnabling() {
        updateRadioButtonsEnabling(bgpDayOfWeek);
    }
    
    public void setCondition(String cond) {
        
        StringTokenizer st = new StringTokenizer(cond, LAX_DELIM);
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int range = tok.indexOf(RANGE_MODIFIER);
            int interval = tok.indexOf(INTERVAL_MODIFIER);
            
            if (tok.indexOf(EVERY_MODIFIER) != -1) {
                radEveryDayOfWeek.setSelected(true);
                return;
            } else if (tok.indexOf(NONSPECIFIC_MODIFIER) != -1) {
                radNoSpecificDayOfWeek.setSelected(true);
                return;
            } else if (range != -1) {
                suppressNotification = true;
                int from = parseInt(tok.substring(0, range)) - 1;
                int to = parseInt(tok.substring(range + 1)) - 1;
                comFromDayOfWeek.setSelectedIndex(from);
                comToDayOfWeek.setSelectedIndex(to);
                suppressNotification = false;
                
                radRange.setSelected(true);
                return;
            } else if (interval != -1) {
                suppressNotification = true;
                int starting = parseInt(tok.substring(0, interval)) - 1;
                int repeat = parseInt(tok.substring(interval + 1));
                comStartingDayOfWeek.setSelectedIndex(starting);
                spnRepeatInterval.setValue(new Integer(repeat));
                suppressNotification = false;
                
                radInterval.setSelected(true);
                return;
            }
            
            int[] ordinality = parseOrdinality(tok);
            if (ordinality != null) {
                suppressNotification = true;
                int dow = parseInt(tok.substring(0, ordinality[1]));
                comOrdinal.setSelectedIndex(ordinality[0] - 1);
                comOrdinalDayOfWeek.setSelectedIndex(dow - 1);
                suppressNotification = false;
                
                radOrdinalDayOfWeek.setSelected(true);
                return;
            }
            
            if (!st.hasMoreTokens()) {
                suppressNotification = true;
                int dow = parseInt(tok) - 1;
                comSingleDayOfWeek.setSelectedIndex(dow);
                suppressNotification = false;
                
                radSingle.setSelected(true);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            do {
                if (null == tok) {
                    tok = st.nextToken();
                }
                int dow = parseInt(tok);
                if (sb.length() > 0) {
                    sb.append(PRETTY_DELIM);
                }
                sb.append(dow);
                tok = null;
            } while (st.hasMoreTokens());
            suppressNotification = true;
            txfMultipleDayOfWeeks.setText((sb.length() > 0) ? sb.toString()
                    : null);
            suppressNotification = false;
            
            radMultiple.setSelected(true);
        }
    }
    
    public String getCondition() {
        StringBuilder sb = new StringBuilder();
        Enumeration<AbstractButton> rads = bgpDayOfWeek.getElements();
        while (rads.hasMoreElements()) {
            AbstractButton btn = rads.nextElement();
            if (btn.isSelected()) {
                if (radSingle.equals(btn)) {
                    sb.append(comSingleDayOfWeek.getSelectedIndex() + 1);
                } else if (radOrdinalDayOfWeek.equals(btn)) {
                    int ord = comOrdinal.getSelectedIndex() + 1;
                    int dow = comOrdinalDayOfWeek.getSelectedIndex() + 1;
                    sb.append(dow);
                    if (ord > 5) {
                        sb.append(LAST_MODIFIER);
                    } else {
                        sb.append(ORDINAL_MODIFIER).append(ord);
                    }
                } else if (radMultiple.equals(btn)) {
                    String dows = txfMultipleDayOfWeeks.getText();
                    if (!Utils.isEmpty(dows)) {
                        StringTokenizer st =
                                new StringTokenizer(dows, LAX_DELIM);
                        List<Integer> dowList = new ArrayList<Integer>();
                        while (st.hasMoreTokens()) {
                            Integer d = new Integer(parseInt(st.nextToken()));
                            if (dowList.indexOf(d) == -1) {
                                int idx = 0;
                                for (Integer i : dowList) {
                                    if (d.compareTo(i) < 0) {
                                        break;
                                    }
                                    idx++;
                                }
                                dowList.add(idx, d);
                            }
                        }
                        for (Integer i : dowList) {
                            if (sb.length() > 0) {
                                sb.append(DELIM);
                            }
                            sb.append(i);
                        }
                    }
                } else if (radInterval.equals(btn)) {
                    try {
                        spnRepeatInterval.commitEdit();
                        sb.append(comStartingDayOfWeek.getSelectedIndex() + 1);
                        sb.append(INTERVAL_MODIFIER);
                        sb.append(spnRepeatInterval.getValue());
                    } catch (ParseException pe) {
                        // ignore
                    }
                } else if (radRange.equals(btn)) {
                    sb.append(comFromDayOfWeek.getSelectedIndex() + 1);
                    sb.append(RANGE_MODIFIER);
                    sb.append(comToDayOfWeek.getSelectedIndex() + 1);
                } else if (radEveryDayOfWeek.equals(btn)) {
                    sb.append(EVERY_MODIFIER);
                } else if (radNoSpecificDayOfWeek.equals(btn)) {
                    sb.append(NONSPECIFIC_MODIFIER);
                }
                break;
            }
        }
        String cond = sb.toString();
        return (!Utils.isEmpty(cond) ? cond : EVERY_MODIFIER);
    }
    
    private int[] parseOrdinality(String s) {
        int modifier = s.indexOf(LAST_MODIFIER);
        if (modifier != -1) {
            return new int[] {6, modifier};
        } else if ((modifier = s.indexOf(ORDINAL_MODIFIER)) != -1) {
            int ord = -1;
            try {
                ord = Integer.parseInt(s.substring(modifier + 1));
            } catch (NumberFormatException nfe) {
                // ignore
            }
            if (ord < 1) {
                ord = 1;
            } else if (ord > 5) {
                ord = 5;
            }
            return new int[] {ord, modifier};
        }
        return null;
    }
    
    private int parseInt(String s) {
        return parseInt(false, s);
    }
    
    int parseInt(boolean user, String s) {
        int result = -1;
        try {
            result = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            int idx = 1;
            for (String dow : dowsSunSat) {
                if (dow.equalsIgnoreCase(s)) {
                    result = idx;
                    break;
                }
                idx++;
            }
        }
        if (result < 1) {
            if (user) {
                throw new SchedulerArgumentException(
                        ERR_INVALID_DAYOFWEEK_VALUE);
            }
            result = 1;
        } else if (result > 7) {
            if (user) {
                throw new SchedulerArgumentException(
                        ERR_INVALID_DAYOFWEEK_VALUE);
            }
            result = 7;
        }
        return result;
    }
    
    public boolean isNoSpecificValue() {
        return radNoSpecificDayOfWeek.isSelected();
    }
    
    public void selectNoSpecificValue() {
        radNoSpecificDayOfWeek.setSelected(true);
    }
    
    public void revertNoSpecificValue() {
        if (isNoSpecificValue()) {
            AbstractButton previous = getPreviousSelectedCondition();
            if (previous != null) {
                previous.setSelected(true);
            } else {
                radEveryDayOfWeek.setSelected(true);
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

        bgpDayOfWeek = new javax.swing.ButtonGroup();
        lblChooseDayOfWeekCondition = new javax.swing.JLabel();
        radSingle = new javax.swing.JRadioButton();
        lblSingleDayOfWeek = new javax.swing.JLabel();
        comSingleDayOfWeek = new javax.swing.JComboBox();
        lblSingleOfWeek = new javax.swing.JLabel();
        radEveryDayOfWeek = new javax.swing.JRadioButton();
        radOrdinalDayOfWeek = new javax.swing.JRadioButton();
        lblOrdinal = new javax.swing.JLabel();
        comOrdinal = new javax.swing.JComboBox();
        comOrdinalDayOfWeek = new javax.swing.JComboBox();
        lblOrdinalDayOfWeek = new javax.swing.JLabel();
        radMultiple = new javax.swing.JRadioButton();
        lblMultipleDayOfWeeks = new javax.swing.JLabel();
        txfMultipleDayOfWeeks = new javax.swing.JTextField();
        lblMultipleHint = new javax.swing.JLabel();
        radInterval = new javax.swing.JRadioButton();
        lblStartingDayOfWeek = new javax.swing.JLabel();
        comStartingDayOfWeek = new javax.swing.JComboBox();
        lblRepeatInterval = new javax.swing.JLabel();
        spnRepeatInterval = new javax.swing.JSpinner();
        lblAfterwards = new javax.swing.JLabel();
        radRange = new javax.swing.JRadioButton();
        lblFromDayOfWeek = new javax.swing.JLabel();
        comFromDayOfWeek = new javax.swing.JComboBox();
        lblToDayOfWeek = new javax.swing.JLabel();
        comToDayOfWeek = new javax.swing.JComboBox();
        lblOfWeek = new javax.swing.JLabel();
        radNoSpecificDayOfWeek = new javax.swing.JRadioButton();

        lblChooseDayOfWeekCondition.setLabelFor(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(lblChooseDayOfWeekCondition, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblChooseDayOfWeekCondition.text")); // NOI18N
        lblChooseDayOfWeekCondition.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblChooseDayOfWeekCondition.toolTipText")); // NOI18N

        bgpDayOfWeek.add(radSingle);
        org.openide.awt.Mnemonics.setLocalizedText(radSingle, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radSingle.text")); // NOI18N
        radSingle.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radSingle.toolTipText")); // NOI18N
        radSingle.setActionCommand("RAD_SINGLE_DOW"); // NOI18N
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

        lblSingleDayOfWeek.setLabelFor(comSingleDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.text")); // NOI18N
        lblSingleDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.toolTipText")); // NOI18N

        comSingleDayOfWeek.setModel(singleDOWModel);
        comSingleDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.toolTipText")); // NOI18N
        comSingleDayOfWeek.setActionCommand("COM_SINGLE_DOW"); // NOI18N
        comSingleDayOfWeek.setSelectedIndex(0);
        comSingleDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comSingleDayOfWeekItemStateChanged(evt);
            }
        });
        comSingleDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comSingleDayOfWeekFocusGained(evt);
            }
        });

        lblSingleOfWeek.setLabelFor(comSingleDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblSingleOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleOfWeek.text")); // NOI18N
        lblSingleOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.toolTipText")); // NOI18N

        bgpDayOfWeek.add(radEveryDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(radEveryDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radEveryDayOfWeek.text")); // NOI18N
        radEveryDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radEveryDayOfWeek.toolTipText")); // NOI18N
        radEveryDayOfWeek.setActionCommand("RAD_EVERY_DOW"); // NOI18N
        radEveryDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radEveryDayOfWeekItemStateChanged(evt);
            }
        });
        radEveryDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radEveryDayOfWeekFocusGained(evt);
            }
        });

        bgpDayOfWeek.add(radOrdinalDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(radOrdinalDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radOrdinalDayOfWeek.text")); // NOI18N
        radOrdinalDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radOrdinalDayOfWeek.toolTipText")); // NOI18N
        radOrdinalDayOfWeek.setActionCommand("RAD_ORDINAL_DOW"); // NOI18N
        radOrdinalDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radOrdinalDayOfWeekItemStateChanged(evt);
            }
        });
        radOrdinalDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radOrdinalDayOfWeekFocusGained(evt);
            }
        });

        lblOrdinal.setLabelFor(comOrdinal);
        org.openide.awt.Mnemonics.setLocalizedText(lblOrdinal, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.text")); // NOI18N
        lblOrdinal.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.toolTipText")); // NOI18N

        comOrdinal.setModel(ordinalModel);
        comOrdinal.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.toolTipText")); // NOI18N
        comOrdinal.setActionCommand("COM_ORD"); // NOI18N
        comOrdinal.setSelectedIndex(0);
        comOrdinal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comOrdinalItemStateChanged(evt);
            }
        });
        comOrdinal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comOrdinalFocusGained(evt);
            }
        });

        comOrdinalDayOfWeek.setModel(ordinalDOWModel);
        comOrdinalDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.comOrdinalDayOfWeek.toolTipText")); // NOI18N
        comOrdinalDayOfWeek.setActionCommand("COM_ORD_DOW"); // NOI18N
        comOrdinalDayOfWeek.setSelectedIndex(0);
        comOrdinalDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comOrdinalDayOfWeekItemStateChanged(evt);
            }
        });
        comOrdinalDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comOrdinalDayOfWeekFocusGained(evt);
            }
        });

        lblOrdinalDayOfWeek.setLabelFor(comOrdinalDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblOrdinalDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinalDayOfWeek.text")); // NOI18N
        lblOrdinalDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.comOrdinalDayOfWeek.toolTipText")); // NOI18N

        bgpDayOfWeek.add(radMultiple);
        org.openide.awt.Mnemonics.setLocalizedText(radMultiple, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radMultiple.text")); // NOI18N
        radMultiple.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radMultiple.toolTipText")); // NOI18N
        radMultiple.setActionCommand("RAD_MULT_DOW"); // NOI18N
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

        lblMultipleDayOfWeeks.setLabelFor(txfMultipleDayOfWeeks);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleDayOfWeeks, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.text")); // NOI18N
        lblMultipleDayOfWeeks.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.toolTipText")); // NOI18N

        txfMultipleDayOfWeeks.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.toolTipText")); // NOI18N
        txfMultipleDayOfWeeks.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMultipleDayOfWeeksFocusGained(evt);
            }
        });
        txfMultipleDayOfWeeks.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                fireConditionPropertyChange(txfMultipleDayOfWeeks);
            }
            public void removeUpdate(DocumentEvent evt) {
                fireConditionPropertyChange(txfMultipleDayOfWeeks);
            }
            public void changedUpdate(DocumentEvent evt) {}
        });

        lblMultipleHint.setLabelFor(txfMultipleDayOfWeeks);
        org.openide.awt.Mnemonics.setLocalizedText(lblMultipleHint, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleHint.toolTipText")); // NOI18N
        lblMultipleHint.setEnabled(false);

        bgpDayOfWeek.add(radInterval);
        org.openide.awt.Mnemonics.setLocalizedText(radInterval, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radInterval.text")); // NOI18N
        radInterval.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radInterval.toolTipText")); // NOI18N
        radInterval.setActionCommand("RAD_INTERVAL_DOW"); // NOI18N
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

        lblStartingDayOfWeek.setLabelFor(comStartingDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblStartingDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.text")); // NOI18N
        lblStartingDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.toolTipText")); // NOI18N

        comStartingDayOfWeek.setModel(intervalDOWModel);
        comStartingDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.toolTipText")); // NOI18N
        comStartingDayOfWeek.setActionCommand("COM_STARTING_DOW"); // NOI18N
        comStartingDayOfWeek.setSelectedIndex(0);
        comStartingDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comStartingDayOfWeekItemStateChanged(evt);
            }
        });
        comStartingDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comStartingDayOfWeekFocusGained(evt);
            }
        });

        lblRepeatInterval.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeatInterval, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.toolTipText")); // NOI18N

        spnRepeatInterval.setModel(new javax.swing.SpinnerNumberModel(1, 1, 6, 1));
        spnRepeatInterval.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRepeatingIntervalStateChanged(evt);
            }
        });
        registerFocusGained(spnRepeatInterval,
            "CronDayOfWeekPanel.radInterval.title",//NOI18N
            "CronDayOfWeekPanel.lblRepeatInterval.toolTipText");//NOI18N

        lblAfterwards.setLabelFor(spnRepeatInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblAfterwards, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.toolTipText")); // NOI18N

        bgpDayOfWeek.add(radRange);
        org.openide.awt.Mnemonics.setLocalizedText(radRange, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radRange.text")); // NOI18N
        radRange.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radRange.toolTipText")); // NOI18N
        radRange.setActionCommand("RAD_RANGE_DOW"); // NOI18N
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

        lblFromDayOfWeek.setLabelFor(comFromDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblFromDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.text")); // NOI18N
        lblFromDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.toolTipText")); // NOI18N

        comFromDayOfWeek.setModel(rangeFromDOWModel);
        comFromDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.toolTipText")); // NOI18N
        comFromDayOfWeek.setActionCommand("COM_FROM_DOW"); // NOI18N
        comFromDayOfWeek.setSelectedIndex(0);
        comFromDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comFromDayOfWeekItemStateChanged(evt);
            }
        });
        comFromDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comFromDayOfWeekFocusGained(evt);
            }
        });

        lblToDayOfWeek.setLabelFor(comToDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblToDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.text")); // NOI18N
        lblToDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.toolTipText")); // NOI18N

        comToDayOfWeek.setModel(rangeToDOWModel);
        comToDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.toolTipText")); // NOI18N
        comToDayOfWeek.setActionCommand("COM_TO_DOW"); // NOI18N
        comToDayOfWeek.setSelectedIndex(1);
        comToDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comToDayOfWeekItemStateChanged(evt);
            }
        });
        comToDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comToDayOfWeekFocusGained(evt);
            }
        });

        lblOfWeek.setLabelFor(comToDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(lblOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOfWeek.text")); // NOI18N
        lblOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.toolTipText")); // NOI18N

        bgpDayOfWeek.add(radNoSpecificDayOfWeek);
        org.openide.awt.Mnemonics.setLocalizedText(radNoSpecificDayOfWeek, NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radNoSpecificDayOfWeek.text")); // NOI18N
        radNoSpecificDayOfWeek.setToolTipText(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radNoSpecificDayOfWeek.toolTipText")); // NOI18N
        radNoSpecificDayOfWeek.setActionCommand("RAD_NO_SPECIFIC_DOW"); // NOI18N
        radNoSpecificDayOfWeek.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                radNoSpecificDayOfWeekItemStateChanged(evt);
            }
        });
        radNoSpecificDayOfWeek.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                radNoSpecificDayOfWeekFocusGained(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblChooseDayOfWeekCondition)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(radOrdinalDayOfWeek)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblOrdinal)
                                .add(4, 4, 4)
                                .add(comOrdinal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comOrdinalDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblOrdinalDayOfWeek))
                            .add(layout.createSequentialGroup()
                                .add(radSingle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleDayOfWeek)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comSingleDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblSingleOfWeek))
                            .add(layout.createSequentialGroup()
                                .add(radMultiple)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblMultipleDayOfWeeks)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txfMultipleDayOfWeeks, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(layout.createSequentialGroup()
                                .add(radInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblStartingDayOfWeek)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comStartingDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRepeatInterval)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblAfterwards))
                            .add(radEveryDayOfWeek)
                            .add(radNoSpecificDayOfWeek)
                            .add(layout.createSequentialGroup()
                                .add(radRange)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblFromDayOfWeek)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comFromDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblToDayOfWeek)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comToDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblOfWeek)))
                        .add(10, 10, 10)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblMultipleHint)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblChooseDayOfWeekCondition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radSingle)
                    .add(lblSingleDayOfWeek)
                    .add(comSingleDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSingleOfWeek))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radEveryDayOfWeek)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radOrdinalDayOfWeek)
                    .add(lblOrdinal)
                    .add(comOrdinalDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblOrdinalDayOfWeek)
                    .add(comOrdinal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radMultiple)
                    .add(lblMultipleDayOfWeeks)
                    .add(txfMultipleDayOfWeeks, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMultipleHint))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radInterval)
                    .add(lblStartingDayOfWeek)
                    .add(comStartingDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblRepeatInterval)
                    .add(spnRepeatInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAfterwards))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radRange)
                    .add(lblFromDayOfWeek)
                    .add(comFromDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblToDayOfWeek)
                    .add(comToDayOfWeek, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblOfWeek))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radNoSpecificDayOfWeek)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblChooseDayOfWeekCondition.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblChooseDayOfWeekCondition.text")); // NOI18N
        lblChooseDayOfWeekCondition.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblChooseDayOfWeekCondition.toolTipText")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radSingle.text")); // NOI18N
        radSingle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radSingle.toolTipText")); // NOI18N
        lblSingleDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.text")); // NOI18N
        lblSingleDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.toolTipText")); // NOI18N
        comSingleDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.text")); // NOI18N
        comSingleDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleDayOfWeek.toolTipText")); // NOI18N
        lblSingleOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleOfWeek.text")); // NOI18N
        lblSingleOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblSingleOfWeek.text")); // NOI18N
        radEveryDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radEveryDayOfWeek.text")); // NOI18N
        radEveryDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radEveryDayOfWeek.toolTipText")); // NOI18N
        radOrdinalDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radOrdinalDayOfWeek.text")); // NOI18N
        radOrdinalDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radOrdinalDayOfWeek.toolTipText")); // NOI18N
        lblOrdinal.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.text")); // NOI18N
        lblOrdinal.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.toolTipText")); // NOI18N
        comOrdinal.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.text")); // NOI18N
        comOrdinal.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinal.toolTipText")); // NOI18N
        comOrdinalDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinalDayOfWeek.text")); // NOI18N
        comOrdinalDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.comOrdinalDayOfWeek.toolTipText")); // NOI18N
        lblOrdinalDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOrdinalDayOfWeek.text")); // NOI18N
        lblOrdinalDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.comOrdinalDayOfWeek.toolTipText")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radMultiple.text")); // NOI18N
        radMultiple.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radMultiple.toolTipText")); // NOI18N
        lblMultipleDayOfWeeks.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.text")); // NOI18N
        lblMultipleDayOfWeeks.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.toolTipText")); // NOI18N
        txfMultipleDayOfWeeks.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.text")); // NOI18N
        txfMultipleDayOfWeeks.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleDayOfWeeks.toolTipText")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleHint.text")); // NOI18N
        lblMultipleHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblMultipleHint.toolTipText")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radInterval.text")); // NOI18N
        radInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radInterval.toolTipText")); // NOI18N
        lblStartingDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.a11yName")); // NOI18N
        lblStartingDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.toolTipText")); // NOI18N
        comStartingDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.text")); // NOI18N
        comStartingDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblStartingDayOfWeek.toolTipText")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.text")); // NOI18N
        lblRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.toolTipText")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.text")); // NOI18N
        spnRepeatInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblRepeatInterval.toolTipText")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblAfterwards.text")); // NOI18N
        lblAfterwards.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblAfterwards.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radRange.text")); // NOI18N
        radRange.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radRange.toolTipText")); // NOI18N
        lblFromDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.text")); // NOI18N
        lblFromDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.toolTipText")); // NOI18N
        comFromDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.text")); // NOI18N
        comFromDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblFromDayOfWeek.toolTipText")); // NOI18N
        lblToDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.text")); // NOI18N
        lblToDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.toolTipText")); // NOI18N
        comToDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.text")); // NOI18N
        comToDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblToDayOfWeek.toolTipText")); // NOI18N
        lblOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOfWeek.text")); // NOI18N
        lblOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.lblOfWeek.text")); // NOI18N
        radNoSpecificDayOfWeek.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radNoSpecificDayOfWeek.text")); // NOI18N
        radNoSpecificDayOfWeek.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronDayOfWeekPanel.class, "CronDayOfWeekPanel.radNoSpecificDayOfWeek.toolTipText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void radSingleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radSingleItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radSingleItemStateChanged

private void radOrdinalDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radOrdinalDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radOrdinalDayOfWeekItemStateChanged

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

private void radEveryDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radEveryDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radEveryDayOfWeekItemStateChanged

private void radNoSpecificDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_radNoSpecificDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        updateRadioButtonsEnabling();
    }
}//GEN-LAST:event_radNoSpecificDayOfWeekItemStateChanged

private void spnRepeatingIntervalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRepeatingIntervalStateChanged
    fireConditionPropertyChange(evt.getSource());
}//GEN-LAST:event_spnRepeatingIntervalStateChanged

private void comSingleDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comSingleDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comSingleDayOfWeekItemStateChanged

private void comOrdinalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comOrdinalItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comOrdinalItemStateChanged

private void comOrdinalDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comOrdinalDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comOrdinalDayOfWeekItemStateChanged

private void radSingleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radSingleFocusGained
    updateDescription("CronDayOfWeekPanel.radSingle.title",             //NOI18N
            "CronDayOfWeekPanel.radSingle.toolTipText");                //NOI18N
}//GEN-LAST:event_radSingleFocusGained

private void radEveryDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radEveryDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radEveryDayOfWeek.title",     //NOI18N
            "CronDayOfWeekPanel.radEveryDayOfWeek.toolTipText");        //NOI18N
}//GEN-LAST:event_radEveryDayOfWeekFocusGained

private void radOrdinalDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radOrdinalDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radOrdinalDayOfWeek.title",   //NOI18N
            "CronDayOfWeekPanel.radOrdinalDayOfWeek.toolTipText");      //NOI18N
}//GEN-LAST:event_radOrdinalDayOfWeekFocusGained

private void radMultipleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radMultipleFocusGained
    updateDescription("CronDayOfWeekPanel.radMultiple.title",           //NOI18N
            "CronDayOfWeekPanel.radMultiple.toolTipText");              //NOI18N
}//GEN-LAST:event_radMultipleFocusGained

private void txfMultipleDayOfWeeksFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMultipleDayOfWeeksFocusGained
    updateDescription("CronDayOfWeekPanel.radMultiple.title",           //NOI18N
            "CronDayOfWeekPanel.lblMultipleDayOfWeeks.toolTipText");    //NOI18N
}//GEN-LAST:event_txfMultipleDayOfWeeksFocusGained

private void comStartingDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comStartingDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comStartingDayOfWeekItemStateChanged

private void comFromDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comFromDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comFromDayOfWeekItemStateChanged

private void comToDayOfWeekItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comToDayOfWeekItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        fireConditionPropertyChange(evt.getSource());
    }
}//GEN-LAST:event_comToDayOfWeekItemStateChanged

private void radIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radIntervalFocusGained
    updateDescription("CronDayOfWeekPanel.radInterval.title",           //NOI18N
            "CronDayOfWeekPanel.radInterval.toolTipText");              //NOI18N
}//GEN-LAST:event_radIntervalFocusGained

private void radRangeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radRangeFocusGained
    updateDescription("CronDayOfWeekPanel.radRange.title",              //NOI18N
            "CronDayOfWeekPanel.radRange.toolTipText");                 //NOI18N
}//GEN-LAST:event_radRangeFocusGained

private void radNoSpecificDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radNoSpecificDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radNoSpecificDayOfWeek.title",//NOI18N
            "CronDayOfWeekPanel.radNoSpecificDayOfWeek.toolTipText");   //NOI18N
}//GEN-LAST:event_radNoSpecificDayOfWeekFocusGained

private void comSingleDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comSingleDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radSingle.title",             //NOI18N
            "CronDayOfWeekPanel.lblSingleDayOfWeek.toolTipText");       //NOI18N
}//GEN-LAST:event_comSingleDayOfWeekFocusGained

private void comOrdinalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comOrdinalFocusGained
    updateDescription("CronDayOfWeekPanel.radOrdinalDayOfWeek.title",   //NOI18N
            "CronDayOfWeekPanel.lblOrdinal.toolTipText");               //NOI18N
}//GEN-LAST:event_comOrdinalFocusGained

private void comOrdinalDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comOrdinalDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radOrdinalDayOfWeek.title",   //NOI18N
            "CronDayOfWeekPanel.comOrdinalDayOfWeek.toolTipText");      //NOI18N
}//GEN-LAST:event_comOrdinalDayOfWeekFocusGained

private void comStartingDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comStartingDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radInterval.title",           //NOI18N
            "CronDayOfWeekPanel.lblStartingDayOfWeek.toolTipText");     //NOI18N
}//GEN-LAST:event_comStartingDayOfWeekFocusGained

private void comFromDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comFromDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radRange.title",              //NOI18N
            "CronDayOfWeekPanel.lblFromDayOfWeek.toolTipText");         //NOI18N
}//GEN-LAST:event_comFromDayOfWeekFocusGained

private void comToDayOfWeekFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comToDayOfWeekFocusGained
    updateDescription("CronDayOfWeekPanel.radRange.title",              //NOI18N
            "CronDayOfWeekPanel.lblToDayOfWeek.toolTipText");           //NOI18N
}//GEN-LAST:event_comToDayOfWeekFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgpDayOfWeek;
    private javax.swing.JComboBox comFromDayOfWeek;
    private javax.swing.JComboBox comOrdinal;
    private javax.swing.JComboBox comOrdinalDayOfWeek;
    private javax.swing.JComboBox comSingleDayOfWeek;
    private javax.swing.JComboBox comStartingDayOfWeek;
    private javax.swing.JComboBox comToDayOfWeek;
    private javax.swing.JLabel lblAfterwards;
    private javax.swing.JLabel lblChooseDayOfWeekCondition;
    private javax.swing.JLabel lblFromDayOfWeek;
    private javax.swing.JLabel lblMultipleDayOfWeeks;
    private javax.swing.JLabel lblMultipleHint;
    private javax.swing.JLabel lblOfWeek;
    private javax.swing.JLabel lblOrdinal;
    private javax.swing.JLabel lblOrdinalDayOfWeek;
    private javax.swing.JLabel lblRepeatInterval;
    private javax.swing.JLabel lblSingleDayOfWeek;
    private javax.swing.JLabel lblSingleOfWeek;
    private javax.swing.JLabel lblStartingDayOfWeek;
    private javax.swing.JLabel lblToDayOfWeek;
    private javax.swing.JRadioButton radEveryDayOfWeek;
    private javax.swing.JRadioButton radInterval;
    private javax.swing.JRadioButton radMultiple;
    private javax.swing.JRadioButton radNoSpecificDayOfWeek;
    private javax.swing.JRadioButton radOrdinalDayOfWeek;
    private javax.swing.JRadioButton radRange;
    private javax.swing.JRadioButton radSingle;
    private javax.swing.JSpinner spnRepeatInterval;
    private javax.swing.JTextField txfMultipleDayOfWeeks;
    // End of variables declaration//GEN-END:variables

}
