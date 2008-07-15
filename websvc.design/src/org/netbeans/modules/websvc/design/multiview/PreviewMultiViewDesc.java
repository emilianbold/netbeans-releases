/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.design.multiview;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Pospisil
 */
public class PreviewMultiViewDesc extends Object implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = 1L;
    public static final String PREFERRED_ID = "webservice-wsdlpreview";
    private DataObject dataObject;

    public PreviewMultiViewDesc() {
    }

    /**
     *
     *
     * @param mvSupport 
     */
    public PreviewMultiViewDesc(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(PreviewMultiViewDesc.class,
                "LBL_wsdlPreview_name");
    }

    public Image getIcon() {
        if(dataObject!=null)
            return dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        else {
            Image fault = ImageUtilities.loadImage("org/netbeans/modules/websvc/design/view/resources/fault.png", false);
            //Image fault = ImageUtilities.loadImage("org/netbeans/modules/websvc/core/webservices/uiresources//error-badge.gif", false);
            return fault;
        }
            
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PreviewMultiViewDesc.class);
    }

    public MultiViewElement createElement() {
        if (dataObject == null) {
            return MultiViewFactory.BLANK_ELEMENT;
        } else {
            return new PreviewMultiViewElement(dataObject.getLookup().lookup(DataEditorSupport.class));
        }
    }
}
