If there was no bi-directional relationship between User and Device, deleting a user which is associated to multiple devices will throw an error. On having a bi-directional relationship, on deleting a user, all devices associated to this user will also be deleted. This probably behaves as a parent-child relationship, where the parent is the User and the child is the device. When you delete the parent (the User), the child (the device associated to the user) is also deleted, which is desirable since a device which does not belong to a user does not serve any purpose.

When not using the jersey-spring3 integration dependency, the setter method with @Autowired annotation is not called when a service call is made (annotated with @Component). On adding the jersey-spring3 dependency and excluding a bunch of dependencies causing error, the setter method for dao is called twice in the service class (ApplicationService.java). The first one when the server container is initiliazed, which is normal as the setter injection is used by spring. However, on calling any API (service call), this is one of the major difference : The ApplicationService as initialized by Spring and later on by jersey have different hashcodes. I don't know why would this happen. And if the class has already been loaded once, why would there be a need for Jersey and Spring to load these classes separately? I can understand if the classloader being used for this is different.

Make sure to annotate all methods with the appropriate HTTP methods. (@GET,@POST,@PUT) etc. When accessing the link through the browser (from an email maybe), it doesn't correctly return the response.

When you use Cascade=CaseType.ALL, if a device is deleted, all its associated relationships will also be deleted. This may or may not be an advantage.

#################################################################################
For rabbitmq, available commands can be found under /usr/local/Cellar/rabbit


docker run -d -p 1401:1401 -p 2775:2775 -p 8990:8990 --name jasmin_01 jookies/jasmin:latest
docker run -d -p 1411:1401 -p 2785:2775 -p 9000:8990 --name jasmin_02 jookies/jasmin:latest
docker run -d -p 1421:1401 -p 2795:2775 -p 9010:8990 --name jasmin_03 jookies/jasmin:latest
docker run -d -p 1431:1401 -p 2805:2775 -p 9020:8990 --name jasmin_04 jookies/jasmin:latest

docker run -d -v ~/jasmin_logs:/var/log/jasmin --name jasmin_100 jookies/jasmin:latest

Adding a new connector
smppccm -a

Host  Host of remote SMS-C  172.16.10.67
Port  SMPP port on remote SMS-C 2775
Username  Authentication username smppclient1
Password  Authentication password password
Throughput  Maximum sent SMS/second 110

Twilio Phone number = (408) 763-8449
Twilio Account Password = H9Uw3Ctzy9D1iQt

PLivo username/password
ashishraghavan13687@gmail.com/H9Uw3Ctzy9D1iQt

TEST ACCOUNT SID
AC37e50df423ed5f422f24c5b71d3a7819

TEST AUTHTOKEN
b5f485867cdc3fe5770f9a1c2460fb4b

Production ACCOUNT SID
ACb99b9a5e0a29d2d869a522225b122783

Production AUTHTOKEN
23a7e397c442426e8501dbeb972afab6

curl 'https://api.twilio.com/2010-04-01/Accounts/ACb99b9a5e0a29d2d869a522225b122783/Messages.json' -X POST \
--data-urlencode 'To=+14082216275' \
--data-urlencode 'From=+14087638449' \
-u ACb99b9a5e0a29d2d869a522225b122783:23a7e397c442426e8501dbeb972afab6

Twilio doesnt seem to work/is expensive (probably)

PLivo Details
AUTH ID = MAMWQWMGU1ZDY5YJZJMM
AUTH TOKEN = M2E3N2IwY2YxNzBhNGQ5NmUzYTJjMTVlNjBhNzAw
source number = +1 917-993-5471

Response after sending an SMS.
MessageResponse [serverCode=202, message=message(s) queued, messageUuids=[4b08bffa-b9d1-11e7-b886-067c5485c240], error=null, apiId=4a74e2a8-b9d1-11e7-b886-067c5485c240]

################################################################################

For running the build.
To skip tests : -Dmaven.test.skip=true=true
mvn -U -X -DargLine="-DDB_SERVER=localhost -DDB_PORT=5432 -DDB_USER=jaguar -DDB_PASSWORD=jaguar -DDB_NAME=jaguar -DDB_MAX_POOL_SIZE=15"  clean install

DB_SERVER=localhost
DB_PORT=5432
DB_USER=jaguar
DB_PASSWORD=jaguar
DB_NAME=jaguar
DB_MAX_POOL_SIZE=15

Only for verification on the mobile application side.
curl -v -X POST -F "time=1504844512068" -F "client_id=1095369" -F "hash=YwUU8FwVNCquE07ldb2nlXV7fiBWcU8AidvVoRZCk0A=" http://localhost:8080/api/apps/verify

Format of cookie.
jaguar_cookie=619fb794-1f04-440c-9bbf-547a6918a4cb;Version=1;Comment="cookie for creating app session";Domain=;Path=apps/verify;Max-Age=100

{"account":{"accountName":"Jaguar","city":"Long Is City","country":"USA","state":"NY","postalCode":"11101","creationDate":"Aug 28, 2017 10:24:24 PM","modificationDate":"Aug 28, 2017 10:24:24 PM","active":true,"id":1},"name":"AppSense","redirectUri":"http://localhost:8080/api/client","clientId":1903475,"clientSecret":"7a5e9fc6-290b-4c97-8386-67237414f469","versionCode":"1.0","packageName":"com.jaguar.jaguarxf","applicationType":"MOBILE_APP","applicationRoles":[],"creationDate":"Aug 28, 2017 10:24:24 PM","modificationDate":"Aug 28, 2017 10:24:24 PM","active":true,"id":1}

Register a user and device.
curl -v -X POST -F "username=ashishraghavan13687@gmail.com" -F "password=12345" -F "role=seller" -F "first_name=Ashish" -F "last_name=Raghavan" -F "phone=4082216275" -F "device_uid=iOS6sPlus-A1687" -F "model=iPhone6sPlus" -F "client_id=1095369" -F "api=15" -F "role=seller" "http://localhost:8080/client/api/user"

Register same user with different device
curl -v -X POST -F "username=ashishraghavan13687@gmail.com" -F "password=12345" -F "role=seller" -F "first_name=Ashish" -F "last_name=Raghavan" -F "phone=4082216275" -F "device_uid=Nexus6-XT2048" -F "model=Nexus 6" -F "client_id=1095369" -F "api=17" "http://localhost:8080/client/api/user"

Resend verification link
curl -v "http://localhost:8080/client/api/user/resendlink?email=ashishraghavan13687@gmail.com&device_uid=iOS6sPlus-A1687&client_id=1095369&role=seller"

Request authorization code
curl -v -L "http://localhost:8080/client/api/oauth/authorize?response_type=json&client_id=1095369&redirect_uri=http://localhost:8080/client/api/files/redirection.html&scope=seller&device_uid=iOS6sPlus-A1687"

curl -v -L "https://dev-jaguar.com/client/api/oauth/authorize?response_type=json&client_id=1095369&redirect_uri=http://localhost:8080&scope=seller&device_uid=iOS6sPlus-A1687"

curl -v -L "https://ashishraghavan.me/client/api/oauth/authorize?response_type=json&client_id=1095369&redirect_uri=http://localhost:8080&scope=seller&device_uid=GOOGLECHROME"

Login
curl -v -X POST -F "username=ashishraghavan13687@gmail.com" -F "password=12345" -F "device_uid=iOS6sPlus-A1687" -F "auth_flow=false" -F "redirect_uri=http://localhost:8080/api/client" -F "client_id=1095369" "http://localhost:8080/client/api/login"

curl -v -k -X POST -F "username=ashishraghavan13687@gmail.com" -F "password=12345" -F "device_uid=iOS6sPlus-A1687" -F "auth_flow=false" -F "redirect_uri=http://localhost:8080/api/client" -F "client_id=1095369" "https://dev-jaguar.com/client/api/login"

Login using a different device (should trigger a device creation on the server side)
curl -v -X POST -F "username=ashishraghavan13687@gmail.com" -F "password=12345" -F "device_uid=Nexus6p-XT1107" -F "auth_flow=false" -F "redirect_uri=http://localhost:8080/api/client" -F "client_id=1095369" -F "model=Nexus" -F "api=17" "http://localhost:8080/client/api/login"


Get the auth token
curl -L -v -X POST -F "authorization_code=596c1397-0cb4-453b-8140-453a17f136b4" -F "client_id=1095369" http://localhost:8080/client/api/oauth/token

{	
  "access_token" : "40d6b613-e77a-4911-858a-64cf2f02cb63",
  "refresh_token" : "261316dd-bfe2-4344-a5a4-1d8f5e3fe919",
  "user" : {
    "creationDate" : 1503973464581,
    "modificationDate" : 1503973464581,
    "active" : true,
    "id" : 1,
    "name" : "Ashish Raghavan",
    "firstName" : "Ashish",
    "lastName" : "Raghavan",
    "password" : "9584bd5fad8be2416f26491bc6a7331acbc0e7f3f03bbd0ec4d714042d938dd6",
    "account" : {
      "creationDate" : 1503973464407,
      "modificationDate" : 1503973464407,
      "active" : true,
      "id" : 1,
      "accountName" : "Jaguar",
      "city" : "Long Is City",
      "country" : "USA",
      "state" : "NY",
      "postalCode" : "11101"
    },
    "email" : "ashish.raghavan@google.com",
    "lastOnline" : null,
    "phoneNumber" : "4082216275"
  }
}

//Get the User details.
curl -v -k -H "Authorization:Bearer 544a70e8-4a0f-4b3c-9304-0187a96bcbed" "http://localhost:8080/client/api/user/ashishraghavan13687@gmail.com"

For sending emails, we are using MailGun, with the username as 'api' and password as 'key-910ae7b7d0722488c0951dfce679fe76'. This is the basic authentication scheme. The domain name used is 'postmaster@ashishraghavan.me', which is the sandbox domain for my free account.

API key for MailGun = key-910ae7b7d0722488c0951dfce679fe76

The email for Jaguar Development is : jaguardevelopmental@gmail.com
scopes : https://mail.google.com/
The "Allow less secure apps" setting has been turned on for the javax.mail client to work correctly.