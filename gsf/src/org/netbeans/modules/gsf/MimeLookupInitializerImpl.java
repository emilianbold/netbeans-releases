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
package org.netbeans.modules.gsf;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.gsf.GsfIndentTaskFactory;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;


/**
 * Listen for editor lookup requests for a particular mime type, and lazily
 * initialize language support and construct an editor kit for the given
 * mime type.
 *
 * Based on MimeLookupInitializer in the Schliemann prototype by Jan Jancura.
 *
 * @author Jan Jancura
 * @author Tor Norbye
 */
public class MimeLookupInitializerImpl implements MimeLookupInitializer {

    private static final int EDITOR_KIT_ID = 1;
    private static final int INDENT_ID = 2;
    private static final int FORMAT_ID = 3;
    private static final int BRACES_ID = 4;

    private String[] mimeTypes;
    private Map<String, Lookup.Result> children = new HashMap<String,Lookup.Result>(); //<mimetype, child Lookup.Result>
    private Lookup lookup;

    public MimeLookupInitializerImpl() {
        this(new String[0]);
    }

    public MimeLookupInitializerImpl(String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    /**
     * Retrieves a Lookup.Result of MimeLookupInitializers for the given sub-mimeType.
     *
     * @param mimeType mime-type string representation e.g. "text/x-java"
     * @return non-null lookup result of MimeLookupInitializer(s).
     *  <br/>
     *  Typically there should be just one child initializer although if there
     *  will be more than one all of them will be taken into consideration.
     *  <br/>
     *  If there will be no specific initializers for the particular mime-type
     *  then an empty result should be returned.
     */
    public Lookup.Result child(String mimeType) {
        synchronized (children) {
            String[] newMimeType = new String[mimeTypes.length + 1];
            System.arraycopy(mimeTypes, 0, newMimeType, 0, mimeTypes.length);
            newMimeType[mimeTypes.length] = mimeType;

            Lookup.Result child = children.get(mimeType);

            if (child == null) {
                child = Lookups.fixed(new Object[]{new MimeLookupInitializerImpl(newMimeType)}).lookup(new Lookup.Template(MimeLookupInitializerImpl.class));
                children.put(mimeType, child);
            }

            return child;
        }
    }

    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     *
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    public Lookup lookup() {
        if (lookup == null) {
            if (mimeTypes.length != 1) {
                lookup = Lookup.EMPTY;

                return lookup;
            }

            if (LanguageRegistry.getInstance().isSupported(mimeTypes[0])) {
                final Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeTypes[0]);
                assert language != null;
                
                if (language.useCustomEditorKit()) {
                    return null;
                }

                Integer[] supportedOps = null;
                if (language.hasFormatter()) {
                    supportedOps = new Integer[]{Integer.valueOf(EDITOR_KIT_ID), Integer.valueOf(INDENT_ID), Integer.valueOf(FORMAT_ID) };
                } else {
                    supportedOps = new Integer[]{Integer.valueOf(EDITOR_KIT_ID), Integer.valueOf(INDENT_ID) };
                }
                
                lookup = Lookups.fixed(supportedOps, new InstanceContent.Convertor<Integer, Object>() {

                    public Object convert(Integer i) {
                        switch (i.intValue()) {
                        case EDITOR_KIT_ID: {
                            GsfEditorKitFactory outer = new GsfEditorKitFactory(language);

                            return outer.kit();
                        }
                        //case BRACES_ID: {
                        //    return new BraceHighlighting(mimeTypes[0]);
                        //}
                        case FORMAT_ID:
                            return new GsfReformatTaskFactory();
                        case INDENT_ID:
                            return new GsfIndentTaskFactory();
                        }

                        return null;
                    }

                    public Class<? extends Object> type(Integer i) {
                        switch (i.intValue()) {
                        case EDITOR_KIT_ID:
                            return GsfEditorKitFactory.GsfEditorKit.class;
                        case FORMAT_ID:
                            return GsfReformatTaskFactory.class;
                        case INDENT_ID:
                            return GsfIndentTaskFactory.class;
                        //case BRACES_ID:
                        //    return BracesMatcherFactory.class;
                        }


                        return null;
                    }

                    public String id(Integer i) {
                        return i.toString();
                    }

                    public String displayName(Integer i) {
                        return i.toString();
                    }
                });
            }
        }

        return lookup;
    }
}
