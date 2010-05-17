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

package org.netbeans.modules.uml.diagrams.actions;

import org.netbeans.modules.uml.diagrams.actions.sqd.*;
import java.awt.Point;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 * is used for vertical movements within some border
 * @author sp153251
 */
public class VerticalWithLimitMoveProvider implements MoveStrategy,MoveProvider{

    private Point startingPoint;
    private int min,max;
    private Widget start;
    
    public VerticalWithLimitMoveProvider(int y_min,int y_max)
    {
        if(y_max<y_min)throw new IllegalArgumentException("Max value can't be lower henmin value");
        min=y_min;
        max=y_max;
    }
    
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        int y=suggestedLocation.y;
        return new Point(originalLocation.x,y);
    }

    public void movementStarted(Widget widget) {
        startingPoint=new Point(widget.getPreferredLocation());
        start=widget;
    }

    public void movementFinished(Widget widget) {
        //
        startingPoint=null;
        start=null;
    }

    public Point getOriginalLocation(Widget widget) {
        return startingPoint == null ? null : new Point(startingPoint);
    }

    public void setNewLocation(Widget widget, Point location) {
        Point tmp=new Point(startingPoint);
        tmp.y+=location.y;
        if(tmp.y>max)tmp.y=max;
        else if(tmp.y<min)tmp.y=min;
        widget.setPreferredLocation(tmp);
    }

    protected void setMin(int value)
    {
        min=value;
    }
    protected void setMax(int value)
    {
        max=value;
    }
}
