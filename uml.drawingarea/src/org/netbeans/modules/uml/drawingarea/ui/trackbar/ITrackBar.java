/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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



package org.netbeans.modules.uml.drawingarea.ui.trackbar;

import java.awt.event.KeyEvent;

import java.util.List;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
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
   
   public DesignerScene getScene();
   
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
   public boolean moveObjects(List < IPresentationElement > pPresentationElements,
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
//   public void expandAssociatedCoupling(ILabelPresentation pLabelPresentation);
   

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
   public void removePresentationElements(List < IPresentationElement > pPresentationElements);
}
