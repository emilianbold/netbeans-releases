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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.openide.awt.StatusDisplayer;
import org.openide.util.WeakListeners;

/** The status line component of the main window. A text can be put into it.
*
* @author Jaroslav Tulach, Jesse Glick
*/
final class StatusLine extends JLabel implements ChangeListener, Runnable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5644391883356409841L;
    
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

    public static JComponent createLabel () {
        StatusLine statusText = new StatusLine ();

        JPanel panel = new JPanel();
        
        // Not to squeeze status line, see #19910.
        statusText.setText(" "); // NOI18N
        statusText.setPreferredSize(new Dimension(0, statusText.getPreferredSize().height));
        
        panel.setLayout(new BorderLayout());
        panel.add(new JSeparator(), BorderLayout.NORTH);
        panel.add(statusText, BorderLayout.CENTER);
        return panel;
    }

}
