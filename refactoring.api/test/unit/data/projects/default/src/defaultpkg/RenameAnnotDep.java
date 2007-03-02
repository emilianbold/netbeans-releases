package defaultpkg;

/**
 *
 */
@RenameAnnot(text="",texts={"aa"},number=1)
public class RenameAnnotDep {
    
    @RenameAnnot(text="",texts={"bb"},number=2)
    int field;
    
    @RenameAnnot(text="",texts={"cc"},number=3)
    public RenameAnnotDep() {
    }
    
    @RenameAnnot(text="",texts={"dd"},number=4)
    class Inner {
        
    }
}
