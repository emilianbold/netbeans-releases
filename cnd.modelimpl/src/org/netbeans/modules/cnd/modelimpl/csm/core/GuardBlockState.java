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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    public static final int NOT_INITIED = -2;
    public static final int SKIPPED = 1;
    public static final int PARSED = -1;
    public static final int NO_GUARD = 0;
    private volatile int guardBlockState = NOT_INITIED;
    public Token guard;
    
    /** Creates a new instance of GuardBlockState */
    public GuardBlockState() {
    }
    
    public void setGuardBlockState(final APTPreprocHandler preprocHandler, Token guard) {
        synchronized (this) {
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
    }
    
    /**
     *----------------------------------------
     *last state\macros  Not defined  defined
     *----------------------------------------
     *NOT_INITIED        false        false
     *NO_GUARD           false        false
     *PARSED             false        false
     *SKIPPED            true         true
     */
    public boolean isNeedReparse(final APTPreprocHandler preprocHandler) {
        synchronized (this) {
            switch (guardBlockState) {
                case NOT_INITIED:
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
    }
    
    // for tests only
    public String testGetGuardName(){
        if (guard != null){
            return guard.getText();
        }
        return null;
    }

    public Token getGuard() {
        return guard;
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

    public boolean isInited() {
        synchronized (this) {
            return this.guardBlockState != NOT_INITIED;
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
