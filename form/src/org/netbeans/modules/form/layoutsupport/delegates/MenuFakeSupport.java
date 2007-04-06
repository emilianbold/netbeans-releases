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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.Component;
import java.awt.Container;
import org.netbeans.modules.form.codestructure.CodeGroup;
import org.netbeans.modules.form.layoutsupport.AbstractLayoutSupport;

/**
 * Fake layout support for Swing menus allowing to treat menu components as
 * visual components/containers and generate correct code. Does not support dragging.
 */
public class MenuFakeSupport extends AbstractLayoutSupport {

    public Class getSupportedClass() {
        return null;
    }

    public boolean isDedicated() {
        return true;
    }

    public boolean shouldHaveNode() {
        return false;
    }

    protected void readLayoutCode(CodeGroup layoutCode) {
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        for (int i=0; i < components.length; i++) {
            containerDelegate.add(components[i], i + index);
        }
    }
}
