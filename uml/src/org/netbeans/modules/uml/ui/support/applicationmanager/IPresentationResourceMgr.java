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


/*
 * Created on Feb 2, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.drawingproperties.ETFontType;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;

/**
 * @author jingmingm
 *
 */
public interface IPresentationResourceMgr
{
	
	public boolean validateFiles();
	public String getSampleDiagramFilename(int nKind);
	public ETList<String> getStandardDrawEngines(int nKind);
	public ETPairT<String, String> getUpgradeString(String sOldName);
	public ETFontType getFont();
	public int getColor();
	public ETFontType getFontResource(String sDrawEngineName, String sResourceName);
	public int getColorResource(String sDrawEngineName, String sResourceName);
	public ETFontType getDefaultFontResource(String sDrawEngineName, String sResourceName);
	public int getDefaultColorResource(String sDrawEngineName, String sResourceName);
	public ETFontType getOverriddenFontResource(String sDrawEngineName, String sResourceName);
	public int getOverriddenColorResource(String sDrawEngineName, String sResourceName);
	public void saveOverriddenResources();
	public void removeOverriddenFontResource(String sDrawEngineName, String sResourceName);
	public void removeOverriddenColorResource(String sDrawEngineName, String sResourceName);
	public void saveOverriddenFontResource( String sDrawEngineName, 
											String sResourceName,
											ETFontType pETFontType);
	public void saveOverriddenColorResource(String sDrawEngineName,
											String sResourceName,
											int nColor);

	public ETList<String> getAllDrawEngineNames();
	public ETList<String> getAllResourceNames(String sDrawEngineName);
	public ETList<IDrawingProperty> getAllDrawingProperties(String sDrawEngineName);
	public String getResourceType(String sDrawEngineName, String sResourceName);
	public boolean isAdvanced(String sDrawEngineName, String sResourceName);
	public IDrawingProperty	getDrawingProperty(String sDrawEngineName, String sResourceName);
	public ETPairT<String, String> getDisplayName(String sDrawEngineName, String sResourceName);
	public ETTripleT<String, String , Integer> getDrawEngineDisplayDetails(String sDrawEngineName);
}



