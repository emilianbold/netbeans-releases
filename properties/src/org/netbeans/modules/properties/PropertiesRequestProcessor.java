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

package org.netbeans.modules.properties;

import org.openide.util.RequestProcessor;

/**
 *
 * Provides an {@link org.openide.util.RequestProcessor} instance for the Properties module.
 *
 * @author Tomas Stupka
 */
public class PropertiesRequestProcessor {

     /** the static {@link org.openide.util.RequestProcessor} instance */
    private final static RequestProcessor requestProcessor =
        new RequestProcessor ("org.netbeans.modules.properties.PropertiesRequestProcessor");

    /** Creates a new instance of PropertiesResourceProcessor */
    public PropertiesRequestProcessor() {        
    }
    
    /**
     * The getter for the shared instance of the {@link org.openide.util.RequestProcessor}
     *
     * @return a shared {@link org.openide.util.RequestProcessor} instance with throughput 1
     */
    public static RequestProcessor getInstance() {
        return requestProcessor;
    }
    
}
