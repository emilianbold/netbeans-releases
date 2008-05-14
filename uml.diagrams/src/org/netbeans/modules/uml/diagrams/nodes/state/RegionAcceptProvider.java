/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.drawingarea.dataobject.PaletteItem;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.MoveWidgetTransferable;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 *
 * @author Sheryl Su
 */
public class RegionAcceptProvider implements AcceptProvider
{

    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable)
    {
        Transferable[] allTrans;
        if (transferable.isDataFlavorSupported(ExTransferable.multiFlavor))
        {
            try
            {
                MultiTransferObject transObj = (MultiTransferObject) transferable.getTransferData(ExTransferable.multiFlavor);
                allTrans = new Transferable[transObj.getCount()];
                for (int i = 0; i < allTrans.length; i++)
                {
                    allTrans[i] = transObj.getTransferableAt(i);
                }
            } catch (UnsupportedFlavorException ex)
            {
                return ConnectorState.REJECT_AND_STOP;
            } catch (IOException ex)
            {
                return ConnectorState.REJECT_AND_STOP;
            }
        } else
        {
            allTrans = new Transferable[] {transferable};
        }

        for (int i = 0; i < allTrans.length; i++)
        {
            Transferable t = allTrans[i];

            if (t.isDataFlavorSupported(PaletteItem.FLAVOR) && isSupportedElement(t))
            {
                continue;
            } else if (t.isDataFlavorSupported(ADTransferable.ADDataFlavor))
            {
                try
                {
                    ADTransferable.ADTransferData transferData = (ADTransferable.ADTransferData) t.getTransferData(ADTransferable.ADDataFlavor);
                    if (transferData.getModelElements().size() == 0 &&
                            transferData.getPresentationElements().size() == 0)
                    {
                        return ConnectorState.REJECT_AND_STOP;
                    }
                } catch (UnsupportedFlavorException ex)
                {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex)
                {
                    Exceptions.printStackTrace(ex);
                }
            }else if (t.isDataFlavorSupported(MoveWidgetTransferable.FLAVOR)) 
            {
                return ConnectorState.ACCEPT;
            }
            else
            {
                return ConnectorState.REJECT_AND_STOP;
            }
        }

        return ConnectorState.ACCEPT;
    }

    public void accept(Widget widget, Point point, Transferable transferable)
    {
        try
        {
            if (transferable.isDataFlavorSupported(PaletteItem.FLAVOR))
            {
                PaletteItem item = (PaletteItem) transferable.getTransferData(PaletteItem.FLAVOR);
                INamespace namespace = getNamespace(widget);
                if (namespace != null)
                {
                    INamedElement value = item.createModelElement(namespace);

                    IPresentationElement presentation = Util.createNodePresentationElement();
                    presentation.addSubject(value);
                    Widget w = ((DesignerScene) widget.getScene()).addNode(presentation);
                    w.removeFromParent();
                    widget.addChild(w);
                    w.setPreferredLocation(point);
                }
            }
            else if (transferable.isDataFlavorSupported(MoveWidgetTransferable.FLAVOR))
            {
                MoveWidgetTransferable t = (MoveWidgetTransferable)transferable.getTransferData(MoveWidgetTransferable.FLAVOR);
                Widget transferWidget = t.getWidget();
                Point p = widget.convertSceneToLocal(point);
             
                transferWidget.removeFromParent();
                IPresentationElement pe = (IPresentationElement)((DesignerScene) widget.getScene()).findObject(transferWidget);
                IPresentationElement thisPE = (IPresentationElement)((DesignerScene) widget.getScene()).findObject(widget);
                thisPE.getFirstSubject().addElement(pe.getFirstSubject());
    
                widget.addChild(transferWidget);
                transferWidget.setPreferredLocation(p);
            }
                

        } catch (Exception ex)
        {
        }
    }

    private boolean isSupportedElement(Transferable transferable)
    {
        try
        {
            PaletteItem item = (PaletteItem) transferable.getTransferData(PaletteItem.FLAVOR);
            Object value = FactoryRetriever.instance().createType(item.getElementType(), null);
            if (value instanceof IStateVertex || value instanceof IComment)
            {
                return true;
            }
        } catch (Exception ex)
        {
            return false;
        }
        return false;
    }

    private INamespace getNamespace(Widget widget)
    {
        ObjectScene scene = (ObjectScene) widget.getScene();
        IPresentationElement pe = (IPresentationElement) scene.findObject(widget);
        IElement region = pe.getFirstSubject();
        if (region instanceof IRegion)
        {
            return (IRegion) region;
        } else
        {
            return null;
        }
    }
}
