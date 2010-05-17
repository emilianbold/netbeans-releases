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

package org.netbeans.modules.visualweb.api.designer;

import java.awt.Graphics2D;
import org.netbeans.modules.visualweb.designer.DesignerServiceHackProviderImpl;
import java.awt.Image;
import java.net.URL;
import java.util.Map;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * XXX Old API, which we wan't to get rid of.
 * Don't add any new methods here. There should be <code>DesignerService</code>
 * or <code>DesignerUtilities</code> which will offer stable API.
 *
 * @author Peter Zavadsky
 */
public final class DesignerServiceHackProvider {

    /** Creates a new instance of DesignerServiceHackProvider */
    private DesignerServiceHackProvider() {
    }

    public static Image getCssPreviewImage(/*DataObject dataObject,*/ Designer designer, Graphics2D g2d,
    String cssStyle, String[] cssStyleClasses,
    /*MarkupDesignBean bean,*/ Element componentRootElement, DocumentFragment df, Element element,
    int width, int height) {
        return DesignerServiceHackProviderImpl.getCssPreviewImage(/*dataObject,*/ designer, g2d, cssStyle, cssStyleClasses,
                /*bean,*/ componentRootElement, df, element,
                width, height);
    }

    public static Image getCssPreviewImage(Map<String, String> properties, URL base,
    int width, int height, int defaultFontSize) {
        return DesignerServiceHackProviderImpl.getCssPreviewImage(properties, base, width, height, defaultFontSize);
    }

    public static Image getPageBoxPreviewImage(/*DataObject dobj,*/ Designer designer, int width, int height) {
        return DesignerServiceHackProviderImpl.getPageBoxPreviewImage(/*dobj,*/designer, width, height);
    }

//    public static boolean canDrop(DataFlavor flavor) {
//        return DesignerServiceHackProviderImpl.canDrop(flavor);
//    }

//    public static void drop(Transferable transferable) {
//        DesignerServiceHackProviderImpl.drop(transferable);
//    }

//    public static void registerTransferable(Transferable transferable) {
//        DesignerServiceHackProviderImpl.registerTransferable(transferable);
//    }

//    public static FileObject getCurrentFile() {
//        return DesignerServiceHackProviderImpl.getCurrentFile();
//    }

//    public static Object getTableInfo(MarkupDesignBean bean) {
//    public static Object getTableInfo(Element componentRootElement) {
//        return DesignerServiceHackProviderImpl.getTableInfo(componentRootElement);
//    }
    // XXX
    public static boolean isTableBox(Object box) {
        return DesignerServiceHackProviderImpl.isTableBox(box);
    }

    public static Element getCellElement(Object tableInfo, int row, int column) {
        return DesignerServiceHackProviderImpl.getCellElement(tableInfo, row, column);
    }

//    public static MarkupDesignBean getCellBean(Object tableInfo, int row, int column) {
    public static Element getCellComponent(Object tableInfo, int row, int column) {
        return DesignerServiceHackProviderImpl.getCellComponent(tableInfo, row, column);
    }

    public static int getColSpan(Object tableInfo, int row, int column) {
        return DesignerServiceHackProviderImpl.getColSpan(tableInfo, row, column);
    }

    public static int getRowSpan(Object tableInfo, int row, int column) {
        return DesignerServiceHackProviderImpl.getRowSpan(tableInfo, row, column);
    }

    public static int getColumnCount(Object tableInfo) {
        return DesignerServiceHackProviderImpl.getColumnCount(tableInfo);
    }

    public static int getRowCount(Object tableInfo) {
        return DesignerServiceHackProviderImpl.getRowCount(tableInfo);
    }

//    public static void notifyCssEdited(DataObject dobj) {
//        DesignerServiceHackProviderImpl.notifyCssEdited(dobj);
//    }

//    public static void refresh(Project project, DataObject dobj, boolean deep) {
//        DesignerServiceHackProviderImpl.refresh(project, dobj, deep);
//    }
//    public static void refreshDataObject(DataObject dobj, boolean deep) {
//        DesignerServiceHackProviderImpl.refreshDataObject(dobj, deep);
//    }
//    public static void refreshProject(Project project, boolean deep) {
//        DesignerServiceHackProviderImpl.refreshProject(project, deep);
//    }

//    public static void destroyWebFormForFileObject(FileObject fo) {
//        DesignerServiceHackProviderImpl.destroyWebFormForFileObject(fo);
//    }

//    public static void copyBoxForElement(Element fromElement, Element toElement) {
//        DesignerServiceHackProviderImpl.copyBoxForElement(fromElement, toElement);
//    }

//    public static FileObject getContextFileForFragmentFile(FileObject fragmentFile) {
//        return DesignerServiceHackProviderImpl.getContextFileForFragmentFile(fragmentFile);
//    }

//    public static FileObject getExternalFormFileForElement(Element element) {
//        return DesignerServiceHackProviderImpl.getExternalFormFileForElement(element);
//    }

//    public static MultiViewElement getDesignerMultiViewElement(DataObject dataObject) {
//        return DesignerServiceHackProviderImpl.getDesignerMultiViewElement(dataObject);
//    }
    
}
