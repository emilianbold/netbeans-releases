/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


