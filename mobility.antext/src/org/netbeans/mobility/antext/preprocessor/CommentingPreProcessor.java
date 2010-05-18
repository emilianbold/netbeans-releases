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
 * CommentingPreProcessor.java
 *
 * Created on August 12, 2005, 9:37 AM
 */
package org.netbeans.mobility.antext.preprocessor;

import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Adam Sotona
 */
public final class CommentingPreProcessor implements Runnable
{
    
    static final String DEFAULT_COMMENT = "//# "; //NOI18N
    
    public static interface AbilitiesEvaluator
    {
        
        public boolean isAbilityDefined(String abilityName);
        
        public String getAbilityValue(String abilityName);
        
        public void requestDefineAbility(String abilityName, String value);
        
        public void requestUndefineAbility(String abilityName);
    }
    
    public static interface Source
    {
        
        public Reader createReader() throws IOException;
        
    }
    
    public static interface Destination
    {
        
        public Writer createWriter(boolean validOutput) throws IOException;
        
        public void doInsert(int line, String s) throws IOException;
        
        public void doRemove(int line, int column, int length) throws IOException;
        
    }
    
    private final Source src;
    private final Destination dest;
    private final Stack<PPBlockInfo> stack;
    private final AbilitiesEvaluator eval;
    private final ArrayList<PPBlockInfo> blockList;
    private final ArrayList<PPLine> lines;
    private PPLine line;
    private LineParser lp;
    
    protected PPBlockInfo stackTop;
    
    public CommentingPreProcessor(Source src, Destination dest, String abilities)
    {
        this(src, dest, decodeAbilitiesMap(abilities));
    }
    
    public CommentingPreProcessor(Source src, Destination dest, Map<String,String> abilities)
    {
        this.src = src;
        this.dest = dest;
        this.blockList = new ArrayList<PPBlockInfo>();
        this.lines = new ArrayList<PPLine>();
        this.stack = new Stack<PPBlockInfo>();
        this.stackTop = null;
        this.eval = new MapEvaluator(abilities);
        this.line = null;
    }
    
    public ArrayList<PPLine> getLines()
    {
        return this.lines;
    }
    
    public ArrayList<PPBlockInfo> getBlockList()
    {
        return this.blockList;
    }
    
    public void run()
    {
        Exception e = null;
        Reader r = null;
        try
        {
            r = src.createReader();
            parse(r);
        }
        catch (Exception ex)
        {
            while (lp.hasMoreLines()) lines.add(lp.nextLine()); //this reads the rest of lines in case of critical error
            e = ex;
        }
        Writer w = null;
        if (dest != null) try
        {
            w = dest.createWriter(e == null && !hasErrors() && conditionIsTrue());
            writeOutput(w);
        }
        catch (IOException ioe)
        {
            throw new PreprocessorException("IOException during write", ioe); //NOI18N
        }
        finally
        {
            if (r != null) try
            {
                r.close();
            }
            catch (IOException ioe)
            {
                throw new PreprocessorException("IOException during source reader close", ioe); //NOI18N
            }
            if (w != null) try
            {
                w.close();
            }
            catch (IOException ioe)
            {
                throw new PreprocessorException("IOException during detination writer close/flush", ioe); //NOI18N
            }
        }
        if (e != null)
        {
            if (e instanceof PreprocessorException)
            {
                e.fillInStackTrace();
                throw (PreprocessorException)e;
            }
            throw new PreprocessorException("Critical Preprocessor Exception", e); //NOI18N
        }
    }
    
    private void parse(final Reader r)
    {
        lp = new LineParser(r, eval);
        while (lp.hasMoreLines())
        {
            line = lp.nextLine();
            lines.add(line);
            final boolean reduceDebug = debugOnTop();
            switch (line.getType())
            {
                case PPLine.IF          :
                case PPLine.CONDITION   :
                case PPLine.IFDEF       :
                case PPLine.IFNDEF      :
                case PPLine.OLDIF       :
                case PPLine.MDEBUG      :
                case PPLine.DEBUG       : push(new PPBlockInfo(stackTop, line, line.hasValue(), line.getValue(), null));
                line.setBlock(stackTop);
                break;
                case PPLine.ENDIF       : line.setBlock(stackTop);
                if (stackTop != null && stackTop.getType() != PPLine.OLDIF && stackTop.getType() != PPLine.MDEBUG) pop(true);
                else line.addError("ERR_redundant_endif"); //NOI18N
                break;
                case PPLine.OLDENDIF    : line.setBlock(stackTop);
                if (stackTop != null && stackTop.getType() == PPLine.OLDIF) pop(true);
                else line.addError("ERR_redundant_old_block_end"); //NOI18N
                break;
                case PPLine.ELSE        :
                case PPLine.ELIF        :
                case PPLine.ELIFDEF     :
                case PPLine.ELIFNDEF    : if (stackTop != null && stackTop.getType() != PPLine.OLDIF && stackTop.getType() != PPLine.MDEBUG && stackTop.getType() != PPLine.ELSE)
                {
                    final PPBlockInfo ifChainAncestor = stackTop;
                    pop(false);
                    push(new PPBlockInfo(stackTop, line, line.hasValue(), line.getValue(), ifChainAncestor));
                }
                else line.addError("ERR_redundant_else"); //NOI18N
                line.setBlock(stackTop);
                break;
                case PPLine.ENDDEBUG    : line.setBlock(stackTop);
                if (stackTop != null && stackTop.getType() == PPLine.MDEBUG) pop(true);
                else line.addError("ERR_redundant_enddebug"); //NOI18N
                break;
                default                 : line.setBlock(stackTop);
            }
            
            if (reduceDebug && debugOnTop()) pop(true); //debug should have short live on stack
        }
        while (stackTop != null)
        {
            if (stackTop.getType() != PPLine.CONDITION) stackTop.addError("ERR_unterminated_block"); //NOI18N
            pop(false);
        }
    }
    
    private void writeOutput(final Writer w) throws IOException
    {
        for ( final PPLine l : lines ) {
            final Iterator<PPToken> tk = l.getTokens().iterator();
            final PPBlockInfo b = l.getBlock();
            if (b == null || b.isToBeCommented())
            {
                if (b == null || b.isActive())
                {
                    if (l.getType() == PPLine.COMMENTED)
                    {
                        final PPToken token = tk.next();
                        if (b == null && "//--".equals(token.getText()))
                        { //NOI18N
                            if (w != null) w.write(token.getText()); // do not remove //-- comments when outside a PP block
                        }
                        else
                        {
                            dest.doRemove(token.getLine(), token.getColumn(), token.getText().length());
                        }
                    }
                }
                else
                {
                    if (l.getType() == PPLine.UNCOMMENTED)
                    {
                        dest.doInsert(l.getLineNumber(), DEFAULT_COMMENT);
                        if (w != null) w.write(DEFAULT_COMMENT);
                    }
                }
            }
            if (w != null)
            {
                while (tk.hasNext())
                {
                    final PPToken token = tk.next();
                    w.write(token.getPadding());
                    w.write(token.getText());
                }
            }
        }
    }
    
    private void push(final PPBlockInfo b)
    {
        blockList.add(b);
        stack.push(stackTop);
        stackTop = b;
    }
    
    private void pop(final boolean hasFooter)
    {
        if (stack.isEmpty())
        {
            stackTop = null;
        }
        else
        {
            stackTop.setEndLine(line.getLineNumber() - (hasFooter ? 0 : 1));
            stackTop.setHasFooter(hasFooter);
            stackTop = stack.pop();
            if (debugOnTop()) pop(false); //reduction of all remaining debugs from top of the stack
        }
    }
    
    private boolean hasErrors()
    {
    	for ( PPLine ppl : lines)
    		if (ppl.hasErrors()) return true;
        return false;
    }
    
    private boolean conditionIsTrue()
    {
        if (lines.size() == 0) return true;
        final PPLine l = lines.get(0);
        return l.getType() != PPLine.CONDITION || !l.hasValue() || l.getValue();
    }
    
    private boolean debugOnTop()
    {
        return stackTop != null && stackTop.getType() == PPLine.DEBUG;
    }
    
    public static String encodeAbilitiesMap(final Map<String,String> abilities)
    {
        final StringBuffer sb = new StringBuffer();
        if (abilities != null)
        {
            for ( final Map.Entry<String,String> me : abilities.entrySet() ) {
                if (sb.length() > 0) sb.append(',');
                sb.append(me.getKey());
                final String val = me.getValue();
                if (val != null)
                {
                    sb.append('=');
                    for (int i=0; i<val.length(); i++)
                    {
                        final char c = val.charAt(i);
                        if (c == '\\' || c ==',') sb.append('\\');
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
    
    public static Map<String,String> decodeAbilitiesMap(final String abilities)
    {
        final HashMap<String,String> map = new HashMap<String,String>();
        if (abilities != null)
        {
            final StringBuffer sb = new StringBuffer();
            boolean backslash = false;
            String key = null;
            for (int i=0; i<abilities.length(); i++)
            {
                final char c = abilities.charAt(i);
                if (key == null)
                {
                    if (c == '=')
                    {
                        key = sb.toString();
                        sb.setLength(0);
                    }
                    else if (c == ',')
                    {
                        map.put(sb.toString(), null);
                        sb.setLength(0);
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
                else
                {
                    if (backslash)
                    {
                        if (c != '\\' && c != ',') sb.append('\\');
                        sb.append(c);
                        backslash = false;
                    }
                    else if (c == '\\')
                    {
                        backslash = true;
                    }
                    else if (c == ',')
                    {
                        map.put(key, sb.toString());
                        key = null;
                        sb.setLength(0);
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
            }
            if (backslash) sb.append('\\');
            if (key == null) map.put(sb.toString(), null);
            else map.put(key, sb.toString());
        }
        map.remove(""); //NOI18N
        return map;
    }
    
    private class MapEvaluator extends HashMap<String,String> implements AbilitiesEvaluator
    {
        
        public MapEvaluator()
        {
            super();
        }
        
        public MapEvaluator(Map<String,String> m)
        {
            super(m);
        }
        
        public boolean isAbilityDefined(final String abilityName)
        {
            return containsKey(abilityName);
        }
        
        public String getAbilityValue(final String abilityName)
        {
            return get(abilityName);
        }
        
        public void requestDefineAbility(final String abilityName, final String value)
        {
            if ((stackTop == null  || !stackTop.isToBeCommented() || stackTop.isActive()) && !containsKey(abilityName)) put(abilityName, value);
        }
        
        public void requestUndefineAbility(final String abilityName)
        {
            if (stackTop == null  || !stackTop.isToBeCommented() || stackTop.isActive()) remove(abilityName);
        }
    }
    
    public static void main(final String args[]) throws Exception
    {
        new CommentingPreProcessor(new Source()
        {
            public Reader createReader() throws IOException
            {
                return new FileReader(args[0]);
            }
        }, new Destination()
        {
            public void doInsert(int line, @SuppressWarnings("unused") String s) throws IOException
            {
                System.err.print(String.valueOf(line) + "+, ");
            }
            
            public void doRemove(int line, @SuppressWarnings("unused") int column, @SuppressWarnings("unused") int length) throws IOException
            {
                System.err.print(String.valueOf(line) + "-, ");
            }
            
            public Writer createWriter(boolean validOutput) throws IOException
            {
                return new StringWriter();
            }
            
        }, Collections.singletonMap("aaa", (String)null)).run();
    }
}
