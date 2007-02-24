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


package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.drawing.TSConnector;

/**
 * This utility class helps with the storage of connector based information
 * for moving connectors vertically within their compartment
 *
 * @see CSmartDragTool
 * @author Trey Spiva
 */
public class ConnectorInfo
{
   private TSConnector            mConnector = null;
   private long                   mVerticalOffset = 0;
   private IConnectorsCompartment mCompartment = null;
   
   ConnectorInfo(TSConnector connector, long lVerticalOffset)
   {
      setConnector(connector);
      setVerticalOffset(lVerticalOffset);
      
      ICompartment compartment = TypeConversions.getCompartment(connector, IConnectorsCompartment.class);
      if (compartment instanceof IConnectorsCompartment)
      {
         setCompartment((IConnectorsCompartment)compartment);         
      }
   }
   
   void moveConnector( long lLogicalY )
   {
      IConnectorsCompartment compartment = getCompartment();
      TSConnector connector = getConnector();
      
      if( (connector != null) &&
          (compartment != null) )
      {
         long offset = lLogicalY + getVerticalOffset();
         compartment.moveConnector( connector, offset, false, true);
      }
   }
   //**************************************************
   // Data Accessor
   //**************************************************
   
   /**
    * @return
    */
   public TSConnector getConnector()
   {
      return mConnector;
   }

   /**
    * @param connector
    */
   public void setConnector(TSConnector connector)
   {
      mConnector = connector;
   }

   /**
    * @return
    */
   public long getVerticalOffset()
   {
      return mVerticalOffset;
   }

   /**
    * @param l
    */
   public void setVerticalOffset(long l)
   {
      mVerticalOffset = l;
   }

   /**
    * @return
    */
   protected IConnectorsCompartment getCompartment()
   {
      return mCompartment;
   }

   /**
    * @param compartment
    */
   protected void setCompartment(IConnectorsCompartment compartment)
   {
      mCompartment = compartment;
   }

}
