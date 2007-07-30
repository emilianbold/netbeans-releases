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

import java.util.List;

/**
 * TcgComponentValidationReport.java
 *
 * Created on September 9, 2005, 2:40 PM
 *
 * @author Bing Lu
 */
public class TcgComponentValidationReport implements TcgModelConstants {
    private TcgComponent mComponent;
    private String mType;
    private List mChildReportList;
    private List mMessageList;
    
    /** Creates a new instance of TcgComponentValidationReport */
    public TcgComponentValidationReport(TcgComponent component, String type, List messageList, List childReportList) {
        mComponent = component;
        mType = type;
        mMessageList = messageList;
        mChildReportList = childReportList;
    }
    
    public List getMessageList() {
        return mMessageList;
    }
    
    public List getChildReportList() {
        return mChildReportList;
    }

    public String getType() {
        return mType;
    }
    
    public boolean isOK() {
        return VALIDATION_OK_KEY.equals(mType);
    }
    
    public boolean hasError() {
        return VALIDATION_ERROR_KEY.equals(mType);
    }
    
    public boolean hasWarning() {
        return VALIDATION_WARNING_KEY.equals(mType);
    }
}
