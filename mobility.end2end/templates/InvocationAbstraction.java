# import org.netbeans.mobility.end2end.core.model.*;
# ProtocolSupport support = new ProtocolSupport(data, this, false);
# setOut(support.getServletSupportPath("InvocationAbstraction"));
# getOutput().addCreatedFile(support.getServletSupportPath("InvocationAbstraction"));
package ${support.serverSupportPackage()};

import java.io.DataInput;
import javax.servlet.http.HttpSession;

/**
 *  This interface is used to abstract the servlet from the functionality be it
 *  an EJB or a plain Java class or a Web service.
 */
public interface InvocationAbstraction {
        /**
         *  This method performs the actual invocation of server functionality. It is
         *  used by the servlet to delegate functionality to external classes.
         *
         *@param  session          this http session
         *@param  input            The stream from which we should read the parameters
         *      for the methods
         *@return                  The return value for the method NULL IS NOT
         *      SUPPORTED!!!!
         *@exception  IOException  Thrown when a protocol error occurs
         */
        public Object invoke(HttpSession session,
            DataInput input) throws Exception;
}

