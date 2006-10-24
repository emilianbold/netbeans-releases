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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.codegen;

/**
 * Golden file.
 *
 * @author  Pavel Flaska
 */
public class RenameTestField {

    // field which will be renamed
    String strToBeRenamed;

    /** Creates a new instance of RenameTestPass */
    public RenameTestField() {
        strToBeRenamed = new String("NetBeers");
    }

    public void differentOccurencies() {
        // occurence in 'for cycle'
        for (int i = 0; i < strToBeRenamed.length(); i++) {
            // do nothing.
        }
        strToBeRenamed += " is not a trademark.";
        String nuovo = strToBeRenamed;
        StringBuffer buf = new StringBuffer(strToBeRenamed);
        strToBeRenamed = "Do not change!";
        getLength(strToBeRenamed);
    }
    
    public int getLength(String s) {
        return s.length();
    }
}
