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

/**
 * SortButton.java
 *
 *
 * Created: Fri Apr 27 15:44:33 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.*;     // widgets
import javax.swing.border.*;     // widgets
import javax.swing.event.*;
import java.awt.event.*;

import org.openide.util.NbBundle;


class SortButton extends JButton {

    private int state = DisplayTable.NEUTRAL;
     
    static protected Icon[] icon = new Icon[3];
    static {
	icon[0] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/unsorted.gif")); // NOI18N)
	icon[1] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/a2z.gif")); // NOI18N
	icon[2] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/z2a.gif")); // NOI18N
    }
    
    private static final boolean debug = false;
        
    public SortButton(final DisplayTable dt) {    
	
	super();
	this.setIcon(icon[state]); 
	this.setBorder(null);
	this.setBorderPainted(false);
        this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonUnsortedA11yDesc"));
	
	state = DisplayTable.NEUTRAL;
	
	this.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
	
		    if(debug) log("Sort requested"); //NOI18N
		    
		    state++;
		    state=state%3;

		    if(debug) log("State is: " + String.valueOf(state)); //NOI18N
		    JButton b = (JButton)e.getSource();
		    b.setIcon(icon[state]); 
		    
		    if(state == DisplayTable.NEUTRAL)
                    {
			// PENDING
                        SortButton.this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonUnsortedA11yDesc"));
                    }
		    else if(state == DisplayTable.A2Z) {
                        SortButton.this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonSortAZA11yDesc"));
		    } else if(state == DisplayTable.Z2A) {
                        SortButton.this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonSortZAA11yDesc"));
                    }
		    dt.setSorting(state);
		}
	    });
    }

    int getMode() { 
	return state;
    }

    private void log(String s) {
	System.out.println("SortButton::" + s); //NOI18N
    }
    
	

} // SortButton
