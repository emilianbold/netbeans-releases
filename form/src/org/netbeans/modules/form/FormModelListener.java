/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

/**
 * Listener class for notifying about changes in FormModel. There's only one
 * method to implement, with an array of FormModelEvent objects as* parameter.
 * (FormModel does batch event firing, all the events corresponding to one user
 * action are fired at once.)
 *
 * @author Tomas Pavek
 */

public interface FormModelListener extends java.util.EventListener {

    /** Notification about changes made in FormModel. Type of the changes
     * can be obtained from FormModelEvent.getChangeType() method.
     * @param events array of events fired from FormModel
     */
    public void formChanged(FormModelEvent[] events);
}
