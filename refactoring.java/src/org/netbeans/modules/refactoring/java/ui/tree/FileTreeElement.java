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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public class FileTreeElement implements TreeElement, Openable {

    private FileObject fo;
    FileTreeElement(FileObject fo) {
        this.fo = fo;
    }


    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(fo.getParent());
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            return TreeElementFactory.getTreeElement(p != null ? p : fo.getParent());
        }
    }

    @Override
    public Icon getIcon() {
        try {
            ImageIcon imageIcon = new ImageIcon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            Boolean inTestFile = ElementGripFactory.getDefault().inTestFile(fo);
            if(Boolean.TRUE == inTestFile) {
                Image mergeImages = ImageUtilities.mergeImages(imageIcon.getImage(),
                        ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_test.png", false).getImage(), 4, 4);
                imageIcon = new ImageIcon(mergeImages);
            }
            return imageIcon;
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    @Override
    public String getText(boolean isLogical) {
        return fo.getNameExt();
    }

    @Override
    public Object getUserObject() {
        return fo;
    }

    @Override
    public void open() {
        try {
            if(fo.isValid()) {
                DataObject od = DataObject.find(fo);
                NbDocument.openDocument(od, 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
