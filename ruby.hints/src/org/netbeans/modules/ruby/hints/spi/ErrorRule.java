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

import java.util.List;
import java.util.Set;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Error;

/** 
 * Represents a rule to be run on the java source in case the compiler 
 * issued an error or a warning.
 *
 * (Copied from java/hints)
 * 
 *
 * @author Petr Hrebejk, Jan Lahoda
 */
public interface ErrorRule extends Rule {//XXX: should ErrorRule extend Rule?

    /** Get the diagnostic codes this rule should run on
     */
    public Set<String> getCodes();

    /** Return possible fixes for a given diagnostic report.
     */
    public void run(CompilationInfo compilationInfo, Error error/*, Data<T> data*/,
             List<Description> result);

}
