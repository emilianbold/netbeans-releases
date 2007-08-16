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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.wsitconf.api;

import java.util.logging.Logger;
import org.netbeans.modules.websvc.wsitconf.*;
import org.netbeans.modules.websvc.wsitconf.util.Util;

/**
 *
 * @author Martin Grebac
 */
public final class DevDefaultsProvider extends Object {

    private static final Logger logger = Logger.getLogger(DevDefaultsProvider.class.getName());

    private static DevDefaultsProvider instance;
  
    private DevDefaultsProvider() { }

    public static synchronized DevDefaultsProvider getDefault() {
        if (instance == null) {
            instance = new DevDefaultsProvider();
        }
        return instance;
    }

     /**
      * Sets up the server environment by creating proper keystores if required, and 
      * filling proper keys into them
      */
    public synchronized final void fillDefaultsToDefaultServer() {
        Util.fillDefaultsToDefaultServer();
    }
    
}
