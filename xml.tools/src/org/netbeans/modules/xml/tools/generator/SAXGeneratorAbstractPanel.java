/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.util.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.HelpCtx;
import java.net.URL;

/**
 * Base class of wizardable customizer panels. <code>updateModel</code>
 * and <code>initView</code> methods need to be implemented. They are called as user goes 
 * over wizard steps and it must (re)store current state.
 *
 * @author  Petr Kuzel
 * @version 
 */
public abstract class SAXGeneratorAbstractPanel extends JPanel implements Customizer {

    /** Serial Version UID */
    private static final long serialVersionUID =5089896677680825691L;
    
    // associated wizard step or null
    private WizardStep step;

    /**
     * After a setObject() call contains current model driving wizard.
     */
    protected SAXGeneratorModel model;
    
    /** Creates new SAXGeneratorAbstractPanel */
    public SAXGeneratorAbstractPanel() {
    }

    public static final class WizardStep implements WizardDescriptor.Panel {

        private SAXGeneratorAbstractPanel peer;
        private Class peerClass;
        private Object bean;
        private Integer index;
        
        private Vector listeners = new Vector(); 
        private final ChangeEvent EVENT = new ChangeEvent(this);
        private boolean valid = true;
        
        /**
         * Create wizard step that uses instance of passed class as its component.
         */
        public WizardStep(Class peerClass) {
            if (SAXGeneratorAbstractPanel.class.isAssignableFrom(peerClass) == false) {
                throw new IllegalArgumentException("SAXGeneratorAbstractPanel required. Got " + peerClass);
            }
            this.peerClass = peerClass;
        }
        
        public java.awt.Component getComponent() {
            return getPeer();
        }
        
        private SAXGeneratorAbstractPanel getPeer() {
            if (peer == null) {
                try {
                    // unfortunately constructor does not initialize this
                    // object properly, client need to call setIndex and setBean
                    if (bean == null) throw new IllegalStateException();
                    if (index == null) throw new IllegalStateException();
                    peer = (SAXGeneratorAbstractPanel) peerClass.newInstance();
                    peer.step = this;
                    peer.setObject(bean);
                    peer.putClientProperty("WizardPanel_contentSelectedIndex", index);  // NOI18N
                } catch (InstantiationException ex) {
                    throw new IllegalStateException();
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException();
                }
            }
            return peer;
        }

        void setBean(Object bean) {
            this.bean = bean;
        }
        
        void setIndex(int index) {
            this.index = new Integer(index);
        }
        
        public void readSettings(java.lang.Object p1) {
            getPeer().updateView();
        }
        
        /**
         * Cunstruct help ctx from WizardPanel_helpURL property.
         */
        public HelpCtx getHelp() {
            //return new HelpCtx(getPeer().getClass());
            return null;
        }

        public void addChangeListener(javax.swing.event.ChangeListener l) {
            listeners.add(l);
        }

        public void storeSettings(java.lang.Object p1) {
            getPeer().updateModel();
        }

        public boolean isValid() {
            return valid;
        }

        void setValid(boolean valid) {

            if (this.valid == valid) return;

            this.valid = valid;

            synchronized (listeners) {
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    ChangeListener next = (ChangeListener) it.next();
                    next.stateChanged(EVENT);
                }
            }
        }

        public void removeChangeListener(javax.swing.event.ChangeListener l) {
            listeners.remove(l);
        }
    }
        

    /**
     * Update validity of associted wizard step or void.
     */
    protected final void setValid(boolean valid) {
        if (step != null) step.setValid(valid);
    }

    /**
     * User just leaved the panel, update model
     */
    protected abstract void updateModel();
    
    /**
     * User just entered the panel, init view by model values
     */
    protected abstract void initView();
    
    /**
     * User just reentered the panel.
     */
    protected abstract void updateView();
    
    
    public void setObject(java.lang.Object peer) {
        if ( not(peer instanceof SAXGeneratorModel) ) {
            throw new IllegalArgumentException("SAXGeneratorModel class expected.");  // NOI18N
        }        
        
        model = (SAXGeneratorModel) peer;
        initView();
    }    
        
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }

    protected final boolean not (boolean expr) {
        return ! expr;
    }
    
}
