/**
 * Created by vidhi on 4/8/14.
 */
public class DisplayTestInfo {

    String fileName;
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void beforeTest (String functionName) throws Exception {
        System.out.println ("\n\n------------------\n Function Details \n------------------");
        System.out.println ("Function Name: "+ functionName);
        System.out.println ("Source file name: " + fileName + ".java");
        System.out.println ("Testing started...");

    }

    public void showException (String functionName, Exception e) {
        System.out.println ("\n------------------\n Exception Details \n------------------");
        System.out.println ("Function Name: "+ functionName);
        System.out.println ("Source file name: " + fileName + ".java");
        System.out.println ("Exception: \n"+e.getMessage ());
    }

}
