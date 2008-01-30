/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.api.server.properties;

/**
 * The set of persisted properties.
 *
 * @author Petr Hejl
 */
public abstract class InstanceProperties {

    private final String id;

    /**
     * Creates the new InstanceProperties.
     *
     * @param id id of the properties, unique in the scope of the namespace
     * @see InstancePropertiesManager
     */
    public InstanceProperties(String id) {
        this.id = id;
    }

    /**
     * Returns unique id of these properties. It is guaranteed that this id is
     * unique in the scope of single namespace used in manager (however it
     * is not related directly to it).
     * <p>
     * Client may use it for its own purposes (don't have to), but client
     * can't influence the actual value of id in any way.
     *
     * @return id of the properties unique in the scope of the property set
     * @see InstancePropertiesManager
     * @see InstancePropertiesManager#createProperties(String)
     */
    public final String getId() {
        return id;
    }

    public abstract String getString(String key, String def);

    public abstract void putString(String key, String value);

    public abstract boolean getBoolean(String key, boolean def);

    public abstract void putBoolean(String key, boolean value);

    public abstract int getInt(String key, int def);

    public abstract void putInt(String key, int value);

    public abstract long getLong(String key, long def);

    public abstract void putLong(String key, long value);

    public abstract float getFloat(String key, float def);

    public abstract void putFloat(String key, float value);

    public abstract double getDouble(String key, double def);

    public abstract void putDouble(String key, double value);

    public abstract void removeKey(String key);

    public abstract void remove();

}
