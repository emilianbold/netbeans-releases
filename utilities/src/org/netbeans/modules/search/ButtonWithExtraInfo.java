/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import javax.swing.JRadioButton;

/**
 * A radio-button with an extra information displayed by the main text.
 *
 * @author  Marian Petras
 */
class ButtonWithExtraInfo extends JRadioButton {

    private static final String START = "("; // NOI18N
    private static final String END = ")"; // NOI18N
    private static final String SP = " "; // NOI18N
    private static final String ELLIPSIS = "..."; // NOI18N

    private static final int MAX_EXTRA_INFO_LEN = 10;

    private String extraInfo;

    public ButtonWithExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    @Override
    public void setText(String text) {
        if(isExtraInfoExists()) {
            setToolTipText(getFullText(text, extraInfo));
            super.setText(getTextForLabel(text));
        }
        else {
            super.setText(text);
        }
    }

    private boolean isExtraInfoExists() {
        return extraInfo != null && extraInfo.length() > 0;
    }

    private String getTextForLabel(String text) {
        String extraText = extraInfo;
        if(extraInfo.length() > MAX_EXTRA_INFO_LEN) {
            extraText = extraInfo.substring(0, MAX_EXTRA_INFO_LEN) + ELLIPSIS;
            if(extraText.length() >= extraInfo.length()) {
                extraText = extraInfo;
            }
        }
        return getFullText(text, extraText);
    }

    private String getFullText(String text, String extraText) {
        return text + SP + START + SP + extraText + SP + END;
    }

}
