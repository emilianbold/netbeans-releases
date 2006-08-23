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

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.util.Mutex;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.api.queries.SharabilityQuery;

/**
 * SharabilityQueryImplementation for j2seproject with multiple sources
 */
public class MakeSharabilityQuery implements SharabilityQueryImplementation {
    private File baseDirFile;
    private String baseDir;
    private int baseDirLength;

    MakeSharabilityQuery (File baseDirFile) {
        this.baseDirFile = baseDirFile;
        this.baseDir = baseDirFile.getPath();
	this.baseDirLength = this.baseDir.length();
    }


    /**
     * Check whether a file or directory should be shared.
     * If it is, it ought to be committed to a VCS if the user is using one.
     * If it is not, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * @param file a file to check for sharability (may or may not yet exist)
     * @return one of {@link org.netbeans.api.queries.SharabilityQuery}'s constants
     */
    public int getSharability(final File file) {
        Integer ret = (Integer) ProjectManager.mutex().readAccess( new Mutex.Action() {
            public Object run() {
                synchronized (MakeSharabilityQuery.this) {
		    boolean sub = file.getPath().startsWith(baseDir);
		    if (!sub)
			return new Integer(SharabilityQuery.UNKNOWN);
		    if (file.getPath().equals(baseDir))
			return new Integer(SharabilityQuery.MIXED);
		    if (file.getPath().length() <= baseDirLength + 1)
			return new Integer(SharabilityQuery.UNKNOWN);
		    String subString = file.getPath().substring(baseDirLength + 1);
		    if (subString.equals("nbproject")) // NOI18N
			return new Integer(SharabilityQuery.MIXED);
		    else if (subString.equals("Makefile")) // NOI18N
			return new Integer(SharabilityQuery.SHARABLE);
		    else if (subString.equals("nbproject" + File.separator + "configurations.xml")) // NOI18N
			return new Integer(SharabilityQuery.SHARABLE);
		    else if (subString.equals("nbproject" + File.separator + "private")) // NOI18N
			return new Integer(SharabilityQuery.NOT_SHARABLE);
		    else if (subString.equals("nbproject" + File.separator + "project.properties")) // NOI18N
			return new Integer(SharabilityQuery.SHARABLE);
		    else if (subString.equals("nbproject" + File.separator + "project.xml")) // NOI18N
			return new Integer(SharabilityQuery.SHARABLE);
		    else if (subString.startsWith("nbproject" + File.separator + "Makefile-")) // NOI18N
			return new Integer(SharabilityQuery.SHARABLE);
                    return new Integer(SharabilityQuery.UNKNOWN);
                }
            }
        });
        return ret.intValue();
    }
}
