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

package org.netbeans.spi.navigator;

/** Interface for SPI clients who want to affect searching mechanism for
 * available NavigatorPanel implementations.<p></p>
 * 
 * Navigator infrastructure searches for instance of this interface in
 * <a href="@org-openide-util@/org/openide/util/Utilities.html#actionsGlobalContext()">Utilities.actionsGlobalContext()</a>
 * lookup and then applies found policy on set of available 
 * <a href="@TOP@/org/netbeans/spi/navigator/NavigatorPanel.html">NavigatorPanel</a>
 * implementations.<p></p>
 * 
 * Note that multiple instances of this interface are not supported in
 * Utilities.actionsGlobalContext() lookup, one instance is chosen randomly
 * in this case.<p></p>
 * 
 * Common Usage: 
 *  <ul>
 *      <li>Implement this interface, return kind of policy that suits you from
 *          <code>getPanelsPolicy()</code></li> method.
 *      <li>Put implementation instance into your TopComponent's subclass lookup,
 *          see <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent.getLookup()</a>
 *          for details.</li>
 *      <li>Now when your TopComponent becomes active in the system, found
 *          panels policy is used to limit/affect set of available NavigatorPanel
 *          implementations.</li>
 *  </ul>
 * 
 * @since 1.6
 *
 * @author Dafe Simonek
 */
public interface NavigatorLookupPanelsPolicy {
    
    /** Shows only NavigatorPanel implementations available through
     * <a href="@TOP@/org/netbeans/spi/navigator/NavigatorLookupHint.html">NavigatorLookupHint</a>
     * in Navigator window, hides NavigatorPanels
     * available from DataObject of active Node.<br></br>
     * 
     * Use when you want to remove NavigatorPanels of active Node from Navigator
     * window. 
     */
    public static final int LOOKUP_HINTS_ONLY = 1;
    
    /** Returns policy for available Navigator panels. Currently only 
     * LOOKUP_HINTS_ONLY policy is supported.
     * 
     * @return Navigator panels policy constant.
     */
    public int getPanelsPolicy ();
    
}
