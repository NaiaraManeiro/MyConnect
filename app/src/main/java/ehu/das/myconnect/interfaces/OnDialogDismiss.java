package ehu.das.myconnect.interfaces;

/**
 * Interfaz para cuando se cierra el dialog
 * @param <T>
 */
public interface OnDialogDismiss<T> {
   void onDismiss(T data);
}
