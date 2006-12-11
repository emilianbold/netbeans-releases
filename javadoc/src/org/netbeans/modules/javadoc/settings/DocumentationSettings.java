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

package org.netbeans.modules.javadoc.settings;

import java.lang.reflect.Modifier;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ServiceType;

//import org.netbeans.modules.javadoc.comments.AutoCommenter;
import org.netbeans.modules.javadoc.search.JavadocSearchType;
import org.netbeans.modules.javadoc.*;
import org.openide.util.Lookup;

/** Options for JavaDoc
*
* @author Petr Hrebejk
*/
public class DocumentationSettings extends SystemOption {

    private static final String PROP_SEARCH_SORT         = "idxSearchSort";   //NOI18N
    private static final String PROP_SEARCH_NO_HTML      = "idxSearchNoHtml";   //NOI18N
    private static final String PROP_SEARCH_SPLIT        = "idxSearchSplit";       //NOI18N
    private static final String PROP_AUTOCOMENT_SPLIT    = "autocommentSplit";   //NOI18N
    private static final String PROP_AUTOCOMENT_MOD_MASK = "autocommentModifierMask";   //NOI18N
    private static final String PROP_AUTOCOMENT_PACKAGE  = "autocommentPackage";   //NOI18N
    private static final String PROP_AUTOCOMENT_ERR_MASK = "autocommentErrorMask";   //NOI18N
    private static final String PROP_SEARCH              = "searchEngine";   //NOI18N
    private static final String PROP_FS_SETTING          = "fileSystemSettings";   //NOI18N
       
    static final long serialVersionUID =-574331845406968391L;

    private transient boolean initializing = false;

    /** Constructor for DocumentationSettings */
    protected void initialize () {
        super.initialize ();
        initializing = true;
        if( getProperty( PROP_SEARCH_SORT ) == null )
            setIdxSearchSort("A");   //NOI18N
        if( getProperty( PROP_SEARCH_NO_HTML ) == null )
            setIdxSearchNoHtml(false);
        if( getProperty( PROP_SEARCH_SPLIT ) == null )
            setIdxSearchSplit(50);
        if( getProperty( PROP_AUTOCOMENT_SPLIT ) == null )
            setAutocommentSplit(35);        
        if( getProperty( PROP_AUTOCOMENT_MOD_MASK ) == null )
            setAutocommentModifierMask(Modifier.PROTECTED | Modifier.PUBLIC);        
        if( getProperty( PROP_AUTOCOMENT_PACKAGE ) == null )
            setAutocommentPackage(false);        
        if( getProperty( PROP_AUTOCOMENT_ERR_MASK ) == null )
            // XXX temporarily commented to remove dependency on the org.netbeans.modules.javadoc.comments package
//            setAutocommentErrorMask(AutoCommenter.JDC_OK | AutoCommenter.JDC_ERROR | AutoCommenter.JDC_MISSING);
            setAutocommentErrorMask(7);
        initializing = false;
    }

    public static DocumentationSettings getDefault(){
        return (DocumentationSettings)DocumentationSettings.findObject(DocumentationSettings.class,true);
    }

    /** @return human presentable name */
    public String displayName() {
        return NbBundle.getBundle(DocumentationSettings.class).getString("CTL_Documentation_settings");   //NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DocumentationSettings.class);
    }
    
    /** Getter for autocommentModifierMask
    */  
    public int getAutocommentModifierMask() {
        return ((Integer)getProperty( PROP_AUTOCOMENT_MOD_MASK )).intValue();
        //return autocommentModifierMask;
    }

    /** Setter for autocommentModifierMask
    */  
    public void setAutocommentModifierMask(int mask) {
        putProperty( PROP_AUTOCOMENT_MOD_MASK, new Integer(mask), !(initializing || isReadExternal()) );
        //autocommentModifierMask = mask;
    }

    /** Getter for autocommentPackage
    */  
    public boolean  getAutocommentPackage() {
        return ((Boolean)getProperty( PROP_AUTOCOMENT_PACKAGE )).booleanValue();
        //return autocommentPackage;
    }

    /** Setter for autocommentPackage
    */  
    public void setAutocommentPackage(boolean pckg) {
        putProperty( PROP_AUTOCOMENT_PACKAGE, pckg ? Boolean.TRUE : Boolean.FALSE, !(initializing || isReadExternal()) );
        //autocommentPackage = pckg;
    }


    /** Getter for autocommentErrorMask
    */  
    public int getAutocommentErrorMask() {
        return ((Integer)getProperty( PROP_AUTOCOMENT_ERR_MASK )).intValue();
        //return autocommentErrorMask;
    }

    /** Setter for documentation autocommentErrorMask
    */  
    public void setAutocommentErrorMask(int mask) {
        putProperty( PROP_AUTOCOMENT_ERR_MASK, new Integer(mask), !(initializing || isReadExternal()) );
        //autocommentErrorMask = mask;
    }

    /** Getter for property idxSearchSort.
     *@return Value of property idxSearchSort.
     */
    public String getIdxSearchSort() {
        return (String)getProperty( PROP_SEARCH_SORT );        
        //return idxSearchSort;
    }

    /** Setter for property idxSearchSort.
     *@param idxSearchSort New value of property idxSearchSort.
     */
    public void setIdxSearchSort(String idxSearchSort) {
        putProperty( PROP_SEARCH_SORT , idxSearchSort, !(initializing || isReadExternal()) );
        //this.idxSearchSort = idxSearchSort;
    }

    /** Getter for property idxSearchNoHtml.
     *@return Value of property idxSearchNoHtml.
     */
    public boolean isIdxSearchNoHtml() {
        return ((Boolean)getProperty( PROP_SEARCH_NO_HTML )).booleanValue();
        //return idxSearchNoHtml;
    }

    /** Setter for property idxSearchNoHtml.
     *@param idxSearchNoHtml New value of property idxSearchNoHtml.
     */
    public void setIdxSearchNoHtml(boolean idxSearchNoHtml) {
        putProperty( PROP_SEARCH_NO_HTML, idxSearchNoHtml ? Boolean.TRUE : Boolean.FALSE, !(initializing || isReadExternal()) );
        //this.idxSearchNoHtml = idxSearchNoHtml;
    }

    /** Getter for property idxSearchSplit.
     *@return Value of property idxSearchSplit.
     */
    public int getIdxSearchSplit() {
        return ((Integer)getProperty( PROP_SEARCH_SPLIT )).intValue();        
        //return idxSearchSplit;
    }

    /** Setter for property idxSearchSplit.
     *@param idxSearchSplit New value of property idxSearchSplit.
     */
    public void setIdxSearchSplit(int idxSearchSplit) {
        putProperty( PROP_SEARCH_SPLIT , new Integer(idxSearchSplit), !(initializing || isReadExternal()) );
        //this.idxSearchSplit = idxSearchSplit;
    }

    /** Getter for property autocommentSplit.
     * @return Position of the splitter in the autocomment window.
     */
    public int getAutocommentSplit() {
        return ((Integer)getProperty( PROP_AUTOCOMENT_SPLIT )).intValue();
        //return autocommentSplit;
    }
    /** Setter for property autocommentSplit.
     * @param autocommentSplit Position of the splitter in the autocomment window.
     */
    public void setAutocommentSplit(int autocommentSplit) {
        putProperty( PROP_AUTOCOMENT_SPLIT , new Integer(autocommentSplit), !(initializing || isReadExternal()) );
        //this.autocommentSplit = autocommentSplit;
    }
    
    /** Getter for property search.
     * @return Value of property search.
    */     
    public ServiceType getSearchEngine() {
        JavadocSearchType.Handle searchType = (JavadocSearchType.Handle)getProperty( PROP_SEARCH );
        JavadocSearchType type = null;
        
        if (searchType != null) {
            type = (JavadocSearchType)searchType.getServiceType();
        }
        if (type == null) {
            if (isWriteExternal()) {
                return null;
            }
            type = (JavadocSearchType)Lookup.getDefault().lookup(org.netbeans.modules.javadoc.search.Jdk12SearchType.class);
            if (type != null)
                return type;
            // find ANY search engine
            type = (JavadocSearchType)Lookup.getDefault().lookup(JavadocSearchType.class);
	    }
        return type;        
    }    
    
    /** Setter for property search.
     * @param search New value of property search.
    */
    public void setSearchEngine(ServiceType search) {
        if (search == null &&
            isReadExternal()) {
            putProperty(PROP_SEARCH, null, false);
        } else {
            putProperty( PROP_SEARCH , new JavadocSearchType.Handle( search ), !(initializing || isReadExternal()));
	}        
    }    

    
    public java.util.HashMap getFileSystemSettings(){
        java.util.HashMap map  = (java.util.HashMap)getProperty( PROP_FS_SETTING );
                
        if (map == null) {
            if (isWriteExternal()) {
                return null;
            }
            return new java.util.HashMap();
	}
        return map;        
    }

    public void setFileSystemSettings(java.util.HashMap map){        
        if (map == null &&
            isReadExternal()) {
            putProperty( PROP_FS_SETTING, null, false);
        } else {
            java.util.HashMap old = (java.util.HashMap)putProperty( PROP_FS_SETTING , map, !(initializing || isReadExternal()));
            if( old != null && old.equals(map) )
                firePropertyChange(PROP_FS_SETTING, null, null);
	}                
    }
    
}
