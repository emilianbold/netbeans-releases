/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.util.Vector;
import java.awt.Component;
import java.awt.Image;
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of Customizer2 to subclass and/or use for convenience.  The 'panelClass'
 * defines the piece of UI to use for the customizer.  If the panelClass has a constructor that
 * takes a DesignBean, that will be used.  Otherwise, a null constructor will be used.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see Customizer2
 */
public class BasicCustomizer2 implements Customizer2 {

    public BasicCustomizer2() {}

    public BasicCustomizer2(Class panelClass) {
        this(panelClass, "Customizer", null, null); // NOI18N
    }

    public BasicCustomizer2(Class panelClass, String displayName) {
        this(panelClass, displayName, null, null);
    }

    public BasicCustomizer2(Class panelClass, String displayName, String description) {
        this(panelClass, displayName, description, null);
    }

    public BasicCustomizer2(Class panelClass, String displayName, String description,
        String helpKey) {
        this.panelClass = panelClass;
        this.displayName = displayName;
        this.description = description;
        this.helpKey = helpKey;
    }

    protected Class panelClass;
    public void setPanelClass(Class panelClass) {
        this.panelClass = panelClass;
    }

    public Class getPanelClass() {
        return panelClass;
    }

    protected Component createCustomizerPanel() {
        if (panelClass != null) {
            Object panel = null;
            try {
                Constructor con = panelClass.getConstructor(new Class[] {DesignBean.class});
                panel = con.newInstance(new Object[] {designBean});
            }
            catch (Exception x) {
                try {
                    panel = panelClass.newInstance();
                }
                catch (Exception ex) {}
            }
            if (panel instanceof Component) {
                return (Component)panel;
            }
        }
        return null;
    }

    public Component getCustomizerPanel(DesignBean designBean) {
        this.designBean = designBean;
        return createCustomizerPanel();
    }

    protected DesignBean designBean;
    public DesignBean getDesignBean() {
        return designBean;
    }

    protected boolean applyCapable = true;
    public void setApplyCapable(boolean applyCapable) {
        this.applyCapable = applyCapable;
    }

    public boolean isApplyCapable() {
        return applyCapable;
    }

    protected boolean modified = false;
    public void setModified(boolean modified) {
        this.modified = modified;
        firePropertyChange();
    }

    public boolean isModified() {
        return modified;
    }

    public Result applyChanges() {
        // do stuff here!
        this.modified = false;
        firePropertyChange();
        return Result.SUCCESS;
    }

    protected String displayName;
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    protected String description;
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    protected Image largeIcon;
    public void setLargeIcon(Image largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Image getLargeIcon() {
        return largeIcon;
    }

    protected Image smallIcon;
    public void setSmallIcon(Image smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Image getSmallIcon() {
        return smallIcon;
    }

    protected String helpKey;
    public void setHelpKey(String helpKey) {
        this.helpKey = helpKey;
    }

    public String getHelpKey() {
        return helpKey;
    }

    protected Vector propertyChangeListeners;
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        Vector v = propertyChangeListeners == null ? new Vector(2) :
            (Vector)propertyChangeListeners.clone();
        if (!v.contains(l)) {
            v.addElement(l);
            propertyChangeListeners = v;
        }
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeListeners != null && propertyChangeListeners.contains(l)) {
            Vector v = (Vector)propertyChangeListeners.clone();
            v.removeElement(l);
            propertyChangeListeners = v;
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        Vector v = propertyChangeListeners == null ? new Vector(2) :
            (Vector)propertyChangeListeners.clone();
        return (PropertyChangeListener[])v.toArray(new PropertyChangeListener[v.size()]);
    }

    public void firePropertyChange() {
        if (designBean != null) {
            PropertyChangeEvent e = new PropertyChangeEvent(designBean.getInstance(), null, null, null);
            firePropertyChange(e);
        }
    }

    public void firePropertyChange(String propName) {
        if (designBean != null) {
            PropertyChangeEvent e = new PropertyChangeEvent(designBean.getInstance(), propName, null, null);
            firePropertyChange(e);
        }
    }

    public void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (designBean != null) {
            PropertyChangeEvent e = new PropertyChangeEvent(designBean.getInstance(), propName,
                oldValue, newValue);
            firePropertyChange(e);
        }
    }

    public void firePropertyChange(PropertyChangeEvent e) {
        if (propertyChangeListeners != null) {
            Vector listeners = propertyChangeListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((PropertyChangeListener)listeners.elementAt(i)).propertyChange(e);
            }
        }
    }
}
