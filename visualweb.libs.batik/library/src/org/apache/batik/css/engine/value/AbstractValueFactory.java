/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.engine.value;

import java.net.URL;

import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMException;
// <rave>
import org.apache.batik.css.engine.CSSEngine;
// </rave>

/**
 * This class provides a base implementation for the value factories.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public abstract class AbstractValueFactory {
    
    /**
     * Returns the name of the property handled.
     */
    public abstract String getPropertyName();
    
    /**
     * Resolves an URI.
     */
    protected static String resolveURI(URL base, String value) {
        return new ParsedURL(base, value).toString();
    }

// <rave>
// BEGIN RAVE MODIFICATIONS
    private String getPropertyName(CSSEngine engine) {
        String name = null;
        if (engine != null) {
            name = engine.getExpandingShorthandProperty();
        }
        if (name == null) {
            name = getPropertyName();
        }
        return name;
    }

    // In the following methods, I have added an engine parameter,
    // since Batik didn't include one. I have also updated all references
    // to pass in the "engine" parameter which was always available at
    // the point of using these error message methods.
    // I also modified all references to getPropertyName() in the below
    // to the local private method getPropertyName(engine) which does the
    // same but checks for a global shorthand property being processed and
    // uses that name instead if set.
// END RAVE MODIFICATIONS
// </rave>    
    /**
     * Creates a DOM exception, given an invalid identifier.
     */
// <rave>
//    protected DOMException createInvalidIdentifierDOMException(String ident) {
//        Object[] p = new Object[] { getPropertyName(), ident };
    protected DOMException createInvalidIdentifierDOMException(String ident, CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine), ident };
// </rave>        
        String s = Messages.formatMessage("invalid.identifier", p);
// <rave>
// BEGIN RAVE MODIFICATIONS
        if (this instanceof IdentifierProvider) {
            StringMap map = ((IdentifierProvider)this).getIdentifierMap();
            java.util.Iterator keys = map.keys();
            java.util.ArrayList list = new java.util.ArrayList();
            while (keys.hasNext()) {
                String identifier = (String)keys.next();
                // Only add non-vendor-specific identifiers
                if (!identifier.startsWith("-")) {
                    list.add(identifier);
                }
            }
            if (list.size() > 0) {
                String identifiers = list.toString();
                if (identifiers.length() > 80) {
                    identifiers = identifiers.substring(0,80) + "...]";
                }
                s = s + " " + identifiers;
            }
        }
// END RAVE MODIFICATIONS
// </rave>
        return new DOMException(DOMException.SYNTAX_ERR, s);
    }

    /**
     * Creates a DOM exception, given an invalid lexical unit type.
     */
// <rave>
//    protected DOMException createInvalidLexicalUnitDOMException(short type) {
//        Object[] p = new Object[] { getPropertyName(),
    protected DOMException createInvalidLexicalUnitDOMException(short type, CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine),
// </rave>
                                    new Integer(type) };
        String s = Messages.formatMessage("invalid.lexical.unit", p);
        return new DOMException(DOMException.NOT_SUPPORTED_ERR, s);
    }

    /**
     * Creates a DOM exception, given an invalid float type.
     */
// <rave>
//    protected DOMException createInvalidFloatTypeDOMException(short t) {
//        Object[] p = new Object[] { getPropertyName(), new Integer(t) };
    protected DOMException createInvalidFloatTypeDOMException(short t, CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine), new Integer(t) };
// </rave>
        String s = Messages.formatMessage("invalid.float.type", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }

    /**
     * Creates a DOM exception, given an invalid float value.
     */
// <rave>
//    protected DOMException createInvalidFloatValueDOMException(float f) {
//        Object[] p = new Object[] { getPropertyName(), new Float(f) };
    protected DOMException createInvalidFloatValueDOMException(float f, CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine), new Float(f) };
// </rave>
        String s = Messages.formatMessage("invalid.float.value", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }

    /**
     * Creates a DOM exception, given an invalid string type.
     */
// <rave>
//    protected DOMException createInvalidStringTypeDOMException(short t) {
//        Object[] p = new Object[] { getPropertyName(), new Integer(t) };
    protected DOMException createInvalidStringTypeDOMException(short t, CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine), new Integer(t) };
// </rave>
        String s = Messages.formatMessage("invalid.string.type", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }

// <rave>
//    protected DOMException createMalformedLexicalUnitDOMException() {
//        Object[] p = new Object[] { getPropertyName() };
    protected DOMException createMalformedLexicalUnitDOMException(CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine) };
// </rave>
        String s = Messages.formatMessage("malformed.lexical.unit", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }

// <rave>
//    protected DOMException createDOMException(CSSEngine engine) {
//        Object[] p = new Object[] { getPropertyName() };
    protected DOMException createDOMException(CSSEngine engine) {
        Object[] p = new Object[] { getPropertyName(engine) };
// </rave>
        String s = Messages.formatMessage("invalid.access", p);
        return new DOMException(DOMException.NOT_SUPPORTED_ERR, s);
    }
}
