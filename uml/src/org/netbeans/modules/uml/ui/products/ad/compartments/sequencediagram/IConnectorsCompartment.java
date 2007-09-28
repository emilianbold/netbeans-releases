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
