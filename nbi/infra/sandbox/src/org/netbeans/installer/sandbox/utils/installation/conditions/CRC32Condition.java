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

package org.netbeans.installer.sandbox.utils.installation.conditions;

import java.io.IOException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.sandbox.utils.installation.InstallationFileObject;

/**
 *
 * @author Dmitry Lipin
 */
public class CRC32Condition implements FileCondition {    
    
    public boolean accept(InstallationFileObject fo) {
        boolean result = false;
        try {
            result =  (fo.getCRC32() == FileUtils.getFileCRC32String(fo.getFile()));
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        } finally {
            return result;
        }
    }
    public String getName() {
        return "CRC32";
    }
}
