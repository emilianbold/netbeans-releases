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

package org.netbeans.modules.properties;

import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.*;

/** Miscellaneous utilities for Properties data loader.
*
* @author Petr Jiricka
*/
public final class Util extends Object {
                                                                                       
  /** Character used to separate parts of bundle properties file name */                                                                                     
  public static final char PRB_SEPARATOR_CHAR = PropertiesDataLoader.PRB_SEPARATOR_CHAR;
  /** Default length for the first part of node label */                                                                                     
  public static final int LABEL_FIRST_PART_LENGTH = 10;
              
  /** Converts a string to a string suitable for a resource bundle key */            
  public static String stringToKey(String source) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < source.length(); i++) {
      char x = source.charAt(i);
      switch (x) {          
        case '=':
        case ':':
        case '\t':
        case '\r':
        case '\n':
        case '\f':
        case ' ': 
          result.append('_'); break;
        default:
          result.append(x); 
      }                       
    }
    return result.toString();
  }
    
  /** returns the file for the primary entry
  *   @param fe entry for a properties file
  */
  private static FileObject getPrimaryFileObject(MultiDataObject.Entry fe) {
    return fe.getDataObject().getPrimaryEntry().getFile();
  }
   
  /** Assembles a file name for a properties file from its base name and language */                                            
  public static String assembleName (String baseName, String lang) {                
    if (lang.length() == 0)
      return baseName;
    else {
      if (lang.charAt(0) != PRB_SEPARATOR_CHAR) {
        StringBuffer res = new StringBuffer().append(baseName).append(PRB_SEPARATOR_CHAR).append(lang);
        return res.toString();
      }  
      else  
        return baseName + lang;
    }  
  }                                                                       
  /** returns a locale part of file name based on the primary file entry for a properties DataObject
  *   for example for file <code>Bundle_en_US.properties</code> returns <code>_en_US</code>, if Bundle.properties exists
  */
  public static String getLocalePartOfFileName(MultiDataObject.Entry fe) {
    String myName   = fe.getFile().getName();
    String baseName = getPrimaryFileObject(fe).getName();
    if (!myName.startsWith(baseName))
      throw new InternalError("Never happens - error in Properties loader");
    return myName.substring(baseName.length());
  }
    
  /** returns a language from a file name based on the primary file entry for a properties DataObject
  *   for example for file <code>Bundle_en_US.properties</code> returns <code>en</code> (if Bundle.properties exists)
  *   @return language for this locale or <code>null</code> if no language is present
  */
  public static String getLanguage(MultiDataObject.Entry fe) {
    String part = getLocalePartOfFileName(fe);
    return getFirstPart(part);
  }
  
  /** returns a country from a file name based on the primary file entry for a properties DataObject
  *   for example for file <code>Bundle_en_US.properties</code> returns <code>US</code> (if Bundle.properties exists)
  *   @return language for this locale or <code>null</code> if no country is present
  */
  public static String getCountry(MultiDataObject.Entry fe) {
    try {
      String part = getLocalePartOfFileName(fe);
      int start = part.indexOf(PRB_SEPARATOR_CHAR, 1);
      if (start == -1)
        return null;
      return getFirstPart(part.substring(start));
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }
  
  /** returns a variant from a file name based on the primary file entry for a properties DataObject
  *   for example for file <code>Bundle_en_US_POSIX.properties</code> returns <code>POSIX</code> (if Bundle.properties exists)
  *   @return language for this locale or <code>null</code> if no variant is present
  */
  public static String getVariant(MultiDataObject.Entry fe) {
    try {
      String part = getLocalePartOfFileName(fe);
      int start = part.indexOf(PRB_SEPARATOR_CHAR, 1);
      if (start == -1)
        return null;
      start = part.indexOf(PRB_SEPARATOR_CHAR, start + 1);
      if (start == -1)
        return null;
      return getFirstPart(part.substring(start));
    }  
    catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }

  /** Returns first substring enclosed between the leading underscore and the next underscore */
  private static String getFirstPart(String part) {
    try {
      if (part.length() == 0)
        return null;        
      if (part.charAt(0) != PRB_SEPARATOR_CHAR)
        throw new InternalError("Never happens - error in Properties loader (" + part + ")");
      int end = part.indexOf(PRB_SEPARATOR_CHAR, 1);
      String result;
      result = (end == -1) ? part.substring(1) : part.substring(1, end);
      return (result.length() == 0) ? null : result;
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }
  
  /** Returns a label for properties nodes for individual locales */
  public static String getPropertiesLabel(MultiDataObject.Entry fe) {
  
    // locale-specific part of the file name
    String temp = getLocalePartOfFileName(fe);
    if (temp.length() > 0)
      if (temp.charAt(0) == PRB_SEPARATOR_CHAR)
        temp = temp.substring(1);
        
    // start constructing the result    
    StringBuffer result = new StringBuffer(temp);
    if (temp.length() > 0)
      result.append(" - ");
    // pad by whitespaces to length LABEL_FIRST_PART_LENGTH
/*    if (result.length() < LABEL_FIRST_PART_LENGTH)
      result.append(new String("                                ").
        substring(0, LABEL_FIRST_PART_LENGTH - result.length()));*/
      
    // append the language  
    String lang = getLanguage(fe);
    if (lang == null)
      temp = NbBundle.getBundle(Util.class).getString("LAB_DefaultBundle_Label");
    else {
      temp = (new Locale(lang, "")).getDisplayLanguage();
      if (temp.length() == 0)
        temp = lang;
    }               
    result.append(temp);
      
    // append the country
    String coun = getCountry(fe);
    if (coun == null)
      temp = "";
    else {
      temp = (new Locale(lang, coun)).getDisplayCountry();
      if (temp.length() == 0)
        temp = coun;
    }               
    if (temp.length() != 0) {
      result.append(" / ");
      result.append(temp);
    }  
      
    // append the variant
    String variant = getVariant(fe);
    if (variant == null)
      temp = "";
    else {
      temp = (new Locale(lang, coun, variant)).getDisplayVariant();
      if (temp.length() == 0)
        temp = variant;
    }               
    if (temp.length() != 0) {
      result.append(" / ");
      result.append(temp);
    }  
    
    // return the result
    return result.toString();
  }
    
}

/*
 * <<Log>>
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/17/99  Petr Jiricka    Added stringToKey 
 *       utility method
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
