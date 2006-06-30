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

    /** Cloning method - creates a new instance of this layout support, just
     * not initialized yet.
     * @return new instance of this layout support
     */
    protected AbstractLayoutSupport createLayoutSupportInstance() {
        return new DefaultLayoutSupport(layoutClass);
    }
}
