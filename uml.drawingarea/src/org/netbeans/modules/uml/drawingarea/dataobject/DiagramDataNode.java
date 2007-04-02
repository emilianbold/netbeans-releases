/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.drawingarea.dataobject;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;


public class DiagramDataNode extends DataNode
{
    public static final String IMAGE_ICON_ATTR_NAME = "iconImage";
    
    public static final String ICON_BASE_PATH =
            "org/netbeans/modules/uml/resources/"; // NOI18N
    
    protected static final String DIAGRAM_ICON_DEFAULT =
            ICON_BASE_PATH + "diagramsRootNode.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_ACTIVITY =
            ICON_BASE_PATH + "ActivityDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_CLASS =
            ICON_BASE_PATH + "ClassDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_COLLABORATION =
            ICON_BASE_PATH + "CollaborationDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_COMPONENT =
            ICON_BASE_PATH + "ComponentDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_DEPLOYMENT =
            ICON_BASE_PATH + "DeploymentDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_SEQUENCE =
            ICON_BASE_PATH + "SequenceDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_STATE =
            ICON_BASE_PATH + "StateDiagram.png"; // NOI18N;
    
    public static final String DIAGRAM_ICON_USECASE =
            ICON_BASE_PATH + "UseCaseDiagram.png"; // NOI18N;
    
    
    public DiagramDataNode(DiagramDataObject obj)
    {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(DIAGRAM_ICON_DEFAULT);
        
        IProxyDiagramManager diagramMgr = ProxyDiagramManager.instance();
        
        IProxyDiagram proxyDiagram = diagramMgr.getDiagram(
                FileUtil.getFileDisplayName(obj.getPrimaryFile()));
        setDisplayName(proxyDiagram.getName());
        setValue(IMAGE_ICON_ATTR_NAME, proxyDiagram.getDiagramKind());
    }
    
    
    public Image getIcon(int i)
    {
        String imageFile = DIAGRAM_ICON_DEFAULT;
        int diagramType = IDiagramKind.DK_UNKNOWN;
        
        Object value = getValue(IMAGE_ICON_ATTR_NAME);
        if (value != null)
            diagramType = ((Integer)value).intValue();
        
        switch (diagramType)
        {
            case IDiagramKind.DK_ACTIVITY_DIAGRAM:
                imageFile = DIAGRAM_ICON_ACTIVITY;
                break;

            case IDiagramKind.DK_CLASS_DIAGRAM:
                imageFile = DIAGRAM_ICON_CLASS;
                break;

            case IDiagramKind.DK_COLLABORATION_DIAGRAM:
                imageFile = DIAGRAM_ICON_COLLABORATION;
                break;

            case IDiagramKind.DK_COMPONENT_DIAGRAM:
                imageFile = DIAGRAM_ICON_COMPONENT;
                break;

            case IDiagramKind.DK_DEPLOYMENT_DIAGRAM:
                imageFile = DIAGRAM_ICON_DEPLOYMENT;
                break;

            case IDiagramKind.DK_SEQUENCE_DIAGRAM:
                imageFile = DIAGRAM_ICON_SEQUENCE;
                break;

            case IDiagramKind.DK_STATE_DIAGRAM:
                imageFile = DIAGRAM_ICON_STATE;
                break;

            case IDiagramKind.DK_USECASE_DIAGRAM:
                imageFile = DIAGRAM_ICON_USECASE;
                break;
        }
        
        return Utilities.loadImage(imageFile);
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
