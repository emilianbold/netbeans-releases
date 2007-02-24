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
 * Created on Jun 11, 2003
 *
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 *
 */
public interface IDrawingPropertyProvider
{
	public ETList<IDrawingProperty> getDrawingProperties();
	public void saveColor(String sDrawEngineType, String sResourceName, int nColor);
	public void saveColor2(IColorProperty pProperty);
	public void saveFont(  String sDrawEngineName,
						   String sResourceName,
						   String sFaceName,
						   int nHeight,
						   int nWeight,
						   boolean bItalic,
						   int nColor);
	public void saveFont2(IFontProperty pProperty);
	public void resetToDefaultResource( String sDrawEngineName, 
										String sResourceName,
										String sResourceType);
	public void resetToDefaultResources();
	public void resetToDefaultResources2(String sDrawEngineName);
	public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile);
	public boolean displayFontDialog(IFontProperty pProperty);
	public boolean displayColorDialog(IColorProperty pProperty);
	public void invalidateProvider();
}


