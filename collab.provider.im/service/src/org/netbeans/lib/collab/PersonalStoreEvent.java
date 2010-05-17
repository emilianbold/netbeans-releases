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

package org.netbeans.lib.collab;

/**
 * This object will be used to notify the changes in the PersonalStoreEntry objects.
 *
 * @since version 0.1
 *
 */
public class PersonalStoreEvent {

    /**
     * indicates that the PersonalStoreEntry has been added
     */
    public static final int TYPE_ADDED = 0x1;

    /**
     * indicates that the PersonalStoreEntry has been modified
     */
    public static final int TYPE_MODIFIED = 0x2;

    /**
     * indicates that the PersonalStoreEntry has been removed
     */
    public static final int TYPE_REMOVED = 0x4;
    
    /**
     * indicates that the PersonalStoreEntry has been removed
     */
    public static final int TYPE_DATA = 0x8;
    
    int type;
    PersonalStoreEntry entry;
    Payload _payload;
    String key;
    
    public PersonalStoreEvent(int type, PersonalStoreEntry entry) {
        this.type = type;
        this.entry = entry;
    }

    public PersonalStoreEvent(int type, PersonalStoreEntry entry,
                String key, String data) {
        this.type  = type;
        this.entry = entry;
        this._payload   = new Payload(key, data);
    }

    /**
     * Returns the event type
     * @return int code for this event type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the entry associated with this event
     * @return PersonalStoreEntry object associated with the event
     */
    public PersonalStoreEntry getEntry() {
        return entry;
    }
    
    public String toString() {
        String s;
        switch(type) {
            case TYPE_ADDED:
                s = "ADDED";
                break;
            case TYPE_MODIFIED:
                s = "MODIFIED";
                break;
            case TYPE_REMOVED:
                s = "REMOVED";
                break;
            default:
                s = "";
        }
        return "Type : " + s + " Entry : " + entry.getEntryId(); 
    }
    
    public Payload getPayload() {
        return _payload;
    }
    
    public class Payload {
        String _name, _value;
        private Payload(String name, String value) {
            _name = name;
            _value = value;
        }
        public String getData() {
            return _value;
        }
        public String getName() {
            return _name;
        }
    }
}
