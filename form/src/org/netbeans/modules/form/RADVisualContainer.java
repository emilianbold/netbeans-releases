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

/* $Id$ */

package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;
import java.util.ArrayList;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;


public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
    private ArrayList subComponents = new ArrayList(10);
    private LayoutSupport layoutSupport;
    private LayoutNode layoutNode;

//    public void setComponent(Class beanClass) {
//        super.setComponent(beanClass);
//        initLayoutSupport();
//    }

    public void setInstance(Object beanInstance) {
        super.setInstance(beanInstance);
        initLayoutSupport();
    }

    public void initLayoutSupport() {
        // first try to find special support dedicated to container type
        Class layoutSupportClass =
            LayoutSupportRegistry.getLayoutSupportForContainer(getBeanClass());

        if (layoutSupportClass == null) {
            // try to find support for LayoutManager used by the container
            LayoutManager lm = getContainerDelegate(getBeanInstance()).getLayout();
            if (lm != null)
                layoutSupportClass =
                    LayoutSupportRegistry.getLayoutSupportForLayout(lm.getClass());
        }

        try {
            LayoutSupport laySup = layoutSupportClass == null ? null :
                LayoutSupportRegistry.createLayoutSupport(layoutSupportClass);
            setLayoutSupport(laySup);
        }
        catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace();
        }
    }

    public String getContainerDelegateGetterName() {
        Object value = getBeanInfo().getBeanDescriptor()
            .getValue("containerDelegate"); // NOI18N
        
        if (value != null && value instanceof String)
            return (String) value;
        else
            return null;
    }

    public String getJavaContainerDelegateString() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return getName() + "." + delegateGetter + "()"; // NOI18N
        }
        else
            return getName();
    }
    
    /**
     * @return The JavaBean visual container represented by this
     * RADVisualComponent
     */
    
    public Container getContainerDelegate(Object container) {
        // XXX is it needed?  Does it mean we cannot rely on @beaninfo tags?
        if (container instanceof javax.swing.RootPaneContainer) {
            return ((javax.swing.RootPaneContainer) container).getContentPane();
        }
        
        Container containerDelegate = (Container) container;
        
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            try {
                java.lang.reflect.Method m = getBeanClass().getMethod(
                    delegateGetter, new Class [0]); // NOI18N
                containerDelegate = (Container) m.invoke(container, new Object [0]);
            } catch (Exception e) {
                // IGNORE
            }
        }
        
        return containerDelegate;
    }

    public void setLayoutNodeReference(LayoutNode node) {
        this.layoutNode = node;
    }

    public LayoutNode getLayoutNodeReference() {
        return layoutNode;
    }

    boolean shouldHaveLayoutNode() {
        return layoutSupport != null && layoutSupport.getLayoutClass() != null;
    }

    // -----------------------------------------------------------------------------
    // Layout Manager management

//      public DesignLayout getPreviousDesignLayout() {
//          return previousLayout;
//      }

//      public DesignLayout getDesignLayout() {
//          return designLayout;
//      }

//      /** Must be called after initSubComponents!!! */
//      public void setDesignLayout(DesignLayout layout) {
//          if (designLayout instanceof DesignSupportLayout) {
//              throw new InternalError("Cannot change a design layout on this container"); // NOI18N
//          }
//          if (designLayout != null) {
//              if (layout.getClass().equals(designLayout.getClass())) return;
//              designLayout.setRADContainer(null);
//          }
//          if (layout == null) return;

//          previousLayout = designLayout;
//          designLayout = layout;
//          designLayout.setRADContainer(this);

//          RADVisualComponent[] children = getSubComponents();
//          for (int i = 0; i < children.length; i++) {
//              designLayout.addComponent(children[i]);
//          }

//          getContainer().validate();
//          getContainer().repaint();

//      }

    public void setLayoutSupport(LayoutSupport laySup) {
//        LayoutSupport oldLayoutSupport = this.layoutSupport;
        RADVisualComponent[] comps = getSubComponents();

        // remove components from current layout
        if (layoutSupport != null)
            for (int i = 0; i < comps.length; i++)
                layoutSupport.removeComponent(comps[i]);

        // set new layout
        this.layoutSupport = laySup;
        if (layoutSupport != null) {
            layoutSupport.initialize(this);

            // add components to the new layout
            for (int i = 0; i < comps.length; i++) {
                RADVisualComponent comp = comps[i];
                layoutSupport.addComponent(comp, 
                                           layoutSupport.getConstraints(comp));
                comp.resetConstraintsProperties();
            }

//            setLayoutNodeReference(layoutSupport.getLayoutClass() != null ?
//                                   new LayoutNode(layoutSupport) : null);
        }

        setLayoutNodeReference(null);
    }

    public LayoutSupport getLayoutSupport() {
        return layoutSupport;
    }

    /** Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").
     * @return the prefix code for generating code to add subcomponents to this
     * container
     */
    public String getContainerGenName() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null)
            return getName() + "." + delegateGetter + "()."; // NOI18N
        else
            return getName() + "."; // NOI18N
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    public RADComponent[] getSubBeans() {
        RADVisualComponent[] components = new RADVisualComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public RADVisualComponent[] getSubComponents() {
        RADVisualComponent[] components = new RADVisualComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            subComponents.add(initComponents[i]);
            ((RADVisualComponent)initComponents[i]).initParent(this);
        }
    }

    public void reorderSubComponents(int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = subComponents.remove(from);
            if (from < to) {
                subComponents.add(to - 1, value);
            } else {
                subComponents.add(to, value);
            }
        }
        //XXXgetDesignLayout().updateLayout();
        getFormModel().fireFormChanged();
    }

    public void add(RADComponent comp) {
        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        subComponents.add(comp);
        ((RADVisualComponent)comp).initParent(this);
        if (getNodeReference() != null) { // it can be null in the case when copying containers with components
            ((RADChildren)getNodeReference().getChildren()).updateKeys();
        }
    }

    public void remove(RADComponent comp) {
        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        layoutSupport.removeComponent(((RADVisualComponent)comp));
        int index = subComponents.indexOf(comp);
        if (index != -1) {
            subComponents.remove(index);
        }
        ((RADChildren)getNodeReference().getChildren()).updateKeys();
    }

    public int getIndexOf(RADComponent comp) {
        if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException();
        return subComponents.indexOf(comp);
    }
}
