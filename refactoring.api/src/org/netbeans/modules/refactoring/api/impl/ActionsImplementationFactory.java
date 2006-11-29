/* * The contents of this file are subject to the terms of the Common Development * and Distribution License (the License). You may not use this file except in * compliance with the License. * * You can obtain a copy of the License at http://www.netbeans.org/cddl.html * or http://www.netbeans.org/cddl.txt. * * When distributing Covered Code, include this CDDL Header Notice in each file * and include the License file at http://www.netbeans.org/cddl.txt. * If applicable, add the following below the CDDL Header, with the fields * enclosed by brackets [] replaced by your own identifying information: * "Portions Copyrighted [year] [name of copyright owner]" * * The Original Software is NetBeans. The Initial Developer of the Original * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun * Microsystems, Inc. All Rights Reserved. */package org.netbeans.modules.refactoring.api.impl;import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;import org.openide.util.Lookup;/** * @author Jan Becicka */public final class ActionsImplementationFactory {        private ActionsImplementationFactory(){}        private static final Lookup.Result<ActionsImplementationProvider> implementations =        Lookup.getDefault().lookup(new Lookup.Template(ActionsImplementationProvider.class));    public static boolean canRename(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canRename(lookup)) {                return true;            }        }        return false;    }        public static Runnable renameImpl(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canRename(lookup)) {                return rafi.renameImpl(lookup);            }        }        return null;    }    public static boolean canFindUsages(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canFindUsages(lookup)) {                return true;            }        }        return false;    }        public static Runnable findUsagesImpl(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canFindUsages(lookup)) {                return rafi.findUsagesImpl(lookup);            }        }        return null;    }    public static boolean canDelete(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canDelete(lookup)) {                return true;            }        }        return false;    }        public static Runnable deleteImpl(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canDelete(lookup)) {                return rafi.deleteImpl(lookup);            }        }        return null;    }        public static Runnable moveImpl(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canMove(lookup)) {                return rafi.moveImpl(lookup);            }        }        return null;    }        public static boolean canMove(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canMove(lookup)) {                return true;            }        }        return false;    }    public static Runnable copyImpl(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canCopy(lookup)) {                return rafi.copyImpl(lookup);            }        }        return null;    }        public static boolean canCopy(Lookup lookup) {        for (ActionsImplementationProvider rafi: implementations.allInstances()) {            if (rafi.canCopy(lookup)) {                return true;            }        }        return false;    }    }