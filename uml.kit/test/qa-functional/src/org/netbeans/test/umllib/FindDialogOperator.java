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


package org.netbeans.test.umllib;

import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JComboBox;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.test.umllib.actions.FindInModelAction;
import org.netbeans.test.umllib.util.LabelsAndTitles;

public class FindDialogOperator extends JDialogOperator{
    public final static String BTN_FIND = "Find";
    public final static String BTN_CLOSE = "Close";
    
    public final static String OPTION_MATCH_CASE = "Match case";
    public final static String OPTION_MATCH_WHOLE_WORD = "Match whole word only";
    public final static String OPTION_XPATH = "This is an XPath expression";
    public final static String OPTION_ALIAS = "Also search alias";

    public final static String SEARCH_IN_ELEMENTS = "Elements";
    public final static String SEARCH_IN_DESCRIPTIONS = "Descriptions";

    public final static String COLUMN_NAME = "Name";
    public final static String COLUMN_ALIAS = "Alias";
    
    public final static String CBOX_FIND_WHAT = "Find What:";
    
    private PrintStream prn = null;
    
    private EventTool eventTool = new EventTool();
    
    public FindDialogOperator() {
        this(LabelsAndTitles.FIND_IN_MODEL_DIALOG_TITLE, null);
    }

    /**
     * 
     * @param log 
     */
    public FindDialogOperator(PrintStream log) {
        this(LabelsAndTitles.FIND_IN_MODEL_DIALOG_TITLE, log);
    }
    
    /**
     * 
     * @param title 
     * @param log 
     */
    protected FindDialogOperator(String title, PrintStream log) {
        super(title);
        setLog(log);
    }
    
    /**
     * 
     * @return 
     */
    public static FindDialogOperator invoke() {
        new FindInModelAction().perform();
        return new FindDialogOperator();
    }

    /**
     * 
     * @param log 
     * @return 
     */
    public static FindDialogOperator invoke(PrintStream log) {
        new FindInModelAction().perform();
        return new FindDialogOperator(log);
    }
    
    protected void closeFindConfirmationDialog(){
        new Thread(new Runnable() {
            public void run() {
                JDialogOperator dlgFind = new JDialogOperator("Find Confirmation", 0);
                new JButtonOperator(dlgFind, "Yes").pushNoBlock();
            }
        }).start();
    }
    
    /**
     * 
     * @param projectName 
     * @param searchString 
     * @param flagMatchCase 
     * @param flagMatchWholeWord 
     * @param flagAlias 
     */
    public void doFindElements(String projectName, String searchString, boolean flagMatchCase, boolean flagMatchWholeWord, boolean flagAlias){
        setSearchString(searchString);
        setOptionSearchInElements();
        setOptionMatchCase(flagMatchCase);
        setOptionMatchWholeWord(flagMatchWholeWord);
        setOptionAlias(flagAlias);
        setProject(projectName);
        
        if (!flagMatchCase){
            closeFindConfirmationDialog();
        }
        clickFind();
        new EventTool().waitNoEvent(1500);
    }

    /**
     * 
     * @param projectName 
     * @param searchString 
     * @param flagMatchCase 
     * @param flagMatchWholeWord 
     */
    public void doFindDescriptions(String projectName, String searchString, boolean flagMatchCase, boolean flagMatchWholeWord){
        setSearchString(searchString);
        setOptionSearchInDescriptions();
        setOptionMatchCase(flagMatchCase);
        setOptionMatchWholeWord(flagMatchWholeWord);
        setProject(projectName);

        if (!flagMatchCase){
            closeFindConfirmationDialog();
        }
        
        clickFind();
        new EventTool().waitNoEvent(1500);
    }

    /**
     * 
     * @param projectName 
     * @param searchString 
     */
    public void doFindXPath(String projectName, String searchString){
        setSearchString(searchString);
        setOptionXPath(true);
        setProject(projectName);
        clickFind();
    }
    
    private void setSearchString(String searchString){
        JLabelOperator dnLbl=new JLabelOperator(this,CBOX_FIND_WHAT);
        JComboBoxOperator cbox = new JComboBoxOperator((JComboBox)(dnLbl.getLabelFor()));
        cbox.clearText();
        cbox.typeText(searchString);
        cbox.pushKey(KeyEvent.VK_ENTER);
    }
    
    /**
     * 
     * @param projectName 
     */
    public void setProject(String projectName){
        JListOperator projects = new JListOperator(this);
        int index = projects.findItemIndex(projectName);
        if (index >= 0){
            projects.clickOnItem(index, 1);
        }
    }

    public void setOptionSearchInElements(){
        JRadioButtonOperator rbtn = new JRadioButtonOperator(this, SEARCH_IN_ELEMENTS);
        if (!rbtn.isSelected()){
            rbtn.clickMouse(1);
        }
    }

    public void setOptionSearchInDescriptions(){
        JRadioButtonOperator rbtn = new JRadioButtonOperator(this, SEARCH_IN_DESCRIPTIONS);
        if (!rbtn.isSelected()){
            rbtn.clickMouse(1);
        }
    }
    
    /**
     * 
     * @param flag 
     */
    public void setOptionMatchCase(boolean flag){
        JCheckBoxOperator chbox = new JCheckBoxOperator(this, OPTION_MATCH_CASE);
        if (chbox.isSelected() != flag){
            chbox.clickMouse(1);
        }
    }

    /**
     * 
     * @param flag 
     */
    public void setOptionMatchWholeWord(boolean flag){
        JCheckBoxOperator chbox = new JCheckBoxOperator(this, OPTION_MATCH_WHOLE_WORD);
        if (chbox.isSelected() != flag){
            chbox.clickMouse(1);
        }
    }

    /**
     * 
     * @param flag 
     */
    public void setOptionAlias(boolean flag){
        if (new JRadioButtonOperator(this, SEARCH_IN_ELEMENTS).isSelected()){
            JCheckBoxOperator chbox = new JCheckBoxOperator(this, OPTION_ALIAS);
            if (chbox.isSelected() != flag){
                chbox.clickMouse(1);
            }
        }
    }
    
    /**
     * 
     * @param flag 
     */
    public void setOptionXPath(boolean flag){
        JCheckBoxOperator chbox = new JCheckBoxOperator(this, OPTION_XPATH);
        if (chbox.isSelected() != flag){
            chbox.clickMouse(1);
        }
    }
    
    public void clickFind(){
        new JButtonOperator(this, BTN_FIND).clickMouse(1);
    }
    
    public void clickClose(){
        new JButtonOperator(this, BTN_CLOSE).clickMouse(1);
    }
 
    /**
     * 
     * @return 
     */
    public JTableOperator getSearchResults(){
        return new JTableOperator(this, 0);
    }

    /**
     * 
     * @return 
     */
    public LinkedList getSearchResultsList(){
        log("===== getSearchResultsList =====");
        LinkedList list = new LinkedList();
        
        JTableOperator findResultsTable = getSearchResults();
        int colName = findResultsTable.findColumn(COLUMN_NAME);
        int colAlias = findResultsTable.findColumn(COLUMN_ALIAS);
        log("ColNameIndex = " + colName + " , colAliasIndex = " + colAlias);
        if ((colName == -1) || (colAlias == -1)){
            return null;
        }
        
        for(int i = 0; i < findResultsTable.getRowCount(); i++){
            String tn = findResultsTable.getValueAt(i, colName).toString();
            String ta = findResultsTable.getValueAt(i, colAlias).toString();
            list.add(new Object[]{new String(tn), new String(ta), "", SearchTarget.ANY});
            log("item: (" + tn + "," + ta + ")");
        }
        
        return list;
    }
    
//------------------------------------------------------------------------------    
    
    /**
     * 
     * @param targetCriteria 
     * @param itemsForSearch 
     * @param searchString 
     * @param flagMatchCase 
     * @param flagMatchWholeWord 
     * @return 
     */
    protected LinkedList searchItems(SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString, boolean flagMatchCase, boolean flagMatchWholeWord){
        LinkedList result = new LinkedList();
        String ss = searchString;
        Iterator iter = itemsForSearch.iterator();
        for(int i = 0; i < itemsForSearch.size(); i++){
            Object[] item = (Object[])iter.next();
            String ns = (String)item[0];
            String as = (String)item[1];
            String ds = (String)item[2];
            
            if (!flagMatchCase){
                ns = ns.toLowerCase();
                as = as.toLowerCase();
                ds = ds.toLowerCase();
                ss = ss.toLowerCase();
            }
            switch (targetCriteria){
                case NAME:
                    if (flagMatchWholeWord){
                        if (ns.equals(ss)){
                            result.add(item);
                        }
                    } else {
                        if (ns.indexOf(ss) != -1){
                            result.add(item);
                        }
                    }
                    break;
                case ALIAS:
                    if (flagMatchWholeWord){
                        if (as.equals(ss)){
                            result.add(item);
                        }
                    } else {
                        if (as.indexOf(ss) != -1){
                            result.add(item);
                        }
                    }
                    break;
                case NAME_AND_ALIAS:
                    if (flagMatchWholeWord){
                        if (ns.equals(ss) || as.equals(ss)){
                            result.add(item);
                        }
                    } else {
                        if ((ns.indexOf(ss) != -1) || (as.indexOf(ss) != -1)){
                            result.add(item);
                        }
                    }
                    break;
                case DESCRIPTION:
                    if (flagMatchWholeWord){
                        if (ds.equals(ss)){
                            result.add(item);
                        }
                    } else {
                        if (ds.indexOf(ss) != -1){
                            result.add(item);
                        }
                    }
                    break;
                default:
            }
            
        }
        return result;
    }
    
    /**
     * 
     * @param target 
     * @param targetCriteria 
     * @param itemsForSearch 
     * @param searchString 
     * @return 
     */
    protected LinkedList searchXPathItems(SearchTarget target, SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString){
        LinkedList result = new LinkedList();
        Iterator iter = itemsForSearch.iterator();
        for(int i = 0; i < itemsForSearch.size(); i++){
            Object[] item = (Object[])iter.next();
            String ns = (String)item[0];
            String as = (String)item[1];
            SearchTarget tgt = (SearchTarget)item[3];
            
            switch (targetCriteria){
                case NAME:
                        if ((tgt == target) && (ns.equals(searchString))){
                            result.add(item);
                        }
                    break;
                case ALIAS:
                        if ((tgt == target) && (as.equals(searchString))){
                            result.add(item);
                        }
                    break;
                default:
            }
        }
        
        return result;
    }
    

    /**
     * 
     * @param target 
     * @param targetCriteria 
     * @param itemsForSearch 
     * @param searchString 
     * @return 
     */
    public boolean checkXPathSearchResult(SearchTarget target, SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString){
        LinkedList ourResults = searchXPathItems(target, targetCriteria, itemsForSearch, searchString);
        return compareResults(ourResults);
    }
    
    /**
     * 
     * @param targetCriteria 
     * @param itemsForSearch 
     * @param searchString 
     * @param flagMatchCase 
     * @param flagMatchWholeWord 
     * @return 
     */
    public boolean checkSearchResult(SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString, boolean flagMatchCase, boolean flagMatchWholeWord){
        LinkedList ourResults = searchItems(targetCriteria, itemsForSearch, searchString, flagMatchCase, flagMatchWholeWord);
        return compareResults(ourResults);
    }
    
    /**
     * 
     * @param ourResults 
     * @return 
     */
    protected boolean compareResults(LinkedList ourResults){
        log("===== Compare results =====");
        JTableOperator findResultsTable = getSearchResults();
        int colName = findResultsTable.findColumn(COLUMN_NAME);
        int colAlias = findResultsTable.findColumn(COLUMN_ALIAS);
        log("ColNameIndex = " + colName + " , colAliasIndex = " + colAlias);
        if ((colName == -1) || (colAlias == -1)){
            return false;
        }
        log("ourResultsSize = " + ourResults.size() + " , tableResultsSize = " + findResultsTable.getRowCount());
        if (ourResults.size() != findResultsTable.getRowCount()){
            log("Items in ourResults:");
            for(int j=0; j < ourResults.size(); j++){
                Object[] item = (Object[])ourResults.get(j);
                log("item: name = " + (String)item[0] + " , alias = " + (String)item[1]);
            }
            log("Items in table:");
            for(int j=0; j < findResultsTable.getRowCount(); j++){
                String tn1 = findResultsTable.getValueAt(j, colName).toString();
                String ta1 = findResultsTable.getValueAt(j, colAlias).toString();
                log("item: name = " + tn1 + " , alias = " + ta1);
            }
            return false;
        }
        for(int i = 0; i < findResultsTable.getRowCount(); i++){
            String tn = findResultsTable.getValueAt(i, colName).toString();
            String ta = findResultsTable.getValueAt(i, colAlias).toString();
            boolean isRemoved = false;
            Iterator iter = ourResults.iterator();
            while (iter.hasNext()){
                Object[] item = (Object[])iter.next();
                if ((tn.equals((String)item[0])) && (ta.equals((String)item[1]))){
                    log("Remove item: name = " + (String)item[0] + " , alias = " + (String)item[1]);
                    iter.remove();
                    isRemoved = true;
                    break;
                }
            }
            if (!isRemoved){
                log("Last item not removed from ourResults");
                log("Items in ourResults:");
                for(int j=0; j < ourResults.size(); j++){
                    Object[] item = (Object[])ourResults.get(j);
                    log("item: name = " + (String)item[0] + " , alias = " + (String)item[1]);
                }
                log("Items in table:");
                for(int j=0; j < findResultsTable.getRowCount(); j++){
                    String tn1 = findResultsTable.getValueAt(j, colName).toString();
                    String ta1 = findResultsTable.getValueAt(j, colAlias).toString();
                    log("item: name = " + tn1 + " , alias = " + ta1);
                }
                return false;
            }
        }
        log("ourResultsSize = " + ourResults.size());
        if (ourResults.size() != 0){
            return false;
        }
        return true;
    }

    /**
     * 
     * @param log 
     */
    public void setLog(PrintStream log){
        prn = log;
    }
    
    /**
     * 
     * @param message 
     */
    protected void log(String message){
        if (prn != null){
            prn.println(message);
        }
    }
    
    public enum SearchTargetCriteria{
        NAME,
        ALIAS,
        NAME_AND_ALIAS,
        DESCRIPTION
    }
    
    public enum SearchTarget{
        ANY,
        PROJECT,
        DIAGRAM,
        CLASS,
        INTERFACE,
        PACKAGE,
        ATTRIBUTE,
        OPERATION,
        PARAMETER,
        DATATYPE,
        GENERALIZATION,
        IMPLEMENTATION
    }
}
