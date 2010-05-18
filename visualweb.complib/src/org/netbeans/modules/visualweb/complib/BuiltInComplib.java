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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.visualweb.complib.api.ComplibException;
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
        ComplibManifest compLibManifest = ComplibManifest.getInstance(configUrl, getClassLoader());
        initCompLibManifest(compLibManifest);

        initPaths();
    }

    protected List<File> convertConfigPathToFileList(List<String> path) throws ComplibException {
        ArrayList<File> retVal = new ArrayList<File>(path.size());
        for (String pathElm : path) {
            File file = InstalledFileLocator.getDefault().locate(pathElm, null, false);
            retVal.add(file);
        }
        return retVal;
    }

    public ClassLoader getClassLoader() {
        // Use the NB toolbox module ClassLoader
        ClassLoader nbModuleClassLoader = BuiltInComplib.class.getClassLoader();
        return nbModuleClassLoader;
    }

    BeanInfo getBeanInfo(String className) throws ClassNotFoundException, IntrospectionException {
        Class beanClass = Class.forName(className, true, getClassLoader());
        return Introspector.getBeanInfo(beanClass);
    }
}
