/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.loader;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Element;

import org.openide.cookies.InstanceCookie;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.Presenter;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.run.TargetExecutor;

/** An instance cookie providing an action running a script.
 * The action provides the standard presenters, so may be used
 * e.g. in menu items.
 */
public class AntActionInstance implements
        InstanceCookie, Action,
        Presenter.Menu, Presenter.Toolbar,
        ChangeListener, Serializable
{
    
    private static final long serialVersionUID = 295629651296596532L;
    
    private final AntProjectCookie proj;
    private transient Set listeners = new HashSet (1); // Set<PropertyChangeListener>
    
    public AntActionInstance (AntProjectCookie proj) {
        this.proj = proj;
        proj.addChangeListener (WeakListener.change (this, proj));
    }
    
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        listeners = new HashSet (1);
    }
    
    // InstanceCookie:
    
    public Class instanceClass () {
        return AntActionInstance.class;
    }
    
    public String instanceName () {
        return instanceClass ().getName ();
    }
    
    /*
    public boolean instanceOf (Class type) {
        return type.isAssignableFrom (AntActionInstance.class);
    }
     */
    
    public Object instanceCreate () {
        return this;
    }
    
    // Action:
    
    public void actionPerformed (ActionEvent ignore) {
        TargetExecutor exec = new TargetExecutor (proj, null);
        try {
            exec.execute ();
        } catch (IOException ioe) {
            AntModule.err.notify (ioe);
        }
    }
    
    public boolean isEnabled () {
        return proj.getParseException () == null &&
               proj.getFile () != null;
    }
    
    public void setEnabled (boolean b) {
        // ignore
    }
    
    public Object getValue (String key) {
        if (Action.NAME.equals (key)) {
            Element el = proj.getProjectElement ();
            if (el != null) {
                return el.getAttribute ("name"); // NOI18N
            }
        } else if (Action.SMALL_ICON.equals (key)) {
            return new ImageIcon (NbBundle.getLocalizedFile
                ("org.apache.tools.ant.module.resources.AntIcon", "gif", // NOI18N
                 Locale.getDefault (), AntActionInstance.class.getClassLoader ()));
        }
        return null;
    }
    
    public void putValue (String key, Object value) {
        // ignore
    }
    
    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        synchronized (listeners) {
            listeners.add (listener);
        }
    }
    
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        synchronized (listeners) {
            listeners.remove (listener);
        }
    }
    
    // Presenter.Menu:
    
    public JMenuItem getMenuPresenter () {
        return new JMenuItem (this);
    }
    
    // Presenter.Toolbar:
    
    public Component getToolbarPresenter () {
        return new JButton (this);
    }
    
    // ChangeListener:
    
    public void stateChanged (ChangeEvent ignore) {
        // Ant script changed; maybe the project name changed with it.
        // Or maybe it is now misparsed.
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        PropertyChangeEvent ev1 = new PropertyChangeEvent (this, Action.NAME, null, getValue (Action.NAME));
        PropertyChangeEvent ev2 = new PropertyChangeEvent (this, "enabled", null, new Boolean (isEnabled ())); // NOI18N
        while (it.hasNext ()) {
            PropertyChangeListener listener = (PropertyChangeListener) it.next ();
            listener.propertyChange (ev1);
            listener.propertyChange (ev2);
        }
    }
    
}
