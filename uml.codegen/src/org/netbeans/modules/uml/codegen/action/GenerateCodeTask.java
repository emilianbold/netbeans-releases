/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.codegen.action;

import java.util.HashMap;
import java.util.Properties;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;

import org.netbeans.modules.uml.core.coreapplication.ICodeGenerator;
import org.netbeans.modules.uml.core.coreapplication.ICodeGeneratorFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.util.AbstractNBTask;


public class GenerateCodeTask extends AbstractNBTask
{
    private ETList<IElement> elements;
    private String projectName;
    private String sourceFolderName;
    private final static int SUBTASK_TOT = 1;
    private boolean backup = true;
    private boolean generateMarkers = true;
    private boolean addMarkers = false;
    private boolean showGCDialog = true;
    
    //Kris Richards - this is no longer an option. Set to default value.
    public final static String ATTR_PREFIX = "m"; // NOI18N
        
    public GenerateCodeTask(
        HashMap settings,
        ETList<IElement> selElements, 
        String umlProjectName, 
        String destFolderName,
        boolean backupFiles,
        boolean generateMarkers,
        boolean addMarkers,
        boolean showGCDialog)
    {
        super(settings);
        elements = selElements;
        projectName = umlProjectName;
        sourceFolderName = destFolderName;
        backup = backupFiles;
        this.generateMarkers = generateMarkers;
        this.addMarkers = addMarkers;
        this.showGCDialog = showGCDialog;
    }
    
    
    protected void initTask()
    {
        setLogLevel(TERSE);
        setTaskName(getBundleMessage("LBL_GenerateCodeDialogTitle")); // NOI18N
        
        //Kris Richards - preference now hidden - always yes.
        
        setDisplayOutput(true);

        progressContribs = new ProgressContributor[SUBTASK_TOT];
        int i = 0;
        
        progressContribs[i] = AggregateProgressFactory
            .createProgressContributor(getBundleMessage("MSG_Processing")); // NOI18N
    }


    protected void begin()
    {
        exportCode();
    }

    protected void finish()
    {
    }
    
    /**
     * @param elements The collection of elements to generate for
     *
     */
    private void exportCode()
    {
        if (elements == null || elements.size() == 0)
        {
            return;
        }
        ICodeGeneratorFactory factory = (ICodeGeneratorFactory) Lookup.getDefault()
            .lookup(ICodeGeneratorFactory.class);

        ICodeGenerator cg = factory.getCodeGenerator("Java"); // NOI18N
        Properties genProps = new Properties();
        genProps.setProperty("generateMarkers", new Boolean(generateMarkers).toString()); // NOI18N
        genProps.setProperty("addMarkers", new Boolean(addMarkers).toString()); // NOI18N
        genProps.setProperty("backup", new Boolean(backup).toString()); // NOI18N
        cg.generate(this, elements, sourceFolderName, genProps);
    }
         

    private static String getBundleMessage(String key)
    {
        return NbBundle.getMessage(GenerateCodeTask.class, key);
    }


}
