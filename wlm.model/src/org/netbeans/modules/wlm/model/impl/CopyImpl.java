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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TFrom;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nikita Krjukov
 */
public class CopyImpl extends WLMComponentBase implements TCopy {

    public CopyImpl(WLMModel model, Element e) {
        super(model, e);
    }

    public CopyImpl(WLMModel model) {
        this(model, createNewElement(WLMQNames.COPY.getQName(), model));
    }

    public void accept(WLMVisitor visitor) {
        visitor.visitCopy(this);
    }

    public WLMComponent createChild(Element childEl) {
        WLMComponent child = null;
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                localName = childEl.getTagName();
            }
            if (FROM_ELEMENT_NAME.equals(localName)) {
                child = new FromImpl(getModel(), childEl);
            } else if (TO_ELEMENT_NAME.equals(localName)) {
                child = new ToImpl(getModel(), childEl);
            }
        }
        return child;
    }

    public TTo getTo() {
        return getChild(TTo.class);
    }

    public void setTo(TTo to) {
        setChild(TTo.class, TO_ELEMENT_NAME, to, TO_POSITION);
    }

    public void removeTo(TTo to) {
        removeChild(TO_ELEMENT_NAME, to);
    }

    public TFrom getFrom() {
        return getChild(TFrom.class);
    }

    public void setFrom(TFrom from) {
        setChild(TFrom.class, FROM_ELEMENT_NAME, from, FROM_POSITON);
    }

    public void removeFrom(TFrom from) {
        removeChild(FROM_ELEMENT_NAME, from);
    }
    
    private static final ElementPosition FROM_POSITON 
            = new ElementPosition(TFrom.class);
    private static final ElementPosition TO_POSITION 
            = new ElementPosition(FROM_POSITON, TTo.class);
}
