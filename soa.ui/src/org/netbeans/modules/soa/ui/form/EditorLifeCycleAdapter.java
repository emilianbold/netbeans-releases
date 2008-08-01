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
package org.netbeans.modules.soa.ui.form;

import javax.swing.JPanel;

/**
 * The default implementation of the EditorLifeCycle.
 *
 * @author nk160297
 */
public class EditorLifeCycleAdapter extends JPanel implements EditorLifeCycle {

    static final long serialVersionUID = 1L;

    public EditorLifeCycleAdapter() {
        super();
    }

    public boolean initControls() {
        return true;
    }

    public void createContent() {
    }

    public boolean subscribeListeners() {
        return true;
    }

    public boolean unsubscribeListeners() {
        return true;
    }

    public boolean applyNewValues() throws Exception {
        return true;
    }

    public boolean afterClose() {
        return true;
    }
    
    public boolean idValid() {
        return true;
    }
    
}
