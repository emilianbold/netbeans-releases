/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.palette;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 *
 * @author Stanislav Aubrecht
 */
public class DummyActions extends PaletteActions {
    
    private Action[] paletteActions = new Action[] { new DummyAction(1), new DummyAction(2), new DummyAction(3) };
    private Action[] categoryActions = new Action[] { new DummyAction(10), new DummyAction(20), new DummyAction(30) };
    private Action[] itemActions = new Action[] { new DummyAction(100), new DummyAction(200), new DummyAction(300) };
    
    private Action preferredAction;
    
    /** Creates a new instance of DummyActions */
    public DummyActions() {
    }

    public javax.swing.Action getPreferredAction(org.openide.util.Lookup item) {
        return preferredAction;
    }
    
    void setPreferredAction( Action a ) {
        this.preferredAction = a;
    }

    public javax.swing.Action[] getCustomItemActions(org.openide.util.Lookup item) {
        return itemActions;
    }

    public javax.swing.Action[] getCustomCategoryActions(org.openide.util.Lookup category) {
        return categoryActions;
    }

    public javax.swing.Action[] getImportActions() {
        return null;
    }

    public javax.swing.Action[] getCustomPaletteActions() {
        return paletteActions;
    }
    
    private static class DummyAction extends AbstractAction {
        public DummyAction( int id ) {
            super( "Action_" + id );
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            System.out.println( "Action " + getValue( Action.NAME ) + " invoked." );
        }
    }
}
