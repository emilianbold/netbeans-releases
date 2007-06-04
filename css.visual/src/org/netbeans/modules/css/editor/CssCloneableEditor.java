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

package org.netbeans.modules.css.editor;

import org.openide.text.CloneableEditor;
import org.openide.util.HelpCtx;

/**
 * CSS Cloneable Editor TopComponent
 * @author Winston Prakash
 * @author Marek Fukala
 * @version 1.0
 */

public class CssCloneableEditor extends CloneableEditor{

    public CssCloneableEditor() {
        super();
    }

    public CssCloneableEditor(CssEditorSupport support) {
        super(support);
    }

    public HelpCtx getHelpCtx() {
        //TODO marek: what to do with the help ID????
        return new HelpCtx("projrave_ui_elements_editors_about_css_editor") ; // NOI18N
    }

    public void componentActivated(){
        super.componentActivated();
        ((CssEditorSupport)cloneableEditorSupport()).cssTCActivated(this);
    }
    
    public void componentDeactivated(){
        super.componentDeactivated();
        ((CssEditorSupport)cloneableEditorSupport()).cssTCDeactivated(this);
    }
}
