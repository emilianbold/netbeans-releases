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
