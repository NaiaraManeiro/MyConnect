package ehu.das.myconnect.interfaces;

/**
 * Interfaz para gestionar los botones del dialogo
 * @param <T>
 */
public interface OnDialogOptionPressed<T>{
    public abstract void onYesPressed(T data1, T data2);
    public abstract void onNoPressed(T data);
    void notifyError(T string);
}
