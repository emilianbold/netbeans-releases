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

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.StringTokenizer;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.UIManager;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel.TriggerDetail;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author  sunsoabi_edwong
 */
public class SimpleTriggerPanel extends AbstractTriggerPanel
        implements SchedulerConstants, TriggerEditor {

    private ComboBoxModel repeatComboModel;
        
    /** Creates new form SimpleTriggerPanel */
    public SimpleTriggerPanel(DescriptionContainer descContainer,
            SchedulerModel schedulerModel) {
        super(descContainer, schedulerModel);
        
        preInitComponents();
        initComponents();
        postInitComponents();
    }
    
    @Override
    protected void preInitComponents() {
        super.preInitComponents();
        
        repeatComboModel = new DefaultComboBoxModel(new String[] {
            INDEFINITE_I18N_VAL,
        });
    }
    
    @Override
    protected void postInitComponents() {
        super.postInitComponents();
    }
        
    public void setFields(boolean typeEditable, TriggerDetail td) {
        setName(td);
        txfDescription.setText(td.getDescription());
        setRepeat(td);
        setInterval(td);
        txfMessage.setText(td.getMessage());
    }
    
    public void captureFields(TriggerDetail td) {
        captureName(td);
        captureType(td);
        captureDescription(td);
        captureRepeat(td);
        captureInterval(td);
        captureMessage(td);
    }
    
    public void validateFields(TriggerDetail td) {
        parseName(td);
        parseDescription();
        parseRepeat();
        parseInterval();
        parseMessage();
    }
    
    private void setName(TriggerDetail td) {
        String name = td.getName();
        if (Utils.isEmpty(name)) {
            name = TriggerType.SIMPLE.getBaseName();
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
        if (!Utils.equalsIgnoreCase(TriggerType.SIMPLE.getProgName(),
                td.getType())) {
            td.setType(TriggerType.SIMPLE.getProgName());
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
    
    private void setRepeat(TriggerDetail td) {
        String repeat = td.getRepeat();
        if (Utils.isEmpty(repeat)) {
            repeat = INDEFINITE_VAL;
        }
        if (INDEFINITE_VAL.equalsIgnoreCase(repeat)) {
            repeat = INDEFINITE_I18N_VAL;
        }
        comRepeat.setSelectedItem(repeat);
    }
    
    private void captureRepeat(TriggerDetail td) {
        String repeat = parseRepeat();
        if (!Utils.equalsIgnoreCase(repeat, td.getRepeat())) {
            td.setRepeat(repeat);
        }
    }
    
    private String parseRepeat() {
        String repeat = Utils.trim((String) comRepeat.getSelectedItem());
        if (!Utils.isEmpty(repeat)) {
            if (INDEFINITE_I18N_VAL.equalsIgnoreCase(repeat)) {
                repeat = INDEFINITE_VAL;
            } else {
                try {
                    int repeatInt = Integer.parseInt(repeat);
                    repeat = Integer.toString(repeatInt);
                } catch (NumberFormatException nfe) {
                    throw new SchedulerArgumentException(INVALID_FIELD,
                            lblRepeat);
                }
            }
            return repeat;
        }
        throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET, lblRepeat);
    }
    
    private void setInterval(TriggerDetail td) {
        long interval = td.getInterval();
        if (0L == interval) {
            txfInterval.setText(null);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = (TimeUnit.values().length - 1); i >= 0; i--) {
                TimeUnit tu = TimeUnit.values()[i];
                int factor = tu.getFactor();
                long num = interval / factor;
                if (((0 == num) && (sb.length() > 0))
                        || (num > 0)) {
                    if (sb.length() > 0) {
                        sb.append(':');                                 //NOI18N
                    }
                    sb.append(num);
                }
                interval -= (num * factor);
            }
            if (interval > 0) {
                if (sb.length() == 0) {
                    sb.append(0);
                }
                sb.append('.').append(String.format("%03d", interval)); //NOI18N
            }
            txfInterval.setText(sb.toString());
        }
    }
    
    private void captureInterval(TriggerDetail td) {
        long interval = parseInterval();
        if (td.getInterval() != interval) {
            td.setInterval(interval);
        }
    }
    
    private long parseInterval() {
        long interval = 0L;
        String intervalStr = Utils.trim(txfInterval.getText());
        if (!Utils.isEmpty(intervalStr)) {
            StringTokenizer strTok =
                    new StringTokenizer(intervalStr, ":");              //NOI18N
            int nUnits = strTok.countTokens();
            if (nUnits > TimeUnit.values().length) {
                throw new SchedulerArgumentException(INVALID_FIELD,
                        lblInterval);
            }
            
            int unitIdx = nUnits - 1;
            while (strTok.hasMoreTokens()) {
                String part = strTok.nextToken();
                if (unitIdx >= 0) {
                    int factor = TimeUnit.values()[unitIdx].getFactor();
                    String i18nName = TimeUnit.values()[unitIdx].getI18nName();
                    if (0 == unitIdx) {
                        try {
                            Double num = Double.parseDouble(part);
                            if (num < 0.0) {
                                throw new SchedulerArgumentException(
                                    NbBundle.getMessage(
                                            SimpleTriggerPanel.class,
                                            "ERR_NEG_TIME_QUANTITY",    //NOI18N
                                            num, i18nName), lblInterval);
                            }
                            interval += (num * factor);
                       } catch (NumberFormatException nfe) {
                            throw new SchedulerArgumentException(
                                    INVALID_FIELD, lblInterval);
                        }
                    } else {
                        try {
                            long num = Long.parseLong(part);
                            if (num < 0L) {
                                throw new SchedulerArgumentException(
                                    NbBundle.getMessage(
                                            SimpleTriggerPanel.class,
                                            "ERR_NEG_TIME_QUANTITY",    //NOI18N
                                            num, i18nName), lblInterval);
                            }
                            interval += (num * factor);
                        } catch (NumberFormatException nfe) {
                            throw new SchedulerArgumentException(
                                    INVALID_FIELD, lblInterval);
                        }
                    }
                }
                unitIdx--;
            }
            return interval;
        }
        throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET,
                lblInterval);
    }
    
    private void captureMessage(TriggerDetail td) {
        String message = parseMessage();
        if (!message.equals(td.getMessage())) {
            td.setMessage(message);
        }
    }
    
    private String parseMessage() {
        String  message = Utils.trim(txfMessage.getText());
        if (Utils.isEmpty(message)) {
            throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET,
                    lblMessage);
        }
        return message;
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
        lblRepeat = new javax.swing.JLabel();
        comRepeat = new javax.swing.JComboBox();
        lblIntervalHint = new javax.swing.JLabel();
        lblIntervalHintSeconds = new javax.swing.JLabel();
        lblIntervalHintMilliseconds = new javax.swing.JLabel();
        lblInterval = new javax.swing.JLabel();
        txfInterval = new javax.swing.JTextField();
        lblMessage = new javax.swing.JLabel();
        txfMessage = new javax.swing.JTextField();
        javax.swing.JLabel _lblErrorDisplay = new ErrorDisplayLabel();
        lblErrorDisplay = _lblErrorDisplay;

        lblName.setLabelFor(txfName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.text")); // NOI18N
        lblName.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.toolTipText")); // NOI18N

        txfName.setText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.txfName.text")); // NOI18N
        txfName.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.toolTipText")); // NOI18N
        txfName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfNameFocusGained(evt);
            }
        });

        lblDescription.setLabelFor(txfDescription);
        org.openide.awt.Mnemonics.setLocalizedText(lblDescription, NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.text")); // NOI18N
        lblDescription.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.toolTipText")); // NOI18N

        txfDescription.setText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.txfDescription.text")); // NOI18N
        txfDescription.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.toolTipText")); // NOI18N
        txfDescription.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfDescriptionFocusGained(evt);
            }
        });

        lblRepeat.setLabelFor(comRepeat);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepeat, NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.text")); // NOI18N
        lblRepeat.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.toolTipText")); // NOI18N

        comRepeat.setEditable(true);
        comRepeat.setModel(repeatComboModel);
        comRepeat.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.toolTipText")); // NOI18N
        comRepeat.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescription("TLE_REPEAT", "DESC_REPEAT"); //NOI18N
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblIntervalHint, NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHint.text")); // NOI18N
        lblIntervalHint.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHint.toolTipText")); // NOI18N
        lblIntervalHint.setEnabled(false);
        if (Utils.isHtml(lblIntervalHint.getText())) {
            lblIntervalHint.setForeground((Color)
                UIManager.getDefaults().get(lblIntervalHint.isEnabled()
                    ? "Label.foreground" : "Label.disabledForeground"));   //NOI18N
        }

        lblIntervalHintSeconds.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblIntervalHintSeconds.setLabelFor(txfInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblIntervalHintSeconds, org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintSeconds.text")); // NOI18N
        lblIntervalHintSeconds.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintSeconds.toolTipText")); // NOI18N
        lblIntervalHintSeconds.setEnabled(false);

        lblIntervalHintMilliseconds.setLabelFor(txfInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblIntervalHintMilliseconds, org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintMilliseconds.text")); // NOI18N
        lblIntervalHintMilliseconds.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintMilliseconds.toolTipText")); // NOI18N
        lblIntervalHintMilliseconds.setEnabled(false);

        lblInterval.setLabelFor(txfInterval);
        org.openide.awt.Mnemonics.setLocalizedText(lblInterval, NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.text")); // NOI18N
        lblInterval.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.toolTipText")); // NOI18N

        txfInterval.setText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.txfInterval.text")); // NOI18N
        txfInterval.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.toolTipText")); // NOI18N
        txfInterval.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfIntervalFocusGained(evt);
            }
        });

        lblMessage.setLabelFor(txfMessage);
        org.openide.awt.Mnemonics.setLocalizedText(lblMessage, NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblMessage.text")); // NOI18N
        lblMessage.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblMessage.toolTipText")); // NOI18N

        txfMessage.setText(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.txfMessage.text")); // NOI18N
        txfMessage.setToolTipText(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblMessage.toolTipText")); // NOI18N
        txfMessage.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfMessageFocusGained(evt);
            }
        });

        _lblErrorDisplay.setForeground(new java.awt.Color(255, 0, 0));
        _lblErrorDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/error16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(_lblErrorDisplay, "to be replaced"); // NOI18N
        _lblErrorDisplay.setToolTipText(NbBundle.getMessage(SimpleTriggerPanel.class, "AbstractTriggerPanel.lblErrorDisplay.toolTipText")); // NOI18N
        _lblErrorDisplay.setMinimumSize(new java.awt.Dimension(485, 41));
        _lblErrorDisplay.setPreferredSize(new java.awt.Dimension(485, 41));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(_lblErrorDisplay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                    .add(lblDescription)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblName)
                            .add(lblRepeat)
                            .add(lblInterval)
                            .add(lblMessage))
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txfMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, txfInterval, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .add(comRepeat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(txfName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(lblIntervalHint)
                                .add(0, 0, 0)
                                .add(lblIntervalHintSeconds)
                                .add(0, 0, 0)
                                .add(lblIntervalHintMilliseconds))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, txfDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE))))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblRepeat)
                    .add(comRepeat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblIntervalHint)
                    .add(lblIntervalHintSeconds)
                    .add(lblIntervalHintMilliseconds))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblInterval)
                    .add(txfInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMessage)
                    .add(txfMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(_lblErrorDisplay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.text")); // NOI18N
        lblName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.toolTipText")); // NOI18N
        txfName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.text")); // NOI18N
        txfName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblName.toolTipText")); // NOI18N
        lblDescription.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.text")); // NOI18N
        lblDescription.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.toolTipText")); // NOI18N
        txfDescription.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.text")); // NOI18N
        txfDescription.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblDescription.toolTipText")); // NOI18N
        lblRepeat.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.text")); // NOI18N
        lblRepeat.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.toolTipText")); // NOI18N
        comRepeat.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.text")); // NOI18N
        comRepeat.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblRepeat.toolTipText")); // NOI18N
        lblIntervalHint.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHint.text")); // NOI18N
        lblIntervalHint.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHint.toolTipText")); // NOI18N
        lblIntervalHintSeconds.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintSeconds.text")); // NOI18N
        lblIntervalHintSeconds.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintSeconds.toolTipText")); // NOI18N
        lblIntervalHintMilliseconds.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintMilliseconds.text")); // NOI18N
        lblIntervalHintMilliseconds.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblIntervalHintMilliseconds.toolTipText")); // NOI18N
        lblInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.text")); // NOI18N
        lblInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.toolTipText")); // NOI18N
        txfInterval.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.text")); // NOI18N
        txfInterval.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblInterval.toolTipText")); // NOI18N
        lblMessage.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblMessage.text")); // NOI18N
        lblMessage.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "SimpleTriggerPanel.lblMessage.toolTipText")); // NOI18N
        _lblErrorDisplay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleTriggerPanel.class, "AbstractTriggerPanel.lblErrorDisplay.toolTipText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void txfMessageFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfMessageFocusGained
    updateDescription("TLE_MESSAGE", "DESC_MESSAGE");                   //NOI18N
}//GEN-LAST:event_txfMessageFocusGained

private void txfIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfIntervalFocusGained
    updateDescription("TLE_INTERVAL", "DESC_INTERVAL");                 //NOI18N
}//GEN-LAST:event_txfIntervalFocusGained

private void txfNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfNameFocusGained
    updateDescription("TLE_NAME", "DESC_NAME");                         //NOI18N
}//GEN-LAST:event_txfNameFocusGained

private void txfDescriptionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfDescriptionFocusGained
    updateDescription("TLE_DESCRIPTION", "DESC_DESCRIPTION");           //NOI18N
}//GEN-LAST:event_txfDescriptionFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comRepeat;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblInterval;
    private javax.swing.JLabel lblIntervalHint;
    private javax.swing.JLabel lblIntervalHintMilliseconds;
    private javax.swing.JLabel lblIntervalHintSeconds;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblRepeat;
    private javax.swing.JTextField txfDescription;
    private javax.swing.JTextField txfInterval;
    private javax.swing.JTextField txfMessage;
    private javax.swing.JTextField txfName;
    // End of variables declaration//GEN-END:variables

}
