/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;

import java.util.*;
import java.io.*;
import java.beans.*;
import javax.swing.Action;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.xml.XMLUtil;
import org.openide.actions.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import org.netbeans.modules.form.project.ClassSource;

/**
 * DataObject for palette item file. It reads the file and creates PaletteItem
 * and node from it.
 *
 * @author Tomas Pavek
 */

class PaletteItemDataObject extends MultiDataObject {

    static final String XML_ROOT = "palette_item"; // NOI18N
    static final String ATTR_VERSION = "version"; // NOI18N
    static final String TAG_COMPONENT = "component"; // NOI18N
    static final String ATTR_CLASSNAME = "classname"; // NOI18N
    static final String ATTR_TYPE = "type"; // NOI18N
//    static final String ATTR_IS_CONTAINER = "is-container"; // NOI18N
    static final String TAG_CLASSPATH = "classpath"; // NOI18N
    static final String TAG_RESOURCE= "resource"; // NOI18N
    static final String ATTR_NAME = "name"; // NOI18N
    static final String TAG_DESCRIPTION = "description"; // NOI18N
    static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N
    static final String TAG_ICON16 = "icon16"; // NOI18N
    static final String ATTR_URL = "urlvalue"; // NOI18N
    static final String TAG_ICON32 = "icon32"; // NOI18N
    // component types: "visual", "menu", "layout", "border"
    // classpath resource types: "jar", "library", "project" (defined in ClassSource)

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private boolean fileLoaded; // at least tried to load

    private PaletteItem paletteItem;

    // some raw data read from the file (other passed to PaletteItem)
    private String displayName_key;
    private String tooltip_key;
    private String bundleName;
    private String icon16URL;
    private String icon32URL;

    // resolved data (derived from raw data)
    String displayName;
    String tooltip;
    java.awt.Image icon16;
    java.awt.Image icon32;

    // --------

    PaletteItemDataObject(FileObject fo, MultiFileLoader loader)
        throws DataObjectExistsException
    {
        super(fo, loader);
    }

    boolean isFileRead() {
        return fileLoaded;
    }

    boolean isItemValid() {
        return paletteItem != null;
    }

    void reloadFile() {
        if (paletteItem != null) {
            paletteItem.reset(); // resets resolved data (but not raw data)

            paletteItem.componentClassSource = null;
//            paletteItem.isContainer_explicit = null;
            paletteItem.componentType_explicit = null;
        }

        displayName = null;
        tooltip = null;
        icon16 = null;
        icon32 = null;

        displayName_key = null;
        tooltip_key = null;
        bundleName = null;
        icon16URL = null;
        icon32URL = null;

        loadFile();
    }

    // ------

    public Node createNodeDelegate() {
        return new ItemNode();
    }

    public Node.Cookie getCookie(Class cookieClass) {
        if (PaletteItem.class.equals(cookieClass)) {
            if (!fileLoaded)
                loadFile();
            return paletteItem;
        }
        return super.getCookie(cookieClass);
    }

    // -------

    private void loadFile() {
        fileLoaded = true;
        PaletteItem item = paletteItem;
        if (item == null)
            item = new PaletteItem(this);

        FileObject file = getPrimaryFile();
        if (file.getSize() == 0L) { // item file is empty
            // just derive the component class name from the file name
            item.setComponentClassSource(file.getName().replace('-', '.'),
                                         null, null);
            paletteItem = item;
            return;
        }
        
        // parse the XML file
        try {
            XMLReader reader = XMLUtil.createXMLReader();
            PaletteItemHandler handler = new PaletteItemHandler();
            reader.setContentHandler(handler);
            InputSource input = new InputSource(getPrimaryFile().getURL().toExternalForm());
            reader.parse(input);
            // TODO report errors, validate using DTD?
            
            item.setComponentExplicitType(handler.componentExplicitType);
            if (handler.componentClassName != null || displayName_key != null) {
                String[] cpTypes;
                String[] cpNames;
                if (handler.cpTypeList.size() > 0) {
                    cpTypes = new String[handler.cpTypeList.size()];
                    handler.cpTypeList.toArray(cpTypes);
                    cpNames = new String[handler.cpNameList.size()];
                    handler.cpNameList.toArray(cpNames);
                } else {
                    cpTypes = cpNames = null;
                }
                
                item.setComponentClassSource(handler.componentClassName, cpTypes, cpNames);
                
                paletteItem = item;
            }
        } catch (SAXException saxex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, saxex);
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioex);
        }
    }

    /**
     * @param folder folder of category where to create new file
     * @param classname name of the component class
     * @param source classpath source type - "jar", "library", "project"
     * @param classpath names of classpath roots - e.g. JAR file paths
     */
    static void createFile(FileObject folder, ClassSource classSource)
        throws IOException
    {
        String classname = classSource.getClassName();

        int idx = classname.lastIndexOf('.');
        String fileName = FileUtil.findFreeFileName(
            folder,
            idx >= 0 ? classname.substring(idx+1) : classname,
            PaletteItemDataLoader.ITEM_EXT);

        FileObject itemFile = folder.createData(fileName,
                                                PaletteItemDataLoader.ITEM_EXT);

        StringBuffer buff = new StringBuffer(512);
        buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"); // NOI18N
        buff.append("<palette_item version=\"1.0\">\n"); // NOI18N
        buff.append("  <component classname=\""); // NOI18N
        buff.append(classname);
        buff.append("\" />\n"); // NOI18N
        buff.append("  <classpath>\n"); // NOI18N
        for (int i=0, n=classSource.getCPRootCount(); i < n; i++) {
            buff.append("      <resource type=\""); // NOI18N
            buff.append(classSource.getCPRootType(i));
            buff.append("\" name=\""); // NOI18N
            buff.append(classSource.getCPRootName(i));
            buff.append("\" />\n"); // NOI18N
            buff.append("  </classpath>\n"); // NOI18N
            buff.append("</palette_item>\n"); // NOI18N
        }

        FileLock lock = itemFile.lock();
        OutputStream os = itemFile.getOutputStream(lock);
        try {
            os.write(buff.toString().getBytes());
        }
        finally {
            os.close();
            lock.releaseLock();
        }
    }

    // -------

    /** DataLoader for the palette item files. */
    public static final class PaletteItemDataLoader extends UniFileLoader {

        static final String ITEM_EXT = "palette_item"; // NOI18N

        PaletteItemDataLoader() {
            super("org.netbeans.modules.form.palette.PaletteItemDataObject"); // NOI18N

            ExtensionList ext = new ExtensionList();
            ext.addExtension(ITEM_EXT);
            setExtensions(ext);
        }
        
        /** Gets default display name. Overides superclass method. */
        protected String defaultDisplayName() {
            return NbBundle.getBundle(PaletteItemDataObject.class)
            .getString("PROP_PaletteItemLoader_Name"); // NOI18N
        }
        

        protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException
        {
            return new PaletteItemDataObject(primaryFile, this);
        }
    }
    
    public static final class PaletteItemDataLoaderBeanInfo extends SimpleBeanInfo {
        private static String iconURL = "org/netbeans/modules/form/resources/palette_manager.png"; // NOI18N
        
        public BeanInfo[] getAdditionalBeanInfo() {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo(UniFileLoader.class) };
            } catch (IntrospectionException ie) {
                org.openide.ErrorManager.getDefault().notify(ie);
                return null;
            }
        }
        
        public java.awt.Image getIcon(final int type) {
            return Utilities.loadImage(iconURL);
        }
        
    }

    // --------

    /** Node representing the palette item (node delegate for the DataObject). */
    class ItemNode extends DataNode {
        private Action[] actions;

        ItemNode() {
            super(PaletteItemDataObject.this, Children.LEAF);
        }

        public String getDisplayName() {
            if (!fileLoaded)
                loadFile();

            if (displayName == null) {
                displayName = getExplicitDisplayName();
                if (displayName == null) { // no explicit name
                    if (isItemValid()) {
                        displayName = paletteItem.getDisplayName();
                        if (displayName == null) { // no name from BeanDescriptor
                            String classname = paletteItem.getComponentClassName();
                            if (classname != null) {
                                int i = classname.lastIndexOf('.'); // NOI18N
                                displayName = i >= 0 ?
                                    classname.substring(i+1) : classname;
                            }
                        }
                    }
                    if (displayName == null) // no name derived from the item
                        displayName = super.getDisplayName();
                }
            }
            return displayName;
        }

        public String getShortDescription() {
            if (!fileLoaded)
                loadFile();

            if (tooltip == null) {
                tooltip = getExplicitTooltip();
                if (tooltip == null) { // no explicit tooltip
                    if (isItemValid()) {
                        tooltip = paletteItem.getTooltip();
                        if (tooltip == null) // no tooltip from BeanDescriptor
                            tooltip = paletteItem.getComponentClassName();
                    }
                    if (tooltip == null) // no tooltip derived from the item
                        tooltip = getDisplayName();
                }
            }
            return tooltip;
        }

        public boolean canRename() {
            return false;
        }

        public java.awt.Image getIcon(int type) {
            if (!fileLoaded)
                loadFile();

            if (type == BeanInfo.ICON_COLOR_32x32
                    || type == BeanInfo.ICON_MONO_32x32)
            {
                if (icon32 == null) {
                    icon32 = getExplicitIcon(type);
                    if (icon32 == null && isItemValid())
                        icon32 = paletteItem.getIcon(type);
                    if (icon32 == null)
                        icon32 = Utilities.loadImage("org/netbeans/modules/form/resources/palette/unknown32.gif"); // NOI18N
                }
                return icon32;
            }
            else { // small icon by default
                if (icon16 == null) {
                    icon16 = getExplicitIcon(type);
                    if (icon16 == null && isItemValid())
                        icon16 = paletteItem.getIcon(type);
                    if (icon16 == null)
                        icon16 = Utilities.loadImage("org/netbeans/modules/form/resources/palette/unknown.gif"); // NOI18N
                }
                return icon16;
            }
            // TODO badged icon for invalid item?
        }

        public Action[] getActions(boolean context) {
            if (actions == null) {
                Node categoryNode = PaletteItemDataObject.this.getFolder().getNodeDelegate();
                actions = new Action[] {
                    new PaletteUtils.ShowNamesAction(),
                    new PaletteUtils.ChangeIconSizeAction(),
                    null,
                    new PaletteUtils.CutBeanAction(this),
                    new PaletteUtils.CopyBeanAction(this),
                    new PaletteUtils.PasteBeanAction(categoryNode),
                    null,
                    new PaletteUtils.RemoveBeanAction(this),
                    null,
                    new PaletteUtils.ReorderCategoryAction(categoryNode)
                };
            }
            return actions;
        }


        // TODO properties
        public Node.PropertySet[] getPropertySets() {
            return NO_PROPERTIES;
        }

        // ------

        private String getExplicitDisplayName() {
            String displayName = null;
            if (displayName_key != null) {
                if (bundleName != null) {
                    try {
                        displayName = NbBundle.getBundle(bundleName)
                                                .getString(displayName_key);
                    }
                    catch (Exception ex) {} // ignore failure
                }
                if (displayName == null)
                    displayName = displayName_key;
            }
            return displayName;
        }

        private String getExplicitTooltip() {
            String tooltip = null;
            if (tooltip_key != null) {
                if (bundleName != null) {
                    try {
                        tooltip = NbBundle.getBundle(bundleName)
                                            .getString(tooltip_key);
                    }
                    catch (Exception ex) {} // ignore failure
                }
                if (tooltip == null)
                    tooltip = tooltip_key;
            }
            return tooltip;
        }

        private java.awt.Image getExplicitIcon(int type) {
            if (type == BeanInfo.ICON_COLOR_32x32
                    || type == BeanInfo.ICON_MONO_32x32)
            {
                if (icon32URL != null) { // explicit icon specified in file
                    try {
                        return java.awt.Toolkit.getDefaultToolkit().getImage(
                                                 new java.net.URL(icon32URL));
                    }
                    catch (java.net.MalformedURLException ex) {} // ignore
                }
                else if (getPrimaryFile().getAttribute("SystemFileSystem.icon32") != null) // NOI18N
                    return super.getIcon(type);
            }
            else { // get small icon in other cases
                if (icon16URL != null) { // explicit icon specified in file
                    try {
                        return java.awt.Toolkit.getDefaultToolkit().getImage(
                                                 new java.net.URL(icon16URL));
                    }
                    catch (java.net.MalformedURLException ex) {} // ignore
                }
                else if (getPrimaryFile().getAttribute("SystemFileSystem.icon") != null) // NOI18N
                    return super.getIcon(type);
            }
            return null;
        }
    }
    
    private class PaletteItemHandler extends DefaultHandler {
        List cpTypeList; // list for classpath type entries
        List cpNameList; // list for classpath root name entries
        String componentClassName;
        String componentExplicitType;
        
        public void startDocument() throws SAXException {
            cpTypeList = new ArrayList();
            cpNameList = new ArrayList();
            componentClassName = null;
            componentExplicitType = null;
        }
                
        public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
            if (XML_ROOT.equals(qName)) {
                String version = attributes.getValue(ATTR_VERSION);
                if (version == null) {
                    String message = NbBundle.getBundle(PaletteItemDataObject.class)
                        .getString("MSG_UnknownPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                } else if (!version.startsWith("1.")) { // NOI18N
                    String message = NbBundle.getBundle(PaletteItemDataObject.class)
                        .getString("MSG_UnsupportedPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                }
                // TODO item ID (for now we take the class name as the ID)
            } else if (TAG_COMPONENT.equals(qName)) {
                String className = attributes.getValue(ATTR_CLASSNAME);
                componentClassName = className;
                componentExplicitType = attributes.getValue(ATTR_TYPE);
            } else if (TAG_CLASSPATH.equals(qName)) {
                // Content is processed in the next branch
            } else if (TAG_RESOURCE.equals(qName)) {
                String type = attributes.getValue(ATTR_TYPE);
                String name = attributes.getValue(ATTR_NAME);
                if ((type != null) && (name != null)) {
                    cpTypeList.add(type);
                    cpNameList.add(name);
                }
            } else if (TAG_DESCRIPTION.equals(qName)) {
                String bundle = attributes.getValue(ATTR_BUNDLE);
                if (bundle != null) {
                    PaletteItemDataObject.this.bundleName = bundle;
                }
                String displayNameKey = attributes.getValue(ATTR_DISPLAY_NAME_KEY);
                if (displayNameKey != null) {
                    PaletteItemDataObject.this.displayName_key = displayNameKey;
                }
                String tooltipKey = attributes.getValue(ATTR_TOOLTIP_KEY);
                if (tooltipKey != null) {
                    PaletteItemDataObject.this.tooltip_key = tooltipKey;
                }
            } else if (TAG_ICON16.equals(qName)) {
                String url = attributes.getValue(ATTR_URL);
                if (url != null) {
                    PaletteItemDataObject.this.icon16URL = url;
                }
                // TODO support also class resource name for icons
            } else if (TAG_ICON32.equals(qName)) {
                String url = attributes.getValue(ATTR_URL);
                if (url != null) {
                    PaletteItemDataObject.this.icon32URL = url;
                }
                // TODO support also class resource name for icons
            }
        }
    }
    
}
