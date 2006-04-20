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

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.WebLink;

/**
 *
 * @author S. Aubrecht
 */
public class Blogs extends RSSFeedReaderPanel {
    
    /** Creates a new instance of NbNews */
    public Blogs() {
        super( "Blogs" ); // NOI18N

        setBottomContent( buildBottomContent() );
    }

    protected JComponent buildBottomContent() {
        WebLink allBlogs = new WebLink( "AllBlogs", false ); // NOI18N
        allBlogs.setFont( HEADER_FONT );
        allBlogs.setForeground( HEADER_TEXT_COLOR );

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        panel.add( allBlogs, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL,new Insets(5,5,0,5),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );

        return panel;
    }
}
