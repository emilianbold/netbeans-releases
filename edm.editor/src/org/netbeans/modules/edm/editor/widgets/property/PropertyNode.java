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
package org.netbeans.modules.edm.editor.widgets.property;

import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.openide.util.NbBundle;


public class PropertyNode extends AbstractNode {

    private MashupDataObject mObj;
    private static final Logger mLogger = Logger.getLogger(GroupByNode.class.getName());

    public PropertyNode(MashupDataObject dObj) {
        super(Children.LEAF);
        mObj = dObj;       
    }

    public PropertyNode() {
        super(Children.LEAF);  
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    /** Creates a property sheet. */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        try {
            PropertySupport.Reflection nameProp = new PropertySupport.Reflection(this.mObj, String.class, "getName", null);
            //TBD : i18n required for this code-section.
            nameProp.setName(NbBundle.getMessage(PropertyNode.class, "LBL_Collaboration_Name"));
            nameProp.setShortDescription(NbBundle.getMessage(PropertyNode.class, "TOOLTIP_Name_of_the_Collaboration"));
            set.put(nameProp);
      
          PropertySupport.Reflection responseTypeProp = new PropertySupport.Reflection(this.mObj.getModel().getEDMDefinition().getSQLDefinition(), String.class, "getResponseType", "setResponseType");
            responseTypeProp.setName(NbBundle.getMessage(PropertyNode.class, "LBL_Response_Type"));
            responseTypeProp.setShortDescription(NbBundle.getMessage(PropertyNode.class, "TOOLTIP_Response_Type"));
            responseTypeProp.setPropertyEditorClass(PropertyEditorManager.getPropertyEditor("RESPONSE_TYPE"));
            set.put(responseTypeProp);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        sheet.put(set);
        return sheet;
    }

    public MashupDataObject getMashupDataObject() {
        return mObj;
    }
}