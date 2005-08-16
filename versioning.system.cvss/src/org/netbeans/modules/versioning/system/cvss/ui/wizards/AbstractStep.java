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

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;

/**
 * Abstract wizard panel with <codE>valid</code>
 * and <codE>errorMessage</code> bound properties.
 *
 * <p>Components use 3:2 (60x25 chars) size mode
 * to avoid wizard resizing after [next>].
 *
 * @author Petr Kuzel
 */
abstract class AbstractStep implements WizardDescriptor.ValidatingPanel {

    private List listeners = new LinkedList();
    private boolean valid;
    private JComponent panel;
    private volatile boolean underConstruction;
    private String errorMessage;
    private boolean applyStandaloneLayout;

    /**
     * If called before getComponent it disables 3:2 size mode.
     */
    public void applyStandaloneLayout() {
        applyStandaloneLayout = true;
    }

    /**
     * Calls to createComponent
     */
    public final synchronized Component getComponent() {
        if (panel == null) {
            try {
                underConstruction = true;
                panel = createComponent();

                if (applyStandaloneLayout == false) {
                    JTextArea template = new JTextArea();
                    template.setColumns(60);
                    template.setRows(25);
                    panel.setPreferredSize(template.getPreferredSize());
                }
            } finally {
                underConstruction = false;
                fireChange();
            }
        }
        return panel;
    }

    protected abstract JComponent createComponent();

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
    }

    protected final void valid() {
        setValid(true, null);
    }

    protected final void invalid(String message) {
        setValid(false, message);
    }

    public final boolean isValid() {
        return valid;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    // comes on next or finish
    public final void validate () throws WizardValidationException {
        validateBeforeNext();
        if (isValid() == false) {
            throw new WizardValidationException (
                panel,
                errorMessage,
                errorMessage
            );
        }
    }

    /**
     * Perform heavy validation reporting results
     * using {@link #valid} and {@link #invalid}.
     */
    protected abstract void validateBeforeNext();

    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    private void setValid(boolean valid, String errorMessage) {
        boolean fire = AbstractStep.this.valid != valid;
        fire |= errorMessage != null && (errorMessage.equals(this.errorMessage) == false);
        AbstractStep.this.valid = valid;
        this.errorMessage = errorMessage;
        if (fire) {
            fireChange();
        }
    }

    private void fireChange() {
        if (underConstruction) return;
        List clone;
        synchronized(listeners) {
            clone = new ArrayList(listeners);
        }
        Iterator it = clone.iterator();
        ChangeEvent event = new ChangeEvent(this);
        while (it.hasNext()) {
            ChangeListener listener = (ChangeListener) it.next();
            listener.stateChanged(event);
        }
    }

}
