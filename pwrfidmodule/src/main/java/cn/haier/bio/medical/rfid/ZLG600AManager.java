package cn.haier.bio.medical.rfid;

import cn.qd.peiwen.pwtools.EmptyUtils;

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

    private ZLG600AManager(){

    }

    public void init(String path) {
        if(EmptyUtils.isEmpty(this.serialPort)){
            this.serialPort = new ZLG600ASerialPort();
            this.serialPort.init(path);
        }
    }

    public void enable() {
        if(EmptyUtils.isNotEmpty(this.serialPort)){
            this.serialPort.enable();
        }
    }

    public void disable() {
        if(EmptyUtils.isNotEmpty(this.serialPort)){
            this.serialPort.disable();
        }
    }

    public void release() {
        if(EmptyUtils.isNotEmpty(this.serialPort)){
            this.serialPort.release();
            this.serialPort = null;
        }
    }

    public void changeListener(IZLG600AListener listener) {
        if(EmptyUtils.isNotEmpty(this.serialPort)){
            this.serialPort.changeListener(listener);
        }
    }
}

