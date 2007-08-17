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



package org.netbeans.modules.uml.palette;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.util.Lookup;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.modules.uml.palette.model.ModelingPaletteNodeDescriptor;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectStateEnteredSink;
import java.util.Enumeration;
import org.openide.nodes.Children;


/**
 *
 * @author Thuy Nguyen
 */
public class PaletteSupport {  
    
    /**
     * Creates a new instance of PaletteSupport
     */
    public PaletteSupport() {
    }
    
    private PaletteController pController = null;
    private PalettePropertyChangeListener propertyChangeListener = null;

    public PaletteController getPalette(IDrawingAreaControl drawingAreaControl) {
        
        // get a string representing the diagram kind given a drawingAreaControl object
        String paletteFolderName = findPaletteRepository(drawingAreaControl.getDiagramKind());   
        pController = UMLPaletteFactory.getPalette(paletteFolderName);
        
        // registers a PropertyChangeListner to the palette controller object
        propertyChangeListener = new PalettePropertyChangeListener(drawingAreaControl);
        pController.addPropertyChangeListener(propertyChangeListener);
        initPaletteSettings();
        
        drawingAreaControl.addDrawingAreaToolSelectionSink(new DrawingAreaSelectStateEnteredListener(pController, propertyChangeListener));
       
        return pController;
    }
    
    public void unregisterListeners() {
        if (pController != null) {
	    pController.removePropertyChangeListener(propertyChangeListener);
	}
	if (propertyChangeListener != null) {
	    propertyChangeListener.setDrawingAreaControl(null);
	    propertyChangeListener = null;
	}
    }
    
    private String findPaletteRepository(int diagramType) {
        String repository = null;
        
        switch(diagramType) {
            case IDiagramKind.DK_CLASS_DIAGRAM:
                repository = "UML/Palette/ClassDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_ACTIVITY_DIAGRAM:
                repository = "UML/Palette/ActivityDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_COLLABORATION_DIAGRAM:
                repository = "UML/Palette/CollaborationDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_COMPONENT_DIAGRAM:
                repository = "UML/Palette/ComponentDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_DEPLOYMENT_DIAGRAM:
                repository = "UML/Palette/DeploymentDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_SEQUENCE_DIAGRAM:
                repository = "UML/Palette/SequenceDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_STATE_DIAGRAM:
                repository = "UML/Palette/StateDiagram"; // NOI18N
                break;
            case IDiagramKind.DK_USECASE_DIAGRAM:
                repository = "UML/Palette/UsecaseDiagram"; // NOI18N
                break;                
        }
        return repository;
    }
    
    private void initPaletteSettings (){
        Lookup nodeLkup = pController.getRoot();
        Node rootNode = (Node) nodeLkup.lookup(Node.class);
        if (rootNode == null )
            return;
        Children categories = rootNode.getChildren();
        if (categories != null) {
            Node cat = null;
            Enumeration enumNodes = categories.nodes();
            while (enumNodes.hasMoreElements()) {
                cat = (Node) enumNodes.nextElement();
                if (cat != null) {
                    // expand the category node initially
                    cat.setValue(PaletteController.ATTR_IS_EXPANDED, true);
                }
            }
        }   
    }
    
    
    private class PalettePropertyChangeListener implements PropertyChangeListener {
        
        private IDrawingAreaControl drawingAreaContrl1;
       
        public PalettePropertyChangeListener (IDrawingAreaControl daController) {
            drawingAreaContrl1 = daController;
        }
        
	public void setDrawingAreaControl(IDrawingAreaControl daController) {
	    drawingAreaContrl1 = daController;
	}

        public void propertyChange(PropertyChangeEvent event) {
            String propName = event.getPropertyName();
            Log.out("PalettePropertyChangeListener():propertyChange(): event is on " + propName); // NOI18N
            
            if (PaletteController.PROP_SELECTED_ITEM.equals(propName)) {
                
                Object newVal = event.getNewValue();
                Log.out("PalettePropertyChangeListener():propertyChange(): Current selection is " + newVal); // NOI18N
                //System.out.println("PalettePropertyChangeListener():propertyChange(): Current selection is " + newVal);
                
                if(newVal != null) {
                    if (newVal instanceof Lookup) {
                        Lookup lkup = (Lookup) newVal;
                        
                        Node selectedNode = (Node) lkup.lookup(Node.class);
                        
                        if (selectedNode == null) {    // when the same node is selected. i.e. the node is deselected                
                            drawingAreaContrl1.enterMode(IDrawingToolKind.DTK_SELECTION);
                            return;
                        }
                        
                        InstanceCookie ic = (InstanceCookie) selectedNode.getCookie(InstanceCookie.class);
                        if(ic != null) {
                            Object obj = null;
                            try {
                                obj = ic.instanceCreate();
                            } catch (Exception ex) {
                                Log.out("PalettePropertyChangeListener(): Error : " + ex); // NOI18N
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                            
                            if(obj instanceof ModelingPaletteNodeDescriptor) {
                                ModelingPaletteNodeDescriptor umlPaletteNodeDescriptor = (ModelingPaletteNodeDescriptor) obj;
                                String paletteItemID = umlPaletteNodeDescriptor.getButtonID();
                                Log.out("Button ID is " + paletteItemID); // NOI18N
                                //System.out.println("Button ID is " + paletteItemID);
                                
                                if(drawingAreaContrl1 != null) {
                                    drawingAreaContrl1.setSelectedPaletteButton(paletteItemID);
                                    //System.out.println("PalettePropertyChangeListener(): paletteItemID="+drawingAreaContrl.getSelectedPaletteButton());
                                    Log.out("PalettePropertyChangeListener(): Calling entermodefromButton()"); // NOI18N
                                    drawingAreaContrl1.enterModeFromButton(paletteItemID);
                                }
                            }
                        }
                    }
                } else {    // No item is selected.
                    // set the state of the graph window to select
                    drawingAreaContrl1.enterMode(IDrawingToolKind.DTK_SELECTION);
                }
            }
        }
    }
    
    
    
    private class DrawingAreaSelectStateEnteredListener implements IDrawingAreaSelectStateEnteredSink {
        
        PaletteController paletteController;
        PalettePropertyChangeListener listener;
        
        public DrawingAreaSelectStateEnteredListener(PaletteController pController, PalettePropertyChangeListener pListener) {
            paletteController = pController;
            listener = pListener;
        }
        
        // Reset the palette so that no item is selected.
        public void onSelectToolSelected() {
            Log.out("CreatePalette():DrawingAreaSelectStateEnteredListener(): resetting state");
                        
            paletteController.removePropertyChangeListener(listener);
            paletteController.setSelectedItem(null, null);
            paletteController.addPropertyChangeListener(listener); 
        }
    }
}




