
package converter;


/**
 * This is the business interface for Converter enterprise bean.
 */
public interface ConverterRemoteBusiness {
    java.math.BigDecimal dollarToYen(java.math.BigDecimal dollars) throws java.rmi.RemoteException;

    java.math.BigDecimal yenToEuro(java.math.BigDecimal yen) throws java.rmi.RemoteException;
    
}
