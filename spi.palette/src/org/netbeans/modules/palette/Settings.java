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

package org.netbeans.modules.palette;

import java.beans.PropertyChangeListener;



/**
 * Palette settings to be remembered over IDE restarts.
 * There's an instance of these settings for each palette model instance.
 *
 * @author S. Aubrecht
 */
public interface Settings {
    
    void addPropertyChangeListener( PropertyChangeListener l );
    
    void removePropertyChangeListener( PropertyChangeListener l );
    
    boolean isVisible( Item item );
    
    void setVisible( Item item, boolean visible );

    boolean isVisible( Category category );
    
    void setVisible( Category category, boolean visible );
    
    boolean isExpanded( Category category );
    
    void setExpanded( Category category, boolean expanded );
    
    void setShowItemNames( boolean showNames );

    boolean getShowItemNames();

    void setIconSize( int iconSize );

    int getIconSize();
    
    void reset();
}
