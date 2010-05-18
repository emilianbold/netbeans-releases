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
package org.netbeans.modules.soa.ui.form.valid;

import java.util.List;

/**
 *
 * @author nk150297
 */
public interface Validator {

    void revalidate(boolean fast);
    void clearReasons();

    void doFastValidation();
    void doDetailedValidation();
    void doValidation(boolean fast);

    Reason getReason();
    List<Reason> getReasons();
    List<Reason> getReasons(Severity severity);
    
    /**
     * If severity isn't specified (null value) then the reasons with 
     * any severity are teken into consideration.
     * @param severity
     * @return
     */
    boolean hasReasons(Severity severity);

    interface Provider {
        Validator getValidator();
    }
    
    final class Reason {
        private Severity mSeverity;
        private String mText;
        
        public Reason(Severity severity, String text) {
            assert text != null && severity != null;
            mSeverity = severity;
            mText = text;
        }
        
        public Severity getSeverity() {
            return mSeverity;
        }
        
        public String getText() {
            return mText;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Reason) {
                return ((Reason)obj).mSeverity == mSeverity &&
                ((Reason)obj).mText.equals(mText);
            }
            return false;
        }
        
        @Override
        public String toString() {
            return "Severity: " + mSeverity + " Text: " + mText;
        }
    }
    
    enum Severity {
        ERROR, WARNING;
    }
}
