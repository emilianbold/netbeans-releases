/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.execution;

import java.util.concurrent.Future;

/**
 * Validateable objects have the ability to validate their internal state
 * against some object of type <tt>T</tt> and report validation errors
 * and/or required actions (to proceed validation) to calling code.
 *
 * @param <T> type of an object that is passed to <tt>validate</tt> method.
 */
public interface Validateable<T> {

    /**
     * Perform validation against provided <tt>objectToValidate</tt>
     * @param objectToValidate object that validation should be performed
     * against.
     * @return a Future representing pending completion of the validation
     */
    public ValidationStatus validate(T objectToValidate);

    /**
     * Discards previous result of <tt>validate</tt> method.
     */
    public void invalidate();

    /**
     * Returns <tt>ValidationStatus</tt> of most recent completed validation.
     * @return <tt>ValidationStatus</tt> of most recent completed validation.
     */
    public ValidationStatus getValidationStatus();

    /**
     * Adds <tt>ValidationListener</tt> listener.
     * @param listener listener to be added
     */
    public void addValidationListener(ValidationListener listener);

    /**
     * Remove <tt>ValidationListener</tt> listener.
     * @param listener listener to be removed
     */
    public void removeValidationListener(ValidationListener listener);
}
