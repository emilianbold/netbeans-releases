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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;

/**
 * This class is used internally to provide default support for any layout
 * manager class. The layout manager is handled as a JavaBean, no component
 * constraints are supported, as well as no drag&drop and no arranging
 * features.
 *
 * @author Tomas Pavek
 */

class DefaultLayoutSupport extends AbstractLayoutSupport {

    private Class layoutClass;

    public DefaultLayoutSupport(Class layoutClass) {
        this.layoutClass = layoutClass;
    }

    public Class getSupportedClass() {
        return layoutClass;
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        // for better robustness catch exceptions that might occur because
        // the default support does not deal with constraints
        try {
            super.addComponentsToContainer(container,
                                           containerDelegate,
                                           components,
                                           index);
        }
        catch (RuntimeException ex) { // just ignore
            ex.printStackTrace();
        }
    }
}
