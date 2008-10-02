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
package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author Jyothi
 */
public class PersistenceUtil {

    //Hashmap to maintain a list of anchors(aka GraphConnectors) and its XMI-IDs
    private static HashMap<Anchor, String> anchors = new HashMap();
    private static boolean diagramLoading = false;

    public PersistenceUtil() {
    }

    public static IPresentationElement getPresentationElement(Widget widget) {
        IPresentationElement retVal = null;
        if (widget != null) {
            GraphScene scene = (GraphScene) widget.getScene();
            Object obj = scene.findObject(widget);
            if (obj instanceof IPresentationElement) {
                retVal = (IPresentationElement)obj;
            }           
        }
        return retVal;
    }

    public static String getPEID(Widget widget) {
        String peid = "";
        IPresentationElement elt = getPresentationElement(widget);
        if (elt != null) {
            peid = elt.getXMIID();
        } else {
            //generate a new ID 
//            peid = UMLXMLManip.generateId(true);
            peid = "NEW_TEMP_PEID";
        }
        return peid;
    }

    public static IElement getModelElement(Widget widget) {
        IElement pModelElt = null;
        IPresentationElement elt = getPresentationElement(widget);
        if (elt != null) {
            pModelElt = elt.getFirstSubject();
        }
        return pModelElt;
    }

    public static String getMEID(Widget widget) {
        String meid = "";
        IElement mElt = getModelElement(widget);
        if (mElt != null) {
            meid = mElt.getXMIID();
        }
        return meid;
    }

    public static void addAnchor(Anchor anchor) {
        anchors.put(anchor, UMLXMLManip.generateId(true));
    }

    public static String findAnchor(Anchor anchor) {
        return anchors.get(anchor);
    }

    public static boolean isAnchorListEmpty() {
        if (anchors.size() <= 0)
            return true;
        else
            return false;
    }
    public static void clearAnchorMap() {
        anchors.clear();
    }


    //helper methods
    public static NodeWriter populateNodeWriter(NodeWriter nodeWriter, Widget widget) {
        nodeWriter.setRootNode(false); //This is NOT a Scene / Diagram
        nodeWriter.setLocation(widget.getPreferredLocation());

//        nodeWriter.setSize(widget.getBounds().getSize());
        Rectangle bnd = widget.getBounds();//border need correction for selection border sizes
        if (bnd != null)
        {
            bnd.x += widget.getBorder().getInsets().left;//may not be necessary because only size is used
            bnd.y += widget.getBorder().getInsets().top;
            bnd.width -= (widget.getBorder()).getInsets().left + (widget.getBorder()).getInsets().right;
            bnd.height -= (widget.getBorder()).getInsets().top + (widget.getBorder()).getInsets().bottom;
            nodeWriter.setSize(bnd.getSize());
        }
        else
        {
            nodeWriter.setSize(new Dimension());
        }
        nodeWriter.setViewport(null);
        nodeWriter.setPEID(PersistenceUtil.getPEID(widget));
        nodeWriter.setMEID(PersistenceUtil.getMEID(widget));
        IElement elt = PersistenceUtil.getModelElement(widget);
        if (elt != null)
        {
            nodeWriter.setElementType(elt.getElementType());
        }
        return nodeWriter;
    }

    public static void clearNodeWriterValues(NodeWriter nodeWriter) {
        nodeWriter.setRootNode(false);
        nodeWriter.setLocation(null);
        nodeWriter.setSize(null);
        nodeWriter.setViewport(null);
        nodeWriter.setPEID(null);
        nodeWriter.setMEID(null);
        nodeWriter.setElementType(null);
    }
    
    public static void populateProperties(NodeWriter nodeWriter, Widget widget)
    {
        if (nodeWriter == null || widget == null)
        {
            return;
        }
        HashMap<String, String> props = nodeWriter.getProperties();
        if (props == null)
        {
            props = new HashMap<String, String>();
        }
        ResourceTable table = widget.getResourceTable();
        Set<String> propertyNames = table.getLocalPropertyNames();
        for (Iterator<String> it = propertyNames.iterator(); it.hasNext();)
        {
            String key = it.next();
//            System.out.println(" Scene property = " + key);
            Object propVal = table.getProperty(key);
//            System.out.println(" property value = " + propVal.toString());
            props.put(key, propVal.toString());
        }
    }
    
    public static void clearProperties(Writer writer) {
        if (writer != null) {
            writer.getProperties().clear();
        }
    }
    
    public static void saveDependencies(Widget widget, NodeWriter nodeWriter)
    {
        Collection depList = widget.getDependencies();
        if (depList.size() > 0)
        {
            nodeWriter.beginDependencies();
            Iterator iter = depList.iterator();
            while (iter.hasNext())
            {
                Object obj = iter.next();
                if (obj instanceof Anchor)
                {
                    //don't do anything yet.. we'll deal with this in anchorage section..
                }
                else { //assuming only movablelabelwidgets here...
//                    System.out.println(" obj is " + obj);
                    if (obj instanceof DiagramNodeWriter) {
                        ((DiagramNodeWriter)obj).save(nodeWriter);
                    }
                }
            }
            nodeWriter.endDependencies();
        }        
    }

    public static boolean isDiagramLoading()
    {
        return diagramLoading;
    }

    public static void setDiagramLoading(boolean diagramLoading)
    {
        PersistenceUtil.diagramLoading = diagramLoading;
    }

    // get the UMLNodeWidget in the parent hierarchy
    public static UMLNodeWidget getParentUMLNodeWidget(Widget widget) {
        Widget parent;
        Widget child = widget;        
        if ((child != null) && !(child instanceof Scene))
        {
            while (true)
            {
                parent = child.getParentWidget();
                if (parent instanceof Scene)
                {
                    return null;
                }
                else if (parent instanceof UMLNodeWidget)
                {
                    return (UMLNodeWidget)parent;
                }
                else
                {
                    child = parent;
                }
            }
        }
        return null;
    }
    
}
