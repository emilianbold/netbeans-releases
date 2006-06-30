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

package org.netbeans.jellytools.modules.debugger;

import java.awt.Component;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.modules.debugger.actions.FinishAllAction;
import org.netbeans.jellytools.modules.debugger.actions.MakeCurrentAction;
import org.netbeans.jellytools.modules.debugger.actions.SessionsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;

/**
 * Provides access to the Sessions tom component.
 * <p>
 * Usage:<br>
 * <pre>
 *      SessionsOperator so = SessionsOperator.invoke();
 *      so.makeCurrent("MyClass");
 *      so.finishAll();
 *      so.close();
 * </pre>
 * 
 * 
 * @author Jiri.Skrivanek@sun.com
 */
public class SessionsOperator extends TopComponentOperator {

    private static final SessionsAction invokeAction = new SessionsAction();
    
    /** Waits for Sessions top component and creates a new operator for it. */
    public SessionsOperator() {
        super(waitTopComponent(null, 
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle",
                                        "CTL_Sessions_view"),
                0, viewSubchooser));
    }
    
    /**
     * Opens Sessions top component from main menu Window|Debugging|Sessions and
     * returns SessionsOperator.
     * 
     * @return instance of SessionsOperator
     */
    public static SessionsOperator invoke() {
        invokeAction.perform();
        return new SessionsOperator();
    }
    
    public TreeTableOperator treeTable() {
        return new TreeTableOperator(this);
    }
    
    /********************************** Actions ****************************/
    
    /** Performs Finish All action on Sessions view. */
    public void finishAll() {
        new FinishAllAction().perform(this);
    }
    
    /** Calls Make Current popup on given session.
     * @param sessionName display name of session
     */
    public void makeCurrent(String sessionName) {
        new MakeCurrentAction().perform(new Node(treeTable().tree(), sessionName));
    }
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser viewSubchooser = new ComponentChooser() {
        private static final String CLASS_NAME="org.netbeans.modules.debugger.ui.views.View";
        
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith(CLASS_NAME);
        }
        
        public String getDescription() {
            return "component instanceof "+CLASS_NAME;// NOI18N
        }
    };
}
