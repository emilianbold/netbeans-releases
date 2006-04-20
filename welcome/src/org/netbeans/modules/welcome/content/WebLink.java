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

/**
 *
 * @author S. Aubrecht
 */
public class WebLink extends HtmlTextLinkButton {

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
}
