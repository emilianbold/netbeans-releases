/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.codegen;

/**
 *
 * @author gpatil
 */
public class CodegenFactory {
    public static String JAXB_MARSHALER = "jaxb.marshaler" ; //NOI18N
    public static String JAXB_UNMARSHALER = "jaxb.unmarshaler" ; //NOI18N
    public static String JAXB_CONSTRUCTS = "jaxb.constructs" ; //NOI18N
    
    public static BaseCodegenerator getCogegenerator(String type){
        BaseCodegenerator gen = null;
        if (JAXB_MARSHALER.equals(type)){
            gen = new JAXBMarshalCodegenerator();
        }else if (JAXB_UNMARSHALER.equals(type)){
            gen = new JAXBUnmarshalCodegenerator();
        }else if (JAXB_CONSTRUCTS.equals(type)){
            gen = new JAXBConstructsCodegenerator();
        }
        return gen;
    }
}
