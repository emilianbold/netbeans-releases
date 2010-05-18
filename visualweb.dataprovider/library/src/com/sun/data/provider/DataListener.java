/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package com.sun.data.provider;

import java.util.EventListener;

/**
 * <p>DataListener is an event listener interface that supports
 * processing events produced by a corresponding {@link DataProvider}
 * instance.  Specialized subinterfaces of this interface add support for
 * events produced by corresponding specialised {@link DataProvider} instances
 * that implement specialized subinterfaces of that API.</p>
 *
 * @author Joe Nuxoll
 */
public interface DataListener extends EventListener {

    /**
     * <p>Process an event indicating that a data element's value has been
     * changed.</p>
     *
     * @param provider <code>DataProvider</code> containing the data element
     *                 that has had a value change
     * @param fieldKey <code>FieldKey</code> representing the specific
     *                data element that has had a value change
     * @param oldValue The old value of this data element
     * @param newValue The new value of this data element
     */
    public void valueChanged(DataProvider provider, FieldKey fieldKey,
                             Object oldValue, Object newValue);

    /**
     * <p>Process an event indicating that the DataProvider has changed in
     * a way outside the bounds of the other event methods.</p>
     *
     * @param provider The DataProvider that has changed
     */
    public void providerChanged(DataProvider provider);
}
