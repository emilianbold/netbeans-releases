/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.vmd.api.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a factory for components. The DescriptorRegistry contains a set of component producers. These producers are
 * usually visualized in the palette and represents a component, a group of components or component with special post-initialization.
 * ComponentProducer is automatically created for all ComponentDescriptors which return non-null value from getPaletteDescriptor method.
 *
 * Usually you have to implement postInitialize method to initialize main component and/or create secondary components.
 * Then you have to implement checkValidity method.
 *
 * @author David Kaspar
 */
public abstract class ComponentProducer {

    private String producerID;
    private TypeID typeID;
    private PaletteDescriptor paletteDescriptor;

    /**
     * Creates a component producer.
     * @param producerID the unique producer id
     * @param typeID the type id of the main component created by the producer.
     * @param paletteDescriptor the palette descriptor used for visualization of the producer.
     */
    protected ComponentProducer (String producerID, TypeID typeID, PaletteDescriptor paletteDescriptor) {
        assert producerID != null  &&  typeID != null  &&  paletteDescriptor != null;
        this.producerID = producerID;
        this.typeID = typeID;
        this.paletteDescriptor = paletteDescriptor;
    }

    /**
     * Returns producer id.
     * @return the producer id
     */
    public final String getProducerID () {
        return producerID;
    }

    /**
     * Returns a type id of the main component created by the producer.
     * @return the type id of the main component
     */
    public final TypeID getMainComponentTypeID () {
        return typeID;
    }

    /**
     * Returns palette descriptor of the producer.
     * @return the palette descriptor
     */
    public final PaletteDescriptor getPaletteDescriptor () {
        return paletteDescriptor;
    }

    /**
     * Creates a component.
     * @param document the document
     * @return the result of creation
     */
    public final Result createComponent (DesignDocument document) {
        DesignComponent mainComponent = createMainComponent(document);
        assert mainComponent != null;
        Result result = postInitialize (document, mainComponent);
        assert result != null;
        assert result.getMainComponent () == mainComponent;
        return result;
    }
    
    /**
     * Creates or finds a main component of the producer for a document.
     * @param document the docuemnt
     * @return created or found non-null main component
     */
    protected DesignComponent createMainComponent (DesignDocument document) {
        return document.createComponent (getMainComponentTypeID ());
    }

    /**
     * Post-initialize main component. You can also create secondary components and initialize them too.
     * Default implementation returns a result with unchanged main component only.
     * @param document the document
     * @param mainComponent the main component usually created from getMainComponentTypeID method
     * @return the result of creation
     */
    public Result postInitialize (DesignDocument document, DesignComponent mainComponent) {
        return new Result (mainComponent);
    }
    
    /**
     * Called for checking validity or availability of the producer for a specified document.
     * Usually it check whether the main component is in registry and the class in target language is
     * available on the class of a project where the document belongs.
     * 
     * @param document the document where the producer could be used (and therefore checked against)
     * @param useCachedValue use value from cache
     * @return the result checking; true if the producer is valid, false is not valid and null if unresolved yet
     */
    public abstract Boolean checkValidity(DesignDocument document, boolean useCachedValue);

    /**
     * Represents the result of creation by the producer. Should be created by implementation of ComponentProducer.createComponent method.
     */
    public static final class Result {

        private DesignComponent mainComponent;
        private List<DesignComponent> components;

        /**
         * Creates a result with an array of components that are created by a producer.
         * @param components the array of components; the first component is taken as the main component
         */
        public Result (DesignComponent... components) {
            this.mainComponent = components.length > 0 ? components[0] : null;
            this.components = Arrays.asList (components);
            assert ! this.components.contains (null);
        }

        /**
         * Creates a result with a list of components that are created by a producer.
         * @param mainComponent the main component
         * @param components the list of non-main components
         */
        public Result (DesignComponent mainComponent, List<DesignComponent> components) {
            this.mainComponent = mainComponent;
            this.components = components;
        }

        /**
         * Returns a main component.
         * @return the main component
         */
        public DesignComponent getMainComponent () {
            return mainComponent;
        }

        /**
         * Returns all components created by a producer.
         * @return the list of all components
         */
        public List<DesignComponent> getComponents () {
            return components;
        }

    }

    static ComponentProducer createDefault (ComponentDescriptor descriptor) {
        PaletteDescriptor paletteDescriptor = descriptor.getPaletteDescriptor ();
        TypeID typeid = descriptor.getTypeDescriptor ().getThisType ();
        if (paletteDescriptor == null)
            return null;

        return new ComponentProducer (typeid.toString (), typeid, paletteDescriptor) {
            public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
                return true;
            }
        };
    }

}
