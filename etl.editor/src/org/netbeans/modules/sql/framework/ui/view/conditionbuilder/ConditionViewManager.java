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

package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionViewManager {

    private static ConditionViewManager instance;

    public static ConditionViewManager getDefault() {
        if (instance == null) {
            instance = new ConditionViewManager();
        }

        return instance;
    }
    private ConditionBuilderView cBuilder;

    /** Creates a new instance of ConditionViewManager */
    private ConditionViewManager() {
    }

    public ConditionBuilderView getCurrentConditionBuilderView() {
        return this.cBuilder;
    }

    public void setCurrentConditionBuilderView(ConditionBuilderView cView) {
        this.cBuilder = cView;
    }

}

