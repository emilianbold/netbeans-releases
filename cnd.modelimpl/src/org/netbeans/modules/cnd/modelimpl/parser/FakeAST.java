/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.parser;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Fake AST managing type
 * @author Vladimir Kvasihn
 */
public class FakeAST extends BaseAST implements Serializable {
    private static final long serialVersionUID = -1975495157952844447L;
    
    private final static String[] tokenText = new String[CPPTokenTypes.CSM_END + 1];
    
    int ttype = Token.INVALID_TYPE;
    
    public FakeAST() {
    }
    
    /** Get the token type for this node */
    public int getType() {
        return ttype;
    }
    
    public void initialize(int t, String txt) {
        setType(t);
        setText(txt);
    }
    
    public void initialize(AST t) {
        setText(t.getText());
        setType(t.getType());
    }
    
    public void initialize(Token tok) {
        setText(tok.getText());
        setType(tok.getType());
    }
    
    /** Set the token type for this node */
    public void setType(int ttype_) {
        ttype = ttype_;
    }
    
    public String getText() {
        init();
        return  tokenText[getType()];
    }
    
    private static boolean initedText = false;
    private synchronized void init() {
        if (!initedText) {
            // fill array by reflection
            // used only for trace
            Field[] fields = CPPTokenTypes.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                int flags = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
                Field field = fields[i];
                if ((field.getModifiers() & flags) == flags &&
                        int.class.isAssignableFrom(field.getType())) {
                    try {
                        int value = field.getInt(null);
                        String name = field.getName();
                        tokenText[value]=name;
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            initedText = true;
        }
    }
}
