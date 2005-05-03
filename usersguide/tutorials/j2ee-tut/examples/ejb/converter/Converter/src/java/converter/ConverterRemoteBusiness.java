
package converter;

import java.math.BigDecimal;


/**
 * This is the business interface for Converter enterprise bean.
 */
public interface ConverterRemoteBusiness {
    BigDecimal yenToEuro(java.math.BigDecimal yen) throws java.rmi.RemoteException;

    BigDecimal dollarToYen(java.math.BigDecimal dollars) throws java.rmi.RemoteException;
    
}
