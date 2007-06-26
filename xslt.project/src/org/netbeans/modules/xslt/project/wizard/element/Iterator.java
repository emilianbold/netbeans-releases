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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentFactory;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
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

    private Service createTMapService(TMapComponentFactory componentFactory,
            TMapModel tMapModel, 
            PartnerLinkType wizardInPlt, 
            Role wizardInRole) 
    {
        assert componentFactory != null && tMapModel != null 
                && wizardInPlt != null && wizardInRole != null;
        Service tMapService = null;

        TransformMap root = tMapModel.getTransformMap();

        // TODO m
        if (root == null) {
            root = componentFactory.createTransformMap();
            tMapModel.addChildComponent(null, root, -1);
        }

        if (root == null) {
            return null;
        }

        tMapService = componentFactory.createService();
        tMapService.setPartnerLinkType(tMapService.createWSDLReference(wizardInPlt, PartnerLinkType.class));
        tMapService.setRole(tMapService.createWSDLReference(wizardInRole, Role.class));

        root.addService(tMapService);
          
        return tMapService;
    }

    private Service getTMapService(
            TMapModel model, 
            PartnerLinkType wizardInPlt, 
            Role wizardInRole) 
    {
        if (model == null || wizardInPlt == null || wizardInRole == null) 
        {
            return null;
        }
        
        Service service = null;
        TransformMap root = model.getTransformMap();
        if (root == null) {
            return service;
        }
        
        List<Service> services = root.getServices();
        if (services == null || services.size() < 1) {
            return service;
        }
        
        for (Service serviceElem : services) {
            WSDLReference<PartnerLinkType> pltRef = serviceElem.getPartnerLinkType();
            WSDLReference<Role> roleRef = serviceElem.getRole();
            
            if (roleRef != null && pltRef != null 
                    && wizardInPlt.equals(pltRef.get()) 
                    && wizardInRole.equals(roleRef.get())) 
            {
                service = serviceElem;
                break;
            }
        }
        
        return service;
    }

    private org.netbeans.modules.xslt.tmap.model.api.Operation getTMapOperation(
            Service tMapService, 
            Operation wizardInputOperation) 
    {
        org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = null;
        
        if (tMapService == null) {
            return tMapOp;
        }
        
        List<org.netbeans.modules.xslt.tmap.model.api.Operation> operations 
                = tMapService.getOperations();
        if (operations == null || operations.size() < 1) {
            return tMapOp;
        }
        
        for (org.netbeans.modules.xslt.tmap.model.api.Operation operationElem : operations) {
            Reference<Operation> opRef = operationElem.getOperation();
            
            if (opRef != null && wizardInputOperation.equals(opRef.get())) {
                tMapOp = operationElem;
                break;
            }
        }

        return tMapOp;
    }

    private org.netbeans.modules.xslt.tmap.model.api.Operation getTMapOperation(
            TMapModel model, 
            PartnerLinkType wizardInPlt, 
            Role wizardInRole, 
            Operation wizardInputOperation) 
    {
        return getTMapOperation(getTMapService(model, wizardInPlt, wizardInRole), wizardInputOperation);
    }

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
//System.out.println("choice panel is : "+choice);              
              
              setAdditionalTransformation(tMapOp, wizard, componentFactory,
                      Panel.CHOICE_FILTER_REQUEST_REPLY.equals(choice));
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
      
      Service tMapService = getTMapService(tMapModel, wizardInPlt, wizardInRole);
      if (tMapService == null) {
          tMapService = createTMapService(componentFactory, 
                  tMapModel, wizardInPlt, wizardInRole);
      }

      // TODO m
      if (tMapService == null) {
          return null;
      }
      
      org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = null;
      tMapOp = getTMapOperation(tMapService, wizardInputOperation); 
      
      if (tMapOp == null) {
          tMapOp =
                  componentFactory.createOperation();
          tMapOp.setOperation(tMapOp.createWSDLReference(wizardInputOperation, Operation.class));

          tMapService.addOperation(tMapOp);  
          tMapOp.setInputVariable(
                  getVariableName(INPUT_OPERATION_VARIABLE_PREFIX,1));
          
          tMapOp.setOutputVariable(
                  getVariableName(OUTPUT_OPERATION_VARIABLE_PREFIX,1)); 
      }
     
      Transform transform = createTransform(
              componentFactory, 
              inputFileStr, 
              wizardInputOperation, 
              tMapOp);
      
      tMapOp.addTransform(transform);

     return tMapOp;
  }
  
  private Transform createTransform(
          TMapComponentFactory componentFactory,
          String inputFileStr,
          Operation wizardOperation,
          VariableDeclarator variableHolder) 
  {
      Transform transform =
              componentFactory.createTransform();
      if (inputFileStr != null && ! "".equals(inputFileStr)) {
          transform.setFile(inputFileStr);
      }
      
      // TODO m
      String sourcePartName = getFirstPartName(wizardOperation.getInput());
      transform.setSource(
              getTMapVarRef(variableHolder.getInputVariable(), sourcePartName));

      String resultPartName = getFirstPartName(wizardOperation.getOutput());
      transform.setResult(
              getTMapVarRef(variableHolder.getOutputVariable(), resultPartName));
      return transform;
  }
  
  private String  getTMapVarRef(String varName, String partName) {
      if (partName == null || varName == null) {
          return null;
      }
      return varName+"."+partName;
  }
  
  private String getFirstPartName(OperationParameter opParam) {
      String partName = null;
      if (opParam == null) {
          return partName;
      }
      
      NamedComponentReference<Message> messageRef = opParam.getMessage();
      Message message = messageRef == null ? null : messageRef.get();
      
      Collection<Part> parts = null;
      if (message != null) {
          parts = message.getParts();
      }
      
      Part part = null;
      if (parts != null && parts.size() > 0) {
          java.util.Iterator<Part> partIter = parts.iterator();
          part = partIter.next();
      }
      
      if (part != null) {
          partName = part.getName();
      }
      
      return partName;
  }
  
// TODO m | r
    private void setAdditionalTransformation(
            org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp,
            TemplateWizard wizard, 
            TMapComponentFactory componentFactory, 
            boolean isFilterRequestReply) 
    {
        assert tMapOp != null && wizard != null && componentFactory != null;
        Invokes invokes = null;
        Transform transform = null;
        String outputFileStr = (String) wizard.getProperty(Panel.OUTPUT_FILE);
        Operation wizardOutputOperation = 
                (Operation) wizard.getProperty(Panel.OUTPUT_OPERATION);
        Panel.PartnerRolePort wizardOutputPartnerRolePort = 
                (Panel.PartnerRolePort) wizard.getProperty(Panel.OUTPUT_PARTNER_ROLE_PORT);

        PartnerLinkType wizardOutPlt = wizardOutputPartnerRolePort == null 
                ? null 
                : wizardOutputPartnerRolePort.getPartnerLinkType();
        Role wizardOutRole = wizardOutputPartnerRolePort == null 
                ? null 
                : wizardOutputPartnerRolePort.getRole();

        if (wizardOutPlt != null && wizardOutRole != null && wizardOutputOperation != null) {
            invokes = componentFactory.createInvokes();
            invokes.setPartnerLinkType(invokes.
                    createWSDLReference(wizardOutPlt, PartnerLinkType.class));
            invokes.setRole(invokes.
                    createWSDLReference(wizardOutRole, Role.class));
            invokes.setOperation(invokes.
                    createWSDLReference(wizardOutputOperation, Operation.class));

            // TODO m
            invokes.setInputVariable(
                  getVariableName(INPUT_INVOKE_VARIABLE_PREFIX, 
                  getVariableNumber(tMapOp, INPUT_INVOKE_VARIABLE_PREFIX, 1)));
            invokes.setOutputVariable(
                  getVariableName(OUTPUT_INVOKE_VARIABLE_PREFIX, 
                  getVariableNumber(tMapOp, OUTPUT_INVOKE_VARIABLE_PREFIX, 1)));


            if (isFilterRequestReply) {
                transform = createTransform(
                        componentFactory, 
                        outputFileStr, 
                        wizardOutputOperation, 
                        invokes);
            }
        }

        if (invokes != null) {
            tMapOp.addInvokes(invokes);
        }
        
        if (transform != null) {
            tMapOp.addTransform(transform);
        }
    }

// TODO m | r
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

  private int getVariableNumber(
          org.netbeans.modules.xslt.tmap.model.api.Operation operation, 
          String varNamePrefix,
          int startNumber) 
  {
      if (operation == null || varNamePrefix == null) {
          return startNumber;
      }
      
      int count = startNumber;
      List<String> varNames = new ArrayList<String>();
      
      String tmpCurVar = operation.getInputVariable();
      if (tmpCurVar != null) {
        varNames.add(tmpCurVar);
      }
      tmpCurVar = operation.getOutputVariable();
      if (tmpCurVar != null) {
        varNames.add(tmpCurVar);
      }
      List<Invokes> invokess = operation.getInvokess();
      if (invokess != null && invokess.size() > 0) {
          for (Invokes elem : invokess) {
              tmpCurVar = elem.getInputVariable();
              if (tmpCurVar != null) {
                varNames.add(tmpCurVar);
              }
              tmpCurVar = elem.getOutputVariable();
              if (tmpCurVar != null) {
                varNames.add(tmpCurVar);
              }
          }
      }
          
      
      while (true) {
          if (!varNames.contains(varNamePrefix+count)) {
              break;
          } 
          count++;
      }

      return count;
  }
  
  private String getVariableName(String varPrefix, int varNumber) {
      varPrefix = varPrefix == null ? DEFAULT_VARIABLE_PREFIX 
              : varPrefix;
      return varPrefix+varNumber;
  }
  
  private static String TEMPLATES_PATH = "Templates/SOA/"; // NOI18N
  private static String XSLT_SERVICE = "xslt.service"; // NOI18N
  private static String XSL = "xsl"; // NOI18N
  
  private static final String DEFAULT_VARIABLE_PREFIX = "var"; // NOI18N
  private static final String INPUT_OPERATION_VARIABLE_PREFIX = "inOpVar"; // NOI18N
  private static final String OUTPUT_OPERATION_VARIABLE_PREFIX = "outOpVar"; // NOI18N
  private static final String INPUT_INVOKE_VARIABLE_PREFIX = "inInvokeVar"; // NOI18N
  private static final String OUTPUT_INVOKE_VARIABLE_PREFIX = "outInvokeVar"; // NOI18N

  private Panel<WizardDescriptor> myPanel;
}
