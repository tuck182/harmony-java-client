harmony-java-client
===================

Java client for communicating with a Harmony Hub

Based on the work done to reverse engineer and implement the Harmony Hub
protocol in the following projects:
  * https://github.com/jterrace/pyharmony
  * https://github.com/petele/pyharmony
  * https://github.com/hdurdle/harmony

The basics of the API are in place, and there's also a simple shell available
through the Main class that demonstrates the API features. The available shell
commands are:

  * list devices            - lists the configured devices and their id's
  * list activities         - lists the configured activities and their id's
  * show activity           - shows the current activity
  * start <activity>        - starts an activity (takes a string or id)
  * press <device> <button> - perform a single button press
  * get_config              - Dumps the full config json, unformatted 

Example
-------

Run `gradle jar` to build a jar file with dependent librariers in lib. Navigate to lib and run:

`java -classpath harmony-java-client-1.0.jar:lib/* net.whistlingfish.harmony.Main HARMONY_HOST LOGITECH_USERNAME LOGITECH_PASSWORD`
