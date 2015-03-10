# Routines
A Toast Groovy Script for Tracking of Autonomous Routines

## What is Routines  
Routines is a Groovy Script built from the [Toast API](http://github.com/Open-RIO/ToastAPI) that allows teams to record and playback autonomous routines. This avoids the pain of hard-coding values, testing, tweaking, repeat. Routines are saved in the autonomous/ directory and can be edited manually if you so desire.

## How to Use  
Routines is extremely simple to use. There are many ways to access Routines from your own code  

### Groovy
If you want to access Routines from your own Groovy script, it's very simple.  
In Groovy you can use the Routines object directly. It can be done as follows:
``` groovy
  Routines routines = Routines.getRoutines()
```
From here you can invoke methods on the Routines object directly

To get the AutonomousContext, do the following:
``` groovy
  def context = routines.getContext("${routine name}")
```
### Java
To access Routines from Java, it is also very simple. There are 2 ways to do this:
  - Groovy Access  
    To access the Routines object from the Groovy Loader, do the following
    ``` java
      GroovyObject routines = GroovyLoader.getObject("Routines");
    ```
     From here you can get the AutonomousContext, as so:  
     ``` java
      GroovyObject context = (GroovyObject) routines.invokeMethod("getContext", "{routine name}");
     ```
     From here you can invoke methods on the Context, as described later  

  - Module Events  
    Methods can also be triggered on the ModuleEventBus to make the API easier to use. Access the methods as follows:  
    ``` java
      ModuleEventBus.raiseEvent("{your module name}", "routines_{method}", "{routine name}", "{other data}");
    ```

### What methods to use
The following are methods to invoke on the Routines API to get it to work.
- Routines
  - stopAll() - Stops all Contexts playback
  - getContext(id) - Get an Autonomous Context with the Given ID
- AutonomousContext
  - setControllers(SpeedController... controllers) - Set the motors for the Context to record and playback on. Should be set before and after playback (Make sure they are ordered correctly!)
  - startRecording() - Begin recording a routine. This will automatically stop after 15 seconds
  - stopRecording() - Manually stop the routine from recording
  - startPlayback() - Playback the routine until it stops
  - stopPlayback() - Manually stop playback

### Demos  
  JAVA:
  ``` java
    ModuleEventBus.raiseEvent("OpenRIO", "routines_setControllers", "demoRoutine", myMotors);
    ModuleEventBus.raiseEvent("OpenRIO", "routines_startRecording", "demoRoutine");
  ```
  ``` java
    ModuleEventBus.raiseEvent("OpenRIO", "routines_setControllers", "demoRoutine", myMotors);
    ModuleEventBus.raiseEvent("OpenRIO", "routines_startPlayback", "demoRoutine");
  ```
  ``` java
    ModuleEventBus.raiseEvent("OpenRIO", "routines_stopAll");
  ```
  GROOVY:
  ``` groovy
    Routines routines = Routines.getRoutines()
    def context = routines.getContext("demoRoutine")
    context.setControllers(myMotors)
    context.startRecording()
  ```
  ``` groovy
    Routines routines = Routines.getRoutines()
    def context = routines.getContext("demoRoutine")
    context.setControllers(myMotors)
    context.startPlayback()
  ```
  ``` groovy
    Routines routines = Routines.getRoutines()
    routines.stopAll()
  ```
