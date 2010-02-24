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

package org.netbeans.modules.java.hints.jackpot.impl.pm;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;

/**
 *
 * @author lahvac
 */
public abstract class BulkSearch {

    private static final BulkSearch INSTANCE = new NFABasedBulkSearch();
//    private static final BulkSearch INSTANCE = new REBasedBulkSearch();

    public static BulkSearch getDefault() {
        return INSTANCE;
    }
    
    private final boolean requiresLightweightVerification;
    
    protected BulkSearch(boolean requiresLightweightVerification) {
        this.requiresLightweightVerification = requiresLightweightVerification;
    }
    
    public final Map<String, Collection<TreePath>> match(CompilationInfo info, TreePath toSearch, BulkPattern pattern) {
        return match(info, toSearch, pattern, null);
    }

    public final boolean requiresLightweightVerification() {
        return requiresLightweightVerification;
    }

    public abstract Map<String, Collection<TreePath>> match(CompilationInfo info, TreePath toSearch, BulkPattern pattern, Map<String, Long> timeLog);

    public abstract boolean matches(InputStream encoded, BulkPattern pattern);
    
    public abstract boolean matches(CompilationInfo info, TreePath toSearch, BulkPattern pattern);

    public abstract void encode(Tree tree, EncodingContext ctx);
    
    public final BulkPattern create(CompilationInfo info, String... code) {
        return create(info, Arrays.asList(code));
    }

    public final BulkPattern create(CompilationInfo info, Collection<? extends String> code) {
        List<Tree> patterns = new LinkedList<Tree>();

        for (String c : code) {
            patterns.add(Utilities.parseAndAttribute(info, c, null));
        }

        return create(code, patterns);
    }
    
    public abstract BulkPattern create(Collection<? extends String> code, Collection<? extends Tree> patterns);

    public static abstract class BulkPattern {

        private final List<? extends Set<? extends String>> identifiers;
        private final List<? extends Set<? extends String>> kinds;

        public BulkPattern(List<? extends Set<? extends String>> identifiers, List<? extends Set<? extends String>> kinds) {
            this.identifiers = identifiers;//TODO: immutable, maybe clone
            this.kinds = kinds;
        }

        public List<? extends Set<? extends String>> getIdentifiers() {
            return identifiers;
        }

        public List<? extends Set<? extends String>> getKinds() {
            return kinds;
        }

    }

    public static final class EncodingContext {

        private final OutputStream out;
        private Set<? extends String> identifiers;
        private Set<? extends String> kinds;

        public EncodingContext(OutputStream out) {
            this.out = out;
        }

        public Set<? extends String> getIdentifiers() {
            return identifiers;
        }

        public Set<? extends String> getKinds() {
            return kinds;
        }

        public OutputStream getOut() {
            return out;
        }

        public void setIdentifiers(Set<? extends String> identifiers) {
            this.identifiers = identifiers;
        }

        public void setKinds(Set<? extends String> kinds) {
            this.kinds = kinds;
        }

    }
}
