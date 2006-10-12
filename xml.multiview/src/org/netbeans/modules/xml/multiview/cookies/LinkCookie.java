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

package org.netbeans.modules.xml.multiview.cookies;

/** 
 * This interface should be implemented by classes that need
 * to respond to pressing of links. See related class 
 * {@link org.netbeans.modules.xml.multiview.ui.LinkButton}.
 * 
 * Created on November 19, 2004, 8:52 AM
 * @author mkuchtiak
 */
public interface LinkCookie {
    
    /**
     * Invoked when a button representing a link is pressed.
     * @param ddBean the model that is affected by the link.
     * @param ddProperty the property of the given <code>ddBean</code>
     * that is affected by the link.
     */
    public void linkButtonPressed(Object ddBean, String ddProperty);
}
