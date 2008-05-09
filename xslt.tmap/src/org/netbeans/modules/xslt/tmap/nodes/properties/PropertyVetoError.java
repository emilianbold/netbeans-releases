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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import org.openide.ErrorManager;

/**
 * This throwable is kind of workaround to prevent closing
 * a property customizer dialog in case of an exception
 * happens while setting property value.
 *
 * The throwable is declared ad extended the Error to prevent catching it 
 * by the property sheet framework. 
 *
 * @author Vitaly Bychkov
 * @author nk160297
 * 
 */
public class PropertyVetoError extends Error {
    
    public PropertyVetoError() {
        super();
    }
    
    public PropertyVetoError(String message) {
        super(message);
    }
    
    public PropertyVetoError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PropertyVetoError(Throwable cause) {
        super(cause);
    }
    
    public static void defaultProcessing(PropertyVetoError ex) {
        Throwable cause = ex.getCause();
        
            ErrorManager.getDefault().notify(cause);
    }
    
}
