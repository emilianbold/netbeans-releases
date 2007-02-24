/*
 * ReverseEngineerTask.java
 *
 * Created on September 27, 2006, 10:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.util.ITaskFinishListener;
import org.openide.util.NbBundle;

import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.util.AbstractNBTask;

/**
 *
 * @author IBM USER
 */
public class ReverseEngineerTask extends AbstractNBTask
{
    private IUMLParsingIntegrator parsingIntegrator;
    private INamespace nameSpace;
    private IStrings files;
    private boolean useFileChooser = false;
    private boolean useDiagramCreateWizard = false;
    private boolean displayOutputWindow = true;
    private boolean extractClasses = true;
    
    
    public ReverseEngineerTask(
        INamespace pNameSpace, 
        IStrings pFiles,
        boolean pUseFileChooser, 
        boolean pUseDiagramCreateWizard, 
        boolean pDisplayOutputWindow, 
        boolean pExtractClasses,
        ITaskFinishListener listener)
    {
        parsingIntegrator = new UMLParsingIntegrator();
        nameSpace = pNameSpace;
        files = pFiles;
        useFileChooser = pUseFileChooser;
        useDiagramCreateWizard = pUseDiagramCreateWizard;
        displayOutputWindow = pDisplayOutputWindow; 
        extractClasses = pExtractClasses;
        
        if (listener != null)
            addListener(listener);
    }

    
    protected void begin()
    {
        parsingIntegrator.setFiles(files);
        parsingIntegrator.setTaskSupervisor(this);
        
//        log("Reverse Engineering selected options:");
//        log("  Source project : " + getSourceProject());
//        log("  Target UML project : " + getTargetProject());
//        log("  Create new project? : " + isCreateNewProject());
//        log("  RE Project or subset sources: " + getREActionType());
//        log("");

        
        parsingIntegrator.reverseEngineer(
            nameSpace, 
            useFileChooser, 
            useDiagramCreateWizard, 
            displayOutputWindow, 
            extractClasses);
    }

    
    private final int SUBTASK_TOT = 4;
    
    protected void initTask()
    {
        setLogLevel(SUMMARY);
        setTaskName(getBundleMessage("IDS_RE_TITLE"));
        
        String prefVal = ProductHelper.getPreferenceManager()
            .getPreferenceValue("Default", "ReverseEngineering", "REShowOutput"); // NOI18N
        
        setDisplayOutput(prefVal.equals("PSK_YES"));
        
        progressContribs = new ProgressContributor[SUBTASK_TOT];
        int i = 0;
        
        progressContribs[i] = AggregateProgressFactory
            .createProgressContributor(
                "Parsing Elements (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
            
        progressContribs[++i] = AggregateProgressFactory
            .createProgressContributor(
                "Analyzing Atribute/Operation Types (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
            
        progressContribs[++i] = AggregateProgressFactory
            .createProgressContributor(
                "Resolving Attribute Types (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
            
        progressContribs[++i] = AggregateProgressFactory
            .createProgressContributor(
                "Integrating Elements (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
    }

    
    protected void finish()
    {
    }
    
    private String getBundleMessage(String key)
    {
        return NbBundle.getMessage(ReverseEngineerTask.class, key);
    }

    public IUMLParsingIntegrator getParsingIntegrator()
    {
        return parsingIntegrator;
    }

    public void setParsingIntegrator(IUMLParsingIntegrator parsingIntegrator)
    {
        this.parsingIntegrator = parsingIntegrator;
    }
}
