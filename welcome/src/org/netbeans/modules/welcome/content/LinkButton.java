/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.welcome.content;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;

public class LinkButton extends HtmlTextLinkButton {

    private Action action;

    public LinkButton( Action a, boolean showBullet ) {
        this( a.getValue( Action.NAME ).toString(), showBullet );
        this.action = a;
        Object icon = a.getValue( Action.SMALL_ICON );
        if( null != icon && icon instanceof Icon )
            setIcon( (Icon)icon );
        Object tooltip = a.getValue( Action.SHORT_DESCRIPTION );
        if( null != tooltip )
            setToolTipText( tooltip.toString() );
    }

    public LinkButton( String label, boolean showBullet ) {
        super( label, showBullet );
    }

    public void actionPerformed(ActionEvent e) {
        if( null != action ) {
            action.actionPerformed( e );
        }
    }

    private static final long serialVersionUID = 1L; 
}
