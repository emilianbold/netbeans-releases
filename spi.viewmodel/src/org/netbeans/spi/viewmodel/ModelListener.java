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

package org.netbeans.spi.viewmodel;

import java.util.EventListener;


/**
 * Notifies about changes in view model.
 *
 * @author   Jan Jancura
 * @since 1.4
 */
public interface ModelListener extends EventListener {

    /**
     * View model has been changed notification.
     * 
     * @param event an event object describing change
     * @since 1.4
     */
    public void modelChanged (ModelEvent event);
}
