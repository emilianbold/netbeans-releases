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

//import org.netbeans.modules.search.ResultView;
import org.netbeans.modules.visualweb.gravy.TopComponentOperator;

import java.awt.Component;
import org.netbeans.jellytools.Bundle;

import org.netbeans.jemmy.ComponentChooser;

import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

import javax.swing.*;

/** 
 * SearchResultsOperator class 
 * @author <a href="mailto:sva@sparc.spb.su">Vladimir Strigun</a> 
 */
public class SearchResultsOperator extends TopComponentOperator{

    JTreeOperator tree;

    public SearchResultsOperator(ContainerOperator cont) {
        super(cont, new SearchResultsChooser());
    }
    public SearchResultsOperator() {
        //this(Util.getMainTab());
        this(Util.getMainWindow());
    }
    
    public static class SearchResultsChooser implements ComponentChooser {
        public SearchResultsChooser() {
        }
        public boolean checkComponent(Component comp) {
            Class curClass = comp.getClass();
            while(curClass != null) {
                if(curClass.getName().equals("org.netbeans.modules.search.ResultView")) {
                    return(true);
                }
                curClass = curClass.getSuperclass();
            }
            return(false);
        }
        public String getDescription() {
            return("org.netbeans.modules.search.ResultView");
        }
    }
    
    public JTreeOperator getTree() {
        if(tree == null) {
            makeComponentVisible();
            tree = new JTreeOperator(this);
        }
        return(tree);
    }

    public void close(){
        //pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_CANCEL"));
        super.close();
    }

    public void pushButton(String title){
        Util.getMainTab().setSelectedIndex(Util.getMainTab().findPage("Search Results"));
        (new JButtonOperator(this, title)).push();
    }

    public void help(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_HELP"));
    }

    public void modifySearch(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_CUSTOMIZE"));
    }

    public void showAllDetails(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_FILL"));
    }

    public void stopSearch(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_STOP"));
    }

    public void showInExplorer(){
        pushButton(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_SHOW"));
    }

    public void sortByName(){
        (new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_SORT"))).doClick();
    }

    public void sortByDefault(){
        (new JRadioButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_UNSORT"))).doClick();
    }

    public void selectFoundedFile(String node){
        JTreeOperator resTree = getTree();
        tree.selectPath(tree.findPath(node));
    }

    public String getFoundedFileName(int index){
        JTreeOperator resTree = getTree();
        return (tree.getChild(tree.getRoot(), index)).toString();
    }

    public int getFoundedFilesCount(){
        JTreeOperator resTree = getTree();
        return tree.getChildCount(tree.getRoot());
    }

}
    
