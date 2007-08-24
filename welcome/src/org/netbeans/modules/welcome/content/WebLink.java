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

package org.netbeans.modules.welcome.content;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author S. Aubrecht
 */
public class WebLink extends LinkButton {

    private String url;

    /** Creates a new instance of WebLink */
    public WebLink( String key, boolean showBullet ) {
        this( BundleSupport.getLabel( key ), BundleSupport.getURL( key ), showBullet );
    }

    public WebLink( String label, String url, boolean showBullet ) {
        super( label, showBullet );
        this.url = url;
    }

    public void actionPerformed(ActionEvent e) {
        Utils.showURL( url );
    }
    
    @Override
    protected void onMouseExited(MouseEvent e) {
        StatusDisplayer.getDefault().setStatusText( "" );
    }

    @Override
    protected void onMouseEntered(MouseEvent e) {
        StatusDisplayer.getDefault().setStatusText( url );
    }
}

