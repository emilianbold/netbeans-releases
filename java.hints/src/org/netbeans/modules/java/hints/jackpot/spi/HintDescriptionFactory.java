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

package org.netbeans.modules.java.hints.jackpot.spi;

import com.sun.source.tree.Tree.Kind;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.Worker;

/**
 *
 * @author lahvac
 */
public class HintDescriptionFactory {

    private       String displayName;
    private       String category;
    private       Kind triggerKind;
    private       PatternDescription triggerPattern;
    private       Worker worker;
    private final List<String> suppressWarningsKeys = new LinkedList<String>();
    private       boolean finished;

    private HintDescriptionFactory() {
    }

    public static HintDescriptionFactory create() {
        return new HintDescriptionFactory();
    }

    public HintDescriptionFactory setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public HintDescriptionFactory setCategory(String category) {
        this.category = category;
        return this;
    }

    public HintDescriptionFactory setTriggerKind(Kind triggerKind) {
        if (this.triggerPattern != null) {
            throw new IllegalStateException(this.triggerPattern.getPattern());
        }

        this.triggerKind = triggerKind;
        return this;
    }

    public HintDescriptionFactory setTriggerPattern(PatternDescription triggerPattern) {
        if (this.triggerKind != null) {
            throw new IllegalStateException(this.triggerKind.name());
        }
        
        this.triggerPattern = triggerPattern;
        return this;
    }

    public HintDescriptionFactory setWorker(Worker worker) {
        this.worker = worker;
        return this;
    }

    public HintDescriptionFactory addSuppressWarningsKeys(String... keys) {
        this.suppressWarningsKeys.addAll(Arrays.asList(keys));
        return this;
    }

    public HintDescription produce() {
        if (this.triggerKind == null) {
            return HintDescription.create(displayName, category, triggerPattern, worker, Collections.unmodifiableList(suppressWarningsKeys));
        } else {
            return HintDescription.create(displayName, category, triggerKind, worker, Collections.unmodifiableList(suppressWarningsKeys));
        }
    }
    
}
