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
 * TcgComponentValidationMsg.java
 *
 * Created on September 12, 2005, 3:03 PM
 *
 * @author Bing Lu
 */
public class TcgComponentValidationMsg {
    private String mType; // VALIDATION_OK_KEY, VALIDATION_ERROR_KEY, VALIDATION_WARNING_KEY

    private String mText;
    
    /** Creates a new instance of TcgComponentValidationMsg */
    public TcgComponentValidationMsg(String type, String text) {
        mType = type;
        mText = text;
    }
    
    public String getType() {
        return mType;
    }
    
    public String getText() {
        return mText;
    }
}
