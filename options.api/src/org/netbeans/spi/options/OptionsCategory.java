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

package org.netbeans.spi.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;


/**
 * Implementation of this class represents one category (like "Fonts & Colors" 
 * or "Editor") in Options Dialog. It should be registerred in layers:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;file name="FooOptionsPanel.instance"&gt;
 *         &lt;attr name="instanceClass" stringvalue="org.foo.FooOptionsPanel"/&gt;
 *     &lt;/file&gt;
 * &lt;/folder&gt;</pre>
 *
 * @author Jan Jancura
 */
public abstract class OptionsCategory {
    
    /**
     * Returns base name of 32x32 icon used in list on the left side of 
     * Options Dialog.
     *
     * @return base name of 32x32 icon
     */
    public abstract String getIconBase ();
    
    /**
     * Returns name of category used in list on the left side of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getCategoryName ();
    
    /**
     * This text will be used in title component on the top of Options Dialog
     * when your panel will be selected.
     *
     * @return title of this panel
     */
    public abstract String getTitle ();
    
    /**
     * Returns {@link PanelController} for this category. PanelController 
     * creates visual component to be used inside of the Options Dialog.
     *
     * @return new instance of PanelController for this options category
     */
    public abstract PanelController create ();
    
    /**
     * PanelController creates visual representation of one Options Dialog
     * category, and manages communication between Options Dialog and this
     * panel.
     */
    public abstract static class PanelController {
        
        /** Property name constant. */
        public static final String PROP_VALID = "valid";
        /** Property name constant. */
        public static final String PROP_CHANGED = "changed";
        /** Property name constant. */
        public static final String PROP_HELP_CTX = "helpCtx";
        
        
        /**
         * This method is called when Options Dialog "OK" button is pressed.
         */
        public abstract void applyChanges ();
        
        /**
         * This method is called when Options Dialog "Cancel" button is pressed.
         */
	public abstract void cancel ();     
        
        /**
         * Should returns <code>true</code> if some option value in this 
         * category is not valid.
         *
         * @return <code>true</code> if some option value in this 
         * category is not valid
         */
        public abstract boolean isValid ();
        
        /**
         * Should returns <code>true</code> if some option value in this 
         * category has been changed.
         *
         * @return <code>true</code> if some option value in this 
         * category has been changed
         */
        public abstract boolean isChanged ();
        
        /**
         * Returns visual component representing this options category.
         *
         * @return visual component representing this options category
         */
        public abstract JComponent getComponent ();

        /** 
         * Get current help context asociated with this panel.
         *
         * @return current help context
         */
        public abstract HelpCtx getHelpCtx ();
        
        /**
         * Registers new listener.
         *
         * @param l a new listener
         */
        public abstract void addPropertyChangeListener (PropertyChangeListener l);
        
        /**
         * Unregisters given listener.
         *
         * @param l a listener to be removed
         */
        public abstract void removePropertyChangeListener (PropertyChangeListener l);
    }
}
