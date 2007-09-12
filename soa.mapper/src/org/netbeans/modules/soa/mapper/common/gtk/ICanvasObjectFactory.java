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

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.Icon;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasObjectFactory {

    /**
     * Creates a canvas node
     *
     * @param location  - location
     * @param size      - size
     * @param label     - name of the node
     * @return          - the created node
     */
    ICanvasNode createCanvasNode(Point location, Dimension size,
        String label);

    /**
     * Creates a canvas node with image
     *
     * @param location  - location of the node
     * @param size      - size of the node
     * @param image     - image
     * @param label     - name of the node
     * @return          - the created node
     */
    ICanvasNode createCanvasNode(Point location, Dimension size,
        Icon image, String label);

    /**
     * Creates a canvas node with image
     *
     * @param location   - location of the node
     * @param size       - size of the node
     * @param label      - name of the node
     * @param labelIcon - the label icon
     * @param icon       Description of the Parameter
     * @return ICanvasNode
     */
    ICanvasNode createCanvasNode(Point location, Dimension size,
        Icon icon, String label, Icon labelIcon);

    /**
     * Creates a link between two nodes
     *
     * @param src   - the source node
     * @param dest  - the destination node
     * @return      - return the node
     */
    ICanvasLink createCanvasLink(ICanvasNode src,
        ICanvasNode dest);

    /**
     * Creates a canvas model with the given name
     *
     * @param name  - the name
     * @return      - a newly created canvas model
     */
    ICanvasModel createCanvasModel(String name);

    /**
     * Description of the Method
     *
     * @param size        Description of the Parameter
     * @param icon        Description of the Parameter
     * @param label       Description of the Parameter
     * @param parentLink  Description of the Parameter
     * @return            Description of the Return Value
     */
    ICanvasLinkLabel createLinkLabel(Dimension size,
        Icon icon, String label, ICanvasLink parentLink);


    /**
     * Creates a canvas
     *
     * @param model - the canvas model
     * @return ICanvas
     */
    ICanvas createCanvas(ICanvasModel model);

    /**
     * creates the controller
     *
     * @param canvas - the canvas
     * @return ICanvasController
     */
    ICanvasController createCanvasController(ICanvas canvas);
}
