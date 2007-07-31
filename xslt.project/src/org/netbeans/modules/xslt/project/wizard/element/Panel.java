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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.xml.namespace.QName;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
abstract class Panel<T> implements WizardDescriptor.ValidatingPanel<T> {
    
  Panel(Project project, Panel<T> parent) {
    myProject = project;
    myFolder = Util.getSrcFolder(project);
    myParent = parent;
  }

  protected final Project getProject() {
    return myProject;
  }

  protected final FileObject getFolder() {
    return myFolder;
  }

  protected final Panel<T> getParent() {
    return myParent;
  }

  protected abstract void createPanel(JPanel panel, GridBagConstraints c);
  protected void setEnabled(boolean enabled) {}
  protected void update() {}

  protected String getComponentName() {
    return null;
  }

  protected Panel<T> getNext() {
    return null;
  }

  protected Object getResult() {
    return null;
  }

  protected final Panel<T> getPrevious() {
    return myParent;
  }

  protected String getError() {
    return null;
  }

  protected final String getError(String error1, String error2) {
    if (error1 != null) {
      return error1;
    }
    return error2;
  }

  public JPanel getComponent() {
    if (myComponent == null) {
      myComponent = createMainPanel();
      String name = getComponentName();
      myComponent.setName(name);
    
      String [] steps = new String [] {NAME_TYPE, NAME_WSDL, NAME_XSLT};
      myComponent.putClientProperty("WizardPanel_contentData", steps); // NOI18N

      for (int i=0; i < steps.length; i++) {
        if (name.equals(steps [i])) {
          myComponent.putClientProperty(
            "WizardPanel_contentSelectedIndex", new Integer(i - 1)); // NOI18N
        }
      }
    }
    return myComponent;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    createPanel(panel, c);

//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));
    return panel;
  }

  public void validate() throws WizardValidationException {
    String error = getError();

    if (error != null) {
      throw new WizardValidationException(myComponent, error, error);
    }
  }

  protected final String i18n(String key) {
    return org.netbeans.modules.print.ui.PrintUI.i18n(Panel.class, key);
  }

  protected final String i18n(String key, String param) {
    return org.netbeans.modules.print.ui.PrintUI.i18n(Panel.class, key, param);
  }

  protected final String i18n(String key, String param1, String param2) {
    return org.netbeans.modules.print.ui.PrintUI.i18n(
      Panel.class, key, param1, param2);
  }

  public boolean isValid() {
    return true;
  }

  public HelpCtx getHelp() {
    return new HelpCtx("xslt_project_addxsl"); // NOI18N
  }

  public void storeSettings(Object object) {}

  public void addChangeListener(ChangeListener listener) {}
  public void removeChangeListener(ChangeListener listener) {}
  public void readSettings(Object object) {}

  protected final int getXslFileNumber(int start) {
    int count = start;

    while (true) {
      if (getFolder().getFileObject(NAME + count, EXT) == null) {
        return count;
      }
      count++;
    }
  }

  protected final String getXslFileName(int number) {
    return NAME + number;
  }

  protected final String getType(OperationParameter parameter) {
    if (parameter == null) {
//out("1");
      return EMPTY;
    }
    NamedComponentReference<Message> reference = parameter.getMessage();

    if (reference == null) {
//out("2");
      return EMPTY;
    }
    Message message = reference.get();

    if (message == null) {
//out("3");
      return EMPTY;
    }
    Collection<Part> parts = message.getParts();

    if (parts == null) {
//out("4");
      return EMPTY;
    }
    java.util.Iterator<Part> iterator = parts.iterator();

    if ( !iterator.hasNext()) {
//out("5");
      return EMPTY;
    }
    return getType(iterator.next());
  }

  private String getType(Part part) {
    NamedComponentReference<? extends NamedReferenceable> refTypeEl = part.getType();
    refTypeEl = refTypeEl == null ? part.getElement() : refTypeEl;
    
    if (refTypeEl != null) {

      QName qName = refTypeEl.getQName();
      if (qName != null) {
          return qName.getLocalPart();
      }
    }
    return EMPTY;
  }

  protected final String addExtension(String file) {
    if (file.endsWith(Panel.DOT + Panel.EXT)) {
      return file;
    }
    return file + Panel.DOT + Panel.EXT;
  }

  // -------------------------------------------------------
  protected class Renderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(
      JList list, Object value, int index,
      boolean isSelected, boolean hasFocus)
   {
      super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);

      if (value instanceof Util.WSDLFile) {
        setText(((Util.WSDLFile) value).getName());
      }
      if (value instanceof Operation) {
        setText(((Operation) value).getName());
      }
      if (value instanceof PartnerRolePort) {
        setText(((PartnerRolePort) value).getName());
      }
      return this;
    }
  }

  // ----------------------------------
  public static class PartnerRolePort {
    public PartnerRolePort(
      PartnerLinkType partnerLinkType,
      Role role,
      PortType portType)
    {
      myPartnerLinkType = partnerLinkType;
      myRole = role;
      myPortType = portType;
    }

    public PartnerLinkType getPartnerLinkType() {
      return myPartnerLinkType;
    }

    public Role getRole() {
      return myRole;
    }

    public PortType getPortType() {
      return myPortType;
    }

    public String getName() {
      return
        myPortType.getName() + " (" + // NOI18N
        myPartnerLinkType.getName() + "/" + // NOI18N
        myRole.getName() + ")"; // NOI18N
    }

    /**{@inheritDoc}*/
    @Override
    public boolean equals(Object object)
    {
      if ( !(object instanceof PartnerRolePort)) {
        return false;
      }
      PartnerRolePort partnerRolePort = (PartnerRolePort) object;

      return
        partnerRolePort.getPartnerLinkType().equals(getPartnerLinkType()) &&
        partnerRolePort.getRole().equals(getRole()) &&
        partnerRolePort.getPortType().equals(getPortType());
    }

    /**{@inheritDoc}*/
    @Override
    public int hashCode()
    {
      return
        getPartnerLinkType().hashCode() *
        getRole().hashCode() *
        getPortType().hashCode();
    }

    private Role myRole;
    private PortType myPortType;
    private PartnerLinkType myPartnerLinkType;
  }

  private Project myProject;
  private JPanel myComponent;
  private FileObject myFolder;
  private Panel<T> myParent;

  private static final String DOT = "."; // NOI18N
  private static final String EXT = "xsl"; // NOI18N
  private static final String NAME = "newXSLFile"; // NOI18N

  protected static final String EMPTY = ""; // NOI18N

  protected static final String NAME_TYPE =
    org.netbeans.modules.print.ui.PrintUI.i18n(Panel.class,
    "LBL_Service_Type"); // NOI18N

  protected static final String NAME_WSDL =
    org.netbeans.modules.print.ui.PrintUI.i18n(Panel.class,
    "LBL_WSDL_File"); // NOI18N

  protected static final String NAME_XSLT =
    org.netbeans.modules.print.ui.PrintUI.i18n(Panel.class,
    "LBL_XSLT_Configuration"); // NOI18N

  public static final String INPUT_FILE = "input.file"; // NOI18N
  public static final String INPUT_OPERATION = "input.operation"; // NOI18N
  public static final String INPUT_PARTNER_ROLE_PORT =
    "input.partner.role.port"; // NOI18N

  public static final String OUTPUT_FILE = "output.file"; // NOI18N
  public static final String OUTPUT_OPERATION = "output.operation"; // NOI18N
  public static final String OUTPUT_PARTNER_ROLE_PORT =
    "output.partner.role.port"; // NOI18N

  public static final String CHOICE = "choice"; // NOI18N
  public static final String CHOICE_REQUEST_REPLY = "choice.request.reply"; // NOI18N
  public static final String CHOICE_FILTER_ONE_WAY =
    "choice.filter.one.way"; // NOI18N
  public static final String CHOICE_FILTER_REQUEST_REPLY =
    "choice.filter.request.reply"; // NOI18N
}
