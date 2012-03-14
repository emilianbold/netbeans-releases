/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tasks.ui.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JComponent;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Issue.Status;
import org.netbeans.modules.tasks.ui.DashboardTopComponent;

/**
 *
 * @author jpeska
 */
public class Utils {

    private final static int VISIBLE_START_CHARS = 5;

    public static String getTaskPlainDisplayText(Issue task, JComponent component, int maxWidth) {
        return computeFitText(component, maxWidth, task.getDisplayName(), false);
    }

    public static String getTaskDisplayString(Issue task, JComponent component, int maxWidth, boolean active) {
        String displayName;
        String fitText = computeFitText(component, maxWidth, task.getDisplayName(), active);
        String activeText = active ? "<b>" + fitText + "</b>" : getFilterBoldText(fitText); //NOI18N
        //TODO task.isFinished is not in the API
//        if (task.isFinished()) {
//            displayName = "<html><font color=\"gray\"><strike>" + activeText + "</strike></font><html>"; //NOI18N
//        }

        Status status = task.getStatus();
        if (status == Status.NEW) {
            displayName = "<html><font color=\"green\">" + activeText + "</font></html>"; //NOI18N
        } else if (status == Status.MODIFIED) {
            displayName = "<html><font color=\"blue\">" + activeText + "</font></html>"; //NOI18N
        } else {
            displayName = "<html>" + activeText + "</html>"; //NOI18N
        }
        return displayName;
    }

    public static String computeFitText(JComponent component, int maxWidth, String text, boolean bold) {
        if (text == null) {
            text = ""; // NOI18N
        }
        if (text.length() <= VISIBLE_START_CHARS + 3) {
            return text;
        }
        FontMetrics fm;
        if (bold) {
            fm = component.getFontMetrics(component.getFont().deriveFont(Font.BOLD));
        } else {
            fm = component.getFontMetrics(component.getFont());
        }
        int width = maxWidth;

        String sufix = "..."; // NOI18N
        int sufixLength = fm.stringWidth(sufix + " "); //NOI18N
        int desired = width - sufixLength;
        if (desired <= 0) {
            return text;
        }

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                if (fm.stringWidth(text.substring(i + 1)) <= fm.stringWidth(sufix)) {
                    return text;
                }
                return prefix.length() > 0 ? prefix + sufix : text;
            }
        }
        return text;
    }

    private static String getFilterBoldText(String fitText) {
        String filterText = DashboardTopComponent.findInstance().getFilterText();
        if (!filterText.equals("")) { //NOI18N
            int searchIndex = 0;
            StringBuilder sb = new StringBuilder(fitText);

            int index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            final String boldTag = "<b>"; //NOI18N
            final String boldCloseTag = "</b>"; //NOI18N
            while (index != -1) {
                sb.insert(index, boldTag);
                index = index + boldTag.length() + filterText.length();
                sb.insert(index, boldCloseTag);
                searchIndex = index + boldCloseTag.length();
                index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            }
            return sb.toString();
        } else {
            return fitText;
        }
    }
}
