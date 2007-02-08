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

package org.netbeans.modules.i18n.form;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.properties.*;
import org.netbeans.modules.i18n.*;
import org.netbeans.modules.i18n.java.JavaResourceHolder;
import org.netbeans.modules.form.I18nService;
import org.netbeans.modules.form.I18nValue;

/**
 * Implementation of form module's I18nService - used by form editor to control
 * internationalization of forms while i18n module owns all the technical means
 * (i18n values, property editors, bundle files).
 */
public class I18nServiceImpl implements I18nService {

    // remembered original state for changes made for given source data objects
    // mapping source DO to a map of properties DO to ChangeInfo
    private Map/*<DataObject, Map<DataObject, ChangeInfo>>*/ changesMap = new HashMap();

    private static class ChangeInfo {
        Map/*<String, Object]>*/ changed = new HashMap(); // for each key holds all data across all locales
        Set/*<String>*/ added = new HashSet(); // holds keys that were not originally present
    }

    /**
     * Creates I18nValue object for given key and value. Should not be added
     * to the bundle file yet. (For that purpose 'update' method is called later.)
     */
    public I18nValue create(String key, String value, DataObject srcDataObject) {
        FormI18nString i18nString = new FormI18nString(srcDataObject);
        i18nString.setKey(key);
        i18nString.setValue(value);
        return i18nString;
    }

    /**
     * Creates a new I18nValue object with a new key. Should do no changes to
     * the bundle file at this moment.
     */
    public I18nValue changeKey(I18nValue prev, String newKey) {
        FormI18nString oldI18nString = (FormI18nString) prev;
        FormI18nString changedI18nString;
        if (oldI18nString.getKey() == I18nValue.COMPUTE_AUTO_KEY) {
            // set key which was unset so far
            changedI18nString = oldI18nString;
        }
        else { // create a new value for the new key
            changedI18nString = new FormI18nString(oldI18nString);
            changedI18nString.allData = oldI18nString.allData;
        }
        changedI18nString.setKey(newKey);
        return changedI18nString;
    }

    /**
     * Creates a new I18nValue object with changed value. Should not do any
     * changes to the bundle file.
     */
    public I18nValue changeValue(I18nValue prev, String value) {
        FormI18nString i18nString = new FormI18nString((FormI18nString)prev);
        i18nString.setValue(value);
        return i18nString;
    }

    /**
     * Creates a new I18nValue refering to given locale (both for reading and
     * writing from now).
     */
    public I18nValue switchLocale(I18nValue value, String localeSuffix) {
        if (value == null || value.getKey() == null)
            return value;

        FormI18nString i18nString = new FormI18nString((FormI18nString)value);
        JavaResourceHolder rh = (JavaResourceHolder) i18nString.getSupport().getResourceHolder();
        rh.setLocalization(localeSuffix);
        i18nString.setValue(rh.getValueForKey(i18nString.getKey()));
        i18nString.setComment(rh.getCommentForKey(i18nString.getKey()));
        return i18nString;
    }

    /**
     * Updates bundle file according to given I18nValue objects - oldValue is
     * removed, newValue added. Update goes into given locale - parent files
     * are updated too if given key is not present in them. New properties file
     * is created if needed.
     */
    public void update(I18nValue oldValue, I18nValue newValue,
                       DataObject srcDataObject, String bundleName, String localeSuffix,
                       boolean canRemove)
        throws IOException
    {
        FormI18nString oldI18nString = (FormI18nString) oldValue;
        FormI18nString newI18nString = (FormI18nString) newValue;

        if (oldI18nString != null) {
            ResourceHolder oldRH = oldI18nString.getSupport().getResourceHolder();
            DataObject oldRes = oldRH.getResource();
            DataObject newRes = null;
            if (newI18nString != null) {
                ResourceHolder newRH = newI18nString.getSupport().getResourceHolder();
                newRes = newRH.getResource();
                if (newRes == null) { // use same resource bundle as old value
                    newRH.setResource(oldRes);
                    newRes = oldRes;
                }
            }

            String oldKey = oldI18nString.getKey();
            if (canRemove && oldKey != null) {
                JavaResourceHolder jrh = (JavaResourceHolder) oldRH;
                Object allData = jrh.getAllData(oldKey);
                registerChange(srcDataObject, oldRes, oldKey, allData);

                if (newI18nString == null
                    || newI18nString.getKey() == null
                    || !newI18nString.getKey().equals(oldKey)
                    || newRes != oldRes)
                {   // removing i18n value, changing key, or moving to another properties file
                    oldI18nString.allData = allData;
                    jrh.removeProperty(oldKey);
                    if (newI18nString != null)
                        newI18nString.allData = oldI18nString.allData;
                }
                else if (localeSuffix != null && !localeSuffix.equals("")) { // NOI18N
                    // remember all locale data (to be able to undo adding new specific value to a locale)
                    oldI18nString.allData = allData;
                }

                if (newI18nString == null
                    && oldRes == getPropertiesDataObject(srcDataObject, bundleName))
                {   // forget the resource bundle file - may want different next time
                    oldRH.setResource(null);
                }
            }
        }

        if (newI18nString != null && newI18nString.getKey() != null
                && newI18nString.getKey() != I18nValue.NOI18N_KEY)
        {   // valid new value - make sure it is up-to-date in the properties file
            JavaResourceHolder rh = (JavaResourceHolder) newI18nString.getSupport().getResourceHolder();

            if (rh.getResource() == null) { // find or create properties file
                DataObject propertiesDO = getPropertiesDataObject(srcDataObject, bundleName);
                if (propertiesDO == null) { // create new properties file
                    propertiesDO = createPropertiesDataObject(srcDataObject, bundleName);
                    if (propertiesDO == null)
                        return;
                }
                rh.setResource(propertiesDO);

                // make sure we use free (unique) key
                newI18nString.setKey(rh.findFreeKey(newI18nString.getKey()));
            }

            rh.setLocalization(localeSuffix);
            String key = newI18nString.getKey();
            if (!isValueUpToDate(rh, newI18nString)) {
                if (newI18nString.allData != null) { // restore complete data across all locales
                    rh.setAllData(key, newI18nString.allData);
                    newI18nString.allData = null;
                    // update also the current value - might have come from a different locale
                    newI18nString.setValue(rh.getValueForKey(key));
                    newI18nString.setComment(rh.getCommentForKey(key));
                }
                else {
                    rh.addProperty(key, newI18nString.getValue(), newI18nString.getComment(), true);
                }
                registerChange(srcDataObject, rh.getResource(), newI18nString.getKey(), null);
            }
        }
    }

    private static boolean isValueUpToDate(ResourceHolder rh, I18nString i18nString) {
        String storedValue = rh.getValueForKey(i18nString.getKey());
        String storedComment = rh.getCommentForKey(i18nString.getKey());
        if ("".equals(storedComment)) // NOI18N
            storedComment = null;
        String newValue = i18nString.getValue();
        String newComment = i18nString.getComment();
        if ("".equals(newComment)) // NOI18N
            newComment = null;
        return (storedValue == newValue || (storedValue != null && storedValue.equals(newValue)))
            && (storedComment == newComment || (storedComment != null && storedComment.equals(newComment)));
    }

    /**
     * Returns property editor to be used for editing internationalized
     * property of given type (e.g. String). If an existing suitable editor is
     * passed then it is returned and no new property editor is created.
     */
    public PropertyEditor getPropertyEditor(Class type, PropertyEditor existing) {
        return existing instanceof FormI18nStringEditor ? existing : new FormI18nStringEditor();
    }

    /**
     * Evaluates the effect of changing a property editor. The property editor
     * determines whether a property can hold internationalized value.
     * @return -1 if an i18n editor is changed to plain type editor,
     *         0 if the type of editor does no change,
     *         1 if a plain type editor is changed to i18n one
     */
    public int analyzePropertyEditorChange(PropertyEditor oldPE, PropertyEditor newPE) {
        if (oldPE instanceof FormI18nStringEditor)
            return isPlainStringEditor(newPE) ? -1 : 0;
        if (isPlainStringEditor(oldPE))
            return newPE instanceof FormI18nStringEditor ? 1 : 0;
        return 0;
    }

    private static boolean isPlainStringEditor(PropertyEditor pe) {
        return pe != null && pe.getClass().getName().endsWith(".StringEditor"); // NOI18N
    }

    /**
     * Provides a component usable as property customizer (so typically a modal
     * dialog) that allows to choose (or create) a properties bundle file within
     * the project of given form data object. The selected file should be
     * written to the given property editor (via setValue) as a resource name
     * string.
     */
    public Component getBundleSelectionComponent(final PropertyEditor prEd, DataObject srcDataObject) {
        try {
            final FileSelector fs = new FileSelector(srcDataObject.getPrimaryFile(), JavaResourceHolder.getTemplate());
            return fs.getDialog(NbBundle.getMessage(I18nServiceImpl.class, "CTL_SELECT_BUNDLE_TITLE"), // NOI18N
                                new ActionListener()
            {
                public void actionPerformed(ActionEvent ev) {
                    DataObject bundleDO = fs.getSelectedDataObject();
                    if (bundleDO != null) {
                        ClassPath cp = ClassPath.getClassPath(bundleDO.getPrimaryFile(), ClassPath.SOURCE);
                        if (cp != null) {
                            String bundleName = cp.getResourceName(bundleDO.getPrimaryFile(), '/', false);
                            prEd.setValue(bundleName);
                        }
                    }
                }
            });
        }
        catch (IOException ex) {
            // means that template for properties file was not found - unlikely
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    /**
     * Returns all currently available locales for given bundle in two arrays
     * os strings. The first one containes locale suffixes, the second one
     * corresponding display names for the user (should be unique).
     */
    public String[][] getAvailableLocales(DataObject srcDataObject, String bundleName) {
        PropertiesDataObject dobj = null;
        try {
            dobj = getPropertiesDataObject(srcDataObject, bundleName);
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        if (dobj == null)
            return null;

        List list = new ArrayList();
        list.add(dobj.getPrimaryEntry());
        list.addAll(dobj.secondaryEntries());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                MultiDataObject.Entry e1 = (MultiDataObject.Entry) o1;
                MultiDataObject.Entry e2 = (MultiDataObject.Entry) o2;
                return e1.getFile().getName().compareTo(e2.getFile().getName());
            }
        });

        String[] locales = new String[list.size()];
        String[] displays = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            MultiDataObject.Entry entry = (MultiDataObject.Entry) list.get(i);
            locales[i] = org.netbeans.modules.properties.Util.getLocaleSuffix(entry);
            displays[i] = org.netbeans.modules.properties.Util.getLocaleLabel(entry);
        }
        return new String[][] { locales, displays };
    }

    /**
     * Provides a visual component (modal dialog) usable as a property
     * customizer that allows create a new locale file for given bundle (default
     * bundle name provided). The created locale should be written as a string
     * (locale suffix) to the given propery editor.
     */
    public Component getCreateLocaleComponent(final PropertyEditor prEd, DataObject srcDataObject, String bundleName) {
        final PropertiesDataObject propertiesDO;
        try {
            propertiesDO = getPropertiesDataObject(srcDataObject, bundleName);
        }
        catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
        final Dialog[] dialog = new Dialog[1];
        final LocalePanel localePanel = new LocalePanel();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            localePanel,
            NbBundle.getBundle(PropertiesDataObject.class).getString("CTL_NewLocaleTitle"), // NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        String locale = localePanel.getLocale().toString();
                        org.netbeans.modules.properties.Util.createLocaleFile(
                                propertiesDO, locale, false);
                        prEd.setValue("_" + locale); // NOI18N
                    }
                    dialog[0].setVisible(false);
                    dialog[0].dispose();
                }
            }
        );
        dialog[0] = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        return dialog[0];
    }

    /**
     * Saves properties files edited for given source object (form). This method
     * is called when a form is being saved - so the corresponding bundle is
     * saved as well.
     */
    public void autoSave(DataObject srcDataObject) {
        Map/*<DataObject, ChangeInfo>*/ relatedMap = (Map) changesMap.remove(srcDataObject);
        if (relatedMap != null) {
            for (Iterator it=relatedMap.keySet().iterator(); it.hasNext(); ) {
                DataObject propertiesDO = (DataObject) it.next();
                // [not sure: should we auto-save only bundles not opened in the editor?
                //  perhaps it's OK to save always...]
                SaveCookie save = (SaveCookie) propertiesDO.getCookie(SaveCookie.class);
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
    }

    /**
     * Called when a form is closed without saving changes. The changes in
     * corresponding properties file need to be discarded (reverted) as well.
     */
    public void close(DataObject srcDataObject) {
        Map/*<DataObject, ChangeInfo>*/ relatedMap = (Map) changesMap.remove(srcDataObject);
        if (relatedMap != null) {
            for (Iterator it=relatedMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry/*<DataObject, ChangeInfo>*/ e = (Map.Entry) it.next();
                PropertiesDataObject propertiesDO = (PropertiesDataObject) e.getKey();
                ChangeInfo changes = (ChangeInfo) e.getValue();
                JavaResourceHolder rh = new JavaResourceHolder();
                rh.setResource(propertiesDO);
                for (Iterator/*<Map.Entry<String, Object>>*/ it2=changes.changed.entrySet().iterator(); it2.hasNext(); ) {
                    Map.Entry/*<String, Object>*/ e2 = (Map.Entry) it2.next();
                    String key = (String) e2.getKey();
                    Object allData = e2.getValue();
                    rh.setAllData(key, allData);
                }
                for (Iterator it2=changes.added.iterator(); it2.hasNext(); ) {
                    String key = (String) it2.next();
                    rh.removeProperty(key);
                }
                // [not sure: should we save the bundle for consistency?
                //  perhaps not necessary...]
//                SaveCookie save = (SaveCookie) propertiesDO.getCookie(SaveCookie.class);
//                if (save != null) {
//                    try {
//                        save.save();
//                    }
//                    catch (IOException ex) {
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    }
//                }
            }
        }
    }

    /**
     * Checks project of given form whether it is suitable to be automatically
     * internationalized by default. Currently new forms in module projects
     * should be set to auto i18n, while standard user (J2SE) projects not.
     * [If we decide all projects should be internationalized, we can remove
     *  this method.]
     */
    public boolean isDefaultInternationalizableProject(DataObject srcDataObject) {
        return org.netbeans.modules.i18n.Util.isNbBundleAvailable(srcDataObject);
    }

    // -----

    private static PropertiesDataObject getPropertiesDataObject(DataObject srcDataObject, String bundleName)
        throws DataObjectNotFoundException
    {
        if (bundleName.startsWith("/")) // NOI18N
            bundleName = bundleName.substring(1);
        if (!bundleName.toLowerCase().endsWith(".properties")) // NOI18N
            bundleName = bundleName + ".properties"; // NOI18N
        FileObject bundleFile = org.netbeans.modules.i18n.Util
                .getResource(srcDataObject.getPrimaryFile(), bundleName);
        if (bundleFile != null) {
            DataObject dobj = DataObject.find(bundleFile);
            if (dobj instanceof PropertiesDataObject)
                return (PropertiesDataObject) dobj;
        }
        return null;
    }

    private static DataObject createPropertiesDataObject(DataObject srcDataObject,
                                                         String filePath)
        throws IOException
    {
        if (filePath == null)
            return null;

        FileObject folder;
        ClassPath cp = ClassPath.getClassPath(srcDataObject.getPrimaryFile(), ClassPath.SOURCE);
        FileObject root = cp.getRoots()[0];
        String fileName;
        int idx = filePath.lastIndexOf('/');
        boolean hasCustomRes = false;
        Project owner = FileOwnerQuery.getOwner(srcDataObject.getPrimaryFile());
        if (owner != null) {
            // this is for projects that have split sources/resources folder structures.
            Sources srcs = ProjectUtils.getSources(owner);
            SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            if (grps != null && grps.length > 0) {
                hasCustomRes = true;
                root = grps[0].getRootFolder();
            }
        }
        if (idx < 0) { // default package
            folder = root;
            fileName = filePath;
        }
        else {
            String folderPath = filePath.substring(0, idx);
            folder = cp.findResource(folderPath);
            if (folder == null || hasCustomRes) {
                folder = FileUtil.createFolder(root, folderPath);
            }
            fileName = filePath.substring(idx + 1);
        }
        if (folder != null) {
            DataObject template = JavaResourceHolder.getTemplate();
            return template.createFromTemplate(DataFolder.findFolder(folder), fileName);
            // [set auto-create attribute?]
        }
        return null; // [throw exception - can't create properties file?]
    }

    /**
     * Keeps original data from properties file when first change is done in
     * given properties file for given source file (form). If the source file
     * is discarded later, all relevant changes in the properties file are reverted.
     */
    private void registerChange(DataObject srcDO, DataObject propertiesDO, String key, Object allData) {
        Map/*<DataObject, ChangeInfo>*/ relatedMap = (Map) changesMap.get(srcDO);
        if (relatedMap == null) {
            relatedMap = new HashMap();
            changesMap.put(srcDO, relatedMap);
        }
        ChangeInfo changes = (ChangeInfo) relatedMap.get(propertiesDO);
        if (changes == null) {
            changes = new ChangeInfo();
            relatedMap.put(propertiesDO, changes);
        }
        if (!changes.changed.containsKey(key) && !changes.added.contains(key)) {
            // original state of this key not registered yet
            if (allData != null) // something changed in existing data
                changes.changed.put(key, allData); // allData contains original data
            else // new key added
                changes.added.add(key);
        }
    }
}
