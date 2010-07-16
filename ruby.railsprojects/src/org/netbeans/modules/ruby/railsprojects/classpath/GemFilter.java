/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.ruby.railsprojects.classpath;

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.util.Exceptions;

/**
 * Helper class for filtering gems for inclusion/exclusion.
 *
 *
 * @author Erno Mononen
 */
final class GemFilter {

    private static final Logger LOGGER = Logger.getLogger(GemFilter.class.getName());
    private static final Pattern GEM_EXCLUDE_FILTER;
    private static final Pattern GEM_INCLUDE_FILTER;
    private final PropertyEvaluator evaluator;
    private final Pattern includeFilter;
    private final Pattern excludeFilter;

    static {
        String userExcludes = System.getProperty("rails.prj.excludegems");
        if (userExcludes == null || "none".equals(userExcludes)) {
            GEM_EXCLUDE_FILTER = null;
        } else {
            GEM_EXCLUDE_FILTER = getPattern(userExcludes, null);
        }
        String userIncludes = System.getProperty("rails.prj.includegems");
        if (userIncludes == null || "all".equals(userIncludes)) {
            GEM_INCLUDE_FILTER = null;
        } else {
            GEM_INCLUDE_FILTER = getPattern(userIncludes, null);
        }
    }

    GemFilter(PropertyEvaluator evaluator) {
        this.evaluator = evaluator;
        String include = evaluator.getProperty("ruby.includegems");
        String exclude = evaluator.getProperty("ruby.excludegems");
        this.includeFilter = getPattern(include, GEM_INCLUDE_FILTER);
        this.excludeFilter = getPattern(exclude, GEM_EXCLUDE_FILTER);

    }

    private static Pattern getPattern(String regex, Pattern defaultValue) {
        if (regex == null) {
            return defaultValue;
        }
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException pse) {
            Exceptions.printStackTrace(pse);
            return defaultValue;
        }
    }

    boolean include(String gem) {
        if (includeFilter == null) {
            return true;
        }
        return includeFilter.matcher(gem).find();
    }

    boolean exclude(String gem) {
        if (excludeFilter == null) {
            return true;
        }
        return excludeFilter.matcher(gem).find();
    }
}
