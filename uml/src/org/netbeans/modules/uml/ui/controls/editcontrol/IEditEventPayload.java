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


package org.netbeans.modules.uml.ui.controls.editcontrol;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;

public interface IEditEventPayload extends IEventPayload
{
	/**
	 * The last keycode prior to this event
	*/
	public int getKey();

	/**
	 * The last keycode prior to this event
	*/
	public void setKey( int value );

	/**
	 * TRUE if the edit control has modified data
	*/
	public boolean getModified();

	/**
	 * TRUE if the edit control has modified data
	*/
	public void setModified( boolean value );

	/**
	 * The edit control window text
	*/
	public String getText();

	/**
	 * The edit control window text
	*/
	public void setText( String value );

}
