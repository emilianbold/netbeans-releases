/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.test.editor.search;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.editor.impl.SearchBar;
import org.openide.util.Exceptions;

/**
 *
 * @author jp159440
 */
public class SearchBarOperator {

    private JPanel searchBar;

    private enum CheckBoxes {

        MatchCase, WholeWords, ReqularExpression, HighlightResults;
    }
    private Set<CheckBoxes> inMenu = new HashSet<CheckBoxes>();
    private JButton prevButton;
    private JButton nextButton;
    private JButton closeButton;
    private JButton expandButton;
    private boolean isPopupVisible = false;

    //----- Operators ----
    private JComboBoxOperator findOp;
    private JButtonOperator nextButtonOp;
    private JButtonOperator prevButtonOp;
    private JButtonOperator closeButtonOp;
    private JCheckBoxOperator match;
    private JCheckBoxOperator whole;
    private JCheckBoxOperator regular;
    private JCheckBoxOperator highlight;

    public SearchBarOperator(JPanel searchBar) {
        this.searchBar = searchBar;
        int checkboxes = 0;
        int buttons = 0;
        for (Component c : searchBar.getComponents()) {
            if (c instanceof JCheckBox) {
                inMenu.add(CheckBoxes.values()[checkboxes++]);
            }
            if (c instanceof JButton) {
                switch (++buttons) {
                    case 1:
                        prevButton = (JButton) c;
                        break;
                    case 2:
                        nextButton = (JButton) c;
                        break;
                    case 3:
                        closeButton = (JButton) c;
                        break;
                    case 4:
                        expandButton = closeButton;
                        closeButton = (JButton) c;
                        break;
                }
            }
        }
    }

    public JComboBoxOperator findCombo() {
        if (findOp == null) {
            findOp = new JComboBoxOperator(new ContainerOperator(searchBar));
        }
        return findOp;
    }

    public JButtonOperator prevButton() {
        if (prevButtonOp == null) {
            prevButtonOp = new JButtonOperator(prevButton);
        }
        return prevButtonOp;
    }

    public JButtonOperator nextButton() {
        if (nextButtonOp == null) {
            nextButtonOp = new JButtonOperator(nextButton);
        }
        return nextButtonOp;
    }

    public JButtonOperator closeButton() {
        if (closeButtonOp == null) {
            closeButtonOp = new JButtonOperator(closeButton);
        }
        return closeButtonOp;
    }

    public JCheckBoxOperator matchCaseCheckBox() {
        return getCheckbox(CheckBoxes.MatchCase);

    }

    public JCheckBoxOperator highlightResultsCheckBox() {
        return getCheckbox(CheckBoxes.HighlightResults);

    }

    public JCheckBoxOperator reqularExpressionCheckBox() {
        return getCheckbox(CheckBoxes.ReqularExpression);

    }

    public JCheckBoxOperator wholeWordsCheckBox() {
        return getCheckbox(CheckBoxes.WholeWords);

    }

    public void expandPopup() {
        if (!isPopupVisible) {
            JButtonOperator jButtonOperator = new JButtonOperator(expandButton);
            jButtonOperator.push();
            isPopupVisible = true;
        }
    }

    private JPopupMenuOperator getPopupMenuOperator() {
        try {
            expandPopup();
            SearchBar bar = (SearchBar) searchBar;            
            Field field = bar.getClass().getDeclaredField("expandPopup");
            field.setAccessible(true);
            JPopupMenu menu = (JPopupMenu) field.get(bar);
            if(!menu.isVisible()) throw new IllegalStateException("Popup menu is not visible");
            JPopupMenuOperator jPopupMenuOperator = new JPopupMenuOperator(menu);
            return jPopupMenuOperator;
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException(ex);
        }
        
        
    }

    private JCheckBoxOperator getCheckbox(CheckBoxes checkBox) {
        int i = Arrays.binarySearch(CheckBoxes.values(), checkBox);
        if(i<0) throw new IllegalArgumentException("Invalid checkbox");
        if (inMenu.contains(checkBox)) {
            return new JCheckBoxOperator(new ContainerOperator(searchBar),i);  
        } else {
            JPopupMenuOperator jpmo = getPopupMenuOperator();
            return new JCheckBoxOperator(jpmo,i-inMenu.size());                        
        }
    }

    public void closePopup() {
        if(isPopupVisible) {
            ContainerOperator containerOperator = new ContainerOperator(searchBar);
            containerOperator.pressMouse();
            new EventTool().waitNoEvent(100);
            containerOperator.releaseMouse();
            isPopupVisible = false;
        }
    }

    public void testOperator() {
        matchCaseCheckBox().setSelected(true);
        new EventTool().waitNoEvent(1000);
        matchCaseCheckBox().setSelected(false);
        wholeWordsCheckBox().setSelected(true);
        new EventTool().waitNoEvent(1000);
        wholeWordsCheckBox().setSelected(false);
        reqularExpressionCheckBox().setSelected(true);
        new EventTool().waitNoEvent(1000);
        reqularExpressionCheckBox().setSelected(false);
        highlightResultsCheckBox().setSelected(true);
        new EventTool().waitNoEvent(1000);
        highlightResultsCheckBox().setSelected(false);
        closePopup();
        new EventTool().waitNoEvent(1000);
        closeButton().push();
        new EventTool().waitNoEvent(1000);
    }
}
