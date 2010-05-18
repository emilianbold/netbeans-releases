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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.anchors;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;

/**
 * all relations in this anchor with pins but compute border by parent widget (box)
 * tale into account message can be from right or left
 * @author sp153251
 */
public class CreateMessageTargetAnchor  extends Anchor{

    private MessagePinWidget pin;
    
    public CreateMessageTargetAnchor(MessagePinWidget  pin)
    {
        super(pin);
        this.pin=pin;
    }
    
    @Override
    public Anchor.Result compute(Entry entry) {
        Widget box=pin.getParentWidget();
        Point boxLocation=box.getLocation();
        boxLocation=box.getParentWidget().convertLocalToScene(boxLocation);
        Point oppositeLocation=entry.getOppositeAnchor().getRelatedWidget().getLocation();
        oppositeLocation=entry.getOppositeAnchor().getRelatedWidget().getParentWidget().convertLocalToScene(oppositeLocation);
        Rectangle rec=box.getBounds();
        Point retPoint=new Point(rec.x+(oppositeLocation.x>boxLocation.x ? rec.width : 0),rec.y+rec.height/2);
        retPoint=box.convertLocalToScene(retPoint);
        return new Result(retPoint, (oppositeLocation.x>boxLocation.x ? Direction.RIGHT : Direction.LEFT));
   }

}
