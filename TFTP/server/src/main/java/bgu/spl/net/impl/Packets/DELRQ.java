package bgu.spl.net.impl.Packets;

public class DELRQ extends Packet{
    private String fileName;

    public DELRQ(String fileName) {
        opcode =8;
        this.fileName=fileName;
    }

    public String getFileName(){
        return fileName;
    }
    @Override
    public byte[] encode() {
        byte [] fileBytes = (fileName+'\0').getBytes();
        byte[] Bytes = new byte [fileBytes.length+2];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        System.arraycopy(fileBytes, 0, Bytes, 2, Bytes.length - 2);
        return Bytes;
    }
}

