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

package org.netbeans.modules.css.lexer;

import java.io.IOException;
import org.netbeans.modules.css.parser.CSSParserConstants;
import org.netbeans.modules.css.parser.CharStream;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author marek
 */
public class LexerCharStream implements CharStream {

    private LexerInput li;
    
    public LexerCharStream(LexerRestartInfo lri) {
        this.li = lri.input();
        
    }

    public char readChar() throws IOException {
        int c = li.read();
        if(c == LexerInput.EOF) {
            throw new IOException("end"); //NOI18N
        } else {
            return (char)c;
        }
    }

    public void backup(int amount) {
        li.backup(amount);
    }

    public char BeginToken() throws IOException {
        return readChar();
    }

    public String GetImage() {
        return li.readText().toString();
    }

    public char[] GetSuffix(int len) {
        String t = GetImage();
        if(t.length() == 0) {
            return new char[]{};
        } else {
            return t.substring(t.length() - len, t.length()).toCharArray();
        }
    }

    public void Done() {
        //do nothing
    }
    
    public int getColumn() {
        return  -1;
    }

    public int getLine() {
        return -1;
    }

    public int getEndColumn() {
        return -1;
    }

    public int getEndLine() {
        return -1;
    }

    public int getBeginColumn() {
        return -1;
    }

    public int getBeginLine() {
        return -1;
    }

    //TODO how to implement this when running in lexer????????
    public int offset() {
        return -1;
    }
    

}
