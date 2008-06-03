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
import java.util.HashSet;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

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
                objectScene.setSelectedObjects(set);
            }
        }
    }
}
