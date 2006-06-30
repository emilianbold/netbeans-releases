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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Clone Document" popup menu item, "Window|Clone Document" main menu item or
 * "org.openide.actions.CloneViewAction".
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class CloneViewAction extends Action {

    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", 
                                                                    "Menu/Window");
    /** "Window|Clone Document" main menu item. */
    private static final String menuPath = windowItem
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle", 
                                                                      "CTL_CloneDocumentAction");

    /** "Clone Document" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                    "LBL_CloneDocumentAction");

    /** Create new CloneViewAction instance. */
    public CloneViewAction() {
        super(menuPath, popupPath, "org.openide.actions.CloneViewAction");
    }
    
    /** Performs popup action Clone Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and cloned
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "CloneViewAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action Clone Window on given top component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param tco top component operator which should be activated and cloned
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }
    
    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }
    
}