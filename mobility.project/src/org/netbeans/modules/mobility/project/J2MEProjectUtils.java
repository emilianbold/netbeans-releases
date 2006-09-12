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

/*
 * Created on Mar 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.mobility.project;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * @author gc149856
 *
 * Helper class for j2me projects
 */
public class J2MEProjectUtils {
    
    private J2MEProjectUtils()
    {
        //All methods are static no need to initialize
    }
    
    public static Project getActiveProject(final TopComponent tc) {
        Node[] nodes;
        if (tc==null)
            nodes = TopComponent.getRegistry().getActivatedNodes();
        else
            nodes = tc.getActivatedNodes();
        
        Project p = null;
        if (nodes != null) {
            for (int i=0;i<nodes.length;i++) {
                final DataObject dobj = (DataObject) nodes[i].getCookie(DataObject.class);
                if (dobj != null) {
                    final Project np = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                    if (p != null && np != null && !p.equals(np)) return null; // two different projects found
                    p = np;
                }
            }
        }
        return p;
    }
    
    public static Project getActiveProject() {
        return getActiveProject(null);
    }
    
    public static Project getProjectForDocument(final Document doc) {
        
        final FileObject fo=NbEditorUtilities.getFileObject(doc);
        if (fo != null)
            return FileOwnerQuery.getOwner(fo);
        return null;
    }
    
    public static ProjectConfigurationProvider getConfigProviderForDoc(final Document doc) {
        final Project p = getProjectForDocument(doc);
        if (p != null) {
            return p.getLookup().lookup(ProjectConfigurationProvider.class);
        }
        return null;
        
    }
    
    public static ProjectConfigurationsHelper getCfgHelperForDoc(final Document doc) {
        final Project p = J2MEProjectUtils.getProjectForDocument(doc);
        if (p != null)
            return p.getLookup().lookup(ProjectConfigurationsHelper.class);
        return null;
    }
    
    
    public static String evaluateProperty(final AntProjectHelper helper, final String propertyName, final String configuration) {
        final PropertyEvaluator ev = helper.getStandardPropertyEvaluator();
        if (configuration != null && configuration.length() > 0) {
            final String val = ev.getProperty(J2MEProjectProperties.CONFIG_PREFIX + configuration + "." + propertyName);//NOI18N
            if (val != null) return val;
        }
        return ev.getProperty(propertyName);
    }
    
    public static String evaluateProperty(final AntProjectHelper helper, final String propertyName) {
        final PropertyEvaluator ev = helper.getStandardPropertyEvaluator();
        final String config = ev.getProperty("config.active");//NOI18N
        return evaluateProperty(helper, propertyName, config);
    }
    
    public static final Set<String> ILEGAL_CONFIGURATION_NAMES = new HashSet<String>(Arrays.asList(new String[] {"preprocessed", "compiled", "obfuscated", "preverified"})); //NOI18N
    
    public static URL deJar(final URL url) {
        if (url == null) return null;
        final URL u = FileUtil.getArchiveFile(url);
        return u == null ? url : u;
    }
    
    public static URL wrapJar(final URL url) {
        if (url == null) return null;
        final String name = url.toExternalForm().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".zip")) return FileUtil.getArchiveRoot(url);  //NOI18N
        if (!name.endsWith("/")) try {
            return new URL(url.toExternalForm() + "/"); //NOI18N
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return url;
    }
    
    public static boolean isParentOf(final URL root, final URL file) {
        if (root == null || file == null) return false;
        return deJar(file).toExternalForm().startsWith(deJar(root).toExternalForm());
        
    }
    
    public static String detectConfiguration(final URL projectRoot, final URL url) {
        if (projectRoot == null || url == null) return null;
        final String r = deJar(projectRoot).toExternalForm();
        String u = deJar(url).toExternalForm();
        if (!u.startsWith(r)) return null;
        u = u.substring(r.length());
        if (u.startsWith("/")) u = u.substring(1);//NOI18N
        final int i = u.indexOf('/');
        final int j = u.indexOf('/', i+1);
        if (i<0 || j<0) return null;
        final String cfg = u.substring(i+1, j);
        return (u.startsWith("build") || u.startsWith("dist")) && !ILEGAL_CONFIGURATION_NAMES.contains(cfg) ? cfg : null;//NOI18N
    }
}
