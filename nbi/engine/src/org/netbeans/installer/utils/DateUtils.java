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
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class DateUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static DateUtils instance;
    
    public static synchronized DateUtils getInstance() {
        if (instance == null) {
            instance = new GenericDateUtils();
        }
        
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract String getTimestamp();
    
    public abstract String getFormattedTimestamp();
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class GenericDateUtils extends DateUtils {
        private static final DateFormat TIMESTAMP_FORMATTER =
                new SimpleDateFormat("yyyyMMddHHmmss");
        
        private static final DateFormat FORMATTED_TIMESTAMP_FORMATTER =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
        public String getTimestamp() {
            return TIMESTAMP_FORMATTER.format(new Date());
        }
        
        public String getFormattedTimestamp() {
            return FORMATTED_TIMESTAMP_FORMATTER.format(new Date());
        }
    }
}