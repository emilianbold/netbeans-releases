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



/**
 * This class is the base class for all Exceptions to be used throughout the
 * project. It displays an internationalized exception message and can be used
 * to wrap non-internationalized exceptions (Exception, ClassCastException,
 * etc) via the setNestedException() method.
 *
 * @author Bing Lu
 */
public class I18nException
    extends Exception
    implements Serializable {

    /**
     * This is the internationalized message describing the exception.
     */
    transient String mString = null;

    /**
     * Handle to our logger.
     */
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(I18nException.class.getName());

    /**
     * The internationalization bundle.
     */
    private String mBundle = null;

    /**
     * The internationalization key to look up the error message.
     */
    private String mKey = null;

    /**
     * A nested exception, if one exists. This field is used to wrap
     * non-internationalized exceptions (subclasses of Exception) inside
     * internationalized ones.
     */
    private Throwable mEmbeddedException = null;

    /**
     * This is a list of arguments that get merged into the final error
     * message. They are supplied by the developer and should contain useful
     * information about the nature of the problem.
     */
    private Object[] mObjList = null;

    /**
     * Constructor for the I18nException object
     *
     * @param keyName The internationalization key to look up the error
     *        template.
     * @param bundleName The bundle where the error template resides.
     * @param params Arguments passed to fill in parameters in the template.
     */
    public I18nException(String keyName, String bundleName, Object[] params) {

        // Call the super class.
        super();

        // Remember the key name, bundle name, and parameter list.
        mKey = keyName;
        mBundle = bundleName;
        mObjList = ArrayUtil.duplicate(params);
    }

    /**
     * Constructor for the I18nException object
     *
     * @param keyName The internationalization key.
     * @param bundleName The internationalizaiton bundle.
     * @param params Bits of information about what went wrong.
     * @param t The exception we wish to embed.
     */
    public I18nException(String keyName, String bundleName, Object[] params,
                        Throwable t) {

        this(keyName, bundleName, params);

        mEmbeddedException = t;
    }

    /**
     * Convenience constructor for the I18nException object. Used when a method
     * catches one kind of I18nException and needs to throw a different kind
     * due to the throws clause in its contract. This constructor should be
     * used sparingly -- only when there is no useful additional information
     * that can be provided by supplying a list of arguments.
     *
     * @param original The original exception being caught, nested and
     *        rethrown.
     */
    public I18nException(I18nException original) {

        // Call the other constructor.
        this(original.getKey(), original.getBundle(), original.getObjList());

        // Set nested exception.
        if (original.getNestedException() == null) {
            mEmbeddedException = original;
        }
    }

    /**
     * Gets the bundle attribute of the I18nException object
     *
     * @return The bundle value
     */
    public String getBundle() {
        return mBundle;
    }

    /**
     * Gets the key attribute of the I18nException object
     *
     * @return The key value
     */
    public String getKey() {
        return mKey;
    }

    /**
     * This method returns the internationalized exception message if
     * internationalization is successful, or an error message if it wasn't.
     *
     * @return The message value
     */
    public String getMessage() {

        if (mString == null) {
            translateException();
        }

        return mString;
    }

    /**
     * Gets the nestedException attribute of the I18nException object
     *
     * @return The nestedException value
     */
    public Throwable getNestedException() {
        return mEmbeddedException;
    }

    /**
     * Gets the objList attribute of the I18nException object
     *
     * @return The objList value
     */
    public Object[] getObjList() {
        return ArrayUtil.duplicate(mObjList);
    }

    /**
     * This method attempts to internationalize the exception message. In case
     * internationalization fails for any of a number of reasons, the
     * exception message is set to explain why the original message could not
     * be translated.
     */
    protected void translateException() {

        I18n translator = null;

        // Reset the internationalized string to null, in case we get an error.
        mString = null;

        // Check the key field from the exception.  If it is null then we cannot
        // map the exception.
        if (mKey == null) {
            mString = formTranslationErrorString(
                "The exception key was not specified.");
        } else if (mBundle == null) {
            mString = formTranslationErrorString(
                "The exception bundle name was not specified.");
        } else {
            try {
                translator = new I18n(mBundle);
            } catch (OutOfMemoryError e) {
                mLog.severe(e.getMessage());

                mString = formTranslationErrorString(
                    "There was not enough memory available.");
            }

            try {
                mString = translator.i18n(mKey, mObjList);
            } catch (Exception e) {
                mLog.severe(e.getMessage());

                mString = formTranslationErrorString(
                    "The bundle, the key, or the value was not found.");
            }
        }
    }

    /**
     * This is a convenience method to help set the message in case of
     * internationalization failure.
     *
     * @return The defaultMessage value
     */
    private String getDefaultMessage() {

        StringBuffer sb = new StringBuffer();
        int idx;

        sb.append("The exception was created with the following key name: ");

        if (mKey == null) {
            sb.append("null");
        } else {
            sb.append("\"" + mKey + "\"");
        }

        sb.append("; bundle name: ");

        if (mBundle == null) {
            sb.append("null");
        } else {
            sb.append("\"" + mBundle + "\"");
        }

        sb.append("; parameters: ");

        if (mObjList == null) {
            sb.append("null");
        } else if (mObjList.length == 0) {
            sb.append("NONE");
        } else {
            sb.append(mObjList[0]);

            for (idx = 1; idx < mObjList.length; idx++) {
                sb.append(", ");
                sb.append(mObjList[idx]);
            }
        }

        sb.append(".");
        return sb.toString();
    }

    /**
     * This is just a convenience method to help report translation errors.
     *
     * @param reason the reason internationalization failed.
     *
     * @return A well formatted String explaining why internationalization
     *         failed.
     */
    private String formTranslationErrorString(String reason) {

        String msg = "Unable to translate the message for "
                     + getClass().getName() + ".  " + reason + "  "
                     + getDefaultMessage();

        if (mEmbeddedException != null) {
            msg += ("  The nested exception message is: "
                    + mEmbeddedException.getMessage());
        }

        return msg;
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
