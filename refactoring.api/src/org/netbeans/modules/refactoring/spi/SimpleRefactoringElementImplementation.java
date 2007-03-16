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

package org.netbeans.modules.refactoring.spi;

import java.awt.Container;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.JEditorPane;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.refactoring.spi.impl.ParametersPanel;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Default implementation of RefactoringElementImplementation interface.
 * It contains implementations of
 * @see RefactoringElementImplementation#isEnabled()
 * @see RefactoringElementImplementation#setEnabled(boolean)
 * @see RefactoringElementImplementation#getStatus()
 * @see RefactoringElementImplementation#setStatus(int) and
 * @see RefactoringElementImplementation#openInEditor()
 * @see RefactoringElementImplementation#showPreview()
 * @author Jan Becicka
 * @see RefactoringElementImplementation
 * @since 1.5.0
 */
public abstract class SimpleRefactoringElementImplementation implements RefactoringElementImplementation {
    
    private boolean enabled = true;
    private int status = NORMAL;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void openInEditor() {
        
        PositionBounds bounds = getPosition();
        if (bounds == null)
            return;
        
        PositionRef beginPos=bounds.getBegin();
        CloneableEditorSupport editSupp=beginPos.getCloneableEditorSupport();
        editSupp.edit();
        JEditorPane[] panes=editSupp.getOpenedPanes();
        
        if (panes!=null) {
            panes[0].setCaretPosition(bounds.getEnd().getOffset());
            panes[0].moveCaretPosition(beginPos.getOffset());
            getTopComponent(panes[0]).requestActive();
        } else {
            // todo (#pf): what to do if there is no pane? -- now, there
            // is a error message. I'm not sure, maybe this code will be
            // never called.
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ParametersPanel.class,"ERR_ErrorOpeningEditor"))
                    );
        }
    }
    
    public void showPreview() {
        try {
            Reader r2 = getReader();
            if (r2!=null) {
                DiffView diffView = null;
                Reader r1 = getFirstReader();
                FileObject f = getParentFile();
                diffView = Diff.getDefault().createDiff(
                        StreamSource.createSource(f.getName(), f.getNameExt(), getMimeType(), r1),
                        StreamSource.createSource("Proposed refactoring", "Refactored " + f.getNameExt(), getMimeType(), r2));
                UI.setComponentForRefactoringPreview(diffView.getComponent());
            } else {
                UI.setComponentForRefactoringPreview(null);
            }
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
    }
    
    private Reader getReader() {
        String newText = getNewFileContent();
        if (newText==null)
            return null;
        return new StringReader(newText);
    }
    
    /**
     * this method is under development. Might be removed in final release.
     * Do not override it so far.
     * return String representation of whole file after refactoring
     * @return 
     */ 
    protected String getNewFileContent() {
        return null;
    }
    
    private Reader getFirstReader() throws IOException {
        PositionRef beginPos=getPosition().getBegin();
        CloneableEditorSupport editSupp=beginPos.getCloneableEditorSupport();
        return new InputStreamReader(editSupp.getInputStream());
    }
    
    private String getMimeType() {
        return getParentFile().getMIMEType();
    }

    private static final TopComponent getTopComponent(Container temp) {
        while (!(temp instanceof TopComponent)) {
            temp = temp.getParent();
        }
        return (TopComponent) temp;
    }
    
    public void undoChange() {
    }
    
}
