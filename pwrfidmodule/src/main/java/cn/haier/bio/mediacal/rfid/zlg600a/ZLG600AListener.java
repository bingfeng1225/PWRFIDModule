package cn.haier.bio.mediacal.rfid.zlg600a;

public interface ZLG600AListener {
    void onZLG600AReady();
    void onZLG600AConnected();
    void onZLG600AException();
    void onZLG600ARecognized(long id, String card);
}
