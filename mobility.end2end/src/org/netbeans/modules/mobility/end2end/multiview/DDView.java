/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.ImageUtilities;
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
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"); //NOI18N
    }
    
    public String preferredID() {
        return DD_MULTIVIEW_PREFIX + name;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage( DDView.class, "TTL_" + name );
    }
}
