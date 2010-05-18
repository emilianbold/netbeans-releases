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

package org.netbeans.modules.visualweb.complib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import org.netbeans.modules.visualweb.complib.Complib.Identifier;
import org.netbeans.modules.visualweb.complib.ComplibManifest.EeSpecVersion;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider.ComponentInfo;
import org.netbeans.modules.visualweb.complib.api.ComplibException;
import org.netbeans.modules.visualweb.palette.api.PaletteItemInfoCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * Facade to simplify interface to underlying NetBeans Palette API
 * 
 * @author Edwin Goei
 */
class PaletteUtil {
    /**
     * Model to represent a palette consisting of three kinds of nodes. Each node can either be a 1)
     * Palette 2) Category or 3) Item. A Palette can have Category children. A Category can have
     * Item children. NetBeans does not support an Item with Item children.
     * 
     * @author Edwin Goei
     */
    private static abstract class AbstractPaletteNode {
        private FileObject fileObject;

        /**
         * Call initFileObject() to fully init this class.
         */
        private AbstractPaletteNode() {
        }

        private AbstractPaletteNode(FileObject fo) {
            initFileObject(fo);
        }

        public void initFileObject(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public void remove() {
            /*
             * Deleting the FileObject itself causes intermittent exceptions so we destroy the Node
             * instead
             */
            try {
                DataObject dataObject = DataObject.find(fileObject);
                Node node = dataObject.getNodeDelegate();
                node.destroy();
            } catch (IOException e) {
                IdeUtil.logWarning("Unable to remove FileObject '" + fileObject.getNameExt() + "'",
                        e);
            }
        }

        protected FileObject getFileObject() {
            return fileObject;
        }

        @Override
        public String toString() {
            return getFileObject().getPath();
        }
    }

    static class Palette extends AbstractPaletteNode {
        private static WeakHashMap<FileObject, Category> foMap = new WeakHashMap<FileObject, Category>();

        private Palette(String path) {
            FileObject paletteFileObject = FileUtil.getConfigFile(path);
            initFileObject(paletteFileObject);
        }

        public List<Category> getChildren() {
            ArrayList<Category> result = new ArrayList<Category>();
            for (FileObject fo : getFileObject().getChildren()) {
                Category cat = foMap.get(fo);
                if (cat == null) {
                    cat = new Category(fo);
                    foMap.put(fo, cat);
                }
                result.add(cat);
            }
            return result;
        }

        /**
         * Get or create a category. If a new category is created, then it also tries to make the
         * category first, so the user can easily see it. If the category exists, don't change its
         * location because the user may have intentionally moved it there.
         * 
         * @param catName
         * @return
         * @throws ComplibException
         */
        public Category getOrCreateCategory(String catName) throws ComplibException {
            FileObject categoryFo;
            try {
                FileObject paletteFo = getFileObject();
                FileObject file = paletteFo.getFileObject(catName);
                if (file == null) {
                    // No category exists yet so create it
                    categoryFo = paletteFo.createFolder(catName);

                    // Tells NB not to issue warning about missing file attribute
                    categoryFo.setAttribute(POSITION, 0);

                    /*
                     * Mark this category as one that was created by the complib module so that it
                     * can be automatically removed later.
                     */
                    categoryFo.setAttribute(CREATED_BY_COMPLIB, Boolean.TRUE);

                    // Try to move new category to the top so user can see it
                    try {
                        makeFileObjectFirst(categoryFo);
                    } catch (IOException e1) {
                        IdeUtil.logWarning(e1);
                    }
                } else if (!file.isFolder()) {
                    // This should not normally happen
                    // Plain file was found
                    throw new ComplibException(
                            "Unable to create category folder, found plain file: " + file);
                } else {
                    // Folder was already created
                    categoryFo = file;
                }
            } catch (IOException e) {
                throw new ComplibException("Unable to create category", e);
            }

            return new Category(categoryFo);
        }

        private void makeFileObjectFirst(FileObject catFo) throws IOException {
            // Use DataFolder.setOrder() to set the ordering of categories
            DataFolder palDf = (DataFolder) DataObject.find(getFileObject());
            DataObject[] childrenArray = palDf.getChildren();
            List<DataObject> childrenList = Arrays.asList(childrenArray);
            // Use a LinkedList which supports List.remove()
            LinkedList<DataObject> children = new LinkedList<DataObject>(childrenList);
            DataObject catDo = DataObject.find(catFo);
            children.remove(catDo);
            children.add(0, catDo);
            childrenArray = (DataObject[]) children.toArray(new DataObject[children.size()]);
            palDf.setOrder(childrenArray);
        }
    }

    /**
     * Represents a category type node in the palette model.
     * 
     * @author Edwin Goei
     */
    static class Category extends AbstractPaletteNode {
        private static WeakHashMap<FileObject, Item> foMap = new WeakHashMap<FileObject, Item>();

        private Category(FileObject folder) {
            super(folder);
        }

        public List<Item> getChildren() {
            ArrayList<Item> result = new ArrayList<Item>();
            for (FileObject fo : getFileObject().getChildren()) {
                Item item = foMap.get(fo);
                if (item == null) {
                    try {
                        item = createItem(fo);
                    } catch (IOException e) {
                        IdeUtil.logWarning("Skipping child item FileObject", e);
                        continue;
                    }
                    foMap.put(fo, item);
                }
                result.add(item);
            }
            return result;
        }

        /**
         * Try to create an Item child of this parent from a FileObject and throw an exception if a
         * problem occurs.
         * 
         * @param fo
         * @return
         * @throws IOException
         */
        private Item createItem(FileObject fo) throws IOException {
            DataObject dataObject = DataObject.find(fo);

            Item newItem;
            if (dataObject instanceof ComplibPaletteItemDataObject) {
                // Complib palette item
                ComplibPaletteItemDataObject cpido = (ComplibPaletteItemDataObject) dataObject;
                newItem = new Item(fo, cpido.getClassName(), cpido.getComplib());
            } else {
                // Built-in complib palette item currently comes from the rave
                // palette module

                // TODO workaround: get the display name to force a file read
                dataObject.getNodeDelegate().getDisplayName();

                PaletteItemInfoCookie itemInfo = (PaletteItemInfoCookie) dataObject
                        .getCookie(PaletteItemInfoCookie.class);
                if (itemInfo == null) {
                    throw new IOException("PaletteItemInfoCookie is null");
                }
                newItem = new Item(fo, itemInfo.getClassName(), null);
            }
            return newItem;
        }

        /**
         * Create a child Item using the appropriate component info
         * 
         * @param compInfo
         * @throws IOException
         */
        public void createItem(ComponentInfo compInfo) throws IOException {
            final String className = compInfo.getClassName();
            Complib complib = compInfo.getComplib();
            final Identifier complibId = complib.getIdentifier();

            FileUtil.runAtomicAction(
                    new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            try {
                                ComplibPaletteItemDataObject.createFile(getFileObject(), className,
                                        complibId.getNamespaceUriString(), complibId
                                                .getVersionString());
                            } catch (IOException e) {
                                IdeUtil.logWarning("Unable to create complib palette item file", e);
                                throw e;
                            }
                        }
                    });
        }

        public String getName() {
            return getFileObject().getNameExt();
        }

        @Override
        public String toString() {
            return getName() + "{" + super.toString() + "}";
        }

        public boolean isCreatedByComplib() {
            return PaletteUtil.isCreatedByComplib(getFileObject());
        }
    }

    /**
     * Represents an item type node in the palette model.
     * 
     * @author Edwin Goei
     */
    static class Item extends AbstractPaletteNode {
        private static final Identifier INVALID_ID = new Identifier("urn:invalid-complib-id",
                "1.0.0");

        private String className;

        private Complib complib;

        private Item(FileObject fo, String className, Complib complib) {
            super(fo);
            this.className = className;
            this.complib = complib;
        }

        public String getClassName() {
            return className;
        }

        public Identifier getComplibId() {
            // TODO Figure out a better way to handle null complib??
            if (complib == null) {
                return INVALID_ID;
            }

            return complib.getIdentifier();
        }

        public Complib getComplib() {
            return complib;
        }

        @Override
        public String toString() {
            return getClassName() + "{" + super.toString() + "}";
        }
    }

    // TODO Danger: this name needs to remain in sync with DesignerTopComponent
    private static final Palette J2EE_1_4 = new Palette("CreatorDesignerPalette");

    private static final Palette JAVA_EE_5 = new Palette("CreatorDesignerPalette5");

    private static final List<Palette> PALETTES_FOR_J2EE_1_4;

    private static final List<Palette> PALETTES_FOR_JAVA_EE_5;

    static {
        PALETTES_FOR_JAVA_EE_5 = new ArrayList<Palette>(1);
        PALETTES_FOR_JAVA_EE_5.add(JAVA_EE_5);

        PALETTES_FOR_J2EE_1_4 = new ArrayList<Palette>(PALETTES_FOR_JAVA_EE_5);
        PALETTES_FOR_J2EE_1_4.add(J2EE_1_4);
    }

    private static final String CREATED_BY_COMPLIB = "created-by-complib";

    /**
     * Documented in http://wiki.netbeans.org/wiki/view/FolderOrdering103187 but I don't see where
     * this constant is defined anywhere so we define it here.
     */
    private static final String POSITION = "position";

    /**
     * Return a list of palettes to install components from a complib based on the required Java EE
     * spec version declared in the complib.
     * 
     * @param complib
     * @return
     */
    public static List<Palette> getPaletteRoots(Complib complib) {
        return complib.getCompLibManifest().getEeSpecVersion() == EeSpecVersion.J2EE_1_4 ? PALETTES_FOR_J2EE_1_4
                : PALETTES_FOR_JAVA_EE_5;
    }

    /**
     * Returns a list of all categories from all palettes
     * 
     * @return
     */
    public static List<Category> getAllCategories() {
        ArrayList<Category> retVal = new ArrayList<Category>();
        for (Palette pal : PALETTES_FOR_J2EE_1_4) {
            retVal.addAll(pal.getChildren());
        }
        return retVal;
    }

    /**
     * Return true iff the category represented by the FileObject was automatically created by the
     * complib module as opposed, for example, created by the user.
     * 
     * @param category
     * @return
     */
    public static boolean isCreatedByComplib(FileObject category) {
        return category.getAttribute(CREATED_BY_COMPLIB) != null;
    }
}
