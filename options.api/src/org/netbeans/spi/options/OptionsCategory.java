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


/**
 *
 * @author Administrator
 */
public abstract class OptionsCategory {
    
    public abstract String getIcon ();
    public abstract String getCategoryName ();
    public abstract String getTitle ();
    public abstract JComponent getPane ();
    
    public interface Panel {
        public static final String PROP_VALID = "valid";
        public static final String PROP_CHANGED = "changed";
        
        public abstract void applyChanges ();
	public abstract void cancel ();     
        public abstract boolean isValid ();
        public abstract boolean isChanged ();
        
        public abstract void addPropertyChangeListener (PropertyChangeListener l);
        public abstract void removePropertyChangeListener (PropertyChangeListener l);
    }
}
