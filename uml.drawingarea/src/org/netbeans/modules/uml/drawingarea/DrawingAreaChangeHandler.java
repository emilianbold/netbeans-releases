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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TaggedValue;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author thuy
 */
public class DrawingAreaChangeHandler implements DrawingAreaChangeListener
{

    private UMLDiagramTopComponent umlTopComponent;
    private DesignerScene scene;

    public DrawingAreaChangeHandler(UMLDiagramTopComponent topComponent)
    {
        umlTopComponent = topComponent;
        scene = umlTopComponent.getScene();
    }

    public void elementChanged(IElement changedElement,
                                IElement secondaryElement,
                                ModelElementChangedKind changeType)
    {
        IElement elementToNotify = changedElement;

        // We may want to put this kind of logic into the diagram engines
        if ((changedElement instanceof IFeature) &&
                (changeType == ModelElementChangedKind.DELETE))
        {
            secondaryElement = changedElement;
            changedElement = changedElement.getOwner();
            elementToNotify = changedElement;
        } 
        // fixed issues 139540, 138859, 135078, 149000
        else if (changedElement instanceof IParameter &&
                (changeType == ModelElementChangedKind.NAME_MODIFIED ||
                 changeType == ModelElementChangedKind.TYPEMODIFIED ||
                 changeType == ModelElementChangedKind.MULTIPLICITYMODIFIED))
        {
            IElement owner = changedElement.getOwner();
            if (owner instanceof IOperation)
            {
                changedElement = owner;
                elementToNotify = changedElement;
            }
        }
        else if ( changedElement instanceof TaggedValue &&
                changeType == ModelElementChangedKind.ELEMENTMODIFIED)
        {
            changedElement = changedElement.getOwner();
            elementToNotify = changedElement;
        }
        // A secondary element is a child element of the chagned element.
        // For example, an attribute would be a secondary element.
        List<IPresentationElement> presentations = umlTopComponent.getPresentationElements(elementToNotify);

        if ((changeType != ModelElementChangedKind.DELETE) &&
                (changeType != ModelElementChangedKind.PRE_DELETE))//update parent only if it's update event, not a delete one
        {
            if (changedElement instanceof IUMLBinding)//common approach was cause of number of regressions, so better to specify objects which require to update parents in current realization
            {
                for (IElement el = elementToNotify.getOwner(); el != null && !(el instanceof IProject) && (presentations == null || presentations.size() == 0); el = el.getOwner())
                {
                    presentations = umlTopComponent.getPresentationElements(el);//sometimes child elements are presented on a diagram but do not have presentations, update parent to get child updated
                //for example binding elements are childs on template binding, and updated this way
                }
            }
        }

        Object oldValue = null;
        Object newValue = null;

        if (changeType == ModelElementChangedKind.FEATUREADDED)
        {
            newValue = secondaryElement;
        } else if ((changeType == ModelElementChangedKind.FEATUREMOVED) ||
                (changeType == ModelElementChangedKind.DELETE) ||
                changeType == ModelElementChangedKind.PRE_DELETE)
        {
            oldValue = secondaryElement;
        } else if (changeType == ModelElementChangedKind.REDEFINED_OWNER_NAME_CHANGED)
        {
            newValue = secondaryElement;
        } else if (secondaryElement != null)
        {
            List<IPresentationElement> secondaryPres = umlTopComponent.getPresentationElements(secondaryElement);
            if ((secondaryPres != null) && (secondaryPres.size() > 0))
            {
                presentations = secondaryPres;
            }
            elementToNotify = secondaryElement;

            // If we have a partfacade we need to see if we're playing in one
            // or more design pattherns (collaborations).  If so those design
            // patterns need to update their template parameters compartment
            if (secondaryElement instanceof IConnectableElement)
            {
                // Find all the roles this guy plays a part in and notify the
                // contexts - these contexts should be the collaborations.
                IConnectableElement connect = (IConnectableElement) secondaryElement;
                umlTopComponent.addDesignPatterns(presentations, connect);
            }
        }

        ContextPaletteManager manager = scene.getContextPaletteManager();
        if (manager != null)
        {
            manager.cancelPalette();
        }

        for (IPresentationElement curPresentation : presentations)
        {
            Widget changedWidget = scene.findWidget(curPresentation);
            if (changedWidget != null)
            {
                if (((changeType == ModelElementChangedKind.DELETE) ||
                        (changeType == ModelElementChangedKind.PRE_DELETE)) &&
                        secondaryElement == null)
                {
                    if (changedWidget instanceof UMLNodeWidget)
                    {
                        ((UMLNodeWidget) changedWidget).remove();
                    } else if (changedWidget instanceof UMLEdgeWidget)
                    {
                        ((UMLEdgeWidget) changedWidget).remove();
                    } else
                    {
                        Widget parentWidget = changedWidget.getParentWidget();
                        if (parentWidget != null)
                        {
                            parentWidget.removeChild(changedWidget);
                            curPresentation.removeSubject(curPresentation.getFirstSubject());
                        }
                        // why not send change event to changed widget to handle further task?
                        // see use case for deleting state region
                        PropertyChangeEvent event = new PropertyChangeEvent(elementToNotify,
                                                                            changeType.toString(),
                                                                            oldValue,
                                                                            newValue);
                        if (changedWidget instanceof PropertyChangeListener)
                        {
                            PropertyChangeListener listener = (PropertyChangeListener) changedWidget;
                            listener.propertyChange(event);
                            umlTopComponent.setDiagramDirty(true);
                        }
                    }
                } // We do not want to actually delete on a pre_delete, well
                // because the delete could be canceled.  Also if we delete
                // during a pre_delete, the presentation element will be 
                // missing when we go to actually delete the element.  
                // Therefore we will end up deleting the owning element
                // (if there is an owning element).
                //
                // TODO: I will have to handle when a feature is filtered out.
                else if (changeType != ModelElementChangedKind.PRE_DELETE)
                {
                    PropertyChangeEvent event = new PropertyChangeEvent(elementToNotify,
                                                                        changeType.toString(),
                                                                        oldValue,
                                                                        newValue);
                    if (changedWidget instanceof PropertyChangeListener)
                    {
                        PropertyChangeListener listener = (PropertyChangeListener) changedWidget;
                        listener.propertyChange(event);
                        umlTopComponent.setDiagramDirty(true);
                    }
                }
            }
        }

        // Need to figure out a way to be a little smarter.  I really want
        // to wait until all events are done.
        scene.validate();

        if (manager != null)
        {
            manager.selectionChanged(null);
        }
    }
}


