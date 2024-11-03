package bgu.spl.net.impl.Packets;

public class ERROR extends Packet{
    private short errorCode;
    private String errMsg;
    public ERROR(short errorCode, String errMsg) {
        opcode =5;
        this.errorCode=errorCode;
        this.errMsg=errMsg;
    }
    public short getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }
    public String getErrMsg() {
        return errMsg;
    }
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    @Override
    public byte[] encode() {
        byte [] errorBytes = (errMsg+'\0').getBytes();
        byte[] Bytes = new byte [errorBytes.length+4];
        Bytes[0] = (byte)((opcode >> 8) & 0xFF);
        Bytes[1] = (byte)(opcode & 0xFF);
        Bytes[2] = (byte)((errorCode >> 8) & 0xFF);
        Bytes[3] = (byte)(errorCode & 0xFF);
        System.arraycopy(errorBytes, 0, Bytes, 4, Bytes.length - 4);
        return Bytes;
    }
}
