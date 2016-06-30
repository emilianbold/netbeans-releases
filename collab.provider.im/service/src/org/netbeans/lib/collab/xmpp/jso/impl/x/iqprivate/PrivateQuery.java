/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.lib.collab.xmpp;


import java.util.Map;
import java.util.Set;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.util.DigestHash;
import org.jabberstudio.jso.util.Utilities;

/**
 * <p>
 * Interface for representing "jabber:iq:private" queries. This interface is
 * designed to more conveniently retrieve and store private information
 * over an {@link org.jabberstudio.jso.Extension}.</p>
 *
 */
public interface PrivateQuery extends Extension
{

    public static final String  NAMESPACE = "jabber:iq:private";


    /**
     * <p>
     * Retrieves the set of field names for this <code>AuthQuery</code>.
     * The returned set reflects only those fields currently present.</p>
     *
     * <p>
     * "Fields" are any contained elements in the same namespace as
     * <code>AuthQuery</code>.</p>
     *
     * <p>
     * The value returned by this method is never <code>null</code>.</p>
     *
     * @return The set of field names present.
     * @since   JSO-0.4
     */
    public Set getFieldNames();
    /**
     * <p>
     * Retrieves the value for the given field in this
     * <code>AuthQuery</code>.</p>
     *
     * <p>
     * The value of <code>name</code> cannot be <code>null</code> or "", or an
     * <code>IllegalArgumentException</code> is thrown.</p>
     *
     * <p>
     * The value returned by this method may be <code>null</code>, if field
     * is not present.</p>
     *
     * @param name The field name.
     * @return The field value.
     * @throws IllegalArgumentException if the parameter is invalid.
     * @since   JSO-0.4
     */
    public String getField(String name) throws IllegalArgumentException;
    /**
     * <p>
     * Sets the value for the given field in this <code>AuthQuery</code>.
     * This method always results in the field being "set".  To remove a field,
     * use {@link #unsetField} instead.</p>
     *
     * <p>
     * The value of <code>name</code> cannot be <code>null</code> or "", or an
     * <code>IllegalArgumentException</code> is thrown.</p>
     *
     * @param name The field name.
     * @throws IllegalArgumentException if the parameter is invalid.
     * @since   JSO-0.4
     */
    public void setField(String name, String value) throws IllegalArgumentException;
    /**
     * <p>
     * Removes the field from this <code>AuthQuery</code>.</p>
     *
     * <p>
     * The value of <code>name</code> cannot be <code>null</code> or "", or an
     * <code>IllegalArgumentException</code> is thrown.</p>
     *
     * @param name The field name.
     * @throws IllegalArgumentException if the parameter is invalid.
     * @since   JSO-0.4
     */
    public void unsetField(String name) throws IllegalArgumentException;

}
