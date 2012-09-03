package renameClass;

public class RenameProperty {

    private String abc;
    
    public String test() {
        String local = getAbc();
        System.out.println(local);
        setAbc("Hello World!");
        System.out.println(getAbc());
        return abc;
    }

    public String getAbc() {
        return abc;
    }

    public void setAbc(String abc) {
        this.abc = abc;
    }
}
