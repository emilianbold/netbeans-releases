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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.insync;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigLoader;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.project.WebProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;

/**
 *
 * @author jdeva
 */
public class JSFConfigUtils {
    private static JSFConfigLoader jsfConfigLoader = new JSFConfigLoader();
    public static void setUp(Project project) throws IOException {
        assert(project instanceof WebProject);
        WebModule webModule = ((WebProject) project).getAPIWebModule();
        assert(webModule != null);
        FileObject[] facesConfigFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        assert(facesConfigFiles.length == 1);

        FileObject facesConfigFile = facesConfigFiles[0];
        assert(facesConfigFile != null);

        DataLoader dl = DataLoaderPool.getPreferredLoader(facesConfigFile);

        if (dl == null || !(dl instanceof JSFConfigLoader)) {
            if (jsfConfigLoader != null) {
                DataLoaderPool.setPreferredLoader(facesConfigFile, jsfConfigLoader);
            }
        }

        DataObject dataObj = DataObject.find(facesConfigFile);
        assert(dataObj != null);
        assert(dataObj instanceof JSFConfigDataObject);
        registerXMLKit();
    }
    
    private static void registerXMLKit() {
        String[] path = new String[] { "Editors", "text", "x-jsf+xml" };
        FileObject target = FileUtil.getConfigRoot();
        try {
            for (int i=0; i<path.length; i++) {
                FileObject f = target.getFileObject(path[i]);
                if (f == null) {
                    f = target.createFolder(path[i]);
                }
                target = f;
            }
            String name = "EditorKit.instance";
            if (target.getFileObject(name) == null) {
                FileObject f = target.createData(name);
                f.setAttribute("instanceClass", "org.netbeans.modules.xml.text.syntax.XMLKit");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }        
}
