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


class SortButton extends JButton {

    private static final int NEUTRAL = 0;
    private static final int A2Z = 1;
    private static final int Z2A = 2;
    private int state = NEUTRAL;
     
    static protected Icon[] icon = new Icon[3];
    static {
	icon[0] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/unsorted.gif")); // NOI18N)
	icon[1] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/a2z.gif")); // NOI18N
	icon[2] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/z2a.gif")); // NOI18N
    }
    
    public SortButton(final DisplayTable dt) {    
	
	super();
	this.setIcon(icon[state]); 
	this.setBorder(null);
	this.setBorderPainted(false);
	
	state = NEUTRAL;
	
	this.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    state++;
		    state=state%3;
		    JButton b = (JButton)e.getSource();
		    b.setIcon(icon[state]); 
		    
		    if(state == NEUTRAL)
			// PENDING
			dt.noSorting();
		    else if(state == A2Z) {
			dt.setSortAscending(true);
			dt.sortByName();
		    } else if(state == Z2A) {
			dt.setSortAscending(false);
			dt.sortByName();
		    }
		}
	    });
    }
} // SortButton
