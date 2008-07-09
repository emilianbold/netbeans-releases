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

package org.netbeans.modules.visualweb.gravy.search;

import java.awt.Point;

import org.netbeans.modules.visualweb.gravy.Util;

import org.netbeans.modules.search.SearchPanel;

import java.awt.Component;

import org.netbeans.jellytools.Bundle;

import org.netbeans.jemmy.ComponentChooser;

import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;


/** 
 * SearchPanelOperator class 
 * @author <a href="mailto:sva@sparc.spb.su">Vladimir Strigun</a> 
 */
public class SearchPanelOperator extends JDialogOperator {

    JTabbedPaneOperator tabbedPane;

    public SearchPanelOperator() {
        super("Find In FIles");
    }        
    
    
    public void switchToObjectNameTab(){
        switchToTab(1);
    }

    public void switchToTypeTab(){
        switchToTab(2);
    }

    public Component switchToTab(int index){
        SearchPanelOperator searchPanel = new SearchPanelOperator();
        if(tabbedPane == null ) this.tabbedPane = new JTabbedPaneOperator(searchPanel, 0);
        return tabbedPane.selectPage(index); 
    }

    public void close(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_CANCEL"));
    }

    public void search(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_SEARCH"));
    }

    public void help(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_HELP"));
    }

    private void pushButton(String title){
        (new JButtonOperator(new SearchPanelOperator(), title)).push();
    }

    public static class SearchPanelChooser implements ComponentChooser {
        public SearchPanelChooser() {
        }
        public boolean checkComponent(Component comp) {
            return(comp instanceof org.netbeans.modules.search.SearchPanel);
        }
        public String getDescription() {
            return(SearchPanel.class.getName());
        }
    }

    public void setDateBetween(int dateNum, String date){
        switchToTab(3);
        (new JTextFieldOperator(this, dateNum)).typeText(date);
    }

    public void setWithinDate(String date){
        switchToTab(3);
        (new JTextFieldOperator(this, 0)).typeText(date);
    }

    public void pressCriterion(int tabNumber, boolean check){
        switchToTab(tabNumber);
        (new JCheckBoxOperator(this, 2)).changeSelection(check);
    }

    public void setSubstringText(int tabNumber, String text){
        switchToTab(tabNumber);
        (new JTextFieldOperator(this, 0)).typeText(text);
    }

    public String getSubstringText(int tabNumber){
        switchToTab(tabNumber);
        return (new JTextFieldOperator(this, 0)).getText();
    }

    public void setRegularExpression(int tabNumber, String text){
        switchToTab(tabNumber);
        (new JTextFieldOperator(this, 1)).typeText(text);
    }

    public void matchCase(int tabNumber, boolean matchCase){
        Component tab = switchToTab(tabNumber);
        (new JRadioButtonOperator(this, 0)).doClick();
        (new JCheckBoxOperator(this, 0)).changeSelection(matchCase);
    }

    public boolean getCaseState(int tabNumber){
        Component tab = switchToTab(tabNumber);
        (new JRadioButtonOperator(this, 0)).doClick();
        return (new JCheckBoxOperator(this, 0)).isSelected();
    }

    public void matchWholeWords(int tabNumber, boolean wholeWords){
        Component tab = switchToTab(tabNumber);
        (new JRadioButtonOperator(this, 0)).doClick();
        (new JCheckBoxOperator(this, 1)).changeSelection(wholeWords);
    }


    public boolean getWholeWordsState(int tabNumber){
        Component tab = switchToTab(tabNumber);
        (new JRadioButtonOperator(this, 0)).doClick();
        return (new JCheckBoxOperator(this, 1)).isSelected();
    }

    public boolean getCriterionState(int tabNumber){
        Component tab = switchToTab(tabNumber);
        (new JRadioButtonOperator(this, 0)).doClick();
        return (new JCheckBoxOperator(this, 2)).isSelected();
    }

    public void saveSettings(int tabNumber, String name){
        Component tab = switchToTab(tabNumber);  
        (new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_SAVE_AS"))).push();
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_LABEL_SAVE_CRITERION"));
        (new JTextFieldOperator(dialog)).typeText(name);
        (new JButtonOperator(dialog, "OK")).push();
    }

    public void restoreSettings(int tabNumber, String name){
        Component tab = switchToTab(tabNumber);  
        (new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_RESTORE"))).push();
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_LABEL_RESTORE_CRITERION"));
        (new JComboBoxOperator(dialog)).selectItem(name);
        (new JButtonOperator(dialog, "OK")).push();
    }

}
    
