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

package org.netbeans.modules.project.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/** Basic action. Serves as a base class for all projects specific 
 * actions.
 * 
 * @author Pet Hrebejk 
 */
abstract class BasicAction extends AbstractAction {
    
    protected BasicAction() {}
    
    protected BasicAction( String displayName, Icon icon ) {
        if ( displayName != null ) {
            setDisplayName( displayName );
        }
        if ( icon != null ) {
            setSmallIcon( icon );
        }
    }
    
    protected final void setDisplayName( String name ) {
        putValue( NAME, name );
    }
    
    protected final void setSmallIcon( Icon icon ) {
        if ( icon != null ) {
            putValue( SMALL_ICON, icon );
        }
    }
    
    protected final void setSmallIcon( String iconResource ) {
        if ( iconResource != null ) {
            putValue( SMALL_ICON, new ImageIcon( Utilities.loadImage( iconResource ) ) );
        }
    }
        
}