/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.api;

import java.util.Iterator;
import org.netbeans.modules.parsing.spi.Parser;


/**
 * ResultIterator allows to iterate tree of embedded blocks of sources, and 
 * request parse results on different levels. You can force parsing of all 
 * blocks of embedded {@link Source}s, or you can find your favourite one and
 * skip parsing of rest of them. In some situations you can even parse embedded 
 * language without parsing of surrounding block.
 * 
 * @author Jan Jancura
 */
public final class ResultIterator<T extends Parser.Result> {
    
    private ResultIterator () {}
    
    public Source getSource () {
        return null;
    }
    
    /**
     * Returns parse {@link Result} for current source.
     * 
     * @return              parse {@link Result} for current source.
     */
    public T getParserResult () {
        return null;
    }
    
    /**
     * Allows iterate all embedded sources.
     * 
     * @return              {@link Iterator} of all embeddings.
     */
    public Iterator<Embedding> getEmbeddedSources () {
        return null;
    }
    
    /**
     * Returns {@link ResultIterator} for one {@link Embedding}.
     * 
     * @param embedding     A embedding.
     * @return              {@link ResultIterator} for one {@link Embedding}.
     */
    public ResultIterator getResultIterator (Embedding embedding) {
        return null;
    }
}
