package test.designe.app.terminalapp.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import test.designe.app.terminalapp.sigeltons.TerminalSingelton;

public class ReaderNFC implements NfcAdapter.ReaderCallback {
    static byte[] aidUser = {(byte) 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
    static byte[] aidBytesController = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x11, 0x22, 0x33};
    static byte[] selectCommandUser = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07, (byte) 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00};
    static byte[] commandApduGetId = {
            (byte) 0x00,  // CLA
            (byte) 0xB0,  // INS (Read Binary)
            (byte) 0x00,  // P1 (Offset High Byte)
            (byte) 0x00,  // P2 (Offset Low Byte)
            (byte) 0x04};  // Le (Expected Length of Response Data)


    private static byte[] selectCommandController = new byte[]{
            (byte) 0x00, // CLA (Class)
            (byte) 0xA4, // INS (Instruction) for SELECT
            0x04,        // P1 (Parameter 1) - Select by DF Name
            0x00,        // P2 (Parameter 2)
            0x07, // LC (Length of data)
            // AID bytes
            (byte) 0xA0, 0x00, 0x00, 0x03, 0x11, 0x22, 0x33,
            0x00         // LE (Expected response length)
    };

   private static byte[] write4BytesCommand = {(byte) 0x00, // CLA (Class)
            (byte) 0xD6, // INS (Instruction) for WRITE BINARY
            0x00,        // P1 (Parameter 1)
            0x00,        // P2 (Parameter 2)
            0x04};

    private NfcAdapter nfcAdapter;
    HandlerThread handlerThread;
    Activity parentActivity;

    HashSet<IdReadSubscriber> idReadSubscribers;

    public ReaderNFC(HandlerThread handlerThread, Activity activity) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        this.parentActivity = activity;
        this.handlerThread = handlerThread;
        idReadSubscribers = new HashSet<IdReadSubscriber>(3);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Handler handler = new Handler(handlerThread.getLooper());
        //READY
        handler.post(() -> {
            //Pronadjen tag
            IsoDep isoDep = IsoDep.get(tag);
            if (!isoDep.isConnected()) {
                try {
                    isoDep.connect();
                    //Konekcija uspostavljena
                } catch (IOException e) {

                    //Konekcija nije uspostavljena
                }
            }
            if (!isoDep.isConnected())
                return;
            isoDep.setTimeout(350);
            {
                byte[] res = new byte[0];
                try {


                    res = isoDep.transceive(selectCommandUser);

                    if (res[0] == -112 && res[1] == 0) {
                        res = isoDep.transceive(commandApduGetId);
                        if(res.length>=10) {
                            String userString = new String(res, StandardCharsets.US_ASCII);
                            isoDep.close();
                            synchronized (idReadSubscribers) {
                                idReadSubscribers.stream().parallel().forEach(sub -> sub.onUserStringRead(userString));
                            }
                        }

                    } else {

                        res = isoDep.transceive(selectCommandController);
                        if (res[0] == -112 && res[1] == 0) {


                            ByteBuffer send = ByteBuffer.allocate(write4BytesCommand.length+4);
                            send.put(write4BytesCommand);
                            send.putInt(TerminalSingelton.getTerminal().getId());
                            byte [] temp = send.array();
                            res = isoDep.transceive(send.array());
                            if(res[0] == -112 && res[1] == 0)
                                isoDep.close();
                        }
                    }

                } catch (IOException e) {
                    //KONEKCIJA IZGUBLJENA
                }
            }
        });


    }

    public void enableReaderMode() {
        if (nfcAdapter != null)


            nfcAdapter.enableReaderMode(this.parentActivity,
                    this, NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);


    }

    public void disableReaderMode() {
        if (nfcAdapter != null)
            nfcAdapter.disableReaderMode(this.parentActivity);


    }

    public void subscribeToIdRead(IdReadSubscriber sub) {
        synchronized (this.idReadSubscribers) {
            this.idReadSubscribers.add(sub);
        }
    }

    public void unsubscribeToIdRead(IdReadSubscriber sub) {
        synchronized (this.idReadSubscribers) {
            this.idReadSubscribers.remove(sub);
        }
    }


}
