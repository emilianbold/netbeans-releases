/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import javax.swing.JPanel;
import javax.swing.text.StyledDocument;

import org.openide.loaders.DataObject;


/**
 * Abstract class. Supports 'one i18n session' -> i18n-zing of one source file.
 * Used as a base class for concrete support implementations.
 *
 * @author  Peter Zavadsky
 */
public abstract class I18nSupport {
    
    /** <code>DataObject</code> which document the i18n session will run thru. */
    protected DataObject sourceDataObject;

    /** Document on which the i18n-session will be performed. */
    protected StyledDocument document;
    
    /** <code>I18nFinder</code>. */
    protected I18nFinder finder;
    
    /** <code>I18nReplacer</code>. */
    protected I18nReplacer replacer;
    

    /** Constructor. */
    public I18nSupport(DataObject sourceDataObject, StyledDocument document) {
        this.sourceDataObject = sourceDataObject;
        this.document = document;
        
        initialize();
    }
    
    
    /** Initializes finder and replacer for this support. */
    private void initialize() {
        finder = createFinder();
        replacer = createReplacer();
    }
    
    /** Creates finder, used for init. */
    protected abstract I18nFinder createFinder();
    
    /** Cretates replacer, used for init.*/
    protected abstract I18nReplacer createReplacer();

    /** Gets initialized I18nString instance for this i18n session. 
     * have to have a non-null resource holder field, but resource of that holder needs not to be initialized yet. */
    public abstract I18nString getDefaultI18nString();
    
    /** Gets JPanel showing info about found hard coded value. */
    public abstract JPanel getInfo(HardCodedString hcString);
    

    /** Gets <code>I18nFinder</code>. */
    public I18nFinder getFinder() {
        return finder;
    }
    
    /** Gets <code>I18nReplacer</code> for this support. */
    public I18nReplacer getReplacer() {
        return replacer;
    }
    
    /**
     * Interface for finder which will search for hard coded (non-i18n-ized)
     * string in i18n-ized source document.
     */
    public interface I18nFinder {

        /** Gets next hard coded string. Starts from the beginning of the source.
         * @return next hard coded string or null if the search reached end. */
        public HardCodedString findNextHardCodedString();

        /** Gets all hard coded strings from docuement. 
         * @return all hard coded strings from source document or null if such don't exist */
        public HardCodedString[] findAllHardCodedStrings();
    }

    /**
     * Interface implemented by objects which replaces (i18n-zes) hard coded strings
     * source specific way. (The way of i18n-zing source is different from java file to
     * jsp file etc.)
     */
    public interface I18nReplacer {

        /** Replaces hard coded string using settigns encapsulated by <code>I18nString</code> typically customized by user. */
        public void replace(HardCodedString hardString, I18nString i18nString);
    }

}
