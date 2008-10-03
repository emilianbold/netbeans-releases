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


package org.netbeans.modules.properties;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FileEntry;

import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;


/**
 * Miscellaneous utilities for properties(reosurce bundles) module.
 * @author Petr Jiricka
 * @author Marian Petras
 */
public final class Util extends Object {
    
    /** Help ID for properties module in general. */
    public static final String HELP_ID_PROPERTIES = "propfiles.prop";   //NOI18N
    /** Help ID for properties new from template. */
    public static final String HELP_ID_CREATING = "propfiles.creating"; //NOI18N
    /** Help ID for new property dialog. */
    public static final String HELP_ID_ADDING = "propfiles.adding";     //NOI18N
    /** Help ID for table view of properties. */
    public static final String HELP_ID_MODIFYING
                               = "propfiles.modifying";                 //NOI18N
    /** Help ID for new locale dialog. */
    public static final String HELP_ID_ADDLOCALE
                               = "propfiles.addlocale";                 //NOI18N
    /** Help ID for source editor of .properties file. */
    public static final String HELP_ID_EDITLOCALE
                               = "propfiles.editlocale";                //NOI18N

    /** Character used to separate parts of bundle properties file name */
    public static final char PRB_SEPARATOR_CHAR
                             = PropertiesDataLoader.PRB_SEPARATOR_CHAR;
    /** Default length for the first part of node label */
    public static final int LABEL_FIRST_PART_LENGTH = 10;

    /** Converts a string to a string suitable for a resource bundle key */
    public static String stringToKey(String source) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < source.length(); i++) {
            char x = source.charAt(i);
            switch (x) {
            case '=':
            case ':':
            case '\t':
            case '\r':
            case '\n':
            case '\f':
            case ' ':
                result.append('_'); break;
            default:
                result.append(x);
            }
        }
        return result.toString();
    }

    /**
     * Assembles a file name for a properties file from its base name and
     * language.
     *
     * @return assembled name
     */
    public static String assembleName (String baseName, String lang) {
        if (lang.length() == 0) {
            return baseName;
        } else {
            if (lang.charAt(0) != PRB_SEPARATOR_CHAR) {
                StringBuffer res = new StringBuffer().append(baseName)
                                                     .append(PRB_SEPARATOR_CHAR)
                                                     .append(lang);
                return res.toString();
            } else {
                return baseName + lang;
            }
        }
    }
    
    /**
     * Returns a locale specification suffix of a given
     * <code>MultiDataObject</code> entry.
     * <p>
     * Examples:<br />
     * <pre>    </pre>Bundle.properties       -&gt; &quot;&quot;<br />
     * <pre>    </pre>Bundle_en_CA.properties -&gt; &quot;_en_CA&quot;
     * 
     * @param  fe  <code>DataObject</code> entry, representing a single bundle
     * @return  locale specification suffix of a given entry;
     *          or an empty string if the given entry has no locale suffix
     * @see  #getLanguage
     * @see  #getCountry
     * @see  #getVariant
     */
    public static String getLocaleSuffix(MultiDataObject.Entry fe) {
        MultiDataObject.Entry pe = fe.getDataObject().getPrimaryEntry();
        if (fe == pe) {
            return "";                                                  //NOI18N
        }
        String myName   = fe.getFile().getName();
        String baseName = pe.getFile().getName();
        assert myName.startsWith(baseName);
        return myName.substring(baseName.length());
    }

    /**
     * Returns a language specification abbreviation of a given
     * <code>MultiDataObject</code> entry.
     * For example, if the specified entry represents file
     * <tt>Bundle_en_UK.properties</tt>, this method returns
     * <code>&quot;en&quot;</code>.
     *
     * @return  <ul>
     *              <li>the language specification part of the locale specification,
     *                  if present</li>
     *              <li>an empty string (<code>&quot;&quot;</code>)
     *                  if the locale specification does not contain
     *                  language specification</li>
     *              <li><code>null</code> if the file (entry) has no locale
     *                  specification</li>
     *          </ul>
     * @see  #getCountry
     * @see  #getVariant
     */
    public static String getLanguage(final String localeSuffix) {
        return getFirstPart(localeSuffix);
    }

    /**
     * Returns a country specification abbreviation of a given
     * <code>MultiDataObject</code> entry.
     * For example, if the specified entry represents file
     * <tt>Bundle_en_UK.properties</tt>, this method returns
     * <code>&quot;UK&quot;</code>.
     *
     * @return  <ul>
     *              <li>the country specification part of the locale
     *                  specification, if present</li>
     *              <li>an empty string (<code>&quot;&quot;</code>)
     *                  if the locale specification does not contain
     *                  country specification</li>
     *              <li><code>null</code> if the file (entry) has no locale
     *                  specification</li>
     *          </ul>
     * @see  #getLanguage
     * @see  #getVariant
     */
    public static String getCountry(final String localeSuffix) {
        if (localeSuffix.length() == 0) {
            return null;
        }
        int start = localeSuffix.indexOf(PRB_SEPARATOR_CHAR, 1);
        return (start != -1)
               ? getFirstPart(localeSuffix.substring(start))
               : "";                                                    //NOI18N
    }

    /**
     * Returns a variant specification abbreviation of a given
     * <code>MultiDataObject</code> entry.
     * For example, if the specified entry represents file
     * <tt>Bundle_en_US_POSIX.properties</tt>, this method returns
     * <code>&quot;POSIX&quot;</code>.
     *
     * @return  <ul>
     *              <li>the variant specification part of the locale
     *                  specification, if present</li>
     *              <li>an empty string (<code>&quot;&quot;</code>)
     *                  if the locale specification does not contain
     *                  variant specification</li>
     *              <li><code>null</code> if the file (entry) has no locale
     *                  specification</li>
     *          </ul>
     * @see  #getLanguage
     * @see  #getCountry
     */
    public static String getVariant(final String localeSuffix) {
        if (localeSuffix.length() == 0) {
            return null;
        }
        int start = localeSuffix.indexOf(PRB_SEPARATOR_CHAR, 1);
        if (start == -1) {
            return "";                                                  //NOI18N
        }
        start = localeSuffix.indexOf(PRB_SEPARATOR_CHAR, start + 1);
        return (start != -1) ? localeSuffix.substring(start + 1) : "";  //NOI18N
    }

    /**
     * Returns the first part of a given locale suffix.
     * The locale suffix must be either empty or start with an underscore.
     *
     * @param  localeSuffix  locale suffix, e.g. &quot;_en_US&quot;
     * @return  first part of the suffix, i.&thinsp;e. the part
     *          between the initial <code>'_'</code> and the
     *          (optional) next <code>'_'</code>; or <code>null</code>
     *          if an empty string was given as an argument
     */
    private static String getFirstPart(String localeSuffix) {
        if (localeSuffix.length() == 0) {
            return null;
        }

        assert localeSuffix.charAt(0) == PRB_SEPARATOR_CHAR;

        int end = localeSuffix.indexOf(PRB_SEPARATOR_CHAR, 1);
        return (end != -1) ? localeSuffix.substring(1, end)
                           : localeSuffix.substring(1);
    }

    /** Gets a label for properties nodes for individual locales. */
    public static String getLocaleLabel(MultiDataObject.Entry fe) {
        
        String localeSuffix = getLocaleSuffix(fe);
        String language;
        String country;
        String variant;

        /*
         * Get the abbreviations for language, country and variant and check
         * if at least one of them is specified. If none of them is specified,
         * return the default label:
         */
        if (localeSuffix.length() == 0) {
            language = "";                                              //NOI18N
            country = "";                                               //NOI18N
            variant = "";                                               //NOI18N
        } else {
            language = getLanguage(localeSuffix);
            country  = getCountry(localeSuffix);
            variant  = getVariant(localeSuffix);

            // intern empty strings so that we can use '==' instead of equals():
            language = language.length() != 0 ? language : "";          //NOI18N
            country  = country.length() != 0  ? country : "";           //NOI18N
            variant  = variant.length() != 0  ? variant : "";           //NOI18N
        }

        String defaultLangName = null;
        if (language == "") {                                           //NOI18N
            defaultLangName = NbBundle.getMessage(
                    Util.class,
                    "LAB_defaultLanguage");                             //NOI18N
        }

        /* Simple case #1 - the default locale */
        if (language == "" && country == "" && variant == "") {         //NOI18N
            return defaultLangName;
        }

        String localeSpec = localeSuffix.substring(1);
        Locale locale = new Locale(language, country, variant);

        /* - language name: */
        String langName;
        if (language == "") {                                           //NOI18N
            langName = defaultLangName;
        } else {
            langName = locale.getDisplayLanguage();
            if (langName.equals(language)) {
                langName = NbBundle.getMessage(Util.class,
                                               "LAB_unknownLanguage",   //NOI18N
                                               language);
            }
        }

        /* Simple case #2 - language specification only */
        if (country == "" && variant == "") {                           //NOI18N
            return NbBundle.getMessage(Util.class,
                                       "LAB_localeSpecLang",            //NOI18N
                                       localeSpec,
                                       langName);
        }

        /* - country name: */
        String countryName = "";                                        //NOI18N
        if (country != "") {                                            //NOI18N
            countryName = locale.getDisplayCountry();
            if (countryName.equals(country)) {
                countryName = NbBundle.getMessage(Util.class,
                                                  "LAB_unknownCountry", //NOI18N
                                                  country);
            }
        }

        /* - variant name: */
        String variantName = variant == "" ? ""                         //NOI18N
                                           : locale.getDisplayVariant();

        /* Last case - country and/or variant specification */
        String countryAndVariant;
        if (variantName == "") {                                        //NOI18N
            countryAndVariant = countryName;
        } else if (countryName == "") {                                 //NOI18N
            countryAndVariant = variantName;
        } else {
            countryAndVariant = countryName + ", " + variantName;       //NOI18N
        }
        return NbBundle.getMessage(Util.class,
                                   "LAB_localeSpecLangCountry",         //NOI18N
                                   localeSpec,
                                   langName,
                                   countryAndVariant);

    }

    /** Notifies an error happened when attempted to create locale which exists already. 
     * @param locale locale which already exists */ 
    private static void notifyError(String locale) {
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
            MessageFormat.format(
                NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                    new Object[] {locale}), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
    }

    /**
     * Creates a new PropertiesDataObject (properties file).
     * @param folder FileObject folder where to create the properties file
     * @param fileName String name of the file without the extension, can include
     *        relative path underneath the folder
     * @return created PropertiesDataObjet
     */
    public static PropertiesDataObject createPropertiesDataObject(FileObject folder, String fileName)
        throws IOException
    {
        int idx = fileName.lastIndexOf('/');
        if (idx > 0) {
            String folderPath = fileName.substring(0, idx);
            folder = FileUtil.createFolder(folder, folderPath);
            fileName = fileName.substring(idx + 1);
        }
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource("Templates/Other/properties.properties"); // NOI18N
        DataObject template = DataObject.find(templateFO);
        return (PropertiesDataObject)
               template.createFromTemplate(DataFolder.findFolder(folder), fileName);
    }

    public static void createLocaleFile(PropertiesDataObject propertiesDataObject,
                                        String locale,
                                        boolean copyInitialContent)
    {
        try {
            if(locale.length() == 0) {
                // It would mean default locale to create again.
                notifyError(locale);
                return;
            }

            if(propertiesDataObject != null) {
                FileObject file = propertiesDataObject.getPrimaryFile();
                final String newName = file.getName() + PropertiesDataLoader.PRB_SEPARATOR_CHAR + locale;
                final FileObject folder = file.getParent();
//                                    final PropertiesEditorSupport editor = (PropertiesEditorSupport)propertiesDataObject.getCookie(PropertiesEditorSupport.class);
                java.util.Iterator it = propertiesDataObject.secondaryEntries().iterator();
                while (it.hasNext()) {
                    FileObject f = ((FileEntry)it.next()).getFile();
                    if (newName.startsWith(f.getName()) && f.getName().length() > file.getName().length())
                        file = f;
                }
                if (file.getName().equals(newName)) {
                    return; // do nothing if the file already exists
                }

                if (copyInitialContent) {
                    if (folder.getFileObject(newName, PropertiesDataLoader.PROPERTIES_EXTENSION) == null) {
                        SaveCookie save = (SaveCookie) propertiesDataObject.getCookie(SaveCookie.class);
                        if (save != null) {
                            save.save();
                        }
                        final FileObject templateFile = file;
                        folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                templateFile.copy(folder, newName, PropertiesDataLoader.PROPERTIES_EXTENSION);
                            }
                        });
                    }
                } else {
                    // Create an empty file - creating from template via DataObject
                    // API would create a separate DataObject for the locale file.
                    // After creation force the DataObject to refresh its entries.
                    DataObject.find(folder.createData(newName, PropertiesDataLoader.PROPERTIES_EXTENSION));
                }
            }
        } catch(IOException ioe) {
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ioe.printStackTrace();

            notifyError(locale);
        }
    }
}
