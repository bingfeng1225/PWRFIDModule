package cn.haier.bio.medical.rfid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ZLG600ATools {
    public static final int RFID_COMMAND_UART = 0;
    public static final int RFID_COMMAND_READ = 1;

    public static boolean checkFrame(byte[] data) {
        if (data[0] < 0x06) {
            return false;
        }
        if ((data[data.length - 1] & 0xff) != 0x03) {
            return false;
        }
        byte check = ZLG600ATools.computeXORInverse(data, 0, data.length - 2);
        return (check == data[data.length - 2]);
    }

    public static byte[] packageCommand(int type) {
        switch (type) {
            case RFID_COMMAND_READ:
                return new byte[]{
                        0x08, 0x06, (byte) 0x4D, 0x02,
                        0x00, 0x52, (byte) 0xEC, 0x03
                };
            default:
                return new byte[]{0x20};
        }
    }

    public static byte computeXORCode(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        return computeXORCode(data, 0, data.length);
    }

    public static byte computeXORCode(byte[] data, int offset, int len) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        if (offset < 0 || offset > data.length - 1) {
            throw new IllegalArgumentException("The offset index out of bounds");
        }
        if (len < 0 || offset + len > data.length) {
            throw new IllegalArgumentException("The len can not be < 0 or (offset + len) index out of bounds");
        }
        byte temp = data[offset];
        for (int i = offset + 1; i < offset + len; i++) {
            temp ^= data[i];
        }
        return temp;
    }

    public static byte computeXORInverse(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        return computeXORInverse(data, 0, data.length);
    }

    public static byte computeXORInverse(byte[] data, int offset, int len) {
        byte xor = computeXORCode(data, offset, len);
        return (byte) (~xor);
    }

    public static long bytes2Long(byte[] bytes, ByteOrder order) {
        return bytes2Long(bytes, 0, order);
    }

    public static long bytes2Long(byte[] bytes, int offset, ByteOrder order) {
        if (offset < 0 || offset > bytes.length - 1) {
            throw new IllegalArgumentException("The offset index out of bounds");
        }
        if (bytes.length - offset < 8) {
            throw new IllegalArgumentException("The bytes (legth - offset) < long bytes(8)");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, 8);
        if (order == ByteOrder.LITTLE_ENDIAN) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer.getLong();
    }

    public static String bytes2HexString(byte[] data) {
        return bytes2HexString(data, false);
    }

    public static String bytes2HexString(byte[] data, boolean hexFlag) {
        return bytes2HexString(data, hexFlag, null);
    }

    public static String bytes2HexString(byte[] data, boolean hexFlag, String separator) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        return bytes2HexString(data, 0, data.length, hexFlag, separator);
    }

    public static String bytes2HexString(byte[] data, int offset, int len) {
        return bytes2HexString(data, offset, len, false);
    }

    public static String bytes2HexString(byte[] data, int offset, int len, boolean hexFlag) {
        return bytes2HexString(data, offset, len, hexFlag, null);
    }

    public static String bytes2HexString(byte[] data, int offset, int len, boolean hexFlag, String separator) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        if (offset < 0 || offset > data.length - 1) {
            throw new IllegalArgumentException("The offset index out of bounds");
        }
        if (len < 0 || offset + len > data.length) {
            throw new IllegalArgumentException("The len can not be < 0 or (offset + len) index out of bounds");
        }
        String format = "%02X";
        if (hexFlag) {
            format = "0x%02X";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = offset; i < offset + len; i++) {
            buffer.append(String.format(format, data[i]));
            if (separator == null) {
                continue;
            }
            if (i != (offset + len - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }
}
