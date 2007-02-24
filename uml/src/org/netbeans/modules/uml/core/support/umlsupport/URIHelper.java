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

package org.netbeans.modules.uml.core.support.umlsupport;

public class URIHelper //implements IURIHelper
{

  public URIHelper() {
  }
  public String retrieveRawID(String uri) {
    String str = URILocator.retrieveRawID(uri);
    return str;
  }
  public static String translateString(String inStr) {
  	String outStr = "";
  	if (inStr != null && inStr.length() > 0)
  	{
  		outStr = inStr;
		// currently we are only having to check strings that have &#58; in them
		// which represents the ":"
		// we couldn't do this in the xslt because the translate function was messing
		// up when hitting the 5 or the 8
		if (inStr.indexOf("&#58;") > 0)
		{
			outStr = StringUtilities.replaceAllSubstrings(inStr, "&#58;", ":");
		}
		else if (inStr.indexOf("\n") > 0)
		{
			outStr = StringUtilities.replaceAllSubstrings(inStr, "\n", "<br/>");
		}
  	}
	return outStr;
  }
  
}