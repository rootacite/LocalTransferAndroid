package com.acite.localtransfer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

class DataHead implements java.io.Serializable {
    public byte rev = (byte)0xff;
    public byte flag;
    public long size;
}



public class Conversation {
    static protected int MODE_FILE = 0;
    static protected int MODE_MSG = 1;
    public static byte[] objectToBytes(Object obj) throws IOException
    {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream sOut = new ObjectOutputStream(out);
        ) {
            sOut.writeObject(obj);
            sOut.flush();
            byte[] bytes = out.toByteArray();
            return bytes;
        }
    }

    public static byte[]LongToByte(long num) {
        byte[] bytes = new byte[8];
        bytes[7] = (byte) ((num >> 56) & 0xff);
        bytes[6] = (byte) ((num >> 48) & 0xff);
        bytes[5] = (byte) ((num >> 40) & 0xff);
        bytes[4] = (byte) ((num >> 32) & 0xff);
        bytes[3] = (byte) ((num >> 24) & 0xff);
        bytes[2] = (byte) ((num >> 16) & 0xff);
        bytes[1] = (byte) ((num >> 8) & 0xff);
        bytes[0] = (byte) (num & 0xff);
        return bytes;
    }
    private static void TransHead(int Mode, long Size, OutputStream Stream) throws IOException {
        DataHead Head = new DataHead();
        Head.rev = (byte) 0xff;
        Head.flag = (byte) Mode;
        Head.size = Size;

        ByteBuffer buffer=ByteBuffer.allocate(10);
        buffer.put(Head.rev);
        buffer.put(Head.flag);
        buffer.put(LongToByte(Head.size));

        Stream.write(buffer.array());
    }

    public InputStream is;
    public String filename;
    public Activity Context;
    public Runnable Comp;
    Runnable Handler =new Runnable() {
        @Override
        public void run() {
            try {
                byte[] FileBuffer = new byte[4096];
                byte[] FileNameData = filename.getBytes("UTF-8");

                Socket Client=new Socket("192.168.3.138",5544);
                OutputStream os = Client.getOutputStream();

                TransHead(MODE_FILE, FileNameData.length, os);
                os.write(FileNameData);
                TransHead(MODE_FILE, is.available(), os);

                int ReadSize = 0;
                while ((ReadSize = is.read(FileBuffer)) > 0) {
                    os.write(FileBuffer, 0, ReadSize);
                }
                Context.runOnUiThread(Comp);
                Context.runOnUiThread(new Runnable()
                {
                    public void run() {
                        Toast toast = Toast.makeText(Context, "Finished transfer file:" + filename.substring(filename.lastIndexOf("/") + 1, filename.length()),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                os.close();
                is.close();
                Client.close();
            }catch (IOException ex)
            { Log.i("Err",ex.getMessage());  }
        }
    };

    public void TransFile(Uri uri, String filename, Activity Context,Runnable Completed) throws IOException
    {
        ContentResolver cr = Context.getContentResolver();
        is = cr.openInputStream(uri);
        this.filename=filename;
        this.Context=Context;
        this.Comp=Completed;

        new Thread(Handler).start();
    }

    static public void TransMsg(String Msg)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Socket Client=new Socket("192.168.3.138",5544);
                    OutputStream os = Client.getOutputStream();

                    byte[] Data = Msg.getBytes("UTF-8");
                    TransHead(MODE_MSG,Data.length,os);
                    os.write(Data);

                    os.close();
                    Client.close();
                }catch (IOException ex)
                {

                }
            }
        }).start();
    }
}
