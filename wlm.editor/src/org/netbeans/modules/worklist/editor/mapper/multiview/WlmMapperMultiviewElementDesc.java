/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.multiview;

import java.awt.Image;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.worklist.dataloader.WlmEditorConstants;
import org.netbeans.modules.worklist.dataloader.WorklistDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Nikita Krjukov
 * @version 1.0
 */
public class WlmMapperMultiviewElementDesc implements MultiViewDescription, 
        Serializable
{

    private static final long serialVersionUID = 1L;   
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    public static final String GROUP_ID = "wlm_mapper_tcgroup";  //NOI18N

    private WorklistDataObject myDataObject;

    public WlmMapperMultiviewElementDesc(WorklistDataObject dObj) {
        myDataObject = dObj;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WlmMapperMultiviewElementDesc.class,
                "LBL_WlmMapperMultiview_DisplayName"); // NOI18N
    }

    public Image getIcon() {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/worklist/editor/worklist.gif"); //NOI18N
    }


    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public String preferredID() {
        return WlmEditorConstants.WLM_MAPPERMV_PREFFERED_ID;
    }

    public MultiViewElement createElement() {
        return new WlmMapperMultiviewElement(myDataObject);
//        return MultiViewFactory.BLANK_ELEMENT;
    }
}
