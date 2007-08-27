/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.game.model;

/**
 *
 * @author karel herink
 */
public class CodeUtils {

	public static String capitalize(String str) {
		assert str != null;
		assert str.length() > 0;
		StringBuffer sb = new StringBuffer();
		sb.append(str.substring(0, 1).toUpperCase());
		sb.append(str.substring(1));
		return sb.toString();
	}

	public static String decapitalize(String str) {
		assert str != null;
		assert str.length() > 0;
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
