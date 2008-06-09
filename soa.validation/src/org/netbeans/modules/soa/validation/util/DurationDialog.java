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
package org.netbeans.modules.soa.validation.util;

import java.awt.BorderLayout;
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
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.27
 */
public final class DurationDialog extends JDialog {

  public DurationDialog() {
    super((Frame) null, i18n(DurationDialog.class, "LBL_Duration"), true); // NOI18N
    a11y(this, i18n(DurationDialog.class, "ACS_Duration")); // NOI18N
    setLayout(new BorderLayout());
    add(getResizable(createPanel()), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    String cancel = "cancel"; // NOI18N
    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, cancel);

    Action action = new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        dispose();
      }
    };
    getRootPane().getActionMap().put(cancel, action);
    
    pack();
    
    Rectangle r = Utilities.getUsableScreenBounds();
    int maxW = (r.width * FACTOR_9) / FACTOR_10;
    int maxH = (r.height * FACTOR_9) / FACTOR_10;
    Dimension d = getPreferredSize();
    d.width = Math.min(d.width, maxW);
    d.height = Math.min(d.height, maxH);
    setBounds(Utilities.findCenterBounds(d));
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    JButton button;

    button = createButton("LBL_OK", "ACS_OK"); // NOI18N
    getRootPane().setDefaultButton(button);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (check()) {
          dispose();
        }
      }
    });
    panel.add(button);

    button = createButton("LBL_Cancel", "ACS_Cancel"); // NOI18N
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        dispose();
      }
    });
    panel.add(button);

    return panel;
  }

  private JButton createButton(String key, String a11y) {
    JButton button = new JButton(i18n(DurationDialog.class, key));
    a11y(button, i18n(DurationDialog.class, a11y));
    return button;
  }

  private JPanel createPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;

    myYear = createField("LBL_Year", "ACS_Year", c, panel); // NOI18N
    myMonth = createField("LBL_Month", "ACS_Month", c, panel); // NOI18N
    myDay = createField("LBL_Day", "ACS_Day", c, panel); // NOI18N

    c.gridy++;

    myHour = createField("LBL_Hour", "ACS_Hour", c, panel); // NOI18N
    myMinute = createField("LBL_Minute", "ACS_Minute", c, panel); // NOI18N
    mySecond = createField("LBL_Second", "ACS_Second", c, panel); // NOI18N

    return panel;
  }

  private JTextField createField(String key, String a11y, GridBagConstraints c, JPanel panel) {
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(INSET, INSET, 0, 0);
    c.anchor = GridBagConstraints.EAST;
    JLabel label = createLabel(i18n(DurationDialog.class, key));
    panel.add(label, c);

    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(INSET, INSET, 0, 0);
    c.anchor = GridBagConstraints.WEST;
    JTextField field = new JTextField(DEFAULT_VALUE);
    a11y(field, i18n(DurationDialog.class, a11y));
    setWidth(field, TEXT_WIDTH);
    panel.add(field, c);
    label.setLabelFor(field);

    return field;
  }

  public String getDuration() {
    return myDuration;
  }

  private boolean check() {
    int year = getInt(myYear.getText());
    int month = getInt(myMonth.getText());
    int day = getInt(myDay.getText());
    int hour = getInt(myHour.getText());
    int minute = getInt(myMinute.getText());
    double second = getDouble(mySecond.getText());

    if (
      check(year, "ERR_invalid_year", myYear) && // NOI18N
      check(month, "ERR_invalid_month", myMonth) && // NOI18N
      check(day, "ERR_invalid_day", myDay) && // NOI18N
      check(hour, "ERR_invalid_hour", myHour) && // NOI18N
      check(minute, "ERR_invalid_minute", myMinute) && // NOI18N
      check(second, "ERR_invalid_second", mySecond) // NOI18N
    ) {
      myDuration = DurationUtil.getContent(true, year, month, day, hour, minute, second);
      return true;
    }
    else {
      myDuration = null;
      return false;
    }
  }

  private boolean check(int value, String key, JTextField field) {
    return check(value >= 0, key, field);
  }

  private boolean check(double value, String key, JTextField field) {
    return check(value >= 0.0, key, field);
  }

  private boolean check(boolean condition, String key, JTextField field) {
    if (condition) {
      return true;
    }
    printError(i18n(DurationDialog.class, key, field.getText()));
    field.requestFocus();
    field.selectAll();

    return false;
  }

  private JTextField myYear;
  private JTextField myMonth;
  private JTextField myDay;
  private JTextField myHour;
  private JTextField myMinute;
  private JTextField mySecond;
  private String myDuration;

  private static final int TEXT_WIDTH = 60;
  private static final String DEFAULT_VALUE = "0"; // NOI18N
  private static final int INSET = 10;
  private static final int FACTOR_9 = 9;
  private static final int FACTOR_10 = 10;
}
