/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.soa.validation.util.Duration;
import org.netbeans.modules.soa.validation.util.DurationUtil;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.ValidablePropertyCustomizer;
import org.netbeans.modules.soa.ui.form.RangeDoubleDocument;
import org.netbeans.modules.soa.ui.form.RangeIntegerDocument;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.bpel.properties.props.PropertyVetoError;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;

/**
 * @author nk160297
 */
public class DurationPropertyCustomizer extends ValidablePropertyCustomizer
        implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    
    private Timer inputDelayTimer;
    
    private PropertyEditor myPropertyEditor;
    
    /** Creates new form StringPropertyCustomizer */
    public DurationPropertyCustomizer() {
        super();
        //
        initComponents();
        //
        getValidStateManager(true).addValidStateListener(new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
        //
        fldYear.setDocument(new RangeIntegerDocument(0, Integer.MAX_VALUE));
        fldMonth.setDocument(new RangeIntegerDocument(0, Integer.MAX_VALUE));
        fldDay.setDocument(new RangeIntegerDocument(0, Integer.MAX_VALUE));
        fldHour.setDocument(new RangeIntegerDocument(0, Integer.MAX_VALUE));
        fldMinute.setDocument(new RangeIntegerDocument(0, Integer.MAX_VALUE));
        fldSecond.setDocument(new RangeDoubleDocument(0.0, Double.MAX_VALUE));
        //
        ActionListener timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                revalidate(true);
            }
        };
        inputDelayTimer = new Timer(Constants.INPUT_VALIDATION_DELAY, timerListener);
        inputDelayTimer.setCoalesce(true);
        inputDelayTimer.setRepeats(false);
        //
        DocumentListener docListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void insertUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
            public void removeUpdate(DocumentEvent e) {
                inputDelayTimer.restart();
            }
        };
        //
        fldYear.getDocument().addDocumentListener(docListener);
        fldMonth.getDocument().addDocumentListener(docListener);
        fldDay.getDocument().addDocumentListener(docListener);
        fldHour.getDocument().addDocumentListener(docListener);
        fldMinute.getDocument().addDocumentListener(docListener);
        fldSecond.getDocument().addDocumentListener(docListener);
        //
        FocusListener fl = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                inputDelayTimer.stop();
                revalidate(true);
            }
        };
        //
        fldYear.addFocusListener(fl);
        fldMonth.addFocusListener(fl);
        fldDay.addFocusListener(fl);
        fldHour.addFocusListener(fl);
        fldMinute.addFocusListener(fl);
        fldSecond.addFocusListener(fl);
        //
        HelpCtx.setHelpIDString(this, this.getClass().getName());
        //
        SoaUtil.activateInlineMnemonics(this);
    }
    
    @Override
    public synchronized void init(PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        assert propertyEnv != null && propertyEditor != null : "Wrong params"; // NOI18N
        //
        if (myPropertyEnv == propertyEnv) {
            return; // Prevent repeated initialization
        }
        //
        if (myPropertyEnv != null) {
            myPropertyEnv.removePropertyChangeListener(this);
        }
        //
        myPropertyEditor = propertyEditor;
        //
        super.init(propertyEnv, propertyEditor);
        //
        myPropertyEnv.addPropertyChangeListener(this);
        //
        // The Ok button will not work without the following line!!!
        myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        //
        String value = propertyEditor.getAsText();
        //
        parseFor(value);
        revalidate(true);
        //
        String newHelpCtxID = null;
        FeatureDescriptor fd = myPropertyEnv.getFeatureDescriptor();
        if (PropertyType.FOR_EXPRESSION.toString().equals(fd.getName())) {
            newHelpCtxID = this.getClass().getName() + ".For"; // NOI18N
        } else if (PropertyType.REPEAT_EVERY_EXPRESSION.toString().equals(fd.getName())) {
            newHelpCtxID = this.getClass().getName() + ".RepeatEvery"; // NOI18N
        }
        if (newHelpCtxID != null && newHelpCtxID.length() != 0) {
            HelpCtx.setHelpIDString(this, newHelpCtxID); // NOI18N
            SoaUtil.fireHelpContextChange(this, new HelpCtx(newHelpCtxID));
        }
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (PropertyEnv.PROP_STATE.equals(event.getPropertyName()) &&
                event.getNewValue() == PropertyEnv.STATE_VALID) {
            String currText = DurationUtil.addQuotes(getContent());
            try {
                myPropertyEditor.setAsText(currText);
            } catch (PropertyVetoError ex) {
                myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                PropertyVetoError.defaultProcessing(ex);
            }
        }
    }
    
    private void parseFor(String text) {
      Duration duration = DurationUtil.parseDuration(text, false);
//System.out.println("set duration: " + duration);
      fldYear.setText("" + duration.getYears());
      fldMonth.setText("" + duration.getMonths());
      fldDay.setText("" + duration.getDays());
      fldHour.setText("" + duration.getHours());
      fldMinute.setText("" + duration.getMinutes());
      fldSecond.setText("" + duration.getSeconds());
    }
    
    private String getContent() {
        return DurationUtil.getContent(true,
               DurationUtil.parseInt(fldYear.getText()),
               DurationUtil.parseInt(fldMonth.getText()),
               DurationUtil.parseInt(fldDay.getText()),
               DurationUtil.parseInt(fldHour.getText()),
               DurationUtil.parseInt(fldMinute.getText()),
               DurationUtil.parseDouble(fldSecond.getText()));
    }
    
    public Validator createValidator() {
        return new MyValidator(this);
    }
    
    private class MyValidator extends DefaultValidator {
        
        public MyValidator(ValidStateManager.Provider vsmProvider) {
            super(vsmProvider, ErrorMessagesBundle.class);
        }
        
        public void doFastValidation() {
            if ( !check(fldYear)) {
                addReasonKey(Severity.ERROR, "ERR_INVALID_YEARS"); // NOI18N
            }
            if ( !check(fldMonth)) {
                addReasonKey(Severity.ERROR, "ERR_INVALID_MONTHS"); // NOI18N
            }
            if ( !check(fldDay)) {
                addReasonKey(Severity.ERROR, "ERR_INVALID_DAYS"); // NOI18N
            }
            if ( !check(fldHour)) {
                addReasonKey(Severity.ERROR, "ERR_INVALID_HOURS"); // NOI18N
            }
            if ( !check(fldMinute)) {
                addReasonKey(Severity.ERROR, "ERR_INVALID_MINUTES"); // NOI18N
            }
            if (DurationUtil.parseDouble(fldSecond.getText()) < 0) {
                addReasonKey(Severity.ERROR, "ERR_INVALID_SECONDS"); // NOI18N
            }
        }
        
        private boolean check(JTextField field) {
            return DurationUtil.parseInt(field.getText()) >= 0;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblYear = new javax.swing.JLabel();
        fldYear = new javax.swing.JTextField();
        lblMonth = new javax.swing.JLabel();
        fldMonth = new javax.swing.JTextField();
        lblDay = new javax.swing.JLabel();
        fldDay = new javax.swing.JTextField();
        lblHour = new javax.swing.JLabel();
        fldHour = new javax.swing.JTextField();
        lblMinute = new javax.swing.JLabel();
        fldMinute = new javax.swing.JTextField();
        lblSecond = new javax.swing.JLabel();
        fldSecond = new javax.swing.JTextField();
        lblErrorMessage = new javax.swing.JLabel();

        lblYear.setLabelFor(fldYear);
        lblYear.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Years"));
        lblYear.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Years"));
        lblYear.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Years"));

        fldYear.setColumns(4);

        lblMonth.setLabelFor(fldMonth);
        lblMonth.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Months"));
        lblMonth.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Months"));
        lblMonth.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Months"));

        fldMonth.setColumns(4);

        lblDay.setLabelFor(fldDay);
        lblDay.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Days"));
        lblDay.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Days"));
        lblDay.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Days"));

        fldDay.setColumns(4);

        lblHour.setLabelFor(fldHour);
        lblHour.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Hours"));
        lblHour.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Hours"));
        lblHour.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Hours"));

        fldHour.setColumns(4);

        lblMinute.setLabelFor(fldMinute);
        lblMinute.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Minutes"));
        lblMinute.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Minutes"));
        lblMinute.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Minutes"));

        fldMinute.setColumns(4);

        lblSecond.setLabelFor(fldSecond);
        lblSecond.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Seconds"));
        lblSecond.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Seconds"));
        lblSecond.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Seconds"));

        fldSecond.setColumns(4);

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel"));
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblYear)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblHour))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldYear, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                            .add(fldHour, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblMonth)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblMinute))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldMonth, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .add(fldMinute, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblDay)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lblSecond))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fldDay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .add(fldSecond, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblYear)
                    .add(fldYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblMonth)
                    .add(lblDay)
                    .add(fldDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fldMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblHour)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lblMinute)
                        .add(lblSecond)
                        .add(fldHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(fldSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(fldMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField fldDay;
    private javax.swing.JTextField fldHour;
    private javax.swing.JTextField fldMinute;
    private javax.swing.JTextField fldMonth;
    private javax.swing.JTextField fldSecond;
    private javax.swing.JTextField fldYear;
    private javax.swing.JLabel lblDay;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblHour;
    private javax.swing.JLabel lblMinute;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblSecond;
    private javax.swing.JLabel lblYear;
    // End of variables declaration//GEN-END:variables
}
