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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.dataobject.PaletteItem;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.WidgetViewManager;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.spi.palette.PaletteController;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author treyspiva
 * 
 */
public class SceneAcceptAction extends WidgetAction.Adapter 
        implements AcceptProvider
{
    protected INamespace sceneNamespace = null;
    private PaletteItem paletteItem = null;
    private IElement element;
    
    public SceneAcceptAction(INamespace space)
    {
        sceneNamespace = space;
    }

    public SceneAcceptAction(INamespace space, PaletteItem item)
    {
        this(space);
        paletteItem = item;
        element = paletteItem.createModelElement(null);
    }

    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event)
    {
        State retVal = WidgetAction.State.REJECTED;
        
        try
        {
            DesignerScene scene = null;
            if(widget.getScene() instanceof DesignerScene)
            {
                scene = (DesignerScene)widget.getScene();
            }
            
            // First only drop something if the left mouse button is pressed.
            if(event.getButton() == MouseEvent.BUTTON1)
            {
                if(paletteItem != null)
                {
                    IPresentationElement element = createModelElement(paletteItem,scene);
                    addWidget(event.getPoint(), scene, paletteItem, element);

                    scene.userSelectionSuggested(Collections.singleton(element), false);
                    scene.setFocusedObject(element);

                    retVal = WidgetAction.State.CONSUMED;

                }
            }
            
            int onmask = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;

            // If the shift key is pressed we are doing a multi-drop
            if (!((event.getModifiersEx() & onmask) == onmask))
            {
                clearPalette(scene);
                scene.removeBackgroundWidget();
            }   
        }
        catch (UnsupportedFlavorException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        return retVal;
    }

    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = WidgetAction.State.REJECTED;
        
        if(event.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            DesignerScene scene = null;
            if(widget.getScene() instanceof DesignerScene)
            {
                scene = (DesignerScene)widget.getScene();
                clearPalette(scene);
                scene.removeBackgroundWidget();
            }
        }
        
        return retVal;
    }

    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
    {
        ConnectorState retVal = ConnectorState.REJECT;
        
        try
        {
            if(isFromPalette(transferable))
            {
                retVal =  ConnectorState.ACCEPT;
            }
            else if(isFromProjectTree(transferable))
            {
                retVal =  ConnectorState.ACCEPT;
            }
        }
        catch(Exception e)
        {
            retVal = ConnectorState.REJECT;
        }
        
        if (widget.getScene() instanceof DesignerScene)
        {
            DesignerScene scene = (DesignerScene) widget.getScene();
            scene.getTopComponent().requestActive();
        }

        return retVal;
    }

    @SuppressWarnings("unchecked")
    public void accept(Widget widget, Point point, Transferable transferable)
    {
        DesignerScene scene=(DesignerScene) widget.getScene();
        DiagramEngine engine=scene.getEngine();
        
        try
        {            
            ArrayList < IPresentationElement > presentations = 
                    new ArrayList < IPresentationElement >();
            
            PaletteItem item = null;
            if(isFromPalette(transferable))
            {
                //got from palette
                item = (PaletteItem) transferable.getTransferData(PaletteItem.FLAVOR);
                IPresentationElement element = createModelElement(item,scene);
                presentations.add(element);
                
                clearPalette(scene);
                scene.removeBackgroundWidget();
            }
            else if(isFromProjectTree(transferable))
            {
                //got from project tree
                ADTransferable.ADTransferData transferData = 
                        (ADTransferable.ADTransferData) transferable.getTransferData(ADTransferable.ADDataFlavor);
                
                for(IElement modelElement : transferData.getModelElements())
                {
                    INamedElement element = (INamedElement)modelElement;
                    
                    // check if drop is possible for this model element and if 
                    // new element should be created
                    INamedElement toDrop = engine.processDrop(element);
                    if(toDrop!=null)
                    {
                        IPresentationElement presentation = Util.createNodePresentationElement();
                        presentations.add(presentation);
                        presentation.addSubject(toDrop);
                    }
                }
                
            }
            
            for(IPresentationElement curElement : presentations)
            {
                addWidget(point, scene, item, curElement);
            }

            if (!presentations.isEmpty())
            {
                scene.userSelectionSuggested(new HashSet(presentations), false);
                
                if(presentations.size() == 1)
                {
                    scene.setFocusedObject(presentations.get(0));
                }
            }
        }
        catch (UnsupportedFlavorException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected DataObject findDataObject(Transferable transferable)
    throws IOException, UnsupportedFlavorException
    {
        DataObject retVal = null;
        
        for(DataFlavor curFlavor : transferable.getTransferDataFlavors())
        {
            if(curFlavor.getMimeType().startsWith("application/x-java-openide-dataobjectdnd") == true)
            {
                retVal = (DataObject)transferable.getTransferData(curFlavor);
            }
        }
        
        return retVal;
    }
    
    protected boolean isFromPalette(Transferable transferable)
    {
        return transferable.isDataFlavorSupported(PaletteItem.FLAVOR);
    }
    
    protected boolean isFromProjectTree(Transferable transferable)
    {
        return transferable.isDataFlavorSupported(ADTransferable.ADDataFlavor);
    }
    
    protected INamedElement getElementFromPalette(Transferable transferable)
    throws UnsupportedFlavorException, IOException 
    {
        PaletteItem item = (PaletteItem) transferable.getTransferData(PaletteItem.FLAVOR);
        return item.createModelElement(getNamespace());
    }
    
    
    protected INamespace getNamespace()
    {
        return sceneNamespace;
    }

    public void addWidget(Point point, 
                           DesignerScene scene,
                           IPresentationElement presentation) 
                           throws UnsupportedFlavorException, IOException 
    {
            addWidget(point, scene, this.paletteItem, presentation);
    }
    
    private void addWidget(Point point, 
                           DesignerScene scene,
                           PaletteItem item, 
                           IPresentationElement presentation) 
        throws UnsupportedFlavorException, IOException
    {
        DiagramEngine engine = scene.getEngine();
        
        if (presentation != null)
        {
            Widget newWidget = engine.addWidget(presentation, point);
            Lookup lookup = newWidget.getLookup();
            WidgetViewManager manager = lookup.lookup(WidgetViewManager.class);
            if (manager != null)
            {
                String viewName = "";
                if (item != null)
                {
                    viewName = item.getDefaultViewName();
                }
                if ((viewName != null) && (viewName.length() > 0))
                {
                    manager.switchViewTo(viewName);
                }
            }
        }
    }

    protected void clearPalette(DesignerScene scene)
    {
        if(scene != null)
        {
            TopComponent topC = scene.getTopComponent();
            PaletteController controller = topC.getLookup().lookup(PaletteController.class);
            if(controller != null)
            {
                controller.setSelectedItem(Lookup.EMPTY, Lookup.EMPTY);
            }
        }
    }

    public IPresentationElement createModelElement (DesignerScene scene)
    {
         IPresentationElement presentation = null;
        if ( this.paletteItem !=  null)
        {
            presentation = createModelElement(this.paletteItem, scene);
        }
         return presentation;
    }
    
    private IPresentationElement createModelElement(PaletteItem item,DesignerScene scene)
    {
        INamedElement element = item.createModelElement(getNamespace());
        element=scene.getEngine().processDrop(element);

        IPresentationElement presentation = Util.createNodePresentationElement();
        presentation.addSubject(element);
        
        return presentation;
    }
    
    
    @Override
    public State mouseMoved(Widget widget, WidgetMouseEvent event)
    {
       return moved(widget, event.getPoint());
    }

    @Override
    public State mouseEntered(Widget widget, WidgetMouseEvent event)
    {
        return entered(widget, event.getPoint());
    }

    @Override
    public State mouseExited(Widget widget, WidgetMouseEvent event)
    {
       return exit(widget);
    }

    @Override
    public State dragEnter(Widget widget, WidgetDropTargetDragEvent event)
    {
        return entered(widget, event.getPoint());
    }

    private State entered(Widget widget, Point point)
    {
        if (paletteItem != null)
        {
            IPresentationElement presentation = Util.createNodePresentationElement();
            presentation.addSubject(element);
            DesignerScene scene = (DesignerScene) widget.getScene();
            scene.setBackgroundWidget(presentation, point, paletteItem.getDefaultViewName());
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    @Override
    public State dragExit(Widget widget, WidgetDropTargetEvent event)
    {
        return exit(widget);
    }

    private State exit(Widget widget)
    {
        DesignerScene scene = (DesignerScene) widget.getScene();
        scene.removeBackgroundWidget();
        return State.CONSUMED;
    }

    private State moved(Widget widget, Point point)
    {
        Widget w = ((DesignerScene) widget.getScene()).getBackgroundWidget();
        if (w != null)
        {
            // todo: waiting for fix to Widget.getBounds(), for now, position 
            // the widget upper left corner to point, rather than its center
//            Point location = new Point(point.x - w.getPreferredBounds().width/2, point.y - w.getPreferredBounds().height/2);
            Point location = point;
            w.setPreferredLocation(location);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }
    
    @Override
    public State mouseDragged(Widget widget, WidgetMouseEvent event)
    {
        return mouseMoved(widget, event);
    }

    public State dragOver(Widget widget, WidgetDropTargetDragEvent event)
    {
        return moved(widget, event.getPoint());
    }
    
    @Override
    public State drop(Widget widget, WidgetDropTargetDropEvent event)
    {
        State ret = State.REJECTED;
        if (paletteItem != null)
        {
            DesignerScene scene = null;
            if (widget.getScene() instanceof DesignerScene)
            {
                scene = (DesignerScene) widget.getScene();
            }
            IPresentationElement pe = createModelElement(paletteItem, scene);
            try
            {
                addWidget(event.getPoint(), scene, paletteItem, pe);

                scene.userSelectionSuggested(Collections.singleton(pe), false);
                scene.setFocusedObject(pe);

                ret = State.CONSUMED;
            } catch (Exception e)
            {
            } finally
            {
                clearPalette(scene);
                scene.removeBackgroundWidget();
            }
        }
        return ret;
    }
        
}