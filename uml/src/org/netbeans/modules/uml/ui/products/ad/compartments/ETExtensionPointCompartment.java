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
 * Created on Dec 3, 2003
 *
 */
package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Point;

import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;

/**
 * @author jingmingm
 *
 */
public class ETExtensionPointCompartment extends ETNameCompartment implements IADExtensionPointCompartment
{
	public ETExtensionPointCompartment()
	{
		super();
		this.init();
	}

	public ETExtensionPointCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.setFontString("Arial-bold-12");
		this.InitResources();
	}

	public void InitResources() {
		this.setName(PreferenceAccessor.instance().getDefaultElementName());
	}
	
	public String getCompartmentID()
	{
		return "ADExtensionPointCompartment";
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		String oldName = getName();
		if (oldName == null || oldName.length() == 0)
		{
			setName(" ");
		}
		IETSize retVal=super.calculateOptimumSize(pDrawInfo, bAt100Pct);
		setName(oldName);
		return retVal;
	}
	
	public long editCompartment(boolean bNew, int nKeyCode, int nShift, int nPos)
	{
		if (m_boundingRect.getIntWidth() == 0)
		{
			IDrawEngine pDrawEngine = this.getEngine();
			ICompartment comp = (ICompartment)((ETNodeDrawEngine)pDrawEngine).getCompartmentByKind(IADExtensionPointListCompartment.class);
			m_boundingRect = comp.getBoundingRect();
			if (m_boundingRect.getIntHeight() == 0)
			{
				m_boundingRect.setBottom(m_boundingRect.getTop() - 16);
			}
		}

		return super.editCompartment(bNew, nKeyCode, nShift, nPos);
	}
}



