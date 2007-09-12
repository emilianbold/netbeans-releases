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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xslt.tmap.multiview.source;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xslt.tmap.TMapDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapSourceMultiViewElementDesc
        implements MultiViewDescription, Serializable 
{

    private static final long serialVersionUID = 1L;
    public static final String PREFERED_ID = "tmapsource";    //NOI18N
    private static final String DISPLAY_NAME = 
        "LBL_SourceMultiview_DisplayName";                      //NOI18N
    private TMapDataObject myDataObject;
    
    // need for serialization
    private TMapSourceMultiViewElementDesc() {
        super();
    }
        
    public TMapSourceMultiViewElementDesc( TMapDataObject dataObject ) {
                super();
        myDataObject = dataObject;
    }
    
    public MultiViewElement createElement() {
        if ( myDataObject.getEditorSupport()!=null ) {
            return new TMapSourceMultiViewElement( myDataObject );
        }
        return MultiViewFactory.BLANK_ELEMENT;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), DISPLAY_NAME ); 
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("tmap_editor_source"); //NOI18N
    }
    
    public Image getIcon() {
        return getDataObject().getNodeDelegate().getIcon(
                                BeanInfo.ICON_COLOR_16x16);
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public String preferredID() {
        return PREFERED_ID;
    }
        
    private TMapDataObject getDataObject() {
        return myDataObject;
    }    
}
