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
package org.netbeans.modules.vmd.api.properties;

import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 * This class is used for resolving DesignEventFilter for a PropertiesPresenter.
 *
 * @author David Kaspar
 */
public abstract class DesignEventFilterResolver {

    /**
     * The DEFResolver for a related component only.
     */
    public static final DesignEventFilterResolver THIS_COMPONENT = new DesignEventFilterResolver() {
        public DesignEventFilter getEventFilter (DesignComponent component) {
            return new DesignEventFilter ().addComponentFilter (component, false);
        }
    };

    /**
     * The DEFResolver for a related component and its parent only.
     */
    public static final DesignEventFilterResolver THIS_COMPONENT_AND_PARENT = new DesignEventFilterResolver() {
        public DesignEventFilter getEventFilter (DesignComponent component) {
            return new DesignEventFilter ().addParentFilter (component, 1, false);
        }
    };

    /**
     * Returns a DesignEventFilter for a specified component.
     * @param component the component
     * @return the event filter
     */
    public abstract DesignEventFilter getEventFilter (DesignComponent component);

}
