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
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.java.editor.codegen.ui.GenerateCodePanel;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class GenerateCodeAction extends BaseAction {

    public static final String generateCode = "generate-code"; //NOI18N
    
    private CodeGenerator.Factory[] generators = new CodeGenerator.Factory[] {
        new ConstructorGenerator.Factory(),
        new GetterSetterGenerator.Factory(),
        new EqualsHashCodeGenerator.Factory(),
        new DelegateMethodGenerator.Factory(),
        new ImplementOverrideMethodGenerator.Factory()
    };

    public GenerateCodeAction(){
        super(generateCode);
        putValue(ExtKit.TRIMMED_TEXT, NbBundle.getBundle(GenerateCodeAction.class).getString("generate-code-trimmed")); //NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getBundle(GenerateCodeAction.class).getString("desc-generate-code")); //NOI18N
        putValue(POPUP_MENU_TEXT, NbBundle.getBundle(GenerateCodeAction.class).getString("popup-generate-code")); //NOI18N
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        try {
            JavaSource js = JavaSource.forDocument(target.getDocument());
            if (js != null) {
                final int caretOffset = target.getCaretPosition();
                final ArrayList<CodeGenerator> gens = new ArrayList<CodeGenerator>();
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.PARSED);
                        TreePath path = controller.getTreeUtilities().pathFor(caretOffset);
                        for (CodeGenerator.Factory factory : getCodeGeneratorFactories()) {
                            for (CodeGenerator gen : factory.create(controller, path))
                                gens.add(gen);
                        }
                    }
                }, true);
                if (gens.size() > 0) {
                    Rectangle carretRectangle = target.modelToView(target.getCaretPosition());
                    Point where = new Point( carretRectangle.x, carretRectangle.y + carretRectangle.height );
                    SwingUtilities.convertPointToScreen( where, target);
                    GenerateCodePanel panel = new GenerateCodePanel(target, gens);
                    PopupUtil.showPopup(panel, null, where.x, where.y, true, carretRectangle.height);
                } else {
                    target.getToolkit().beep();
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
    
    private CodeGenerator.Factory[] getCodeGeneratorFactories() {
        return generators;
    }    
}
