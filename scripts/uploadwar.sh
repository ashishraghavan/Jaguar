#!/bin/bash
sftp -i ~/Production/google-cloud-platform/jaguar_google_cloud_platform ashishhraghavan13687@35.196.113.4 << EOF
#echo "Uploading the test _config.yml to the Miscllaneous directory"
#now upload the client.war from source to target folder.
put /Users/ashishraghavan/Production/Jaguar/client-api/target/client.war /home/ashishhraghavan13687/WarFiles/
#echo "Finished uploading the test file _config.yml to the Miscllaneous directory"
#exit from sftp and gain control of our shell.
#echo "Exiting from the shell"
quit
EOF
#Connect using ssh, do a sudo su, copy the same file from Miscllaneous to /var/lib/tomcat8/webapps
#echo "Connecting back using ssh as the user ashishhraghavan13687"
ssh -i ~/Production/google-cloud-platform/jaguar_google_cloud_platform ashishhraghavan13687@35.196.113.4 << EOF
#echo "Loggin in as the root user"
sudo su
#echo "Copying the uploaded file through SFTP to the tomcat8 webapps folder."
cp /home/ashishhraghavan13687/WarFiles/client.war /var/lib/tomcat8/webapps/
#exit as root user
#echo "Exiting as the root user"
exit
#delete the original file in ~/Miscllaneous to save space
#echo "Deleting the original war/test file to save space"
rm -rf /home/ashishhraghavan13687/WarFiles/client.war
#echo "Exiting SSH session"
exit
EOF

