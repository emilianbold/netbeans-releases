/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

/** Class implementing all necessary methods for handling "Question" dialog.
 *
 * @author Jiri.Kovalsky@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class QuestionDialogOperator extends NbDialogOperator {

    /** instance of JLabelOperator of question */
    private JLabelOperator _lblQuestion;


    /** Waits until dialog with "Question" title is found.
     * If dialog is not found, runtime exception is thrown.
     */
    public QuestionDialogOperator() {
        super(Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title"));
    }
    
    /** Waits until dialog with "Question" title and given text is found.
     * If dialog is not found, runtime exception is thrown.
     * @param questionLabelText text to be compared to text dialog
     */
    public QuestionDialogOperator(String questionLabelText) {
        this();
        _lblQuestion = new JLabelOperator(this, questionLabelText);
    }
    
    /** Returns operator of question's label.
     * @return JLabelOperator instance of question's label
     */
    public JLabelOperator lblQuestion() {
        if(_lblQuestion == null) {
            _lblQuestion = new JLabelOperator(this);
        }
        return _lblQuestion;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblQuestion();
    }
}