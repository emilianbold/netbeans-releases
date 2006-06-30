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
