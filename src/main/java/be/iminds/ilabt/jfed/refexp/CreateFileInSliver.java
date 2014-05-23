/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.iminds.ilabt.jfed.refexp;

import com.jcraft.jsch.*;
import java.awt.*;
import java.io.InputStream;
import javax.swing.*;
 

/**
 *
 * @author gerard
 * 
 * Code based on Exec example from JCraft
 * 
 */
public class CreateFileInSliver {
    
    // based on example from JCraft
    // JSch - Example Exec.java
    // http://www.jcraft.com/jsch/examples/Exec.java.html
    //
    public static String run(String privateKey, String sliverHost, String sliverUser, int sliverId){
    //public static void main(String[] args){
        System.out.print("--> Create file in Sliver "+sliverHost+" : ");
        String output = "";
        String fileName = "refexp_file"+sliverId;

        try{
            JSch jsch=new JSch();  
            jsch.addIdentity(privateKey);

            Session session=jsch.getSession(sliverUser, sliverHost, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            String createFileCommand="echo \"File created by sliver" +sliverId+ "("+sliverHost+")"+ "in RefExp\" > "+fileName;
            
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(createFileCommand);

            //channel.setInputStream(System.in);
            channel.setInputStream(null); //COMMENT

            InputStream in=channel.getInputStream(); //COMMENT

            channel.connect();
            
            //COMMENT
            byte[] tmp=new byte[1024];
            while(true){
              while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
                String r = new String(tmp, 0, i);
                //System.out.print(r);
                output = output + r;
              }
              if(channel.isClosed()){
                if(in.available()>0) continue; 
                //System.out.println("exit-status: "+channel.getExitStatus());
                break;
              }
              try{Thread.sleep(1000);}catch(Exception ee){}
            }
            // END OF COMMENT
            String exitStatus = channel.getExitStatus()==0 ? "SUCCESS" : "FAILED";
            System.out.println(" ["+exitStatus+"]");
            
            channel.disconnect();
            session.disconnect();
          }
          catch(Exception e){
            System.out.println(e);
          }
        return fileName;
        }
   
}
