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
import java.util.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;

/**
 * @author Tran Duc Trung, Tomas Pavek
 */

public abstract class AbstractLayoutSupport implements LayoutSupport
{
    private static Image defaultLayoutIcon =
        Toolkit.getDefaultToolkit().getImage(
            AbstractLayoutSupport.class.getResource(
                "resources/AbstractLayout.gif")); // NOI18N
    private static Image defaultLayoutIcon32 =
        Toolkit.getDefaultToolkit().getImage(
            AbstractLayoutSupport.class.getResource(
                "resources/AbstractLayout32.gif")); // NOI18N

    private static ResourceBundle bundle = null;

    private RADComponent metaLayout;

    private RADVisualContainer container;

    private boolean setToContainer = false;
    
    // -------------------------------
    // LayoutSupport implementation

    public Image getIcon(int type) {
        if (metaLayout != null) {
            Image icon = metaLayout.getBeanInfo().getIcon(type);
            if (icon != null) return icon;
        }

        switch (type) {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                return defaultLayoutIcon;
            default:
                return defaultLayoutIcon32;
        }
    }
    
    public String getDisplayName() {
        String name = getLayoutClass().getName();
        int lastdot = name.lastIndexOf('.');
        if (lastdot > 0)
            name = name.substring(lastdot + 1);
        return name;
    }

    public void initialize(RADVisualContainer container) {
        Class layoutClass = getLayoutClass();
        if (layoutClass != null) {
            if (this.container != container) {
                this.container = container;

                metaLayout = new MetaLayout(this, container);
                metaLayout.setComponent(layoutClass);

                // LayoutSupport may not be set to container (yet)
                setToContainer = container.getLayoutSupport() == this;
            }
            // LayoutSupport has already been initialized, but may not have
            // been set to container (test if it already is now)
            else if (!setToContainer && container.getLayoutSupport() == this) {
                Container cont = container.getContainerDelegate(
                                              container.getBeanInstance());
                cont.setLayout((LayoutManager)metaLayout.getBeanInstance());
                setToContainer = true;
            }
        }
        else { // no real layout is used
            this.container = container;
            metaLayout = null;
        }
    }

    public final RADVisualContainer getContainer() {
        return container;
    }

    public LayoutManager createDefaultLayoutInstance(Container cont) {
        Class layoutClass = getLayoutClass();
        return layoutClass == null ? null :
               (LayoutManager) BeanSupport.createBeanInstance(layoutClass);
    }

    public LayoutManager cloneLayoutInstance(Container cont) {
        if (metaLayout == null) return null;

        try {
            return (LayoutManager) metaLayout.cloneBeanInstance();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        // alternatively:
//        LayoutManager lm = createLayoutInstance(cont);
//        BeanSupport.copyProperties(metaLayout.getAllBeanProperties(), lm);
    }

    public Node.PropertySet[] getPropertySets() {
        return metaLayout != null ? metaLayout.getProperties() : null;
    }

    public Class getCustomizerClass() {
        return null;
    }

    public ConstraintsDesc getNewConstraints(Container cont, Point posInCont,
                                             Component comp, Point posInComp) {
        return null;
    }

    public int getNewIndex(Container container,
                           Point posInCont,
                           Component component,
                           Point posInComp) {
        return -1;
    }

    public boolean paintDragFeedback(Container container,
                                     Component component,
                                     ConstraintsDesc newConstraints,
                                     int newIndex,
                                     Graphics g) {
        return false;
    }

    public ConstraintsDesc fixConstraints(ConstraintsDesc constraintsDesc) {
        return constraintsDesc;
    }

    public int getResizableDirections(Component component) {
        return 0;
    }

    public ConstraintsDesc getResizedConstraints(Component component,
                                                 Insets sizeChanges) {
        return null;
    }

    public void addComponent(RADVisualComponent component, ConstraintsDesc desc) {
        if (desc != null)
            component.setConstraintsDesc(getClass(), desc);

        // add the component to real container (although never shown)
        Container cont = container.getContainerDelegate(container.getBeanInstance());
        if (cont != null)
            if (desc != null)
                cont.add(component.getComponent(), desc.getConstraintsObject());
            else
                cont.add(component.getComponent());
    }
    
    public void removeComponent(RADVisualComponent component) {
        // remove the component from real container
        Container cont = container.getContainerDelegate(container.getBeanInstance());
        if (cont != null)
            cont.remove(component.getComponent());
    }

    public ConstraintsDesc getConstraints(RADVisualComponent component) {
        return (ConstraintsDesc) component.getConstraintsDesc(getClass());
    }

    public String getJavaSetLayoutString(/*RADVisualContainer cont*/) {
        if (getLayoutClass() == null) return null;

        StringBuffer buf = new StringBuffer();
        String delegate = getContainer().getJavaContainerDelegateString();
        if (!"".equals(delegate)) { // NOI18N
            buf.append(delegate);
            buf.append("."); // NOI18N
        }
        buf.append("setLayout("); // NOI18N

        CreationDescriptor cd = CreationFactory.getDescriptor(getLayoutClass());
        if (cd != null) {
            FormProperty[] props = getCreationProperties();
            CreationDescriptor.Creator creator = cd.findBestCreator(props,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);
            if (creator != null)
                buf.append(creator.getJavaCreationCode(props));
            else
                cd = null;
        }

        if (cd == null) { // no special creation code
            buf.append("new "); // NOI18N
            buf.append(getLayoutClass().getName().replace('$', '.'));
            buf.append("()");
        }

        buf.append(");\n"); // NOI18N
        return buf.toString();
    }

    public String getJavaAddComponentString(/*RADVisualContainer cont,*/
                                            RADVisualComponent comp) {
        StringBuffer buf = new StringBuffer();

        String delegate = getContainer().getJavaContainerDelegateString();
        if (!"".equals(delegate)) { // NOI18N
            buf.append(delegate);
            buf.append("."); // NOI18N
        }

        buf.append("add("); // NOI18N
        buf.append(comp.getName());

        ConstraintsDesc desc = getConstraints(comp);
        String constr = desc != null ? desc.getJavaInitializationString() : null;
        if (constr != null && !"".equals(constr)) { // NOI18N
            buf.append(", "); // NOI18N
            buf.append(constr);
        }

        buf.append(");\n"); // NOI18N
        return buf.toString();
    }

    // ---------------------
    // other methods

    public FormProperty getProperty(String propName) {
        return metaLayout == null ? null :
                                    metaLayout.getPropertyByName(propName);
    }

    protected final RADComponent getMetaLayout() {
        return metaLayout;
    }

    protected FormProperty[] getCreationProperties() {
        return metaLayout.getAllBeanProperties();
    }

    protected ConstraintsDesc getConstraints(Component comp, Container cont) {
        if (comp == null || cont == null) return null;

        Component[] comps = cont.getComponents();
        for (int i=0; i < comps.length; i++)
            if (comps[i] == comp) {
                RADVisualComponent[] metacomps = container.getSubComponents();
                return getConstraints(metacomps[i]);
            }

        return null;
    }

    static protected ResourceBundle getBundle() {
        if (bundle == null)
            bundle = NbBundle.getBundle(AbstractLayoutSupport.class);
        return bundle;
    }
}
