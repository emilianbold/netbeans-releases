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

package org.netbeans.modules.soa.mapper.common.gtk;

import com.nwoods.jgo.JGoPort;

/**
 * ICanvasLink connects two ports.  If the position of one or both of
 * its ports changes, the ICanvasLink redraws itself to connect the
 * new positions.
 * <p>
 * The default link stroke will consist of three segments.  The
 * end segments, at the ports, will be relatively short and either
 * horizontal or vertical.  The middle segment will be just a straight
 * line connecting the two short segments at the ports.  There is
 * no short end segment if the corresponding port does not have a
 * link port spot (i.e., the value is NoSpot).
 * <p>
 * By turning on the Orthogonal property, a link will have segments
 * that are all either horizontal or vertical.  To handle what would
 * normally be "Z" shaped links, there are five segments (i.e. six
 * stroke points).
 * <p>
 * A selected link will not have selection handles at the very
 * end points, unless there are only one or two segments in the stroke.
 * Resizing a link causes a new link to be started, keeping one
 * of the ports for the new link.
 * <p>
 * You can define your own style of link by overriding calculateStroke()
 * or by overriding paint().
 * <p>
 * The resize behavior for the user's "relinking" an existing link
 * to a different port sets the link's corresponding port to null
 * and calls the Canvas.startReLink method.
 * <p>
 * Three properties have been added: Relinkable, AvoidsNodes, and JumpsOver.
 * <p>
 * A Relinkable link (by default true) allows the user to interactively
 * reconnect one end of the link to another valid port (or to the original
 * port; or to nothing, thereby deleting the link).
 * <p>
 * A link that AvoidsNodes has a smarter implementation of calculateStroke()
 * that routes the path of the link so as to try not to cross any avoidable
 * objects.  Of course, if either end of the link is inside an object,
 * or if there is no non-area-crossing path that be found, the path
 * of the link may default to the standard calculateStroke behavior.
 * You can customize the objects to be avoided by overriding
 * ICanvasModel.isAvoidable and ICanvasModel.getAvoidableRectangle
 * -- by default ICanvasModel.isAvoidable is true for instances of JGoArea,
 * and the size and position of the object to be avoided is just the
 * bounding rectangle of the area.
 * This property, by default false, only applies when isOrthogonal()
 * is true and isCubic() is false.
 * Setting this property to true will incur some additional overhead to
 * search for the shortest, straightest path between the ports.
 * You may wish to set JGoView.setDragsRealtime(false) in order to
 * improve responsiveness while dragging.
 * <p>
 * A link that JumpsOver is drawn with small half-ellipses where the
 * path of the segment crosses over another link whose isJumpsOver()
 * property is true.
 * Both the link that crosses over and the link that is crossed over
 * must have the Orthogonal property true, the Cubic property false,
 * and the JumpsOver property true.  The crossed over link must
 * be "behind" the crossing link.  This property is false by default.
 * Setting this property to true will incur some additional overhead to
 * detect segment intersections.
 * You may wish to set ICanvas.setDragsRealtime(false) in order to
 * improve responsiveness while dragging.
 * This property may also be ignored when the view's scale is small.
 * <P>
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasLink {
    /**
     * Retrieves the source node
     *
     * @return   - source
     */
    ICanvasNode getSourceNode();

    /**
     * Retrieves the destination node
     *
     * @return ICanvasNode
     */
    ICanvasNode getDestinationNode();

    /**
     * Updates the label text
     *
     * @param name - the name
     */
    void updateLabelText(String name);

    /**
     * sets the label text
     *
     * @param label - the canvas link label
     */
    void setLabel(ICanvasLinkLabel label);

    /**
     * gets the linklabel
     *
     * @return       The label value
     */
    ICanvasLinkLabel getLabel();

    /**
     * Sets the labelVisible attribute of the ICanvasLink object
     *
     * @param val  The new labelVisible value
     */
    void setLabelVisible(boolean val);

    /**
     * Sets the data object
     * @param  dataobject  The data object
     */
    void setDataObject(Object dataObject);

    /**
     * Gets the data object
     * @return  dataobject
     */
    Object getDataObject();
    
    /**
     * start highlighting this link
     */
    void startHighlighting();

    /**
     * stop highlighting this link
     */
    void stopHighlighting();

    /**
     * get start port
     * @return
     */
    JGoPort getStartPort();
    
    /**
     * get end port
     * @return
     */
    JGoPort getEndPort();
}
