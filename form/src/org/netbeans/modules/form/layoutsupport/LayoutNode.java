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
import java.util.*;
import java.beans.Customizer;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.actions.*;

/**
 * @author Tomas Pavek
 */

public class LayoutNode extends FormNode implements FormLayoutCookie
{
    private LayoutSupportManager layoutSupport;
    
    public LayoutNode(RADVisualContainer cont) {
        super(Children.LEAF, cont.getFormModel());
        layoutSupport = cont.getLayoutSupport();
        setName(layoutSupport.getDisplayName());
        cont.setLayoutNodeReference(this);
    }

    public LayoutNode getLayoutNode() {
        return this;
    }

    public RADVisualContainer getMetaContainer() {
        return layoutSupport.getMetaContainer();
    }

    public void fireLayoutPropertiesChange() {
        firePropertyChange(null, null, null);
    }

    public void fireLayoutPropertySetsChange() {
        firePropertySetsChange(null, null);
    }

    public Image getIcon(int iconType) {
        return layoutSupport.getIcon(iconType);
    }

    public Node.PropertySet[] getPropertySets() {
        return layoutSupport.getPropertySets();
    }

    public boolean hasCustomizer() {
        if (layoutSupport.getMetaContainer().isReadOnly()
               || layoutSupport.getCustomizerClass() == null)
            return false;

        RADVisualContainer container = layoutSupport.getMetaContainer();
        FormDesigner designer = getFormModel().getFormDesigner();
        return designer.isInDesignedTree(container);
    }

    public Component getCustomizer() {
        Class customizerClass = layoutSupport.getCustomizerClass();
        if (customizerClass == null)
            return null;

        Object customizer;
        try {
            customizer = customizerClass.newInstance();
        }
        catch (InstantiationException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
        if (customizer instanceof Component 
                && customizer instanceof Customizer)
        {
            ((java.beans.Customizer)customizer).setObject(
                                            layoutSupport.getLayoutDelegate());
            return (Component) customizer;
        }
        else return null;
    }

    protected SystemAction[] createActions() {
        ArrayList actions = new ArrayList(10);

        if (!layoutSupport.getMetaContainer().isReadOnly()) {
            actions.add(SystemAction.get(SelectLayoutAction.class));
            actions.add(null);
        }

        SystemAction[] superActions = super.createActions();
        for (int i=0; i < superActions.length; i++)
            actions.add(superActions[i]);

        SystemAction[] array = new SystemAction[actions.size()];
        actions.toArray(array);
        return array;
    }

/*    public HelpCtx getHelpCtx() {
        Class layoutClass = layoutSupport.getLayoutClass();
        String helpID = null;
        if (layoutClass != null) {
            if (layoutClass == BorderLayout.class)
                helpID = "gui.layouts.managers.border"; // NOI18N
            else if (layoutClass == FlowLayout.class)
                helpID = "gui.layouts.managers.flow"; // NOI18N
            else if (layoutClass == GridLayout.class)
                helpID = "gui.layouts.managers.grid"; // NOI18N
            else if (layoutClass == GridBagLayout.class)
                helpID = "gui.layouts.managers.gridbag"; // NOI18N
            else if (layoutClass == CardLayout.class)
                helpID = "gui.layouts.managers.card"; // NOI18N
            else if (layoutClass == javax.swing.BoxLayout.class)
                helpID = "gui.layouts.managers.box"; // NOI18N
            else if (layoutClass == org.netbeans.lib.awtextra.AbsoluteLayout.class)
                helpID = "gui.layouts.managers.absolute"; // NOI18N
        }
        if (helpID != null)
            return new HelpCtx(helpID);
        return super.getHelpCtx();
    } */
}
