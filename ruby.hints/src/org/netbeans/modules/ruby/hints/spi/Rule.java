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

package org.netbeans.modules.ruby.hints.spi;

/** Represents a rule to be run on the source.
 * Only contains the basic identification and UI properties of the rule. 
 * Instances of the rules can be placed into the system filesystem.
 * 
 * (Copied from java/hints)
 *
 * @author Petr Hrebejk
 */
public interface Rule {
    
    /** Gets unique ID of the rule
     */
    public String getId();

    /** Get's UI usable name of the rule
     */
    public String getDisplayName();

    /** Gets longer description of the rule
     */
    public String getDescription();

    // XXX Add Others
    // public JPanel getCustomizer() or Hash map getParameters()

}
