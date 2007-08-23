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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.StringTokenizer;
import org.netbeans.api.project.Project;

public class ConfigurationSupport {
    public static String appendConfName(String oldConfs, Configuration newConf) {
	return oldConfs + "," + newConf.getDisplayName(); // NOI18N
    }

    public static String removeConfName(String oldConfs, Configuration oldConf) {
	StringBuilder newConfs = new StringBuilder();
	StringTokenizer st = new StringTokenizer(oldConfs, ","); // NOI18N
	while (st.hasMoreTokens()) {
	    // Strip "'s
	    String token = st.nextToken();
	    String displayName = token; //token.substring(1, token.length()-1);
	    if (displayName.equals(oldConf.getDisplayName()))
		continue;
	    if (newConfs.length() > 0)
		newConfs.append(","); // NOI18N
	    newConfs.append(displayName);
	}
	return newConfs.toString();
    }

    public static String renameConfName(String oldConfs, String oldDisplayName, String newDisplayName) {
	int i = oldConfs.indexOf(oldDisplayName);
	if (i < 0) {
	    // Error FIXUP; should be there!
	}
	String newConfs = oldConfs.substring(0, i) + newDisplayName + oldConfs.substring(i + oldDisplayName.length());
	return newConfs;
	
    }


    public static String makeNameLegal(String displayName) {
	StringBuilder tmp = new StringBuilder();
	for (int i = 0; i < displayName.length(); i++) {
	    if (i == 0 && (
                isLetterOrDigit(displayName.charAt(i)) ||
		displayName.charAt(i) == '_')) {
		tmp.append(displayName.charAt(i));
	    }
            else if (i != 0 &&
                (isLetterOrDigit(displayName.charAt(i)) ||
		displayName.charAt(i) == '_' ||
		displayName.charAt(i) == '-' ||
		displayName.charAt(i) == '.')) {
		tmp.append(displayName.charAt(i));
	    }
	    else {
		tmp.append("_"); // NOI18N
	    }
	}
	if (tmp.length() == 0) 
	    return "Configuration"; // NOI18N
	else
	    return tmp.toString();
    }
    
    private static boolean isLetterOrDigit(char ch) {
        if (ch < '0' || ch > 'z')
            return false;
        else
            return Character.isLetterOrDigit(ch);
    }

    public static String getNameFromDisplayName(String displayName) {
	/*
	StringBuffer tmp = new StringBuffer();
	for (int i = 0; i < displayName.length(); i++) {
	    if (!Character.isWhitespace(displayName.charAt(i)) &&
		displayName.charAt(i) != '(' &&
		displayName.charAt(i) != ')' &&
		displayName.charAt(i) != ',') {
		tmp.append(displayName.charAt(i));
	    }
	}
	return tmp.toString();
	*/
	return displayName; // FIXUP: are thay always identical????
    }

    // Unique names
    public static String getUniqueName(Configuration[] cs, String baseName) {
	int number = 1;
	String newDisplayName;
	while (true) {
	    if (number == 1)
		newDisplayName = baseName;
	    else
		newDisplayName = baseName + "-" + number; // NOI18N
	    if (isNameUnique(cs, newDisplayName))
		break;
	    number++;
	}
	return newDisplayName;
    }

    public static String getUniqueNewName(Configuration[] cs) {
	return getUniqueName(cs, "NewConfiguration"); // NOI18N
    }

    public static String getUniqueCopyName(Configuration[] cs, Configuration copy) {
	int number = 1;
	String newBaseName = "Copy"; // NOI18N
	String newName;
	while (true) {
	    if (number == 1)
		newName = newBaseName + "_of_" + copy.getName(); // NOI18N
	    else
		newName = newBaseName + "-" + number + "_of_" + copy.getName(); // NOI18N
	    if (isNameUnique(cs, newName))
		break;
	    number++;
	}
	return newName;
    }

    public static boolean isNameUnique(Configuration[] cs, String displayName) {
	boolean unique = true;
	String name = getNameFromDisplayName(displayName);
	for (int i = 0; i < cs.length; i++) {
	    if (cs[i].getName().equals(name)) {
		unique = false;
		break;
	    }
	}
	return unique;
    }

    // property values
    public static String getConfsPropertyValue(Configuration[] cs) {
	StringBuilder configurationProperty = new StringBuilder();
	for (int i = 0; i < cs.length; i++) {
	    if (configurationProperty.length() > 0)
		configurationProperty.append(","); // NOI18N
	    configurationProperty.append(cs[i].getDisplayName());
	}
	return configurationProperty.toString();
    }

    public static String getDefaultConfPropertyValue(Configuration conf) {
	return conf.getDisplayName();
    }

    public static ConfigurationDescriptor getProjectDescriptor(Project project) {
	ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
	return pdp.getConfigurationDescriptor();
    }
}
