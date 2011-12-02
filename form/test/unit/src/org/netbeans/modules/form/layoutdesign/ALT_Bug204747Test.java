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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug204747Test extends LayoutTestCase {

    public ALT_Bug204747Test(String name) {
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
     * Move jTextArea1 (in scrollpane) right of jToggleButton1 snapped at medium
     * default gap and bottom-aligned with jCheckBox3.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 648, 388));
        contInterior.put("Form", new Rectangle(0, 0, 648, 388));
        compBounds.put("jPanel1", new Rectangle(12, 13, 624, 362));
        baselinePosition.put("jPanel1-624-362", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(12, 13, 624, 362));
        compBounds.put("jScrollPane1", new Rectangle(127, 26, 98, 98));
        baselinePosition.put("jScrollPane1-98-98", new Integer(0));
        compBounds.put("jButton1", new Rectangle(237, 26, 79, 25));
        baselinePosition.put("jButton1-79-25", new Integer(17));
        compBounds.put("jTextField1", new Rectangle(328, 27, 69, 22));
        baselinePosition.put("jTextField1-69-22", new Integer(16));
        compBounds.put("jButton2", new Rectangle(237, 64, 79, 25));
        baselinePosition.put("jButton2-79-25", new Integer(17));
        compBounds.put("jTextField2", new Rectangle(328, 65, 69, 22));
        baselinePosition.put("jTextField2-69-22", new Integer(16));
        compBounds.put("jPasswordField1", new Rectangle(415, 72, 126, 22));
        baselinePosition.put("jPasswordField1-126-22", new Integer(16));
        compBounds.put("jToggleButton1", new Rectangle(328, 107, 119, 25));
        baselinePosition.put("jToggleButton1-119-25", new Integer(17));
        compBounds.put("jLabel1", new Rectangle(583, 26, 41, 16));
        baselinePosition.put("jLabel1-41-16", new Integer(13));
        compBounds.put("jLabel3", new Rectangle(583, 49, 41, 16));
        baselinePosition.put("jLabel3-41-16", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(583, 75, 41, 16));
        baselinePosition.put("jLabel2-41-16", new Integer(13));
        compBounds.put("jSpinner1", new Rectangle(572, 112, 52, 22));
        baselinePosition.put("jSpinner1-52-22", new Integer(16));
        compBounds.put("jSlider1", new Rectangle(237, 294, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jSlider2", new Rectangle(237, 264, 200, 23));
        baselinePosition.put("jSlider2-200-23", new Integer(0));
        compBounds.put("jCheckBox3", new Rectangle(237, 200, 93, 25));
        baselinePosition.put("jCheckBox3-93-25", new Integer(17));
        compBounds.put("jCheckBox2", new Rectangle(237, 175, 93, 25));
        baselinePosition.put("jCheckBox2-93-25", new Integer(17));
        compBounds.put("jCheckBox1", new Rectangle(237, 150, 93, 25));
        baselinePosition.put("jCheckBox1-93-25", new Integer(17));
        compBounds.put("jSeparator1", new Rectangle(12, 336, 624, 10));
        baselinePosition.put("jSeparator1-624-10", new Integer(-1));
        compBounds.put("jTextField3", new Rectangle(12, 353, 624, 22));
        baselinePosition.put("jTextField3-624-22", new Integer(16));
        compMinSize.put("jPanel1", new Dimension(587, 313));
        compBounds.put("jPanel1", new Rectangle(12, 13, 624, 362));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        prefPadding.put("jTextField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jTextField3", new Dimension(69, 22));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(611, 339));
        compBounds.put("Form", new Rectangle(0, 0, 648, 388));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START MOVING
        baselinePosition.put("jScrollPane1-98-98", new Integer(0));
        {
            String[] compIds = new String[]{
                "jScrollPane1"
            };
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(127, 26, 98, 98)
            };
            Point hotspot = new Point(165, 83);
            ld.startMoving(compIds, bounds, hotspot);
        }
// < START MOVING
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-0", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSeparator1-jScrollPane1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField3-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-0", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-1", new Integer(8)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-2", new Integer(24)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-1", new Integer(8)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-2", new Integer(24)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-1", new Integer(8)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-2", new Integer(24)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(498, 184);
            String containerId = "jPanel1";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(459, 127, 98, 98)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-0", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-1-1", new Integer(13)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel2-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSeparator1-jScrollPane1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSeparator1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jTextField3-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-0", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("jPanel1-jScrollPane1-0-1", new Integer(12)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSpinner1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-1", new Integer(8)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-2", new Integer(24)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-1", new Integer(8)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-2", new Integer(24)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox2-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-1", new Integer(8)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-2", new Integer(24)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox1-jScrollPane1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
// > MOVE
        {
            Point p = new Point(497, 184);
            String containerId = "jPanel1";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{
                new Rectangle(459, 127, 98, 98)
            };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jTextField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jTextField3", new Dimension(69, 22));
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jSpinner1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        compBounds.put("Form", new Rectangle(0, 0, 648, 388));
        contInterior.put("Form", new Rectangle(0, 0, 648, 388));
        compBounds.put("jPanel1", new Rectangle(12, 13, 624, 362));
        baselinePosition.put("jPanel1-624-362", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(12, 13, 624, 362));
        compBounds.put("jScrollPane1", new Rectangle(459, 127, 98, 98));
        baselinePosition.put("jScrollPane1-98-98", new Integer(0));
        compBounds.put("jButton1", new Rectangle(237, 26, 79, 25));
        baselinePosition.put("jButton1-79-25", new Integer(17));
        compBounds.put("jTextField1", new Rectangle(328, 27, 69, 22));
        baselinePosition.put("jTextField1-69-22", new Integer(16));
        compBounds.put("jButton2", new Rectangle(237, 64, 79, 25));
        baselinePosition.put("jButton2-79-25", new Integer(17));
        compBounds.put("jTextField2", new Rectangle(328, 65, 69, 22));
        baselinePosition.put("jTextField2-69-22", new Integer(16));
        compBounds.put("jPasswordField1", new Rectangle(415, 72, 126, 22));
        baselinePosition.put("jPasswordField1-126-22", new Integer(16));
        compBounds.put("jToggleButton1", new Rectangle(328, 107, 119, 25));
        baselinePosition.put("jToggleButton1-119-25", new Integer(17));
        compBounds.put("jLabel1", new Rectangle(583, 26, 41, 16));
        baselinePosition.put("jLabel1-41-16", new Integer(13));
        compBounds.put("jLabel3", new Rectangle(583, 49, 41, 16));
        baselinePosition.put("jLabel3-41-16", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(583, 75, 41, 16));
        baselinePosition.put("jLabel2-41-16", new Integer(13));
        compBounds.put("jSpinner1", new Rectangle(572, 112, 52, 22));
        baselinePosition.put("jSpinner1-52-22", new Integer(16));
        compBounds.put("jSlider1", new Rectangle(237, 294, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jSlider2", new Rectangle(237, 264, 200, 23));
        baselinePosition.put("jSlider2-200-23", new Integer(0));
        compBounds.put("jCheckBox3", new Rectangle(237, 200, 93, 25));
        baselinePosition.put("jCheckBox3-93-25", new Integer(17));
        compBounds.put("jCheckBox2", new Rectangle(237, 175, 93, 25));
        baselinePosition.put("jCheckBox2-93-25", new Integer(17));
        compBounds.put("jCheckBox1", new Rectangle(237, 150, 93, 25));
        baselinePosition.put("jCheckBox1-93-25", new Integer(17));
        compBounds.put("jSeparator1", new Rectangle(12, 336, 624, 10));
        baselinePosition.put("jSeparator1-624-10", new Integer(-1));
        compBounds.put("jTextField3", new Rectangle(12, 353, 624, 22));
        baselinePosition.put("jTextField3-624-22", new Integer(16));
        compMinSize.put("jPanel1", new Dimension(603, 315));
        compBounds.put("jPanel1", new Rectangle(12, 13, 624, 362));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        prefPadding.put("jTextField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jTextField3", new Dimension(69, 22));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        compMinSize.put("Form", new Dimension(627, 341));
        compBounds.put("Form", new Rectangle(0, 0, 648, 388));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        compBounds.put("Form", new Rectangle(0, 0, 648, 388));
        contInterior.put("Form", new Rectangle(0, 0, 648, 388));
        compBounds.put("jPanel1", new Rectangle(12, 13, 624, 362));
        baselinePosition.put("jPanel1-624-362", new Integer(0));
        contInterior.put("jPanel1", new Rectangle(12, 13, 624, 362));
        compBounds.put("jScrollPane1", new Rectangle(459, 127, 98, 98));
        baselinePosition.put("jScrollPane1-98-98", new Integer(0));
        compBounds.put("jButton1", new Rectangle(237, 26, 79, 25));
        baselinePosition.put("jButton1-79-25", new Integer(17));
        compBounds.put("jTextField1", new Rectangle(328, 27, 69, 22));
        baselinePosition.put("jTextField1-69-22", new Integer(16));
        compBounds.put("jButton2", new Rectangle(237, 64, 79, 25));
        baselinePosition.put("jButton2-79-25", new Integer(17));
        compBounds.put("jTextField2", new Rectangle(328, 65, 69, 22));
        baselinePosition.put("jTextField2-69-22", new Integer(16));
        compBounds.put("jPasswordField1", new Rectangle(415, 72, 126, 22));
        baselinePosition.put("jPasswordField1-126-22", new Integer(16));
        compBounds.put("jToggleButton1", new Rectangle(328, 107, 119, 25));
        baselinePosition.put("jToggleButton1-119-25", new Integer(17));
        compBounds.put("jLabel1", new Rectangle(583, 26, 41, 16));
        baselinePosition.put("jLabel1-41-16", new Integer(13));
        compBounds.put("jLabel3", new Rectangle(583, 49, 41, 16));
        baselinePosition.put("jLabel3-41-16", new Integer(13));
        compBounds.put("jLabel2", new Rectangle(583, 75, 41, 16));
        baselinePosition.put("jLabel2-41-16", new Integer(13));
        compBounds.put("jSpinner1", new Rectangle(572, 112, 52, 22));
        baselinePosition.put("jSpinner1-52-22", new Integer(16));
        compBounds.put("jSlider1", new Rectangle(237, 294, 200, 23));
        baselinePosition.put("jSlider1-200-23", new Integer(0));
        compBounds.put("jSlider2", new Rectangle(237, 264, 200, 23));
        baselinePosition.put("jSlider2-200-23", new Integer(0));
        compBounds.put("jCheckBox3", new Rectangle(237, 200, 93, 25));
        baselinePosition.put("jCheckBox3-93-25", new Integer(17));
        compBounds.put("jCheckBox2", new Rectangle(237, 175, 93, 25));
        baselinePosition.put("jCheckBox2-93-25", new Integer(17));
        compBounds.put("jCheckBox1", new Rectangle(237, 150, 93, 25));
        baselinePosition.put("jCheckBox1-93-25", new Integer(17));
        compBounds.put("jSeparator1", new Rectangle(12, 336, 624, 10));
        baselinePosition.put("jSeparator1-624-10", new Integer(-1));
        compBounds.put("jTextField3", new Rectangle(12, 353, 624, 22));
        baselinePosition.put("jTextField3-624-22", new Integer(16));
        compMinSize.put("jPanel1", new Dimension(603, 315));
        compBounds.put("jPanel1", new Rectangle(12, 13, 624, 362));
        prefPadding.put("jTextField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPasswordField1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel1-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel3-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-0", new Integer(5)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-1", new Integer(12)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-0-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jSeparator1", new Dimension(0, 2));
        compPrefSize.put("jTextField3", new Dimension(69, 22));
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jCheckBox1-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-0", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-1", new Integer(13)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-2", new Integer(7)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jSlider2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-0", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-1", new Integer(9)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-2", new Integer(3)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jCheckBox3-jSlider2-1-0-3", new Integer(18)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compMinSize.put("Form", new Dimension(627, 341));
        compBounds.put("Form", new Rectangle(0, 0, 648, 388));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        compPrefSize.put("jPanel1", new Dimension(624, 362));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
    }
}
