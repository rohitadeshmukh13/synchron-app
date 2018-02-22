--------------------------- Synchron App ---------------------------

The App:
- The app was built using the Spring Boot framework
- The app uses Maven to manage dependencies

Directory/File Structure and Contents:
- src/main/java contains the java source
- src/main/resources contains the files containing semantic data that is used as input by the app
  - src/main/resources/Data: The semantic data sets to be consumed by the app can be placed in this folder
  - src/main/resources/Vocabulary: The vocabularies needed by the data in src/main/resources/Data can be placed in this folder
- src/main/webapp contains the source for the frontend webapp that uses AngularJS
- screenshots folder contains the application screenshots
- Task.pdf (and also docs/Task-Challenges.txt) describes the challenges to be implemented by the app. These are the expected functionalities of the app
- docs/Task-Solution.txt describes the solution implemented by the task to solve the challenges described in docs/Task-Challenges.txt file
- docs/samples.txt contains request and responses for a few sample API calls to the Synchron App and to the FIWARE IoT Broker

Prerequisites:
1. Java 1.8 or higher
2. Apache Maven 3.5.0 or higher
3. FIWARE IoT Broker running on [http://localhost:8060] and all of its dependencies (HSQLDB, NEConfMan, PostgreSQL, CouchDB, NECIoTKnowledge, Fuseki, etc as described in docs/Task-Challenges.txt)

Startup:
1. Import the 'synchron-app' Project as a Maven project in Spring Tool Suite IDE
2. Wait for Maven to resolve the dependencies
3. Run (By default, the app runs on [http://localhost:8080])

API:
- Endpoints
HTTP POST http://localhost:8080/createEntity
HTTP POST http://localhost:8080/getSensorData
HTTP GET http://localhost:8080/getSensors
- Samples in docs/samples.txt

Frontend/UI:
- Uses AngularJS, Bootstrap 4 internally
- The [http://localhost:8080/getSensorData] endpoint can be accessed using frontend at [http://localhost:8080/sensorData.html]
- On entering Sensor Id, Lower Timestamp, Higher Timestamp; the sensor data is shown on the same page below the 'Search' button
