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


package org.netbeans.modules.i18n.wizard;


import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx; 
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.WizardDescriptor;


/**
 * Wizard descriptor of i18n wizard.
 *
 * @author  Peter Zavadsky
 */
public class I18nWizardDescriptor extends WizardDescriptor {

    /** Preferred size for panels in i18n wizard. */
    public static final Dimension PREFERRED_DIMENSION = new Dimension(500, 300);
    
    /** Hack. In super it's private. */
    private final WizardDescriptor.Iterator panels;

    /** Hack. In super it's private. */
    private final Object settings;
    
    /** Next button. */
    private final JButton nextButton = new JButton();
    /** Previous button. */
    private final JButton previousButton = new JButton();
    /** Finish button. */
    private final JButton finishButton = new JButton();
    /** Cancel button. */
    private final JButton cancelButton = new JButton();

    
    /** Creates new I18nWizardDescriptor */
    public I18nWizardDescriptor(WizardDescriptor.Iterator panels, Object settings) {
        super(panels, settings);
        
        Listener listener = new Listener();

        // Button init.
        nextButton.setText(NbBundle.getBundle(getClass()).getString("CTL_Next"));
        previousButton.setText(NbBundle.getBundle(getClass()).getString("CTL_Previous"));
        finishButton.setText(NbBundle.getBundle(getClass()).getString("CTL_Finish"));
        cancelButton.setText(NbBundle.getBundle(getClass()).getString("CTL_Cancel"));

        finishButton.setDefaultCapable(true);
        nextButton.setDefaultCapable(true);
        previousButton.setDefaultCapable(false);
        cancelButton.setDefaultCapable(false);
        
        nextButton.addActionListener(listener);
        previousButton.addActionListener(listener);
        finishButton.addActionListener(listener);
        cancelButton.addActionListener(listener);

        setOptions(new Object[] { previousButton, nextButton, finishButton, cancelButton });
        setClosingOptions(new Object[] { cancelButton });

        this.panels = panels;
        this.settings = settings;
    }
    
    /** Overrides superclass method. */
    protected synchronized void updateState () {
        // Do superclass typical job.
        super.updateState();

        // And do the same for our buttons.        
        WizardDescriptor.Panel current = panels.current();
        
        boolean next = panels.hasNext ();
        boolean prev = panels.hasPrevious ();
        boolean valid = current.isValid();

        nextButton.setEnabled(next && valid);
        previousButton.setEnabled(prev);
        finishButton.setEnabled(valid && (!next || (current instanceof FinishPanel)));

        if(next)
            setValue(nextButton);
        else
            setValue(finishButton);

        setHelpCtx(current.getHelp());
    }

    
    /** Listener to changes in the iterator and panels. 
     * Hack, it's private in super. */
    private class Listener extends Object implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            if(ev.getSource () == nextButton) {
                
                final WizardDescriptor.Panel current = panels.current();
                
                if(current instanceof ProgressMonitor) {
                    // Do the search job first.
                    RequestProcessor.postRequest(new ProgressThread((ProgressMonitor)current) {
                        public void handleAction() {
                            handleNextButton();
                        }
                    });
                } else { 
                    handleNextButton();
                }
                
            } else if(ev.getSource () == previousButton) {
                panels.previousPanel ();
                updateState ();
            } else if(ev.getSource () == finishButton) {
                final WizardDescriptor.Panel current = panels.current();
                
                current.storeSettings(settings);                
                setValue(OK_OPTION);

                if(current instanceof ProgressMonitor) {
                    // Do the search job first.
                    RequestProcessor.postRequest(new ProgressThread((ProgressMonitor)current) {
                        public void handleAction() {
                            Dialog dialog = (Dialog)SwingUtilities.getAncestorOfClass(Dialog.class, current.getComponent());
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    });
                }
            } else if(ev.getSource () == cancelButton) {
                panels.current().storeSettings(settings);
                setValue(CANCEL_OPTION);
            }
        }
        
        /** Helper method. It's actually next button event handler. */
        private void handleNextButton() {
            panels.nextPanel ();
            try {
                updateState ();
            } catch (IllegalStateException ise) {
                panels.previousPanel();
                TopManager.getDefault().notify(new NotifyDescriptor.Message(ise.getMessage()));
                updateState();
            }
        }
    } // End of inner class Listener;

    
    /** Class used to handle <code>ProgressMonitor</code> inseparate thread. */
    private abstract class ProgressThread implements Runnable {
        
        /** <code>ProgressMonitor</code> to handle. */
        private ProgressMonitor progressMonitor;
        
        
        /** Constructor. */
        public ProgressThread(ProgressMonitor progressMonitor) {
            this.progressMonitor = progressMonitor;
        }
        
        
        /** Implements <code>Runnable</code> interface. */
        public void run() {
            try {
                previousButton.setEnabled(false);
                nextButton.setEnabled(false);
                finishButton.setEnabled(false);
                cancelButton.setEnabled(false);

                progressMonitor.doLongTimeChanges();

                handleAction();
            } finally {
                progressMonitor.reset();
            }
        }
        
        /** Method which provides additional handling. */
        public abstract void handleAction();
    } // End of inner ProgressThread.

    /** Interface which indicates for descriptor that the panel is provides long time changes and shows
     * progress monitoring. */    
    public interface ProgressMonitor {
        
        /** Provides long time changes. */
        public void doLongTimeChanges();
        
        /** Reset after finish of the changes. Call after previous method. */
        public void reset();
    } // End of interface.
    
    
    /**
     * Kind of abstract "adapter" implementing <code>WizardDescriptor.Panel interface</code>.
     * Used by i18n wizard.
     *
     * @author  Peter Zavadsky
     * @see org.openide.WizardDescriptor.Panel
     */
    public static abstract class Panel extends Object implements WizardDescriptor.Panel {

        /** Reference to panel. */
        private Component component;

        /** Keeps only one listener. It's fine since WizardDescriptor registers always the same listener. */
        private ChangeListener changeListener;


        /** Gets component to display. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return this instance */
        public final Component getComponent() {
            if(component == null) {
                synchronized(this) {
                    component = createComponent();
                }
            }

            return component;
        }

        /** Creates component. */
        protected abstract Component createComponent();

        /** Indicates if panel is valid. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return true */
        public boolean isValid() {
            return true;
        }

        /** Reads settings at the start when the panel comes to play. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void readSettings(Object settings) {
        }

        /** Stores settings at the end of panel show. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void storeSettings(Object settings) {
        }

        /** Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void addChangeListener(ChangeListener listener) {
            changeListener = listener;
        }

        /** Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void removeChangeListener(ChangeListener listener) {
            if(changeListener != null && changeListener == listener)
                changeListener = null;
        }

        /** Fires state changed event. Helper method. */
        public final void fireStateChanged() {
            if(changeListener != null)
                changeListener.stateChanged(new ChangeEvent(this));
        }

        /** Gets help context. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public HelpCtx getHelp() {
            return new HelpCtx(getClass());
        }
    } // End of nested class Panel.
    
}
