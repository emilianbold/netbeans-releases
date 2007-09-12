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

package org.netbeans.modules.soa.ui.tnv.impl;

/**
 * Keeps the current position and size of the Tnumbnail view and the visible area.
 *
 * @author supernikita
 */
public class ThumbnailPositionState {
    //
    public double mvSize;
    //
    public double tnvPosition;
    public double tnvSize;
    //
    public double vaPosition;
    public double vaHalfSize;
    
    /**
     * Checks if the visible area with the center in the specified point 
     * is inside of thumbnail view region.
     * Returns 0 If the visible area inside of the TNV region. 
     * Returns -1 If the visible area overlaps minimum edge of TNV region.
     * Returns -1 If the visible area overlaps maximum edge of TNV region.
     */
    public int isInsideOfTnv(double point) {
        if (tnvPosition + vaHalfSize > point) {
            return -1;
        }
        if (tnvPosition + tnvSize - vaHalfSize < point) {
            return 1;
        }
        return 0;
    }
    
    public String toString() {
        return "MV Size: " + mvSize + "; " +
                "TNV: [" + tnvPosition + ", " + tnvSize + "]; " + 
                "VA: [" + vaPosition + ", " + vaHalfSize * 2d + "]; ";
                
    }
}
