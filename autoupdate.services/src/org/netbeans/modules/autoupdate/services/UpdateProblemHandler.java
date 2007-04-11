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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.api.autoupdate.*;
import java.util.Set;
import org.openide.modules.Dependency;

/** Callback interface which interacts with user when needs user's input.
 *
 * @author Jiri Rechtacek (jrechtacek@netbeans.org)
 */
public abstract class UpdateProblemHandler {
    
    /** Notifies an user that some dependency seems broken and allows
     * to decide if can ignore it or not.
     * 
     * @param dependency a dependency what seems as broken
     * @return true if can ignore it
     */
    public boolean ignoreBrokenDependency (Dependency dependency) {
        return true;
    }
    
    /** Notifies an user that some <code>UpdateElement</code> should be added
     * among elements what is being performed.
     * An user can decide if elements should be added or not.
     * 
     * @param elements list of <code>UpdateElement</code> what are required
     * @return true if an user agree to add them
     */
    public boolean addRequiredElements (Set<UpdateElement> elements) {
        return true;
    }
    
    /** Notifies an user the <code>UpdateElement</code> which is being performed
     * is not trusted but has a suspect state.
     * 
     * @param state suspect state
     * @param element <code>UpdateElement</code> already performed
     * @return true if allow untrusted element
     */
    public abstract boolean allowUntrustedUpdateElement (String state, UpdateElement element);
    
    /** Ask an user for approve the license agreement of handing <code>UpdateElement</code>.
     * 
     * @param license content of license
     * @return true if an user accpets the license agreement
     */
    public abstract boolean approveLicenseAgreement (String license);
    
    /** Sometimes completion of the proceeding operation needs restart of IDE. In this
     * case an user should decide if the IDE will restart now or later.
     *
     * 
     * @return true if IDE can be restart now
     */
    public boolean restartNow () {
        return true;
    }
    
}
