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

/**
 *
 */
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class DocumentationImpl extends GenericExtensibilityElement implements Documentation {

    public DocumentationImpl( WSDLModel model, Element e ) {
        super(model, e);
    }

/*    public DocumentationImpl( WSDLModel model, QName qname ) {
        this(model, createPrefixedElement(BPELQName.DOCUMENTATION.getQName(),
                model));
    }*/

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#getContent()
     */
    public String getContent() {
        return getText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#setContent(java.lang.String)
     */
    public void setContent( String value ) {
        setText( CONTENT_PROPERTY, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent#accept(org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent.Visitor)
     */
    public void accept( Visitor v ) {
        v.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#getSource()
     */
    public String getSource() {
        return getAttribute(BPELAttribute.SOURCE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#setSource()
     */
    public void setSource( String value ) {
        setAttribute( SOURCE , BPELAttribute.SOURCE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#removeSource()
     */
    public void removeSource() {
        setAttribute( SOURCE , BPELAttribute.SOURCE , null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#getLang()
     */
    public String getLang() {
        return getAttribute( BPELAttribute.LANG );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#removeLang()
     */
    public void removeLang() {
        setAttribute( LANG , BPELAttribute.LANG, null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation#setLang(java.lang.String)
     */
    public void setLang( String value ) {
        setAttribute( LANG , BPELAttribute.LANG, value );
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof PartnerLinkType || target instanceof Role) {
            return true;
        }
        return false;
    }
}
