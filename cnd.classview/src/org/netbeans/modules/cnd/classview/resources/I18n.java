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

package org.netbeans.modules.cnd.classview.resources;

import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public class I18n {
	
	public static String getMessage(String key) {
		return NbBundle.getMessage(I18n.class, key);
	}

	public static String getMessage(String key, Object param1) {
		return NbBundle.getMessage(I18n.class, key, param1);
	}
	
	public static String getMessage(String key, Object param1, Object param2) {
		return NbBundle.getMessage(I18n.class, key, param1, param2);
	}
	
	public static String getMessage(String key, Object param1, Object param2, Object param3) {
		return NbBundle.getMessage(I18n.class, key, param1, param2,  param3);
	}
	
	public static String getMessage(String key, Object[] params) {
		return NbBundle.getMessage(I18n.class, key, params);
	}
	
}
