/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JTable;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;

/**
 * Interceptor to fire an event to a class which implements FireEvent interface.
 * @author tl156378
 */
public class AddAttrTableRowListener extends AddTableRowListener{
    
    private FireEvent fireEvent;
    
    /**
     * Constructor
     * @param table the Jtable in which a row is to add
     * @param model the corresponding table model
     * @param remButton a reference to the remove line button
     * @param fireEvent the panel to notify for events
     */
    public AddAttrTableRowListener(JTable table, AbstractJMXTableModel model, 
            JButton remButton, FireEvent fireEvent) {
            super(table, model, remButton);
            this.fireEvent = fireEvent;
    }
    
    /**
     * Method handling what to do if the listener has been invoked
     * Here: adds a row
     * @param e an ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        fireEvent.event();
    }
    
}
