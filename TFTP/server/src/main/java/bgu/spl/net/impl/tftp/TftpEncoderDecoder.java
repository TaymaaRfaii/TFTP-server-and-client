package bgu.spl.net.impl.tftp;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.Packets.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;


public class TftpEncoderDecoder implements MessageEncoderDecoder<Packet> {
     private short opcode = 0;
	private Vector<Byte> bytes = new Vector<Byte>();
	private byte[] opCodeByte = new byte[2];
	private byte[] bytesArr = null; //the array that will contain the elements from the bytes vector
	private boolean opCodeReceived = false;
	private short packetSize = 0;
    private byte[] packetSizeArr;
	private byte[] blockNumArr = new byte[2];

        //TODO: Implement here the TFTP encoder and decoder
    @Override
    public byte[] encode(Packet message) {
        return message.encode();
    }
    @Override
    public Packet decodeNextByte(byte nextByte) {
        // TODO: implement this
        bytes.add(nextByte);
		if (bytes.size() == 1)
			opCodeByte[0] = nextByte;
		if (bytes.size() == 2){
			opCodeByte[1] = nextByte;
			opcode = bytesToShort(opCodeByte);
			opCodeReceived = true;
		}
		if (opCodeReceived){
        switch (opcode) {
            case 1: // RRQ
                if (nextByte == '\0'){
                    bytesArr = new byte[bytes.size()];
                    bytesArr = vectorToArray(bytes);
                    String fileName = new String(bytesArr, 2, bytesArr.length - 3, StandardCharsets.UTF_8); 
                    bytes = new Vector<Byte>();
                    opCodeReceived = false;
                    return new RRQ(fileName);
                }
            break;
            case 2: //WRQ
                if (nextByte == '\0'){
                    bytesArr = new byte[bytes.size()];
                    bytesArr = vectorToArray(bytes);
                    String fileName = new String(bytesArr, 2, bytesArr.length - 3, StandardCharsets.UTF_8); 
                    bytes = new Vector<Byte>();
                    opCodeReceived = false;
                    return new WRQ(fileName);
            }
            break;
            case 3: //DATA
            if (bytes.size() == 3) {
                packetSizeArr = new byte[2];				
                packetSizeArr[0] = bytes.get(2);
            }
            if (bytes.size() == 4) {
                packetSizeArr[1] = bytes.get(3);
                packetSize = bytesToShort(packetSizeArr);	
            }
            if (bytes.size() == packetSize + 6) {
                bytesArr = new byte[bytes.size()];
                bytesArr = vectorToArray(bytes);
                short numOfBlocks = bytesToShort(Arrays.copyOfRange(bytesArr, 4, 6));
                byte[] data = Arrays.copyOfRange(bytesArr, 6, bytesArr.length);
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new DATA(packetSize, numOfBlocks, data);
            }
            break;
            case 4: //ACK
            if (bytes.size() == 3) 
                blockNumArr[0] = nextByte;
            if (bytes.size() == 4) {
                blockNumArr[1] = nextByte;
                short numOfBlocks = bytesToShort(blockNumArr);
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new ACK(numOfBlocks);
            }
            break;
            case 5: //ERROR
            byte[] ErrorCode = new byte[2];
            if (nextByte == 0) {
                ErrorCode[0] = bytes.get(2);
                ErrorCode[1] = bytes.get(3);
                bytesArr = new byte[bytes.size()];
                bytesArr = vectorToArray(bytes);
                String errorMessage = new String(bytesArr, 4, bytesArr.length - 5, StandardCharsets.UTF_8);
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new ERROR(bytesToShort(ErrorCode),errorMessage);
            }
            break;
            case 6: //DIRQ
            if (bytes.size() ==  2) {
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new DIRQ();
            }
            break;
            case 7: //LOGRQ
            if (nextByte == '\0'){
                bytesArr = new byte[bytes.size()];
                bytesArr = vectorToArray(bytes);
                String userName = new String(bytesArr, 2, bytesArr.length - 3, StandardCharsets.UTF_8);
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new LOGRQ(userName);
            }
            break;
            case 8: //DELRQ
            if (nextByte == '\0'){
                bytesArr = new byte[bytes.size()];
                bytesArr = vectorToArray(bytes);
                String fileName = new String(bytesArr, 2, bytesArr.length - 3, StandardCharsets.UTF_8);
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new DELRQ(fileName);
            }
            break;
            case 9: // BCAST
            if (bytes.size() >= 4 && nextByte == '\0') {
                byte deleteOrAdd = bytes.get(2);
                bytesArr = new byte[bytes.size()];
                bytesArr = vectorToArray(bytes);
                String fileName = new String(bytesArr, 3, bytesArr.length - 4, StandardCharsets.UTF_8);
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new BCAST(deleteOrAdd, fileName);
            }
            break;
              
            case 10: // DISC
                bytes = new Vector<Byte>();
                opCodeReceived = false;
                return new DISC();
        }
       }
        return null;
    }
    public static short bytesToShort(byte[] byteArr)
	{
		short result = (short)((byteArr[0] & 0xff) << 8);
		result += (short)(byteArr[1] & 0xff);
		return result;
	}
	public int findEnd (byte[] arr) {
		for (int i = 0; i<arr.length; i++) {
			if (arr[i] == '\0')
				return i; // returns the index of the last byte of this message
		}
		return -1; // Didn't file the end of the message
	}
	private byte[] vectorToArray(Vector<Byte> vec){
		byte[] bytesArr = new byte[vec.size()];
		for (int i=0; i<bytesArr.length; i++)
			bytesArr[i] = vec.remove(0);
		return bytesArr;
	}
}