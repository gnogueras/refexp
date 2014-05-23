/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.iminds.ilabt.jfed.refexp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author gerard
 * 
 * Code based on ScpTo and ScpFrom examples from JCraft
 * 
 */


public class SCP {
    
    // based on example from JCraft
    // JSch - Example ScpTo.java
    // http://www.jcraft.com/jsch/examples/ScpTo.java.html
    //
    public static void toRemoteHost(String privateKey, String localFile,
            String remoteUser, String remoteHost, String remoteFile){  
        
        System.out.print("--> Copy file ("+localFile+") from localhost to remotehost ("+remoteHost+"): ");
        
        FileInputStream fis=null;
        try{
 
          JSch jsch=new JSch();
          jsch.addIdentity(privateKey);
          Session session=jsch.getSession(remoteUser, remoteHost, 22);
          session.setConfig("StrictHostKeyChecking", "no");
          session.connect();

          boolean ptimestamp = true;

          // exec 'scp -t rfile' remotely
          String command="scp " + (ptimestamp ? "-p" :"") +" -t "+remoteFile;
          Channel channel=session.openChannel("exec");
          ((ChannelExec)channel).setCommand(command);

          // get I/O streams for remote scp
          OutputStream out=channel.getOutputStream();
          InputStream in=channel.getInputStream();

          channel.connect();

          if(checkAckTo(in)!=0){
            //System.exit(0);
          }

          File _lfile = new File(localFile);

          if(ptimestamp){
              command="T "+(_lfile.lastModified()/1000)+" 0";
              // The access time should be sent here,
              // but it is not accessible with JavaAPI ;-<
              command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
              out.write(command.getBytes()); out.flush();
              if(checkAckTo(in)!=0){
                //System.exit(0);
              }
          }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize=_lfile.length();
            command="C0644 "+filesize+" ";
            if(localFile.lastIndexOf('/')>0){
              command+=localFile.substring(localFile.lastIndexOf('/')+1);
            }
            else{
              command+=localFile;
            }
            command+="\n";
            out.write(command.getBytes()); out.flush();
            if(checkAckTo(in)!=0){
              //System.exit(0);
            }

            // send a content of lfile
            fis=new FileInputStream(localFile);
            byte[] buf=new byte[1024];
            while(true){
              int len=fis.read(buf, 0, buf.length);
              if(len<=0) break;
              out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis=null;
            // send '\0'
            buf[0]=0; out.write(buf, 0, 1); out.flush();
            if(checkAckTo(in)!=0){
              //System.exit(0);
            }
            out.close();
            channel.disconnect();
            session.disconnect();
            System.out.println("[SUCCESS]");
            //System.exit(0);
          }
          catch(Exception e){
            System.out.println(e);
            try{if(fis!=null)fis.close();}catch(Exception ee){}
          }
        return;
        }

        static int checkAckTo(InputStream in) throws IOException{
          int b=in.read();
          // b may be 0 for success,
          //          1 for error,
          //          2 for fatal error,
          //          -1
          if(b==0) return b;
          if(b==-1) return b;

          if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c;
            do {
              c=in.read();
              sb.append((char)c);
            }
            while(c!='\n');
            if(b==1){ // error
              System.out.print(sb.toString());
            }
            if(b==2){ // fatal error
              System.out.print(sb.toString());
            }
          }
          return b;
        }
        
        
        
        // based on example from JCraft
        // JSch - Example ScpFrom.java
        // http://www.jcraft.com/jsch/examples/ScpFrom.java.html
        //
        public static void fromRemoteHost(String privateKey, String localFile,
            String remoteUser, String remoteHost, String remoteFile){ 
            
            System.out.print("--> Copy file ("+remoteFile+") from remotehost ("+remoteHost+") to localhost: ");
            FileOutputStream fos=null;
            try{

              String prefix=null;
              if(new File(localFile).isDirectory()){
                prefix=localFile+File.separator;
              }

              JSch jsch=new JSch();
              jsch.addIdentity(privateKey);
       
              Session session=jsch.getSession(remoteUser, remoteHost, 22);
              session.setConfig("StrictHostKeyChecking", "no");
              session.connect();

              // exec 'scp -f rfile' remotely
              String command="scp -f "+remoteFile;
              Channel channel=session.openChannel("exec");
              ((ChannelExec)channel).setCommand(command);

              // get I/O streams for remote scp
              OutputStream out=channel.getOutputStream();
              InputStream in=channel.getInputStream();

              channel.connect();

              byte[] buf=new byte[1024];

              // send '\0'
              buf[0]=0; out.write(buf, 0, 1); out.flush();

              while(true){
                int c=checkAckFrom(in);
                if(c!='C'){
                  break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize=0L;
                while(true){
                  if(in.read(buf, 0, 1)<0){
                    // error
                    break; 
                  }
                  if(buf[0]==' ')break;
                  filesize=filesize*10L+(long)(buf[0]-'0');
                }

                String file=null;
                for(int i=0;;i++){
                  in.read(buf, i, 1);
                  if(buf[i]==(byte)0x0a){
                    file=new String(buf, 0, i);
                    break;
                  }
                }

                //System.out.println("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0]=0; out.write(buf, 0, 1); out.flush();

                // read a content of lfile
                fos=new FileOutputStream(prefix==null ? localFile : prefix+file);
                int foo;
                while(true){
                  if(buf.length<filesize) foo=buf.length;
                  else foo=(int)filesize;
                  foo=in.read(buf, 0, foo);
                  if(foo<0){
                    // error 
                    break;
                  }
                  fos.write(buf, 0, foo);
                  filesize-=foo;
                  if(filesize==0L) break;
                }
                fos.close();
                fos=null;

                if(checkAckFrom(in)!=0){
                  //System.exit(0);
                }

                // send '\0'
                buf[0]=0; out.write(buf, 0, 1); out.flush();
              }
              
              session.disconnect();
              System.out.println("[SUCCESS]");
              //System.exit(0);
            }
            catch(Exception e){
              System.out.println(e);
              try{if(fos!=null)fos.close();}catch(Exception ee){}
            }
            return;
          }

          static int checkAckFrom(InputStream in) throws IOException{
            int b=in.read();
            // b may be 0 for success,
            //          1 for error,
            //          2 for fatal error,
            //          -1
            if(b==0) return b;
            if(b==-1) return b;

            if(b==1 || b==2){
              StringBuffer sb=new StringBuffer();
              int c;
              do {
                c=in.read();
                sb.append((char)c);
              }
              while(c!='\n');
              if(b==1){ // error
                System.out.print(sb.toString());
              }
              if(b==2){ // fatal error
                System.out.print(sb.toString());
              }
            }
            return b;
          }


          
    
}
