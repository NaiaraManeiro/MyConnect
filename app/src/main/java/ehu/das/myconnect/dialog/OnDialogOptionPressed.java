package ehu.das.myconnect.dialog;

public interface OnDialogOptionPressed<T>{

    public abstract void onYesPressed(T data1, T data2);
    public abstract void onNoPressed(T data);
    void notifyError(T string);
}
