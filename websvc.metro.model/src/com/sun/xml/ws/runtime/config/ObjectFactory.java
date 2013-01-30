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


package com.sun.xml.ws.runtime.config;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.xml.ws.runtime.config package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Tubelines_QNAME = new QName("http://java.sun.com/xml/ns/metro/config", "tubelines");
    private final static QName _TubelineMapping_QNAME = new QName("http://java.sun.com/xml/ns/metro/config", "tubeline-mapping");
    private final static QName _Tubeline_QNAME = new QName("http://java.sun.com/xml/ns/metro/config", "tubeline");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.runtime.config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MetroConfig }
     * 
     */
    public MetroConfig createMetroConfig() {
        return new MetroConfig();
    }

    /**
     * Create an instance of {@link TubelineDefinition }
     * 
     */
    public TubelineDefinition createTubelineDefinition() {
        return new TubelineDefinition();
    }

    /**
     * Create an instance of {@link TubeFactoryList }
     * 
     */
    public TubeFactoryList createTubeFactoryList() {
        return new TubeFactoryList();
    }

    /**
     * Create an instance of {@link TubeFactoryConfig }
     * 
     */
    public TubeFactoryConfig createTubeFactoryConfig() {
        return new TubeFactoryConfig();
    }

    /**
     * Create an instance of {@link TubelineMapping }
     * 
     */
    public TubelineMapping createTubelineMapping() {
        return new TubelineMapping();
    }

    /**
     * Create an instance of {@link Tubelines }
     * 
     */
    public Tubelines createTubelines() {
        return new Tubelines();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Tubelines }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/metro/config", name = "tubelines")
    public JAXBElement<Tubelines> createTubelines(Tubelines value) {
        return new JAXBElement<Tubelines>(_Tubelines_QNAME, Tubelines.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TubelineMapping }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/metro/config", name = "tubeline-mapping")
    public JAXBElement<TubelineMapping> createTubelineMapping(TubelineMapping value) {
        return new JAXBElement<TubelineMapping>(_TubelineMapping_QNAME, TubelineMapping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TubelineDefinition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/metro/config", name = "tubeline")
    public JAXBElement<TubelineDefinition> createTubeline(TubelineDefinition value) {
        return new JAXBElement<TubelineDefinition>(_Tubeline_QNAME, TubelineDefinition.class, null, value);
    }

}
