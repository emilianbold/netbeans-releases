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

/*
 * BatchProcessRunnable.java
 *
 * Created on June 15, 2004, 11:02 AM
 */

package org.netbeans.modules.uml.ui.support;

import java.util.ArrayList;

/**
 *
 * @author  Trey Spiva
 */
public abstract class BatchProcessRunnable < T > implements Runnable
{
   private ArrayList < T > m_UpdateItems = new ArrayList < T >();
   private boolean m_IsRunning = false;

   /** Creates a new instance of BatchProcessRunnable */
   public BatchProcessRunnable()
   {
   }

   public synchronized void addItem(T item)
   {
      m_UpdateItems.add(item);
   }
   
   public synchronized boolean hasItems()
   {
      return m_UpdateItems.size() > 0;
   }
   
   public synchronized T nextItem()
   {
      T retVal = null;
      
      if(m_UpdateItems.size() > 0)
      {
         retVal = m_UpdateItems.get(0);
         m_UpdateItems.remove(0);
      }
      
      return retVal;
      
   }
   
   protected synchronized void startRunning()
   {
      m_IsRunning = true;
   }
   
   protected synchronized void finishRunning()
   {
      m_IsRunning = false;
   }
   
   public synchronized boolean isRunning()
   {
      return m_IsRunning;
   }
   
   public void run()
   {
      startRunning();
      
      T nextIElement = nextItem();
      while(nextIElement != null)
      {
         execute(nextIElement);
         nextIElement = nextItem();
      }
      
      finishRunning();
   }
   
   protected abstract void execute(T param);
   
}
