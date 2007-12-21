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
 * This a non-internationalized exception used by the BundleHandler and
 * I18nException classes when internationalization fails. It should not be used
 * by classes outside of the org.netbeans.modules.iep.editor.tcg.exception package, which is why it
 * has package-level access.
 *
 * @author Bing Lu
 */
public class NonI18nException
    extends Exception {

    /**
     * Constructor for the MissingStringException object
     */
    public NonI18nException() {
        super("MissingStringException: Exception Undefined");
    }

    /**
     * Constructor for the MissingStringException object
     *
     * @param description A description of what is causing the exception to
     *        occur.
     */
    public NonI18nException(String description) {
        super(description);
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
