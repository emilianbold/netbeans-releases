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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.util.EventObject;
import java.util.Map;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */
public class CanvasModelUpdateEvent
     extends EventObject {

    /**
     * The data model to be loaded
     */
    protected Map mDataMap;


    /**
     * Constructor for the CanvasModelUpdateEvent object
     *
     * @param source   Description of the Parameter
     * @param dataMap  Description of the Parameter
     */
    public CanvasModelUpdateEvent(Object source, Map dataMap) {
        super(source);
        mDataMap = dataMap;
    }

    /**
     * Gets the value attribute of the CanvasModelUpdateEvent object
     *
     * @param key  Description of the Parameter
     * @return     The value value
     */
    public Object getValue(String key) {
        return mDataMap.get(key);
    }
}
