/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetCD;

import org.netbeans.modules.vmd.midp.components.databinding.ContactsDataSetCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetAbstractCD;
import org.netbeans.modules.vmd.midp.components.databinding.IndexableDataSetCD;
import org.netbeans.modules.vmd.midp.java.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public abstract class DataSetAbstractProducer extends ComponentProducer {

    private String fqnNameCheck;

    public DataSetAbstractProducer(String fqnNameCheck, String producerID, TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super(producerID, typeID, paletteDescriptor);
        this.fqnNameCheck = fqnNameCheck;
    }

    @Override
    public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
        Boolean isValid;
        if (useCachedValue) {
            isValid = MidpJavaSupport.getCache(document).checkValidityCached(fqnNameCheck); // NOI18N
        } else {
            isValid = MidpJavaSupport.checkValidity(document, fqnNameCheck); // NOI18N
        }

        return isValid;
    }

    public static final class DataSetProducer extends DataSetAbstractProducer {

        public DataSetProducer() {
            super("javax.microedition.lcdui.Canvas", //NOI18N
                    DataSetCD.TYPEID.toString(),
                    DataSetCD.TYPEID,
                    new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DATABINDING,
                                          NbBundle.getMessage(DataSetAbstractProducer.class, "DISP_DataSet"),//NOI18N
                                          NbBundle.getMessage(DataSetAbstractProducer.class, "TTIP_DataSet"), //NOI18N
                                          DataSetAbstractCD.ICON_PATH,
                                          DataSetAbstractCD.LARGE_ICON_PATH));
        }

        @Override
        public Result postInitialize(DesignDocument document, DesignComponent mainComponent) {
            MidpProjectSupport.addLibraryToProject(document, "DataBindingME"); //NOI18N
            mainComponent.writeProperty(DataSetAbstractCD.PROP_READ_ONLY, MidpTypes.createBooleanValue(false));
            return new Result (mainComponent);
        }
    }
   
    public static final class ContactsDataSetProducer extends IndexableDataSetAbstractProducer {

        public ContactsDataSetProducer() {
            super("javax.microedition.lcdui.Canvas", //NOI18N
                    ContactsDataSetCD.TYPEID.toString(),
                    ContactsDataSetCD.TYPEID,
                    new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DATABINDING,
                                          NbBundle.getMessage(DataSetAbstractProducer.class, "DISP_ContactsDataSet"), //NOI18N
                                          NbBundle.getMessage(DataSetAbstractProducer.class, "TTIP_ContactsDataSet"), //NOI18N
                                          DataSetAbstractCD.ICON_PATH,
                                          DataSetAbstractCD.LARGE_ICON_PATH));
        }

        @Override
        public Result postInitialize(DesignDocument document, DesignComponent mainComponent) {
            MidpProjectSupport.addLibraryToProject(document, "DataBindingME", "DataBindingME-PIM"); //NOI18N
            mainComponent.writeProperty(DataSetAbstractCD.PROP_READ_ONLY, MidpTypes.createBooleanValue(true));
            return new Result (mainComponent);
        }
    }

    public static final class IndexableDataSetProducer extends DataSetAbstractProducer {

        public IndexableDataSetProducer() {
            super("javax.microedition.lcdui.Canvas", //NOI18N
                  IndexableDataSetCD.TYPEID.toString(),
                  IndexableDataSetCD.TYPEID,
                  new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DATABINDING,
                                        NbBundle.getMessage(DataSetAbstractProducer.class, "DISP_IndexableDataSet"), //NOI18N
                                        NbBundle.getMessage(DataSetAbstractProducer.class, "TTIP_IndexableDataSet"), //NOI18N,
                                        DataSetAbstractCD.ICON_PATH,
                                        DataSetAbstractCD.LARGE_ICON_PATH));
        }
    }

    protected static class IndexableDataSetAbstractProducer extends DataSetAbstractProducer {

        public IndexableDataSetAbstractProducer(String fqnNameCheck, String producerID, TypeID typeID, PaletteDescriptor paletteDescriptor) {
            super(fqnNameCheck, producerID, typeID, paletteDescriptor);
        }

        @Override
        public Result postInitialize(DesignDocument document, DesignComponent mainComponent) {
            MidpProjectSupport.addLibraryToProject(document, "DataBindingME", "DataBindingME-PIM"); //NOI18N
            mainComponent.writeProperty(DataSetAbstractCD.PROP_READ_ONLY, MidpTypes.createBooleanValue(false));
            return new Result (mainComponent);
        }
    }
}


