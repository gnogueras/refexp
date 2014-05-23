/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.iminds.ilabt.jfed.refexp;

import be.iminds.ilabt.jfed.util.IOUtils;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author gerard
 */
public class Configuration {

    // Default configuration parameters
    File defaultPemKeyCertFile = new File(System.getProperty("user.home")+ File.separator+"Desktop"+File.separator+"wall2_gerardmn.pem");
    String defaultPemKeyCertPassword = "barcelona.girona";
    File defaultSshPublicKeyFile = new File(System.getProperty("user.home")+ File.separator+".ssh"+File.separator+"sfawrap_no_passphrase.pub");
    File defaultSshPrivateKeyFile = new File(System.getProperty("user.home")+ File.separator+".ssh"+File.separator+"sfawrap_no_passphrase");
    String defaultCertificateUsername = "gerardmn";
    String defaultNodeForSliver1 = "UPC-D6-105-RD1";
    String defaultNodeForSliver2 = "UPC-lab104-RD2";
    String defaultUrnCLabWrapper = "urn:publicid:IDN+confine:clab+authority+sa";
    
    String pemKeyCertFilename;
    String pemKeyCertPassword;
    String sshPublicKeyFilename;
    String sshPrivateKeyFilename;
    String sshPublicKey;
    String sshPrivateKey;
    String certificateUsername;
    String nodeForSliver1;
    String nodeForSliver2;
    String urnCLabWrapper;
    String experimentName;

    public Configuration() {
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        Date date = new Date();
        experimentName = "RefExp"+dateFormat.format(date);
    }
    
    public void setDefaultConfiguration() throws Exception{
        pemKeyCertFilename = defaultCertificateUsername;
        pemKeyCertPassword = defaultPemKeyCertPassword;
        sshPublicKeyFilename = defaultSshPublicKeyFile.getPath();
        sshPublicKey = IOUtils.fileToString(sshPublicKeyFilename);
        sshPrivateKeyFilename = defaultSshPublicKeyFile.getPath();
        sshPrivateKey = IOUtils.fileToString(sshPrivateKeyFilename);
        certificateUsername = defaultCertificateUsername;
        nodeForSliver1 = defaultNodeForSliver1;
        nodeForSliver2 = defaultNodeForSliver2;
        urnCLabWrapper = defaultUrnCLabWrapper;
    }
    
    public void askConfigurationParameters() throws Exception{
        
        
        // Obtain Certificate file for login, .pem file and password
        pemKeyCertFilename = IOUtils.askCommandLineInput("PEM key and certificate filename (default: \"" + defaultPemKeyCertFile.getPath() + "\")");
        if (pemKeyCertFilename == null || pemKeyCertFilename.equals(""))
            pemKeyCertFilename = defaultPemKeyCertFile.getPath();
        pemKeyCertPassword = IOUtils.askCommandLineInput("Key password for the certificate (if any) (ENTER for default password in the code)");
        if(pemKeyCertPassword == null || pemKeyCertPassword.toString().equals(""))
            pemKeyCertPassword = defaultPemKeyCertPassword;
        

        // Obtain SSH public key file
        sshPublicKeyFilename = IOUtils.askCommandLineInput("SSH Public Key filename (default: \"" + defaultSshPublicKeyFile.getPath() + "\")");
        if (sshPublicKeyFilename == null || sshPublicKeyFilename.equals(""))
            sshPublicKeyFilename = defaultSshPublicKeyFile.getPath();
        // Store SSH public key in a sting
        sshPublicKey = IOUtils.fileToString(sshPublicKeyFilename);
        
        // Obtain SSH private key
        sshPrivateKeyFilename = IOUtils.askCommandLineInput("SSH Private Key filename (default: \"" + defaultSshPrivateKeyFile.getPath() + "\")");
        if (sshPrivateKeyFilename == null || sshPrivateKeyFilename.equals(""))
            sshPrivateKeyFilename = defaultSshPrivateKeyFile.getPath();
        // Store SSH public key in a sting
        sshPrivateKey = IOUtils.fileToString(sshPrivateKeyFilename);

        // Obtain certificate Username
        certificateUsername = IOUtils.askCommandLineInput("Username for the certificate (default: \""+defaultCertificateUsername+"\")");
        if (certificateUsername == null || certificateUsername.equals("")) certificateUsername = defaultCertificateUsername;

        // Obtain nodes for slivers
        nodeForSliver1 = IOUtils.askCommandLineInput("Name of the node for Sliver 1 (default: \""+defaultNodeForSliver1+"\")");
        if (nodeForSliver1 == null || nodeForSliver1.equals("")) nodeForSliver1 = defaultNodeForSliver1;
        nodeForSliver2 = IOUtils.askCommandLineInput("Name of the node for Sliver 2 (default: \""+defaultNodeForSliver2+"\")");
        if (nodeForSliver2 == null || nodeForSliver2.equals("")) nodeForSliver2 = defaultNodeForSliver2;

        // Obtain URN of the CLab Wrapper
        urnCLabWrapper = IOUtils.askCommandLineInput("Enter the URN of the C-Lab Wrapper server (jFed authorities.xml list) (default: \""+defaultUrnCLabWrapper+"\")");
        if (urnCLabWrapper == null || urnCLabWrapper.equals("")) urnCLabWrapper = defaultUrnCLabWrapper;
        
        // Change experiment name
        urnCLabWrapper = IOUtils.askCommandLineInput("Enter experiment name (default: "+experimentName+")");
        if (urnCLabWrapper == null || urnCLabWrapper.equals("")) urnCLabWrapper = defaultUrnCLabWrapper;
        
    }

    public String getPemKeyCertFilename() {
        return pemKeyCertFilename;
    }

    public void setPemKeyCertFilename(String pemKeyCertFilename) {
        this.pemKeyCertFilename = pemKeyCertFilename;
    }

    public String getPemKeyCertPassword() {
        return pemKeyCertPassword;
    }

    public void setPemKeyCertPassword(String pemKeyCertPassword) {
        this.pemKeyCertPassword = pemKeyCertPassword;
    }

    public String getSshPublicKeyFilename() {
        return sshPublicKeyFilename;
    }

    public void setSshPublicKeyFilename(String sshPublicKeyFilename) {
        this.sshPublicKeyFilename = sshPublicKeyFilename;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public String getCertificateUsername() {
        return certificateUsername;
    }

    public void setCertificateUsername(String certificateUsername) {
        this.certificateUsername = certificateUsername;
    }

    public String getNodeForSliver1() {
        return nodeForSliver1;
    }

    public void setNodeForSliver1(String nodeForSliver1) {
        this.nodeForSliver1 = nodeForSliver1;
    }

    public String getNodeForSliver2() {
        return nodeForSliver2;
    }

    public void setNodeForSliver2(String nodeForSliver2) {
        this.nodeForSliver2 = nodeForSliver2;
    }

    public String getUrnCLabWrapper() {
        return urnCLabWrapper;
    }

    public void setUrnCLabWrapper(String urnCLabWrapper) {
        this.urnCLabWrapper = urnCLabWrapper;
    }

    public String getSshPrivateKeyFilename() {
        return sshPrivateKeyFilename;
    }

    public void setSshPrivateKeyFilename(String sshPrivateKeyFilename) {
        this.sshPrivateKeyFilename = sshPrivateKeyFilename;
    }

    public String getSshPrivateKey() {
        return sshPrivateKey;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

}