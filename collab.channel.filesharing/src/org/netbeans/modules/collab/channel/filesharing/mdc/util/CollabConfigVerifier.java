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
