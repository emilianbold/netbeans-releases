/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * SecurityWSEditorProvider.java
 *
 * Created on April 10, 2006, 5:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.profile.ui.editor;

import java.io.File;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProvider;
import org.openide.nodes.Node;

/**
 * Security editor provider for the web service attribuet editor.
 *
 * Created on April 10, 2006, 5:54 PM
 *
 * @author Vidhya Narayanan
 */
public class SecurityWSEditorProvider implements WSEditorProvider {

    /** Creates a new instance of SecurityWSEditorProvider */
    public SecurityWSEditorProvider() {
    }
    
    public WSEditor createWSEditor() {
        return new SecurityWSEditor();
    }
    
    public boolean enable(Node node) {
        return true;
    }
    
    
}
