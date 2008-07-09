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

package org.netbeans.modules.visualweb.gravy.websvc;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.visualweb.gravy.NbDialogOperator;

/**
 * TestMethodOperator class
 * Implements test functionality for "Test Web Service Method" dialog
 *
 * @author <a href="mailto:alexey.butenko@sun.com">Alexey Butenko</a>
 */
public class TestMethodOperator  extends NbDialogOperator{
    
    private JButtonOperator _btSubmit;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;
    private JTableOperator _tblValues;
    private JTableOperator _tblResults;
    private String STR_SUBMIT = "Submit";
    private String STR_CLOSE = "Close";
    private String STR_HELP = "Help";
    
    public TestMethodOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.websvcmgr.ui.Bundle","TEST_WEB_SERVICE_METHOD"));
    }
    
    public JButtonOperator btSubmit() {
        if (_btSubmit == null) {
            _btSubmit = new JButtonOperator(this, STR_SUBMIT);
        }
        return _btSubmit;
    }

    public JButtonOperator btClose() {
        if (_btClose == null) {
            _btClose = new JButtonOperator(this, STR_CLOSE);
        }
        return _btClose;
    }
    
    public JButtonOperator btHelp() {
        if (_btHelp == null) {
            _btHelp = new JButtonOperator(this, STR_HELP);
        }
        return _btHelp;
    }

    public JTableOperator tblValues() {
        if (_tblValues == null) {
            _tblValues = new JTableOperator(this, 0);
        }
        return _tblValues;
    }

    public JTableOperator tblResults() {
        if (_tblResults == null) {
            _tblResults = new JTableOperator(this, 1);
        }
        return _tblResults;
    }


    public void verify() {
        btSubmit();
        btClose();
        btHelp();
        tblValues();
        tblResults();
    }
}
