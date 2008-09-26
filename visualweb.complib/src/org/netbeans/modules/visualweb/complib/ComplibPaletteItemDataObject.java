/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.complib;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.impl.BasicBeanCreateInfo;

/**
 * DataObject for palette item file. It reads the file and creates PaletteItem
 * and node from it.
 * 
 * @author Tomas Pavek
 * @author Edwin Goei
 */

class ComplibPaletteItemDataObject extends MultiDataObject {
    private static final String ELM_COMPLIB_ITEM = "complibItem";

    private static final String ATTR_VERSION = "version"; // NOI18N

    private static final String ELM_COMPONENT = "component"; // NOI18N

    private static final String ATTR_CLASSNAME = "className"; // NOI18N

    private static final String ATTR_COMPLIB_VERSION = "complibVersion";

    private static final String ATTR_COMPLIB_NAMESPACE = "complibNamespace";

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private boolean fileLoaded; // at least tried to load

    private BeanInfo beanInfo;

    private String className;

    private Complib complib;

    ComplibPaletteItemDataObject(FileObject fo, MultiFileLoader loader)
            throws IOException {
        super(fo, loader);
    }

    public Node createNodeDelegate() {
        return new ItemNode();
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    /**
     * Returns the complib associated with this palette item (normal case) or
     * null if one cannot be found. For example, it is possible for someone to
     * muck with their userdir such that they remove a complib, but yet the
     * palette data still exists and now refers to a non-existant complib. In
     * this case, null will be returned here.
     * 
     * @return associated complib or null
     */
    public Complib getComplib() {
        loadFile();
        return complib;
    }

    private BeanInfo getComponentBeanInfo() {
        if (beanInfo == null) {
            Complib aComplib = getComplib();
            if (aComplib == null) {
                // Complib is missing so try to recover
                beanInfo = getFallbackBeanInfo();
            } else {
                try {
                    // Normal case
                    beanInfo = aComplib.getBeanInfo(className);
                } catch (Exception e) {
                    // Unable to load BeanInfo so try to recover
                    IdeUtil.logError(e);
                    beanInfo = getFallbackBeanInfo();
                }
            }
        }
        return beanInfo;
    }

    private BeanInfo getFallbackBeanInfo() {
        beanInfo = UnknownBeanInfo.getInstance();

        // Try to recover by removing the palette item file object
        FileObject primaryFile = getPrimaryFile();
        try {
            primaryFile.delete();
        } catch (IOException e1) {
            IdeUtil.logWarning("Unable to remove FileObject '"
                    + primaryFile.getNameExt() + "'", e1);
        }

        return beanInfo;
    }

    private void loadFile() {
        if (fileLoaded) {
            return;
        }

        // Synchronized to prevent partial file reads. See createFile().
        synchronized (getClass()) {
            PaletteItemHandler handler = null;

            // parse the XML file
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                handler = new PaletteItemHandler();
                reader.setContentHandler(handler);

                InputSource input = new InputSource(getPrimaryFile().getURL()
                        .toExternalForm());
                reader.parse(input);
                // TODO report errors, validate using DTD?
            } catch (SAXException saxex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        saxex);
            } catch (IOException ioex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        ioex);
            }

            className = handler.getClassName();
            String namespace = handler.getNamespace();
            String version = handler.getVersion();

            // Try to find the installed complib
            complib = ComplibServiceProvider.getInstance().getInstalledComplib(
                    namespace, version);

            fileLoaded = true;
        }
    }

    /**
     * Returns the class name associated with this palette item.
     * 
     * @return class name of palette item
     */
    public String getClassName() {
        loadFile();
        return className;
    }

    /**
     * Create a file corresponding to a palette item. This method is
     * synchronized so that other threads that read the file via loadFile() will
     * not get partial file contents.
     * 
     * @param parent
     * @param className
     * @param namespace
     * @param version
     * @return
     * @throws IOException
     */
    static synchronized FileObject createFile(FileObject parent,
        String className, String namespace, String version) throws IOException {
        String fileExtension = PaletteItemDataLoader.ITEM_EXT;
        String baseFileName = FileUtil.findFreeFileName(parent, IdeUtil
                .baseClassName(className), fileExtension);

        XmlUtil xmlOut = new XmlUtil();
        Document doc = xmlOut.createDocument();

        Element rootElm = doc.createElement(ELM_COMPLIB_ITEM);
        rootElm.setAttribute(ATTR_VERSION, "1.0");
        doc.appendChild(rootElm);

        Element compElm = doc.createElement(ELM_COMPONENT);
        compElm.setAttribute(ATTR_CLASSNAME, className);
        compElm.setAttribute(ATTR_COMPLIB_NAMESPACE, namespace);
        compElm.setAttribute(ATTR_COMPLIB_VERSION, version);
        rootElm.appendChild(compElm);

        FileObject itemFO = parent.createData(baseFileName, fileExtension);
        File itemFile = FileUtil.toFile(itemFO);
        xmlOut.write(itemFile);
        return itemFO;
    }

    /** DataLoader for the palette item files. */
    public static final class PaletteItemDataLoader extends UniFileLoader {

        static final String ITEM_EXT = "complib_item"; // NOI18N

        PaletteItemDataLoader() {
            super(ComplibPaletteItemDataObject.class.getName());

            ExtensionList ext = new ExtensionList();
            //ext.addExtension(ITEM_EXT);
            //setExtensions(ext);
            getExtensions().addMimeType("text/x-complib-palette+xml");
        }

        /** Gets default display name. Overides superclass method. */
        protected String defaultDisplayName() {
            return NbBundle.getBundle(ComplibPaletteItemDataObject.class)
                    .getString("PROP_PaletteItemLoader_Name"); // NOI18N
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile)
                throws DataObjectExistsException, IOException {
            return new ComplibPaletteItemDataObject(primaryFile, this);
        }
    }

    public static final class PaletteItemDataLoaderBeanInfo extends
            SimpleBeanInfo {
        private static String iconURL = "org/netbeans/modules/form/resources/palette_manager.png"; // NOI18N

        public BeanInfo[] getAdditionalBeanInfo() {
            try {
                return new BeanInfo[] { Introspector
                        .getBeanInfo(UniFileLoader.class) };
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
            super(ComplibPaletteItemDataObject.this, Children.LEAF);
        }

        public String getDisplayName() {
            BeanInfo componentInfo = getComponentBeanInfo();
            return componentInfo == null ? "Unknown" : componentInfo
                    .getBeanDescriptor().getDisplayName();
        }

        public String getShortDescription() {
            BeanInfo componentInfo = getComponentBeanInfo();
            return componentInfo == null ? "Unknown" : componentInfo
                    .getBeanDescriptor().getShortDescription();
        }

        public boolean canRename() {
            return false;
        }

        public Image getIcon(int type) {
            BeanInfo componentInfo = getComponentBeanInfo();
            Image icon = componentInfo == null ? null : componentInfo
                    .getIcon(type);
            if (icon == null) {
                icon = Utilities
                        .loadImage("org/netbeans/modules/visualweb/palette/resources/custom_component.png"); // NOI18N
            }
            return icon;
        }

        // TODO properties
        public Node.PropertySet[] getPropertySets() {
            return NO_PROPERTIES;
        }

        public Transferable clipboardCopy() throws IOException {
            // ensureComplibCopiedToProject();
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
            // ensureComplibCopiedToProject();
            Transferable t = super.drag();
            addPaletteFlavor(t);

            // XXX NB#82645, when the issue is fixed, remove this.
            hackExplorerDnD(this, t);

            return t;
        }

        private Transferable addPaletteFlavor(Transferable t) {
            // ExTransferable allows you to add additional flavors.
            ExTransferable et = ExTransferable.create(t);

            // The DisplayItem Flavor make items dropable.
            String humanName = NbBundle.getMessage(
                    ComplibPaletteItemDataObject.class, "humanName");
            DataFlavor df = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + "; class="
                            + DisplayItem.class.getName(), humanName);

            ExTransferable.Single ex_sgl = new ExTransferable.Single(df) {
                protected Object getData() throws IOException,
                        UnsupportedFlavorException {
                    return new BasicBeanCreateInfo(getClassName(),
                            getDisplayName());
                }

            };
            et.put(ex_sgl);

            return t;
        }
    }

    private class PaletteItemHandler extends DefaultHandler {
        private String className;

        private String namespace;

        private String version;

        public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
            if (ELM_COMPLIB_ITEM.equals(qName)) {
                String version = attributes.getValue(ATTR_VERSION);
                if (version == null) {
                    String message = NbBundle.getBundle(
                            ComplibPaletteItemDataObject.class).getString(
                            "MSG_UnknownPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                } else if (!version.startsWith("1.")) { // NOI18N
                    String message = NbBundle.getBundle(
                            ComplibPaletteItemDataObject.class).getString(
                            "MSG_UnsupportedPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                }
            } else if (ELM_COMPONENT.equals(qName)) {
                className = attributes.getValue(ATTR_CLASSNAME);
                namespace = attributes.getValue(ATTR_COMPLIB_NAMESPACE);
                version = attributes.getValue(ATTR_COMPLIB_VERSION);
            }
        }

        public String getClassName() {
            return className;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getVersion() {
            return version;
        }
    }

    private static class UnknownBeanInfo extends SimpleBeanInfo {
        private BeanDescriptor beanDescriptor;

        private static final UnknownBeanInfo INSTANCE = new UnknownBeanInfo();

        public static UnknownBeanInfo getInstance() {
            return INSTANCE;
        }

        public BeanDescriptor getBeanDescriptor() {
            if (beanDescriptor == null) {
                beanDescriptor = new UnknownBeanDescriptor();
            }
            return beanDescriptor;
        }

        public Image getIcon(int iconKind) {
            return Utilities
                    .loadImage("org/netbeans/modules/visualweb/palette/resources/custom_component.png"); // NOI18N
        }
    }

    private static class UnknownBeanDescriptor extends BeanDescriptor {
        public UnknownBeanDescriptor() {
            super(Object.class);
        }

        public String getDisplayName() {
            return "Unknown";
        }

        public String getShortDescription() {
            return "Unknown short description";
        };
    }

    // ////////////////////
    // >>> Hack workaround.
    /** XXX Workaround of NB #82645. */
    private static void hackExplorerDnD(ItemNode itemNode, Transferable trans) {
        Object explorerDnDManager = getExplorerDnDManager();
        if (explorerDnDManager != null) {
            setNodeAllowedActions(explorerDnDManager, DnDConstants.ACTION_MOVE);
            setDraggedTransferable(explorerDnDManager, trans, true);
            setDraggedNodes(explorerDnDManager, new Node[] { itemNode });
            setDnDActive(explorerDnDManager, true);
        }
    }

    private static void setNodeAllowedActions(Object explorerDnDManager,
        int actions) {
        invokeOnExplorerDnDManager(explorerDnDManager, "setNodeAllowedActions", // NOI18N
                new Class[] { Integer.TYPE }, new Object[] { Integer
                        .valueOf(actions) });
    }

    private static void setDraggedTransferable(Object explorerDnDManager,
        Transferable trans, boolean isCut) {
        invokeOnExplorerDnDManager(explorerDnDManager,
                "setDraggedTransferable", // NOI18N
                new Class[] { Transferable.class, Boolean.TYPE }, new Object[] {
                        trans, Boolean.valueOf(isCut) });
    }

    private static void setDraggedNodes(Object explorerDnDManager, Node[] nodes) {
        invokeOnExplorerDnDManager(explorerDnDManager, "setDraggedNodes", // NOI18N
                new Class[] { Node[].class }, new Object[] { nodes });
    }

    private static void setDnDActive(Object explorerDnDManager, boolean active) {
        invokeOnExplorerDnDManager(explorerDnDManager, "setDnDActive", // NOI18N
                new Class[] { Boolean.TYPE }, new Object[] { Boolean
                        .valueOf(active) });
    }

    private static Object getExplorerDnDManager() {
        ClassLoader contextCL = (ClassLoader) Lookup.getDefault().lookup(
                ClassLoader.class);
        try {
            Class explorerDnDClass = Class.forName(
                    "org.openide.explorer.view.ExplorerDnDManager", true,
                    contextCL); // NOI18N
            Method getDefaultMethod = explorerDnDClass.getDeclaredMethod(
                    "getDefault", new Class[0]); // NOI18N
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

    private static void invokeOnExplorerDnDManager(Object explorerDnDManager,
        String methodName, Class[] argumentTypes, Object[] arguments) {
        try {
            Method setNodeAllowedActions = explorerDnDManager.getClass()
                    .getDeclaredMethod(methodName, argumentTypes);
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
    // ////////////////////
}
