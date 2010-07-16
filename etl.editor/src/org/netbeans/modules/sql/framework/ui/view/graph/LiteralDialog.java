/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.sun.etl.utils.StringUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Configures type and value of an SQLBuilder literal element.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class LiteralDialog extends JDialog implements ActionListener {

    /* Array of Strings representing available SQL datatypes */
    public static final String[] DISPLAY_NAMES;

    /* Action command string representing OK user option */
    private static final String CMD_OK = "ok"; //NOI18N

    /* Action command string representing Cancel user option */
    private static final String CMD_CANCEL = "cancel"; //NOI18N
    

    static {
        List<String> types = SQLLiteral.VALID_TYPE_NAMES;

        // Now populated DISPLAY_NAMES with contents of the restricted list.
        DISPLAY_NAMES = types.toArray(new String[types.size()]);
    }

    /* Holds available SQL types */
    private JComboBox mTypesBox = new JComboBox(DISPLAY_NAMES);

    /* Holds value of literal */
    private JTextField mInput = new JTextField();

    /*
     * If date literals are supported, delete the JTextField declaration and uncomment
     * this field. Also uncomment the inner class FormatTypesChangeListener to
     * enable/disable the secondary dialog button.
     */
    //  private CalendarComboBox mInput = new CalendarComboBox(false);
    /* OK dialog button */
    private JButton mOkButton;

    /* Cancel dialog button */
    private JButton mCancelButton;

    /* Indicates whether user cancelled dialog box */
    private boolean mIsCanceled = true;
    private static transient final Logger mLogger = Logger.getLogger(LiteralDialog.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Creates a new LiteralDialog object.
     * 
     * @param title
     * @param modal
     */
    public LiteralDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);

        try {
            String nbBundle1 = mLoc.t("BUND408: Ok");
            mOkButton = new JButton(nbBundle1.substring(15)); //NOI18N
            mOkButton.getAccessibleContext().setAccessibleName(nbBundle1.substring(15));
            String nbBundle2 = mLoc.t("BUND048: Cancel");
            mCancelButton = new JButton(nbBundle2.substring(15)); //NOI18N
            mCancelButton.getAccessibleContext().setAccessibleName(nbBundle2.substring(15));
            initComponents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Indicates whether the given String value is a valid representation of a literal of
     * the given type, displaying a GUI error message dialog if it is not.
     * 
     * @param literalVal value to test
     * @param type asserted JDBC type of literal
     * @return true if <code>literalVal</code> is valid; false otherwise
     */
    public static final boolean isValidLiteral(String literalVal, int type) {
        boolean returnVal = true;

        String errorMessage = evaluateIfLiteralValid(literalVal, type);
        if (errorMessage != null) {
            returnVal = false;
            showMessage(errorMessage);
        }

        return returnVal;
    }

    public static final String evaluateIfLiteralValid(String literalVal, int type) {
        String errorMsg = null;

        switch (type) {
            // We'll accept blank text for char-like literals, so treat
            // them differently from other types.
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                break;

            case Types.BOOLEAN:
                if (!(Boolean.TRUE.toString().equals(literalVal) || Boolean.FALSE.toString().equalsIgnoreCase(literalVal))) {
                    String nbBundle3 = mLoc.t("BUND415: Please enter either true or false.");
                    errorMsg = nbBundle3.substring(15);//NOI18N
                }
                break;

            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
                try {
                    Double.valueOf(literalVal);
                } catch (NumberFormatException e) {
                    String nbBundle4 = mLoc.t("BUND505: Please enter a valid number value.");
                    errorMsg = nbBundle4.substring(15);//NOI18N
                }
                break;

            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                if (!StringUtil.isValid(literalVal, "[0-9]+")) {
                    String nbBundle5 = mLoc.t("BUND418: Please enter a valid whole number value.");
                    errorMsg = nbBundle5.substring(15);//NOI18N
                }
                break;

            case Types.DATE:
                try {
                    DateFormat.getDateInstance().parse(literalVal);
                } catch (ParseException e) {
                    String nbBundle6 = mLoc.t("BUND419: Please enter a valid date string.");
                    errorMsg = nbBundle6.substring(15);//NOI18N
                }
                break;

            case Types.TIME:
                if (!StringUtil.isValid(literalVal, "[0-9]?[0-9]:[0-9][0-9].?[AP]?[M]?")) { //NOI18N
                    String nbBundle7 = mLoc.t("BUND420: Please enter a valid time string.");
                    errorMsg = nbBundle7.substring(15); //NOI18N
                }
                break;

            case Types.TIMESTAMP:
                try {
                    java.sql.Timestamp.valueOf(literalVal);
                } catch (IllegalArgumentException e) {
                    String nbBundle8 = mLoc.t("BUND421: Please enter a valid timestamp string, in the form yyyy-mm-dd hh:mm:ss.fffffffff (fractional seconds optional).");
                    errorMsg = nbBundle8.substring(15);
                }
                break;
            default:
                if (StringUtil.isNullString(literalVal)) {
                    String nbBundle9 = mLoc.t("BUND422: Please enter a value.");
                    errorMsg = nbBundle9.substring(15);
                }
        }

        return errorMsg;
    }

    public static void showMessage(String msg) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void show() {
        pack();
        this.setResizable(false);
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

        LiteralDialog dia = new LiteralDialog(win, "Testing", true); //NOI18N
        dia.setVisible(true);
        dia.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Gets user-defined literal value.
     * 
     * @return String representing literal value
     */
    public String getLiteral() {
        switch (getType()) {
            // We'll accept blank text for char-like literals, so treat
            // them differently from other types.
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
                return mInput.getText();

            default:
                return mInput.getText().trim();
        }
    }

    /**
     * Gets user-selected JDBC datatype of this literal
     * 
     * @return JDBC datatype, as enumerated in java.sql.Types
     */
    public int getType() {
        return SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem());
    }

    /**
     * Indicates whether user cancelled this dialog box.
     * 
     * @return true if user cancelled dialog box, false otherwise.
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
                        String text = mInput.getText();
                        int len = (text == null) ? -1 : text.length();
                        if (len != -1) {
                            mInput.setCaretPosition(len);
                        }

                        mInput.requestFocusInWindow();
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
        return LiteralDialog.isValidLiteral(mInput.getText(), SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem()));
    }

    private void initEventHandle() {
        mOkButton.setActionCommand(CMD_OK); // NOI18N
        mCancelButton.setActionCommand(CMD_CANCEL); // NOI18N

        mOkButton.addActionListener(this);
        mCancelButton.addActionListener(this);

        ButtonKeyAdapter bKeyAdapter = new ButtonKeyAdapter();
        mOkButton.addKeyListener(bKeyAdapter);
        mCancelButton.addKeyListener(bKeyAdapter);

        mInput.setActionCommand(CMD_OK); // NOI18N
        mInput.addActionListener(this);

        //FIXME: Uncomment the following line if date literals are supported.
        // mTypesBox.addItemListener(new FormatTypesChangeListener());
        this.addWindowListener(new WindowAdapter() {

            @Override
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
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.insets = leftInsets;

        String nbBundle10 = mLoc.t("BUND410: Type:");
        JLabel typeLabel = new JLabel(nbBundle10.substring(15)); //NOI18N
        typeLabel.getAccessibleContext().setAccessibleName(nbBundle10.substring(15));
        gridBag.setConstraints(typeLabel, constraints);
        formPanel.add(typeLabel);

        constraints.gridx = 1;
        constraints.insets = rightInsets;
        constraints.weightx = 1.0;
        gridBag.setConstraints(mTypesBox, constraints);
        formPanel.add(mTypesBox);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets = leftInsets;
        constraints.weightx = 0.0;
        constraints.gridwidth = 2;

        String nbBundle11 = mLoc.t("BUND423: Value:");
        JLabel valueLabel = new JLabel(nbBundle11.substring(15)); //NOI18N
        valueLabel.getAccessibleContext().setAccessibleName(nbBundle11.substring(15));
        gridBag.setConstraints(valueLabel, constraints);
        formPanel.add(valueLabel);

        mInput.setColumns(20);
        constraints.gridx = 1;
        constraints.insets = rightInsets;
        constraints.weightx = 1.0;
        gridBag.setConstraints(mInput, constraints);
        formPanel.add(mInput);

        initEventHandle();
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(getButtonPane(), BorderLayout.SOUTH);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    /*
     * Uncomment this inner class if and when literal dates are again supported. This
     * would imply that mInput is an instance of CalendarComboBox instead of JTextInput.
     */
    //    class FormatTypesChangeListener implements ItemListener {
    //        /**
    //         * Invoked when an item has been selected or deselected by the user. The code
    //         * written for this method performs the operations that need to occur when an
    //         * item is selected (or deselected).
    //         */
    //        public void itemStateChanged(ItemEvent e) {
    //            int type = SQLUtils.getStdJdbcType((String) mTypesBox.getSelectedItem());
    //            if (type == Types.DATE) {
    //                mInput.showCalendarPopUp();
    //            } else {
    //                mInput.hideCalendarPopUp();
    //            }
    //        }
    //    }
    class ButtonKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                buttonActionPerformed(evt.getSource());
            }
        }
    }
}

