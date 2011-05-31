/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el;

import com.sun.el.parser.AstBracketSuffix;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Helper class for dealing with (JSF) resource bundles.
 *
 * TODO: should define an SPI and have the JSF module (and others) implement it.
 * Not urgent ATM as there would be just one impl anyway.
 *
 *
 * @author Erno Mononen
 */
public final class ResourceBundles {

    private static final Logger LOGGER = Logger.getLogger(ResourceBundles.class.getName());
    /**
     * Caches the bundles to avoid reading them again. Holds the bundles for
     * one FileObject at time.
     */
    private static final Map<FileObject, ResourceBundles> CACHE = new WeakHashMap<FileObject, ResourceBundles>(1);

    private final WebModule webModule;
    private final Project project;
    private List<String> bundles;
    private Map<String, java.util.ResourceBundle> bundlesMap;

    private ResourceBundles(WebModule webModule, Project project) {
        this.webModule = webModule;
        this.project = project;
    }

    public static ResourceBundles get(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject);
        if (CACHE.containsKey(fileObject)) {
            return CACHE.get(fileObject);
        } else {
            CACHE.clear();
            Project owner = FileOwnerQuery.getOwner(fileObject);
            WebModule webModule = WebModule.getWebModule(fileObject);
            ResourceBundles result = new ResourceBundles(webModule, owner);
            CACHE.put(fileObject, result);
            return result;
        }
    }

    public boolean canHaveBundles() {
        return webModule != null && project != null;
    }

    /**
     * Checks whether the given {@code identifier} represents 
     * a base name of a resource bundle.
     * @param identifier
     * @return
     */
    public boolean isResourceBundleIdentifier(String identifier) {
        for (String bundle : getBundles()) {
            if (bundle.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given {@code key} is defined in the given {@code bundle}.
     * @param bundle the base name of the bundle.
     * @param key the key to check.
     * @return {@code true} if the given {@code bundle} exists and contains the given 
     * {@code key}; {@code false} otherwise.
     */
    public boolean isValidKey(String bundle, String key) {
        java.util.ResourceBundle bundleFile = getBundlesMap().get(bundle);
        if (bundleFile == null) {
            // no matching bundle file
            return true;
        }
        return bundleFile.containsKey(key);
    }

    /**
     * Collects references to resource bundle keys in the given {@code root}.
     * @return List of identifier/string pairs. Identifier = resource bundle base name - string = res bundle key.
     */
    public List<Pair<AstIdentifier, AstString>> collectKeys(final Node root) {
        final List<Pair<AstIdentifier, AstString>> result = new ArrayList<Pair<AstIdentifier, AstString>>();
        List<Node> path = new AstPath(root).rootToLeaf();
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            if (node instanceof AstIdentifier && isResourceBundleIdentifier(node.getImage())) {
                // check for i18n["my.key"] => AST for that is: identifier, brackets and string
                if (i + 2 < path.size()) {
                    Node brackets = path.get(i + 1);
                    Node string = path.get(i + 2);
                    if (brackets instanceof AstBracketSuffix
                            && string instanceof AstString) {
                        result.add(Pair.of((AstIdentifier) node, (AstString) string));
                    }
                }
            }
        }
        return result;
    }

    public String findResourceBundleIdentifier(AstPath astPath) {
        List<Node> path = astPath.leafToRoot();
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            if (node instanceof AstString) {
                // check for i18n["my.key"] => AST for that is: identifier, brackets and string - since 
                // we're searching from the leaf to root here, so the order 
                // is string, brackets and identifier
                if (i + 2 < path.size()) {
                    Node brackets = path.get(i + 1);
                    Node identifier = path.get(i + 2);
                    if (brackets instanceof AstBracketSuffix
                            && identifier instanceof AstIdentifier
                            && isResourceBundleIdentifier(identifier.getImage())) {
                        return identifier.getImage();
                    }
                }
            }
        }
        return null;
    }
    /**
     * Gets the value of the given {@code key} in the given {@code bundle}.
     * @param bundle the base name of the bundle.
     * @param key key in the given bundle.
     * @return the value or {@code null}.
     */
    public String getValue(String bundle, String key) {
        java.util.ResourceBundle bundleFile = getBundlesMap().get(bundle);
        if (bundleFile == null || !bundleFile.containsKey(key)) {
            // no matching bundle file
            return null;
        }
        try {
            return bundleFile.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /**
     * Gets the entries in the bundle identified by {@code bundleName}.
     * @param bundleName
     * @return
     */
    public Map<String,String> getEntries(String bundleName) {
        java.util.ResourceBundle resourceBundle = getBundlesMap().get(bundleName);
        if (resourceBundle == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<String, String>();
        for (String key : resourceBundle.keySet()) {
            String value = resourceBundle.getString(key);
            result.put(key, value);
        }
        return result;
    }
    
    /**
     * Gets the base names of the defined bundles (such as {@code i18n}.
     * @return a list of the base names; never {@code null}.
     */
    public List<String> getBundles() {
        if (bundles == null) {
            bundles = initJSFResourceBundles();
        }
        return bundles;
    }

    /**
     * Finds list of all ResourceBundles, which are registered in all
     * JSF configuration files in a web module.
     * @param webModule
     * @return
     */
    private List<String> initJSFResourceBundles() {
        return webModule != null ? ELPlugin.Query.getResourceBundles(webModule.getDocumentBase()) : Collections.<String>emptyList();
    }
     
    private Map<String, java.util.ResourceBundle> getBundlesMap() {
        if (bundlesMap == null) {
            bundlesMap = initResourceBundleMap();
        }
        return bundlesMap;
    }
    
    private Map<String, java.util.ResourceBundle> initResourceBundleMap() {

        Map<String, java.util.ResourceBundle> result = new HashMap<String, java.util.ResourceBundle>();
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }

        Sources sources = project.getLookup().lookup(Sources.class);
        if (sources == null) {
            return null;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (String bundleFile : getBundles()) {
            for (SourceGroup sourceGroup : sourceGroups) {
                FileObject rootFolder = sourceGroup.getRootFolder();

                for (String classPathType : new String[]{ClassPath.SOURCE, ClassPath.COMPILE}) {
                    ClassPath classPath = ClassPath.getClassPath(rootFolder, classPathType);
                    if (classPath == null) {
                        continue;
                    }
                    ClassLoader classLoader = classPath.getClassLoader(false);
                    try {
                        java.util.ResourceBundle found = java.util.ResourceBundle.getBundle(bundleFile, Locale.getDefault(), classLoader);
                        result.put(bundleFile, found);
                        break; // found the bundle in source cp, skip searching compile cp
                    } catch (MissingResourceException exception) {
                        continue;
                    }
                }

            }
        }
        return result;
    }
}
