/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.edm.editor.graph;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Types;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.edm.editor.utils.SQLUtils;

/**
 * Configures type, precision and scale (as appropriate) of a cast as operator.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class CastAsDialog extends JDialog implements ActionListener {

    /* Array of Strings representing available SQL datatypes */
    public static final String[] DISPLAY_NAMES;

    /* Action command string representing OK user option */
    private static final String CMD_OK = "ok"; //NOI18N

    /* Action command string representing Cancel user option */
    private static final String CMD_CANCEL = "cancel"; //NOI18N
    private static final SpinnerNumberModel PRECISION_MODEL = new SpinnerNumberModel(1, 1, 38, 1);
    private static final SpinnerNumberModel SCALE_MODEL = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
    private static final Integer MAX_NUMBER_PRECISION = new Integer(100);
    private static final Integer MAX_CHAR_PRECISION = new Integer(Integer.MAX_VALUE);
    private static final Integer ZERO = new Integer(0);
    private static final Integer ONE = new Integer(1);
    private static transient final Logger mLogger = Logger.getLogger(CastAsDialog.class.getName());
    

    static {
        List types = SQLUtils.getSupportedCastTypes();

        // Now populated DISPLAY_NAMES with contents of the restricted list.
        DISPLAY_NAMES = (String[]) types.toArray(new String[types.size()]);
    }

    /* Holds available SQL types */
    private JComboBox mTypesBox = new JComboBox(DISPLAY_NAMES);

    /* Holds precision/length value */
    private JSpinner mPrecLength = new JSpinner();
    /* Holds scale value */
    private JSpinner mScale = new JSpinner();
    /* OK dialog button */
    private JButton mOkButton;

    /* Cancel dialog button */
    private JButton mCancelButton;

    /* Indicates whether user canceled dialog box */
    private boolean mIsCanceled = true;

    /**
     * Creates a new LiteralDialog object.
     * 
     * @param title
     * @param modal
     */
    public CastAsDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);

        try {
            mOkButton = new JButton("Ok"); //NOI18N
            mOkButton.getAccessibleContext().setAccessibleName("Ok");
            mCancelButton = new JButton("Cancel"); //NOI18N
            mCancelButton.getAccessibleContext().setAccessibleName("Cancel");

            initComponents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void show() {
        pack();
        // this.setResizable(false);
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        mOkButton.requestFocus();
        super.show();
    }

    /**
     * Exposes standalone test entry-point.
     * 
     * @param args command-line arguments
     */
    public static void main(String args[]) {
        JFrame win = new JFrame("main"); //NOI18N
        win.setSize(100, 100);
        win.setVisible(true);
        win.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        CastAsDialog dia = new CastAsDialog(win, "Testing", true); //NOI18N
        dia.setVisible(true);
        dia.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void setJdbcTypeEditable(boolean editable) {
        mTypesBox.setEnabled(editable);
    }

    /**
     * Gets user-selected JDBC datatype for this cast-as operator.
     * 
     * @return JDBC datatype, as enumerated in java.sql.Types
     */
    public int getJdbcType() {
        return SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem());
    }

    /**
     * Sets user-selected JDBC datatype for this cast-as operator.
     * 
     * @param newType new JDBC datatype, as enumerated in java.sql.Types
     */
    public void setJdbcType(int newType) {
        mTypesBox.setSelectedItem(SQLUtils.getStdSqlType(newType));
        updatePrecisionModel();
        updateScaleModel();
    }

    /**
     * Gets current precision value in dialog.
     * 
     * @return current precision - possibly 0 if selected datatype does not allow for a 
     * precision to be specified
     */
    public int getPrecision() {
        Integer value = (Integer) PRECISION_MODEL.getValue();
        return (mPrecLength.isEnabled() && value != null) ? value.intValue() : 0;
    }

    /**
     * Sets current precision value in dialog, subject to model limits.
     * 
     * @param newValue new value for precision
     */
    public void setPrecision(int newValue) {
        Integer newValueObj = new Integer(newValue);
        Integer maxValueObj = (Integer) PRECISION_MODEL.getMaximum();

        newValueObj = setBoundedValue(newValueObj, ONE, maxValueObj);
        PRECISION_MODEL.setValue(newValueObj);
    }

    /**
     * Gets current scale value in dialog.
     * 
     * @return current scale - possibly 0 if selected datatype does not allow for a 
     * precision to be specified
     */
    public int getScale() {
        Integer value = (Integer) SCALE_MODEL.getValue();
        return (mScale.isEnabled() && value != null) ? value.intValue() : 0;
    }

    /**
     * Sets current scale value in dialog, subject to model limits.
     * 
     * @param newValue new value for scale
     */
    public void setScale(int newValue) {
        Integer newValueObj = new Integer(newValue);
        Integer maxValueObj = (Integer) SCALE_MODEL.getMaximum();

        newValueObj = setBoundedValue(newValueObj, ZERO, maxValueObj);
        SCALE_MODEL.setValue(newValueObj);
    }

    private Integer setBoundedValue(Integer value, Integer min, Integer max) {
        if (value.compareTo(max) > 0) {
            value = max;
        } else if (value.compareTo(min) < 0) {
            value = min;
        }

        return value;
    }

    /**
     * Indicates whether user canceled this dialog box.
     * 
     * @return true if user canceled dialog box, false otherwise.
     */
    public boolean isCanceled() {
        return mIsCanceled;
    }

    /**
     * @param e ActionEvent to handle.
     */
    public void actionPerformed(ActionEvent e) {
        if (CMD_CANCEL.equals(e.getActionCommand())) { //NOI18N
            mIsCanceled = true;
            this.setVisible(false);
        } else if (CMD_OK.equals(e.getActionCommand())) { //NOI18N
            if (checkForm()) {
                mIsCanceled = false;
                this.setVisible(false);
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        mTypesBox.requestFocusInWindow();
                    }
                });
            }
        }
    }

    private void buttonActionPerformed(Object source) {
        if (source.equals(mCancelButton)) {
            mIsCanceled = true;
            this.setVisible(false);
        } else if (source.equals(mOkButton)) {
            if (checkForm()) {
                mIsCanceled = false;
                this.setVisible(false);
            }
        }
    }

    /*
     * Creates button pane for this dialog. @return Container containing control buttons.
     */
    private Container getButtonPane() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 10, 10));
        mOkButton.requestFocus();
        buttonPanel.add(mOkButton);
        buttonPanel.add(mCancelButton);

        return buttonPanel;
    }

    private boolean checkForm() {
        // return CastAsDialog.isValidLiteral(mInput.getText(), SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem()));
        return true;
    }

    private void initEventHandle() {
        mOkButton.setActionCommand(CMD_OK); // NOI18N
        mCancelButton.setActionCommand(CMD_CANCEL); // NOI18N

        mOkButton.addActionListener(this);
        mCancelButton.addActionListener(this);

        ButtonKeyAdapter bKeyAdapter = new ButtonKeyAdapter();
        mOkButton.addKeyListener(bKeyAdapter);
        mCancelButton.addKeyListener(bKeyAdapter);

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                mIsCanceled = true;
            }
        });
    }

    private void initComponents() throws Exception {
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel formPanel = new JPanel();
        formPanel.setBorder(new EmptyBorder(25, 25, 0, 25));

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        formPanel.setLayout(gridBag);

        Insets leftInsets = new Insets(0, 0, 5, 5);
        Insets rightInsets = new Insets(0, 5, 5, 0);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.insets = leftInsets;

        JLabel typeLabel = new JLabel("Type:"); //NOI18N
        typeLabel.getAccessibleContext().setAccessibleName("Type:");
        gridBag.setConstraints(typeLabel, constraints);
        formPanel.add(typeLabel);

        constraints.gridx = 1;
        constraints.insets = rightInsets;
        constraints.weightx = 0.0;
        gridBag.setConstraints(mTypesBox, constraints);
        formPanel.add(mTypesBox);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets = leftInsets;
        constraints.weightx = 1.0;
        JLabel precLengthLabel = new JLabel("Precision/length:"); //NOI18N
        precLengthLabel.getAccessibleContext().setAccessibleName("Precision/length:");
        gridBag.setConstraints(precLengthLabel, constraints);
        formPanel.add(precLengthLabel);

        mPrecLength.setModel(PRECISION_MODEL);
        mPrecLength.setEnabled(true);
        mPrecLength.addChangeListener(new PrecisionChangeListener());
        constraints.gridx = 1;
        constraints.insets = rightInsets;
        constraints.weightx = 0.0;
        gridBag.setConstraints(mPrecLength, constraints);
        formPanel.add(mPrecLength);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets = leftInsets;
        constraints.weightx = 1.0;
        JLabel scaleLabel = new JLabel("Scale:"); //NOI18N
        scaleLabel.getAccessibleContext().setAccessibleName("Scale:");
        gridBag.setConstraints(scaleLabel, constraints);
        formPanel.add(scaleLabel);

        mScale.setModel(SCALE_MODEL);
        mScale.setEnabled(false);
        constraints.gridx = 1;
        constraints.insets = rightInsets;
        constraints.weightx = 0.0;
        gridBag.setConstraints(mScale, constraints);
        formPanel.add(mScale);

        initEventHandle();
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(getButtonPane(), BorderLayout.SOUTH);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        mTypesBox.addItemListener(new TypeChangeItemListener());
    }

    private void updatePrecisionModel() {
        int type = SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem());
        if (SQLUtils.isPrecisionRequired(type)) {
            mPrecLength.setEnabled(true);
            switch (type) {
                case Types.NUMERIC:
                case Types.DECIMAL:
                    Integer currentVal = (Integer) PRECISION_MODEL.getValue();
                    if (currentVal.compareTo(MAX_NUMBER_PRECISION) > 0) {
                        PRECISION_MODEL.setValue(MAX_NUMBER_PRECISION);
                    }
                    PRECISION_MODEL.setMaximum(MAX_NUMBER_PRECISION);
                    break;

                default:
                    mPrecLength.setEnabled(true);
                    PRECISION_MODEL.setMaximum(MAX_CHAR_PRECISION);
                    break;
            }
        } else {
            mPrecLength.setEnabled(false);
        }
    }

    private void updateScaleModel() {
        int type = SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem());
        if (SQLUtils.isScaleRequired(type)) {
            mScale.setEnabled(true);
            Integer currentVal = (Integer) SCALE_MODEL.getValue();
            Integer currentPrecision = (Integer) PRECISION_MODEL.getValue();
            if (currentVal.compareTo(currentPrecision) > 0) {
                SCALE_MODEL.setValue(currentPrecision);
            }
            SCALE_MODEL.setMaximum(currentPrecision);
        } else {
            mScale.setEnabled(false);
        }
    }

    class TypeChangeItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            CastAsDialog.this.updatePrecisionModel();
            CastAsDialog.this.updateScaleModel();
        }
    }

    class PrecisionChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            CastAsDialog.this.updateScaleModel();
        }
    }

    class ButtonKeyAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                buttonActionPerformed(evt.getSource());
            }
        }
    }
}

