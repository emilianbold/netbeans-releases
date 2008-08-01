package pkg.subpkg;
public class B {
    public int otherval = 12;
    static {
        System.err.println("Should never be loaded");
        Thread.dumpStack();
    }
}
