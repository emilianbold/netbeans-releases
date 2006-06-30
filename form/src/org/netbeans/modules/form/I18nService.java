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

package org.netbeans.modules.form;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.io.IOException;
import org.openide.loaders.DataObject;

/**
 * Interface of an internationalization service - form editor needs it to
 * perform automatic internationalization of forms. It is designed with respect
 * to the existing i18n architecture - i.e. to keep FormI18nStringEditor working
 * with the values created by the form editor.
 */
public interface I18nService {

    /**
     * Creates I18nValue object for given key and value. Should not be added
     * to the bundle file yet. (For that purpose 'update' method is called later.)
     */
    I18nValue create(String key, String value, DataObject srcDataObject);

    /**
     * Creates a new I18nValue object with a new key. Should do no changes to
     * the bundle file at this moment.
     */
    I18nValue changeKey(I18nValue prev, String newKey);

    /**
     * Creates a new I18nValue object with changed value. Should not do any
     * changes to the bundle file.
     */
    I18nValue changeValue(I18nValue prev, String value);

    /**
     * Creates a new I18nValue refering to given locale (both for reading and
     * writing from now).
     */
    I18nValue switchLocale(I18nValue value, String localeSuffix);

    /**
     * Updates bundle file according to given I18nValue objects - oldValue is
     * removed, newValue added. Update goes into given locale - parent files
     * are updated too if given key is not present in them. New properties file
     * is created if needed.
     */
    void update(I18nValue oldValue, I18nValue newValue,
                DataObject srcDataObject, String bundleName, String localeSuffix,
                boolean canRemove)
        throws IOException;

    /**
     * Returns property editor to be used for editing internationalized
     * property of given type (e.g. String). If an existing suitable editor is
     * passed then it is returned and no new property editor is created.
     */
    PropertyEditor getPropertyEditor(Class type, PropertyEditor existing);

    /**
     * Evaluates the effect of changing a property editor. The property editor
     * determines whether a property can hold internationalized value.
     * @return -1 if an i18n editor is changed to plain type editor,
     *         0 if the type of editor does no change,
     *         1 if a plain type editor is changed to i18n one
     */
    int analyzePropertyEditorChange(PropertyEditor oldPE, PropertyEditor newPE);

    /**
     * Provides a component usable as property customizer (so typically a modal
     * dialog) that allows to choose (or create) a properties bundle file within
     * the project of given form data object. The selected file should be
     * written to the given property editor (via setValue) as a resource name
     * string.
     */
    Component getBundleSelectionComponent(PropertyEditor pe, DataObject srcDataObject);

    /**
     * Returns all currently available locales for given bundle in two arrays
     * os strings. The first one containes locale suffixes, the second one
     * corresponding display names for the user (should be unique).
     */
    String[][] getAvailableLocales(DataObject srcDataObject, String bundleName);

    /**
     * Provides a visual component (modal dialog) usable as a property
     * customizer that allows create a new locale file for given bundle (default
     * bundle name provided). The created locale should be written as a string
     * (locale suffix) to the given propery editor.
     */
    Component getCreateLocaleComponent(PropertyEditor pe, DataObject srcDataObject, String bundleName);

    /**
     * Saves properties files edited for given source object (form). This method
     * is called when a form is being saved - so the corresponding bundle is
     * saved as well.
     */
    void autoSave(DataObject srcDataObject);

    /**
     * Called when a form is closed without saving changes. The changes in
     * corresponding properties file can be discarded as well.
     */
    void close(DataObject srcDataObject);

    /**
     * Checks project of given form whether it is suitable to be automatically
     * internationalized by default. Currently new forms in module projects
     * should be set to auto i18n, while standard user (J2SE) projects not.
     * [If we decide all projects should be internationalized, we can remove
     *  this method.]
     */
    boolean isDefaultInternationalizableProject(DataObject srcDataObject);
}
