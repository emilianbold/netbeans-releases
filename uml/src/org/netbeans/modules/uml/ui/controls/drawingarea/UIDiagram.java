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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import java.awt.Frame;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.Diagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.ITwoPhaseCommit;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import com.tomsawyer.editor.TSEGraph;
import javax.swing.SwingUtilities;

/**
 * @author sumitabhk
 *
 */
public class UIDiagram extends Diagram implements IUIDiagram, IDrawingPropertyProvider {
    //private IDrawingAreaControl m_RawDrawingAreaControl = null;
    private WeakReference m_RawDrawingAreaControl = null;
    
    /**
     *
     */
    public UIDiagram() {
        super();
    }
    
    /**
     * The is the parent diagram that implements the diagram.
     */
    public void setDrawingArea(IDrawingAreaControl newVal) {
        m_RawDrawingAreaControl = null;
        m_RawDrawingAreaControl = new WeakReference(newVal);
    }
    
    /**
     * The is the parent diagram that implements the diagram.
     */
    public IDrawingAreaControl getDrawingArea() {
        return (IDrawingAreaControl)m_RawDrawingAreaControl.get();
    }
    
    /**
     * Saves the diagram
     */
    public void save() {
        if (m_RawDrawingAreaControl != null) {
            final IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                //Removing the asynch execution of ctrl.save() on the AWT event dispatching thread. 
                //This was originally a workaround for the 
                //ConcurrentModificationException described in bug 6283146 and committed in 
                //IN 75828. It turns out the changes made in REGUIAddin and ADDrawingAreaControl
                //are sufficient for the workaround.
                //This caused bug 6299963
                /*
                SwingUtilities.invokeLater(
                        new Runnable(){
                    public void run(){
                        ctrl.save();
                    }
                }
                );
                 */
                if (ctrl.getIsDirty())
                    ctrl.save();
                setIsDirty(false);
            }
        }
    }
    
    /**
     * Allows the diagram to perform some cleanup before the diagram is actually
     * closed.
     */
    public void preClose() {
        ETSystem.out.println("UIDiagram preClose()");
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.preClose();
            }
        }
    }
    
    /**
     * Is this diagram readonly?
     *
     * @param pVal [out,retval] true if the diagram is readonly
     */
    public boolean getReadOnly() {
        //return m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getReadOnly() : false;
        boolean retVal = false;
        
        IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
        if(ctrl != null) {
            retVal = ctrl.getReadOnly();
        }
        
        return retVal;
    }
    
    /**
     * Is this diagram readonly?
     *
     * @param newVal [in] true to make the diagram readonly
     */
    public void setReadOnly(boolean value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setReadOnly(value);
            }
        }
    }
    
    /**
     * Saves the diagram as a graphic
     */
    public void saveAsGraphic(String sFilename, int nKind) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.saveAsGraphic(sFilename, nKind);
            }
        }
    }
    
    /**
     * Saves the diagram as a graphic
     */
    public IGraphicExportDetails saveAsGraphic2(String sFilename, int nKind) {
        return saveAsGraphic2(sFilename, nKind, 1);
    }
    
    /**
     * Saves the diagram as a graphic
     */
    public IGraphicExportDetails saveAsGraphic2(String sFilename, int nKind, double scale) {
        IGraphicExportDetails retVal = null;
        
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.saveAsGraphic2(sFilename, nKind, scale);
            }
        }
        
        return retVal;
    }
	
    public String getName() {
        //return m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getName() : "";
        String retVal = "";
        
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getName();
            }
        }
        
        return retVal;
    }
    
    public void setName(String value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setName(value);
            }
        }
    }
    
    public String getAlias() {
        String retVal = "";
        //return m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getAlias() : "";
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAlias();
            }
        }
        
        return retVal;
    }
    
    public void setAlias(String value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setAlias(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getNameWithAlias()
         */
    public String getNameWithAlias() {
        String retVal = "";
        
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getNameWithAlias();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setNameWithAlias(java.lang.String)
         */
    public void setNameWithAlias(String value) {
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setNameWithAlias(value);
            }
        }
    }
    
    public String getQualifiedName() {
        String retVal = "";
        
        //return m_RawDrawingAreaControl != null ?  m_RawDrawingAreaControl.getQualifiedName() : "";
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getQualifiedName();
            }
        }
        
        return retVal;
    }
    
    public String getFilename() {
        String retVal = "";
        
        //return  m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getFilename() : "";
        if(m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getFilename();
            }
        }
        
        return retVal;
    }
    
    public void setLayoutStyle(int value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setLayoutStyle(value);
            }
        }
    }
    
    public int getLayoutStyle() {
        //return  m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getLayoutStyle() : ILayoutKind.LK_UNKNOWN_LAYOUT;
        int retVal = ILayoutKind.LK_UNKNOWN_LAYOUT;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getLayoutStyle();
            }
        }
        
        return retVal;
    }
    
    public void setLayoutStyleSilently(int value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setLayoutStyleSilently(value);
            }
        }
    }
    
    public void immediatelySetLayoutStyle(int nLayoutStyle, boolean bSilent) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.immediatelySetLayoutStyle(nLayoutStyle, bSilent);
            }
        }
    }
    
    public void delayedLayoutStyle(int nLayoutStyle, boolean bIgnoreContainment) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.delayedLayoutStyle(nLayoutStyle, bIgnoreContainment);
            }
        }
    }
    
    public void showImageDialog() {
//		if (m_RawDrawingAreaControl != null)
//		{
//			m_RawDrawingAreaControl.showImageDialog();
//		}
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.showImageDialog();
            }
        }
    }
    
    public void printPreview(String sTitle, boolean bCanMoveParent) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.printPreview(sTitle, bCanMoveParent);
            }
        }
    }
    
    public void loadPrintSetupDialog() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.loadPrintSetupDialog();
            }
        }
    }
    
    public void printGraph(boolean bShowDialog) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.printGraph(bShowDialog);
            }
        }
    }
    
    public boolean getLayoutRunning() {
        boolean retVal = false;
        
        //return  m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getLayoutRunning() : false;
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getLayoutRunning();
            }
        }
        
        return retVal;
    }
    
    public double getCurrentZoom() {
        //return  m_RawDrawingAreaControl != null ? m_RawDrawingAreaControl.getCurrentZoom() : 0.0;
        double retVal = 0.0;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getCurrentZoom();
            }
        }
        
        return retVal;
    }
    
        /*
         * Param one is the min Zoom level and param two is the max zoom level inclusive.
         */
    public ETPairT<Double, Double> getExtremeZoomValues() {
        return new ETPairT<Double, Double>(new Double(.01), new Double(4.0));
    }
    
    public void zoom(double nScaleFactor) {
//		if (m_RawDrawingAreaControl != null)
//		{
//			m_RawDrawingAreaControl.zoom(nScaleFactor);
//		}
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.zoom(nScaleFactor);
            }
        }
    }
    
    public void zoomIn() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.zoomIn();
            }
        }
    }
    
    public void zoomOut() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.zoomOut();
            }
        }
    }
    
    public void fitInWindow() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.fitInWindow();
            }
        }
    }
    
    public void onCustomZoom() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.onCustomZoom();
            }
        }
    }
    
    public INamespace getNamespace() {
        INamespace retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getNamespace();
            }
        }
        
        return retVal;
    }
    
    public void setNamespace(INamespace value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setNamespace(value);
            }
        }
    }
    
    public INamespace getNamespaceForCreatedElements() {
        INamespace retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getNamespaceForCreatedElements();
            }
        }
        
        return retVal;
    }
    
    public int load(String sFilename) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.load(sFilename);
            }
        }
        return 0;
    }
    
    public void enterMode(int nDrawingToolKind) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.enterMode(nDrawingToolKind);
            }
        }
    }
    
    public void enterModeFromButton(String sButtonID) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.enterModeFromButton(sButtonID);
            }
        }
    }
    
    public void refresh(boolean bPostMessage) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.refresh(bPostMessage);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#cut()
         */
    public void cut() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.cut();
            }
        }
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#copy()
         */
    public void copy() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.copy();
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#paste()
         */
    public void paste() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.paste();
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#clearClipboard()
         */
    public void clearClipboard() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.clearClipboard();
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#deleteSelected(boolean)
         */
    public void deleteSelected(boolean bAskUser) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.deleteSelected(bAskUser);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#itemsOnClipboard(boolean)
         */
    public void itemsOnClipboard(boolean bItemsOnClipboard) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.itemsOnClipboard();
            }
        }
    }
    
    /**
     * Selects all the items on the diagram
     */
    public void selectAll(boolean bSelect) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.selectAll(bSelect);
            }
        }
    }
    
    /**
     * Selects all the similar items on the diagram
     */
    public void selectAllSimilar() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.selectAllSimilar();
            }
        }
    }
    
    /**
     * Transforms a rect from logical coordinates to device coordinates.
     */
    public IETRect logicalToDeviceRect(IETRect rcLogical) {
        IETRect retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.logicalToDeviceRect(rcLogical);
            }
        }
        
        return retVal;
    }
    
    /**
     * Transforms a point from logical coordinates to device coordinates.
     */
    public IETPoint logicalToDevicePoint(IETPoint ptLogical) {
        IETPoint retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.logicalToDevicePoint(ptLogical);
            }
        }
        
        return retVal;
    }
    
    /**
     * Transforms a rect from device coordinates to logical coordinates.
     */
    public IETRect deviceToLogicalRect(IETRect rcDevice) {
        IETRect retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.deviceToLogicalRect(rcDevice);
            }
        }
        
        return retVal;
    }
    
    /**
     * Transforms a point from device coordinates to logical coordinates.
     */
    public IETPoint deviceToLogicalPoint(IETPoint ptDevice) {
        IETPoint retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.deviceToLogicalPoint(ptDevice);
            }
        }
        
        return retVal;
    }
    
    /**
     * Transforms a point from device coordinates to logical coordinates.
     */
    public IETPoint deviceToLogicalPoint(int x, int y) {
        IETPoint retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.deviceToLogicalPoint(x, y);
            }
        }
        
        return retVal;
    }
    
    /**
     * Centers the diagram on this presentation object
     */
    public void centerPresentationElement(IPresentationElement pPresentationElement, boolean bSelectIt, boolean bDeselectAllOthers) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.centerPresentationElement(pPresentationElement, bSelectIt, bDeselectAllOthers);
            }
        }
    }
    
    /**
     * Centers the diagram on this presentation object
     */
    public void centerPresentationElement2(String sXMIID, boolean bSelectIt, boolean bDeselectAllOthers) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.centerPresentationElement2(sXMIID, bSelectIt, bDeselectAllOthers);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isStackingCommandAllowed(int)
         */
    public boolean isStackingCommandAllowed(int nStackingCommand) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.isStackingCommandAllowed(nStackingCommand);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#executeStackingCommand(int)
         */
    public void executeStackingCommand(int nStackingCommand) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.executeStackingCommand(nStackingCommand, true);
            }
        }
    }
    
    public void hasGraphObjects(boolean bHasObjects) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.hasGraphObjects(bHasObjects);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getDiagramKind()
         */
    public int getDiagramKind() {
        int retVal = IDiagramKind.DK_UNKNOWN;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getDiagramKind();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setDiagramKind(int)
         */
    public void setDiagramKind(int value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setDiagramKind(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getDiagramKind2()
         */
    public String getDiagramKind2() {
        String retVal = "";
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getDiagramKind2();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setDiagramKind2(java.lang.String)
         */
    public void setDiagramKind2(String value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setDiagramKind2(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#initializeNewDiagram(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, java.lang.String, int)
         */
    public void initializeNewDiagram(INamespace pNamespace, String sName, int pKind) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.initializeNewDiagram(pNamespace, sName, pKind);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#invertSelection()
         */
    public void invertSelection() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.invertSelection();
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getSelected()
         */
    public ETList<IPresentationElement> getSelected() {
        ETList < IPresentationElement > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getSelected();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getSelectedByType(java.lang.String)
         */
    public ETList<IPresentationElement> getSelectedByType(String bstrType) {
        ETList < IPresentationElement > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getSelectedByType(bstrType);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getWindowHandle()
         */
    public Frame getWindowHandle() {
        Frame retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.getWindowHandle();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllItems()
         */
    public ETList<IPresentationElement> getAllItems() {
        ETList < IPresentationElement > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAllItems();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllItems2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public ETList<IPresentationElement> getAllItems2(IElement pModelElement) {
        ETList < IPresentationElement > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAllItems2(pModelElement);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllByType(java.lang.String)
         */
    public ETList<IPresentationElement> getAllByType(String bstrType) {
        ETList < IPresentationElement > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAllByType(bstrType);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getIsLayoutPropertiesDialogOpen()
         */
    public boolean getIsLayoutPropertiesDialogOpen() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getIsLayoutPropertiesDialogOpen();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#layoutPropertiesDialog(boolean)
         */
    public void layoutPropertiesDialog(boolean bShow) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.layoutPropertiesDialog(bShow);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getIsGraphPreferencesDialogOpen()
         */
    public boolean getIsGraphPreferencesDialogOpen() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getIsGraphPreferencesDialogOpen();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#graphPreferencesDialog(boolean)
         */
    public void graphPreferencesDialog(boolean bShow) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.graphPreferencesDialog(bShow);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getIsOverviewWindowOpen()
         */
    public boolean getIsOverviewWindowOpen() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getIsOverviewWindowOpen();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#overviewWindow(boolean)
         */
    public void overviewWindow(boolean bShowIt) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.overviewWindow(bShowIt);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getOverviewWindowRect(int, int, int, int)
         */
    public Rectangle getOverviewWindowRect(int pLeft, int pTop, int pWidth, int pHeight) {
//		Rectangle retObj = null;
//		if (m_RawDrawingAreaControl != null)
//		{
////			retObj = m_RawDrawingAreaControl.getOverviewWindowRect(pLeft, pTop, pWidth, pHeight);
//		}
//		return retObj;
        
        Rectangle retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.getOverviewWindowRect();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setOverviewWindowRect(int, int, int, int)
         */
    public void setOverviewWindowRect(int nLeft, int nTop, int nWidth, int nHeight) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setOverviewWindowRect(nLeft, nTop, nWidth, nHeight);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAreTooltipsEnabled()
         */
    public boolean getAreTooltipsEnabled() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAreTooltipsEnabled();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setEnableTooltips(boolean)
         */
    public void setEnableTooltips(boolean bEnable) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setEnableTooltips(bEnable);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getHasSelected(boolean)
         */
    public boolean getHasSelected(boolean bDeep) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getHasSelected(bDeep);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getHasSelectedNodes(boolean)
         */
    public boolean getHasSelectedNodes(boolean bDeep) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getHasSelectedNodes(bDeep);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getShowGrid()
         */
    public boolean getShowGrid() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getShowGrid();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setShowGrid(boolean)
         */
    public void setShowGrid(boolean value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setShowGrid(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getGridSize()
         */
    public int getGridSize() {
        int retVal = 0;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getGridSize();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setGridSize(int)
         */
    public void setGridSize(int value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setGridSize(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getGridType()
         */
    public int getGridType() {
        int retVal = 0;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getGridType();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setGridType(int)
         */
    public void setGridType(int value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setGridType(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getTwoPhaseCommit()
         */
    public ITwoPhaseCommit getTwoPhaseCommit() {
        ITwoPhaseCommit retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
//            retVal = ctrl.getTwoPhaseCommit();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getModeLocked()
         */
    public boolean getModeLocked() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getModeLocked();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setModeLocked(boolean)
         */
    public void setModeLocked(boolean value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setModeLocked(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getLastSelectedButton()
         */
    public int getLastSelectedButton() {
        int retVal = 0;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getLastSelectedButton();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setLastSelectedButton(int)
         */
    public void setLastSelectedButton(int value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setLastSelectedButton(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#validateDiagram(boolean, org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation)
         */
    public IDiagramValidationResult validateDiagram(boolean bOnlySelectedElements, IDiagramValidation pDiagramValidation) {
        IDiagramValidationResult retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.validateDiagram(bOnlySelectedElements, pDiagramValidation);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#syncElements(boolean)
         */
    public void syncElements(boolean bOnlySelectedElements) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.syncElements(bOnlySelectedElements);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setFocus()
         */
    public void setFocus() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setFocus();
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#reconnectLink(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
         */
    public boolean reconnectLink(IPresentationElement pLink, IPresentationElement pFromNode, IPresentationElement pToNode) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.reconnectLink(pLink, pFromNode, pToNode);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#sizeToContents(boolean)
         */
    public void sizeToContents(boolean bJustSelectedElements) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.sizeToContents(bJustSelectedElements);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#postDelayedAction(org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction)
         */
    public void postDelayedAction(IDelayedAction pAction) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.postDelayedAction(pAction);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#receiveBroadcast(org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction)
         */
    public void receiveBroadcast(IBroadcastAction pAction) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.receiveBroadcast(pAction);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getIsDirty()
         */
    public boolean getIsDirty() {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getIsDirty();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setIsDirty(boolean)
         */
    public void setIsDirty(boolean value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setIsDirty(value);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isSame(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
         */
    public boolean isSame(IDiagram pDiagram) {
        if (pDiagram != null) {
            String thisFilename = getFilename();
            String otherFilename = pDiagram.getFilename();
            return thisFilename != null && otherFilename != null && thisFilename.equals(otherFilename);
        }
        return false;
    }
    
    /**
     * Search for, and return if found, the presentation element on this diagram that has the specified xmi.id
     *
     * @param sXMIID [in] The xmi.id of the presentation to search for, and find
     * @param pPresentationElement [out,retval] Returns the presentation element on the diagram with the specified xmi.id
     *
     * @return HRESULT
     */
    public IPresentationElement findPresentationElement(String sXMLID) {
        IPresentationElement retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.findPresentationElement(sXMLID);
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns the relationship discovery object
     *
     * @param pDiscoverer [out,retval] Returns the relationship discovery object
     *
     * @return HRESULT
     */
    public ICoreRelationshipDiscovery getRelationshipDiscovery() {
        ICoreRelationshipDiscovery retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getRelationshipDiscovery();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#pumpMessages(boolean)
         */
    public void pumpMessages(boolean bJustDrawingMessages) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.pumpMessages(bJustDrawingMessages);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedDiagram(java.lang.String)
         */
    public void addAssociatedDiagram(String sDiagramXMIID) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.addAssociatedDiagram(sDiagramXMIID);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
         */
    public void addAssociatedDiagram2(IProxyDiagram pDiagram) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.addAssociatedDiagram2(pDiagram);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedDiagram(java.lang.String)
         */
    public void removeAssociatedDiagram(String sDiagramXMIID) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.removeAssociatedDiagram(sDiagramXMIID);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
         */
    public void removeAssociatedDiagram2(IProxyDiagram pDiagram) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.removeAssociatedDiagram2(pDiagram);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedDiagram(java.lang.String)
         */
    public boolean isAssociatedDiagram(String sDiagramXMIID) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.isAssociatedDiagram(sDiagramXMIID);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
         */
    public boolean isAssociatedDiagram2(IProxyDiagram pDiagram) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.isAssociatedDiagram2(pDiagram);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedElement(java.lang.String, java.lang.String)
         */
    public void addAssociatedElement(String sTopLevelElementXMIID, String sModelElementXMIID) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.addAssociatedElement(sTopLevelElementXMIID, sModelElementXMIID);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#addAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public void addAssociatedElement2(IElement pElement) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.addAssociatedElement2(pElement);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedElement(java.lang.String, java.lang.String)
         */
    public void removeAssociatedElement(String sTopLevelElementXMIID, String sModelElementXMIID) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.removeAssociatedElement(sTopLevelElementXMIID, sModelElementXMIID);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#removeAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public void removeAssociatedElement2(IElement pElement) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.removeAssociatedElement2(pElement);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedElement(java.lang.String)
         */
    public boolean isAssociatedElement(String sModelElementXMIID) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.isAssociatedElement(sModelElementXMIID);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#isAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
    public boolean isAssociatedElement2(IElement pElement) {
        boolean retVal = false;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.isAssociatedElement2(pElement);
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAssociatedDiagrams()
         */
    public ETList<IProxyDiagram> getAssociatedDiagrams() {
        ETList < IProxyDiagram > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAssociatedDiagrams();
            }
        }
        
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAssociatedElements()
         */
    public ETList<IElement> getAssociatedElements() {
        ETList < IElement > retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAssociatedElements();
            }
        }
        
        return retVal;
    }
    
    public ETList<IElement> getAllItems3() {
        ETList < IElement > retVal = null;
        
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getAllItems3();
            }
        }
        
        return retVal;
    }
    
    public ETList<IElement> getElements() {
        return getAllItems3();
    }
    
    public IProject getProject() {
        IProject retVal = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getProject();
            }
        }
        
        return retVal;
    }
    
    public String getDocumentation() {
        String retVal = "";
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                retVal = ctrl.getDocumentation();
            }
        }
        
        return retVal;
    }
    
    public void setDocumentation(String newDoc) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setDocumentation(newDoc);
            }
        }
    }
    
    public IElement getOwner() {
        return getNamespace();
    }
    
    public void setOwner(IElement newOwner) {
        if (newOwner != null && newOwner instanceof INamespace) {
            setNamespace((INamespace)newOwner);
        }
    }
    
    // IDrawingPropertyProvider
    public ETList<IDrawingProperty> getDrawingProperties() {
        ETList<IDrawingProperty> pProperties = null;
        
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProperties = pProvider.getDrawingProperties();
        }
        
        return pProperties;
    }
    
    public void saveColor(String sDrawEngineType, String sResourceName, int nColor) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.saveColor(sDrawEngineType, sResourceName, nColor);
        }
    }
    
    public void saveColor2(IColorProperty pProperty) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.saveColor2(pProperty);
        }
    }
    
    public void saveFont(  String sDrawEngineName,
            String sResourceName,
            String sFaceName,
            int nHeight,
            int nWeight,
            boolean bItalic,
            int nColor) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.saveFont(sDrawEngineName,
                    sResourceName,
                    sFaceName,
                    nHeight,
                    nWeight,
                    bItalic,
                    nColor);
        }
    }
    
    public void saveFont2(IFontProperty pProperty) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.saveFont2(pProperty);
        }
    }
    
    public void resetToDefaultResource( String sDrawEngineName,
            String sResourceName,
            String sResourceType) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.resetToDefaultResource( sDrawEngineName,
                    sResourceName,
                    sResourceType);
        }
    }
    
    public void resetToDefaultResources() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.resetToDefaultResources();
        }
    }
    
    public void resetToDefaultResources2(String sDrawEngineName) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.resetToDefaultResources2(sDrawEngineName);
        }
    }
    
    public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.dumpToFile(sFile, bDumpChildren, bAppendToExistingFile);
        }
    }
    
    public boolean displayFontDialog(IFontProperty pProperty) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            return pProvider.displayFontDialog(pProperty);
        } else {
            return false;
        }
    }
    
    public boolean displayColorDialog(IColorProperty pProperty) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            return pProvider.displayColorDialog(pProperty);
        } else {
            return false;
        }
        
    }
    
    public void invalidateProvider() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)ctrl;
            pProvider.invalidateProvider();
        }
    }
    
    /**
     * This method sets wheter the graph should be updated automatically or on
     * request.
     *
     * @param value <code>true</code> if the boudns should update automatcially.
     */
    public void setAutoUpdateBounds(boolean value) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                TSEGraph graph = ctrl.getGraphWindow().getGraph();
                graph.setBoundsUpdatingEnabled(value);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#getAllowRedraw()
    */
    public boolean getAllowRedraw() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null && ctrl.getGraphWindow() != null) {
                return ctrl.getGraphWindow().getAllowRedraw();
            }
        }
        return false;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram#setAllowRedraw(boolean)
    */
    public void setAllowRedraw(boolean allow) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null && ctrl.getGraphWindow() != null) {
                ctrl.getGraphWindow().setAllowRedraw(allow);
            }
        }
    }
    
        /*
         * Set when the diagrma is creating itself from selected elements.
         */
    public void setPopulating(boolean busy) {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                ctrl.setPopulating(busy);
            }
        }
    }
    
        /*
         * Returns if the is busy populating
         */
    public boolean getPopulating() {
        if (m_RawDrawingAreaControl != null) {
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null) {
                return ctrl.getPopulating();
            }
        }
        return false;
    }
	
	public double getFrameWidth()
	{
		return getDrawingArea().getGraphWindow().getGraph().getFrameBounds().getWidth();
	}
	
	public double getFrameHeight()
	{
		return getDrawingArea().getGraphWindow().getGraph().getFrameBounds().getHeight();
	}
}

