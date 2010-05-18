/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class GeneralEditorModel implements GeneralEditorForm.Model{
    

    private String location = "";
    private String transportProtocol = "";
    private String use = "";
    private String encodingStyle = "";
    private String llpType = "";
    private Byte startBlockCharacter = null;
    private Byte endBlockCharacter = null;
    private Byte endDataCharacter = null;
    private boolean hllpChecksumEnabled;
    private int mllpv2RetriesCountOnNak = 0;
    private long mllpv2RetryInterval = 0;
    private long mllpv2TimeToWaitForAckNak = 0 ;
    private String part = "";
    private boolean persistenceEnabled;
    
            
    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = Utils.safeString(location);
    }

    public String getTransportProtocol() {
        return this.transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = Utils.safeString(transportProtocol);
    }

    public String getUse() {
        return this.use;
    }

    public void setUse(String use) {
        this.use = Utils.safeString(use);
    }

    public String getEncodingStyle() {
        return this.encodingStyle;
    }

    public void setEncodingStyle(String encodingStyle) {
        this.encodingStyle = Utils.safeString(encodingStyle);
    }

    public String getLLPType() {
        return this.llpType;
    }

    public void setLLPType(String llpType) {
        this.llpType = Utils.safeString(llpType);
    }

    public Byte getStartBlockCharacter() {
        return this.startBlockCharacter;
    }

    public void setStartBlockCharacter(Byte startBlockChar) {
        this.startBlockCharacter = startBlockChar;
    }

    public Byte getEndBlockCharacter() {
        return this.endBlockCharacter;
    }

    public void setEndBlockCharacter(Byte endBlockChar) {
        this.endBlockCharacter = endBlockChar;
    }

    public Byte getEndDataCharacter() {
        return this.endDataCharacter;
    }

    public void setEndDataCharacter(Byte endDataChar) {
        this.endDataCharacter = endDataChar;
    }

    public boolean isHLLPChecksumEnabled() {
        return this.hllpChecksumEnabled;
    }

    public void setHLLPChecksumEnabled(boolean enabled) {
        this.hllpChecksumEnabled = enabled;
    }


    public int getMllpv2RetriesCountOnNak() {
        return this.mllpv2RetriesCountOnNak;
    }

    public void setMllpv2RetriesCountOnNak(int count) {
        this.mllpv2RetriesCountOnNak = count;
    }

    public long getMllpv2RetryInterval() {
        return this.mllpv2RetryInterval;
    }

    public void setMllpv2RetryInterval(long interval) {
        this.mllpv2RetryInterval = interval;
    }

    public long getMllpv2TimeToWaitForAckNak() {
        return this.mllpv2TimeToWaitForAckNak;
    }

    public void setMllpv2TimeToWaitForAckNak(long duration) {
        this.mllpv2TimeToWaitForAckNak = duration;
    }

    public String getPart() {
        return this.part;
    }

    public void setPart(String part) {
        this.part = Utils.safeString(part);
    }
    public boolean isPersistenceEnabled() {
        return this.persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean enabled) {
        this.persistenceEnabled = enabled;
    }
    

}
