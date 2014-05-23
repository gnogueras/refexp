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
public class PingSlivers {
    
    // based on example from JCraft
    // JSch - Example Exec.java
    // http://www.jcraft.com/jsch/examples/Exec.java.html
    //
    public static String run(int counter, String privateKey, String fromSliver, String toSliver){
    //public static void main(String[] args){
        long startTime, endTime, duration;
        System.out.print("--> Created sliver 1 pings created sliver 2: ");
        String output = "";
        startTime = System.nanoTime();
        try{
            JSch jsch=new JSch();  
            jsch.addIdentity(privateKey);

            String fromUser = "root";
            Session session=jsch.getSession(fromUser, fromSliver, 22);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            String pingCommand="ping6 -c"+counter+" "+toSliver;
            
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(pingCommand);

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
            endTime = System.nanoTime();
            String exitStatus = channel.getExitStatus()==0 ? "SUCCESS" : "FAILED";
            System.out.print(" ["+exitStatus+"]");
            duration = endTime - startTime;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            channel.disconnect();
            session.disconnect();
          }
          catch(Exception e){
            System.out.println(e);
          }
        return output;
        }
   
}
