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


package org.netbeans.modules.iep.model.lib;

import java.io.Serializable;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Locale;

import org.openide.util.NbBundle; 


/**
 * This class handles bundles for internationalization. It is placed in the
 * org.netbeans.modules.iep.editor.tcg.exception package because -- if internationalization fails --
 * it needs to access non-internationalized exceptions
 * NonI18nException and NonI18nException. Those
 * exceptions have only package level access.
 *
 * @author Bing Lu
 */
public class I18n implements Serializable {
    private static final long serialVersionUID = -4575776982116252393L;    
    

    /**
     * A handle to our logger.
     */
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(I18n.class.getName());

    /**
     * The list of bundles to search for internationalized Strings.
     */
    private ArrayList mBundleList;

    /**
     * Constructor for the I18n object
     *
     * @param bundle The name of our bundle.
     */
    public I18n(String bundle) {

        mBundleList = new ArrayList();

        setBundle(bundle);
    }

    /**
     * Sets the bundle for this I18n. Note this call wipes out any pre-existing
     * list of bundles.
     *
     * @param bundle The new bundle value
     */
    public void setBundle(String bundle) {

        mBundleList.clear();
        mBundleList.trimToSize();
        mBundleList.add(bundle);
    }

    /**
     * Gets the bundleList attribute of the I18n object
     *
     * @return The bundle list.
     */
    public ArrayList getBundleList() {
        return mBundleList;
    }

    /**
     * Adds a 'fallback' bundle to search in case the primary bundle wasn't
     * found.
     *
     * @param bundle The supplemental bundle.
     */
    public void addBundle(String bundle) {
        mBundleList.add(bundle);
    }

    /**
     * Gets an internationalized String.
     *
     * @param key The key to search for in the properties table.
     * @param list A list of items to populate into a formatted string.
     *
     * @return The internationalized String.
     *
     */
    public String oldI18n(String key, Object[] list) {
        return NbBundle.getMessage(I18n.class, key, list);
    }    

    /**
     * Gets an internationalized String.
     *
     * @param key The key to search for in the properties table.
     * @param list A list of items to populate into a formatted string.
     *
     * @return The internationalized String.
     *
     * @exception NonI18nException If internationalization fails.
     */
    public String i18n(String key, Object[] list)
        throws NonI18nException {

        ResourceBundle res;

        // NOTE: The exceptions have hard coded strings in them.
        // This is because there may be a major
        // problem with the configuration of the system if a
        // resource cannot be located.  These
        // should be the only hard coded strings in the system.
        // 
        // Make sure we have at least one bundle to look at!!!
        if (mBundleList.size() < 1) {
            NonI18nException ex =
                new NonI18nException(
                    "The bundle name was missing!");

            throw ex;
        }

        // Okay, let's walk the list..
        String bundleName = "Unknown";

        for (int i = 0; i < mBundleList.size(); i++) {
            try {
                bundleName = (String) mBundleList.get(i);
                res = ResourceBundle.getBundle(bundleName, Locale.getDefault(), getClass().getClassLoader());
            } catch (MissingResourceException e) {
                NonI18nException ex =
                    new NonI18nException("The bundle, "
                                                      + bundleName
                                                      + ", was not found.");

                mLog.warning(ex.getMessage());

                throw ex;
            } catch (NullPointerException e) {
                NonI18nException ex =
                    new NonI18nException(
                        "The bundle name was missing!");

                mLog.warning(ex.getMessage());

                throw ex;
            }

            try {
                String s = res.getString(key);

                return format(s, list);
            } catch (MissingResourceException e) {
                if (i < (mBundleList.size() - 1)) {

                    // We are consuming the exception here, so we log it.
                    mLog.warning("bundle:" + bundleName + "was missing:"
                               + e.getMessage());

                    // Just continue on to the next bundle!
                    continue;
                }

                NonI18nException ex =
                    new NonI18nException(
                        "The key, " + key
                        + ", could not be found in the bundle " + bundleName
                        + ".");
                // Uncomment the following line to find which string is not i18ned
                // throw ex;
            } catch (Exception e) {
                NonI18nException ex =
                    new NonI18nException(
                        "The key could not be found in the bundle "
                        + bundleName + ".");

                throw ex;
            }
        }

        // We really cannot get here, if we do it is a bug.
        throw new NonI18nException(
            "The key, " + key + ", could not be found in the bundle "
            + bundleName + ".");
    }

    /**
     * Gets an internationalized String.
     *
     * @param key The key to search for in the properties table.
     * @param defaultValue A value to use if the key is not found.
     * @param list A list of items to populate into a formatted string.
     *
     * @return The internationalized String.
     *
     * @exception NonI18nException If internationalization fails.
     */
    public String i18n(String key, String defaultValue, Object[] list) 
        throws NonI18nException {
        String s = null;
        try {
            s = i18n(key, list);
        } catch (NonI18nException e) {    
            if (defaultValue == null) {
                throw e;
            }    
            s = defaultValue;
        }
        return s;
    }    

    /**
     * Gets an internationalized String.
     *
     * @param key The key to search for in the properties table.
     * @param defaultValue A value to use if the key is not found.
     * @param list A list of items to populate into a formatted string.
     *
     * @return The internationalized String.
     *
     * @exception NonI18nException If internationalization fails.
     */
    private String oldI18n(String key, String defaultValue, Object[] list)
        throws NonI18nException {

        ResourceBundle res;

        if (mBundleList.size() < 1) {
            NonI18nException ex =
                new NonI18nException(
                    "The bundle name was missing!");

            throw ex;
        }

        // Okay, let's walk the list..
        String bundleName = "Unknown";

        for (int i = 0; i < mBundleList.size(); i++) {
            try {
                bundleName = (String) mBundleList.get(i);
                res = ResourceBundle.getBundle(bundleName, 
                    Locale.getDefault(), getClass().getClassLoader());
            } catch (MissingResourceException e) {
                NonI18nException ex =
                    new NonI18nException("The bundle, "
                                                      + bundleName
                                                      + ", was not found.");

                mLog.warning(ex.getMessage());

                throw ex;
            } catch (NullPointerException e) {
                NonI18nException ex =
                    new NonI18nException(
                        "The bundle name was missing!");

                mLog.warning(ex.getMessage());

                throw ex;
            }

            try {
                String s = res.getString(key);

                return format(s, list);
            } catch (MissingResourceException e) {
                return format(defaultValue, list);
            } catch (Exception e) {
                mLog.warning(e.getMessage());

                NonI18nException ex =
                    new NonI18nException(
                        "The key could not be found in the bundle "
                        + bundleName + ".");

                throw ex;
            }
        }

        // We really cannot get here, if we do it is a bug.
        NonI18nException ex = new NonI18nException(
            "The key, " + key + ", could not be found in the bundle "
            + bundleName + ".");

        throw ex;
    }

    /**
     * This method completes the internationalization process by populating the
     * internationalized String with the elements of the object list.
     *
     * @param stringFormat The format of the internationalized String
     * @param objList Informational elements to populate the {0} place holders
     *        with.
     *
     * @return The formatted String
     *
     * @exception NonI18nException If internationalization fails.
     */
    private String format(String stringFormat, Object[] objList)
        throws NonI18nException {

        String s = MessageFormat.format(stringFormat, objList);

        if (s == null) {
            NonI18nException ex =
                new NonI18nException(
                    "The format string was missing");

            throw ex;
        } else {
            return s;
        }
    }
}


