/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.platform.gems;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class GemListParser {

    private GemListParser() {
        // no instances
    }

    private static final Logger LOGGER = Logger.getLogger(GemListParser.class.getName());

    static List<Gem> parseLocal(final List<? extends String> output) {
        return parse(output, true);
    }

    static List<Gem> parseRemote(final List<? extends String> output) {
        return parse(output, false);
    }

    private static List<Gem> parse(final List<? extends String> output, final boolean local) {
        LOGGER.finer("Going to parse Gem list");
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Using the following output:");
            LOGGER.finest("=== Output Start ===");
            for (String line : output) {
                LOGGER.finest(line);
            }
            LOGGER.finest("=== Output End ===");
        }
        Gem gem = null;
        List<Gem> gems = new ArrayList<Gem>();
        for (String line : output) {
            if (line.length() == 0 || Character.isWhitespace(line.charAt(0))) {
                if (gem != null) {
                    String description = line.trim();

                    if (gem.getDescription() == null) {
                        gem.setDescription(description);
                    } else {
                        gem.setDescription(gem.getDescription() + "\n" + description); // NOI18N
                    }
                }
            } else {
                if (line.charAt(0) == '.') {
                    continue;
                }

                // Should be a gem - but could be an error message!
                int versionIndex = line.indexOf('(');

                if (versionIndex != -1) {
                    String name = line.substring(0, versionIndex).trim();
                    int endIndex = line.indexOf(')');
                    String versions;

                    if (endIndex != -1) {
                        versions = line.substring(versionIndex + 1, endIndex);
                    } else {
                        versions = line.substring(versionIndex);
                    }

                    gem = new Gem(name, local ? versions : null, local ? null : versions);
                    gems.add(gem);
                } else {
                    gem = null;
                }
            }
        }
        LOGGER.finer("Parsed " + gems.size() + " gems");
        return gems;
    }
}
