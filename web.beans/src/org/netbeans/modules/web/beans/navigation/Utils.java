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
package org.netbeans.modules.web.beans.navigation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JTree;
import javax.swing.SwingUtilities;


/**
 * Based on Utils class from java.navigation
 * @author ads
 *
 */
final class Utils {
    
    static void firstRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(0);
            scrollTreeToSelectedRow(tree);
        }
    }

    static void scrollTreeToSelectedRow(final JTree tree) {
        final int selectedRow = tree.getLeadSelectionRow();
        if (selectedRow >=0) {
            SwingUtilities.invokeLater(
                    new Runnable() {
                public void run() {
                    tree.scrollRectToVisible(tree.getRowBounds(selectedRow));
                }
            }
            );
        }
    }
    
    static void previousRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = (rowCount -1);
            } else {
                selectedRow--;
                if (selectedRow < 0) {
                    selectedRow = (rowCount -1);
                }
            }
            tree.setSelectionRow(selectedRow);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void nextRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = 0;
                tree.setSelectionRow(selectedRow);
            } else {
                selectedRow++;
            }
            tree.setSelectionRow(selectedRow % rowCount);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void lastRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(rowCount - 1);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static boolean patternMatch(JavaElement javaToolsJavaElement, String pattern, 
            String patternLowerCase) 
    {

        if (pattern == null) {
            return true;
        }

        String patternRegexpString = pattern;

        if (pattern.trim().length() == 0) {
            patternRegexpString = pattern + ".*";
        } else {
            patternRegexpString = pattern.
                    replaceAll(Pattern.quote("*"), Matcher.quoteReplacement(".*")).
                    replaceAll(Pattern.quote("?"), Matcher.quoteReplacement(".")) +
                    (pattern.endsWith("$") ? "" : ".*");
        }

        String name = javaToolsJavaElement.getName();

        try {
            Pattern compiledPattern = Pattern.compile(patternRegexpString,
                    WebBeansNavigationOptions.isCaseSensitive() ? 0
                                                           : Pattern.CASE_INSENSITIVE);
            Matcher m = compiledPattern.matcher(name);

            return m.matches();
        } catch (PatternSyntaxException pse) {
            if (WebBeansNavigationOptions.isCaseSensitive()) {
                return name.startsWith(pattern);
            }

            return name.toLowerCase().startsWith(patternLowerCase);
        }
    }
}
