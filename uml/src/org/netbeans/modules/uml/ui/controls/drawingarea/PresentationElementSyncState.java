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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISynchStateKind;
import org.netbeans.modules.uml.ui.support.SynchStateKindEnum;
import org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState;

/**
 * @author josephg
 *
 */
public class PresentationElementSyncState implements IPresentationElementSyncState
{

   private IDiagram mDaigram = null;
   private int mOrigSyncState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
   private int mNewSyncState = ISynchStateKind.SSK_UNKNOWN_SYNCH_STATE;
   private IPresentationElement mElement = null;

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#getDiagram()
    */
   public IDiagram getDiagram()
   {
      return mDaigram;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#getNewSynchState()
    */
   public int getNewSynchState()
   {
      return mNewSyncState;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#getOriginalSynchState()
    */
   public int getOriginalSynchState()
   {
      return mOrigSyncState;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#getPresentationElement()
    */
   public IPresentationElement getPresentationElement()
   {
      return mElement;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#setDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
    */
   public void setDiagram(IDiagram value)
   {
      mDaigram = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#setNewSynchState(int)
    */
   public void setNewSynchState(int value)
   {
      mNewSyncState = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#setOriginalSynchState(int)
    */
   public void setOriginalSynchState(int value)
   {
      mOrigSyncState = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementSyncState#setPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void setPresentationElement(IPresentationElement value)
   {
      mElement = value;
   }

}


