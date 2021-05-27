package Spark;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/16 11:08
 */

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-16 11:08
 **/
public class LtpcData {
    private int header=0x1eadc0de;
    private short targetBoardAddress;
    private short packageLength;
    private short sourceBoardAddress;
    private byte PackageNumber;
    private byte PackageType;
    private byte channelId;
    private byte sampleLength;
    private byte sampleId;
    private int triggerSource;
    private short trigger;
    private long triggerTimestamp;
    private short[] points;

//    public static byte[] creatByteData(){
//
//    }
}
