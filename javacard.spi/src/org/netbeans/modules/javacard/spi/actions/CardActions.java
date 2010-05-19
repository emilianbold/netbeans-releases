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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.spi.actions;

import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.spi.actions.ContextAction;
import org.netbeans.spi.actions.LookupProviderAction;
import org.openide.nodes.Node;

/**
 * Utility class for creating instances of various common card action classes.
 * The actions returned by the factory methods here are suitable for using
 * on the popup menu of a node or on a main menu or toolbar.  They will
 * automatically attach to the selection context in question and look up
 * the ICardCapability that controls them through the lookup of the Card
 * instance in the selected node (if any).
 *
 * @author Tim Boudreau
 */
public final class CardActions {
    private CardActions(){}

    public static ContextAction<?> createDeleteAction() {
        return LookupProviderAction.createIndirectAction(Node.class,
                LookupProviderAction.createIndirectAction(Card.class, new DeleteCardAction()));
    }

    public static ContextAction<?> createStartAction() {
        //sensitive to StartCapability in lookup of Card in lookup of Node in Global Selection Lookup
        ContextAction<?> a = LookupProviderAction.createIndirectAction(Node.class,
                LookupProviderAction.createIndirectAction(Card.class, new StartCardAction()));
        return a;
    }

    public static ContextAction<?> createStopAction() {
        ContextAction<?> a = LookupProviderAction.createIndirectAction(Node.class,
                LookupProviderAction.createIndirectAction(Card.class, new StopCardAction()));
        return a;
    }

    public static ContextAction<?> createResumeAction() {
        ContextAction<?> a = LookupProviderAction.createIndirectAction(Node.class,
                LookupProviderAction.createIndirectAction(Card.class, new ResumeCardAction(RunMode.RUN)));
        return a;
    }

    public static ContextAction<?> createResumeIntoDebugModeAction() {
        ContextAction<?> a = LookupProviderAction.createIndirectAction(Node.class,
                LookupProviderAction.createIndirectAction(Card.class, new ResumeCardDebugAction(RunMode.DEBUG)));
        return a;
    }

    public static ContextAction<?> createClearEpromAction() {
        ContextAction<?> a = LookupProviderAction.createIndirectAction(Node.class,
                LookupProviderAction.createIndirectAction(Card.class, new ClearEpromAction()));
        return a;
    }

    public static ContextAction<?> createCustomizeAction() {
        ContextAction<?> a = LookupProviderAction.createIndirectAction(Node.class,
                new CustomizeCardAction());
        return a;
    }
}
