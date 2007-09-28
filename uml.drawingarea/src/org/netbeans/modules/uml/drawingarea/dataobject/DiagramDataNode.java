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

package org.netbeans.modules.uml.drawingarea.dataobject;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;


public class DiagramDataNode extends DataNode
{
    public static final String IMAGE_ICON_ATTR_NAME = "iconImage"; // NOI18N

    
    public DiagramDataNode(DiagramDataObject obj)
    {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(ImageUtil.DIAGRAM_ICON_DEFAULT);
        
        IProxyDiagram proxyDiagram = ProxyDiagramManager.instance().getDiagram(
            FileUtil.getFileDisplayName(obj.getPrimaryFile()));

        setValue(IMAGE_ICON_ATTR_NAME, proxyDiagram.getDiagramKind());
    }
    
    public Image getIcon(int i)
    {
        String imageFile = ImageUtil.DIAGRAM_ICON_DEFAULT;
        int diagramType = IDiagramKind.DK_UNKNOWN;
        
        Object value = getValue(IMAGE_ICON_ATTR_NAME);
        
        if (value != null)
            diagramType = ((Integer)value).intValue();
        
        switch (diagramType)
        {
            case IDiagramKind.DK_ACTIVITY_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_ACTIVITY;
                break;

            case IDiagramKind.DK_CLASS_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_CLASS;
                break;

            case IDiagramKind.DK_COLLABORATION_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_COLLABORATION;
                break;

            case IDiagramKind.DK_COMPONENT_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_COMPONENT;
                break;

            case IDiagramKind.DK_DEPLOYMENT_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_DEPLOYMENT;
                break;

            case IDiagramKind.DK_SEQUENCE_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_SEQUENCE;
                break;

            case IDiagramKind.DK_STATE_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_STATE;
                break;

            case IDiagramKind.DK_USECASE_DIAGRAM:
                imageFile = ImageUtil.DIAGRAM_ICON_USECASE;
                break;
        }
        
        return Utilities.loadImage(ImageUtil.IMAGE_FOLDER + imageFile);
    }
    
    
    @Override
    public javax.swing.Action getPreferredAction()
    {
        return SystemAction.get(OpenAction.class);
    }
    
    @Override
    public Action[] getActions(boolean context)
    {
        Action[] defaultActions = super.getActions(context);
        Action[] diagramActions = new Action[defaultActions.length+2];
        diagramActions[0] = SystemAction.get(org.openide.actions.OpenAction.class);
        diagramActions[1] = SystemAction.get(org.openide.actions.ViewAction.class);
        System.arraycopy(defaultActions, 1, diagramActions, 2, defaultActions.length-1);
        return diagramActions;
    }
}
