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
 * InnerPanelFactory.java
 *
 * Created on November 22, 2004, 6:45 PM
 */

package org.netbeans.modules.xml.multiview.ui;

/** InnerPanelFactory.java
 *  Factory for dynamic inner panels - bodies of section panels
 *
 * Created on November 22, 2004, 6:45 PM
 * @author mkuchtiak
 */
public interface InnerPanelFactory {
    /** Creates SectionInnerPanel object from the key, e.g. for bean obtained from DD-API
     */
    public SectionInnerPanel createInnerPanel(Object key);
}
