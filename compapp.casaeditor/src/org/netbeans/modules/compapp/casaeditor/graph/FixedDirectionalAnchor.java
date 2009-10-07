/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
* FixedDirectionalAnchor.java
*
* Created on November 28, 2006, 9:50 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Direction;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.api.visual.anchor.Anchor.Result;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Utilities;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities.Directions;

/**
 *
 * @author rdara
 */
public final class FixedDirectionalAnchor extends Anchor {

    private Directions kind;
    private int gap;

    public FixedDirectionalAnchor(Widget widget, Directions kind, int gap) {
        super (widget);
        this.kind = kind;
        this.gap = gap;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        Widget widget = getRelatedWidget();
        Rectangle bounds = widget.convertLocalToScene(widget.getBounds());
        Point center = Utilities.center (bounds);

        switch (kind) {
            case LEFT:
                    return new Anchor.Result (new Point (bounds.x - gap, center.y), Direction.LEFT);
            case RIGHT:
                    return new Anchor.Result (new Point (bounds.x + bounds.width + gap, center.y), Direction.RIGHT);
            case TOP:
                    return new Anchor.Result (new Point (center.x, bounds.y - gap), Direction.TOP);
            case BOTTOM:
                    return new Anchor.Result (new Point (center.x, bounds.y + bounds.height + gap), Direction.BOTTOM);
        }
        return null;
    }
};
