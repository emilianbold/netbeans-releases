/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import javax.swing.JComponent;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IAcceleratorManager
{
	// Register a window to receive WM_COMMAND notifications when an accelerator keycode is detected. Notifications will only be sent if the window has focus unless bNoFocus is TRUE.
	public void register(JComponent hwnd, IAcceleratorListener listener, String accelerator, boolean bNoFocus );
	public void register(JComponent hwnd, IAcceleratorListener listener, ETList<String> accelerators, boolean bNoFocus );
        public void register(JComponent hwnd, IAcceleratorListener listener, int keyCode, int modifierMask, boolean bNoFocus);
        
	// Remove a window from receiving accelerator notifications.
	public void revoke(JComponent hwnd);

//	// Register an AcceleratorListener to notifications when an accelerator keycode is detected. Notifications will be sent to listeners before any other windows receive a notification.
//	public void registerListener(IAcceleratorListener listener, ETList<String> pAccelerators);

//	// Remove an AcceleratorListener from receiving accelerator notifications.
//	public void revokeListener(IAcceleratorListener listener);

	// Execute the accelerator manager against the current keyboard state.  All keys in the down position will be evaluated.
	public boolean translateAccelerators(String keyCode);

//	// If the accelerator is handled then set this property. 
//	public void setHandled(boolean newVal);
	
//	// If the accelerator is handled then set this property. 
//	public boolean getHandled();
}
