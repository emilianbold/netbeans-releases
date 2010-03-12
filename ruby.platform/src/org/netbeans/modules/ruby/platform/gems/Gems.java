/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.platform.gems;

import java.net.URL;

/**
 * Gem-related utility methods.
 *
 * @author Erno Mononen
 */
public final class Gems {

    /**
     * The Rails gems, i.e. rails and its dependencies.
     */
    private static final String[] RAILS_GEMS = new String[]{"actionmailer", "actionpack", "activerecord", // NOI18N
        "activeresource", "activesupport", "rails", // NOI18N
        "activemodel", // NOI18N    activemodel is Rails 3.x only
        "actionwebservice"}; // NOI18N    actionwebservice is Rails 1.x only

    /**
     * Known test framework gems.
     */
    private static final String[] TESTING_GEMS = new String[]{"rspec", "rspec-rails", "test-unit",
        "thoughtbot-shoulda", "ZenTest", "cucumber", "mocha"}; // NOI18N

    private static final String RAKE_GEM = "rake"; //NOI18N

    private Gems() {
    }
    /**
     * Returns the gem name from the given <code>gemUrl</code>.
     *
     * @param gemUrl an URL of a gem.
     * @return
     */
    public static String getGemName(URL gemUrl) {
        String urlString = gemUrl.getFile();
        if (urlString.endsWith("/lib/")) {
            urlString = urlString.substring(urlString.lastIndexOf('/', urlString.length()-6)+1,
                    urlString.length()-5);
        }
        return urlString;
    }

    public static boolean isRailsGem(String name) {
        for (String each : RAILS_GEMS) {
            if (each.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRakeGem(String name) {
        return RAKE_GEM.equals(name);
    }

    /**
     * @return the names of the Rails framework gems.
     */
    public static String[] getRailsGems() {
        return RAILS_GEMS;
    }

    /**
     * @param name
     * @return true if the given <code>name</code> represents a gem that 
     * is likely needed only in test files.
     */
    public static boolean isTestingGem(String name) {
        for (String each : TESTING_GEMS) {
            if (each.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
