/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTree;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;

import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.jemmy.util.EmptyVisualizer;

/**
 * Handle IDE's org.openide.explorer.view.TreeTable component
 * which is used instead of JTree in Options dialog, SetupWizard, ...
 */
public class TreeTableOperator extends JTableOperator {
    
    private JTreeOperator _tree;

    /**
     * Creates new instance.
     */    
    public TreeTableOperator(JTable view) {
        super(view);
    }
    
    /**
     * Creates new instance for the first TreeTable in container.
     */
    public TreeTableOperator(ContainerOperator cont) {
	this((JTable)
	     waitComponent(cont, 
			   new TreeTableFinder(ComponentSearcher.
                                               getTrueChooser("Any TreeTable")),
			   0));
	copyEnvironment(cont);
    }

    /**
     * Returns operator for a tree which is showed as a part of
     * the table.
     */
    public JTreeOperator tree() {
        if(_tree == null) {
            // cell renderer component for first column is JTree
            Object value = getValueAt(0, 0);
            JTree jTree = (JTree)getCellRenderer(0, 0).getTableCellRendererComponent((JTable)this.getSource(), value, false, false, 0, 0);
            // Need to set EmptyVisualizer because found JTree doesn't have any parent Container
            // and calling makeComponentVisible() throws NPE
            _tree = new JTreeOperator(jTree);
            _tree.setVisualizer(new EmptyVisualizer());
        }
        return _tree;
    }

    static class TreeTableFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public TreeTableFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
            Class cls = comp.getClass();
            do {
                if(cls.getName().equals("org.openide.explorer.view.TreeTable")) {
                    return(subFinder.checkComponent(comp));
                }
            } while((cls = cls.getSuperclass()) != null);
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }

}
