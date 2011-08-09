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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.profiler.selector.api.nodes;

import java.lang.reflect.Modifier;
import javax.swing.Icon;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.lib.profiler.utils.formatting.DefaultMethodNameFormatter;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ConstructorNode extends SelectorNode {
    private SourceMethodInfo method;
    private SourceCodeSelection signature;
    
    private static MethodNameFormatterFactory formatterFactory = MethodNameFormatterFactory.getDefault(new DefaultMethodNameFormatter(DefaultMethodNameFormatter.VERBOSITY_METHOD));
    
    /** Creates a new instance of ConstructorNode */
    public ConstructorNode(SourceMethodInfo mi, ConstructorsNode parent) {
        super(mi.getName(), mi.getName(), null, SelectorChildren.LEAF, parent);
        this.method = mi;

        signature = new SourceCodeSelection(method.getClassName(),
                    method.getName(), method.getSignature());
        updateDisplayName(formatterFactory.getFormatter().formatMethodName(signature).toFormatted());
    }

    @Override
    final public boolean getAllowsChildren() {
        return false;
    }

    @Override
    final public int getChildCount() {
        return 0;
    }

    @Override
    final public boolean isLeaf() {
        return true;
    }

    @Override
    final public Icon getIcon() {
        Icon icon;

        if (Modifier.isPublic(method.getModifiers())) {
            icon = Icons.getIcon(LanguageIcons.CONSTRUCTOR_PUBLIC);
        } else if (Modifier.isProtected(method.getModifiers())) {
            icon = Icons.getIcon(LanguageIcons.CONSTRUCTOR_PROTECTED);
        } else if (Modifier.isPrivate(method.getModifiers())) {
            icon = Icons.getIcon(LanguageIcons.CONSTRUCTOR_PRIVATE);
        } else {
            icon = Icons.getIcon(LanguageIcons.CONSTRUCTOR_PACKAGE);
        }

        return icon;
    }

    final public ClassNode getParentClass() {
        return ((ConstructorsNode)getParent()).getParentClass();
    }

    @Override
    final public SourceCodeSelection getSignature() {
        return signature;
    }
}
