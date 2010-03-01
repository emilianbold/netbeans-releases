/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.api.configurations;

public class BooleanConfiguration {

    private BooleanConfiguration master;
    private boolean def;
    private String falseValue;
    private String trueValue;
    private boolean value;
    private boolean modified;
    private boolean dirty = false;

    public BooleanConfiguration(BooleanConfiguration master, boolean def) {
        this.master = master;
        this.def = def;
        falseValue = ""; // NOI18N
        trueValue = ""; // NOI18N
        reset();
    }

    public BooleanConfiguration(BooleanConfiguration master, boolean def, String falseValue, String trueValue) {
        this.master = master;
        this.def = def;
        this.falseValue = falseValue;
        this.trueValue = trueValue;
        reset();
    }

    protected BooleanConfiguration getMaster() {
        return master;
    }

    public void setMaster(BooleanConfiguration master) {
        this.master = master;
    }

    public void setValue(boolean b) {
        this.value = b;
        if (master != null) {
            setModified(true);
        } else {
            setModified(b != getDefault());
        }
    }

    public boolean getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }

    public final void setModified(boolean b) {
        this.modified = b;
    }

    public boolean getModified() {
        return modified;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    public boolean getDefault() {
        return def;
    }

    public void setDefault(boolean b) {
        def = b;
        setModified(value != def);
    }

    public final void reset() {
        value = getDefault();
        setModified(false);
    }

    public String getOption() {
        if (getValue()) {
            return trueValue;
        } else {
            return falseValue;
        }
    }

    // Clone and Assign
    public void assign(BooleanConfiguration conf) {
        dirty |= conf.getValue() ^ getValue();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public BooleanConfiguration clone() {
        BooleanConfiguration clone = new BooleanConfiguration(master, def, falseValue, trueValue);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }
}
