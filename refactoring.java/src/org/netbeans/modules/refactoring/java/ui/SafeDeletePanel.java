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
package org.netbeans.modules.refactoring.java.ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * Subclass of CustomRefactoringPanel representing the
 * Safe Delete refactoring UI
 * @author Bharath Ravikumar
 */
public class SafeDeletePanel extends JPanel implements CustomRefactoringPanel {
    
    private final transient Collection elements;
    private final transient SafeDeleteRefactoring refactoring;
    private boolean regulardelete;
    
    /**
     * Creates new form RenamePanelName
     * @param refactoring The SafeDelete refactoring used by this panel
     * @param selectedElements A Collection of selected elements
     */
    public SafeDeletePanel(SafeDeleteRefactoring refactoring, Collection selectedElements, boolean regulardelete) {
        setName(NbBundle.getMessage(SafeDeletePanel.class,"LBL_SafeDel")); // NOI18N
        this.elements = selectedElements;
        this.refactoring = refactoring;
        this.regulardelete = regulardelete;
        initComponents();
    }
    
    private boolean initialized = false;
    private String methodDeclaringClass = null;
    
    String getMethodDeclaringClass() {
        return methodDeclaringClass;
    }
    /**
     * Initialization method. Creates appropriate labels in the panel.
     */
    public void initialize() {
        //This is needed since the checkBox is gets disabled on a
        //repeated invocation of SafeDelete follwing removal of references
        //to the element
        searchInComments.setEnabled(true);
        
        if (initialized) return;
        
        final String labelText;
        
        Collection<? extends FileObject> files = refactoring.getRefactoringSource().lookupAll(FileObject.class);
        final Collection<? extends TreePathHandle> handles = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        
        if (files.size()>1 && files.size() == handles.size()) {
            //delete multiple files
            if (regulardelete) {
                labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_RegularDelete",handles.size());
            } else {
                labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Classes",handles.size());
            }
        } else if (handles.size()>1) {
            labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Classes",handles.size());;
        } else if (handles.size()==1) {
          JavaSource s = JavaSource.forFileObject(handles.iterator().next().getFileObject());
          final String[] name = new String[1];
          try {
              s.runUserActionTask(new CancellableTask<CompilationController>() {
                  public void cancel() {
                  }
                  
                  public void run(CompilationController parameter) throws Exception {
                      parameter.toPhase(Phase.RESOLVED);
                      name[0] = handles.iterator().next().resolveElement(parameter).getSimpleName().toString();
                  }
              }, true);
          } catch (IOException ioe) {
              throw (RuntimeException) new RuntimeException().initCause(ioe);
          }
          if (regulardelete) {
              labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_RegularDeleteElement",name[0]);
          } else {
              labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Element",name[0]);
          }
        } else {
            labelText ="";
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (regulardelete) {
                    safeDelete = new JCheckBox();
                    Mnemonics.setLocalizedText(safeDelete, NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDelCheckBox"));
                    safeDelete.setMargin(new java.awt.Insets(2, 14, 2, 2));
                    searchInComments.setEnabled(false);
                    safeDelete.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent evt) {
                            searchInComments.setEnabled(safeDelete.isSelected());
                        }
                    });

                    checkBoxes.add(safeDelete, BorderLayout.CENTER);
                }
                label.setText(labelText);
                validate();
            }
        });
        initialized = true;
    }
    
    public void requestFocus() {
        super.requestFocus();
    }

    boolean isRegularDelete() {
        if (safeDelete!=null) {
            return !safeDelete.isSelected();
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        checkBoxes = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        checkBoxes.setLayout(new java.awt.BorderLayout());

        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        checkBoxes.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, bundle.getString("LBL_SafeDelInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(2, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        checkBoxes.add(searchInComments, java.awt.BorderLayout.SOUTH);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(checkBoxes, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for deleteInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.whereUsed", b);
        refactoring.setCheckInComments(b.booleanValue());
    }//GEN-LAST:event_searchInCommentsItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel checkBoxes;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JCheckBox safeDelete;
    
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30 , orig.height + 30);
    }
    
    /**
     * Indicates whether the element usage must be checked in comments
     * before deleting each element.
     * @return Returns the isSelected() attribute of the
     * underlying check box that controls search in comments
     */
    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }
    
    public Component getComponent() {
        return this;
    }

    
//--public utility methods--
//    
//    //This method has been made public so that another class might be able to reuse this.
//    //This should be moved to a common utility class.
//    /**
//     * Returns the formatted string corresponding to the declaration
//     * of a CallableFeature(a {@link org.netbeans.jmi.javamodel.Method}
//     * or a {@link org.netbeans.jmi.javamodel.Constructor})
//     * Copied from {@link org.netbeans.modules.refactoring.ui.WhereUsedPanel}
//     */
//    public String getHeader(CallableFeature call) {
//        if (((CallableFeatureImpl) call).getParser() == null) {
//            if (call instanceof Method) {
//                return ((Method) call).getName();
//            } else if (call instanceof Constructor) {
//                return getSimpleName(call.getDeclaringClass());
//            }
//            return "";
//        }
//        int s = ((MetadataElement) call).getPartStartOffset(ElementPartKindEnum.HEADER);
//        int element = ((MetadataElement) call).getPartEndOffset(ElementPartKindEnum.HEADER);
//        String result =  call.getResource().getSourceText().substring(s,element);
//        if (result.length() > 50) {
//            result = result.substring(0,49) + "..."; // NOI18N
//        }
//        return CheckUtils.htmlize(result);
//    }
//    
//    //This method has been made public so that another class might be able to reuse this.
//    //This should be moved to a common utility class.
//    /**
//     * Returns the SimpleName for a class, accounting for
//     * anonymous classes as well.
//     * Copied from {@link org.netbeans.modules.refactoring.ui.WhereUsedPanel}
//     */
//    public String getSimpleName(ClassDefinition clazz) {
//        if (clazz instanceof JavaClass) {
//            return ((JavaClass) clazz).getSimpleName();
//        } else {
//            return NbBundle.getMessage(SafeDeleteUI.class, "LBL_AnonymousClass"); // NOI18N
//        }
//    }
//    
//    
////--private helper methods--
//    /**
//     * Returns a string constructed by passing the parameter to
//     * <CODE>NbBundle.getMessage</CODE> with the Bundle key LBL_SafeDelHeader
//     */
//    private String getCustomString(Object refElement) {
//        //This check for elementreference may not be needed, now that safe delete action
//        //extracts the referred element. TODO:remove this check later.
//        if(refElement instanceof ElementReference){
//            NamedElement referredElement = ((ElementReference) refElement).getElement();
//            return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelRefWarning",referredElement.getName());//NOI18N
//        } else{
//            if(refElement instanceof Method)
//                return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelMethod", 
//                        getHeader((CallableFeature)refElement), 
//                        getSimpleName(((CallableFeature) refElement).getDeclaringClass())); // NOI18N
//            else if (refElement instanceof Constructor) {
//                return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelConstructor", 
//                        getHeader((CallableFeature)refElement), 
//                        getSimpleName(((CallableFeature) refElement).getDeclaringClass())); // NOI18N
//            } else if (refElement instanceof Field) {
//                return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelField", 
//                        ((Field)refElement).getName(), 
//                        getSimpleName(((Field) refElement).getDeclaringClass())); // NOI18N
//            }  else if (refElement instanceof Variable) {
//                return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelVariable",((Variable)refElement).getName());//NOI18N
//            }
//            else if(refElement instanceof NamedElement){
//                //Handle any named element that is not of any of the above types
//                String typeName = ((NamedElement) refElement).getName();
//                return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelHeader",typeName);//NOI18N
//            }
//            else
//                return NbBundle.getMessage(SafeDeleteUI.class,"LBL_SafeDelHeader",refElement);//NOI18N
//        }
//    }
//    
//    private String getElementName(Element element) {
//        if(element instanceof Resource){
//            FileObject fileObject = JavaModel.getFileObject((Resource) element);
//            return fileObject.getNameExt();
//        } else
//            return element.toString();
//    }
//    
}

