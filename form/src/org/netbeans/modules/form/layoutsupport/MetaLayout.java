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
import java.beans.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/**
 * Meta component representing a LayoutManager instance.
 *
 * @author Tomas Pavek
 */

class MetaLayout extends RADComponent {

    private AbstractLayoutSupport abstLayoutDelegate;

    public MetaLayout(AbstractLayoutSupport layoutDelegate,
                      LayoutManager lmInstance,
                      boolean defaultInstance)
    {
        super();

        abstLayoutDelegate = layoutDelegate;

        initialize(((LayoutSupportManager)abstLayoutDelegate.getLayoutContext())
                         .getMetaContainer().getFormModel());

        if (defaultInstance)
            setBeanInstance(lmInstance);
        else
            setInstance(lmInstance);
    }

    protected void createCodeExpression() {
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
