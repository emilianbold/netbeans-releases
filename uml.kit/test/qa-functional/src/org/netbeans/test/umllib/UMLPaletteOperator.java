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

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.lang.Enum;
import javax.swing.JList;
import javax.swing.ListModel;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.util.LibProperties;
import org.netbeans.test.umllib.DNDDriver;

public class UMLPaletteOperator extends TopComponentOperator {
    
    public final static String PALETTE_NAME = "Palette";
    
    private static String debug_info;
    
    private EventTool eventTool = new EventTool();
    public UMLPaletteOperator() {
        super(PALETTE_NAME);
        waitComponentVisible(true);
    }
    
    
    /**
     * 
     * @param toolName 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public void selectTool(String toolName) throws NotFoundException{   
        debug_info="Select Tool Starts;\n";
        makeComponentVisible();
        waitComponentVisible(true);
        //
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        ComponentChooser listChooser = new JListByItemChooser(toolName);   
        Component comp = waitSubComponent(listChooser);
        if (comp == null){
            throw new NotFoundException("The element with name "+ toolName +" was not found on UML palette");
        }
        Container cont = comp.getParent();        
        JToggleButtonOperator btn = new JToggleButtonOperator(new ContainerOperator(cont));
        JListOperator listOperator = new JListOperator((JList)comp);
        eventTool.waitNoEvent(500);
        if (!btn.isSelected()){
            btn.pushNoBlock();
            btn.waitSelected(true);
        }
        try{Thread.sleep(100);}catch(Exception ex){}
        //refresh
        listOperator = new JListOperator((JList)comp);
        int itemIndex = listOperator.findItemIndex(new PaletteListItemChooser(toolName));
        try
        {
            listOperator.scrollToItem(itemIndex);
        }
        catch(TimeoutExpiredException ex)
        {
            //sometimes scrolling generate exceptions without visible reason
            //may be thhid check will help
            listOperator = new JListOperator((JList)comp);//refresh list operator
            if(((JList)(listOperator.getSource())).getLastVisibleIndex()<itemIndex || ((JList)(listOperator.getSource())).getFirstVisibleIndex()>itemIndex)throw new UMLCommonException("Scrolling, current index: "+itemIndex+"; min: "+((JList)(listOperator.getSource())).getFirstVisibleIndex()+"; max: "+((JList)(listOperator.getSource())).getLastVisibleIndex());
            //else all is good and we can click
        }
        listOperator.getTimeouts().setTimeout("JScrollBarOperator.WholeScrollTimeout",  500);
        try{Thread.sleep(100);}catch(Exception ex){}
        if(listOperator.getSelectedIndex()!=itemIndex)
        {
            listOperator.clickOnItem(itemIndex, 1);
            listOperator.waitItemSelection(itemIndex,true);
            try{Thread.sleep(100);}catch(Exception ex){}
        }       
        listOperator.getTimeouts().setTimeout("JScrollBarOperator.WholeScrollTimeout",  30000);
        //wait again with waiter
        waitSelection(toolName,true);
    }
    
    
    /**
     * 
     * @param type 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public void selectToolByType(Enum type) throws NotFoundException{
        selectTool(getToolNameByElementType(type));
    }
   
    /**
     * @return 
     */
    public String getSelectedItemname()
    {
        JList lst=null;
        JListWithSelectionChooser tmp=new JListWithSelectionChooser();
        lst=(JList)findSubComponent(tmp);
        Object sel=null;
        if(lst!=null)sel=lst.getSelectedValue();
        return (sel!=null)?(sel.toString()):("null");
    }
    
    /**
     * 
     * @param elementType 
     * @return 
     */
    public String getToolNameByElementType(Enum elementType) {
        return LibProperties.getCurrentToolName(elementType);
    }
    
    
    /**
     * 
     * @param groupName 
     */
    public void expandGroup(String groupName){
        makeComponentVisible();
        JToggleButtonOperator groupBtn = new JToggleButtonOperator(this, groupName);
        if (!groupBtn.isSelected()){
            groupBtn.pushNoBlock();
            groupBtn.waitSelected(true);
        }               
    }
    
    
    /**
     * 
     * @param groupName 
     */
    public void collapseGroup(String groupName){
        makeComponentVisible();
        JToggleButtonOperator groupBtn = new JToggleButtonOperator(this, groupName);
        if (groupBtn.isSelected()){
            groupBtn.pushNoBlock();
            groupBtn.waitSelected(false);
        }               
    }
    
    
    
    private class PaletteListItemChooser implements JListOperator.ListItemChooser{
	String itemLabel;
	
	public PaletteListItemChooser(String itemLabel) {
	    this.itemLabel = itemLabel;	    
	}

	public boolean checkItem(JListOperator oper, int index) {
            Object obj = oper.getModel().getElementAt(index);
            return itemLabel.equals(obj.toString());
            
            //FilterNode node = (FilterNode)obj;
            //getOutput().printError("chooser:"+node.getDisplayName());
	    //return (node.getDisplayName().equals(itemLabel));				     
	}

	public String getDescription() {
	    return("Item equal to \"" + itemLabel + "\" string");
	}
    }
    
    
    
    class JListByItemChooser implements ComponentChooser {
        String itemName = null;
        
        
        /**
         * 
         * @param itemName 
         */
        JListByItemChooser(String itemName) {
            this.itemName = itemName.trim();
            System.out.println("   Item Name = \"" + itemName + "\"");
        }
        
        
        /*
         * (non-Javadoc)
         *
         * @see org.netbeans.jemmy.ComponentChooser#checkComponent(java.awt.Component)
         */
        /**
         * 
         * @param comp 
         * @return 
         */
        public boolean checkComponent(Component comp){
                
                if (comp instanceof JList){
                    JList list = (JList)comp;
                    ListModel model = list.getModel();
                    for(int i = 0; i < model.getSize(); i++) {
                        Object obj = model.getElementAt(i);

                        System.out.println("   Elem = \"" + obj.toString() + "\"");
                        if(itemName.equals(obj.toString())){
                            return true;
                        }
                        /*
                        if (obj instanceof FilterNode){
                            FilterNode node = (FilterNode)obj;
                            //getOutput().printError("display:|"+node.getDisplayName()+ "|  "+obj+" index"+i+"search:"+itemName);                            
                            if (node.getDisplayName().trim().equals(itemName)){                                
                                return true;
                            }                        
                        }
                         */
                    }
                }
                return false;
            }
    
        /**
         * 
         * @return 
         */
            public String getDescription(){
                return "Tool list by tool with name \""+itemName+"\" chooser";
            }       
    }
    class JListWithSelectionChooser implements ComponentChooser {
        /**
          */
        JListWithSelectionChooser() {
         }
        
        
        /*
         * (non-Javadoc)
         *
         * @see org.netbeans.jemmy.ComponentChooser#checkComponent(java.awt.Component)
         */
        /**
         * 
         * @param comp 
         * @return 
         */
        public boolean checkComponent(Component comp){
                
                if (comp instanceof JList){
                    JList list = (JList)comp;
                    if(list.getSelectedIndices().length>0)return true;
                    }
                
                return false;
            }
    
        /**
         * 
         * @return 
         */
            public String getDescription(){
                return "Tool list which have selected elements";
            }       
    }
    
    /**
     * wait tool to be selected/deselected
     * do not open/close, scroll lists or make palette visible
     * @param toolName 
     * @param selected 
     */
    public void waitSelection(String toolName,boolean selected)
    {
        ComponentChooser listChooser = new JListByItemChooser(toolName);   
        Component comp = waitSubComponent(listChooser);
        if (comp == null){
            throw new NotFoundException("The element with name "+ toolName +" was not found on UML palette");
        }
        Container cont = comp.getParent();        
        JToggleButtonOperator btn = new JToggleButtonOperator(new ContainerOperator(cont));
         JListOperator listOperator = new JListOperator((JList)comp);
       eventTool.waitNoEvent(500);
        int itemIndex = listOperator.findItemIndex(new PaletteListItemChooser(toolName));
        listOperator.waitItemSelection(itemIndex, selected);
    }
    
    /**
     * 
     * @param type 
     * @param selected 
     */
    public void waitSelection(Enum type,boolean selected)
    {
        waitSelection(getToolNameByElementType(type), selected);
    }
    
    public static String getDebugInfo()
    {
        return debug_info;
    }
   
   public void dndToolByType(Enum type, ComponentOperator drawingArea, Point drawingAreaPoint) throws NotFoundException {
        dndTool(getToolNameByElementType(type),  drawingArea, drawingAreaPoint);
   }
      
      
   public void dndTool(String toolName, ComponentOperator drawingArea, Point drawingAreaPoint) throws NotFoundException {
        //selectToolByType(type);
        makeComponentVisible();
        waitComponentVisible(true);
        //
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        ComponentChooser listChooser = new JListByItemChooser(toolName);   
        Component comp = waitSubComponent(listChooser);
        if (comp == null){
            throw new NotFoundException("The element with name "+ toolName +" was not found on UML palette");
        }
        Container cont = comp.getParent();        
        JToggleButtonOperator btn = new JToggleButtonOperator(new ContainerOperator(cont));
        JListOperator listOperator = new JListOperator((JList)comp);
        eventTool.waitNoEvent(500);
        if (!btn.isSelected()){
            btn.pushNoBlock();
            btn.waitSelected(true);
        }
        try{Thread.sleep(100);}catch(Exception ex){}
        //refresh
        listOperator = new JListOperator((JList)comp);
        int itemIndex = listOperator.findItemIndex(new PaletteListItemChooser(toolName));
        try
        {
            listOperator.scrollToItem(itemIndex);
        }
        catch(TimeoutExpiredException ex)
        {
            //sometimes scrolling generate exceptions without visible reason
            //may be thhid check will help
            listOperator = new JListOperator((JList)comp);//refresh list operator
            if(((JList)(listOperator.getSource())).getLastVisibleIndex()<itemIndex || ((JList)(listOperator.getSource())).getFirstVisibleIndex()>itemIndex)throw new UMLCommonException("Scrolling, current index: "+itemIndex+"; min: "+((JList)(listOperator.getSource())).getFirstVisibleIndex()+"; max: "+((JList)(listOperator.getSource())).getLastVisibleIndex());
            //else all is good and we can click
        }
        listOperator.getTimeouts().setTimeout("JScrollBarOperator.WholeScrollTimeout",  500);
        try{Thread.sleep(100);}catch(Exception ex){}
        if(listOperator.getSelectedIndex()!=itemIndex)
        {
            listOperator.clickOnItem(itemIndex, 1);
            listOperator.waitItemSelection(itemIndex,true);
            try{Thread.sleep(100);}catch(Exception ex){}
        }       
        listOperator.getTimeouts().setTimeout("JScrollBarOperator.WholeScrollTimeout",  30000);
        //wait again with waiter
        waitSelection(toolName,true);
        Point paletteClickPoint=listOperator.getClickPoint(itemIndex);
        
        DNDDriver dndDriver = new DNDDriver();
       
        dndDriver.dnd(listOperator, paletteClickPoint, drawingArea, drawingAreaPoint,
                InputEvent.BUTTON1_MASK, 0);
         
    }
    
     
}
