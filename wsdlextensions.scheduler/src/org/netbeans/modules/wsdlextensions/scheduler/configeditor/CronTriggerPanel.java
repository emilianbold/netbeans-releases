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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.scheduler.model.CronConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel.TriggerDetail;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author  sunsoabi_edwong
 */
@SuppressWarnings("serial")
public class CronTriggerPanel extends AbstractTriggerPanel
        implements CronConstants, SchedulerConstants, TriggerEditor {

    private CronConditionEditor[] condEditors;
    private PropertyChangeListener cronExpUpdater;
    private DocumentListener cronExpSynchronizer;
    
    private boolean cronExpressionInvalid;
    private String priorValidCronExpression;
    
    private static String[] cronFieldValues = new String[] {
            "0", EVERY_MODIFIER, EVERY_MODIFIER,                        //NOI18N
            EVERY_MODIFIER, EVERY_MODIFIER, NONSPECIFIC_MODIFIER,
            null
        };
        
    /** Creates new form CronTriggerPanel */
    public CronTriggerPanel(DescriptionContainer descContainer,
            SchedulerModel schedulerModel) {
        super(descContainer, schedulerModel);
        
        preInitComponents();
        initComponents();
        postInitComponents();
    }
    
    private static String OUT_OF_RANGE(String field, int minVal, int maxVal) {
        return NbBundle.getMessage(CronTriggerPanel.class,
                "ERR_OUT_OF_RANGE",                                     //NOI18N
                field, Integer.toString(minVal), Integer.toString(maxVal));
    }
    
    private static String UNKNOWN_KEYWORD(String field, String[] keys) {
        return NbBundle.getMessage(CronTriggerPanel.class,
                "ERR_UNKNOWN_KEYWORD", field,                           //NOI18N
                Utils.firstSecondLastOfList(keys));
    }
    
    private static String MUTUAL_EXCLUSION(String field, String other) {
        return NbBundle.getMessage(CronTriggerPanel.class,
                "ERR_MUTUAL_EXCLUSION", field, other,                   //NOI18N
                NONSPECIFIC_MODIFIER);
    }
    
    private static String DEFINE_ONE_OR_OTHER(String field, String other) {
        return NbBundle.getMessage(CronTriggerPanel.class,
                "ERR_DEFINE_ONE_OR_OTHER", field, other,                //NOI18N
                NONSPECIFIC_MODIFIER);
    }
    
    private String underscore(boolean under, String s) {
        if (s.indexOf(under ? ' ' : '_') != -1) {                       //NOI18N
            return s.replace(under ? ' ' : '_', under ? '_' : ' ');     //NOI18N
        }
        return s;
    }
    
    @Override
    protected void preInitComponents() {
        super.preInitComponents();
        
        cronExpressionInvalid = false;
    }
    
    @Override
    protected void postInitComponents() {
        // Updates Cron Expression according to inputs in condition tabs
        cronExpUpdater = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                CronField cf = null;
                try {
                    cf = CronField.valueOf(evt.getPropertyName());
                } catch (IllegalArgumentException iae) {
                    return; // ignore
                }
                
                CronConditionEditor cce = (CronConditionEditor) evt.getSource();
                cronFieldValues[cf.getOrder()] = cce.getCondition();
                if (!txfCronExpression.isFocusOwner()) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : cronFieldValues) {
                        if (s != null) {
                            if (sb.length() > 0) {
                                sb.append(FIELD_SEP);
                            }
                            sb.append(s);
                        }
                    }
                    txfCronExpression.setText(sb.toString());
                }
                
                // Make Day (of month) and Day-of-Week mutually exclusive
                if (CronField.DAY.equals(cf)) {
                    final CronConditionEditor.MutexNonSpecific ccensDay =
                            (CronConditionEditor.MutexNonSpecific) cce;
                    final CronConditionEditor.MutexNonSpecific ccensDow =
                        (CronConditionEditor.MutexNonSpecific)
                            condEditors[CronField.DAY_OF_WEEK.getOrder()];
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (ccensDay.isNoSpecificValue()) {
                                ccensDow.revertNoSpecificValue();
                            } else {
                                ccensDow.selectNoSpecificValue();
                            }
                        }
                    });
                } else if (CronField.DAY_OF_WEEK.equals(cf)) {
                    final CronConditionEditor.MutexNonSpecific ccensDow =
                        (CronConditionEditor.MutexNonSpecific) cce;
                    final CronConditionEditor.MutexNonSpecific ccensDay =
                        (CronConditionEditor.MutexNonSpecific)
                            condEditors[CronField.DAY.getOrder()];
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (ccensDow.isNoSpecificValue()) {
                                ccensDay.revertNoSpecificValue();
                            } else {
                                ccensDay.selectNoSpecificValue();
                            }
                        }
                    });
                }
            }
        };
        
        condEditors = new CronConditionEditor[] {
            new CronSecondPanel(this),
            new CronMinutePanel(this),
            new CronHourPanel(this),
            new CronDayPanel(this),
            new CronMonthPanel(this),
            new CronDayOfWeekPanel(this),
            new CronYearPanel(this)
        };
        for (CronField f : CronField.values()) {
            tbpConditions.add((Component) condEditors[f.getOrder()],
                    f.getDisplayName(), f.getOrder());
            tbpConditions.setMnemonicAt(f.getOrder(), f.getMnemonic());
            tbpConditions.setToolTipTextAt(f.getOrder(), f.getToolTip());
            condEditors[f.getOrder()].addConditionPropertyChangeListener(
                    cronExpUpdater);
        }
        
        // Updates condition tabs according to user keying into Cron Expression
        cronExpSynchronizer = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validateExpression();
            }

            public void removeUpdate(DocumentEvent e) {
                validateExpression();
            }

            public void changedUpdate(DocumentEvent e) {}
            
            private void validateExpression() {
                // Avoid feedback if Cron Expression is view-only or
                // doesn't have focus (i.e. user is using tabs to set fields)
                if (!txfCronExpression.isEditable()
                        || !txfCronExpression.isFocusOwner()) {
                    return;
                }
                
                String expression = txfCronExpression.getText();
                if (!Utils.isEmpty(expression)) {
                    StringTokenizer st = new StringTokenizer(expression,
                            LAX_FIELD_SEP);
                    if (st.countTokens() >= (cronFieldValues.length - 1)) {
                        try {
                            expression = parseCronExpression();
                            priorValidCronExpression = expression;
                            cronExpressionInvalid = false;
                            clearError();
                            setCronExpression(expression);
                        } catch (SchedulerArgumentException sae) {
                            showError(sae);
                            cronExpressionInvalid = true;
                        }
                    }
                }
            }
        };
        txfCronExpression.getDocument().addDocumentListener(
                cronExpSynchronizer);
        
        txfMessage.setText(null);
        
        super.postInitComponents();
    }
    
    public void setFields(boolean edit, TriggerDetail td) {
        setName(td);
        txfDescription.setText(td.getDescription());
        setCronExpression(td);
        txfMessage.setText(td.getMessage());
    }

    public void captureFields(TriggerDetail td) {
        captureName(td);
        captureType(td);
        captureDescription(td);
        captureCronExpression(td);
        captureMessage(td);
    }

    public void validateFields(TriggerDetail td) {
        parseName(td);
        parseDescription();
        parseCronExpression();
        parseMessage();
    }
    
    private void setName(TriggerDetail td) {
        String name = td.getName();
        if (Utils.isEmpty(name)) {
            name = TriggerType.CRON.getBaseName();
            String tName = null;
            int suffix = 1;
            boolean unique = true;
            do {
                unique = true;
                tName = name + (suffix++);
                for (TriggerDetail t : schedulerModel.getTriggers()) {
                    if (tName.equals(t.getName())) {
                        unique = false;
                        break;
                    }
                }
            } while (!unique);
            name = tName;
        }
        txfName.setText(name);
    }
    
    private void captureName(TriggerDetail td) {
        String name = parseName(td);
        if (!Utils.equalsIgnoreCase(name, td.getName())) {
            td.setName(name);
        }
    }
    
    private String parseName(TriggerDetail td) {
        String name = Utils.trim(txfName.getText());
        if (!Utils.isEmpty(name)) {
            if (!name.equals(td.getName())) {
                for (TriggerDetail t : schedulerModel.getTriggers()) {
                    if (name.equals(t.getName())) {
                        throw new SchedulerArgumentException(
                                NbBundle.getMessage(SimpleTriggerPanel.class,
                                    "ERR_DUPLICATE_TRIGGER_NAME"),      //NOI18N
                                lblName);
                    }
                }
            }
            return name;
        }
        throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET, lblName);
    }
    
    private void captureType(TriggerDetail td) {
        if (!Utils.equalsIgnoreCase(TriggerType.CRON.getProgName(),
                td.getType())) {
            td.setType(TriggerType.CRON.getProgName());
        }
    }
    
    private void captureDescription(TriggerDetail td) {
        String description = parseDescription();
        if (!Utils.equals(description, td.getDescription())) {
            td.setDescription(description);
        }
    }
    
    private String parseDescription() {
        return Utils.trim(txfDescription.getText());
    }
    
    private void setCronExpression(TriggerDetail td) {
        String expression = td.getCronExpression();
        setCronExpression(expression);
    }
    
    private void setCronExpression(String expression) {
        if (Utils.isEmpty(expression)) {
            expression = DEFAULT_EXPRESSION;
        }
        int ed = 0;
        StringTokenizer st = new StringTokenizer(expression, LAX_FIELD_SEP);
        while (st.hasMoreTokens()) {
            String condition = st.nextToken();
            condEditors[ed].setCondition(condition);
            ed++;
        }
        if (ed != condEditors.length) {
            condEditors[condEditors.length - 1].setCondition(null);
        }
    }
    
    private String parseCronExpression() {
        return parseCronExpression(Utils.trim(txfCronExpression.getText()),
                lblCronExpression);
    }
    
    public static String parseCronExpression(String expression,
            JLabel cronExpressionLbl) {
        if (!Utils.isEmpty(expression)) {
            StringTokenizer st = new StringTokenizer(expression, LAX_FIELD_SEP);
            if (st.countTokens() < (cronFieldValues.length - 1)) {
                throw new SchedulerArgumentException(INVALID_FIELD,
                        cronExpressionLbl);
            }
            
            int i = 0;
            EnumMap<CronField, Boolean> noSpecVal =
                    new EnumMap<CronField, Boolean>(CronField.class);
            while (st.hasMoreTokens()) {
                if (!validateCronField(st.nextToken(), CronField.values()[i],
                        noSpecVal, cronExpressionLbl)) {
                    throw new SchedulerArgumentException(INVALID_FIELD,
                            cronExpressionLbl);
                }
                i++;
            }
            int countNoSpecVal = noSpecVal.size();
            if (0 == countNoSpecVal) {
                throw new SchedulerArgumentException(MUTUAL_EXCLUSION(
                        CronField.DAY.getFieldName(),
                        CronField.DAY_OF_WEEK.getFieldName()),
                        cronExpressionLbl);
            } else if (2 == countNoSpecVal) {
                throw new SchedulerArgumentException(DEFINE_ONE_OR_OTHER(
                        CronField.DAY.getFieldName(),
                        CronField.DAY_OF_WEEK.getFieldName()),
                        cronExpressionLbl);
            }
            
            return expression;
        }
        throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET,
                cronExpressionLbl);
    }
    
    private static int indexOfModifier(String str, String modifier,
            CronField field) {
        int modIdx = (Utils.isEmpty(str) ? -1 : str.indexOf(modifier));
        if (modIdx != -1) {
            // Filter out false positives, i.e. modifier character
            // is part of a legal keyword
            if (field.getKeys() != null) {
                for (String k : field.getKeys()) {
                    int keyModIdx = k.indexOf(modifier);
                    if (keyModIdx != -1) {
                        // false positive, search further down
                        if (str.regionMatches(modIdx - keyModIdx,
                                k, 0, k.length())) {
                            return indexOfModifier(str.substring(modIdx + 1),
                                    modifier, field);
                        }
                        break;
                    }
                }
            }
        }
        return modIdx;
    }
    
    private static boolean validateCronField(String str, CronField field,
            EnumMap<CronField, Boolean> noSpecVal, JLabel cronExpressionLbl) {
        
        //TODO: test for non mutual excl of day and day-of-week field
        
        if (!Utils.isEmpty(str)) {
            str = str.toUpperCase();
            StringTokenizer st = new StringTokenizer(str, DELIM);
            int nTok = st.countTokens();
            if (0 == nTok) {
                return false;
            }
            
            boolean multiple = nTok > 1;
            boolean nonSpecificValueModif =
                    (str.indexOf(NONSPECIFIC_MODIFIER) != -1);
            boolean everyModif = (str.indexOf(EVERY_MODIFIER) != -1);
            int intervalIdx = str.indexOf(INTERVAL_MODIFIER);
            boolean intervalModif = (intervalIdx != -1);
            int rangeIdx = str.indexOf(RANGE_MODIFIER);
            boolean rangeModif = (rangeIdx != -1);
            int ordinalIdx = str.indexOf(ORDINAL_MODIFIER);
            boolean ordinalModif = (ordinalIdx != -1);
            int lastIdx = indexOfModifier(str, LAST_MODIFIER, field);
            boolean lastModif = (lastIdx != -1);
            int weekdayIdx = indexOfModifier(str, WEEKDAY_MODIFIER, field);
            boolean weekdayModif = (weekdayIdx != -1);
            int modif = -1;
            
            if (nonSpecificValueModif) {
                boolean valid = (field.isNonSpecificAllowed()
                        && !multiple && (str.length() == 1));
                if (valid) {
                    noSpecVal.put(field, Boolean.TRUE);
                }
                return valid;
            }
            if (everyModif) {
                return (!multiple && (str.length() == 1));
            }
            if (intervalModif) {
                if (multiple || rangeModif || ordinalModif
                        || lastModif || weekdayModif) {
                    return false;
                }
                modif = intervalIdx;
            }
            if (rangeModif) {
                if (multiple || ordinalModif || lastModif || weekdayModif) {
                    return false;
                }
                modif = rangeIdx;
            }
            if (ordinalModif) {
                if (!field.isOrdinalityAllowed() || multiple || rangeModif
                        || intervalModif || lastModif || weekdayModif) {
                    return false;
                }
                modif = ordinalIdx;
            }
            if (lastModif) {
                if (!field.isLastAllowed() || multiple || rangeModif
                        || intervalModif || ordinalModif || weekdayModif) {
                    return false;
                }
            }
            if (weekdayModif) {
                if (!field.isWeekdayAllowed() || multiple || rangeModif
                        || intervalModif || ordinalModif) {
                    return false;
                }
            }
            
            do {
                String tok = st.nextToken();
                int[] pairs = parseFieldToken(tok, modif, field,
                        cronExpressionLbl);
                for (int i = 0; i < pairs.length; i++) {
                    int p = pairs[i];
                    if (p > -1) {
                        if ((0 == i) || rangeModif) {
                            if ((p < field.getIntRange()[0])
                                    || (p > field.getIntRange()[1])) {
                                throw new SchedulerArgumentException(
                                        OUT_OF_RANGE(field.getFieldName(),
                                                field.getIntRange()[0],
                                                field.getIntRange()[1]),
                                        cronExpressionLbl);
                            }
                        } else if (1 == i) {
                            if (intervalModif) {
                                int delta = field.getIntRange()[1]
                                        - field.getIntRange()[0];
                                if ((p < 1) || (p > delta)) {
                                    return false;
                                }
                            } else if (ordinalModif) {
                                if ((p < 1) || (p > field.getMaxOrdinality())) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                }
                modif = -1;
            } while (st.hasMoreTokens());
            
            return true;
        }
        return false;
    }
    
    private static int[] parseFieldToken(String tok, int modif,
            CronField field, JLabel cronExpressionLbl) {
        String before = (modif != -1) ? tok.substring(0, modif) : tok;
        String after = (modif != -1) ? tok.substring(modif + 1) : null;
        int[] results = new int[] {-1, -1};
        try {
            results[0] = Integer.parseInt(before);
        } catch (NumberFormatException nfe) {
            if (field.getKeys() != null) {
                int i = 0;
                for (String k : field.getKeys()) {
                    if (k.equalsIgnoreCase(before)) {
                        results[0] = field.getIntRange()[0] + i;
                        break;
                    }
                    i++;
                }
                if (-1 == results[0]) {
                    throw new SchedulerArgumentException(
                            UNKNOWN_KEYWORD(field.getFieldName(),
                                    field.getKeys()), cronExpressionLbl);
                }
            } else {
                throw new SchedulerArgumentException(INVALID_FIELD,
                        cronExpressionLbl);
            }
        }
        
        if (after != null) {
            try {
                results[1] = Integer.parseInt(after);
            } catch (NumberFormatException nfe) {
                if (tok.charAt(modif) == RANGE_MODIFIER.charAt(0)) {
                    int i = 0;
                    for (String k : field.getKeys()) {
                        if (k.equalsIgnoreCase(after)) {
                            results[1] = field.getIntRange()[0] + i;
                            break;
                        }
                        i++;
                    }
                    if (-1 == results[1]) {
                        throw new SchedulerArgumentException(
                                UNKNOWN_KEYWORD(field.getFieldName(),
                                        field.getKeys()), cronExpressionLbl);
                    }
                } else {
                    throw new SchedulerArgumentException(INVALID_FIELD,
                            cronExpressionLbl);
                }
            }
        }
        
        return results;
    }
    
    private void captureCronExpression(TriggerDetail td) {
        String expression = parseCronExpression();
        if (!Utils.equalsIgnoreCase(expression, td.getCronExpression())) {
            td.setCronExpression(expression);
        }
    }
    
    private String parseMessage() {
        String message = Utils.trim(txfMessage.getText());
        if (!Utils.isEmpty(message)) {
            return message;
        }
        throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET,
                lblMessage);
    }
    
    private void captureMessage(TriggerDetail td) {
        String message = parseMessage();
        if (!Utils.equals(message, td.getMessage())) {
            td.setMessage(message);
        }
    }
    
    public boolean isCronExpressionValid() {
        return !cronExpressionInvalid;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        txfName = new javax.swing.JTextField();
        lblDescription = new javax.swing.JLabel();
        txfDescription = new javax.swing.JTextField();
        lblSpecifyConditions = new javax.swing.JLabel();
        tbpConditions = new javax.swing.JTabbedPane();
        lblCronExpressionHint = new javax.swing.JLabel();
        lblCronExpression = new javax.swing.JLabel();
        txfCronExpression = new javax.swing.JTextField();
        togEditView = new javax.swing.JToggleButton();
        lblMessage = new javax.swing.JLabel();
        txfMessage = new javax.swing.JTextField();
        javax.swing.JLabel _lblErrorDisplay = new ErrorDisplayLabel();
        lblErrorDisplay = _lblErrorDisplay;

        lblName.setLabelFor(txfName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.text")); // NOI18N
        lblName.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.toolTipText")); // NOI18N

        txfName.setText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.txfName.text")); // NOI18N
        txfName.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.toolTipText")); // NOI18N
        txfName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfNameFocusGained(evt);
            }
        });

        lblDescription.setLabelFor(txfDescription);
        org.openide.awt.Mnemonics.setLocalizedText(lblDescription, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.text")); // NOI18N
        lblDescription.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.toolTipText")); // NOI18N

        txfDescription.setText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.txfDescription.text")); // NOI18N
        txfDescription.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.toolTipText")); // NOI18N
        txfDescription.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfDescriptionFocusGained(evt);
            }
        });

        lblSpecifyConditions.setLabelFor(tbpConditions);
        org.openide.awt.Mnemonics.setLocalizedText(lblSpecifyConditions, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblSpecifyConditions.text")); // NOI18N
        lblSpecifyConditions.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblSpecifyConditions.toolTipText")); // NOI18N

        lblCronExpressionHint.setLabelFor(txfCronExpression);
        org.openide.awt.Mnemonics.setLocalizedText(lblCronExpressionHint, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpressionHint.text")); // NOI18N
        lblCronExpressionHint.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpressionHint.toolTipText")); // NOI18N
        lblCronExpressionHint.setEnabled(false);

        lblCronExpression.setLabelFor(txfCronExpression);
        org.openide.awt.Mnemonics.setLocalizedText(lblCronExpression, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpression.text")); // NOI18N
        lblCronExpression.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpression.toolTipText")); // NOI18N

        txfCronExpression.setEditable(false);
        txfCronExpression.setText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.txfCronExpression.text")); // NOI18N
        txfCronExpression.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpression.toolTipText")); // NOI18N
        txfCronExpression.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfCronExpressionFocusGained(evt);
            }
        });

        togEditView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/pencil16x16.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(togEditView, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.togEditView.text")); // NOI18N
        togEditView.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.togEditView.toolTipText")); // NOI18N
        togEditView.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/eye16x16.png"))); // NOI18N
        togEditView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togEditViewActionPerformed(evt);
            }
        });

        lblMessage.setLabelFor(txfMessage);
        org.openide.awt.Mnemonics.setLocalizedText(lblMessage, NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.text")); // NOI18N
        lblMessage.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.toolTipText")); // NOI18N

        txfMessage.setText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.txfMessage.text")); // NOI18N
        txfMessage.setToolTipText(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.toolTipText")); // NOI18N
        txfMessage.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMessageFocusGained(evt);
            }
        });

        _lblErrorDisplay.setForeground(new java.awt.Color(255, 0, 0));
        _lblErrorDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/error16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(_lblErrorDisplay, "to be replaced");
        _lblErrorDisplay.setToolTipText(org.openide.util.NbBundle.getMessage(CronTriggerPanel.class, "AbstractTriggerPanel.lblErrorDisplay.toolTipText")); // NOI18N
        _lblErrorDisplay.setMinimumSize(new java.awt.Dimension(485, 41));
        _lblErrorDisplay.setPreferredSize(new java.awt.Dimension(485, 41));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tbpConditions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblCronExpression)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(txfCronExpression, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(togEditView, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(lblCronExpressionHint)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblName)
                            .add(lblDescription))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(txfName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txfDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)))
                    .add(lblSpecifyConditions)
                    .add(layout.createSequentialGroup()
                        .add(lblMessage)
                        .add(42, 42, 42)
                        .add(txfMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
                    .add(_lblErrorDisplay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(txfName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblDescription)
                    .add(txfDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lblSpecifyConditions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tbpConditions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 254, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lblCronExpressionHint)
                .add(1, 1, 1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCronExpression)
                    .add(togEditView)
                    .add(txfCronExpression, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMessage)
                    .add(txfMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(_lblErrorDisplay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.text")); // NOI18N
        lblName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.toolTipText")); // NOI18N
        txfName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.text")); // NOI18N
        txfName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblName.toolTipText")); // NOI18N
        lblDescription.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.text")); // NOI18N
        lblDescription.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.toolTipText")); // NOI18N
        txfDescription.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.text")); // NOI18N
        txfDescription.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblDescription.toolTipText")); // NOI18N
        lblSpecifyConditions.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblSpecifyConditions.text")); // NOI18N
        lblSpecifyConditions.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblSpecifyConditions.toolTipText")); // NOI18N
        lblCronExpressionHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpressionHint.text")); // NOI18N
        lblCronExpressionHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpressionHint.toolTipText")); // NOI18N
        txfCronExpression.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpression.text")); // NOI18N
        txfCronExpression.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblCronExpression.toolTipText")); // NOI18N
        togEditView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.togEditView.a11y.text")); // NOI18N
        togEditView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.togEditView.toolTipText")); // NOI18N
        lblMessage.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.text")); // NOI18N
        lblMessage.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.toolTipText")); // NOI18N
        txfMessage.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.text")); // NOI18N
        txfMessage.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CronTriggerPanel.class, "CronTriggerPanel.lblMessage.toolTipText")); // NOI18N
        _lblErrorDisplay.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CronTriggerPanel.class, "AbstractTriggerPanel.lblErrorDisplay.toolTipText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void togEditViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togEditViewActionPerformed
    boolean selected = togEditView.isSelected();
    txfCronExpression.setEditable(selected);
    if (selected) {
        priorValidCronExpression = txfCronExpression.getText();
        cronExpressionInvalid = false;
        if (!txfCronExpression.isFocusOwner()) {
            txfCronExpression.requestFocusInWindow();
            txfCronExpression.setCaretPosition(
                    priorValidCronExpression.length());
        }
    } else {
        if (cronExpressionInvalid) {
            txfCronExpression.setText(priorValidCronExpression);
            clearError();
            cronExpressionInvalid = false;
        }
    }
}//GEN-LAST:event_togEditViewActionPerformed

private void txfCronExpressionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfCronExpressionFocusGained
    updateDescription("CronTriggerPanel.lblCronExpression.text",        //NOI18N
            "CronTriggerPanel.lblCronExpression.toolTipText");          //NOI18N
}//GEN-LAST:event_txfCronExpressionFocusGained

private void txfMessageFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMessageFocusGained
    updateDescription("TLE_MESSAGE", "DESC_MESSAGE");                   //NOI18N
}//GEN-LAST:event_txfMessageFocusGained

private void txfNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfNameFocusGained
    updateDescription("TLE_NAME", "DESC_NAME");                         //NOI18N
}//GEN-LAST:event_txfNameFocusGained

private void txfDescriptionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfDescriptionFocusGained
    updateDescription("TLE_DESCRIPTION", "DESC_DESCRIPTION");           //NOI18N
}//GEN-LAST:event_txfDescriptionFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCronExpression;
    private javax.swing.JLabel lblCronExpressionHint;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblSpecifyConditions;
    private javax.swing.JTabbedPane tbpConditions;
    private javax.swing.JToggleButton togEditView;
    private javax.swing.JTextField txfCronExpression;
    private javax.swing.JTextField txfDescription;
    private javax.swing.JTextField txfMessage;
    private javax.swing.JTextField txfName;
    // End of variables declaration//GEN-END:variables

}
