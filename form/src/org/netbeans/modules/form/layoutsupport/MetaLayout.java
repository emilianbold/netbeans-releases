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
import java.beans.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/**
 * Meta component representing a LayoutManager instance as a JavaBean.
 *
 * @author Tomas Pavek
 */

class MetaLayout extends RADComponent {

    private AbstractLayoutSupport abstLayoutDelegate;

    public MetaLayout(AbstractLayoutSupport layoutDelegate,
                      LayoutManager lmInstance)
    {
        super();

        abstLayoutDelegate = layoutDelegate;

        initialize(((LayoutSupportManager)abstLayoutDelegate.getLayoutContext())
                         .getMetaContainer().getFormModel());

        setBeanInstance(lmInstance);
    }

    protected void createCodeExpression() {
        // code expression is handled by the layout support class
    }

    protected void createPropertySets(java.util.List propSets) {
        super.createPropertySets(propSets);

        // RADComponent provides also Code Generation properties for which
        // we have no use here (yet) - so we remove them now
        for (int i=0, n=propSets.size(); i < n; i++) {
            Node.PropertySet propSet = (Node.PropertySet)propSets.get(i);
            if (!"properties".equals(propSet.getName()) // NOI18N
                    && !"properties2".equals(propSet.getName())) { // NOI18N
                propSets.remove(i);
                i--;  n--;
            }
        }
    }

    protected PropertyChangeListener createPropertyListener() {
        // cannot reuse RADComponent.PropertyListener, because this is not
        // a regular RADComponent (properties have a special meaning)
        return null;
    }
}
