/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.model.impl;

import java.util.List;

import org.netbeans.modules.xslt.model.CharacterMap;
import org.netbeans.modules.xslt.model.Output;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.Standalone;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class OutputImpl extends QNameableImpl implements Output {

    OutputImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }

    OutputImpl( XslModelImpl model ) {
        super( model , XslElements.OUTPUT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#accept(org.netbeans.modules.xslt.model.XslVisitor)
     */
    @Override
    public void accept( XslVisitor visitor )
    {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#getComponentType()
     */
    @Override
    public Class<? extends XslComponent> getComponentType()
    {
        return Output.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.EncodingSpec#getEncoding()
     */
    public String getEncoding() {
        return getAttribute( XslAttributes.ENCODING );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.EncodingSpec#setEncoding(java.lang.String)
     */
    public void setEncoding( String encoding ) {
        setAttribute( XslAttributes.ENCODING, encoding );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.IndentSpec#getIndent()
     */
    public TBoolean getIndent() {
        return TBoolean.forString( getAttribute( XslAttributes.INDENT ) );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.IndentSpec#setIndent(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setIndent( TBoolean value ) {
        setAttribute( XslAttributes.INDENT, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Output#getStandalone()
     */
    public Standalone getStandalone() {
        return Standalone.forString( getAttribute( XslAttributes.STANDALONE ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Output#setStandalone(org.netbeans.modules.xslt.model.enums.Standalone)
     */
    public void setStandalone( Standalone value ) {
        setAttribute( XslAttributes.STANDALONE, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Output#getUndeclarePrefixes()
     */
    public TBoolean getUndeclarePrefixes() {
        return TBoolean.forString( getAttribute( XslAttributes.UNDECLARE_PREFIXES));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Output#setUndeclarePrefixes(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setUndeclarePrefixes( TBoolean value ) {
        setAttribute( XslAttributes.UNDECLARE_PREFIXES, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.UseCharacterMapsSpec#getUseCharacterMaps()
     */
    public List<XslReference<CharacterMap>> getUseCharacterMaps() {
        return resolveGlobalReferenceList( CharacterMap.class, 
                XslAttributes.USE_CHARACTER_MAPS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.UseCharacterMapsSpec#setUseCharacterMaps(java.util.List)
     */
    public void setUseCharacterMaps( List<XslReference<CharacterMap>> list ) {
        setAttribute( XslAttributes.USE_CHARACTER_MAPS, list );
    }

}
