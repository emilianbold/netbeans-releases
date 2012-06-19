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
package org.netbeans.test.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.jemmy.EventTool;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jindrich Sedek
 */
public class DumpTokens {

    private File file = null;
    private String str = null;
    private List<Token> tokens = null;

    public DumpTokens(File file) {
        this.file = file;
    }
    
    /**
     * Use <code>DumpTokens.printTokens()</code> instead for more accurate results (includes embedding)
     * @return
     * @deprecated
     */
    @Deprecated
    public String getTokenString() {
        if (str == null) {
            Logger.getLogger(DumpTokens.class.getName()).info("Getting token string");
            Iterator<Token> iterator = getTokens().iterator();
            while (iterator.hasNext()) {
                Token token = iterator.next();
                String next = token.id().name() + ":" + token.text().toString() + "\n";
                if (str == null) {
                    str = next;
                } else {
                    str = str.concat(next);
                }
            }
        }
        return str;
    }

    private List<Token> getTokens() {
        if (tokens == null) {
            try{
                tokens = dumpTokens();
            }catch(IOException e){
                AssertionError error = new AssertionError("Dumping error");
                error.initCause(e);
                throw error;
            }
        }
        return tokens;
    }

    @SuppressWarnings("unchecked")
    private List<Token> dumpTokens() throws IOException {
        Logger.getLogger(DumpTokens.class.getName()).info("Dumping tokens");
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);

        StyledDocument sDoc = ed.openDocument();
        BaseDocument doc = (BaseDocument) sDoc;
        TokenHierarchy th = null;
        TokenSequence ts = null;
        int roundCount = 0;
        while ((th == null) || (ts == null)){
            th = TokenHierarchy.get(doc);
            if (th != null){
                ts = th.tokenSequence();
            }
            roundCount++;
            if (roundCount > 50){
                throw new AssertionError("Impossible to get token hierarchy " +roundCount+ "times");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

        }
        try{
            Logger.getLogger(DumpTokens.class.getName()).info("Parsing token sequence");
            List<Token> tok = dumpTokens(ts);
            return tok;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private List<Token> dumpTokens(TokenSequence ts){
        List<Token> result = null;
        if (ts == null) {
            throw new AssertionError("No token sequence");
        }
        ts.move(0);

        if (result == null) {
            result = new ArrayList<Token>();
        }
        while (ts.moveNext()) {
            Token token = ts.token();
            if (ts.embedded()!= null){
                List<Token> emb = dumpTokens(ts.embedded());
                if (emb != null){
                    result.addAll(emb);
                }else{
                    result.add(token);
                }
            }else{
                result.add(token);
            }
        }
        return result;
    }

    public static String printTokens(File file) throws IOException {
        Logger.getLogger(DumpTokens.class.getName()).info("Dumping tokens");
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        StyledDocument sDoc = ed.openDocument();
        BaseDocument doc = (BaseDocument) sDoc;
        TokenHierarchy th = null;
        TokenSequence ts = null;
        int roundCount = 0;
        while ((th == null) || (ts == null)) {
            th = TokenHierarchy.get(doc);
            if (th != null) {
                ts = th.tokenSequence();
            }
            roundCount++;
            if (roundCount > 50) {
                throw new AssertionError("Impossible to get token hierarchy " + roundCount + "times");
            }
            
            new EventTool().waitNoEvent(1000);
        }
        
           return ts.toString().replaceAll("(st=.*)*IHC=[0-9]*", "");// remove System.identityHashCode
           
    }
}
