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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import antlr.Token;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;

/**
 *
 * @author Alexander Simon
 */
public class GuardBlockState {
    public static final int SKIPPED = 1;
    public static final int PARSED = -1;
    public static final int NO_GUARD = 0;
    private int guardBlockState = 0;
    public Token guard;
    
    /** Creates a new instance of GuardBlockState */
    public GuardBlockState() {
    }
    
    public void setGuardBlockState(final APTPreprocHandler preprocHandler, Token guard) {
        if (guard != null) {
            this.guard = new MyToken(guard.getText());
            assert preprocHandler.getMacroMap() != null;
            if (preprocHandler.getMacroMap().isDefined(guard)){
                if (guardBlockState != SKIPPED) {
                    guardBlockState = SKIPPED;
                }
            } else {
                if (guardBlockState != PARSED) {
                    guardBlockState = PARSED;
                }
            }
        } else {
            this.guard = null;
            if (guardBlockState != NO_GUARD) {
                guardBlockState = NO_GUARD;
            }
        }
    }
    
    /**
     *----------------------------------------
     *last state\macros  Not defined  defined
     *----------------------------------------
     *NO_GUARD           false        false
     *PARSED             false        false
     *SKIPPED            true         true
     */
    public boolean isNeedReparse(final APTPreprocHandler preprocHandler) {
        switch (guardBlockState) {
            case NO_GUARD:
                return false;
            case PARSED:
                return false;
            case SKIPPED:
                assert guard != null : "Guard is null";
                assert preprocHandler.getMacroMap() != null : "Macro map is null";
                return !preprocHandler.getMacroMap().isDefined(guard);
        }
        return false;
    }
    
    // for tests only
    public String testGetGuardName(){
        if (guard != null){
            return guard.getText();
        }
        return null;
    }

    public void write(DataOutput output) throws IOException {
        output.writeInt(guardBlockState);
        output.writeBoolean(guard != null);
        if (guard != null) {
            output.writeUTF(guard.getText());
        }
    }  
    
    public GuardBlockState(DataInput input) throws IOException {
        guardBlockState = input.readInt();
        if (input.readBoolean()){
            guard = new MyToken(input.readUTF());
        }
    }       

    private class MyToken implements Token{
        private final String text;
        private MyToken(String text){
            this.text = text;
        }
        public int getColumn() {
            throw new UnsupportedOperationException();
        }

        public void setColumn(int c) {
            throw new UnsupportedOperationException();
        }

        public int getLine() {
            throw new UnsupportedOperationException();
        }

        public void setLine(int l) {
            throw new UnsupportedOperationException();
        }

        public String getFilename() {
            throw new UnsupportedOperationException();
        }

        public void setFilename(String name) {
            throw new UnsupportedOperationException();
        }

        public String getText() {
            return text;
        }

        public void setText(String t) {
            throw new UnsupportedOperationException();
        }

        public int getType() {
            throw new UnsupportedOperationException();
        }

        public void setType(int t) {
            throw new UnsupportedOperationException();
        }
    }
}
