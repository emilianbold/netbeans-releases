/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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