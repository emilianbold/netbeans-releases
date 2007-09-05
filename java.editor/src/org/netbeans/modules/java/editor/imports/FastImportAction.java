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
package org.netbeans.modules.java.editor.imports;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class FastImportAction extends BaseAction {
    
    public static final String NAME = "fast-import";
    
    /** Creates a new instance of FastImportAction */
    public FastImportAction() {
        super(NAME);
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        try {
            final Rectangle carretRectangle = target.modelToView(target.getCaretPosition());
            final Font font = target.getFont();
            final Point where = new Point( carretRectangle.x, carretRectangle.y + carretRectangle.height );
            SwingUtilities.convertPointToScreen( where, target);

            final int position = target.getCaretPosition();
            final String ident = Utilities.getIdentifier(Utilities.getDocument(target), position);
            FileObject file = getFile(target.getDocument());
            
            if (ident == null || file == null) {
                Toolkit.getDefaultToolkit().beep();
                return ;
            }
            
            JavaSource js = JavaSource.forFileObject(file);
            
            if (js == null) {
                Toolkit.getDefaultToolkit().beep();
                return ;
            }
            
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(final CompilationController parameter) throws IOException {
                    parameter.toPhase(Phase.RESOLVED);
                    final JavaSource javaSource = parameter.getJavaSource();
                    Pair<Map<String, List<TypeElement>>, Map<String, List<TypeElement>>> result = new ComputeImports().computeCandidates(parameter, Collections.singleton(ident));
                    
                    final List<TypeElement> priviledged = result.a.get(ident);
                    
                    if (priviledged == null) {
                        //not found?
                        Toolkit.getDefaultToolkit().beep();
                        return ;
                    }
                    
                    final List<TypeElement> denied = new ArrayList<TypeElement>(result.b.get(ident));
                    
                    denied.removeAll(priviledged);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ImportClassPanel panel = new ImportClassPanel(priviledged, denied, font, javaSource, position);
                            PopupUtil.showPopup(panel, "", where.x, where.y, true, carretRectangle.height );
                        }
                    });
                }
            }, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    
    private FileObject getFile(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null)
            return null;
        
        return od.getPrimaryFile();
    }
}
