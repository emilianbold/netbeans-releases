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

package org.netbeans.api.autoupdate;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl;

/**
 *
 * @param Support 
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationContainer<Support> {
    public static OperationContainer<InstallSupport> createForInstall() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForInstall(), new InstallSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    public static OperationContainer<OperationSupport> createForDirectInstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForDirectInstall(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }    
    public static OperationContainer<InstallSupport> createForUpdate() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForUpdate(), new InstallSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    public static OperationContainer<OperationSupport> createForDirectUpdate() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDirectUpdate(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }    
    public static OperationContainer<OperationSupport> createForUninstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForUninstall(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    public static OperationContainer<OperationSupport> createForEnable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForEnable(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;        
    }
    public static OperationContainer<OperationSupport> createForDisable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDisable(), new OperationSupport());
        retval.getSupport().setContainer(retval);
        return retval;
    }
    
    public static OperationContainer<OperationSupport> createForCustomInstallComponent () {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForInstallNativeComponent (), new OperationSupport());
        retval.getSupport ().setContainer (retval);
        return retval;
    }
    
    public static OperationContainer<OperationSupport> createForCustomUninstallComponent () {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForUninstallNativeComponent (), new OperationSupport());
        retval.getSupport ().setContainer (retval);
        return retval;
    }
    
    public Support getSupport() {
        if (!init) {
            init = true;
            return support;
        }
        return (listAll().size() > 0 && listInvalid().size() == 0) ? support : null;
    }
    public boolean canBeAdded(UpdateUnit updateUnit, UpdateElement updateElement) {
        return impl.isValid(updateUnit, updateElement);
    }
    public void add(Collection<UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException("Cannot add null value.");
        for (UpdateElement el : elems) {
            add(el);
        }
    }
    
    public void add (Map<UpdateUnit, UpdateElement> elems) { 
        if (elems == null) throw new IllegalArgumentException ("Cannot add null value.");
        for (Map.Entry<UpdateUnit, UpdateElement> entry : elems.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }
    
    
    /*public OperationInfo<Support> add(UpdateElement updateElement) {
        UpdateUnit updateUnit = UpdateManagerImpl.getInstance().getUpdateUnit(updateElement.getCodeName());
        return impl.add (updateUnit, updateElement);
    }*/
    public OperationInfo<Support> add(UpdateUnit updateUnit,UpdateElement updateElement) {
        //UpdateUnit updateUnit = UpdateManagerImpl.getInstance().getUpdateUnit(updateElement.getCodeName());
        return impl.add (updateUnit, updateElement);
    }
    
    public OperationInfo<Support> add(UpdateElement updateElement) {
        UpdateUnit updateUnit = updateElement.getUpdateUnit ();
        return impl.add (updateUnit, updateElement);
    }
    
    
    public void remove (Collection<UpdateElement> elems) { 
        if (elems == null) throw new IllegalArgumentException ("Cannot add null value.");
        for (UpdateElement el : elems) {
            remove (el);
        }
    }        
    public boolean remove(UpdateElement updateElement) {
        return impl.remove(updateElement);
    }
    public boolean contains(UpdateElement updateElement) {
        return impl.contains(updateElement);
    }

    public List<OperationInfo<Support>> listAll () {
        return impl.listAllWithPossibleEager ();
    }
    //returns invalid OperationInfo
    public List<OperationInfo<Support>> listInvalid () {
        return impl.listInvalid ();
    }
    public void remove (OperationInfo<Support> op) {
        impl.remove (op);
    }
    public void removeAll () {
        impl.removeAll ();
    }
    
    public static final class OperationInfo<Support> {
        OperationContainerImpl<Support>.OperationInfoImpl<Support> impl;
        
        OperationInfo (OperationContainerImpl<Support>.OperationInfoImpl<Support> impl) {
            this.impl = impl;
        }
        
        public UpdateElement getUpdateElement() {return impl.getUpdateElement();}
        public UpdateUnit getUpdateUnit() {return impl.getUpdateUnit();}        
        public Set<UpdateElement> getRequiredElements(){return new LinkedHashSet<UpdateElement> (impl.getRequiredElements ());}
        public Set<String> getBrokenDependencies(){return impl.getBrokenDependencies();}
    }

    //end of API - next just impl details
    /** Creates a new instance of OperationContainer */
    private  OperationContainer(OperationContainerImpl<Support> impl, Support t) {
        this.impl = impl;
        this.support = t;
        impl.setOperationContainer (this);
    }
    
    OperationContainerImpl<Support> impl;
    private Support support;
    private boolean init = false;
}