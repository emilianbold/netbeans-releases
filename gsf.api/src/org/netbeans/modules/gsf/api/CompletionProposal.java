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
package org.netbeans.modules.gsf.api;

import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;


/**
 * An item to be added to the code completion dialog
 *
 * @author Tor Norbye
 */
public interface CompletionProposal {
    /** The offset at which the completion item substitution should begin */
    int getAnchorOffset();

    ElementHandle getElement();

    String getName();

    String getInsertPrefix();

    String getSortText();

    String getLhsHtml();

    String getRhsHtml();

    ElementKind getKind();

    ImageIcon getIcon();

    Set<Modifier> getModifiers();
    
    /**
     * Return true iff this is a "smart" completion item - one that should be emphasized
     * (currently the IDE flushes these to the top and separates them with a line)
     */
    boolean isSmart();

    /**
     * Provide a custom live code template that will be inserted when
     * this item is chosen for insertion. 
     *
     * @return A live code template to be inserted into the document
     *   at the anchor offset. Return null to get the default behavior
     *   where the insert prefix, the insert params and param list delimiters
     *   are used instead.
     */
    String getCustomInsertTemplate();

    /**
     * Parameters to be inserted for this item, if any. Has no effect
     * if getCustomInsertTemplate() returns non null.
     * @return
     */
    List<String> getInsertParams();

    /** The strings to be inserted to start and end a parameter list. Should be a String of length 2.
     * In Java we would expect {(,)}, and in Ruby it's either {(,)} or { ,}.
     */
    String[] getParamListDelimiters();
}
