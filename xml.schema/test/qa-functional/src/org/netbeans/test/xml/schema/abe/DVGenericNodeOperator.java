package org.netbeans.test.xml.schema.abe;

import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JLabelOperator.JLabelByLabelFinder;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author Mikhail Matveev
 */
public class DVGenericNodeOperator extends ContainerOperator{
    
    protected JLabelOperator m_label=null;
    protected MouseRobotDriver drv=new MouseRobotDriver(new Timeout("",  50));
    
    public DVGenericNodeOperator(Container comp){
        super(comp);
        m_label=new JLabelOperator((JLabel)waitComponent(comp, new JLabelByLabelFinder(comp.getName(), getComparator()),0));
    }
    
    public DVGenericNodeOperator(ContainerOperator parent, String text){
        
        this(parent,text,null);
    }
    
    public DVGenericNodeOperator(ContainerOperator parent, String text, boolean ce, boolean ccs){
        
        this(parent,text,new DefaultStringComparator(ce,ccs));
    }    
    
    public DVGenericNodeOperator(ContainerOperator parent, String text, StringComparator comparator){
        
	super(waitComponent(parent, new JLabelByLabelFinder(text, comparator==null?parent.getComparator():comparator),0).getParent().getParent());
        m_label=new JLabelOperator((JLabel)waitComponent(this, new JLabelByLabelFinder(text, this.getComparator()),0));
	copyEnvironment(parent);        
    }    
    
    public JLabelOperator getLabel(){
        return m_label;
    }
    
    public void clickMouseRobot(){
        drv.clickMouse(m_label, m_label.getCenterXForClick(), m_label.getCenterYForClick(), 1, InputEvent.BUTTON1_MASK, 0, new Timeout("",  30));
    }    
    
    public void dblClickMouseRobot(){
        drv.clickMouse(m_label, m_label.getCenterXForClick(), m_label.getCenterYForClick(), 2, InputEvent.BUTTON1_MASK, 0, new Timeout("",  30));
    }        
    
    public void clickForPopupRobot(){
        drv.clickMouse(m_label, m_label.getCenterXForClick(), m_label.getCenterYForClick(), 1, InputEvent.BUTTON3_MASK, 0, new Timeout("",  30));
    }
    
    public void renameInplace(String name){
        dblClickMouseRobot();
        m_label.setText(name);
    }
    
    public DVNodeOperator addElement(String name){
        clickForPopupRobot();
        new JPopupMenuOperator().pushMenu("Add|Element");
        DVNodeOperator res=new DVNodeOperator(this, DesignViewOperator.defaultElementName, true, true);
        res.getLabel().setText(name);
        res.pushKey(KeyEvent.VK_ENTER);
        return res;
    }    
    
}
