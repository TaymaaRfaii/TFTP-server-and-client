package bgu.spl.net.impl.Packets;
public class WRQ extends Packet {
    private String fileName;

    public WRQ(String fileName) {
        opcode =2;
        this.fileName=fileName;
    }
    public String getFileName(){
        return fileName;
    }
    @Override
    public byte[] encode() {
        byte [] fileBytes = (fileName+'\0').getBytes();
        byte[] bytesArr = new byte [fileBytes.length+2];
        bytesArr[0] = (byte)((opcode >> 8) & 0xFF);
        bytesArr[1] = (byte)(opcode & 0xFF);
        System.arraycopy(fileBytes, 0, bytesArr, 2, bytesArr.length - 2);
        return bytesArr;
    }
}
