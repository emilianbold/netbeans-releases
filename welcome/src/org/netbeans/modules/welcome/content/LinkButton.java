/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */
package org.netbeans.modules.welcome.content;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

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
