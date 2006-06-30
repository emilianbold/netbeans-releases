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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug66919Test extends LayoutTestCase {

    public ALT_Bug66919Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize 'Save' button slightly to the right. The layout is screwed in
    // vertical dimension so the button overlaps with the panel above it, but
    // resizing in horizontal dimension only should not care about this overlap.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(115, 31, 274, 157));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(12, 26));
        compPrefSize.put("jPanel1", new Dimension(286, 183));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 778, 481));
        compBounds.put("add", new Rectangle(10, 11, 93, 23));
        baselinePosition.put("add-93-23", new Integer(15));
        compPrefSize.put("add", new Dimension(93, 23));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compBounds.put("save", new Rectangle(401, 173, 92, 23));
        baselinePosition.put("save-92-23", new Integer(15));
        compPrefSize.put("save", new Dimension(59, 23));
        compBounds.put("delete", new Rectangle(631, 173, 137, 23));
        baselinePosition.put("delete-137-23", new Integer(15));
        compPrefSize.put("delete", new Dimension(65, 23));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        prefPaddingInParent.put("Form-add-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("add-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-delete-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-add-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel2", new Rectangle(407, 31, 336, 138));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(12, 26));
        compPrefSize.put("jPanel2", new Dimension(348, 164));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        {
            String[] compIds = new String[] {
                "save"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(401, 173, 92, 23)
                };
            Point hotspot = new Point(494,186);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        baselinePosition.put("save-92-23", new Integer(15));
        compPrefSize.put("save", new Dimension(59, 23));
        // < START RESIZING
        prefPadding.put("save-delete-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(515,184);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(401, 173, 113, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("save-delete-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(516,184);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(401, 173, 114, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("save-delete-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(115, 31, 274, 157));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(12, 26));
        compPrefSize.put("jPanel1", new Dimension(286, 183));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 778, 481));
        compBounds.put("add", new Rectangle(10, 11, 93, 23));
        baselinePosition.put("add-93-23", new Integer(15));
        compPrefSize.put("add", new Dimension(93, 23));
        compBounds.put("jPanel1", new Rectangle(109, 11, 286, 183));
        baselinePosition.put("jPanel1-286-183", new Integer(0));
        compBounds.put("delete", new Rectangle(631, 173, 137, 23));
        baselinePosition.put("delete-137-23", new Integer(15));
        compPrefSize.put("delete", new Dimension(65, 23));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        compBounds.put("save", new Rectangle(401, 173, 114, 23));
        baselinePosition.put("save-114-23", new Integer(15));
        compPrefSize.put("save", new Dimension(59, 23));
        prefPaddingInParent.put("Form-add-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("add-jPanel1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-delete-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jPanel1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-add-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("jPanel2", new Rectangle(407, 31, 336, 138));
        compBounds.put("jPanel2", new Rectangle(401, 11, 348, 164));
        baselinePosition.put("jPanel2-348-164", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(12, 26));
        compPrefSize.put("jPanel2", new Dimension(348, 164));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
