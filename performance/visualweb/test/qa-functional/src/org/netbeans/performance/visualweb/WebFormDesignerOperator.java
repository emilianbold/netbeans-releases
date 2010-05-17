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

package org.netbeans.performance.visualweb;

import java.awt.Component;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.ContainerOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebFormDesignerOperator  extends TopComponentOperator {

    private ComponentOperator  surfacecomp =  null;
    /**
     * Creates a new instance of WebFormDesignerOperator
     * @param topComponentName creating component name
     */
    public WebFormDesignerOperator(String topComponentName) {
        this(topComponentName,0);
    }
    
    public WebFormDesignerOperator(String topComponentName, int Index) {

        super(TopComponentOperator.waitTopComponent(null,topComponentName, Index, new WebFormDesignerSubchooser()));
        try {
            surfacecomp = new ComponentOperator(this, new DesignerPaneChooser());
        } catch(TimeoutExpiredException tex) {
            JemmyProperties.getCurrentOutput().getOutput().println("TimeoutExpired exception");
            throw new JemmyException("Cannot find designer surface because of expired timeout");            
        }      
    }

    public void SetCaretOff() {
        Component surf = surfacecomp.getSource();
    }
    
    public void SetCaretOn() {
        Component surf = surfacecomp.getSource();        
    }

    /**
     * Find web designer operator located certain top component
     * @param topComponentName name of the top component
     * @return WebFormDesignerOperator
     */
    public static WebFormDesignerOperator findWebFormDesignerOperator(String topComponentName){
        return findWebFormDesignerOperator(topComponentName, true);
    }

    /**
     * Find web designer operator located certain top component
     * @param topComponentName name of the top component
     * @param exactlyMatch flag to match component name exactly
     * @return WebFormDesignerOperator
     */
    public static WebFormDesignerOperator findWebFormDesignerOperator(String topComponentName, boolean exactlyMatch){
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",120000);
        
        StringComparator oldOperator = Operator.getDefaultStringComparator();
        if(exactlyMatch) {            
            Operator.setDefaultStringComparator(new DefaultStringComparator(false, false));            
        }
        WebFormDesignerOperator webFormDesignerOperator =  new WebFormDesignerOperator(topComponentName);
        Operator.setDefaultStringComparator(oldOperator);
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",oldTimeout);
        return webFormDesignerOperator;
    }
        
    public void switchToDesignView() {
        ContainerOperator JSFContainer = new ContainerOperator(this.getParent().getParent());
        JToggleButtonOperator designViewButton = new JToggleButtonOperator(JSFContainer,"Design"); // NOI18N
        
        if(!designViewButton.isSelected())
            designViewButton.pushNoBlock();
    }
    
    public void switchToJSPView() {
        ContainerOperator JSFContainer = new ContainerOperator(this.getParent().getParent());        
        JToggleButtonOperator jspViewButton = new JToggleButtonOperator(JSFContainer,"JSP"); // NOI18N
        
        if(!jspViewButton.isSelected())
            jspViewButton.pushNoBlock();
    }
    
    public void switchToCodeView() {
        ContainerOperator JSFContainer = new ContainerOperator(this.getParent().getParent());        
        JToggleButtonOperator javaViewButton = new JToggleButtonOperator(JSFContainer,"Java"); // NOI18N
        
        if(!javaViewButton.isSelected())
            javaViewButton.pushNoBlock();
    }
    
    @Override
    public  void closeDiscard() {
        
        new Thread(new Runnable() {
            public void run() {
                pushMenuOnTab(Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","LBL_CloseWindowAction"));
            };
        }, "thread to close TopComponent").start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // do nothing
        }
        
        try {
            NbDialogOperator dop = new NbDialogOperator("Question"); // NOI18N
            JButtonOperator but = new JButtonOperator(dop,"Discard"); // NOI18N
            but.push();
        } catch(TimeoutExpiredException exc){
            // do nothing
        }
    }
    
    public void cancelSelection() {
        this.getDesignerPaneComponentOperator().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    public void deleteSelection() {
        this.getDesignerPaneComponentOperator().pushKey(java.awt.event.KeyEvent.VK_DELETE);
    }
    
    public void clickOnSurface() {
        getDesignerPaneComponentOperator().clickMouse();
        new QueueTool().waitEmpty();
    }
    
    public void clickOnSurface(int x, int y) {
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
    
    public void pushPopupMenu(String menuPath){
        JPopupMenuOperator popup = clickPopup();
        popup.pushMenuNoBlock(menuPath);
    }
    
    public void pushPopupMenu(String menuPath, int x, int y){
        JPopupMenuOperator popup = clickPopup(x,y);
        popup.pushMenuNoBlock(menuPath);
    }
    
    public ComponentOperator getDesignerPaneComponentOperator() {
        if(surfacecomp == null) { throw new JemmyException("The design surface component is empty"); }
        return surfacecomp;
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

    public static final class TopComponentSubchooser implements ComponentChooser {

        public boolean checkComponent(Component component) {
            JemmyProperties.getCurrentOutput().getOutput().println(component.getClass().getName());
            boolean result = component.getClass().getName().equals("org.netbeans.core.multiview.MultiViewCloneableTopComponent");
            
            return  result;
        }

        public String getDescription() {
            return "Web Designer TopComponent";
        }
    }
    
    public static final class WebFormDesignerSubchooser implements ComponentChooser {
        public boolean checkComponent(Component component) {
            return component.getClass().getName().equals("org.netbeans.modules.visualweb.designer.jsf.ui.JsfTopComponent");
        }
        
        public String getDescription() {
            return "Web Designer Component";
        }
    }
}

