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

import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.xml.XMLUtil;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;

/**
 * DataObject for palette item file. It reads the file and creates PaletteItem
 * and node from it.
 */

class PaletteItemDataObject extends MultiDataObject {

    static final String XML_ROOT = "palette_item"; // NOI18N
    static final String ATTR_VERSION = "version"; // NOI18N
    static final String TAG_COMPONENT = "component"; // NOI18N
    static final String ATTR_CLASSNAME = "classname"; // NOI18N
    static final String ATTR_TYPE = "type"; // NOI18N
//    static final String ATTR_IS_CONTAINER = "is-container"; // NOI18N
    static final String TAG_ORIGIN = "origin"; // NOI18N
    static final String ATTR_LOCATION = "location"; // NOI18N
    static final String TAG_DESCRIPTION = "description"; // NOI18N
    static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N
    static final String TAG_ICON16 = "icon16"; // NOI18N
    static final String ATTR_URL = "urlvalue"; // NOI18N
    static final String TAG_ICON32 = "icon32"; // NOI18N
    // component types: "visual", "menu", "layout", "border"
    // origin types: "jar", "library", "project"

    private static SystemAction[] staticActions;

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
//    private static final String DEFAULT_ICON = "org/openide/resources/pending.gif"; // NOI18N

    private boolean fileLoaded; // at least tried to load

    private PaletteItem paletteItem;

    // --------

    PaletteItemDataObject(FileObject fo, MultiFileLoader loader)
        throws DataObjectExistsException
    {
        super(fo, loader);
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
            org.w3c.dom.Node childNode = childNodes.item(i);
            org.w3c.dom.NamedNodeMap attr = childNode.getAttributes();
            String nodeName = childNode.getNodeName();
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

            else if (TAG_ORIGIN.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_TYPE);
                if (node != null) {
                    item.originType_explicit = node.getNodeValue();

                    node = attr.getNamedItem(ATTR_LOCATION);
                    if (node != null)
                        item.originLocation = node.getNodeValue();
                    else
                        item.originType_explicit = null;
                }
            }

            else if (TAG_DESCRIPTION.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_BUNDLE);
                if (node != null)
                    item.bundleName = node.getNodeValue();

                node = attr.getNamedItem(ATTR_DISPLAY_NAME_KEY);
                if (node != null)
                    item.displayName_key = node.getNodeValue();

                node = attr.getNamedItem(ATTR_TOOLTIP_KEY);
                if (node != null)
                    item.tooltip_key = node.getNodeValue();
            }

            else if (TAG_ICON16.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_URL);
                if (node != null)
                    item.icon16URL = node.getNodeValue();
                // TODO support also class resource name for icons
            }

            else if (TAG_ICON32.equals(nodeName)) {
                node = attr.getNamedItem(ATTR_URL);
                if (node != null)
                    item.icon32URL = node.getNodeValue();
                // TODO support also class resource name for icons
            }
        }

        if (item.componentClassName != null || item.displayName_key != null)
            paletteItem = item;
    }

    private void saveFile() {
        // TBD
    }

    // -------

    /** DataLoader for the palette item files. */
    static final class PaletteItemDataLoader extends UniFileLoader {

        static final String ITEM_EXT = "palette_item"; // NOI18N

        PaletteItemDataLoader() {
            super("org.netbeans.modules.form.palette.PaletteItemDataObject"); // NOI18N

            ExtensionList ext = new ExtensionList();
            ext.addExtension(ITEM_EXT);
            setExtensions(ext);
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException
        {
            return new PaletteItemDataObject(primaryFile, this);
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

            String displayName = isItemValid() ? paletteItem.getDisplayName() : null;
            return displayName != null ? displayName : super.getDisplayName();
        }

        public String getShortDescription() {
            if (!fileLoaded)
                loadFile();

            String tooltip = isItemValid() ? paletteItem.getTooltip() : null;
            return tooltip != null ? tooltip : super.getShortDescription();
        }

        public boolean canRename() {
            return false;
        }

        public java.awt.Image getIcon(int type) {
            if (!fileLoaded)
                loadFile();

            java.awt.Image icon = isItemValid() ? paletteItem.getIcon(type) : null;
            return icon != null ? icon : super.getIcon(type); // Utilities.loadImage(DEFAULT_ICON);
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
    }
}
