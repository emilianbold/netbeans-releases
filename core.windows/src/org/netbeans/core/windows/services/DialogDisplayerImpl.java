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


package org.netbeans.core.windows.services;


import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;

import java.awt.*;


// Extracted from core/NbTopManager.
/**
 * Implementation of <code>org.openide.DialogDisplayer</code>.
 *
 * @author  Jesse Glick
 */
public class DialogDisplayerImpl extends DialogDisplayer {
    /** non-null if we are running in unit test and should no show any dialogs */
    private Object testResult;
    
    /** Creates a new instance of DialogDisplayerImpl */
    public DialogDisplayerImpl() {
        this (null);
    }
    
    DialogDisplayerImpl (Object testResult) {
        this.testResult = testResult;
    }

    /** Creates new dialog. */
    public Dialog createDialog (final DialogDescriptor d) {
        return (Dialog)Mutex.EVENT.readAccess (new Mutex.Action () {
            public Object run () {
                // if a modal dialog active use it as parent
                // otherwise use the main window
                if (NbPresenter.currentModalDialog != null) {
                    if (NbPresenter.currentModalDialog.isLeaf ()) {
                        return new NbDialog(d, WindowManager.getDefault ().getMainWindow ());
                    } else {
                        return new NbDialog(d, NbPresenter.currentModalDialog);
                    }
                }
                else {
                    Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();
                    if (w instanceof NbPresenter && ((NbPresenter) w).isLeaf ()) {
                        w = WindowManager.getDefault ().getMainWindow ();
                    }
                    if (w instanceof Dialog) {
                        NbDialog dlg = new NbDialog(d, (Dialog) w);
                        dlg.requestFocusInWindow ();
                        return dlg;
                    } else {
                        Frame f = w instanceof Frame ? (Frame) w : WindowManager.getDefault ().getMainWindow ();
                        NbDialog dlg = new NbDialog(d, f);
                        dlg.requestFocusInWindow ();
                        return dlg;
                    }
                }
            }
        });
    }
    
    /** Notifies user by a dialog.
     * @param descriptor description that contains needed informations
     * @return the option that has been choosen in the notification. */
    public Object notify (final NotifyDescriptor descriptor) {
        class AWTQuery implements Runnable {
            public Object result;
            public boolean running;
        
            public void run () {
                synchronized (this) {
                    notify ();   
                    running = true;
                }
                
                showDialog ();

                synchronized (this) {
                    this.result = descriptor.getValue();
                    notifyAll ();
                }
            }
            
            public void showDialog () {
                if (testResult != null) {
                    // running in Unit test
                    descriptor.setValue (testResult);
                    return;
                }
                
                Component focusOwner = null;
                Component comp = org.openide.windows.TopComponent.getRegistry ().getActivated ();
                Component win = comp;
                while ((win != null) && (!(win instanceof Window))) win = win.getParent ();
                if (win != null) focusOwner = ((Window)win).getFocusOwner ();

                // if a modal dialog is active use it as parent
                // otherwise use the main window

                NbPresenter presenter = null;
                if (descriptor instanceof DialogDescriptor) {
                    if (NbPresenter.currentModalDialog != null) {
                        if (NbPresenter.currentModalDialog.isLeaf ()) {
                            presenter = new NbDialog((DialogDescriptor) descriptor, WindowManager.getDefault ().getMainWindow ());
                        } else {
                            presenter = new NbDialog((DialogDescriptor) descriptor, NbPresenter.currentModalDialog);
                        }
                    } else {
                        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();
                        if (w instanceof NbPresenter && ((NbPresenter) w).isLeaf ()) {
                            w = WindowManager.getDefault ().getMainWindow ();
                        }
                        Frame f = w instanceof Frame ? (Frame) w : WindowManager.getDefault().getMainWindow();
                        presenter = new NbDialog((DialogDescriptor) descriptor, f);
                    }
                } else {
                    if (NbPresenter.currentModalDialog != null) {
                        presenter = new NbPresenter(descriptor, NbPresenter.currentModalDialog, true);
                    } else {
                        Frame f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                            instanceof Frame ? 
                            (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                            : WindowManager.getDefault().getMainWindow();
                        presenter = new NbPresenter(descriptor, f, true);
                    }
                }
                
                //#47150 - horrible hack for vcs module
                if ("true".equals(System.getProperty("javahelp.ignore.modality"))) { //NOI18N
                    presenter.getRootPane().putClientProperty ("javahelp.ignore.modality", "true"); //NOI18N
                    System.setProperty("javahelp.ignore.modality", "false"); //NOI18N
                }

                //Bugfix #8551
                presenter.getRootPane().requestDefaultFocus();
                presenter.setVisible(true);

                // dialog is gone, restore the focus

                if (focusOwner != null) {
                    win.requestFocus ();
                    comp.requestFocus ();
                    focusOwner.requestFocus ();
                }
            }
        }
        
        AWTQuery query = new AWTQuery ();
        
        if (javax.swing.SwingUtilities.isEventDispatchThread ()) {
            query.showDialog ();
            return descriptor.getValue ();
        }
        
        synchronized (query) {
            javax.swing.SwingUtilities.invokeLater (query);
            try {
                query.wait (10000);
            } catch (InterruptedException ex) {
                // ok, should not happen and does not matter
            }
            
            if (query.running) {
                while (query.result == null) {
                    try {
                        query.wait ();
                    } catch (InterruptedException ex) {
                        // one more round
                    }
                }
                return query.result;
            } else {
                return NotifyDescriptor.CLOSED_OPTION;
            }
        }
    }

}
