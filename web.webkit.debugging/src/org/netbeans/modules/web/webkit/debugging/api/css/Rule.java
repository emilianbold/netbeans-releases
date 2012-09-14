/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * CSS Rule.
 *
 * @author Jan Stola
 */
public class Rule {
    /** Identifier of the rule (absent for user agent stylesheet and user-specified stylesheet rules). */
    private RuleId id;
    /*** Selector of the rule. */
    private String selector;
    /** Parent stylesheet resource URL. */
    private String sourceURL;
    /** Line number of the first character of the selector. */
    private int sourceLine;
    /** Origin of the parent stylesheet. */
    private StyleSheetOrigin origin;
    /** Associated style declaration. */
    private Style style;
    /** Rule selector range in the underlying resource (if available). */
    private SourceRange selectorRange;
    /**
     * Media list (for rules involving media queries). It enumerates media
     * queries starting with the innermost one, going outwards.
     */
    private List<Media> media;
    /** Parent stylesheet of the rule. */
    private StyleSheetBody parentStyleSheet;

    /**
     * Creates a new {@code Rule} that corresponds to the given JSONObject.
     *
     * @param rule JSONObject describing the rule.
     */
    Rule(JSONObject rule) {
        if (rule.containsKey("ruleId")) { // NOI18N
            id = new RuleId((JSONObject)rule.get("ruleId")); // NOI18N
        }
        selector = (String)rule.get("selectorText"); // NOI18N
        sourceURL = (String)rule.get("sourceURL"); // NOI18N
        if (rule.containsKey("sourceLine")) { // NOI18N
            sourceLine = ((Number)rule.get("sourceLine")).intValue(); // NOI18N
        } else {
            sourceLine = -1;
        }
        String originCode = (String)rule.get("origin"); // NOI18N
        origin = StyleSheetOrigin.forCode(originCode);
        style = new Style((JSONObject)rule.get("style")); // NOI18N
        if (rule.containsKey("selectorRange")) { // NOI18N
            selectorRange = new SourceRange((JSONObject)rule.get("selectorRange")); // NOI18N
        }
        if (rule.containsKey("media")) { // NOI18N
            JSONArray array = (JSONArray)rule.get("media"); // NOI18N
            media = new ArrayList<Media>(array.size());
            for (Object o : array) {
                media.add(new Media((JSONObject)o));
            }
        } else {
            media = Collections.EMPTY_LIST;
        }
    }

    /**
     * Returns the identifier of the rule.
     *
     * @return identifier of the rule or {@code null} for user agent stylesheet
     * and user-specified stylesheet rules.
     */
    public RuleId getId() {
        return id;
    }

    /**
     * Returns the selector of the rule.
     *
     * @return selector of the rule.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Returns URL of the parent stylesheet.
     *
     * @return URL of the parent stylesheet.
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * Returns the line number of the first character of the selector.
     *
     * @return line number of the first character of the selector.
     */
    public int getSourceLine() {
        return sourceLine;
    }

    /**
     * Returns the origin of the parent stylesheet.
     *
     * @return origin of the parent stylesheet.
     */
    public StyleSheetOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns the associated style declaration.
     *
     * @return associated style declaration.
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Return the selector range in the underlying resource.
     *
     * @return selector range in the underlying resource or {@code null}
     * if this information is not available.
     */
    public SourceRange getSelectorRange() {
        return selectorRange;
    }

    /**
     * Returns the media list (for rules involving media queries).
     *
     * @return media list that enumerates media queries starting with
     * the innermost one, going outwards.
     */
    public List<Media> getMedia() {
        return Collections.unmodifiableList(media);
    }

    /**
     * Returns the parent style sheet of the rule. Note that this method
     * is supported on instances obtained from {@code StyleSheetBody} only.
     * It returns {@code null} if the {@code Rule} object is obtain by any
     * other mean.
     *
     * @return parent style sheet of the rule or {@code null}.
     */
    public StyleSheetBody getParentStyleSheet() {
        return parentStyleSheet;
    }

    /**
     * Sets the parent style sheet of this rule.
     *
     * @param parentStyleSheet parent style sheet of this rule.
     */
    void setParentStyleSheet(StyleSheetBody parentStyleSheet) {
        this.parentStyleSheet = parentStyleSheet;
    }

}
