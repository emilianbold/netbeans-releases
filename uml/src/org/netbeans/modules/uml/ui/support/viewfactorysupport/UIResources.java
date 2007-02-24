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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.*;

/**
 * @author josephg
 *
 */
public class UIResources {
	public UIResources getParentResources() {
		return m_parentResources;
	}
	
	public void setParentResources(UIResources resources) {
		m_parentResources = resources;
	}
	
	public void setResourceID(int resourceKind){
		setResourceID(resourceKind,new String());
	}
	
	public void setResourceID(int resourceKind, String id) {
		// any existing element with that key will be overwritten		
		m_ResourceIDs.put(new Integer(resourceKind),id);
	}
	
	public String getResourceID(int resourceKind){ 
		return getResourceID(resourceKind,true);
	}

	public void setResourceToDefaultID(int resourceID)
	{
	}
	
	public String getResourceID(int resourceKind, boolean defaultToParent) {
		String returnString = (String)m_ResourceIDs.get(new Integer(resourceKind));
		if(returnString != null && returnString.length() != 0)
			return returnString;
			
		if(defaultToParent && returnString != null)
		{
			UIResources parent = getParentResources();
			if(parent != null)
			{
				returnString = parent.getResourceID(resourceKind,true);
			}
		}
		if(returnString != null)
			return returnString;

		return new String();
	}

	public String getDefaultResourceID(int resourceKind) {
		String returnString = (String)m_DefaultResourceIDs.get(new Integer(resourceKind));
		
		if(returnString != null)
			return returnString;
			
		return new String();
	}
	public void getColor(String index) {
		 
	}
	
	// ResourceIDKind
	public static final int CK_FIRSTCOLOR = 1;
	public static final int CK_TEXTCOLOR = CK_FIRSTCOLOR;
	public static final int CK_BACKCOLOR = 2;
	public static final int CK_BORDERCOLOR = 3;
	public static final int CK_FILLCOLOR = 4;
	public static final int CK_LASTCOLOR = 5;
	public static final int CK_FIRSTFONT = 6;
	public static final int CK_FONT = CK_FIRSTFONT;
	public static final int CK_TITLEFONT = 7;
	public static final int CK_LASTFONT = 8;
	
	private HashMap m_ResourceIDs = new HashMap();
	private HashMap m_DefaultResourceIDs = new HashMap();
	private UIResources m_parentResources = null;
}


