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


/*
 * DialogLifetime.java
 *
 * Created on January 11, 2005, 3:17 PM
 */

package org.netbeans.modules.search;

/**
 * Interface for dialogs that want to track dialog lifetime events. 
 * Namely OKs and Cancels. This is necessary for modal dialogs 
 * @author Ondrej Rypacek (rypacek@netbeans.org)
 */
public interface DialogLifetime {
    /**
     * Called when a dialog is closed with the OK button.
     */
    public void onOk();
    
    /**
     * Called when a dialog is closed with the Cancel button.
     */
    public void onCancel();
}
