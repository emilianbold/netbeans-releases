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
package org.netbeans.modules.xml.time;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.util.Utilities;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.27
 */
abstract class AbstractDialog extends JDialog {

    protected abstract Time parseValue(String value);

    protected abstract String createValue(Object year, Object month, Object day, Object hour, Object minute, Object second);

    protected abstract void createComponent(JPanel panel, GridBagConstraints c);

    protected abstract void setTime(Time time);

    protected abstract boolean checkValue();

    protected AbstractDialog(String value, String title, String accessibility) {
        super((Frame) null, title, true);
        a11y(this, accessibility);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        add(getResizableX(createPanel()), c);

        c.gridy++;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(MEDIUM_SIZE, 0, 0, 0);
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.fill = GridBagConstraints.NONE;
        add(createButtonPanel(), c);

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, CANCEL);

        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        };
        getRootPane().getActionMap().put(CANCEL, action);
        pack();

        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * FACTOR_9) / FACTOR_10;
        int maxH = (r.height * FACTOR_9) / FACTOR_10;
        Dimension d = getPreferredSize();
        d.width = Math.min(d.width, maxW);
        d.height = Math.min(d.height, maxH);
        setBounds(Utilities.findCenterBounds(d));

        if (value == null) {
            return;
        }
//out();
//out("VALUE: " + value);
        value = TimeUtil.addQuotes(value);
//out("     : " + value);
        Time time = parseValue(value);
//out(" time: " + time);

        if (time == null) {
            return;
        }
        myDay.setText(time.getDays().toString());
        myHour.setText(time.getHours().toString());
        myMinute.setText(time.getMinutes().toString());
        myMonth.setText(time.getMonths().toString());
        mySecond.setText(time.getSeconds().toString());
        myYear.setText(time.getYears().toString());
        setTime(time);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = createButton("LBL_OK", "ACS_OK"); // NOI18N
        getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (check()) {
                    dispose();
                }
            }
        });
        panel.add(okButton);

        JButton cancelButton = createButton("LBL_Cancel", "ACS_Cancel"); // NOI18N
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        panel.add(cancelButton);

        org.netbeans.modules.xml.util.UI.setSize(okButton, cancelButton.getPreferredSize());

        return panel;
    }

    private JButton createButton(String key, String a11y) {
        JButton button = new JButton(i18n(AbstractDialog.class, key));
        a11y(button, i18n(AbstractDialog.class, a11y));
        return button;
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridy = 0;
        myYear = createField("LBL_Year", "ACS_Year", panel, c); // NOI18N
        myMonth = createField("LBL_Month", "ACS_Month", panel, c); // NOI18N
        myDay = createField("LBL_Day", "ACS_Day", panel, c); // NOI18N

        c.gridy++;
        myHour = createField("LBL_Hour", "ACS_Hour", panel, c); // NOI18N
        myMinute = createField("LBL_Minute", "ACS_Minute", panel, c); // NOI18N
        mySecond = createField("LBL_Second", "ACS_Second", panel, c); // NOI18N

        c.gridy++;
        createComponent(panel, c);

        return panel;
    }

    protected final JTextField createField(String key, String a11y, JPanel panel, GridBagConstraints c) {
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(MEDIUM_SIZE, MEDIUM_SIZE, MEDIUM_SIZE, MEDIUM_SIZE);
        c.anchor = GridBagConstraints.EAST;
        JLabel label = createLabel(i18n(AbstractDialog.class, key));
        panel.add(label, c);

        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(MEDIUM_SIZE, MEDIUM_SIZE, MEDIUM_SIZE, MEDIUM_SIZE);
        c.anchor = GridBagConstraints.WEST;
        JTextField field = new JTextField(DEFAULT_VALUE);
        a11y(field, i18n(AbstractDialog.class, a11y));
        setWidth(field, TEXT_WIDTH);
        panel.add(field, c);
        label.setLabelFor(field);

        return field;
    }

    public String getValue() {
        return myValue;
    }

    private boolean check() {
        Object year = TimeUtil.parseInt(myYear.getText());
        Object month = TimeUtil.parseInt(myMonth.getText());
        Object day = TimeUtil.parseInt(myDay.getText());
        Object hour = TimeUtil.parseInt(myHour.getText());
        Object minute = TimeUtil.parseInt(myMinute.getText());
        Object second = TimeUtil.parseDouble(mySecond.getText());
//out();
//out("check: " + year);

        if (
            checkInt(year, "ERR_invalid_year", myYear) && // NOI18N
            checkInt(month, "ERR_invalid_month", myMonth) && // NOI18N
            checkInt(day, "ERR_invalid_day", myDay) && // NOI18N
            checkInt(hour, "ERR_invalid_hour", myHour) && // NOI18N
            checkInt(minute, "ERR_invalid_minute", myMinute) && // NOI18N
            checkDbl(second, "ERR_invalid_second", mySecond) && // NOI18N
            checkValue()
        ) {
            myValue = createValue(year, month, day, hour, minute, second);
            return true;
        }
        else {
            myValue = null;
            return false;
        }
    }

    private boolean checkInt(Object value, String key, JTextField field) {
//out("ch int: " + value + (value == null));
        if (value == null) {
            return check(false, key, field.getText(), field);
        }
        if (value instanceof String) {
            return TimeUtil.isApplicationVariable((String) value);
        }
        if ( !(value instanceof Integer)) {
            return false;
        }
        int k = ((Integer) value).intValue();
        return check(k >= 0, key, field.getText(), field);
    }

    private boolean checkDbl(Object value, String key, JTextField field) {
        if (value == null) {
            return check(false, key, field.getText(), field);
        }
        if (value instanceof String) {
            return TimeUtil.isApplicationVariable((String) value);
        }
        if ( !(value instanceof Double)) {
            return false;
        }
        double k = ((Double) value).doubleValue();
        return check(k >= 0.0, key, field.getText(), field);
    }

    protected final boolean check(boolean condition, String key, String text, JComponent component) {
        if (condition) {
            return true;
        }
        printError(i18n(AbstractDialog.class, key, text));
        component.requestFocus();

        if (component instanceof JTextField) {
            ((JTextField) component).selectAll();
        }
        return false;
    }

    private String myValue;
    private JTextField myYear;
    private JTextField myMonth;
    private JTextField myDay;
    private JTextField myHour;
    private JTextField myMinute;
    private JTextField mySecond;

    private static final int FACTOR_9 = 9;
    private static final int FACTOR_10 = 10;
    private static final int TEXT_WIDTH = 60;
    private static final String DEFAULT_VALUE = "0"; // NOI18N
    private static final String CANCEL = "cancel"; // NOI18N
}
