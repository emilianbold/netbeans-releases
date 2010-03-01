/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.openide.util.Parameters;

/**
 * Represents a gem requirement in a Ruby/Rails application.
 * 
 * @author Erno Mononen
 */
public final class GemRequirement implements Comparable<GemRequirement>{

    /**
     * Pattern for parsing Bundler output
     */
    private static final Pattern BUNDLER = Pattern.compile("\\s+\\*\\s(\\S+)\\s\\((\\S+)\\)\\s*");
    /**
     * Patterns for parsing requirement info from 'rake gems' output.
     */
    private static final Pattern STATUS = Pattern.compile("\\s*-\\s*\\[(.*)\\].*");
    private static final Pattern NAME = Pattern.compile("\\s*-\\s*\\[.*\\]\\s(\\S*).*");
    private static final Pattern VERSION = Pattern.compile(".*\\s+(\\d+[\\.\\d]*)\\s*");
    private static final Pattern OPERATOR = Pattern.compile(".*\\s(\\S+)\\s\\d+.*");

    enum Status {
        INSTALLED("I"),
        NOT_INSTALLED(" "),
        FROZEN("F"),
        FRAMEWORK("R"),
        UNKNOWN("unknown");
        
        private final String code;

        private Status(String code) {
            this.code = code;
        }
        
        static Status statusFor(String status) {
            for (Status each : values()) {
                if (each.code.equals(status)) {
                    return each;
                }
            }
            return UNKNOWN;
        }
    }

    private final Status status;
    /**
     * The name of the required gem.
     */
    private final String name;
    /**
     * The version required.
     */
    private final String version;
    /**
     * The operator for the version requirement; one of the following:
     * <code>=, !=, >=, <=, >, <, ~></code>.
     */
    private final String operator;

    public static GemRequirement fromString(String gemRequirement) {
        String[] parts = gemRequirement.split(" ");
        if (parts.length == 1) {
            // contains just the name
            return new GemRequirement(gemRequirement, "", "", Status.UNKNOWN);
        }
        assert parts.length == 3 : "Invalid requirement " + gemRequirement;
        return new GemRequirement(parts[0], parts[2], parts[1], Status.UNKNOWN);
    }

    GemRequirement(String name, String version, String operator, Status status) {
        Parameters.notNull("name", name);
        Parameters.notNull("status", status);
        this.name = name;
        this.version = version;
        this.operator = operator;
        this.status = status;
    }

    public static GemRequirement forGem(Gem gem) {
        return new GemRequirement(gem.getName(), null, null, Status.INSTALLED);
    }

    public static String[] getOperators() {
        return new String[]{">=", ">", "=", "<=", "<", "!=", "~>"};
    }

    Status getStatus() {
        return status;
    }

    /**
     * @see #name
     */
    public String getName() {
        return name;
    }

    public String getNameWithVersion() {
        if (version == null || "".equals(version)) {
            return name;
        }
        return name + "-" + version;
    }

    /**
     * @see #operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @see #version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the version requirement, e.g. <code>">= 1.2.3"</code>.
     * 
     * @return the version requirement; returns an empty string if it isn't specified.
     */
    public String getVersionRequirement() {
        if (version == null || operator == null) {
            return "";
        }
        return operator + " " + version;
    }

    /**
     * Checks whether the given <code>gemVersion</code> satisfies the
     * version requirement of <codet>this</code>.
     * 
     * @param gemVersion
     * @return
     */
    public boolean satisfiedBy(String gemVersion) {
        if (this.version == null || "".equals(this.version)) {
            return true;
        }
        if ("=".equals(operator) && version.equals(gemVersion)) {
            return true;
        }
        if (">".equals(operator)) {
            return Util.compareVersions(gemVersion, version) > 0;
        }
        if (">=".equals(operator)) {
            return Util.compareVersions(gemVersion, version) >= 0;
        }
        if ("<".equals(operator)) {
            return Util.compareVersions(gemVersion, version) < 0;
        }
        if ("<=".equals(operator)) {
            return Util.compareVersions(gemVersion, version) < 1;
        }
        if ("!=".equals(operator)) {
            return Util.compareVersions(gemVersion, version) != 0;
        }
        if ("~>".equals(operator)) {
            return Util.compareVersions(gemVersion, version) >= 0
                    && Util.compareVersions(gemVersion, bumbVersion(version)) < 0;
        }
        return false;

    }

    /**
     * Increments the given version by one (minor or major if no minor version
     * is present).
     * @param version
     * @return
     */
    private static String bumbVersion(String version) {
        String[] ints = version.split("\\.");
        try {
            if (ints.length == 1) {
                int major = Integer.parseInt(ints[0]);
                return "" + major++;
            } else if (ints.length > 1) {
                int minor = Integer.parseInt(ints[1]);
                ints[1] = "" + (minor + 1);
                String res = "";
                for (int i = 0; i < ints.length; i++) {
                    if (i == 0) {
                        res += ints[i];
                    } else {
                        res += "." + ints[i];
                    }
                }
                return res;
            }
        } catch (NumberFormatException numberFormatException) {
            //XXX not sure what do here
            return version;
        }
        return version;
    }

    /**
     * Parser a <code>GemRequirement</code> from the given line. The expected
     * format of the line is the same as what either <code>bundle show</code> or 
     * <code>rake gems</code> outputs.
     *
     * @param line the line to parse.
     * @return the parsed requirement or <code>null</code>.
     */
    public static GemRequirement parse(String line) {
        Matcher bundlerMatcher = BUNDLER.matcher(line);
        if (bundlerMatcher.matches()) {
            return new GemRequirement(bundlerMatcher.group(1), bundlerMatcher.group(2), "=", Status.UNKNOWN);
        }
        Matcher statusMatcher = STATUS.matcher(line);
        Matcher nameMatcher = NAME.matcher(line);
        Matcher versionMatcher = VERSION.matcher(line);
        Matcher operatorMatcher = OPERATOR.matcher(line);
        String name, version, operator, status = null;
        if (nameMatcher.matches()) {
            name = nameMatcher.group(1);
            version = versionMatcher.matches() ? versionMatcher.group(1) : "";
            operator = operatorMatcher.matches() ? operatorMatcher.group(1) : "";
            status = statusMatcher.matches() ? statusMatcher.group(1) : "";
            return new GemRequirement(name, version, operator, Status.statusFor(status));
        }
        return null;
    }
    /**
     * @return this in a string format, e.g. <code>some-gem >= 1.2.3</code>.
     */
    String asString() {
        StringBuilder result = new StringBuilder(name);
        if (!isEmpty(operator) && !isEmpty(version)) {
            result.append(" " + operator);
            result.append(" " + version);
        }
        return result.toString();
    }

    private static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    @Override
    public String toString() {
        return GemRequirement.class.getName()
                + "[" + name + " " + operator + " " + version + " " + status + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GemRequirement other = (GemRequirement) obj;
        if (this.status != other.status && (this.status == null || !this.status.equals(other.status))) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        if ((this.operator == null) ? (other.operator != null) : !this.operator.equals(other.operator)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.status != null ? this.status.hashCode() : 0);
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 67 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        return hash;
    }

    public int compareTo(GemRequirement o) {
        int result = this.name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        if (this.version != null && o.version != null) {
            return this.version.compareTo(o.version);
        }
        if (this.version != null) {
            return 1;
        } else {
            return -1;
        }
    }

}


