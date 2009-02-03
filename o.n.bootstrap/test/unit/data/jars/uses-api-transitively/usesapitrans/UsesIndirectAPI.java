package usesapitrans;
import usesapi.*;
import org.netbeans.api.foo.*;
public class UsesIndirectAPI {
    public UsesIndirectAPI() {
        new UsesPublicClass();
        new PublicClass();
    }
}

