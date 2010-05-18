/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
