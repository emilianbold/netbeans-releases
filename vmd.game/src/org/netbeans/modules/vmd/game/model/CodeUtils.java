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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */package org.netbeans.modules.vmd.game.model;

/**
 *
 * @author karel herink
 */
public class CodeUtils {

	public static String capitalize(String str) {
		assert str != null;
        /* Fix for IZ#145512 - [65cat] AssertionError at
         * org.netbeans.modules.vmd.game.model.CodeUtils.capitalize
		 * assert str.length() > 0;
         */
        if ( str.length() == 0 ){
            return str;
        }
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0, 1).toUpperCase());
		sb.append(str.substring(1));
		return sb.toString();
	}

	public static String decapitalize(String str) {
		assert str != null;
		/*
                 * Fix for IZ#144199 - [65cat] AssertionError at org.netbeans.modules.vmd.game.model.CodeUtils.decapitalize
                 * assert str.length() > 0;
                 */ 
                if ( str.length() == 0 ){
                    return str;
                }
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0, 1).toLowerCase());
		sb.append(str.substring(1));
		return sb.toString();
	}

	public static String createGetterMethodName(String fieldName) {
		assert fieldName != null;
		assert fieldName.length() > 0;
		StringBuffer sb = new StringBuffer();
		sb.append("get"); // NOI18N
		sb.append(capitalize(fieldName));
		return sb.toString();
	}
	
	public static String createSetterMethodName(String fieldName) {
		assert fieldName != null;
		assert fieldName.length() > 0;
		StringBuffer sb = new StringBuffer();
		sb.append("set"); // NOI18N
		sb.append(capitalize(fieldName));
		return sb.toString();
	}

	public static String getIdealImageName(String imagePath) {
		int index = imagePath.lastIndexOf("/");
		String str = imagePath.substring(index + 1);
		str = str.substring(0, str.lastIndexOf("."));

		StringBuilder idealName = new StringBuilder(str);
		if (!Character.isJavaIdentifierStart(idealName.charAt(0))) {
			idealName.setCharAt(0, '_');
		}
		if (idealName.length() == 1) {
			return idealName.toString();
		}
		for (int i = 1; i < idealName.length(); i++) {
			int curChar = idealName.charAt(i);
			if (!Character.isJavaIdentifierPart(curChar)) {
				idealName.setCharAt(i, '_');
			}
		}
		return idealName.toString();
	}
		
}
