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
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xslt.core.transformmap.api.BooleanType;
import org.netbeans.modules.xslt.core.transformmap.api.Invokes;
import org.netbeans.modules.xslt.core.transformmap.api.Service;
import org.netbeans.modules.xslt.core.transformmap.api.TMapComponentFactory;
import org.netbeans.modules.xslt.core.transformmap.api.TMapModel;
import org.netbeans.modules.xslt.core.transformmap.api.TransformMap;
import org.netbeans.modules.xslt.core.transformmap.api.WSDLReference;

import static org.netbeans.modules.print.api.PrintUI.*;

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

  private DataObject createFile(TemplateWizard wizard) throws IOException {
    FileObject file = null;
    Project project = Templates.getProject(wizard);
    String choice = (String) wizard.getProperty(Panel.CHOICE);
    
//    TMapModelFactory tMapModelFactory = Lookup.getDefault().lookup(TMapModelFactory.class);
//    assert tMapModelFactory != null;
    FileObject tMapFo = org.netbeans.modules.xslt.core.xsltmap.util.Util.getTMapFo(project);
    if (tMapFo == null) {
        tMapFo = org.netbeans.modules.xslt.core.xsltmap.util.Util.createDefaultTransformmap(project);
    }
    
    TMapModel tMapModel = 
            org.netbeans.modules.xslt.core.xsltmap.util.Util.getTMapModel(tMapFo);

    configureTMapModel(tMapModel, wizard);
    file = createXslFile(
            project, (String) wizard.getProperty(Panel.INPUT_FILE),
            ((Boolean) wizard.getProperty(Panel.INPUT_TRANSFORM_JBI)).booleanValue());
// TODO m    
/*
    if (Panel.CHOICE_REQUEST_REPLY.equals(choice)) {
      file = createXslFile(
      project, (String) wizard.getProperty(Panel.INPUT_FILE),
      ((Boolean) wizard.getProperty(Panel.INPUT_TRANSFORM_JBI)).booleanValue());
    }
    else if (Panel.CHOICE_FILTER_ONE_WAY.equals(choice)) {
      file = createXslFile(
        project, (String) wizard.getProperty(Panel.INPUT_FILE),
        ((Boolean) wizard.getProperty(Panel.INPUT_TRANSFORM_JBI)).booleanValue());
    }
    else */if (Panel.CHOICE_FILTER_REQUEST_REPLY.equals(choice)) {
//      file = createXslFile(
//        project, (String) wizard.getProperty(Panel.INPUT_FILE),
//        ((Boolean) wizard.getProperty(Panel.INPUT_TRANSFORM_JBI)).booleanValue());
      file = createXslFile(
        project, (String) wizard.getProperty(Panel.OUTPUT_FILE),
        ((Boolean) wizard.getProperty(Panel.OUTPUT_TRANSFORM_JBI)).booleanValue());
    }
//    model.addTransformationUC(use);
//    tMapModel.sync();

    return DataObject.find(file);
  }

  private void configureTMapModel(TMapModel tMapModel, TemplateWizard wizard) {
      assert tMapModel != null && wizard != null;
      if (! TMapModel.State.VALID.equals(tMapModel.getState())) {
          return;
      }
      
      TMapComponentFactory componentFactory = tMapModel.getFactory();

      String inputFileStr;
      Operation wizardInputOperation;
      Panel.PartnerRolePort wizardInputPartnerRolePort;
      Boolean wizardInputIsTransformJBI = false;
      
      inputFileStr = (String) wizard.getProperty(Panel.INPUT_FILE);
      wizardInputOperation = (Operation) wizard.getProperty(Panel.INPUT_OPERATION);
      wizardInputPartnerRolePort =
              (Panel.PartnerRolePort) wizard.getProperty(Panel.INPUT_PARTNER_ROLE_PORT);
      PartnerLinkType wizardInPlt = wizardInputPartnerRolePort == null ? null : wizardInputPartnerRolePort.getPartnerLinkType();
      Role wizardInRole = wizardInputPartnerRolePort == null ? null : wizardInputPartnerRolePort.getRole();
      wizardInputIsTransformJBI =
              ((Boolean) wizard.getProperty(Panel.INPUT_TRANSFORM_JBI));
      
      
      if (wizardInPlt == null || wizardInRole == null || wizardInputOperation == null) {
          return;
      }
      

      String outputFileStr;
      Operation wizardOutputOperation;
      Panel.PartnerRolePort wizardOutputPartnerRolePort;
      Boolean wizardOutputIsTransformJBI = false;
      
      outputFileStr = (String) wizard.getProperty(Panel.OUTPUT_FILE);
      wizardOutputOperation = (Operation) wizard.getProperty(Panel.OUTPUT_OPERATION);
      wizardOutputPartnerRolePort =
              (Panel.PartnerRolePort) wizard.getProperty(Panel.OUTPUT_PARTNER_ROLE_PORT);
      PartnerLinkType wizardOutPlt = wizardOutputPartnerRolePort == null ? null : wizardOutputPartnerRolePort.getPartnerLinkType();
      Role wizardOutRole = wizardOutputPartnerRolePort == null ? null : wizardOutputPartnerRolePort.getRole();
      wizardOutputIsTransformJBI =
              ((Boolean) wizard.getProperty(Panel.OUTPUT_TRANSFORM_JBI));
      
      
      try {
          tMapModel.startTransaction();
          
          TransformMap root = tMapModel.getTransformMap();
          // TODO m
          if (root == null) {
              root = componentFactory.createTransformMap();
              tMapModel.addChildComponent(null, root, -1);
          }
          
          if (root == null) {
              return;
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
          
          // reqService != null
          org.netbeans.modules.xslt.core.transformmap.api.Operation tMapOp =
                  componentFactory.createOperation();
          tMapOp.setOperation(tMapOp.createWSDLReference(wizardInputOperation, Operation.class));
          if (inputFileStr != null && ! "".equals(inputFileStr)) {
              tMapOp.setFile(inputFileStr);
          }
          tMapOp.setTransformJbi(BooleanType.parseBooleanType(wizardInputIsTransformJBI));
          
          reqService.addOperation(tMapOp);
          
          
          // create invokes
          if (wizardOutPlt != null && wizardOutRole != null && wizardOutputOperation != null) {
              Invokes invokes = componentFactory.createInvokes();
              invokes.setPartnerLinkType(invokes.createWSDLReference(wizardOutPlt, PartnerLinkType.class));
              invokes.setRole(invokes.createWSDLReference(wizardOutRole, Role.class));
              invokes.setOperation(invokes.createWSDLReference(wizardOutputOperation, Operation.class));
              
              if (outputFileStr != null && ! "".equals(outputFileStr)) {
                  invokes.setFile(outputFileStr);
              }
              invokes.setTransformJbi(BooleanType.parseBooleanType(wizardOutputIsTransformJBI));
              
              tMapOp.setInvokes(invokes);
          }
      } finally {
          tMapModel.endTransaction();
      }
  }

  private FileObject createXslFile(
    Project project,
    String file,
    boolean transformJBI) throws IOException
  {
      if (file == null || "".equals(file)) {
          return null;
      }
      
    String text;

    if (transformJBI) {
      text = 
        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"" + // NOI18N
        " version=\"1.0\">" + LS + // NOI18N
        "    <xsl:template match=\"/\">" + LS + // NOI18N
        "        <jbi:message xmlns:ns2=\"http://sun.com/EmplOutput\"" + // NOI18N
        " type=\"ns2:output-msg\" version=\"1.0\"" + // NOI18N
        " xmlns:jbi=\"http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper\">" + LS + // NOI18N
        "            <jbi:part>" + LS + // NOI18N
        "                <xsl:apply-templates/>" + LS + // NOI18N
        "            </jbi:part>" + LS + // NOI18N
        "            <jbi:part>" + LS + // NOI18N
        "                <xsl:apply-templates/>" + LS + // NOI18N
        "            </jbi:part>" + LS + // NOI18N
        "        </jbi:message>" + LS + // NOI18N
        "    </xsl:template>" + LS + // NOI18N
        "</xsl:stylesheet>" + LS; // NOI18N
    }
    else {
      text =
        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform" + // NOI18N
        "\" version=\"1.0\">" + LS + // NOI18N
        "    <xsl:template match=\"/\">" + LS + // NOI18N
        "    </xsl:template>" + LS + // NOI18N
        "</xsl:stylesheet>" + LS; // NOI18N
    }
    return Util.createFile(Util.getSrcFolder(project), file, text);
  }

  private Panel<WizardDescriptor> myPanel;
}
