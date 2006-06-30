/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
