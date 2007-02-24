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

import org.netbeans.modules.uml.core.eventframework.EventPayload;

/**
 * @author sumitabhk
 *
 */
public class EditEventPayload extends EventPayload implements IEditEventPayload
{
	private int m_KeyCode = 0;
	private boolean m_Modified = false;
	private String m_Text = "";

	/**
	 *
	 */
	public EditEventPayload()
	{
		super();
	}

	/**
	 * Description
	 *
	 * @param keycode[out]
	 *
	 */
	public int getKey()
	{
		return m_KeyCode;
	}

	/**
	 * Description
	 *
	 * @param keycode[in]
	 *
	 */
	public void setKey(int keycode)
	{
		m_KeyCode = keycode;
	}

	/**
	 * Description
	 *
	 * @param bModified[out]
	 *
	 */
	public boolean getModified()
	{
		return m_Modified;
	}

	/**
	 * Description
	 *
	 * @param bModified[in]
	 *
	 */
	public void setModified(boolean bModified)
	{
		m_Modified = bModified;
	}

	/**
	 * Description
	 *
	 * @param sText[out]
	 *
	 */
	public String getText()
	{
		return m_Text;
	}

	/**
	 * Description
	 *
	 * @param sText[in]
	 *
	 */
	public void setText(String text)
	{
		m_Text = text;
	}

}


