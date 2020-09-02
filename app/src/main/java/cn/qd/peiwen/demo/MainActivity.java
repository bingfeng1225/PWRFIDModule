package cn.qd.peiwen.demo;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cn.haier.bio.medical.rfid.IZLG600AListener;
import cn.haier.bio.medical.rfid.ZLG600AManager;
import cn.qd.peiwen.logger.PWLogger;
import cn.qd.peiwen.pwtools.ThreadUtils;
import cn.qd.peiwen.serialport.PWSerialPort;

public class MainActivity extends AppCompatActivity implements IZLG600AListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZLG600AManager.getInstance().release();
    }

    @Override
    public void onZLG600AReady() {
        PWLogger.debug("ZLG600ASerialPort ready");
    }

    @Override
    public void onZLG600AReset() {
        PWLogger.debug("ZLG600ASerialPort reset");
        if (!"magton".equals(Build.MODEL)) {
            resetRFIDReader();
        }
    }

    @Override
    public void onZLG600AConnected() {
        PWLogger.debug("ZLG600ASerialPort connected");
    }

    @Override
    public void onZLG600ADisconnected() {
        PWLogger.debug("ZLG600ASerialPort disconnected");
    }

    @Override
    public void onZLG600APrint(String msg) {
        PWLogger.debug("" + msg);
    }

    @Override
    public void onZLG600AException(Throwable throwable) {
        PWLogger.error(throwable);
    }

    @Override
    public void onZLG600ARecognized(long id, String card) {
        PWLogger.debug("ZLG600ASerialPort recognized, id: " + id + ", card: " + card);
    }


    //智能电子读卡器模块重置
    private static final String RFID_RESET_ON = "1";
    private static final String RFID_RESET_OFF = "0";
    private static final String RFID_RESET_PATH = "/sys/kernel/finger_set/nfc_value";

    public static void resetRFIDReader() {
        PWLogger.debug("ZLG600ASerialPort OFF");
        PWSerialPort.writeFile(RFID_RESET_PATH, RFID_RESET_OFF);
        ThreadUtils.sleep(200);
        PWLogger.debug("ZLG600ASerialPort  ON");
        PWSerialPort.writeFile(RFID_RESET_PATH, RFID_RESET_ON);
        ThreadUtils.sleep(200);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                String path = "/dev/ttyS1";
                if ("magton".equals(Build.MODEL)) {
                    path = "/dev/ttyS5";
                }
                ZLG600AManager.getInstance().init(path);
                ZLG600AManager.getInstance().changeListener(this);
                break;
            case R.id.button2:
                ZLG600AManager.getInstance().enable();
                break;
            case R.id.button3:
                ZLG600AManager.getInstance().disable();
                break;
            case R.id.button4:
                ZLG600AManager.getInstance().release();
                break;
        }
    }
}
