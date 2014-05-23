package be.iminds.ilabt.jfed.examples;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.AnyCredential;
import be.iminds.ilabt.jfed.lowlevel.JFedException;
import be.iminds.ilabt.jfed.lowlevel.SimpleGeniUser;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.AggregateManagerWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.UserAndSliceApiWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.impl.AutomaticAggregateManagerWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.impl.AutomaticUserAndSliceApiWrapper;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnectionPool;
import be.iminds.ilabt.jfed.util.IOUtils;

import java.io.File;

public class ClientWrapperApiExample {
    public static void main(String[] args) throws Exception {
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Logger logger = new Logger();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            File defaultPemKeyCertFile =
                    new File(System.getProperty("user.home") + File.separator + ".ssl" + File.separator + "geni_cert.pem");
            String pemKeyCertFilename =
                    IOUtils.askCommandLineInput("PEM key and certificate filename (default: \"" + defaultPemKeyCertFile.getPath() + "\")");
            char[] pass = IOUtils.askCommandLinePassword("Key password (if any)");
            if (pemKeyCertFilename == null || pemKeyCertFilename.equals(""))
                pemKeyCertFilename = defaultPemKeyCertFile.getPath();

            ////////////////////////////////////////// Get target test authority ///////////////////////////////////////

            SfaAuthority wall =
                    JFedAuthorityList.getAuthorityListModel().getByUrn("urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");

            //////////////////////////////////////////// Setup user ////////////////////////////////////////////////////

            SimpleGeniUser user = new SimpleGeniUser(null, null, IOUtils.fileToString(pemKeyCertFilename),
                    (pass.length == 0) ? null : pass, defaultPemKeyCertFile, defaultPemKeyCertFile);

            ///////////////////////////////////////////// Call any GetVersion /////////////////////////////////////////

            SfaConnectionPool conPool = new SfaConnectionPool();
            AggregateManagerWrapper amWrapper = new AutomaticAggregateManagerWrapper(logger, user, conPool, wall);
            UserAndSliceApiWrapper credWrapper = new AutomaticUserAndSliceApiWrapper(logger, user, conPool);

            amWrapper.getVersion();

            ///////////////////// Get a user credential. /////////////////////////

            AnyCredential cred = credWrapper.getUserCredentials(user.getUserUrn());
            System.out.println("\n\ncredential:\n" + cred.getCredentialXml().substring(0,
                    cred.getCredentialXml().length() > 300 ? 300 : cred.getCredentialXml().length()) + "...\n");

            //////////////////////////////////////////// Call ListResources ///////////////////////////////////////

            String rspec = amWrapper.listResources(cred, true/*available*/);

            if (rspec != null)
                System.out.println("\n\nAdvertisement RSpec: " + rspec);
            else
                System.out.println("\n\nListResources failed.");

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        } catch (JFedException e) {
            System.err.println("JFedException: " + e);
            e.printStackTrace();
            if (e.getXmlRpcResult() != null) {
                System.err.println("HTTP request:\n" + e.getXmlRpcResult().getRequestHttpContent());
                System.err.println("\nHTTP response:\n" + e.getXmlRpcResult().getResultHttpContent());
                System.err.println("\nXMLRPC request:\n" + e.getXmlRpcResult().getRequestXmlRpcString());
                System.err.println("\nXMLRPC response:\n" + e.getXmlRpcResult().getResultXmlRpcString());
            }
        }
    }
}
