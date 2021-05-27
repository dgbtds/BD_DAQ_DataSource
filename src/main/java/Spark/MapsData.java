package Spark;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/10/30 20:43
 */

import Util.IntByteConvert;
import com.sun.crypto.provider.PBEWithMD5AndDESCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCounted;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import static Util.IntByteConvert.byteArrayToInt;

/**
 * @program: BG_DAQ_FrameWork
 * @description:
 * @author: WuYe
 * @create: 2020-10-30 20:43
 **/
public class MapsData implements Serializable {
    private String type;
    private int ladder;
    private int trigger;
    private int pckLength;
    private static int nextPckPid = 1;
    private static int maxPidNUm = 1024;
    private int pid;
    private byte[] bytedata;
    public static int basePckLength;

    public static void main(String[] args) {
//        byte[] bytes = creatByteData(1, 0, 252, 1024);
//        byteTriggerUp(bytes, 1200, 1024);
        ArrayList<ByteBuf> bytes = writeDirectCompositeBuffer(0, 10);
        System.out.println("aa");
        bytes.forEach(ReferenceCounted::release);
    }

    public static ArrayList<byte[]> creatVariableLengthPackages(int ladder, int trigger, int basePckLength) {
        ArrayList<byte[]> pckList = new ArrayList<>(100);
        for (int j = 0; j < 100; j++) {
            int energy = new Random().nextInt(100);
            //int length = new Random().nextInt(64) * 8 + 512;
            int length = new Random().nextInt(50) * 4 + basePckLength;
            byte[] pck = new byte[length];
            IntByteConvert.intToByteArray(pck, 0, ladder);
            IntByteConvert.intToByteArray(pck, 4, length);
            IntByteConvert.intToByteArray(pck, 8, trigger);
            for (int i = 0; i < length / 4 - 4; i++) {
                IntByteConvert.intToByteArray(pck, 12 + 4 * i, energy);
            }
            IntByteConvert.intToByteArray(pck, length - 4, 0x22334455);
            pckList.add(pck);
        }
        return pckList;
    }
    public static ArrayList<ByteBuf> writeDirectCompositeBuffer(int ladder, int trigger) {
        ArrayList<ByteBuf> pckList = new ArrayList<>(100);
        for (int j = 0; j < 100; j++) {
            int energy = new Random().nextInt(100);
            //int length =2048;
            int length = new Random().nextInt(50) * 4 + basePckLength;
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(length);
            byteBuf.writeInt(0x44556677);
            byteBuf.writeInt(length);
            byteBuf.writeInt(trigger);
            for (int i = 0; i < length / 4 - 4; i++) {
                byteBuf.writeInt(energy);
            }
            byteBuf.writeInt(0x22334455);
            pckList.add(byteBuf);
        }
        return pckList;
    }

    public static ByteBuf creatNullPck(int ladder, int trigger) {
        int length = 16;
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeInt(ladder);
        byteBuf.writeInt(length);
        byteBuf.writeInt(trigger);
        byteBuf.writeInt(0x22334455);
        return byteBuf;
    }
    public static ByteBuf creatLengthFixedPck(int ladder, int trigger) {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(basePckLength);
        byteBuf.writeInt(ladder);
        byteBuf.writeInt(basePckLength);
        byteBuf.writeInt(trigger);
        byteBuf.writerIndex(basePckLength-4);
        byteBuf.writeInt(0x22334455);
        return byteBuf;
    }


    public static byte[] creatByteData(int ladder, int trigger, int pointNum, int pcklengthK) {
        int energy = new Random().nextInt(100);
        byte[] pck = new byte[(12 + pointNum * 4 + 4) * pcklengthK];
        IntByteConvert.intToByteArray(pck, 0, ladder);
        IntByteConvert.intToByteArray(pck, 4, trigger);
        IntByteConvert.intToByteArray(pck, 8, nextPckPid);
        for (int i = 0; i < pointNum + 1; i++) {
            IntByteConvert.intToByteArray(pck, 12 + 4 * i, energy);
        }
        IntByteConvert.intToByteArray(pck, 12 + pointNum * 4, 0x22334455);
        for (int i = 1; i < pcklengthK; i++) {
            nextPckPid++;
            if (nextPckPid > maxPidNUm) {
                nextPckPid = 1;
            }
            System.arraycopy(pck, 0, pck, (12 + pointNum * 4 + 4) * i, 12 + pointNum * 4 + 4);
            IntByteConvert.intToByteArray(pck, 8 + (12 + pointNum * 4 + 4) * i, nextPckPid);
        }
        return pck;
    }

    public static byte[] creatByteData1(int ladder, int trigger, int pointNum, int pcklengthK) {
        int energy = new Random().nextInt(100);
        byte[] pck = new byte[(12 + pointNum * 4 + 4) * pcklengthK];
        IntByteConvert.intToByteArray(pck, 0, ladder);
        IntByteConvert.intToByteArray(pck, 4, trigger);
        IntByteConvert.intToByteArray(pck, 8, nextPckPid);
        for (int i = 0; i < pointNum + 1; i++) {
            IntByteConvert.intToByteArray(pck, 12 + 4 * i, energy);
        }
        IntByteConvert.intToByteArray(pck, 12 + pointNum * 4, 0x22334455);
        for (int i = 1; i < pcklengthK; i++) {
            System.arraycopy(pck, 0, pck, (12 + pointNum * 4 + 4) * i, 12 + pointNum * 4 + 4);
        }
        return pck;
    }

    public static void byteTriggerUp(byte[] bytes, int pcklengthK, int datalength) {
        int trigger = byteArrayToInt(bytes, 4);
        for (int i = 0; i < pcklengthK; i++) {
            IntByteConvert.intToByteArray(bytes, 4 + datalength * i, trigger + 1);
        }
    }

    public MapsData(int pckLength, int pckLength1, byte[] pck) {
        this.pckLength = pckLength1;
        if (pckLength <= 12 || pckLength % 4 != 0) {
            this.type = "error";
            this.ladder = -1;
            this.trigger = -1;
            this.pid = -1;
            this.bytedata = pck;
        } else {
            this.type = "sample";
            this.ladder = byteArrayToInt(pck, 0);
            this.trigger = byteArrayToInt(pck, 4);
            this.pid = byteArrayToInt(pck, 8);
            this.bytedata = pck;
        }
    }

    @Override
    public String toString() {
        return "MapsData{" +
                "type='" + type + '\'' +
                ", ladder=" + ladder +
                ", trigger=" + trigger +
                //", point=" + Arrays.toString(point) +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLadder() {
        return ladder;
    }

    public void setLadder(int ladder) {
        this.ladder = ladder;
    }

    public int getTrigger() {
        return trigger;
    }

    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public byte[] getBytedata() {
        return bytedata;
    }

    public void setBytedata(byte[] bytedata) {
        this.bytedata = bytedata;
    }
}
