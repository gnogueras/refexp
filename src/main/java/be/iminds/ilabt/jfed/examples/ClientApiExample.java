package be.iminds.ilabt.jfed.examples;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractGeniAggregateManager;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.ProtogeniSliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnection;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnectionPool;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaSslConnection;
import be.iminds.ilabt.jfed.util.IOUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClientApiExample {
    public static void main(String[] args) throws Exception {
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Logger logger = new Logger();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            File defaultPemKeyCertFile = new File(System.getProperty("user.home")+ File.separator+".ssl"+File.separator+"geni_cert.pem");
            String pemKeyCertFilename = IOUtils.askCommandLineInput("PEM key and certificate filename (default: \"" + defaultPemKeyCertFile.getPath() + "\")");
            char[] pass = IOUtils.askCommandLinePassword("Key password (if any)");
            if (pemKeyCertFilename == null || pemKeyCertFilename.equals(""))
                pemKeyCertFilename = defaultPemKeyCertFile.getPath();

            ////////////////////////////////////////// Get target test authority ///////////////////////////////////////

            SfaAuthority wall = JFedAuthorityList.getAuthorityListModel().getByUrn("urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");

            //////////////////////////////////////////// Setup user ////////////////////////////////////////////////////

            SimpleGeniUser user = new SimpleGeniUser(null, null, IOUtils.fileToString(pemKeyCertFilename), (pass.length == 0) ? null : pass, defaultPemKeyCertFile, defaultPemKeyCertFile);

            ///////////////////////////////////////////// Call AMv3 GetVersion /////////////////////////////////////////

            AggregateManager3 am3 = new AggregateManager3(logger);

            URL wallAm3Url = wall.getUrl(ServerType.GeniServerRole.AM, 3);
            SfaSslConnection am3Con = new SfaSslConnection(wall, wallAm3Url.toExternalForm(),
                    user.getClientCertificateChain(), user.getPrivateKey(), user,
                    null/*ProxyInfo*/, false, null/*handleUntrustedCallback*/);

            AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager3.VersionInfo> amGetVersionReply = am3.getVersion(am3Con);
            if (!amGetVersionReply.getGeniResponseCode().isSuccess()) {
                System.err.println("GetVersion failed");
                System.exit(-1);
            }
            AggregateManager3.VersionInfo versionInfo = amGetVersionReply.getValue();
            System.out.println("\n\nServer supports single_allocation:" + versionInfo.isSingleSliceAllocation()+"\n");


            ///////////////////// Get a credential from the SA. This time use a ConnectionPool /////////////////////////

            ProtogeniSliceAuthority sa = new ProtogeniSliceAuthority(logger);

            SfaConnectionPool conPool = new SfaConnectionPool();
            SfaConnection saCon = (SfaConnection) conPool.getConnectionByAuthority(user, wall, new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));

            ProtogeniSliceAuthority.SliceAuthorityReply<AnyCredential> saReply = sa.getCredential(saCon);
            AnyCredential cred = saReply.getValue();
            System.out.println("\n\ncredential:\n"+cred.getCredentialXml().substring(0, cred.getCredentialXml().length() > 300 ? 300 : cred.getCredentialXml().length())+"...\n");

            //////////////////////////////////////////// Call AMv3 ListResources ///////////////////////////////////////

            List<AnyCredential> credList = new ArrayList<AnyCredential>();
            credList.add(cred);
            AbstractGeniAggregateManager.AggregateManagerReply<String> listResourcesReply = am3.listResources(
                    am3Con, credList, "geni", "3", true/*available*/, true /*compressed*/, null/*extra options*/);
            if (listResourcesReply.getGeniResponseCode().isSuccess())
                System.out.println("\n\nAdvertisement RSpec: "+listResourcesReply.getValue());
            else
                System.out.println("\n\nListResources failed: "+listResourcesReply.getGeniResponseCode()+" output="+listResourcesReply.getOutput());

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
    }
}
