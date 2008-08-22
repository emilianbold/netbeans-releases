/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.core.ui.options.filetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Model holds mapping between extension and MIME type.
 *
 * @author Jiri Skrivanek
 */
final class FileAssociationsModel {

    private static final String MIME_RESOLVERS_PATH = "Services/MIMEResolver";  //NOI18N
    private static final String USER_DEFINED = "user-defined-mime-resolver";  //NOI18N
    /** Position of user-defined mime resolver. Need to very low to override all other resolvers. */
    private static final int USER_DEFINED_POSITION = 10;
    private static final Logger LOGGER = Logger.getLogger(FileAssociationsModel.class.getName());
    /** Maps both system and user-defined extensions to MIME type. */
    private HashMap<String, String> extensionToMimeAll = new HashMap<String, String>();
    /** Maps system extensions to MIME type. */
    private HashMap<String, String> extensionToMimeSystem = new HashMap<String, String>();
    /** Maps user-defined extensions to MIME type. */
    private HashMap<String, String> extensionToMimeUser = new HashMap<String, String>();
    /** Ordered set of all MIME types registered in system. */
    private TreeSet<String> mimeTypes = new TreeSet<String>();
    /** Maps MIME type to MimeItem object which holds display name. */
    private HashMap<String, MimeItem> mimeToItem = new HashMap<String, MimeItem>();
    private boolean initialized = false;
    private final FileChangeListener mimeResolversListener = new FileChangeAdapter() {
        public @Override void fileDeleted(FileEvent fe) {
            initialized = false;
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            initialized = false;
        }
        public @Override void fileDataCreated(FileEvent fe) {
            initialized = false;
        }
        public @Override void fileChanged(FileEvent fe) {
            initialized = false;
        }
    };
    private FileObject userDefinedResolverFO;

    /** Creates new model. */
    FileAssociationsModel() {
        FileObject resolvers = Repository.getDefault().getDefaultFileSystem().findResource(MIME_RESOLVERS_PATH);
        if (resolvers != null) {
            resolvers.addFileChangeListener(FileUtil.weakFileChangeListener(mimeResolversListener, resolvers));
        }
    }

    /** Returns true if model includes given extension. */
    boolean containsExtension(String extension) {
        return extensionToMimeAll.containsKey(extension);
    }

    /** Returns string of extensions also associated with given MIME type
     * excluding given extension.
     * @param extension extension to be excluded from the list
     * @param newMimeType MIME type of interest
     * @return comma separated list of extensions (e.g. "gif, jpg, bmp")
     */
    String getAssociatedAlso(String extension, String newMimeType) {
        StringBuilder result = new StringBuilder();
        for (String extensionKey : getExtensions()) {
            if (!extensionKey.equals(extension) && extensionToMimeAll.get(extensionKey).equals(newMimeType)) {
                if (result.length() != 0) {
                    result.append(", ");  //NOI18N
                }
                result.append(extensionKey);
            }
        }
        return result.toString();
    }

    /** Returns ordered list of registered extensions.
     * @return list of ordered extensions
     */
    List<String> getExtensions() {
        init();
        ArrayList<String> list = new ArrayList<String>(extensionToMimeAll.keySet());
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    /** Returns ordered set of all known MIME types
     * @return ordered set of MIME types
     */
    Set<String> getMimeTypes() {
        init();
        return mimeTypes;
    }

    /** Reads MIME types registered in Loaders folder and fills mimeTypes set. */
    private void readMimeTypesFromLoaders() {
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject[] children = defaultFS.findResource("Loaders").getChildren();  //NOI18N
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];
            String mime1 = child.getNameExt();
            FileObject[] subchildren = child.getChildren();
            for (int j = 0; j < subchildren.length; j++) {
                FileObject subchild = subchildren[j];
                FileObject factoriesFO = subchild.getFileObject("Factories");  //NOI18N
                if(factoriesFO != null && factoriesFO.getChildren().length > 0) {
                    // add only MIME types where some loader exists
                    mimeTypes.add(mime1 + "/" + subchild.getNameExt()); //NOI18N
                }
            }
        }
        mimeTypes.remove("content/unknown"); //NOI18N
    }

    /** Returns MIME type corresponding to given extension. Cannot return null. */
    String getMimeType(String extension) {
        init();
        return extensionToMimeAll.get(extension);
    }

    /** Returns MimeItem corresponding to given extension. */
    MimeItem getMimeItem(String extension) {
        return mimeToItem.get(getMimeType(extension));
    }

    /** Removes user defined extension to MIME type mapping. */
    void remove(String extension) {
        extensionToMimeUser.remove(extension);
        extensionToMimeAll.remove(extension);
    }

    /** Sets default (system) MIME type for given extension. */
    void setDefault(String extension) {
        remove(extension);
        extensionToMimeAll.put(extension, extensionToMimeSystem.get(extension));
    }

    /** Sets new extension to MIME type mapping (only if differs from current).
     * Returns true if really changed, false otherwise. */
    boolean setMimeType(String extension, String newMimeType) {
        String oldMmimeType = getMimeType(extension);
        if (!newMimeType.equals(oldMmimeType)) {
            LOGGER.fine("setMimeType - " + extension + "=" + newMimeType);
            extensionToMimeUser.put(extension, newMimeType);
            extensionToMimeAll.put(extension, newMimeType);
            return true;
        }
        return false;
    }

    /** Returns true if mapping of extension to MIME type was changed and 
     * exists default/system mapping. */
    boolean canBeRestored(String extension) {
        return extensionToMimeUser.containsKey(extension) && extensionToMimeSystem.containsKey(extension);
    }

    /** Returns true if extension doesn't have default/system mapping. */
    boolean canBeRemoved(String extension) {
        return !extensionToMimeSystem.containsKey(extension);
    }
    
        /** Returns localized display name of loader for given MIME type or null if not defined. */
    private static String getLoaderDisplayName(String mimeType) {
        FileSystem root = Repository.getDefault().getDefaultFileSystem();
        FileObject factoriesFO = root.findResource("Loaders/" + mimeType + "/Factories");  //NOI18N
        if(factoriesFO != null) {
            FileObject[] children = factoriesFO.getChildren();
            for (FileObject child : children) {
                String childName = child.getNameExt();
                String displayName = root.getStatus().annotateName(childName, Collections.singleton(child));
                if(!childName.equals(displayName)) {
                    return displayName;
                }
            }
        }
        return null;
    }

    /** Returns sorted list of MimeItem objects. */
    ArrayList<MimeItem> getMimeItems() {
        init();
        ArrayList<MimeItem> items = new ArrayList<MimeItem>(mimeToItem.values());
        Collections.sort(items);
        return items;
    }
    
    /** Stores current state of model. It deletes user-defined mime resolver
     * and writes a new one. */
    void store() {
        if (userDefinedResolverFO != null) {
            try {
                // delete previous resolver because we need to refresh MIMEResolvers
                userDefinedResolverFO.delete();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Cannot delete resolver " + FileUtil.toFile(userDefinedResolverFO), e);  //NOI18N
                return;
            }
        }
        if (extensionToMimeUser.isEmpty()) {
            // nothing to write
            return;
        }

        FileUtil.runAtomicAction(new Runnable() {

            public void run() {
                Document document = XMLUtil.createDocument("MIME-resolver", null, "-//NetBeans//DTD MIME Resolver 1.0//EN", "http://www.netbeans.org/dtds/mime-resolver-1_0.dtd");  //NOI18N
                for (String extension : extensionToMimeUser.keySet()) {
                    Element fileElement = document.createElement("file");  //NOI18N
                    Element extElement = document.createElement("ext");  //NOI18N
                    Element resolverElement = document.createElement("resolver");  //NOI18N
                    extElement.setAttribute("name", extension);  //NOI18N
                    resolverElement.setAttribute("mime", extensionToMimeUser.get(extension));  //NOI18N
                    fileElement.appendChild(extElement);
                    fileElement.appendChild(resolverElement);
                    document.getDocumentElement().appendChild(fileElement);
                }

                OutputStream os = null;
                try {
                    FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
                    userDefinedResolverFO = defaultFS.findResource(MIME_RESOLVERS_PATH).createData(USER_DEFINED + ".xml");  //NOI18N
                    userDefinedResolverFO.setAttribute(USER_DEFINED, Boolean.TRUE);
                    userDefinedResolverFO.setAttribute("position", USER_DEFINED_POSITION);  //NOI18N
                    os = userDefinedResolverFO.getOutputStream();
                    XMLUtil.write(document, os, "UTF-8"); //NOI18N
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Cannot write resolver " + FileUtil.toFile(userDefinedResolverFO), e);  //NOI18N
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Cannot close OutputStreamof file " + FileUtil.toFile(userDefinedResolverFO), e);  //NOI18N
                        }
                    }
                }
            }
        });
    }

    private void init() {
        if (initialized) {
            return;
        }
        LOGGER.fine("FileAssociationsModel.init");  //NOI18N
        initialized = true;
        // scan resolvers and order them to be able to assign extension to mime type from resolver with the lowest position
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject[] resolvers = defaultFS.findResource(MIME_RESOLVERS_PATH).getChildren();
        TreeMap<Integer, FileObject> orderedResolvers = new TreeMap<Integer, FileObject>(Collections.reverseOrder());
        for (int i = 0; i < resolvers.length; i++) {
            FileObject mimeResolverFO = resolvers[i];
            Integer position = (Integer) mimeResolverFO.getAttribute("position");  //NOI18N
            if (position == null) {
                position = Integer.MAX_VALUE;
            }
            while (orderedResolvers.containsKey(position)) {
                position--;
            }
            orderedResolvers.put(position, mimeResolverFO);
        }
        Iterator<FileObject> resolversIter = orderedResolvers.values().iterator();
        while (resolversIter.hasNext()) {
            FileObject mimeResolverFO = resolversIter.next();
            boolean userDefined = mimeResolverFO.getAttribute(USER_DEFINED) != null;
            if (userDefined) {
                userDefinedResolverFO = mimeResolverFO;
            }
            assert mimeResolverFO.getPath().startsWith("Services/MIMEResolver");  //NOI18N
            List<String[]> extAndMimePairs = MIMEResolverImpl.getExtensionsAndMIMETypes(mimeResolverFO);
            Iterator<String[]> iter = extAndMimePairs.iterator();
            while (iter.hasNext()) {
                String[] pair = iter.next();
                String extension = pair[0];
                String mimeType = pair[1];
                if (extension != null) {
                    extensionToMimeAll.put(extension, mimeType);
                    if (userDefined) {
                        extensionToMimeUser.put(extension, mimeType);
                    } else {
                        extensionToMimeSystem.put(extension, mimeType);
                    }
                }
                mimeTypes.add(mimeType);
            }
        }
        readMimeTypesFromLoaders();
        // init mimeItems
        for (String mimeType : mimeTypes) {
            MimeItem mimeItem = new MimeItem(mimeType, getLoaderDisplayName(mimeType));
            mimeToItem.put(mimeType, mimeItem);
        }
        LOGGER.fine("extensionToMimeSystem=" + extensionToMimeSystem);  //NOI18N
        LOGGER.fine("extensionToMimeUser=" + extensionToMimeUser);  //NOI18N
    }
    
    /** To store MIME type and its loader display name. It is used in combo box. */
    static final class MimeItem implements Comparable {

        String mimeType;
        String displayName;

        MimeItem(String mimeType, String displayName) {
            this.mimeType = mimeType;
            this.displayName = displayName;
        }

        String getMimeType() {
            return mimeType;
        }

        @Override
        public String toString() {
            return displayName == null ? mimeType : displayName + " (" + mimeType + ")";
        }

        public int compareTo(Object o) {
            return toString().compareToIgnoreCase(o.toString());
        }
    }
}
