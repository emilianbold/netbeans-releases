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

package org.netbeans.modules.web.jsf.api.editor;


import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl
 */
public interface JSFConfigEditorContext {

    /**
     * The method provides the faces configuration file, for which the editor is opened.
     * @return faces configuration file
     */
    public FileObject getFacesConfigFile();
    
    /**
     * Provide UndoRedo manager for the editor.
     * @return 
     */
    public UndoRedo getUndoRedo();
    
    /**
     * This method should be called by from the implementation of 
     * MultiViewElement.setMultiViewCallback. The editor needs to know, which TopComponent
     * is now displayed.
     * @param topComponent which is displayed 
     */
    public void setMultiViewTopComponent(TopComponent topComponent);
}
