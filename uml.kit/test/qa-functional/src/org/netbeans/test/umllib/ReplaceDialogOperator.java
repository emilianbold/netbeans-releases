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
import java.util.ListIterator;
import javax.swing.JComboBox;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.test.umllib.FindDialogOperator.SearchTarget;
import org.netbeans.test.umllib.actions.ReplaceInModelAction;
import org.netbeans.test.umllib.util.LabelsAndTitles;

public class ReplaceDialogOperator extends FindDialogOperator{
    
    public final static String BTN_REPLACE = "Replace";
    public final static String BTN_REPLACE_ALL = "Replace All";
    
    public final static String SEARCH_IN_ALIAS = "Alias";
    public final static String CBOX_REPLACE_WITH = "Replace With:";
    
    
    public ReplaceDialogOperator() {
        super(LabelsAndTitles.REPLACE_IN_MODEL_DIALOG_TITLE, null);
    }
    
    /**
     *
     * @param log
     */
    public ReplaceDialogOperator(PrintStream log) {
        super(LabelsAndTitles.REPLACE_IN_MODEL_DIALOG_TITLE, log);
    }
    
    /**
     *
     * @return
     */
    public static ReplaceDialogOperator invoke() {
        new ReplaceInModelAction().perform();
        return new ReplaceDialogOperator();
    }
    
    /**
     *
     * @param log
     * @return
     */
    public static ReplaceDialogOperator invoke(PrintStream log) {
        new ReplaceInModelAction().perform();
        return new ReplaceDialogOperator(log);
    }
    
    /**
     *
     * @param replaceString
     */
    public void setReplaceString(String replaceString){
        JLabelOperator dnLbl=new JLabelOperator(this,CBOX_REPLACE_WITH);
        JComboBoxOperator cbox = new JComboBoxOperator((JComboBox)(dnLbl.getLabelFor()));
        cbox.clearText();
        cbox.typeText(replaceString);
        cbox.pushKey(KeyEvent.VK_ENTER);
        
        // pressed enter activates find. this opens 'find' warning again if mach case was deselected.
        // we should close it.
        JCheckBoxOperator chbox = new JCheckBoxOperator(this, OPTION_MATCH_CASE);
        if (!chbox.isSelected()){
            closeFindConfirmationDialog();
            new EventTool().waitNoEvent(1500);
        }
        
    }
    
    /**
     *
     * @param flag
     */
    public void setOptionAlias(boolean flag){
        setOptionXPath(false);
        if (new JRadioButtonOperator(this, SEARCH_IN_ELEMENTS).isSelected()){
            JRadioButtonOperator rbtn = new JRadioButtonOperator(this, SEARCH_IN_ALIAS);
            if (rbtn.isSelected() != flag){
                rbtn.clickMouse(1);
            }
        }
    }
    
    public void clickReplace(){
        JButtonOperator btn = new JButtonOperator(this, BTN_REPLACE);
        if (btn.isEnabled()){
            btn.clickMouse(1);
        }
    }
    
    public void clickReplaceAll(){
        JButtonOperator btn = new JButtonOperator(this, BTN_REPLACE_ALL);
        if (btn.isEnabled()){
            btn.clickMouse(1);
        }
    }
    
    private void replaceAllItems(SearchTargetCriteria targetCriteria, LinkedList searchResults, String searchString, String replaceString, boolean flagMatchCase){
        ListIterator iter = searchResults.listIterator();
        while (iter.hasNext()){
            Object[] item = (Object[])iter.next();
            switch (targetCriteria){
                case NAME:
                    if (((String)item[0]).equals((String)item[1])){
                        item[1] = replaceString((String)item[1], searchString, replaceString, flagMatchCase);
                    }
                    item[0] = replaceString((String)item[0], searchString, replaceString, flagMatchCase);
                    break;
                case ALIAS:
                    item[1] = replaceString((String)item[1], searchString, replaceString, flagMatchCase);
                    break;
                default:
            }
            iter.set(item);
        }
    }
    
    private String replaceString(String sourceString, String searchString, String replaceString, boolean flagMatchCase){
        if (flagMatchCase){
            return sourceString.replaceAll(searchString, replaceString);
        }else{
            String result = sourceString;
            String rs = sourceString.toLowerCase();
            String ss = searchString.toLowerCase();
            int pos = rs.indexOf(ss, 0);
            while (pos != -1){
                result = result.substring(0, pos) + replaceString + result.substring(pos + ss.length());
                rs = result.toLowerCase();
                pos = rs.indexOf(ss, pos + replaceString.length());
            }
            return result;
        }
    }
    
    /**
     *
     * @param targetCriteria
     * @param itemsForSearch
     * @param searchString
     * @param replaceString
     * @param flagMatchCase
     * @param flagMatchWholeWord
     * @return
     */
    public boolean checkReplace(SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString, String replaceString, boolean flagMatchCase, boolean flagMatchWholeWord){
//        LinkedList ourResults = searchAndRemoveItems(targetCriteria, itemsForSearch, searchString, flagMatchCase, flagMatchWholeWord);
        LinkedList ourResults = getSearchResultsList();
        replaceAllItems(targetCriteria, ourResults, searchString, replaceString, flagMatchCase);
//        itemsForSearch.addAll(ourResults);
        getSearchResults().selectAll();
        clickReplace();
        new EventTool().waitNoEvent(1500);
        boolean res = compareResults(ourResults);
        return res;
    }
    
    /**
     *
     * @param targetCriteria
     * @param itemsForSearch
     * @param searchString
     * @param replaceString
     * @param flagMatchCase
     * @param flagMatchWholeWord
     * @return
     */
    public boolean checkReplaceAll(SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString, String replaceString, boolean flagMatchCase, boolean flagMatchWholeWord){
//        LinkedList ourResults = searchItems(targetCriteria, itemsForSearch, searchString, flagMatchCase, flagMatchWholeWord);
        LinkedList ourResults = getSearchResultsList();
        replaceAllItems(targetCriteria, ourResults, searchString, replaceString, flagMatchCase);
//        itemsForSearch.addAll(ourResults);
        clickReplaceAll();
        new EventTool().waitNoEvent(1500);
        boolean res = compareResults(ourResults);
        return res;
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
    protected LinkedList searchAndRemoveItems(SearchTargetCriteria targetCriteria, LinkedList itemsForSearch, String searchString, boolean flagMatchCase, boolean flagMatchWholeWord){
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
//            prn.println("ns=" + ns + " , as=" + as + " , ds=" + ds + " , ss=" + ss);
            switch (targetCriteria){
                case NAME:
                    if (flagMatchWholeWord){
                        if (ns.equals(ss)){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    } else {
                        if (ns.indexOf(ss) != -1){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    }
                    break;
                case ALIAS:
                    if (flagMatchWholeWord){
//                        prn.println("case ALIAS 1");
                        if (as.equals(ss)){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    } else {
//                        prn.println("case ALIAS 2");
                        if (as.indexOf(ss) != -1){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    }
                    break;
                case NAME_AND_ALIAS:
                    if (flagMatchWholeWord){
//                        prn.println("case NAME and ALIAS 1");
                        if (ns.equals(ss) || as.equals(ss)){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    } else {
//                        prn.println("case NAME and ALIAS 2");
                        if ((ns.indexOf(ss) != -1) || (as.indexOf(ss) != -1)){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    }
                    break;
                case DESCRIPTION:
                    if (flagMatchWholeWord){
                        if (ds.equals(ss)){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    } else {
                        if (ds.indexOf(ss) != -1){
                            result.add(cloneObjectArray(item));
                            iter.remove();
                        }
                    }
                    break;
                default:
            }
            
        }
        return result;
    }
    
    private Object[] cloneObjectArray(Object[] array){
        return new Object[]{new String((String)array[0]),
        new String((String)array[1]),
        new String((String)array[2]),
        (SearchTarget)array[3]
        };
    }
    
}
