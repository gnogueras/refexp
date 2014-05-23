package be.iminds.ilabt.jfed.refexp;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractGeniAggregateManager;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3.SliverInfo;
import be.iminds.ilabt.jfed.lowlevel.api.ProtogeniSliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnection;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnectionPool;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaSslConnection;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.IOUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SetupSliceSlivers {
    
    
    protected static String getValueFieldFromRspec(String rspec, String field, int matching_pos) {
        String value = "?";
        Pattern pattern = Pattern.compile(field+"=\"(.*?)\"");
        Matcher matcher = pattern.matcher(rspec);
        if (matcher.find())
        {
            value=matcher.group(matching_pos);
        }
        return value;
    }
    
    //public static void main(String[] args) throws Exception {
    public List<MySliverInfo> run(String pemKeyCertFilename, String certificateUsername, String pemKeyCertPassword, 
            String nodeForSliver1, String nodeForSliver2, String sshPublicKeyFilename, String experimentName) throws Exception {
        
        long startTime, endTime, duration, aggregate=0;
        List<MySliverInfo> slivers = new ArrayList<MySliverInfo>();

        try {
            
            ////////////////////////////////////////// Preparation for the Reference Experiment ///////////////////////////////////////
            
            // Create new logger
            Logger logger = new Logger();
            
            File defaultPemKeyCertFile = new File(System.getProperty("user.home")+ File.separator+"Desktop"+File.separator+"wall2_gerardmn.pem");

            ////////////////////////////////////////// Operations on the VirtualWall2 Authority ///////////////////////////////////////
            
            // Get target authority, VirtualWall2
            SfaAuthority wall = JFedAuthorityList.getAuthorityListModel().getByUrn("urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");
            
            // Setup user
            char[] pass = pemKeyCertPassword.toCharArray();
            SimpleGeniUser user = new SimpleGeniUser(wall, new GeniUrn("urn:publicid:IDN+wall2.ilabt.iminds.be+user+"+certificateUsername), IOUtils.fileToString(pemKeyCertFilename), (pass.length == 0) ? null : pass, defaultPemKeyCertFile, defaultPemKeyCertFile);
            
            
            // Get a credential from the SA. This time use a ConnectionPool 
            ProtogeniSliceAuthority sa = new ProtogeniSliceAuthority(logger);

            SfaConnectionPool conPool = new SfaConnectionPool();
            SfaConnection saCon = (SfaConnection) conPool.getConnectionByAuthority(user, wall, new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));

            //IOUtils.askCommandLineInput("1. Get user credential from Virtual Wall 2 Authority");
            System.out.print("--> Get user credential from Virtual Wall 2 Authority: ");
            startTime = System.nanoTime();
            ProtogeniSliceAuthority.SliceAuthorityReply<AnyCredential> getCredentialReply = sa.getCredential(saCon);
            endTime = System.nanoTime();
            AnyCredential userCred = getCredentialReply.getValue();
            System.out.print("["+getCredentialReply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);            
            //System.out.println("\n\ncredential:\n"+userCred.getCredentialXml().substring(0, userCred.getCredentialXml().length() > 300 ? 300 : userCred.getCredentialXml().length())+"...\n");
            
            
            // Register a slice in VirtualWall2
            //IOUtils.askCommandLineInput("2. Register new slice at Virtual Wall 2 Authority");
            //DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
            //Date date = new Date();
            //String sliceName = "RefExp"+dateFormat.format(date);
            System.out.print("--> Register new slice at Virtual Wall 2 Authority: ");
            String authority = "wall2.ilabt.iminds.be";
            String newSliceUrn = "urn:publicid:IDN+"+authority+"+slice+"+experimentName;
            ResourceUrn slice = new ResourceUrn(newSliceUrn); //"urn:publicid:IDN+<AUTHORITY>+slice+<SLICENAME>"
            startTime = System.nanoTime();
            ProtogeniSliceAuthority.SliceAuthorityReply<AnyCredential> registerReply = sa.register(saCon, userCred, slice);
            endTime = System.nanoTime();
            System.out.print("["+registerReply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            // Get Slice credential from VirtualWall2
            //IOUtils.askCommandLineInput("3. Get Slice Credential of the new registered slice at Virtual Wall 2 Authority");
            System.out.print("--> Get Slice Credential of the new registered slice at Virtual Wall 2 Authority: ");
            startTime = System.nanoTime();
            ProtogeniSliceAuthority.SliceAuthorityReply<AnyCredential> getSliceCredReply = sa.getSliceCredential(saCon, userCred, slice);
            endTime = System.nanoTime();
            AnyCredential sliceCred = getSliceCredReply.getValue();
            System.out.print("["+getSliceCredReply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);            
            
            
            ////////////////////////////////////////// Operations on the CLab Wrapper Authority ///////////////////////////////////////
            
            AggregateManager3 am3 = new AggregateManager3(logger);
            
            // Get target authority, CLab wrapper
            SfaAuthority clab = JFedAuthorityList.getAuthorityListModel().getByUrn("urn:publicid:IDN+confine:clab+authority+sa");

            URL clabAm3Url = clab.getUrl(ServerType.GeniServerRole.AM, 3);
            SfaSslConnection am3Con;
            //SfaSslConnection am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);

            // Prepare parameters for Allocate operation
            List<AnyCredential> sliceCredList = new ArrayList<AnyCredential>();
            sliceCredList.add(sliceCred);
            
            String sliceUrn = slice.getValue();
            String expirationTime = null;
            // SfaConnection con, List<AnyCredential> credentialList, String sliceUrn, String rspec, String endTime, Map<String, Object> extraOptions

            // Allocate Sliver 1
            //IOUtils.askCommandLineInput("4a. Allocate Sliver 1 at C-Lab SFAWrap AM");
            System.out.print("--> Allocate Sliver 1 at C-Lab SFAWrap AM with the previous Slice credential: ");
            String reqRSpecSliver1 = "<rspec type=\"request\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">   \n" +
                                    "  <node client_id=\""+nodeForSliver1+"\" component_id=\"urn:publicid:IDN+confine:clab+node+"+nodeForSliver1+"\" component_manager_id=\"urn:publicid:IDN+confine+authority+am\" exclusive=\"true\">\n" +
                                    "    <sliver_type name=\"RD_sliver\"/>\n" +
                                    "  </node>\n" +
                                    "</rspec>";
            startTime = System.nanoTime();
            am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
            AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> allocateSliver1Reply = am3.allocate(am3Con, sliceCredList, sliceUrn, reqRSpecSliver1, expirationTime, null);
            endTime = System.nanoTime();
            SliverInfo sliver1 = allocateSliver1Reply.getValue().getSliverInfo().get(0);
            System.out.print("["+allocateSliver1Reply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            // Allocate Sliver 2  
            //IOUtils.askCommandLineInput("4b. Allocate Sliver 2 at C-Lab SFAWrap AM");
            System.out.print("--> Allocate Sliver 2 at C-Lab SFAWrap AM with the previous Slice credential: ");
            String reqRSpecSliver2 = "<rspec type=\"request\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">   \n" +
                                    "  <node client_id=\""+nodeForSliver2+"\" component_id=\"urn:publicid:IDN+confine:clab+node+"+nodeForSliver2+"\" component_manager_id=\"urn:publicid:IDN+confine+authority+am\" exclusive=\"true\">\n" +
                                    "    <sliver_type name=\"RD_sliver\"/>\n" +
                                    "  </node>\n" +
                                    "</rspec>";
            startTime = System.nanoTime();
            am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
            AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> allocateSliver2Reply = am3.allocate(am3Con, sliceCredList, sliceUrn, reqRSpecSliver2, expirationTime, null);
            endTime = System.nanoTime();
            SliverInfo sliver2 = allocateSliver2Reply.getValue().getSliverInfo().get(0);
            System.out.print("["+allocateSliver2Reply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            // Prepare parameters for Provision operation 
            List<String> urnsList = new ArrayList<String>();
            String rspecType = "geni"; 
            String rspecVersion = "3";
            
            // Store SSH public key in a sting
            String sshPublicKey = IOUtils.fileToString(sshPublicKeyFilename);
            Vector<String> sshKeys = new Vector<String>();
            sshKeys.add(sshPublicKey);
            
            UserSpec userSpec = new UserSpec(user.getUserUrnString(), sshKeys);
            List<UserSpec> users = new ArrayList<UserSpec>();
            users.add(userSpec);
            
            // Provision Sliver 1
            //IOUtils.askCommandLineInput("5a. Provision Sliver 1 at C-Lab SFAWrap AM");
            System.out.print("--> Provision Sliver 1 at C-Lab SFAWrap AM: ");
            urnsList.add(sliver1.getSliverUrn());
            startTime = System.nanoTime();
            am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
            AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> provisionSliver1Reply = am3.provision(am3Con, urnsList, sliceCredList, rspecType, rspecVersion, Boolean.TRUE, expirationTime, users, null);
            endTime = System.nanoTime();
            String provisionRspecSliver1 = provisionSliver1Reply.getValue().getRspec();
            System.out.print("["+provisionSliver1Reply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            // Provision Sliver 2
            //IOUtils.askCommandLineInput("5b. Provision Sliver 2 at C-Lab SFAWrap AM");
            System.out.print("--> Provision Sliver 2 at C-Lab SFAWrap AM: ");
            urnsList.clear();
            urnsList.add(sliver2.getSliverUrn());
            startTime = System.nanoTime();
            am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
            AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> provisionSliver2Reply = am3.provision(am3Con, urnsList, sliceCredList, rspecType, rspecVersion, Boolean.TRUE, expirationTime, users, null);
            endTime = System.nanoTime();
            String provisionRspecSliver2 = provisionSliver2Reply.getValue().getRspec();
            System.out.print("["+provisionSliver2Reply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            
            // Start Slice (so all the slivers too)
            //IOUtils.askCommandLineInput("6. Start Slice with the Sliver 1 and 2 at C-Lab SFAWrap AM");
            System.out.print("--> Start Slice with the Sliver 1 and 2 at C-Lab SFAWrap AM: ");
            urnsList.clear();
            urnsList.add(sliceUrn);
            startTime = System.nanoTime();
            am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
            AbstractGeniAggregateManager.AggregateManagerReply<List<SliverInfo>> performOperationalActionReply = am3.performOperationalAction(am3Con, urnsList, sliceCredList, "geni_start", Boolean.TRUE, null);
            endTime = System.nanoTime();
            System.out.print("["+performOperationalActionReply.getGeniResponseCode().getDescription()+"]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);
            
            
            // Wait for slivers being ready
            //IOUtils.askCommandLineInput("7. Wait for slivers being Ready (wait 30 sec and check Status)");
            System.out.println("--> Wait for slivers being Ready. Wait 90 sec and check Status of Sliver 1 and 2: ");
            startTime = System.nanoTime();
            Thread.sleep(90000);
            am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
            AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager3.StatusInfo> statusReply = am3.status(am3Con, urnsList, sliceCredList, Boolean.TRUE, null);
            //System.out.println("--> Check Status of Sliver 1 and 2: ["+statusReply.getGeniResponseCode().getDescription()+"]");
            List<SliverInfo> sliverInfoList = statusReply.getValue().getSliverInfo();
            
            while(!(sliverInfoList.get(0).getOperationalStatus().equals("geni_ready") && sliverInfoList.get(1).getOperationalStatus().equals("geni_ready"))){
                // print status
                System.out.println("\t Sliver 1: "+sliverInfoList.get(0).getOperationalStatus()+ " || Sliver2: "+sliverInfoList.get(1).getOperationalStatus()+" [NOT READY]");
                //IOUtils.askCommandLineInput("7'. Slivers not ready. Wait 10 sec more and check Status again");
                Thread.sleep(10000);
                am3Con = new SfaSslConnection(clab, clabAm3Url.toExternalForm(), user.getClientCertificateChain(), user.getPrivateKey(), user, null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);
                statusReply = am3.status(am3Con, urnsList, sliceCredList, Boolean.TRUE, null);
                //System.out.println("--> Check Status of Sliver 1 and 2: ["+statusReply.getGeniResponseCode().getDescription()+"]");
                sliverInfoList = statusReply.getValue().getSliverInfo();
            }
            //print status
            endTime = System.nanoTime();
            System.out.print("\t Sliver 1: "+sliverInfoList.get(0).getOperationalStatus()+ " || Sliver2: "+sliverInfoList.get(1).getOperationalStatus() + " [SUCCESS]");
            duration = endTime - startTime;
            aggregate += duration;
            System.out.printf(" ( %.3f s )\n",duration/1000000000.0);   
            
            
            ////////////////////////////////////////// Return the information of the slivers ///////////////////////////////////////
            
            //IOUtils.askCommandLineInput("8. Slivers are ready. Print information of slivers");
            System.out.printf("--> Slivers are ready. Resource allocation process: %.3f s \n", aggregate/1000000000.0);
            System.out.println("--> Information of slivers");
                
            String urnSliver1 = sliverInfoList.get(0).getSliverUrn();
            String stateSliver1 = sliverInfoList.get(0).getAllocationStatus() + ":" + sliverInfoList.get(0).getOperationalStatus();
            String nodeUrnSliver1 = getValueFieldFromRspec(provisionRspecSliver1, "component_id", 1);
            String loginSliver1 = "\nLogin Information:"+
                                    "\n\t authentication="+getValueFieldFromRspec(provisionRspecSliver1, "authentication", 1) +
                                    "\n\t hostname="+getValueFieldFromRspec(provisionRspecSliver1, "hostname", 1) +
                                    "\n\t port="+getValueFieldFromRspec(provisionRspecSliver1, "port", 1) +
                                    "\n\t username="+getValueFieldFromRspec(provisionRspecSliver1, "username", 1);
            System.out.println("\n\nSLIVER 1\n--------"+
                    "\nSliver: " + urnSliver1 +
                    "\nSlice: " + sliceUrn +
                    "\nNode: " + nodeUrnSliver1 +
                    loginSliver1);
                    
            String urnSliver2 = sliverInfoList.get(1).getSliverUrn();
            String stateSliver2 = sliverInfoList.get(1).getAllocationStatus() + ":" + sliverInfoList.get(1).getOperationalStatus();
            String nodeUrnSliver2 = getValueFieldFromRspec(provisionRspecSliver2, "component_id", 1);
            String loginSliver2 = "\nLogin Information:"+
                                    "\n\t authentication="+getValueFieldFromRspec(provisionRspecSliver2, "authentication", 1) +
                                    "\n\t hostname="+getValueFieldFromRspec(provisionRspecSliver2, "hostname", 1) +
                                    "\n\t port="+getValueFieldFromRspec(provisionRspecSliver2, "port", 1) +
                                    "\n\t username="+getValueFieldFromRspec(provisionRspecSliver2, "username", 1);
            System.out.println("\n\nSLIVER 2\n--------"+
                    "\nSliver: " + urnSliver2 +
                    "\nSlice: " + sliceUrn +
                    "\nNode: " + nodeUrnSliver2 +
                    loginSliver2);
            
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Prepare Return
            MySliverInfo sliverInfo1 = new MySliverInfo(urnSliver1, nodeUrnSliver1, sliceUrn, 
                    getValueFieldFromRspec(provisionRspecSliver1, "authentication", 1), getValueFieldFromRspec(provisionRspecSliver1, "hostname", 1), 
                    getValueFieldFromRspec(provisionRspecSliver1, "port", 1), getValueFieldFromRspec(provisionRspecSliver1, "username", 1));
             MySliverInfo sliverInfo2 = new MySliverInfo(urnSliver2, nodeUrnSliver2, sliceUrn, 
                    getValueFieldFromRspec(provisionRspecSliver2, "authentication", 1), getValueFieldFromRspec(provisionRspecSliver2, "hostname", 1), 
                    getValueFieldFromRspec(provisionRspecSliver2, "port", 1), getValueFieldFromRspec(provisionRspecSliver2, "username", 1));
            slivers.add(sliverInfo1);
            slivers.add(sliverInfo2);
            
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
        } catch (JFedException e) {
            System.err.println("JFedException: "+e);
            e.printStackTrace();
            if (e.getXmlRpcResult() != null) {
                System.err.println("HTTP request:\n"+e.getXmlRpcResult().getRequestHttpContent());
                System.err.println("\nHTTP response:\n"+e.getXmlRpcResult().getResultHttpContent());
                System.err.println("\nXMLRPC request:\n"+e.getXmlRpcResult().getRequestXmlRpcString());
                System.err.println("\nXMLRPC response:\n"+e.getXmlRpcResult().getResultXmlRpcString());
            }
        }
        return slivers;

    }
    
    
    public class MySliverInfo {
        String urn;
        String nodeUrn;
        String sliceUrn;
        String state;
        String loginAuthentication;
        String loginHostname;
        String loginPort;
        String loginUsername;

        public MySliverInfo() {
        }

        public MySliverInfo(String urn, String nodeUrn, String state, String loginAuthentication, String loginHostname, String loginPort, String loginUsername) {
            this.urn = urn;
            this.nodeUrn = nodeUrn;
            this.state = state;
            this.loginAuthentication = loginAuthentication;
            this.loginHostname = loginHostname;
            this.loginPort = loginPort;
            this.loginUsername = loginUsername;
        }

        public String getUrn() {
            return urn;
        }

        public void setUrn(String urn) {
            this.urn = urn;
        }

        public String getNodeUrn() {
            return nodeUrn;
        }

        public void setNodeUrn(String nodeUrn) {
            this.nodeUrn = nodeUrn;
        }
        
        public String getSliceUrn() {
            return nodeUrn;
        }

        public void setSliceUrn(String nodeUrn) {
            this.nodeUrn = nodeUrn;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getLoginAuthentication() {
            return loginAuthentication;
        }

        public void setLoginAuthentication(String loginAuthentication) {
            this.loginAuthentication = loginAuthentication;
        }

        public String getLoginHostname() {
            return loginHostname;
        }

        public void setLoginHostname(String loginHostname) {
            this.loginHostname = loginHostname;
        }

        public String getLoginPort() {
            return loginPort;
        }

        public void setLoginPort(String loginPort) {
            this.loginPort = loginPort;
        }

        public String getLoginUsername() {
            return loginUsername;
        }

        public void setLoginUsername(String loginUsername) {
            this.loginUsername = loginUsername;
        }
        
        
    }
}
