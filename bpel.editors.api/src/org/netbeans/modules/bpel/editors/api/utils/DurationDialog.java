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
package org.netbeans.modules.bpel.editors.api.utils;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.11.27
 */
public final class DurationDialog extends Dialog {

  private JPanel createPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;

    myYear = createField("LBL_Year", c, panel); // NOI18N
    myMonth = createField("LBL_Month", c, panel); // NOI18N
    myDay = createField("LBL_Day", c, panel); // NOI18N

    c.gridy++;

    myHour = createField("LBL_Hour", c, panel); // NOI18N
    myMinute = createField("LBL_Minute", c, panel); // NOI18N
    mySecond = createField("LBL_Second", c, panel); // NOI18N

    return panel;
  }

  private JTextField createField(String key, GridBagConstraints c, JPanel panel) {
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(10, 10, 0, 0);
    c.anchor = GridBagConstraints.EAST;
    JLabel label = createLabel(i18n(key));
    panel.add(label, c);

    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(10, 10, 0, 0);
    c.anchor = GridBagConstraints.WEST;
    JTextField field = new JTextField(DEFAULT_VALUE);
    setWidth(field, TEXT_WIDTH);
    panel.add(field, c);
    label.setLabelFor(field);

    return field;
  }

  public String getDuration() {
    return myDuration;
  }

  @Override
  protected DialogDescriptor createDescriptor() {
    myDescriptor = new DialogDescriptor(
      getResizable(createPanel()),
      i18n("LBL_Duration"), // NOI18N
      true, // modal
      new Object [] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION },
      DialogDescriptor.OK_OPTION,
      DialogDescriptor.DEFAULT_ALIGN,
      null,
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (DialogDescriptor.OK_OPTION == event.getSource()) {
            if (check()) {
              myDescriptor.setClosingOptions(
                new Object [] { DialogDescriptor.OK_OPTION,
                  DialogDescriptor.CANCEL_OPTION });
            }
            else {
              myDescriptor.setClosingOptions(
                new Object [] { DialogDescriptor.CANCEL_OPTION });
            }
          }
        }
      }
    );
    return myDescriptor;
  }

  private boolean check() {
    if (myDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
      int year = getInt(myYear.getText());
      int month = getInt(myMonth.getText());
      int day = getInt(myDay.getText());
      int hour = getInt(myHour.getText());
      int minute = getInt(myMinute.getText());
      int second = getInt(mySecond.getText());

      if (
        check(year, "ERR_invalid_year", myYear.getText()) && // NOI18N
        check(month, "ERR_invalid_month", myMonth.getText()) && // NOI18N
        check(day, "ERR_invalid_day", myDay.getText()) && // NOI18N
        check(hour, "ERR_invalid_hour", myHour.getText()) && // NOI18N
        check(minute, "ERR_invalid_minute", myMinute.getText()) && // NOI18N
        check(second, "ERR_invalid_second", mySecond.getText())) // NOI18N
      {
        myDuration = TimeEventUtil.getContent(true, year, month, day, hour, minute, second);
        return true;
      }
    }
    myDuration = null;
    return false;
  }

  private boolean check(int value, String key, String str) {
    if (value >= 0) {
      return true;
    }
    printError(i18n(key, str));
    return false;
  }

  private JTextField myYear;
  private JTextField myMonth;
  private JTextField myDay;
  private JTextField myHour;
  private JTextField myMinute;
  private JTextField mySecond;
  private String myDuration;
  private DialogDescriptor myDescriptor;

  private static final int TEXT_WIDTH = 60;
  private static final String DEFAULT_VALUE = "0"; // NOI18N
}
