# Routines
A Toast Module for Tracking of Autonomous Routines

## What does it do
Routines is a simple Toast Module that will record each of the Motors on your Robot during an autonomous period, allowing you to record and playback your autonomous configuration.

## Commands
`rdisable` - Disable Routines from running on Autonomous Started.  
`rset <name>` - Set the default routine to run on Autonomous Started.  
`rrecord [name]` - Begin recording a routine. This will give a 5 second countdown.  
`rplay [name]` - Playback a prerecorded routine.  

## Installation
To add Routines as a dependency for your Module, place the following in your `build.gradle`
```gradle
dependencies {
    compile group: 'jaci.openrio.modules', name: 'Routines', version: '+'
    toastModule group: 'jaci.openrio.modules', name: 'Routines', version: '+'
}
```

Next time you run `gradlew eclipse` or `gradlew idea`, the Routines Jar will be added as a dependency to your project.  
Next time you run `gradlew deploy`, Routines will be loaded onto your RoboRIO. 

If you wish to install Routines onto a USB Stick instead, remove the line in the above example starting with `toastModule` and instead download
the Routines jar from the [Releases Page](https://github.com/Open-RIO/Routines/releases) and copy it to the `toast/modules` directory on your USB device.