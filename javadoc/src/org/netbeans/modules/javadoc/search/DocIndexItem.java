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

package com.netbeans.developer.modules.javadoc.search;


import java.net.URL;

/** Represents one item found in document index

 @author Petr Hrebejk
*/
class DocIndexItem extends Object {

	private String text = null;
  private URL contextURL = null;
  private String spec = null;
  private String remark = null;

  private int iconIndex = DocSearchIcons.ICON_NOTRESOLVED;

  public DocIndexItem ( String text, String remark, URL contextURL, String spec ) {
    this.text = text;
    this.remark = remark;
    this.contextURL = contextURL;  
    this.spec = spec;
  }
  
  public URL getURL () throws java.net.MalformedURLException {
    return new URL( contextURL, spec );
  }

  public String toString() {
    if ( remark != null )
      return text + remark;
    else 
      return text;
  }

  public int getIconIndex() {
    return iconIndex;
  }

  public void setIconIndex( int iconIndex ) {
    this.iconIndex = iconIndex;    
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark( String remark ) {
    this.remark = remark;
  }
}

/* 
 * Log
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 