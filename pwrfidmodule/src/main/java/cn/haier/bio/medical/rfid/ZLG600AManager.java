package cn.haier.bio.medical.rfid;

/***
 * ZLG600A
 * 集成电路卡读写器
 */
public class ZLG600AManager {
    private ZLG600ASerialPort serialPort;
    private static ZLG600AManager manager;

    public static ZLG600AManager getInstance() {
        if (manager == null) {
            synchronized (ZLG600AManager.class) {
                if (manager == null)
                    manager = new ZLG600AManager();
            }
        }
        return manager;
    }

    private ZLG600AManager() {

    }

    public void init(String path) {
        if (this.serialPort == null) {
            this.serialPort = new ZLG600ASerialPort();
            this.serialPort.init(path);
        }
    }

    public void enable() {
        if (null != this.serialPort) {
            this.serialPort.enable();
        }
    }

    public void disable() {
        if (null != this.serialPort) {
            this.serialPort.disable();
        }
    }

    public void release() {
        if (null != this.serialPort) {
            this.serialPort.release();
            this.serialPort = null;
        }
    }

    public void changeListener(IZLG600AListener listener) {
        if (null != this.serialPort) {
            this.serialPort.changeListener(listener);
        }
    }
}

