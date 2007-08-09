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
package org.netbeans.jellytools.modules.web;

import org.netbeans.jellytools.*;
import java.awt.Component;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/**Keeps methods to access navigator component.
 *
 * @author Jindrich Sedek
 */
public class NavigatorOperator extends TopComponentOperator{
    private JTreeOperator treeOperator;
    
    private static final String NAVIGATOR_TITLE =
            Bundle.getString("org.netbeans.modules.navigator.Bundle", "LBL_Navigator");
    
    /** NavigatorOperator is created for navigator window. 
     *  Navigator window must be displayed.
     */ 
    public NavigatorOperator(){
        super(waitTopComponent(null, NAVIGATOR_TITLE, 0, new NavigatorComponentChooser()));
    }
    
    /** This function dislays navigator window and returns operator for it
     * 
     *@return navigator operator
     * 
     */ 
    public static NavigatorOperator invokeNavigator() {
        new NavigatorAction().perform();
        return new NavigatorOperator();
    }
    
    /**Using navagation Tree you can access root node and then it's childen 
     * recursively
     * 
     * @return Operator of the navigation tree
     */ 
    public JTreeOperator getTree(){
        if (treeOperator == null){
            treeOperator = new JTreeOperator(this, 0);
        }
        return treeOperator;
    }
    
    private static final class NavigatorAction extends Action{
        private static final String navigatorActionName = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")
                + "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.navigator.Bundle", "Menu/Window/Navigator")
                + "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.navigator.Bundle", "LBL_Action");
        
        public NavigatorAction() {
            super(navigatorActionName, null, "org.netbeans.modules.navigator.ShowNavigatorAction");
        }
    }
    
    private static final class NavigatorComponentChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.navigator.NavigatorTC"));
        }
        
        public String getDescription() {
            return "Navigator Window";
        }
    }
    
    
    
    
}
