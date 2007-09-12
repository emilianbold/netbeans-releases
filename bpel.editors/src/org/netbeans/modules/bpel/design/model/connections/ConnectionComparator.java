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


package org.netbeans.modules.bpel.design.model.connections;

import java.util.Comparator;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;


public class ConnectionComparator implements Comparator<Connection> {

    private VisualElement visualElement;
    private Direction direction;


    public ConnectionComparator(VisualElement visualElement,
            Direction direction)
    {
        this.visualElement = visualElement;
        this.direction = direction;
    }


    public int compare(Connection c1, Connection c2) {
        VisualElement e1 = c1.getSource();
        VisualElement e2 = c2.getSource();
        Direction d1 = c1.getSourceDirection();
        Direction d2 = c2.getSourceDirection();

        if (e1 == visualElement) {
            e1 = c1.getTarget();
            d1 = c1.getTargetDirection();
        }

        if (e2 == visualElement) { 
            e2 = c2.getTarget();
            d2 = c2.getTargetDirection();
        }            

        int result;
        
        if (direction == Direction.TOP) {
            result = compareTop(e1, d1, e2, d2);
        } else if (direction == Direction.BOTTOM) {
            result = compareBottom(e1, d1, e2, d2);
        } else if (direction == Direction.LEFT) {
            result = compareLeft(e1, d1, e2, d2);
        } else {
            result = compareRight(e1, d1, e2, d2);
        }
        
        return (result == 0) ? (c1.getUID() - c2.getUID()) : result;
    }

    
    private int compareLeft(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.TOP) {
            n1 = 3;
            v1 = e1.getCenterX();
        } else if (d1 == Direction.BOTTOM) {
            n1 = 1;
            v1 = -e1.getCenterX();
        } else {
            n1 = 2;
            v1 = e1.getCenterY();
        }

        if (d2 == Direction.TOP) {
            n2 = 3;
            v2 = e2.getCenterX();
        } else if (d2 == Direction.BOTTOM) {
            n2 = 1;
            v2 = -e2.getCenterX();
        } else {
            n2 = 2;
            v2 = e2.getCenterY();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);
    }
    

    private int compareRight(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.TOP) {
            n1 = 3;
            v1 = -e1.getCenterX();
        } else if (d1 == Direction.BOTTOM) {
            n1 = 1;
            v1 = e1.getCenterX();
        } else {
            n1 = 2;
            v1 = e1.getCenterY();
        }

        if (d2 == Direction.TOP) {
            n2 = 3;
            v2 = -e2.getCenterX();
        } else if (d2 == Direction.BOTTOM) {
            n2 = 1;
            v2 = e2.getCenterX();
        } else {
            n2 = 2;
            v2 = e2.getCenterY();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);        
    }

    
    private int compareTop(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.RIGHT) {
            n1 = 1;
            v1 = e1.getCenterY();
        } else if (d1 == Direction.LEFT) {
            n1 = 3;
            v1 = -e1.getCenterY();
        } else {
            n1 = 2;
            v1 = e1.getCenterX();
        }

        if (d2 == Direction.RIGHT) {
            n2 = 1;
            v2 = e2.getCenterY();
        } else if (d2 == Direction.LEFT) {
            n2 = 3;
            v2 = -e2.getCenterY();
        } else {
            n2 = 2;
            v2 = e2.getCenterX();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);        
    }

    
    private int compareBottom(VisualElement e1, Direction d1, 
            VisualElement e2, Direction d2) 
    {
        int n1, n2; 
        double v1, v2; 

        if (d1 == Direction.RIGHT) {
            n1 = 1;
            v1 = e1.getCenterY();
        } else if (d1 == Direction.LEFT) {
            n1 = 3;
            v1 = -e1.getCenterY();
        } else {
            n1 = 2;
            v1 = e1.getCenterX();
        }

        if (d2 == Direction.RIGHT) {
            n2 = 1;
            v2 = e2.getCenterY();
        } else if (d2 == Direction.LEFT) {
            n2 = 3;
            v2 = -e2.getCenterY();
        } else {
            n2 = 2;
            v2 = e2.getCenterX();
        }

        return (n1 != n2) ? (n1 - n2) : (int) Math.signum(v1 - v2);        
    }
}
