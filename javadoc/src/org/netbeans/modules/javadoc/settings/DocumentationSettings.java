/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.javadoc.settings;

import java.io.File;
import java.lang.reflect.Modifier;

import org.openide.options.ContextSystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import com.netbeans.developer.modules.javadoc.comments.AutoCommenter;

/** Options for applets - which applet viewer use ...
*
* @author Petr Hrebejk
* @version 0.1, Apr 15, 1999
*/
public class DocumentationSettings extends ContextSystemOption //implements ViewerConstants 
  {

  /** autocoment window settings */
  private static int autocommentModifierMask = 
    Modifier.PROTECTED | Modifier.PUBLIC;
  private static boolean autocommentPackage = false;
  private static int autocommentErrorMask = 
    AutoCommenter.JDC_OK | AutoCommenter.JDC_ERROR | AutoCommenter.JDC_MISSING;

  /** idexsearch windows settings */

  /** generation */
  private static boolean externalJavadoc = false;

  /** searchpath */
  private static String[] searchPath = new String[] {"c:/Jdk1.2/doc" };

  /** Holds value of property idxSearchSort. */
  private static String idxSearchSort = "A";
  
  /** Holds value of property idxSearchNoHtml. */
  private static boolean idxSearchNoHtml = false;
  
  /** Holds value of property idxSearchSplit. */
  private static int idxSearchSplit = 50;
  
  // Private attributes for option's children

  private static JavadocSettings javadocSettings;
  private static StdDocletSettings stdDocletSettings;


  static {
    // Create option's children
    javadocSettings  = new JavadocSettings ();
    stdDocletSettings =  new StdDocletSettings ();
  }


  /** Constructor for DocumentationSettings adds optipn's children */
  public DocumentationSettings () {
    addOption( javadocSettings );
    addOption( stdDocletSettings );
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
  
  public boolean isExternalJavadoc () {
    return externalJavadoc;
  }
  
  /** setter for viewer */
   
  public void setExternalJavadoc(boolean b) {
    externalJavadoc = b;
    /*
    if (v.equals(INTERNAL_BROWSER) || v.equals(APPLETVIEWER) || v.equals(EXTERNAL))
      viewer = v;
    */
  }
  
  /** Getter for documentation search path
  */  
  public String[] getSearchPath() {
    return searchPath;
  }
  
  /** Setter for documentation search path
  */  
  public void setSearchPath(String[] s) {
    searchPath = s;
  }

  /** Getter for autocommentModifierMask
  */  
  public int getAutocommentModifierMask() {
    return autocommentModifierMask;
  }
  
  /** Setter for autocommentModifierMask
  */  
  public void setAutocommentModifierMask(int mask) {
    autocommentModifierMask = mask;
  }
  
  /** Getter for autocommentPackage
  */  
  public boolean  getAutocommentPackage() {
    return autocommentPackage;
  }
  
  /** Setter for autocommentPackage
  */  
  public void setAutocommentPackage(boolean pckg) {
    autocommentPackage = pckg;
  }
  
  
  /** Getter for autocommentErrorMask
  */  
  public int getAutocommentErrorMask() {
    return autocommentErrorMask;
  }
  
  /** Setter for documentation autocommentErrorMask
  */  
  public void setAutocommentErrorMask(int mask) {
    autocommentErrorMask = mask;
  }
  
  /** Getter for property idxSearchSort.
   *@return Value of property idxSearchSort.
   */
  public String getIdxSearchSort() {
    return idxSearchSort;
  }
  
  /** Setter for property idxSearchSort.
   *@param idxSearchSort New value of property idxSearchSort.
   */
  public void setIdxSearchSort(String idxSearchSort) {
    this.idxSearchSort = idxSearchSort;
  }
  
  /** Getter for property idxSearchNoHtml.
   *@return Value of property idxSearchNoHtml.
   */
  public boolean isIdxSearchNoHtml() {
      return idxSearchNoHtml;
  }
  
  /** Setter for property idxSearchNoHtml.
   *@param idxSearchNoHtml New value of property idxSearchNoHtml.
   */
  public void setIdxSearchNoHtml(boolean idxSearchNoHtml) {
    this.idxSearchNoHtml = idxSearchNoHtml;
  }
  
  /** Getter for property idxSearchSplit.
   *@return Value of property idxSearchSplit.
   */
  public int getIdxSearchSplit() {
    return idxSearchSplit;
  }
  
  /** Setter for property idxSearchSplit.
   *@param idxSearchSplit New value of property idxSearchSplit.
   */
  public void setIdxSearchSplit(int idxSearchSplit) {
    this.idxSearchSplit = idxSearchSplit;
  }
  
}


/*
 * Log
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
