/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.core.eventframework;

public class OriginalAndNewEventPayload implements IOriginalAndNewEventPayload

{
  private String m_OrigValue = null;
  private String m_NewValue = null;

  public OriginalAndNewEventPayload()
  {
  }

  public String getOriginalValue()
  {
    return m_OrigValue;
  }

  public void setOriginalValue( String value )
  {
    m_OrigValue = value;
  }

  public String getNewValue()
  {
    return m_NewValue;
  }

  public void setNewValue( String value )
  {
    m_NewValue = value;
  }

  public static final String CLSID = "{71C6959E-66A3-4B11-B529-A72855A1ACE7}";
}
