/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import java.util.*;


/**
 * CollabConfig Verifier
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabConfigVerifier extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////

    /* config version */
    public static final String DEFAULT_CONFIG_VERSION = "1.0"; //No I18n

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* instance */
    private String configVersion = null;

    /**
     * constructor
     *
     */
    public CollabConfigVerifier() {
        super();
        this.configVersion = DEFAULT_CONFIG_VERSION;
    }

    /**
     * constructor
     *
     */
    public CollabConfigVerifier(String configVersion) {
        super();
        this.configVersion = configVersion;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * verify
     *
     * @param normalizedEventIDMap
     * @param eventHandlerMap
     * @return        true if verification passed
     */
    public boolean verify(HashMap normalizedEventIDMap, HashMap eventHandlerMap) {
        String[] normalizedEventID = (String[]) normalizedEventIDMap.values().toArray(new String[0]);

        if (normalizedEventID.length != eventHandlerMap.size()) {
            return false;
        }

        for (int i = 0; i < normalizedEventID.length; i++) {
            if (!eventHandlerMap.containsKey(normalizedEventID[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * createUniqueNormalizedEventID
     *
     * @param configVersion
     * @param normalizedEventID
     * @return        unique normalized event ID
     */
    public String createUniqueNormalizedEventID(String configVersion, String normalizedEventID) {
        return normalizedEventID;
    }

    /**
     * setVersion
     *
     * @param version
     */
    public void setVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    /**
     * getVersion
     *
     * @return        version
     */
    public String getVersion() {
        return this.configVersion;
    }
}
