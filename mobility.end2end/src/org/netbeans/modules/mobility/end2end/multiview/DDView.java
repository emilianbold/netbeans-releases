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

/*
 * DDView.java
 *
 * Created on August 26, 2005, 11:00 AM
 *
 */
package org.netbeans.modules.mobility.end2end.multiview;

import java.io.Serializable;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author suchys
 */
public class DDView extends DesignMultiViewDesc implements Serializable {
    
    static final long serialVersionUID = -1;
    
    public static final String DD_MULTIVIEW_PREFIX  = "dd_multiview"; // NOI18N
    public static final String MULTIVIEW_CLIENT     = "ClientConfig"; // NOI18N
    public static final String MULTIVIEW_SERVER     = "ServerConfig"; // NOI18N
    
    final private String name;
    
    public DDView( E2EDataObject dataObject, String name ) {
        super( dataObject, name );
        this.name = name;
    }
    
    public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
        final E2EDataObject dataObject = (E2EDataObject)getDataObject();
        if( name.equals( MULTIVIEW_CLIENT )) {
            return new ClientMultiViewElement( dataObject, 0 );
        } else if( name.equals( MULTIVIEW_SERVER )) {
            return new ServerMultiViewElement( dataObject, 1 );
        }
        
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        if( name.equals( MULTIVIEW_CLIENT )) {
            // FIXME: devel hack
            return new HelpCtx( "overviewNode" ); //NOI18N
        } else if( name.equals( MULTIVIEW_SERVER )) {
            // FIXME: devel hack
            return new HelpCtx( "overviewNode" ); //NOI18N
        }
        
        return null;
    }
    
    public java.awt.Image getIcon() {
        return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"); //NOI18N
    }
    
    public String preferredID() {
        return DD_MULTIVIEW_PREFIX + name;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage( DDView.class, "TTL_" + name );
    }
}
