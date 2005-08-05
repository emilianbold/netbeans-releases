/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.loader;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.Presenter;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

/** An instance cookie providing an action running a script.
 * The action provides the standard presenters, so may be used
 * e.g. in menu items.
 */
public class AntActionInstance implements
        InstanceCookie, Action,
        Presenter.Menu, Presenter.Toolbar,
        ChangeListener
{
    
    private final AntProjectCookie proj;
    private transient PropertyChangeSupport changeSupport;
    
    public AntActionInstance (AntProjectCookie proj) {
        this.proj = proj;
        proj.addChangeListener(WeakListeners.change(this, proj));
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        changeSupport = null;
    }
    
    // InstanceCookie:
    
    public Class instanceClass () {
        return AntActionInstance.class;
    }
    
    public String instanceName () {
        FileObject fo = proj.getFileObject();
        if (fo != null) {
            return fo.getName(); // without .xml extension
        } else {
            // ???
            return ""; // NOI18N
        }
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
        // #21355 similar to fix of #16720 - don't do this in the event thread...
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                TargetExecutor exec = new TargetExecutor (proj, null);
                try {
                    exec.execute ();
                } catch (IOException ioe) {
                    AntModule.err.notify (ioe);
                }
            }
        });
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
                String pname = el.getAttribute ("name"); // NOI18N
                return Actions.cutAmpersand(pname);
            }
        } else if (Action.SMALL_ICON.equals (key)) {
            try {
                return new ImageIcon(new URL("nbresloc:/org/apache/tools/ant/module/resources/AntIcon.gif"));
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
        } else if (Action.MNEMONIC_KEY.equals (key)) {
            Element el = proj.getProjectElement ();
            if (el != null) {
                String pname = el.getAttribute ("name"); // NOI18N
                int idx = Mnemonics.findMnemonicAmpersand(pname);
                if (idx != -1) {
                    // XXX this is wrong, should use some method in Mnemonics...
                    return new Integer (pname.charAt (idx + 1));
                }
            }
            return new Integer (0); // #: 13084
        }
        return null;
    }
    
    public void putValue (String key, Object value) {
        // ignore
    }
    
    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }
    
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(listener);
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

        if (changeSupport == null)
            return;
        
        changeSupport.firePropertyChange(Action.NAME, null, getValue (Action.NAME));
        changeSupport.firePropertyChange("enabled", null, isEnabled () ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        changeSupport.firePropertyChange(Action.MNEMONIC_KEY, null, getValue (Action.MNEMONIC_KEY));
    }
    
}
