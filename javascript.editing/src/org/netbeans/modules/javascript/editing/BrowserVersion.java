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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.EnumSet;

/**
 *
 * @author Tor Norbye
 */
public enum BrowserVersion {
    // Keep synced with version in generatestubs
    IE55("Internet Explorer 5.5"), 
    IE6("Internet Explorer 6"), 
    IE7("Internet Explorer 7"), 
    FF1("FireFox 1.x"), 
    FF2("FireFox 2.x"),
    FF3("FireFox 3.x"), 
    OPERA("Opera"), 
    SAFARI2("Safari 2"), 
    SAFARI3("Safari 3"), 
    KONQ("Konqueror");
    private static final int COUNT = 10; // Number of enum entries

    private String displayName;
    
    BrowserVersion(String displayName) {
        this.displayName = displayName;
    } 
    
    static EnumSet<BrowserVersion> ALL = EnumSet.allOf(BrowserVersion.class);
    static EnumSet<BrowserVersion> IE_ONLY = EnumSet.of(IE55, IE6, IE7);
        
    public String getDisplayName() {
        return displayName;
    }

    public static EnumSet<BrowserVersion> fromFlags(String flags) {
        EnumSet<BrowserVersion> es = EnumSet.noneOf(BrowserVersion.class);

        String[] versions = flags.split("\\|"); // NOI18N
        for (String version : versions) {
            BrowserVersion b = BrowserVersion.valueOf(version);

            if (b != null) {
                es.add(b);
            } else {
                // What do I do here?
                assert false : version;
            }
        }

        return es;
    }

    public static String toFlags(EnumSet<BrowserVersion> es) {
        StringBuilder sb = new StringBuilder();
        for (BrowserVersion b : es) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(b.name());
        }
        return sb.toString();
    }

    public static String toCompactFlags(EnumSet<BrowserVersion> es) {
        if (es.size() == COUNT) {
            return "all";
        }

        // TODO - compact format
        StringBuilder sb = new StringBuilder();
        for (BrowserVersion b : es) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(b.name());
        }
        return sb.toString();
    }

    public static EnumSet<BrowserVersion> fromCompactFlags(String flags) {
        if ("all".equals(flags)) {
            return ALL;
        }
        
        // TODO - compact format
        EnumSet<BrowserVersion> es = EnumSet.noneOf(BrowserVersion.class);

        String[] versions = flags.split("\\|"); // NOI18N
        for (String version : versions) {
            BrowserVersion b = BrowserVersion.valueOf(version);

            if (b != null) {
                es.add(b);
            } else {
                // What do I do here?
                assert false : version;
            }
        }

        return es;
    }

}
