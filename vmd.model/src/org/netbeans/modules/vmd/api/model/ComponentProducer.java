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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a factory for components. The DescriptorRegistry contains a set of component producers. These producers are
 * usually visualized in the palette and represents a component, a group of components or component with special post-initialization.
 * ComponentProducer is automatically created for all ComponentDescriptors which return non-null value from getPaletteDescriptor method.
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
        this.producerID = producerID;
        this.typeID = typeID;
        this.paletteDescriptor = paletteDescriptor;
    }

    /**
     * Returns producer id.
     * @return the producer id
     */
    public String getProducerID () {
        return producerID;
    }

    /**
     * Returns a type id of the main component created by the producer.
     * @return the type id of the main component
     */
    public TypeID getComponentTypeID () {
        return typeID;
    }

    /**
     * Returns palette descriptor of the producer.
     * @return the palette descriptor
     */
    public PaletteDescriptor getPaletteDescriptor () {
        return paletteDescriptor;
    }

    /**
     * Called for creating a component or a group of components together with their initialization.
     * @param document the document where the component should be created
     * @return the result of creation
     */
    public abstract Result createComponent (DesignDocument document);
    
    /**
     * Called for checking validity or availability of the producer for a specified document.
     * Usually it check whether the main component is in registry and the class in target language is
     * available on the class of a project where the document belongs.
     * 
     * @param document the document where the producer could be used (and therefore checked against)
     * @return the result checking; true if the producer is valid
     */
    public abstract boolean checkValidity(DesignDocument document);

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
            public Result createComponent (DesignDocument document) {
                return new Result (document.createComponent (getComponentTypeID ()));
            }

            public boolean checkValidity(DesignDocument document) {
                return true;
            }
        };
    }

}
