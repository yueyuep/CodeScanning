package GraphProcess;

public class ChangedFunction {

    private String mFunctionName;
    private String mFunctionLineNumber;
    private String mFunctionParse;

    private ChangedFunction() {
    }

    public static ChangedFunction newInstance(String functionName, String functionLineNumber) {
        ChangedFunction changedFunction = new ChangedFunction();
        changedFunction.setFunctionName(functionName);
        changedFunction.setFunctionLineNumber(functionLineNumber);
        return changedFunction;
    }

    public String getFunctionName() {
        return mFunctionName;
    }

    public void setFunctionName(String functionName) {
        mFunctionName = functionName;
    }

    public String getFunctionLineNumber() {
        return mFunctionLineNumber;
    }

    public void setFunctionLineNumber(String functionLineNumber) {
        mFunctionLineNumber = functionLineNumber;
    }

    public String getFunctionParse() {
        return mFunctionParse;
    }

    public void setFunctionParse(String functionParse) {
        mFunctionParse = functionParse;
    }
}
