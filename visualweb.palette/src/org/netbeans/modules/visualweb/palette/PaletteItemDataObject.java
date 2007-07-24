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
/*
 * PaletteItemDataObjec t.java
 *
 * Created on August 2, 2006, 11:52 AM
 *
 * DataObject for palette item file. It reads the file and creates PaletteItem
 * and node from it.
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 */

package org.netbeans.modules.visualweb.palette;

import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import org.netbeans.modules.visualweb.palette.api.PaletteItemInfoCookie;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.io.*;
import java.beans.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

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



public class PaletteItemDataObject extends MultiDataObject {

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
    static final String ATTR_HELP_KEY = "help-key"; // NOI18N
    static final String TAG_ICON16 = "icon16"; // NOI18N
    static final String ATTR_URL = "urlvalue"; // NOI18N
    static final String TAG_ICON32 = "icon32"; // NOI18N
    // component types: "visual", "menu", "layout", "border"
    // classpath resource types: "jar", "library", "project" (defined in ClassSource)

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private boolean fileLoaded; // at least tried to load

//    private PaletteItem paletteItem;
    private String componentClassName;


    // some raw data read from the file (other passed to PaletteItem)
    private String displayName_key;
    private String tooltip_key;
    private String help_key;
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
    throws DataObjectExistsException {
        super(fo, loader);
    }

    boolean isFileRead() {
        return fileLoaded;
    }

    boolean isItemValid() {
        return true;
    }

    public HelpCtx getHelpCtx() {
//        System.out.println("\nHelp Context Being Called.");
        if ( help_key == null ) {
            System.out.println("Help Context Found: " + help_key);
            return super.getHelpCtx();
        }
        return new HelpCtx(help_key);
    } 
   
    public Node createNodeDelegate() {
        return new ItemNode();
    }
    
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cookieClass) {
        
        if (PaletteItemInfoCookie.class.isAssignableFrom(cookieClass)){
            return cookieClass.cast(new PaletteItemInfoImpl( this ));
        }                
        return cookieClass.cast(super.getCookie(cookieClass));
    }
    
    // -------
    
    private void loadFile() {
        fileLoaded = true;
       
        FileObject file = getPrimaryFile();
        if (file.getSize() == 0L) { // item file is empty
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
            
            if (handler.componentClassName != null || displayName_key != null) {
                String[] cpTypes = null;
                String[] cpNames = null;
                if (handler.cpTypeList.size() > 0) {
                    cpTypes = new String[handler.cpTypeList.size()];
                    handler.cpTypeList.toArray(cpTypes);
                    cpNames = new String[handler.cpNameList.size()];
                    handler.cpNameList.toArray(cpNames);
                } 
                
                componentClassName = handler.componentClassName;
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
//    static void createFile(FileObject folder, ClassSource classSource)
//        throws IOException
//    {
//        String classname = classSource.getClassName();
//
//        int idx = classname.lastIndexOf('.');
//        String fileName = FileUtil.findFreeFileName(
//            folder,
//            idx >= 0 ? classname.substring(idx+1) : classname,
//            PaletteItemDataLoader.ITEM_EXT);
//
//        FileObject itemFile = folder.createData(fileName,
//                                                PaletteItemDataLoader.ITEM_EXT);
//
//        StringBuffer buff = new StringBuffer(512);
//        buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"); // NOI18N
//        buff.append("<palette_item version=\"1.0\">\n"); // NOI18N
//        buff.append("  <component classname=\""); // NOI18N
//        buff.append(classname);
//        buff.append("\" />\n"); // NOI18N
//        buff.append("  <classpath>\n"); // NOI18N
//        for (int i=0, n=classSource.getCPRootCount(); i < n; i++) {
//            buff.append("      <resource type=\""); // NOI18N
//            buff.append(classSource.getCPRootType(i));
//            buff.append("\" name=\""); // NOI18N
//            buff.append(classSource.getCPRootName(i));
//            buff.append("\" />\n"); // NOI18N
//            buff.append("  </classpath>\n"); // NOI18N
//            buff.append("</palette_item>\n"); // NOI18N
//        }
//
//        FileLock lock = itemFile.lock();
//        OutputStream os = itemFile.getOutputStream(lock);
//        try {
//            os.write(buff.toString().getBytes());
//        }
//        finally {
//            os.close();
//            lock.releaseLock();
//        }
//    }
    
    // -------
    
    /** DataLoader for the palette item files. */
    public static final class PaletteItemDataLoader extends UniFileLoader {
        
        static final String ITEM_EXT = "comp_palette_item"; // NOI18N
        
        PaletteItemDataLoader() {
            super("org.netbeans.modules.visualweb.palette.PaletteItemDataObject"); // NOI18N
            
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
        throws DataObjectExistsException, IOException {
            return new PaletteItemDataObject(primaryFile, this);
        }
    }
    
    public static final class PaletteItemDataLoaderBeanInfo extends SimpleBeanInfo {
        private static String iconURL = "org/netbeans/modules/visualweb/palette/resources/palette_manager.png"; // NOI18N
        
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
            //classname = PaletteItemDataObject.this.componentClassName;
            super(PaletteItemDataObject.this, Children.LEAF);
        }
        
        public String getDisplayName() {
            if (!fileLoaded)
                loadFile();
            
            if (displayName == null) {
                displayName = getExplicitDisplayName();
                if (displayName == null) { // no explicit name
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
                        tooltip = getDisplayName();
                }
                
                
            }
            return tooltip;
        }
        
        public boolean canRename() {
            return true;
        }
        
        public java.awt.Image getIcon(int type) {
            if (!fileLoaded)
                loadFile();
//            System.out.println("\nPaletteItemDataObject: getIcon.");
//            System.out.println("File: " + PaletteItemDataObject.this.getPrimaryFile());
            
            if (type == BeanInfo.ICON_COLOR_32x32
                    || type == BeanInfo.ICON_MONO_32x32) {
                if (icon32 == null) {
                    icon32 = getExplicitIcon(type);
//                    if (icon32 == null && isItemValid())
//                        icon32 = paletteItem.getIcon(type);
                    if (icon32 == null)
                        icon32 = Utilities.loadImage("org/netbeans/modules/visualweb/palette/resources/custom_component_32.png"); // NOI18N
                }
                return icon32;
            } else { // small icon by default
//                System.out.println("Small Icon");
                if (icon16 == null) {                    
//                    System.out.println("Small Icon is null");
                    icon16 = getExplicitIcon(type);
//                    if (icon16 == null && isItemValid())
//                        icon16 = paletteItem.getIcon(type);
                    if (icon16 == null) {                        
//                        System.out.println("Small Icon is null after assignment");
                        icon16 = Utilities.loadImage("org/netbeans/modules/visualweb/palette/resources/custom_component.png"); // NOI18N
                    }
                }
                return icon16;
            }
            // TODO badged icon for invalid item?
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
                    } catch (Exception ex) {} // ignore failure
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
                    } catch (Exception ex) {} // ignore failure
                }
                if (tooltip == null)
                    tooltip = tooltip_key;
            }
            return tooltip;
        }
        
        private java.awt.Image getExplicitIcon(int type) {
            if (type == BeanInfo.ICON_COLOR_32x32
                    || type == BeanInfo.ICON_MONO_32x32) {
                if (icon32URL != null) { // explicit icon specified in file
                    try {
                        return java.awt.Toolkit.getDefaultToolkit().getImage(
                                new java.net.URL(icon32URL));
                    } catch (java.net.MalformedURLException ex) {} // ignore
                } else if (getPrimaryFile().getAttribute("SystemFileSystem.icon32") != null) // NOI18N
                    return super.getIcon(type);
            } else { // get small icon in other cases
                if (icon16URL != null) { // explicit icon specified in file
                    try {
                        return java.awt.Toolkit.getDefaultToolkit().getImage(
                                new java.net.URL(icon16URL));
                    } catch (java.net.MalformedURLException ex) {} // ignore
                } else if (getPrimaryFile().getAttribute("SystemFileSystem.icon") != null) // NOI18N
                    return super.getIcon(type);
            }
            return null;
        }
        
        public Transferable clipboardCopy() throws IOException {
            Transferable t = super.clipboardCopy();
            addPaletteFlavor(t);
            
            return t;
            
        }
        
        public Transferable clipboardCut() throws IOException {
            Transferable t = super.clipboardCut();            
            addPaletteFlavor(t);            
            return t;
        }
        
        public Transferable drag() throws IOException {
            Transferable t = super.drag();            
            addPaletteFlavor(t);
            
            // XXX NB#82645, when the issue is fixed, remove this.
            hackExplorerDnD(this, t);
            
            return t;
            
        }
        
        private Transferable addPaletteFlavor(Transferable t) {
            
            String DISPLAY_ITEM_HUMAN_NAME = NbBundle.getMessage(PaletteItemDataObject.class, "DISPLAY_ITEM_HUMAN_NAME"); //NOIl8N
                        
//            System.out.println("\nPaletteItemDataObject: Transferable: "  + t);
//            System.out.println("Flavors: " + Arrays.asList(t.getTransferDataFlavors()));            
            
            //ExTransferable allows you to add additional flavors.
            ExTransferable et = ExTransferable.create(t);
            
            //The DisplayItem Flavor make items dropable.
            DataFlavor df = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
                    DISPLAY_ITEM_HUMAN_NAME);
            
//                        DataFlavor df = new DataFlavor(
//                    DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
//                    "DISPLAY_ITEM_HUMAN_NAME");
            
            ExTransferable.Single ex_sgl = new ExTransferable.Single(df) {
                protected Object getData() throws IOException, UnsupportedFlavorException {
                    
                    return new PlainBeanCreateInfo( PaletteItemDataObject.this.componentClassName, PaletteItemDataObject.this.displayName  );
                }
                
            };
            et.put(ex_sgl);
            
            return t;
        } 

       
        
    }

    
    
    
    private class PaletteItemHandler extends DefaultHandler {
        List<String> cpTypeList; // list for classpath type entries
        List<String> cpNameList; // list for classpath root name entries
        String componentClassName;
        
        public void startDocument() throws SAXException {
            cpTypeList = new ArrayList<String>();
            cpNameList = new ArrayList<String>();
            componentClassName = null;
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
                String help_key = attributes.getValue(ATTR_HELP_KEY);
                if (help_key != null) {
                    PaletteItemDataObject.this.help_key=help_key;
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
    
    
    
    private static class PlainBeanCreateInfo implements BeanCreateInfo {
        
        private final String beanClassName;
        private final String displayName;
        
        public PlainBeanCreateInfo(String beanClassName, String displayName ) {
            this.beanClassName = beanClassName;
            this.displayName = displayName;
        }
        
        
        public String getBeanClassName() {
            return beanClassName;
        }
        
        public Result beanCreatedSetup(DesignBean designBean) {
            // XXX Don't do anything?
            return null;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return null;
        }
        
        public Image getLargeIcon() {
            return null;
        }
        
        public Image getSmallIcon() {
            return null;
        }
        
        public String getHelpKey() {
            return null;
        }
        
    }
    
    private static class DisplayItemTransferable implements Transferable {
        private String componentClassName;
        private String displayName;
        
        private static final String HUMAN_NAME = NbBundle.getMessage(PaletteItemDataObject.class, "HUMAN_NAME");
        
        private static final DataFlavor FLAVOR_DISPLAY_ITEM = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
                HUMAN_NAME);
        
        public DisplayItemTransferable(String componentClassName, String displayName){
            this.componentClassName = componentClassName;
            this.displayName = displayName;
        }
        
        
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{FLAVOR_DISPLAY_ITEM};
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if(FLAVOR_DISPLAY_ITEM.equals(flavor)) {
                return true;
            }
            return false;
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!FLAVOR_DISPLAY_ITEM.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return new PlainBeanCreateInfo( componentClassName, displayName );
            
        }
        
    }
    
    private static class PaletteItemInfoImpl implements PaletteItemInfoCookie {
        
        private final PaletteItemDataObject pido;
        
        PaletteItemInfoImpl(PaletteItemDataObject pido){
            this.pido = pido;
        }
        
        public String getClassName() {
            return pido.componentClassName;
        }

        public Image getIcon() {
            return pido.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        }

        public String getDisplayName() {
            return pido.getNodeDelegate().getDisplayName();
        }
           
    }

    
    //////////////////////
    // >>> Hack workaround.
    /** XXX Workaround of NB #82645. */
    private static void hackExplorerDnD(ItemNode itemNode, Transferable trans) {
        Object explorerDnDManager = getExplorerDnDManager();
        if (explorerDnDManager != null) {
            setNodeAllowedActions(explorerDnDManager, DnDConstants.ACTION_MOVE);
            setDraggedTransferable(explorerDnDManager, trans, true);
            setDraggedNodes(explorerDnDManager, new Node[] {itemNode});
            setDnDActive(explorerDnDManager, true);
        }
    }
    
    private static void setNodeAllowedActions(Object explorerDnDManager, int actions) {
        invokeOnExplorerDnDManager(explorerDnDManager,
                "setNodeAllowedActions", // NOI18N
                new Class[] {Integer.TYPE},
                new Object[] { Integer.valueOf(actions)});
    }
    
    private static void setDraggedTransferable(Object explorerDnDManager, Transferable trans, boolean isCut) {
        invokeOnExplorerDnDManager(explorerDnDManager,
                "setDraggedTransferable", // NOI18N
                new Class[] {Transferable.class, Boolean.TYPE},
                new Object[] {trans, Boolean.valueOf(isCut)});
    }
    
    private static void setDraggedNodes(Object explorerDnDManager, Node[] nodes) {
        invokeOnExplorerDnDManager(explorerDnDManager,
                "setDraggedNodes", // NOI18N
                new Class[] {Node[].class},
                new Object[] {nodes});
    }
    
    private static void setDnDActive(Object explorerDnDManager, boolean active) {
        invokeOnExplorerDnDManager(explorerDnDManager,
                "setDnDActive", // NOI18N
                new Class[] {Boolean.TYPE},
                new Object[] {Boolean.valueOf(active)});
    }
    
    private static Object getExplorerDnDManager() {
        ClassLoader contextCL = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class explorerDnDClass = Class.forName("org.openide.explorer.view.ExplorerDnDManager", true, contextCL); // NOI18N
            Method getDefaultMethod = explorerDnDClass.getDeclaredMethod("getDefault", new Class[0]); // NOI18N
            getDefaultMethod.setAccessible(true);
            return getDefaultMethod.invoke(null, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (SecurityException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    private static void invokeOnExplorerDnDManager(Object explorerDnDManager, String methodName, Class[] argumentTypes, Object[] arguments) {
        try {
            Method setNodeAllowedActions = explorerDnDManager.getClass().getDeclaredMethod(methodName, argumentTypes);
            setNodeAllowedActions.setAccessible(true);
            setNodeAllowedActions.invoke(explorerDnDManager, arguments);
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (SecurityException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    // <<< Hack workaround.
    //////////////////////
    
}
