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

package com.netbeans.developer.modules.beans;

/** Names of properties of patterns.
*
*
* @author Petr Hrebejk
*/
public interface PatternProperties {
  /** Name of type property for all {@link PropertyPattern}s.
  */
  public static final String PROP_TYPE = "type"; // NOI18N

  public static final String PROP_MODE = "mode"; // NOI18N

  public static final String PROP_NAME = "name"; // NOI18N

  public static final String PROP_GETTER = "getter"; // NOI18N

  public static final String PROP_SETTER = "setter"; // NOI18N

  public static final String PROP_ESTIMATEDFIELD = "estimatedField"; // NOI18N

  public static final String PROP_INDEXEDTYPE = "indexedType"; // NOI18N

  public static final String PROP_INDEXEDGETTER = "indexedGetter"; // NOI18N

  public static final String PROP_INDEXEDSETTER = "indexedSetter"; // NOI18N

  public static final String PROP_ADDLISTENER = "addListener"; // NOI18N
  
  public static final String PROP_REMOVELISTENER = "removeListener"; // NOI18N

  public static final String PROP_ISUNICAST = "isUnicast"; // NOI18N
}

/*
 * Log
 *  3    Gandalf   1.2         1/12/00  Petr Hrebejk    i18n  
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $
 */
