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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import java.util.TimerTask;

import javax.swing.SwingUtilities;


/**
 *
 * @author  ayub khan
 */
public class SwingThreadTask extends TimerTask {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* task */
    private TimerTask task = null;

    /**
     *
     *
     */
    public SwingThreadTask(TimerTask task) {
        super();
        this.task = task;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * run
     *
     */
    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    task.run();
                }
            }
        );
    }
}
