/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.javadoc.Doc;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Jan Becicka
 */
public class IntroduceParameterPanel extends JPanel implements CustomRefactoringPanel {
    private static final String COMPATIBLE = "compatible.introduceParameter"; // NOI18N
    private static final String REPLACEALL = "replaceall.introduceParameter"; // NOI18N
    private static final String DECLAREFINAL = "declarefinal.introduceParameter"; // NOI18N
    private static final String UPDATEJAVADOC = "updateJavadoc.introduceParameters"; // NOI18N
    private static final String GENJAVADOC = "generateJavadoc.introduceParameters"; // NOI18N
    private static final String MIME_JAVA = "text/x-java"; // NOI18N
    private static final String DEFAULT_NAME = "par"; // NOI18N

    TreePathHandle refactoredObj;
    private ChangeListener parent;
    private final JComponent[] singleLineEditor;
    private final DocumentListener nameChangedListener;
    private int startOffset;
    
    public Component getComponent() {
        return this;
    }


    /** Creates new form ChangeMethodSignature */
    public IntroduceParameterPanel(TreePathHandle refactoredObj, final ChangeListener parent) {
        this.refactoredObj = refactoredObj;
        this.parent = parent;
        singleLineEditor = Utilities.createSingleLineEditor(MIME_JAVA);
        initComponents();
        nameChangedListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent de) {
                parent.stateChanged(null);
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                parent.stateChanged(null);
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                parent.stateChanged(null);
            }
        };
        ((JEditorPane) singleLineEditor[1]).getDocument().addDocumentListener(nameChangedListener);
    }
    
    private boolean initialized = false;
    public void initialize() {
        try {
            if (initialized) {
                return;
            }
            JavaSource source = JavaSource.forFileObject(refactoredObj.getFileObject());
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void run(org.netbeans.api.java.source.CompilationController info) {
                    try {
                        info.toPhase(org.netbeans.api.java.source.JavaSource.Phase.RESOLVED);
                        
                        final FileObject fileObject = refactoredObj.getFileObject();
                        DataObject dob = DataObject.find(fileObject);
                        ((JEditorPane)singleLineEditor[1]).getDocument().putProperty(
                                Document.StreamDescriptionProperty,
                                dob);
                        final TreePath path = refactoredObj.resolve(info);
                        
                        final TreePath methodPath = JavaPluginUtils.findMethod(path);
                        MethodTree methodTree = (MethodTree) methodPath.getLeaf();
                        final int[] parameterSpan = info.getTreeUtilities().findMethodParameterSpan(methodTree);
                        final TypeMirror tm = info.getTrees().getTypeMirror(path);
                        
                        Element methodElement = info.getTrees().getElement(methodPath);
                        Doc javadocDoc = info.getElementUtilities().javaDocFor(methodElement);
                        if(javadocDoc.commentText() == null || javadocDoc.getRawCommentText().equals("")) {
                            chkGenJavadoc.setEnabled(true);
                            chkGenJavadoc.setVisible(true);
                            chkUpdateJavadoc.setVisible(false);
                        } else {
                            chkUpdateJavadoc.setEnabled(true);
                            chkUpdateJavadoc.setVisible(true);
                            chkGenJavadoc.setVisible(false);
                        }
                                                
                        String name = getName(path.getLeaf());
                        if (name == null) {
                            name = DEFAULT_NAME;
                        }
                        
                        Scope scope =  null;
                        TreePath bodyPath = new TreePath(methodPath, methodTree.getBody());
                        scope = info.getTrees().getScope(bodyPath);
                        
                        final String parameterName = makeNameUnique(info, scope, name, methodTree);
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                final JEditorPane editorPane = (JEditorPane)singleLineEditor[1];
                                
                                DialogBinding.bindComponentToFile(fileObject, parameterSpan[0] + 1, parameterSpan[1] - parameterSpan[0], editorPane);
                                editorPane.setText(tm.toString() + " " + parameterName); //NOI18N
                                startOffset = tm.toString().length() + 1;
                                int endOffset = parameterName.length() + startOffset;
                                editorPane.select(startOffset, endOffset);
                                try {
                                    Position startPos = editorPane.getDocument().createPosition(startOffset);
                                    Position endPos = editorPane.getDocument().createPosition(endOffset);
                                    editorPane.putClientProperty("document-view-start-position", startPos); //NOI18N
                                    editorPane.putClientProperty("document-view-end-position", endPos); //NOI18N
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }

                                ((JEditorPane)singleLineEditor[1]).getDocument().addDocumentListener(nameChangedListener);
                                ((JEditorPane)singleLineEditor[1]).putClientProperty(
                                    "HighlightsLayerExcludes", //NOI18N
                                    "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
                                );
                                initialized = true;
                            }});
                    }
                    catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                public void cancel() {
                }
            }, true);
            initialized = true;
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        chkIsDeclareFinal = new javax.swing.JCheckBox();
        chkIsReplaceAll = new javax.swing.JCheckBox();
        chkIsCompatible = new javax.swing.JCheckBox();
        jScrollPane1 = (JScrollPane)singleLineEditor[0];
        chkGenJavadoc = new javax.swing.JCheckBox();
        chkUpdateJavadoc = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setAutoscrolls(true);
        setName(getString("LBL_TitleIntroduceParameter"));

        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(IntroduceParameterPanel.class, "IntroduceParameterPanel.lblName.text")); // NOI18N

        chkIsDeclareFinal.setSelected(((Boolean) RefactoringModule.getOption(DECLAREFINAL, Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(chkIsDeclareFinal, org.openide.util.NbBundle.getMessage(IntroduceParameterPanel.class, "IntroduceParameterPanel.chkIsDeclareFinal.text")); // NOI18N
        chkIsDeclareFinal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkIsDeclareFinalItemStateChanged(evt);
            }
        });

        chkIsReplaceAll.setSelected(((Boolean) RefactoringModule.getOption(REPLACEALL, Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(chkIsReplaceAll, org.openide.util.NbBundle.getMessage(IntroduceParameterPanel.class, "IntroduceParameterPanel.chkIsReplaceAll.text")); // NOI18N
        chkIsReplaceAll.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkIsReplaceAllItemStateChanged(evt);
            }
        });

        chkIsCompatible.setSelected(((Boolean) RefactoringModule.getOption(COMPATIBLE, Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(chkIsCompatible, org.openide.util.NbBundle.getMessage(IntroduceParameterPanel.class, "IntroduceParameterPanel.chkIsCompatible.text")); // NOI18N
        chkIsCompatible.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkIsCompatibleItemStateChanged(evt);
            }
        });

        chkGenJavadoc.setSelected(((Boolean) RefactoringModule.getOption(GENJAVADOC, Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(chkGenJavadoc, org.openide.util.NbBundle.getMessage(IntroduceParameterPanel.class, "IntroduceParameterPanel.chkGenJavadoc.text")); // NOI18N
        chkGenJavadoc.setEnabled(false);
        chkGenJavadoc.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkGenJavadocItemStateChanged(evt);
            }
        });

        chkUpdateJavadoc.setSelected(((Boolean) RefactoringModule.getOption(UPDATEJAVADOC, Boolean.FALSE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(chkUpdateJavadoc, org.openide.util.NbBundle.getMessage(IntroduceParameterPanel.class, "IntroduceParameterPanel.chkUpdateJavadoc.text")); // NOI18N
        chkUpdateJavadoc.setEnabled(false);
        chkUpdateJavadoc.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkUpdateJavadocItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblName)
                .addGap(2, 2, 2)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 336, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(chkIsDeclareFinal)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(chkIsReplaceAll)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(chkIsCompatible)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkUpdateJavadoc)
                    .addComponent(chkGenJavadoc))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblName)
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(chkIsDeclareFinal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkIsReplaceAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkIsCompatible)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkUpdateJavadoc)
                    .addComponent(chkGenJavadoc))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chkIsCompatibleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkIsCompatibleItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(COMPATIBLE, b);
        parent.stateChanged(null);
    }//GEN-LAST:event_chkIsCompatibleItemStateChanged

    private void chkIsReplaceAllItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkIsReplaceAllItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(REPLACEALL, b);
        parent.stateChanged(null);
    }//GEN-LAST:event_chkIsReplaceAllItemStateChanged

    private void chkIsDeclareFinalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkIsDeclareFinalItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(DECLAREFINAL, b);
        parent.stateChanged(null);
    }//GEN-LAST:event_chkIsDeclareFinalItemStateChanged

    private void chkGenJavadocItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkGenJavadocItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
	RefactoringModule.setOption(GENJAVADOC, b); // NOI18N
	parent.stateChanged(null);
    }//GEN-LAST:event_chkGenJavadocItemStateChanged

    private void chkUpdateJavadocItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkUpdateJavadocItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
	RefactoringModule.setOption(UPDATEJAVADOC, b); // NOI18N
	parent.stateChanged(null);
    }//GEN-LAST:event_chkUpdateJavadocItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkGenJavadoc;
    private javax.swing.JCheckBox chkIsCompatible;
    private javax.swing.JCheckBox chkIsDeclareFinal;
    private javax.swing.JCheckBox chkIsReplaceAll;
    private javax.swing.JCheckBox chkUpdateJavadoc;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblName;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPanel.class, key);
    }

    public boolean isCompatible() {
        return chkIsCompatible.isSelected();
    }

    public boolean isDeclareFinal() {
        return chkIsDeclareFinal.isSelected();
    }

    public boolean isReplaceAll() {
        return chkIsReplaceAll.isSelected();
    }
    
    public String getParameterName() {
        return ((JEditorPane)singleLineEditor[1]).getText().substring(startOffset);
    }
    
    protected Javadoc getJavadoc() {
        if(chkUpdateJavadoc.isVisible() && chkUpdateJavadoc.isSelected()) {
            return Javadoc.UPDATE;
        } else if(chkGenJavadoc.isVisible() && chkGenJavadoc.isSelected()) {
            return Javadoc.GENERATE;
        } else {
            return Javadoc.NONE;
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        ((JEditorPane)singleLineEditor[1]).requestFocus();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Copy from org.netbeans.modules.java.hints.errors.Utilities">
    private static String makeNameUnique(CompilationInfo info, Scope s, String name, MethodTree method) {
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            if (s != null) {
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                    if (proposedName.equals(e.getSimpleName().toString())) {
                        counter++;
                        cont = true;
                        break;
                    }
                }
            }
        }
        
        return proposedName;
    }
    
    public static String getName(Tree et) {
        return adjustName(getNameRaw(et));
    }
    
    private static String getNameRaw(Tree et) {
        if (et == null)
            return null;
        
        switch (et.getKind()) {
            case IDENTIFIER:
                return ((IdentifierTree) et).getName().toString();
            case METHOD_INVOCATION:
                return getName(((MethodInvocationTree) et).getMethodSelect());
            case MEMBER_SELECT:
                return ((MemberSelectTree) et).getIdentifier().toString();
            case NEW_CLASS:
                return firstToLower(getName(((NewClassTree) et).getIdentifier()));
            case PARAMETERIZED_TYPE:
                return firstToLower(getName(((ParameterizedTypeTree) et).getType()));
            case STRING_LITERAL:
                String name = guessLiteralName((String) ((LiteralTree) et).getValue());
                if(name == null) {
                    return firstToLower(String.class.getSimpleName());
                } else {
                    return firstToLower(name);
                }
            case VARIABLE:
                return ((VariableTree) et).getName().toString();
            default:
                return null;
        }
    }
    
    static String adjustName(String name) {
        if (name == null)
            return null;
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return firstToLower(shortName);
        }
        
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
        }
    }
    
    private static String firstToLower(String name) {
        if (name.length() == 0)
            return null;
        
        StringBuilder result = new StringBuilder();
        boolean toLower = true;
        char last = Character.toLowerCase(name.charAt(0));
        
        for (int i = 1; i < name.length(); i++) {
            if (toLower && Character.isUpperCase(name.charAt(i))) {
                result.append(Character.toLowerCase(last));
            } else {
                result.append(last);
                toLower = false;
            }
            last = name.charAt(i);
            
        }
        
        result.append(last);
        
        if (SourceVersion.isKeyword(result)) {
            return "a" + name;
        } else {
            return result.toString();
        }
    }
    
    private static String guessLiteralName(String str) {
        StringBuilder sb = new StringBuilder();
        if(str.length() == 0)
            return null;
        char first = str.charAt(0);
        if(Character.isJavaIdentifierStart(str.charAt(0)))
            sb.append(first);
        
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == ' ') {
                sb.append('_');
                continue;
            }
            if (Character.isJavaIdentifierPart(ch))
                sb.append(ch);
            if (i > 40)
                break;
        }
        if (sb.length() == 0)
            return null;
        else
            return sb.toString();
    }
    
    public static final class VariablesFilter implements ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }
    //</editor-fold>
}
