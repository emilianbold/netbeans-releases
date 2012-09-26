/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.oracle.cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.whitelist.WhiteListQuery.Operation;
import org.netbeans.api.whitelist.WhiteListQuery.Result;
import org.netbeans.api.whitelist.WhiteListQuery.RuleDescription;
import org.netbeans.libs.oracle.cloud.api.CloudSDKHelper;
import org.netbeans.libs.oracle.cloud.scanningwrapper.IClassConfiguration;
import org.netbeans.libs.oracle.cloud.sdkwrapper.exception.SDKException;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=WhiteListQueryImplementation.UserSelectable.class,
    path="org-netbeans-api-java/whitelists")
public class WhiteListQueryImpl implements WhiteListQueryImplementation.UserSelectable, PreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(WhiteListQueryImpl.class.getSimpleName());
    
    private IClassConfiguration icc;
    private static final String WHITELIST_ID = "oracle";

    public WhiteListQueryImpl() {
        initialize();
        CloudSDKHelper.getSDKFolderPreferences().addPreferenceChangeListener(this);
    }
    
    private void initialize() {
        String folder = CloudSDKHelper.getSDKFolder();
        if (folder.length() > 0) {
            try {
                icc = CloudSDKHelper.createScanningConfiguration(folder);
            } catch (SDKException ex) {
                LOG.log(Level.INFO, "SDK folder "+folder+ " is not valid.");
            }
        }
    }
    
    @Override
    public WhiteListImplementation getWhiteList(FileObject file) {
        return icc != null ? new WhiteListImpl(icc) : null;
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WhiteListQueryImpl.class, "WhiteListQueryImpl-name");
    }

    @Override
    public String getId() {
        return WHITELIST_ID;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(CloudSDKHelper.SDK_FOLDER)) {
            initialize();
        }
    }
    
    private static class WhiteListImpl implements WhiteListImplementation {

        private IClassConfiguration icc;

        public WhiteListImpl(IClassConfiguration icc) {
            this.icc = icc;
        }
        
        @Override
        public Result check(ElementHandle<?> element, Operation operation) {
            final String[] vmSignatures = SourceUtils.getJVMSignature(element);
            org.netbeans.libs.oracle.cloud.scanningwrapper.Result res;
            final ElementKind kind = element.getKind();
            switch (kind) {
                case CLASS:
                case PACKAGE:
                case ENUM:
                case INTERFACE:
                case ANNOTATION_TYPE:
                    res = icc.checkClassAllowed(vmSignatures[0]);
                    if (!res.isAllowed() && !res.isWarningOnly()) {
                            return new Result(Collections.singletonList(new RuleDescription(
                                    NbBundle.getMessage(WhiteListQueryImpl.class, "WhiteListQueryImpl-name"), 
                                    res.getMessage(), WHITELIST_ID)));
                    }
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    String methodName = vmSignatures[1];
                    List<String> params = paramsOnly(vmSignatures[2]);
                    res = icc.checkMethodAllowed(vmSignatures[0], methodName, params);
                    if (!res.isAllowed() && !res.isWarningOnly()) {
                        return new Result(Collections.singletonList(new RuleDescription(
                                NbBundle.getMessage(WhiteListQueryImpl.class, "WhiteListQueryImpl-name"), 
                                res.getMessage(), WHITELIST_ID)));
                    }
                    break;
                case FIELD:
                    String fieldName = vmSignatures[1];
                    res = icc.checkFieldAllowed(vmSignatures[0], fieldName);
                    if (!res.isAllowed() && !res.isWarningOnly()) {
                        return new Result(Collections.singletonList(new RuleDescription(
                                NbBundle.getMessage(WhiteListQueryImpl.class, "WhiteListQueryImpl-name"), 
                                res.getMessage(), WHITELIST_ID)));
                    }
                    break;
                
            }
            return new Result();
        }
        
        @NonNull
        private List<String> paramsOnly(
                @NonNull String name) {
            assert name.charAt(0) == '(';   //NOI18N;
            int index = name.lastIndexOf(')');  //NOI18N
            assert index > 0;
            name = name.substring(1, index);
            List<String> l = new ArrayList<String>();
            
            // this is what is expected:
            //    E.g) 1. java.lang.String
            //         2. int
            //         3. java.lang.Integer[] - for array.
            //         4. com.something.MyClass.StaticInnerClass - innerclass 

            // XXX TOMAS ZEZULA: is this correct:
            
            for (String param : name.split(", ")) {
                if (param.startsWith("L")) {
                    param = param.substring(1);
                }
                if (param.endsWith(";")) {
                    param = param.substring(0, param.length()-1);
                }
                param = param.replace('/', '.');
                param = param.replace('$', '.');
                l.add(param);
            }
            return l;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            //Imutable - nop
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            //Imutable - nop
        }
    }
    
}
