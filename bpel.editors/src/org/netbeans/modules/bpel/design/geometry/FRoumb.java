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


package org.netbeans.modules.bpel.design.geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import org.netbeans.modules.bpel.design.geometry.FShape.EmptyPathIterator;
import org.netbeans.modules.bpel.design.geometry.FShape.LinePathIterator;



public class FRoumb extends FShape {


    public FRoumb(double size) {
        super(0.0, 0.0, size, size);
    }
    
    
    public FRoumb(double width, double height) {
        super(0.0, 0.0, width, height);
    }

    
    public FRoumb(double x, double y, double width, double height) {
        super(x, y, width, height);
    }
    
    
    public FRoumb enlarge(double v) {
        double v2 = v * 2;
        return new FRoumb(x - v, y - v, width + v2, height + v2);
    }

    
    public FRoumb reshape(double x, double y, double w, double h) {
        return new FRoumb(x, y, w, h);
    }
    
    
    public boolean intersect(FIntersector intersector) {
        double rx = (double) width / 2.0;
        double ry = (double) height / 2.0;
        double cx = (double) x + rx;
        double cy = (double) y + ry;
        
        intersector.intersectByRoumb(cx, cy, rx, ry);
        
        return intersector.ok();
    }    
    
    
    public boolean contains(double px, double py) {
        if (!super.contains(px, py)) return false;
        
        double rx = width / 2.0;
        double ry = height / 2.0;
        
        px -= x + rx;
        py -= y + ry;

        if (px < 0.0) {
            px = -px;
        }
        
        if (py < 0.0) {
            py = -py;
        }

        return px * ry + py * rx <= rx * ry;
    }
    
    
    public PathIterator getPathIterator(AffineTransform at) {
        if (width == 0.0f) {
            return (height == 0.0f) 
                    ? new EmptyPathIterator() 
                    : new LinePathIterator(at);
        } else if (height == 0.0f) {
            return new LinePathIterator(at);
        }

        return new RoumbPathItarator(at);
    }
    
    
    
    private class RoumbPathItarator implements PathIterator {
        
        private AffineTransform at;
        private int index = 0;
        
        
        public RoumbPathItarator(AffineTransform at) {
            this.at = at;
        }    

        public int currentSegment(double[] coords) {
            
            int type;
            
            if (index == 0) {
                coords[0] = x + width / 2.0;
                coords[1] = y;
                type = PathIterator.SEG_MOVETO;
            } else if (index == 1) {
                coords[0] = x + width;
                coords[1] = y + height / 2.0;
                type = PathIterator.SEG_LINETO;
            } else if (index == 2) {
                coords[0] = x + width / 2.0;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            } else if (index == 3) {
                coords[0] = x;
                coords[1] = y + height / 2.0;
                type = PathIterator.SEG_LINETO;
            } else if (index == 4) {
                coords[0] = x + width / 2.0;
                coords[1] = y;
                type = PathIterator.SEG_LINETO;
            } else {
                return PathIterator.SEG_CLOSE;
            }

            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return type;
        }

        public int currentSegment(float[] coords) {
            int type;
            if (index == 0) {
                coords[0] = x + width / 2.0f;
                coords[1] = y;
                type = PathIterator.SEG_MOVETO;
            } else if (index == 1) {
                coords[0] = x + width;
                coords[1] = y + height / 2.0f;
                type = PathIterator.SEG_LINETO;
            } else if (index == 2) {
                coords[0] = x + width / 2.0f;
                coords[1] = y + height;
                type = PathIterator.SEG_LINETO;
            } else if (index == 3) {
                coords[0] = x;
                coords[1] = y + height / 2.0f;
                type = PathIterator.SEG_LINETO;
            } else if (index == 4) {
                coords[0] = x + width / 2.0f;
                coords[1] = y;
                type = PathIterator.SEG_LINETO;
            } else {
                return PathIterator.SEG_CLOSE;
            }
            
            if (at != null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            
            return type;
        }

        public void next() {
            index++;
        }

        
        public boolean isDone() {
            return (index > 5);
        }

        
        public int getWindingRule() {
            return PathIterator.WIND_NON_ZERO;
        }
    }
}



//    
//    
//    public PathIterator getPathIterator(AffineTransform at) {
//        if (width == 0.0f) {
//            return (height == 0.0f) 
//                    ? new EmptyPathIterator() 
//                    : new LinePathIterator(at);
//        } else if (height == 0.0f) {
//            return new LinePathIterator(at);
//        }
//
//        return new EllipsePathItarator(at);
//    }
//    
//    
//    private class EllipsePathIterator implements PathIterator {
//        
//        private AffineTransform at;
//        private int index = 0;
//        
//        
//        public EllipsePathIterator(AffineTransform at) {
//            this.at = at;
//        }
//        
//        public int currentSegment(double[] coords) {
//            
//        }
//
//        
//        public int currentSegment(float[] coords) {
//        }
//
//        
//        public int getWindingRule() {
//        }
//
//        
//        public boolean isDone() {
//        }
//
//        
//        public void next() {
//        }
//    }
//}
