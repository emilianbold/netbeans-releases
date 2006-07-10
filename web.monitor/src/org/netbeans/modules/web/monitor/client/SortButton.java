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

/**
 * @author Ana von Klopp
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.openide.util.NbBundle;


class SortButton extends JButton {

    private int state = DisplayTable.NEUTRAL;

    private Icon[] icon = new Icon[3];
    
    public SortButton(final DisplayTable dt) {    
	
	super();
	icon[0] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/unsorted.gif")); // NOI18N)
	icon[1] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/a2z.gif")); // NOI18N
	icon[2] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/z2a.gif")); // NOI18N
	setIcon(icon[state]); 
	setBorder(null);
	setBorderPainted(false);
        setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonUnsortedA11yDesc"));
	
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
	
                    Logger.getLogger(SortButton.class.getName()).info("Sort requested");
		    
		    state++;
		    state=state%3;

                    Logger.getLogger(SortButton.class.getName()).info("State is: " + String.valueOf(state));
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
} // SortButton
