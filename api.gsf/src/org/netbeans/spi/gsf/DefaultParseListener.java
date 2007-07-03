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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.gsf;

import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserResult;
import org.openide.util.Exceptions;

/**
 * A default parse listener which keeps track of the most recently 
 * returned completed ParserResult
 * 
 * @author Tor Norbye
 */
public class DefaultParseListener implements ParseListener {
    private ParserResult result;
    
    public DefaultParseListener() {
    }
    
    public void started(ParseEvent e) {
    }

    public void finished(ParseEvent e) {
        if (e.getKind() == ParseEvent.Kind.PARSE) {
            result = e.getResult();
        }
    }

    public void error(Error e) {
    }

    public void exception(Exception e) {
        Exceptions.printStackTrace(e);
    }

    public ParserResult getParserResult() {
        return result;
    }
}
