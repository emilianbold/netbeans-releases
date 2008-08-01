/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.xml.schema.abe;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author Misha
 */
public class RefactoringDialogOperator extends JDialogOperator{
    
    protected String m_newName;
    
    public RefactoringDialogOperator(String newName){
        super();
        m_newName=newName;
        if (m_newName!=null){
            getTextField().setText(m_newName);
        }
    }
    
    public JTextFieldOperator getTextField(){
        return new JTextFieldOperator(this);
    }
    
    public void refactorImmediately(){
        new JButtonOperator(this, "Refactor").push();
    }
    
    public void refactorPreview(){
        new JButtonOperator(this, "Preview").push();
    }    

}
