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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeListener;

/*
 * Interface for slide in and slide out operations. Acts as command interface
 * for desktop part of winsys to be able to request slide operation.
 *
 * @author Dafe Simonek
 */
public interface SlidingFx {

    public void prepareEffect (SlideOperation operation);

    public void showEffect(JLayeredPane pane, Integer layer, SlideOperation operation);

    public boolean shouldOperationWait();
    
    public void setFinishListener(ChangeListener finishL);
    
}