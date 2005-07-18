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

package org.netbeans.spi.editor.errorstripe;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.editor.errorstripe.apimodule.SPIAccessor;

/**
 *
 * @author Jan Lahoda
 */
/*package private*/ class SPIAccessorImpl extends SPIAccessor {
    
    /** Creates a new instance of SPIAccessorImpl */
    public SPIAccessorImpl() {
    }

    public void removePropertyChangeListener(UpToDateStatusProvider provider, PropertyChangeListener l) {
        provider.removePropertyChangeListener(l);
    }
    
    public void addPropertyChangeListener(UpToDateStatusProvider provider, PropertyChangeListener l) {
        provider.addPropertyChangeListener(l);
    }
    
}
