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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;


/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebFormDesignerOperator  extends TopComponentOperator {
    private String topComponentName;
    private Component designerPaneComponent = null;
    
    /**
     * Creates a new instance of WebFormDesignerOperator
     */
    public WebFormDesignerOperator(String topComponentName) {
        this(topComponentName,0);
    }
    
    public WebFormDesignerOperator(String topComponentName, int Index) {
        super(waitTopComponent(null,topComponentName,Index,new WebFormDesignerSubchooser()));
        this.topComponentName = topComponentName;
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
    
    public void close() {
        // need to find parent MultiviewTopComponent and close it
        new TopComponentOperator(findParentTopComponent()).close();
    }
    
    public void closeDiscard() {
        System.out.println("::	close and discard Designer surface");
        TopComponentOperator tco = new TopComponentOperator(topComponentName);
        //tco.closeDiscard();
        tco.closeWindow();
        //tco.close();
        System.out.println("::closing and wait Question dialog");
        try {
            NbDialogOperator dialog = new NbDialogOperator(NbDialogOperator.waitJDialog("Question", false, false));
            JButtonOperator db = new JButtonOperator(dialog,"Discard");
            db.pushNoBlock();
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
            //Do nothing
        }
        
    }
    
    public void cancelSelection() {
        this.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    public void deleteSelection() {
        this.pushKey(java.awt.event.KeyEvent.VK_DELETE);
    }
    
    public void clickOnSurface() {
        if(designerPaneComponent == null) {
            designerPaneComponent = getDesignerPaneComponent();
        }
        ComponentOperator wrap = new ComponentOperator(designerPaneComponent);
        wrap.clickMouse();
    }
    
    public void clickOnSurface(int x, int y) {
        if(designerPaneComponent == null) {
            designerPaneComponent = getDesignerPaneComponent();
            
        }
        ComponentOperator wrap = new ComponentOperator(designerPaneComponent);
        
        wrap.clickMouse(x,y,1);
        System.out.println("Mouse clicked on surface at: "+x+","+y);
    }
    
    public JPopupMenuOperator clickPopup() {
        if(designerPaneComponent == null) {
            designerPaneComponent = getDesignerPaneComponent();
            
        }
        ComponentOperator wrap = new ComponentOperator(designerPaneComponent);
        wrap.clickForPopup();
        
        return new JPopupMenuOperator();
    }
    
    public JPopupMenuOperator clickPopup(int x, int y) {
        if(designerPaneComponent == null) { designerPaneComponent = getDesignerPaneComponent(); }
        ComponentOperator wrap = new ComponentOperator(designerPaneComponent);
        wrap.clickForPopup(x,y);
        return new JPopupMenuOperator();
    }
    
    private Component getDesignerPaneComponent() {
        return this.findSubComponent(new DesignerPaneChooser());
    }
    
    public ComponentOperator getDesignerPaneComponentOperator() {
        return new ComponentOperator(designerPaneComponent);
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
            System.out.println("passed component to DesignerPaneChooser = "+component.toString());
            return component.getClass().getName().equals("org.netbeans.modules.visualweb.designer.DesignerPane");
        }
        public String getDescription() {
            return "Designer Surface";
        }
    }
    
    public static final class WebFormDesignerSubchooser implements ComponentChooser {
        public boolean checkComponent(Component component) {
            System.out.println("passed component = "+component.toString());
            return component.getClass().getName().equals("org.netbeans.modules.visualweb.designer.DesignerTopComp");
        }
        
        public String getDescription() {
            return " Web Designer Component";
        }
    }
}

