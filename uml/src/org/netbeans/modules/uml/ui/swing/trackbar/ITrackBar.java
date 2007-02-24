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



package org.netbeans.modules.uml.ui.swing.trackbar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;

/**
 *
 * @author Trey Spiva
 */
public interface ITrackBar
{
 public void setBackStyle(long style);
   
   public long getBackStyle();
   
   public void setDrawMode(long mode);
   
   public long getDrawMode();
   
   public void setDrawStyle(long style);
   
   public long getDrawStyle();
   
   public void setDrawWidth(long width);
   
   public long getDrawWidth();
   
   public void setFillStyle(long style);
   
   public long getFillStyle();
   
// Does not seem to be used any more. 
//   public void setValid(boolean bValid);
//   
//   public boolean getValid();

//   /** Sets The diagram we're on */
//   public void setDiagram(IDiagram pDiagram);
   
   /** Retreives the diagram we're on */
   public IDiagram getDiagram();
   
   /** Process any objects on the graph creating new cars */
   public void initialize();

// Java does not need this because the layout managers do the job.   
//   /**
//    * Fits the track bar within the given rectangle, and modifies the 
//    * rectangle to exclude the trackbar
//    */
//   public Rectangle updateSize(Rectangle pRectView);
   
   /** Inavalidate the window area */
   public void invalidate(boolean bErase);
   
   /** Load any persistent information from the archive */
   public void load(IProductArchive pProductArchive);
   
   /** Save persisten information to the archive */
   public void save(IProductArchive pProductArchive);
   

   /** Add an element to the track bar */
   public void addPresentationElement(IPresentationElement pPresentationElement);
   
   /** Remove an element from the track bar */
   public void removePresentationElement(IPresentationElement pPresentationElement);
   
   /** Move the cars based on a list of nodes, by a delta along the track */
   public boolean moveObjects(ETList < IPresentationElement > pPresentationElements,
                              long lDelta);
   
   /** Informs the track bar that a resize is about to take place, so it can store some state information */
   public void preResize(IPresentationElement pPresentationElement);
   
   /** Resize the car associated with the presentation element */
   public boolean resize(IPresentationElement pPresentationElement);
   
   /** Update the name of the car associated with the presentation element */
   public void updateName(IPresentationElement pPresentationElement);
   
   /** Update all the names of all the track bar cars */
   public void updateAllCarNames();
   
   /** If necessary, the coupling is expanded to fit the label */
   public void expandAssociatedCoupling(ILabelPresentation pLabelPresentation);
   

   /** Layout all the cars */
   public void layout();
   
   /** Expand the couplings to contain the message labels */
   public void expandCouplings();

//   /** 
//    * Pre scroll/zoom event passed from the drawing area control
//    * 
//    * @return <code>true</code> if the event is handled, <code>false</code>
//    *         if the event is not handled. 
//    */
//   public boolean onPreScrollZoom(double deltaX, double deltaY);
   
   /** 
    * Post scroll/zoom event passed from the drawing area control
    * 
    * @return <code>true</code> if the event is handled, <code>false</code>
    *         if the event is not handled.  
    */
   public boolean onPostScrollZoom();

   /** 
    * Handle the OnKeyDown event from a TSGraphEditor
    * 
    * @return <code>true</code> if the event is handled, <code>false</code>
    *         if the event is not handled.  
    */
   public boolean onKeyDown(KeyEvent e);
   

   /** Handle the OnKeyUp event from a TSGraphEditor */
   public boolean onKeyUp(KeyEvent e);

   /** Creates new cars based on the location of the presentation elements */
   public void postLayoutSequenceDiagram();

   /** Returns the HWND to the trackbar. */
   //public int GetWindowHandle();

   /** Remove these elements from the track bar */
   public void removePresentationElements(ETList < IPresentationElement > pPresentationElements);
}
