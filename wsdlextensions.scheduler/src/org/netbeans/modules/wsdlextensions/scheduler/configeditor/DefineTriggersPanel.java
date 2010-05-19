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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  sunsoabi_edwong
 */
public class DefineTriggersPanel extends JPanel
        implements SchedulerConstants {
    
    private DescriptionContainer descriptionContainer;
    private SchedulerModel schedulerModel;
    private SimpleDateFormatChooser.DateTimeFormatComboBoxModel
            dateFormatComboModel;
    private SimpleDateFormatChooser dateFormatChooser;
    private DateEditor startDateTimeEditor;
    private DateEditor endDateTimeEditor;
    private TriggerTableViewController tableController;
    private TableModel triggerTableModel;
    private ComboBoxModel addTriggerComboModel;
    private boolean inInit;
    private JLabel highlitLabel;
    private Boolean validState = null;
    private ComboBoxModel timeZoneModel;
    private String defaultTZ;
    private boolean dontAskRemove = false;
    
    private static final SimpleDateFormat DEFAULT_SIMPLE_DATE_FORMAT =
            new SimpleDateFormat();
    private static final String LBL_CUSTOM =
            SimpleDateFormatChooser.PLAIN_TEXT_ITEM_PREFIX + "CUSTOM";  //NOI18N
    private static final Color NORMAL_FOREGROUND = (new JLabel("xxx"))  //NOI18N
            .getForeground();
    private static final Color ERROR_FOREGROUND = Color.RED;
    private static final String REQUIRED_FIELD_NOT_SET = 
            NbBundle.getMessage(DefineTriggersPanel.class,
                "ERR_REQUIRED_FIELD_NOT_SPECIFIED");                    //NOI18N
    
    /** Creates new form DefineTriggersPanel */
    public DefineTriggersPanel() {
        super();
    }
    
    public DefineTriggersPanel(DescriptionContainer descriptionContainer,
            SchedulerModel schedulerModel) {
        super();
        this.descriptionContainer = descriptionContainer;
        setSchedulerModel(schedulerModel);
    }
    
    public void setSchedulerModel(SchedulerModel schedulerModel) {
        this.schedulerModel = schedulerModel;
        preInitComponents();
        initComponents();
        postInitComponents();
    }
    
    public Boolean getValidState() {
        return validState;
    }
    
    private String getAddTriggerType() {
        String type = (String) comAddTrigger.getSelectedItem();
        if (type.endsWith("...")) {                                     //NOI18N
            type = type.substring(0, type.length() - 3);
        }
        return type;
    }
    
    private void setAddTriggerType(String type) {
        if (!type.endsWith("...")) {                                    //NOI18N
            type = type + "...";                                        //NOI18N
        }
        comAddTrigger.setSelectedItem(type);
    }
    
    private class DynamicButton extends JButton {

        @Override
        public String getToolTipText(MouseEvent event) {
            return NbBundle.getMessage(DefineTriggersPanel.class,
                    "DefineTriggersPanel.btnAddTrigger.toolTipText_0",  //NOI18N
                    getAddTriggerType());
        }
    }
    
    private String underscore(boolean under, String s) {
        if (s.indexOf(under ? ' ' : '_') != -1) {                       //NOI18N
            return s.replace(under ? ' ' : '_', under ? '_' : ' ');     //NOI18N
        }
        return s;
    }
    
    private class DynamicLabel extends JLabel {

        @Override
        public String getToolTipText(MouseEvent event) {
            Point pt = event.getPoint();
            if ((pt.getX() < 16) && (pt.getY() < 16)) {
                return NbBundle.getMessage(DefineTriggersPanel.class,
                        "DefineTriggersPanel.DynamicLabel.toolTipText");//NOI18N
            }
            return super.getToolTipText(event);
        }
    }
    
    private void preInitComponents() {
        dateFormatComboModel = new SimpleDateFormatChooser
            .DateTimeFormatComboBoxModel(new String[][] {
                {LBL_CUSTOM, "LBL_CUSTOM_HINT"},                        //NOI18N
                {DEFAULT_SIMPLE_DATE_FORMAT.toPattern(),
                         "SDF_DEFAULT_LOCALE"},                         //NOI18N
                {"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "SDF_W3C_XML_DATETIME"}, //NOI18N
        });
        dateFormatChooser = new SimpleDateFormatChooser();
        
        triggerTableModel = new TriggerTableModel(schedulerModel);
        
        addTriggerComboModel = new DefaultComboBoxModel(new String[] {
            TriggerType.SIMPLE.getI18nName() + "...",                   //NOI18N
            TriggerType.CRON.getI18nName() + "...",                     //NOI18N
            TriggerType.HYBRID.getI18nName() + "...",                   //NOI18N
        });
        
        String[] availableIDs = TimeZone.getAvailableIDs();
        Arrays.sort(availableIDs);
        for (int i = 0; i < availableIDs.length; i++) {
            availableIDs[i] = underscore(false, availableIDs[i]);
        }
        timeZoneModel = new DefaultComboBoxModel(availableIDs);
        defaultTZ = underscore(false, TimeZone.getDefault().getID());
    }
    
    private void postInitComponents() {
        inInit = true;
        
        descriptionContainer.setOther(this);
        
        SimpleDateFormatChooser.DateTimeFormatComboBoxRenderer renderer =
                new SimpleDateFormatChooser.DateTimeFormatComboBoxRenderer(
                        dateFormatComboModel);
        comDateFormat.setRenderer(renderer);
        
        spnStart.setValue(new Date());
        spnEnd.setValue(new Date());

        tableController =
                new TriggerTableViewController(scrTriggers, tblTriggers);
        tblTriggers.getModel().addTableModelListener(
                tableController.getTableModelListener());
        schedulerModel.addPropertyChangeListener(
                tableController.getTablePropertyChangeListener());
        schedulerModel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                String propKey = evt.getPropertyName();
                if (TRIGGER_ADDED.equals(propKey)
                        || TRIGGER_REMOVED.equals(propKey)) {
                    handleValidation();
                }
            }
        });
        
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {

            @Override
            public void ancestorResized(HierarchyEvent e) {
                tableController.adjustTriggersTable();
            }
        });
        
        
        ListSelectionModel rowLSM = tblTriggers.getSelectionModel();
        rowLSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()
                        || (schedulerModel.getTriggers().size() == 0)) {
                    btnEdit.setEnabled(false);
                    btnRemove.setEnabled(false);
                } else {
                    btnRemove.setEnabled(true);
                    btnEdit.setEnabled(lsm.getMaxSelectionIndex()
                            == lsm.getMinSelectionIndex());
                }
            }
        });
        
        Dimension maxDim = Utils.max(btnEdit.getPreferredSize(),
                btnRemove.getPreferredSize());
        btnEdit.setPreferredSize(maxDim);
        btnEdit.setMinimumSize(maxDim);
        btnRemove.setPreferredSize(maxDim);
        btnRemove.setMinimumSize(maxDim);
        btnEdit.setEnabled(false);
        btnRemove.setEnabled(false);
        
        setFields();
        TriggerType type = TriggerType.toEnum(Utils.getSchedulerPrefs()
                .get(TRIGGER_TYPE_KEY, TriggerType.SIMPLE.getProgName()));
        if (type != null) {
            setAddTriggerType(type.getI18nName());
        }
        
        txfGroup.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                handleValidation();
            }

            public void removeUpdate(DocumentEvent e) {
                handleValidation();
            }

            public void changedUpdate(DocumentEvent e) {}
        });
        
        inInit = false;
    }
    
    private void leftAlign(JSpinner spn) {
        JComponent editor = spn.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField ftf = ((JSpinner.DefaultEditor) editor)
                    .getTextField();
            ftf.setHorizontalAlignment(JTextField.LEFT);
        }
    }
    
    public void updateDescription(String titleKey, String descKey) {
        descriptionContainer.setDescription(NbBundle.getMessage(
                DefineTriggersPanel.class, titleKey),
                NbBundle.getMessage(DefineTriggersPanel.class, descKey));
    }
    
    public void setFields() {
        setGroup();
        setDateFormat();
        setStart();
        setEnd();
        setTimeZone();
    }
    
    public void captureFields() {
        captureGroup();
        captureDateFormat();
        captureStart();
        captureEnd();
        captureTimeZone();
    }
    
    public void validateFields() {
        try {
            parseGroup();
            parseDateFormat();
            parseStart();
            parseEnd();
            parseTimeZone();
            parseTriggers();
            validState = Boolean.TRUE;
        } catch (SchedulerArgumentException sae) {
            validState = Boolean.FALSE;
            throw sae;
        }
    }
    
    private void enableNextFinish(String warning) {
        descriptionContainer.broadcastPropertyChange(
                ExtensibilityElementConfigurationEditorComponent
                        .PROPERTY_CLEAR_MESSAGES_EVT, null, "");        //NOI18N
        if (!Utils.isEmpty(warning)) {
            descriptionContainer.broadcastPropertyChange(
                ExtensibilityElementConfigurationEditorComponent
                        .PROPERTY_WARNING_EVT, null, warning);
        }
    }
    
    private void disableNextFinish(String error) {
        descriptionContainer.broadcastPropertyChange(
                ExtensibilityElementConfigurationEditorComponent
                        .PROPERTY_ERROR_EVT, null, error);
    }
    
    public boolean handleValidation() {
        if (inInit) {
            return true;
        }
        
        boolean valid = false;
        try {
            validateFields();
            enableNextFinish("");                                       //NOI18N
            highlightLabel(false, null);
            valid = true;
        } catch (SchedulerArgumentException sae) {
            String errMsg = sae.getMessage();
            JLabel errLabel = (sae.getReference() instanceof JLabel)
                    ? (JLabel) sae.getReference() : null;
            if (Utils.isEmpty(errMsg)) {
                errMsg = NbBundle.getMessage(DefineTriggersPanel.class,
                        "ERR_INVALID_DATA_SPECIFIED");                  //NOI18N
            }
            disableNextFinish(errMsg);
            highlightLabel(true, errLabel);
        }
        return valid;
    }
    
    private void highlightLabel(boolean highlight, JLabel label) {
        if ((highlitLabel != null) && (highlitLabel != label)) {
            highlitLabel.setForeground(NORMAL_FOREGROUND);
        }
        if (highlight && (label != null) && (highlitLabel != label)) {
            label.setForeground(ERROR_FOREGROUND);
        }
        highlitLabel = label;
    }
    
    private void setGroup() {
        txfGroup.setText(Utils.trim(schedulerModel.getGroup()));
    }
    
    private void captureGroup() {
        String group = parseGroup();
        if (!Utils.equals(group, schedulerModel.getGroup())) {
            schedulerModel.setGroup(group);
        }
    }
    
    private String parseGroup() {
        String group = Utils.trim(txfGroup.getText());
        if (Utils.isEmpty(group)) {
            throw new SchedulerArgumentException(NbBundle.getMessage(
                    DefineTriggersPanel.class, "ERR_GROUP_EMPTY"),      //NOI18N
                    lblGroup);
        }
        return group;
    }

    static String getDefaultDateFormat() {
        return Utils.getSchedulerPrefs().get(DATE_FORMAT_KEY,
                DEFAULT_SIMPLE_DATE_FORMAT.toPattern());
    }
    
    private void setDateFormat() {
        String sdfmt = Utils.trim(schedulerModel.getDateFormat());
        if (Utils.isEmpty(sdfmt)) {
            sdfmt = getDefaultDateFormat();
        }
        int idx = dateFormatComboModel.indexOfSimpleDateFormat(sdfmt);
        if (-1 == idx) {
            dateFormatComboModel.addSimpleDateFormat(sdfmt);
            idx = dateFormatComboModel.indexOfSimpleDateFormat(sdfmt);
        }
        if (comDateFormat.getSelectedIndex() != idx) {
            comDateFormat.setSelectedIndex(idx);
        }
        startDateTimeEditor =
                syncDateTimeSpinner(startDateTimeEditor, spnStart);
        endDateTimeEditor =
                syncDateTimeSpinner(endDateTimeEditor, spnEnd);
    }
    
    private void captureDateFormat() {
        SimpleDateFormat sdf = parseDateFormat();
        Utils.getSchedulerPrefs().put(DATE_FORMAT_KEY, sdf.toPattern());
        if (!sdf.toPattern().equals(schedulerModel.getDateFormat())) {
            schedulerModel.setDateFormat(sdf.toPattern());
            startDateTimeEditor =
                    syncDateTimeSpinner(startDateTimeEditor, spnStart);
            endDateTimeEditor =
                    syncDateTimeSpinner(endDateTimeEditor, spnEnd);
        }
    }
    
    private SimpleDateFormat parseDateFormat() {
        SimpleDateFormat sdf =
                dateFormatComboModel.getSelectedSimpleDateFormat();
        if (null == sdf) {
            throw new SchedulerArgumentException(NbBundle.getMessage(
                    DefineTriggersPanel.class,
                            "ERROR_INVALID_DATEFORMAT"), lblDateFormat);
        }
        return sdf;
    }
        
    private SimpleDateFormat getSpinnerDateTimeFormat(SimpleDateFormatChooser
            chooser) {
        String pattern = chooser.getSimpleDateFormatPattern();
        if (!Utils.isEmpty(pattern)) {
            return new SimpleDateFormat(pattern);
        }
        return DEFAULT_SIMPLE_DATE_FORMAT;
    }

    private void setStart() {
        String start = Utils.trim(schedulerModel.getStart());
        Date startDate = new Date();
        boolean now = NOW_VAL.equalsIgnoreCase(start);
        if (Utils.isEmpty(start)) {
            now = Utils.getSchedulerPrefs().getBoolean(TRIGGER_NOW_KEY, true);
        } else if (!now) {
            SimpleDateFormat sdf =
                    dateFormatComboModel.getSelectedSimpleDateFormat();
            try {
                startDate = sdf.parse(start);
            } catch (ParseException ex) {
                // ignore
            }
        }
        ckbNow.setSelected(now);
        spnStart.setValue(startDate);
        spnStart.setEnabled(!now);
    }
    
    private void captureStart() {
        boolean now = ckbNow.isSelected();
        String start = parseStart();
        Utils.getSchedulerPrefs().putBoolean(TRIGGER_NOW_KEY, now);
        if (!start.equals(schedulerModel.getStart())) {
            schedulerModel.setStart(start);
        }
    }
    
    private String parseStart() {
        boolean now = ckbNow.isSelected();
        String start = (now ? NOW_VAL
                : startDateTimeEditor.getFormat().format(
                        startDateTimeEditor.getModel().getDate()));
        if (!now) {
            Date startDate = startDateTimeEditor.getModel().getDate();
            Date endDate = (ckbNever.isSelected() ? new Date(Long.MAX_VALUE)
                    : endDateTimeEditor.getModel().getDate());
            if (startDate.compareTo(endDate) > 0) {
                throw new SchedulerArgumentException(NbBundle.getMessage(
                        DefineTriggersPanel.class,
                        "ERR_START_GT_END_TIME"), lblStart);
            }
        }
        return start;
    }
    
    private void setEnd() {
        String end = Utils.trim(schedulerModel.getEnd());
        Date endDate = new Date();
        boolean never = NEVER_VAL.equalsIgnoreCase(end);
        if (Utils.isEmpty(end)) {
            never = Utils.getSchedulerPrefs()
                    .getBoolean(TRIGGER_NEVER_KEY, true);
        } else if (!never) {
            SimpleDateFormat sdf =
                    dateFormatComboModel.getSelectedSimpleDateFormat();
            try {
                endDate = sdf.parse(end);
            } catch (ParseException ex) {
                // ignore
            }
        }
        ckbNever.setSelected(never);
        spnEnd.setValue(endDate);
        spnEnd.setEnabled(!never);
    }
    
    private void captureEnd() {
        boolean never = ckbNever.isSelected();
        String end = parseEnd();
        Utils.getSchedulerPrefs().putBoolean(TRIGGER_NEVER_KEY, never);
        if (!end.equals(schedulerModel.getEnd())) {
            schedulerModel.setEnd(end);
        }
    }
    
    private String parseEnd() {
        boolean never = ckbNever.isSelected();
        String end = (never ? NEVER_VAL
                : endDateTimeEditor.getFormat().format(
                        endDateTimeEditor.getModel().getDate()));
        if (!never) {
            Date endDate = endDateTimeEditor.getModel().getDate();
            Date startDate = (ckbNow.isSelected() ? new Date()
                    : startDateTimeEditor.getModel().getDate());
            if (endDate.compareTo(startDate) < 0) {
                throw new SchedulerArgumentException(NbBundle.getMessage(
                        DefineTriggersPanel.class,
                        "ERR_END_LT_START_TIME"), lblEnd);
            }
        }
        return end;
    }
    
    private void setTimeZone() {
        String timezone = schedulerModel.getTimeZone();
        if (Utils.isEmpty(timezone)) {
            timezone = Utils.getSchedulerPrefs().get(TIMEZONE_KEY, null);
        }
        boolean defaultSel = false;
        if (Utils.isEmpty(timezone)) {
            timezone = defaultTZ;
            defaultSel = true;
        } else {
            timezone = underscore(false, timezone);
        }
        ckbDefault.setSelected(defaultSel);
        comTimeZone.setSelectedItem(timezone);
        comTimeZone.setEnabled(!defaultSel);
    }
    
    private String parseTimeZone() {
        if (ckbDefault.isSelected()) {
            return null;
        }
        String timezone = Utils.trim((String) comTimeZone.getSelectedItem());
        if (!Utils.isEmpty(timezone)) {
            return underscore(true, timezone);
        }
        throw new SchedulerArgumentException(REQUIRED_FIELD_NOT_SET,
                lblTimeZone);
    }
    
    private void captureTimeZone() {
        String timezone = parseTimeZone();
        if (Utils.isEmpty(timezone)) {
            Utils.getSchedulerPrefs().remove(TIMEZONE_KEY);
        } else {
            Utils.getSchedulerPrefs().put(TIMEZONE_KEY, timezone);
        }
        if (!Utils.equals(timezone, schedulerModel.getTimeZone())) {
            schedulerModel.setTimeZone(timezone);
        }
    }
    
    private void parseTriggers() {
        if (tblTriggers.getRowCount() == 0) {
            throw new SchedulerArgumentException(NbBundle.getMessage(
                    DefineTriggersPanel.class,
                    "ERR_NO_TRIGGERS_DEFINED"), lblTriggersSection);    //NOI18N
        }
    }
    
    private JSpinner.DateEditor syncDateTimeSpinner(JSpinner.DateEditor editor,
            final JSpinner spinner) {
        SimpleDateFormat sdf =
                dateFormatComboModel.getSelectedSimpleDateFormat();
        
        if (!sdf.equals(editor.getFormat())) {
            Date currValue = null;
            try {
                spinner.commitEdit();
                currValue = (Date) spinner.getValue();
            } catch (ParseException pe) {
                currValue = null;
            }
            if (null == currValue) {
                currValue = new Date();
            }
            
            editor = new JSpinner.DateEditor(spinner, sdf.toPattern());
            spinner.setEditor(editor);
            editor.getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent evt) {
                    String key = spinner.equals(spnStart)
                            ? "START" : "END";                          //NOI18N
                    String titleKey = "TLE_" + key;                     //NOI18N
                    String descKey = "DESC_" + key;                     //NOI18N
                    updateDescription(titleKey, descKey);
                }
            });
            
            spinner.setValue(currValue);
            leftAlign(spinner);
        }
        
        return editor;
    }
    
    private void doAddTrigger() {
        if (inInit) {
            return;
        }
        
        TriggerType type = TriggerType.toEnum(getAddTriggerType());
        if (null == type) {
            return;
        }
        EditTriggerContainer.showDialog(type, -1, schedulerModel);
    }
    
    private void doRemoveTriggers() {
        if (inInit) {
            return;
        }

        if (tblTriggers.getSelectedRowCount() > 0) {
            int[] selected = tblTriggers.getSelectedRows();
            if (selected.length > 0) {
                // Confirm deletion
                if (!dontAskRemove) {
                    String confirmRemoveMsg = (selected.length == 1)
                        ? NbBundle.getMessage(DefineTriggersPanel.class,
                            "DefineTriggersPanel.lblConfirmRemove.text",//NOI18N
                            schedulerModel.getTriggers()
                                .get(selected[0]).getName())
                        : NbBundle.getMessage(DefineTriggersPanel.class,
                            "DefineTriggersPanel.lblConfirmRemove.multiple.text",//NOI18N
                            Integer.toString(selected.length));
                    String confirmRemoveTitle = NbBundle.getMessage(
                            DefineTriggersPanel.class, (selected.length == 1
                                ? "DefineTriggersPanel.dlgConfirmRemove.text"//NOI18N
                                : "DefineTriggersPanel.dlgConfirmRemove.multiple.text"));//NOI18N
                    JLabel lblConfirmRemove = new JLabel(confirmRemoveMsg);
                    JCheckBox ckbDontAskAgain = new JCheckBox();
                    Mnemonics.setLocalizedText(ckbDontAskAgain,
                            NbBundle.getMessage(DefineTriggersPanel.class,
                            "DefineTriggersPanel.ckbDontAskAgain.text"));//NOI18N
                    ckbDontAskAgain.setToolTipText(NbBundle.getMessage(
                            DefineTriggersPanel.class,
                            "DefineTriggersPanel.ckbDontAskAgain.toolTipText"));//NOI18N
                    lblConfirmRemove.getAccessibleContext().setAccessibleName(
                            confirmRemoveMsg);
                    lblConfirmRemove.getAccessibleContext()
                            .setAccessibleDescription(confirmRemoveMsg);
                    ckbDontAskAgain.getAccessibleContext().setAccessibleName(
                            NbBundle.getMessage(DefineTriggersPanel.class,
                                "DefineTriggersPanel.ckbDontAskAgain.text"));//NOI18N
                    ckbDontAskAgain.getAccessibleContext()
                        .setAccessibleDescription(NbBundle.getMessage(
                            DefineTriggersPanel.class,
                            "DefineTriggersPanel.ckbDontAskAgain.toolTipText"));//NOI18N
                    
                    Object[] message = new Object[] {
                        lblConfirmRemove,
                        Box.createVerticalStrut(5),
                        ckbDontAskAgain,
                    };
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            message, confirmRemoveTitle,
                            NotifyDescriptor.YES_NO_OPTION);
                    Object ans = DialogDisplayer.getDefault().notify(nd);
                    if (!NotifyDescriptor.YES_OPTION.equals(ans)) {
                        return;
                    }
                    dontAskRemove = ckbDontAskAgain.isSelected();
                }
                schedulerModel.setSuppressPropertyChangeEvent(true);
                for (int i = selected.length - 1; i >= 0; i--) {
                    int tIdx = selected[i];
                    if (0 == i) {
                        schedulerModel.setSuppressPropertyChangeEvent(false);
                    }
                    schedulerModel.removeTrigger(tIdx);
                }
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

        lblControlSection = new javax.swing.JLabel();
        sepControlSection = new javax.swing.JSeparator();
        lblGroup = new javax.swing.JLabel();
        txfGroup = new javax.swing.JTextField();
        lblDateFormat = new javax.swing.JLabel();
        comDateFormat = new javax.swing.JComboBox();
        lblStart = new DynamicLabel();
        ckbNow = new javax.swing.JCheckBox();
        spnStart = new javax.swing.JSpinner();
        lblEnd = new DynamicLabel();
        ckbNever = new javax.swing.JCheckBox();
        spnEnd = new javax.swing.JSpinner();
        lblTimeZone = new DynamicLabel();
        ckbDefault = new javax.swing.JCheckBox();
        comTimeZone = new javax.swing.JComboBox();
        lblTriggersSection = new javax.swing.JLabel();
        sepTriggersSection = new javax.swing.JSeparator();
        scrTriggers = new javax.swing.JScrollPane();
        tblTriggers = new javax.swing.JTable();
        lblAddTrigger = new javax.swing.JLabel();
        comAddTrigger = new javax.swing.JComboBox();
        tbrAddTrigger = new javax.swing.JToolBar();
        btnAddTrigger = new DynamicButton();
        btnEdit = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(lblControlSection, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblControlSection.text")); // NOI18N
        lblControlSection.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblControlSection.toolTipText")); // NOI18N

        lblGroup.setLabelFor(txfGroup);
        org.openide.awt.Mnemonics.setLocalizedText(lblGroup, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblGroup.text")); // NOI18N
        lblGroup.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblGroup.toolTipText")); // NOI18N

        txfGroup.setText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.txfGroup.text")); // NOI18N
        txfGroup.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txfGroupFocusGained(evt);
            }
        });

        lblDateFormat.setLabelFor(comDateFormat);
        org.openide.awt.Mnemonics.setLocalizedText(lblDateFormat, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblDateFormat.text")); // NOI18N
        lblDateFormat.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblDateFormat.toolTipText")); // NOI18N

        comDateFormat.setModel(dateFormatComboModel);
        comDateFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comDateFormatActionPerformed(evt);
            }
        });
        comDateFormat.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comDateFormatFocusGained(evt);
            }
        });

        lblStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/service_composition_16.png"))); // NOI18N
        lblStart.setLabelFor(spnStart);
        org.openide.awt.Mnemonics.setLocalizedText(lblStart, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.text")); // NOI18N
        lblStart.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.toolTipText")); // NOI18N

        ckbNow.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ckbNow, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNow.text")); // NOI18N
        ckbNow.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNow.toolTipText")); // NOI18N
        ckbNow.setMargin(new java.awt.Insets(2, 0, 2, 2));
        ckbNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckbNowActionPerformed(evt);
            }
        });
        ckbNow.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ckbNowFocusGained(evt);
            }
        });

        spnStart.setModel(new javax.swing.SpinnerDateModel());
        spnStart.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.toolTipText")); // NOI18N
        spnStart.setEditor(startDateTimeEditor = new JSpinner.DateEditor(spnStart));
        ((JSpinner.DateEditor) spnStart.getEditor()).getTextField().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescription("TLE_START", "DESC_START");   //NOI18N
            }
        });

        spnStart.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnStartStateChanged(evt);
            }
        });

        lblEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/service_composition_16.png"))); // NOI18N
        lblEnd.setLabelFor(spnEnd);
        org.openide.awt.Mnemonics.setLocalizedText(lblEnd, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.text")); // NOI18N
        lblEnd.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.toolTipText")); // NOI18N

        ckbNever.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ckbNever, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNever.text")); // NOI18N
        ckbNever.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNever.toolTipText")); // NOI18N
        ckbNever.setMargin(new java.awt.Insets(2, 0, 2, 2));
        ckbNever.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckbNeverActionPerformed(evt);
            }
        });
        ckbNever.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ckbNeverFocusGained(evt);
            }
        });

        spnEnd.setModel(new javax.swing.SpinnerDateModel());
        spnEnd.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.toolTipText")); // NOI18N
        spnEnd.setEditor(endDateTimeEditor = new JSpinner.DateEditor(spnEnd));
        ((JSpinner.DateEditor) spnEnd.getEditor()).getTextField().addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                updateDescription("TLE_END", "DESC_END");   //NOI18N
            }
        });
        spnEnd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnEndStateChanged(evt);
            }
        });

        lblTimeZone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/service_composition_16.png"))); // NOI18N
        lblTimeZone.setLabelFor(comTimeZone);
        org.openide.awt.Mnemonics.setLocalizedText(lblTimeZone, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTimeZone.text")); // NOI18N
        lblTimeZone.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTimeZone.toolTipText")); // NOI18N

        ckbDefault.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ckbDefault, org.openide.util.NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbDefault.text")); // NOI18N
        ckbDefault.setToolTipText(org.openide.util.NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbDefault.toolTipText")); // NOI18N
        ckbDefault.setMargin(new java.awt.Insets(2, 0, 2, 2));
        ckbDefault.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ckbDefaultItemStateChanged(evt);
            }
        });
        ckbDefault.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ckbDefaultFocusGained(evt);
            }
        });

        comTimeZone.setModel(timeZoneModel);
        comTimeZone.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTimeZone.toolTipText")); // NOI18N
        comTimeZone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comTimeZoneFocusGained(evt);
            }
        });

        lblTriggersSection.setLabelFor(tblTriggers);
        org.openide.awt.Mnemonics.setLocalizedText(lblTriggersSection, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTriggersSection.text")); // NOI18N
        lblTriggersSection.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTriggersSection.toolTipText")); // NOI18N

        tblTriggers.setModel(triggerTableModel);
        tblTriggers.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblTriggers.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblTriggers.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tblTriggersFocusGained(evt);
            }
        });
        tblTriggers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tblTriggersKeyReleased(evt);
            }
        });
        tblTriggers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTriggersMouseClicked(evt);
            }
        });
        scrTriggers.setViewportView(tblTriggers);

        lblAddTrigger.setLabelFor(comAddTrigger);
        org.openide.awt.Mnemonics.setLocalizedText(lblAddTrigger, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.text")); // NOI18N
        lblAddTrigger.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.toolTipText")); // NOI18N

        comAddTrigger.setModel(addTriggerComboModel);
        comAddTrigger.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.toolTipText")); // NOI18N
        comAddTrigger.setActionCommand("ADD_TRIGGER_TYPE"); // NOI18N
        comAddTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comAddTriggerActionPerformed(evt);
            }
        });
        comAddTrigger.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comAddTriggerFocusGained(evt);
            }
        });

        tbrAddTrigger.setFloatable(false);
        tbrAddTrigger.setRollover(true);

        btnAddTrigger.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/plus16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddTrigger, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnAddTrigger.text")); // NOI18N
        btnAddTrigger.setToolTipText("TBD"); // NOI18N
        btnAddTrigger.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddTrigger.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTriggerActionPerformed(evt);
            }
        });
        tbrAddTrigger.add(btnAddTrigger);
        btnAddTrigger.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.text")); // NOI18N
        btnAddTrigger.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnAddTrigger.a11y.description")); // NOI18N

        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/pencil16x16.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnEdit, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnEdit.text")); // NOI18N
        btnEdit.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnEdit.toolTipText")); // NOI18N
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/minus16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnRemove.text")); // NOI18N
        btnRemove.setToolTipText(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnRemove.toolTipText")); // NOI18N
        btnRemove.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblControlSection)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sepControlSection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblStart)
                            .add(lblEnd)
                            .add(lblTimeZone)
                            .add(lblDateFormat)
                            .add(lblGroup))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(comDateFormat, 0, 408, Short.MAX_VALUE)
                            .add(txfGroup, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(ckbNow)
                                    .add(ckbNever)
                                    .add(ckbDefault))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, spnStart, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, spnEnd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, comTimeZone, 0, 347, Short.MAX_VALUE)))))
                    .add(layout.createSequentialGroup()
                        .add(lblTriggersSection)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sepTriggersSection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(lblAddTrigger)
                                .add(4, 4, 4)
                                .add(comAddTrigger, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(tbrAddTrigger, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(btnEdit)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnRemove))
                            .add(scrTriggers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblControlSection)
                    .add(sepControlSection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblGroup)
                    .add(txfGroup, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblDateFormat)
                    .add(comDateFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblStart)
                    .add(ckbNow)
                    .add(spnStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblEnd)
                    .add(ckbNever)
                    .add(spnEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblTimeZone)
                    .add(ckbDefault)
                    .add(comTimeZone, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblTriggersSection)
                    .add(sepTriggersSection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrTriggers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lblAddTrigger)
                        .add(comAddTrigger, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(btnEdit)
                        .add(btnRemove))
                    .add(tbrAddTrigger, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        lblControlSection.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblControlSection.text")); // NOI18N
        lblControlSection.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblControlSection.toolTipText")); // NOI18N
        lblGroup.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblGroup.text")); // NOI18N
        lblGroup.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblGroup.toolTipText")); // NOI18N
        lblDateFormat.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblDateFormat.text")); // NOI18N
        lblDateFormat.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblDateFormat.toolTipText")); // NOI18N
        lblStart.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.text")); // NOI18N
        lblStart.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.toolTipText")); // NOI18N
        ckbNow.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNow.text")); // NOI18N
        ckbNow.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNow.toolTipText")); // NOI18N
        spnStart.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.text")); // NOI18N
        spnStart.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblStart.toolTipText")); // NOI18N
        lblEnd.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.text")); // NOI18N
        lblEnd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.toolTipText")); // NOI18N
        ckbNever.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNever.text")); // NOI18N
        ckbNever.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.ckbNever.toolTipText")); // NOI18N
        spnEnd.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.text")); // NOI18N
        spnEnd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblEnd.toolTipText")); // NOI18N
        lblTriggersSection.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTriggersSection.text")); // NOI18N
        lblTriggersSection.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblTriggersSection.toolTipText")); // NOI18N
        lblAddTrigger.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.text")); // NOI18N
        lblAddTrigger.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.toolTipText")); // NOI18N
        comAddTrigger.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.toolTipText")); // NOI18N
        tbrAddTrigger.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.lblAddTrigger.text")); // NOI18N
        tbrAddTrigger.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnAddTrigger.a11y.description")); // NOI18N
        btnEdit.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnEdit.text")); // NOI18N
        btnEdit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnEdit.toolTipText")); // NOI18N
        btnRemove.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnRemove.text")); // NOI18N
        btnRemove.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefineTriggersPanel.class, "DefineTriggersPanel.btnRemove.toolTipText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void txfGroupFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txfGroupFocusGained
    updateDescription("TLE_GROUP", "DESC_GROUP");                       //NOI18N
}//GEN-LAST:event_txfGroupFocusGained

private void comDateFormatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comDateFormatFocusGained
    updateDescription("TLE_DATE_FORMAT", "DESC_DATE_FORMAT");           //NOI18N
}//GEN-LAST:event_comDateFormatFocusGained

private void ckbNowFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ckbNowFocusGained
    updateDescription("TLE_NOW", "DESC_NOW");                           //NOI18N
}//GEN-LAST:event_ckbNowFocusGained

private void ckbNeverFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ckbNeverFocusGained
    updateDescription("TLE_NEVER", "DESC_NEVER");                       //NOI18N
}//GEN-LAST:event_ckbNeverFocusGained

private void tblTriggersFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tblTriggersFocusGained
    updateDescription("TLE_TRIGGERS", "DESC_TRIGGERS");                 //NOI18N
}//GEN-LAST:event_tblTriggersFocusGained

private void btnAddTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTriggerActionPerformed
    doAddTrigger();
}//GEN-LAST:event_btnAddTriggerActionPerformed

private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
    if (inInit) {
        return;
    }
    
    if (tblTriggers.getSelectedRow() > -1) {
        EditTriggerContainer.showDialog(tblTriggers.getSelectedRow(),
                schedulerModel);
    }
}//GEN-LAST:event_btnEditActionPerformed

private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
    doRemoveTriggers();
}//GEN-LAST:event_btnRemoveActionPerformed

private void comDateFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comDateFormatActionPerformed
    if (inInit) {
        return;
    }
    
    String customI18nStr =
            NbBundle.getMessage(DefineTriggersPanel.class, LBL_CUSTOM);
    if (customI18nStr.equals(comDateFormat.getSelectedItem())) {
        String currPattern = schedulerModel.getDateFormat();
        dateFormatChooser.showDialog(currPattern, comDateFormat,
                NbBundle.getMessage(DefineTriggersPanel.class,
                        "LBL_DEFINE_CUSTOM"));                          //NOI18N
    } else {
        captureDateFormat();
    }
}//GEN-LAST:event_comDateFormatActionPerformed

private void ckbNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckbNowActionPerformed
    spnStart.setEnabled(!ckbNow.isSelected());
    handleValidation();
}//GEN-LAST:event_ckbNowActionPerformed

private void ckbNeverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckbNeverActionPerformed
    spnEnd.setEnabled(!ckbNever.isSelected());
    handleValidation();
}//GEN-LAST:event_ckbNeverActionPerformed

private void comAddTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comAddTriggerActionPerformed
    doAddTrigger();
}//GEN-LAST:event_comAddTriggerActionPerformed

private void comAddTriggerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comAddTriggerFocusGained
    updateDescription("DefineTriggersPanel.comAddTrigger.title",        //NOI18N
            "DefineTriggersPanel.lblAddTrigger.toolTipText");           //NOI18N
}//GEN-LAST:event_comAddTriggerFocusGained

private void spnStartStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnStartStateChanged
    handleValidation();
}//GEN-LAST:event_spnStartStateChanged

private void spnEndStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnEndStateChanged
    handleValidation();
}//GEN-LAST:event_spnEndStateChanged

private void tblTriggersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTriggersMouseClicked
    if ((evt.getClickCount() == 2)
            && (tblTriggers.getSelectedRowCount() == 1)) {
        EditTriggerContainer.showDialog(tblTriggers.getSelectedRow(),
                schedulerModel);
    }
}//GEN-LAST:event_tblTriggersMouseClicked

private void comTimeZoneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comTimeZoneFocusGained
    updateDescription("DefineTriggersPanel.lblTimeZone.text",           //NOI18N
            "DefineTriggersPanel.lblTimeZone.toolTipText");             //NOI18N
}//GEN-LAST:event_comTimeZoneFocusGained

private void tblTriggersKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tblTriggersKeyReleased
    if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
        doRemoveTriggers();
    }
}//GEN-LAST:event_tblTriggersKeyReleased

private void ckbDefaultFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ckbDefaultFocusGained
    updateDescription("TLE_DEFAULT_TIMEZONE",                           //NOI18N
            "DefineTriggersPanel.ckbDefault.toolTipText");              //NOI18N
}//GEN-LAST:event_ckbDefaultFocusGained

private void ckbDefaultItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ckbDefaultItemStateChanged
    comTimeZone.setEnabled(!ckbDefault.isSelected());
    if (ckbDefault.isSelected()) {
        comTimeZone.setSelectedItem(defaultTZ);
    }
}//GEN-LAST:event_ckbDefaultItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddTrigger;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox ckbDefault;
    private javax.swing.JCheckBox ckbNever;
    private javax.swing.JCheckBox ckbNow;
    private javax.swing.JComboBox comAddTrigger;
    private javax.swing.JComboBox comDateFormat;
    private javax.swing.JComboBox comTimeZone;
    private javax.swing.JLabel lblAddTrigger;
    private javax.swing.JLabel lblControlSection;
    private javax.swing.JLabel lblDateFormat;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblGroup;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblTimeZone;
    private javax.swing.JLabel lblTriggersSection;
    private javax.swing.JScrollPane scrTriggers;
    private javax.swing.JSeparator sepControlSection;
    private javax.swing.JSeparator sepTriggersSection;
    private javax.swing.JSpinner spnEnd;
    private javax.swing.JSpinner spnStart;
    private javax.swing.JTable tblTriggers;
    private javax.swing.JToolBar tbrAddTrigger;
    private javax.swing.JTextField txfGroup;
    // End of variables declaration//GEN-END:variables

}
