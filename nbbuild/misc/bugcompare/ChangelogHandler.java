
/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

public interface ChangelogHandler {
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_name(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_msg(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_time(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_file(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_date(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_author(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_commondir(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_utag(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_weekday(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_changelog(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_revision(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_entry(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_branch(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_branchroot(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    /**
     * An event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_tag(final java.lang.String data, final org.xml.sax.AttributeList meta) throws org.xml.sax.SAXException;
    
    
}