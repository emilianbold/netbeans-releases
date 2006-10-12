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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author Andrei Badea
 */
public class PersistenceGeneratorImpl implements PersistenceGenerator {
    
    public String generateEntityName(String className) {
        return className;
    }

    public void init(WizardDescriptor wiz) {
    }

    public void generateBeans(final ProgressPanel progressPanel, final RelatedCMPHelper helper, final FileObject dbschemaFile, final ProgressHandle handle, boolean justTesting) throws IOException {
    }

    public void uninit() {
    }

    public boolean isCMP() {
        return false;
    }

    public Set createdObjects() {
        return Collections.emptySet();
    }

    public String getFQClassName(String tableName) {
        return null;
    }
}
