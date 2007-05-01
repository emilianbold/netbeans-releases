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
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.sql.framework.model.ValidationInfo;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ValidationInfoImpl implements ValidationInfo {

    private String desc;

    private int validationType;

    private Object valObj;

    /**
     * Creates a new instance of ValidationInfoImpl with the given String description and
     * information type and associated with the given (optional) Object.
     * 
     * @param obj Object that was validated, for reference to quickly access the object
     *        for editing; may be null if object is not directly editable
     * @param description description of the error or warning
     * @param vType One of {@link ValidationInfo#VALIDATION_ERROR} or
     *        {@link ValidationInfo#VALIDATION_WARNING}
     */
    public ValidationInfoImpl(Object obj, String description, int vType) {
        this.valObj = obj;
        this.desc = description;
        this.validationType = vType;
    }

    public String getDescription() {
        return desc;
    }

    public Object getValidatedObject() {
        return valObj;
    }

    public int getValidationType() {
        return validationType;
    }

}
