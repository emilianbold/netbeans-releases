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

/*
 * WSEditor.java
 *
 * Created on March 9, 2006, 2:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.wseditor.spi;

import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.nodes.Node;

/**
 *
 * @author Roderico Cruz
 */
public interface WSEditor {
    /**
     * Return the main panel of the editor
     */
    JComponent createWSEditorComponent(Node node, JaxWsModel jaxWsModel);   

    /**
     * The title text that will be displayed in the tab corresponding
     * to the editor.
     */
    String getTitle();
    
    /**
     * This is called when the OK button is selected 
     */
    void save(Node node, JaxWsModel jaxWsModel);
    
    /**
     * This is called when the Cancel button is selected
     */
    void cancel(Node node, JaxWsModel jaxWsModel);
}
