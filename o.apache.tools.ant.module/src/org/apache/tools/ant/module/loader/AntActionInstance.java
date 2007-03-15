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

package org.apache.tools.ant.module.loader;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.Presenter;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** An instance cookie providing an action running a script.
 * The action provides the standard presenters, so may be used
 * e.g. in menu items.
 */
public class AntActionInstance implements
        InstanceCookie, Action,
        Presenter.Menu, Presenter.Toolbar,
        ChangeListener, PropertyChangeListener
{
    
    private boolean inited;
    private final AntProjectCookie proj;
    private transient PropertyChangeSupport changeSupport;
    
    public AntActionInstance (AntProjectCookie proj) {
        this.proj = proj;
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        init();
    }
    
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
    
    public Object instanceCreate () {
        init();
        return this;
    }
    
    private void init() {
        if (inited) {
            return;
        }
        inited = true;
        proj.addChangeListener(WeakListeners.change(this, proj));
        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjects.getDefault()));
        changeSupport = new PropertyChangeSupport(this);
    }
    
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
        if (proj.getFile() == null) {
            return false; // cannot run script not on disk
        }
        // #21249: if it delegates to a script in a project, enable only if that project is open
        Element root = proj.getProjectElement();
        if (root == null) {
            return false; // misparse
        }
        NodeList nl = root.getElementsByTagName("ant"); // NOI18N
        if (nl.getLength() == 1) {
            Element ant = (Element) nl.item(0);
            String antfile = ant.getAttribute("antfile"); // NOI18N
            if (antfile.length() == 0) {
                String dir = ant.getAttribute("dir"); // NOI18N
                if (dir.length() > 0) {
                    antfile = dir + File.separatorChar + "build.xml"; // NOI18N
                }
            }
            if (antfile.length() > 0) {
                FileObject fo = FileUtil.toFileObject(new File(antfile));
                if (fo != null) {
                    Project owner = FileOwnerQuery.getOwner(fo);
                    if (owner != null) {
                        return Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(owner);
                    }
                }
            }
        }
        return true;
    }
    
    public void setEnabled (boolean b) {
        assert false;
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
        changeSupport.addPropertyChangeListener(listener);
    }
    
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
    public JMenuItem getMenuPresenter () {
        class AntMenuItem extends JMenuItem implements DynamicMenuContent {
            public AntMenuItem() {
                super(AntActionInstance.this);
            }
            public JComponent[] getMenuPresenters() {
                return isEnabled() ? new JComponent[] {this} : new JComponent[0];
            }
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return getMenuPresenters();
            }
        }
        return new AntMenuItem();
    }

    public Component getToolbarPresenter () {
        class AntButton extends JButton implements PropertyChangeListener {
            public AntButton() {
                super(AntActionInstance.this);
                // XXX setVisible(false) said to be poor on GTK L&F; consider using #26338 instead
                setVisible(isEnabled());
                AntActionInstance.this.addPropertyChangeListener(WeakListeners.propertyChange(this, AntActionInstance.this));
            }
            public void propertyChange(PropertyChangeEvent evt) {
                if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                    setVisible(isEnabled());
                }
            }
        }
        return new AntButton();
    }
    
    public void stateChanged (ChangeEvent ignore) {
        // Ant script changed; maybe the project name changed with it.
        // Or maybe it is now misparsed.
        changeSupport.firePropertyChange(Action.NAME, null, getValue (Action.NAME));
        changeSupport.firePropertyChange("enabled", null, isEnabled () ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        changeSupport.firePropertyChange(Action.MNEMONIC_KEY, null, getValue (Action.MNEMONIC_KEY));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // Open projects list may have changed.
        changeSupport.firePropertyChange("enabled", null, isEnabled() ? Boolean.TRUE : Boolean.FALSE); // NOI18N
    }
    
}
