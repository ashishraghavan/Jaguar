package com.jaguar.test;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;
import org.testng.util.Strings;

import java.io.File;

/**
 * apache webapps location is /var/lib/tomcat8/webapps
 * First remove the client directory present in this folder.
 * Then upload the war file present in target/client.war to this folder.
 * Usage
 * java UploadToGoogleCloudPlatform ~/Production/Jaguar/client-api/target/client.war ~/Production/google-cloud-platform/jaguar_google_cloud_platform
 */
public class UploadToGoogleCloudPlatform {
    private static final Logger uploadLogger = Logger.getLogger(UploadToGoogleCloudPlatform.class.getSimpleName());
    public static void main(String[] args) throws Exception {
        //The first argument is the path to the client.war file or the directory where this file resides.
        //35.196.113.4
        final String hostName = "ashishraghavan.me";
        final String userName = "ashishhraghavan13687";
        if(args == null || args.length <= 0) {
            uploadLogger.error("Incorrect usage for this program, correct usage is "+UploadToGoogleCloudPlatform.class.getSimpleName()+
                    "\"path to client war\"  "+" \"path to identity file\"");
            throw new IllegalArgumentException("Incorrect usage for this program, correct usage is "+UploadToGoogleCloudPlatform.class.getSimpleName()+
                    "\"path to client war\""+"  "+"\"path to identity file\"");
        }

        final String pathToClientWar = args[0];
        if(Strings.isNullOrEmpty(pathToClientWar)) {
            uploadLogger.error("Path to client war was null/empty");
            throw new IllegalArgumentException("Path to client war was null/empty");
        }

        final String pathToIdentityFile = args[1];
        if(Strings.isNullOrEmpty(pathToIdentityFile)) {
            uploadLogger.error("Path to identity file was null/empty");
            throw new IllegalArgumentException("Path to identity file was null/empty");
        }

        //Check if the path to client war is valid and the actual client.war file is correct and has a non null size.
        final File clientWarDirectory = new File(pathToClientWar);
        if(!clientWarDirectory.exists()) {
            uploadLogger.error("The path "+pathToClientWar+" does not exist");
            throw new IllegalArgumentException("The path "+pathToClientWar+" does not exist");
        }
        final File identityFilePath = new File(pathToIdentityFile);
        if(!identityFilePath.exists()) {
            uploadLogger.error("The path "+identityFilePath+" does not exist");
            throw new IllegalArgumentException("The path "+identityFilePath+" does not exist");
        }
        final JSch googleSecureShell = new JSch();
        //Set the identity file.
        final Session session = googleSecureShell.getSession(userName,hostName,22);
        session.setPassword("AShi13**");
        session.connect();
        if(session.isConnected()) {
            uploadLogger.info("Session created and connected");
            session.disconnect();
        }
    }
}
