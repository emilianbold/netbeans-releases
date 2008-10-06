/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api.customizer.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.maven.api.customizer.ModelHandle;

/**
 *
 * @author mkleint
 */
public class ReflectionTextComponentUpdater extends TextComponentUpdater {
    private Object model;
    private Object defaults;
    private Method modelgetter;
    private Method defgetter;
    private Method modelsetter;
    private ModelHandle handle;
    /** Creates a new instance of ReflectionTextComponentUpdater */
    public ReflectionTextComponentUpdater(String getter, String setter, Object model, Object defaults, JTextComponent field, JLabel label, ModelHandle handle) 
                        throws NoSuchMethodException {
        super(field, label);
        this.model = model;
        this.defaults = defaults;
        modelgetter = model.getClass().getMethod(getter, new Class[0]);
        modelsetter = model.getClass().getMethod(setter, new Class[] {String.class});
        if (defaults != null) {
            defgetter = defaults.getClass().getMethod(getter, new Class[0]);
        }
        this.handle = handle;
        
    }
    
    public String getValue() {
        try {
            return (String)modelgetter.invoke(model, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public String getDefaultValue() {
        if (defgetter == null) {
            return null;
        }
        try {
            return (String)defgetter.invoke(defaults, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void setValue(String value) {
        try {
            modelsetter.invoke(model, new Object[] { value });
            handle.markAsModified(model);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
}
