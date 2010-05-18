/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.designer.jsf.palette;

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.util.Lookup;

public class MergedPaletteActions extends PaletteActions {

        private PaletteActions palActions1;

        private PaletteActions palActions2;

        public MergedPaletteActions(PaletteActions palActions1,
                PaletteActions palActions2) {
            this.palActions1 = palActions1;
            this.palActions2 = palActions2;
        }

        @Override
        public Action[] getCustomCategoryActions(Lookup category) {
            Action[] actions1 = palActions1.getCustomCategoryActions(category);
            Action[] actions2 = palActions2.getCustomCategoryActions(category);
            return mergeActions(actions1, actions2);
        }

        @Override
        public Action[] getCustomItemActions(Lookup item) {
            Action[] actions1 = palActions1.getCustomItemActions(item);
            Action[] actions2 = palActions2.getCustomItemActions(item);
            return mergeActions(actions1, actions2);
        }

        @Override
        public Action[] getCustomPaletteActions() {
            Action[] actions1 = palActions1.getCustomPaletteActions();
            Action[] actions2 = palActions2.getCustomPaletteActions();
            return mergeActions(actions1, actions2);
        }

        @Override
        public Action[] getImportActions() {
            Action[] actions1 = palActions1.getImportActions();
            Action[] actions2 = palActions2.getImportActions();
            return mergeActions(actions1, actions2);
        }

        @Override
        public Action getPreferredAction(Lookup item) {
            // Choose the preferred action from the first PaletteActions
            return palActions1.getPreferredAction(item);
        }

        private static Action[] mergeActions(Action[] actions1,
            Action[] actions2) {
            if (actions2 == null) {
                return actions1;
            }
            if (actions1 == null) {
                return actions2;
            }

            Action[] allActions = new Action[actions1.length + actions2.length];
            System.arraycopy(actions1, 0, allActions, 0, actions1.length);
            System.arraycopy(actions2, 0, allActions, actions1.length,
                    actions2.length);
            return allActions;
        }
    }
