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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.java.JavaClassNameResolver;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.LoginScreenLoginCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.LoginScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.LoginScreenLoginCommandEventSourceCD;
import org.openide.util.NbBundle;

/*
 * @author Karol Harezlak
 */
public class LoginScreenProducer extends MidpComponentProducer {

    public LoginScreenProducer() {
        super(LoginScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES,
                NbBundle.getMessage(LoginScreenProducer.class, "DISP_Login_Screen"), // NOI18N
                NbBundle.getMessage(LoginScreenProducer.class, "TTIP_Login_Screen"), // NOI18N
                LoginScreenCD.ICON_PATH, LoginScreenCD.ICON_LARGE_PATH));
    }

    @Override
    public Result postInitialize(DesignDocument document, DesignComponent loginScreen) {
        DesignComponent loginCommand = MidpDocumentSupport.getSingletonCommand(document, LoginScreenLoginCommandCD.TYPEID);
        DesignComponent loginEventSource = document.createComponent(LoginScreenLoginCommandEventSourceCD.TYPEID);
        loginEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(loginScreen));
        loginEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(loginCommand));
        MidpDocumentSupport.addEventSource(loginScreen, DisplayableCD.PROP_COMMANDS, loginEventSource);
        loginScreen.writeProperty(LoginScreenCD.PROP_BGK_COLOR, MidpTypes.createIntegerValue(-3355444));
        loginScreen.writeProperty(LoginScreenCD.PROP_FRG_COLOR, MidpTypes.createIntegerValue(0x0));
        loginScreen.writeProperty(LoginScreenCD.PROP_USE_LOGIN_BUTTON, MidpTypes.createBooleanValue(Boolean.FALSE));
        loginScreen.writeProperty(LoginScreenCD.PROP_USERNAME_LABEL, MidpTypes.createStringValue(LoginScreenCD.USERNAME_LOGIN));
        loginScreen.writeProperty(LoginScreenCD.PROP_PASSWORD_LABEL, MidpTypes.createStringValue(LoginScreenCD.PASSWORD_LOGIN));
        PropertyValue loginButtonScreen = MidpTypes.createStringValue(NbBundle.getMessage(LoginScreenProducer.class, "LBL_LoginScreen_LoginButtonScreen")); // NOI18N
        loginScreen.writeProperty(LoginScreenCD.PROP_LOGIN_BUTTON_TEXT, loginButtonScreen);
        return new Result(loginScreen, loginCommand, loginEventSource);
    }

    @Override
    public boolean checkValidity(DesignDocument document) {
        JavaClassNameResolver resolver = JavaClassNameResolver.getInstance(document);
        resolver.addResolveListenerIfNotRegistered(this);
        Boolean isValid = resolver.isValid("javax.microedition.lcdui.Canvas"); // NOI18N
        return isValid != null ? isValid : true;
    }
}
