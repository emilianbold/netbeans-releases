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
 */
package org.netbeans.modules.xslt.project.wizard.element;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELComponentFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;

import org.netbeans.modules.xslt.core.xsltmap.InputTransformationDesc;
import org.netbeans.modules.xslt.core.xsltmap.RequestReplyServiceUC;
import org.netbeans.modules.xslt.core.xsltmap.XsltMapModel;

import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
public final class Iterator implements TemplateWizard.Iterator {

  /**{@inheritDoc}*/
  public static Iterator createXsl() {
    return new Iterator();
  }

//  private Template() {
//  }
// todo r

//  protected Template (String [][] templates, String path, boolean isSetMain) {
//    myCreatedXsl = null;
//    myTemplates = templates;
//    myPath = path;
//    myIsSetMain = isSetMain;
//  }

  /**{@inheritDoc}*/
  public Set instantiate(TemplateWizard wizard) throws IOException {
//todo r
//    FileObject targetDirectory = Templates.getTargetFolder(wizard);
//    String name = Templates.getTargetName(wizard);
//    String nameUTF8 = encodeUTF8 (Templates.getTargetName (wizard));
//    String pakage;
//    String packageAndNameUTF8;
//Log.out ();
//Log.out (" targetDirectory: " +  targetDirectory.getPath ());
//Log.out ("projectDirectory: " + projectDirectory.getPath ());
//Log.out ("package: '" + myPackage + "'");

//    if (myPackage.equals ("")) { // NOI18N
//      pakage = ""; // NOI18N
//      packageAndNameUTF8 = nameUTF8;
//    }
//    else {
//      pakage = "package " + myPackage + ";"; // NOI18N
//      packageAndNameUTF8 = myPackageUTF8 + "." + nameUTF8; // NOI18N
//    }
//    String fullNameUTF8 = packageAndNameUTF8.replace (".", "/"); // NOI18N
//
//    String [][] param = new String [][] {
//      {"__USER__", System.getProperty ("user.name")}, // NOI18N
//      {"__NAME__", name}, // NOI18N
//      {"__PACKAGE__", pakage}, // NOI18N
//      {"__NAME_UTF_8__", nameUTF8}, // NOI18N
//      {"__PACKAGE_AND_NAME_UTF_8__", packageAndNameUTF8}, // NOI18N
//      {"__FULL_NAME_UTF_8__", fullNameUTF8}, // NOI18N
//    };
//    addName (myTemplates, name);
//    unsetMainElement (project);
//    
    return Collections.singleton(createFiles(wizard));
  }

  /**{@inheritDoc}*/
  public void initialize(TemplateWizard wizard) {
//    myIndex = 0;
    Project project = Templates.getProject(wizard);
    createPanel(project);
  }

  /**{@inheritDoc}*/
  public void uninitialize(TemplateWizard wizard) {
    myPanel = null;
  }

  /**{@inheritDoc}*/
  public String name() {
    return NbBundle.getMessage(
      Iterator.class, "LBL_x_of_y", // NOI18N
      new Integer(1/*myIndex + 1*/),
      new Integer(1/*myPanels.length)*/));
// todo r
  }
  
  /**{@inheritDoc}*/
  public boolean hasNext() {
// todo r
    return false;//myIndex < myPanels.length - 1;
  }
  
  /**{@inheritDoc}*/
  public boolean hasPrevious() {
// todo r
    return false;//myIndex > 0;
  }
  
  /**{@inheritDoc}*/
  public void nextPanel() {
// todo r
    //myIndex++;
  }
  
  /**{@inheritDoc}*/
  public void previousPanel() {
// todo r
    //myIndex--;
  }

  /**{@inheritDoc}*/
  public WizardDescriptor.Panel current() {
// todo r
    return myPanel;//s [myIndex];
  }
  
  /**{@inheritDoc}*/
  public void addChangeListener(ChangeListener listener) {}

  /**{@inheritDoc}*/
  public void removeChangeListener(ChangeListener listener) {}

  private void createPanel(Project project) {
// todo r
    myPanel = //new WizardDescriptor.Panel [] {
      new Panel(project);
//    };
  }
  
  private FileObject createFiles(TemplateWizard wizard) throws IOException {
    Project project = Templates.getProject(wizard);
    String file = (String) wizard.getProperty(Panel.FILE);
//    WSDLModel model = Util.getWSDLModel((FileObject) wizard.getProperty(Panel.WSDL));
    Operation operation = (Operation) wizard.getProperty(Panel.OPERATION);

    createXsltMap(project, file, operation);
//    updateWSDLModel(model, operation);//todo r
    return createXslFile(project, file);
  }

  private FileObject createXslFile(Project project, String file) throws IOException {
    String text =
      "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">" + LS + // NOI18N
      "    <xsl:template match=\"/\">" + LS + // NOI18N
      "    </xsl:template>" + LS + // NOI18N
      "</xsl:stylesheet>"; // NOI18N

    return Util.createFile(Util.getSrcFolder(project), file, text);
  }

  private void createXsltMap(
    Project project,
    String file,
    Operation operation) throws IOException
  {
    XsltMapModel model = XsltMapModel.getDefault(project);
    RequestReplyServiceUC use = new RequestReplyServiceUC();
    InputTransformationDesc description = new InputTransformationDesc(model, use);

    description.setPartnerLink(
      "{" + operation.getModel().getDefinitions().getTargetNamespace() + "}" + // NOI18N
      "PartnerLinkType"); // todo m
    description.setRoleName("Role"); // todo m
    description.setPortType(((PortType) operation.getParent()).getName());
    description.setOperation(operation.getName());
    description.setMessageType(
      "{" + operation.getModel().getDefinitions().getTargetNamespace() + "}" + // NOI18N
      operation.getOutput().getMessage().get().getName());//todo m {ns}
    description.setFile(file);
    description.setTransformJBI("no"); // NOI18N

    model.addTransformationUC(use);
    model.sync();
  }

// todo r
//  private int myIndex;
  private WizardDescriptor.Panel myPanel;
}
