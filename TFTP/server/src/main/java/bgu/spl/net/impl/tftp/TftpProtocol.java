package bgu.spl.net.impl.tftp;
import java.nio.file.Files;
import java.nio.file.Path;
import bgu.spl.net.impl.Packets.*;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import bgu.spl.net.srv.OperationStateTracker;

public class TftpProtocol implements BidiMessagingProtocol<Packet>  {
    private boolean shouldTerminate;
    private ConnectionsImpl connections;
    int connectionId;
    //readDataPackets and writeDataPackets are used to store DATA packets that are either read from files or received from clients for writing to files
    private ConcurrentLinkedDeque<DATA> readDPackets;
    private ConcurrentLinkedDeque<DATA> writeDPackets;
    private static ConcurrentHashMap<Integer, String> loggedUsers=new ConcurrentHashMap<Integer,String>();
    private OperationStateTracker state;
    private String path="";

    public TftpProtocol(){
        shouldTerminate=false;
        readDPackets= new ConcurrentLinkedDeque<>();
        writeDPackets= new ConcurrentLinkedDeque<>();
        state=new OperationStateTracker();
    }
    public void start(int connectionId, Connections connections) {
        this.connections=(ConnectionsImpl)connections;

        this.connectionId=connectionId;
    }
    @Override
    public void process(Packet message) {
        if(message.getOpcode()==1) { //RRQ
            if (isLoggedin()) {
                String file = ((RRQ) message).getFileName(); // extract the requested file name
                Path filePath = Paths.get("Files/" + file);
                if (Files.exists(filePath)) {
                    try {
                        byte[] fileBytes = Files.readAllBytes(filePath); 
                        for (int i = 0; i < fileBytes.length; i += 512) {
                            byte[] currData;
                            DATA currtPacket;   
                            if (fileBytes.length - i < 512) {
                                currData = Arrays.copyOfRange(fileBytes, i, fileBytes.length);
                                currtPacket = new DATA((short) (fileBytes.length - i), (short) (i / 512 + 1), currData);
                            } else {
                                currData = Arrays.copyOfRange(fileBytes, i, i + 512);
                                currtPacket = new DATA((short) (512), (short) (i / 512 + 1), currData);
                            } 
                            readDPackets.add(currtPacket); // add the packet to the queue
                            state.incDataRRQCounter();
                        }  
                        state.setSendRRQ(true);
                        connections.send(connectionId, readDPackets.poll());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Packet error = new ERROR((short) 2, "Access violation");
                        connections.send(connectionId, error);
                    }
                } else {
                    Packet error = new ERROR((short) 1, "File not found");
                    connections.send(connectionId, error);
                }
            }
        }
        if(message.getOpcode()==2){//WRQ
            if (isLoggedin()) {
                path = ((WRQ) message).getFileName();
                boolean exist = false;
                File folder = new File("Files");
                String[] fileNames = folder.list(); // get a list of the filenames from the folder
                for (int i = 0; i < Objects.requireNonNull(fileNames).length; i++) { // check if the file already exist
                    if (fileNames[i].equals(path)) {
                        exist = true;
                        break;
                    }
                }
                if (exist) {
                    Packet error = new ERROR((short) 5, "File already exists");
                    connections.send(connectionId, error);
                }
                else {
                    state.setExpWRQ(true);
                    ACK ack = new ACK((short) 0);
                    connections.send(connectionId, ack);
                }
            }
        }
        if(message.getOpcode()==3){//DATA
            if (isLoggedin()){
                if (state.isExpWRQ()){
                    writeDPackets.add((DATA)message);
                    ACK ack=new ACK (((DATA)message).getNumOfBlock());
                    connections.send(connectionId, ack);
                    if(((DATA)message).getData().length<512){
                        File newFile=new File ("Files/"+path);
                        try {
                            boolean created = newFile.createNewFile(); // Check if the file was created successfully
                            if (created) {
                                FileOutputStream output = new FileOutputStream("Files/" + path);
                                while (!writeDPackets.isEmpty()) {
                                    output.write(writeDPackets.poll().getData());
                                }
                                state.setExpWRQ(false);
                                BCAST bcast = new BCAST((byte) 1, path);
                                Enumeration<Integer> users = loggedUsers.keys();
                                while (users.hasMoreElements()) { //sends the broadcast message to all logged-in users
                                    connections.send(users.nextElement(), bcast);
                                }
                            }
                            else {
                                // Handle file creation failure
                                ERROR error = new ERROR((short) 0, "Failed to create file");
                                connections.send(connectionId, error);
                            }
                        }
                        catch (IOException e) {
                            writeDPackets.clear();
                            ERROR error=new ERROR((short)0, "file was already written");
                            connections.send(connectionId, error);
                            e.printStackTrace();
                        }

                    }
                }
                else{
                    ERROR error=new ERROR((short)6, "not expecting data packet");
                    connections.send(connectionId, error);
                }
            }
        }
        if(message.getOpcode()==4){//ACK
            if (isLoggedin()){
                if(state.isSendDirq()){
                    if(!readDPackets.isEmpty()){
                        connections.send(connectionId, readDPackets.poll());
                        state.decDataDIRQCounter();
                    }
                    if (state.getDataDIRQCounter()==0)
                        state.setSendDirq(false);
                }
                else if(state.isSendRRQ()){
                    if(!readDPackets.isEmpty()){
                        DATA data=readDPackets.poll();
                        connections.send(connectionId, data);
                        state.decDataRRQCounter();
                    }
                    if (state.getDataRRQCounter()==0)
                        state.setSendRRQ(false);
                }
                else{
                    ERROR error=new ERROR((short)0, "not expecting ack packet"); //if not, throw an error
                    connections.send(connectionId, error);
                }
            }
        }if(message.getOpcode()==5){ //ERROR
            isLoggedin();
        }
        if(message.getOpcode()==6){ //DIRQ
            if (isLoggedin()){
                File folder=new File("Files");
                String []fileNames=folder.list();
                String answer="";
                for(int i = 0; i< fileNames.length; i++){ //create a string with all the filenames
                    answer=answer + fileNames[i]+ '\0';
                }
                byte[] stringBytes=answer.getBytes();
                if(stringBytes.length==0){ //the case in which the folder is empty
                    DATA data = new DATA((short)0,(short)1,stringBytes);
                    connections.send(connectionId, data);
                }
                for (int i=0; i<stringBytes.length; i=i+512){ //creates data packets and adds them to the queue
                    byte[] currentData;
                    DATA currentPacket;
                    if (stringBytes.length-i<512){ //the last packet
                        currentData=Arrays.copyOfRange(stringBytes, i, stringBytes.length);
                        currentPacket=new DATA ((short)(stringBytes.length-i),(short)(i/512+1), currentData);
                        readDPackets.add(currentPacket); //add the packet to the queue
                    }
                    else{
                        currentData=Arrays.copyOfRange(stringBytes, i, i+512);
                        currentPacket=new DATA ((short)(i+512),(short)(i/512), currentData);
                        readDPackets.add(currentPacket); //add the packet to the queue
                        state.incDataDIRQCounter();
                    }
                }
                state.setSendDirq(true);
                connections.send(connectionId, readDPackets.poll());
            }
        }
        if(message.getOpcode()==7){//LOGRQ
            //check if connectionId already exists
            if (loggedUsers.containsKey(connectionId)){
                ERROR error=new ERROR((short) 7, "user already logged in");
                connections.send(connectionId, error);
            }
            else{
                loggedUsers.put(connectionId, ((LOGRQ) message).getName());
                Packet ack=new ACK((short)0);
                connections.send(connectionId, ack);}
        }
        if(message.getOpcode()==8){//DELRQ
            if (isLoggedin()){
                String file=((DELRQ)message).getFileName();
                boolean exist=false;
                File folder = new File ("Files");
                String [] fileNames=folder.list(); //get a list of the filenames from the folder
                for (int i = 0; i< Objects.requireNonNull(fileNames).length; i++){ //check if the file already exist
                    if (fileNames[i].equals(file)){
                        exist=true;
                        break;
                    }
                }
                if(exist){
                    File fileDelete =new File ("Files/"+file);
                    fileDelete.delete();
                    BCAST bcast=new BCAST ((byte)'\0', path);
                    Enumeration<Integer> users =loggedUsers.keys();
                    while (users.hasMoreElements()){ //sends the broadcast message to all logged-in users
                        connections.send(users.nextElement(), bcast);
                    }
                }
                else{
                    Packet error=new ERROR ((short) 1, "File not found");
                    connections.send(connectionId, error);
                }
            }
        }
        if(message.getOpcode()==9){//BCAST
            if (isLoggedin()) {
             // Extract necessary information from the BCAST packet
                byte addOrDel = ((BCAST) message).getDelOrAdd();
                String name = ((BCAST) message).getFileName();
                BCAST bcast = new BCAST(addOrDel, name);
                Enumeration<Integer> users = loggedUsers.keys();
                while (users.hasMoreElements()) {
                    connections.send(users.nextElement(), bcast);//broadcasting to all logged-in users
                }
            }
        }
        if(message.getOpcode()==10){ //DISC
            if (isLoggedin()){
                loggedUsers.remove(connectionId);
                shouldTerminate=true;
                ACK ack = new ACK ((short)0);
                connections.send(connectionId, ack);
            }
        }
        if(message.getOpcode()==11){//Illegal
            Packet error=new ERROR ((short) 4, "Illegal TFTP operation");
            connections.send(connectionId, error);
        }
        }
    
    @Override
    public boolean shouldTerminate() {
        if(shouldTerminate){
            connections.disconnect(connectionId);
            loggedUsers.remove(connectionId);
        }
        return shouldTerminate;
    }
    public boolean isLoggedin() {
        if (!loggedUsers.containsKey(connectionId)) {
            Packet error = new ERROR((short) 6, "User not logged in");
            connections.send(connectionId, error);
            return false;
        } else {
            return true;
        }
    }

}
