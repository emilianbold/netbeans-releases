/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.chat.messagetype;

import java.awt.Image;


/**
 *
 * @author  Smitha Krishna Nagesh
 */
public abstract class CollabContentType {
    public abstract String getDisplayName();

    public abstract Image getIcon();

    public abstract String getContentType();
    
}
