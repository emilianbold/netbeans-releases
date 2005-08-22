/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
