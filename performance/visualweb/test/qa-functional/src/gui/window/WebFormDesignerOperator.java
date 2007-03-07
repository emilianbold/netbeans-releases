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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;


/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebFormDesignerOperator  extends TopComponentOperator {
    private Component surface = null;
    
    /**
     * Creates a new instance of WebFormDesignerOperator
     */
    public WebFormDesignerOperator(String topComponentName) {
        this(topComponentName,0);
        surface = this.findSubComponent(new DesignerPaneChooser());     
    }
    
    public WebFormDesignerOperator(String topComponentName, int Index) {
        super(topComponentName,Index); 
    }
    
    public void switchToDesignView() {
        new JToggleButtonOperator(this,"Design").pushNoBlock(); // NOI18N
    }
    
    public void switchToJSPView() {
        new JToggleButtonOperator(this,"JSP").pushNoBlock(); // NOI18N
    }
    
    public void switchToCodeView() {
        new JToggleButtonOperator(this,"Java").pushNoBlock(); // NOI18N
    }
    
    public void closeDiscard() {
       close();
       try {
            JDialogOperator dop = new JDialogOperator("Question");
            JButtonOperator but = new JButtonOperator(dop,"Discard");
            but.pushNoBlock();           
       } catch(TimeoutExpiredException e) {
            //do nothing
       }
    }
    
    public void cancelSelection() {
        this.getDesignerPaneComponentOperator().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    public void deleteSelection() {
        this.getDesignerPaneComponentOperator().pushKey(java.awt.event.KeyEvent.VK_DELETE);
    }
    
    public void clickOnSurface() {
        System.out.println("Click on surface...");
        getDesignerPaneComponentOperator().clickMouse();
        new QueueTool().waitEmpty();
    }
    
    public void clickOnSurface(int x, int y) {
        System.out.println("Click on surface at: "+x+","+y);
        
        getDesignerPaneComponentOperator().clickMouse(x,y,1);
        new QueueTool().waitEmpty();

    }
    
    public JPopupMenuOperator clickPopup() {
        
        getDesignerPaneComponentOperator().clickForPopup();
        return new JPopupMenuOperator();
    }
    
    public JPopupMenuOperator clickPopup(int x, int y) {
        
        getDesignerPaneComponentOperator().clickForPopup(x,y);
        return new JPopupMenuOperator();
    }
    
    public ComponentOperator getDesignerPaneComponentOperator() {
        if(surface == null) {
            throw new JemmyException("The design surface component is empty");
        }
        return new ComponentOperator(surface);
    }
    
    public void dump() {
        System.out.println("Dumping nested components");
        Component[] liss = null;
        try {
            liss = this.getComponents();
        } catch (Exception ex) {
            System.out.println("Exception during getComponents call");
        }
        System.out.println("Liss counted: "+liss.length);
        for(int i = 0; i<liss.length;i++) {
            System.out.println(liss[i].toString());
        }
    }
    
    public static final class DesignerPaneChooser implements ComponentChooser {
        
        public boolean checkComponent(Component component) {
            //dumpComponent(component);
            // NB 5.5 class name
            //return component.getClass().getName().equals("com.sun.rave.designer.DesignerPane");
            // NB 6.0 class name
            return component.getClass().getName().equals("org.netbeans.modules.visualweb.designer.DesignerPane");
        }
        private void dumpComponent(Component component) {
            System.out.println(component.getClass().toString());
            System.out.println(component.getX());
            System.out.println(component.getY());
            System.out.println(component.getWidth());
            System.out.println(component.getHeight());
        }
        public String getDescription() {
            return "Designer Surface";
        }
    }
    
    public static final class WebFormDesignerSubchooser implements ComponentChooser {
        public boolean checkComponent(Component component) {
            return component.getClass().getName().equals("org.netbeans.modules.visualweb.designer.DesignerTopComp");
        }
        
        public String getDescription() {
            return " Web Designer Component";
        }
    }
}

