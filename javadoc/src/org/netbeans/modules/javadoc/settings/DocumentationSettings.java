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
//import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.javadoc.comments.AutoCommenter;

/** Options for applets - which applet viewer use ...
*
* @author Petr Hrebejk
* @version 0.1, Apr 15, 1999
*/
public class DocumentationSettings extends SharedClassObject //ContextSystemOption //implements ViewerConstants
{
    private static final String PROP_SEARCH_PATH         = "searchPatch";
    private static final String PROP_SEARCH_SORT         = "idxSearchSort";
    private static final String PROP_SEARCH_NO_HTML      = "idxSearchNoHtml";
    private static final String PROP_SEARCH_SPLIT        = "idxSearchSplit";    
    private static final String PROP_AUTOCOMENT_SPLIT    = "autocommentSplit";
    private static final String PROP_AUTOCOMENT_MOD_MASK = "autocommentModifierMask";
    private static final String PROP_AUTOCOMENT_PACKAGE  = "autocommentPackage";
    private static final String PROP_AUTOCOMENT_ERR_MASK = "autocommentErrorMask";
    
    
    /** autocoment window settings */
    //private static int autocommentModifierMask = Modifier.PROTECTED | Modifier.PUBLIC;
    //private static boolean autocommentPackage = false;
    //private static int autocommentErrorMask =  AutoCommenter.JDC_OK | AutoCommenter.JDC_ERROR | AutoCommenter.JDC_MISSING;

    /** idexsearch windows settings */

    /** generation */
    private static boolean externalJavadoc = false;

    /** searchpath */
    //private static String[] searchPath = new String[] {"c:/Jdk1.2/doc" }; // NOI18N

    /** Holds value of property idxSearchSort. */
    //private static String idxSearchSort = "A"; // NOI18N

    /** Holds value of property idxSearchNoHtml. */
    //private static boolean idxSearchNoHtml = false;

    /** Holds value of property idxSearchSplit. */
    //private static int idxSearchSplit = 50;

    /** Holds value of property autocommentSplit. */
    //private static int autocommentSplit = 35;
    
    static final long serialVersionUID =-574331845406968391L;
    
    /** Constructor for DocumentationSettings */
    public DocumentationSettings () {
        setSearchPath(new String[] {"c:/Jdk1.2/doc" });
        setIdxSearchSort("A");
        setIdxSearchNoHtml(false);
        setIdxSearchSplit(50);
        setAutocommentSplit(35);
        setAutocommentModifierMask(Modifier.PROTECTED | Modifier.PUBLIC);
        setAutocommentPackage(false);
        setAutocommentErrorMask(AutoCommenter.JDC_OK | AutoCommenter.JDC_ERROR | AutoCommenter.JDC_MISSING);
        
    }


    /** @return human presentable name */
    public String displayName() {
        return NbBundle.getBundle(JavadocSettings.class).getString("CTL_Documentation_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DocumentationSettings.class);
    }

    /** getter for type of generation
    */
/*
    public boolean isExternalJavadoc () {
        return externalJavadoc;
    }
*/
    /** setter for viewer */
/*
    public void setExternalJavadoc(boolean b) {
        externalJavadoc = b;
    }
*/
    /** Getter for documentation search path
    */  
    public String[] getSearchPath() {
        return (String[])getProperty( PROP_SEARCH_PATH );
        //return searchPath;
    }

    /** Setter for documentation search path
    */  
    public void setSearchPath(String[] s) {
        putProperty( PROP_SEARCH_PATH, s, true );
        //searchPath = s;
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
