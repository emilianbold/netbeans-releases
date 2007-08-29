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


package org.netbeans.modules.uml.ui.support.projecttreesupport;

import java.util.Comparator;

/**
 * The comparable class used to sort the project tree contents.
 *
 * @author Trey Spiva
 */
public class ProjectTreeComparable implements Comparator
{
   public final static int EQUAL_TO    = 0;
   public final static int LESS_THAN     = -1;
   public final static int GREATER_THAN  = 1;

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compare(Object o1, Object o2)
   {
      return compareTo(o1, o2);
   }
   
   public static int compareTo(Object o1, Object o2)
   {
      int retVal = EQUAL_TO;
            
      if((o1 instanceof ITreeItem) && (o2 instanceof ITreeItem))
      { 
         ITreeItem lhs = (ITreeItem)o1;
         ITreeItem rhs = (ITreeItem)o2;
         
         long lhsPriority = lhs.getSortPriority();
         long rhsPriority = rhs.getSortPriority();
         if(lhsPriority == rhsPriority)
         {
	    String lhsName;
	    String rhsName;
	    if (o1 instanceof ITreeDiagram && o2 instanceof ITreeDiagram) 
	    {
		ITreeDiagram diagram1 = (ITreeDiagram)o1;
		ITreeDiagram diagram2 = (ITreeDiagram)o2;
		lhsName = diagram1.getData().getDescription();
		rhsName = diagram2.getData().getDescription();		
	    } 
	    else 
	    {
	        
		lhsName = lhs.getDisplayedName().toLowerCase();
		rhsName = rhs.getDisplayedName().toLowerCase();
	    }

	    int result = lhsName.compareTo(rhsName);
	    
	    retVal = EQUAL_TO;
	    if(result > 0)
	    {
		retVal = GREATER_THAN;
	    }
	    else if(result < 0)
	    {
		retVal = LESS_THAN;
            }
         }
         else if(lhsPriority < rhsPriority)
         {
            retVal = LESS_THAN;
         }
         else
         {
            retVal = GREATER_THAN;
         }
      }
       
      // The NetBeans tree does not like it when two items are equal for some
      // reason. 
      if(retVal == EQUAL_TO && (! (o1 != null && o1.equals(o2))) )
      {
	  retVal = GREATER_THAN;
      }

      return retVal;
   }

}
