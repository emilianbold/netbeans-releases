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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.FavoritesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Favorites TopComponent.<p>
 * Functionality related to files tree is delegated to JTreeOperator (method
 * tree()) and nodes.<p>
 *
 * Example:<p>
 * <pre>
 *      FavoritesOperator fo = FavoritesOperator.invoke();
 *      // or when Favorites pane is already opened
 *      //FavoritesOperator fo = new FavoritesOperator();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = fo.tree();
 *      // work with nodes
 *      new Node(tree, "myNode|subnode").select();
 * </pre> 
 *
 * @see FavoritesAction
 */
public class FavoritesOperator extends TopComponentOperator {
    
    static final String FAVORITES_CAPTION = Bundle.getStringTrimmed(
                                            "org.netbeans.modules.favorites.Bundle", 
                                            "Favorites");
    private static final FavoritesAction viewAction = new FavoritesAction();
    
    private JTreeOperator _tree;
    
    /** Search for Favorites TopComponent within all IDE. */
    public FavoritesOperator() {
        super(waitTopComponent(null, FAVORITES_CAPTION, 0, new FavoritesTabSubchooser()));
    }

    /** invokes Favorites and returns new instance of FavoritesOperator
     * @return new instance of FavoritesOperator */
    public static FavoritesOperator invoke() {
        viewAction.performMenu();
        return new FavoritesOperator();
    }
    
    /** Getter for Favorites JTreeOperator
     * @return JTreeOperator of files tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.favorites.Tab
     * Used in constructor.
     */
    private static final class FavoritesTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().equals("org.netbeans.modules.favorites.Tab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.favorites.Tab";
        }
    }
}
