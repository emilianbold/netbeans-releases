/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.Refreshable;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.cookies.ErrorLocator;
import org.netbeans.modules.xml.multiview.cookies.LinkCookie;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author mkuchtiak
 */
public abstract class SectionInnerPanel extends javax.swing.JPanel implements LinkCookie, ErrorLocator {
    private SectionView sectionView;
    private java.util.List refreshableList = new LinkedList();

    private boolean localFocusListenerInitialized = false;
    private FocusListener localFocusListener = new FocusListener() {
        public void focusGained(FocusEvent evt) {
            final FocusListener[] focusListeners = getFocusListeners();
            for (int i = 0; i < focusListeners.length; i++) {
                focusListeners[i].focusGained(evt);
            }
        }

        public void focusLost(FocusEvent evt) {
            processFocusEvent(evt);
        }
    };

    private RequestProcessor.Task refreshTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            refreshView();
        }
    });

    private static final int REFRESH_DELAY = 50;
    private FlushFocusListener activeListener = null;
    private boolean closing = false;

    /** Constructor that takes the enclosing SectionView object as its argument
     * @param sectionView enclosing SectionView object
     */
    public SectionInnerPanel(SectionView sectionView) {
        this.sectionView = sectionView;
    }

    public synchronized void addFocusListener(FocusListener l) {
        super.addFocusListener(l);
        if (!localFocusListenerInitialized) {
            localFocusListenerInitialized = true;
            Container container = this;
            FocusListener focusListener = localFocusListener;
            addFocusListenerRecursively(container, focusListener);
        }
    }

    private void addFocusListenerRecursively(Container container, FocusListener focusListener) {
        final Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component.isFocusable() && !(component instanceof JLabel)) {
                component.addFocusListener(focusListener);
            }
            if (component instanceof Container) {
                if (!(component instanceof SectionNodePanel)) {
                    addFocusListenerRecursively((Container) component, focusListener);
                }
            }
        }
    }

    /** Getter for section view
     * @return sectionView enclosing SectionView object
     */
    public SectionView getSectionView() {
        return sectionView;
    }

    /** Callback method called on focus lost event after the value was checked for correctness.
     * @param source last focused JComponent
     * @param value the value that has been set (typed) in the component
     */
    public abstract void setValue(JComponent source, Object value);

    /** Callback method called on document change event. This is called for components that
     * require just-in-time validation.
     * @param source JTextComponent being actually edited
     * @param value the actual value of the component
     */
    public void documentChanged(javax.swing.text.JTextComponent source, String value) {
    }

    /** Callback method called on focus lost event after the value was checked for correctness.
     * and the result is that the value is wrong. The value should be rollbacked from the model.
     * @param source last focused JComponent
     */
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }

    /** Adds text component to the set of JTextComponents that modifies the model.
     * After the value in this component is changed the setValue() method is called.
     * @param tc JTextComponent whose content is related to data model
     */
    public final void addModifier(final JTextComponent tc) {
        tc.addFocusListener(new ModifyFocusListener(tc));
    }

    /** Adds text component to the set of JTextComponents that should be validated correctness.
     * After the value in this component is changed either setValue() method is called(value is correct)
     * or rollbackValue() method is called(value is incorrect). Also the documentChanged() method is called during editing.
     * @param tc JTextComponent whose content is related to data model and should be validated before saving to data model.
     */
    public final void addValidatee(final JTextComponent tc) {
        tc.getDocument().addDocumentListener(new TextListener(tc));
        tc.addFocusListener(new ValidateFocusListener(tc));
    }

    protected void scheduleRefreshView() {
        refreshTask.schedule(REFRESH_DELAY);
    }

    /**
     * Reloads data from data model
     */
    public void refreshView() {
        for (Iterator it = refreshableList.iterator(); it.hasNext();) {
            ((Refreshable) it.next()).refresh();
        }
    }

    protected void addRefreshable(Refreshable refreshable) {
        refreshableList.add(refreshable);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        scheduleRefreshView();
    }

    private class TextListener implements javax.swing.event.DocumentListener {

        private JTextComponent tc;

        TextListener(JTextComponent tc) {
            this.tc = tc;
        }

        /**
         * Method from DocumentListener
         */
        public void changedUpdate(javax.swing.event.DocumentEvent evt) {
            update(evt);
        }

        /**
         * Method from DocumentListener
         */
        public void insertUpdate(javax.swing.event.DocumentEvent evt) {
            update(evt);
        }

        /**
         * Method from DocumentListener
         */
        public void removeUpdate(javax.swing.event.DocumentEvent evt) {
            update(evt);
        }

        private void update(javax.swing.event.DocumentEvent evt) {
            try {
                String text = evt.getDocument().getText(0, evt.getDocument().getLength());
                documentChanged(tc, text);
            } catch (javax.swing.text.BadLocationException ex) {
            }
        }
    }

    private abstract class FlushFocusListener extends java.awt.event.FocusAdapter {
        public abstract boolean flushData();
    }

    private class ValidateFocusListener extends FlushFocusListener {
        private String orgValue;
        private boolean viewIsBuggy;
        private final JTextComponent tc;

        public ValidateFocusListener(JTextComponent tc) {
            this.tc = tc;
        }

        public void focusGained(FocusEvent evt) {
            activeListener = this;
            orgValue = tc.getText();
            if (sectionView.getErrorPanel().getError() != null) {
                viewIsBuggy = true;
            } else {
                viewIsBuggy = false;
            }
        }

        public void focusLost(FocusEvent evt) {
            if (!closing) {
                if (!flushData()) {
                    Utils.runInAwtDispatchThread(new Runnable() {
                        public void run() {
                            //todo: make sure the panel is visible
                            tc.requestFocus();
                        }
                    });
                } else {
                    activeListener = null;
                }
            }
        }

        public boolean flushData() {
            Error error = sectionView.getErrorPanel().getError();
            if (error != null && error.isEditError() && tc == error.getFocusableComponent()) {
                if (Error.TYPE_WARNING == error.getSeverityLevel()) {
                    org.openide.DialogDescriptor desc = new RefreshSaveDialog(sectionView.getErrorPanel());
                    Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                    dialog.show();
                    Integer opt = (Integer) desc.getValue();
                    if (opt.equals(RefreshSaveDialog.OPTION_FIX)) {
                        return false;
                    } else if (opt.equals(RefreshSaveDialog.OPTION_REFRESH)) {
                        rollbackValue(tc);
                        sectionView.checkValidity();
                    } else {
                        signalUIChange();
                        setValue(tc, tc.getText());
                        sectionView.checkValidity();
                    }
                } else {
                    org.openide.DialogDescriptor desc = new RefreshDialog(sectionView.getErrorPanel());
                    Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                    dialog.show();
                    Integer opt = (Integer) desc.getValue();
                    if (opt.equals(RefreshDialog.OPTION_FIX)) {
                        return false;
                    } else if (opt.equals(RefreshDialog.OPTION_REFRESH)) {
                        rollbackValue(tc);
                        sectionView.checkValidity();
                    }
                }
            } else {
                if (!tc.getText().equals(orgValue)) {
                    signalUIChange();
                    setValue(tc, tc.getText());
                    sectionView.checkValidity();
                } else {
                    if (viewIsBuggy) {
                        sectionView.checkValidity();
                    }
                }
            }
            return true;
        }
    }

    private class ModifyFocusListener extends FlushFocusListener {
        private String orgValue;
        private final JTextComponent tc;

        public ModifyFocusListener(JTextComponent tc) {
            this.tc = tc;
        }

        public void focusGained(FocusEvent evt) {
            orgValue = tc.getText();
            activeListener = this;
        }

        public void focusLost(FocusEvent evt) {
            if (!closing) {
                flushData();
                activeListener = null;
            }
        }

        public boolean flushData() {
            if (!tc.getText().equals(orgValue)) {
                signalUIChange();
                setValue(tc, tc.getText());
            }
            return true;
        }
    }

    public boolean canClose() {
        closing = true;
        try {
            if (activeListener != null) {
                return activeListener.flushData();
            }
            return true;
        } finally {
            closing = false;
        }
    }
    /** This will be called before model is changed from this panel
     */
    protected void signalUIChange() {
    }
}
