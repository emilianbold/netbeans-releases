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

import org.netbeans.modules.xml.multiview.cookies.LinkCookie;
import org.netbeans.modules.xml.multiview.cookies.ErrorLocator;
import org.netbeans.modules.xml.multiview.Error;

import javax.swing.*;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.*;

/**
 * @author mkuchtiak
 */
public abstract class SectionInnerPanel extends javax.swing.JPanel implements LinkCookie, ErrorLocator {
    private SectionView sectionView;

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

    public SectionInnerPanel(SectionView sectionView) {
        this.sectionView = sectionView;
    }

    public synchronized void addFocusListener(FocusListener l) {
        super.addFocusListener(l);
        if (!localFocusListenerInitialized) {
            localFocusListenerInitialized = true;
            final Component[] components = getComponents();
            for (int i = 0; i < components.length; i++) {
                Component component = components[i];
                if (component.isFocusable() && !(component instanceof JLabel)) {
                    component.addFocusListener(localFocusListener);
                }
            }
        }
    }

    public abstract JComponent getErrorComponent(String errorId);

    public abstract void setValue(JComponent source, Object value);

    public void documentChanged(javax.swing.text.JTextComponent source, String value) {
    }

    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }

    public void addModifier(final JTextField tf) {
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            private String orgValue;

            public void focusGained(FocusEvent evt) {
                orgValue = tf.getText();
            }

            public void focusLost(FocusEvent evt) {
                if (!tf.getText().equals(orgValue)) {
                    setValue(tf, tf.getText());
                }
            }
        });
    }

    public void addValidatee(final JTextField tf) {
        tf.getDocument().addDocumentListener(new TextListener(tf));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            private String orgValue;
            private boolean viewIsBuggy;

            public void focusGained(FocusEvent evt) {
                orgValue = tf.getText();
                if (sectionView.getErrorPanel().getError() != null) {
                    viewIsBuggy = true;
                } else {
                    viewIsBuggy = false;
                }
            }

            public void focusLost(FocusEvent evt) {
                org.netbeans.modules.xml.multiview.Error error = sectionView.getErrorPanel().getError();
                if (error != null && error.isEditError() && tf == error.getFocusableComponent()) {
                    if (Error.TYPE_WARNING == error.getSeverityLevel()) {
                        org.openide.DialogDescriptor desc = new RefreshSaveDialog(sectionView.getErrorPanel());
                        Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                        dialog.show();
                        Integer opt = (Integer) desc.getValue();
                        if (opt.equals(RefreshSaveDialog.OPTION_FIX)) {
                            tf.requestFocus();
                        } else if (opt.equals(RefreshSaveDialog.OPTION_REFRESH)) {
                            rollbackValue(tf);
                            sectionView.validateView();
                        } else {
                            setValue(tf, tf.getText());
                            sectionView.validateView();
                        }
                    } else {
                        org.openide.DialogDescriptor desc = new RefreshDialog(sectionView.getErrorPanel());
                        Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                        dialog.show();
                        Integer opt = (Integer) desc.getValue();
                        if (opt.equals(RefreshDialog.OPTION_FIX)) {
                            tf.requestFocus();
                        } else if (opt.equals(RefreshDialog.OPTION_REFRESH)) {
                            rollbackValue(tf);
                            sectionView.validateView();
                        }
                    }
                } else {
                    if (!tf.getText().equals(orgValue)) {
                        setValue(tf, tf.getText());
                        sectionView.validateView();
                    } else {
                        if (viewIsBuggy) {
                            sectionView.validateView();
                        }
                    }
                }
            }
        });
    }

    private class TextListener implements javax.swing.event.DocumentListener {
        private JTextField tf;

        TextListener(JTextField tf) {
            this.tf = tf;
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
                documentChanged(tf, text);
            } catch (javax.swing.text.BadLocationException ex) {
            }
        }
    }

}
