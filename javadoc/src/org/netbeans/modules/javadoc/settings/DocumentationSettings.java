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

package org.netbeans.modules.javadoc.settings;

import java.io.File;
import java.lang.reflect.Modifier;

//import org.openide.options.ContextSystemOption;
import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ServiceType;

import org.netbeans.modules.javadoc.comments.AutoCommenter;
import org.netbeans.modules.javadoc.settings.ExternalJavadocSettingsService;
import org.netbeans.modules.javadoc.search.Jdk12SearchType;
import org.netbeans.modules.javadoc.search.JavadocSearchType;
import org.netbeans.modules.javadoc.*;
import org.openide.util.Lookup;

/** Options for applets - which applet viewer use ...
*
* @author Petr Hrebejk
* @version 0.1, Apr 15, 1999
*/
public class DocumentationSettings extends SystemOption {
    
    private static final String PROP_SEARCH_PATH         = "searchPatch";   //NOI18N
    private static final String PROP_SEARCH_SORT         = "idxSearchSort";   //NOI18N
    private static final String PROP_SEARCH_NO_HTML      = "idxSearchNoHtml";   //NOI18N
    private static final String PROP_SEARCH_SPLIT        = "idxSearchSplit";       //NOI18N
    private static final String PROP_AUTOCOMENT_SPLIT    = "autocommentSplit";   //NOI18N
    private static final String PROP_AUTOCOMENT_MOD_MASK = "autocommentModifierMask";   //NOI18N
    private static final String PROP_AUTOCOMENT_PACKAGE  = "autocommentPackage";   //NOI18N
    private static final String PROP_AUTOCOMENT_ERR_MASK = "autocommentErrorMask";   //NOI18N
    private static final String PROP_EXECUTOR            = "executorEngine";       //NOI18N
    private static final String PROP_SEARCH              = "searchEngine";   //NOI18N
    private static final String PROP_FS_SETTING          = "filesystemSetting";   //NOI18N
    private static final String PROP_ASK_BEFORE_GEN      = "askBeforeGenerating";   //NOI18N
    private static final String PROP_ASK_AFTER_GEN       = "askAfterGenerating";   //NOI18N
       
    static final long serialVersionUID =-574331845406968391L;
    
    /** Constructor for DocumentationSettings */
    protected void initialize () {
        super.initialize ();
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
            setAutocommentErrorMask(AutoCommenter.JDC_OK | AutoCommenter.JDC_ERROR | AutoCommenter.JDC_MISSING);
        if( getProperty( PROP_EXECUTOR ) == null )
            setExecutor( new ExternalJavadocSettingsService() );
        if( getProperty( PROP_SEARCH ) == null )
            setSearchEngine( new Jdk12SearchType() );
        if( getFileSystemSettings() == null )
            setFileSystemSettings( new java.util.HashMap() );
        if( getProperty( PROP_ASK_BEFORE_GEN ) == null )
            setAskBeforeGenerating( false );
        if( getProperty( PROP_ASK_AFTER_GEN ) == null )
            setAskAfterGenerating( true );
    }

    public static DocumentationSettings getDefault(){
        return (DocumentationSettings)Lookup.getDefault().lookup(DocumentationSettings.class);
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
        putProperty( PROP_AUTOCOMENT_MOD_MASK, new Integer(mask), true );
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
        putProperty( PROP_AUTOCOMENT_PACKAGE, new Boolean(pckg), true );
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
        putProperty( PROP_AUTOCOMENT_ERR_MASK, new Integer(mask), true );
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
        putProperty( PROP_SEARCH_SORT , idxSearchSort, true );
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
        putProperty( PROP_SEARCH_NO_HTML, new Boolean(idxSearchNoHtml), true );
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
        putProperty( PROP_SEARCH_SPLIT , new Integer(idxSearchSplit), true );
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
        putProperty( PROP_AUTOCOMENT_SPLIT , new Integer(autocommentSplit), true );
        //this.autocommentSplit = autocommentSplit;
    }
    
    /** Getter for property executor.
     * @return Value of property executor.
 */
    public ServiceType getExecutor() {
        JavadocType.Handle javadocType = (JavadocType.Handle)getProperty( PROP_EXECUTOR );
        JavadocType type = null;
        
        if (javadocType != null) {
            type = (JavadocType)javadocType.getServiceType();
        }
        if (type == null) {
            return setDefaultJavadocExecutor();
	}
        return type;        
    }
    
    JavadocType setDefaultJavadocExecutor() {
        JavadocType javadocType = (JavadocType)Lookup.getDefault().lookup(ExternalJavadocSettingsService.class);
        //ServiceType.Registry.find("org.netbeans.ExternalJavadocSettingsService");
        //ServiceType
        //new ExternalJavadocSettingsService();
        setExecutor(javadocType);
        return javadocType;        
    }    
    
    /** Setter for property executor.
     * @param executor New value of property executor.
     */
    public void setExecutor(ServiceType executor) {
        putProperty( PROP_EXECUTOR , new JavadocType.Handle(executor), true );        
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
            return setDefaultJavadocSearchType();
	}
        return type;        
    }    
    
    JavadocSearchType setDefaultJavadocSearchType() {
        JavadocSearchType searchType = new Jdk12SearchType();
        setSearchEngine(searchType);
        return searchType;        
    }    

    /** Setter for property search.
     * @param search New value of property search.
    */
    public void setSearchEngine(ServiceType search) {
        putProperty( PROP_SEARCH , new JavadocSearchType.Handle( search ), true );
    }    

    
    public java.util.HashMap getFileSystemSettings(){
        //Thread.currentThread().dumpStack();
        java.util.HashMap map  = (java.util.HashMap)getProperty( PROP_FS_SETTING );
        /*
        if( map != null ){
            System.err.println("Getting map size " + map.size());
        }
        else{
        */
        if( map == null ){
            //System.err.println("Getting map size null");
            //map = new java.util.HashMap();
            //map.put("test", "test");
            setFileSystemSettings( new java.util.HashMap() );
        }
        return map;
    }

    public void setFileSystemSettings(java.util.HashMap map){
        //Thread.currentThread().dumpStack();
        //System.err.println("Setting map size " + map.size());
        //java.util.HashMap oldMap = map;
        putProperty( PROP_FS_SETTING, map, true );        
        //firePropertyChange(PROP_FS_SETTING, oldMap, map);
    }
    
    public boolean getAskBeforeGenerating(){
        return ((Boolean)getProperty( PROP_ASK_BEFORE_GEN )).booleanValue();
    }

    public void setAskBeforeGenerating( boolean ask ){
        putProperty( PROP_ASK_BEFORE_GEN, new Boolean(ask), true );
    }

    public boolean getAskAfterGenerating(){
        return ((Boolean)getProperty( PROP_ASK_AFTER_GEN )).booleanValue();
    }

    public void setAskAfterGenerating( boolean ask ){
        putProperty( PROP_ASK_AFTER_GEN, new Boolean(ask), true );
    }    
}


/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Petr Hrebejk    i18n
 *  9    Gandalf   1.8         11/27/99 Patrik Knakal   
 *  8    Gandalf   1.7         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/17/99  Petr Hrebejk    IndexSearch window 
 *       serialization
 *  5    Gandalf   1.4         8/13/99  Petr Hrebejk    Serialization of 
 *       autocomment window added  
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/17/99  Petr Hrebejk    
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
