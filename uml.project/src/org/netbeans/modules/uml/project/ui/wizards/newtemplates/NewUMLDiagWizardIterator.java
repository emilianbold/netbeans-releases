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

package org.netbeans.modules.uml.project.ui.wizards.newtemplates;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.core.UMLSettings;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.project.UMLProjectGenerator;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.ui.controls.newdialog.AddElementWizardPanel1;
import org.netbeans.modules.uml.ui.controls.newdialog.AddPackageWizardPanel1;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogElementDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogPackageDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogResultProcessor;
import org.netbeans.modules.uml.ui.controls.newdialog.INewUMLFileTemplates;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogElementDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogPackageDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogResultProcessor;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogUtilities;
import org.netbeans.modules.uml.ui.controls.newdialog.NewUMLDiagWizardPanel1;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public final class NewUMLDiagWizardIterator
      implements TemplateWizard.Iterator, INewUMLFileTemplates
{
   private int index;
   private TemplateWizard wizard;
   private WizardDescriptor.Panel[] panels;
   private int templateType = NEW_DIAGRAM;
   
   private NewUMLDiagWizardIterator(int type)
   {
      this.templateType = type;
   }
   
   public static NewUMLDiagWizardIterator createDiagramIterator()
   {
      return new NewUMLDiagWizardIterator(NEW_DIAGRAM);
   }
   
   public static NewUMLDiagWizardIterator createPackageIterator()
   {
      return new NewUMLDiagWizardIterator(NEW_PACKAGE);
   }
   
   public static NewUMLDiagWizardIterator createElementIterator()
   {
      return new NewUMLDiagWizardIterator(NEW_ELEMENT);
   }
   
   public void initialize(TemplateWizard wiz)
   {
      this.wizard = wiz;
      createPanels();
      Project currentProject = Templates.getProject(wiz); 
      if (currentProject == null)
      {
         throw new IllegalArgumentException();
      }
      
      UMLProjectHelper prjHelper = (UMLProjectHelper)
            currentProject.getLookup().lookup(UMLProjectHelper.class);
      
      if ( prjHelper != null )
      {
         IProject umlProject = prjHelper.getProject();
         
         switch (this.templateType)
         {
         case NEW_PACKAGE:
            INewDialogPackageDetails pDetails = new NewDialogPackageDetails();
            if (umlProject instanceof INamespace)
            {
               pDetails.setNamespace( (INamespace) umlProject );
            }
            
            pDetails.setAllowFromRESelection( false );
            wiz.putProperty(PACKAGE_DETAILS, pDetails);
            break;
            
         case NEW_ELEMENT:
            INewDialogElementDetails eDetails = new NewDialogElementDetails();
            if (umlProject instanceof INamespace)
            {
               eDetails.setNamespace( (INamespace) umlProject );
            }
            
            wiz.putProperty(ELEMENT_DETAILS, eDetails);
            break;
            
         case NEW_DIAGRAM:
         default:
            INewDialogDiagramDetails details = new NewDialogDiagramDetails();
            if (umlProject instanceof INamespace)
            {
               details.setNamespace((INamespace) umlProject);
            }
            
            details.setDiagramKind(IDiagramKind.DK_UNKNOWN);
            details.setAvailableDiagramKinds(IDiagramKind.DK_ALL);
            wiz.putProperty(DIAGRAM_DETAILS, details);
            wiz.putProperty(PROP_PROJECT, currentProject);
            break;
         }
      }
   }
   
   public void uninitialize(TemplateWizard wiz)
   {
      panels = null;
      wizard = null;
   }
   
   /**
    * Initialize panels representing individual wizard's steps and sets
    * various properties for them influencing wizard appearance.
    */
   private WizardDescriptor.Panel[] createPanels()
   {
      if (panels == null)
      {
         switch (this.templateType)
         {
         case NEW_PACKAGE:
            panels = new WizardDescriptor.Panel[] {
               new AddPackageWizardPanel1()
            };
            break;
            
         case NEW_ELEMENT:
            panels = new WizardDescriptor.Panel[] {
               new AddElementWizardPanel1()
            };
            break;
            
         case NEW_DIAGRAM:
         default:  //NEW_DIAGRAM
            panels = new WizardDescriptor.Panel[] {
               new NewUMLDiagWizardPanel1()
            };
            break;
         }
         
         String[] steps = createSteps();
         for (int i = 0; i < panels.length; i++)
         {
            Component c = panels[i].getComponent();
            if (steps[i] == null)
            {
               steps[i] = c.getName();
            }
            
            if (c instanceof JComponent)
            { // assume Swing components
               JComponent jc = (JComponent) c;
               // Sets step number of a component
               jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
               // Sets steps names for a panel
               jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
               // Turn on subtitle creation on each step
               jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
            }
         }
      }
      
      return panels;
   }
   
   private String[] createSteps()
   {
      String[] beforeSteps = null;
      Object prop = wizard.getProperty("WizardPanel_contentData"); // NOI18N
      if (prop != null && prop instanceof String[])
      {
         beforeSteps = (String[]) prop;
      }
      
      if (beforeSteps == null)
      {
         beforeSteps = new String[0];
      }
      
      String[] res = new String[(beforeSteps.length - 1) + panels.length];
      for (int i = 0; i < res.length; i++)
      {
         if (i < (beforeSteps.length - 1))
         {
            res[i] = beforeSteps[i];
         }
         
         else
         {
            res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
         }
      }
      return res;
   }
   
   public Set instantiate(TemplateWizard wiz) throws IOException
   {
      Set resultSet = new HashSet();

      switch (this.templateType)
      {
      case NEW_PACKAGE:
         INewDialogPackageDetails pDetails = (INewDialogPackageDetails)
               wiz.getProperty(this.PACKAGE_DETAILS);
         if (pDetails != null)
         {
            INewDialogResultProcessor processor = new NewDialogResultProcessor();
            processor.handleResult( pDetails );
         }
         break;
         
      case NEW_ELEMENT:
         INewDialogElementDetails eDetails = (INewDialogElementDetails)
               wiz.getProperty(this.ELEMENT_DETAILS);
         if (eDetails != null)
         {
            INewDialogResultProcessor processor = new NewDialogResultProcessor();
            processor.handleResult( eDetails );
         }
         break;
         
      case NEW_DIAGRAM:
      default:
         INewDialogDiagramDetails dDetails = (INewDialogDiagramDetails)
               wiz.getProperty(DIAGRAM_DETAILS);
         
         if (dDetails != null)
         {
//             UMLSettings.getDefault().incrementDiagramCount(
//                 dDetails.getName(), dDetails.getDiagramKind());
             
             UMLProjectGenerator.createNewDiagram(dDetails.getNamespace(), 
                 dDetails.getDiagramKind(), dDetails.getName());
         }
         break;
      }
      
      Project project = (Project)wiz.getProperty(this.PROP_PROJECT);
      FileObject dir = project.getProjectDirectory();
      resultSet.add(dir);
      return resultSet;
   }
   
   public WizardDescriptor.Panel current()
   {
      return panels[index];
   }
   
   public String name()
   {
      return  NbBundle.getMessage(
            NewUMLDiagWizardIterator.class,
            "NEWWIZARD_TITLE_INDEX", // NOI18N
            String.valueOf(index + 1),
            String.valueOf(panels.length) );
   }
   
   public boolean hasNext()
   {
      return index < panels.length - 1;
   }
   
   public boolean hasPrevious()
   {
      return index > 0;
   }
   
   public void nextPanel()
   {
      if (!hasNext())
      {
         throw new NoSuchElementException();
      }
      index++;
   }
   
   public void previousPanel()
   {
      if (!hasPrevious())
      {
         throw new NoSuchElementException();
      }
      index--;
   }
   
   // If nothing unusual changes in the middle of the wizard, simply:
   public void addChangeListener(ChangeListener l)
   {}
   public void removeChangeListener(ChangeListener l)
   {}
}
