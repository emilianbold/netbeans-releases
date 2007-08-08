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

package org.netbeans.modules.vmd.midp.components.general;

import java.util.List;

/**
 *
 * @author Martin Brehovsky, Karol Harezlak
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
