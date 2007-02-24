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



package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;

/**
 *
 * @author Trey Spiva
 */
public interface IConnectorsCompartment
{
   /**
    * Indicates that a message edge can be started from the current logical location.
    *
    * @param  ptLogical[in] Logical view coordinates to test
    * @return TRUE if the location is a place where a message can be started
    */
   boolean canStartMessage( IETPoint point );
   
   /**
    * Indicates that a message edge can be finished from the current logical location.
    *
    * @param ptLogical[in] Logical view coordinates to test
    * @return TRUE if the location is a place where a message can be finished
    */
   boolean canFinishMessage( IETPoint ptLogical );
   
   /** 
    * Connects a message to this compartment.
    * 
    * @param point The point to connect the message.
    * @param kind The message type.  The value must be one of the IMessageKind 
    *             values.
    * @param connectMessageKind The type of connection to make.  The value must
    *                           be one of the IConnectMessageKind.
    * @param connector The connector used for connecting the message.
    *                  When null, the connector is created.
    * @return The connect that was used to connect the message.
    * 
    * @see IMessageKind 
    * @see IConnectMessageKind
    */
   TSEConnector connectMessage( IETPoint pPoint,
                                int kind,
                                int connectMessageKind,
                                TSEConnector connector );

   /** Ensures that the connected messages are horizontal. pDrawInfo may be NULL. */
   void updateConnectors(IDrawInfo pDrawInfo);

   /** Moves the connector to the vertical location, in logical view coordinates */
   void moveConnector( TSConnector pConnector, 
                       double nY, 
                       boolean bDoItNow, 
                       boolean bSetYOfAssociatedPiece );
}
