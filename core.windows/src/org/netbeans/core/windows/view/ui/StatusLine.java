/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.openide.awt.StatusDisplayer;
import org.openide.util.WeakListeners;

/** The status line component of the main window. A text can be put into it.
*
* @author Jaroslav Tulach, Jesse Glick
*/
final class StatusLine extends JLabel implements ChangeListener, Runnable {
    private StatusDisplayer d = StatusDisplayer.getDefault();

    /** Creates a new StatusLine with specified workspace switcher. */
    public StatusLine () {
        run();
        d.addChangeListener(WeakListeners.change(this, d));
    }

    public void stateChanged(ChangeEvent e) {
        if(SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater (this);
        }
    }
    
    /** Called in event queue, should update the status text.
    */
    public void run () {
        setText (d.getStatusText ());
    }

}
