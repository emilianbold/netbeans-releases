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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;

/**
 * AddWebServiceOperator class
 * Implements test functionality for "Add Web Service" dialog
 *
 * @author <a href="mailto:alexey.butenko@sun.com">Alexey Butenko</a>
 */
public class AddWebServiceOperator extends NbDialogOperator{
    
    private JRadioButtonOperator _rbtURL;
    private JRadioButtonOperator _rbtLocal;
    private JButtonOperator _btBrowse;
    //private JButtonOperator _btGetWSInf;
    private JButtonOperator _btSetProxy;
    private JButtonOperator _btAdd;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    private JTextFieldOperator _txtURL;
    private JTextFieldOperator _txtLocal;
    //private JTextFieldOperator _txtWSName;
    private JTextFieldOperator _txtWSPackageName;
    private boolean _isProxySet = false;
    private String STR_BROWSE = "Browse...";
    private String STR_PROXY = "Set Proxy...";
    private String STR_ADD = "Add";
    private String STR_CANCEL = "Cancel";
    private String STR_HELP = "Help";
    private String STR_OPEN = "Open";
    private String STR_OK = "OK";
    private String STR_OPTIONS = "Options";
    private String STR_FILE = "Local File:";
    private String STR_URL = "URL:";
    private String STR_SYSTEM_PROXY = "Use System Proxy Settings";
    private String STR_MANUAL_PROXY = "Manual Proxy Settings";
    private String wsLabel = "Services tab web services compiling script";
    
    //private JTextComponentOperator _txtResults;
    
    public AddWebServiceOperator() {
        super("Add Web Service");
    }
    
    public JRadioButtonOperator rbtURL() {
        if (_rbtURL == null) {
            _rbtURL = new JRadioButtonOperator(this, STR_URL);
        }
        return _rbtURL;
    }
    
    public JRadioButtonOperator rbtLocal() {
        if (_rbtLocal == null) {
            _rbtLocal = new JRadioButtonOperator(this, STR_FILE);
        }
        return _rbtLocal;
    }
    
    public JButtonOperator btBrowse() {
        if (_btBrowse == null) {
            _btBrowse = new JButtonOperator(this, STR_BROWSE);
        }
        return _btBrowse;
    }
    
    /*public JButtonOperator btGetWSInf() {
        if (_btGetWSInf == null) {
            _btGetWSInf = new JButtonOperator(this, "Get Web Service Information");
        }
        return _btGetWSInf;
    }*/
    
    public JButtonOperator btSetProxy() {
        if (_btSetProxy == null) {
            _btSetProxy = new JButtonOperator(this, STR_PROXY);
            
        }
        return _btSetProxy;
    }
    
    public JButtonOperator btAdd() {
        if (_btAdd == null) {
            _btAdd = new JButtonOperator(this, STR_ADD);
        }
        return _btAdd;
    }
    
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(this, STR_CANCEL);
        }
        return _btCancel;
    }
    
    public JButtonOperator btHelp() {
        if (_btHelp == null) {
            _btHelp = new JButtonOperator(this, STR_HELP);
        }
        return _btHelp;
    }
    
    public JTextFieldOperator txtURL() {
        if (_txtURL == null) {
            _txtURL = new JTextFieldOperator(this, 0);
        }
        return _txtURL;
    }
    
    public JTextFieldOperator txtLocal() {
        if (_txtLocal == null) {
            _txtLocal = new JTextFieldOperator(this, 1);
        }
        return _txtLocal;
    }
    
    /*public JTextFieldOperator txtWSName() {
        if (_txtWSName == null) {
            //System.out.println("TRACE: "+new JTextFieldOperator(this, 0).getText());
            //System.out.println("TRACE: "+new JTextFieldOperator(this, 1).getText());
            //System.out.println("TRACE: "+new JTextFieldOperator(this, 2).getText());
            //System.out.println("TRACE: "+new JTextFieldOperator(this, 3).getText());
            //System.out.println("TRACE: "+new JTextFieldOperator(this, 4).getText());
            //_txtWSName = new JTextFieldOperator(this, 4);
            try{
                _txtWSName = new JTextFieldOperator(this, 6);
            }catch(Exception e){
                _txtWSName = null;
            }
        }
        return _txtWSName;
    }*/
    
    public JTextFieldOperator txtWSPackageName() {
        if (_txtWSPackageName == null) {
            _txtWSPackageName = new JTextFieldOperator(this, 2);
        }
        return _txtWSPackageName;
    }
    
    /*public JTextComponentOperator txtResults() {
        if (_txtResults == null) {
            //_txtResults = new JTextComponentOperator(this,9);
            _txtResults = new JTextComponentOperator(this,8);
        }
        return _txtResults;
    }*/
    
    public boolean isProxySet() {
        return _isProxySet;
    }
    
    
    public void verify() {
        btAdd();
        btCancel();
        btHelp();
        //btGetWSInf();
        btBrowse();
        btSetProxy();
        rbtLocal();
        rbtURL();
        txtLocal();
        txtURL();
        //txtWSName();
        txtWSPackageName();
        //txtResults();
    }
    
    public String addLocalWebService(String name) {
        JRadioButtonOperator wsType = rbtLocal();
        wsType.requestFocus();
        wsType.setSelected(true);
        wsType.doClick();
        TestUtils.wait(2000);
        /*btBrowse().pushNoBlock();
        TestUtils.wait(2000);
        JDialogOperator open = new JDialogOperator(STR_OPEN);
        JTextFieldOperator wsdlFileName = new JTextFieldOperator(open, 0);
        wsdlFileName.typeText(name);
        TestUtils.wait(1000);
        new JButtonOperator(open, STR_OPEN).pushNoBlock();*/
        JTextFieldOperator wsdlUrl = txtLocal();
        wsdlUrl.clearText();
        wsdlUrl.setText(name);
        TestUtils.wait(1000);
        //TODO hardcoded?
        //if ((new JTextComponentOperator(this, 2)).getText().indexOf("error occurred")!= -1) return null;
        //String wsName = "empty";
        //JTextFieldOperator wsNameOper = txtWSName();
        //if (wsNameOper!=null ) {wsName = wsNameOper.getText();}
        //        btAdd().pushNoBlock();
        btAdd().pushNoBlock();
        isWebServiceClientGenerated();
        //return wsName;
        return null;
    }
    
    public String addWebService(String url) {
        return addWebService(url, "websvc");
    }
    
    public String addWebService(String url, String packageName) {
        JRadioButtonOperator wsType = rbtURL();
        wsType.requestFocus();
        wsType.setSelected(true);
        wsType.doClick();
        TestUtils.wait(2000);
        JTextFieldOperator wsdlUrl = txtURL();
        wsdlUrl.clearText();
        wsdlUrl.setText(url);
        TestUtils.wait(1000);
        JTextFieldOperator pkgName = txtWSPackageName();
        pkgName.clearText();
        pkgName.setText(packageName);
        TestUtils.wait(1000);
        btAdd().pushNoBlock();;
        isWebServiceClientGenerated();
        return null;
    }
    
    public void setProxy(String proxyHost, String proxyPort) {
        if (isProxySet()) clearProxy();
        btSetProxy().pushNoBlock();
        //JDialogOperator proxyConfig = new JDialogOperator("Proxy Configuration");
        JDialogOperator proxyConfig = new JDialogOperator(STR_OPTIONS);
        //JCheckBoxOperator proxyCb = new JCheckBoxOperator(proxyConfig);
        TestUtils.wait(1000);
        JRadioButtonOperator proxyRb = new JRadioButtonOperator(proxyConfig, STR_MANUAL_PROXY);
        proxyRb.requestFocus();
        proxyRb.setSelected(true);
        proxyRb.doClick();
        //proxyCb.requestFocus();
        //proxyCb.setSelected(true);
        //        proxyCb.doClick();
        _isProxySet = true;
        TestUtils.wait(2000);
        new JTextFieldOperator(proxyConfig, 0).setText(proxyHost);
        TestUtils.wait(1000);
        new JTextFieldOperator(proxyConfig,1).setText(proxyPort);
        TestUtils.wait(1000);
        new JButtonOperator(proxyConfig, STR_OK).pushNoBlock();
    }
    
    public void clearProxy() {
        if (isProxySet()) {
            btSetProxy().pushNoBlock();
            //JDialogOperator proxyConfig = new JDialogOperator("Proxy Configuration");
            JDialogOperator proxyConfig = new JDialogOperator(STR_OPTIONS);
            //JCheckBoxOperator proxyCb = new JCheckBoxOperator(proxyConfig);
            TestUtils.wait(1000);
            JRadioButtonOperator proxyRb = new JRadioButtonOperator(proxyConfig, STR_SYSTEM_PROXY);
            //proxyCb.requestFocus();
            //proxyCb.setSelected(false);
            //proxyCb.doClick();
            proxyRb.requestFocus();
            proxyRb.setSelected(true);
            proxyRb.doClick();
            _isProxySet = false;
            TestUtils.wait(1000);
            new JButtonOperator(proxyConfig, STR_OK).pushNoBlock();
        }
    }
    
    private boolean isWebServiceClientGenerated() {
        TestUtils.wait(500);
        JLabelOperator jlo = new JLabelOperator(Util.getMainWindow(), 1);
        while (jlo.getText() == null || jlo.getText().indexOf(wsLabel) == -1) {
            jlo = new JLabelOperator(Util.getMainWindow(), 1);
            System.out.println("label in first cycle = " + jlo.getText());
            TestUtils.wait(1000);
        }
        while (jlo.getText() != null && jlo.getText().indexOf(wsLabel) != -1) {
            jlo = new JLabelOperator(Util.getMainWindow(), 1);
            TestUtils.wait(1000);
        }
        TestUtils.wait(1000);
        return true;
    }
}
