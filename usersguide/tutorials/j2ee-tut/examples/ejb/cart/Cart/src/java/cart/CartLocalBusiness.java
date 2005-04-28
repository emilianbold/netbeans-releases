
package cart;

import exception.BookException;
import java.util.Vector;


/**
 * This is the business interface for Cart enterprise bean.
 */
public interface CartLocalBusiness {
    void addBook(java.lang.String title);

    void removeBook(java.lang.String title) throws BookException;

    Vector getContents();
    
}
