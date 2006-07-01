/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
/*
 * EqualPolygon.java
 *
 * Created on February 10, 2004, 1:17 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;


/**
 * A Polygon which implements a proper equals/hashcode contract.  In order to
 * optimize drag and drop repainting, it is necessary that the Shape objects
 * returned by getTabIndication() be able to be compared properly.
 * <p/>
 * To ease migration of older code, this class also implements a couple methods
 * of GeneralPath, which was used before. These methods just delegate to
 * addPoint(), so the full functionality of GeneralPath is not replicated
 * (specifically, a polygon must be contiguous and closed).
 * <p/>
 *
 * @author Tim Boudreau
 */
public final class EqualPolygon extends Polygon {

    /**
     * Creates a new instance of EqualGeneralPath
     */
    public EqualPolygon() {
    }

    /**
     * Copy constructor will copy the xpoints/ypoints arrays so the caller can
     * later modify them without changing the polygon constructor here.
     */
    public EqualPolygon(int[] x, int[] y, int n) {
        //Must clone the arrays, or transforms on the source of the polygon
        //will also transform this one
        xpoints = new int[n];
        ypoints = new int[n];
        System.arraycopy(x, 0, xpoints, 0, xpoints.length);
        System.arraycopy(y, 0, ypoints, 0, ypoints.length);
        npoints = n;
    }

    /**
     * Copy constructor - takes either another EqualPolygon or a Polygon. Copies
     * the points arrays of the original polygon, so the passed polygon may be
     * modified without affecting the instance constructed here.
     *
     * @param p
     */
    public EqualPolygon(Polygon p) {
        super(p.xpoints, p.ypoints, p.npoints);
    }

    /** Convenience constructor which takes a Rectangle */
    public EqualPolygon(Rectangle r) {
        super (
            new int[] {r.x, r.x + r.width, r.x + r.width,  r.x},
            new int[] {r.y, r.y,           r.y + r.height, r.y + r.height},
            4
        );
    }

    /**
     * Non copy constructor based on fixed arrays.  Takes the point count
     * parameter from<code>x.length</code>.
     */
    public EqualPolygon(int[] x, int[] y) {
        super(x, y, x.length);
    }

    /**
     * Delegates to <code>Polygon.addPoint()</code>.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void moveTo(int x, int y) {
        addPoint(x, y);
    }

    /**
     * Delegates to <code>Polygon.addPoint()</code>.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void lineTo(int x, int y) {
        addPoint(x, y);
    }

    /**
     * Creates a new EqualPolygon using the copy constructor - the resulting
     * polygon may be modified without affecting the original.
     *
     * @return A new instance of EqualPolygon with the same point values
     */
    public Object clone() {
        return new EqualPolygon(xpoints, ypoints, xpoints.length);
    }

    /**
     * Overridden to produce a meaningful result.
     *
     * @return A string representation of the EqualPolygon
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("EqualPolygon: "); //NOI18N
        for (int i = 0; i < npoints; i++) {
            sb.append(' '); //NOI18N
            sb.append(xpoints[i]);
            sb.append(','); //NOI18N
            sb.append(ypoints[i]);
        }
        return sb.toString();
    }

    /**
     * Computes a hashCode based on the points arrays.
     *
     * @return The hash code
     */
    public int hashCode() {
        return arrayHashCode(xpoints) ^ arrayHashCode(ypoints);
    }

    private int arrayHashCode(int[] o) {
        int result = 0;
        for (int i = 0; i < npoints; i++) {
            result += o[i] ^ i;
        }
        return result;
    }

    /**
     * Returns true if the argument is a Polygon (does not need to be
     * EqualPolygon) and its point arrays and number of points matches.
     *
     * @param o Another polygon
     * @return whether or not they are equal
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Polygon) {
            Polygon p = (Polygon) o;
            int[] ox = p.xpoints;
            int[] oy = p.ypoints;
            boolean result = Arrays.equals(xpoints, ox)
                    && Arrays.equals(ypoints, oy);
            result &= p.npoints == npoints;
            return result;
        } else {
            return false;
        }
    }

    private Point[] sortPoints(Point[] p) {
        //Prune duplicates
        HashSet set = new HashSet(Arrays.asList(p));
        p = new Point[set.size()];
        p = (Point[]) set.toArray(p);
        //Then sort
        Arrays.sort(p, comparator);
        return p;
    }

    private static final Comparator comparator = new PointsComparator();

    private static class PointsComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Point a = (Point) o1;
            Point b = (Point) o2;
            int result = (a.y * (a.x - b.x)) - (b.y * (b.x - a.x));
            return result;
        }
    }

}
