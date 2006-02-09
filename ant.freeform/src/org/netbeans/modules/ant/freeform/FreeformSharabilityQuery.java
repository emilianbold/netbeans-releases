/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.queries.SharabilityQueryImplementation;

/**
 *
 * @author Jan Lahoda
 */
public class FreeformSharabilityQuery implements SharabilityQueryImplementation {
    
    private String nbproject;
    private String nbprojectPrivate;
    
    /** Creates a new instance of FreeformSharabilityQuery */
    public FreeformSharabilityQuery(AntProjectHelper helper) {
	nbproject = helper.resolveFile("nbproject").getAbsolutePath();
	nbprojectPrivate = helper.resolveFile("nbproject/private").getAbsolutePath();
    }

    public int getSharability(File file) {
	String absolutePath = file.getAbsolutePath();
	
	if (absolutePath.equals(nbproject)) {
	    return SharabilityQuery.MIXED;
	}
	
	if (absolutePath.startsWith(nbproject)) {
	    return absolutePath.startsWith(nbprojectPrivate) ? SharabilityQuery.NOT_SHARABLE : SharabilityQuery.SHARABLE;
	}
	
	return SharabilityQuery.UNKNOWN;
    }
    
}
