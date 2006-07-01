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

package org.netbeans.core.windows.view.ui.toolbars;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;
import org.openide.util.WeakListeners;

import java.io.IOException;

/**
 * ToolbarProcessor is cookie of XMLDataObject which creates ToolbarConfiguration.
 *
 * @author Libor Kramolis
 */
public class ToolbarProcessor implements XMLDataObject.Processor, InstanceCookie {
    /** XML data object. */
    protected XMLDataObject xmlDataObject;
    /** created configuration */
    private ToolbarConfiguration configuration;

    /** Attach XML data object to processor. */
    public void attachTo (XMLDataObject o) {
        xmlDataObject = o;
    }

    /**
     * The bean name for the instance.
     */
    public String instanceName () {
        return instanceClass().getName();
    }

    /**
     * The representation type that may be created as instances.
     */
    public Class instanceClass () {
        return ToolbarConfiguration.class;
    }

    /**
     * Create an instance of ToolbarConfiguration.
     */
    public Object instanceCreate () throws IOException {
        if (configuration != null) {
            return configuration;
        }
        ToolbarConfiguration tc = new ToolbarConfiguration (xmlDataObject);
        xmlDataObject.addPropertyChangeListener(WeakListeners.propertyChange (
                                                    tc, xmlDataObject
                                                    ));
        configuration = tc;
        return tc;
    }
}

