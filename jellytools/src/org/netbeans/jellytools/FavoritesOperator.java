/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
