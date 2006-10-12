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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject;

import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public enum ModuleType {
    
    WEB(NbBundle.getMessage(ModuleType.class, "CTL_WebModule")),
    EJB(NbBundle.getMessage(ModuleType.class, "CTL_EjbModule")),
    CLIENT(NbBundle.getMessage(ModuleType.class, "CTL_ClientModule"));
    
    private final String description;
    
    ModuleType(final String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /** Maps relative deployment descriptor's path to {@link ModuleType}. */
    private static final Map<String, ModuleType> DEFAULT_DD = new HashMap<String, ModuleType>();
    
    static {
        DEFAULT_DD.put("web/WEB-INF/web.xml", ModuleType.WEB); // NOI18N
        DEFAULT_DD.put("src/conf/ejb-jar.xml", ModuleType.EJB); // NOI18N
        DEFAULT_DD.put("src/conf/application-client.xml", ModuleType.CLIENT); // NOI18N
    }
    
    /**
     * Detects Enterprise Application modules in the <code>appRoot</code>'s
     * subfolders recursively.
     *
     * @param folder root folder - typically enterprise application folder
     * @return map of FileObject to ModuleType entries
     */
    public static Map<FileObject, ModuleType> detectModules(final FileObject appRoot) {
        Map<FileObject, ModuleType> descriptors =
                new HashMap<FileObject, ModuleType>();
        // do detection for each subdirectory
        for (FileObject subprojectRoot : appRoot.getChildren()) {
            if (subprojectRoot.isFolder()) {
                ModuleType type = ModuleType.detectModuleType(subprojectRoot);
                if (type != null) {
                    descriptors.put(subprojectRoot, type);
                }
            }
        }
        return descriptors;
    }
    
    /**
     * Tries to detect Enterprise Application module's type in the given folder.
     *
     * @param folder folder which possibly containing module
     * @return <code>null</code> if no module were detected; instance otherwise
     */
    public static ModuleType detectModuleType(final FileObject moduleRoot) {
        ModuleType result = null;
        for (Map.Entry<String, ModuleType> entry : DEFAULT_DD.entrySet()) {
            FileObject ddFO = moduleRoot.getFileObject(entry.getKey());
            if (ddFO != null && ddFO.isData()) { // deployment descriptor detected
                result = entry.getValue();
            }
        }
        return result;
    }
    
}
