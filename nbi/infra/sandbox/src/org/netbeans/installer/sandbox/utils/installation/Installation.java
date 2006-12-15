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
 * $Id$
 */

package org.netbeans.installer.sandbox.utils.installation;

import java.io.File;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.sandbox.utils.installation.conditions.FileCondition;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public interface Installation {
    public InstallationFiles getInstallationFiles();
    
    public void setProgress(Progress progress);
    
    public void deleteInstallationFiles();
    
    public void saveInstallationFiles(File file) throws XMLException;
    
    public void loadInstallationFiles(File file) throws XMLException;
    
    public void extract(File dataFile, File location, boolean unpack, boolean useZipFile);
}
