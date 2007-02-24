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



package org.netbeans.modules.uml.ui.support.wizard;

import java.awt.GraphicsConfiguration;

import javax.swing.Icon;

public interface IWizardSheet {

	public static final int PSWIZB_BACK = 0;
	public static final int PSWIZB_NEXT = 1;
	public static final int PSWIZB_CANCEL = 2;
	public static final int PSWIZB_FINISH = 3;

	public Icon getBmpHeader();

	public Icon getBmpWatermark();
	
	public void setTitle(String newValue);
	
	public int getActiveIndex();
	
	public int getPageCount();
			
	public void setActivePage(int pIndex);

	public void onPageChange();
	
	public int doModal();
	
	public void setCursor(int newValue);

	public void init(Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader);
	
	public void setButtonEnabled(int button, boolean enabled);
	
}
