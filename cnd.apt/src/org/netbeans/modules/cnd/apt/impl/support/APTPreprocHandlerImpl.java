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

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * composition of include handler and macro map for parsing file phase
 * @author Vladimir Voskresensky
 */
public class APTPreprocHandlerImpl implements APTPreprocHandler {
    private boolean compileContext;
    private boolean isValid = true;
    private APTMacroMap macroMap;
    private APTIncludeHandler inclHandler;
    
    /**
     * @param compileContext determine wether state created for real parse-valid
     * context, i.e. source file has always correct state, but header itself has
     * not correct state untill it was included into any source file (may be recursively)
     */
    public APTPreprocHandlerImpl(APTMacroMap macroMap, APTIncludeHandler inclHandler, boolean compileContext) {
        this.macroMap = macroMap;
        this.inclHandler = inclHandler;
        this.compileContext = compileContext;
    }
    
    public APTMacroMap getMacroMap() {
        return macroMap;
    }

    public APTIncludeHandler getIncludeHandler() {
        return inclHandler;
    }   
    
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)
    
    public State getState() {
        return createStateImpl();
    }
    
    public void setState(State state) {
        if (state instanceof StateImpl) {
            ((StateImpl)state).restoreTo(this);
        }
    }
    
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean isValid() {
        return isValid;
    }
    
    public boolean isCompileContext() {
        return compileContext;
    }
    
    protected StateImpl createStateImpl() {
        return new StateImpl(this);
    }
    
    private void setCompileContext(boolean state) {
        this.compileContext = state;
    }
    
    public final static class StateImpl implements State {
        /*package*/ final APTMacroMap.State macroState;
        /*package*/ final APTIncludeHandler.State inclState;
        private final byte attributes;
        
        private final static byte COMPILE_CONTEXT_FLAG = 1 << 0;
        private final static byte CLEANED_FLAG = 1 << 1;
        private final static byte VALID_FLAG = 1 << 2;
        
        private static byte createAttributes(boolean compileContext, boolean cleaned, boolean valid) {
            byte out = 0;
            if (compileContext) {
                out |= COMPILE_CONTEXT_FLAG;
            } else {
                out &= ~COMPILE_CONTEXT_FLAG;
            }
            if (cleaned) {
                out |= CLEANED_FLAG;
            } else {
                out &= ~CLEANED_FLAG;
            }
            if (valid) {
                out |= VALID_FLAG;
            } else {
                out &= ~VALID_FLAG;
            }
            return out;
        }
        
        protected StateImpl(APTPreprocHandlerImpl handler) {
            if (handler.getMacroMap() != null) {
                this.macroState = handler.getMacroMap().getState();
            } else {
                this.macroState = null;
            }
            if (handler.getIncludeHandler() != null) {
                this.inclState = handler.getIncludeHandler().getState();
            } else {
                this.inclState = null;
            }
            this.attributes = createAttributes(handler.isCompileContext(), false, handler.isValid());
        }
        
        private StateImpl(StateImpl other, boolean cleanState, boolean compileContext, boolean valid) {
            boolean cleaned;
            if (cleanState && !other.isCleaned()) {
                // first time cleaning
                // own copy of include information and macro state
                this.inclState = APTHandlersSupportImpl.copyIncludeState(other.inclState, true);
                this.macroState = APTHandlersSupportImpl.createCleanMacroState(other.macroState);
                cleaned = true;
            } else {
                // share states
                this.macroState = other.macroState;
                cleaned = other.isCleaned();
                this.inclState = other.inclState;
            }
            this.attributes = createAttributes(compileContext, cleaned, valid);
        }
        
        private void restoreTo(APTPreprocHandlerImpl handler) {
            if (handler.getMacroMap() != null) {
                handler.getMacroMap().setState(this.macroState);
            }
            if (handler.getIncludeHandler() != null) {
                handler.getIncludeHandler().setState(this.inclState);
            }
            handler.setCompileContext(this.isCompileContext());

            handler.setValid(this.isValid());
            if (!isValid()) {
                APTUtils.LOG.log(Level.SEVERE, "setting invalid state {0}", new Object[] { this } ); // NOI18N
            }
        }

        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append(isCleaned() ? "\nCleaned State;" : "\nNot Cleaned State;"); // NOI18N
            retValue.append(isCompileContext() ? "Compile Context;" : "Default/Null State;"); // NOI18N
            retValue.append(isValid() ? "Valid State;" : "Invalid State;"); // NOI18N
            retValue.append("\nInclude state Info:\n"); // NOI18N
            retValue.append(inclState);
            retValue.append("\nMACROS state info:\n"); // NOI18N
            retValue.append(this.macroState);
            return retValue.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            // we do not compare macroStates because in case of 
            // parsing from the same include sequence they are equal
            return this.isCompileContext() == other.isCompileContext() &&
                    this.isValid() == other.isValid() && 
                    ( (this.inclState == null && other.inclState == null) ||
                      (this.inclState.equals(other.inclState)));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + (this.isCompileContext() ? 1 : 0);
            hash = 83 * hash + (this.isValid() ? 1 : 0);
            hash = 83 * hash + (this.inclState != null ? this.inclState.hashCode() : 0);
            return hash;
        }
                
        public boolean isCompileContext() {
            return (this.attributes & COMPILE_CONTEXT_FLAG) == COMPILE_CONTEXT_FLAG;
        }
        
        public boolean isCleaned() {
            return (this.attributes & CLEANED_FLAG) == CLEANED_FLAG;
        }

        public boolean isValid() {
            return (this.attributes & VALID_FLAG) == VALID_FLAG;
        }
        
        /*package*/ APTPreprocHandler.State copy() {
            return new StateImpl(this, this.isCleaned(), this.isCompileContext(), this.isValid());
        }
        
        /*package*/ APTPreprocHandler.State copyCleaned() {
            return new StateImpl(this, true, this.isCompileContext(), this.isValid());
        }
        
        /*package*/ APTPreprocHandler.State copyInvalid() {
            return new StateImpl(this, this.isCleaned(), this.isCompileContext(), false);
        }

        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(DataOutput output) throws IOException {
            output.writeByte(this.attributes);
            APTSerializeUtils.writeIncludeState(this.inclState, output);
            APTSerializeUtils.writeMacroMapState(this.macroState, output);
        }

        public StateImpl(DataInput input) throws IOException {
            this.attributes = input.readByte();
            this.inclState = APTSerializeUtils.readIncludeState(input);
            this.macroState = APTSerializeUtils.readMacroMapState(input);
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append(this.isCompileContext() ? "\nCompile Context" : "\nDefault/Null State"); // NOI18N
        retValue.append("\nInclude Info:\n"); // NOI18N
        retValue.append(this.inclHandler);
        retValue.append("\nMACROS info:\n"); // NOI18N
        retValue.append(this.macroMap);
        return retValue.toString();
    }
}
