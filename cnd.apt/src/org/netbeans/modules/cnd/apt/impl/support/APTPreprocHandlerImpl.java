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

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;

/**
 * composition of include handler and macro map for parsing file phase
 * @author Vladimir Voskresensky
 */
public class APTPreprocHandlerImpl implements APTPreprocHandler {
    private boolean stateCorrect;
    private APTMacroMap macroMap;
    private APTIncludeHandler inclHandler;
    
    /**
     * @param stateCorrect determine wether state created for real parse-valid
     * context, i.e. source file has always correct state, but header itself has
     * not correct state untill it was included into any source file (may be recursively)
     */
    public APTPreprocHandlerImpl(APTMacroMap macroMap, APTIncludeHandler inclHandler, boolean stateCorrect) {
        this.macroMap = macroMap;
        this.inclHandler = inclHandler;
        this.stateCorrect = stateCorrect;
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

    public boolean isStateCorrect() {
        return stateCorrect;
    }
    
    protected StateImpl createStateImpl() {
        return new StateImpl(this);
    }
    
    private void setStateCorrect(boolean state) {
        this.stateCorrect = state;
    }
    
    public final static class StateImpl implements State {
        private final boolean stateCorrect;
        private final APTMacroMap.State macroState;
        private final APTIncludeHandler.State inclState;
        private final boolean cleaned;
        
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
            this.stateCorrect = handler.isStateCorrect();   
            this.cleaned = false;
        }
        
        private StateImpl(StateImpl other, boolean cleanState, boolean correctness) {
            this.stateCorrect = correctness;
            
            if (cleanState || !other.cleaned) {
                // first time cleaning
                // own copy of include information and macro state
                this.inclState = APTHandlersSupportImpl.copyIncludeState(other.inclState, true);
                this.macroState = APTHandlersSupportImpl.createCleanMacroState(other.macroState);
                // set the real "cleaned" state. for source files nothing is cleaned in fact
                this.cleaned = APTHandlersSupportImpl.isCleanedIncludeState(this.inclState);
            } else {
                // share states
                this.macroState = other.macroState;
                this.cleaned = other.cleaned;
                this.inclState = other.inclState;
            }
        }
        
        private void restoreTo(APTPreprocHandlerImpl handler) {
            if (handler.getMacroMap() != null) {
                handler.getMacroMap().setState(this.macroState);
            }
            if (handler.getIncludeHandler() != null) {
                handler.getIncludeHandler().setState(this.inclState);
            }
            handler.setStateCorrect(this.stateCorrect);
        }

        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append(this.cleaned ? "\nCleaned State\n" : "\nNot Cleaned State\n"); // NOI18N
            retValue.append(this.stateCorrect ? "Correct State" : "Default/Null State"); // NOI18N
            retValue.append("\nMACROS state info:\n"); // NOI18N
            retValue.append(this.macroState);
            retValue.append("\nInclude state Info:\n"); // NOI18N
            retValue.append(inclState);
            return retValue.toString();
        }

        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            return this.stateCorrect == other.stateCorrect &&
                    ( (this.inclState == null && other.inclState == null) ||
                      (this.inclState.equals(other.inclState)));
        }
                
        public boolean isStateCorrect() {
            return this.stateCorrect;
        }
        
        public boolean isCleaned() {
            return cleaned;
        }

        /*package*/ List<APTIncludeHandler.IncludeInfo> getIncludeStack() {
            return APTHandlersSupportImpl.getIncludeStack(this.inclState);
        }        
        
        /*package*/ APTPreprocHandler.State copy() {
            return new StateImpl(this, false, this.stateCorrect);
        }
        
        /*package*/ APTPreprocHandler.State copyCleaned() {
            return new StateImpl(this, true, this.stateCorrect);
        }
        
        /*package*/ APTPreprocHandler.State copyInvalid() {
            return new StateImpl(this, false, false);
        }
        
        /*package*/ List<String> getSysIncludePaths() {
            return APTHandlersSupportImpl.extractSystemIncludePaths(this.inclState);
        }
        
        /*package*/ List<String> getUserIncludePaths() {
            return APTHandlersSupportImpl.extractUserIncludePaths(this.inclState);
        }        
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(DataOutput output) throws IOException {
            output.writeBoolean(this.cleaned);
            output.writeBoolean(this.stateCorrect);
            APTSerializeUtils.writeIncludeState(this.inclState, output);
            APTSerializeUtils.writeMacroMapState(this.macroState, output);
        }

        public StateImpl(DataInput input) throws IOException {
            this.cleaned = input.readBoolean();
            this.stateCorrect = input.readBoolean();
            this.inclState = APTSerializeUtils.readIncludeState(input);
            this.macroState = APTSerializeUtils.readMacroMapState(input);
        }        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append(this.stateCorrect ? "\nCorrect State" : "\nDefault/Null State"); // NOI18N
        retValue.append("\nMACROS info:\n"); // NOI18N
        retValue.append(this.macroMap);
        retValue.append("\nInclude Info:\n"); // NOI18N
        retValue.append(this.inclHandler);
        return retValue.toString();
    }    

}
