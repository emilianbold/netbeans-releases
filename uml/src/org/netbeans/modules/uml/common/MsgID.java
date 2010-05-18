/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.common;

import java.util.Locale;

/**
 * Encapsulates a message ID and the StringResolver used to
 * resolve the locale dependant message.
 */
public class MsgID implements Comparable
{
 /** Constructor. */
  public MsgID (String ID, StringResolver rb)
  {
    iID = ID;
    iRB = rb;
  }
  public int compareTo(Object o)
	{
  	if (! (o instanceof MsgID) ) return -1;
  	
    MsgID msg = (MsgID)o;

    return get().compareToIgnoreCase(msg.get());
	}
 /** Gets the message ID.*/
  public String getID() { return iID; }
  
 /** Gets the string resolver used to resolve the locale dependent message. */
  public StringResolver getRB() { return iRB; }
  
 /*---------------------------------------------------------
  * Convenience methods for getting locale dependant string
  *---------------------------------------------------------*/
  
 /** Convenience method. Returns the same value as getID(). */ 
  public String toString() { return getID(); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. */
  public final String get() { return iRB.get(iID); }

 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(int p1) { return iRB.get(iID, p1); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(long p1) { return iRB.get(iID, p1); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(boolean p1) { return iRB.get(iID, p1); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(float p1) { return iRB.get(iID, p1); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(double p1) { return iRB.get(iID, p1); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1) { return iRB.get(iID, p1); }
  
 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Object p2) { return iRB.get(iID, p1, p2); }

 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Object p2, Object p3)
  {
    return iRB.get(iID, p1, p2, p3);
  }

 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Object p2, Object p3, Object p4)
  {
    return iRB.get(iID, p1, p2, p3, p4);
  }

 /** Convenience method. Gets the locale dependent message for the default locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object[] params) { return iRB.get(iID, params); }
  
 /** Convenience method. Gets the locale dependent message for the given locale. */
  public final String get(Locale locale) { return iRB.get(iID, locale); }
  
 /** Convenience method. Gets the locale dependent message for the given locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Locale locale) { return iRB.get(iID, p1, locale); }
  
 /** Convenience method. Gets the locale dependent message for the given locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Object p2, Locale locale)
  {
    return iRB.get(iID, p1, p2, locale);
  }
  
 /** Convenience method. Gets the locale dependent message for the given locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Object p2, Object p3, Locale locale)
  {
    return iRB.get(iID, p1, p2, p3, locale);
  }

 /** Convenience method. Gets the locale dependent message for the given locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object p1, Object p2, Object p3, Object p4, Locale locale)
  {
    return iRB.get(iID, p1, p2, p3, p4, locale);
  }
  
 /** Convenience method. Gets the locale dependent message for the given locale. 
     The occurences of %x are replace by the given parameters. */
  public final String get(Object[] params, Locale locale) { return iRB.get(iID, params, locale); }

 /** Compares this object with another MsgID. */
  public boolean equals(Object obj)
  {
    if (obj == null) return false;
    if ( !(obj instanceof MsgID) ) return false;
    MsgID tmp = (MsgID)obj;
    
    return Util.compare(iID, tmp.iID);
  }
  
  private String iID;
  private StringResolver iRB;
}
