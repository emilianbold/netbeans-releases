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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import java.util.*;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class CollectionTranslator
			<SourceType, DestinationType>
{
    public static <Source, Result> ETList<Result> translate(
            Collection<Source> source)
    {
        if (source == null)
            return null;

        ETList<Result> dest = new ETArrayList<Result>();
        Iterator<Source> iter = source.iterator();
        while (iter.hasNext())
            dest.add((Result) iter.next());
        return dest;
    }
    
	public ETList<DestinationType> copyCollection(Collection<SourceType> source)
	{
        if (source == null)
            return null;

		ETList<DestinationType> dest = new ETArrayList<DestinationType>();
		Iterator<SourceType> iter = source.iterator();
		while (iter.hasNext()) 
		{
			dest.add((DestinationType) iter.next());		
		}
		return dest;
	}
	
	public void addToCollection(Collection<SourceType> source, 
                                Collection<DestinationType> destination)
	{		
		if (source != null && destination != null)
		{		
			Iterator<SourceType> iter = source.iterator();
			while (iter.hasNext()) 
			{
				destination.add((DestinationType) iter.next());		
			}
		}	
	}
	
	public void cleanCollection(Collection<SourceType> source)
	{		
		if (source != null)
		{		
			source.clear();
		}		
	}
/*	
	public DestinationType[] copyArray(SourceType[] arr) 
	{
		if (arr == null)
			return null;		
		DestinationType[] dest = new DestinationType[arr.length];
		System.arraycopy(arr, 0, dest, 0, arr.length);
		return dest;
	}		
*/	
}


