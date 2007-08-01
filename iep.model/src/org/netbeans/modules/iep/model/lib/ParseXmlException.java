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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.model.lib;

/**
 * For use to throw internationalized XML related exception
 *
 * @author Bing Lu
 */
public class ParseXmlException
    extends I18nException {

	public ParseXmlException(String message) {
		super(message);
	}
	
	public ParseXmlException(String message, Exception ex) {
		super(message, ex);
	}
	
    /**
     * Constructor for the ParseXmlException object
     *
     * @param keyName The internationalization key to look up the error
     *        template.
     * @param bundleName The bundle where the error template resides.
     * @param params Arguments passed to fill in parameters in the template.
     */
    public ParseXmlException(String keyName, String bundleName,
                                Object[] params) {

        // Call the super class.
        super(keyName, bundleName, params);
    }

    /**
     * Constructor for the I18nException object
     *
     * @param keyName The internationalization key.
     * @param bundleName The internationalizaiton bundle.
     * @param params Bits of information about what went wrong.
     * @param t The exception we wish to embed.
     */
    public ParseXmlException(String keyName, String bundleName,
                                Object[] params, Throwable t) {
        super(keyName, bundleName, params, t);
    }

    /**
     * Convenience constructor for the I18nException object. Used when a method
     * catches one kind of I18nException and needs to throw a different kind
     * due to the throws clause in its contract. This constructor should be
     * used sparingly -- only when there is no useful additional information
     * that can be provided by supplying a list of arguments.
     *
     * @param original The original exception being caught, nested and
     *        rethrown.
     */
    public ParseXmlException(I18nException original) {
        super(original);
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
