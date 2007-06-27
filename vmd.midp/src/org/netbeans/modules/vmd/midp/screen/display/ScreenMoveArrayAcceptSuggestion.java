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
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;


/**
 *
 * @author Karol Harezlak
 */
public class ScreenMoveArrayAcceptSuggestion implements AcceptSuggestion {
    
    private ScreenDeviceInfo.Edge horizontalPosition;
    private ScreenDeviceInfo.Edge verticalPosition;
    
    public ScreenMoveArrayAcceptSuggestion(ScreenDeviceInfo.Edge horizontalPosition, ScreenDeviceInfo.Edge verticalPosition) {
        this.horizontalPosition = horizontalPosition;
        this.verticalPosition = verticalPosition;
    }
    
    public ScreenDeviceInfo.Edge getHorizontalPosition() {
        return horizontalPosition;
    }
    
    public ScreenDeviceInfo.Edge getVerticalPosition() {
        return verticalPosition;
    }

}
