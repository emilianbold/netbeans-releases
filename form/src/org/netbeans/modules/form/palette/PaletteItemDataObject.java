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

import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.xml.XMLUtil;
import org.openide.actions.*;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;

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
    // classpath resource types: "jar", "library", "project"

    private static SystemAction[] staticActions;

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private boolean fileLoaded; // at least tried to load

    private PaletteItem paletteItem;

    // some raw data read from the file (other passed to PaletteItem)
    private String displayName_key;
    private String tooltip_key;
    private String bundleName;
    private String icon16URL;
    private String icon32URL;

    // data derived from raw data
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
        paletteItem = null;
        PaletteItem item = new PaletteItem(this);

        FileObject file = getPrimaryFile();
        if (file.getSize() == 0L) { // item file without any content
            // just derive the component class name from the file name
            item.componentClassName = file.getName().replace('-', '.');
            paletteItem = item;
            return;
        }

        // parse the XML file
        org.w3c.dom.Element mainElement = null;
        try {
            org.w3c.dom.Document doc = XMLUtil.parse(
                new org.xml.sax.InputSource(getPrimaryFile().getURL().toExternalForm()),
                false, false, null, null);

            mainElement = doc.getDocumentElement();
        }
        catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        catch (org.xml.sax.SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        // TODO report errors or validate using DTD?
        // TODO handle empty files analogically to .instance files?

        // read the DOM tree
        if (mainElement == null)
            return;

        if (!XML_ROOT.equals(mainElement.getTagName()))
            return;

        if (!mainElement.getAttribute(ATTR_VERSION).startsWith("1.")) // NOI18N
            return; // unsupported version

        // TODO item ID (for now we take the class name as the ID)

        // root element ok, read the content
        org.w3c.dom.NodeList childNodes = mainElement.getChildNodes();
        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.NamedNodeMap attr = childNodes.item(i).getAttributes();
            String nodeName = childNodes.item(i).getNodeName();
            org.w3c.dom.Node node;

            if (TAG_COMPONENT.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_CLASSNAME);
                if (node != null) {
                    item.componentClassName = node.getNodeValue();

                    node = attr.getNamedItem(ATTR_TYPE);
                    if (node != null)
                        item.componentType_explicit = node.getNodeValue();

//                    node = attr.getNamedItem(ATTR_IS_CONTAINER);
//                    if (node != null)
//                        item.isContainer_explicit =
//                            Boolean.valueOf(node.getNodeValue());
                }
            }

            else if (TAG_CLASSPATH.equals(nodeName)) {
                List cpList = new ArrayList();
                org.w3c.dom.NodeList cpNodes = childNodes.item(i).getChildNodes();
                for (int j=0, m=cpNodes.getLength(); j < m; j++) {
                    attr = cpNodes.item(j).getAttributes();
                    nodeName = cpNodes.item(j).getNodeName();

                    if (TAG_RESOURCE.equals(nodeName)) {
                        node = attr.getNamedItem(ATTR_TYPE);
                        if (node != null) {
                            String type = node.getNodeValue();
                            node = attr.getNamedItem(ATTR_NAME);
                            if (node != null) {
                                cpList.add(type);
                                cpList.add(node.getNodeValue());
                            }
                        }
                    }
                }
                if (cpList.size() > 0)
                    item.classpath_raw =
                        (String[]) cpList.toArray(new String[cpList.size()]);
            }

            else if (TAG_DESCRIPTION.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_BUNDLE);
                if (node != null)
                    bundleName = node.getNodeValue();

                node = attr.getNamedItem(ATTR_DISPLAY_NAME_KEY);
                if (node != null)
                    displayName_key = node.getNodeValue();

                node = attr.getNamedItem(ATTR_TOOLTIP_KEY);
                if (node != null)
                    tooltip_key = node.getNodeValue();
            }

            else if (TAG_ICON16.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_URL);
                if (node != null)
                    icon16URL = node.getNodeValue();
                // TODO support also class resource name for icons
            }

            else if (TAG_ICON32.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_URL);
                if (node != null)
                    icon32URL = node.getNodeValue();
                // TODO support also class resource name for icons
            }
        }

        if (item.componentClassName != null || displayName_key != null)
            paletteItem = item;
    }

    private void saveFile() {
        // TBD
    }

    /**
     * @param folder folder of category where to create new file
     * @param classname name of the component class
     * @param source classpath source type - "jar", "library", "project"
     * @param classpath names of classpath roots - e.g. JAR file paths
     */
    static void createFile(FileObject folder,
                           String classname,
                           String source,
                           String[] classpath)
        throws IOException
    {
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
        for (int i=0; i < classpath.length; i++) {
            buff.append("      <resource type=\""); // NOI18N
            buff.append(source);
            buff.append("\" name=\""); // NOI18N
            buff.append(classpath[i]);
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

        public SystemAction[] getActions() {
            if (staticActions == null)
                staticActions = new SystemAction[] {
                    SystemAction.get(MoveUpAction.class),
                    SystemAction.get(MoveDownAction.class),
                    null,
                    SystemAction.get(CutAction.class),
                    SystemAction.get(CopyAction.class),
                    null,
                    SystemAction.get(DeleteAction.class),
                    null,
                    SystemAction.get(ToolsAction.class),
                    SystemAction.get(PropertiesAction.class),
                };
            return staticActions;
        }

        // TODO properties
        public Node.PropertySet[] getPropertySets() {
            return NO_PROPERTIES;
        }

        // ------

        private String getExplicitDisplayName() {
            String displaName = null;
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
}
