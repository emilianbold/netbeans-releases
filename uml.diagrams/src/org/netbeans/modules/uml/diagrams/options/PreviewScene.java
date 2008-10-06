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
package org.netbeans.modules.uml.diagrams.options;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramTypesManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.drawingarea.NodeWidgetFactory;
import org.netbeans.modules.uml.drawingarea.palette.NodeInitializer;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class PreviewScene extends GraphScene<IPresentationElement, IPresentationElement> implements Customizable
{
    private ResourceType[] customizableResTypes = Customizable.DEFAULT_RESTYPES;
    private static Map<String, NodeWidgetFactory> map = new HashMap<String, NodeWidgetFactory>();
    private static IPresentationElement[] elements;
    public static final int[] diagramTypes = new int[]
    {
        IDiagramKind.DK_CLASS_DIAGRAM,
        IDiagramKind.DK_SEQUENCE_DIAGRAM,
        IDiagramKind.DK_ACTIVITY_DIAGRAM,
        IDiagramKind.DK_STATE_DIAGRAM,
        IDiagramKind.DK_USECASE_DIAGRAM,
        
    };
    

    static
    {

        FileSystem system = Repository.getDefault().getDefaultFileSystem();
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();

        if (system != null && factory != null)
        {
            String diagramTypeName;
            List<IPresentationElement> list = new ArrayList<IPresentationElement>();

            String[] path = new String[diagramTypes.length + 1];
            path[0] = "UML/Nodes";

            int index = 1;

            for (int t : diagramTypes)
            {
                diagramTypeName = DiagramTypesManager.instance().getDiagramTypeNameNoSpaces(t);
                path[index++] = "UML/" + diagramTypeName + "/Nodes";
            }

            for (String p : path)
            {
                FileObject fo = system.findResource(p);
                DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
                if (df != null)
                {
                    DataObject[] nodes = df.getChildren();
                    String[] types = new String[nodes.length];
                    for (int i = 0; i < nodes.length; i++)
                    {
                        types[i] = nodes[i].getName();
                        Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
                        if (presentationObj instanceof IPresentationElement)
                        {
                            IPresentationElement pe = ((IPresentationElement) presentationObj);
                            IElement element = (IElement) FactoryRetriever.instance().createType(types[i], null);

                            // todo: should we  use general approach for those element that define
                            // expanded element type?
                            if (element instanceof IStateVertex)
                            {
                                FileObject f = system.findResource(p + "/" + element.getElementType());
                                DataFolder d = f != null ? DataFolder.findFolder(f) : null;
                                if (d != null)
                                {
                                    DataObject[] subs = d.getChildren();
                                    for (DataObject o : subs)
                                    {
                                        Object showPreference = o.getPrimaryFile().getAttribute("showPreference");
                                        if (showPreference instanceof Boolean && ((Boolean)showPreference).booleanValue() == false)
                                            continue;
                                        presentationObj = factory.retrieveMetaType("NodePresentation", null);
                                        pe = ((IPresentationElement) presentationObj);
                                        element = (IElement) FactoryRetriever.instance().createType(types[i], null);
                                        Object init = o.getPrimaryFile().getAttribute("initializer");
                                        if (init instanceof NodeInitializer)
                                        {
                                            ((NodeInitializer) init).initialize(element);
                                        }
                                        pe.addSubject(element);

                                        initConstructors(p + "/" + types[i] + "/" + o.getName(), pe);
                                        if (map.containsKey(element.getExpandedElementType()))
                                        {
                                            list.add(pe);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                pe.addSubject(element);
                                
                                initConstructors(p + "/" + types[i], pe);
                                if (map.containsKey(element.getExpandedElementType()))
                                {
                                    list.add(pe);
                                }
                            }
                        }
                    }
                }
            }
            elements = new IPresentationElement[list.size()];
            list.toArray(elements);

        }
    }

    public PreviewScene()
    {
        // The preview scene needs to use a copy of the resource table.
        initializeResources(ResourceValue.getSystemResourceTable());
    }

    public void initializeResources(ResourceTable table)
    {
        ResourceTable sceneTable = new ResourceTable();
        for(String property : table.getLocalPropertyNames())
        {
            Object value = table.getProperty(property);
            sceneTable.addProperty(property, value);
        }
        
        setResourceTable(sceneTable);
        ResourceValue.initResources(getID(), this);
    }
    
    public static IPresentationElement[] getElements()
    {
        return elements;
    }

    protected Widget attachNodeWidget(IPresentationElement type)
    {
        Widget w = createWidget(type);
        if (w != null)
        {
            w.setPreferredLocation(new Point(50, 50));
            addChild(w);
        }

        return w;
    }

    protected Widget attachEdgeWidget(IPresentationElement edge)
    {
        return null;
    }

    protected void attachEdgeSourceAnchor(IPresentationElement edge, IPresentationElement oldSourceNode, IPresentationElement sourceNode)
    {
    }

    protected void attachEdgeTargetAnchor(IPresentationElement edge, IPresentationElement oldTargetNode, IPresentationElement targetNode)
    {
    }

    private Widget createWidget(IPresentationElement element)
    {
        try
        {
            NodeWidgetFactory c = map.get(element.getFirstSubject().getExpandedElementType());
            if (c == null) 
            {
                c = map.get(element.getFirstSubject().getElementType());
            }
            UMLNodeWidget widget = (UMLNodeWidget) c.createNode(this);
            widget.initializeNode(element, true);
            return widget;

        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }       
    }

    private static void initConstructors(String path, IPresentationElement pe)
    {
        FileSystem system = Repository.getDefault().getDefaultFileSystem();

        if (system != null)
        {
            FileObject fo = system.findResource(path);
            DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
            if (df != null)
            {
                DataObject[] engineObjects = df.getChildren();
                for (int i = 0; i < engineObjects.length; i++)
                {
                    InstanceCookie ic = engineObjects[i].getCookie(org.openide.cookies.InstanceCookie.class);
                    if (ic != null)
                    {
                        try
                        {
                            Class cl = ic.instanceClass();
                            if (cl != null)
                            {
                                //Constructor constructor = cl.getConstructor(Scene.class);
                                if (cl.isAssignableFrom(NodeWidgetFactory.class));
                                {
                                    map.put(pe.getFirstSubject().getExpandedElementType(),(NodeWidgetFactory) cl.getConstructor().newInstance());
                                }
                            }
                        } catch (Exception e)
                        {
                            //Exceptions.printStackTrace(e);
                            e.printStackTrace();
                            continue;
                        }
                    }
                }
            }
        }
    }

    public String getID()
    {
        return DesignerScene.SceneDefaultWidgetID;
    }

    public String getDisplayName()
    {
        return NbBundle.getMessage(PreviewScene.class, "default");
    }

    public void update()
    {
        ResourceValue.initResources(getID(), this);
    }

      public void setCustomizableResourceTypes (ResourceType[] resTypes) 
    {
        customizableResTypes = resTypes;
    }
      
    public ResourceType[] getCustomizableResourceTypes()
    {
        return customizableResTypes;
    }
}
