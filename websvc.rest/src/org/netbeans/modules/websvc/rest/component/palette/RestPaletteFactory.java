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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.beans.BeanInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Ayub Khan
 */
public class RestPaletteFactory {
    
    public static final String REST_PALETTE_FOLDER = "RestPalette";
    
    public static final String REST_COMPONENTS_FOLDER = "RestComponents";
    
    private static PaletteController pc = null;
    
    public RestPaletteFactory() {
    }
    
    public static PaletteController createPalette() {
        try {
            RestPaletteFactory pf = new RestPaletteFactory();
            pf.getPaletteRoot(); //create palette root REST_PALETTE_FOLDER if necessary
            pc = PaletteFactory.createPalette(REST_PALETTE_FOLDER,
                    new RestPaletteActions());
            pf.populatePaletteItems(pc);
            return pc;
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public void populatePaletteItems(PaletteController pc)
            throws IOException, ParserConfigurationException, SAXException {
        //Find all REST components from /RestComponents in layer.xml
        String folderName = REST_COMPONENTS_FOLDER;
        FileObject rcFolder = getRestComponentsFolder(folderName);
        for(FileObject fo: rcFolder.getChildren()) {
            System.out.println("file: " + fo.getNameExt());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fo.getInputStream());
            RestComponentData data = new RestComponentData(doc);
            
            String itemsFolderPath = data.getCategoryPath();
            FileObject root = getPaletteRoot();
            FileObject itemsFolder1 = createItemsFolder(root, itemsFolderPath);
            String itemsFolderDisplay = data.getCategoryName();
            if(itemsFolderDisplay != null) {
                DataObject d = DataObject.find(itemsFolder1);
                if(d != null && d.getNodeDelegate() != null)
                    d.getNodeDelegate().setDisplayName(itemsFolderDisplay);
            }
            String displayName = data.getName();
            String description = data.getDescription();
            Node itemNode1 = createItemNode(itemsFolder1, getComponentFile(data),
                    data.getClassName(), data.getIcon16(), data.getIcon32(), displayName, description);
            itemNode1.setValue("RestComponentData", data);
            System.out.println("test url: "+itemNode1.getValue("url"));
        }
    }
    
    //----------------------------------   helpers  ------------------------------------------------------------------
    
    private String getComponentFile(RestComponentData data) {
        return data.getName() + ".xml";
    }
    
    private FileObject createItemsFolder(FileObject root, String itemsFolder) throws IOException {
        FileObject fooCategory = root.getFileObject(itemsFolder);
        if(fooCategory == null)
            fooCategory = FileUtil.createFolder(root,itemsFolder);
        return fooCategory;
    }
    
    private Node createItemNode(FileObject itemsFolder, String name,
            String className, String icon16, String icon32, String displayName,
            String description)
            throws IOException, ParserConfigurationException, SAXException {
        FileObject itemFile = createItemFileWithActiveEditorDrop(itemsFolder, name,
                className, icon16, icon32, displayName, description);
        Node itemNode = DataObject.find(itemFile).getNodeDelegate();
        System.out.println("Item display name. "+itemNode.getDisplayName());
        System.out.println("Item description. "+itemNode.getShortDescription());
        System.out.println("Item small icon. "+itemNode.getIcon(BeanInfo.ICON_COLOR_16x16));
        System.out.println("Item big icon. "+itemNode.getIcon(BeanInfo.ICON_COLOR_32x32));
        itemNode.setHidden(false);
        
        getPaletteRoot().refresh();
        
        Object o = itemNode.getLookup().lookup(ActiveEditorDrop.class);
        System.out.println("Item does not contain ActiveEditorDrop implementation in its lookup."+o);
        return itemNode;
    }
    
    private Node createItemNode(FileObject itemsFolder, String name,
            String className, String icon16, String icon32, String bundleName,
            String nameKey, String toolTipKey)
            throws IOException, ParserConfigurationException, SAXException {
        FileObject itemFile = createItemFileWithActiveEditorDrop(itemsFolder, name,
                className, icon16, icon32, bundleName, nameKey, toolTipKey);
        Node itemNode = DataObject.find(itemFile).getNodeDelegate();
        System.out.println("Item display name. "+itemNode.getDisplayName());
        System.out.println("Item description. "+itemNode.getShortDescription());
        System.out.println("Item small icon. "+itemNode.getIcon(BeanInfo.ICON_COLOR_16x16));
        System.out.println("Item big icon. "+itemNode.getIcon(BeanInfo.ICON_COLOR_32x32));
        itemNode.setHidden(false);
        
        getPaletteRoot().refresh();
        
        Object o = itemNode.getLookup().lookup(ActiveEditorDrop.class);
        System.out.println("Item does not contain ActiveEditorDrop implementation in its lookup."+o);
        return itemNode;
    }
    
    private FileObject createItemFileWithActiveEditorDrop(FileObject itemsFolder,
            String itemFile, String className, String icon16, String icon32,
            String displayName, String description) throws IOException {
        FileObject fo = itemsFolder.getFileObject(itemFile);
        if(fo != null)
            fo.delete();
        fo = itemsFolder.createData(itemFile);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.1//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd'>");
                writer.write("<editor_palette_item version='1.1'>");
                writer.write("<class name='" + className + "' />");
                writer.write("<icon16 urlvalue='" + icon16 + "' />");
                writer.write("<icon32 urlvalue='" + icon32 + "' />");
                writer.write("<inline-description>");
                writer.write("<display-name>"+displayName+"</display-name>");
                writer.write("<tooltip> <![CDATA[ "+description+" ]]> </tooltip>");
                writer.write("</inline-description>");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    private FileObject createItemFileWithActiveEditorDrop(FileObject itemsFolder,
            String itemFile, String className, String icon16, String icon32,
            String bundleName, String nameKey, String toolTipKey) throws IOException {
        FileObject fo = itemsFolder.getFileObject(itemFile);
        if(fo != null)
            fo.delete();
        fo = itemsFolder.createData(itemFile);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.1//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd'>");
                writer.write("<editor_palette_item version='1.1'>");
                writer.write("<class name='" + className + "' />");
                writer.write("<icon16 urlvalue='" + icon16 + "' />");
                writer.write("<icon32 urlvalue='" + icon32 + "' />");
                writer.write("<description localizing-bundle='" + bundleName + "' display-name-key='" + nameKey + "' tooltip-key='" + toolTipKey + "' />");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    private FileObject getPaletteRoot() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject paletteRoot = fs.findResource(REST_PALETTE_FOLDER);
        if(paletteRoot == null)
            paletteRoot = createItemsFolder(fs.getRoot(), REST_PALETTE_FOLDER);
        return paletteRoot;
    }
    
    private FileObject getRestComponentsFolder(String folderName) throws IOException {
        FileObject paletteFolder;
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        paletteFolder = fs.findResource(folderName);
        if (paletteFolder == null) { // not found, cannot continue
            throw new FileNotFoundException(folderName);
        }
        return paletteFolder;
    }
    
    public static Lookup getCurrentPaletteItem() {
        return pc.getSelectedItem();
    }
}
