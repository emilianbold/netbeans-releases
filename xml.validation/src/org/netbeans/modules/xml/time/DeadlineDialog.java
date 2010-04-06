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

import java.awt.GridBagConstraints;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.08.13
 */
public final class DeadlineDialog extends AbstractDialog {

    public DeadlineDialog() {
        this(TimeUtil.getUntilValue(new Date(System.currentTimeMillis())));
    }

    public DeadlineDialog(String value) {
        super(value, i18n(DeadlineDialog.class, "LBL_Deadline"), i18n(DeadlineDialog.class, "ACS_Deadline")); // NOI18N
    }

    @Override
    protected Time parseValue(String value) {
        return TimeUtil.parseDeadline(value, false);
    }

    @Override
    protected String createValue(Object year, Object month, Object day, Object hour, Object minute, Object second) {
        return TimeUtil.getUntilValue(year, month, day, hour, minute, second, myTimeZone.getText().trim());
    }

    @Override
    protected void createComponent(JPanel panel, GridBagConstraints c) {
        myTimeZone = createField("LBL_TimeZone", "ACS_TimeZone", panel, c); // NOI18N
    }

    @Override
    protected void setTime(Time time) {
        if (time instanceof Deadline) {
            myTimeZone.setText(((Deadline) time).getTimeZone());
        }
    }

    @Override
    protected boolean checkValue() {
        String value = myTimeZone.getText();

        if ( !check(value)) {
            return check(false, "ERR_invalid_time_zone", "{+|-}hh:mm", myTimeZone); // NOI18N
        }
        return true;
    }

    private boolean check(String value) {
        value = value.trim();
        int k = value.length();

        if (k != 2 + 2 + 2) {
            return false;
        }
        int i = 0;

        if (value.charAt(i) != '-' && value.charAt(i) != '+') {
            return false;
        }
        i++;

        if (!isDigit(value.charAt(i))) {
            return false;
        }
        i++;

        if (!isDigit(value.charAt(i))) {
            return false;
        }
        i++;

        if (value.charAt(i) != ':') {
            return false;
        }
        i++;

        if (!isDigit(value.charAt(i))) {
            return false;
        }
        i++;

        if (!isDigit(value.charAt(i))) {
            return false;
        }
        return true;
    }

    private JTextField myTimeZone;
}
