/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

/**
 * Tests merging of more parallel inclusions that is not always possible.
 */
public class ALT_Positioning16Test extends LayoutTestCase {

    private Object changeMark;

    public ALT_Positioning16Test(String name) {
        super(name);
        try {
            className = this.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    /**
     * Add a new component (e.g. progress bar) so that it is bottom-aligned with
     * jScrollPane2 and horizontally its left edge is between the right edge of
     * jPanel1 and right edge of jScrollPane1.
     * (The number of components that can be in parallel vertically with the
     *  added component is limited by the size of jScrollPane3 on the right.
     *  I.e. it can't be parallel with jPanel1.)
     */
    public void doChanges0() {
        lm.setChangeRecording(true);
        changeMark = lm.getChangeMark();
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        contInterior.put("Form", new Rectangle(0, 0, 400, 484));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 144, 118));
        baselinePosition.put("jScrollPane1-144-118", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(10, 243, 35, 107));
        baselinePosition.put("jScrollPane2-35-107", new Integer(0));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        baselinePosition.put("jPanel1-102-102", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(11, 136, 100, 100));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        compPrefSize.put("jPanel1", new Dimension(102, 102));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane3", new Rectangle(160, 11, 166, 182));
        baselinePosition.put("jScrollPane3-166-182", new Integer(0));
        compMinSize.put("Form", new Dimension(336, 361));
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        prefPaddingInParent.put("Form-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lc = new LayoutComponent("jProgressBar1", false);
// > START ADDING
        baselinePosition.put("jProgressBar1-146-14", new Integer(-1));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 146, 14)
            };
            String defaultContId = null;
            Point hotspot = new Point(69, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
// < START ADDING
        prefPaddingInParent.put("Form-jProgressBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jProgressBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jProgressBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(210, 349);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(141, 336, 146, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jProgressBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jProgressBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jProgressBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(209, 349);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(140, 336, 146, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jProgressBar1", new Dimension(146, 14));
        prefPadding.put("jScrollPane2-jProgressBar1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane2-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jScrollPane2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jScrollPane2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jScrollPane2-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jScrollPane2-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jScrollPane2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jProgressBar1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jProgressBar1-1-0-1", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jProgressBar1-1-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jProgressBar1-1-0-3", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        contInterior.put("Form", new Rectangle(0, 0, 400, 484));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 144, 118));
        baselinePosition.put("jScrollPane1-144-118", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(10, 243, 35, 107));
        baselinePosition.put("jScrollPane2-35-107", new Integer(0));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        baselinePosition.put("jPanel1-102-102", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(11, 136, 100, 100));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        compPrefSize.put("jPanel1", new Dimension(102, 102));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane3", new Rectangle(160, 11, 166, 182));
        baselinePosition.put("jScrollPane3-166-182", new Integer(0));
        compBounds.put("jProgressBar1", new Rectangle(140, 336, 146, 14));
        baselinePosition.put("jProgressBar1-146-14", new Integer(-1));
        compMinSize.put("Form", new Dimension(336, 361));
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        prefPaddingInParent.put("Form-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }

    /**
     * Undo previous addition. Resize jScrollPane3 vertically so now it goes
     * slightly beyond jPanel1's bottom edge. Then add a new progress bar,
     * bottom aligned with jScrollPane2, left edge within jPanel1 (no snap).
     * (Here the component cannot be added to sequence with jPanel1 and
     *  jScrollPane3 together - jScrollPane2 would move if extracted due to the
     *  size of jScrollPane3. It's an S-layout situation in fact.)
     */
    public void doChanges1() {
        lm.undo(changeMark, lm.getChangeMark());
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        contInterior.put("Form", new Rectangle(0, 0, 400, 484));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 144, 118));
        baselinePosition.put("jScrollPane1-144-118", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(10, 243, 35, 107));
        baselinePosition.put("jScrollPane2-35-107", new Integer(0));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        baselinePosition.put("jPanel1-102-102", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(11, 136, 100, 100));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        compBounds.put("jScrollPane3", new Rectangle(160, 11, 166, 182));
        baselinePosition.put("jScrollPane3-166-182", new Integer(0));
        compMinSize.put("Form", new Dimension(336, 361));
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        prefPaddingInParent.put("Form-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jScrollPane3-166-182", new Integer(0));
        compPrefSize.put("jScrollPane3", new Dimension(166, 96));
        {
            String[] compIds = new String[]{
                "jScrollPane3"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(160, 11, 166, 182)
            };
            Point hotspot = new Point(255, 198);
            int[] resizeEdges = new int[]{
                -1,
                1
            };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jScrollPane3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(257, 282);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(160, 11, 166, 266)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jScrollPane3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(258, 283);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(160, 11, 166, 267)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        contInterior.put("Form", new Rectangle(0, 0, 400, 484));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 144, 118));
        baselinePosition.put("jScrollPane1-144-118", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(10, 243, 35, 107));
        baselinePosition.put("jScrollPane2-35-107", new Integer(0));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        baselinePosition.put("jPanel1-102-102", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(11, 136, 100, 100));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        compPrefSize.put("jPanel1", new Dimension(102, 102));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane3", new Rectangle(160, 11, 166, 267));
        baselinePosition.put("jScrollPane3-166-267", new Integer(0));
        compMinSize.put("Form", new Dimension(336, 361));
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        prefPaddingInParent.put("Form-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane3-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
        lc = new LayoutComponent("jProgressBar1", false);
// > START ADDING
        baselinePosition.put("jProgressBar1-146-14", new Integer(-1));
        {
            LayoutComponent[] comps = new LayoutComponent[]{lc};
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(0, 0, 146, 14)
            };
            String defaultContId = null;
            Point hotspot = new Point(69, 7);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
// < START ADDING
        prefPaddingInParent.put("Form-jProgressBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jProgressBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jProgressBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(156, 340);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(87, 336, 146, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jProgressBar1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jPanel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jProgressBar1-jScrollPane3-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jProgressBar1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane2-jProgressBar1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(155, 340);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(86, 336, 146, 14)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        compPrefSize.put("jProgressBar1", new Dimension(146, 14));
        prefPadding.put("jScrollPane2-jProgressBar1-0-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        contInterior.put("Form", new Rectangle(0, 0, 400, 484));
        compBounds.put("jScrollPane1", new Rectangle(10, 11, 144, 118));
        baselinePosition.put("jScrollPane1-144-118", new Integer(0));
        compBounds.put("jScrollPane2", new Rectangle(10, 243, 35, 107));
        baselinePosition.put("jScrollPane2-35-107", new Integer(0));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        baselinePosition.put("jPanel1-102-102", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(11, 136, 100, 100));
        compMinSize.put("jPanel1", new Dimension(2, 2));
        compBounds.put("jPanel1", new Rectangle(10, 135, 102, 102));
        compPrefSize.put("jPanel1", new Dimension(102, 102));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compBounds.put("jScrollPane3", new Rectangle(160, 11, 166, 267));
        baselinePosition.put("jScrollPane3-166-267", new Integer(0));
        compBounds.put("jProgressBar1", new Rectangle(86, 336, 146, 14));
        baselinePosition.put("jProgressBar1-146-14", new Integer(-1));
        compMinSize.put("Form", new Dimension(336, 361));
        compBounds.put("Form", new Rectangle(0, 0, 400, 484));
        prefPaddingInParent.put("Form-jScrollPane3-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-1", new Integer(11)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-2", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane3-jProgressBar1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jScrollPane2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jProgressBar1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
