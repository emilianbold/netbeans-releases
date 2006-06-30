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
