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

/* Base class providing search for JDK1.2/1.3 documentation
 * Jdk12SearchType.java
 *
 * Created on 19. ?or 2001, 17:14
 * @author Petr Hrebejk, Petr Suchomel
 */

package org.netbeans.modules.javadoc.search;

import java.util.*;
import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

public class Jdk12SearchType_japan extends JavadocSearchType {

    private boolean caseSensitive = true;
    private String  japanEncoding;
    
    /** generated Serialized Version UID */
    static final long serialVersionUID =-2453877778724454324L;
                                         
    /** Returns human presentable name
     * @return human presentable name
    */
    public String displayName() {
        return NbBundle.getBundle( Jdk12SearchType_japan.class ).getString("CTL_Jdk12_search_eng_ja");   //NOI18N
    }

    /** Returns HelpCtx
     * @return help
     */    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (Jdk12SearchType_japan.class);
    }
    
    /** Getter for property caseSensitive.
     * @return Value of property caseSensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /** Setter for property caseSensitive.
     * @param caseSensitive New value of property caseSensitive.
    */
    public void setCaseSensitive(boolean caseSensitive) {
        boolean oldVal = caseSensitive;
        this.caseSensitive = caseSensitive;
        this.firePropertyChange("caseSensitive", new Boolean(oldVal), new Boolean(caseSensitive));   //NOI18N
    }

    /** Getter for property encoding.
     * @return Value of property encoding.
    */
    public java.lang.String getJapanEncoding() {
        return ( japanEncoding != null ) ? japanEncoding : "JISAutoDetect";    //NOI18N
    }
    
    /** Setter for property encoding.
     * @param encoding New value of property encoding.
    */
    public void setJapanEncoding(java.lang.String japanEncoding) {
        this.japanEncoding = japanEncoding;
    }    
    
    /** default returns null, must be overriden
     * @param fs File system where to find index files
     * @param rootOffset offset , position of index files in file system, normally null
     * @return File object containing index-files
    */
    public FileObject getDocFileObject( FileSystem fs , String rootOffset ) {
        //System.out.println("in jdk 12");        
        if( rootOffset != null && rootOffset.length() != 0 ){
            rootOffset = ( rootOffset.replace('/', '.') + '.' );
        }
        else{
            rootOffset = "";   //NOI18N
        }            
        FileObject fo = fs.find( rootOffset + "index-files", null, null ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = fs.find( rootOffset + "", "index-all", "html" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = fs.find( rootOffset + "api.index-files", null, null ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = fs.find( rootOffset + "api", "index-all", "html" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        return null;
    }    
    
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public IndexSearchThread getSearchThread( String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer ){
        //here you can send one more parameter .. getJapanEncoding
        return new SearchThreadJdk12_japan ( toFind, fo, diiConsumer, isCaseSensitive(), getJapanEncoding() );
    }    
}
