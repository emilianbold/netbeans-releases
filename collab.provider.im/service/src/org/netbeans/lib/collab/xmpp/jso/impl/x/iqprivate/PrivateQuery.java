/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved. 
 * 
 * Use is subject to the terms of the Sun Industry Standards Source License, 
 * a copy of which must accompany the software distribution.  License terms are
 * available at http://www.opensource.org/licenses/sisslpl.php .
 *
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
