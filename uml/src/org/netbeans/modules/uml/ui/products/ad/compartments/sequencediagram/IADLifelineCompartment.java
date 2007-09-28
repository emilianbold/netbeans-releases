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

import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEEdge;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IMessageEdgeDrawEngine;

/**
 *
 * @author Trey Spiva
 */
public interface IADLifelineCompartment extends IADCompartment
{
//	 The number of parts currently being displayed
//  HRESULT NumParts([out, retval] long *pVal);
//
//	 Creates a part port
//  HRESULT CreatePartPort();
//	 Creates an Attribute port
//  HRESULT CreateAttributePort();
//	 Creates a Parameter port
//  HRESULT CreateParameterPort();
//
    /**
     * The minimum height for the lifeline as determined by its lowest piece (excluding the destructor).
     *
     * @return The minimum height in logical dimensions
     */
    public int getMinimumHeight();
    
    /**
     * Creates a lifeline element in the compartment
     *
     * @param kind The type of element to create
     * @param pptInLogicalCoords The desired location for the creation of the
     *                           piece, and the actual location for the piece
     *                           after creation, Tom Sawyer logical drawing
     *                           area coordinates.
     * @param pConnector Tom Sawyer connector to be attached to this piece
     * @param lclCorner Enumerated value for the location on the piece where the
     *                  Tom Sawyer connector is to be attached.
     */
    public IETPoint createElement( int          kind,
            TSConnector pConnector,
            int          lpcCorner );
    
    /** Creates a lifeline element in the compartment */
    public LifelinePiece createLifelinePiece( int      kind,
            IETPoint pInLogicalCoordinates );
    
    /**
     * Copies the input lifeline piece to this lifeline including connected messages
     */
    public TSConnector copyLifelinePiece( LifelinePiece piece );
//
//	 Removes the input lifeline piece from this lifeline, includes associated connectors
//  HRESULT RemoveLifelinePiece( [in] ILifelinePiece * pPiece );
    
    /**
     * Connects a return edge to the bottom of the pieces connected to the input
     * connectors
     */
    public void connectReturnEdge( TSConnector fromConnector,
            TSConnector toConnector,
            TSEEdge     returnEdge );
//
    /**
     * Indicates that a message edge can be started from the current logical location
     */
    public boolean canStartMessage( IETPoint pLogical );
    
    /**
     * Indicates that a message edge can be finished on the current logical
     * location
     */
    public boolean canFinishMessage( IETPoint pLogical );
    
    /**
     * Indicates that a message edge can be finished on the current logical
     * location
     */
    public boolean canFinishMessage( double x, double y );
    
    /**
     * Indicates that a message edge can be finished from the current logical location.
     *
     * @param ptLogical[in] Logical view coordinates to test
     *
     * @return true if the location is a place where a message can be finished
     */
    public boolean canReallyFinishMessage( int messageKind,
            IADLifelineCompartment romCompartment,
            final IETPoint ptLogical );
    
    /**
     * Returns true when the lifeline is terminated by a destroy element
     */
    public boolean getIsDestroyed();
    
    /**
     * Returns the closest piece to this point, in logical view coordinates
     */
    public LifelineCompartmentPiece getClosestPiece( IETPoint pLogical );
    
    /** Returns the closest lifeline piece to this point, in logical view coordinates */
    public LifelinePiece getClosestLifelinePiece( IETPoint pLogical );
    
    // Ensures all the pieces are valid, i.e. either have child pieces, or connectors attached
    public void validatePieces();
    
    // Make sure the reflexive messages look right
    public void updateReflexiveBends();
    
    // Forces any activation bars that should be connected to connect to each other
    public void cleanUpActivationBars();
    
    /**
     * Returns the location of where the next message should be placed.  The
     * returned location will always be the last message.
     *
     * @return The Y location of where the next message should be placed.
     */
    public int getLocationOfNextMessage();
    
    public void addMessageBefore(ILifelineDrawEngine toEngine, 
                                 IMessageEdgeDrawEngine beforeMsg, 
                                 int msgType);

    void addMessageAfter(ILifelineDrawEngine to, 
                         IMessageEdgeDrawEngine relativeMessage, 
                         int msgType);

    /**
     * Moving a create message, moves the entire lifeline.  The attached edges 
     * start to slant, because they are not adjusted unless the head bumps into
     * the message.  Therefore, adjust the pieces on the created lifeline, to 
     * match the amount that the lifeline was moved.
     *
     * @param delta The amount to adjust the pieces.
     */
    void movingCreate(int delta);
    
    public void lifelineTopHeightChanged(int delta);
}
