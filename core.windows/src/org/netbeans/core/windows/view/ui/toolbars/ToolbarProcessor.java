/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

