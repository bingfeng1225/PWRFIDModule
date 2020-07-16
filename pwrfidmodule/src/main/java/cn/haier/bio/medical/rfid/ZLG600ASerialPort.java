package cn.haier.bio.medical.rfid;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteOrder;

import cn.qd.peiwen.serialport.PWSerialPortHelper;
import cn.qd.peiwen.serialport.PWSerialPortListener;
import cn.qd.peiwen.serialport.PWSerialPortState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ZLG600ASerialPort implements PWSerialPortListener {
    private ByteBuf buffer;
    private RFIDHandler handler;
    private HandlerThread thread;
    private PWSerialPortHelper helper;

    private int times = 0;
    private boolean ready = false;
    private boolean enabled = false;
    private WeakReference<IZLG600AListener> listener;

    public ZLG600ASerialPort() {

    }

    public void init(String path) {
        this.createHandler();
        this.createHelper(path);
        this.createBuffer();
    }

    public void enable() {
        if (this.isInitialized() && !this.enabled) {
            this.enabled = true;
            this.helper.open();
        }
    }

    public void disable() {
        if (this.isInitialized() && this.enabled) {
            this.enabled = false;
            this.helper.close();
        }
    }

    public void release() {
        this.listener = null;
        this.destoryHandler();
        this.destoryHelper();
        this.destoryBuffer();
    }

    public void changeListener(IZLG600AListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    private boolean isInitialized() {
        if (this.handler == null) {
            return false;
        }
        if (this.helper == null) {
            return false;
        }
        if (this.buffer == null) {
            return false;
        }
        return true;
    }

    private void createHelper(String path) {
        if (this.helper == null) {
            this.helper = new PWSerialPortHelper("ZLG600ASerialPort");
            this.helper.setTimeout(2);
            this.helper.setPath(path);
            this.helper.setBaudrate(19200);
            this.helper.init(this);
        }
    }

    private void destoryHelper() {
        if (null != this.helper) {
            this.helper.release();
            this.helper = null;
        }
    }

    private void createHandler() {
        if (this.thread == null && this.handler == null) {
            this.thread = new HandlerThread("ZLG600ASerialPort");
            this.thread.start();
            this.handler = new RFIDHandler(this.thread.getLooper());
        }
    }

    private void destoryHandler() {
        if (null != this.thread) {
            this.thread.quitSafely();
            this.thread = null;
            this.handler = null;
        }
    }

    private void createBuffer() {
        if (this.buffer == null) {
            this.buffer = Unpooled.buffer(4);
        }
    }

    private void destoryBuffer() {
        if (null != this.buffer) {
            this.buffer.release();
            this.buffer = null;
        }
    }

    private void write(int type) {
        byte[] data = ZLG600ATools.packageCommand(type);
        if (!this.isInitialized() || !this.enabled) {
            return;
        }
        this.helper.write(data);
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onZLG600APrint("ZLG600ASerialPort Send:" + ZLG600ATools.bytes2HexString(data, true, ", "));
        }
    }

    @Override
    public void onConnected(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.times = 0;
        this.ready = false;
        this.buffer.clear();
        this.handler.sendEmptyMessage(0);
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onZLG600AConnected();
        }
    }

    @Override
    public void onReadThreadReleased(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onZLG600APrint("ZLG600ASerialPort read thread released");
        }
    }

    @Override
    public void onException(PWSerialPortHelper helper, Throwable throwable) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.ready = false;
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onZLG600AException(throwable);
        }
        if(this.enabled){
            if (null != this.listener && null != this.listener.get()) {
                this.listener.get().onZLG600AReset();
            }
        }
    }

    @Override
    public void onStateChanged(PWSerialPortHelper helper, PWSerialPortState state) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onZLG600APrint("ZLG600ASerialPort state changed: " + state.name());
        }
    }

    @Override
    public void onByteReceived(PWSerialPortHelper helper, byte[] buffer, int length) throws IOException {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.buffer.writeBytes(buffer, 0, length);
        if (!this.ready) {
            this.buffer.markReaderIndex();
            byte mark = this.buffer.readByte();
            if (mark == 0x06) {
                this.ready = true;
                if (this.buffer.readableBytes() == 0) {
                    this.buffer.discardReadBytes();
                } else {
                    this.buffer.resetReaderIndex();
                }
                if (null != this.listener && null != this.listener.get()) {
                    this.listener.get().onZLG600AReady();
                }
            }
        }
        if (this.buffer.readableBytes() > 0) {
            int len = this.buffer.getByte(0);
            if (this.buffer.readableBytes() < len) {
                return;
            }
            this.buffer.markReaderIndex();
            byte[] data = new byte[len];
            this.buffer.readBytes(data, 0, len);
            this.buffer.resetReaderIndex();
            if (!ZLG600ATools.checkFrame(data)) {
                return;
            }
            if (null != this.listener && null != this.listener.get()) {
                this.listener.get().onZLG600APrint("ZLG600ASerialPort Recv:" + ZLG600ATools.bytes2HexString(data, true, ", "));
            }
            this.parseRFIDPackage();
            this.postRecognize();
        }
    }

    private void postRecognize() {
        if(null != this.handler) {
            this.handler.removeMessages(1);
            this.handler.sendEmptyMessageDelayed(1, 1000);
        }
    }

    private void parseRFIDPackage() {
        int total = this.buffer.readByte();
        byte type = this.buffer.readByte();
        if (type != 0x06) {
            this.buffer.skipBytes(total - 2);
            this.buffer.discardReadBytes();
            return;
        }
        byte command = this.buffer.readByte();
        if (command != 0x00) {
            this.buffer.skipBytes(total - 3);
            this.buffer.discardReadBytes();
            return;
        }
        int len = this.buffer.readByte();
        if (len + 6 != total) {
            this.buffer.skipBytes(total - 4);
            this.buffer.discardReadBytes();
            return;
        }
        this.buffer.skipBytes(3);
        byte length = this.buffer.readByte();
        if (length + 4 != len) {
            this.buffer.skipBytes(total - 8);
            this.buffer.discardReadBytes();
            return;
        }
        byte[] data = new byte[8];
        this.buffer.readBytes(data, 0, length > 8 ? 8 : length);

        long id = ZLG600ATools.bytes2Long(data, ByteOrder.LITTLE_ENDIAN);
        String card = ZLG600ATools.bytes2HexString(data, 0, length);
        this.buffer.skipBytes(2);
        this.buffer.discardReadBytes();
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onZLG600ARecognized(id, card);
        }
    }

    private class RFIDHandler extends Handler {
        public RFIDHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    times++;
                    if (times < 2) {
                        sendEmptyMessageDelayed(0, 1);
                    } else {
                        ZLG600ASerialPort.this.postRecognize();
                    }
                    write(ZLG600ATools.RFID_COMMAND_UART);
                    break;
                case 1:
                    write(ZLG600ATools.RFID_COMMAND_READ);
                    break;
            }
        }
    }
}