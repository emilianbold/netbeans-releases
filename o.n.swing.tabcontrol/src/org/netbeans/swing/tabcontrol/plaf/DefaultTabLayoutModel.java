/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
