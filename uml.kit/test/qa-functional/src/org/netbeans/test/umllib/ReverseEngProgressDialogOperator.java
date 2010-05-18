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


/*
 * AddJavaProjectDialogOperator.java
 *
 * Created on March 29, 2005, 7:16 PM
 */

package org.netbeans.test.umllib;

import javax.swing.tree.TreePath;
import org.netbeans.jemmy.operators.JTextFieldOperator ;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.jemmy.Timeout;
/**
 *
 * @author VijayaBabu Mummaneni
 */
public class ReverseEngProgressDialogOperator extends NbDialogOperator {
    private JTreeOperator _javaProjects = null;
    private JButtonOperator _btSaveMessage = null;
    private JButtonOperator _btDone = null;
    
    /** Creates a new instance of AddJavaProjectDialogOperator */
    public ReverseEngProgressDialogOperator() {
        super(LabelsAndTitles.RE_PROGRESS_DIALOG_TITLE);
    }
    
    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator javaProjects() {
        if (_javaProjects==null) {
            _javaProjects = new JTreeOperator(this);
        }
        return _javaProjects;
    }
    
    /** returns selected path in java projects
     * @return TreePath
     */
    public TreePath getSelectedProject() {
        return javaProjects().getSelectionPath();
    }
    /**
     * Selects given project category
     * @param project 
     */
    public void selectProject(String project) {
        new Node(javaProjects(), project).select();
    }
    
    
    /** Returns operator of "Done" button.
     * @return  JButtonOperator instance of "Done" button
     */
    public JButtonOperator btDone() {
        if (_btDone == null) {
            _btDone = new JButtonOperator(this, "Done");
        }
        return _btDone;
    }
    
    /** Pushes "Done" button. */
    public void clickDone() {
        btDone().push();
    }
    
    public void clickDoneNoBlock() {
        btDone().pushNoBlock();
    }
    
    
    /** Returns operator of "SaveMessage..." button.
     * @return  JButtonOperator instance of "Done" button
     */
    public JButtonOperator btSaveMessage() {
        if (_btSaveMessage == null) {
            _btSaveMessage = new JButtonOperator(this, "Save Message...");
        }
        return _btSaveMessage;
    }
    
    /**
     * Pushes "SaveMessage..." button.
     * @param resultsFileName 
     */
    public void clickSaveMessageToFile(String resultsFileName) {
        btSaveMessage().pushNoBlock();
        NbDialogOperator saveDailog = new NbDialogOperator(LabelsAndTitles.RE_RESULTS_SAVE_DIALOG_TITLE);
        new Timeout("", 5000).sleep();
        JTextFieldOperator tfOper = new JTextFieldOperator(saveDailog, 0);
         // clear text field
        tfOper.setText("");        
        tfOper.typeText(resultsFileName);
        JButtonOperator btOper = new JButtonOperator(saveDailog, "Save");
        btOper.push();
        
       
    }
    
    public void clickSaveMessageNoBlock() {
        btSaveMessage().pushNoBlock();
    }
    
    
}
