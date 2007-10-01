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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.lexer;

/**
 * Token hierarchy event type determines the reason
 * why token hierarchy modification described by {@link TokenHierarchyEvent}
 * happened.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public enum TokenHierarchyEventType {

    /**
     * The token change was caused by modification (insert/remove) of the characters
     * in the underlying character sequence.
     */
    MODIFICATION,

    /**
     * The token change was caused by relexing of a part of the token hierarchy
     * without any text modification.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    RELEX,

    /**
     * The token change was caused by a complete rebuild
     * of the token hierarchy.
     * <br/>
     * That may be necessary because of changes
     * in input attributes that influence the lexing.
     * <br/>
     * When the whole hierarchy is rebuilt only the removed tokens
     * will be notified. There will be no added tokens
     * because they will be created lazily when asked.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    REBUILD,

    /**
     * The token change was caused by change in activity
     * of the token hierarchy.
     * <br/>
     * The current activity state can be determined by {@link TokenHierarchy#isActive()}.
     * <br/>
     * Firing an event with this type may happen because the input source
     * (for which the token hierarchy was created) has not been used for a long time
     * and its token hierarchy is being deactivated. Or the token hierarchy is just going
     * to be activated again.
     * <br/>
     * The hierarchy will only notify the tokens being removed (for the case when
     * the hierarchy is going to be deactivated). There will be no added tokens
     * because they will be created lazily when asked.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    ACTIVITY,
        
    /**
     * Custom language embedding was created by
     * {@link TokenSequence#createEmbedding(Language,int,int)}.
     * <br/>
     * The {@link TokenHierarchyEvent#tokenChange()} contains the token
     * where the embedding was created and the embedded change
     * {@link TokenChange#embeddedChange(int)} that describes the added
     * embedded language.
     */
    EMBEDDING,
    
    /**
     * Notification that result of
     * {@link TokenHierarchy#languagePaths()} has changed.
     */
    LANGUAGE_PATHS;

}