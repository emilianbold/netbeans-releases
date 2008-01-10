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

package org.netbeans.modules.spring.beans.hyperlink;

import javax.swing.text.Document;
import org.netbeans.modules.spring.beans.hyperlink.BeansContextHyperlinkProvider.Type;
import org.netbeans.modules.xml.text.syntax.dom.Tag;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class HyperlinkEnv {
    
    private Document document;
    private Tag currentTag;
    private String tagName;
    private String attribName;
    private String valueString;
    private Type type;

    /**
     * Holds environment information for a hyperlink situation
     * 
     * @param document document on which hyperlink invoked
     * @param currentTag tag where hyperlink was invoked
     * @param tagName name of the tag
     * @param attribName attribute name
     * @param valueString value of attribute
     * @param type type of hyperlink
     */
    public HyperlinkEnv(Document document, Tag currentTag, String tagName, 
            String attribName, String valueString, Type type) {
        this.document = document;
        this.currentTag = currentTag;
        this.tagName = tagName;
        this.attribName = attribName;
        this.valueString = valueString;
        this.type = type;
    }

    public String getAttribName() {
        return attribName;
    }

    public Tag getCurrentTag() {
        return currentTag;
    }

    public Document getDocument() {
        return document;
    }

    public String getTagName() {
        return tagName;
    }

    public String getValueString() {
        return valueString;
    }

    public Type getType() {
        return type;
    }
}
