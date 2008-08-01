/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.xml.schema.abe;

import org.netbeans.test.xml.schema.*;
import java.awt.Container;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.xml.schema.abe.ElementPanel;
import org.netbeans.test.xml.schema.lib.util.Helpers;

/**
 *
 * @author Mikhail Matveev
 */
public class DVNodeOperator extends DVGenericNodeOperator{
    
    private ElementPanel m_panel=null;
    
    public DVNodeOperator(Container comp){
        super(comp);
        m_panel=(ElementPanel)comp;
    }
    
    public DVNodeOperator(ContainerOperator parent, String text){
        
        this(parent,text,null);
    }
    
    public DVNodeOperator(ContainerOperator parent, String text, boolean ce, boolean ccs){
        
        this(parent,text,new DefaultStringComparator(ce,ccs));
    }    
    
    public DVNodeOperator(ContainerOperator parent, String text, StringComparator comparator){
        
	super(parent,text,comparator);
        m_panel=(ElementPanel)m_label.getSource().getParent().getParent();
    }    
    
    public boolean isExpanded() {
        return m_panel.isExpanded();
    }
    
   public void setExpanded(boolean value) {
        m_panel.setExpanded(value);
        if (value){
            m_panel.expandChild();
        }
    }

    public ElementPanel getElementPanel() {
        return m_panel;
    }
    
    public void renameRefactor(String name){
        clickForPopupRobot();
        Helpers.waitNoEvent();
        new JPopupMenuOperator().pushMenuNoBlock("Refactor|Rename...");
        Helpers.waitNoEvent();
        new RefactoringDialogOperator(name).refactorImmediately();        
    }
    
    public void deleteRefactor(){
        clickForPopupRobot();
        Helpers.waitNoEvent();
        new JPopupMenuOperator().pushMenuNoBlock("Refactor|Safe Delete...");
        Helpers.waitNoEvent();
        new RefactoringDialogOperator(null).refactorImmediately();        
    }    

    public void delete(){
        clickForPopupRobot();
        Helpers.waitNoEvent();
        new JPopupMenuOperator().pushMenuNoBlock("Delete");
        Helpers.waitNoEvent();
        new RefactoringDialogOperator(null).refactorImmediately();        
    }    
    
    public DVGenericNodeOperator addAttribute(String name){
        clickForPopupRobot();
        new JPopupMenuOperator().pushMenu("Add|Attribute");
        
        DVGenericNodeOperator res=new DVGenericNodeOperator(this, DesignViewOperator.defaultAttributeName, true, true);
        res.getLabel().setText(name);
        res.pushKey(KeyEvent.VK_ENTER);
        return res;
    }    
    
    
}
