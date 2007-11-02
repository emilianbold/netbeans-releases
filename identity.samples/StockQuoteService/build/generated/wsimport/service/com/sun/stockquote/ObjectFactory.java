
package com.sun.stockquote;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.stockquote package. 
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

    private final static QName _QuoteRequest_QNAME = new QName("http://sun.com/stockquote.xsd", "QuoteRequest");
    private final static QName _QuoteResponse_QNAME = new QName("http://sun.com/stockquote.xsd", "QuoteResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.stockquote
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link QuoteRequestType }
     * 
     */
    public QuoteRequestType createQuoteRequestType() {
        return new QuoteRequestType();
    }

    /**
     * Create an instance of {@link PriceType }
     * 
     */
    public PriceType createPriceType() {
        return new PriceType();
    }

    /**
     * Create an instance of {@link QuoteResponseType }
     * 
     */
    public QuoteResponseType createQuoteResponseType() {
        return new QuoteResponseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QuoteRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://sun.com/stockquote.xsd", name = "QuoteRequest")
    public JAXBElement<QuoteRequestType> createQuoteRequest(QuoteRequestType value) {
        return new JAXBElement<QuoteRequestType>(_QuoteRequest_QNAME, QuoteRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QuoteResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://sun.com/stockquote.xsd", name = "QuoteResponse")
    public JAXBElement<QuoteResponseType> createQuoteResponse(QuoteResponseType value) {
        return new JAXBElement<QuoteResponseType>(_QuoteResponse_QNAME, QuoteResponseType.class, null, value);
    }

}
