package cn.haier.bio.medical.rfid.tools;

import cn.qd.peiwen.pwlogger.PWLogger;
import cn.qd.peiwen.pwtools.ByteUtils;
import cn.qd.peiwen.pwtools.ThreadUtils;
import cn.qd.peiwen.serialport.PWSerialPort;


public class ZLG600ATools {
    public static final int RFID_COMMAND_UART = 0;
    public static final int RFID_COMMAND_READ = 1;
    public static final int RFID_COMMAND_RESET = 2;

    public static boolean checkFrame(byte[] data) {
        if (data[0] < 0x06) {
            return false;
        }
        if ((data[data.length - 1] & 0xff) != 0x03) {
            return false;
        }
        byte check = ByteUtils.computeXORInverse(data, 0, data.length - 2);
        return (check == data[data.length - 2]);
    }

    public static byte[] packageCommand(int type) {
        switch (type) {
            case RFID_COMMAND_READ:
                return new byte[]{
                        0x08, 0x06, (byte) 0x4D, 0x02,
                        0x00, 0x52, (byte) 0xEC, 0x03
                };
            case RFID_COMMAND_RESET:
                return new byte[]{
                        0x06, 0x05, 0x45, 0x00, (byte) 0xB9, 0x03
                };
            default:
                return new byte[]{0x20};
        }
    }
}
