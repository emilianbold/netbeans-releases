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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.WebLink;

/**
 *
 * @author S. Aubrecht
 */
class Blogs extends RSSFeedReaderPanel {

    public Blogs() {
        super( "Blogs", false ); // NOI18N

        add( buildBottomContent(), BorderLayout.SOUTH );
    }

    protected JComponent buildBottomContent() {
        WebLink allBlogs = new WebLink( "AllBlogs", false ); // NOI18N
        BundleSupport.setAccessibilityProperties( allBlogs, "AllBlogs" ); //NOI18N

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        panel.add( allBlogs, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.HORIZONTAL,new Insets(5,5,0,5),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );

        return panel;
    }
}
