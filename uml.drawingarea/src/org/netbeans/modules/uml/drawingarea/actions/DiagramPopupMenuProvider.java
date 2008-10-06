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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class DiagramPopupMenuProvider implements PopupMenuProvider
{
    public static String widgetActionPath = "Editors/modeling/Popup";
    public static String nodeWidgetActionPath = "Editors/modeling/node/Popup";
    public static String connectionWidgetActionPath = "Editors/modeling/connection/Popup";
    public static String sceneWidgetActionPath = "Editors/modeling/x-alldiagrams/Popup";
    public static TreeMap<Integer, Action> commonWidgetActioins = getCommonActions(widgetActionPath);
    public static TreeMap<Integer, Action> commonNodeActioins = getCommonActions(nodeWidgetActionPath);
    public static TreeMap<Integer, Action> commonConnectionActions = getCommonActions(connectionWidgetActionPath);
    public static TreeMap<Integer, Action> commonSceneActions = getCommonActions(sceneWidgetActionPath);

    public JPopupMenu getPopupMenu(Widget widget, Point localLocation)
    {
        Scene scene = widget.getScene();
        WidgetContext context = null;
        if (scene instanceof ObjectScene)
        {
            ObjectScene s = (ObjectScene) scene;
//            if (widget instanceof Scene)
//            {
//                s.setSelectedObjects(new HashSet<Object>());
//            } 
//            else if (!s.getSelectedObjects().contains(s.findObject(widget)))
//            {
//                Set<Object> selected = new HashSet<Object>();
//                selected.add(s.findObject(widget));
//                if (selected.size() > 0)
//                {
//                    s.setSelectedObjects(selected);
//                }
//            }

            Collection<Action> common = null;
            Map<Integer, Action> map = null;
            // the popup menu action was invoked on diagram
            if (s.getSelectedObjects().size() == 0 && widget instanceof DesignerScene)
            {
                // 1. get the common actions defined for all diagrams
                Map<Integer, Action> commonDiagramActions = getCommonWidgetActions(widget);
                
                // 2. get the specific actions for this type of diagram
                String menuPath = "Editors/modeling/x-" + ((DesignerScene) widget).getDiagram().getDiagramKindAsString().toLowerCase().replaceAll(" ", "") + "/Popup";
                map = getActions(menuPath, context);
                map.putAll(commonDiagramActions);
            }
            // the popup menu action was invoked on one of the selected widgets
            else
            {
                //handle null locallocation (case of popup invocation via key)
                if(localLocation==null)
                {
                    //get left-top point of the widget with small shift about 5px or half of width/height
                   Rectangle bnd=widget.getBounds();
                   int x=Math.min(bnd.x+5, bnd.x+bnd.width/2);
                   int y=Math.min(bnd.y+5, bnd.y+bnd.height/2);
                   //may it have sense to consider always get center point?
                   localLocation=new Point(x,y);
                }
                //
                for (Object e : s.getSelectedObjects())
                {
                    if (e instanceof IPresentationElement)
                    {
                        // 1. get the actions defined for this type of element
                        context = getWidgetContext(widget, localLocation);
                        String menuPath = "Editors/modeling/x-" + ((IPresentationElement) e).getFirstSubject().getExpandedElementType().toLowerCase() + "-element/Popup";
                        Map<Integer, Action> actions = getActions(menuPath, context);
                        if (actions.size() == 0)
                        {
                            menuPath = "Editors/modeling/x-" + ((IPresentationElement) e).getFirstSubjectsType().toLowerCase() + "-element/Popup";
                            actions = getActions(menuPath, context);
                        }
//                        if (actions.size() ==0)
//                        {
//                            menuPath = "Editors/modeling/x-" +  ((IPresentationElement) e).getFirstSubject().getOwner().getElementType().toLowerCase() +  "-element/Popup";
//                            actions = getActions(menuPath, context);                         
//                        }

                        // 2. get all common actions defined for either node or connection, e.g. cut/copy/paste/properties etc
                        if (!isOverriding(menuPath))    
                            actions.putAll(getCommonWidgetActions(s.findWidget(e)));
                        
                        // 3. get the actions that apply to all selected widgets
                        Collection<Action> c = actions.values();
                        if (common == null)
                        {
                            common = c;
                            map = actions;
                        }                       
                        common.retainAll(c);
                    }
                }
            }

            TreeMap<Integer, Action> actions = new TreeMap<Integer, Action>();
            if (map != null)
            {
                actions.putAll(map);
            }
            Action[] actionArray = new Action[actions.size()];
            int i = 0;
            for (Action a : actions.values())
            {
                actionArray[i++] = a;
            }

            Lookup lookup;
            if (s.getSelectedObjects().size() == 1 && context != null)
            {

                lookup = new ProxyLookup(Utilities.actionsGlobalContext(), 
                                         Lookups.fixed(context.getContextItems()), 
                                         Lookups.singleton(context),
                                         widget.getLookup());
            } else
            {
                //lookup = Utilities.actionsGlobalContext();
                lookup = new ProxyLookup(Utilities.actionsGlobalContext(),
                                         widget.getLookup());
            }

            return Utilities.actionsToPopup(actionArray, lookup);
        }
        return null;
    }

    private Map<Integer, Action> getActions(String path, WidgetContext context)
    {

        TreeMap<Integer, Action> actions = new TreeMap<Integer, Action>();
        FileSystem system = Repository.getDefault().getDefaultFileSystem();


        FileObject fo = system.findResource(path);
        if (fo == null)
        {
            return actions;
        }
        DataFolder folder = DataFolder.findFolder(fo);


        String contextName = null;
        if (context != null)
        {
            contextName = context.getContextName();
        }

        if (folder != null)
        {
            for (DataObject child : folder.getChildren())
            {
                List<String> contextValues = getContextValues(child);

                boolean isCorrectContext = false;
                if ((contextName != null) && (contextValues.size() > 0))
                {
                    isCorrectContext = contextValues.contains(contextName);
                } else if (((contextName == null) && (contextValues.size() == 0)) || ((contextName != null) && (contextValues.size() == 0)))
                {
                    isCorrectContext = true;
                }

                if (isCorrectContext == true)
                {
                    if (child.getPrimaryFile().isFolder() == true)
                    {
                        int p = getActionPosition(child);
                        if (p == 0)
                        {
                            if (actions.size() > 0)
                                p = actions.lastKey() + 1;
                        }
                        actions.put(p, buildSubMenu((DataFolder) child));
                    } else
                    {

                        InstanceCookie cookie = child.getLookup().lookup(InstanceCookie.class);
                        if (cookie != null)
                        {
                            try
                            {
                                Class clazz = cookie.instanceClass();

                                Action action = null;

                                if (JSeparator.class.isAssignableFrom(clazz) != true)
                                {
                                    action = SystemAction.get(clazz);
                                }
                                int p = getActionPosition(child);
                                if (p == 0)
                                {
                                    if (actions.size() > 0)
                                    {
                                        p = actions.lastKey() + 1;
                                    }
                                }
                                actions.put(p, action);
                            } catch (IOException e)
                            {
                            } catch (ClassNotFoundException e)
                            {
                            }
                        }
                    }
                }
            }
        }
        return actions;
    }

    
    private boolean isOverriding(String path)
    {
        FileSystem system = Repository.getDefault().getDefaultFileSystem();

        FileObject fo = system.findResource(path);
        if (fo == null)
        {
            return false;
        }
        DataFolder folder = DataFolder.findFolder(fo);

        Object override = folder.getPrimaryFile().getAttribute("override");
        return override instanceof Boolean? ((Boolean)override).booleanValue() : false;
    }

    
    
    public static DataFolder getDataFolder(String path) throws DataObjectNotFoundException
    {
        DataFolder retVal = null;

        FileSystem system = Repository.getDefault().getDefaultFileSystem();

        if (system != null)
        {
            FileObject fo = system.findResource(path);
            retVal = fo != null ? DataFolder.findFolder(fo) : null;
        }

        return retVal;
    }

    protected static Action buildSubMenu(DataFolder folder)
    {
        SubMenuAction retVal = null;

        if (folder != null)
        {
            retVal = new SubMenuAction(folder.getName());

            for (DataObject child : folder.getChildren())
            {
                if (child.getPrimaryFile().isFolder() == true)
                {
                    retVal.addAction(buildSubMenu((DataFolder) child));
                } else
                {
                    InstanceCookie cookie = child.getLookup().lookup(InstanceCookie.class);
                    if (cookie != null)
                    {
                        try
                        {
                            Class clazz = cookie.instanceClass();
                            if (Action.class.isAssignableFrom(clazz) == true)
                            {
                                retVal.addAction((Action) cookie.instanceCreate());
                            } else if (JSeparator.class.isAssignableFrom(clazz) == true)
                            {
                                retVal.addAction(null);
                            } else if (JMenuItem.class.isAssignableFrom(clazz) == true)
                            {
                                JMenuItem item = (JMenuItem) cookie.instanceCreate();
                                if (item.getAction() != null)
                                {
                                    retVal.addAction(item.getAction());
                                }
                            }
                        } catch (IOException e)
                        {
                        } catch (ClassNotFoundException e)
                        {
                        }
                    }
                }
            }
        }

        return retVal;
    }

    private List<String> getContextValues(DataObject child)
    {
        List<String> retVal = new ArrayList<String>();

        if (child != null)
        {
            String value = (String) child.getPrimaryFile().getAttribute("context");
            if ((value != null) && (value.length() > 0))
            {
                String[] values = value.split(",");
                for (String curValue : values)
                {
                    retVal.add(curValue.trim());
                }
            }
        }

        return retVal;
    }

    private WidgetContext getWidgetContext(Widget widget, Point localLocation)
    {
        WidgetContext retVal = null;

        Widget targetWidget = Util.findChildWidgetAt(widget, localLocation);
        while ((targetWidget != null) && (retVal == null))
        {
            if ((targetWidget != null) && (targetWidget.getLookup() != null))
            {
                retVal = targetWidget.getLookup().lookup(WidgetContext.class);
            }

            if (targetWidget != widget)
            {
                targetWidget = targetWidget.getParentWidget();
            } else
            {
                break;
            }
        }

        if ((retVal == null) && (widget.getLookup() != null))
        {
            WidgetContextFactory factory = widget.getLookup().lookup(WidgetContextFactory.class);
            if (factory != null)
            {
                retVal = factory.findWidgetContext(localLocation);
            }
        }

        return retVal;
    }

    private TreeMap<Integer, Action> getCommonWidgetActions(Widget w)
    {
        if (w instanceof UMLNodeWidget)
        {
            return commonNodeActioins;
        } else if (w instanceof ConnectionWidget)
        {
            return commonConnectionActions;
        } else if (w instanceof Scene)
        {
            return commonSceneActions;
        }      
        return commonWidgetActioins;
    }

    private static TreeMap<Integer, Action> getCommonActions(String path)
    {
        TreeMap<Integer, Action> ret = new TreeMap<Integer, Action>();
        try
        {
            DataFolder folder = getDataFolder(path);
            if (folder != null)
            {
                for (DataObject child : folder.getChildren())
                {
                    int p = getActionPosition(child);
                    if (p == 0)
                    {
                        if (ret.size() > 0)
                        {
                            p = ret.lastKey() + 1;
                        }
                    }
                    if (child.getPrimaryFile().isFolder() == true)
                    {
                        ret.put(p, buildSubMenu((DataFolder) child));
                    } else
                    {
                        InstanceCookie cookie = child.getLookup().lookup(InstanceCookie.class);
                        if (cookie != null)
                        {
                            Class clazz = cookie.instanceClass();

                            if (clazz != null)
                            {
                                Action a = null;
                                if (!JSeparator.class.isAssignableFrom(clazz))
                                {
                                    a = SystemAction.get(clazz);
                                }
                                ret.put(p, a);
                            }
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    private static Integer getActionPosition(DataObject obj)
    {
        Object p = obj.getPrimaryFile().getAttribute("position");
        if (p instanceof Integer)
        {
            return (Integer) p;
        }
        return new Integer(0);
    }
}