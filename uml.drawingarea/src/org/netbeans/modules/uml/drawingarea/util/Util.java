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
package org.netbeans.modules.uml.drawingarea.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.support.ModelElementBridge;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Jyothi
 */
public class Util 
{
    public static void buildActionMap(InputMap input, 
                                      ActionMap actions,
                                      String filesystem)
    {
        FileSystem system = Repository.getDefault().getDefaultFileSystem();
        
        if (system != null)
        {
            FileObject fo = system.findResource(filesystem);
            DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
            if (df != null)
            {
                DataObject[] actionsDataObjects = df.getChildren();
                for (DataObject dObj : actionsDataObjects)
                {
                    InstanceCookie ic = dObj.getCookie(org.openide.cookies.InstanceCookie.class);
                }
            }
        }
    }
    
    public static Widget findChildWidgetAt(Widget parent, Point location)
    {
        Widget retVal = null;

        Rectangle bounds = parent.getBounds();
        if (bounds.contains(location))//TBD should be isHit there also (bounds is approhimation for complex  objects)
        {

            List<Widget> children = parent.getChildren();
            for (Widget child : children)
            {
                if (child.isVisible() == true)
                {
                    Point childLoc = child.getLocation();
                    Point testPoint = new Point(location.x - childLoc.x,
                                                location.y - childLoc.y);
                    retVal = findChildWidgetAt(child, testPoint);
                    if(retVal != null)
                    {
                        break;
                    }
                }
            }
            
            if(retVal == null)
            {
                retVal = parent;
            }
        }
        return retVal;
    }
    
    public static Object findChildObject(Widget parent, Point location)
    {
        Widget w = findChildWidgetAt(parent, location);
        
        Object retVal = null;
        
        if((w != null) && (w.getScene() instanceof ObjectScene))
        {
            ObjectScene scene = (ObjectScene) w.getScene();
            retVal = scene.findObject(w);
        }
        
        return retVal;
    }
    
    /**
     * Creates a new presentation and associates the presentation element to the 
     * the model element.
     * 
     * @param element The model element.
     * @return The presentation element.
     */
    public static IPresentationElement createNodePresentationElement()
    {
        IPresentationElement retVal = null;
        
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if(factory != null)
        {
           Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;    
           }
        }
        
        return retVal;
    }
    
    /** 
     * Retreives the node that owns the widget. 
     * 
     * @param child The reference widget
     * @return The node that owns the child widget.
     */
    public static Widget getParentNodeWidget(Widget child)
    {
        Widget retVal = null;
        
        // The method ObjectScene.findObject will traverse the parents to find
        // the first parent that is associated with data.
        //
        // Therefore findObject does not tell us which of the parents is 
        // associated with the data, only that one exist.  So, to find the 
        // exact element that is associated with data (which should be the 
        // root widget), we need to first get the data object, then call
        // ObjectScene.findWidget to get the widget that is actually assocated 
        // with the data object.
        ObjectScene objScene = (ObjectScene)child.getScene();
        Object parentData = objScene.findObject(child);
        if(parentData != null)
        {
            retVal = objScene.findWidget(parentData);
        }
        
        return retVal;
    }
    
    /**
     * First checks if the widget is in the viewable area.  If the widget is
     * not in the viewable area, then center the widget.
     * 
     * @param widget The widget to make vieable.
     */
    public static void makeSureWidgetIsVisible(Widget widget)
    {
        Scene contextScene = widget.getScene();
        Rectangle visibleRect = contextScene.getView().getVisibleRect();
        Rectangle sceneRect = widget.convertLocalToScene(widget.getBounds());
        Rectangle viewWidgetRect = contextScene.convertSceneToView(sceneRect);

        if(visibleRect.contains(viewWidgetRect) == false)
        {
            Util.centerWidget(widget);
        }
    }
    
    /**
     * Tries to move the scene viewable area so that the the widget is in the 
     * center of the view.  
     * 
     * @param widget The widget to be centered.
     */
    public static void centerWidget(Widget widget)
    {
        Scene scene = widget.getScene();

        JComponent view = scene.getView();
        if (view != null)
        {
            Rectangle viewBounds = view.getVisibleRect();
            Rectangle rectangle = widget.convertLocalToScene(widget.getBounds());
            Point center = new Point(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);

            center = scene.convertSceneToView(center);

            view.scrollRectToVisible(new Rectangle(center.x - viewBounds.width / 2, center.y - viewBounds.height / 2, viewBounds.width, viewBounds.height));
            if (scene instanceof ObjectScene)
            {
                ObjectScene objectScene = (ObjectScene) scene;
                Object obj = objectScene.findObject(widget);
                HashSet<Object> set = new HashSet<Object>();
                set.add(obj);
                objectScene.userSelectionSuggested(set, false);
            }
        }
    }
    
    /**
     * Resets the size of the node to display the contents of the node.
     *  
     * @param scene The scene that contains the node.
     * @param modelElement 
     */
    public static void resizeNodeToContents(final Widget widget)
    {
        if (widget instanceof UMLNodeWidget)
        {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        final UMLNodeWidget nW = (UMLNodeWidget) widget;
                        //check mode first
                        nW.setResizeMode(UMLNodeWidget.RESIZEMODE.MINIMUMSIZE);
                        nW.setIsManuallyResized(false); //drop manually resized status
                        //
                        nW.setPreferredBounds(null);
                        nW.setPreferredSize(null);
                        nW.setMinimumSize(null);
                        switch (nW.getResizeMode())
                        {
                            case MINIMUMSIZE:
                                nW.setMinimumSize(nW.getDefaultMinimumSize());
                                break;
                            case PREFERREDBOUNDS:
                                nW.setPreferredBounds(new Rectangle(new Point(), nW.getDefaultMinimumSize()));
                                break;
                            case PREFERREDSIZE:
                                nW.setPreferredSize(nW.getPreferredSize());
                                break;
                        }
                        //as in 6.1 if mode is set to never resize we need to change min size if necessary to poref bounds after validation

                        ActionProvider provider = new ActionProvider()
                        {
                            public void perfomeAction()
                            {
                                nW.updateSizeWithOptions();
                            }
                        };

                        new AfterValidationExecutor(provider, widget.getScene());
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    
    public static Collection<ConnectionWidget> getAllContainedEdges(Widget widget)
    {
        HashSet<ConnectionWidget> set = new HashSet<ConnectionWidget>();
        Scene scene = widget.getScene();
        if (scene instanceof GraphScene)
        {
            GraphScene gs = (GraphScene) scene;

            List<Object> nodeChildren = getAllNodeChildren(widget);
            for (Object obj : nodeChildren)
            {
                Collection<Object> edges = gs.findNodeEdges(obj, true, true);
                for (Object e : edges)
                {
                    Object source = gs.getEdgeSource(e);
                    Object target = gs.getEdgeTarget(e);
                    if (nodeChildren.contains(source) && nodeChildren.contains(target))
                    {
                        set.add((ConnectionWidget) gs.findWidget(e));
                    }
                }
            }
        }
        return set;
    }
    
    
    public static List<Object> getAllNodeChildren(Widget widget)
    {
        if (!(widget.getScene() instanceof GraphScene))
            return new ArrayList<Object>();
        
        return getAllNodeChildrenRecursive(new ArrayList<Object>(), widget);
    }
    
    private static List<Object> getAllNodeChildrenRecursive(List<Object> list, Widget widget)
    {
        for (Widget child : widget.getChildren())
        {           
            Object pe = ((GraphScene) widget.getScene()).findObject(child);
            if (((GraphScene) widget.getScene()).isNode(pe))
            {
                list.add(pe);
            }
            
            list = getAllNodeChildrenRecursive(list, child);
        }
        return list;
    }
    
    public static Widget getParentWidgetByClass(Widget startWith,
                                                  Class<? extends Widget> cls)
    {
        Widget ret = null;
        if (startWith != null && cls != null)
        {
            for (Widget tmp = startWith;
                    tmp != null && !(tmp instanceof Scene);
                    tmp = tmp.getParentWidget())
            {
                if (cls.isInstance(tmp))
                {
                    ret = tmp;
                    break;
                } 
            }
        }
        return ret;
    }    
    
     public static Point center (Rectangle rectangle) {
        return new Point (rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    }
     
     
    
    public static boolean isPaletteExecute(WidgetKeyEvent event)
    {
        boolean retVal = false;
        
        if(Utilities.isMac() == true)
        {
            if((event.isMetaDown() == true) && 
                (event.getKeyCode() == KeyEvent.VK_ENTER))
            {
               retVal = true; 
            }
        }
        else
        {
            if((event.isControlDown() == true) && 
                (event.getKeyCode() == KeyEvent.VK_ENTER))
            {
               retVal = true; 
            }
        }
        
        return retVal;
    }
}
