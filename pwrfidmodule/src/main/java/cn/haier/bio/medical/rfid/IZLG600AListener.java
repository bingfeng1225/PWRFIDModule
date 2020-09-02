package cn.haier.bio.medical.rfid;

public interface IZLG600AListener {
    void onZLG600AReady();
    void onZLG600AReset();
    void onZLG600AConnected();
    void onZLG600ADisconnected();
    void onZLG600APrint(String msg);
    void onZLG600AException(Throwable throwable);
    void onZLG600ARecognized(long id, String card);
}
