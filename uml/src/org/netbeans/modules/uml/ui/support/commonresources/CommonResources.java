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

package org.netbeans.modules.uml.ui.support.commonresources;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

public class CommonResources
{
	public static void setMnemonic(Object obj, String text)
	{
		if (obj != null && text != null && text.length() > 0)
		{	
			int pos = text.indexOf('&');
			if (pos > -1)
			{
				String under = text.substring(pos + 1, pos + 2);
				if (under != null && under.length() > 0)
				{
					if (obj instanceof JLabel)
					{
						JLabel lab = (JLabel)obj;
						lab.setDisplayedMnemonic(under.charAt(0));
					}
					else if (obj instanceof JCheckBox)
					{
						JCheckBox box = (JCheckBox)obj;
						box.setMnemonic(under.charAt(0));
					}
					else if (obj instanceof JRadioButton)
					{
						JRadioButton button = (JRadioButton)obj;
						button.setMnemonic(under.charAt(0));
					}
					else if (obj instanceof JButton)
					{
						JButton button = (JButton)obj;
						button.setMnemonic(under.charAt(0));
					}
				}
			}
		}
	}
	
	public static String determineText(String text)
	{
		String retStr = text;
		if (text != null && text.length() > 0)
		{	
			retStr = StringUtilities.replaceAllSubstrings(text, "&", "");
		}
		return retStr;
	}
	
	public static void setFocusAccelerator(Object obj, String text)
	{
		if (obj != null && text != null && text.length() > 0)
		{	
			int pos = text.indexOf('&');
			if (pos > -1)
			{
				String under = text.substring(pos + 1, pos + 2);
				if (under != null && under.length() > 0)
				{
					if (obj instanceof JComboBox)
					{
						JComboBox combo = (JComboBox)obj;
						((JTextField)combo.getEditor().getEditorComponent()).setFocusAccelerator(under.charAt(0));
					}
				}
			}
		}
	}
	
}
