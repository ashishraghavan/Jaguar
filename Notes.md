If there was no bi-directional relationship between User and Device, deleting a user which is associated to multiple devices will throw an error. On having a bi-directional relationship, on deleting a user, all devices associated to this user will also be deleted. This probably behaves as a parent-child relationship, where the parent is the User and the child is the device. When you delete the parent (the User), the child (the device associated to the user) is also deleted, which is desirable since a device which does not belong to a user does not serve any purpose.

When not using the jersey-spring3 integration dependency, the setter method with @Autowired annotation is not called when a service call is made (annotated with @Component). On adding the jersey-spring3 dependency and excluding a bunch of dependencies causing error, the setter method for dao is called twice in the service class (ApplicationService.java). The first one when the server container is initiliazed, which is normal as the setter injection is used by spring. However, on calling any API (service call), this is one of the major difference : The ApplicationService as initialized by Spring and later on by jersey have different hashcodes. I don't know why would this happen. And if the class has already been loaded once, why would there be a need for Jersey and Spring to load these classes separately? I can understand if the classloader being used for this is different.

Verification API
curl -v -X POST -F "time=1503372933614" -F "client_id=2190832" -F "hash=H8OA4lOY6VikRxeNbGj9K3amnm1r6zIx7pdBmEl+Sd8=" http://localhost:8080/api/apps/verify

HTTP/1.1 100 Continue
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: jaguar_cookie=d4262019-cb52-4630-9906-bc9f585a855c;Version=1;Comment="cookie for creating app session";Domain=;Path=apps/verify;Max-Age=100
Content-Type: text/plain
Content-Length: 571
Date: Tue, 22 Aug 2017 04:27:15 GMT

{
   "account":{
      "accountName":"Jaguar",
      "city":"Long Is City",
      "country":"USA",
      "state":"NY",
      "postalCode":"11101",
      "creationDate":"Aug 15, 2017 6:15:44 PM",
      "modificationDate":"Aug 15, 2017 6:15:44 PM",
      "active":true,
      "id":1
   },
   "name":"AppSense",
   "redirectUri":"http://localhost:8080/api/client",
   "clientId":2190832,
   "clientSecret":"5d4b8308-525f-4631-b6ea-55d926a08be3",
   "versionCode":"1.0",
   "packageName":"com.jaguar.jaguarxf",
   "applicationType":"MOBILE_APP",
   "applicationRoles":[

   ],
   "creationDate":"Aug 15, 2017 6:15:44 PM",
   "modificationDate":"Aug 15, 2017 6:15:44 PM",
   "active":true,
   "id":1
}