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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midp.components.general;

import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public abstract class Bitmask {

    private int bitmask;

    public Bitmask(int bitmask) {
        this.bitmask = bitmask;
    }

    /**
     * Gets enum all available values
     * @return
     */
    public abstract List<BitmaskItem> getBitmaskItems();


    public int getBitmask() {
        return bitmask;
    }

    public boolean isSet(BitmaskItem item) {
        int affectedBits = item.getAffectedBits();
        return (affectedBits & bitmask) == affectedBits;
    }
    
    
    public int addToBitmask(BitmaskItem item, boolean value) {
        if (value) {
            bitmask |= item.getAffectedBits();
        } else {
            bitmask &= ~item.getAffectedBits();
        }
        return bitmask;
    }
    
    public int setBitmask(int bitmask) {
        this.bitmask = bitmask;
        return this.bitmask;
    }
    
    public boolean equals(Bitmask bitmask) {
        if (this == bitmask) {
            return true;
        } else {
            // need to check the bitmask value
            return this.bitmask == bitmask.bitmask;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bitmask) {
            return equals((Bitmask)obj);
        } else {
            return false;
        }
    }
    
    public BitmaskItem getBitmaskItem(int affectedBits) {
        for (BitmaskItem bitmaskItem : getBitmaskItems()) {
            if (bitmaskItem.getAffectedBits() == affectedBits)
                return bitmaskItem;
        }
        
        return null;
    }
    
    public BitmaskItem getBitmaskItem(String displayName) {
        for (BitmaskItem bitmaskItem : getBitmaskItems()) {
            if (bitmaskItem.getDisplayName().equals(displayName))
                return bitmaskItem;
        }
        
        return null;
    }
    
    /**
     * Individual bit mask item
     * @author breh
     * @version
     */
    public final  static class BitmaskItem {
        
        private String name;
        private String displayName;
        private int affectedBits;
        
        
        public BitmaskItem(String name, int affectedBits) {
            this(affectedBits, null, name);
        }
        
        public BitmaskItem( int affectedBits, String displayName, String name) {
            if (name == null) throw new NullPointerException("Name parameter cannot be null"); // NOI18N
            this.name = name;
            if (displayName != null) {
                this.displayName = displayName;
            } else {
                this.displayName = this.name;
            }
            this.affectedBits = affectedBits;
        }
        
        /**
         * Gets display name of this element (usefull for property editors)
         * @return
         */
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Gets name of this element (useful for code genrators)
         * @return
         */
        public String getName() {
            return name;
        }
        
        /**
         * Gets affectedBits of this individual bitmask item (i.e. which bits are
         * set when this items set is true)
         * @return
         */
        public int getAffectedBits() {
            return affectedBits;
        }
        
    }
    
    
}
