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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.util.EventListener;

/**
 * Listen to server instances changes.
 *
 * @author Stepan Herold
 *
 * @since 1.6
 */
public interface InstanceListener extends EventListener {
        
        /**
         * Default server instance has been changed.
         *
         * @param oldServerInstanceID id of the old default server instance.
         * @param newServerInstanceID id of the new default server instance.
         */
        public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID);
        
        /**
         * Server instance has been added.
         *
         * @param serverInstanceID id of the server instance that has been added.
         */
        public void instanceAdded(String serverInstanceID);
        
        /**
         * Server instance has been removed.
         *
         * @param serverInstanceID id of the server instance that has been removed.
         */
        public void instanceRemoved(String serverInstanceID);
}
