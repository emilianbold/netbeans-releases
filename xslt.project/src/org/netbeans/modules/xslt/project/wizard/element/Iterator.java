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
package org.netbeans.modules.xslt.project.wizard.element;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xslt.tmap.model.api.BooleanType;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentFactory;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
public final class Iterator implements TemplateWizard.Iterator {

  /**{@inheritDoc}*/
  public static Iterator createXsl() {
    return new Iterator();
  }

  /**{@inheritDoc}*/
  public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
    return Collections.singleton(createFile(wizard));
  }

  /**{@inheritDoc}*/
  public void initialize(TemplateWizard wizard) {
    myPanel = new PanelStartup<WizardDescriptor>(Templates.getProject(wizard), null);
  }

  /**{@inheritDoc}*/
  public void uninitialize(TemplateWizard wizard) {
    myPanel = null;
  }

  /**{@inheritDoc}*/
  public String name() {
    return i18n(Iterator.class, "LBL_Title"); // NOI18N
  }
  
  /**{@inheritDoc}*/
  public boolean hasNext() {
    return myPanel.getNext() != null;
  }
  
  /**{@inheritDoc}*/
  public boolean hasPrevious() {
    return myPanel.getPrevious() != null;
  }
  
  /**{@inheritDoc}*/
  public void nextPanel() {
    myPanel = myPanel.getNext();
  }
  
  /**{@inheritDoc}*/
  public void previousPanel() {
    myPanel = myPanel.getPrevious();
  }

  /**{@inheritDoc}*/
  public WizardDescriptor.Panel<WizardDescriptor> current() {
    return myPanel;
  }
  
  /**{@inheritDoc}*/
  public void addChangeListener(ChangeListener listener) {}

  /**{@inheritDoc}*/
  public void removeChangeListener(ChangeListener listener) {}

  private boolean isRequestTransform(TemplateWizard wizard) {
    String inputFileStr = (String) wizard.getProperty(Panel.INPUT_FILE);
    return !(inputFileStr == null || "".equals(inputFileStr)
            || ".xsl".equals(inputFileStr));

  }
  
  private DataObject createFile(TemplateWizard wizard) throws IOException {
    FileObject file = null;
    Project project = Templates.getProject(wizard);
    String choice = (String) wizard.getProperty(Panel.CHOICE);
    
//    TMapModelFactory tMapModelFactory = Lookup.getDefault().lookup(TMapModelFactory.class);
//    assert tMapModelFactory != null;
    FileObject tMapFo = org.netbeans.modules.xslt.tmap.util.Util.getTMapFo(project);
    if (tMapFo == null) {
        tMapFo = org.netbeans.modules.xslt.tmap.util.Util.createDefaultTransformmap(project);
    }
    
    TMapModel tMapModel = 
            org.netbeans.modules.xslt.tmap.util.Util.getTMapModel(tMapFo);

    configureTMapModel(tMapModel, wizard);
    ProjectManager.getDefault().saveProject(project);
    
    if (isRequestTransform(wizard)) {
        file = createXslFile(
                project, (String) wizard.getProperty(Panel.INPUT_FILE));
    }

    if (Panel.CHOICE_FILTER_REQUEST_REPLY.equals(choice)) {
      file = createXslFile(
        project, (String) wizard.getProperty(Panel.OUTPUT_FILE));
    }


    return DataObject.find(file);
  }

  // TODO m
  private void configureTMapModel(TMapModel tMapModel, TemplateWizard wizard) {
      assert tMapModel != null && wizard != null;
      if (! TMapModel.State.VALID.equals(tMapModel.getState())) {
          return;
      }
      
      String choice = (String) wizard.getProperty(Panel.CHOICE);      
      TMapComponentFactory componentFactory = tMapModel.getFactory();

      try {
          tMapModel.startTransaction();
          
          org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = 
                  setOperation(tMapModel, wizard, componentFactory);
          
          if (tMapOp != null 
                  && (Panel.CHOICE_FILTER_REQUEST_REPLY.equals(choice) 
                  || Panel.CHOICE_FILTER_ONE_WAY.equals(choice))) 
          {
// TODO m | r               
//              setInvokes(tMapOp, wizard, componentFactory,
//                      Panel.CHOICE_FILTER_REQUEST_REPLY.equals(choice));
          }
      } finally {
          tMapModel.endTransaction();
      }
  }

  private org.netbeans.modules.xslt.tmap.model.api.Operation setOperation(
          TMapModel tMapModel,
          TemplateWizard wizard, 
          TMapComponentFactory componentFactory) 
  {
      assert tMapModel != null && wizard != null && componentFactory != null;
      
      String inputFileStr = (String) wizard.getProperty(Panel.INPUT_FILE);
      Operation wizardInputOperation =
              (Operation) wizard.getProperty(Panel.INPUT_OPERATION);
      Panel.PartnerRolePort wizardInputPartnerRolePort =
              (Panel.PartnerRolePort) wizard.getProperty(Panel.INPUT_PARTNER_ROLE_PORT);
      
      PartnerLinkType wizardInPlt = wizardInputPartnerRolePort == null ? null : wizardInputPartnerRolePort.getPartnerLinkType();
      Role wizardInRole = wizardInputPartnerRolePort == null ? null : wizardInputPartnerRolePort.getRole();
      
      if (wizardInPlt == null || wizardInRole == null || wizardInputOperation == null) {
          return null;
      }
      
      TransformMap root = tMapModel.getTransformMap();
      // TODO m
      if (root == null) {
          root = componentFactory.createTransformMap();
          tMapModel.addChildComponent(null, root, -1);
      }
      
      if (root == null) {
          return null;
      }
      
      Service reqService = null;
      Collection<Service> services = root.getServices();
      // search for approprite service
      if (services != null) {
          for (Service serviceElem : services) {
              WSDLReference<PartnerLinkType> pltRef = serviceElem.getPartnerLinkType();
              PartnerLinkType plt = pltRef == null ? null : pltRef.get();
              
              WSDLReference<Role> roleRef = serviceElem.getRole();
              Role role = roleRef == null ? null : roleRef.get();
              
              if (plt != null && role != null
                      && plt.equals(wizardInPlt) && role.equals(wizardInRole)) {
                  reqService = serviceElem;
                  break;
              }
          }
      }
      
      if (reqService == null) {
          reqService = componentFactory.createService();
          reqService.setPartnerLinkType(reqService.createWSDLReference(wizardInPlt, PartnerLinkType.class));
          reqService.setRole(reqService.createWSDLReference(wizardInRole, Role.class));
          
          root.addService(reqService);
      }
      
      org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp =
              componentFactory.createOperation();
      tMapOp.setOperation(tMapOp.createWSDLReference(wizardInputOperation, Operation.class));

// TODO r
//      if (isRequestTransform(wizard)) {
//          if (inputFileStr != null && ! "".equals(inputFileStr)) {
//              tMapOp.setFile(inputFileStr);
//          }
//          tMapOp.setTransformJbi(BooleanType.parseBooleanType(wizardInputIsTransformJBI));
//      }
      
     reqService.addOperation(tMapOp);      
     
     
     
      Transform transform =
              componentFactory.createTransform();
      if (inputFileStr != null && ! "".equals(inputFileStr)) {
          transform.setFile(inputFileStr);
      }
      
      // TODO m
      transform.setSource("sourceVal");
      transform.setResult("resultVal");
      
      tMapOp.addTransform(transform);

     return tMapOp;
}
// TODO m | r  
//  private Invokes setInvokes(
//          org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp,
//          TemplateWizard wizard, 
//          TMapComponentFactory componentFactory,
//          boolean isFilterRequestReply) 
//  {
//      assert tMapOp != null && wizard != null && componentFactory != null;
//      Invokes invokes = null;
//      String outputFileStr = (String) wizard.getProperty(Panel.OUTPUT_FILE);
//      Operation wizardOutputOperation =
//              (Operation) wizard.getProperty(Panel.OUTPUT_OPERATION);
//      Panel.PartnerRolePort wizardOutputPartnerRolePort = 
//              (Panel.PartnerRolePort) wizard.getProperty(Panel.OUTPUT_PARTNER_ROLE_PORT);
//      Boolean wizardOutputIsTransformJBI = ((Boolean) wizard.getProperty(Panel.OUTPUT_TRANSFORM_JBI));
//      
//      PartnerLinkType wizardOutPlt = wizardOutputPartnerRolePort == null ? null : wizardOutputPartnerRolePort.getPartnerLinkType();
//      Role wizardOutRole = wizardOutputPartnerRolePort == null ? null : wizardOutputPartnerRolePort.getRole();
//      
//      if (wizardOutPlt != null 
//              && wizardOutRole != null 
//              && wizardOutputOperation != null) 
//      {
//          invokes = componentFactory.createInvokes();
//          invokes.setPartnerLinkType(invokes.createWSDLReference(wizardOutPlt, PartnerLinkType.class));
//          invokes.setRole(invokes.createWSDLReference(wizardOutRole, Role.class));
//          invokes.setOperation(invokes.createWSDLReference(wizardOutputOperation, Operation.class));
//          
//          if (isFilterRequestReply) {
//              if (outputFileStr != null && ! "".equals(outputFileStr)) {
//                  invokes.setFile(outputFileStr);
//              }
//              invokes.setTransformJbi(BooleanType.parseBooleanType(wizardOutputIsTransformJBI));
//          }
//      }
//      
//      if (invokes != null) {
//        tMapOp.addInvokes(invokes);
//      }
//      
//      return invokes;
//  }
  
  private FileObject createXslFile(
    Project project,
    String file) throws IOException
  {
    if (file == null || "".equals(file)) {
        return null;
    }

    int extIndex = file.lastIndexOf(XSL)-1; 
    if (extIndex <= 0) {
        return null;
    }
    
    file = file.substring(0, extIndex);
      
    if ("".equals(file)) {
        return null;
    }
    
    return Util.copyFile(
            Util.getSrcFolder(project), 
            TEMPLATES_PATH, XSLT_SERVICE,
            file, XSL);
  }

  
  private static String TEMPLATES_PATH = "Templates/SOA/"; // NOI18N
  private static String XSLT_SERVICE = "xslt.service"; // NOI18N
  private static String XSL = "xsl"; // NOI18N
  private Panel<WizardDescriptor> myPanel;
}
