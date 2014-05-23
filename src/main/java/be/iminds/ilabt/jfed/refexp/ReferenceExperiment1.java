/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package be.iminds.ilabt.jfed.refexp;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author gerard
 */
public class ReferenceExperiment1 {
    // ping6 Reference Experiment
    public static void main(String[] args) throws Exception {
        
        System.out.println("\n-----------------------------\nCONFIGURATION PARAMETERS\n-----------------------------");
        Configuration config = new Configuration();
        config.askConfigurationParameters();
        
        String homeDir = System.getProperty("user.home");
        File  dir = new File(homeDir + File.separator + "referenceExperiment" + File.separator + config.getExperimentName());
        dir.mkdirs();
        
        System.out.println("\n-----------------------------\nSLICE AND SLIVERS SETUP\n-----------------------------");
        List<SetupSliceSlivers.MySliverInfo> sliversInfo = new SetupSliceSlivers().run(config.getPemKeyCertFilename(), config.getCertificateUsername(), config.getPemKeyCertPassword(), 
                config.getNodeForSliver1(), config.getNodeForSliver2(), config.getSshPublicKeyFilename(), config.getExperimentName());
        String setupResults = "SLIVER 1\n--------" +
                "\nSliver URN: " + sliversInfo.get(0).getUrn() +
                "\nSlice URN: " + sliversInfo.get(0).getSliceUrn() +
                "\nNode URN: " + sliversInfo.get(0).getNodeUrn() +
                "\nLogin Info: " + 
                "\n\t authentication: " + sliversInfo.get(0).getLoginAuthentication() +
                "\n\t hostname: " + sliversInfo.get(0).getLoginHostname() +
                "\n\t port: " + sliversInfo.get(0).getLoginPort() +
                "\n\t username: " + sliversInfo.get(0).getLoginUsername() +
                "\n\nSLIVER 2\n--------" +
                "\nSliver URN: " + sliversInfo.get(1).getUrn() +
                "\nSlice URN: " + sliversInfo.get(1).getSliceUrn() +
                "\nNode URN: " + sliversInfo.get(1).getNodeUrn() +
                "\nLogin Info: " + 
                "\n\t authentication: " + sliversInfo.get(1).getLoginAuthentication() +
                "\n\t hostname: " + sliversInfo.get(1).getLoginHostname() +
                "\n\t port: " + sliversInfo.get(1).getLoginPort() +
                "\n\t username: " + sliversInfo.get(1).getLoginUsername();


        PrintWriter writer = new PrintWriter(dir.getPath()+File.separator+"sliverSetupDetails.txt", "UTF-8");
        writer.println(setupResults);
        writer.close();
     
        System.out.println("\n-----------------------------\nPING EXPERIMENT\n-----------------------------");
        String pingResults = PingSlivers.run(3, config.getSshPrivateKeyFilename(), 
                sliversInfo.get(0).getLoginHostname(), sliversInfo.get(1).getLoginHostname());
        

        writer = new PrintWriter(dir.getPath()+File.separator+"pingDetails.txt", "UTF-8");
        writer.println("Sliver1 sends 3 pings to sliver 2");
        writer.println("\tSliver1: "+sliversInfo.get(0).getUrn()+ "\n\t        "+sliversInfo.get(0).getLoginHostname());
        writer.println("\tSliver2: "+sliversInfo.get(1).getUrn()+ "\n\t        "+sliversInfo.get(1).getLoginHostname());
        writer.println("\n"+pingResults);
        writer.close();
        
        System.out.println("\n-----------------------------\nRESULTS\n-----------------------------");
        System.out.println("[SUCCESS]\nCheck directory "+dir.getPath()+" for more details");
        
        
    }
    
}
