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
package org.netbeans.modules.ruby;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstraction which might represent none, one or more real Ruby Types.
 */
public final class RubyType {

    private static final Logger LOGGER = Logger.getLogger(RubyType.class.getName());

    /* Core Types */
    public static final RubyType ARRAY = new RubyType("Array");
    public static final RubyType BIGNUM = new RubyType("Bignum");
    public static final RubyType FALSE_CLASS = new RubyType("FalseClass");
    public static final RubyType FIXNUM = new RubyType("Fixnum");
    public static final RubyType FLOAT = new RubyType("Float");
    public static final RubyType HASH = new RubyType("Hash");
    public static final RubyType NIL_CLASS = new RubyType("NilClass");
    public static final RubyType OBJECT = new RubyType("Object");
    public static final RubyType RANGE = new RubyType("Range");
    public static final RubyType REGEXP = new RubyType("Regexp");
    public static final RubyType STRING = new RubyType("String");
    public static final RubyType SYMBOL = new RubyType("Symbol");
    public static final RubyType TRUE_CLASS = new RubyType("TrueClass");
    public static final RubyType INTEGER = new RubyType("Integer");

    /**
     * Union type for {{@link #TRUE_CLASS}, {@link #FALSE_CLASS}}. Value of this
     * type is typed to one of them.
     */
    public static final RubyType BOOLEAN = new RubyType(TRUE_CLASS, FALSE_CLASS);

    private static final Map<String, RubyType> CORE_TYPES = new HashMap<String, RubyType>(16);

    static {
        CORE_TYPES.put(ARRAY.first(), ARRAY);
        CORE_TYPES.put(BIGNUM.first(), BIGNUM);
        CORE_TYPES.put(FALSE_CLASS.first(), FALSE_CLASS);
        CORE_TYPES.put(FIXNUM.first(), FIXNUM);
        CORE_TYPES.put(FLOAT.first(), FLOAT);
        CORE_TYPES.put(HASH.first(), HASH);
        CORE_TYPES.put(NIL_CLASS.first(), NIL_CLASS);
        CORE_TYPES.put(OBJECT.first(), OBJECT);
        CORE_TYPES.put(RANGE.first(), RANGE);
        CORE_TYPES.put(REGEXP.first(), REGEXP);
        CORE_TYPES.put(STRING.first(), STRING);
        CORE_TYPES.put(SYMBOL.first(), SYMBOL);
        CORE_TYPES.put(TRUE_CLASS.first(), TRUE_CLASS);
    }

    public static RubyType create(final String realType) {
        checkType(realType);
        RubyType coreType = CORE_TYPES.get(realType);
        return coreType == null ? new RubyType(realType) : coreType;
    }

    public static RubyType createUnknown() {
        RubyType type = new RubyType();
        type.hasUnknownMember = true;
        return type;
    }
    
    private Set<String> realTypes;

    /** See {@link  #hasUnknownMember()}. */
    private boolean hasUnknownMember;

    public RubyType() {
        this.realTypes = new LinkedHashSet<String>();
    }

    public RubyType(RubyType... types) {
        this.realTypes = new LinkedHashSet<String>();
        for (RubyType rubyType : types) {
            append(rubyType);
        }
    }

    public RubyType(String... types) {
        this(Arrays.asList(types));
    }

    public RubyType(Collection<String> types) {
        assert !types.contains(null) : "cannot add arrays with null realType member";
        this.realTypes = new LinkedHashSet<String>(types);
    }

    private RubyType(final String realType) {
        assert realType != null : "cannot add null realType";
        checkType(realType);
        this.realTypes = new LinkedHashSet<String>(Collections.singleton(realType));
    }

    /**
     * Returns real Ruby types for this abstraction. Note that this set does not
     * include {@link #hasUnknownMember() unknown} member.
     */
    public Iterable<? extends String> getRealTypes() {
        return realTypes;
    }

    public String first() {
        return getRealTypes().iterator().next();
    }

    void append(RubyType type) {
        assert !type.realTypes.contains(null) : "cannot add arrays with null realType member";
        if (type.isKnown()) {
            this.realTypes.addAll(type.realTypes);
        }
        if (type.hasUnknownMember()) {
            this.hasUnknownMember = true;
        }
    }

    void add(String realType) {
        assert realType != null : "cannot add null realType";
        checkType(realType);
        realTypes.add(realType);
    }

    private static void checkType(String realType) {
        if (LOGGER.isLoggable(Level.FINE)) {
            if (realType.length() == 0 || Character.isLowerCase(realType.charAt(0))) {
                LOGGER.log(Level.FINE, "Likely not a valid type {0}", realType);
            }
        }
    }

    /**
     * Whether the backing set of real types has exactly one member. I.e.
     * <code>{String}</code>, <code>{Array}</code>, but not <code>{String,
     * Array}</code> or <code>{}</code>.
     */
    public boolean isSingleton() {
        return isKnown() && realTypes.size() == 1;
    }

    /**
     * The underlaying set of real Ruby types is empty.
     */
    public boolean isKnown() {
        return !this.realTypes.isEmpty();
    }

    /**
     * Means that <code>this</code> type has a member which could not be
     * inferred for some reason. E.g. in the following <code>if</code>
     * expression:
     *
     * <pre>
     * var = nil
     * if cond1
     *   var = not_able_to_infer_this
     * end
     * var.to_i
     * </pre>
     *
     * we infer union type {NilClass, unknown}. Note that this member is not
     * listed among the {@link #getRealTypes() real Ruby types}.
     */
    boolean hasUnknownMember() {
        return hasUnknownMember;
    }

    String asIndexedString() {
        return asString("|");
    }

    String asString(final String delimiter) {
        return RubyUtils.join(realTypes, delimiter);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RubyType other = (RubyType) obj;
        if (this.realTypes != other.realTypes && (this.realTypes == null || !this.realTypes.equals(other.realTypes))) {
            return false;
        }
        if (this.hasUnknownMember != other.hasUnknownMember) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.realTypes != null ? this.realTypes.hashCode() : 0);
        hash = 97 * hash + (this.hasUnknownMember ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "RubyType[realTypes:" + realTypes + ", hasUnknownMember: " + hasUnknownMember + ']'; // NOI18N
    }
}
