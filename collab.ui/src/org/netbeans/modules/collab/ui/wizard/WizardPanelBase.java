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
package org.netbeans.modules.collab.ui.wizard;

import org.openide.*;
import org.openide.util.*;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class WizardPanelBase extends JPanel implements WizardDescriptor.Panel {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String name;
    private boolean initialized;
    private boolean valid;
    private Set changeListeners = new HashSet();

    /**
     *
     *
     */
    public WizardPanelBase() {
        super();
    }

    /**
     *
     *
     */
    public WizardPanelBase(String name) {
        this();
        setName(name);
    }

    /**
     *
     *
     */
    public synchronized Component getComponent() {
        return this;
    }

    /**
     *
     *
     */
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    public boolean isValid() {
        return valid;
    }

    /**
     *
     *
     */
    public void setValid(boolean value) {
        boolean oldValue = value;
        valid = value;
        firePropertyChange("valid", oldValue, value); // NOI18N
        fireStateChanged();
    }

    /**
     *
     *
     */
    public abstract void readSettings(Object settings);

    /**
     *
     *
     */
    public abstract void storeSettings(Object settings);

    /**
     *
     *
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     *
     *
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     *
     *
     */
    public void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);

        for (Iterator i = changeListeners.iterator(); i.hasNext();) {
            try {
                ((ChangeListener) i.next()).stateChanged(event);
            } catch (Exception e) {
                Debug.debugNotify(e);
            }
        }
    }
}
