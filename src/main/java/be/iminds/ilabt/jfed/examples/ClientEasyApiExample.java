package be.iminds.ilabt.jfed.examples;

import be.iminds.ilabt.jfed.highlevel.api.EasyAggregateManager3;
import be.iminds.ilabt.jfed.highlevel.api.EasySliceAuthority;
import be.iminds.ilabt.jfed.highlevel.controller.HighLevelController;
import be.iminds.ilabt.jfed.highlevel.model.*;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractGeniAggregateManager;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.ProtogeniSliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnection;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnectionPool;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.UserLoginModelManager;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.JavaFXLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClientEasyApiExample {
    public static void main(String[] args) throws Exception {
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            AppModel appModel = new BasicAppModel();
            UserLoginModelManager userLoginModel = (UserLoginModelManager) appModel.getGeniUserProvider(); //TODO this needs improvement

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            File defaultPemKeyCertFile = new File(System.getProperty("user.home")+ File.separator+".ssl"+File.separator+"geni_cert.pem");
            String pemKeyCertFilename = IOUtils.askCommandLineInput("PEM key and certificate filename (default: \"" + defaultPemKeyCertFile.getPath() + "\")");
            char[] pass = IOUtils.askCommandLinePassword("Key password (if any)");
            if (pemKeyCertFilename == null || pemKeyCertFilename.equals(""))
                pemKeyCertFilename = defaultPemKeyCertFile.getPath();
            userLoginModel.setUserLoginModelType(UserLoginModelManager.UserLoginModelType.KEY_CERT_INTERNAL_INFO);
            userLoginModel.getKeyCertUserLoginModel().setKeyCertPemFile(new File(pemKeyCertFilename));
            userLoginModel.getKeyCertUserLoginModel().unlock(pass);
            userLoginModel.login();

            ////////////////////////////////////////// Get target test authority ///////////////////////////////////////

            AuthorityInfo wall = appModel.getAuthorityList().getByUrn("urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");

            ///////////////////////////////////////////// Call AMv3 GetVersion /////////////////////////////////////////

            EasyAggregateManager3 am3 = new EasyAggregateManager3(appModel, wall);
            AggregateManager3.VersionInfo versionInfo = am3.getVersion();
            System.out.println("\n\nServer supports single_allocation:" + versionInfo.isSingleSliceAllocation()+"\n");

            /////////////////////////////////////////// Get a credential from the SA. //////////////////////////////////

            EasySliceAuthority sa = new EasySliceAuthority(appModel, wall);
            sa.getCredential();

            //////////////////////////////////////////// Call AMv3 ListResources ///////////////////////////////////////

            RSpecInfo advertisementRpsec = am3.listResources(true);
            System.out.println("\n\nAdvertisement RSpec: "+advertisementRpsec.getStringContent());

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            System.exit(0);
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
