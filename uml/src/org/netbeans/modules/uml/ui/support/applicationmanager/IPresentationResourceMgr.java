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



