/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaRequestProcessor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.NewPackageKind;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class AddPackageVisualPanel1 extends JPanel 
      implements DocumentListener, ItemListener, INewUMLFileTemplates
{
    private boolean valid = true;
    private AddPackageWizardPanel1 panel;
    private INewDialogPackageDetails mDetails = null;
    private int errorType = -1;
    static final int INVALID_PACKAGE_NAME = 0;
    static final int PACKAGE_NAME_CONFLICT = 1;
    static final int INVALID_DIAGRAME_NAME = 2;
    static final int ENTER_PACKAGE_NAME = 4;
    
    
    /** Creates new form AddPackageVisualPanel1 */
    public AddPackageVisualPanel1(final AddPackageWizardPanel1 panel)
    {
        this.panel = panel;
        initComponents();
        
        jTextField1.getDocument().addDocumentListener(this);
        jTextField2.getDocument().addDocumentListener(this);
        jComboBox1.addItemListener(this);
        jCheckBox1.addItemListener(this);
    }
    
    public void read(WizardDescriptor wizDesc) {
        mDetails = (INewDialogPackageDetails) wizDesc.getProperty(PACKAGE_DETAILS);
        
        populateNamespaceCombobox();
        populateDiagramTypeCombobox();
    }
    
    void store(WizardDescriptor wizDesc) {

       // Get package kind
        mDetails.setPackageKind (NewPackageKind.NPKGK_PACKAGE);
        
       // Get package name
        mDetails.setName (getPackageName ());
        
       // Get the namespace
        INamespace pSelectedNamespace = NewDialogUtilities
                .getNamespace ((String)getPackageNamespace ());
        mDetails.setNamespace (pSelectedNamespace);
        
       // Get the scoped diagram flag
        mDetails.setCreateScopedDiagram (isCheckboxSelected ());
        
        // Get the scoped diagram name
        mDetails.setScopedDiagramName (getScopedDiagramName ());   
        
        // Get the diagram kind
        String diaTypeName = (String) getScopedDiagramKind ();
        mDetails.setScopedDiagramKind (NewDialogUtilities.diagramNameToKind(diaTypeName));
        
        // store the Package Details
        wizDesc.putProperty(PACKAGE_DETAILS, mDetails);
    }
    
    public String getName()
    {
        return NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_CREATEPACKAGE");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
   private void initComponents()
   {

      jPanel1 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      jTextField1 = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      jComboBox1 = new javax.swing.JComboBox();
      jPanel2 = new javax.swing.JPanel();
      jCheckBox1 = new javax.swing.JCheckBox();
      jLabel3 = new javax.swing.JLabel();
      jTextField2 = new javax.swing.JTextField();
      jLabel4 = new javax.swing.JLabel();
      jComboBox2 = new javax.swing.JComboBox();

      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_PACKAGE"))); // NOI18N

      jLabel1.setLabelFor(jTextField1);
      org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_NAME")); // NOI18N

      jTextField1.setText(NewDialogUtilities.getDefaultPackageName());
      jTextField1.selectAll();
      jTextField1.requestFocus();

      jLabel2.setLabelFor(jComboBox1);
      org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_NAMESPACE")); // NOI18N

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jLabel1)
               .add(jLabel2))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 309, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 281, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );

      jPanel1Layout.linkSize(new java.awt.Component[] {jComboBox1, jTextField1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

      jPanel1Layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel1)
               .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(15, 15, 15)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel2)
               .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(24, Short.MAX_VALUE))
      );

      jTextField1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("ACSD_NEW_PACKAGE_WIZARD_PACKAGENAME_TEXTFIELD")); // NOI18N
      jComboBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("ACSD_NEW_PACKAGE_WIZARD_PACKAGENAMESPACE_COMBOBOX")); // NOI18N

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_SCOPEDDIAGRAM"))); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_CREATESCOPED")); // NOI18N
      jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

      jLabel3.setLabelFor(jTextField2);
      org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_DIAGRAMNAME")); // NOI18N

      jTextField2.setText(NewDialogUtilities.getDefaultDiagramName());
      jTextField2.setEnabled(false);

      jLabel4.setLabelFor(jComboBox2);
      org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("IDS_DIAGRAMTYPE")); // NOI18N

      jComboBox2.setEnabled(false);

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel2Layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel3)
                     .add(jLabel4))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 295, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 298, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
               .add(jPanel2Layout.createSequentialGroup()
                  .add(10, 10, 10)
                  .add(jCheckBox1)))
            .addContainerGap())
      );

      jPanel2Layout.linkSize(new java.awt.Component[] {jComboBox2, jTextField2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

      jPanel2Layout.linkSize(new java.awt.Component[] {jLabel3, jLabel4}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .add(jCheckBox1)
            .add(14, 14, 14)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel3)
               .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(15, 15, 15)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel4)
               .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(24, Short.MAX_VALUE))
      );

      jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("ACSD_NEW_PACKAGE_WIZARD_CREATESCOPEDDIAGRAM_CHECKBOX")); // NOI18N
      jTextField2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("ACSD_NEW_PACKAGE_WIZARD_SCOPEDDIAGRAM_NAME_TEXTFIELD")); // NOI18N
      jComboBox2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddPackageVisualPanel1.class).getString("ACSD_NEW_PACKAGE_WIZARD_SCOPEDDIAGRAM_NAMESPACE_COMBOBOX")); // NOI18N

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(21, Short.MAX_VALUE))
      );

      jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddPackageVisualPanel1.class, "IDS_PACKAGE")); // NOI18N
   }// </editor-fold>//GEN-END:initComponents
    
    
    
    private void populateNamespaceCombobox()
    {
        //load namespaces
        if (jComboBox1 != null)
        {
            INamespace space = null;
            if(mDetails != null)
            {
                space = mDetails.getNamespace();
            }
            NewDialogUtilities.loadNamespace(jComboBox1, space);
        }
    }
    
    private void populateDiagramTypeCombobox()
    {
        //load diagram types
        if (jComboBox2 != null)
        {
            NewDialogUtilities.loadDiagramTypes(jComboBox2);
            // default to "Class Diagram"
            jComboBox2.setSelectedIndex(1);
        }
        
    }
    
    protected String getPackageName()
    {
        return jTextField1.getText().trim();
    }
    
    protected Object getPackageNamespace()
    {
        return jComboBox1.getSelectedItem();
    }
    
    protected String getScopedDiagramName()
    {
        return jTextField2.getText().trim();
    }
    
    protected Object getScopedDiagramKind()
    {
        return jComboBox2.getSelectedItem();
    }
    
    protected int getScopedDiagramType()
    {
        return jComboBox2.getSelectedIndex();
    }
    
    protected boolean isCheckboxSelected()
    {
        return jCheckBox1.isSelected();
    }
    
    public void itemStateChanged(ItemEvent event)
    {
        valid = true;
        if (event.getSource() == jCheckBox1)
        {
            jTextField2.setEnabled(jCheckBox1.isSelected());
            jComboBox2.setEnabled(jCheckBox1.isSelected());
            
            if (jCheckBox1.isSelected())
            {
                jTextField2.selectAll();
                jTextField2.requestFocus();
                validateDiagramName();
            }
            if (valid)
                validatePackageName();
        }
        else if (event.getSource() == jComboBox1)
        {
            validatePackageName();
        }
        if (panel != null) {
            panel.fireChangeEvent();
        }
    }
    
    
    public void changedUpdate(DocumentEvent event)
    {
        update(event);
    }
    
    public void insertUpdate( DocumentEvent event )
    {
        update(event);
    }
    
    public void removeUpdate(DocumentEvent event)
    {
        update(event);
    }
    
    private void update(DocumentEvent event)
    {
        if (event.getDocument() == jTextField1.getDocument() )   // package name text field
        {
            validatePackageName();
            if (jCheckBox1.isEnabled() && jCheckBox1.isSelected())
                validateDiagramName();
        }
        else {
            validateDiagramName();
        }
        if (panel != null) {
            panel.fireChangeEvent();
        }
    }
    
    private void validatePackageName()
    {
        valid = true;
        
        if (getPackageName().length() == 0)
        {
            errorType = ENTER_PACKAGE_NAME;
            valid = false;
        }
        else
        {        
            JavaRequestProcessor p = new JavaRequestProcessor();
            if (p.checkIfCorrectLanguage(mDetails.getNamespace()))
            {
                if (p.invalidPackage(getPackageName()))
                {
                    errorType = INVALID_PACKAGE_NAME;
                    valid = false;
                } else  // check if package name already exist in the current namespace

                {
                    IElementLocator pElementLocator = new ElementLocator();
                    String ns = (String) getPackageNamespace();
                    INamespace namespace = NewDialogUtilities.getNamespace(ns);
                    ETList<INamedElement> pFoundElements =
                            pElementLocator.findByName(namespace, getPackageName());

                    if (pFoundElements != null)
                    {
                        int count = pFoundElements.getCount();
                        for (int i = 0; i < count; i++)
                        {
                            INamedElement pFoundElement = pFoundElements.get(i);

                            if (pFoundElement != null)
                            {
                                if (pFoundElement.getElementType().equals("Package"))
                                {
                                    errorType = PACKAGE_NAME_CONFLICT;
                                    valid = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        jCheckBox1.setEnabled(valid);
        jTextField2.setEnabled(jCheckBox1.isEnabled() && jCheckBox1.isSelected());
        jComboBox2.setEnabled(jCheckBox1.isEnabled() && jCheckBox1.isSelected());
    }
    
    
    private void validateDiagramName()
    {
        valid = true;
        if (!Util.isDiagramNameValid(getScopedDiagramName()))
        {
            valid = false;
            errorType = INVALID_DIAGRAME_NAME;
        }
    }
    
    
    public boolean isValid(WizardDescriptor descriptor)
    {
        String msg = "";
        String msg_type = WizardDescriptor.PROP_ERROR_MESSAGE;

        if (!valid)
        {
            switch (errorType)
            {
                case ENTER_PACKAGE_NAME:
                    msg = NbBundle.getMessage(AddPackageVisualPanel1.class,
                            "MSG_Enter_Package_Name");
                    msg_type = WizardDescriptor.PROP_INFO_MESSAGE;
                    break;
                case INVALID_PACKAGE_NAME:
                    msg = NbBundle.getMessage(AddPackageVisualPanel1.class,
                            "MSG_Invalid_Package_Name");
                    break;
                case PACKAGE_NAME_CONFLICT:
                    msg = NbBundle.getMessage(
                            NewElementUI.class, "IDS_NAMESPACECOLLISION");
                    break;
                case INVALID_DIAGRAME_NAME:
                    msg = NbBundle.getMessage(AddPackageVisualPanel1.class,
                            "MSG_Invalid_Diagram_Name", getScopedDiagramName());
                    break;
            }
        }
        descriptor.putProperty(msg_type, msg);
        return valid;
    }
    
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JCheckBox jCheckBox1;
   private javax.swing.JComboBox jComboBox1;
   private javax.swing.JComboBox jComboBox2;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JTextField jTextField1;
   private javax.swing.JTextField jTextField2;
   // End of variables declaration//GEN-END:variables
}

