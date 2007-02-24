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



package org.netbeans.modules.uml.ui.swing.drawingarea.cursors;

import java.awt.Cursor;


/**
 * @author KevinM
 *
 */
public class NoDropCursor  extends ETCustomCursor{

	protected String getCursorName() {
		return "NoDropCursor";
	}
	
	public static Cursor getCursor() {
		if (m_customCursor == null)
		{
			ETCustomCursor instance = new NoDropCursor();
			m_customCursor = instance.createCursor();
		}
		return m_customCursor;
	}
	protected static Cursor m_customCursor = null;
}
