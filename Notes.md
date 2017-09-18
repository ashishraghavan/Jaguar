If there was no bi-directional relationship between User and Device, deleting a user which is associated to multiple devices will throw an error. On having a bi-directional relationship, on deleting a user, all devices associated to this user will also be deleted. This probably behaves as a parent-child relationship, where the parent is the User and the child is the device. When you delete the parent (the User), the child (the device associated to the user) is also deleted, which is desirable since a device which does not belong to a user does not serve any purpose.

When not using the jersey-spring3 integration dependency, the setter method with @Autowired annotation is not called when a service call is made (annotated with @Component). On adding the jersey-spring3 dependency and excluding a bunch of dependencies causing error, the setter method for dao is called twice in the service class (ApplicationService.java). The first one when the server container is initiliazed, which is normal as the setter injection is used by spring. However, on calling any API (service call), this is one of the major difference : The ApplicationService as initialized by Spring and later on by jersey have different hashcodes. I don't know why would this happen. And if the class has already been loaded once, why would there be a need for Jersey and Spring to load these classes separately? I can understand if the classloader being used for this is different.

For running the build.
mvn -U -X -DargLine="-DDB_SERVER=localhost -DDB_PORT=5432 -DDB_USER=jaguar -DDB_PASSWORD=jaguar -DDB_NAME=jaguar -DDB_MAX_POOL_SIZE=15" clean install

Only for verification on the mobile application side.
curl -v -X POST -F "time=1504844512068" -F "client_id=1095369" -F "hash=YwUU8FwVNCquE07ldb2nlXV7fiBWcU8AidvVoRZCk0A=" http://localhost:8080/api/apps/verify

Format of cookie.
jaguar_cookie=619fb794-1f04-440c-9bbf-547a6918a4cb;Version=1;Comment="cookie for creating app session";Domain=;Path=apps/verify;Max-Age=100

{"account":{"accountName":"Jaguar","city":"Long Is City","country":"USA","state":"NY","postalCode":"11101","creationDate":"Aug 28, 2017 10:24:24 PM","modificationDate":"Aug 28, 2017 10:24:24 PM","active":true,"id":1},"name":"AppSense","redirectUri":"http://localhost:8080/api/client","clientId":1903475,"clientSecret":"7a5e9fc6-290b-4c97-8386-67237414f469","versionCode":"1.0","packageName":"com.jaguar.jaguarxf","applicationType":"MOBILE_APP","applicationRoles":[],"creationDate":"Aug 28, 2017 10:24:24 PM","modificationDate":"Aug 28, 2017 10:24:24 PM","active":true,"id":1}

Request authorization code
curl -v -L "http://localhost:8080/api/oauth/authorize?response_type=json&client_id=1095369&redirect_uri=http://localhost:8080&scope=seller&device_uid=GOOGLECHROME"

curl -v -X POST --cookie "jaguar_cookie=a17870b6-4d8e-4097-a473-a9aa04f19135" -F "username=ashish.raghavan@google.com" -F "password=12345" -F "device_uid=GOOGLECHROME" -F "auth_flow=true" -F "redirect_uri=http://localhost:8080/api/client" -F "client_id=1095369" "http://localhost:8080/api/authorize/login"

curl -L -v -X POST -F "redirect_uri=http://localhost:8080" -F "authorization_code=3d15883b-5df6-4086-8bef-1701447eb4a5" -F "authorization=AGREE" -F "client_id=1095369" http://localhost:8080/api/oauth/token

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

For sending emails, we are using MailGun, with the username as 'api' and password as 'key-910ae7b7d0722488c0951dfce679fe76'. This is the basic authentication scheme. The domain name used is 'sandbox263d8e2e3daf4b92bbd0cff99aa5cdd3.mailgun.org', which is the sandbox domain for my free account.

API key for MailGun = key-910ae7b7d0722488c0951dfce679fe76
