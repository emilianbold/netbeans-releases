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
    
    //Kris Richards - this is no longer an option. Set to default value.
    public final static String ATTR_PREFIX = "m"; // NOI18N
        
    public GenerateCodeTask(
        HashMap settings,
        ETList<IElement> selElements, 
        String umlProjectName, 
        String destFolderName,
        boolean backupFiles,
        boolean generateMarkers,
	boolean addMarkers)
    {
        super(settings);
        elements = selElements;
        projectName = umlProjectName;
        sourceFolderName = destFolderName;
        backup = backupFiles;
	this.generateMarkers = generateMarkers;
	this.addMarkers = addMarkers;
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
            return;

	ICodeGeneratorFactory factory = 
	    (ICodeGeneratorFactory)Lookup.getDefault()
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
