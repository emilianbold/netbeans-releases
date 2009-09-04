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
package org.netbeans.modules.dlight.api.tool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 */
final class ToolsConfiguration {

    private static final String ENABLE_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";//NOI18N
    private static final String KNOWN_TOOLS_SET = "KnownToolsConfigurationProviders"; //NOI18N
    private final FileObject rootFolder;
    private boolean useRootFolder = false;
    private List<DLightTool> cachedList = null;
    //String - toolID, FileObject - FileObject
    private final HashMap<String, FileObject> toolsProviders = new HashMap<String, FileObject>();

    static ToolsConfiguration create(FileObject fileObject) {
        return new ToolsConfiguration(fileObject, false);
    }

    static ToolsConfiguration createDefault(FileObject fileObject) {
        return new ToolsConfiguration(fileObject, true);
    }

    private ToolsConfiguration(FileObject fileObject, boolean useRootFolder) {
        this.rootFolder = fileObject;
        this.useRootFolder = useRootFolder;
    }

    final List<DLightTool> getToolsSet(boolean cached) {
        if (!cached || cachedList == null) {
            return getToolsSet();
        }
        return cachedList;
    }

    FileObject getFileObject(String toolID) {
        if (toolsProviders.isEmpty()) {
            //tru to read
            getToolsSet();
        }
        return toolsProviders.get(toolID);
    }

    boolean remove(String toolID) {
        if (toolsProviders.isEmpty()) {
            //tru to read
            getToolsSet();
        }
        FileObject fo = toolsProviders.get(toolID);
        if (fo == null) {
            return false;
        }
        try {
            fo.delete();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    boolean register(FileObject fileObject, boolean isEnabled) {
        FileObject configurationsFolder = useRootFolder ? rootFolder : rootFolder.getFileObject(KNOWN_TOOLS_SET);

        if (configurationsFolder == null) {
            return false;
        }

        final String fname = fileObject.getName();
        final String shadowExt = "shadow"; // NOI18N

        if (configurationsFolder.getFileObject(fname, shadowExt) == null) {
            try {
                FileObject fo = configurationsFolder.createData(fname, shadowExt);
                fo.setAttribute("originalFile", fileObject.getPath()); // NOI18N
                fo.setAttribute(ENABLE_BY_DEFAULT_ATTRIBUTE, isEnabled);
            } catch (IOException ex) {
                return false;
            }

        } else {
            FileObject fo = configurationsFolder.getFileObject(fname, shadowExt);
            try {
                fo.setAttribute(ENABLE_BY_DEFAULT_ATTRIBUTE, isEnabled);
            } catch (IOException ex) {
                return false;
            }
        }
        // try {
//            DataObject dobj = DataObject.find(fileObject);
//            DataFolder configurationDataObject  = DataFolder.findFolder(configurationsFolder);
//            try {
//                DataShadow shadow = dobj.createShadow(configurationDataObject);
//
//            } catch (IOException ex) {
//                return false;
//            }
//        } catch (DataObjectNotFoundException ex) {
//            return false;
//        }
        return true;
    }

    /**
     * Returns tools set which will be used to run {@link org.netbeans.modules.dlight.core.execution.model.DLightTarget} with
     * @return tools set
     */
    final List<DLightTool> getToolsSet() {
        List<DLightTool> result = new ArrayList<DLightTool>();

        FileObject configurationsFolder = useRootFolder ? rootFolder : rootFolder.getFileObject(KNOWN_TOOLS_SET);

        if (configurationsFolder == null) {
            return result;
        }

        FileObject[] children = configurationsFolder.getChildren();

        if (children == null || children.length == 0) {
            return result;
        }

        for (FileObject child : children) {
            Enumeration<String> attrs = child.getAttributes();
            DataObject dobj = null;
            try {
                dobj = DataObject.find(child);
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(ToolsConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }

            InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
            if (ic == null) {
                String message = "D-Light tool configuration " + child.getName() + " not found"; //NOI18N
                Logger.getLogger(ToolsConfiguration.class.getName()).log(Level.SEVERE, message, new Exception(message));
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                Class<? extends DLightToolConfigurationProvider> clazz = (Class<? extends DLightToolConfigurationProvider>) ic.instanceClass();
                DLightToolConfigurationProvider configurationProvider = clazz.getConstructor().newInstance();
                DLightTool tool = DLightToolAccessor.getDefault().newDLightTool(configurationProvider.create());
                toolsProviders.put(tool.getID(), child);
                boolean enabledByDefault = true;
                while (attrs.hasMoreElements()) {
                    String an = attrs.nextElement();
                    if (ENABLE_BY_DEFAULT_ATTRIBUTE.equals(an)) {
                        enabledByDefault = (Boolean) child.getAttribute(an);
                        break;
                    }
                }
                if (enabledByDefault) {
                    tool.enable();
                } else {
                    tool.disable();
                }
                result.add(tool);
//        Class<? extends DLightTool.Configuration> clazz = (Class<? extends DLightTool>) ic.instanceClass();
//        result.add(clazz.getConstructor().newInstance());
            } catch (InstantiationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (cachedList == null) {
            cachedList = result;
        }
        return result;
    }
}
