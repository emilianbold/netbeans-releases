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

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.progress.ProgressHandle;

/**
 * @author Radek Matous
 */
public abstract class OperationSupportImpl {
    private static final OperationSupportImpl FOR_INSTALL = new ForInstall();
    private static final OperationSupportImpl FOR_UPDATE = new ForUpdate();
    private static final OperationSupportImpl FOR_ENABLE = new ForEnable();
    private static final OperationSupportImpl FOR_DISABLE = new ForDisable();
    private static final OperationSupportImpl FOR_UNINSTALL = new ForUninstall();
    
    public static OperationSupportImpl forInstall() {
        return FOR_INSTALL;
    }
    public static OperationSupportImpl forUpdate() {
        return FOR_UPDATE;
    }
    public static OperationSupportImpl forUninstall() {
        return FOR_UNINSTALL;
    }
    public static OperationSupportImpl forEnable() {
        return FOR_ENABLE;
    }
    public static OperationSupportImpl forDisable() {
        return FOR_DISABLE;
    }
    
    public abstract void doOperation(ProgressHandle progress/*or null*/, OperationContainer<?> container) throws OperationException;
    
    /** Creates a new instance of OperationContainer */
    private OperationSupportImpl() {
    }
    
    private static class ForEnable extends OperationSupportImpl {
        public void doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                
                Set<Module> modules = new HashSet<Module>();
                ModuleManager mm = null;
                List<? extends OperationInfo> elements = container.listAll();
                for (OperationInfo operationInfo : elements) {
                    UpdateElement updateElement = operationInfo.getUpdateElement();
                    Module m = Utils.toModule(updateElement.getCodeName (), updateElement.getSpecificationVersion ());
                    modules.add(m);
                    if (mm == null) {
                        mm = m.getManager();
                    }
                }
                assert mm != null;
                enable(mm, modules);
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }            
        }
        
        private static boolean enable(ModuleManager mm, Set<Module> toRun) throws OperationException {
            boolean retval = false;
            try {
                mm.enable(toRun);
                retval = true;
            } catch(IllegalArgumentException ilae) {
                throw new OperationException(OperationException.ERROR_TYPE.ENABLE);
            } catch(InvalidException ie) {
                throw new OperationException(OperationException.ERROR_TYPE.ENABLE);
            }
            return retval;
        }
        
    }
    private static class ForDisable extends OperationSupportImpl {
        public void doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                Set<Module> modules = new HashSet<Module>();
                ModuleManager mm = null;
                
                List<? extends OperationInfo> elements = container.listAll();
                for (OperationInfo operationInfo : elements) {
                    UpdateElement updateElement = operationInfo.getUpdateElement();
                    Module m = Utils.toModule(updateElement.getCodeName (), updateElement.getSpecificationVersion ());
                    modules.add(m);
                    if (mm == null) {
                        mm = m.getManager();
                    }
                }
                assert mm != null;
                mm.disable(modules);
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }
        }
    }
    private static class ForUninstall extends OperationSupportImpl {
        public void doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                ModuleDeleterImpl deleter = new ModuleDeleterImpl();
                
                List<? extends OperationInfo> elements = container.listAll();
                List<Module> modules = new ArrayList<Module> ();
                for (OperationInfo operationInfo : elements) {
                    UpdateElement updateElement = operationInfo.getUpdateElement();
                    UpdateUnit u = UpdateManagerImpl.getInstance().getUpdateUnit(updateElement.getCodeName());
                    modules.add(Utils.toModule(u));
                }
                try {
                    deleter.delete((Module[])modules.toArray(new Module[modules.size()]));
                } catch(IOException iex) {
                    throw new OperationException(OperationException.ERROR_TYPE.UNINSTALL);
                }
                

                for (OperationInfo operationInfo : elements) {
                    UpdateElement updateElement = operationInfo.getUpdateElement();
                    UpdateUnit u = UpdateManagerImpl.getInstance().getUpdateUnit(updateElement.getCodeName());
                    assert u.getInstalled() != null;
                    UpdateUnitImpl impl = Trampoline.API.impl(u);
                    impl.setAsUninstalled();
                }                
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }
        }
    }
    private static class ForInstall extends OperationSupportImpl {
        public void doOperation(ProgressHandle progress,
                OperationContainer container) throws OperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    private static class ForUpdate extends OperationSupportImpl {
        public void doOperation(ProgressHandle progress,
                OperationContainer container) throws OperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
