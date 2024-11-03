package bgu.spl.net.impl.Packets;

public class BCAST extends Packet {
    private byte deleteOrAdd;
    private String fileName;

    public BCAST(byte deleteOrAdd, String fileName) {
        opcode =9;
        this.deleteOrAdd=deleteOrAdd;
        this.fileName=fileName;
    }

    @Override
    public byte[] encode() {
        byte [] fileBytes = (fileName+'\0').getBytes();
        byte[] Bytes = new byte [fileBytes.length+3];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        Bytes[2]= deleteOrAdd;
        System.arraycopy(fileBytes, 0, Bytes, 3, Bytes.length - 3);
        return Bytes;
    }
    public  byte getDelOrAdd(){
        return deleteOrAdd;
    }
    public  String getFileName(){
        return fileName;
    }
}