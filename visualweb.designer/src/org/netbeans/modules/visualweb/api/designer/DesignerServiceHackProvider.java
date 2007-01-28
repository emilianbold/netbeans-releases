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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.api.designer;

import org.netbeans.modules.visualweb.designer.DesignerServiceHackProviderImpl;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;
import java.util.Map;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
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

    public static Image getCssPreviewImage(DataObject dataObject,
    String cssStyle, String[] cssStyleClasses, MarkupDesignBean bean, int width, int height) {
        return DesignerServiceHackProviderImpl.getCssPreviewImage(dataObject, cssStyle, cssStyleClasses, bean, width, height);
    }

    public static Image getCssPreviewImage(Map properties, URL base, int width, int height) {
        return DesignerServiceHackProviderImpl.getCssPreviewImage(properties, base, width, height);
    }

    public static Image getPageBoxPreviewImage(DataObject dobj, int width, int height) {
        return DesignerServiceHackProviderImpl.getPageBoxPreviewImage(dobj, width, height);
    }

    public static boolean canDrop(DataFlavor flavor) {
        return DesignerServiceHackProviderImpl.canDrop(flavor);
    }

    public static void drop(Transferable transferable) {
        DesignerServiceHackProviderImpl.drop(transferable);
    }

//    public static void registerTransferable(Transferable transferable) {
//        DesignerServiceHackProviderImpl.registerTransferable(transferable);
//    }

    public static FileObject getCurrentFile() {
        return DesignerServiceHackProviderImpl.getCurrentFile();
    }

    public static Object getTableInfo(MarkupDesignBean bean) {
        return DesignerServiceHackProviderImpl.getTableInfo(bean);
    }

    public static Element getCellElement(Object tableInfo, int row, int column) {
        return DesignerServiceHackProviderImpl.getCellElement(tableInfo, row, column);
    }

    public static MarkupDesignBean getCellBean(Object tableInfo, int row, int column) {
        return DesignerServiceHackProviderImpl.getCellBean(tableInfo, row, column);
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

    public static void copyBoxForElement(Element fromElement, Element toElement) {
        DesignerServiceHackProviderImpl.copyBoxForElement(fromElement, toElement);
    }

    public static FileObject getContextFileForFragmentFile(FileObject fragmentFile) {
        return DesignerServiceHackProviderImpl.getContextFileForFragmentFile(fragmentFile);
    }

    public static FileObject getExternalFormFileForElement(Element element) {
        return DesignerServiceHackProviderImpl.getExternalFormFileForElement(element);
    }

//    public static MultiViewElement getDesignerMultiViewElement(DataObject dataObject) {
//        return DesignerServiceHackProviderImpl.getDesignerMultiViewElement(dataObject);
//    }
    
}
