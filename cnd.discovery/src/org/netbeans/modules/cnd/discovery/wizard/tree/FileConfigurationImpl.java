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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;

/**
 *
 * @author Alexander Simon
 */
public class FileConfigurationImpl extends NodeConfigurationImpl implements FileConfiguration {
    private SourceFileProperties sourceFile;
    
    public FileConfigurationImpl(SourceFileProperties source) {
        sourceFile = source;
    }

    public String getCompilePath() {
        return sourceFile.getCompilePath();
    }

    public String getFilePath() {
        return sourceFile.getItemPath();
    }

    public String getFileName() {
        return sourceFile.getItemName();
    }

    public List<String> getUserInludePaths() {
        return sourceFile.getUserInludePaths();
    }

    public Map<String,String> getUserMacros() {
        return sourceFile.getUserMacros();
    }
    
}
