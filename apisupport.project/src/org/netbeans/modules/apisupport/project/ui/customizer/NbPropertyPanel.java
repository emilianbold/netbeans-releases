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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import org.openide.util.HelpCtx;

/**
 * Provides common support for a <em>standard</em> panels in the NetBeans module
 * and suite customizers.
 *
 * @author Martin Krauskopf
 */
abstract class NbPropertyPanel extends JPanel implements
        BasicCustomizer.LazyStorage, PropertyChangeListener, HelpCtx.Provider {

    private Class helpCtxClass;

    /** Property whether <code>this</code> panel is valid. */
    static final String VALID_PROPERTY = "isPanelValid"; // NOI18N
    
    /** Property for error message of this panel. */
    static final String ERROR_MESSAGE_PROPERTY = "errorMessage"; // NOI18N
    
    protected ModuleProperties props;
    
    /** Whether this panel is valid or not. */
    private boolean valid;
    
    /** Error message for this panel (may be null). */
    private String errMessage;
    
    /** Creates new NbPropertyPanel */
    NbPropertyPanel(final ModuleProperties props, final Class helpCtxClass) {
        this.valid = true; // panel is valid by default
        this.props = props;
        initComponents();
        props.addPropertyChangeListener(this);
        this.helpCtxClass = helpCtxClass;
    }
    
    /**
     * This method is called whenever {@link ModuleProperties} are refreshed.
     */
    abstract void refresh();
    
    String getProperty(String key) {
        return props.getProperty(key);
    }
    
    void setProperty(String key, String property) {
        props.setProperty(key, property);
    }
    
    boolean getBooleanProperty(String key) {
        return props.getBooleanProperty(key);
    }
    
    void setBooleanProperty(String key, boolean property) {
        props.setBooleanProperty(key, property);
    }
    
    /**
     * Sets whether panel is valid and fire property change. See {@link
     * #VALID_PROPERTY}
     */
    protected void setValid(boolean valid) {
        if (this.valid != valid) {
            this.valid = valid;
            firePropertyChange(NbPropertyPanel.VALID_PROPERTY, !valid, valid);
        }
    }
    
    /**
     * Gives subclasses a chance to set a warning or an error message after a
     * customizer is loaded/displayed. Just use this method for checking a
     * validity of a panel's data and eventually call {@link
     * #setWarning(String)} or {@link #setErrorMessage(String)}. Default
     * implementation does nothing.
     */
    protected void checkForm() {}

    /**
     * Sets an error message which will be shown in the customizer. Pass
     * <code>null</code> to clear current message (or warning). Also set this
     * panel to be invalid for non-<code>null</code>, nonempty message. Invalid
     * otherwise.
     *
     * @see #setWarning(String)
     */
    protected void setErrorMessage(String message) {
        setWarning(message);
        setValid(null == message || "".equals(message));
    }

    
    /**
     * Sets a warning which will be shown in the customizer. Pass
     * <code>null</code> to clear current warning (or message).
     *
     * @see #setErrorMessage(String)
     */
    protected void setWarning(String message) {
        String newMessage = message == null ? "" : message;
        if (!newMessage.equals(this.errMessage)) {
            String oldMessage = this.errMessage;
            this.errMessage = newMessage;
            firePropertyChange(NbPropertyPanel.ERROR_MESSAGE_PROPERTY, oldMessage, newMessage);
        }
    }
    
    public void store() { /* empty implementation */ }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleProperties.PROPERTIES_REFRESHED == evt.getPropertyName()) {
            refresh();
        }
    }
    
    public void addNotify() {
        super.addNotify();
        firePropertyChange(CustomizerProviderImpl.LAST_SELECTED_PANEL, null, this);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(helpCtxClass);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    abstract static class Single extends NbPropertyPanel {
        Single(final SingleModuleProperties props, final Class helpCtxClass) {
            super(props, helpCtxClass);
        }
        SingleModuleProperties getProperties() {
            return (SingleModuleProperties) props;
        }
    }
    
    abstract static class Suite extends NbPropertyPanel {
        Suite(final SuiteProperties props, final Class helpCtxClass) {
            super(props, helpCtxClass);
        }
        SuiteProperties getProperties() {
            return (SuiteProperties) props;
        }
    }
    
}
