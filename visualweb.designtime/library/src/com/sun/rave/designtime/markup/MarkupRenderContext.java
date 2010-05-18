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

package com.sun.rave.designtime.markup;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * <P>A MarkupRenderContext represents the context in which a markup bean will be rendering its
 * markup output.  This is a call-back interface used in the MarkupDesignInfo.customizeRender(...)
 * method.  A component (bean) author can use the methods in this interface to alter the markup
 * stream that will be displayed on the design surface.  They can also associate custom behavior
 * with portions of markup using the associateMouseRegion(...) method.</P>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see MarkupDesignInfo#customizeRender(MarkupDesignBean, MarkupRenderContext)
 */
public interface MarkupRenderContext {

    /**
     * The 'rendered' DOM document fragment from the JSF component.
     */
    public DocumentFragment getDocumentFragment();

    /**
     * The starting position within the renderFragment from where nodes were rendered for this
     * component.
     */
    public MarkupPosition getBeginPosition();

    /**
     * The ending position within the renderFragment to which nodes were rendered for this
     * component. If this position is the same as begin, no nodes were rendered for the component.
     * Note that the end position is NOT inclusive, it points to the NEXT node in the node list.
     * Note also that the parent node for begin and end will always be the same.
     */
    public MarkupPosition getEndPosition();

    /**
     * Associates a mouse region with a particular element of rendered markup.
     *
     * @param element Element
     * @param region MarkupMouseRegion
     */
    public void associateMouseRegion(Element element, MarkupMouseRegion region);
}
