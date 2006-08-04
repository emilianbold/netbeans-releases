# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.classdata.*;
# import java.io.*;
# import java.util.*;
#
# ProtocolSupport support = new ProtocolSupport(data, this, true);
# String nameSuffix = "Test" + (data.isMIDlet() ? "" : "MIDlet");
# String outputDir = data.getClientOutputDirectory();
# setOut(support.getClientPath(data.getClientClassName() + nameSuffix));
# getOutput().addCreatedFile(support.getClientPath(data.getClientClassName() + nameSuffix));
${support.clientPackageLine()}
import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;

# String testClassName = data.getClientClassName() + nameSuffix;
# String extendsString = data.isMIDlet() ? "" : "extends MIDlet ";

public class ${testClassName} ${extendsString} {
#if(!data.isMIDlet()) {

    public void startApp() {
    }

    public void destroyApp(boolean b) {
    }

    public void pauseApp() {
    }
#}

# // generate constants for typesS
# ClassData[] types = data.getSupportedTypes();
# for (int i = 0; i < types.length; i++) {
#   String constantName = data.getConstantNameForType(types[i]);
#   short constantValue = data.getValueForType(types[i]);
public final static Short ${constantName} = new Short( (short) ${constantValue} );
#}

    public Displayable getTestDisplayable() {
        //List list = new List("Remote Methods");
    }

}