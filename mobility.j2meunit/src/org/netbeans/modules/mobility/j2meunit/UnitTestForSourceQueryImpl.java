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

/*
 * UnitTestForSourceQueryImpl.java
 *
 * Created on April 19, 2006, 7:02 PM
 *
 */
package org.netbeans.modules.mobility.j2meunit;

import java.net.URL;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bohemius
 */
public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {
    
    final private AntProjectHelper myHelper;
    
    public UnitTestForSourceQueryImpl(AntProjectHelper aph) {
        this.myHelper=aph;
    }
    
    public URL[] findUnitTests(@SuppressWarnings("unused")
	final FileObject source) {
        //we have only one source root in J2ME projects, it is same for both sources and tests
        try {
            final String sourceRootPath=myHelper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
            if (sourceRootPath == null) return null;
            final FileObject sourceRoot=this.myHelper.resolveFileObject(sourceRootPath);
            if (sourceRoot == null) return null;
            return new URL[] {sourceRoot.getURL()};
        } catch (Exception e) {
            return null;
        }
    }
    
    public URL[] findSources(final FileObject unitTest) {
        //we have only one source root in J2ME projects, it is same for both sources and tests
        return findUnitTests(unitTest);
    }
}
