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

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;


/**
 * Abstract class. Supports 'one i18n session' -> i18n-zing of one source file.
 * Used as a base class for concrete support implementations.
 *
 * @author  Peter Zavadsky
 */
public abstract class I18nSupport {
    
    /** <code>I18nFinder</code>. */
    private I18nFinder finder;
    
    /** <code>I18nReplacer</code>. */
    private I18nReplacer replacer;

    /** <code>DataObject</code> which document the i18n session will run thru. */
    protected DataObject sourceDataObject;

    /** Document on which the i18n-session will be performed. */
    protected StyledDocument document;
    
    /** Resource holder for sepcific subclass instance. */
    protected final ResourceHolder resourceHolder;
    

    /** Constructor. */
    public I18nSupport(DataObject sourceDataObject) {
        this.sourceDataObject = sourceDataObject;
        
        EditorCookie editorCookie = (EditorCookie)sourceDataObject.getCookie(EditorCookie.class);
        
        if(editorCookie == null)
            throw new IllegalArgumentException("I18N: Illegal data object type"+ sourceDataObject); // NOI18N
        
        this.document = editorCookie.getDocument();
        
        this.resourceHolder = createResourceHolder();
    }
    
    
    /** Creates <code>I18nFinder</code> instance used by this instance. */
    protected abstract I18nFinder createFinder();
    
    /** Cretates <code>I18nReplacer</code> instance used by this instance. */
    protected abstract I18nReplacer createReplacer();
    
    /** Creates <code>ResourceHolder<code> for this instance. */
    protected abstract ResourceHolder createResourceHolder();

    /** Gets <code>I18nFinder</code>. */
    public final I18nFinder getFinder() {
        if(finder == null)
            finder = createFinder();
            
        return finder;
    }
    
    /** Gets <code>I18nReplacer</code> for this support. */
    public final I18nReplacer getReplacer() {
        if(replacer == null)
            replacer = createReplacer();
        
        return replacer;
    }

    /** Getter for <code>sourceDataObject</code> property. */
    public final DataObject getSourceDataObject() {
        return sourceDataObject;
    }
    
    /** Getter for <code>document</code> property. */
    public final StyledDocument getDocument() {
        return document;
    }
    
    /** Gets default <code>I18nString</code> instance for this i18n session,
     * has a non-null resource holder field, but resource of that holder may be not initialized yet. */
    public I18nString getDefaultI18nString() {
        return getDefaultI18nString(null);
    }
    
    /** Gets default <code>I18nString</code> for this instance. */
    public abstract I18nString getDefaultI18nString(HardCodedString hcString);
    
    /** Gets JPanel showing info about found hard coded value. */
    public abstract JPanel getInfo(HardCodedString hcString);

    /** Getter for <code>resourceHolder</code>. */
    public ResourceHolder getResourceHolder() {
        return resourceHolder;
    }

    /** Indicates if supports customizer for additional source specific values. Override in subclasses if nedded.
     * @return false 
     * @see #getAdditionalCustommizer */
    public boolean hasAdditionalCustomizer() {
        return false;
    }
    
    /** Gets additional customizer. Override in subclasses if needed.
     * @param i18nString instance for which could additional values be customized.
     * @return null 
     * @see #hasAdditionalCustomizer */
    public JPanel getAdditionalCustomizer(I18nString i18nString) {
        return null;
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

    /** Factory inteface for creating <code>I18nSupport</code> instances. */
    public interface Factory {
        
        /** Creates <code>I18nSupport</code> instance for specified data object and document. */
        public I18nSupport create(DataObject dataObject);
    } // End of nested I18nSupportFactory class.
    
}

