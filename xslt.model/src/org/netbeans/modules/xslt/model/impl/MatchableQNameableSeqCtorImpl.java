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

import org.netbeans.modules.xslt.model.MatchSpec;
import org.w3c.dom.Element;

/**
 * @author ads
 *
 */
abstract class MatchableQNameableSeqCtorImpl extends
        QNameableSequenceConstructor implements MatchSpec
{

    MatchableQNameableSeqCtorImpl( XslModelImpl model, Element e ) {
        super(model, e);
    }

    MatchableQNameableSeqCtorImpl( XslModelImpl model, XslElements type )
    {
        super(model, type);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.MatchSpec#getMatch()
     */
    public String getMatch() {
        return getAttribute( XslAttributes.MATCH );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.MatchSpec#setMatch(java.lang.String)
     */
    public void setMatch( String match ) {
        setAttribute( XslAttributes.MATCH, match ); 
    }

}
