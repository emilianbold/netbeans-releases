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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
public class Logo extends JPanel implements Constants, MouseListener {

    private String url;

    /** Creates a new instance of RecentProjects */
    public Logo( String img, String url ) {
        super( new BorderLayout() );
        Icon image = new ImageIcon(Utilities.loadImage(img, true));
        JLabel label = new JLabel( image );
        label.setBorder( BorderFactory.createEmptyBorder() );
        label.setOpaque( false );
        label.addMouseListener( this );
        setOpaque( false );
        add( label, BorderLayout.CENTER );
        setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        this.url = url;
    }

    public void mouseClicked(MouseEvent e) {
        Utils.showURL( url );
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
