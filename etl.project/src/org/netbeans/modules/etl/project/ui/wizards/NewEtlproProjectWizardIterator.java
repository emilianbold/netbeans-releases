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

package org.netbeans.modules.etl.project.ui.wizards;

import java.io.File;
import java.io.IOException;

import net.java.hulp.i18n.Logger;

import org.netbeans.modules.compapp.projects.base.ui.wizards.*;
import org.netbeans.modules.etl.project.EtlproProjectGenerator;
import org.netbeans.modules.etl.project.Localizer;
import org.netbeans.modules.etl.project.LogUtil;

public class NewEtlproProjectWizardIterator extends NewIcanproProjectWizardIterator{
    
    private static final long serialVersionUID = 1L;
    private static transient final Logger mLogger = LogUtil.getLogger(NewEtlproProjectWizardIterator.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    protected String getDefaultName() {
        String nbBundle1 = mLoc.t("PRSR001: DataIntegratorApp{0}");
        return Localizer.parse(nbBundle1);//NOI18N        
    }

    protected void createProject(File dirF, String name, String j2eeLevel) throws IOException {
        EtlproProjectGenerator.createProject(dirF, name, j2eeLevel);
    }
}
