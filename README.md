# HandRider_UniBus
 HandRider is a real-time tracking mobile application mainly for Beirut Arab University (BAU) buses with set of features to help reduce the hassle of students' daily transportation. Once drivers install our app, passengers - BAU students - can track buses visually on the map wherever and whenever they want. Besides, users will be notified if the bus is full or if it has already left, and the estimated time of the next bus arrival will be shown. The app also provides the ability to notify the driver of the location of passengers waiting outside the checkpoint on the map. In addition, users are able to review and rate the service, the bus driver, and the bus conditions.
###### NOTE: This is the driver part of the project. The passenger app in done in IOS.
  
 ### The Bus Application
  With the help of the bus application, BAU students will be able locate the moving buses on a real-time map, measure the time and distance needed for the bus to arrive to their waiting point and to the destination university, and much more. In order to achieve the previous goals, the bus application should be able to:
* Detect the bus departure and arrival.
* Track bus movement during its trip.
* Be notified about and view passengers waiting anywhere along the road.

#### Google Maps API
 Google maps API provides us with so many features that seem impossible to implement individually. These features include, but not limited to, measuring the estimated time and distance needed to drive or walk from one location to another, getting current location updates every period of time (tracking), and figuring out the exact directions between multiple locations. One more magnificent Google maps API feature is Geofencing. Using geofencing, buses departure and arrival can be automatically detected.
 
#### Using Mobile Phones While Driving!
 It is claimed that it is not recommended to interact with mobile phones and applications while driving. It is even banned in some laws, and whoever does so may be legally liable and may get punished. In fact, in UK, Bus drivers could face fines from £1,000 and £2,500 if taken to court (source http://www.liverpoolecho.co.uk/news/liverpool-news/new-laws-using-your-phone-12666820). But what if the driver has full control of the application hands-free? This will even let the driver command the application and interact with it while driving without touching the smart phone. Therefore, we integrated another great feature which is voice commands. In the background, speech recognition works as following, the voice commands will be streamed to Google servers. On the servers, voice will be converted to text using some speech recognition algorithms. Finally, text will be sent back to our app. So, for example, if “start driving” is commanded, it will simulate clicking the bottom button with a car steering wheel. “Where am I” command will set the map camera on your current location. And so on. Check voice commands demo: https://www.dropbox.com/s/e2btd3zq80qousv/voice%20commands.mp4?dl=0
 
 #### Privacy and Security
 To protect the system's integrity and the user's privacy, Android runs each app in a limited access sandbox. If the app wants to use resources or information outside of its sandbox, the app has to explicitly request permission. Depending on the type of permission the app requests, the system may grant the permission automatically, or the system may ask the user to grant the permission. Android Marshmallow 6.0 introduces the idea of run-time permissions. Users will be asked to grant the permission only when it is needed. This, of course, significantly increases the security. Otherwise, developers can exploit permanently granted permissions and do malicious behavior in the background without any acknowledgement of the user (Exploiting Android granted permissions has been demonstrated in another project, and solutions have been provided. Check the project: https://github.com/ObeidaElJundi/GPA). HandRider Android bus application is developed taking all these sensitive issues into consideration.
 
 #### DEMO (video)
  https://www.dropbox.com/s/liiso8es4odm7wa/HandRider%20Bus.mp4?dl=0
  
  #### DEMO (images) 
  1. This will be the first interface after opening the application. The map is automatically scaled to show both Beirut campus and Debbieh campus. A marker, BAU icon, has been set on each campus.
![image](https://user-images.githubusercontent.com/9033365/27075071-9939a0e0-5031-11e7-974d-364d302e44cc.png)
  
  2. Before the driver starts driving from his station, he should click on the lower button, the button with a car steering wheel, and specify when he is leaving: now, after some time, or at specific time.

![image](https://user-images.githubusercontent.com/9033365/27075158-e0d14a48-5031-11e7-86d1-4d63fbd4922f.png)
  
  3. If **AFTER** or **AT** is checked, you need to specify the exact time by scrolling the hours and minutes wheels. If leaving now, just check **NOW**. Finally, click **LET’S GO**.

![image](https://user-images.githubusercontent.com/9033365/27075185-0125a74e-5032-11e7-8042-95b455ef4bfd.png)
  
  4. After pressing **LET’S GO**, the dialog will be dismissed, a new record will be added to the database, and the user interface (UI) will be updates as illustrated:
    * Bus status at the top.
    * Trajectory on map in blue.
    * The current location of the bus as orange bus icon.
    * Remaining time and distance (updated each 2 second).

![image](https://user-images.githubusercontent.com/9033365/27075214-246a7bc6-5032-11e7-9d6b-5d5c9d23241e.png)
  
  5. Notice how the status is updated when the driver leaves the station (exits the geofence). Notice that the remaining time and distance are updated, too.

![image](https://user-images.githubusercontent.com/9033365/27075259-4c44423a-5032-11e7-89fa-85bd0c90492f.png)
  
  6. When a passenger requests a ride telling he is waiting somewhere in the middle of the trip, in Khalde for example, the driver will be notified (see notification represented by orange passenger icon on the top left), and a marker, orange passenger icon, will be shown on the exact location of the waiting passenger.

![image](https://user-images.githubusercontent.com/9033365/27075328-7792a1ca-5032-11e7-95f5-12562d02dd3c.png)
  
  7. When the driver gets close to the waiting passenger (detected by geofencing), a reminder will be fired, and the map will zoom to show the bus and the passenger. After picking the passenger, its marker will be removed.

![image](https://user-images.githubusercontent.com/9033365/27075368-8d8d4e6c-5032-11e7-9123-06da133d7d87.png)

![image](https://user-images.githubusercontent.com/9033365/27075391-a590a324-5032-11e7-9556-a9187cbd3110.png)
