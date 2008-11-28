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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;

import org.netbeans.modules.cnd.modelutil.CsmImageLoader;

/**
 * An ElementDescriptor for macros (CsmMacro)
 * @author Vladimir Kvashin
 */

/* package */
class MacroElementDescriptor extends BaseElementDescriptor implements ElementDescriptor {

    private final CsmMacro macro;
    private String displayName = null;
    private String contextName = null;
    private static Icon icon;

    MacroElementDescriptor(CsmMacro macro) {
        this.macro = macro;
        List<? extends CharSequence> params = macro.getParameters();
        if (params == null || params.size() == 0) {
            displayName = macro.getName().toString();
        } else {
            StringBuilder sb = new StringBuilder(macro.getName());
            sb.append('(');
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(params.get(i));
            }
            sb.append(')');
            displayName = sb.toString();
        }
        contextName = macro.getContainingFile().getName().toString();
        if (icon == null) {
            icon = new ImageIcon(CsmImageLoader.getImage(macro));
        }
    }

    protected CsmOffsetable getElement() {
        return macro;
    }

    protected String getContextNameImpl() {
        return contextName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }
}
