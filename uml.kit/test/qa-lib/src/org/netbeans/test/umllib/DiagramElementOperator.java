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


/*
 * DiagramElementOperator.java
 *
 */

package org.netbeans.test.umllib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.PaletteOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.project.ui.palette.UMLPalette;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.NodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.test.umllib.actions.Actionable;
import org.netbeans.test.umllib.actions.ActionablePoint;
import org.netbeans.test.umllib.actions.LabelsNameElementAction;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.util.JPopupByPointChooser;

/**
 * This is common class for the all diagram elements
 * @author Alexei Mokeev
 */
public class DiagramElementOperator extends Operator implements Actionable {

    private DiagramOperator diagramOperator = null;
    private IETGraphObject elementGraphObject = null;


    /**
     * Default delay for Diagram Element Waiter. Real value can be changed
     * as for the all other operators(For example via JemmyProperties)
     */
    public static final long WAIT_GRAPHOBJECT_TIMEOUT = 60000;

    /**
     * Construct DiagramElementOperator by visible name
     * @param diagramOperator Diagram to look for element
     * @param elementVN Element visible name. Visible name is the name as you see it on a diagram
     * @throws qa.uml.exceptions.NotFoundException when no suitable element found
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, String elementVN) throws NotFoundException {
        this(diagramOperator, new ElementByVNChooser(elementVN, ElementTypes.ANY), 0);
    }

    /**
     * Construct DiagramElementOperator by visible name, type and index
     * @param diagramOperator Diagram to look for element
     * @param elementVN Element visible name. Visible name is the name as you see it on a diagram
     * @param elementType Type of element(e.g Class, Component, Actor, etc)
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable element found
     * @see qa.uml.util.ElementTypes
     */

    public DiagramElementOperator(DiagramOperator diagramOperator, String elementVN, ExpandedElementTypes elementType, int index) throws NotFoundException {
        this(diagramOperator, new ElementByVNChooser(elementVN, elementType), index);
    }

    /**
     * Construct DiagramElementOperator by visible name, type and index
     * @param diagramOperator Diagram to look for element
     * @param elementVN Element visible name. Visible name is the name as you see it on a diagram
     * @param elementType Type of element(e.g Class, Component, Actor, etc)
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable element found
     * @see qa.uml.util.ElementTypes
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, String elementVN, ElementTypes elementType, int index) throws NotFoundException {
        this(diagramOperator, new ElementByVNChooser(elementVN, elementType), index);
    }

    /**
     *
     * @param diagramOperator
     * @param elementVN
     * @param elementType
     * @throws qa.uml.exceptions.NotFoundException
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, String elementVN, ElementTypes elementType) throws NotFoundException {
        this(diagramOperator, new ElementByVNChooser(elementVN, elementType), 0);
    }

    /**
     * Construct DiagramElementOperator by visible name and index
     * @param diagramOperator Diagram to look for element
     * @param elementVN Element visible name. Visible name is the name as you see it on a diagram
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable element found
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, String elementVN, int index) throws NotFoundException {
        this(diagramOperator, new ElementByVNChooser(elementVN, ElementTypes.ANY), index);
    }

    /**
     * Construct DiagramElementOperator by custom finder and index
     * @param diagramOperator Diagram to look for element
     * @param elementFinder custom finder
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable element found
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, DiagramElementChooser elementFinder, int index) throws NotFoundException {
        this(diagramOperator, waitForGraphObject(diagramOperator, elementFinder, index));
    }

    /**
     *
     * @param diagramOperator
     * @param elementFinder
     * @param index
     * @param lookForAllElements
     * @throws qa.uml.exceptions.NotFoundException
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, DiagramElementChooser elementFinder, int index, boolean lookForAllElements) throws NotFoundException {
        this(diagramOperator, waitForGraphObject(diagramOperator, elementFinder, index, lookForAllElements));
    }

    /**
     * Construct DiagramElementOperator by given graphObject
     * @param diagramOperator Diagram to look for element
     * @param graphObject given graph object
     */
    public DiagramElementOperator(DiagramOperator diagramOperator, IETGraphObject graphObject) {
        this.diagramOperator = diagramOperator;
        this.elementGraphObject = graphObject;
    }



    /**
     * Search for suitable graph object
     * @return Graph object if found
     * @param lookForAllElements
     * @param diagramOperator Diagram to look for element
     * @param elementFinder custom finder
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when nothing suitable found
     */
    public static IETGraphObject findGraphObject(DiagramOperator diagramOperator, DiagramElementChooser elementFinder, int index, boolean lookForAllElements) throws NotFoundException {
        ArrayList<IETGraphObject> elementsFound = new ArrayList<IETGraphObject>();

        //searching for elements matching elemenFinder criteria
        IDrawingAreaControl cntrl = diagramOperator.getDrawingAreaControl();
        ETList<IETGraphObject> allGraphs = cntrl.getAllItems6();
        Iterator<IETGraphObject> tsIt = allGraphs.iterator();
        while (tsIt.hasNext()) {
            IETGraphObject graphObject = tsIt.next();
            IPresentationElement presElement = graphObject.getPresentationElement();
            if (presElement == null) {
                continue;
            }

            if (!lookForAllElements) {
                if (!(presElement instanceof NodePresentation)) {
                    //We are looking only for nodes here
continue;       }
            }

            if (elementFinder.checkElement(graphObject)) {
                elementsFound.add(graphObject);
            }
        }

        //sorting found elements
        DiagramElementComparator<IETGraphObject> c = new DiagramElementComparator<IETGraphObject>();
        IETGraphObject[] arr = (IETGraphObject[]) elementsFound.toArray(new IETGraphObject[0]);
        Arrays.sort(arr, c);
        if (arr.length > index) {
            return arr[index];
        } else {
            throw new NotFoundException("Element matching the criteria not found on diagram " + diagramOperator.getDiagramName() + ":" + elementFinder.getDescription());
        }
    }


    /**
     *
     * @param diagramOperator
     * @param elementFinder
     * @param index
     * @throws qa.uml.exceptions.NotFoundException
     * @return
     */
    public static IETGraphObject findGraphObject(DiagramOperator diagramOperator, DiagramElementChooser elementFinder, int index) throws NotFoundException {
        return findGraphObject(diagramOperator, elementFinder, index, false);
    }


    /**
     * Wait for suitable graph object
     * @return Element's GraphObject if found
     * @param lookForAllElements
     * @param diagramOperator Diagram to look for element
     * @param elementFinder custom finder
     * @param index index
     */
    public static IETGraphObject waitForGraphObject(final DiagramOperator diagramOperator, final DiagramElementChooser elementFinder, final int index, final boolean lookForAllElements) {
        try {
            Waiter w = new Waiter(new Waitable() {

                public Object actionProduced(Object obj) {
                    try {
                        IETGraphObject go = findGraphObject(diagramOperator, elementFinder, index, lookForAllElements);
                        return go;
                    } catch (NotFoundException e) {
                        return null;
                    }
                }

                public String getDescription() {
                    return "Wait for " + elementFinder.getDescription();
                }
            });
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("DiagramElementOperator.WaitDiagramElementTime"));
            return (IETGraphObject) w.waitAction(null);
        } catch (InterruptedException ie) {
            return null;
        }
    }

    /**
     *
     * @param diagramOperator
     * @param elementFinder
     * @param index
     * @return
     */
    public static IETGraphObject waitForGraphObject(final DiagramOperator diagramOperator, final DiagramElementChooser elementFinder, final int index) {
        return waitForGraphObject(diagramOperator, elementFinder, index, false);
    }

    /**
     * Wait for suitable graph object disaapearnce
     * @param diagramOperator Diagram to look for element
     * @param elementFinder custom finder
     * @param index index
     * @return true if element disaapears and false otherwise
     */
    public static boolean waitForDeletion(final DiagramOperator diagramOperator, final DiagramElementChooser elementFinder, final int index) {
        try {
            Waiter w = new Waiter(new Waitable() {

                public Object actionProduced(Object obj) {
                    try {
                        IETGraphObject go = findGraphObject(diagramOperator, elementFinder, index);
                        if (go != null) {
                            return null;
                        }
                    } catch (NotFoundException e) {
                    }
                    return new Object();
                }

                public String getDescription() {
                    return "Wait for deletion of " + elementFinder.getDescription();
                }
            });
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("DiagramElementOperator.WaitDiagramElementTime"));
            Object o = w.waitAction(null);
            if (o != null) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ie) {
            return false;
        }
    }

    /**
     * Wait for selection state, timeout 5 seconds
     * @param selected
     */
    public void waitSelection(boolean selected) {
        waitSelection(selected, 5000);
    }

    /**
     * Wait for selection state
     * @param selected
     * @param timeout
     */
    public void waitSelection(boolean selected, long timeout) {
        for (int i = 0; i < (timeout / 50); i++) {
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }
            if (isSelected() == selected) {
                return;
            }
        }
        throw new UMLCommonException("Failed to wait for '" + selected + "' selection state//debug: " + elementGraphObject.isSelected() + ":" + elementGraphObject + ":" + elementGraphObject.getText() + ":" + elementGraphObject.isEdge() + ":" + elementGraphObject.isNode() + ":" + elementGraphObject.getPresentationElement() + ":" + elementGraphObject.getPresentationElement().getFirstSubject() + ":" + elementGraphObject.getPresentationElement().getFirstSubject().getExpandedElementType());
    }

    /**
     *
     * @return
     */
    public Color getBorderColor() {
        try {
            INodeDrawEngine engine = (INodeDrawEngine) elementGraphObject.getEngine();
            return engine.getBorderColor();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     *
     * @return
     */
    public Color getBackgroundColor() {
        try {
            INodeDrawEngine engine = (INodeDrawEngine) elementGraphObject.getEngine();
            return engine.getFillColor();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     *
     * @return
     */
    public Font getFont() {
        try {
            IETNodeUI ui = (IETNodeUI) elementGraphObject.getETUI();
            return ui.getFont().getFont();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Return element type
     * @return Type of element
     */
    public String getElementType() {
        return elementGraphObject.getEngine().getElementType();
    }

    /**
     * The method has to resize the element by x in width and y in height
     * CAUTION! May not work when scrolling is involved
     * @param x
     * @param y
     */
    public void resize(int x, int y) {
        //TODO: add scrolling support
        IETRect rect = elementGraphObject.getEngine().getBoundingRect();
        Point point = rect.getBottomRight();
        this.select();
        new Timeout("", 500).sleep();
        diagramOperator.getDrawingArea().moveMouse(point.x + 4, point.y + 4);
        new Timeout("", 500).sleep();
        diagramOperator.getDrawingArea().dragNDrop(point.x + 4, point.y + 4, point.x + x, point.y + y);
    }


    /**
     * We must implement this method to extend Operator, but since
     * we are not working with Component this method always return null;
     * @return null
     */
    public Component getSource() {
        return null;
    }



    /**
     * Returns GraphObject for this diagram element
     * @return GraphObject for this diagram element
     */
    public IETGraphObject getGraphObject() {
        return elementGraphObject;
    }

    /**
     * Wrapper for elementGraphObject.getEngine().getBoundingRect().getRectangle();
     * @return Rectangle
     */
    public Rectangle getElementRectangle() {
        return elementGraphObject.getEngine().getBoundingRect().getRectangle();
    }

    /**
     * Return Diagram, where this element is placed
     * @return Diagram, where this element is placed
     */
    public DiagramOperator getDiagram() {
        return diagramOperator;
    }


    /**
     * Returns name of element
     * @return name of element
     */

    public String getName() {

        String name = "";

        ETList<IElement> subjects = getGraphObject().getPresentationElement().getSubjects();

        if (subjects.size() > 0) {
            name = subjects.get(0).toString();
        }

        if (name.equals("")) {
            name = getGraphObject().getText();
        }


        return name;
    }


    /**
     * Returns type or null if we have several subjects. Should be overriden in subclasses
     * @return type or null if we have several subjects. Should be overriden in subclasses
     */
    public String getType() {
        if (elementGraphObject.getPresentationElement().getSubjectCount() == 1) {
            return elementGraphObject.getPresentationElement().getFirstSubject().getElementType();
        }
        return null; //We can't detect type
    }

    /**
     * Returns expanded type or null if we have several subjects. Should be overriden in subclasses
     * @return expanded type or null if we have several subjects. Should be overriden in subclasses
     */
    public String getExpandedType() {
        if (elementGraphObject.getPresentationElement().getSubjectCount() == 1) {
            return elementGraphObject.getPresentationElement().getFirstSubject().getExpandedElementType();
        }
        return null; //We can't detect type
    }

    /**
     * Returns all links from and to the diagram element
     * @return all links from and to the diagram element
     */
    public HashSet<LinkOperator> getLinks() {
        HashSet<LinkOperator> links = new HashSet<LinkOperator>();
        IETGraphObject sObject = getGraphObject();
        if ((sObject != null) && (sObject instanceof IETNode)) {
            IETNode node = (IETNode) sObject;

            ETList<IETEdge> list = node.getEdges();
            Iterator<IETEdge> it = list.iterator();
            while (it.hasNext()) {
                IETEdge edge = (IETEdge) it.next();
                IPresentationElement presentation = edge.getPresentationElement();
                if (presentation != null) {
                    links.add(new LinkOperator(diagramOperator, edge));
                }
            }
        }
        return links;
    }

    /**
     * Returns all incoming links
     * @return All incoming links
     */
    public HashSet<LinkOperator> getInLinks() {
        HashSet<LinkOperator> links = new HashSet<LinkOperator>();
        IETGraphObject sObject = getGraphObject();
        if ((sObject != null) && (sObject instanceof IETNode)) {
            IETNode node = (IETNode) sObject;
            List list = node.getInEdges();
            Iterator it = list.iterator();
            while (it.hasNext()) {
                IETEdge edge = (IETEdge) it.next();
                IPresentationElement presentation = edge.getPresentationElement();
                if (presentation != null) {
                    links.add(new LinkOperator(diagramOperator, edge));
                }
            }
        }
        return links;
    }

    /**
     * Returns all outcoming links
     * @return All outcoming links
     */
    public HashSet<LinkOperator> getOutLinks() {
        HashSet<LinkOperator> links = new HashSet<LinkOperator>();
        IETGraphObject sObject = getGraphObject();
        if ((sObject != null) && (sObject instanceof IETNode)) {
            IETNode node = (IETNode) sObject;
            List list = node.getOutEdges();
            Iterator it = list.iterator();
            while (it.hasNext()) {
                IETEdge edge = (IETEdge) it.next();
                IPresentationElement presentation = edge.getPresentationElement();
                if (presentation != null) {
                    links.add(new LinkOperator(diagramOperator, edge));
                }
            }
        }
        return links;
    }


    /**
     * Returns center point of this diagram element
     * @return Center point of this diagram element
     */
    public Point getCenterPoint() {
        Rectangle tmp = getBoundingRect();
        Point ret = tmp.getLocation();
        ret.translate(tmp.width / 2, tmp.height / 2);
        return ret;
        //return elementGraphObject.getEngine().getBoundingRect().getCenterPoint();
    }

    /**
     *
     * @return bounding rect for element
     */
    public Rectangle getBoundingRect() {
        IETRect tmpRect = elementGraphObject.getEngine().getBoundingRect();
        ETDeviceRect tmpDevRect = null;
        int x;
        int y;
        //transform to device coordinates
        if (tmpRect instanceof ETRect || tmpRect instanceof ETRectEx) {
            // This special case is for all the code that depends
            // on the bounding rectangle in device coordinates.
            tmpDevRect = ((ETRect) tmpRect).getAsDeviceRect();
        } else if (tmpRect instanceof ETDeviceRect) {
            tmpDevRect = ((ETDeviceRect) tmpRect);
        }
        return tmpDevRect.getBounds();
    }

    /**
     *
     * @param clickCount
     * @param mouseButton
     * @param modifiers
     */
    public void clickOnCenter(int clickCount, int mouseButton, int modifiers) {
        clickOn(getCenterPoint(), clickCount, mouseButton, modifiers);
    }


    /**
     *
     * @param clickCount
     * @param mouseButton
     */
    public void clickOnCenter(int clickCount, int mouseButton) {
        clickOn(getCenterPoint(), clickCount, mouseButton, 0);
    }

    public void clickOnCenter() {
        clickOn(getCenterPoint(), 1, InputEvent.BUTTON1_MASK, 0);
    }


    /**
     *
     * @param p
     * @param clickCount
     * @param mouseButton
     * @param modifiers
     */
    public void clickOn(Point p, int clickCount, int mouseButton, int modifiers) {
        p = makeVisible(p);
        diagramOperator.getDrawingArea().clickMouse(p.x, p.y, clickCount, mouseButton, modifiers);
    }

    public void clickForPopup() {
        if (ElementTypes.COMBINED_FRAGMENT.toString().equals(getType())) {
            //workaround for 90586
            Point loc = getBoundingRect().getLocation();
            loc.translate(2, 2);
            clickOn(loc, 1, MouseEvent.BUTTON3_MASK, 0);
        } else {
            clickOn(getCenterPoint(), 1, InputEvent.BUTTON3_MASK, 0);
        }
    }

    private void dummy() {
        //diagramOperator.getDrawingArea
        //com.embarcadero.uml.ui.swing.drawingarea.ADGraphWindow a;
        // a.sc
    }


    public void center() {
        center(false, false);
    }


    /**
     *
     * @param selectIt
     * @param deselectOthers
     */
    public void center(boolean selectIt, boolean deselectOthers) {
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        ADGraphWindow area = diagramOperator.getDrawingArea().getArea();
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        area.getDrawingArea().centerPresentationElement(elementGraphObject.getPresentationElement(), selectIt, deselectOthers);
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
    }

    /**
     * gets point inside a component and, if component is not visible centers window in this component.
     * The updated device point is returned
     * @param point
     * @return
     */
    public Point makeVisible(Point point) {
        ADGraphWindow area = diagramOperator.getDrawingArea().getArea();
        IDrawingAreaControl daControl = area.getDrawingArea();

        IETPoint etPoint = daControl.deviceToLogicalPoint(point.x, point.y);

        IETRect eDeviceAreaRect = new ETRect(area.getVisibleRect());
        IETRect eVisibleAreaRect = daControl.deviceToLogicalRect(eDeviceAreaRect);
        IETRect eElementRect = elementGraphObject.getEngine().getLogicalBoundingRect(true);

        if (!eVisibleAreaRect.contains(eElementRect)) {
            center();
            new Timeout("", 500);
        }
        return daControl.logicalToDevicePoint(etPoint).asPoint();
    }


    /**
     *
     * @return
     */
    public ArrayList<String> getSubjectVNs() {
        ArrayList<String> al = new ArrayList<String>();
        ETList<IElement> subjects = getGraphObject().getPresentationElement().getSubjects();
        Iterator<IElement> itSubj = subjects.iterator();
        while (itSubj.hasNext()) {
            IElement sbj = (IElement) itSubj.next();
            if (sbj instanceof INamedElement) {
                al.add(((INamedElement) sbj).getName());
            }
        }
        return al;
    }

    /**
     * Call popup from up-left corner of element
     * @return popup
     */
    public JPopupMenuOperator getGeneralPopup() {
        Point loc = getCenterPoint(); 
        loc = getBoundingRect().getLocation();
        loc.translate(10, 5);
       
        //
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        loc = makeVisible(loc);
        //workarround for Issue 79519
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
            clickOn(loc, 1, InputEvent.BUTTON1_MASK, 0);
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }
        }
        clickOn(loc, 1, InputEvent.BUTTON3_MASK, 0);
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        JPopupMenuOperator ret = new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container) (MainWindowOperator.getDefault().getSource()), new JPopupByPointChooser(loc, diagramOperator.getDrawingArea().getSource(), 0)));

        return ret;
    }


    //Methods from Actionable interface
    /**
     * Call popup from central of element
     * @return
     */
    public JPopupMenuOperator getPopup() {
        Point loc = getCenterPoint();
         if (ElementTypes.COMBINED_FRAGMENT.toString().equals(getType())) {
            //workaround for 90586
            //works with big fragments only
            loc = getBoundingRect().getLocation();
            loc.translate(10, 5);
        }
        //
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        loc = makeVisible(loc);
        //workarround for Issue 79519
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
            clickOn(loc, 1, InputEvent.BUTTON1_MASK, 0);
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }
        }
        clickOn(loc, 1, InputEvent.BUTTON3_MASK, 0);
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        JPopupMenuOperator ret = new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container) (MainWindowOperator.getDefault().getSource()), new JPopupByPointChooser(loc, diagramOperator.getDrawingArea().getSource(), 0)));

        return ret;
    }

    public void select() {
        if (!isSelected()) {
            if (ElementTypes.COMBINED_FRAGMENT.toString().equals(getType())) {
                //workaround for 90586
                Point loc = getBoundingRect().getLocation();
                loc.translate(2, 2);
                clickOn(loc, 1, MouseEvent.BUTTON1_MASK, 0);
                try {
                    waitSelection(true);
                } catch (Exception ex) {
                    //workaround to workaround (very thin active area)
                    loc.translate(-1, -1);
                    clickOn(loc, 1, MouseEvent.BUTTON1_MASK, 0);
                    waitSelection(true);
                }
            } else {
                clickOnCenter();
                waitSelection(true);
            }
        } else {
            waitSelection(true);
        }
    }

    /**
     *
     * @param avoidcollitionsandretry
     */
    public void select(boolean avoidcollitionsandretry) {
        if (avoidcollitionsandretry) {
            int count = 0;
            Point click = new Point(getCenterPoint());
            //
            int r = 5;
            int dx = 0;
            int dy = 0;

            while (!isSelected() && count < 10) {
                count++;
                clickOn(click, 1, InputEvent.BUTTON1_MASK, 0);
                try {
                    waitSelection(true);
                } catch (Exception ex) {
                    if (dx > 0 && dy > 0) {
                        dx = 0;
                        dy = -2 * dy;
                    } else if (dx == 0 && dy < 0) {
                        dx = dy;
                        dy = 0;
                    } else if (dx < 0 && dy == 0) {
                        dy = -dx;
                        dx = 0;
                    } else if (dx == 0 && dy > 0) {
                        dx = dy;
                        dy = 0;
                    } else {
                        dx = r;
                        dy = r;
                        r *= 2;
                        click = new Point(getCenterPoint());
                    }
                    click.translate(dx, dy);
                }
            }
        } else {
            select();
        }
    }

    /**
     *
     * @return
     */
    public boolean isSelected() {
        return elementGraphObject.isSelected();
    }



    public void addToSelection() {
        new Timeout("", 10).sleep();
        if (ElementTypes.COMBINED_FRAGMENT.toString().equals(getType())) {
            //workaround for 90586
            Point loc = getBoundingRect().getLocation();
            loc.translate(2, 2);
            clickOn(loc, 1, MouseEvent.BUTTON1_MASK, KeyEvent.CTRL_MASK);
            try {
                waitSelection(true);
            } catch (Exception ex) {
                //workaround to workaround (very thin active area)
                loc.translate(-1, -1);
                clickOn(loc, 1, MouseEvent.BUTTON1_MASK, KeyEvent.CTRL_MASK);
                waitSelection(true);
            }
        } else {
            clickOnCenter(1, InputEvent.BUTTON1_MASK, KeyEvent.CTRL_MASK);
            waitSelection(true);
        }
    }

    /**
     * Change element size with usage of mouse robot driver
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        if (!isSelected()) {
            select();
        }
        java.awt.Rectangle parB = getBoundingRect();
        DrawingAreaOperator drA = diagramOperator.getDrawingArea();
        int shift = (int) (Math.round(4.0*drA.getZoomLevel()));
        MouseRobotDriver driver = new MouseRobotDriver(new Timeout("", 250));
        driver.moveMouse(drA, parB.x + parB.width + shift, parB.y + parB.height + shift);
        driver.pressMouse(drA, parB.x + parB.width + shift, parB.y + parB.height + shift, InputEvent.BUTTON1_MASK, 0);
        new Timeout("", 500).sleep();
        driver.moveMouse(drA, parB.x + width + 2 * shift, parB.y + height + 2 * shift);
        new Timeout("", 500).sleep();
        driver.releaseMouse(drA, parB.x + width + 2 * shift, parB.y + height + 2 * shift, InputEvent.BUTTON1_MASK, 0);
        new Timeout("", 500).sleep();
    }

    /**
     *
     * @param width
     * @param height
     */
    public void setSize(double width, double height) {
        setSize((int) width, (int) height);
    }

    /**
     * Change element position with usage of mouse robot driver
     * @param x
     * @param y
     */
    public void moveTo(int x, int y) {
        java.awt.Rectangle parB = getBoundingRect();
        DrawingAreaOperator drA = diagramOperator.getDrawingArea();
        MouseRobotDriver driver = new MouseRobotDriver(new Timeout("", 250));
        int corner_shift = 4;
        driver.moveMouse(drA, parB.x + corner_shift, parB.y + corner_shift);
        driver.pressMouse(drA, parB.x + corner_shift, parB.y + corner_shift, InputEvent.BUTTON1_MASK, 0);
        new Timeout("", 500).sleep();
        driver.moveMouse(drA, x, y);
        new Timeout("", 500).sleep();
        driver.releaseMouse(drA, x, y, InputEvent.BUTTON1_MASK, 0);
        new Timeout("", 500).sleep();
    }

    /**
     *
     * @param x
     * @param y
     */
    public void moveTo(double x, double y) {
        moveTo((int) x, (int) y);
    }

    /**
     * Change element position with usage of mouse robot driver
     * @param dx
     * @param dy
     */
    public void shift(int dx, int dy) {
        java.awt.Rectangle parB = getBoundingRect();
        DrawingAreaOperator drA = diagramOperator.getDrawingArea();
        MouseRobotDriver driver = new MouseRobotDriver(new Timeout("", 250));
        int corner_shift = 4;
        driver.moveMouse(drA, parB.x + corner_shift, parB.y + corner_shift);
        driver.pressMouse(drA, parB.x + corner_shift, parB.y + corner_shift, InputEvent.BUTTON1_MASK, 0);
        new Timeout("", 500).sleep();
        driver.moveMouse(drA, parB.x + dx, parB.y + dy);
        new Timeout("", 500).sleep();
        driver.releaseMouse(drA, parB.x + dx, parB.y + dy, InputEvent.BUTTON1_MASK, 0);
        new Timeout("", 500).sleep();
    }

    /**
     *
     * @param dx
     * @param dy
     */
    public void shift(double dx, double dy) {
        shift((int) dx, (int) dy);
    }

    /**
     *
     * @param el
     * @return
     */
    public boolean equals(Object el) {
        if (el instanceof DiagramElementOperator) {
            return this.elementGraphObject == ((DiagramElementOperator) el).elementGraphObject;
        } else {
            return false;
        }
    }
    static {
        Timeouts.initDefault("DiagramElementOperator.WaitDiagramElementTime", WAIT_GRAPHOBJECT_TIMEOUT);
    }


/**
     *
     */


    public static class DiagramElementComparator<C extends IETGraphObject> implements Comparator<C> {

        /**
         *
         * @param o1
         * @param o2
         * @return
         */
        public int compare(C o1, C o2) {
            Point o1Center = o1.getEngine().getBoundingRect().getCenterPoint();
            Point o2Center = o2.getEngine().getBoundingRect().getCenterPoint();
            if (o1Center.y > o2Center.y) {
                return 1;
            } else if (o1Center.y == o2Center.y) {
                if (o1Center.x > o2Center.y) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        }
    }




    public static class ElementByVNChooser implements DiagramElementChooser {

        private String vn = null;
        private String elementType = null;
        private ElementTypes elemTypeEnu = null;
        private ExpandedElementTypes elemExTypeEnu = null;
        private StringComparator comparator = null;

        /**
         *
         * @param vn
         * @param elementType
         * @param comparator
         */
        public ElementByVNChooser(String vn, ElementTypes elementType, StringComparator comparator) {
            this.vn = vn;
            this.elementType = elementType.toString();
            this.elemTypeEnu = elementType;
            this.comparator = comparator;
        }

        /**
         *
         * @param vn
         * @param elementType
         */
        public ElementByVNChooser(String vn, ElementTypes elementType) {
            this(vn, elementType, new Operator.DefaultStringComparator(true, true));
        }

        /**
         *
         * @param vn
         * @param elementType
         * @param comparator
         */
        public ElementByVNChooser(String vn, ExpandedElementTypes elementType, StringComparator comparator) {
            this.vn = vn;
            this.elementType = elementType.toString();
            this.elemExTypeEnu = elementType;
            this.comparator = comparator;
        }

        /**
         *
         * @param vn
         * @param elementType
         */
        public ElementByVNChooser(String vn, ExpandedElementTypes elementType) {
            this(vn, elementType, new Operator.DefaultStringComparator(true, true));
        }

        /**
         *
         * @param graphObject
         * @return
         */
        public boolean checkElement(IETGraphObject graphObject) {
            //check type
            String inType = null;
            String any = null;
            if (graphObject.getPresentationElement().getSubjectCount() == 1) {
                if (elemTypeEnu != null) {
                    inType = graphObject.getPresentationElement().getFirstSubject().getElementType();
                    any = elemTypeEnu.ANY.toString();
                } else if (elemExTypeEnu != null) {
                    inType = graphObject.getPresentationElement().getFirstSubject().getExpandedElementType();
                    any = elemExTypeEnu.ANY.toString();
                }
            }


            if ((elementType == null) || (!elementType.equals(inType) && !elementType.equals(any))) {
                return false;
            }

            IPresentationElement presElement = graphObject.getPresentationElement();
            ETList<IElement> subjects = presElement.getSubjects();
            Iterator<IElement> itSubj = subjects.iterator();
            while (itSubj.hasNext()) {
                IElement sbj = (IElement) itSubj.next();
                if (sbj instanceof INamedElement) {
                    if (comparator.equals(((INamedElement) sbj).getName(), vn)) {
                        return true;
                    } else if (vn == null && "".equals(((INamedElement) sbj).getName())) {
                        //consider requested null name as empty name
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         *
         * @return
         */
        public String getDescription() {
            //
            return "Choose element with Name: " + vn + "; Type: " + elemTypeEnu + "; or exType: " + elemExTypeEnu + "; string type: " + elementType + ";";
        }
    }



    public static class ElementByTypeChooser implements DiagramElementChooser {

        private String elementType = null;

        /**
         *
         * @param elementType
         */
        public ElementByTypeChooser(ElementTypes elementType) {
            this.elementType = elementType.toString();
        }


        /**
         *
         * @param graphObject
         * @return
         */
        public boolean checkElement(IETGraphObject graphObject) {
            String inType = null;
            if (graphObject.getPresentationElement().getSubjectCount() == 1) {
                inType = graphObject.getPresentationElement().getFirstSubject().getElementType();
            }

            if ((elementType == null) || (!elementType.equals(inType))) {
                return false;
            } else {
                return true;
            }
        }

        /**
         *
         * @return
         */
        public String getDescription() {
            return "Choose element by Type:" + elementType;
        }
    }






    public static class DefaultNamer implements SetName {

        public DefaultNamer() {
        }

        /**
         *
         * @param drawingArea
         * @param x
         * @param y
         * @param name
         */
        public void setName(ComponentOperator drawingArea, int x, int y, String name) {
            new EventTool().waitNoEvent(1000);
            for (int i = 0; i < name.length(); i++) {
                drawingArea.typeKey(name.charAt(i));
            }
            drawingArea.typeKey('\n');
            new Timeout("", 500).sleep();
        }
    }




    public static class LabelsNamer implements SetName {

        public LabelsNamer() {
        }

        /**
         *
         * @param drawingArea
         * @param x
         * @param y
         * @param name
         */
        public void setName(ComponentOperator drawingArea, int x, int y, String name) {
            new LabelsNameElementAction().performPopup(new ActionablePoint(drawingArea, x, y));
            for (int i = 0; i < name.length(); i++) {
                drawingArea.typeKey(name.charAt(i));
            }
            drawingArea.typeKey('\n');
            new Timeout("", 500).sleep();
        }
    }

    public static class PropertyNamer implements SetName {

        public PropertyNamer() {
        }

        /**
         *
         * @param drawingArea
         * @param x
         * @param y
         * @param name
         */
        public void setName(ComponentOperator drawingArea, int x, int y, String name) {
            PropertySheetOperator ps = new PropertySheetOperator();
            Property nmProp = new Property(ps, "Name");
            double nmPntX = ps.tblSheet().getCellRect(nmProp.getRow(), 1, false).getCenterX();
            double nmPntY = ps.tblSheet().getCellRect(nmProp.getRow(), 1, false).getCenterY();
            ps.clickMouse((int) nmPntX, (int) nmPntY, 1);
            for (int i = 0; i < name.length(); i++) {
                ps.typeKey(name.charAt(i));
            }
            ps.pushKey(KeyEvent.VK_ENTER);
            new Timeout("", 500).sleep();
        }
    }
}
