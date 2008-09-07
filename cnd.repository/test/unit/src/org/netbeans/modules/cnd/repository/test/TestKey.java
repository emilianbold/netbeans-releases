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

package org.netbeans.modules.cnd.repository.test;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


/**
 * Test interface Implementation 
 * for tests
 * @author Vladimir Kvashin
 */
public class TestKey implements Key, SelfPersistent {
    
    private String key;
    private String unit;
    private Behavior behavior;
    
    public Behavior getBehavior() {
	return Behavior.Default;
    }
    
    public TestKey(String key, String unit, Behavior behavior) {
	this.key = key;
        this.unit = unit;
        this.behavior = behavior;
    }
    

    public TestKey(DataInput stream) throws IOException {
        this(stream.readUTF(), stream.readUTF(), 
                stream.readBoolean() ? Behavior.LargeAndMutable : Behavior.Default);
    }
    
    
    public String getAt(int level) {
	return key;
    }
    
    public int getDepth() {
	return 1;
    }
    
    public PersistentFactory getPersistentFactory() {
	return TestFactory.instance();
    }
    
    public int getSecondaryAt(int level) {
	return 0;
    }
    
    public int getSecondaryDepth() {
	return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestKey other = (TestKey) obj;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.unit != other.unit && (this.unit == null || !this.unit.equals(other.unit))) {
            return false;
        }
        if (this.behavior != other.behavior) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 59 * hash + (this.unit != null ? this.unit.hashCode() : 0);
        hash = 59 * hash + (this.behavior != null ? this.behavior.hashCode() : 0);
        return hash;
    }

    
    @Override
    public String toString() {
	return unit + ':' + key + ' ' + behavior;
    }

    public String getUnit() {
	return unit;
    }

    public int getUnitId() {
        return 0;
    }

    public void write(DataOutput output) throws IOException {
        output.writeUTF(key);
        output.writeUTF(unit);
        output.writeBoolean(behavior == Behavior.LargeAndMutable);
    }

}
