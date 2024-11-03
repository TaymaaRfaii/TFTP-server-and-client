package bgu.spl.net.impl.Packets;

public class LOGRQ extends Packet{
    private String userName;

    public LOGRQ(String userName) {
        opcode=7;
        this.userName=userName;
    }
    public String getName() {
        return this.userName;
    }

    @Override
    public byte[] encode() {
        byte [] nameBytes = (userName+'\0').getBytes();
        byte[] bytesArr = new byte [nameBytes.length+2];
        bytesArr[0] = (byte)((opcode >> 8) & 0xFF);
        bytesArr[1] = (byte)(opcode & 0xFF);
        System.arraycopy(nameBytes, 0, bytesArr, 2, bytesArr.length - 2);
        return bytesArr;
    }
}
