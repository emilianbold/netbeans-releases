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

/*
 * PPToken.java
 *
 * Created on August 12, 2005, 10:48 AM
 *
 */
package org.netbeans.mobility.antext.preprocessor;

/**
 *
 * @author Adam Sotona
 */
public final class PPToken implements LineParserTokens
{
    
    private final int type;
    private final int line;
    private final int column;
    private final String padding;
    private final String text;
    private boolean value;
    private boolean hasValue;
    
    /** Creates an empty new instance of PPToken - used in LineScanner */
    PPToken()
    {
        this(END_OF_FILE, 0, 0, "", ""); //NOI18N
    }
    
    /** Creates a new instance of PPToken */
    PPToken(int type, int line, int column, String padding, String text)
    {
        this.type = type;
        this.line = line;
        this.column = column;
        this.padding = padding;
        this.text = text;
        this.value = false;
        this.hasValue = false;
    }
    
    public int getType()
    {
        return type;
    }
    
    public int getLine()
    {
        return line;
    }
    
    public int getColumn()
    {
        return column;
    }
    
    public String getPadding()
    {
        return padding;
    }
    
    public String getText()
    {
        return text;
    }
    
    public boolean hasValue()
    {
        return hasValue;
    }
    
    public boolean getValue()
    {
        return value;
    }
    
    public String toString()
    {
        return LineParser.yyname[type] + "(" + String.valueOf(line) + ", " + String.valueOf(column) + ", \"" + padding + "\", \"" + text  + (hasValue ? "\", " + String.valueOf(value) + ")" : "\")"); //NOI18N
    }
    
    /* package-private methods to setup PPLine during parsing */
    
    void setValue(final boolean value)
    {
        this.value = value;
        this.hasValue = true;
    }
}
