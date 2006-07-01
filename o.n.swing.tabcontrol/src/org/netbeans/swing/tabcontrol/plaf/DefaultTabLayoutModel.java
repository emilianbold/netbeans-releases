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
 * DefaultTabLayoutModel.java
 *
 * Created on April 2, 2004, 3:59 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.JComponent;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 * Default implementation of TabLayoutModel.  Simply provides a series of
 * rectangles for each tab starting at 0 and ending at the last element, with
 * the width set to the calculated width for the string plus a padding value
 * assigned in <code>setPadding</code>.
 * <p>
 * To implement TabLayoutModel, it is often useful to create an implementation which
 * wraps an instance of <code>DefaultTabLayoutModel</code>, and uses it to calculate
 * tab sizes.
 *
 * @author Tim Boudreau
 */
public final class DefaultTabLayoutModel extends BaseTabLayoutModel {
    
    /** Creates a new instance of DefaultTabLayoutModel */
    public DefaultTabLayoutModel(TabDataModel model, JComponent renderTarget) {
        super (model, renderTarget);
    }
    
}
