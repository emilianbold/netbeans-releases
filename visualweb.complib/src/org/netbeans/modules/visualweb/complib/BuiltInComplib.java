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

package org.netbeans.modules.visualweb.complib;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openide.modules.InstalledFileLocator;

/**
 * Represents the built-in component library
 *
 * @author Edwin Goei
 */
public class BuiltInComplib extends Complib {
    private static BuiltInComplib instance;

    /**
     * Returns a single instance. Main entry point.
     *
     * @return
     */
    public static BuiltInComplib getInstance() {
        if (instance == null) {
            try {
                instance = new BuiltInComplib();
            } catch (Exception e) {
                IdeUtil.logError(e);
            }
        }
        return instance;
    }

    private BuiltInComplib() throws XmlException, ComplibException {
        // TODO this should probably be read in from a NB layer file instead
        URL configUrl = BuiltInComplib.class.getResource("built-in-config.xml"); // NOI18N
        ComplibManifest compLibManifest = ComplibManifest.getInstance(
                configUrl, getClassLoader());
        initCompLibManifest(compLibManifest);

        initPaths();
    }

    protected List<File> convertConfigPathToFileList(List<String> path)
            throws ComplibException {
        ArrayList<File> retVal = new ArrayList<File>(path.size());
        for (String pathElm : path) {
            File file = InstalledFileLocator.getDefault().locate(pathElm, null,
                    false);
            retVal.add(file);
        }
        return retVal;
    }

    public ClassLoader getClassLoader() {
        // Use the NB toolbox module ClassLoader
        ClassLoader nbModuleClassLoader = BuiltInComplib.class.getClassLoader();
        return nbModuleClassLoader;
    }

    BeanInfo getBeanInfo(String className) throws ClassNotFoundException,
            IntrospectionException {
        Class beanClass = Class.forName(className, true, getClassLoader());
        return Introspector.getBeanInfo(beanClass);
    }
}
