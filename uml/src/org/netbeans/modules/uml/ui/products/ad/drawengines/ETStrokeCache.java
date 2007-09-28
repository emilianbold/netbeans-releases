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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;

/*
 * @author KevinM
 *
 */
public class ETStrokeCache {

	/**
	 *
	 */
	public ETStrokeCache() {
		super();
	}

	/*
	 * Accessor to the cached objects.
	 */
	public Stroke getStroke(int nLineKind, float width) {
		Object key = getKey(nLineKind, width);
		Stroke pen = (Stroke) m_cache.get(key);
		if (pen == null) {
			pen = createNewStroke(nLineKind, width);
			m_cache.put(key, pen);
		}

		return pen;
	}

	/*
	 * Stoke factory function.
	 */
	protected Stroke createNewStroke(int nLineKind, float width) {
		switch (nLineKind) {
			case DrawEngineLineKindEnum.DELK_DASH :
				{
					float dash1[] = { 10.0f };
					return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
				}

			case DrawEngineLineKindEnum.DELK_COUNT :
				{
					float[] dash2 = { 6.0f, 4.0f, 2.0f, 4.0f, 2.0f, 4.0f };
					return new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash2, 0.0f);
				}

			case DrawEngineLineKindEnum.DELK_DOT :
				{
					float[] pattern = { 4.0f, 4.0f };
					return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, pattern, 0.0f);
				}

			case DrawEngineLineKindEnum.DELK_HATCHED:
			{
				// I'm not sure the pattern I have to play with it.
				float[] pattern = { 4.0f, 4.0f };
				return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, pattern, 0.0f);
			}
			
			// Just a solid pen
			default :
			case DrawEngineLineKindEnum.DELK_SOLID :
			case DrawEngineLineKindEnum.DELK_UNKNOWN :
				return new BasicStroke(width);
		}
	}

	/* 
	 * Converts the input params to an object which becomes the hash key.
	 */
	protected Object getKey(int nLineKind, float width) {
		String key = new String("Kind=");
		key = key.concat(Integer.toString(nLineKind));
		key = key.concat("Width=");
		key = key.concat(Float.toString(width));
		return key;
	}

	protected HashMap m_cache = new HashMap();
}
