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

package  org.netbeans.modules.web.taglib;

import java.beans.*;
import java.awt.Image;

/** TLD Loader bean info.
*
* @author Simran Gleason
*/
public class TLDLoaderBeanInfo extends SimpleBeanInfo {

    /** @param type Desired type of the icon
     * @return returns the Image loader's icon
     */
    public Image getIcon(int type) {
        return org.openide.util.Utilities.loadImage("org/netbeans/modules/web/taglib/resources/tags.gif"); // NOI18N
    }
}

