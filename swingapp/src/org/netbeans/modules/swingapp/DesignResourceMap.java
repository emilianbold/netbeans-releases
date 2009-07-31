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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.swingapp;

import org.jdesktop.application.ResourceMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.Element;
import org.netbeans.modules.properties.Element.ItemElem;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;

/**
 * Reads and writes data according to the ResourceMap specification.
 * One DesignResourceMap instance manages properties files at one level of storage.
 * There are three levels as defined by the app framewrok: class, package,
 * application. For one source file (corresponding to a class at runtime) there
 * are three instances of chained DesignResourceMap objects. (It is enough to
 * just hold the class-level instance which knows its parent resource map for
 * package which knows its parent resource map for the whole application).
 * The implementation is based on BundleStructure, so it works with data of
 * properties file even if opened and edited as documents.
 * @see ResourceUtils.createDesignResourceMap
 * 
 * @author Tomas Pavek
 */
final class DesignResourceMap extends ResourceMap {

    static final int APP_LEVEL = 0;
    static final int CLASS_LEVEL = 2;

    private static final String NOI18N_COMMENT = "NOI18N"; // NOI18N

    private FileObject sourceFile;
    private BundleStructure[] bundles;
    private String locale;
    private String[] localeBundleNames; // short bundle names including the current locale
    private int storageLevel;

    private Map<String, ChangeInfo> changes = new HashMap<String, ChangeInfo>();

    private static class ChangeInfo {
        private String[] originalData; // if null, the key was just added
        private BundleStructure bundle;
    }

    private static final String EVALUATING_KEY = "#EVALUATING_KEY#"; // NOI18N
    private String evaluateStringValue;

    /**
     * Creates a new resource map.
     * @param parent the parent DesignResourceMap
     * @param classLoader the class loader to be used to load the resources
     * @param srcFile the source file for which the resource map is created
     * @param bundleNames names of resource bundles to be used on this level
     * @param level the level of resources this DesignResourceMap targets
     *         (one of CLASS_LEVEL, PACKAGE_LEVEL, APP_LEVEL)
     */
    DesignResourceMap(ResourceMap parent,
                      ClassLoader classLoader,
                      FileObject srcFile,
                      String[] bundleNames,
                      int level)
    {
        super(parent, classLoader, bundleNames);
        sourceFile = srcFile;
        bundles = new BundleStructure[bundleNames.length];
        locale = ""; // NOI18N
        localeBundleNames = new String[bundleNames.length];
        for (int i = 0; i < bundleNames.length; i++) {
            localeBundleNames[i] = getShortBundleName(bundleNames[i]);
        }
        storageLevel = level;
    }

    public DesignResourceMap getDesignParent() {
        ResourceMap parent = super.getParent();
        return parent instanceof DesignResourceMap ? (DesignResourceMap) parent : null;
    }

    /**
     * @return DesignResourceMap that corresponds to given level (should be
     *         called on the base instance of the chain)
     */
    DesignResourceMap getLevel(int level) {
        if (level == storageLevel) {
            return this;
        }
        else {
            DesignResourceMap parent = getDesignParent();
            if (parent != null)
                return parent.getLevel(level);
        }
        return null;
    }

    /**
     * @return the source file for which this DesignResourceMap is created
     */
    FileObject getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the locale in which DesignResourceMap (whole chain) should operate.
     * All reading/writing then targets the corresponding locale variants of
     * properties files.
     * @param locale localization suffix including the initial underscore (e.g. _cs_CZ)
     */
    void setLocalization(String locale) {
        if (this.locale.equals(locale))
            return;

        this.locale = locale;
        List<String> bundleNames = getBundleNames();
        for (int i = 0; i < bundleNames.size(); i++) {
            String shortName = getShortBundleName(bundleNames.get(i));
            localeBundleNames[i] = locale != null && !locale.equals("") ? // NOI18N
                shortName + locale : shortName;
        }
        DesignResourceMap parent = getDesignParent();
        if (parent != null)
            parent.setLocalization(locale);
    }

    private static String getShortBundleName(String bundleName) {
        int idx = bundleName.lastIndexOf('.');
        return idx < 0 ? bundleName : bundleName.substring(idx+1);
    }

    private BundleStructure[] getBundles() {
        List<String> bundleNames = null;
        for (int i=0; i < bundles.length; i++) {
            if (bundles[i] == null) {
                if (bundleNames == null)
                    bundleNames = getBundleNames();
                bundles[i] = ResourceUtils.getBundleStructure(sourceFile, bundleNames.get(i));
            }
        }
        return bundles;
    }

    /**
     * Returns a representative PropertiesDataObject for the resource map
     * (basically the first existing properties file in the chain of resource
     * maps). Used for creating new locale variants.
     * @return a representative DataObject for a properties file
     */
    PropertiesDataObject getRepresentativeDataObject() {
        for (String bundleName : getBundleNames()) {
            PropertiesDataObject dobj = ResourceUtils.getPropertiesDataObject(sourceFile, bundleName, false);
            if (dobj != null)
                return dobj;
        }
        DesignResourceMap parent = getDesignParent();
        return parent != null ? parent.getRepresentativeDataObject() : null;
    }

    /**
     * Finds a value for given key in the resource map chanin and creates a new
     * ResourceValueImpl object for it (which also contains the string value
     * as present in the properties file and the level on which it was found).
     * @param key the key
     * @param type the type of the expected value (required by ResourceMap to
     *        determine the right convertor)
     * @return ResourceValueImpl with all data for given key
     */
    ResourceValueImpl getResourceValue(String key, Class type) {
        BundleStructure[] bundles = getBundles();
        for (int i=0; i < bundles.length; i++) {
            BundleStructure b = bundles[i];
            if (b != null) {
                ItemElem item = b.getItem(localeBundleNames[i], key);
                if (item != null) {
                    Object value;
                    try {
                        value = getObject(key, type);
                    }
                    catch (ResourceMap.LookupException ex) {
                        // [TODO: handle invalid resource values somehow
                        //  (special value object? special flag on ResourceValue?)]
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        value = null;
                    }
                    String stringValue = item.getValue();
                    String resName = getResourceName(type, stringValue);
                    return new ResourceValueImpl(key, type,
                            value, resName, stringValue,
                            !isNoI18nComment(item.getComment()),
                            storageLevel,
                            sourceFile);
                }
            }
        }
        DesignResourceMap parent = getDesignParent();
        return parent != null ? parent.getResourceValue(key, type) : null;
    }

    private static boolean isNoI18nComment(String comment) {
        if (comment != null && comment.length() > 0) {
            comment = comment.trim().toUpperCase();
            return comment.contains(NOI18N_COMMENT);
        }
        return false;
    }

    private String getResourceName(Class valueType, String stringValue) {
        if (javax.swing.Icon.class.isAssignableFrom(valueType)) {
            // hack: icons are defined via resource names [anything else?]
            return getResourcesDir() + stringValue;
            // [some check it is really a valid resource name?]
        }
        else return null;
    }

    /**
     * Computes the object that would be returned by ResourceMap when given
     * string value was present in the properties file. I.e. it lets the
     * ResourceMap to interpret the possible expressions (like links to other
     * keys) and convert from string to the required type.
     * @param stringValue the string to evaluate
     * @param type the type of the expected value (required by ResourceMap to
     *        determine the right convertor)
     * @return the value corresponding to the string
     * @exception will throw ResourceMap.LookupException (RuntimeException) if
     *            the value can't be interpreted (string can't be converted
     *            to an object of given type)
     */
    Object evaluateStringValue(String stringValue, Class type) {
        evaluateStringValue = stringValue;
        Object value = getObject(EVALUATING_KEY, type);
        evaluateStringValue = null;
        return value;
    }

    /**
     * Collects all keys that matches given regular expression (String.matches
     * method is used). Based on the wholeChain parameter it either scans the
     * whole chain of resource maps, or just one level.
     * @param regex a regular expression the collected keys should match
     * @param wholeChain if true, this resource maps and all parents are scanned;
     *        if false, only this resource map is scanned
     * @return collection of matching keys
     */
    Collection<String> collectKeys(String regex, boolean wholeChain) {
        Collection<String> col = collectKeys(regex, null, wholeChain);
        if (col == null)
            col = Collections.emptyList();
        return col;
    }

    private Collection<String> collectKeys(String regex, Collection<String> col, boolean wholeChain) {
        BundleStructure[] bundles = getBundles();
        for (int i=0; i < bundles.length; i++) {
            BundleStructure b = bundles[i];
            if (b != null) {
                for (String key : b.getKeys()) {
                    if (key.matches(regex)) {
                        if (col == null)
                            col = new LinkedList<String>();
                        col.add(key);
                    }
                }
            }
        }
        if (wholeChain) {
            DesignResourceMap parent = getDesignParent();
            if (parent != null)
                col = parent.collectKeys(regex, col, wholeChain);
        }
        return col;
         // TBD perhaps should also collect the keys from the framework's default app resource map
    }

    /**
     * Collects all locale variants (existing properties files) in the scope
     * of this resource map and its parents (whole chain).
     * @return collection of data object entries representing the properties files
     */
    Collection<MultiDataObject.Entry> collectLocaleEntries() {
        Collection<MultiDataObject.Entry> col = collectLocaleEntries(null);
        if (col == null)
            col = Collections.emptyList();
        return col;
    }

    private Collection<MultiDataObject.Entry> collectLocaleEntries(Collection<MultiDataObject.Entry> col) {
        for (String bundleName : getBundleNames()) {
            MultiDataObject dobj = ResourceUtils.getPropertiesDataObject(sourceFile, bundleName, false);
            if (dobj != null) {
                if (col == null)
                    col = new LinkedList<MultiDataObject.Entry>();
                col.add(dobj.getPrimaryEntry());
                col.addAll(dobj.secondaryEntries());
                try {
                    String baseName = dobj.getName() + "_"; // NOI18N
                    for (FileObject fo : dobj.getPrimaryFile().getParent().getChildren()) {
                        String fileName = fo.getNameExt();
                        if (fileName.endsWith(".properties") && fileName.startsWith(baseName)) { // NOI18N
                            DataObject dobj2 = DataObject.find(fo);
                            if (dobj2 instanceof PropertiesDataObject) {
                                col.add(((MultiDataObject)dobj2).getPrimaryEntry());
                            }
                        }
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        DesignResourceMap parent = getDesignParent();
        return parent != null ? parent.collectLocaleEntries(col) : col;
    }

    /**
     * Removes a value from the resource map. It is removed from the level
     * specified in the ResourceValueImpl object.
     * @param resValue ResourceValueImpl object to remove
     */
    void removeResourceValue(ResourceValueImpl resValue) {
        if (resValue.getStorageLevel() != storageLevel) {
            DesignResourceMap parent = getDesignParent();
            if (parent != null)
                parent.removeResourceValue(resValue);
        }
        else {
            String key = resValue.getKey();
            for (BundleStructure b : bundles) {
                if (b != null) {
                    String[] data = b.getAllData(key);
                    if (data != null) { // this bundle contains the key
                        b.removeItem(key);
                        recordChange(key, data, b);
                        resValue.setAllData(data);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds a new value to the resource map as specified by the
     * ResourceValueImpl object. If a specific locale is set on the resource map
     * and the value is locale-sensitive, then the value is stored to the
     * corresponding locale variant of the properties file (plus in the default
     * properties file). Otherwise the value goes into the default properties
     * file only.
     * @param resValue ResourceValueImpl object to add
     */
    void addResourceValue(ResourceValueImpl resValue) {
        if (resValue.getStorageLevel() != storageLevel) {
            DesignResourceMap parent = getDesignParent();
            if (parent != null)
                parent.addResourceValue(resValue);
        }
        else {
            String key = resValue.getKey();
            int bundleIndex = 0;
            BundleStructure bundle = null;
            String[] currentData = null;
            for (BundleStructure b : getBundles()) {
                if (b != null) {
                    currentData = b.getAllData(key);
                    if (currentData != null) {
                        bundle = b;
                        break;
                    }
                }
                bundleIndex++;
            }

            if (bundle == null) {
                bundleIndex = 0;
                bundle = bundles[0];
            }
            String bundleName = getBundleNames().get(bundleIndex);
            String locName = resValue.isInternationalized() ? // take care about locale variant?
                localeBundleNames[bundleIndex] : getShortBundleName(bundleName);
            PropertiesDataObject dobj = null;
            if (bundle == null) {
                try {
                    dobj = ResourceUtils.createPropertiesDataObject(getSourceFile(), bundleName);
                    bundle = dobj.getBundleStructure();
                }
                catch (IOException ex) { // [can't create properties file, so now what...]
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    return;
                }
            }
            if (!bundleName.endsWith(locName)) { // make sure the locale file exists
                String locale = locName.substring(getShortBundleName(bundleName).length()+1);
                org.netbeans.modules.properties.Util.createLocaleFile(dobj, locale, false);
            }

            // check if the provided string value is valid - if not try to
            // determine it from the value itself
            String stringValue = resValue.getStringValue();
            Class valueType = resValue.getValueType();
            Object value;
            try {
                value = evaluateStringValue(stringValue, valueType);
            }
            catch (ResourceMap.LookupException ex) { // don't understand it
                value = resValue.getValue();
                String strValue = ResourceUtils.getValueAsString(value);
                if (strValue != null) {
                    resValue.setStringValue(strValue);
                    stringValue = strValue;
                }
                // otherwise keep the invalid string value...
            }

            bundle.addItem(locName, key, stringValue,
                           resValue.isInternationalized() ? "" : NOI18N_COMMENT, // NOI18N
                           true);
            recordChange(key, currentData, bundle);
            if (currentData == null) { // was not present, might want to restore earlier data
                String[] data = resValue.getAllData();
                if (data != null) {
                    bundle.setAllData(key, data);
                    resValue.setAllData(null);
                }
            }

            resValue.setValue(value); //getObject(key, resValue.getValueType()));
            if (resValue.getClassPathResourceName() == null)
                resValue.setClassPathResourceName(getResourceName(valueType, stringValue));
        }
    }

    private void recordChange(String key, String[] originalData, BundleStructure bundle) {
        if (!changes.containsKey(key)) {
            ChangeInfo ch = new ChangeInfo();
            ch.originalData = originalData;
            ch.bundle = bundle;
            changes.put(key, ch);
        }
    }

    /**
     * Saves all edited properties file of the resource map and its parents
     * (whole chain). This is called when the source file for which this
     * resource map is dedicated is saved.
     */
    void save() {
        save(true);
    }

    private void save(boolean wholeChain) {
        if (!changes.isEmpty()) {
            for (String bundleName : getBundleNames()) {
                DataObject dobj = ResourceUtils.getPropertiesDataObject(sourceFile, bundleName, false);
                if (dobj != null) {
                    SaveCookie save = dobj.getCookie(SaveCookie.class);
                    if (save != null) {
                        try {
                            save.save();
                        }
                        catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                }
            }
            changes.clear();
        }
        if (wholeChain) {
            DesignResourceMap parent = getDesignParent();
            if (parent != null)
                parent.save(wholeChain);
        }
    }

    /**
     * Reverts all changes in the properties files of this resource maps.
     * This is called when the source file this resource maps belong to is
     * closed without saving. After the changes are reverted, the properties
     * files are saved (so there are no unsaved files left around).
     */
    void revertChanges() {
        for (Map.Entry<String, ChangeInfo> entry : changes.entrySet()) {
            String key = entry.getKey();
            BundleStructure bundle = entry.getValue().bundle;
            String[] data = entry.getValue().originalData;
            if (data != null) {
                bundle.setAllData(key, data);
            }
            else {
                bundle.removeItem(key);
            }
        }
//        changes.clear();

        DesignResourceMap parent = getDesignParent();
        if (parent != null)
            parent.revertChanges();

        save(false);
    }

    // -----
    // ResourceMap methods - implementing our own way of obtaining string values
    // for keys (using BundleStructure)

    @Override
    protected boolean containsResourceKey(String key) {
        if (key == EVALUATING_KEY && evaluateStringValue != null)
            return true;

        getBundles();
        for (int i=0; i < bundles.length; i++) {
            if (bundles[i] != null && bundles[i].getItem(localeBundleNames[i], key) != null)
                return true;
        }
        return false;
    }

    @Override
    protected Object getResource(String key) {
        if (key == EVALUATING_KEY)
            return evaluateStringValue;

        getBundles();
        for (int i=0; i < bundles.length; i++) {
            if (bundles[i] != null) {
                Element.ItemElem item = bundles[i].getItem(localeBundleNames[i], key);
                if (item != null)
                    return item.getValue();
            }
        }
        return null;
    }

    @Override
    protected void putResource(String key, Object value) {
        // this is noop - we don't want to cache computed resource values
    }
}
