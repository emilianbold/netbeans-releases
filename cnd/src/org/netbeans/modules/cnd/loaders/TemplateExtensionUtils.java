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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class TemplateExtensionUtils {

    public static final String CPP_TEMPLATE_PREFIX = "Templates/cppFiles";
    public static final String C_TEMPLATE_PREFIX = "Templates/cFiles";
    public static final String NAME_ATTRIBUTE = "name";

    public static String getCppExtension() {
        return findExtension(CPP_TEMPLATE_PREFIX, CCDataLoader.getInstance());
    }

    public static void renameCppExtension(String newExtension) {
        renameTemplateExtension(CPP_TEMPLATE_PREFIX, CCDataLoader.getInstance(), newExtension);
    }

    public static String getCppHExtension() {
        return findExtension(CPP_TEMPLATE_PREFIX, HDataLoader.getInstance());
    }

    public static void renameCppHExtension(String newExtension) {
        renameTemplateExtension(CPP_TEMPLATE_PREFIX, HDataLoader.getInstance(), newExtension);
    }

    public static String getCExtension() {
        return findExtension(C_TEMPLATE_PREFIX, CDataLoader.getInstance());
    }

    public static void renameCExtension(String newExtension) {
        renameTemplateExtension(C_TEMPLATE_PREFIX, CDataLoader.getInstance(), newExtension);
    }

    public static String getCHExtension() {
        return findExtension(C_TEMPLATE_PREFIX, HDataLoader.getInstance());
    }

    public static void renameCHExtension(String newExtension) {
        renameTemplateExtension(C_TEMPLATE_PREFIX, HDataLoader.getInstance(), newExtension);
    }

    public static String checkTemplate(String template) {
        if (template.startsWith(CPP_TEMPLATE_PREFIX)) {
            return checkTemplate(template, CPP_TEMPLATE_PREFIX, CCDataLoader.getInstance());
        } else if (template.startsWith(C_TEMPLATE_PREFIX)) {
            return checkTemplate(template, C_TEMPLATE_PREFIX, CDataLoader.getInstance());
        }
        return template;
    }

    private static String checkTemplate(String template, String folder, CndAbstractDataLoader loader) {
        String nameExt = template.substring(folder.length() + 1);
        String name = nameExt.substring(0, nameExt.indexOf('.'));
        FileObject dir = Repository.getDefault().getDefaultFileSystem().findResource(folder);
        if (dir != null) {
            for (FileObject fo : dir.getChildren()) {
                if (name.equals(fo.getName())) {
                    if (HDataLoader.getInstance().getExtensions().isRegistered(fo) &&
                            HDataLoader.getInstance().getExtensions().isRegistered(nameExt) ||
                            loader.getExtensions().isRegistered(fo) &&
                            loader.getExtensions().isRegistered(nameExt)) {
                        template = folder + "/" + fo.getNameExt();
                        try {
                            DataObject dao = DataObject.find(fo);
                            String displayName = (String) fo.getAttribute("name");
                            if (dao != null && displayName != null && dao.getNodeDelegate() != null) {
                                dao.getNodeDelegate().setDisplayName(displayName);
                            }
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                }
            }
        }
        return template;
    }

    private static String findExtension(String folder, CndAbstractDataLoader loader) {
        FileObject dir = Repository.getDefault().getDefaultFileSystem().findResource(folder);
        if (dir != null) {
            for (FileObject fo : dir.getChildren()) {
                if (loader.getExtensions().isRegistered(fo)) {
                    return fo.getExt();
                }
            }
        }
        return null;
    }

    private static void renameTemplateExtension(String folder, CndAbstractDataLoader loader, String newExtension) {
        FileObject dir = Repository.getDefault().getDefaultFileSystem().findResource(folder);
        if (dir != null) {
            String oldExtension = findExtension(folder, loader);
            if (oldExtension != null && !newExtension.equals(oldExtension)) {
                for (FileObject fo : dir.getChildren()) {
                    if (oldExtension.equals(fo.getExt())) {
                        try {
                            FileLock lock = fo.lock();
                            try {
                                Map<String, Object> attributes = new HashMap<String, Object>();
                                for (Enumeration<String> en = fo.getAttributes(); en.hasMoreElements();) {
                                    String key = en.nextElement();
                                    attributes.put(key, fo.getAttribute(key));
                                }
                                String name = null;
                                DataObject dao;
                                if (!attributes.containsKey(NAME_ATTRIBUTE)) {
                                    dao = DataObject.find(fo);
                                    if (dao != null) {
                                        name = dao.getNodeDelegate().getDisplayName();
                                        attributes.put(NAME_ATTRIBUTE, name);
                                    }
                                } else {
                                    name = (String) attributes.get(NAME_ATTRIBUTE);
                                }
                                fo.rename(lock, fo.getName(), newExtension);
                                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                                    fo.setAttribute(entry.getKey(), entry.getValue());
                                }
                                dao = DataObject.find(fo);
                                if (dao != null && name != null) {
                                    dao.getNodeDelegate().setDisplayName(name);
                                }
                            } finally {
                                lock.releaseLock();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}